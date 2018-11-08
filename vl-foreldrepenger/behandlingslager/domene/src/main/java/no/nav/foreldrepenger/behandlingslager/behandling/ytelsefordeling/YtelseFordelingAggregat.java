package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;

public class YtelseFordelingAggregat {

    private OppgittFordeling oppgittFordeling;
    private OppgittFordeling overstyrtFordeling;
    private OppgittDekningsgrad oppgittDekningsgrad;
    private OppgittRettighet oppgittRettighet;
    private PerioderUtenOmsorg perioderUtenOmsorg;
    private PerioderAleneOmsorg perioderAleneOmsorg;
    private PerioderUttakDokumentasjon perioderUttakDokumentasjon;
    private AvklarteUttakDatoer avklarteDatoer;

    protected YtelseFordelingAggregat() {
    }

    public OppgittFordeling getOppgittFordeling() {
        return oppgittFordeling;
    }

    /**
     * Skal ikke brukes.
     * Bruk {@link no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository}
     */
    @Deprecated
    public OppgittDekningsgrad getOppgittDekningsgrad() {
        return oppgittDekningsgrad;
    }

    public OppgittRettighet getOppgittRettighet() {
        return oppgittRettighet;
    }

    public Optional<PerioderUtenOmsorg> getPerioderUtenOmsorg() {
        return Optional.ofNullable(perioderUtenOmsorg);
    }

    public Optional<PerioderAleneOmsorg> getPerioderAleneOmsorg() {
        return Optional.ofNullable(perioderAleneOmsorg);
    }

    public OppgittFordeling getGjeldendeSøknadsperioder() {
        if (getOverstyrtFordeling().isPresent()) {
            return overstyrtFordeling;
        }
        return oppgittFordeling;
    }

    public Optional<AvklarteUttakDatoer> getAvklarteDatoer() {
        return Optional.ofNullable(avklarteDatoer);
    }

    public Optional<OppgittFordeling> getOverstyrtFordeling() {
        return Optional.ofNullable(overstyrtFordeling);
    }
    public Optional<PerioderUttakDokumentasjon> getPerioderUttakDokumentasjon() {
        return Optional.ofNullable(perioderUttakDokumentasjon);
    }

    public static Builder oppdatere(Optional<YtelseFordelingAggregat> ytelseFordelingAggregat) {
        return ytelseFordelingAggregat.map(Builder::oppdatere).orElseGet(Builder::nytt);
    }

    public static class Builder {
        private YtelseFordelingAggregat kladd;

        private Builder() {
            this.kladd = new YtelseFordelingAggregat();
        }

        private Builder(YtelseFordelingAggregat ytelseFordelingAggregat) {
            this.kladd = ytelseFordelingAggregat;
        }

        private static Builder nytt() {
            return new Builder();
        }

        private static Builder oppdatere(YtelseFordelingAggregat ytelseFordelingAggregat) {
            return new Builder(ytelseFordelingAggregat);
        }

        public static Builder oppdatere(Optional<YtelseFordelingAggregat> ytelseFordelingAggregat) {
            return ytelseFordelingAggregat.map(Builder::oppdatere).orElseGet(Builder::nytt);
        }

        Builder medOppgittFordeling(OppgittFordeling fordeling) {
            kladd.oppgittFordeling = fordeling;
            return this;
        }

        Builder medOverstyrtFordeling(OppgittFordeling fordeling) {
            if (fordeling != null && kladd.getOppgittFordeling() == null) {
                throw YtelseFordelingFeil.FACTORY.kanIkkeOverstyreDetFinnesIkkeOrginalSøknadsperiode().toException();
            }
            kladd.overstyrtFordeling = fordeling;
            return this;
        }

        Builder medOppgittDekningsgrad(OppgittDekningsgrad oppgittDekningsgrad) {
            kladd.oppgittDekningsgrad = oppgittDekningsgrad;
            return this;
        }

        Builder medOppgittRettighet(OppgittRettighet oppgittRettighet) {
            kladd.oppgittRettighet = oppgittRettighet;
            return this;
        }

        Builder medPerioderUtenOmsorg(PerioderUtenOmsorg perioderUtenOmsorg) {
            kladd.perioderUtenOmsorg = perioderUtenOmsorg;
            return this;
        }

        Builder medPerioderAleneOmsorg(PerioderAleneOmsorg perioderAleneOmsorg) {
            kladd.perioderAleneOmsorg = perioderAleneOmsorg;
            return this;
        }

        Builder medPerioderUttakDokumentasjon(PerioderUttakDokumentasjon perioderUttakDokumentasjon) {
            kladd.perioderUttakDokumentasjon = perioderUttakDokumentasjon;
            return this;
        }

        Builder medAvklarteDatoer(AvklarteUttakDatoer avklarteUttakDatoer) {
            kladd.avklarteDatoer = avklarteUttakDatoer;
            return this;
        }

        public YtelseFordelingAggregat build() {
            return kladd;
        }
    }
}
