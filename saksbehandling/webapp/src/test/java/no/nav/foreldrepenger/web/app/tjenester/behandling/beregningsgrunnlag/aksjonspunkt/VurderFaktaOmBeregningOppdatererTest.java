package no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.FaktaOmBeregningTilfelle;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.FastsattBeløpTilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.FastsettBGTilstøtendeYtelseDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.RedigerbarAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.aksjonspunkt.dto.VurderFaktaOmBeregningDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class VurderFaktaOmBeregningOppdatererTest {


    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String ARB_ID = "123124";
    private static final String ORGNR = "7887897435973";
    private static final Long ANDELSNR = 1L;
    public static final String BEGRUNNELSE = "Dette er ein begrunnelse.";
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private VurderFaktaOmBeregningOppdaterer vurderFaktaOmBeregningOppdaterer;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
    @Inject
    private HistorikkTjenesteAdapter historikkTjenesteAdapter;
    public ScenarioMorSøkerForeldrepenger scenario;
    public Behandling behandling;
    private Arbeidsgiver virksomheten;
    private AksjonspunktRepository aksjonspunktRepository;
    private Aksjonspunkt aksjonspunkt;

    @Before
    public void setup() {
        ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjeneste(null);
        this.vurderFaktaOmBeregningOppdaterer = new VurderFaktaOmBeregningOppdaterer(historikkTjenesteAdapter, repositoryProvider, resultatRepositoryProvider, arbeidsgiverHistorikkinnslagTjeneste);
        this.scenario = ScenarioMorSøkerForeldrepenger.forAktør(AKTØR_ID);
        this.behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        BeregningArbeidsgiverTestUtil arbeidsgiverTestUtil = new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository());
        this.virksomheten = arbeidsgiverTestUtil.forArbeidsgiverVirksomhet(ORGNR);
        lagBeregningsgrunnlag();
        aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.VURDER_FAKTA_FOR_ATFL_SN);
    }


    @Test
    public void skal_ikkje_lage_historikkinnslag_om_ingenting_er_endret() {
        // Arrange
        utførOgReåpneAksjonspunkt();
        FastsettBGTilstøtendeYtelseDto tyDto = lagTYDtoForIngenEndring();
        VurderFaktaOmBeregningDto dto = new VurderFaktaOmBeregningDto(BEGRUNNELSE,
            Collections.singletonList(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE),
            tyDto);

        // Act
        vurderFaktaOmBeregningOppdaterer.oppdater(dto, behandling);

        // Assert
        historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);
        List<Historikkinnslag> historikk = repositoryProvider.getHistorikkRepository().hentHistorikk(behandling.getId());
        assertThat(historikk.isEmpty()).isTrue();
    }


    @Test
    public void skal_lage_historikkinnslag_om_kun_begrunnelse_er_endret() {
        // Arrange
        utførOgReåpneAksjonspunkt();
        FastsettBGTilstøtendeYtelseDto tyDto = lagTYDtoForIngenEndring();
        VurderFaktaOmBeregningDto dto = new VurderFaktaOmBeregningDto("Ny begrunnelse",
            Collections.singletonList(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE),
            tyDto);


        // Act
        vurderFaktaOmBeregningOppdaterer.oppdater(dto, behandling);

        // Assert
        historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);
        List<Historikkinnslag> historikk = repositoryProvider.getHistorikkRepository().hentHistorikk(behandling.getId());
        assertThat(historikk.size()).isEqualTo(1);
        List<HistorikkinnslagDel> deler = historikk.get(0).getHistorikkinnslagDeler();
        assertThat(deler.size()).isEqualTo(2);
        assertThat(deler.get(0).getBegrunnelse().get()).isEqualTo("Ny begrunnelse");
        assertThat(deler.get(1).getSkjermlenke().get()).isEqualTo(SkjermlenkeType.FAKTA_OM_BEREGNING.getKode());

    }

    @Test
    public void skal_lage_kun_eit_historikkinnslag_med_skjermlenke() {
        // Arrange
        utførOgReåpneAksjonspunkt();
        FastsettBGTilstøtendeYtelseDto tyDto = lagTYDtoForEndring();
        VurderFaktaOmBeregningDto dto = new VurderFaktaOmBeregningDto("Ny begrunnelse",
            Collections.singletonList(FaktaOmBeregningTilfelle.TILSTØTENDE_YTELSE),
            tyDto);

        // Act
        vurderFaktaOmBeregningOppdaterer.oppdater(dto, behandling);

        // Assert
        historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);

        List<Historikkinnslag> historikk = repositoryProvider.getHistorikkRepository().hentHistorikk(behandling.getId());
        assertThat(historikk.size()).isEqualTo(1);
        List<HistorikkinnslagDel> deler = historikk.get(0).getHistorikkinnslagDeler();
        assertThat(deler.size()).isEqualTo(2);
        assertThat(deler.get(1).getBegrunnelse().get()).isEqualTo("Ny begrunnelse");
        assertThat(deler.stream().filter(del -> del.getSkjermlenke().isPresent()).count()).isEqualTo(1);
    }

    private FastsettBGTilstøtendeYtelseDto lagTYDtoForIngenEndring() {
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.ARBEIDSTAKER;
        FastsettBGTilstøtendeYtelseDto tyDto = lagTYDto(nyAndel, lagtTilAvSaksbehandler, fastsatt, faktor, inntektskategori);
        settOverstyrtIForrigeGrunnlag(fastsatt);
        return tyDto;
    }

    private FastsettBGTilstøtendeYtelseDto lagTYDtoForEndring() {
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 200000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        FastsettBGTilstøtendeYtelseDto tyDto = lagTYDto(nyAndel, lagtTilAvSaksbehandler, fastsatt, faktor, inntektskategori);
        settOverstyrtIForrigeGrunnlag(fastsatt);
        return tyDto;
    }

    private FastsettBGTilstøtendeYtelseDto lagTYDto(boolean nyAndel, boolean lagtTilAvSaksbehandler, Integer fastsatt, Double faktor, Inntektskategori inntektskategori) {
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        return new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));
    }

    private void settOverstyrtIForrigeGrunnlag(Integer fastsatt) {
        Beregningsgrunnlag opprinneligBg = beregningsgrunnlagRepository.hentAggregat(behandling);
        Beregningsgrunnlag eksisterendeGrunnlag = opprinneligBg.dypKopi();
        eksisterendeGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode ->
            periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
                BeregningsgrunnlagPrStatusOgAndel.builder(andel).medOverstyrtPrÅr(BigDecimal.valueOf(fastsatt))));
        beregningsgrunnlagRepository.lagre(behandling, eksisterendeGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        beregningsgrunnlagRepository.lagre(behandling, opprinneligBg, BeregningsgrunnlagTilstand.OPPRETTET);
    }

    private void utførOgReåpneAksjonspunkt() {
        aksjonspunktRepository.setTilUtført(aksjonspunkt, BEGRUNNELSE);
        aksjonspunktRepository.setReåpnet(aksjonspunkt);
    }

    private void lagBeregningsgrunnlag() {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder()
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT)
            .medGrunnbeløp(GRUNNBELØP)
            .medRedusertGrunnbeløp(GRUNNBELØP)
            .medSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT).build();
        BeregningsgrunnlagPeriode periode1 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT, SKJÆRINGSTIDSPUNKT.plusMonths(2).minusDays(1))
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(virksomheten).medArbforholdRef(ARB_ID))
            .medAndelsnr(ANDELSNR)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode1);
        BeregningsgrunnlagPeriode periode2 = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(SKJÆRINGSTIDSPUNKT.plusMonths(2), null)
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(virksomheten).medArbforholdRef(ARB_ID))
            .medAndelsnr(ANDELSNR)
            .medLagtTilAvSaksbehandler(false)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode2);

        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.OPPRETTET);
    }

}
