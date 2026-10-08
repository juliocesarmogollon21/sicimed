#!/usr/bin/env bash
# Prueba de humo de la API SICIMED (Spring Boot + JWT + JPA)
# Uso: bash docs/probar_api.sh [http://localhost:8090]
# Uso: bash docs/probar_api.sh [base]   (base SIN /api)
#   Tomcat + MySQL  : bash docs/probar_api.sh http://localhost:8080/sicimed
#   Standalone (H2) : bash docs/probar_api.sh http://localhost:8090
B="${1:-http://localhost:8090}"
PASS=0; FAIL=0
# curl es un binario nativo de Windows: no puede escribir en rutas /tmp de MSYS.
# cygpath convierte la ruta temporal a formato Windows.
OUT_DIR="$TEMP"; mkdir -p "$OUT_DIR"
OUT=$(cygpath -w "$OUT_DIR/resp.json" 2>/dev/null || echo "$OUT_DIR/resp.json")

req() { # metodo ruta [body] [token]
  local metodo="$1" ruta="$2" body="$3" token="$4"
  case "$ruta" in
    http*) url="$ruta" ;;
    *) url="$B$ruta" ;;
  esac
  local args=(-s -o "$OUT" -w "%{http_code}" -X "$metodo" "$url")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$body" ] && args+=(-H "Content-Type: application/json" -d "$body")
  CODE=$(curl "${args[@]}")
  RESP=$(cat "$OUT_DIR/resp.json" 2>/dev/null)
}

check() { # codigo_esperado etiqueta
  if [ "$CODE" = "$1" ]; then
    printf 'OK   %-3s %s\n' "$CODE" "$2"; PASS=$((PASS+1))
  else
    printf 'FAIL %-3s %s  (esperado %s)\n     %s\n' "$CODE" "$2" "$1" "${RESP:0:220}"; FAIL=$((FAIL+1))
  fi
}

echo "=== Autenticacion ==="
# El backend acepta peticiones antes de que termine de sembrar la base:
# esperamos a que /api/sistema/estado reporte preparado=true.
for i in $(seq 1 60); do
  req GET /api/sistema/estado
  echo "$RESP" | grep -q '"preparado":true' && break
  sleep 1
done
check 200 "backend activo y base de datos preparada"

req POST /api/auth/login '{"username":"admin","password":"admin123"}'
check 200 "login admin valido"
TOKEN=$(echo "$RESP" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

req POST /api/auth/login '{"username":"admin","password":"mala"}'
check 400 "login con clave incorrecta"

req GET /api/citas
check 401 "peticion sin token"

req GET /api/sistema/estado
check 200 "estado del backend es publico"

req GET /api/citas '' "$TOKEN"
check 200 "listado de citas con token"

req GET /api/citas '' "token.invalido.xyz"
check 401 "token invalido"

echo "=== Registro ==="
# Usuario unico por ejecucion: el script debe poder repetirse contra un servidor ya sembrado.
USUARIO="nuevo$RANDOM"
DNI="9$RANDOM$RANDOM"
req POST /api/auth/register "{\"username\":\"$USUARIO\",\"password\":\"clave123\",\"nombreCompleto\":\"Ana Nuevo\",\"email\":\"$USUARIO@correo.com\",\"dni\":\"$DNI\"}" 
check 201 "registro de paciente"
PACTOKEN=$(echo "$RESP" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

req POST /api/auth/register "{\"username\":\"$USUARIO\",\"password\":\"clave123\",\"nombreCompleto\":\"Ana Nuevo\",\"email\":\"$USUARIO@correo.com\",\"dni\":\"$DNI\"}"
check 409 "usuario repetido da 409"

req POST /api/auth/register '{"username":"corto","password":"123","nombreCompleto":"X","email":"no-es-mail","dni":"555"}'
check 400 "validacion de entrada (contrasena corta y email invalido)"

echo; echo "=== Catalogos (admin) ==="
req GET /api/sedes '' "$TOKEN";        check 200 "listar sedes"
req GET /api/especialidades '' "$TOKEN"; check 200 "listar especialidades"
req GET /api/medicamentos '' "$TOKEN";   check 200 "listar medicamentos"
req GET /api/medicos '' "$TOKEN";        check 200 "listar medicos"
# La cita de prueba debe ser del medico de la agenda de medico1 (Dr. Carlos Mendoza),
# porque es el unico que puede diagnosticarla. No basta con tomar el primero de la lista.
MEDICO_ID=$(echo "$RESP" | sed 's/},{/}\n{/g' | grep 'Carlos Mendoza' | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)
[ -z "$MEDICO_ID" ] && { echo "     ERROR: no se encontro el medico de la agenda"; exit 1; }
echo "     medicoId de la agenda: $MEDICO_ID"
req GET /api/usuarios '' "$TOKEN";       check 200 "listar usuarios"

echo; echo "=== Autorizacion por rol ==="
req POST /api/usuarios '{"username":"intruso","password":"clave123","nombreCompleto":"Intruso","email":"i@x.com","rol":"ADMIN"}' "$PACTOKEN"
check 403 "paciente no puede crear usuarios (403)"

req GET /api/usuarios '' "$PACTOKEN"
check 403 "paciente no puede listar usuarios (403)"

echo; echo "=== Citas ==="
FECHA=$(date -d "+2 days" +%Y-%m-%d 2>/dev/null || date +%Y-%m-%d)

req GET "/api/citas/disponibilidad?medicoId=$MEDICO_ID&fecha=$FECHA" '' "$TOKEN"
check 200 "disponibilidad de horarios"
echo "     horas libres: $(echo "$RESP" | head -c 90)"

# Elegimos la primera hora realmente libre, para que el script se pueda repetir
# contra un servidor que ya tenga citas reservadas.
HORA=$(echo "$RESP" | sed -n 's/.*\["\([^"]*\)".*/\1/p')
[ -z "$HORA" ] && { echo "     ERROR: el medico no tiene horas libres"; exit 1; }
echo "     hora elegida para la prueba: $HORA"

req POST /api/citas "{\"medicoId\":$MEDICO_ID,\"pacienteId\":1,\"sedeId\":1,\"fecha\":\"$FECHA\",\"hora\":\"$HORA\",\"motivo\":\"Control general\"}" "$TOKEN"
check 201 "crear cita"
CITA_ID=$(echo "$RESP" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)

req POST /api/citas "{\"medicoId\":$MEDICO_ID,\"pacienteId\":1,\"sedeId\":1,\"fecha\":\"$FECHA\",\"hora\":\"$HORA\",\"motivo\":\"Duplicada\"}" "$TOKEN"
check 409 "choque de horario da 409"

req POST /api/citas "{\"medicoId\":$MEDICO_ID,\"pacienteId\":1,\"sedeId\":1,\"fecha\":\"2020-01-01\",\"hora\":\"11:00\",\"motivo\":\"Pasado\"}" "$TOKEN"
check 400 "fecha pasada da 400"

req GET "/api/citas/$CITA_ID" '' "$TOKEN"
check 200 "obtener cita por id"

# La hora debe volver como HH:mm, igual que en /disponibilidad, no como HH:mm:ss.
if echo "$RESP" | grep -qE '"hora":"[0-9]{2}:[0-9]{2}"'; then
  printf 'OK   --- %s\n' "formato de hora HH:mm en la respuesta"; PASS=$((PASS+1))
else
  printf 'FAIL --- %s\n     %s\n' "formato de hora HH:mm en la respuesta" "$(echo "$RESP" | head -c 160)"; FAIL=$((FAIL+1))
fi

echo; echo "=== Atencion medica ==="
req POST /api/auth/login '{"username":"medico1","password":"medico123"}'
check 200 "login medico1"
MEDTOKEN=$(echo "$RESP" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

req POST "/api/citas/$CITA_ID/diagnostico" '{"diagnostico":"Riesgo cardiovascular controlado"}' "$MEDTOKEN"
check 200 "registrar diagnostico (cita queda ATENDIDO)"

req POST "/api/citas/$CITA_ID/receta" '{"indicaciones":"Tomar 1 pastilla al dia","medicamentos":"[{\"nombre\":\"Paracetamol 500mg\",\"dosis\":\"500mg\",\"frecuencia\":\"cada 8h\"}]"}' "$MEDTOKEN"
check 200 "emitir receta"

req PUT "/api/citas/$CITA_ID" "{\"medicoId\":$MEDICO_ID,\"pacienteId\":1,\"sedeId\":1,\"fecha\":\"$FECHA\",\"hora\":\"12:00\",\"motivo\":\"Reprogramar\"}" "$TOKEN"
check 400 "no se puede reprogramar una cita atendida (400)"

echo; echo "=== Aislamiento entre roles ==="
req POST /api/auth/login '{"username":"paciente1","password":"paciente123"}'
check 200 "login paciente1"
PACTOKEN2=$(echo "$RESP" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

req GET "/api/citas/$CITA_ID/receta" '' "$PACTOKEN2"
check 200 "paciente ve la receta de su cita ATENDIDO"

req POST "/api/citas/$CITA_ID/diagnostico" '{"diagnostico":"No autorizado"}' "$PACTOKEN2"
check 403 "paciente no puede diagnosticar (403)"

req GET /api/citas '' "$PACTOKEN2"
check 200 "paciente lista citas"
echo "     el paciente ve: $(echo "$RESP" | grep -o '"id":[0-9]*' | head -5 | tr '\n' ' ')"

echo; echo "=== Filtros del listado ==="
# El filtro por medico debe aplicarse en el servidor: si devolviera citas de
# otros medicos, la vista de agenda por medico estaria contaminada.
req GET "/api/citas?medicoId=$MEDICO_ID&size=100" '' "$TOKEN"
check 200 "filtrar citas por medicoId"
# Cuenta ids de medico distintos al pedido. Se separan los objetos en lineas
# porque '"id"' tambien aparece dentro de '"medicoId"' y '"pacienteId"'.
MEDS=$(echo "$RESP" | sed 's/},{/}\n{/g' | grep -oE '"medicoId":[0-9]+' | cut -d: -f2)
TOTAL_M=$(echo "$MEDS" | grep -c .)
COINCIDEN_M=$(echo "$MEDS" | grep -c "^$MEDICO_ID$")
OTROS=$(( TOTAL_M - COINCIDEN_M ))
if [ "$OTROS" = "0" ]; then
  printf 'OK   --- %s\n' "el filtro medicoId solo devuelve citas de ese medico"; PASS=$((PASS+1))
else
  printf 'FAIL --- %s\n     %s citas de otros medicos\n' "el filtro medicoId solo devuelve citas de ese medico" "$OTROS"; FAIL=$((FAIL+1))
fi

req GET "/api/citas?fecha=$FECHA&size=100" '' "$TOKEN"
check 200 "filtrar citas por fecha"
FECHAS=$(echo "$RESP" | sed 's/},{/}\n{/g' | grep -oE '"fecha":"[0-9-]+' | tr -d '"' | cut -d: -f2)
TOTAL_F=$(echo "$FECHAS" | grep -c .)
COINCIDEN=$(echo "$FECHAS" | grep -c "^$FECHA$")
OTRAF=$(( TOTAL_F - COINCIDEN ))
if [ "$OTRAF" = "0" ]; then
  printf 'OK   --- %s\n' "el filtro fecha solo devuelve citas de esa fecha"; PASS=$((PASS+1))
else
  printf 'FAIL --- %s\n     %s citas de otras fechas\n' "el filtro fecha solo devuelve citas de esa fecha" "$OTRAF"; FAIL=$((FAIL+1))
fi

req GET "/api/citas?medicoId=$MEDICO_ID&fecha=$FECHA&size=100" '' "$TOKEN"
check 200 "combinar medicoId y fecha"

# El MEDICO debe seguir viendo solo su agenda aunque pida otro medicoId.
req GET "/api/citas?medicoId=999999" '' "$MEDTOKEN"
check 200 "medico requesting la agenda de otro (queda acotado a la suya)"
MEDS2=$(echo "$RESP" | sed 's/},{/}\n{/g' | grep -oE '"medicoId":[0-9]+' | cut -d: -f2)
TOTAL_M2=$(echo "$MEDS2" | grep -c .)
COINCIDEN_M2=$(echo "$MEDS2" | grep -c "^$MEDICO_ID$")
FUGA=$(( TOTAL_M2 - COINCIDEN_M2 ))
if [ "$FUGA" = "0" ]; then
  printf 'OK   --- %s\n' "el rol MEDICO no ve citas de otros medicos"; PASS=$((PASS+1))
else
  printf 'FAIL --- %s\n     %s citas ajenas\n' "el rol MEDICO no ve citas de otros medicos" "$FUGA"; FAIL=$((FAIL+1))
fi

echo; echo "=== Paginacion y reportes ==="
req GET "/api/citas?page=0&size=2" '' "$TOKEN"
check 200 "listado paginado"
echo "     $(echo "$RESP" | head -c 160)"

req GET "/api/reportes/citas?estado=PENDIENTE&size=5" '' "$TOKEN"
check 200 "reporte de citas por estado"

req GET "/api/reportes/citas?desde=2020-01-01&hasta=2030-12-31" '' "$PACTOKEN2"
check 403 "paciente no puede ver reportes (403)"

echo; echo "=== Catalogo: alta y baja logica ==="
req POST /api/sedes '{"nombre":"Sede Temporal","direccion":"Av. Test 999","telefono":"044-000000"}' "$TOKEN"
check 201 "crear sede"
req DELETE /api/sedes/3 '' "$TOKEN"
check 204 "desactivar sede (baja logica)"

req POST /api/sedes '{"nombre":""}' "$TOKEN"
check 400 "validacion: nombre de sede obligatorio"

echo; echo "=== Documentacion ==="
req GET /v3/api-docs '';   check 200 "especificacion OpenAPI"
req GET /swagger-ui/index.html; check 200 "Swagger UI"

echo
echo "-----------------------------------------"
echo "Aprobados: $PASS   Fallidos: $FAIL"
[ "$FAIL" -eq 0 ] && echo "Todas las pruebas pasaron." || echo "Hay pruebas fallidas."
exit $([ "$FAIL" -eq 0 ] && echo 0 || echo 1)
