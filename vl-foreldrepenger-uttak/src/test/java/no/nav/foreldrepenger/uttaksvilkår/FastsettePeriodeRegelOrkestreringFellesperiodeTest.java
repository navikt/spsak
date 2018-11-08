package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeRegelOrkestreringFellesperiodeTest extends FastsettePerioderRegelOrkestreringTestBase {

    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private LocalDate førsteLovligeUttaksdag = førsteLovligeUttaksdag(fødselsdato);

    @Test
    public void fellesperiode_mor_etter_uke_7_etter_fødsel_uten_nok_dager_blir_innvilget_med_knekk_og_manuell_behandling_av_periode_uten_nok_dager() {
        basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 4 * 5);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), INNVILGET, FORELDREPENGER_FØR_FØDSEL);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), INNVILGET, MØDREKVOTE);
        verifiserPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), INNVILGET, FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(15).minusDays(1), FELLESPERIODE, IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN, Manuellbehandlingårsak.STØNADSKONTO_TOM);
    }

    @Test
    public void fellesperiode_far_etter_uke_7_etter_fødsel_blir_manuell_behandling_pga_aktivitetskravet() {
        basicGrunnlagFar()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 4 * 5);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(1);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(15), FELLESPERIODE, null, Manuellbehandlingårsak.AKTIVITEKTSKRAVET_MÅ_SJEKKES_MANUELT);
    }

    @Test
    public void for_tidlig_fellesperiode_far_blir_knekt_og_må_behandles_manuelt() {
        basicGrunnlagFar()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(5), fødselsdato.plusWeeks(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(3);
        verifiserManuellBehandlingPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(5), fødselsdato.minusWeeks(3).minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), FELLESPERIODE, null, null);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(1), FELLESPERIODE, null, null);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_ved_12_uker_og_slutter_etter_3_uker_før_fødsel_blir_innvilget_med_knekk_ved_3_uker_resten_blir_manuell_behandling() {
        basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(1).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(1), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5)
                .build();

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(4);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1), Perioderesultattype.INNVILGET, FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusWeeks(1).minusDays(1), FELLESPERIODE, null, Manuellbehandlingårsak.UGYLDIG_STØNADSKONTO);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.minusWeeks(1), fødselsdato.minusDays(1), FORELDREPENGER_FØR_FØDSEL, null, null);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE, null, null);
    }


    @Test
    public void fellesperiode_mor_uttak_starter_ved_3_uker_etter_fødsel_blir_knekt_ved_6_uker_og_må_behandles_manuelt() {
        basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(4);
        assertThat(resultater.get(0).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        verifiserAvslåttPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), Stønadskontotype.FORELDREPENGER_FØR_FØDSEL,
                IkkeOppfyltÅrsak.MOR_TAR_IKKE_ALLE_UKENE);
        assertThat(resultater.get(1).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        verifiserManuellBehandlingPeriode(resultater.get(1).getUttakPeriode(), fødselsdato, fødselsdato.plusWeeks(3).minusDays(1), Stønadskontotype.MØDREKVOTE, IkkeOppfyltÅrsak.HULL_MELLOM_FORELDRENES_PERIODER, Manuellbehandlingårsak.MANGLENDE_SØKT_PERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.plusWeeks(3), fødselsdato.plusWeeks(6).minusDays(1), FELLESPERIODE, null, null);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), FELLESPERIODE, null, null);
    }

    @Test
    public void fellesperiode_mor_uttak_starter_før_12_uker_blir_avslått_med_knekk_ved_12_uker_før_fødsel() {
        basicGrunnlagMor()
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(13), fødselsdato, PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FELLESPERIODE, 13 * 5);

        List<FastsettePeriodeResultat> resultater = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultater).hasSize(5);
        verifiserPeriode(resultater.get(0).getUttakPeriode(), fødselsdato.minusWeeks(13), fødselsdato.minusWeeks(12).minusDays(1), Perioderesultattype.MANUELL_BEHANDLING, Stønadskontotype.FELLESPERIODE);
        verifiserPeriode(resultater.get(1).getUttakPeriode(), fødselsdato.minusWeeks(12), fødselsdato.minusWeeks(3).minusDays(1), Perioderesultattype.MANUELL_BEHANDLING, Stønadskontotype.FELLESPERIODE);
        verifiserManuellBehandlingPeriode(resultater.get(2).getUttakPeriode(), fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), FELLESPERIODE, null, null);
        verifiserManuellBehandlingPeriode(resultater.get(3).getUttakPeriode(), fødselsdato, fødselsdato, FELLESPERIODE, null, null);
        assertThat(resultater.get(4).getUttakPeriode()).isInstanceOf(OppholdPeriode.class);
        verifiserManuellBehandlingPeriode(resultater.get(4).getUttakPeriode(), fødselsdato.plusDays(1), fødselsdato.plusWeeks(6).minusDays(1), MØDREKVOTE, null, null);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagMor() {
        return basicGrunnlag().medSøkerMor(true);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlagFar() {
        return basicGrunnlag().medSøkerMor(false);
    }

    private FastsettePeriodeGrunnlagBuilder basicGrunnlag() {
        return grunnlag.medFørsteLovligeUttaksdag(førsteLovligeUttaksdag)
                .medFamiliehendelseDato(fødselsdato)
                .medSamtykke(true)
                .medMorRett(true)
                .medFarRett(true)
                .medSøknadstype(Søknadstype.FØDSEL);
    }
}
