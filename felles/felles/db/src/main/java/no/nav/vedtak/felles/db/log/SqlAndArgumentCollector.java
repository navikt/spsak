package no.nav.vedtak.felles.db.log;

import org.slf4j.Logger;

import java.util.NavigableMap;
import java.util.TreeMap;

class SqlAndArgumentCollector {
    private static final Logger log = FastConnectionLogSpy.log;
    private String sql;
    private final NavigableMap<Integer, Object> positionalArgs = new TreeMap<>();
    private final NavigableMap<String, Object> namedArgs = new TreeMap<>();
    private StringBuilder logStatements = new StringBuilder(200);
    private final FastConnectionLogSpy fastConnectionLogSpy;

    SqlAndArgumentCollector(String sql, FastConnectionLogSpy fastConnectionLogSpy) {
        this.sql = sql;
        this.fastConnectionLogSpy = fastConnectionLogSpy;
    }

    void addLogStatements() {
        if (sql != null) {
            logStatements.append(formatterLogEntry());
            this.sql = null;
        }
        clearParameters();
    }

    private CharSequence formatterLogEntry() {
        if (namedArgs.isEmpty()) {
            return fastConnectionLogSpy.logUtils.createLogEntry(sql, positionalArgs);
        } else {
            // handles named and positional arguments
            String formattedSql = fastConnectionLogSpy.logUtils.createLogEntryForNamedParameters(sql, namedArgs).toString();
            return fastConnectionLogSpy.logUtils.createLogEntry(formattedSql, positionalArgs);
        }
    }

    void addPositionalArg(int index, Object value) {
        positionalArgs.put(index, value);
    }

    void addNamedArg(String name, Object value) {
        namedArgs.put(name, value);
    }

    void addNewSql(String sql) {
        this.sql = sql;
        clearParameters();
    }

    void clearParameters() {
        positionalArgs.clear();
        namedArgs.clear();
    }

    void logStatements() {
        addLogStatements();
        if (logStatements.length() > 0) {
            String message = logStatements.toString();
            log.info(message);
        } else {
            addNewSql(null);
        }
    }

}
