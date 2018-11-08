package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedBarnInnlagt;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFerie;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFulltArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedSykdomEllerSkade;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UtsettelsePeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;

public class FastsettePerioderRegelOrkestreringUtsettelseTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void periode_med_gyldig_utsettelse_pga_barn_innlagt_i_helseinstitusjon_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.INNLAGT_BARN, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedBarnInnlagt(new PeriodeMedBarnInnlagt(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_søker_innlagt_i_helseinstitusjon_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.INNLAGT_HELSEINSTITUSJON, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedInnleggelse(new PeriodeMedInnleggelse(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_søkers_sykdom_eller_skade_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.SYKDOM_SKADE, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedSykdomEllerSkade(new PeriodeMedSykdomEllerSkade(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_arbeid_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.ARBEID, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedFulltArbeid(new PeriodeMedFulltArbeid(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_ferie_skal_innvilges() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedFerie(new PeriodeMedFerie(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med gyldig utsettelse
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }

    @Test
    public void periode_med_gyldig_utsettelse_pga_ferie_skal_til_manuell_behandling_grunnet_bevegelige_helligdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 15);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedFerie(new PeriodeMedFerie(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(8);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        // 26.03 - 28.03 er en periode grunnet helligdag den 29.03
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(2));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //2 neste uker med ugyldig utsettelse grunnet bevegelig helligdag

        // 29.03 - 29.03 er en periode fordi 29.mars er skjærtorsdag
        uttakPeriode = resultat.get(4).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 30.03 - 01.04 er en periode fordi 30.mars er en helligdag (langfredag), 31.03 er helg og 01.04 er helg&helligdag
        uttakPeriode = resultat.get(5).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(6));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 02.04 - 02.04 er en periode fordi 02.april er 2.påskedag
        uttakPeriode = resultat.get(6).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 03.04 - 08.04 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(7).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(1));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(6));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    public void flere_perioder_med_gyldig_utsettelse_pga_ferie_skal_til_manuell_behandling_grunnet_bevegelige_helligdager() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 15);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medFarRett(true)
            .medMorRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedFerie(new PeriodeMedFerie(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1)))
            .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(12), fødselsdato.plusWeeks(17).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(17), fødselsdato.plusWeeks(18).minusDays(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
            .medPeriodeMedFerie(new PeriodeMedFerie(fødselsdato.plusWeeks(17), fødselsdato.plusWeeks(18).minusDays(1)))
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(12);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        // 26.03 - 28.03 er en periode grunnet helligdag den 29.03
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(2));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //2 neste uker med ugyldig utsettelse grunnet bevegelig helligdag

        // 29.03 - 29.03 er en periode fordi 29.mars er skjærtorsdag
        uttakPeriode = resultat.get(4).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(3));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 30.03 - 01.04 er en periode fordi 30.mars er en helligdag (langfredag), 31.03 er helg og 01.04 er helg&helligdag
        uttakPeriode = resultat.get(5).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(4));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).plusDays(6));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 02.04 - 02.04 er en periode fordi 02.april er 2.påskedag
        uttakPeriode = resultat.get(6).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(11));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 03.04 - 08.04 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(7).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(11).plusDays(1));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 09.04 - 13.05 er en periode fordi det er søkt om vanlig fellesperiode, ingen utsettelse
        uttakPeriode = resultat.get(8).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(12));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 14.05 - 16.05 er en periode grunnet helligdag den 17.05
        uttakPeriode = resultat.get(9).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(2));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 17.05 - 17.05 er en periode pga 17.mai
        uttakPeriode = resultat.get(10).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(3));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(3));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);

        // 18.05 - 20.05 er en periode fordi det er resten av perioden, uten helligdag
        uttakPeriode = resultat.get(11).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(17).plusDays(4));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(18).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    @Test
    public void periode_med_ugyldig_utsettelse_skal_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag.medFørsteLovligeUttaksdag(fødselsdato.minusMonths(3))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSamtykke(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medUtsettelsePeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), Utsettelseårsaktype.INNLAGT_BARN, PeriodeVurderingType.PERIODE_OK, false, false)
            .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        assertDeTreFørstePeriodene(resultat, fødselsdato);

        //2 neste uker med forsøkt utsettelse
        UttakPeriode uttakPeriode = resultat.get(3).getUttakPeriode();
        assertTrue(uttakPeriode instanceof UtsettelsePeriode);
        UtsettelsePeriode utsettelsePeriode = (UtsettelsePeriode) uttakPeriode;
        assertThat(utsettelsePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(utsettelsePeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(utsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
    }

    private void assertDeTreFørstePeriodene(List<FastsettePeriodeResultat> resultat, LocalDate fødselsdato) {
        //3 uker før fødsel - innvilges
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //6 første uker mødrekvote innvilges
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //4 neste uker mødrekvote innvilges
        uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
    }
}
