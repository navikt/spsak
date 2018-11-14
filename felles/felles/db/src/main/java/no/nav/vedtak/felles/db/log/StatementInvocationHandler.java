package no.nav.vedtak.felles.db.log;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.Objects;

class StatementInvocationHandler extends SqlAndArgumentCollector implements InvocationHandler {
    private final WeakReference<Statement> refStatement;

    StatementInvocationHandler(Statement realStmt, String sql, FastConnectionLogSpy fastConnectionLogSpy) {
        super(sql, fastConnectionLogSpy);
        this.refStatement = new WeakReference<>(realStmt);
    }

    @SuppressWarnings("resource")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Statement stmt = refStatement.get();
            if (stmt == null) {
                return null;
            }

            handleLogging(method, args);

            return method.invoke(stmt, args);
        } catch (InvocationTargetException e) { // NOSONAR
            throw e.getCause(); // NOSONAR
        }
    }

    private void handleBatchMethod(Method method, Object... args) {
        String name = method.getName();

        String expectedName = "addBatch"; // NOSONAR

        if (!Objects.equals(expectedName, name)) {
            throw new IllegalArgumentException("Supports only handling addBatch methods");
        }
        if (args != null && args.length > 0) {
            // Statement addBatch
            super.addNewSql((String) args[0]);
        }
    }

    private void handleExecuteMethod(Object[] args, Class<?>[] parameterTypes) {
        if (parameterTypes.length == 0) {
            super.logStatements();
        } else if (String.class.equals(parameterTypes[0])) {
            super.addNewSql((String) args[0]);
            super.logStatements();
        }
    }

    private void handleLogging(Method method, Object... args) {
        String name = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (isSetterForString(name, parameterTypes)) {
            super.addPositionalArg(((Integer) args[0]), args[1]);
        } else if (isSetterForInteger(name, parameterTypes)) {
            super.addNamedArg(((String) args[0]), args[1]);
        } else {
            if (name.startsWith("execute")) {
                handleExecuteMethod(args, parameterTypes);
            } else if (isBatchMethod(name, args)) {
                handleBatchMethod(method, args);
            } else if ("clearParameters".equals(name)) {
                super.clearParameters();
            }
        }
    }

    private boolean isBatchMethod(String name, Object... args) {
        return "addBatch".equals(name) && (args == null || args.length == 0);
    }

    private boolean isSetterForInteger(String name, Class<?>... parameterTypes) {
        return parameterTypes.length == 2 && String.class.isAssignableFrom(parameterTypes[0]) && name.startsWith("set");
    }

    private boolean isSetterForString(String name, Class<?>... parameterTypes) {
        return parameterTypes.length == 2 && Integer.TYPE.isAssignableFrom(parameterTypes[0]) && name.startsWith("set");
    }

}
