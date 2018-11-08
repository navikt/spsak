package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørYtelseEndring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoer;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.FamilieHendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.OpplysningerOmDødEndringIdentifiserer;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.FeilFactory;

@Dependent
public class EndringsdatoRevurderingUtleder {

    private static final Logger log = LoggerFactory.getLogger(EndringsdatoRevurderingUtleder.class);
    private static final Comparator<LocalDate> LOCAL_DATE_COMPARATOR = Comparator.comparing(LocalDate::toEpochDay);

    private FamilieHendelseTjeneste familieHendelseTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SøknadRepository søknadRepository;
    private UttakRepository uttakRepository;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private OpplysningerOmDødEndringIdentifiserer opplysningerOmDødEndringIdentifiserer;

    EndringsdatoRevurderingUtleder() {
        //CDI
    }

    @Inject
    public EndringsdatoRevurderingUtleder(FamilieHendelseTjeneste familieHendelseTjeneste,
                                          InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                          BehandlingRepositoryProvider repositoryProvider,
                                          OpplysningerOmDødEndringIdentifiserer opplysningerOmDødEndringIdentifiserer) {
        this.familieHendelseTjeneste = familieHendelseTjeneste;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.ytelsesFordelingRepository = repositoryProvider.getYtelsesFordelingRepository();
        this.opplysningerOmDødEndringIdentifiserer = opplysningerOmDødEndringIdentifiserer;
    }

    public LocalDate utledEndringsdato(Behandling revurdering) {
        EnumSet<Endringsdato> endringsdatoEnumSet = utledEndringsdatoEnumSet(revurdering);
        if (endringsdatoEnumSet.isEmpty()) {
            endringsdatoEnumSet.add(Endringsdato.FØRSTE_UTTAKSDATO_GJELDENDE_VEDTAK);
            log.info("Kunne ikke utlede endringsdato for revurdering med behandlingId=" + revurdering.getId() + ". Satte FØRSTE_UTTAKSDATO_GJELDENDE_VEDTAK.");
        }

        Optional<LocalDate> endringsdato = utledEndringsdato(revurdering, endringsdatoEnumSet);
        return endringsdato.orElseThrow(() -> kanIkkeUtledeException(revurdering));
    }

    private Optional<LocalDate> utledEndringsdato(Behandling revurdering, EnumSet<Endringsdato> endringsdatoEnumSet) {
        LocalDate endringsdato = finnFørsteDato(endringsdatoEnumSet, revurdering);
        //Sjekk om endringsdato overlapper med tidligere vedtak. Hvis periode i tidligere vedtak er manuelt behandling så skal endringsdato flyttes start av perioden
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(finnForrigeBehandling(revurdering));
        if (!uttakResultat.isPresent()) {
            return Optional.ofNullable(endringsdato);
        }
        Optional<UttakResultatPeriodeEntitet> vedtattPeriodeMedOverlapp = finnVedtattPeriodeMedOverlapp(endringsdato, uttakResultat.get());
        if (vedtattPeriodeMedOverlapp.isPresent() && vedtattPeriodeMedOverlapp.get().isManueltBehandlet()) {
            return Optional.ofNullable(vedtattPeriodeMedOverlapp.get().getFom());

        }
        return Optional.ofNullable(endringsdato);
    }

    private EnumSet<Endringsdato> utledEndringsdatoEnumSet(Behandling revurdering) {
        EnumSet<Endringsdato> endringsdatoEnumSet = EnumSet.noneOf(Endringsdato.class);

        // #4
        if (revurdering.erBerørtBehandling()) {
            endringsdatoEnumSet.add(Endringsdato.ENDRINGSDATO_I_BEHANDLING_SOM_FØRTE_TIL_BERØRT_BEHANDLING);
            return endringsdatoEnumSet;
        }

        // #1
        if (fødselHarSkjeddSidenForrigeBehandling(revurdering)) {
            LocalDate fødselsdato = finnFødselsdato(revurdering).get(); //NOSONAR
            Optional<LocalDate> førsteUttaksdato = finnFørsteUttaksdatoGjeldendeVedtak(finnForrigeBehandling(revurdering));
            if (!førsteUttaksdato.isPresent() || fødselsdato.isBefore(førsteUttaksdato.get())) {
                endringsdatoEnumSet.add(Endringsdato.FØDSELSDATO);
            } else {
                endringsdatoEnumSet.add(Endringsdato.FØRSTE_UTTAKSDATO_GJELDENDE_VEDTAK);
            }
        }

        // #2
        if (erEndringssøknadMottatt(revurdering)) {
            endringsdatoEnumSet.add(Endringsdato.FØRSTE_UTTAKSDATO_SØKNAD);
        }

        // #5
        if (harManueltSattFørsteUttaksdato(revurdering)) {
            endringsdatoEnumSet.add(Endringsdato.MANUELT_SATT_FØRSTE_UTTAKSDATO);
        }

        sjekkOmFørsteUttaksdatoSkalVæreEndringsdato(revurdering, endringsdatoEnumSet);


        // Adopsjon
        if (erAdopsjon(revurdering)) {
            endringsdatoEnumSet.add(finnEndringsdatoAdopsjon(revurdering));
        }

        if (!forrigeBehandlingUtenUttaksresultat(revurdering)) {
            endringsdatoEnumSet.add(Endringsdato.FØRSTE_UTTAKSDATO_SØKNAD_FORRIGE_BEHANDLING);
        }

        return endringsdatoEnumSet;
    }

    private void sjekkOmFørsteUttaksdatoSkalVæreEndringsdato(Behandling revurdering, EnumSet<Endringsdato> endringsdatoEnumSet) {
        // #3+6+7+8
        if (revurdering.erManueltOpprettet() ||
            erInntektsmeldingMottattEtterGjeldendeVedtak(revurdering) ||
            erOpplysningerOmDød(revurdering) ||
            erAktørYtelseEndret(revurdering)) {

            endringsdatoEnumSet.add(Endringsdato.FØRSTE_UTTAKSDATO_GJELDENDE_VEDTAK);

        }
    }


    private boolean forrigeBehandlingUtenUttaksresultat(Behandling revurdering) {
        return uttakRepository.hentUttakResultatHvisEksisterer(finnForrigeBehandling(revurdering)).isPresent();
    }

    private boolean fødselHarSkjeddSidenForrigeBehandling(Behandling revurdering) {
        Behandling forrigeBehandling = finnForrigeBehandling(revurdering);
        Optional<LocalDate> fødselsdatoForrigeBehandling = finnFødselsdato(forrigeBehandling);
        Optional<LocalDate> fødselsdatoRevurdering = finnFødselsdato(revurdering);
        return !fødselsdatoForrigeBehandling.isPresent() && fødselsdatoRevurdering.isPresent();
    }

    private Optional<LocalDate> finnFødselsdato(Behandling behandling) {
        Optional<FamilieHendelse> familieHendelse = familieHendelseTjeneste.hentAggregat(behandling).getGjeldendeBekreftetVersjon();
        return familieHendelse.flatMap(FamilieHendelse::getFødselsdato);
    }

    private Optional<LocalDate> finnFørsteUttaksdatoGjeldendeVedtak(Behandling revurdering) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(revurdering);
        if (!uttakResultat.isPresent()) {
            return Optional.empty();
        }
        List<UttakResultatPeriodeEntitet> uttakPerioder = uttakResultat.get()
            .getGjeldendePerioder().getPerioder();
        return uttakPerioder.stream()
            .min(Comparator.comparing(UttakResultatPeriodeEntitet::getFom))
            .map(UttakResultatPeriodeEntitet::getFom);
    }

    private Optional<LocalDate> finnFørsteUttaksdatoSøknad(Behandling behandling) {
        YtelseFordelingAggregat ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregat(behandling);
        return ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder().stream()
            .map(OppgittPeriode::getFom).min(LOCAL_DATE_COMPARATOR);
    }

    private Optional<UttakResultatPeriodeEntitet> finnVedtattPeriodeMedOverlapp(LocalDate førsteUttaksdatoSøknad, UttakResultatEntitet uttakResultat) {
        List<UttakResultatPeriodeEntitet> uttakPerioder = uttakResultat.getGjeldendePerioder().getPerioder();
        return uttakPerioder
            .stream()
            .filter(p -> p.overlapper(førsteUttaksdatoSøknad))
            .findFirst();
    }

    private boolean erEndringssøknadMottatt(Behandling revurdering) {
        Optional<Søknad> søknadOptional = søknadRepository.hentSøknadHvisEksisterer(revurdering);
        return søknadOptional.isPresent() && søknadOptional.get().erEndringssøknad();
    }

    private boolean harManueltSattFørsteUttaksdato(Behandling revurdering) {
        return finnManueltSattFørsteUttaksdato(revurdering).isPresent();
    }

    private Optional<LocalDate> finnManueltSattFørsteUttaksdato(Behandling revurdering) {
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = ytelsesFordelingRepository.hentAggregatHvisEksisterer(revurdering);
        if (ytelseFordelingAggregat.isPresent()) {
            return ytelseFordelingAggregat.get().getAvklarteDatoer().map(AvklarteUttakDatoer::getFørsteUttaksDato);
        }
        return Optional.empty();
    }

    private boolean erInntektsmeldingMottattEtterGjeldendeVedtak(Behandling revurdering) {
        return !inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(revurdering).isEmpty();
    }

    private boolean erOpplysningerOmDød(Behandling revurdering) {
        return opplysningerOmDødEndringIdentifiserer.erEndret(revurdering);
    }

    private boolean erAktørYtelseEndret(Behandling revurdering) {
        InntektArbeidYtelseGrunnlag grunnlagFør = inntektArbeidYtelseTjeneste.hentFørsteVersjon(revurdering);
        InntektArbeidYtelseGrunnlag grunnlagNå = inntektArbeidYtelseTjeneste.hentAggregat(revurdering);
        AktørYtelseEndring aktørYtelseEndring = inntektArbeidYtelseTjeneste.endringPåAktørYtelse(grunnlagFør, grunnlagNå);
        return aktørYtelseEndring.erEndret();
    }

    private boolean erAdopsjon(Behandling revurdering) {
        FamilieHendelseType familieHendelseType = finnFamiliehendelse(revurdering).getType();
        return FamilieHendelseType.ADOPSJON.equals(familieHendelseType);
    }

    private Endringsdato finnEndringsdatoAdopsjon(Behandling revurdering) {
        if (finnAnkomstNorgeDato(revurdering).isPresent()) {
            return Endringsdato.ANKOMST_NORGE_DATO;
        } else {
            return Endringsdato.OMSORGSOVERTAKELSEDATO;
        }
    }

    private Optional<LocalDate> finnOmsorgsovertakelseDato(Behandling revurdering) {
        return finnFamiliehendelse(revurdering).getAdopsjon().map(Adopsjon::getOmsorgsovertakelseDato);
    }

    private Optional<LocalDate> finnAnkomstNorgeDato(Behandling revurdering) {
        return finnFamiliehendelse(revurdering).getAdopsjon().map(Adopsjon::getAnkomstNorgeDato);
    }

    private FamilieHendelse finnFamiliehendelse(Behandling revurdering) {
        FamilieHendelseGrunnlag hendelseGrunnlag = familieHendelseTjeneste.hentAggregat(revurdering);
        return hendelseGrunnlag.getGjeldendeVersjon();
    }

    private LocalDate finnFørsteDato(Set<Endringsdato> endringsdatoer, Behandling revurdering) {
        Set<LocalDate> datoer = new HashSet<>();

        for (Endringsdato endringsdato : endringsdatoer) {
            switch (endringsdato) {
                case FØDSELSDATO:
                    finnFødselsdato(revurdering).ifPresent(datoer::add);
                    break;
                case FØRSTE_UTTAKSDATO_GJELDENDE_VEDTAK:
                    finnFørsteUttaksdatoGjeldendeVedtak(finnForrigeBehandling(revurdering)).ifPresent(datoer::add);
                    break;
                case FØRSTE_UTTAKSDATO_SØKNAD:
                    finnFørsteUttaksdatoSøknad(revurdering).ifPresent(datoer::add);
                    break;
                case ENDRINGSDATO_I_BEHANDLING_SOM_FØRTE_TIL_BERØRT_BEHANDLING:
                    finnEndringsdatoForBerørtBehandling(revurdering, datoer);
                    break;
                case MANUELT_SATT_FØRSTE_UTTAKSDATO:
                    finnManueltSattFørsteUttaksdato(revurdering).ifPresent(datoer::add);
                    break;
                case OMSORGSOVERTAKELSEDATO:
                    finnOmsorgsovertakelseDato(revurdering).ifPresent(datoer::add);
                    break;
                case ANKOMST_NORGE_DATO:
                    finnAnkomstNorgeDato(revurdering).ifPresent(datoer::add);
                    break;
                case FØRSTE_UTTAKSDATO_SØKNAD_FORRIGE_BEHANDLING:
                    finnFørsteUttaksdatoSøknadForrigeBehandling(revurdering).ifPresent(datoer::add);
            }
        }

        return datoer.stream().min(LOCAL_DATE_COMPARATOR).orElse(null);
    }

    private Optional<LocalDate> finnFørsteUttaksdatoSøknadForrigeBehandling(Behandling revurdering) {
        return finnFørsteUttaksdatoSøknad(finnForrigeBehandling(revurdering));
    }

    private void finnEndringsdatoForBerørtBehandling(Behandling revurdering, Set<LocalDate> datoer) {
        Behandling annenForeldersBehandling = revurdering.getBerørtBehandling()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Berørt behandling mangler annen forelders behandling - skal ikke skje"));
        Optional<LocalDate> annenForeldersEndringsdato = finnFørsteUttaksdatoGjeldendeVedtak(annenForeldersBehandling);

        Optional<LocalDate> førsteUttaksdatoGjeldendeVedtak = finnFørsteUttaksdatoGjeldendeVedtak(finnForrigeBehandling(revurdering));
        if (førsteUttaksdatoGjeldendeVedtak.isPresent() && annenForeldersEndringsdato.isPresent()) {
            if (førsteUttaksdatoGjeldendeVedtak.get().isAfter(annenForeldersEndringsdato.get())) {
                datoer.add(førsteUttaksdatoGjeldendeVedtak.get());
            } else {
                datoer.add(annenForeldersEndringsdato.get());
            }
        } else {
            if (førsteUttaksdatoGjeldendeVedtak.isPresent()) {
                datoer.add(førsteUttaksdatoGjeldendeVedtak.get());
            }
            if (annenForeldersEndringsdato.isPresent()) {
                datoer.add(annenForeldersEndringsdato.get());
            }
        }
    }

    private Behandling finnForrigeBehandling(Behandling behandling) {
        return behandling.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Original behandling mangler på revurdering - skal ikke skje"));
    }

    private VLException kanIkkeUtledeException(Behandling revurdering) {
        return FeilFactory.create(FastsettuttaksgrunnalagFeil.class).kunneIkkeUtledeEndringsdato(revurdering.getId()).toException();
    }
}
