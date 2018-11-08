package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAndeltype;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.beregningsgrunnlag.adapter.util.BeregningArbeidsgiverTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsgiverHistorikkinnslagTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.impl.ArbeidsgiverHistorikkinnslagTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsattBeløpTilstøtendeYtelseAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FastsettBGTilstøtendeYtelseDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.RedigerbarAndelDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapterImpl;
import no.nav.foreldrepenger.web.app.tjenester.historikk.dto.HistorikkInnslagKonverter;

public class FastsettBGTilstøtendeYtelseOppdatererTest {

    private static final AktørId AKTØR_ID = new AktørId("210195");
    private static final String ARB_ID = "123124";
    private static final String ORGNR = "7887897435973";
    private static final Long ANDELSNR = 1L;
    private final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private final Beløp GRUNNBELØP = new Beløp(600000);


    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private HistorikkTjenesteAdapter historikkAdapter = new HistorikkTjenesteAdapterImpl(
        new HistorikkRepositoryImpl(repositoryRule.getEntityManager()), new HistorikkInnslagKonverter(
        repositoryProvider.getKodeverkRepository(), repositoryProvider.getAksjonspunktRepository()));
    ArbeidsgiverHistorikkinnslagTjeneste arbeidsgiverHistorikkinnslagTjeneste = new ArbeidsgiverHistorikkinnslagTjenesteImpl(null);
    private FastsettBGTilstøtendeYtelseOppdaterer fastsettBGTilstøtendeYtelseOppdaterer = new FastsettBGTilstøtendeYtelseOppdaterer(repositoryProvider, historikkAdapter, arbeidsgiverHistorikkinnslagTjeneste);
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    public ScenarioMorSøkerForeldrepenger scenario;
    public Behandling behandling;
    private Arbeidsgiver virksomheten;

    @Before
    public void setup() {
        this.scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        this.behandling = scenario.lagre(repositoryProvider);
        BeregningArbeidsgiverTestUtil virksomhetTestUtil = new BeregningArbeidsgiverTestUtil(repositoryProvider.getVirksomhetRepository());
        this.virksomheten = virksomhetTestUtil.forArbeidsgiverVirksomhet(ORGNR);
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



    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_ved_første_utførelse_av_aksjonspunkt() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));


        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert1 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert1.getInntektskategori()).isEqualTo(inntektskategori);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert2 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert2.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert2.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert2.getInntektskategori()).isEqualTo(inntektskategori);
        String andelsinfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(oppdatert2);

        assertHistorikkinnslagFordeling(fastsatt, null, andelsinfo);
    }


    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_fastsatt_lik_overstyrt_i_forrige_utførelse_av_aksonspunkt() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        Beregningsgrunnlag eksisterendeGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        eksisterendeGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
            BeregningsgrunnlagPrStatusOgAndel.builder(andel).medOverstyrtPrÅr(BigDecimal.valueOf(fastsatt))));
        beregningsgrunnlagRepository.lagre(behandling, eksisterendeGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET)
            .get().getBeregningsgrunnlag().dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert1 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert1.getInntektskategori()).isEqualTo(inntektskategori);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert2 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert2.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert2.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert2.getInntektskategori()).isEqualTo(inntektskategori);

        assertHistorikkinnslagFordeling(fastsatt, fastsatt, "Beregningvirksomhet (7887897435973)");
    }

    @Test
    public void skal_ikkje_lage_historikkinnslagdel_for_andel_som_eksisterte_fra_før_i_grunnlag_med_overstyrt_i_forrige_utførelse_lik_fastsatt() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.ARBEIDSTAKER;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        Beregningsgrunnlag eksisterendeGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        eksisterendeGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode ->
            periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
            BeregningsgrunnlagPrStatusOgAndel.builder(andel).medOverstyrtPrÅr(BigDecimal.valueOf(fastsatt))));
        beregningsgrunnlagRepository.lagre(behandling, eksisterendeGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET)
            .get().getBeregningsgrunnlag().dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert1 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert1.getInntektskategori()).isEqualTo(inntektskategori);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert2 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert2.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert2.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert2.getInntektskategori()).isEqualTo(inntektskategori);

        assertHistorikkinnslagFordeling(fastsatt, fastsatt, "Beregningvirksomhet (7887897435973)");
    }

    @Test
    public void skal_sette_verdier_på_andel_som_eksisterte_fra_før_i_grunnlag_med_overstyrt_verdi_ved_forrige_utførelse_av_aksjonspunkt_ulik_fastsatt() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = false;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        Beregningsgrunnlag eksisterendeGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        eksisterendeGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> periode.getBeregningsgrunnlagPrStatusOgAndelList().forEach(andel ->
            BeregningsgrunnlagPrStatusOgAndel.builder(andel).medOverstyrtPrÅr(BigDecimal.valueOf(fastsatt*2))));
        beregningsgrunnlagRepository.lagre(behandling, eksisterendeGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET)
            .get().getBeregningsgrunnlag().dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert1 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert1.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert1.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert1.getInntektskategori()).isEqualTo(inntektskategori);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(1);
        BeregningsgrunnlagPrStatusOgAndel oppdatert2 = bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().get(0);
        assertThat(oppdatert2.getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(oppdatert2.getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(oppdatert2.getInntektskategori()).isEqualTo(inntektskategori);

        String andelsinfo = arbeidsgiverHistorikkinnslagTjeneste.lagHistorikkinnslagTekstForBeregningsgrunnlag(oppdatert2);

        assertHistorikkinnslagFordeling(fastsatt, fastsatt*2, andelsinfo);
    }

    private void assertHistorikkinnslagFordeling(Integer fastsatt, Integer overstyrt, String andelsInfo) {
        List<HistorikkinnslagDel> deler = historikkAdapter.tekstBuilder().getHistorikkinnslagDeler();
        List<HistorikkinnslagDel> andelHistorikkinnslag = deler.stream().filter(del ->
            del != null &&
                del.getTema().isPresent() &&
                andelsInfo.equals(del.getTema().get().getNavnVerdi()))
            .collect(Collectors.toList());
        Optional<HistorikkinnslagDel> fordelingInnslag = andelHistorikkinnslag.stream().filter(del -> del.getEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_ANDEL).isPresent()).findFirst();
        if (overstyrt != null && overstyrt.equals(fastsatt)) {
            assertThat(fordelingInnslag.isPresent()).isFalse();
        } else if (overstyrt == null) {
            assertThat(fordelingInnslag.isPresent()).isTrue();
            assertThat(fordelingInnslag.get().getEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_ANDEL).get().getFraVerdi()).isNull();
            assertThat(fordelingInnslag.get().getEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_ANDEL).get().getTilVerdi()).isEqualTo(fastsatt.toString());
        } else {
            assertThat(fordelingInnslag.isPresent()).isTrue();
            assertThat(fordelingInnslag.get().getEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_ANDEL).get().getFraVerdi()).isEqualTo(overstyrt.toString());
            assertThat(fordelingInnslag.get().getEndretFelt(HistorikkEndretFeltType.FORDELING_FOR_ANDEL).get().getTilVerdi()).isEqualTo(fastsatt.toString());
        }
    }


    @Test
    public void skal_sette_verdier_på_ny_andel() {
        // Arrange
        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil1.size()).isEqualTo(1);
        assertThat(lagtTil1.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil1.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler1.size()).isEqualTo(1);


        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil2.size()).isEqualTo(1);
        assertThat(lagtTil2.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler2.size()).isEqualTo(1);

    }



    @Test
    public void skal_sette_verdier_på_andel_lagt_til_av_saksbehandler_ved_tilbakehopp_til_KOFAKBER() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;

        Beregningsgrunnlag førsteGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        Long andelsnr = 2133L;
        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(ARB_ID))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .build(periode));
        beregningsgrunnlagRepository.lagre(behandling, førsteGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);


        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, andelsnr, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto( Collections.singletonList(tyAndel));

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgEntitet = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET);
        Beregningsgrunnlag bg = bgEntitet.get().getBeregningsgrunnlag().dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);


        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil1.size()).isEqualTo(1);
        assertThat(lagtTil1.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil1.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler1.size()).isEqualTo(1);


        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil2.size()).isEqualTo(1);
        assertThat(lagtTil2.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler2.size()).isEqualTo(1);
    }


    @Test
    public void skal_sette_verdier_på_andel_lagt_til_av_saksbehandler_ved_tilbakehopp_til_steg_før_KOFAKBER() {
        // Arrange
        Beregningsgrunnlag førsteGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        Long andelsnr = 2133L;
        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbeidsgiver(virksomheten).medArbforholdRef(ARB_ID))
            .medAndelsnr(andelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.SJØMANN)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medOverstyrtPrÅr(BigDecimal.valueOf(100000))
            .build(periode));
        beregningsgrunnlagRepository.lagre(behandling, førsteGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, ARB_ID, andelsnr, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        // Act
        Optional<BeregningsgrunnlagGrunnlagEntitet> bgEntitet = beregningsgrunnlagRepository.hentSisteBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET);
        Beregningsgrunnlag bg = bgEntitet.get().getBeregningsgrunnlag().dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);


        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil1.size()).isEqualTo(1);
        assertThat(lagtTil1.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil1.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler1.size()).isEqualTo(1);


        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil2.size()).isEqualTo(1);
        assertThat(lagtTil2.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler2.size()).isEqualTo(1);

        assertHistorikkinnslagFordeling(fastsatt, fastsatt, "Beregningvirksomhet (7887897435973)");
}


    @Test
    public void skal_sette_verdier_på_ny_andel_brukers_andel() {
        // Arrange
        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto(BeregningsgrunnlagAndeltype.BRUKERS_ANDEL.getKode(), nyAndel, null, null, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil1.size()).isEqualTo(1);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(lagtTil1.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil1.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler1.size()).isEqualTo(1);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil2.size()).isEqualTo(1);
        assertThat(lagtTil2.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.BRUKERS_ANDEL);
        assertThat(lagtTil2.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler2.size()).isEqualTo(1);

    }

    @Test
    public void skal_sette_verdier_på_ny_andel_frilans() {
        // Arrange
        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto(BeregningsgrunnlagAndeltype.FRILANS.getKode(), nyAndel, null, null, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil1.size()).isEqualTo(1);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(lagtTil1.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil1.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler1.size()).isEqualTo(1);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil2.size()).isEqualTo(1);
        assertThat(lagtTil2.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.FRILANSER);
        assertThat(lagtTil2.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler2.size()).isEqualTo(1);

    }


    @Test
    public void skal_sette_verdier_på_ny_andel_SN() {
        // Arrange
        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;
        Integer fastsatt = 100000;
        Double faktor = 0.8;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto(BeregningsgrunnlagAndeltype.EGEN_NÆRING.getKode(), nyAndel, null, null, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);
        FastsettBGTilstøtendeYtelseDto dto = new FastsettBGTilstøtendeYtelseDto(Collections.singletonList(tyAndel));

        // Act
        Beregningsgrunnlag bg = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        fastsettBGTilstøtendeYtelseOppdaterer.oppdater(dto, behandling, bg);

        // Assert
        assertThat(bg.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(bg.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil1.size()).isEqualTo(1);
        assertThat(lagtTil1.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(lagtTil1.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil1.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil1.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler1 = bg.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler1.size()).isEqualTo(1);

        assertThat(bg.getBeregningsgrunnlagPerioder().get(1).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(2);
        List<BeregningsgrunnlagPrStatusOgAndel> lagtTil2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(BeregningsgrunnlagPrStatusOgAndel::getLagtTilAvSaksbehandler).collect(Collectors.toList());
        assertThat(lagtTil2.size()).isEqualTo(1);
        assertThat(lagtTil2.get(0).getAktivitetStatus()).isEqualTo(AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE);
        assertThat(lagtTil2.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(lagtTil2.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(lagtTil2.get(0).getInntektskategori()).isEqualTo(inntektskategori);

        List<BeregningsgrunnlagPrStatusOgAndel> fastsattAvSaksbehandler2 = bg.getBeregningsgrunnlagPerioder().get(1)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> Boolean.TRUE.equals(a.getFastsattAvSaksbehandler())).collect(Collectors.toList());
        assertThat(fastsattAvSaksbehandler2.size()).isEqualTo(1);

    }

    @Test
    public void skal_kunne_finne_korrekt_andel_for_ny_arbeidsforhold_andel() {
        // Arrange
        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;

        long nyttAndelsnr = 2131L;

        Beregningsgrunnlag førsteGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(nyttAndelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode));

        Integer fastsatt = 100000;
        Double faktor = 0.5;
        Inntektskategori inntektskategori = Inntektskategori.FRILANSER;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("wdauhw", nyAndel, ARB_ID, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);

        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettBGTilstøtendeYtelseOppdaterer.getKorrektAndel(behandling, førsteGrunnlag.getBeregningsgrunnlagPerioder(), tyAndel);

        assertThat(korrektAndel).isEqualToComparingFieldByField(førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0).
            getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(andel -> andel.getAndelsnr().equals(ANDELSNR)).findFirst().get());

    }

    @Test
    public void skal_legge_til_og_sette_inntektskategori_og_fastsatt_beløp_for_ny_andel(){

        boolean nyAndel = true;
        boolean lagtTilAvSaksbehandler = true;

        long nyttAndelsnr = 3141L;

        Beregningsgrunnlag førsteGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();

        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(nyttAndelsnr)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(periode));

        Integer fastsatt = 200000;
        Double faktor = 0.9;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("wdauhw", nyAndel, null, ANDELSNR, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);

        fastsettBGTilstøtendeYtelseOppdaterer.settInntektskategoriOgFastsattBeløp(tyAndel, førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getAndelsnr().equals(ANDELSNR)).findFirst().get(), førsteGrunnlag.getBeregningsgrunnlagPerioder(), false);

        assertThat(førsteGrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);
        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList().stream().filter(a -> a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent() && a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get().getReferanse().equals(ARB_ID) &&
                a.getLagtTilAvSaksbehandler()
            ).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(1);
        assertThat(andelLagtTil.get(0).getInntektskategori()).isEqualTo(inntektskategori);
        assertThat(andelLagtTil.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(andelLagtTil.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(andelLagtTil.get(0).getFastsattAvSaksbehandler()).isTrue();
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isNull();
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(ANDELSNR);

    }

    @Test
    public void skal_legge_til_og_sette_inntektskategori_og_fastsatt_beløp_for_gammel_andel_ved_tilbakehopp_til_KOFAKBER(){

        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;

        long nyttAndelsnr = 3141L;
        long andelsnrLagtTil = 23131L;

        Beregningsgrunnlag førsteGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();

        førsteGrunnlag.getBeregningsgrunnlagPerioder().forEach(periode -> {
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAndelsnr(nyttAndelsnr)
                .medLagtTilAvSaksbehandler(true)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
                .build(periode);
        });

        Beregningsgrunnlag dummyGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        BeregningsgrunnlagPrStatusOgAndel korrektAndel = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(BGAndelArbeidsforhold.builder().medArbforholdRef(ARB_ID))
            .medAndelsnr(andelsnrLagtTil)
            .medBeregnetPrÅr(BigDecimal.valueOf(50000))
            .medLagtTilAvSaksbehandler(true)
            .medFastsattAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER_UTEN_FERIEPENGER)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(dummyGrunnlag.getBeregningsgrunnlagPerioder().get(0));

        Integer fastsatt = 200000;
        Double faktor = 0.9;
        Inntektskategori inntektskategori = Inntektskategori.SJØMANN;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("wdauhw", nyAndel, ARB_ID, andelsnrLagtTil, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);

        fastsettBGTilstøtendeYtelseOppdaterer.settInntektskategoriOgFastsattBeløp(tyAndel, korrektAndel, førsteGrunnlag.getBeregningsgrunnlagPerioder(), false);

        assertThat(førsteGrunnlag.getBeregningsgrunnlagPerioder().size()).isEqualTo(2);
        assertThat(førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0).getBeregningsgrunnlagPrStatusOgAndelList().size()).isEqualTo(3);
        List<BeregningsgrunnlagPrStatusOgAndel> andelLagtTil = førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0)
            .getBeregningsgrunnlagPrStatusOgAndelList()
            .stream()
            .filter(a -> a.getBgAndelArbeidsforhold()
                .flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).isPresent() && a.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get().getReferanse().equals(ARB_ID) && a.getLagtTilAvSaksbehandler()
            ).collect(Collectors.toList());

        assertThat(andelLagtTil.size()).isEqualTo(1);
        assertThat(andelLagtTil.get(0).getInntektskategori()).isEqualTo(inntektskategori);
        assertThat(andelLagtTil.get(0).getBeregnetPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt));
        assertThat(andelLagtTil.get(0).getOverstyrtPrÅr()).isEqualByComparingTo(BigDecimal.valueOf(fastsatt*faktor));
        assertThat(andelLagtTil.get(0).getFastsattAvSaksbehandler()).isTrue();
        assertThat(andelLagtTil.get(0).getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getRefusjonskravPrÅr).orElse(null)).isNull();
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(ANDELSNR);
        assertThat(andelLagtTil.get(0).getAndelsnr()).isNotEqualTo(andelsnrLagtTil);

    }

    @Test
    public void skal_kunne_finne_korrekt_andel_for_gammel_brukers_andel_ved_tilbakehopp_til_KOFAKBER() {
        // Arrange
        boolean nyAndel = false;
        boolean lagtTilAvSaksbehandler = true;

        Beregningsgrunnlag førsteGrunnlag = beregningsgrunnlagRepository.hentAggregat(behandling).dypKopi();
        Long andelsnrForBrukerandel = 2133L;
        BeregningsgrunnlagPrStatusOgAndel andelIFørstePeriode = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(andelsnrForBrukerandel)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(førsteGrunnlag.getBeregningsgrunnlagPerioder().get(0));
        BeregningsgrunnlagPrStatusOgAndel andelIAndrePeriode = BeregningsgrunnlagPrStatusOgAndel.builder()
            .medAndelsnr(53534L)
            .medLagtTilAvSaksbehandler(true)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medAktivitetStatus(AktivitetStatus.BRUKERS_ANDEL)
            .build(førsteGrunnlag.getBeregningsgrunnlagPerioder().get(1));
        beregningsgrunnlagRepository.lagre(behandling, førsteGrunnlag, BeregningsgrunnlagTilstand.KOFAKBER_UT);

        Integer fastsatt = 100000;
        Double faktor = 0.5;
        Inntektskategori inntektskategori = Inntektskategori.FRILANSER;
        RedigerbarAndelDto andelDto = new RedigerbarAndelDto("Andelen", nyAndel, null, andelsnrForBrukerandel, lagtTilAvSaksbehandler);
        FastsattBeløpTilstøtendeYtelseAndelDto tyAndel = new FastsattBeløpTilstøtendeYtelseAndelDto(andelDto, fastsatt, null, inntektskategori, faktor);

        BeregningsgrunnlagPrStatusOgAndel korrektAndel = fastsettBGTilstøtendeYtelseOppdaterer.getKorrektAndel(behandling, førsteGrunnlag.getBeregningsgrunnlagPerioder(), tyAndel);

        assertThat(andelIFørstePeriode).isEqualToComparingFieldByField(korrektAndel);
        assertThat(andelIAndrePeriode.getAndelsnr()).isNotEqualTo(korrektAndel.getAndelsnr());
    }

}
