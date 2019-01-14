package no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class BeregningsgrunnlagPrStatusOgAndelTest {
    private static final LocalDate SKJÆRINGSTIDSPUNKT = LocalDate.now();
    private static final String ARBEIDSFORHOLD_ORGNR = "987";
    private static final String ARBEIDSFORHOLD_ID = "123abc456";
    private static final OpptjeningAktivitetType ARBEIDSFORHOLD_TYPE = OpptjeningAktivitetType.ETTERLØNN_ARBEIDSGIVER;
    private final LocalDate PERIODE_FOM = LocalDate.now();

    private BeregningsgrunnlagPeriode beregningsgrunnlagPeriode;
    private BeregningsgrunnlagPrStatusOgAndel prStatusOgAndel;
    private VirksomhetEntitet beregningVirksomhet;

    @Before
    public void setup() {
        beregningVirksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(ARBEIDSFORHOLD_ORGNR)
                .medNavn("BeregningVirksomheten")
                .oppdatertOpplysningerNå()
                .build();
        Beregningsgrunnlag beregningsgrunnlag = lagBeregningsgrunnlag();
        BeregningsgrunnlagPeriode.Builder builder = lagBeregningsgrunnlagPeriodeBuilder();
        beregningsgrunnlagPeriode= builder.build(beregningsgrunnlag);
        prStatusOgAndel = lagBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriode);
    }

    @Test
    public void skal_bygge_instans_med_påkrevde_felter() {
        assertThat(prStatusOgAndel.getBeregningsgrunnlagPeriode()).isEqualTo(beregningsgrunnlagPeriode);
        assertThat(prStatusOgAndel.getAktivitetStatus()).isEqualTo(AktivitetStatus.ARBEIDSTAKER);
        assertThat(prStatusOgAndel.getBgAndelArbeidsforhold().map(BGAndelArbeidsforhold::getArbeidsforholdOrgnr).get()).isEqualTo(ARBEIDSFORHOLD_ORGNR);
        assertThat(prStatusOgAndel.getBgAndelArbeidsforhold().flatMap(BGAndelArbeidsforhold::getArbeidsforholdRef).get().getReferanse()).isEqualTo(ARBEIDSFORHOLD_ID);
        assertThat(prStatusOgAndel.getArbeidsforholdType()).isEqualTo(ARBEIDSFORHOLD_TYPE);
    }

    @Test
    public void skal_ikke_bygge_instans_hvis_mangler_påkrevde_felter() {
        BeregningsgrunnlagPrStatusOgAndel.Builder beregningsgrunnlagPeriodeBuilder = BeregningsgrunnlagPrStatusOgAndel.builder();
        try {
            beregningsgrunnlagPeriodeBuilder.build(null);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsgrunnlagPeriode");
        }

        try {
            beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlagPeriode);
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("aktivitetStatus");
        }

        beregningsgrunnlagPeriodeBuilder.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);

        try {
            beregningsgrunnlagPeriodeBuilder.build(beregningsgrunnlagPeriode);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("beregningsperiodeFom");
        }

        try {
            beregningsgrunnlagPeriodeBuilder.medBeregningsperiode(PERIODE_FOM, null);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("Til og med dato må være satt");
        }

        try {
            beregningsgrunnlagPeriodeBuilder.medBeregningsperiode(PERIODE_FOM, PERIODE_FOM.plusDays(2));
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void skal_håndtere_null_this_feilKlasse_i_equals() {
        assertThat(prStatusOgAndel).isNotEqualTo(null);
        assertThat(prStatusOgAndel).isNotEqualTo("blabla");
        assertThat(prStatusOgAndel).isEqualTo(prStatusOgAndel);
    }

    @Test
    public void skal_ha_refleksiv_equalsOgHashCode() {
        BeregningsgrunnlagPrStatusOgAndel prStatusOgAndel2 = lagBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel).isEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel2).isEqualTo(prStatusOgAndel);
        assertThat(prStatusOgAndel.hashCode()).isEqualTo(prStatusOgAndel2.hashCode());
        assertThat(prStatusOgAndel2.hashCode()).isEqualTo(prStatusOgAndel.hashCode());

        BeregningsgrunnlagPrStatusOgAndel.Builder builder = lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.virksomhet(beregningVirksomhet));
        builder.medAktivitetStatus(AktivitetStatus.FRILANSER);
        prStatusOgAndel2 = builder.build(beregningsgrunnlagPeriode);
        assertThat(prStatusOgAndel).isNotEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel2).isNotEqualTo(prStatusOgAndel);
        assertThat(prStatusOgAndel.hashCode()).isNotEqualTo(prStatusOgAndel2.hashCode());
        assertThat(prStatusOgAndel2.hashCode()).isNotEqualTo(prStatusOgAndel.hashCode());
    }

    @Test
    public void skal_bruke_aktivitetStatus_i_equalsOgHashCode() {
        BeregningsgrunnlagPrStatusOgAndel prStatusOgAndel2 = lagBeregningsgrunnlagPrStatusOgAndel(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel).isEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel.hashCode()).isEqualTo(prStatusOgAndel2.hashCode());

        BeregningsgrunnlagPrStatusOgAndel.Builder builder = lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.virksomhet(beregningVirksomhet));
        builder.medAktivitetStatus(AktivitetStatus.FRILANSER);
        prStatusOgAndel2 = builder.build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel).isNotEqualTo(prStatusOgAndel2);
        assertThat(prStatusOgAndel.hashCode()).isNotEqualTo(prStatusOgAndel2.hashCode());
    }

    @Test
    public void skal_runde_av_og_sette_dagsats_riktig() {
        prStatusOgAndel = BeregningsgrunnlagPrStatusOgAndel.builder(prStatusOgAndel)
            .medRedusertBrukersAndelPrÅr(BigDecimal.valueOf(377127.4))
            .medRedusertRefusjonPrÅr(BigDecimal.valueOf(214892.574))
            .build(beregningsgrunnlagPeriode);

        assertThat(prStatusOgAndel.getDagsatsBruker()).isEqualTo(1450);
        assertThat(prStatusOgAndel.getDagsatsArbeidsgiver()).isEqualTo(827);

    }

    @Test
    public void skal_kunne_ha_privatperson_som_arbeidsgiver() {
        String aktørId = "123213123123";
        BeregningsgrunnlagPrStatusOgAndel.Builder builder = lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.person(new AktørId(aktørId)));
        builder.medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER);
        BeregningsgrunnlagPrStatusOgAndel bgpsa = builder.build(beregningsgrunnlagPeriode);

        assertThat(bgpsa.getBgAndelArbeidsforhold().get().getArbeidsgiver().get().getIdentifikator()).isEqualTo("123213123123");
    }

    private BeregningsgrunnlagPrStatusOgAndel lagBeregningsgrunnlagPrStatusOgAndel(BeregningsgrunnlagPeriode beregningsgrunnlagPeriode) {
        return lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver.virksomhet(beregningVirksomhet)).build(beregningsgrunnlagPeriode);
    }

    private BeregningsgrunnlagPrStatusOgAndel.Builder lagBeregningsgrunnlagPrStatusOgAndelBuilder(Arbeidsgiver arbeidsgiver) {
        BGAndelArbeidsforhold.Builder bga = BGAndelArbeidsforhold.builder()
            .medArbeidsgiver(arbeidsgiver)
            .medArbforholdRef(ARBEIDSFORHOLD_ID)
            .medArbeidsperiodeFom(LocalDate.now().minusYears(1))
            .medArbeidsperiodeTom(LocalDate.now().plusYears(2));

        return BeregningsgrunnlagPrStatusOgAndel.builder()
            .medBGAndelArbeidsforhold(bga)
            .medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER)
            .medArbforholdType(ARBEIDSFORHOLD_TYPE);
    }

    private BeregningsgrunnlagPeriode.Builder lagBeregningsgrunnlagPeriodeBuilder() {
        return BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(PERIODE_FOM, null);
    }

    private static Beregningsgrunnlag lagBeregningsgrunnlag() {
        return lagBeregningsgrunnlagMedSkjæringstidspunkt(SKJÆRINGSTIDSPUNKT);
    }

    private static Beregningsgrunnlag lagBeregningsgrunnlagMedSkjæringstidspunkt(LocalDate skjæringstidspunkt) {
        return Beregningsgrunnlag.builder().medSkjæringstidspunkt(skjæringstidspunkt).build();
    }
}
