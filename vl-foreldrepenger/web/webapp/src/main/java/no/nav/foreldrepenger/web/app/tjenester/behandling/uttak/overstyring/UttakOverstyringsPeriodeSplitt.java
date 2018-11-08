package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.overstyring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import no.nav.fpsak.tidsserie.LocalDateInterval;

class UttakOverstyringsPeriodeSplitt {
    private LocalDateInterval opprinnelig;
    private List<LocalDateInterval> splittet = new ArrayList<>();

    private UttakOverstyringsPeriodeSplitt() {

    }

    public LocalDateInterval getOpprinnelig() {
        return opprinnelig;
    }

    public List<LocalDateInterval> getSplittet() {
        return splittet.stream().sorted(Comparator.comparing(LocalDateInterval::getFomDato)).collect(Collectors.toList());
    }

    static class Builder {

        UttakOverstyringsPeriodeSplitt kladd = new UttakOverstyringsPeriodeSplitt();

        public Builder medOpprinnelig(LocalDateInterval opprinnelig) {
            kladd.opprinnelig = opprinnelig;
            return this;
        }
        public UttakOverstyringsPeriodeSplitt build() {
            Objects.requireNonNull(kladd.opprinnelig);
            Objects.requireNonNull(kladd.splittet);
            return kladd;
        }

        public Builder leggTil(LocalDateInterval splittetPeriode) {
            kladd.splittet.add(splittetPeriode);
            return null;
        }
    }
}
