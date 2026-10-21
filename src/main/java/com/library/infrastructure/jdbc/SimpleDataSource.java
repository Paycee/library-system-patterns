package com.library.infrastructure.jdbc;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Najjednostavniji DataSource: za svaki zahtev otvara novu konekciju.
 * Dovoljno za vežbu. U Spring Boot-u ovo radi Spring sam, preko pool-a konekcija (HikariCP).
 */
public class SimpleDataSource implements DataSource {
    private final String url;
    private final String user;
    private final String password;

    public SimpleDataSource(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Connection getConnection(String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Ostale metode interfejsa nam ne trebaju
    @Override public PrintWriter getLogWriter()               { return null; }
    @Override public void setLogWriter(PrintWriter out)       { }
    @Override public void setLoginTimeout(int seconds)        { }
    @Override public int getLoginTimeout()                    { return 0; }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Nije wrapper");
    }
    @Override public boolean isWrapperFor(Class<?> iface)     { return false; }
}
