package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Permisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Gradering;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.domene.uttak.UttakArbeidTjeneste;
import no.nav.foreldrepenger.domene.uttak.UttakStillingsprosentTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.ArbeidTidslinjeTjeneste;
import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakBeregningsandelTjeneste;
import no.nav.foreldrepenger.domene.uttak.perioder.PerioderUtenHelgUtil;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class ArbeidTidslinjeTjenesteImpl implements ArbeidTidslinjeTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private UttakStillingsprosentTjeneste stillingsprosentTjeneste;
    private UttakBeregningsandelTjeneste beregningsandelTjeneste;
    private UttakArbeidTjeneste uttakArbeidTjeneste;

    ArbeidTidslinjeTjenesteImpl() {
        // For CDI
    }

    @Inject
    public ArbeidTidslinjeTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                       UttakStillingsprosentTjeneste stillingsprosentTjeneste,
                                       UttakBeregningsandelTjeneste beregningsandelTjeneste,
                                       UttakArbeidTjeneste uttakArbeidTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.stillingsprosentTjeneste = stillingsprosentTjeneste;
        this.beregningsandelTjeneste = beregningsandelTjeneste;
        this.uttakArbeidTjeneste = uttakArbeidTjeneste;
    }

    @Override
    public Map<AktivitetIdentifikator, ArbeidTidslinje> lagTidslinjer(Behandling behandling) {

        Map<AktivitetIdentifikator, ArbeidTidslinje> resultat = new HashMap<>();
        List<OppgittPeriode> oppgittePerioder = hentOppgittePerioder(behandling);

        List<BeregningsgrunnlagPrStatusOgAndel> andeler = beregningsandelTjeneste.hentAndeler(behandling);
        boolean erSelvNæringsdrivende = erSelvstendigNæringsdrivende(andeler);
        andeler.forEach(beregningsgrunnlagPrStatusOgAndel -> leggTilAktivetetForAndel(behandling, resultat, beregningsgrunnlagPrStatusOgAndel,
            erSelvNæringsdrivende, oppgittePerioder));
        return resultat;
    }

    private void leggTilAktivetetForAndel(Behandling behandling,
                                          Map<AktivitetIdentifikator, ArbeidTidslinje> resultat,
                                          BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel,
                                          boolean erSelvNæringsdrivende,
                                          List<OppgittPeriode> oppgittePerioder) {
        if (beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erArbeidstaker()) {
            leggTilArbeidstaker(behandling, resultat, beregningsgrunnlagPrStatusOgAndel, oppgittePerioder);
        } else if (beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erFrilanser()) {
            leggTilFrilans(resultat, oppgittePerioder, erSelvNæringsdrivende);
        } else if (beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus().erSelvstendigNæringsdrivende()) {
            leggTilNæringsdrivende(resultat, oppgittePerioder);
        } else {
            leggTilAnnet(resultat, oppgittePerioder);
        }
    }

    private void leggTilArbeidstaker(Behandling behandling,
                                     Map<AktivitetIdentifikator, ArbeidTidslinje> resultat,
                                     BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel,
                                     List<OppgittPeriode> oppgittePerioder) {
        String orgNr = beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold()
            .map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr)
            .orElse(null);
        String arbeidsforholdId = beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold()
            .flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef)
            .map(ArbeidsforholdRef::getReferanse)
            .orElse(null);
        AktivitetIdentifikator aktivitet = AktivitetIdentifikator.forArbeid(orgNr, arbeidsforholdId);
        resultat.put(aktivitet, arbeidsTidslinjeForOrdinærtArbeid(behandling, oppgittePerioder, aktivitet));
    }

    private ArbeidTidslinje arbeidsTidslinjeForOrdinærtArbeid(Behandling behandling,
                                                              List<OppgittPeriode> oppgittePerioder,
                                                              AktivitetIdentifikator aktivitet) {
        ArbeidTidslinje.Builder builder = new ArbeidTidslinje.Builder();
        String orgnr = aktivitet.getOrgNr();
        String arbeidsforholdId = aktivitet.getArbeidsforholdId();
        for (OppgittPeriode søknadsperiode : oppgittePerioder) {
            if (søktOmGraderingForArbeidsforhold(behandling, søknadsperiode, orgnr, aktivitet.getArbeidsforholdId())) {
                BigDecimal stillingsprosent = finnStillingsprosent(behandling, orgnr, arbeidsforholdId, søknadsperiode.getFom());
                Arbeid arbeid = Arbeid.forGradertOrdinærtArbeid(søknadsperiode.getArbeidsprosent(), stillingsprosent);
                builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), arbeid);
            } else if (søktOmUtsettelsePgaArbeid(søknadsperiode)) {
                leggTilArbeidBasertPåUtsettelsePgaArbeidIinntektsmelding(behandling, builder, orgnr, arbeidsforholdId, søknadsperiode);
            } else {
                leggTilPeriodeForOrdinærtArbeid(behandling, søknadsperiode, builder, orgnr, arbeidsforholdId);
            }
        }

        return builder.build();
    }

    private void leggTilArbeidBasertPåUtsettelsePgaArbeidIinntektsmelding(Behandling behandling,
                                                                          ArbeidTidslinje.Builder builder,
                                                                          String orgnr,
                                                                          String arbeidsforholdId,
                                                                          OppgittPeriode søknadsperiode) {
        BigDecimal stillingsprosent = finnStillingsprosent(behandling, orgnr, arbeidsforholdId, søknadsperiode.getFom());
        //Arbeidstidsprosent settes til stillingsprosent ved utsettelse pga arbeid
        builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), Arbeid.forOrdinærtArbeid(stillingsprosent, stillingsprosent, null));
    }

    private void leggTilAnnet(Map<AktivitetIdentifikator, ArbeidTidslinje> resultat, List<OppgittPeriode> oppgittePerioder) {
        resultat.put(AktivitetIdentifikator.annenAktivitet(), arbeidsTidslinjeForAnnetAktivitet(oppgittePerioder));
    }

    private void leggTilNæringsdrivende(Map<AktivitetIdentifikator, ArbeidTidslinje> resultat, List<OppgittPeriode> oppgittePeridoer) {
        resultat.put(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), arbeidsTidslinjeForSelvstendigNæringsdrivende(oppgittePeridoer));
    }

    private void leggTilFrilans(Map<AktivitetIdentifikator, ArbeidTidslinje> resultat, List<OppgittPeriode> oppgittePerioder, boolean erSelvNæringsdrivende) {
        resultat.put(AktivitetIdentifikator.forFrilans(), arbeidsTidslinjeForFrilans(oppgittePerioder, erSelvNæringsdrivende));
    }

    private boolean søktOmUtsettelsePgaArbeid(OppgittPeriode søknadsperiode) {
        return søknadsperiode.getÅrsak().equals(UtsettelseÅrsak.ARBEID);
    }

    private boolean søktOmGraderingForArbeidsforhold(Behandling behandling,
                                                     OppgittPeriode søknadsperiode,
                                                     String orgnr,
                                                     String arbeidsforholdId) {
        //Gradering ved selvstendig næringsdrivende og frilans trenger ikke orgnr i søknad
        String orgnrFraSøknad = søknadsperiode.getVirksomhet() == null ? null : søknadsperiode.getVirksomhet().getOrgnr();
        if (søktOmGraderingForVirksomhet(søknadsperiode, orgnr, orgnrFraSøknad)) {
            List<Inntektsmelding> inntektsmeldinger = uttakArbeidTjeneste.hentInntektsmeldinger(behandling);
            return finnesGraderingForSøknadsperiodenIInntektsmeldingForArbeidsforhold(inntektsmeldinger, søknadsperiode, orgnr, arbeidsforholdId) ||
                !finnesGraderingForSøknadsperiodenIInntektsmeldingerForVirksomhet(inntektsmeldinger, søknadsperiode, orgnr);
        }
        return false;
    }

    private boolean finnesGraderingForSøknadsperiodenIInntektsmeldingForArbeidsforhold(List<Inntektsmelding> inntektsmeldinger,
                                                                                       OppgittPeriode søknadsperiode,
                                                                                       String orgnr,
                                                                                       String arbeidsforholdId) {
        return graderingFraInntektsmelding(inntektsmeldinger, søknadsperiode, orgnr, arbeidsforholdId).isPresent();
    }

    private Optional<Gradering> graderingFraInntektsmelding(List<Inntektsmelding> inntektsmeldinger,
                                                            OppgittPeriode søknadsperiode,
                                                            String orgnr,
                                                            String arbeidsforholdId) {
        Optional<Inntektsmelding> inntektsmelding = inntektsmeldingForArbeidsforhold(inntektsmeldinger, orgnr, arbeidsforholdId);
        if (!inntektsmelding.isPresent()) {
            return Optional.empty();
        }
        return inntektsmelding.get().getGraderinger()
            .stream()
            .filter(gradering -> overlapper(søknadsperiode, gradering))
            .findFirst();
    }

    private Optional<Gradering> graderingFraInntektsmelding(Behandling behandling,
                                                            OppgittPeriode søknadsperiode,
                                                            String orgnr,
                                                            String arbeidsforholdId) {
        List<Inntektsmelding> inntektsmeldinger = uttakArbeidTjeneste.hentInntektsmeldinger(behandling);
        return graderingFraInntektsmelding(inntektsmeldinger, søknadsperiode, orgnr, arbeidsforholdId);
    }

    private Optional<Inntektsmelding> inntektsmeldingForArbeidsforhold(List<Inntektsmelding> inntektsmeldinger, String orgnr, String arbeidsforholdId) {
        return inntektsmeldinger.stream()
            .filter(inntektsmelding -> Objects.equals(orgnr, inntektsmelding.getVirksomhet().getOrgnr()))
            .filter(inntektsmelding -> Objects.equals(arbeidsforholdId, inntektsmelding.getArbeidsforholdRef().getReferanse()))
            .findFirst();
    }

    private boolean finnesGraderingForSøknadsperiodenIInntektsmeldingerForVirksomhet(List<Inntektsmelding> inntektsmeldinger,
                                                                                     OppgittPeriode søknadsperiode,
                                                                                     String orgnr) {
        return inntektsmeldinger.stream()
            .filter(inntektsmelding -> Objects.equals(orgnr, inntektsmelding.getVirksomhet().getOrgnr()))
            .flatMap(inntektsmelding -> inntektsmelding.getGraderinger().stream())
            .anyMatch(gradering -> overlapper(søknadsperiode, gradering));
    }

    private boolean overlapper(OppgittPeriode søknadsperiode, Gradering gradering) {
        return PerioderUtenHelgUtil.perioderUtenHelgOverlapper(gradering.getPeriode().getFomDato(),
            gradering.getPeriode().getTomDato(), søknadsperiode.getFom(), søknadsperiode.getTom());
    }

    private boolean overlapper(OppgittPeriode søknadsperiode, UtsettelsePeriode utsettelsePeriode) {
        return PerioderUtenHelgUtil.perioderUtenHelgOverlapper(utsettelsePeriode.getPeriode().getFomDato(),
            utsettelsePeriode.getPeriode().getTomDato(), søknadsperiode.getFom(), søknadsperiode.getTom());
    }

    private boolean søktOmGraderingForVirksomhet(OppgittPeriode søknadsperiode, String orgnr, String orgnrFraSøknad) {
        return erGradering(søknadsperiode) && Objects.equals(orgnr, orgnrFraSøknad);
    }

    private ArbeidTidslinje arbeidsTidslinjeForSelvstendigNæringsdrivende(List<OppgittPeriode> oppgittePeridoer) {
        ArbeidTidslinje.Builder builder = new ArbeidTidslinje.Builder();
        for (OppgittPeriode søknadsperiode : oppgittePeridoer) {
            if (erGradering(søknadsperiode) && !søknadsperiode.getErArbeidstaker()) {
                builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), Arbeid.forSelvstendigNæringsdrivende(søknadsperiode.getArbeidsprosent()));
            } else if (søktOmUtsettelsePgaArbeid(søknadsperiode)) {
                builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), Arbeid.forSelvstendigNæringsdrivende(BigDecimal.valueOf(100)));
            }
        }
        return builder.build();
    }

    private void leggTilPeriodeForOrdinærtArbeid(Behandling behandling,
                                                 OppgittPeriode søknadsperiode,
                                                 ArbeidTidslinje.Builder builder,
                                                 String orgnr,
                                                 String arbeidsforholdId) {
        Optional<Gradering> graderingFraInntektsmelding = graderingFraInntektsmelding(behandling, søknadsperiode, orgnr, arbeidsforholdId);
        Optional<UtsettelsePeriode> utsettelseFraInntektsmelding = utsettelseFraInntektsmelding(behandling, søknadsperiode, orgnr, arbeidsforholdId);
        if (graderingFraInntektsmelding.isPresent()) {
            leggTilArbeidBasertPåGraderingIinntektsmelding(behandling, søknadsperiode, builder, orgnr, arbeidsforholdId, graderingFraInntektsmelding.get());
        } else if (utsettelseFraInntektsmelding.isPresent() && Objects.equals(utsettelseFraInntektsmelding.get().getÅrsak(), UtsettelseÅrsak.ARBEID)) {
            leggTilArbeidBasertPåUtsettelsePgaArbeidIinntektsmelding(behandling, builder, orgnr, arbeidsforholdId, søknadsperiode);
        } else {
            leggTilArbeidBasertPåPermisjoner(behandling, søknadsperiode, builder, orgnr, arbeidsforholdId);
        }
    }

    private Optional<UtsettelsePeriode> utsettelseFraInntektsmelding(Behandling behandling, OppgittPeriode søknadsperiode, String orgnr, String arbeidsforholdId) {
        List<Inntektsmelding> inntektsmeldinger = uttakArbeidTjeneste.hentInntektsmeldinger(behandling);
        return utsettelseFraInntektsmelding(inntektsmeldinger, søknadsperiode, orgnr, arbeidsforholdId);
    }

    private Optional<UtsettelsePeriode> utsettelseFraInntektsmelding(List<Inntektsmelding> inntektsmeldinger, OppgittPeriode søknadsperiode, String orgnr, String arbeidsforholdId) {
        Optional<Inntektsmelding> inntektsmelding = inntektsmeldingForArbeidsforhold(inntektsmeldinger, orgnr, arbeidsforholdId);
        if (!inntektsmelding.isPresent()) {
            return Optional.empty();
        }
        return inntektsmelding.get().getUtsettelsePerioder()
            .stream()
            .filter(utsettelse -> overlapper(søknadsperiode, utsettelse))
            .findFirst();
    }

    private void leggTilArbeidBasertPåPermisjoner(Behandling behandling, OppgittPeriode søknadsperiode, ArbeidTidslinje.Builder builder, String orgnr, String arbeidsforholdId) {
        List<Yrkesaktivitet> yrkesaktiviteterForVirksomhet = yrkesAktiviteterForVirksomhet(behandling, orgnr, arbeidsforholdId);
        //TODO SOMMERFUGL støtte flere arbeidsforhold i samme virksomhet der det fler enn ett arbeidsforhold som har permisjon
        sjekkOmFlereArbeidsforholdHarPermisjon(yrkesaktiviteterForVirksomhet);

        for (Yrkesaktivitet yrkesaktivitet : yrkesaktiviteterForVirksomhet) {
            for (Permisjon permisjon : yrkesaktivitet.getPermisjon()) {
                DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(permisjon.getFraOgMed(), permisjon.getTilOgMed());
                if (PerioderUtenHelgUtil.perioderUtenHelgOverlapper(periode.getFomDato(), periode.getTomDato(), søknadsperiode.getFom(), søknadsperiode.getTom())) {
                    LocalDate stillingsprosentTidspunkt = senestAv(permisjon.getFraOgMed(), søknadsperiode.getFom());
                    BigDecimal stillingsprosent = finnStillingsprosent(behandling, orgnr, arbeidsforholdId, stillingsprosentTidspunkt);
                    BigDecimal arbeidstidprosent = FastsettePerioderUtil.finnArbeidstidsprosentFraPermisjonPeriode(permisjon, stillingsprosent);
                    if (arbeidstidprosent.compareTo(BigDecimal.ZERO) > 0) {
                        Arbeid arbeid = Arbeid.forOrdinærtArbeid(arbeidstidprosent, stillingsprosent, permisjon.getProsentsats().getVerdi());
                        leggTilArbeid(søknadsperiode, builder, permisjon, arbeid);
                    }
                }
            }
        }
    }

    private void leggTilArbeidBasertPåGraderingIinntektsmelding(Behandling behandling,
                                                                OppgittPeriode søknadsperiode,
                                                                ArbeidTidslinje.Builder builder,
                                                                String orgnr,
                                                                String arbeidsforholdId,
                                                                Gradering graderingFraInntektsmelding) {
        BigDecimal stillingsprosent = finnStillingsprosent(behandling, orgnr, arbeidsforholdId, søknadsperiode.getFom());
        BigDecimal arbeidstidsprosent = graderingFraInntektsmelding.getArbeidstidProsent();
        Arbeid arbeid = Arbeid.forOrdinærtArbeid(arbeidstidsprosent, stillingsprosent, null);
        builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), arbeid);
    }

    private List<Yrkesaktivitet> yrkesAktiviteterForVirksomhet(Behandling behandling, String orgnr, String arbeidsforholdId) {
        return uttakArbeidTjeneste.hentYrkesAktiviteterOrdinærtArbeidsforhold(behandling)
            .stream()
            .filter(yrkesaktivitet -> yrkesaktivitet.getArbeidsgiver().getIdentifikator().equals(orgnr))
            .filter(yrkesaktivitet -> {
                Optional<ArbeidsforholdRef> arbeidsforholdRef = yrkesaktivitet.getArbeidsforholdRef();
                if (arbeidsforholdId != null && arbeidsforholdRef.isPresent()) {
                    return Objects.equals(arbeidsforholdRef.get().getReferanse(), arbeidsforholdId);
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    private void leggTilArbeid(OppgittPeriode søknadsperiode,
                               ArbeidTidslinje.Builder builder,
                               Permisjon permisjon,
                               Arbeid arbeid) {
        LocalDate fraOgMed = senestAv(permisjon.getFraOgMed(), søknadsperiode.getFom());
        LocalDate tilOgMed = tidligstAv(permisjon.getTilOgMed(), søknadsperiode.getTom());
        builder.medArbeid(fraOgMed, tilOgMed, arbeid);
    }

    private LocalDate tidligstAv(LocalDate dato1, LocalDate dato2) {
        return dato1.isBefore(dato2) ? dato1 : dato2;
    }

    private LocalDate senestAv(LocalDate dato1, LocalDate dato2) {
        return dato1.isAfter(dato2) ? dato1 : dato2;
    }

    private void sjekkOmFlereArbeidsforholdHarPermisjon(List<Yrkesaktivitet> yrkesaktiviteter) {
        boolean funnetPermisjon = false;
        for (Yrkesaktivitet yrkesaktivitet : yrkesaktiviteter) {
            if (!yrkesaktivitet.getPermisjon().isEmpty()) {
                if (funnetPermisjon) {
                    throw FeilFactory.create(FastsettePerioderFeil.class).støtterIkkeFlereArbeidsforholdMedPerimisjonISammeVirksomhet().toException();
                }
                funnetPermisjon = true;
            }
        }
    }


    private BigDecimal finnStillingsprosent(Behandling behandling,
                                            String orgnr,
                                            String arbeidsforholdId,
                                            LocalDate dato) {
        Optional<BigDecimal> stillingsprosent = stillingsprosentTjeneste.finnStillingsprosentOrdinærtArbeid(behandling,
            orgnr, arbeidsforholdId, dato);
        if (stillingsprosent.isPresent()) {
            return stillingsprosent.get();
        }
        throw FeilFactory.create(FastsettePerioderFeil.class).manglendeStillingsprosent(orgnr, arbeidsforholdId, dato, UttakArbeidType.ORDINÆRT_ARBEID).toException();
    }

    private ArbeidTidslinje arbeidsTidslinjeForFrilans(List<OppgittPeriode> oppgittePerioder, boolean erSelvNæringsdrivende) {
        ArbeidTidslinje.Builder builder = new ArbeidTidslinje.Builder();
        for (OppgittPeriode søknadsperiode : oppgittePerioder) {
            //Siden søknad har frilans/selvstendig næringsdrivende i samme kategori kan vi ikke skille på hvilken som skal graderes
            //Velger nå å sette gradering på selvstendig næringsdrivende for å så fikse etter utbyggeren
            if (!erSelvNæringsdrivende && !søknadsperiode.getErArbeidstaker() && erGradering(søknadsperiode)) {
                builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), Arbeid.forFrilans(søknadsperiode.getArbeidsprosent()));
            } else if (søktOmUtsettelsePgaArbeid(søknadsperiode)) {
                BigDecimal stillingsprosent = BigDecimal.valueOf(100);
                //Arbeidstidsprosent settes til stillingsprosent ved utsettelse pga arbeid
                builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), Arbeid.forFrilans(stillingsprosent));
            }
        }
        return builder.build();
    }

    private ArbeidTidslinje arbeidsTidslinjeForAnnetAktivitet(List<OppgittPeriode> oppgittePerioder) {
        ArbeidTidslinje.Builder builder = new ArbeidTidslinje.Builder();
        for (OppgittPeriode søknadsperiode : oppgittePerioder) {
            builder.medArbeid(søknadsperiode.getFom(), søknadsperiode.getTom(), Arbeid.forAnnet());
        }
        return builder.build();
    }

    private boolean erSelvstendigNæringsdrivende(List<BeregningsgrunnlagPrStatusOgAndel> andeler) {
        return andeler.stream().map(BeregningsgrunnlagPrStatusOgAndel::getAktivitetStatus).anyMatch(AktivitetStatus::erSelvstendigNæringsdrivende);
    }

    private List<OppgittPeriode> hentOppgittePerioder(Behandling behandling) {
        return repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling).getGjeldendeSøknadsperioder().getOppgittePerioder();
    }

    private boolean erGradering(OppgittPeriode søknadsperiode) {
        return søknadsperiode.getArbeidsprosent() != null && søknadsperiode.getArbeidsprosent().compareTo(BigDecimal.ZERO) > 0;
    }
}
