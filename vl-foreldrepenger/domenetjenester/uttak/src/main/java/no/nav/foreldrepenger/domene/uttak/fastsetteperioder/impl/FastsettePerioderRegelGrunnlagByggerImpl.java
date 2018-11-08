package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType.PERIODE_KAN_IKKE_AVKLARES;
import static no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePerioderRevurderingUtil.finnEndringsdatoRevurdering;
import static no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.FastsettePerioderRevurderingUtil.reduserteStønadskontoer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.UttakDokumentasjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.FordelingPeriodeKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeMapper;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakUtsettelseType;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidUtil;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidPåHeltidTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidTidslinjeTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.FastsettePerioderRegelGrunnlagBygger;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl.grunnlagbyggere.OmsorgOgRettGrunnlagBygger;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFerie;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFulltArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.jpa.tid.IntervalUtils;

@ApplicationScoped
public class FastsettePerioderRegelGrunnlagByggerImpl implements FastsettePerioderRegelGrunnlagBygger {

    private static final String IKKE_STØTTE_ÅRSAKTYPE = "Har ikke støtte for årsaktype ";
    private static final KodeMapper<OppholdÅrsak, Stønadskontotype> oppholdÅrsakStønadskontoMapper = initOppholdÅrsakStønadskontoMapper();
    private static final KodeMapper<StønadskontoType, Stønadskontotype> stønadskontotypeMapper = initStønadskontotypeMapper();
    private static final KodeMapper<UttakPeriodeType, Stønadskontotype> uttakPeriodeTypeMapper = initUttakPeriodeTypeMapper();
    private static final KodeMapper<UtsettelseÅrsak, Utsettelseårsaktype> utsettelseÅrsakMapper = initUtsettelseÅrsakMapper();
    private static final KodeMapper<OverføringÅrsak, no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak> overføringÅrsakMapper = initOverføringÅrsakMapper();
    private static final KodeMapper<UttakPeriodeVurderingType, PeriodeVurderingType> periodeVurderingTypeMapper = initVurderingTypeMapper();

    private BehandlingRepositoryProvider repositoryProvider;
    private ArbeidTidslinjeTjeneste arbeidTidslinjeTjeneste;
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;
    private UttakArbeidTjeneste uttakArbeidTjeneste;

    FastsettePerioderRegelGrunnlagByggerImpl() {
        // For CDI
    }

    @Inject
    public FastsettePerioderRegelGrunnlagByggerImpl(BehandlingRepositoryProvider repositoryProvider,
                                                    ArbeidTidslinjeTjeneste arbeidTidslinjeTjeneste,
                                                    RelatertBehandlingTjeneste relatertBehandlingTjeneste,
                                                    UttakArbeidTjeneste uttakArbeidTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.arbeidTidslinjeTjeneste = arbeidTidslinjeTjeneste;
        this.relatertBehandlingTjeneste = relatertBehandlingTjeneste;
        this.uttakArbeidTjeneste = uttakArbeidTjeneste;
    }

    @Override
    public FastsettePeriodeGrunnlag byggGrunnlag(Behandling behandling) {
        Map<AktivitetIdentifikator, ArbeidTidslinje> arbeidsprosenter = hentArbeidsprosenter(behandling);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        LocalDate familiehendelseDato = finnFamiliehendelsesdato(familieHendelseGrunnlag);
        List<AktivitetIdentifikator> aktiviteter = new ArrayList<>(arbeidsprosenter.keySet());
        Uttaksperiodegrense uttaksperiodegrense = repositoryProvider.getUttakRepository().hentUttaksperiodegrense(behandling.getId());
        FastsettePeriodeGrunnlagBuilder grunnlagBuilder = FastsettePeriodeGrunnlagBuilder.create()
            .medAktivitetIdentifikatorer(aktiviteter)
            .medSøknadstype(toSøknadstype(familieHendelseGrunnlag.getGjeldendeVersjon().getType()))
            .medBehandlingType(toBehandlingtype(behandling.getType(), behandling.erBerørtBehandling()))
            .medFamiliehendelseDato(familiehendelseDato)
            .medSøkerMor(erMor(behandling))
            .medFørsteLovligeUttaksdag(uttaksperiodegrense.getFørsteLovligeUttaksdag());

        leggTilStønadskontoer(behandling, grunnlagBuilder, aktiviteter);

        ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste = new ArbeidPåHeltidTjenesteImpl(behandling, uttakArbeidTjeneste);
        YtelseFordelingAggregat ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);
        Arbeidstidslinjer arbeidstidslinjer = new Arbeidstidslinjer(arbeidsprosenter);
        leggTilSøknadsperioder(ytelseFordelingAggregat, grunnlagBuilder, arbeidPåHeltidTjeneste, arbeidstidslinjer);
        leggTilAnnenPartsUttaksPerioder(grunnlagBuilder, behandling);
        leggTilArbeidstidslinjer(arbeidstidslinjer, grunnlagBuilder);
        leggTilSamtykke(grunnlagBuilder, ytelseFordelingAggregat);
        ytelseFordelingAggregat.getPerioderUtenOmsorg().ifPresent(p -> behandlePerioderUtenOmsorg(p, grunnlagBuilder));
        ytelseFordelingAggregat.getPerioderUttakDokumentasjon().ifPresent(uttakDokumentasjon -> leggTilDokumentertePerioder(uttakDokumentasjon, grunnlagBuilder));
        leggTilPerioderMedFulltEllerIArbeid(grunnlagBuilder, behandling);
        leggTilEndringssøknadMottattDato(grunnlagBuilder, behandling);
        leggTilEndringsdato(grunnlagBuilder, behandling);
        new OmsorgOgRettGrunnlagBygger().byggGrunnlag(grunnlagBuilder, behandling, ytelseFordelingAggregat);
        return grunnlagBuilder.build();
    }

    private void leggTilEndringsdato(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, Behandling behandling) {
        if (behandling.erRevurdering()) {
            LocalDate endringsdato = finnEndringsdatoRevurdering(behandling, repositoryProvider.getYtelsesFordelingRepository());
            grunnlagBuilder.medRevurderingEndringsdato(endringsdato);
        }
    }

    private void leggTilStønadskontoer(Behandling behandling, FastsettePeriodeGrunnlagBuilder grunnlagBuilder, List<AktivitetIdentifikator> aktiviteter) {
        if (behandling.erRevurdering()) {
            leggTilStønadskontoerRevurdering(behandling, grunnlagBuilder, aktiviteter);
        } else {
            leggTilStønadskontoerFørstegangs(behandling, grunnlagBuilder, aktiviteter);
        }
    }

    private void leggTilStønadskontoerRevurdering(Behandling behandling, FastsettePeriodeGrunnlagBuilder grunnlagBuilder, List<AktivitetIdentifikator> aktiviteter) {
        Behandling originalBehandling = FastsettePerioderRevurderingUtil.finnOriginalBehandling(behandling);
        Optional<UttakResultatEntitet> originaltUttak = repositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(originalBehandling);
        if (originaltUttak.isPresent()) {
            LocalDate endringsdato = finnEndringsdatoRevurdering(behandling, repositoryProvider.getYtelsesFordelingRepository());
            Set<Stønadskonto> originaleStønadskontoer = hentStønadskontoer(behandling);
            for (AktivitetIdentifikator aktivitetIdentifikator : aktiviteter) {
                Set<Stønadskonto> reduserte = reduserteStønadskontoer(originaltUttak.get(), endringsdato, originaleStønadskontoer, aktivitetIdentifikator);
                leggTilStønadskonto(grunnlagBuilder, aktivitetIdentifikator, reduserte);
            }
        } else {
            leggTilStønadskontoerFørstegangs(behandling, grunnlagBuilder, aktiviteter);
        }
    }

    private void leggTilStønadskonto(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, AktivitetIdentifikator aktivitetIdentifikator, Set<Stønadskonto> reduserte) {
        reduserte.forEach(stønadskonto -> grunnlagBuilder.medSaldo(aktivitetIdentifikator, toStønadskontotype(stønadskonto.getStønadskontoType()),
            stønadskonto.getMaxDager()));
    }

    private void leggTilStønadskontoerFørstegangs(Behandling behandling, FastsettePeriodeGrunnlagBuilder grunnlagBuilder, List<AktivitetIdentifikator> aktiviteter) {
        final Set<Stønadskonto> stønadskontoer = hentStønadskontoer(behandling);
        for (AktivitetIdentifikator aktivitetIdentifikator : aktiviteter) {
            leggTilStønadskonto(grunnlagBuilder, aktivitetIdentifikator, stønadskontoer);
        }
    }

    private void leggTilEndringssøknadMottattDato(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, Behandling behandling) {
        Søknad søknad = repositoryProvider.getSøknadRepository().hentSøknad(behandling);
        if (søknad.erEndringssøknad()) {
            grunnlagBuilder.medEndringssøknadMottattdato(søknad.getMottattDato());
        }
    }

    private void leggTilSamtykke(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                 YtelseFordelingAggregat ytelseFordelingAggregat) {
        grunnlagBuilder.medSamtykke(ytelseFordelingAggregat.getOppgittFordeling().getErAnnenForelderInformert());
    }

    private Map<AktivitetIdentifikator, ArbeidTidslinje> hentArbeidsprosenter(Behandling behandling) {
        Map<AktivitetIdentifikator, ArbeidTidslinje> arbeidsprosenter = arbeidTidslinjeTjeneste.lagTidslinjer(behandling);
        if (arbeidsprosenter == null || arbeidsprosenter.isEmpty()) {
            arbeidsprosenter = new HashMap<>();
            arbeidsprosenter.put(AktivitetIdentifikator.ingenAktivitet(), new ArbeidTidslinje.Builder().build());
        }
        return arbeidsprosenter;
    }

    private static boolean erMor(Behandling behandling) {
        return behandling.getFagsak().getRelasjonsRolleType().equals(RelasjonsRolleType.MORA);
    }

    private Set<Stønadskonto> hentStønadskontoer(Behandling behandling) {
        return repositoryProvider.getFagsakRelasjonRepository().finnRelasjonFor(behandling.getFagsak()).getStønadskontoberegning()
            .orElseThrow(() -> new IllegalArgumentException("Behandling mangler stønadsperioder"))
            .getStønadskontoer();
    }

    private void leggTilArbeidstidslinjer(Arbeidstidslinjer arbeidstidslinjer, FastsettePeriodeGrunnlagBuilder grunnlagBuilder) {
        for (Map.Entry<AktivitetIdentifikator, ArbeidTidslinje> entry : arbeidstidslinjer.getArbeidsprosenter().entrySet()) {
            grunnlagBuilder.medArbeid(entry.getKey(), entry.getValue());
        }
    }

    private void leggTilPerioderMedFulltEllerIArbeid(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                                     Behandling behandling) {
        Collection<Yrkesaktivitet> yrkesaktiviteter = uttakArbeidTjeneste.hentYrkesAktiviteterOrdinærtArbeidsforhold(behandling);

        //TreeMap med Startdato og Endedato av hver periode og arbeidsprosent på dette datoene
        Map<LocalDate, BigDecimal> arbeidsProsentEndringMap = opprettDatoArbeidsprosentTreeMap(yrkesaktiviteter);

        if (!arbeidsProsentEndringMap.isEmpty()) {
            //Finne ut arbeidsprosent på en periode fra (arbeidsProsentEndringMap)TreeMap
            leggTilPeriode(grunnlagBuilder, arbeidsProsentEndringMap);
        }
    }

    private void leggTilPeriode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                Map<LocalDate, BigDecimal> arbeidsProsentEndringMap) {
        BigDecimal arbeidProsentForPeriode = BigDecimal.ZERO;
        LocalDate fom = null;
        for (Map.Entry<LocalDate, BigDecimal> entry : arbeidsProsentEndringMap.entrySet()) {
            LocalDate tom = entry.getKey();
            if (fom != null) {
                if (arbeidProsentForPeriode.compareTo(BigDecimal.valueOf(100)) >= 0) {
                    //arbeidsprosent 100 eller mer
                    grunnlagBuilder.medPeriodeMedFulltArbeid(new PeriodeMedFulltArbeid(fom, tom));
                } else if (arbeidProsentForPeriode.compareTo(BigDecimal.ZERO) > 0) {
                    //arbeidsprosent mellom 0 og 100
                    grunnlagBuilder.medPeriodeMedArbeid(new PeriodeMedArbeid(fom, tom));
                }
            }
            arbeidProsentForPeriode = entry.getValue();
            fom = tom;
        }
    }

    private Map<LocalDate, BigDecimal> opprettDatoArbeidsprosentTreeMap(Collection<Yrkesaktivitet> yrkesaktiviteter) {
        List<AktivitetsAvtale> aktivitetsAvtaler = yrkesaktiviteter.stream()
            .flatMap(yrkesaktivitet -> yrkesaktivitet.getAktivitetsAvtaler().stream()).collect(Collectors.toList());

        Map<LocalDate, BigDecimal> arbeidsProsentEndringMap = new TreeMap<>();
        yrkesaktiviteter.forEach(yrkesaktivitet ->
            yrkesaktivitet.getPermisjon().forEach(permisjon -> oppdatereTreeMap(yrkesaktivitet, aktivitetsAvtaler, arbeidsProsentEndringMap, permisjon))
        );

        return arbeidsProsentEndringMap;
    }

    private void oppdatereTreeMap(Yrkesaktivitet yrkesaktivitet, List<AktivitetsAvtale> aktivitetsAvtaler, Map<LocalDate, BigDecimal> arbeidsProsentEndringMap, Permisjon permisjon) {
        LocalDate fomDato = permisjon.getFraOgMed();
        LocalDate tomDato = permisjon.getTilOgMed();
        //arbeidsprosent må finne fra oppringelig(fra avtale) og permisjonsprosent
        BigDecimal arbeidsProsent = finnArbeidsProsentPåStartDatoAvHverPermisjonPeriode(yrkesaktivitet, permisjon, aktivitetsAvtaler);
        //add fomDato
        if (arbeidsProsentEndringMap.containsKey(fomDato)) {
            BigDecimal newArbeidsProsent = arbeidsProsentEndringMap.get(fomDato).add(arbeidsProsent);
            arbeidsProsentEndringMap.put(fomDato, newArbeidsProsent);
        } else {
            arbeidsProsentEndringMap.put(fomDato, arbeidsProsent);
        }
        //minus tomdato
        if (arbeidsProsentEndringMap.containsKey(tomDato)) {
            BigDecimal newArbeidsProsent = arbeidsProsentEndringMap.get(fomDato).add(arbeidsProsent.negate());
            arbeidsProsentEndringMap.put(tomDato, newArbeidsProsent);
        } else {
            arbeidsProsentEndringMap.put(tomDato, arbeidsProsent.negate());
        }
    }

    private static BigDecimal finnArbeidsProsentPåStartDatoAvHverPermisjonPeriode(Yrkesaktivitet yrkesaktivitet, Permisjon permisjon, List<AktivitetsAvtale> aktivitetsAvtaler) {

        Optional<AktivitetsAvtale> avtale = aktivitetsAvtaler.stream()
            .filter(aktivitetsAvtale -> erSammeOrgnr(aktivitetsAvtale.getYrkesaktivitet().getArbeidsgiver().getVirksomhet().getOrgnr(), yrkesaktivitet))
            .filter(aktivitetsAvtale -> {
                String arbeidsforholdId = aktivitetsAvtale.getYrkesaktivitet().getArbeidsforholdRef().isPresent() ? aktivitetsAvtale.getYrkesaktivitet().getArbeidsforholdRef().get().getReferanse() : null;
                Optional<ArbeidsforholdRef> arbeidsforholdRef = yrkesaktivitet.getArbeidsforholdRef();
                if (arbeidsforholdId != null && arbeidsforholdRef.isPresent()) {
                    return Objects.equals(arbeidsforholdRef.get().getReferanse(), arbeidsforholdId);
                }
                return true;
            }).filter(aktivitetsAvtale -> inneholder(aktivitetsAvtale, permisjon.getFraOgMed())).findFirst();

        if (avtale.isPresent()) {
            return FastsettePerioderUtil.finnArbeidstidsprosentFraPermisjonPeriode(permisjon,
                UttakArbeidUtil.hentStillingsprosent(avtale.get()));
        }
        return BigDecimal.ZERO;
    }

    private static boolean inneholder(AktivitetsAvtale aktivitetsAvtale, LocalDate permisjonFraDato) {
        return (aktivitetsAvtale.getFraOgMed().isBefore(permisjonFraDato) || aktivitetsAvtale.getFraOgMed().isEqual(permisjonFraDato))
            && (aktivitetsAvtale.getTilOgMed().isAfter(permisjonFraDato) || aktivitetsAvtale.getTilOgMed().isEqual(permisjonFraDato));
    }

    private static boolean erSammeOrgnr(String orgnr, Yrkesaktivitet yrkesaktivitet) {
        return Objects.equals(orgnr, yrkesaktivitet.getArbeidsgiver().getVirksomhet().getOrgnr());
    }

    private static void leggTilSøknadsperioder(YtelseFordelingAggregat ytelseFordelingAggregat,
                                               FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                               ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste,
                                               Arbeidstidslinjer arbeidstidslinjer) {
        List<OppgittPeriode> søknadPerioder = ytelseFordelingAggregat.getGjeldendeSøknadsperioder().getOppgittePerioder();
        validerIkkeOverlappSøknadsperioder(søknadPerioder);

        søknadPerioder.stream()
            .sorted(Comparator.comparing(OppgittPeriode::getFom))
            .forEachOrdered(periode -> leggTilPeriode(grunnlagBuilder, periode, arbeidPåHeltidTjeneste, arbeidstidslinjer));
    }

    private void leggTilAnnenPartsUttaksPerioder(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, Behandling behandling) {
        Optional<UttakResultatEntitet> uttakResultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(behandling);
        if (uttakResultat.isPresent()) {
            List<UttakResultatPeriodeEntitet> perioder = uttakResultat.get().getGjeldendePerioder().getPerioder();
            perioder.stream()
                .filter(this::erInnvilgetPeriodeEllerHarTrekkdager)
                .sorted(Comparator.comparing(UttakResultatPeriodeEntitet::getFom))
                .forEachOrdered(periode -> leggTilAnnenPartsUttaksPeriode(periode, grunnlagBuilder));
        }
    }

    private boolean erInnvilgetPeriodeEllerHarTrekkdager(UttakResultatPeriodeEntitet p) {
        boolean harTrekkdager = p.getAktiviteter().stream().anyMatch(akt -> akt.getTrekkdager() > 0);
        return p.getPeriodeResultatType().equals(PeriodeResultatType.INNVILGET) || harTrekkdager;
    }

    private void leggTilAnnenPartsUttaksPeriode(UttakResultatPeriodeEntitet periode, FastsettePeriodeGrunnlagBuilder grunnlagBuilder) {
        FastsattPeriodeAnnenPart.Builder builder = new FastsattPeriodeAnnenPart.Builder(periode.getFom(), periode.getTom(),
            periode.isSamtidigUttak(), erInnvilgetUtsettelse(periode))
            .medFlerbarnsdager(periode.isFlerbarnsdager());

        periode.getAktiviteter().forEach(aktivitet -> builder
            .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(mapAktiviet(aktivitet.getUttakAktivitet()),
                toStønadskontotype(aktivitet.getTrekkonto()), aktivitet.getTrekkdager(), aktivitet.getUtbetalingsprosent(),
                aktivitet.getArbeidsprosent())));
        grunnlagBuilder.medUttakPeriodeForAnnenPart(builder.build());
    }

    private boolean erInnvilgetUtsettelse(UttakResultatPeriodeEntitet periode) {
        return (PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()) && Arrays.asList(UttakUtsettelseType.ARBEID,
            UttakUtsettelseType.BARN_INNLAGT, UttakUtsettelseType.FERIE, UttakUtsettelseType.SYKDOM_SKADE,
            UttakUtsettelseType.SØKER_INNLAGT).contains(periode.getUtsettelseType()));
    }

    private AktivitetIdentifikator mapAktiviet(UttakAktivitetEntitet aktivitetEntitet) {
        UttakArbeidType uttakArbeidType = aktivitetEntitet.getUttakArbeidType();
        if (UttakArbeidType.ORDINÆRT_ARBEID.equals(uttakArbeidType)) {
            return AktivitetIdentifikator.forArbeid(aktivitetEntitet.getArbeidsforholdOrgnr(), aktivitetEntitet.getArbeidsforholdId());
        }
        if (UttakArbeidType.FRILANS.equals(uttakArbeidType)) {
            return AktivitetIdentifikator.forFrilans();
        }
        if (UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE.equals(uttakArbeidType)) {
            return AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        }
        if (UttakArbeidType.ANNET.equals(uttakArbeidType)) {
            return AktivitetIdentifikator.annenAktivitet();
        }
        return AktivitetIdentifikator.ingenAktivitet();
    }

    private static void validerIkkeOverlappSøknadsperioder(List<OppgittPeriode> søknadPerioder) {
        int size = søknadPerioder.size();
        for (int i = 0; i < size; i++) {
            OppgittPeriode periode1 = søknadPerioder.get(i);

            IntervalUtils p1 = new IntervalUtils(periode1.getFom(), periode1.getTom());
            for (int j = i + 1; j < size; j++) {
                OppgittPeriode periode2 = søknadPerioder.get(j);
                IntervalUtils p2 = new IntervalUtils(periode2.getFom(), periode2.getTom());
                if (p1.overlapper(p2)) {
                    throw new IllegalStateException("Støtter ikke å ha overlappende søknadsperioder, men fikk overlapp mellom periodene " + p1 + " og " + p2);
                }
            }
        }
    }

    private static void behandlePerioderUtenOmsorg(PerioderUtenOmsorg søknadPerioder, FastsettePeriodeGrunnlagBuilder grunnlagBuilder) {
        søknadPerioder.getPerioder().stream()
            .sorted(Comparator.comparing(p -> p.getPeriode().getFomDato()))
            .forEachOrdered(op -> leggTilPeriode(grunnlagBuilder, op));
    }

    private static void leggTilDokumentertePerioder(PerioderUttakDokumentasjon perioderUttakDokumentasjon, FastsettePeriodeGrunnlagBuilder grunnlagBuilder) {
        perioderUttakDokumentasjon.getPerioder().stream()
            .sorted(Comparator.comparing(uttakDokumentasjon -> uttakDokumentasjon.getPeriode().getFomDato()))
            .forEachOrdered(uttakDok -> leggTilPeriode(grunnlagBuilder, uttakDok));
    }

    private static void leggTilPeriode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, PeriodeUttakDokumentasjon uttakDokumentasjon) {
        DatoIntervallEntitet periode = uttakDokumentasjon.getPeriode();
        if (UttakDokumentasjonType.SYK_SØKER.equals(uttakDokumentasjon.getDokumentasjonType())) {
            grunnlagBuilder.medPeriodeMedSykdomEllerSkade(new PeriodeMedSykdomEllerSkade(periode.getFomDato(), periode.getTomDato()));
        } else if (UttakDokumentasjonType.INNLAGT_BARN.equals(uttakDokumentasjon.getDokumentasjonType())) {
            grunnlagBuilder.medPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(periode.getFomDato(), periode.getTomDato()));
        } else if (UttakDokumentasjonType.INNLAGT_SØKER.equals(uttakDokumentasjon.getDokumentasjonType())) {
            grunnlagBuilder.medPeriodeMedInnleggelse(new PeriodeMedInnleggelse(periode.getFomDato(), periode.getTomDato()));
        } else {
            grunnlagBuilder.medGyldigGrunnForTidligOppstartPeriode(periode.getFomDato(), periode.getTomDato());
        }
    }

    private static void leggTilPeriode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                       OppgittPeriode oppgittPeriode,
                                       ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste,
                                       Arbeidstidslinjer arbeidstidslinjer) {
        UttakPeriodeType oppgittPeriodeType = oppgittPeriode.getPeriodeType();
        Stønadskontotype stønadskontotype = toStønadskontotype(oppgittPeriodeType);

        if (UttakPeriodeType.STØNADSPERIODETYPER.contains(oppgittPeriodeType)) {
            if (oppgittPeriode.getÅrsak() instanceof UtsettelseÅrsak) {
                leggTilUtsettelseperiode(grunnlagBuilder, oppgittPeriode, stønadskontotype, arbeidPåHeltidTjeneste);
            } else if (oppgittPeriode.getÅrsak() instanceof OverføringÅrsak) {
                leggTilOverføringPeriode(grunnlagBuilder, oppgittPeriode, stønadskontotype);
            } else {
                leggTilStønadsperiode(grunnlagBuilder, oppgittPeriode, stønadskontotype, arbeidstidslinjer);
            }
        } else if (UttakPeriodeType.ANNET.equals(oppgittPeriodeType)) {
            leggTilOppholdPeriode(grunnlagBuilder, oppgittPeriode);
        } else {
            throw new IllegalArgumentException("Ikke-støttet UttakPeriodeType: " + oppgittPeriodeType);
        }
    }

    private static void leggTilOverføringPeriode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                                 OppgittPeriode oppgittPeriode,
                                                 Stønadskontotype stønadskontotype) {
        if (OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE.equals(oppgittPeriode.getÅrsak()) || OverføringÅrsak.SYKDOM_ANNEN_FORELDER.equals(oppgittPeriode.getÅrsak())) {
            no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak overføringÅrsak = toOverføringÅrsak((OverføringÅrsak) oppgittPeriode.getÅrsak());
            PeriodeVurderingType periodeVurderingType = toSaksbehandlerPeriodeResultat(oppgittPeriode.getPeriodeVurderingType());
            grunnlagBuilder.medOverføringAvKvote(stønadskontotype, toPeriodeKilde(oppgittPeriode.getPeriodeKilde()), oppgittPeriode.getFom(), oppgittPeriode.getTom(), overføringÅrsak, periodeVurderingType,
                oppgittPeriode.isSamtidigUttak(), oppgittPeriode.isFlerbarnsdager());
        }
    }

    private static void leggTilUtsettelseperiode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                                 OppgittPeriode oppgittPeriode,
                                                 Stønadskontotype stønadskontotype,
                                                 ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste) {
        Utsettelseårsaktype utsettelseårsaktype = toUtsettelseårsaktype((UtsettelseÅrsak) oppgittPeriode.getÅrsak());
        PeriodeVurderingType periodeVurderingType = toSaksbehandlerPeriodeResultat(oppgittPeriode.getPeriodeVurderingType());

        if (erArbeidEllerFerie(utsettelseårsaktype) && !PERIODE_KAN_IKKE_AVKLARES.equals(oppgittPeriode.getPeriodeVurderingType())) {
            leggTilArbeidOgFerieUtsettelse(utsettelseårsaktype, oppgittPeriode, grunnlagBuilder, arbeidPåHeltidTjeneste);
        }
        grunnlagBuilder.medUtsettelsePeriode(stønadskontotype, toPeriodeKilde(oppgittPeriode.getPeriodeKilde()), oppgittPeriode.getFom(), oppgittPeriode.getTom(), utsettelseårsaktype, periodeVurderingType,
            oppgittPeriode.isSamtidigUttak(), oppgittPeriode.isFlerbarnsdager());
    }

    private static boolean erArbeidEllerFerie(Utsettelseårsaktype utsettelseårsaktype) {
        return Utsettelseårsaktype.ARBEID.equals(utsettelseårsaktype) || Utsettelseårsaktype.FERIE.equals(utsettelseårsaktype);
    }

    private static void leggTilArbeidOgFerieUtsettelse(Utsettelseårsaktype utsettelseårsaktype,
                                                       OppgittPeriode oppgittPeriode,
                                                       FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                                       ArbeidPåHeltidTjeneste arbeidPåHeltidTjeneste) {
        if (Utsettelseårsaktype.ARBEID.equals(utsettelseårsaktype)) {
            if (arbeidPåHeltidTjeneste.jobberFulltid(oppgittPeriode)) {
                grunnlagBuilder.medPeriodeMedFulltArbeid(new PeriodeMedFulltArbeid(oppgittPeriode.getFom(), oppgittPeriode.getTom()));
            }
        } else {
            grunnlagBuilder.medPeriodeMedFerie(new PeriodeMedFerie(oppgittPeriode.getFom(), oppgittPeriode.getTom()));
        }
    }

    private static void leggTilPeriode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, PeriodeUtenOmsorg periodeUtenOmsorg) { //NOSONAR
        LocalDate fom = periodeUtenOmsorg.getPeriode().getFomDato();
        LocalDate tom = periodeUtenOmsorg.getPeriode().getTomDato();
        grunnlagBuilder.medPeriodeUtenOmsorg(fom, tom);
    }

    private static void leggTilStønadsperiode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                              OppgittPeriode oppgittPeriode,
                                              Stønadskontotype stønadskontotype,
                                              Arbeidstidslinjer arbeidstidslinjer) {
        LocalDate fom = oppgittPeriode.getFom();
        LocalDate tom = oppgittPeriode.getTom();
        if (periodeErGradert(oppgittPeriode)) {
            leggTilGradertStønadsperiode(grunnlagBuilder, oppgittPeriode, stønadskontotype, arbeidstidslinjer, fom, tom);
        } else {
            grunnlagBuilder.medStønadsPeriode(stønadskontotype, toPeriodeKilde(oppgittPeriode.getPeriodeKilde()), fom, tom, toSaksbehandlerPeriodeResultat(oppgittPeriode.getPeriodeVurderingType()),
                oppgittPeriode.isSamtidigUttak(), oppgittPeriode.isFlerbarnsdager());
        }
    }

    private static void leggTilGradertStønadsperiode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder,
                                                     OppgittPeriode oppgittPeriode,
                                                     Stønadskontotype stønadskontotype,
                                                     Arbeidstidslinjer arbeidstidslinjer,
                                                     LocalDate fom,
                                                     LocalDate tom) {
        PeriodeVurderingType periodeVurderingType = toSaksbehandlerPeriodeResultat(oppgittPeriode.getPeriodeVurderingType());
        List<AktivitetIdentifikator> gradertAktivitet = finnGraderteAktiviteter(oppgittPeriode, arbeidstidslinjer);
        grunnlagBuilder.medGradertStønadsPeriode(stønadskontotype, toPeriodeKilde(oppgittPeriode.getPeriodeKilde()), fom, tom, gradertAktivitet, oppgittPeriode.getArbeidsprosent(), periodeVurderingType,
            oppgittPeriode.isSamtidigUttak(), oppgittPeriode.isFlerbarnsdager());
    }

    private static List<AktivitetIdentifikator> finnGraderteAktiviteter(OppgittPeriode oppgittPeriode, Arbeidstidslinjer arbeidstidslinjer) {
        if (!oppgittPeriode.getErArbeidstaker()) {
            return Collections.singletonList(finnGradertFrilansSelvstendigNæringsdrivendeAktivitet(oppgittPeriode, arbeidstidslinjer));
        }
        return arbeidstidslinjer.graderteAktiviteter(oppgittPeriode.getFom(), oppgittPeriode.getTom(), oppgittPeriode.getVirksomhet().getOrgnr());
    }

    private static AktivitetIdentifikator finnGradertFrilansSelvstendigNæringsdrivendeAktivitet(OppgittPeriode oppgittPeriode, Arbeidstidslinjer arbeidstidslinjer) {
        return arbeidstidslinjer.gradertFrilansSelvstendigNæringsdrivendeAktivitet(oppgittPeriode.getFom(), oppgittPeriode.getTom());
    }

    private static boolean periodeErGradert(OppgittPeriode oppgittPeriode) {
        return oppgittPeriode.getArbeidsprosent() != null;
    }

    private static void leggTilOppholdPeriode(FastsettePeriodeGrunnlagBuilder grunnlagBuilder, OppgittPeriode oppgittPeriode) {
        Årsak årsak = oppgittPeriode.getÅrsak();
        List<OppholdÅrsak> årsakKvoteAnnenForelder = Arrays.asList(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER, OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER, OppholdÅrsak.KVOTE_FORELDREPENGER_ANNEN_FORELDER);
        if (årsak instanceof OppholdÅrsak) {
            OppholdÅrsak oppholdÅrsak = (OppholdÅrsak) årsak;
            Stønadskontotype stønadskontotype = toStønadskontotype(oppholdÅrsak);
            Oppholdårsaktype oppholdårsaktype;
            if (oppholdÅrsak.equals(OppholdÅrsak.KVOTE_FELLESPERIODE_ANNEN_FORELDER)) {
                oppholdårsaktype = Oppholdårsaktype.KVOTE_FELLESPERIODE_ANNEN_FORELDER;
            } else if (årsakKvoteAnnenForelder.contains(oppholdÅrsak)) {
                oppholdårsaktype = Oppholdårsaktype.KVOTE_ANNEN_FORELDER;
            } else {
               throw new UnsupportedOperationException(IKKE_STØTTE_ÅRSAKTYPE + oppholdÅrsak.getKode());
            }
            grunnlagBuilder.medOppholdPeriode(stønadskontotype, oppholdårsaktype, toPeriodeKilde(oppgittPeriode.getPeriodeKilde()), oppgittPeriode.getFom(), oppgittPeriode.getTom());
        } else {
            throw new IllegalArgumentException("Ikke-støttet årsakstype: " + årsak);
        }
    }

    private static PeriodeKilde toPeriodeKilde(FordelingPeriodeKilde fordelingPeriodeKilde) {
        if (FordelingPeriodeKilde.TIDLIGERE_VEDTAK.equals(fordelingPeriodeKilde)) {
            return PeriodeKilde.TIDLIGERE_VEDTAK;
        } else if (FordelingPeriodeKilde.SØKNAD.equals(fordelingPeriodeKilde)) {
            return PeriodeKilde.SØKNAD;
        }
        throw new UnsupportedOperationException("Har ikke støtte for periodekilde " + fordelingPeriodeKilde.getKode());
    }

    private static Stønadskontotype toStønadskontotype(OppholdÅrsak årsakType) {
        return oppholdÅrsakStønadskontoMapper
            .map(årsakType)
            .orElseThrow(() -> new UnsupportedOperationException(IKKE_STØTTE_ÅRSAKTYPE + årsakType.getKode()));
    }

    private static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak toOverføringÅrsak(OverføringÅrsak overføringÅrsak) {
        return overføringÅrsakMapper
            .map(overføringÅrsak)
            .orElseThrow(() -> new UnsupportedOperationException(IKKE_STØTTE_ÅRSAKTYPE + overføringÅrsak.getKode()));
    }

    private static PeriodeVurderingType toSaksbehandlerPeriodeResultat(UttakPeriodeVurderingType uttakPeriodeVurderingType) {
        return periodeVurderingTypeMapper
            .map(uttakPeriodeVurderingType)
            .orElseThrow(() -> new UnsupportedOperationException(IKKE_STØTTE_ÅRSAKTYPE + uttakPeriodeVurderingType.getKode()));
    }

    private static Utsettelseårsaktype toUtsettelseårsaktype(UtsettelseÅrsak årsakType) {
        return utsettelseÅrsakMapper
            .map(årsakType)
            .orElseThrow(() -> new UnsupportedOperationException(IKKE_STØTTE_ÅRSAKTYPE + årsakType.getKode()));
    }

    private static Søknadstype toSøknadstype(FamilieHendelseType familieHendelseType) {
        if (Stream.of(FamilieHendelseType.FØDSEL, FamilieHendelseType.TERMIN).anyMatch(familieHendelseType::equals)) {
            return Søknadstype.FØDSEL;
        }
        // TODO SOMMERFUGL støtte for omsorgsovertakelse når dette kommer med i uttak-regler
        return Søknadstype.ADOPSJON;
    }

    private static Behandlingtype toBehandlingtype(BehandlingType behandlingType, boolean erBerørtBehandling) {
        if (BehandlingType.REVURDERING.equals(behandlingType)) {
            if (erBerørtBehandling) {
                return Behandlingtype.REVURDERING_BERØRT_SAK;
            }
            return Behandlingtype.REVURDERING;
        }
        return Behandlingtype.FØRSTEGANGSSØKNAD;
    }

    private static Stønadskontotype toStønadskontotype(StønadskontoType stønadskontoType) {
        return stønadskontotypeMapper
            .map(stønadskontoType)
            .orElseThrow(() -> new UnsupportedOperationException(String.format("Har ikke støtte for søknadstype %s", stønadskontoType.getNavn())));
    }

    private static Stønadskontotype toStønadskontotype(UttakPeriodeType uttakPeriodeType) {
        return uttakPeriodeTypeMapper
            .map(uttakPeriodeType)
            .orElse(Stønadskontotype.UKJENT);
    }

    private static LocalDate finnFamiliehendelsesdato(FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        FamilieHendelse familieHendelse = familieHendelseGrunnlag.getGjeldendeVersjon();
        if (familieHendelse.getGjelderFødsel()) {
            return familieHendelseGrunnlag.finnGjeldendeFødselsdato();
        }

        Optional<Adopsjon> adopsjon = familieHendelseGrunnlag.getGjeldendeAdopsjon();
        if (adopsjon.isPresent()) {
            return adopsjon.get().getOmsorgsovertakelseDato();
        }
        throw new IllegalArgumentException("Fant ikke familiehendelsedato");
    }

    private static KodeMapper<UttakPeriodeVurderingType, PeriodeVurderingType> initVurderingTypeMapper() {
        return KodeMapper
            .medMapping(UttakPeriodeVurderingType.PERIODE_OK, PeriodeVurderingType.PERIODE_OK)
            .medMapping(UttakPeriodeVurderingType.PERIODE_OK_ENDRET, PeriodeVurderingType.ENDRE_PERIODE)
            .medMapping(UttakPeriodeVurderingType.PERIODE_KAN_IKKE_AVKLARES, PeriodeVurderingType.UAVKLART_PERIODE)
            .medMapping(UttakPeriodeVurderingType.PERIODE_IKKE_VURDERT, PeriodeVurderingType.IKKE_VURDERT)
            .build();
    }

    private static KodeMapper<OverføringÅrsak, no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak> initOverføringÅrsakMapper() {
        return KodeMapper
            .medMapping(OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE, no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.INNLEGGELSE)
            .medMapping(OverføringÅrsak.SYKDOM_ANNEN_FORELDER, no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak.SYKDOM_ELLER_SKADE)
            .build();
    }

    private static KodeMapper<UtsettelseÅrsak, Utsettelseårsaktype> initUtsettelseÅrsakMapper() {
        return KodeMapper
            .medMapping(UtsettelseÅrsak.FERIE, Utsettelseårsaktype.FERIE)
            .medMapping(UtsettelseÅrsak.ARBEID, Utsettelseårsaktype.ARBEID)
            .medMapping(UtsettelseÅrsak.SYKDOM, Utsettelseårsaktype.SYKDOM_SKADE)
            .medMapping(UtsettelseÅrsak.INSTITUSJON_SØKER, Utsettelseårsaktype.INNLAGT_HELSEINSTITUSJON)
            .medMapping(UtsettelseÅrsak.INSTITUSJON_BARN, Utsettelseårsaktype.INNLAGT_BARN)
            .build();
    }

    private static KodeMapper<OppholdÅrsak, Stønadskontotype> initOppholdÅrsakStønadskontoMapper() {
        return KodeMapper
            .medMapping(OppholdÅrsak.KVOTE_FELLESPERIODE_ANNEN_FORELDER, Stønadskontotype.FELLESPERIODE)
            .medMapping(OppholdÅrsak.FEDREKVOTE_ANNEN_FORELDER, Stønadskontotype.FEDREKVOTE)
            .medMapping(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER, Stønadskontotype.MØDREKVOTE)
            .medMapping(OppholdÅrsak.KVOTE_FORELDREPENGER_ANNEN_FORELDER, Stønadskontotype.FORELDREPENGER)
            .build();
    }

    private static KodeMapper<StønadskontoType, Stønadskontotype> initStønadskontotypeMapper() {
        return KodeMapper
            .medMapping(StønadskontoType.FORELDREPENGER, Stønadskontotype.FORELDREPENGER)
            .medMapping(StønadskontoType.FELLESPERIODE, Stønadskontotype.FELLESPERIODE)
            .medMapping(StønadskontoType.MØDREKVOTE, Stønadskontotype.MØDREKVOTE)
            .medMapping(StønadskontoType.FEDREKVOTE, Stønadskontotype.FEDREKVOTE)
            .medMapping(StønadskontoType.FLERBARNSDAGER, Stønadskontotype.FLERBARNSDAGER)
            .medMapping(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)
            .build();
    }

    private static KodeMapper<UttakPeriodeType, Stønadskontotype> initUttakPeriodeTypeMapper() {
        return KodeMapper
            .medMapping(UttakPeriodeType.FORELDREPENGER, Stønadskontotype.FORELDREPENGER)
            .medMapping(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL)
            .medMapping(UttakPeriodeType.FELLESPERIODE, Stønadskontotype.FELLESPERIODE)
            .medMapping(UttakPeriodeType.MØDREKVOTE, Stønadskontotype.MØDREKVOTE)
            .medMapping(UttakPeriodeType.FEDREKVOTE, Stønadskontotype.FEDREKVOTE)
            .build();
    }
}
