package no.nav.vedtak.felles.db.log;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.logging.Logger;

public class FastDataSourceSpy implements DataSource {

    private final DataSource realDs;

    public FastDataSourceSpy(DataSource realDs) {
        Objects.requireNonNull(realDs, "missing realDs, is null");
        this.realDs = realDs;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return realDs.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        realDs.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        realDs.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return realDs.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return realDs.getParentLogger();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(realDs)) {
            return (T) realDs; // NOSONAR
        } else {
            return realDs.unwrap(iface);
        }
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        if (iface.isInstance(realDs)) {
            return true;
        } else {
            return realDs.isWrapperFor(iface);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return FastConnectionLogSpy.spy(realDs.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return FastConnectionLogSpy.spy(realDs.getConnection(username, password));
    }

}
