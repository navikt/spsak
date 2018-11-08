package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.EditDistanceLetter;

public class UttakPeriodeEditDistance implements EditDistanceLetter {
    private OppgittPeriode periode;
    private Boolean periodeErDokumentert;

    UttakPeriodeEditDistance(OppgittPeriode periode) {
        Objects.requireNonNull(periode, "Periode");
        this.periode = periode;
    }

    @Override
    public int kostnadSettInn() {
        return 3;
    }

    @Override
    public int kostnadSlette() {
        return 2;
    }

    @Override
    public int kostnadEndre(EditDistanceLetter annen) {
        UttakPeriodeEditDistance annenUttakPeriode = (UttakPeriodeEditDistance) annen;
        if (!Objects.equals(periode.getÅrsak(), annenUttakPeriode.getPeriode().getÅrsak())) {
            return 6;
        }
        return 1;
    }

    @Override
    public boolean lik(EditDistanceLetter annen) {
        if (!(annen instanceof UttakPeriodeEditDistance)) {
            return false;
        }

        UttakPeriodeEditDistance annenUttakPeriode = (UttakPeriodeEditDistance) annen;
        return annenUttakPeriode.periode.equals(this.periode)
            && Objects.equals(annenUttakPeriode.periodeErDokumentert, this.periodeErDokumentert);
    }

    public static Builder builder(OppgittPeriode periode) {
        return new Builder(periode);
    }

    public OppgittPeriode getPeriode() {
        return periode;
    }

    public Boolean isPeriodeDokumentert() {
        return periodeErDokumentert;
    }

    public void setPeriodeErDokumentert(Boolean periodeErDokumentert) {
        this.periodeErDokumentert = periodeErDokumentert;
    }


    public static class Builder {
        private UttakPeriodeEditDistance kladd;

        public Builder(OppgittPeriode periode) {
            Objects.requireNonNull(periode, "Periode");
            kladd = new UttakPeriodeEditDistance(periode);
        }

        public Builder medPeriodeErDokumentert(boolean erDokumentert) {
            kladd.periodeErDokumentert = erDokumentert;
            return this;
        }

        public UttakPeriodeEditDistance build() {
            return kladd;
        }
    }
}
