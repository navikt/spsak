package no.nav.foreldrepenger.behandling.søknadsfrist.impl;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandling.søknadsfrist.VurderSøknadsfristAksjonspunktDto;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.uttaksvilkår.SøknadsfristResultat;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class SøknadsfristForeldrepengerTjenesteImpl implements SøknadsfristForeldrepengerTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private int søknadsfristMndEtterFørsteUttaksdag;

    SøknadsfristForeldrepengerTjenesteImpl() {
        //For CDI
    }

    @Inject
    public SøknadsfristForeldrepengerTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                                        @KonfigVerdi("søknadfrist.måneder.etter.første.uttaksdag") int søknadsfristMndEtterFørsteUttaksdag) {
        this.repositoryProvider = repositoryProvider;
        this.søknadsfristMndEtterFørsteUttaksdag = søknadsfristMndEtterFørsteUttaksdag;
    }

    @Override
    public Optional<AksjonspunktDefinisjon> vurderSøknadsfristForForeldrepenger(BehandlingskontrollKontekst kontekst) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(kontekst.getBehandlingId());
        Søknad søknad = repositoryProvider.getSøknadRepository().hentSøknad(behandling);
        YtelseFordelingAggregat fordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);
        List<OppgittPeriode> oppgittePerioder = fordelingAggregat.getGjeldendeSøknadsperioder().getOppgittePerioder();

        SøknadsfristForeldrepengerRegelAdapter regelAdapter = new SøknadsfristForeldrepengerRegelAdapter();
        SøknadsfristResultat resultat = regelAdapter.vurderSøknadsfristForForeldrepenger(søknad, oppgittePerioder, søknadsfristMndEtterFørsteUttaksdag);

        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(behandling)
            .medFørsteLovligeUttaksdag(resultat.getTidligsteLovligeUttak())
            .medMottattDato(søknad.getMottattDato())
            .medSporingInput(resultat.getInnsendtGrunnlag())
            .medSporingRegel(resultat.getEvalueringResultat())
            .build();
        repositoryProvider.getUttakRepository().lagreUttaksperiodegrense(behandling, uttaksperiodegrense);


        Optional<String> årsakKode = resultat.getÅrsakKodeIkkeVurdert();
        if(!resultat.isRegelOppfylt() && årsakKode.isPresent()) {
            AksjonspunktRepository aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
            AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository
                .finnAksjonspunktDefinisjon(årsakKode.get());

            return Optional.of(aksjonspunktDefinisjon);
        }
        return Optional.empty();
    }

    @Override
    public void lagreVurderSøknadsfristResultat(Behandling behandling, VurderSøknadsfristAksjonspunktDto adapter) {

        if (adapter.getMottattDato() != null){
            LocalDate mottattDato = adapter.getMottattDato();
            LocalDate førsteLovligeUttaksdag = mottattDato.with(DAY_OF_MONTH, 1).minusMonths(søknadsfristMndEtterFørsteUttaksdag);
            Uttaksperiodegrense.Builder uttaksperiodegrenseBuilder = new Uttaksperiodegrense.Builder(behandling)
                .medMottattDato(adapter.getMottattDato())
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag);
            repositoryProvider.getUttakRepository().lagreUttaksperiodegrense(behandling, uttaksperiodegrenseBuilder.build());
        }
    }

    @Override
    public LocalDate finnSøknadsfristForPeriodeMedStart(LocalDate periodeStart) {
        return periodeStart.plusMonths(søknadsfristMndEtterFørsteUttaksdag).with(TemporalAdjusters.lastDayOfMonth());
    }
}
