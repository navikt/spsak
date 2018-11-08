package no.nav.foreldrepenger.domene.medlem.api;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

/**
 * Endringsresultat i personopplysninger for medlemskap
 */
public class EndringsresultatPersonopplysningerForMedlemskap {

    private Optional<LocalDate> gjeldendeFra;
    private List<Endring> endringer = new ArrayList<>();

    private EndringsresultatPersonopplysningerForMedlemskap() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean harEndringer() {
        return endringer.stream().anyMatch(Endring::isErEndret);
    }

    public List<Endring> getEndredeAttributter() {
        return endringer.stream().filter(Endring::isErEndret).collect(Collectors.toList());
    }

    /**
     * @return er satt hvis det er endringer
     */
    public Optional<LocalDate> getGjeldendeFra() {
        return gjeldendeFra;
    }

    public enum EndretAttributt {
        Personstatus, StatsborgerskapRegion, Adresse;
    }

    public static final class Endring {
        private boolean erEndret;
        private EndretAttributt endretAttributt;
        String endretFra;
        String endretTil;
        private DatoIntervallEntitet periode;

        private Endring(EndretAttributt endretAttributt, DatoIntervallEntitet periode, String endretFra, String endretTil) {
            Objects.requireNonNull(endretAttributt);
            Objects.requireNonNull(endretFra);
            Objects.requireNonNull(endretTil);
            Objects.requireNonNull(periode);

            if (!endretFra.trim().equalsIgnoreCase(endretTil.trim())) {
                this.erEndret = true;
            }
            this.endretAttributt = endretAttributt;
            this.endretFra = endretFra;
            this.endretTil = endretTil;
            this.periode = periode;
        }

        public EndretAttributt getEndretAttributt() {
            return endretAttributt;
        }

        public String getEndretFra() {
            return endretFra;
        }

        public String getEndretTil() {
            return endretTil;
        }

        public boolean isErEndret() {
            return erEndret;
        }

        public DatoIntervallEntitet getPeriode() {
            return periode;
        }
    }

    public static final class Builder {
        EndringsresultatPersonopplysningerForMedlemskap kladd = new EndringsresultatPersonopplysningerForMedlemskap();

        private Builder() {
        }

        public EndringsresultatPersonopplysningerForMedlemskap build() {
            this.kladd.gjeldendeFra = kladd.getEndredeAttributter().stream()
                .map(e -> e.getPeriode().getFomDato())
                .min(LocalDate::compareTo);
            return kladd;
        }

        public Builder leggTilEndring(EndretAttributt endretAttributt, DatoIntervallEntitet periode, String endretFra, String endretTil) {
            Endring endring = new Endring(endretAttributt, periode, endretFra, endretTil);
            kladd.endringer.add(endring);
            return this;
        }
    }
}
