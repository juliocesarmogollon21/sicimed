package com.sicimed.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión JDBC. Por defecto SQL Server Express (localhost\SQLEXPRESS / SICIMED).
 * Perfiles: sqlserver (default), h2, mysql — vía db.properties o -Dsicimed.db=...
 */
public final class DbConnection {

    private static final Properties PROPS = new Properties();
    private static volatile boolean initialized = false;
    private static volatile String activeProfile = "sqlserver";

    private DbConnection() {}

    private static synchronized void init() {
        if (initialized) return;
        try (InputStream in = DbConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (Exception ignored) {
            // defaults
        }
        activeProfile = System.getProperty("sicimed.db", PROPS.getProperty("sicimed.db", "sqlserver"));
        if ("mysql".equalsIgnoreCase(activeProfile)) {
            PROPS.putIfAbsent("jdbc.url", "jdbc:mysql://localhost:3306/sicimed?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima");
            PROPS.putIfAbsent("jdbc.user", "root");
            PROPS.putIfAbsent("jdbc.password", "root");
            PROPS.putIfAbsent("jdbc.driver", "com.mysql.cj.jdbc.Driver");
        } else if ("h2".equalsIgnoreCase(activeProfile)) {
            PROPS.putIfAbsent("jdbc.url", "jdbc:h2:mem:sicimed;DB_CLOSE_DELAY=-1;MODE=MySQL");
            PROPS.putIfAbsent("jdbc.user", "sa");
            PROPS.putIfAbsent("jdbc.password", "");
            PROPS.putIfAbsent("jdbc.driver", "org.h2.Driver");
        } else {
            // sqlserver
            PROPS.putIfAbsent("jdbc.url",
                    "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=SICIMED;integratedSecurity=true;encrypt=true;trustServerCertificate=true");
            PROPS.putIfAbsent("jdbc.user", "sa");
            PROPS.putIfAbsent("jdbc.password", "");
            PROPS.putIfAbsent("jdbc.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
            activeProfile = "sqlserver";
        }
        try {
            Class.forName(PROPS.getProperty("jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC no encontrado: " + PROPS.getProperty("jdbc.driver"), e);
        }
        initialized = true;
    }

    public static Connection getConnection() throws SQLException {
        init();
        String url = PROPS.getProperty("jdbc.url");
        String user = PROPS.getProperty("jdbc.user", "");
        String pass = PROPS.getProperty("jdbc.password", "");

        if (isSqlServer()) {
            boolean wantsIntegrated = url.toLowerCase().contains("integratedsecurity=true");
            if (wantsIntegrated) {
                try {
                    return DriverManager.getConnection(url);
                } catch (SQLException integratedEx) {
                    // Fallback: SQL authentication (quitar integratedSecurity de la URL)
                    String sqlAuthUrl = url.replaceAll("(?i);?integratedSecurity=true", "");
                    if (!sqlAuthUrl.toLowerCase().contains("encrypt=")) {
                        sqlAuthUrl += ";encrypt=true;trustServerCertificate=true";
                    }
                    try {
                        return DriverManager.getConnection(sqlAuthUrl, user, pass);
                    } catch (SQLException sqlAuthEx) {
                        SQLException combined = new SQLException(
                                "SQL Server: falló Windows Auth y también SQL Auth. "
                                        + "Windows: " + integratedEx.getMessage()
                                        + " | SQL Auth (" + user + "): " + sqlAuthEx.getMessage()
                                        + ". Cree la BD con backend/sql/01_create_sicimed.sql o configure jdbc.user/jdbc.password.",
                                sqlAuthEx);
                        combined.setNextException(integratedEx);
                        throw combined;
                    }
                }
            }
            return DriverManager.getConnection(url, user, pass);
        }
        return DriverManager.getConnection(url, user, pass);
    }

    public static String getProfile() {
        init();
        return activeProfile;
    }

    public static boolean isSqlServer() {
        return "sqlserver".equalsIgnoreCase(getProfile());
    }

    public static boolean isH2() {
        return "h2".equalsIgnoreCase(getProfile());
    }
}
