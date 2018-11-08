package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.dto;

import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;

public class UttakDto {
    private final int trekkdager;
    private final String stonadskontoType;
    private final String periodeResultatType;
    private final boolean gradering;

    private UttakDto(int trekkdager, String stonadskontoType, String periodeResultatType, boolean gradering) {
        this.trekkdager = trekkdager;
        this.stonadskontoType = stonadskontoType;
        this.periodeResultatType = periodeResultatType;
        this.gradering = gradering;
    }

    public int getTrekkdager() {
        return trekkdager;
    }

    public String getStonadskontoType() {
        return stonadskontoType;
    }

    public String getPeriodeResultatType() {
        return periodeResultatType;
    }

    public boolean isGradering() {
        return gradering;
    }

    public static Builder build() {
        return new Builder();
    }

    public static class Builder {
        private int trekkdager;
        private StønadskontoType stønadskontoType;
        private PeriodeResultatType periodeResultatType;
        private boolean gradering;

        private Builder() {
        }

        public Builder medTrekkdager(int trekkdager) {
            this.trekkdager = trekkdager;
            return this;
        }

        public Builder medStønadskontoType(StønadskontoType stønadskontoType) {
            this.stønadskontoType = stønadskontoType;
            return this;
        }

        public Builder medPeriodeResultatType(PeriodeResultatType periodeResultatType) {
            this.periodeResultatType = periodeResultatType;
            return this;
        }

        public Builder medGradering(boolean gradering) {
            this.gradering = gradering;
            return this;
        }

        public UttakDto create() {
            String stonadskontoTypeString = stønadskontoType == null ? null : stønadskontoType.getKode();
            String periodeResultatTypeString = periodeResultatType == null ? null : periodeResultatType.getKode();
            return new UttakDto(trekkdager, stonadskontoTypeString, periodeResultatTypeString, gradering);
        }
    }
}
