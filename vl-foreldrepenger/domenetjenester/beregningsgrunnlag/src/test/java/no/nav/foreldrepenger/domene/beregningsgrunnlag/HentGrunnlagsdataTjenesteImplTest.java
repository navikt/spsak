package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingFPTjenesteImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BGAndelArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseStørrelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.InntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningInntektArbeidYtelseTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.HentGrunnlagsdataTjenesteImpl;
import no.nav.foreldrepenger.domene.beregningsgrunnlag.verdikjede.VerdikjedeTestHjelper;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class HentGrunnlagsdataTjenesteImplTest {

    private static LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.of(2018, 7, 1);

    @Rule
    public final RepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider, Period.of(0, 10, 0));
    private AksjonspunktutlederForVurderOpptjening apOpptjening = new AksjonspunktutlederForVurderOpptjening(repositoryProvider, skjæringstidspunktTjeneste);
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste = new InntektArbeidYtelseTjenesteImpl(repositoryProvider, null, null, null, skjæringstidspunktTjeneste, apOpptjening);
    private OpptjeningsperioderTjeneste periodeTjeneste = mock(OpptjeningsperioderTjeneste.class);
    private OpptjeningInntektArbeidYtelseTjeneste opptjeningTjeneste = new OpptjeningInntektArbeidYtelseTjenesteImpl(inntektArbeidYtelseTjeneste, repositoryProvider, periodeTjeneste);
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
    private OpptjeningRepository opptjeningRepository = repositoryProvider.getOpptjeningRepository();
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
    private VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
    private MottatteDokumentRepository mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
    private HentGrunnlagsdataTjenesteImpl hentGrunnlagsdataTjeneste;

    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider,
        mock(BehandlingModellRepository.class), null);
    private RevurderingFPTjenesteImpl revurderingTjeneste = new RevurderingFPTjenesteImpl(repositoryProvider, behandlingskontrollTjeneste, mock(HistorikkRepository.class), null);

    private Behandling behandling;
    private Behandling forrigeBehandling;
    private Virksomhet virksomhet;
    private String arbeidsforholdId = "2314234234";

    @Before
    public void setUp() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        forrigeBehandling = scenario.lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(forrigeBehandling);
        virksomhet = hentVirksomhet();
        hentGrunnlagsdataTjeneste = new HentGrunnlagsdataTjenesteImpl(repositoryProvider, opptjeningTjeneste, inntektArbeidYtelseTjeneste, null);
    }

    @Test
    public void skalHenteInnNyeDataNårIkkeGjeldendeBGFinnes() {
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assertr
        assertThat(skalHenteNyesteGrunnlag).isTrue();
    }

    @Test
    public void skalHenteInnNyeDataNårEndringIOpptjeningsAktiviteter() {
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, true);
        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assertr
        assertThat(skalHenteNyesteGrunnlag).isTrue();
    }

    @Test
    public void skalHenteInnNyeDataNårSkjæringstidspunktetErEndret() {
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT.plusDays(5), false, null, arbeidsforholdId, false);
        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.FASTSATT);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg2, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);
        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isTrue();
    }

    @Test
    public void skalHenteInnNyeDataNårManuellRevurderingOgÅrsakErOpplysningerOmInntekt() {
        //Arrange
        behandling = revurderingTjeneste.opprettManuellRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_OPPLYSNINGER_OM_INNTEKT);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg2, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);

        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isTrue();
    }

    @Test
    public void skalHenteInnNyeDataNårEndringerIYtelseIkkeBesteBeregningOgEndringerGjelderSisteYtelsePeriode() {
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg2, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);
        InntektArbeidYtelseAggregatBuilder iay1 = lagIAY(false, false, false);
        InntektArbeidYtelseAggregatBuilder iay2 = lagIAY(true, true, false);
        inntektArbeidYtelseTjeneste.lagre(forrigeBehandling, iay1);
        inntektArbeidYtelseTjeneste.lagre(behandling, iay2);

        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isTrue();
    }

    @Test
    public void skalIkkeHenteInnNyeDataNårEndringerIYtelseIkkeBesteBeregningOgSisteYtelsePeriodeHarFPfraFørstegangsbehandlingen() { //Fikset bug
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg2, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);
        InntektArbeidYtelseAggregatBuilder iay1 = lagIAY(false, false, true);
        InntektArbeidYtelseAggregatBuilder iay2 = lagIAY(true, false, false);
        inntektArbeidYtelseTjeneste.lagre(forrigeBehandling, iay1);
        inntektArbeidYtelseTjeneste.lagre(behandling, iay2);

        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isFalse();
    }

    @Test
    public void skalHenteInnNyeDataNårEndringerIYtelseOgBesteBeregningOgEndringerGjelderSiste10Måneder() {
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, true, null, arbeidsforholdId, false);
        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, true, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg2, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);
        InntektArbeidYtelseAggregatBuilder iay1 = lagIAY(false, false, false);
        InntektArbeidYtelseAggregatBuilder iay2 = lagIAY(true, false, false);
        inntektArbeidYtelseTjeneste.lagre(forrigeBehandling, iay1);
        inntektArbeidYtelseTjeneste.lagre(behandling, iay2);

        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isTrue();
    }

    @Test
    public void skalReturnereFalse() {
        //Arrange
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_HENDELSE_FØDSEL);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg2, BeregningsgrunnlagTilstand.FASTSATT);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);
        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isFalse();
    }

    @Test
    public void skalReturnereFalseNårIngenEndringIInntektOgAndreKravIkkeOppfylt() {
        //Arrange
        int inntekt1 = 20000;
        int inntekt2 = 20000;
        int originalInntekt1 = 20000;
        int originalInntekt2 = 20000;
        String arbeidsforholdId1 = "1234566";
        String arbeidsforholdId2 = "1234568";
        lagInntektsmelding(forrigeBehandling, originalInntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(forrigeBehandling, originalInntekt2, 0, arbeidsforholdId2);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmelding(behandling, inntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(behandling, inntekt2, 0, arbeidsforholdId2);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);


        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(inntekt1 * 12), BigDecimal.valueOf(inntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(originalInntekt1 * 12), BigDecimal.valueOf(originalInntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);
        InntektArbeidYtelseAggregatBuilder iay1 = lagIAY(false, false, false);
        InntektArbeidYtelseAggregatBuilder iay2 = lagIAY(false, false, false);
        inntektArbeidYtelseTjeneste.lagre(forrigeBehandling, iay1);
        inntektArbeidYtelseTjeneste.lagre(behandling, iay2);

        //Act
        boolean skalHenteNyesteGrunnlag = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        //Assert
        assertThat(skalHenteNyesteGrunnlag).isFalse();
    }

    @Test
    public void skalBrukeNyttIAYGrunnlagNårEndretInntekt() {
        // Arrange
        int inntekt1 = 10000;
        int inntekt2 = 10000;
        int originalInntekt1 = 20000;
        int originalInntekt2 = 20000;
        int refusjon1 = 10000;
        int refusjon2 = 10000;
        String arbeidsforholdId1 = "1234566";
        String arbeidsforholdId2 = "1234568";
        lagInntektsmelding(forrigeBehandling, originalInntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(forrigeBehandling, originalInntekt2, 0, arbeidsforholdId2);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmelding(behandling, inntekt1, refusjon1, arbeidsforholdId1);
        lagInntektsmelding(behandling, inntekt2, refusjon2, arbeidsforholdId2);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);


        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(inntekt1 * 12), BigDecimal.valueOf(inntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(originalInntekt1 * 12), BigDecimal.valueOf(originalInntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);
        //Act
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);

        //Assert
        assertThat(iayGrunnlagOpt.isPresent()).isTrue();

        InntektArbeidYtelseGrunnlag grunnlag = iayGrunnlagOpt.get();
        Optional<InntektsmeldingAggregat> imOpt = grunnlag.getInntektsmeldinger();
        assertThat(imOpt.isPresent()).isTrue();
        List<Inntektsmelding> inntektsmeldinger = imOpt.get().getInntektsmeldinger();
        assertThat(inntektsmeldinger).hasSize(2);
        assertThat(inntektsmeldinger.get(0).getRefusjonBeløpPerMnd().getVerdi()).isEqualByComparingTo(BigDecimal.valueOf(refusjon1));
    }


    @Test
    public void skalGiEndringIInntektForSeparateInntektsmeldingForOriginalBehandlingSammenlignetMedFellesForRevurdering() {

        // Arrange
        int inntekt = 40000;
        int originalInntekt1 = 20000;
        int originalInntekt2 = 20000;
        String arbeidsforholdId1 = "1234566";
        String arbeidsforholdId2 = "1234568";
        lagInntektsmelding(forrigeBehandling, originalInntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(forrigeBehandling, originalInntekt2, 0, arbeidsforholdId2);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmeldingIkkjeForSpesifiktArbeidsforhold(behandling, inntekt, 0);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);


        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, BigDecimal.valueOf(inntekt * 12), null, false);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(originalInntekt1 * 12), BigDecimal.valueOf(originalInntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);

        // Act
        boolean endring = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        // Assert
        assertThat(endring).isTrue();
    }


    @Test
    public void skalGiEndringIInntektForSeparateInntektsmeldingForOriginalBehandlingSammenlignetMedSeparateForRevurdering() {

        // Arrange
        int inntekt1 = 10000;
        int inntekt2 = 10000;
        int originalInntekt1 = 20000;
        int originalInntekt2 = 20000;
        String arbeidsforholdId1 = "1234566";
        String arbeidsforholdId2 = "1234568";
        lagInntektsmelding(forrigeBehandling, originalInntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(forrigeBehandling, originalInntekt2, 0, arbeidsforholdId2);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmelding(behandling, inntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(behandling, inntekt2, 0, arbeidsforholdId2);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);


        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(inntekt1 * 12), BigDecimal.valueOf(inntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(originalInntekt1 * 12), BigDecimal.valueOf(originalInntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);

        // Act
        boolean endring = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        // Assert
        assertThat(endring).isTrue();
    }

    @Test
    public void skalIkkjeGiEndringIInntektForSeparateInntektsmeldingForOriginalBehandlingSammenlignetMedSeparateForRevurdering() {

        // Arrange
        int inntekt1 = 20000;
        int inntekt2 = 20000;
        int originalInntekt1 = 20000;
        int originalInntekt2 = 20000;
        String arbeidsforholdId1 = "1234566";
        String arbeidsforholdId2 = "1234568";
        lagInntektsmelding(forrigeBehandling, originalInntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(forrigeBehandling, originalInntekt2, 0, arbeidsforholdId2);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmelding(behandling, inntekt1, 0, arbeidsforholdId1);
        lagInntektsmelding(behandling, inntekt2, 0, arbeidsforholdId2);
        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);


        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(inntekt1 * 12), BigDecimal.valueOf(inntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false,
            Arrays.asList(BigDecimal.valueOf(originalInntekt1 * 12), BigDecimal.valueOf(originalInntekt2 * 12)), Arrays.asList(arbeidsforholdId1, arbeidsforholdId2));
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);

        // Act
        boolean endring = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        // Assert
        assertThat(endring).isFalse();
    }


    @Test
    public void skalIkkjeGiEndringIInntektForFellesInntektsmeldingForOriginalBehandlingSammenlignetMedFellesForRevurdering() {

        // Arrange
        int inntekt = 20000;
        int originalInntekt = 20000;


        lagInntektsmeldingIkkjeForSpesifiktArbeidsforhold(forrigeBehandling, originalInntekt, 0);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmeldingIkkjeForSpesifiktArbeidsforhold(behandling, inntekt, 0);

        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);

        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, BigDecimal.valueOf(inntekt * 12), null, false);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, BigDecimal.valueOf(originalInntekt * 12), null, false);
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);

        // Act
        boolean endring = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        // Assert
        assertThat(endring).isFalse();
    }


    @Test
    public void skalGiEndringIInntektForFellesInntektsmeldingForOriginalBehandlingSammenlignetMedFellesForRevurdering() {

        // Arrange
        int inntekt = 10000;
        int originalInntekt = 20000;


        lagInntektsmeldingIkkjeForSpesifiktArbeidsforhold(forrigeBehandling, originalInntekt, 0);
        behandling = revurderingTjeneste.opprettAutomatiskRevurdering(forrigeBehandling.getFagsak(), BehandlingÅrsakType.RE_ENDRET_INNTEKTSMELDING);
        lagInntektsmeldingIkkjeForSpesifiktArbeidsforhold(behandling, inntekt, 0);

        opprettOpptjening(behandling, false);
        opprettOpptjening(forrigeBehandling, false);

        Beregningsgrunnlag bg2 = opprettBeregninggrunnlag(false, SKJÆRINGSTIDSPUNKT, false, BigDecimal.valueOf(inntekt * 12), null, false);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, BigDecimal.valueOf(originalInntekt * 12), null, false);
        beregningsgrunnlagRepository.lagre(behandling, bg2, BeregningsgrunnlagTilstand.OPPRETTET);
        beregningsgrunnlagRepository.lagre(forrigeBehandling, bg, BeregningsgrunnlagTilstand.FASTSATT);

        // Act
        boolean endring = hentGrunnlagsdataTjeneste.vurderOmNyesteGrunnlagsdataSkalHentes(behandling);

        // Assert
        assertThat(endring).isTrue();
    }

    @Test
    public void skalGiBesteberegningNårOvergangFraSykepengerMedKat06eller23() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        opprettOpptjening(behandling, false);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, true, null, arbeidsforholdId, true);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);

        boolean resultat = hentGrunnlagsdataTjeneste.brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling);

        assertThat(resultat).isTrue();
    }

    @Test
    public void skalIkkeGiBesteberegningNårOvergangFraSykepengerMenIkkeDagpenger() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        opprettOpptjening(behandling, false);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, false, null, arbeidsforholdId, true);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);

        boolean resultat = hentGrunnlagsdataTjeneste.brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling);

        assertThat(resultat).isFalse();
    }

    @Test
    public void skalIkkeGiBesteberegningNårOvergangFraSykepengerMenIkkeEkstraOpptjeningsaktivitet() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        opprettOpptjening(behandling, OpptjeningAktivitetType.DAGPENGER, OpptjeningAktivitetType.SYKEPENGER);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, true, null, arbeidsforholdId, true);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);

        boolean resultat = hentGrunnlagsdataTjeneste.brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling);

        assertThat(resultat).isFalse();
    }

    @Test
    public void skalIkkeGiBesteberegningNårDagpengerMenIkkeEkstraOpptjeningsaktivitet() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagre(repositoryProvider);
        opprettOpptjening(behandling, OpptjeningAktivitetType.DAGPENGER);
        Beregningsgrunnlag bg = opprettBeregninggrunnlag(true, SKJÆRINGSTIDSPUNKT, true, null, arbeidsforholdId, false);
        beregningsgrunnlagRepository.lagre(behandling, bg, BeregningsgrunnlagTilstand.OPPRETTET);

        boolean resultat = hentGrunnlagsdataTjeneste.brukerOmfattesAvBesteBeregningsRegelForFødendeKvinne(behandling);

        assertThat(resultat).isFalse();
    }


    private void opprettOpptjening(Behandling behandling, boolean ekstraAktivitet) {
        if (ekstraAktivitet) {
            opprettOpptjening(behandling, OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetType.SYKEPENGER);
        } else {
            opprettOpptjening(behandling, OpptjeningAktivitetType.ARBEID);
        }
    }

    private void opprettOpptjening(Behandling behandling, OpptjeningAktivitetType... opptjeningAktivitetTypes) {
        List<OpptjeningAktivitet> aktiviteter = new ArrayList<>();
        for (OpptjeningAktivitetType aktivitet : Arrays.asList(opptjeningAktivitetTypes)) {
            aktiviteter.add(VerdikjedeTestHjelper.leggTilOpptjening("111", aktivitet));
        }
        opptjeningRepository.lagreOpptjeningsperiode(behandling, SKJÆRINGSTIDSPUNKT.minusYears(1), SKJÆRINGSTIDSPUNKT.plusYears(10));
        opptjeningRepository.lagreOpptjeningResultat(behandling, Period.ofDays(100), aktiviteter);
    }

    private Beregningsgrunnlag opprettBeregninggrunnlag(boolean gjeldende, LocalDate skjæringstidspunkt, boolean harDagpenger, List<BigDecimal> andelBeløpPrÅr, List<String> arbeidsforholdId) {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(skjæringstidspunkt).medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
            .medDekningsgrad(100L).medGrunnbeløp(BigDecimal.valueOf(93000)).medRedusertGrunnbeløp(BigDecimal.valueOf(93000)).build();
        if (harDagpenger) {
            BeregningsgrunnlagAktivitetStatus.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER).build(beregningsgrunnlag);
        }
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(skjæringstidspunkt.minusYears(1), null);
        if (gjeldende) {
            periodeBuilder.medRedusertPrÅr(BigDecimal.valueOf(50000));
        }
        BeregningsgrunnlagPeriode periode = periodeBuilder.build(beregningsgrunnlag);
        int i = 0;
        for (String arbId : arbeidsforholdId) {
            BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
                .builder()
                .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet))
                .medArbforholdRef(arbId);
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medBGAndelArbeidsforhold(bga)
                .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medBeregnetPrÅr(andelBeløpPrÅr.get(i))
                .build(periode);
            i++;
        }
        if (harDagpenger) {
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                .medInntektskategori(Inntektskategori.DAGPENGER)
                .medBeregnetPrÅr(BigDecimal.valueOf(100000))
                .build(periode);
        }
        return beregningsgrunnlag;
    }

    private Beregningsgrunnlag opprettBeregninggrunnlag(boolean gjeldende, LocalDate skjæringstidspunkt, boolean harDagpenger, BigDecimal andelBeløpPrÅr, String arbeidsforholdId, boolean fraSykepenger) {
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(skjæringstidspunkt).medOpprinneligSkjæringstidspunkt(skjæringstidspunkt)
            .medDekningsgrad(100L).medGrunnbeløp(BigDecimal.valueOf(93000)).medRedusertGrunnbeløp(BigDecimal.valueOf(93000)).build();
        if (fraSykepenger) {
            BeregningsgrunnlagAktivitetStatus.builder().medAktivitetStatus(AktivitetStatus.TILSTØTENDE_YTELSE).build(beregningsgrunnlag);
        } else if (harDagpenger) {
            BeregningsgrunnlagAktivitetStatus.builder().medAktivitetStatus(AktivitetStatus.DAGPENGER).build(beregningsgrunnlag);
        }
        BeregningsgrunnlagPeriode.Builder periodeBuilder = BeregningsgrunnlagPeriode.builder().medBeregningsgrunnlagPeriode(skjæringstidspunkt.minusYears(1), null);
        if (gjeldende) {
            periodeBuilder.medRedusertPrÅr(BigDecimal.valueOf(50000));
        }
        BeregningsgrunnlagPeriode periode = periodeBuilder.build(beregningsgrunnlag);
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold
            .builder()
            .medArbforholdRef(arbeidsforholdId)
            .medArbeidsgiver(Arbeidsgiver.virksomhet(virksomhet));
        BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
            .medBeregnetPrÅr(andelBeløpPrÅr)
            .build(periode);
        if (harDagpenger) {
            BeregningsgrunnlagPrStatusOgAndel.builder()
                .medAktivitetStatus(AktivitetStatus.DAGPENGER)
                .medInntektskategori(Inntektskategori.DAGPENGER)
                .medBeregnetPrÅr(BigDecimal.valueOf(100000))
                .medYtelse(fraSykepenger ? RelatertYtelseType.SYKEPENGER : null)
                .build(periode);
        }
        return beregningsgrunnlag;
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        FagsakRepository fagsakRepository = repositoryProvider.getFagsakRepository();
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }

    private InntektArbeidYtelseAggregatBuilder lagIAY(boolean endretYtelse, boolean endretYtelsePeriode, boolean fpVedtakFraFørstegangsbehandling) {
        InntektArbeidYtelseAggregatBuilder aggBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);
        lagAktørYtelse(aggBuilder, endretYtelse, endretYtelsePeriode, fpVedtakFraFørstegangsbehandling);
        return aggBuilder;
    }

    private void lagAktørYtelse(InntektArbeidYtelseAggregatBuilder aggBuilder, boolean endretYtelse, boolean endretYtelsesPeriode, boolean fpVedtakFraFørstegangsbehandling) {
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder aktørYtelseBuilder = aggBuilder.getAktørYtelseBuilder(behandling.getAktørId());
        Saksnummer sakId = new Saksnummer("1200095");
        YtelseBuilder ytelseBuilder = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.DAGPENGER, sakId);
        lagYtelse(ytelseBuilder, sakId, endretYtelsesPeriode, endretYtelse, false);
        aktørYtelseBuilder.leggTilYtelse(ytelseBuilder);
        if (fpVedtakFraFørstegangsbehandling) {
            Saksnummer saksnummer = forrigeBehandling.getFagsak().getSaksnummer();
            YtelseBuilder ytelseBuilderFPVedtak = aktørYtelseBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.FORELDREPENGER, saksnummer);
            lagYtelse(ytelseBuilderFPVedtak, saksnummer, false, false, true);
            aktørYtelseBuilder.leggTilYtelse(ytelseBuilderFPVedtak);
        }
        aggBuilder.leggTilAktørYtelse(aktørYtelseBuilder);

    }

    private void lagYtelse(YtelseBuilder ytelseBuilder, Saksnummer sakId, boolean endretYtelsesPeriode, boolean endretYtelse, boolean fpVedtak) {
        ytelseBuilder.medKilde(fpVedtak ? Fagsystem.FPSAK : Fagsystem.ARENA)
            .medYtelseType(fpVedtak ? RelatertYtelseType.FORELDREPENGER : RelatertYtelseType.DAGPENGER)
            .medBehandlingsTema(TemaUnderkategori.UDEFINERT)
            .medStatus(RelatertYtelseTilstand.AVSLUTTET)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(endretYtelsesPeriode ? 4 : 3), SKJÆRINGSTIDSPUNKT.minusMonths(endretYtelsesPeriode ? 2 : 1)))
            .medSaksnummer(sakId)
            .medYtelseGrunnlag(
                ytelseBuilder.getGrunnlagBuilder()
                    .medYtelseStørrelse(YtelseStørrelseBuilder.ny().medBeløp(BigDecimal.valueOf(endretYtelse ? 1000 : 1500)).build())
                    .build())
            .medYtelseAnvist(ytelseBuilder.getAnvistBuilder()
                .medAnvistPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(SKJÆRINGSTIDSPUNKT.minusMonths(1), SKJÆRINGSTIDSPUNKT.minusWeeks(2)))
                .medDagsats(BigDecimal.valueOf(endretYtelse ? 1000 : 1500))
                .medUtbetalingsgradProsent(BigDecimal.valueOf(200))
                .build());
    }

    private String lagInntektsmelding(Behandling beh, int beløp, int refusjon, String arbeidsforholdId) {
        MottattDokument mottattDokument = lagMottattDokument(beh, SKJÆRINGSTIDSPUNKT.minusMonths(1));
        InntektsmeldingBuilder inntektsmeldingBuilder = getInntektsmeldingBuilder()
            .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
            .medBeløp(BigDecimal.valueOf(beløp))
            .medRefusjon(BigDecimal.valueOf(refusjon))
            .medMottattDokument(mottattDokument)
            .medVirksomhet(virksomhet)
            .medArbeidsforholdId(arbeidsforholdId);
        inntektArbeidYtelseRepository.lagre(beh, inntektsmeldingBuilder.build());

        return inntektArbeidYtelseRepository.hentInntektsMeldingFor(mottattDokument).get().getArbeidsforholdRef().getReferanse();//NOSONAR
    }

    private InntektsmeldingBuilder getInntektsmeldingBuilder() {
        return InntektsmeldingBuilder.builder().medInnsendingstidspunkt(LocalDateTime.now());
    }

    private String lagInntektsmeldingIkkjeForSpesifiktArbeidsforhold(Behandling beh, int beløp, int refusjon) {
        MottattDokument mottattDokument = lagMottattDokument(beh, SKJÆRINGSTIDSPUNKT.minusMonths(1));
        InntektsmeldingBuilder inntektsmeldingBuilder = getInntektsmeldingBuilder()
            .medStartDatoPermisjon(SKJÆRINGSTIDSPUNKT)
            .medBeløp(BigDecimal.valueOf(beløp))
            .medRefusjon(BigDecimal.valueOf(refusjon))
            .medMottattDokument(mottattDokument)
            .medVirksomhet(virksomhet);
        inntektArbeidYtelseRepository.lagre(beh, inntektsmeldingBuilder.build());

        return inntektArbeidYtelseRepository.hentInntektsMeldingFor(mottattDokument).get().getArbeidsforholdRef().getReferanse();//NOSONAR
    }

    private VirksomhetEntitet hentVirksomhet() {
        String orgnr = "21542512";
        final Optional<Virksomhet> hent = virksomhetRepository.hent(orgnr);
        if (hent.isPresent()) {
            return (VirksomhetEntitet) hent.get();
        }
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(orgnr)
            .medNavn("EPLEHUSET")
            .medRegistrert(LocalDate.now().minusYears(3L))
            .medOppstart(LocalDate.now().minusYears(3L))
            .oppdatertOpplysningerNå()
            .build();
        virksomhetRepository.lagre(virksomhet);
        return virksomhet;
    }

    private MottattDokument lagMottattDokument(Behandling behandling, LocalDate mottattDato) {
        final MottattDokument mottattDokument = new MottattDokument.Builder().medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING)
            .medFagsakId(behandling.getFagsakId())
            .medBehandlingId(behandling.getId())
            .medMottattDato(mottattDato)
            .medElektroniskRegistrert(true)
            .medJournalPostId(new JournalpostId("123123123"))
            .medDokumentId("123123")
            .build();
        mottatteDokumentRepository.lagre(mottattDokument);
        return mottattDokument;
    }
}
