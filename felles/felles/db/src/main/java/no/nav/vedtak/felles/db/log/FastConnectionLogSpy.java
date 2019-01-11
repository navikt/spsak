package no.nav.vedtak.felles.db.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Fast jdbc log outputter. (not so much overhead as other libraries, does not try to log resultset
 * output). Zero overhead if logging not switched on.
 * <p>
 * <h3>Usage</h3>
 * Intercept calls to connection before passing this around
 * Call {@link FastConnectionLogSpy#spy(Connection)}
 * Set logging level to INFO
 */
public class FastConnectionLogSpy implements InvocationHandler {

    private static final Class<?>[] SS = {Statement.class};
    private static final Class<?>[] PS = {PreparedStatement.class};
    private static final Class<?>[] CS = {CallableStatement.class};
    static final Logger log = LoggerFactory.getLogger("jdbc.sql");  // NOSONAR

    private final Connection refConnection;
    private ClassLoader classLoader;
    final LogUtils logUtils;

    private FastConnectionLogSpy(Connection realConn, LogUtils logUtils) {
        this.logUtils = logUtils;
        if (realConn == null) {
            throw new IllegalArgumentException("realConn kan ikke være null");
        }
        this.refConnection = realConn;
        classLoader = realConn.getClass().getClassLoader();
    }

    @SuppressWarnings("resource")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Connection conn = this.refConnection;
            if (conn == null) {
                return null;
            }
            if (!log.isInfoEnabled()) {
                return invokeMethod(method, conn, args);
            }

            Class<?> returnType = method.getReturnType();
            if (CallableStatement.class.isAssignableFrom(returnType)) {
                CallableStatement result = (CallableStatement) invokeMethod(method, conn, args);
                return newProxyInstance(classLoader, CS, new StatementInvocationHandler(result, (String) args[0], this));
            } else if (PreparedStatement.class.isAssignableFrom(returnType)) {
                PreparedStatement result = (PreparedStatement) invokeMethod(method, conn, args);
                return newProxyInstance(classLoader, PS, new StatementInvocationHandler(result, (String) args[0], this));
            } else if (Statement.class.isAssignableFrom(returnType)) {
                Statement result = (Statement) invokeMethod(method, conn, args);
                return newProxyInstance(classLoader, SS, new StatementInvocationHandler(result, null, this));
            } else {
                return invokeMethod(method, conn, args);
            }
        } catch (InvocationTargetException e) {  // NOSONAR
            throw e.getCause();  // NOSONAR
        }

    }

    private static Object invokeMethod(Method method, Object real, Object[] args) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(real, args);
    }

    /**
     * spy on a connection.
     */
    public static Connection spy(Connection realConn) {
        return spy(realConn, new DefaultLogFormatter());
    }

    public static Connection spy(Connection realConn, LogFormatter formatter) {
        InvocationHandler handler = new FastConnectionLogSpy(realConn, new LogUtils(formatter));
        return (Connection) Proxy.newProxyInstance(realConn.getClass().getClassLoader(), new Class<?>[]{Connection.class}, handler);

    }

}
