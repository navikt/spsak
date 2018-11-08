package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GraderingIkkeInnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.FeatureToggles;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class ForeldrepengerFødselDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON, featureToggles());

    private FeatureToggles featureToggles() {
        return new FeatureToggles() {
            @Override
            public boolean foreldrepengerFødsel() {
                return true;
            }
        };
    }

    @Test
    public void UT1185_mor_starter_tidligere_enn_12_uker_før_termin() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(12).minusDays(1), familiehendelseDato, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 15)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_SØKER_FELLESPERIODE_FØR_12_UKER_FØR_TERMIN_FØDSEL);
    }

    @Test
    public void UT1186_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(6),
                        familiehendelseDato.minusWeeks(5), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medMorRett(false)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1211_utenAleneomsorg_morRett_aleneomsorg_før3ukerFørFødsel_disponibleDager_ikkeGradering_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(6),
                        familiehendelseDato.minusWeeks(5), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medMorRett(true)
                .medAleneomsorg(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));
        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    public void UT1187_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(6), familiehendelseDato,
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1212_mor_aleneomsorg_før3ukerFørFødsel_disponibleDager_gradering_bareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(6), familiehendelseDato,
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medMorRett(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilgetMenAvslåttGradering(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT, GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1210_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    public void UT1213_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_gradering_avklart_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN,
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medMorRett(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    @Test
    public void UT1190_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1214_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_ikkeGradering_morRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8),
                        PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medMorRett(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_KUN_MOR_HAR_RETT);
    }

    private void assertInnvilgetMenAvslåttGradering(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak, GraderingIkkeInnvilgetÅrsak graderingIkkeInnvilgetÅrsak) {
        assertInnvilget(regelresultat, innvilgetÅrsak);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(graderingIkkeInnvilgetÅrsak);
    }

    @Test
    public void UT1188_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_ikkeDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(7), familiehendelseDato.plusWeeks(8), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 0)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void UT1189_mor_aleneomsorg_etter6ukerEtterFødsel_omsorg_disponibleDager_uavklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(6),
                        familiehendelseDato.plusWeeks(7), Collections.singletonList(AktivitetIdentifikator.forArbeid("orgnr", "arbid")),
                        BigDecimal.TEN,  PeriodeVurderingType.UAVKLART_PERIODE)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.PERIODE_UAVKLART);
    }

    @Test
    public void UT1191_mor_aleneomsorg_etter6ukerEtterFødsel_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(6);
        LocalDate tom = familiehendelseDato.plusWeeks(7);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,  PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medPeriodeUtenOmsorg(fom, tom)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1209_mor_utenAleneomsorg_ikkeBareMorRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(6);
        LocalDate tom = familiehendelseDato.plusWeeks(7);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,  PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medPeriodeUtenOmsorg(fom, tom)
                .medAleneomsorg(false)
                .medFarRett(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    private void assertManuellBehandling(Regelresultat regelresultat, IkkeOppfyltÅrsak ikkeOppfyltÅrsak, Manuellbehandlingårsak manuellBehandlingÅrsak) {
        assertManuellBehandling(regelresultat, ikkeOppfyltÅrsak, manuellBehandlingÅrsak, false, false);
    }

    private void assertManuellBehandling(Regelresultat regelresultat,
                                         IkkeOppfyltÅrsak ikkeOppfyltÅrsak,
                                         Manuellbehandlingårsak manuellBehandlingÅrsak,
                                         boolean trekkdager,
                                         boolean utbetal) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isEqualTo(trekkdager);
        assertThat(regelresultat.isUtbetal()).isEqualTo(utbetal);
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(ikkeOppfyltÅrsak);
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(manuellBehandlingÅrsak);
    }

    @Test
    public void UT1205_mor_utenAleneomsorg_bareMorRett_før3ukerFørFamiliehendelse_utenDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(5),
                        familiehendelseDato.minusWeeks(4), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 0)
                .medAleneomsorg(false)
                .medMorRett(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, null);
    }

    @Test
    public void UT1193_far_før_familiehendelse() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(3),
                        familiehendelseDato.minusWeeks(2), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    @Test
    public void UT1194_far_etterFamiliehendelse_aleneomsorg_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .medPeriodeUtenOmsorg(fom, tom)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1195_far_etterFamiliehendelse_aleneomsorg_medOmsorg_utenDisponibledager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 0)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void UT1196_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_medGradering_førUke7() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(1);
        LocalDate tom = familiehendelseDato.plusWeeks(2);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, Collections.singletonList(AktivitetIdentifikator.forFrilans()),
                        BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.GRADERING_ALENEOMSORG);
    }

    @Test
    public void UT1197_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_medGradering_etterUke7_periodeUavklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, Collections.singletonList(AktivitetIdentifikator.forFrilans()),
                        BigDecimal.TEN, PeriodeVurderingType.UAVKLART_PERIODE)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.PERIODE_UAVKLART);
    }

    @Test
    public void UT1198_far_etterFamiliehendelse_aleneomsorg_medOmsorg_medDisponibledager_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medAleneomsorg(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_ALENEOMSORG);
    }

    @Test
    public void UT1199_far_etterFamiliehendelse_utenAleneomsorg_farRett_utenOmsorg() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medPeriodeUtenOmsorg(fom, tom)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.FAR_HAR_IKKE_OMSORG);
    }

    @Test
    public void UT1200_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_utenGyldigGrunn() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.UAVKLART_PERIODE)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.BEGRUNNELSE_IKKE_GYLDIG);
    }

    @Test
    public void UT1201_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null,     Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, true);
    }

    @Test
    public void UT1201_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_utenGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null,     Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, true);
    }

    @Test
    public void UT1216_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(4);
        LocalDate tom = familiehendelseDato.plusWeeks(5);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, true);
    }

    @Test
    public void UT1216_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_medGradering_avklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT, true, true);
    }

    @Test
    public void UT1202_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_medDisponibleDager_medGradering_uavklart() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom,
                        Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.UAVKLART_PERIODE)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.PERIODE_UAVKLART);
    }

    @Test
    public void UT1202_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_førUke7_medGyldigGrunn_medGradering_uavklart() {
        // TODO BIXBITE Vil ikke kunne nå dette sluttpunktet før gyldig grunn endres til å ikke se på om perioden er avklart
    }

    @Test
    public void UT1203_far_etterFamiliehendelse_utenAleneomsorg_farRett_medOmsorg_EtterUke7_utenDisponibleDager() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 0)
                .medFarRett(true)
                .medMorRett(false)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void UT1204_far_etterFamiliehendelse_utenAleneomsorg_utenFarRett() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        LocalDate fom = familiehendelseDato.plusWeeks(8);
        LocalDate tom = familiehendelseDato.plusWeeks(9);
        FastsettePeriodeGrunnlag grunnlag = grunnlagFar(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER, PeriodeKilde.SØKNAD, fom, tom, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER, 100)
                .medMorRett(true)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuellBehandling(regelresultat, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    private void assertInnvilget(Regelresultat regelresultat, InnvilgetÅrsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private FastsettePeriodeGrunnlagBuilder grunnlagMor(LocalDate familiehendelseDato) {
        return grunnlag(familiehendelseDato, true);
    }

    private FastsettePeriodeGrunnlagBuilder grunnlagFar(LocalDate familiehendelseDato) {
        return grunnlag(familiehendelseDato, false);
    }

    private FastsettePeriodeGrunnlagBuilder grunnlag(LocalDate familiehendelseDato, boolean søkerMor) {
        return FastsettePeriodeGrunnlagTestBuilder.create()
                .medFørsteLovligeUttaksdag(familiehendelseDato.minusWeeks(15))
                .medFamiliehendelseDato(familiehendelseDato)
                .medSøkerMor(søkerMor)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL);
    }
}