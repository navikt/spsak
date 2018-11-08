package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.erTomListe;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.getLandkode;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.konverterDato;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapAnnenForelder;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapRelasjonTilBarnet;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapRettigheter;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.mapSøknad;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperFelles.opprettOppholdNorge;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto.AndreYtelserDto;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto.NaringsvirksomhetTypeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.AnnenForelderDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.EgenVirksomhetDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.FrilansDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.GraderingDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEndringsøknadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.OppholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.OverføringsperiodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtenlandsoppholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtsettelseDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.VirksomhetDto;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdUtlandet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Periode;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.AnnenOpptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Dekningsgrad;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.EgenNaering;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Frilans;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Frilansoppdrag;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.NorskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.ObjectFactory;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Opptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Regnskapsfoerer;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.UtenlandskArbeidsforhold;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.UtenlandskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.AnnenOpptjeningTyper;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Dekningsgrader;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.MorsAktivitetsTyper;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Oppholdsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Overfoeringsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Utsettelsesaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Uttaksperiodetyper;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Virksomhetstyper;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Gradering;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Oppholdsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Overfoeringsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Utsettelsesperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Uttaksperiode;
import no.nav.vedtak.felles.xml.soeknad.v1.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.StringUtils;

public class SøknadMapperFP {

    private SøknadMapperFP() {
    }

    public static Soeknad mapTilForeldrepengerEndringssøknad(ManuellRegistreringEndringsøknadDto registreringDto, NavBruker navBruker) {
        Soeknad søknad = mapSøknad(registreringDto, navBruker);

        Endringssoeknad endringssøknad = new no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.ObjectFactory().createEndringssoeknad();
        endringssøknad.setFordeling(mapFordelingEndringssøknad(registreringDto));
        OmYtelse omYtelse = new no.nav.vedtak.felles.xml.soeknad.v1.ObjectFactory().createOmYtelse();
        omYtelse.getAny().add(new no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.ObjectFactory().createEndringssoeknad(endringssøknad));
        søknad.setOmYtelse(omYtelse);
        return søknad;
    }

    public static Soeknad mapTilForeldrepenger(ManuellRegistreringForeldrepengerDto registreringDto, NavBruker navBruker, TpsTjeneste tpsTjeneste, VirksomhetTjeneste virksomhetTjeneste) {
        Soeknad søknad = mapSøknad(registreringDto, navBruker);

        Foreldrepenger foreldrepenger = new ObjectFactory().createForeldrepenger();

        SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = mapRelasjonTilBarnet(registreringDto); //Fødsel, termin, adopsjon eller omsorg
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        foreldrepenger.setRettigheter(mapRettigheter(registreringDto));
        foreldrepenger.setMedlemskap(mapMedlemskapFP(registreringDto));
        foreldrepenger.setAnnenForelder(mapAnnenForelder(registreringDto, tpsTjeneste));
        foreldrepenger.setDekningsgrad(mapDekningsgrad(registreringDto));
        foreldrepenger.setFordeling(mapFordeling(registreringDto));
        foreldrepenger.setOpptjening(mapOpptjening(registreringDto, virksomhetTjeneste));

        OmYtelse omYtelse = new no.nav.vedtak.felles.xml.soeknad.v1.ObjectFactory().createOmYtelse();
        omYtelse.getAny().add(new ObjectFactory().createForeldrepenger(foreldrepenger));
        søknad.setOmYtelse(omYtelse);
        søknad.setTilleggsopplysninger(registreringDto.getTilleggsopplysninger());

        return søknad;
    }

    static Opptjening mapOpptjening(ManuellRegistreringForeldrepengerDto registreringDto, VirksomhetTjeneste virksomhetTjeneste) {
        Opptjening opptjening = new Opptjening();
        opptjening.getAnnenOpptjening().addAll(mapAndreYtelser(registreringDto.getAndreYtelser()));
        opptjening.getEgenNaering().addAll(mapEgneNæringer(registreringDto.getEgenVirksomhet(), virksomhetTjeneste));
        opptjening.getUtenlandskArbeidsforhold().addAll(mapAlleUtenlandskeArbeidsforhold(registreringDto.getArbeidsforhold()));
        opptjening.setFrilans(mapFrilans(registreringDto.getFrilans()));
        return opptjening;
    }

    private static Frilans mapFrilans(FrilansDto dto) {
        if (dto == null || dto.getPerioder() == null) {
            return null;
        }
        Frilans frilans = new Frilans();
        frilans.getPeriode().addAll(dto.getPerioder().stream().map(p -> {
            Periode periode = new Periode();
            periode.setFom(konverterDato(p.getPeriodeFom()));
            periode.setTom(konverterDato(p.getPeriodeTom()));
            return periode;
        }).collect(Collectors.toList()));

        frilans.setErNyoppstartet(getNullBooleanAsFalse(dto.getErNyoppstartetFrilanser()));
        frilans.setNaerRelasjon(getNullBooleanAsFalse(dto.getHarHattOppdragForFamilie()));
        frilans.setHarInntektFraFosterhjem(getNullBooleanAsFalse(dto.getHarInntektFraFosterhjem()));

        if (getNullBooleanAsFalse(dto.getHarHattOppdragForFamilie())) {
            List<Frilansoppdrag> frilansoppdrag = dto.getOppdragPerioder().stream().map(SøknadMapperFP::mapAlleFrilansOppdragperioder).collect(Collectors.toList());
            frilans.getFrilansoppdrag().addAll(frilansoppdrag);
        }

        return frilans;
    }

    private static Frilansoppdrag mapAlleFrilansOppdragperioder(FrilansDto.Oppdragperiode oppdragperiode) {
        Frilansoppdrag oppdrag = new Frilansoppdrag();
        oppdrag.setOppdragsgiver(oppdragperiode.getOppdragsgiver());

        Periode periode = new Periode();
        periode.setFom(konverterDato(oppdragperiode.getFomDato()));
        periode.setTom(konverterDato(oppdragperiode.getTomDato()));
        oppdrag.setPeriode(periode);

        return oppdrag;
    }

    static List<UtenlandskArbeidsforhold> mapAlleUtenlandskeArbeidsforhold(List<ArbeidsforholdDto> arbeidsforhold) {
        if (isNull(arbeidsforhold)) {
            return new ArrayList<>();
        }
        //Arbeidsforhold kan komme inn som liste med ett ArbeidsforholdDto objekt som bare inneholder null verdier. Denne må filtreres bort, siden frontend ikke gjør dette for oss i tilfellene hvor det ikke registreres utenlands arbeidsforhold.
        Predicate<ArbeidsforholdDto> predicateArbeidsforholdFelterUlikNull = arbeidsforholdDto -> arbeidsforholdDto.getPeriodeFom() != null && arbeidsforholdDto.getPeriodeTom() != null && arbeidsforholdDto.getLand() != null;
        return arbeidsforhold.stream().filter(predicateArbeidsforholdFelterUlikNull).map(SøknadMapperFP::mapUtenlandskArbeidsforhold).collect(Collectors.toList());
    }

    private static UtenlandskArbeidsforhold mapUtenlandskArbeidsforhold(ArbeidsforholdDto arbeidsforholdDto) {
        UtenlandskArbeidsforhold arbeidsforhold = new UtenlandskArbeidsforhold();
        arbeidsforhold.setArbeidsgiversnavn(arbeidsforholdDto.getArbeidsgiver());

        Periode periode = new Periode();
        periode.setFom(konverterDato(arbeidsforholdDto.getPeriodeFom()));
        periode.setTom(konverterDato(arbeidsforholdDto.getPeriodeTom()));
        arbeidsforhold.setPeriode(periode);

        arbeidsforhold.setArbeidsland(getLandkode(arbeidsforholdDto.getLand()));
        return arbeidsforhold;

    }

    static List<EgenNaering> mapEgneNæringer(EgenVirksomhetDto egenVirksomhetDto, VirksomhetTjeneste virksomhetTjeneste) {
        if ((isNull(egenVirksomhetDto)) || isNull(egenVirksomhetDto.getVirksomheter())) {
            return new ArrayList<>();
        }
        return egenVirksomhetDto.getVirksomheter().stream().map(v -> mapEgenNæring(v, virksomhetTjeneste)).collect(Collectors.toList());
    }

    //TODO: mapEgenNæring kan ikke fullføres. Er avheig av avklaringer: https://confluence.adeo.no/display/MODNAV/05g.+Avklaringer
    static EgenNaering mapEgenNæring(VirksomhetDto virksomhetDto, VirksomhetTjeneste virksomhetTjeneste) {
        EgenNaering egenNaering;
        if (TRUE.equals(virksomhetDto.getVirksomhetRegistrertINorge())) {
            NorskOrganisasjon norskOrganisasjon = new NorskOrganisasjon();
            norskOrganisasjon.setOrganisasjonsnummer(virksomhetDto.getOrganisasjonsnummer());
            norskOrganisasjon.setNavn(virksomhetDto.getNavn());
            Virksomhet virksomhet = virksomhetTjeneste.hentOgLagreOrganisasjon(virksomhetDto.getOrganisasjonsnummer());
            Periode periode = new Periode();
            periode.setFom(konverterDato(virksomhet.getRegistrert()));
            periode.setTom(konverterDato(virksomhet.getAvslutt() != null ? virksomhet.getAvslutt() : Tid.TIDENES_ENDE));
            norskOrganisasjon.setPeriode(periode);
            egenNaering = norskOrganisasjon;
        } else {
            UtenlandskOrganisasjon utenlandskOrganisasjon = new UtenlandskOrganisasjon();
            utenlandskOrganisasjon.setNavn(virksomhetDto.getNavn());
            Periode periode = new Periode(); //TODO PKMANTIS-1079: Hardkoder en periode for å komme videre i testing av egen næring.
            periode.setFom(konverterDato(LocalDate.now().minusYears(3)));
            periode.setTom(konverterDato(LocalDate.now()));
            utenlandskOrganisasjon.setPeriode(periode);
            egenNaering = utenlandskOrganisasjon;
        }
        egenNaering.setArbeidsland(getLandkode(virksomhetDto.getLandJobberFra()));
        egenNaering.setNaerRelasjon(getNullBooleanAsFalse(virksomhetDto.getFamilieEllerVennerTilknyttetNaringen()));
        egenNaering.setErNyIArbeidslivet(getNullBooleanAsFalse(virksomhetDto.getErNyIArbeidslivet()));
        egenNaering.setOppstartsdato(konverterDato(virksomhetDto.getOppstartsdato()));

        if (TRUE.equals(virksomhetDto.getHarRegnskapsforer())) {
            Regnskapsfoerer regnskapsfoerer = new Regnskapsfoerer();
            regnskapsfoerer.setNavn(virksomhetDto.getNavnRegnskapsforer());
            regnskapsfoerer.setTelefon(virksomhetDto.getTlfRegnskapsforer());
            egenNaering.setRegnskapsfoerer(regnskapsfoerer);
        }

        if (TRUE.equals(virksomhetDto.getVarigEndretEllerStartetSisteFireAr())) {
            egenNaering.setBeskrivelseAvEndring(virksomhetDto.getBeskrivelseAvEndring());
            egenNaering.setErVarigEndring(getNullBooleanAsFalse(virksomhetDto.getHarVarigEndring()));
            egenNaering.setEndringsDato(konverterDato(virksomhetDto.getVarigEndringGjeldendeFom()));
            if (virksomhetDto.getInntekt() != null) {
                egenNaering.setNaeringsinntektBrutto(BigInteger.valueOf(virksomhetDto.getInntekt()));
            }
            egenNaering.setErNyoppstartet(getNullBooleanAsFalse(virksomhetDto.getErNyoppstartet()));
        }
        finnTypeVirksomhet(virksomhetDto, egenNaering);
        return egenNaering;
    }

    private static Boolean getNullBooleanAsFalse(Boolean booleanInn) {
        return booleanInn != null ? booleanInn : false;
    }

    private static void finnTypeVirksomhet(VirksomhetDto virksomhetDto, EgenNaering egenNaering) {
        NaringsvirksomhetTypeDto typeVirksomhet = virksomhetDto.getTypeVirksomhet();
        List<Virksomhetstyper> virksomhetstyper = egenNaering.getVirksomhetstype();
        if (!isNull(typeVirksomhet)) {
            if (typeVirksomhet.getAnnen()) {
                Virksomhetstyper virksomhetstype = new Virksomhetstyper();
                virksomhetstype.setKode(VirksomhetType.ANNEN.getKode());
                virksomhetstyper.add(virksomhetstype);
            }
            if (typeVirksomhet.getFiske()) {
                Virksomhetstyper virksomhetstype = new Virksomhetstyper();
                virksomhetstype.setKode(VirksomhetType.FISKE.getKode());
                virksomhetstyper.add(virksomhetstype);
            }
            if (typeVirksomhet.getDagmammaEllerFamiliebarnehage()) {
                Virksomhetstyper virksomhetstype = new Virksomhetstyper();
                virksomhetstype.setKode(VirksomhetType.DAGMAMMA.getKode());
                virksomhetstyper.add(virksomhetstype);
            }
            if (typeVirksomhet.getJordbrukEllerSkogbruk()) {
                Virksomhetstyper virksomhetstype = new Virksomhetstyper();
                virksomhetstype.setKode(VirksomhetType.JORDBRUK_SKOGBRUK.getKode());
                virksomhetstyper.add(virksomhetstype);
            }
        } else {
            Virksomhetstyper virksomhetstype = new Virksomhetstyper();
            virksomhetstype.setKode(VirksomhetType.UDEFINERT.getKode());
            virksomhetstyper.add(virksomhetstype);
        }
    }

    static List<AnnenOpptjening> mapAndreYtelser(List<AndreYtelserDto> andreYtelser) {
        if (isNull(andreYtelser)) {
            return new ArrayList<>();
        }
        return andreYtelser.stream().map(SøknadMapperFP::opprettAnnenOpptjening).collect(Collectors.toList());
    }

    private static AnnenOpptjening opprettAnnenOpptjening(AndreYtelserDto opptjening) {
        AnnenOpptjening annenOpptjening = new AnnenOpptjening();
        AnnenOpptjeningTyper typer = new AnnenOpptjeningTyper();
        typer.setKode(opptjening.getYtelseType().getKode());
        annenOpptjening.setType(typer);

        Periode periode = new Periode();
        periode.setFom(konverterDato(opptjening.getPeriodeFom()));
        periode.setTom(konverterDato(opptjening.getPeriodeTom()));
        annenOpptjening.setPeriode(periode);
        return annenOpptjening;

    }

    static Dekningsgrad mapDekningsgrad(ManuellRegistreringForeldrepengerDto registreringDto) {
        if (isNull(registreringDto.getDekningsgrad())) {
            return null;
        }

        Dekningsgrad dekningsgrad = new Dekningsgrad();
        Dekningsgrader dekningsgrader = new Dekningsgrader();
        dekningsgrader.setKode(registreringDto.getDekningsgrad().getValue());
        dekningsgrad.setDekningsgrad(dekningsgrader);

        return dekningsgrad;
    }

    static Fordeling mapFordelingEndringssøknad(ManuellRegistreringEndringsøknadDto registreringDto) {
        Fordeling fordeling = new Fordeling();
        List<LukketPeriodeMedVedlegg> perioder = mapFordelingPerioder(registreringDto.getTidsromPermisjon(), registreringDto.getSoker());
        fordeling.getPerioder().addAll(perioder.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        return fordeling;
    }

    static Fordeling mapFordeling(ManuellRegistreringForeldrepengerDto registreringDto) {
        Fordeling fordeling = new Fordeling();

        AnnenForelderDto annenForelder = registreringDto.getAnnenForelder();
        if (!isNull(annenForelder)) {
            fordeling.setAnnenForelderErInformert(annenForelder.getAnnenForelderInformert());
        }
        List<LukketPeriodeMedVedlegg> perioder = mapFordelingPerioder(registreringDto.getTidsromPermisjon(), registreringDto.getSoker());
        fordeling.getPerioder().addAll(perioder.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        return fordeling;
    }

    private static List<LukketPeriodeMedVedlegg> mapFordelingPerioder(TidsromPermisjonDto tidsromPermisjon, ForeldreType soker) {
        List<LukketPeriodeMedVedlegg> result = new ArrayList<>();
        if (!isNull(tidsromPermisjon)) {
            if (tidsromPermisjon.getOverforingsperiode() != null) {
                result.add(mapOverfoeringsperiode(tidsromPermisjon.getOverforingsperiode(), soker));
            }
            result.addAll(mapUtsettelsesperioder(tidsromPermisjon.getUtsettelsePeriode()));
            result.addAll(mapUttaksperioder(tidsromPermisjon.getPermisjonsPerioder()));
            result.addAll(mapGraderingsperioder(tidsromPermisjon.getGraderingPeriode()));
            result.addAll(mapOppholdsperioder(tidsromPermisjon.getOppholdPerioder()));
        }
        return result;
    }

    static List<Uttaksperiode> mapUttaksperioder(List<PermisjonPeriodeDto> permisjonsPerioder) {
        List<Uttaksperiode> result = new ArrayList<>();
        if (!isNull(permisjonsPerioder)) {
            result.addAll(permisjonsPerioder.stream().map(SøknadMapperFP::mapPermisjonPeriodeDto).collect(Collectors.toList()));
        }
        return result;
    }

    static List<Uttaksperiode> mapGraderingsperioder(List<GraderingDto> graderingsperioder) {
        List<Uttaksperiode> result = new ArrayList<>();

        if (!isNull(graderingsperioder)) {
            return graderingsperioder.stream().map(SøknadMapperFP::mapGraderingsperiode).collect(Collectors.toList());
        }
        return result;
    }

    static List<Oppholdsperiode> mapOppholdsperioder(List<OppholdDto> oppholdPerioder) {
        List<Oppholdsperiode> result = new ArrayList<>();

        if (!isNull(oppholdPerioder)) {
            return oppholdPerioder.stream().map(SøknadMapperFP::mapOppholdPeriode).collect(Collectors.toList());
        }
        return result;
    }

    private static Oppholdsperiode mapOppholdPeriode(OppholdDto oppholdDto) {
        Oppholdsperiode oppholdPeriode = new Oppholdsperiode();
        oppholdPeriode.setFom(konverterDato(oppholdDto.getPeriodeFom()));
        oppholdPeriode.setTom(konverterDato(oppholdDto.getPeriodeTom()));
        Oppholdsaarsaker oppholdsaarsaker = new Oppholdsaarsaker();
        oppholdsaarsaker.setKode(oppholdDto.getÅrsak().getKode());
        oppholdsaarsaker.setKodeverk(oppholdDto.getÅrsak().getKodeverk());
        oppholdPeriode.setAarsak(oppholdsaarsaker);
        return oppholdPeriode;
    }

    static Uttaksperiode mapPermisjonPeriodeDto(PermisjonPeriodeDto dto) {
        MorsAktivitet morsAktivitet = dto.getMorsAktivitet();
        Uttaksperiode uttaksperiode = new Uttaksperiode();
        uttaksperiode.setFom(konverterDato(dto.getPeriodeFom()));
        uttaksperiode.setTom(konverterDato(dto.getPeriodeTom()));
        uttaksperiode.setOenskerSamtidigUttak(dto.getHarSamtidigUttak());
        uttaksperiode.setOenskerFlerbarnsdager(dto.isFlerbarnsdager());

        if (dto.getSamtidigUttaksprosent() != null) {
            uttaksperiode.setSamtidigUttakProsent(dto.getSamtidigUttaksprosent().doubleValue());
        }

        Uttaksperiodetyper uttaksperiodetyper = new Uttaksperiodetyper();
        uttaksperiodetyper.setKode(dto.getPeriodeType().getKode());
        uttaksperiode.setType(uttaksperiodetyper);

        if ((!isNull(morsAktivitet)) && (!StringUtils.nullOrEmpty(morsAktivitet.getKode()))) {
            MorsAktivitetsTyper morsAktivitetsTyper = new MorsAktivitetsTyper();
            morsAktivitetsTyper.setKode(morsAktivitet.getKode());
            uttaksperiode.setMorsAktivitetIPerioden(morsAktivitetsTyper);
        }

        return uttaksperiode;
    }

    static Uttaksperiode mapGraderingsperiode(GraderingDto dto) {
        Gradering gradering = new Gradering();
        gradering.setArbeidsforholdSomSkalGraderes(TRUE.equals(dto.getSkalGraderes()));
        gradering.setVirksomhetsnummer(dto.getOrgNr());
        gradering.setFom(konverterDato(dto.getPeriodeFom()));
        gradering.setTom(konverterDato(dto.getPeriodeTom()));
        gradering.setArbeidtidProsent(dto.getProsentandelArbeid().doubleValue());
        gradering.setErArbeidstaker(dto.isErArbeidstaker());
        gradering.setOenskerSamtidigUttak(dto.getHarSamtidigUttak());
        gradering.setOenskerFlerbarnsdager(dto.isFlerbarnsdager());
        if (dto.getSamtidigUttaksprosent() != null) {
            gradering.setSamtidigUttakProsent(dto.getSamtidigUttaksprosent().doubleValue());
        }

        Uttaksperiodetyper uttaksperiodetyper = new Uttaksperiodetyper();
        uttaksperiodetyper.setKode(dto.getPeriodeForGradering().getKode());
        gradering.setType(uttaksperiodetyper);

        return gradering;
    }

    static List<Utsettelsesperiode> mapUtsettelsesperioder(List<UtsettelseDto> utsettelserDto) {
        if (isNull(utsettelserDto)) {
            return new ArrayList<>();
        }
        return utsettelserDto.stream().map(SøknadMapperFP::mapUtsettelsesperiode).collect(Collectors.toList());
    }

    static Utsettelsesperiode mapUtsettelsesperiode(UtsettelseDto utsettelserDto) {
        Utsettelsesperiode utsettelsesperiode = new Utsettelsesperiode();
        if (!isNull(utsettelserDto.getArsakForUtsettelse())) {
            Utsettelsesaarsaker årsak = new Utsettelsesaarsaker();
            årsak.setKode(utsettelserDto.getArsakForUtsettelse().getKode());
            utsettelsesperiode.setAarsak(årsak);
        }

        if (!isNull(utsettelserDto.getPeriodeForUtsettelse())) {
            Uttaksperiodetyper uttaksperiodetyper = new Uttaksperiodetyper();
            uttaksperiodetyper.setKode(utsettelserDto.getPeriodeForUtsettelse().getKode());
            utsettelsesperiode.setUtsettelseAv(uttaksperiodetyper);
        }
        utsettelsesperiode.setFom(konverterDato(utsettelserDto.getPeriodeFom()));
        utsettelsesperiode.setTom(konverterDato(utsettelserDto.getPeriodeTom()));
        utsettelsesperiode.setVirksomhetsnummer(utsettelserDto.getOrgNr());
        utsettelsesperiode.setErArbeidstaker(utsettelserDto.isErArbeidstaker());

        return utsettelsesperiode;
    }

    static Overfoeringsperiode mapOverfoeringsperiode(OverføringsperiodeDto overføringsperiode, ForeldreType soker) {
        Overfoeringsperiode overfoeringsperiode = new Overfoeringsperiode();
        Uttaksperiodetyper uttaksperiodetyper = new Uttaksperiodetyper();
        // TODO: Implementer for annen omsorgsperon. Da er ikke periodetype gitt av foreldretype.
        if (soker.equals(ForeldreType.MOR)) {
            uttaksperiodetyper.setKode(UttakPeriodeType.FEDREKVOTE.getKode());
        } else {
            uttaksperiodetyper.setKode(UttakPeriodeType.MØDREKVOTE.getKode());
        }
        overfoeringsperiode.setOverfoeringAv(uttaksperiodetyper);

        overfoeringsperiode.setFom(konverterDato(overføringsperiode.getFomDato()));
        overfoeringsperiode.setTom(konverterDato(overføringsperiode.getTomDato()));

        Overfoeringsaarsaker årsak = new Overfoeringsaarsaker();
        årsak.setKode(overføringsperiode.getOverforingArsak().getKode());
        årsak.setKodeverk(overføringsperiode.getOverforingArsak().getKodeverk());
        overfoeringsperiode.setAarsak(årsak);

        return overfoeringsperiode;
    }

    static List<OppholdUtlandet> mapUtenlandsopphold(List<UtenlandsoppholdDto> utenlandsopphold) {
        List<OppholdUtlandet> utenlandsoppholdListe = new ArrayList<>();
        for (UtenlandsoppholdDto utenlandsoppholdDto : utenlandsopphold) {
            OppholdUtlandet nyttOpphold = new OppholdUtlandet();
            nyttOpphold.setLand(getLandkode(utenlandsoppholdDto.getLand()));
            Periode periode = new Periode();
            periode.setFom(konverterDato(utenlandsoppholdDto.getPeriodeFom()));
            periode.setTom(konverterDato(utenlandsoppholdDto.getPeriodeTom()));
            nyttOpphold.setPeriode(periode);
            utenlandsoppholdListe.add(nyttOpphold);
        }
        return utenlandsoppholdListe;
    }

    static Medlemskap mapMedlemskapFP(ManuellRegistreringDto registreringDto) {
        Medlemskap medlemskap = new Medlemskap();

        boolean harFremtidigOppholdUtenlands = registreringDto.getHarFremtidigeOppholdUtenlands();
        boolean harTidligereOppholdUtenlands = registreringDto.getHarTidligereOppholdUtenlands();

        List<OppholdNorge> oppholdNorge = opprettOppholdNorge(registreringDto.getMottattDato(), !harFremtidigOppholdUtenlands, !harTidligereOppholdUtenlands);//Ikke utenlandsopphold tolkes som opphold i norge
        medlemskap.getOppholdNorge().addAll(oppholdNorge);
        medlemskap.setINorgeVedFoedselstidspunkt(registreringDto.getOppholdINorge());
        if (harFremtidigOppholdUtenlands) {
            if (!erTomListe(registreringDto.getFremtidigeOppholdUtenlands())) {
                medlemskap.getOppholdUtlandet().addAll(mapUtenlandsopphold(registreringDto.getFremtidigeOppholdUtenlands()));
            }
        }
        if (harTidligereOppholdUtenlands) {
            if (!erTomListe(registreringDto.getTidligereOppholdUtenlands())) {
                medlemskap.getOppholdUtlandet().addAll(mapUtenlandsopphold(registreringDto.getTidligereOppholdUtenlands()));
            }
        }

        return medlemskap;
    }
}
