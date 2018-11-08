package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.FastsettUttaksgrunnlagTjeneste;
import no.nav.vedtak.util.Tuple;

@Dependent
public class FastsettUttaksgrunnlagTjenesteImpl implements FastsettUttaksgrunnlagTjeneste {

    private UttakRepository uttakRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private FamilieHendelseRepository familieHendelseRepository;
    private EndringsdatoFørstegangsbehandlingUtleder endringsdatoFørstegangsbehandlingUtleder;
    private EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder;

    private VedtaksperioderHelper vedtaksperioderHelper = new VedtaksperioderHelper();
    private JusterPeriodeHelper justerPeriodeHelper = new JusterPeriodeHelper();

    @Inject
    public FastsettUttaksgrunnlagTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider,
                                              EndringsdatoFørstegangsbehandlingUtleder endringsdatoFørstegangsbehandlingUtleder,
                                              EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder) {
        this.uttakRepository = behandlingRepositoryProvider.getUttakRepository();
        this.ytelsesFordelingRepository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
        this.familieHendelseRepository = behandlingRepositoryProvider.getFamilieGrunnlagRepository();
        this.endringsdatoFørstegangsbehandlingUtleder = endringsdatoFørstegangsbehandlingUtleder;
        this.endringsdatoRevurderingUtleder = endringsdatoRevurderingUtleder;
    }

    @Override
    public void fastsettUttaksgrunnlag(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        OppgittFordeling oppgittFordeling = ytelseFordelingAggregat.getOppgittFordeling();

        final LocalDate endringsdato;
        if (behandling.erRevurdering()) {
            if (!behandling.getOriginalBehandling().isPresent()) {
                throw new IllegalArgumentException("Utvikler-feil: ved revurdering skal det alltid finnes en original behandling");
            }
            endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(behandling);
            if (forrigeBehandlingHarUttaksresultat(behandling.getOriginalBehandling().get())) {
                oppgittFordeling = kopierVedtaksperioderFomEndringsdato(oppgittFordeling, endringsdato, behandling.getOriginalBehandling().get());
            } else {
                oppgittFordeling = kopierOppgittFordelingFraForrigeBehandling(behandling.getOriginalBehandling().get());
            }
        } else {
            endringsdato = endringsdatoFørstegangsbehandlingUtleder.utledEndringsdato(behandling);
        }

        FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseRepository.hentAggregat(behandling);
        Tuple<Optional<LocalDate>, LocalDate> familiehendelser = finnFamiliehendelseEndring(behandling);
        if (terminEllerFødselSøknad(familieHendelseGrunnlag.getGjeldendeVersjon().getType())) {
            if (erDetEndringAvFamiliehendelse(familiehendelser.getElement1().orElse(null), familiehendelser.getElement2())) {
                oppgittFordeling = justerPeriodeHelper.juster(
                    oppgittFordeling,
                    familiehendelser.getElement1().orElse(null),
                    familiehendelser.getElement2());
            }
        }

        ytelsesFordelingRepository.lagre(behandling, oppgittFordeling);
        ytelsesFordelingRepository.lagre(behandling, oppdatertMedEndringsdato(ytelseFordelingAggregat.getAvklarteDatoer(), endringsdato));
    }

    private OppgittFordeling kopierOppgittFordelingFraForrigeBehandling(Behandling forrigeBehandling) {
        YtelseFordelingAggregat forrigeBehandlingYtelseFordeling = ytelsesFordelingRepository.hentAggregat(forrigeBehandling);
        return vedtaksperioderHelper.kopierOppgittFordelingFraForrigeBehandling(forrigeBehandlingYtelseFordeling.getOppgittFordeling());
    }

    private boolean forrigeBehandlingHarUttaksresultat(Behandling forrigeBehandling) {
        return uttakRepository.hentUttakResultatHvisEksisterer(forrigeBehandling).isPresent();
    }

    private AvklarteUttakDatoer oppdatertMedEndringsdato(Optional<AvklarteUttakDatoer> avklarteUttakDatoer, LocalDate endringsdato) {
        if (avklarteUttakDatoer.isPresent()) {
            return new AvklarteUttakDatoerEntitet(avklarteUttakDatoer.get().getFørsteUttaksDato(), endringsdato);
        }
        return new AvklarteUttakDatoerEntitet(null, endringsdato);
    }

    private static boolean terminEllerFødselSøknad(FamilieHendelseType familieHendelseType) {
        return Stream.of(FamilieHendelseType.FØDSEL, FamilieHendelseType.TERMIN).anyMatch(familieHendelseType::equals);
    }

    private boolean erDetEndringAvFamiliehendelse(LocalDate tidligereFamiliehendelse, LocalDate nåværendeFamiliehendelse) {
        if (tidligereFamiliehendelse != null && nåværendeFamiliehendelse != null) {
            if (!tidligereFamiliehendelse.equals(nåværendeFamiliehendelse)) {
                return true;
            }
        }
        return false;
    }

    private OppgittFordeling kopierVedtaksperioderFomEndringsdato(OppgittFordeling oppgittFordeling, LocalDate endringsdato, Behandling forrigeBehandling) {
        //Kopier vedtaksperioder fom endringsdato.
        UttakResultatEntitet uttakResultatEntitet = uttakRepository.hentUttakResultat(forrigeBehandling);
        return vedtaksperioderHelper.opprettOppgittFordeling(uttakResultatEntitet, oppgittFordeling, endringsdato);
    }

    private Tuple<Optional<LocalDate>, LocalDate> finnFamiliehendelseEndring(Behandling behandling) {
        Objects.requireNonNull(behandling);

        Optional<Behandling> forrigeBehandling = behandling.getOriginalBehandling();
        FamilieHendelseGrunnlag familieHendelseGrunnlagForBehandling = familieHendelseRepository.hentAggregat(behandling);
        LocalDate nyFamiliehendelse = familieHendelseGrunnlagForBehandling.finnGjeldendeFødselsdato();
        if (forrigeBehandling.isPresent()) {
            LocalDate forrigeFamiliehendelse = familieHendelseRepository.hentAggregat(forrigeBehandling.get()).finnGjeldendeFødselsdato();
            return new Tuple<>(Optional.ofNullable(forrigeFamiliehendelse), nyFamiliehendelse);
        } else {
            Optional<Terminbekreftelse> terminbekreftelse = familieHendelseGrunnlagForBehandling.getSøknadVersjon().getTerminbekreftelse();
            if (terminbekreftelse.isPresent()) {
                LocalDate termindato = terminbekreftelse.get().getTermindato();
                return new Tuple<>(Optional.ofNullable(termindato), nyFamiliehendelse);
            } else {
                Optional<LocalDate> fødselsdato = familieHendelseGrunnlagForBehandling.getSøknadVersjon().getFødselsdato();
                return new Tuple<>(fødselsdato, nyFamiliehendelse);
            }
        }
    }

}
