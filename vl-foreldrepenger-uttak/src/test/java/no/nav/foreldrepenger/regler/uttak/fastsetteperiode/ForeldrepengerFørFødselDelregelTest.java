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
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Årsak;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class ForeldrepengerFørFødselDelregelTest {

    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void UT1070_mor_utenFor3UkerFørFødsel() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, familiehendelseDato.plusWeeks(8),
                        familiehendelseDato.plusWeeks(9), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuell(regelresultat, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagMor(LocalDate familiehendelseDato) {
        return basicGrunnlag(familiehendelseDato)
                .medSøkerMor(true);
    }

    @Test
    public void UT1071_mor_innenFor3UkerFørFødsel_ikkeManglendeSøktPeriode_ikkeGradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(2),
                        familiehendelseDato.minusWeeks(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
    }

    @Test
    public void UT1072_mor_innenFor3UkerFørFødsel_ikkeManglendeSøktPeriode_gradering() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medGradertStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(2),
                        familiehendelseDato.minusWeeks(1), Collections.singletonList(AktivitetIdentifikator.forFrilans()), BigDecimal.TEN, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertInnvilget(regelresultat, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
        assertThat(regelresultat.getGraderingIkkeInnvilgetÅrsak()).isEqualTo(GraderingIkkeInnvilgetÅrsak.AVSLAG_PGA_FOR_TIDLIG_GRADERING);
    }

    @Test
    public void UT1073_mor_innenFor3UkerFørFødsel_manglendeSøktPeriode() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlagMor(familiehendelseDato)
                .medOppholdPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, Oppholdårsaktype.MANGLENDE_SØKT_PERIODE, PeriodeKilde.SØKNAD,
                        familiehendelseDato.minusWeeks(2), familiehendelseDato.minusWeeks(1))
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
    }

    @Test
    public void UT1076_far_søker_fpff() {
        LocalDate familiehendelseDato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = basicGrunnlag(familiehendelseDato)
                .medSøkerMor(false)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, familiehendelseDato.minusWeeks(2),
                        familiehendelseDato.minusWeeks(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 100)
                //Må ha ellers å faller vi ut på FP_VK 10.5.1 - SjekkOmTomForAlleSineKontoer
                .medSaldo(Stønadskontotype.FEDREKVOTE, 100)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertManuell(regelresultat, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
    }

    private void assertInnvilget(Regelresultat regelresultat, Årsak innvilgetÅrsak) {
        assertThat(regelresultat.oppfylt()).isTrue();
        assertThat(regelresultat.isUtbetal()).isTrue();
        assertThat(regelresultat.getInnvilgetÅrsak()).isEqualTo(innvilgetÅrsak);
    }

    private void assertManuell(Regelresultat regelresultat, Manuellbehandlingårsak manuellbehandlingårsak) {
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isFalse();
        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(manuellbehandlingårsak);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlag(LocalDate familiehendelseDato) {
        return FastsettePeriodeGrunnlagTestBuilder.create()
                .medFørsteLovligeUttaksdag(familiehendelseDato.minusWeeks(15))
                .medFamiliehendelseDato(familiehendelseDato)
                .medSamtykke(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSøknadstype(Søknadstype.FØDSEL);
    }
}