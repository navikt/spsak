package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.KLAGE;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.REVURDERING;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnListeMedVerdierAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnStrukturertVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FlettefeltJsonObjectMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnvilgelseForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.AnnenAktivitetDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BeregningsgrunnlagAndelDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BeregningsgrunnlagRegelDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.NæringDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.AndelListeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.AndelType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.AnnenAktivitetListeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.AnnenAktivitetType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.ArbeidsforholdListeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.ArbeidsforholdType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.BehandlingsResultatKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.BehandlingsTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.BeregningsgrunnlagRegelListeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.BeregningsgrunnlagRegelType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.InnvilgetForeldrepengerConstants;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.KonsekvensForYtelseKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.LovhjemmelType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.NæringListeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.NæringType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.PeriodeListeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.PeriodeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.PersonstatusKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.RelasjonskodeKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.StatusTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.TilstøtendeYtelseKode;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.VurderingsstatusKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class InnvilgelseForeldrepengerMapper implements DokumentTypeMapper {

    public static final String DISCRIMINATOR_4007 = "_annen_forelder_er_far";
    public static final String DISCRIMINATOR_4005 = "_medmor";
    public static final String ENDRING_BEREGNING_OG_UTTAK = "ENDRING_BEREGNING_OG_UTTAK";

    private ObjectFactory objectFactory;

    public InnvilgelseForeldrepengerMapper() {
        this.objectFactory = new ObjectFactory();
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalJaxb(InnvilgetForeldrepengerConstants.JAXB_CLASS, brevdataTypeJAXBElement);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = objectFactory.createFagType();

        //Obligatoriske felter
        fagType.setAleneomsorg(VurderingsstatusKode.fromValue(finnVerdiAv(InnvilgelseForeldrepengerDokument.ALENEOMSORG, dokumentTypeDataListe)));
        fagType.setAnnenForelderHarRett(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.ANNENFORELDERHARRETT, dokumentTypeDataListe)));
        fagType.setAntallArbeidsgivere(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(InnvilgelseForeldrepengerDokument.ANTALLARBEIDSGIVERE, dokumentTypeDataListe))));
        fagType.setAntallBarn(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(Flettefelt.ANTALL_BARN, dokumentTypeDataListe))));
        fagType.setAvslagFinnes(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.AVSLAGFINNES, dokumentTypeDataListe)));
        fagType.setBarnErFødt(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.BARNERFØDT, dokumentTypeDataListe)));
        fagType.setBehandlingsType(fra(finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe)));
        fagType.setBeregningsgrunnlagRegelListe(konverterBeregningsgrunnlagRegelListe(dokumentTypeDataListe));
        fagType.setDagerTaptFørTermin(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(InnvilgelseForeldrepengerDokument.DAGERTAPTFØRTERMIN, dokumentTypeDataListe))));
        fagType.setDagsats(Long.parseLong(finnVerdiAv(InnvilgelseForeldrepengerDokument.DAGSATS, dokumentTypeDataListe)));
        fagType.setDekningsgrad(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(InnvilgelseForeldrepengerDokument.DEKNINGSGRAD, dokumentTypeDataListe))));
        fagType.setDisponibleDager(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(InnvilgelseForeldrepengerDokument.DISPONIBLEDAGER, dokumentTypeDataListe))));
        fagType.setDisponibleFellesDager(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(InnvilgelseForeldrepengerDokument.DISPONIBLEFELLESDAGER, dokumentTypeDataListe))));
        fagType.setGjelderFoedsel(Boolean.parseBoolean(finnVerdiAv(Flettefelt.GJELDER_FØDSEL, dokumentTypeDataListe)));
        fagType.setInntektOverSeksG(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.INNTEKTOVERSEKSG, dokumentTypeDataListe)));
        fagType.setInnvilgetFinnes(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.INNVILGETFINNES, dokumentTypeDataListe)));
        fagType.setGraderingFinnes(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.GRADERINGFINNES, dokumentTypeDataListe)));
        fagType.setKlageFristUker(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe))));
        LovhjemmelType lovhjemmelType = objectFactory.createLovhjemmelType();
        lovhjemmelType.setBeregning(finnVerdiAv(InnvilgelseForeldrepengerDokument.LOVHJEMMEL_BEREGNING, dokumentTypeDataListe));
        lovhjemmelType.setVurdering(finnVerdiAv(InnvilgelseForeldrepengerDokument.LOVHJEMMEL_VURDERING, dokumentTypeDataListe));
        fagType.setLovhjemmel(lovhjemmelType);
        fagType.setMottattDato(finnDatoVerdiAvUtenTidSone(Flettefelt.MOTTATT_DATO, dokumentTypeDataListe));
        fagType.setMånedsbeløp(Long.parseLong(finnVerdiAv(InnvilgelseForeldrepengerDokument.MÅNEDSBELØP, dokumentTypeDataListe)));
        fagType.setPersonstatus(PersonstatusKode.fromValue(finnVerdiAv(Flettefelt.PERSON_STATUS, dokumentTypeDataListe)));
        fagType.setRelasjonskode(tilRelasjonskode(finnVerdiAv(Flettefelt.RELASJONSKODE, dokumentTypeDataListe), finnVerdiAv(Flettefelt.KJØNN, dokumentTypeDataListe)));
        fagType.setSeksG(Long.parseLong(finnVerdiAv(InnvilgelseForeldrepengerDokument.SEKSG, dokumentTypeDataListe)));
        fagType.setSisteDagAvSistePeriode(finnDatoVerdiAvUtenTidSone(InnvilgelseForeldrepengerDokument.SISTEDAGAVSISTEPERIODE, dokumentTypeDataListe));
        fagType.setSokersNavn(finnVerdiAv(Flettefelt.SOKERSNAVN, dokumentTypeDataListe));
        fagType.setStønadsperiodeFom(finnDatoVerdiAvUtenTidSone(InnvilgelseForeldrepengerDokument.STØNADSPERIODEFOM, dokumentTypeDataListe));
        fagType.setStønadsperiodeTom(finnDatoVerdiAvUtenTidSone(InnvilgelseForeldrepengerDokument.STØNADSPERIODETOM, dokumentTypeDataListe));
        fagType.setTotalArbeidsgiverAndel(Long.parseLong(finnVerdiAv(InnvilgelseForeldrepengerDokument.TOTALARBEIDSGIVERANDEL, dokumentTypeDataListe)));
        fagType.setTotalBrukerAndel(Long.parseLong(finnVerdiAv(InnvilgelseForeldrepengerDokument.TOTALBRUKERANDEL, dokumentTypeDataListe)));
        fagType.setKonsekvensForYtelse(tilKonsekvensForYtelseKode(finnVerdiAv(InnvilgelseForeldrepengerDokument.KONSEKVENSFORYTELSE, dokumentTypeDataListe)));
        fagType.setBehandlingsResultat(tilBehandlingsResultatKode(finnVerdiAv(InnvilgelseForeldrepengerDokument.BEHANDLINGSRESULTAT, dokumentTypeDataListe)));
        fagType.setInntektMottattArbgiver(Boolean.parseBoolean(finnVerdiAv(InnvilgelseForeldrepengerDokument.INNTEKTMOTTATTARBGIVER, dokumentTypeDataListe)));
        PeriodeListeType konvertertPeriodeListe = konverterPeriodeListe(dokumentTypeDataListe);
        fagType.setPeriodeListe(konvertertPeriodeListe);
        fagType.setAntallPerioder(BigInteger.valueOf(konvertertPeriodeListe.getPeriode().size()));

        //ikke obligatoriske felter
        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.SISTE_DAG_I_FELLES_PERIODE, dokumentTypeDataListe).ifPresent(fagType::setSisteDagIFellesPeriode);
        finnOptionalDatoVerdiAvUtenTidSone(InnvilgelseForeldrepengerDokument.SISTEDAGMEDUTSETTELSE, dokumentTypeDataListe).ifPresent(fagType::setSisteDagMedUtsettelse);
        finnOptionalDatoVerdiAvUtenTidSone(InnvilgelseForeldrepengerDokument.SISTEUTBETALINGSDAG, dokumentTypeDataListe).ifPresent(fagType::setSisteUtbetalingsdag);
        finnOptionalVerdiAv(InnvilgelseForeldrepengerDokument.FORELDREPENGEPERIODENUTVIDETUKER, dokumentTypeDataListe).map(BigInteger::new).ifPresent(fagType::setForeldrepengeperiodenUtvidetUker);
        finnOptionalVerdiAv(Flettefelt.FRITEKST, dokumentTypeDataListe).ifPresent(fagType::setFritekst);

        return fagType;
    }

    private PeriodeListeType konverterPeriodeListe(List<DokumentTypeData> dokumentTypeDataListe) {
        PeriodeListeType liste = objectFactory.createPeriodeListeType();
        List<PeriodeDto> periodeDtos = new ArrayList<>();
        for (DokumentTypeData data : finnListeMedVerdierAv(Flettefelt.PERIODE, dokumentTypeDataListe)) {
            String feltNavnMedIndeks = data.getDoksysId();
            String strukturertFelt = finnStrukturertVerdiAv(feltNavnMedIndeks, dokumentTypeDataListe);
            PeriodeDto dto = FlettefeltJsonObjectMapper.readValue(strukturertFelt, PeriodeDto.class);
            periodeDtos.add(dto);
        }
        periodeDtos.forEach(dto -> {
            PeriodeType periode = objectFactory.createPeriodeType();
            periode.setInnvilget(dto.getInnvilget());
            periode.setÅrsak(dto.getÅrsak());
            periode.setPeriodeFom(finnDatoVerdiAvUtenTidSone(dto.getPeriodeFom()));
            periode.setPeriodeTom(finnDatoVerdiAvUtenTidSone(dto.getPeriodeTom()));
            periode.setAntallTapteDager(BigInteger.valueOf(dto.getAntallTapteDager()));
            if (!dto.getArbeidsforhold().isEmpty()) {
                periode.setArbeidsforholdListe(fraArbeidsforhold(dto.getArbeidsforhold()));
            }
            if (dto.getNæring() != null) {
                periode.setNæringListe(fraNæring(dto.getNæring()));
            }
            if (!dto.getAnnenAktivitet().isEmpty()) {
                periode.setAnnenAktivitetListe(fraAnnenAktivitet(dto.getAnnenAktivitet()));
            }
            liste.getPeriode().add(periode);
        });

        return liste;
    }

    private AnnenAktivitetListeType fraAnnenAktivitet(List<AnnenAktivitetDto> dtoListe) {
        AnnenAktivitetListeType liste = objectFactory.createAnnenAktivitetListeType();
        dtoListe.forEach(dto -> {
            AnnenAktivitetType aktivitet = objectFactory.createAnnenAktivitetType();
            aktivitet.setAktivitetType(tilStatusTypeKode(dto.getAktivitetType()));
            aktivitet.setGradering(dto.getGradering());
            aktivitet.setProsentArbeid(BigInteger.valueOf(dto.getProsentArbeid()));
            aktivitet.setUttaksgrad(BigInteger.valueOf(dto.getUttaksgrad()));
            aktivitet.setAktivitetDagsats(dto.getDagsats());
            liste.getAnnenAktivitet().add(aktivitet);
        });
        return liste;
    }

    private NæringListeType fraNæring(NæringDto dto) {
        NæringListeType liste = null;
        if (dto != null) {
            liste = objectFactory.createNæringListeType();
            NæringType næring = objectFactory.createNæringType();
            næring.setGradering(dto.getGradering());
            næring.setInntekt1(dto.getInntekt1());
            næring.setInntekt2(dto.getInntekt2());
            næring.setInntekt3(dto.getInntekt3());
            næring.setAktivitetDagsats(dto.getDagsats());
            næring.setUttaksgrad(BigInteger.valueOf(dto.getUttaksgrad()));
            næring.setProsentArbeid(BigInteger.valueOf(dto.getProsentArbeid()));
            næring.setSistLignedeÅr(BigInteger.valueOf(dto.getSistLignedeÅr()));
            liste.setNæring(næring);
        }
        return liste;
    }

    private ArbeidsforholdListeType fraArbeidsforhold(List<ArbeidsforholdDto> dtoListe) {
        ArbeidsforholdListeType liste = objectFactory.createArbeidsforholdListeType();
        dtoListe.forEach(dto -> {
            ArbeidsforholdType arbeidsforhold = objectFactory.createArbeidsforholdType();
            arbeidsforhold.setArbeidsgiverNavn(dto.getArbeidsgiverNavn());
            arbeidsforhold.setGradering(dto.getGradering());
            arbeidsforhold.setAktivitetDagsats(dto.getDagsats());
            arbeidsforhold.setProsentArbeid(BigInteger.valueOf(dto.getProsentArbeid()));
            arbeidsforhold.setStillingsprosent(BigInteger.valueOf(dto.getStillingsprosent()));
            arbeidsforhold.setUttaksgrad(BigInteger.valueOf(dto.getUttaksgrad()));
            arbeidsforhold.setUtbetalingsgrad(BigInteger.valueOf(dto.getUtbetalingsgrad()));
            liste.getArbeidsforhold().add(arbeidsforhold);
        });
        return liste;
    }

    private BeregningsgrunnlagRegelListeType konverterBeregningsgrunnlagRegelListe(List<DokumentTypeData> dokumentTypeDataListe) {
        BeregningsgrunnlagRegelListeType liste = objectFactory.createBeregningsgrunnlagRegelListeType();
        for (DokumentTypeData data : finnListeMedVerdierAv(InnvilgelseForeldrepengerDokument.BEREGNINGSGRUNNLAGREGEL, dokumentTypeDataListe)) {
            String feltNavnMedIndeks = data.getDoksysId();
            String strukturertFelt = finnStrukturertVerdiAv(feltNavnMedIndeks, dokumentTypeDataListe);
            BeregningsgrunnlagRegelDto dto = FlettefeltJsonObjectMapper.readValue(strukturertFelt, BeregningsgrunnlagRegelDto.class);
            BeregningsgrunnlagRegelType beregningsgrunnlagRegel = objectFactory.createBeregningsgrunnlagRegelType();
            beregningsgrunnlagRegel.setRegelStatus(tilStatusTypeKode(dto.getStatus()));
            beregningsgrunnlagRegel.setAntallArbeidsgivereIBeregning(BigInteger.valueOf(dto.getAntallArbeidsgivereIBeregning()));
            beregningsgrunnlagRegel.setBesteBeregning(dto.getBesteBeregning());
            beregningsgrunnlagRegel.setTotaltRedusertBeregningsgrunnlag(dto.getTotaltRedusertBeregningsgrunnlag());
            //UOBLIGATORISKE FELTER
            if (dto.getNavBetalerRestbeløp() != null) {
                beregningsgrunnlagRegel.setNavBetalerRestbeløp(dto.getNavBetalerRestbeløp());
            }
            if (dto.getTilstøtendeYtelse() != null) {
                beregningsgrunnlagRegel.setTilstøtendeYtelse(fraRelatertYtelseKode(dto.getTilstøtendeYtelse()));
            }
            beregningsgrunnlagRegel.setAndelListe(fraAndelListe(dto.getBeregningsgrunnlagAndelDto()));
            liste.getBeregningsgrunnlagRegel().add(beregningsgrunnlagRegel);
        }
        return liste;
    }

    private TilstøtendeYtelseKode fraRelatertYtelseKode(String kode) {
        Map<String, TilstøtendeYtelseKode> ytelseKodeMap = new HashMap<>();
        ytelseKodeMap.put(RelatertYtelseType.SYKEPENGER.getKode(), TilstøtendeYtelseKode.SYK_FØR_FP);
        ytelseKodeMap.put(RelatertYtelseType.FORELDREPENGER.getKode(), TilstøtendeYtelseKode.FP_FØR_FP);
        ytelseKodeMap.put(RelatertYtelseType.PÅRØRENDESYKDOM.getKode(), TilstøtendeYtelseKode.PLEIE_FØR_FP);
        ytelseKodeMap.put(RelatertYtelseType.SVANGERSKAPSPENGER.getKode(), TilstøtendeYtelseKode.SVANGER_FØR_FP);
        ytelseKodeMap.put(RelatertYtelseType.ARBEIDSAVKLARINGSPENGER.getKode(), TilstøtendeYtelseKode.ARB_8_49);
        if (ytelseKodeMap.containsKey(kode)) {
            return ytelseKodeMap.get(kode);
        }
        throw new IllegalArgumentException("Finner ingen TilstøtendeYtelseKode fra RelatertYtelseType: " + kode);
    }

    private AndelListeType fraAndelListe(List<BeregningsgrunnlagAndelDto> beregningsgrunnlagAndelDto) {
        AndelListeType liste = objectFactory.createAndelListeType();
        beregningsgrunnlagAndelDto.forEach(dto -> {
            AndelType andel = objectFactory.createAndelType();
            andel.setStatus(tilStatusTypeKode(dto.getStatus()));
            andel.setDagsats(Long.parseLong(dto.getDagsats()));
            andel.setMånedsinntekt(Long.parseLong(dto.getMånedsinntekt()));
            andel.setÅrsinntekt(Long.parseLong(dto.getÅrsinntekt()));
            if (StatusTypeKode.ARBEIDSTAKER.equals(andel.getStatus())) {
                andel.setArbeidsgiverNavn(dto.getArbeidsgiverNavn());
            } else if (StatusTypeKode.SELVSTENDIG_NÆRINGSDRIVENDE.equals(andel.getStatus())) {
                Optional.ofNullable(dto.getSisteLignedeÅr()).map(Long::parseLong).ifPresent(andel::setSisteLignedeÅr);
                Optional.ofNullable(dto.getPensjonsgivendeInntekt()).map(Long::parseLong).ifPresent(andel::setPensjonsgivendeInntekt);
            }
            liste.getAndel().add(andel);
        });
        return liste;
    }

    private BehandlingsTypeKode fra(String behandlingsType) {
        if (REVURDERING.equals(behandlingsType)) {
            return BehandlingsTypeKode.REVURDERING;
        }
        if (KLAGE.equals(behandlingsType)) {
            return BehandlingsTypeKode.KLAGE;
        }
        return BehandlingsTypeKode.FOERSTEGANGSBEHANDLING;
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        BrevdataType brevdataType = objectFactory.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return objectFactory.createBrevdata(brevdataType);
    }

    private StatusTypeKode tilStatusTypeKode(String statuskode) {
        Map<String, StatusTypeKode> aktivitetStatusKodeStatusTypeKodeMap = new HashMap<>();
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.ARBEIDSTAKER.getKode(), StatusTypeKode.ARBEIDSTAKER);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.FRILANSER.getKode(), StatusTypeKode.FRILANSER);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE.getKode(), StatusTypeKode.SELVSTENDIG_NÆRINGSDRIVENDE);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.KOMBINERT_AT_FL.getKode(), StatusTypeKode.KOMBINERT_AT_FL);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.KOMBINERT_AT_FL_SN.getKode(), StatusTypeKode.KOMBINERT_AT_FL_SN);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.KOMBINERT_AT_SN.getKode(), StatusTypeKode.KOMBINERT_AT_SN);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.KOMBINERT_FL_SN.getKode(), StatusTypeKode.KOMBINERT_FL_SN);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.DAGPENGER.getKode(), StatusTypeKode.DAGPENGER);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.ARBEIDSAVKLARINGSPENGER.getKode(), StatusTypeKode.ARBEIDSAVKLARINGSPENGER);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.MILITÆR_ELLER_SIVIL.getKode(), StatusTypeKode.MILITÆR_ELLER_SIVIL);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.TILSTØTENDE_YTELSE.getKode(), StatusTypeKode.TILSTØTENDE_YTELSE);
        aktivitetStatusKodeStatusTypeKodeMap.put(AktivitetStatus.BRUKERS_ANDEL.getKode(), StatusTypeKode.BRUKERSANDEL);

        if (aktivitetStatusKodeStatusTypeKodeMap.containsKey(statuskode)) {
            return aktivitetStatusKodeStatusTypeKodeMap.get(statuskode);
        }
        throw new IllegalArgumentException("Utviklerfeil: Fant ikke riktig aktivitetstatus " + statuskode);
    }

    private KonsekvensForYtelseKode tilKonsekvensForYtelseKode(String konsekvens) {
        Map<String, KonsekvensForYtelseKode> konsekvensForYtelseKodeMap = new HashMap<>();
        konsekvensForYtelseKodeMap.put(ENDRING_BEREGNING_OG_UTTAK, KonsekvensForYtelseKode.ENDRING_I_BEREGNING_OG_UTTAK);
        konsekvensForYtelseKodeMap.put(KonsekvensForYtelsen.ENDRING_I_BEREGNING.getKode(), KonsekvensForYtelseKode.ENDRING_I_BEREGNING);
        konsekvensForYtelseKodeMap.put(KonsekvensForYtelsen.INGEN_ENDRING.getKode(), KonsekvensForYtelseKode.INGEN_ENDRING);
        konsekvensForYtelseKodeMap.put(KonsekvensForYtelsen.ENDRING_I_UTTAK.getKode(), KonsekvensForYtelseKode.ENDRING_I_UTTAK);
        konsekvensForYtelseKodeMap.put(KonsekvensForYtelsen.FORELDREPENGER_OPPHØRER.getKode(), KonsekvensForYtelseKode.FORELDREPENGER_OPPHØRER);
        if (konsekvensForYtelseKodeMap.containsKey(konsekvens)) {
            return konsekvensForYtelseKodeMap.get(konsekvens);
        }
        return KonsekvensForYtelseKode.INGEN_ENDRING;
    }

    private BehandlingsResultatKode tilBehandlingsResultatKode(String behandlingsresultatkode) {
        if (BehandlingResultatType.INNVILGET.getKode().equals(behandlingsresultatkode)) {
            return BehandlingsResultatKode.INNVILGET;
        } else if (BehandlingResultatType.AVSLÅTT.getKode().equals(behandlingsresultatkode)) {
            return BehandlingsResultatKode.AVSLÅTT;
        } else if (BehandlingResultatType.OPPHØR.getKode().equals(behandlingsresultatkode)) {
            return BehandlingsResultatKode.OPPHØR;
        } else if (BehandlingResultatType.FORELDREPENGER_ENDRET.getKode().equals(behandlingsresultatkode)) {
            return BehandlingsResultatKode.FORELDREPENGER_ENDRET;
        } else {
            return BehandlingsResultatKode.INGEN_ENDRING;
        }
    }

    private RelasjonskodeKode tilRelasjonskode(String brukerRolle, String navBrukerKjønn) {
        if (RelasjonsRolleType.MORA.getKode().equals(brukerRolle)) {
            return RelasjonskodeKode.MOR;
        } else if (NavBrukerKjønn.MANN.getKode().equals(navBrukerKjønn)) {
            return RelasjonskodeKode.FAR;
        } else {
            return RelasjonskodeKode.MEDMOR;
        }
    }
}
