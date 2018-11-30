package no.nav.foreldrepenger.batch.task;

import java.time.LocalTime;

class BatchConfig {
    String name;
    String params;
    int hour;
    int minute;

    BatchConfig(int time, int minutt, String name, String params) { // NOSONAR
        this.name = name;
        this.params = params;
        this.hour = time;
        this.minute = minutt;
    }

    String getName() {
            return name;
        }

    String getParams() {
            return params;
        }

    LocalTime getKjøreTidspunkt() {
            return LocalTime.of(hour, minute);
        }

}
