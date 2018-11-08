package no.nav.foreldrepenger.batch;

public enum BatchStatus {
    RUNNING(-1),
    OK(0),
    WARNING(4),
    ERROR(8),
    BATCH_STOPPED(10),
    FATAL(16);

    private int value;

    BatchStatus(int v) {
        value = v;
    }

    public int value() {
        return value;
    }
}
