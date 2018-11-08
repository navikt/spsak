package no.nav.foreldrepenger.domene.vedtak.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse.InfotrygdHendelse;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse.InfotrygdHendelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse.Meldingstype;
import no.nav.foreldrepenger.domene.vedtak.KanVedtaketIverksettesTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class KanVedtaketIverksettesTjenesteImplTest {

    private static final String INNVILGET = Meldingstype.INFOTRYGD_INNVILGET.getType();
    private static final String ANNULERT = Meldingstype.INFOTRYGD_ANNULLERT.getType();
    private static final String OPPHOERT = Meldingstype.INFOTRYGD_OPPHOERT.getType();
    private static final String ENDRET = Meldingstype.INFOTRYGD_ENDRET.getType();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().silent();

    private BehandlingRepositoryProvider repositoryProvider;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;

    private KanVedtaketIverksettesTjeneste kanVedtaketIverksettesTjeneste;

    @Mock
    private InfotrygdHendelseTjeneste infotrygdHendelseTjeneste;

    @Mock
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    @Mock
    private InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag;

    private Behandling behandling;
    
    private LocalDate startdatoVLYtelse;

    @Before
    public void oppsett(){
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        behandling = scenario.lagMocked();
        repositoryProvider = scenario.mockBehandlingRepositoryProvider();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.startdatoVLYtelse = LocalDate.now();
    }

    // CASE 1:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Innvilget
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_nyeste_hendelse_er_innvilget_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse, startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse infotrygdAktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, infotrygdAktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 2:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Innvilget
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_nyeste_hendelse_er_innvilget_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(2))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse infotrygdAktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, infotrygdAktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 3:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Innvilget
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_nyeste_hendelse_er_innvilget_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(4))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse infotrygdAktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, infotrygdAktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 4:
    // Løpende ytelse: Ja, FP opphører samme dag som infotrygd ytelse
    // Nyeste hendelse: Innvilget
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_nyeste_hendelse_er_innvilget_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse, startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse infotrygdAktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(2)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, infotrygdAktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 5:
    // Løpende ytelse: Ja, FP opphører samme dag som infotrygd ytelse
    // Nyeste hendelse: Innvilget
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_nyeste_hendelse_er_innvilget_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(2)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 6:
    // Løpende ytelse: Ja, FP opphører samme dag som infotrygd ytelse
    // Nyeste hendelse: Innvilget
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_nyeste_hendelse_er_innvilget_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(4))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(2)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 7:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_nyeste_hendelse_er_opphoert_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse)
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 8:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_nyeste_hendelse_er_opphoert_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(3), startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 9:
    // Løpende ytelse: Ja, FP opphører samme dag som infotrygd ytelse
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_nyeste_hendelse_er_opphoert_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse)
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(2)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 10:
    // Løpende ytelse: Ja, FP opphører samme dag som infotrygd ytelse
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_nyeste_hendelse_er_opphoert_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(3), startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(2)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }


    // CASE 11:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_nyeste_hendelse_er_opphoert_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 12:
    // Løpende ytelse: Ja
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_nyeste_hendelse_er_opphoert_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(2)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 13:
    // Løpende ytelse: Ja, infotrygd ytelse opphører samme dag som FP
    // Nyeste hendelse: Ingen
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: N/A
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_samme_dag_som_FP_starter_og_ingen_nyeste_hendelse() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Collections.emptyList(),
            Collections.emptyList()
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse, RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 14:
    // Løpende ytelse: Ja, FP opphører samme dag som infotrygd ytelse
    // Nyeste hendelse: Ingen
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: N/A
    // Skal iverksettes: Nei
    @Test
    public  void skal_ikke_kunne_iverksette_når_FP_opphører_samme_dag_som_infotrygd_ytelse_starter_og_ingen_nyeste_hendelse(){

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Collections.emptyList(),
            Collections.emptyList()
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(7), startdatoVLYtelse.plusDays(14), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 15:
    // Løpende ytelse: Nei, infotrygd ytlese opphører dagen før FP
    // Nyeste hendelse: Innvilet
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_nyeste_hendelse_er_innvilget_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(2))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 16:
    // Løpende ytelse: Nei, infotrygd ytelse opphører dagen for FP
    // Nyeste hendelse: Innvilet
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_nyeste_hendelse_er_innvilget_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(4))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 17:
    // Løpende ytelse: Nei, infotrygd ytelse opphører dagen før FP
    // Nyeste hendelse: Innvilet
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_nyeste_hendelse_er_innvilget_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse, startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 18:
    // Løpende ytelse: Nei, FP opphører dagen før infotrygd ytelse
    // Nyeste hendelse: Innvilet
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_nyeste_hendelse_er_innvilget_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(2))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 19:
    // Løpende ytelse: Nei, FP opphører dagen før infotrygd ytelse
    // Nyeste hendelse: Innvilet
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_nyeste_hendelse_er_innvilget_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3), startdatoVLYtelse.plusDays(4))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 20:
    // Løpende ytelse: Nei, FP opphører dagen før infotrygd ytelse
    // Nyeste hendelse: Innvilet
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_nyeste_hendelse_er_innvilget_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT, ANNULERT),
            Arrays.asList(startdatoVLYtelse, startdatoVLYtelse.plusDays(1), startdatoVLYtelse.plusDays(2), startdatoVLYtelse.plusDays(3))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 21:
    // Løpende ytelse: Nei, infotrygd ytlese opphører dagen før FP
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_nyeste_hendelse_er_opphoert_med_fom_før_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(3), startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 22:
    // Løpende ytelse: Nei, infotrygd ytlese opphører dagen før FP
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_nyeste_hendelse_er_opphoert_med_fom_lik_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse)
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 23:
    // Løpende ytelse: Nei, FP opphører dagen før infortrygd ytelse
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, tidligere
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_nyeste_hendelse_er_opphoert_med_fom_før_startdato(){

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(3), startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 24:
    // Løpende ytelse: Nei, FP opphører dagen før infortrygd ytelse
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Ja, lik
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_nyeste_hendelse_er_opphoert_med_fom_lik_startdato(){

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(2), startdatoVLYtelse.minusDays(1), startdatoVLYtelse)
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 25:
    // Løpende ytelse: Nei, infotrygd ytelse opphører før FP starter
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_nyeste_hendelse_er_opphoert_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }


    // CASE 26:
    // Løpende ytelse: Nei, FP opphører før infortrygd ytelse starter
    // Nyeste hendelse: Opphoert
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: Nei, etter
    // Skal iverksettes: Nei
    @Test
    public void skal_ikke_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_nyeste_hendelse_er_opphoert_med_fom_etter_startdato() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Arrays.asList(INNVILGET, ENDRET, OPPHOERT),
            Arrays.asList(startdatoVLYtelse.minusDays(1), startdatoVLYtelse, startdatoVLYtelse.plusDays(1))
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(false);

    }

    // CASE 27:
    // Løpende ytelse: Nei, infortrygd ytlese opphører før FP starter
    // Nyeste hendelse: Ingen
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: N/A
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_infotrygd_ytelse_opphører_dagen_før_FP_starter_og_ingen_nyeste_hendelse() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Collections.emptyList(),
            Collections.emptyList()
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.minusDays(5), startdatoVLYtelse.minusDays(1), RelatertYtelseTilstand.AVSLUTTET);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusWeeks(1)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusWeeks(1).plusDays(1), startdatoVLYtelse.plusWeeks(2))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    // CASE 28:
    // Løpende ytelse: Nei, FP opphører før infotrygd ytelse starter
    // Nyeste hendelse: Ingen
    // Hendelse FOM dato tidligere/lik startdato for VL ytelse: N/A
    // Skal iverksettes: Ja
    @Test
    public void skal_kunne_iverksette_når_FP_opphører_dagen_før_infotrygd_ytelse_starter_og_ingen_nyeste_hendelse() {

        // Arrange
        List<InfotrygdHendelse> hendelser = lagInfotrygdHendelse(
            Collections.emptyList(),
            Collections.emptyList()
        );
        Beregningsgrunnlag beregningsgrunnlag = buildBeregningsgrunnlag(startdatoVLYtelse);
        lagreBeregningsgrunnlag(behandling, beregningsgrunnlag);
        AktørYtelse aktørYtelse = lagAktørYtelse(startdatoVLYtelse.plusDays(8), startdatoVLYtelse.plusDays(12), RelatertYtelseTilstand.LØPENDE);
        opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse, startdatoVLYtelse.plusDays(3)),
            DatoIntervallEntitet.fraOgMedTilOgMed(startdatoVLYtelse.plusDays(4), startdatoVLYtelse.plusDays(7))
        );

        opprettOgMockFelleseTjenester(hendelser, aktørYtelse);

        // Act
        boolean kanInverksettes = kanVedtaketIverksettesTjeneste.kanVedtaketIverksettes(behandling);

        // Assert
        assertThat(kanInverksettes).isEqualTo(true);

    }

    private List<InfotrygdHendelse> lagInfotrygdHendelse(List<String> typeList, List<LocalDate> datoList) {
        List<InfotrygdHendelse> hendelser = new ArrayList<>();
        int ix = 0;
        long sekvensnummer = 0L;
        for (String type : typeList) {
            InfotrygdHendelse hendelse = InfotrygdHendelse.builder()
                .medAktørId(1001L)
                .medIdentDato("20180101")
                .medFom(datoList.get(ix++))
                .medSekvensnummer(sekvensnummer++)
                .medType(type)
                .medTypeYtelse("FA")
                .build();
            hendelser.add(hendelse);
        }
        return hendelser;
    }

    private Beregningsgrunnlag buildBeregningsgrunnlag(LocalDate startDato) {
        return Beregningsgrunnlag.builder()
            .medSkjæringstidspunkt(startDato)
            .medDekningsgrad(100L)
            .medOpprinneligSkjæringstidspunkt(LocalDate.now().minusDays(5))
            .medGrunnbeløp(BigDecimal.valueOf(91425))
            .medRedusertGrunnbeløp(BigDecimal.valueOf(91425))
            .medRegelloggSkjæringstidspunkt("input1", "clob1")
            .medRegelloggBrukersStatus("input2", "clob2")
            .build();
    }

    private void lagreBeregningsgrunnlag(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag) {
        beregningsgrunnlagRepository.lagre(behandling, beregningsgrunnlag, BeregningsgrunnlagTilstand.FASTSATT);
    }

    private AktørYtelse lagAktørYtelse(LocalDate fom, LocalDate tom, RelatertYtelseTilstand relatertYtelseTilstand) {
        YtelseBuilder ytelseBuilder = lagYtelse(fom, tom, relatertYtelseTilstand);
        return InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty())
            .medAktørId(behandling.getAktørId())
            .leggTilYtelse(ytelseBuilder)
            .build();
    }

    private YtelseBuilder lagYtelse(LocalDate fom, LocalDate tom, RelatertYtelseTilstand relatertYtelseTilstand) {
        return YtelseBuilder.oppdatere(Optional.empty())
            .medYtelseType(RelatertYtelseType.FORELDREPENGER)
            .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom))
            .medKilde(Fagsystem.INFOTRYGD)
            .medStatus(relatertYtelseTilstand);
    }

    private void opprettOgLagreBeregningsResultatFPOgBeregningsResultatPerioder(DatoIntervallEntitet... perioder){
        BeregningsresultatFP beregningsresultatFP = BeregningsresultatFP.builder()
            .medRegelInput("clob1")
            .medRegelSporing("clob2")
            .build();
        for (DatoIntervallEntitet periode : perioder) {
            BeregningsresultatPeriode beregningsresultatPeriode = BeregningsresultatPeriode.builder()
                .medBeregningsresultatPeriodeFomOgTom(periode.getFomDato(), periode.getTomDato())
                .build(beregningsresultatFP);
            beregningsresultatFP.addBeregningsresultatPeriode(beregningsresultatPeriode);
        }
        beregningsresultatFPRepository.lagre(behandling, beregningsresultatFP);
    }

    private void opprettOgMockFelleseTjenester(List<InfotrygdHendelse> hendelser, AktørYtelse aktørYtelse) {
        kanVedtaketIverksettesTjeneste = new KanVedtaketIverksettesTjenesteImpl(repositoryProvider, infotrygdHendelseTjeneste, inntektArbeidYtelseTjeneste);
        when(infotrygdHendelseTjeneste.hentHendelsesListFraInfotrygdFeed(behandling)).thenReturn(hendelser);
        when(inntektArbeidYtelseTjeneste.hentAggregat(behandling)).thenReturn(inntektArbeidYtelseGrunnlag);
        when(inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp(behandling.getAktørId())).thenReturn(Optional.of(aktørYtelse));
    }

}
