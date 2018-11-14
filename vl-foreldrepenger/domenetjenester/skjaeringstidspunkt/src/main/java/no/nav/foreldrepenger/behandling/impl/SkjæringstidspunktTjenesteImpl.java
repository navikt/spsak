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
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
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
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.FagsakÅrsak;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.SoekerRolle;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class SkjæringstidspunktTjenesteImpl implements SkjæringstidspunktTjeneste {

    private FamilieHendelseRepository familieGrunnlagRepository;
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
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.opptjeningRepository = repositoryProvider.getOpptjeningRepository();
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.endringTjeneste = endringTjeneste;
        this.antallMånederOpptjeningsperiode = antallMånederOpptjeningsperiode;
        this.tidligsteUttakFørFødselPeriode = tidligsteUttakFørFødselPeriode;
    }

    /**
     * Bestem skjæringstidspunkt ut fra bekreftede data
     */
    @Override
    public Optional<LocalDate> utledSkjæringstidspunktForEngangsstønadFraBekreftedeData(Behandling behandling) {
        final Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
        if (familieHendelseAggregat.isPresent() && familieHendelseAggregat.get().getHarBekreftedeData()) {
            Optional<FamilieHendelse> gjeldendeBekreftetVersjon = familieHendelseAggregat.get().getGjeldendeBekreftetVersjon();
            return gjeldendeBekreftetVersjon.map(FamilieHendelse::getSkjæringstidspunkt);
        }
        return Optional.empty();
    }

    /**
     * Bestem skjæringstidspunkt ut fra oppgitte data i søknad.
     */
    @Override
    public LocalDate utledSkjæringstidspunktForEngangsstønadFraOppgitteData(Behandling behandling) {
        return utledSkjæringstidspunktForEngangsstønadFraOppgitteData(behandling.getId());
    }

    @Override
    public LocalDate utledSkjæringstidspunktForEngangsstønadFraOppgitteData(Long behandlingId) {
        final Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandlingId);
        if (!familieHendelseAggregat.isPresent() || familieHendelseAggregat.get().getSøknadVersjon() == null) {
            return null;
        }
        final FamilieHendelseGrunnlag grunnlag = familieHendelseAggregat.get();
        return grunnlag.getSøknadVersjon().getSkjæringstidspunkt();
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
        final Optional<FamilieHendelseGrunnlag> familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
        if (familieHendelseGrunnlag.isPresent()) {
            OpptjeningsperiodeGrunnlag input = fasettPeriodefor(behandling, familieHendelseGrunnlag.get().getSøknadVersjon());
            final OpptjeningsPeriode periode = new OpptjeningsPeriode();
            fastsettPeriode.evaluer(input, periode);

            final LocalDate oppgittSkjæringstidspunkt = periode.getOpptjeningsperiodeTom().plusDays(1);

            final Optional<FamilieHendelse> bekreftetVersjon = familieHendelseGrunnlag.get().getGjeldendeBekreftetVersjon();
            final Optional<Opptjening> opptjening = opptjeningRepository.finnOpptjening(behandling);
            LocalDate bekreftetSkjæringstidspunkt = null;
            if (opptjening.isPresent()) {
                bekreftetSkjæringstidspunkt = opptjening.get().getTom();
            } else if (bekreftetVersjon.isPresent()) {
                input = fasettPeriodefor(behandling, bekreftetVersjon.get());
                fastsettPeriode.evaluer(input, periode);

                bekreftetSkjæringstidspunkt = periode.getOpptjeningsperiodeTom().plusDays(1);
            }
            if (bekreftetSkjæringstidspunkt != null) {
                if (endringTjeneste.erEndringIPerioden(oppgittSkjæringstidspunkt, bekreftetSkjæringstidspunkt, behandling.getFagsakYtelseType())) {
                    return bekreftetSkjæringstidspunkt;
                }
            }
            return oppgittSkjæringstidspunkt;
        }
        return LocalDate.now(FPDateUtil.getOffset());
    }

    private OpptjeningsperiodeGrunnlag fasettPeriodefor(Behandling behandling, FamilieHendelse hendelse) {
        OpptjeningsperiodeGrunnlag grunnlag = new OpptjeningsperiodeGrunnlag();

        final FamilieHendelseType hendelseType = hendelse.getType();

        grunnlag.setFagsakÅrsak(finnFagsakÅrsak(hendelse));
        grunnlag.setSøkerRolle(finnFagsakSøkerRolle(behandling));
        if (grunnlag.getFagsakÅrsak() == null || grunnlag.getSøkerRolle() == null) {
            throw new IllegalArgumentException("Utvikler-feil: Finner ikke årsak(" + grunnlag.getFagsakÅrsak() + ")/rolle(" + grunnlag.getSøkerRolle() + ") for behandling:" + behandling.getId());
        }

        if (grunnlag.getFagsakÅrsak().equals(FagsakÅrsak.FØDSEL)) {
            if (hendelse.getTerminbekreftelse().isPresent()) {
                grunnlag.setTerminDato(hendelse.getTerminbekreftelse().get().getTermindato());
            }
            grunnlag.setHendelsesDato(hendelse.getSkjæringstidspunkt());
        } else {
            if (hendelseType.equals(FamilieHendelseType.ADOPSJON) || hendelseType.equals(FamilieHendelseType.OMSORG)) {
                hendelse.getAdopsjon().ifPresent(adopsjon1 -> grunnlag.setHendelsesDato(adopsjon1.getOmsorgsovertakelseDato()));
            }
        }
        if (grunnlag.getHendelsesDato() == null) {
            // TODO: FIX
            grunnlag.setHendelsesDato(familieGrunnlagRepository.hentAggregat(behandling).finnGjeldendeFødselsdato());
            if (grunnlag.getHendelsesDato() == null) {
                throw new IllegalArgumentException("Utvikler-feil: Finner ikke hendelsesdato for behandling:" + behandling.getId());
            }
        }

        grunnlag.setTidligsteUttakFørFødselPeriode(tidligsteUttakFørFødselPeriode);
        grunnlag.setPeriodeLengde(antallMånederOpptjeningsperiode);
        grunnlag.setFørsteUttaksDato(førsteUttaksdag(behandling));

        return grunnlag;
    }

    // TODO(Termitt): Håndtere MMOR, SAMB mm.
    private SoekerRolle finnFagsakSøkerRolle(Behandling behandling) {
        RelasjonsRolleType relasjonsRolleType = behandling.getRelasjonsRolleType();
        if (RelasjonsRolleType.MORA.equals(relasjonsRolleType)) {
            return SoekerRolle.MORA;
        }
        if (RelasjonsRolleType.UDEFINERT.equals(relasjonsRolleType) || RelasjonsRolleType.BARN.equals(relasjonsRolleType)) {
            return null;
        }
        return SoekerRolle.FARA;
    }

    private FagsakÅrsak finnFagsakÅrsak(FamilieHendelse gjeldendeVersjon) {
        final FamilieHendelseType type = gjeldendeVersjon.getType();
        if (gjeldendeVersjon.getGjelderFødsel()) {
            return FagsakÅrsak.FØDSEL;
        } else if (FamilieHendelseType.ADOPSJON.equals(type)) {
            return FagsakÅrsak.ADOPSJON;
        } else if (FamilieHendelseType.OMSORG.equals(type)) {
            return FagsakÅrsak.OMSORG;
        }
        return null;
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
