package no.nav.foreldrepenger.uttaksvilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public class FastsettePeriodeRegelOrkestreringKnekkpunktTest extends FastsettePerioderRegelOrkestreringTestBase {



    @Test
    public void foreldrepengerFørFødssel_søknad_fra_3_uker_før_fødsel_til_og_med_fødsel_blir_knekt_ved_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 3 * 5)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato);
    }


    @Test
    public void foreldrepengerFørFødsel_periode_dekker_alle_ukene_før_fødsel_slutter_etter_fødsel_blir_hvor_ukene_etter_fødsel_blir_avslått() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(4), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 3 * 5)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);

        assertThat(perioder).hasSize(3); //3 perioder fordi det også lages en manglende søkt periode for de 6 første ukene.

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(4));
    }

    @Test
    public void riktig_knekk_ved_tom_for_dager_ifm_helg() {
        LocalDate fødselsdato = LocalDate.of(2018, 5, 15);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, LocalDate.of(2018, 6, 30), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 7, 1), LocalDate.of(2018, 12, 21), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(Stønadskontotype.MØDREKVOTE, 15*5)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);

        assertThat(perioder).hasSize(5);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 6, 26));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 6, 30));

        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 7, 1));
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 8, 27));

        assertThat(perioder.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(4).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
        assertThat(perioder.get(4).getUttakPeriode().getFom()).isEqualTo(LocalDate.of(2018, 8, 28));
        assertThat(perioder.get(4).getUttakPeriode().getTom()).isEqualTo(LocalDate.of(2018, 12, 21));
    }

}
