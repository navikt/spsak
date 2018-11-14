package no.nav.vedtak.felles.db.log;

public class DefaultLogFormatter implements LogFormatter {
    private LogUtils logUtils;

    @Override
    public String formatParameter(Object object) {
        if (object == null) {
            return "null";
        } else if (object instanceof String) {
            String text = logUtils.replaceEach(
                    (String) object,
                    new String[]{"\\", "$", "'", "&", "\r", "\n", "\t"},
                    new String[]{"\\\\", "\\$", "''", "'||chr(38)||'", "", "'||chr(10)||'", "'||chr(9)||'"});

            // handle Matcher's appendReplacement method special characters: \ and $

            return "'" + text + "'";
        } else if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue() ? "Y" : "N";
        } else {
            return object.toString();
        }
    }

    @Override
    public void setLogUtils(LogUtils logUtils) {
        this.logUtils = logUtils;
    }

    protected LogUtils getLogUtils() {
        return logUtils;
    }
}
