package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.avklarFritekst;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettObligatoriskeFlettefelt;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles.opprettStrukturertFlettefeltListe;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.ANTALL_BARN;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.BEHANDLINGSTYPE;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.FRITEKST;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.GJELDER_FØDSEL;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.KJØNN;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.KLAGE_FRIST_UKER;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.MOTTATT_DATO;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.PERSON_STATUS;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.RELASJONSKODE;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.SISTE_DAG_I_FELLES_PERIODE;
import static no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt.SOKERSNAVN;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMerger;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.PersonstatusKodeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.BehandlingsResultatKode;

public class InnvilgelseForeldrepengerDokument implements DokumentType {
    public static final String DEKNINGSGRAD = "dekningsgrad";
    public static final String DAGSATS = "dagsats";
    public static final String MÅNEDSBELØP = "månedsbeløp";
    public static final String STØNADSPERIODEFOM = "stønadsperiodeFom";
    public static final String STØNADSPERIODETOM = "stønadsperiodeTom";
    public static final String TOTALBRUKERANDEL = "totalBrukerAndel";
    public static final String TOTALARBEIDSGIVERANDEL = "totalArbeidsgiverAndel";
    public static final String ANTALLARBEIDSGIVERE = "antallArbeidsgivere";
    public static final String ANNENFORELDERHARRETT = "annenForelderHarRett";
    public static final String ALENEOMSORG = "aleneomsorg";
    public static final String BARNERFØDT = "barnErFødt";
    public static final String DAGERTAPTFØRTERMIN = "dagerTaptFørTermin";
    public static final String ANTALLPERIODER = "antallPerioder";
    public static final String AVSLAGFINNES = "avslagFinnes";
    public static final String SISTEUTBETALINGSDAG = "sisteUtbetalingsdag";
    public static final String SISTEDAGMEDUTSETTELSE = "sisteDagMedUtsettelse";
    public static final String DISPONIBLEDAGER = "disponibleDager";
    public static final String DISPONIBLEFELLESDAGER = "disponibleFellesDager";
    public static final String SEKSG = "seksG";
    public static final String LOVHJEMMEL_VURDERING = "lovhjemmelVurdering";
    public static final String LOVHJEMMEL_BEREGNING = "lovhjemmelBeregning";
    public static final String BEREGNINGSGRUNNLAGREGEL = "beregningsgrunnlagRegel";
    public static final String FORELDREPENGEPERIODENUTVIDETUKER = "foreldrepengeperiodenUtvidetUker";
    public static final String INNTEKTOVERSEKSG = "inntektOverSeksG";
    public static final String INNVILGETFINNES = "innvilgetFinnes";
    public static final String GRADERINGFINNES = "graderingFinnes";
    public static final String SISTEDAGAVSISTEPERIODE = "sisteDagAvSistePeriode";
    public static final String INNTEKTMOTTATTARBGIVER = "inntektMottattArbgiver";
    public static final String BEHANDLINGSRESULTAT = "behandlingsResultat";
    public static final String OVERBETALING = "overbetaling";
    public static final String KONSEKVENSFORYTELSE = "konsekvensForYtelse";
    public static final String OVERSTYRT_BEREGNING = "overstyrBeregning";

    private BrevParametere brevParametere;
    private String fritekst;

    public InnvilgelseForeldrepengerDokument(BrevParametere brevParametere) {
        this.brevParametere = brevParametere;
    }

    public InnvilgelseForeldrepengerDokument(BrevParametere brevParametere, String fritekst) {
        this.brevParametere = brevParametere;
        this.fritekst = fritekst;
    }

    @Override
    public String getDokumentMalType() {
        return DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK;
    }

    @Override
    public List<Flettefelt> getFlettefelter(DokumentTypeDto dtoIn) {
        if (dtoIn.getHarIkkeBehandlingsResultat()) {
            throw DokumentBestillerFeil.FACTORY.behandlingManglerResultat(dtoIn.getBehandlingId()).toException();
        }
        if (!(dtoIn instanceof DokumentTypeMedPerioderDto)) {
            throw DokumentBestillerFeil.FACTORY.behandlingManglerResultat(dtoIn.getBehandlingId()).toException();
        }
        DokumentTypeMedPerioderDto dto = (DokumentTypeMedPerioderDto) dtoIn;
        List<Flettefelt> flettefelter = new ArrayList<>();
        leggtilObligatoriskeFelter(dto, flettefelter);
        leggtilIkkeobligatoriskeFelter(dto, flettefelter);
        leggtilStrukturerteFelter(dto, flettefelter);
        Optional<String> faktiskFritekst = avklarFritekst(fritekst, dto.getDokumentBehandlingsresultatDto().getFritekst());
        faktiskFritekst.ifPresent(fritekstEn -> flettefelter.add(opprettFlettefelt(Flettefelt.FRITEKST, fritekstEn)));
        return flettefelter;
    }

    private void leggtilStrukturerteFelter(DokumentTypeMedPerioderDto dto, List<Flettefelt> flettefelter) {
        List<PeriodeDto> periodeDtos = new ArrayList<>(dto.getPeriode());
        periodeDtos = PeriodeMerger.mergePerioder(periodeDtos);
        flettefelter.addAll(opprettStrukturertFlettefeltListe(Flettefelt.PERIODE, periodeDtos));

        flettefelter.addAll(opprettStrukturertFlettefeltListe(BEREGNINGSGRUNNLAGREGEL, dto.getDokumentBeregningsgrunnlagDto().getBeregningsgrunnlagRegel()));

        String konsekvensForYtelse = dto.getDokumentBehandlingsresultatDto().getKonsekvensForYtelse();
        boolean revurdering = DokumentMapperKonstanter.REVURDERING.equals(dto.getBehandlingsTypePositivtVedtak());
        boolean innvilget = BehandlingsResultatKode.INNVILGET.equals(dto.getDokumentBehandlingsresultatDto().getBehandlingsResultat());
        boolean innvilgetRevurdering = revurdering && innvilget;
        flettefelter.add(opprettFlettefelt(LOVHJEMMEL_VURDERING, DokumentMalFelles.formaterLovhjemlerUttak(dto.getLovhjemmelVurdering(), konsekvensForYtelse, innvilgetRevurdering)));
        flettefelter.add(opprettFlettefelt(LOVHJEMMEL_BEREGNING, DokumentMalFelles.formaterLovhjemlerForBeregning(dto.getDokumentBeregningsgrunnlagDto().getLovhjemmelBeregning(), konsekvensForYtelse, innvilgetRevurdering)));
    }

    private void leggtilIkkeobligatoriskeFelter(DokumentTypeMedPerioderDto dto, List<Flettefelt> flettefelter) {
        if (dto.getSisteDagIFellesPeriode() != null) {
            flettefelter.add(opprettFlettefelt(SISTE_DAG_I_FELLES_PERIODE, dto.getSisteDagIFellesPeriode().toString()));
        }
        if (dto.getSisteUtbetalingsdag() != null) {
            flettefelter.add(opprettFlettefelt(SISTEUTBETALINGSDAG, dto.getSisteUtbetalingsdag().toString()));
        }
        if (dto.getSisteDagMedUtsettelse() != null) {
            flettefelter.add(opprettFlettefelt(SISTEDAGMEDUTSETTELSE, dto.getSisteDagMedUtsettelse().toString()));
        }
        if (dto.getDokumentBehandlingsresultatDto().getFritekst().isPresent()) {
            flettefelter.add(opprettFlettefelt(FRITEKST, dto.getDokumentBehandlingsresultatDto().getFritekst().get()));
        }
        if (dto.getForeldrepengeperiodenUtvidetUker() != null) {
            flettefelter.add(opprettFlettefelt(FORELDREPENGEPERIODENUTVIDETUKER, dto.getForeldrepengeperiodenUtvidetUker().toString()));
        }
    }

    private void leggtilObligatoriskeFelter(DokumentTypeMedPerioderDto dto, List<Flettefelt> flettefelter) {
        flettefelter.add(opprettObligatoriskeFlettefelt(BEHANDLINGSTYPE, dto.getBehandlingsTypePositivtVedtak()));
        flettefelter.add(opprettObligatoriskeFlettefelt(SOKERSNAVN, dto.getSøkersNavn()));
        flettefelter.add(opprettObligatoriskeFlettefelt(PERSON_STATUS, dto.getPersonstatus()));
        flettefelter.add(opprettObligatoriskeFlettefelt(KJØNN, dto.getBrukerKjønnKode()));
        flettefelter.add(opprettObligatoriskeFlettefelt(RELASJONSKODE, dto.getRelasjonsKode()));
        flettefelter.add(opprettObligatoriskeFlettefelt(MOTTATT_DATO, dto.getMottattDato()));

        flettefelter.add(opprettObligatoriskeFlettefelt(DEKNINGSGRAD, dto.getDokumentBeregningsgrunnlagDto().getDekningsgrad()));
        flettefelter.add(opprettObligatoriskeFlettefelt(BEHANDLINGSRESULTAT, dto.getDokumentBehandlingsresultatDto().getBehandlingsResultat()));
        flettefelter.add(opprettObligatoriskeFlettefelt(OVERBETALING, dto.getDokumentBeregningsgrunnlagDto().getOverbetaling()));
        flettefelter.add(opprettObligatoriskeFlettefelt(DAGSATS, dto.getDokumentBeregningsgrunnlagDto().getDagsats()));
        flettefelter.add(opprettObligatoriskeFlettefelt(MÅNEDSBELØP, dto.getDokumentBeregningsgrunnlagDto().getMånedsbeløp()));
        flettefelter.add(opprettObligatoriskeFlettefelt(STØNADSPERIODEFOM, dto.getStønadsperiodeFom()));
        flettefelter.add(opprettObligatoriskeFlettefelt(STØNADSPERIODETOM, dto.getStønadsperiodeTom()));
        flettefelter.add(opprettObligatoriskeFlettefelt(TOTALBRUKERANDEL, dto.getDokumentBeregningsgrunnlagDto().getTotalBrukerAndel()));
        flettefelter.add(opprettObligatoriskeFlettefelt(TOTALARBEIDSGIVERANDEL, dto.getDokumentBeregningsgrunnlagDto().getTotalArbeidsgiverAndel()));
        flettefelter.add(opprettObligatoriskeFlettefelt(ANTALLARBEIDSGIVERE, dto.getDokumentBeregningsresultatDto().getAntallArbeidsgivere()));
        flettefelter.add(opprettObligatoriskeFlettefelt(ANNENFORELDERHARRETT, dto.getAnnenForelderHarRett()));
        flettefelter.add(opprettObligatoriskeFlettefelt(ALENEOMSORG, dto.getAleneomsorg()));
        flettefelter.add(opprettObligatoriskeFlettefelt(ANTALL_BARN, dto.getAntallBarn()));
        flettefelter.add(opprettObligatoriskeFlettefelt(BARNERFØDT, dto.getBarnErFødt()));
        flettefelter.add(opprettObligatoriskeFlettefelt(GJELDER_FØDSEL, dto.getGjelderFødsel()));
        flettefelter.add(opprettObligatoriskeFlettefelt(DAGERTAPTFØRTERMIN, dto.getDagerTaptFørTermin()));
        flettefelter.add(opprettObligatoriskeFlettefelt(ANTALLPERIODER, dto.getAntallPerioder()));
        flettefelter.add(opprettObligatoriskeFlettefelt(AVSLAGFINNES, dto.getAvslagFinnes()));
        flettefelter.add(opprettObligatoriskeFlettefelt(INNVILGETFINNES, dto.getInnvilgetFinnes()));
        flettefelter.add(opprettObligatoriskeFlettefelt(GRADERINGFINNES, dto.isInnvilgetGraderingFinnes()));
        flettefelter.add(opprettObligatoriskeFlettefelt(OVERSTYRT_BEREGNING, dto.isOverstyrtBeløpBeregning()));

        flettefelter.add(opprettObligatoriskeFlettefelt(INNTEKTOVERSEKSG, dto.getDokumentBeregningsgrunnlagDto().getInntektOverSeksG()));
        flettefelter.add(opprettObligatoriskeFlettefelt(SISTEDAGAVSISTEPERIODE, dto.getSisteDagAvSistePeriode()));
        flettefelter.add(opprettObligatoriskeFlettefelt(INNTEKTMOTTATTARBGIVER, dto.isInntektMottattArbgiver()));
        flettefelter.add(opprettObligatoriskeFlettefelt(KONSEKVENSFORYTELSE, dto.getDokumentBehandlingsresultatDto().getKonsekvensForYtelse()));

        flettefelter.add(opprettObligatoriskeFlettefelt(DISPONIBLEDAGER, dto.getDisponibleDager()));
        flettefelter.add(opprettObligatoriskeFlettefelt(DISPONIBLEFELLESDAGER, dto.getDisponibleFellesDager()));
        flettefelter.add(opprettObligatoriskeFlettefelt(SEKSG, dto.getDokumentBeregningsgrunnlagDto().getSeksG()));

        flettefelter.add(opprettObligatoriskeFlettefelt(KLAGE_FRIST_UKER, brevParametere.getKlagefristUker().toString()));
    }



    @Override
    public String getPersonstatusVerdi(PersonstatusType personstatus) {
        return Objects.equals(personstatus, PersonstatusType.DØD) ? PersonstatusKodeType.DOD.value() : PersonstatusKodeType.ANNET.value();
    }

    @Override
    public boolean harPerioder() {
        return true;
    }
}
