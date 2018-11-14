package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.beregningsgrunnlag.HentGrunnlagsdataTjeneste;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.felles.v2.FloatOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.LongOpplysning;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.GjennomsnittligPensjonsgivendeInntekt;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.RefusjonTilArbeidsgiver;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsgrunnlag;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class BeregningsgrunnlagXmlTjenesteForeldrepenger implements BeregningsgrunnlagXmlTjeneste {

    private ObjectFactory beregningObjectFactory;
    private HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste;

    public BeregningsgrunnlagXmlTjenesteForeldrepenger() {
        //For CDI
    }

    @Inject
    public BeregningsgrunnlagXmlTjenesteForeldrepenger(HentGrunnlagsdataTjeneste hentGrunnlagsdataTjeneste) {
        this.beregningObjectFactory = new ObjectFactory();
        this.hentGrunnlagsdataTjeneste = hentGrunnlagsdataTjeneste;
    }


    @Override
    public void setBeregningsgrunnlag(Beregningsresultat beregningsresultat, Behandling behandling) {
        BeregningsgrunnlagForeldrepenger beregningsgrunnlag = beregningObjectFactory.createBeregningsgrunnlagForeldrepenger();
        Optional<no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag> beregningsgrunnlagDomeneOptional = hentGrunnlagsdataTjeneste.hentGjeldendeBeregningsgrunnlag(behandling);
        if (beregningsgrunnlagDomeneOptional.isPresent()) {
            no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag beregningsgrunnlagDomene = beregningsgrunnlagDomeneOptional.get();
            setBeregningsgrunnlagAktivitetStatuser(beregningsgrunnlag, beregningsgrunnlagDomene.getAktivitetStatuser());
            beregningsgrunnlag.setDekningsgrad(VedtakXmlUtil.lagLongOpplysning(beregningsgrunnlagDomene.getDekningsgrad()));
            VedtakXmlUtil.lagDateOpplysning(beregningsgrunnlagDomene.getSkjæringstidspunkt()).ifPresent(dateOpplysning -> beregningsgrunnlag.setSkjaeringstidspunkt(dateOpplysning));
            setBeregningsgrunnlagPerioder(beregningsgrunnlag, beregningsgrunnlagDomene.getBeregningsgrunnlagPerioder());
        }
        Beregningsgrunnlag beregningsgrunnlag1 = new Beregningsgrunnlag();
        beregningsgrunnlag1.getAny().add(new no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.ObjectFactory().createBeregningsgrunnlag(beregningsgrunnlag));
        beregningsresultat.setBeregningsgrunnlag(beregningsgrunnlag1);
    }

    private void setBeregningsgrunnlagPerioder(BeregningsgrunnlagForeldrepenger beregningsgrunnlag, List<BeregningsgrunnlagPeriode> beregningsgrunnlagPerioder) {
        List<no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPeriode> periodeListe = beregningsgrunnlagPerioder
            .stream()
            .map(beregningsgrunnlagPeriode -> konverterFraDomene(beregningsgrunnlagPeriode)).collect(Collectors.toList());
        beregningsgrunnlag.getBeregningsgrunnlagPeriode().addAll(periodeListe);
    }

    private void setBeregningsgrunnlagAktivitetStatuser(BeregningsgrunnlagForeldrepenger beregningsgrunnlag, List<BeregningsgrunnlagAktivitetStatus> aktivitetStatuser) {
        List<no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.AktivitetStatus> aktivitetStatusListe = aktivitetStatuser
            .stream()
            .map(status -> konverterFraDomene(status)).collect(Collectors.toList());
        beregningsgrunnlag.getAktivitetstatuser().addAll(aktivitetStatusListe);
    }

    private no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.AktivitetStatus konverterFraDomene(BeregningsgrunnlagAktivitetStatus beregningsgrunnlagAktivitetStatus) {
        no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.AktivitetStatus kontrakt = new no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.AktivitetStatus();
        kontrakt.setAktivitetStatus(VedtakXmlUtil.lagKodeverkOpplysning(beregningsgrunnlagAktivitetStatus.getAktivitetStatus()));
        kontrakt.setHjemmel(VedtakXmlUtil.lagKodeverkOpplysning(beregningsgrunnlagAktivitetStatus.getHjemmel()));
        return kontrakt;
    }

    private no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPeriode konverterFraDomene(BeregningsgrunnlagPeriode domene) {
        no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPeriode kontrakt = new no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPeriode();
        kontrakt.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(domene.getBeregningsgrunnlagPeriodeFom(), domene.getBeregningsgrunnlagPeriodeTom()));
        kontrakt.setBrutto(lagFloatOpplysning(domene.getBruttoPrÅr()));
        kontrakt.setAvkortet(lagFloatOpplysning(domene.getAvkortetPrÅr()));
        kontrakt.setRedusert(lagFloatOpplysning(domene.getAvkortetPrÅr()));
        Optional.ofNullable(domene.getDagsats()).ifPresent(sats -> kontrakt.setDagsats(VedtakXmlUtil.lagLongOpplysning(sats)));
        setBeregningsgrunnlagPrStatusOgAndel(kontrakt, domene.getBeregningsgrunnlagPrStatusOgAndelList());

        return kontrakt;
    }

    private FloatOpplysning lagFloatOpplysning(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        return VedtakXmlUtil.lagFloatOpplysning(bigDecimal.floatValue());
    }

    private LongOpplysning lagLongOpplysning(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        return VedtakXmlUtil.lagLongOpplysning(bigDecimal.longValue());
    }

    private void setBeregningsgrunnlagPrStatusOgAndel(no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPeriode kontrakt, List<BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelList) {
        List<no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPrStatusOgAndel> beregningsgrunnlagPrStatusOgAndelKontrakt = beregningsgrunnlagPrStatusOgAndelList
            .stream()
            .map(beregningsgrunnlagPrStatusOgAndel -> konverterFraDomene(beregningsgrunnlagPrStatusOgAndel)).collect(Collectors.toList());
        kontrakt.getBeregningsgrunnlagPrStatusOgAndel().addAll(beregningsgrunnlagPrStatusOgAndelKontrakt);
    }

    private no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPrStatusOgAndel konverterFraDomene(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPrStatusOgAndel kontrakt = new no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.fp.v2.BeregningsgrunnlagPrStatusOgAndel();
        kontrakt.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeFom(), beregningsgrunnlagPrStatusOgAndel.getBeregningsperiodeTom()));
        kontrakt.setAktivitetstatus(VedtakXmlUtil.lagKodeverkOpplysning(beregningsgrunnlagPrStatusOgAndel.getAktivitetStatus()));
        beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsgiver).ifPresent(arbeidsgiver -> kontrakt.setVirksomhetsnummer(VedtakXmlUtil.lagStringOpplysning(arbeidsgiver.getIdentifikator())));
        kontrakt.setErTidsbegrensetArbeidsforhold(VedtakXmlUtil.lagBooleanOpplysning(beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getErTidsbegrensetArbeidsforhold).orElse(null)));
        kontrakt.setBrutto(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getBruttoPrÅr()));
        kontrakt.setAvkortet(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getAvkortetPrÅr()));
        kontrakt.setRedusert(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getRedusertPrÅr()));
        kontrakt.setOverstyrt(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getOverstyrtPrÅr()));
        kontrakt.setInntektskategori(VedtakXmlUtil.lagKodeverkOpplysning(beregningsgrunnlagPrStatusOgAndel.getInntektskategori()));
        kontrakt.setRefusjonTilArbeidsgiver(convertRefusjonTilArbeidsgiverFraDomene(beregningsgrunnlagPrStatusOgAndel));
        beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getNaturalytelseBortfaltPrÅr).ifPresent(nybpå -> kontrakt.setNaturalytelseBortfall(VedtakXmlUtil.lagFloatOpplysning(nybpå.floatValue())));
        kontrakt.setBeregnet(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getBeregnetPrÅr()));
        kontrakt.setGjennomsnittligPensjonsgivendeInntekt(konverterGjennomsnittligPensjonsgivendeInntektFraDomene(beregningsgrunnlagPrStatusOgAndel));
        kontrakt.setTilstoetendeYtelseType(VedtakXmlUtil.lagKodeverkOpplysning(beregningsgrunnlagPrStatusOgAndel.getYtelse()));
        kontrakt.setTilstoetendeYtelse(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getÅrsbeløpFraTilstøtendeYtelseVerdi()));
        kontrakt.setAvkortetBrukersAndel(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getAvkortetBrukersAndelPrÅr()));
        kontrakt.setRedusertBrukersAndel(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getRedusertBrukersAndelPrÅr()));
        kontrakt.setDagsatsBruker(VedtakXmlUtil.lagLongOpplysning(beregningsgrunnlagPrStatusOgAndel.getDagsatsBruker()));
        kontrakt.setDagsatsArbeidsgiver(VedtakXmlUtil.lagLongOpplysning(beregningsgrunnlagPrStatusOgAndel.getDagsatsArbeidsgiver()));

        return kontrakt;

    }

    private GjennomsnittligPensjonsgivendeInntekt konverterGjennomsnittligPensjonsgivendeInntektFraDomene(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        GjennomsnittligPensjonsgivendeInntekt gjennomsnittligPensjonsgivendeInntekt = new GjennomsnittligPensjonsgivendeInntekt();
        gjennomsnittligPensjonsgivendeInntekt.setPgisnitt(lagLongOpplysning(beregningsgrunnlagPrStatusOgAndel.getPgiSnitt()));
        gjennomsnittligPensjonsgivendeInntekt.setPgiaar1(lagLongOpplysning(beregningsgrunnlagPrStatusOgAndel.getPgi1()));
        gjennomsnittligPensjonsgivendeInntekt.setPgiaar2(lagLongOpplysning(beregningsgrunnlagPrStatusOgAndel.getPgi2()));
        gjennomsnittligPensjonsgivendeInntekt.setPgiaar3(lagLongOpplysning(beregningsgrunnlagPrStatusOgAndel.getPgi3()));
        return gjennomsnittligPensjonsgivendeInntekt;

    }

    private RefusjonTilArbeidsgiver convertRefusjonTilArbeidsgiverFraDomene(BeregningsgrunnlagPrStatusOgAndel beregningsgrunnlagPrStatusOgAndel) {
        RefusjonTilArbeidsgiver refusjonTilArbeidsgiver = new RefusjonTilArbeidsgiver();
        refusjonTilArbeidsgiver.setRefusjonskrav(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)));
        refusjonTilArbeidsgiver.setMaksimal(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getMaksimalRefusjonPrÅr()));
        refusjonTilArbeidsgiver.setAvkortet(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getAvkortetRefusjonPrÅr()));
        refusjonTilArbeidsgiver.setRedusert(lagFloatOpplysning(beregningsgrunnlagPrStatusOgAndel.getRedusertRefusjonPrÅr()));
        return refusjonTilArbeidsgiver;
    }
}
