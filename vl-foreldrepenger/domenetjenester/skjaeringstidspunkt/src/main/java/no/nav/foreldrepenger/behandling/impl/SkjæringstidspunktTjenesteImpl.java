package no.nav.foreldrepenger.behandling.impl;

import static no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak.SØKNADSFRIST;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.Comparator;
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
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class SkjæringstidspunktTjenesteImpl implements SkjæringstidspunktTjeneste {

    private UttakRepository uttakRepository;
    private OpptjeningRepository opptjeningRepository;
    private SøknadRepository søknadRepository;
    @SuppressWarnings("unused")
    private Period antallMånederOpptjeningsperiode;

    SkjæringstidspunktTjenesteImpl() {
        // CDI
    }

    @Inject
    public SkjæringstidspunktTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                          @KonfigVerdi(value = "opptjeningsperiode.lengde") Period antallMånederOpptjeningsperiode) {
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.antallMånederOpptjeningsperiode = antallMånederOpptjeningsperiode;
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

        return førsteØnskedeUttaksdag(behandling);
    }

    @Override
    public LocalDate utledSkjæringstidspunktForRegisterInnhenting(Behandling behandling) {
        if (behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            return utledSkjæringstidspunktForRegisterinnhentingFP(behandling);
        }
        throw new IllegalStateException("Ukjent ytelse type.");
    }

    @SuppressWarnings("unused")
    private LocalDate utledSkjæringstidspunktForRegisterinnhentingFP(Behandling behandling) {
        // TODO: Må utbedres for SP
        return LocalDate.now(FPDateUtil.getOffset());
    }

    private LocalDate førsteØnskedeUttaksdag(Behandling behandling) {
        final Optional<LocalDate> førsteØnskedeUttaksdagIBehandling = finnFørsteØnskedeUttaksdagFor(behandling);
        if (behandling.erRevurdering()) {
            final Optional<LocalDate> førsteUttaksdagIForrigeVedtak = finnFørsteDatoIUttakResultat(behandling);
            if (!førsteUttaksdagIForrigeVedtak.isPresent() && !førsteØnskedeUttaksdagIBehandling.isPresent()) {
                // FIXME SP: Trengs noe annet enn feilhåndtering her? foreldrepenger utleder fra fordeling
                throw SkjæringstidspunktFeil.FACTORY.finnerIkkeSkjæringstidspunktForForeldrepenger(behandling).toException();
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
        // FIXME SP: trenger ny startdato?
        return Optional.ofNullable(behandling.getOpprettetDato().toLocalDate());
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
