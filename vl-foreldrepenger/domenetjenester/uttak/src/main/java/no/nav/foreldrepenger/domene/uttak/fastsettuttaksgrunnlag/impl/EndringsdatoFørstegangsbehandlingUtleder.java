package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;

@Dependent
public class EndringsdatoFørstegangsbehandlingUtleder {

    private YtelsesFordelingRepository ytelsesFordelingRepository;

    EndringsdatoFørstegangsbehandlingUtleder() {
        //CDI
    }

    @Inject
    public EndringsdatoFørstegangsbehandlingUtleder(YtelsesFordelingRepository ytelsesFordelingRepository) {
        this.ytelsesFordelingRepository = ytelsesFordelingRepository;
    }

    public LocalDate utledEndringsdato(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        OppgittFordeling oppgittFordeling = ytelseFordelingAggregat.getOppgittFordeling();
        Optional<LocalDate> optionalFørsteSøkteUttaksdato = OppgittPeriodeUtil.finnFørsteSøkteUttaksdato(oppgittFordeling);
        if (!optionalFørsteSøkteUttaksdato.isPresent()) {
            throw new IllegalArgumentException("Utvikler-feil: Dette skal ikke skje. Ingen perioder i førstegangsøknad.");
        }
        LocalDate førsteSøkteUttaksdato = optionalFørsteSøkteUttaksdato.get();
        Optional<AvklarteUttakDatoer> avklarteUttakDatoer = ytelseFordelingAggregat.getAvklarteDatoer();
        LocalDate endringsdato;
        if (avklarteUttakDatoer.isPresent()) {
            LocalDate manueltSattFørsteUttaksdato = avklarteUttakDatoer.get().getFørsteUttaksDato();
            if (manueltSattFørsteUttaksdato != null && manueltSattFørsteUttaksdato.isBefore(førsteSøkteUttaksdato)) {
                endringsdato = manueltSattFørsteUttaksdato;
            } else {
                endringsdato = førsteSøkteUttaksdato;
            }
        } else {
            endringsdato = førsteSøkteUttaksdato;
        }
        return endringsdato;
    }
}
