package no.nav.foreldrepenger.uttaksvilkår;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periodetype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeRegelOrkestreringForeldrepengerFørFødselTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void foreldrepengerFørFødsel_happy_case() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(2);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(15);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

    }

    @Test
    public void foreldrepengerFørFødsel_far_søker_fpff() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagFar(fødselsdato)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(1);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(15);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
    }

    @Test
    public void foreldrepengerFørFødsel_manglende_fpff_periode_fører_til_avslag() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(2);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(15);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

    }

    @Test
    public void foreldrepengerFørFødsel_for_lang_fpff_periode_før_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(4), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(4));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(perioder.get(0).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).getEvalueringResultat()).isNotNull();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(1).getInnsendtGrunnlag()).isNull();
        assertThat(perioder.get(1).getEvalueringResultat()).isNull();

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(perioder.get(2).getUttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(2).getInnsendtGrunnlag()).isNull();
        assertThat(perioder.get(2).getEvalueringResultat()).isNull();
    }

    @Test
    public void foreldrepengerFørFødsel_for_lang_fpff_periode_etter_fødsel() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagMor(fødselsdato)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.plusWeeks(2).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(2), fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(0).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).getEvalueringResultat()).isNotNull();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(2).minusDays(1));
        assertThat(perioder.get(1).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).getEvalueringResultat()).isNotNull();

        //Mødrekvoten blir satt til manuell pga forrige periode ble manuell. Ingen årsak eller regelvurdering.
        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(2));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(2).getInnsendtGrunnlag()).isNull();
        assertThat(perioder.get(2).getEvalueringResultat()).isNull();
    }

    @Test
    public void foreldrepengerFørFødsel_for_kort_fpff_periode_slutter_for_tidlig() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagMor(fødselsdato)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3).minusDays(1), fødselsdato.minusWeeks(2), PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(4);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(perioder.get(0).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(3).minusDays(1));
        assertThat(perioder.get(0).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).getEvalueringResultat()).isNotNull();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).getUttakPeriode().getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(perioder.get(1).getUttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(2));
        assertThat(perioder.get(1).getInnsendtGrunnlag()).isNull();
        assertThat(perioder.get(1).getEvalueringResultat()).isNull();

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(2).getUttakPeriode().getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(perioder.get(2).getUttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(2).plusDays(1));
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(2).getInnsendtGrunnlag()).isNull();
        assertThat(perioder.get(2).getEvalueringResultat()).isNull();

        assertThat(perioder.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(3).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(3).getUttakPeriode().getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(perioder.get(3).getUttakPeriode().getManuellbehandlingårsak()).isNull();
        assertThat(perioder.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(3).getInnsendtGrunnlag()).isNull();
        assertThat(perioder.get(3).getEvalueringResultat()).isNull();
    }

    @Test
    public void foreldrepengerFørFødsel_for_kort_fpff_starter_for_sent() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        basicGrunnlagMor(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(1), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> perioder = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(perioder).hasSize(3);

        assertThat(perioder.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(perioder.get(0).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
        assertThat(perioder.get(0).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(perioder.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusWeeks(1).minusDays(1));
        assertThat(perioder.get(0).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(10);
        assertThat(perioder.get(0).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(0).getEvalueringResultat()).isNotNull();

        assertThat(perioder.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(perioder.get(1).getUttakPeriode().getÅrsak()).isEqualTo(InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL);
        assertThat(perioder.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(1));
        assertThat(perioder.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(perioder.get(1).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(5);
        assertThat(perioder.get(1).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(1).getEvalueringResultat()).isNotNull();

        assertThat(perioder.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(perioder.get(2).getUttakPeriode().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MANGLENDE_SØKT_PERIODE);
        assertThat(perioder.get(2).getUttakPeriode().getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER);
        assertThat(perioder.get(2).getUttakPeriode().getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(perioder.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(perioder.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(perioder.get(2).getUttakPeriode().getTrekkdager(AREBEIDSFORHOLD)).isEqualTo(30);
        assertThat(perioder.get(2).getInnsendtGrunnlag()).isNotNull();
        assertThat(perioder.get(2).getEvalueringResultat()).isNotNull();
    }


    private FastsettePeriodeGrunnlagBuilder basicGrunnlagMor(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medSøkerMor(true);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagFar(LocalDate fødselsdato) {
        return basicGrunnlag(fødselsdato)
                .medSøkerMor(false);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlag(LocalDate fødselsdato) {
        return grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL);
    }
}
