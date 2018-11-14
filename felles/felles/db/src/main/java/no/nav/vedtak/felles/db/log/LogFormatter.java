package no.nav.vedtak.felles.db.log;

public interface LogFormatter {

    void setLogUtils(LogUtils logUtils);

    String formatParameter(Object object);

}