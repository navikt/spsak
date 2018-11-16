package no.nav.foreldrepenger.behandling.impl;

import static no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak.SØKNADSFRIST;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.inngangsvilkaar.opptjeningsperiode.RegelFastsettOpptjeningsperiode;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.OpptjeningsperiodeGrunnlag;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class SkjæringstidspunktTjenesteImpl implements SkjæringstidspunktTjeneste {

    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private UttakRepository uttakRepository;
    private OpptjeningRepository opptjeningRepository;
    private SøknadRepository søknadRepository;
    private RegisterInnhentingIntervallEndringTjeneste endringTjeneste;
    private Period antallMånederOpptjeningsperiode;
    private Period tidligsteUttakFørFødselPeriode;

    SkjæringstidspunktTjenesteImpl() {
        // CDI
    }

    @Inject
    public SkjæringstidspunktTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                          RegisterInnhentingIntervallEndringTjeneste endringTjeneste,
                                          @KonfigVerdi(value = "opptjeningsperiode.lengde") Period antallMånederOpptjeningsperiode,
                                          @KonfigVerdi(value = "uttak.tidligst.før.fødsel") Period tidligsteUttakFørFødselPeriode) {
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.endringTjeneste = endringTjeneste;
        this.antallMånederOpptjeningsperiode = antallMånederOpptjeningsperiode;
        this.tidligsteUttakFørFødselPeriode = tidligsteUttakFørFødselPeriode;
    }

    @Override
    public LocalDate utledSkjæringstidspunktForForeldrepenger(Behandling behandling) {
        if (!behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            throw SkjæringstidspunktFeil.FACTORY.ikkeForeldrepengerSak(behandling).toException();
        }

        final Optional<Opptjening> opptjening = opptjeningRepository.finnOpptjening(behandling);
        if (opptjening.isPresent()) {
            return opptjening.get().getTom().plusDays(1);
        }

        final Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);

        final Optional<LocalDate> avklartStartDato = ytelseFordelingAggregat.flatMap(YtelseFordelingAggregat::getAvklarteDatoer)
            .map(AvklarteUttakDatoer::getFørsteUttaksDato);

        return avklartStartDato.orElseGet(() -> førsteØnskedeUttaksdag(behandling));
    }

    @Override
    public LocalDate utledSkjæringstidspunktForRegisterInnhenting(Behandling behandling) {
        if (behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            return utledSkjæringstidspunktForRegisterinnhentingFP(behandling);
        }
        throw new IllegalStateException("Ukjent ytelse type.");
    }

    private LocalDate utledSkjæringstidspunktForRegisterinnhentingFP(Behandling behandling) {
        final RegelFastsettOpptjeningsperiode fastsettPeriode = new RegelFastsettOpptjeningsperiode();
        // TODO: Må utbedres for SP
        return LocalDate.now(FPDateUtil.getOffset());
    }

    private OpptjeningsperiodeGrunnlag fasettPeriodefor(Behandling behandling) {
        OpptjeningsperiodeGrunnlag grunnlag = new OpptjeningsperiodeGrunnlag();


        grunnlag.setTidligsteUttakFørFødselPeriode(tidligsteUttakFørFødselPeriode);
        grunnlag.setPeriodeLengde(antallMånederOpptjeningsperiode);
        grunnlag.setFørsteUttaksDato(førsteUttaksdag(behandling));

        return grunnlag;
    }

    @Override
    public LocalDate førsteUttaksdag(Behandling behandling) {
        final Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);

        final Optional<LocalDate> avklartStartDato = ytelseFordelingAggregat.flatMap(YtelseFordelingAggregat::getAvklarteDatoer)
            .map(AvklarteUttakDatoer::getFørsteUttaksDato);

        return avklartStartDato.orElseGet(() -> førsteØnskedeUttaksdag(behandling));
    }

    private LocalDate førsteØnskedeUttaksdag(Behandling behandling) {
        final Optional<LocalDate> førsteØnskedeUttaksdagIBehandling = finnFørsteØnskedeUttaksdagFor(behandling);
        if (behandling.erRevurdering()) {
            final Optional<LocalDate> førsteUttaksdagIForrigeVedtak = finnFørsteDatoIUttakResultat(behandling);
            if (!førsteUttaksdagIForrigeVedtak.isPresent() && !førsteØnskedeUttaksdagIBehandling.isPresent()) {
                return finnFørsteDatoFraOppgittePerioder(søknadRepository.hentFørstegangsSøknad(behandling).getFordeling().getOppgittePerioder())
                    .orElseThrow(() -> SkjæringstidspunktFeil.FACTORY.finnerIkkeSkjæringstidspunktForForeldrepenger(behandling).toException());
            } else {
                final LocalDate skjæringstidspunkt = utledTidligste(førsteØnskedeUttaksdagIBehandling.orElse(Tid.TIDENES_ENDE), førsteUttaksdagIForrigeVedtak.orElse(Tid.TIDENES_ENDE));
                if (skjæringstidspunkt.equals(Tid.TIDENES_ENDE)) {
                    // Fant da ikke noe skjæringstidspunkt i tidligere vedtak heller.
                    throw SkjæringstidspunktFeil.FACTORY.finnerIkkeSkjæringstidspunktForForeldrepenger(behandling).toException();
                }
                return skjæringstidspunkt;
            }
        } else {
            if (manglerSøknadIFørstegangsbehandling(behandling)) {
                // Har ikke grunnlag for å avgjøre skjæringstidspunkt enda så gir midlertidig dagens dato. for at DTOer skal fungere.
                return førsteØnskedeUttaksdagIBehandling.orElse(LocalDate.now(FPDateUtil.getOffset()));
            }
            return førsteØnskedeUttaksdagIBehandling.orElseThrow(() -> SkjæringstidspunktFeil.FACTORY.finnerIkkeSkjæringstidspunktForForeldrepenger(behandling).toException());
        }
    }

    private boolean manglerSøknadIFørstegangsbehandling(Behandling behandling) {
        return BehandlingType.FØRSTEGANGSSØKNAD.equals(behandling.getType()) && !søknadRepository.hentSøknadHvisEksisterer(behandling).isPresent();
    }

    private Optional<LocalDate> finnFørsteØnskedeUttaksdagFor(Behandling behandling) {
        return finnFørsteDatoFraOppgittePerioder(ytelsesFordelingRepository
            .hentAggregatHvisEksisterer(behandling)
            .map(YtelseFordelingAggregat::getOppgittFordeling)
            .map(OppgittFordeling::getOppgittePerioder)
            .orElse(Collections.emptyList()));
    }

    private LocalDate utledTidligste(LocalDate første, LocalDate andre) {
        if (første.isBefore(andre)) {
            return første;
        }
        return andre;
    }

    @Override
    public LocalDate utledSkjæringstidspunktFor(Behandling behandling) {
        if (behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            return utledSkjæringstidspunktForForeldrepenger(behandling);
        }
        throw new IllegalStateException("Ukjent ytelse type.");
    }

    private Behandling originalBehandling(Behandling behandling) {
        Optional<Behandling> originalBehandling = behandling.getOriginalBehandling();
        if (!originalBehandling.isPresent()) {
            throw new IllegalArgumentException("Revurdering må ha original behandling");
        }
        return originalBehandling.get();
    }

    private Optional<LocalDate> finnFørsteDatoFraOppgittePerioder(List<OppgittPeriode> oppgittePerioder) {
        return oppgittePerioder.stream()
            .map(OppgittPeriode::getFom)
            .min(Comparator.naturalOrder());
    }

    private Optional<LocalDate> finnFørsteDatoIUttakResultat(Behandling behandling) {
        final Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(originalBehandling(behandling));
        return uttakResultat.map(UttakResultatEntitet::getGjeldendePerioder)
            .map(UttakResultatPerioderEntitet::getPerioder)
            .orElse(Collections.emptyList())
            .stream()
            .filter(it -> it.getPeriodeResultatType().equals(PeriodeResultatType.INNVILGET) || SØKNADSFRIST.equals(it.getPeriodeResultatÅrsak()))
            .sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom))
            .map(UttakResultatPeriodeEntitet::getFom)
            .findFirst();
    }
}
