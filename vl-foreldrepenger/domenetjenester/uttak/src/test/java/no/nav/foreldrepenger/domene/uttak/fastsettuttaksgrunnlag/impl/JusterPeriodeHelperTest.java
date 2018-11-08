package no.nav.foreldrepenger.domene.uttak.fastsettuttaksgrunnlag.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;

public class JusterPeriodeHelperTest {

    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
    private JusterPeriodeHelper justerPeriodeHelper = new JusterPeriodeHelper();

    @Test
    public void normal_case_føder_på_termin() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mk = lagPeriode(UttakPeriodeType.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(15).minusDays(1));
        OppgittPeriode fp = lagPeriode(UttakPeriodeType.FELLESPERIODE, fødselsdato.plusWeeks(15), fødselsdato.plusWeeks(31).minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff, mk, fp), true);

        //Føder på termin
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, fødselsdato);

        //OppgittFordelig er uendret
        assertThat(likePerioder(oppgittFordeling, justertFordeling)).isTrue();
    }

    @Test
    public void foreldrepenger_før_fødsel_forkortes_ved_for_tidlig_fødsel() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff), true);

        //Føder en dag før termin
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, fødselsdato.minusDays(1));

        //Periode skal flyttes 1 dag tidligere, men ikke før første uttak
        assertThat(likePerioder(oppgittFordeling, justertFordeling)).isFalse();
        assertThat(justertFordeling.getOppgittePerioder()).hasSize(1);
        OppgittPeriode justertFpff = justertFordeling.getOppgittePerioder().get(0);
        assertThat(justertFpff.getFom()).isEqualTo(fpff.getFom());
        assertThat(justertFpff.getTom()).isEqualTo(fpff.getTom().minusDays(1));
    }

    @Test
    public void foreldrepenger_før_fødsel_forsvinner_når_fødsel_er_4_uker_for_tidlig() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mk = lagPeriode(UttakPeriodeType.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff, mk), true);

        //Føder en dag før termin
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, fødselsdato.minusDays(28));

        //Periode skal flyttes 1 dag tidligere, men ikke før første uttak
        assertThat(likePerioder(oppgittFordeling, justertFordeling)).isFalse();
        assertThat(justertFordeling.getOppgittePerioder()).hasSize(1);
        OppgittPeriode justertMk = justertFordeling.getOppgittePerioder().get(0);
        assertThat(justertMk.getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertMk.getFom()).isEqualTo(mk.getFom().minusDays(28));
        assertThat(justertMk.getTom()).isEqualTo(mk.getTom());
    }

    @Test
    public void sen_fødsel_med_hull_i_perioden_med_fødsel() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mk = lagPeriode(UttakPeriodeType.MØDREKVOTE, fødselsdato.plusWeeks(1), fødselsdato.plusWeeks(7).minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff, mk), true);

        //Føder to dager etter termin
        LocalDate faktiskFødselsdato = fødselsdato.plusDays(4);
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, faktiskFødselsdato);

        //Opprett fellesperiode før på 4 dager
        assertThat(justertFordeling.getOppgittePerioder()).hasSize(4);
        assertThat(justertFordeling.getOppgittePerioder().get(0).getPeriodeType()).isEqualTo(UttakPeriodeType.FELLESPERIODE);
        assertThat(justertFordeling.getOppgittePerioder().get(0).getFom()).isEqualTo(fpff.getFom());
        assertThat(justertFordeling.getOppgittePerioder().get(0).getTom()).isEqualTo(fpff.getFom().plusDays(3));

        //Flytt søkt fpff
        assertThat(justertFordeling.getOppgittePerioder().get(1).getPeriodeType()).isEqualTo(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(justertFordeling.getOppgittePerioder().get(1).getFom()).isEqualTo(faktiskFødselsdato.minusWeeks(3));
        assertThat(justertFordeling.getOppgittePerioder().get(1).getTom()).isEqualTo(faktiskFødselsdato.minusDays(1));

        //Opprett en mødrekvoteperiode på 4 dager
        assertThat(justertFordeling.getOppgittePerioder().get(2).getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertFordeling.getOppgittePerioder().get(2).getFom()).isEqualTo(faktiskFødselsdato);
        assertThat(justertFordeling.getOppgittePerioder().get(2).getTom()).isEqualTo(faktiskFødselsdato.plusWeeks(1).minusDays(1));

        //Flytt søkt mødrekvote 4 dager, men ikke flytt TOM
        assertThat(justertFordeling.getOppgittePerioder().get(3).getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertFordeling.getOppgittePerioder().get(3).getFom()).isEqualTo(mk.getFom().plusDays(4));
        assertThat(justertFordeling.getOppgittePerioder().get(3).getTom()).isEqualTo(mk.getTom());
    }

    @Test
    public void fyller_på_med_fellesperiode_i_start_ved_fødsel_1_dag_etter_termin() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mk = lagPeriode(UttakPeriodeType.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff, mk), true);

        //Føder en dag etter termin
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, fødselsdato.plusDays(1));

        //Periode skal flyttes 1 dag fremover, og det skal fylles på med fellesperiode i starten
        assertThat(likePerioder(oppgittFordeling, justertFordeling)).isFalse();
        assertThat(justertFordeling.getOppgittePerioder()).hasSize(3);

        OppgittPeriode ekstraFp = justertFordeling.getOppgittePerioder().get(0);
        assertThat(ekstraFp.getPeriodeType()).isEqualTo(UttakPeriodeType.FELLESPERIODE);
        assertThat(ekstraFp.getFom()).isEqualTo(fpff.getFom());
        assertThat(ekstraFp.getTom()).isEqualTo(fpff.getFom());

        OppgittPeriode justertFpff = justertFordeling.getOppgittePerioder().get(1);
        assertThat(justertFpff.getPeriodeType()).isEqualTo(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(justertFpff.getFom()).isEqualTo(fpff.getFom().plusDays(1));
        assertThat(justertFpff.getTom()).isEqualTo(fpff.getTom().plusDays(1));

        OppgittPeriode justertMk = justertFordeling.getOppgittePerioder().get(2);
        assertThat(justertMk.getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertMk.getFom()).isEqualTo(mk.getFom().plusDays(1));
        assertThat(justertMk.getTom()).isEqualTo(mk.getTom());
    }

    @Test
    public void utsettelses_skal_ikke_flyttes_dersom_fødsel_før_termin() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mk = lagPeriode(UttakPeriodeType.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        OppgittPeriode utsettelsePgaFerie = lagUtsettelse(UttakPeriodeType.FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(14).minusDays(1));
        OppgittPeriode fp = lagPeriode(UttakPeriodeType.FELLESPERIODE, fødselsdato.plusWeeks(14), fødselsdato.plusWeeks(24).minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff, mk, utsettelsePgaFerie, fp), true);


        //Føder en uke før termin
        LocalDate justertFødselsdato = fødselsdato.minusWeeks(1);
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, justertFødselsdato);

        //Periodene skal flyttes 1 uke, utsettelse skal være uendret og fpff skal avkortes.
        assertThat(likePerioder(oppgittFordeling, justertFordeling)).isFalse();
        assertThat(justertFordeling.getOppgittePerioder()).hasSize(5);

        OppgittPeriode justertFpff = justertFordeling.getOppgittePerioder().get(0);
        assertThat(justertFpff.getPeriodeType()).isEqualTo(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(justertFpff.getFom()).isEqualTo(fpff.getFom());
        assertThat(justertFpff.getTom()).isEqualTo(justertFødselsdato.minusDays(1));

        OppgittPeriode justertMk = justertFordeling.getOppgittePerioder().get(1);
        assertThat(justertMk.getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertMk.getFom()).isEqualTo(justertFødselsdato);
        assertThat(justertMk.getTom()).isEqualTo(justertFødselsdato.plusWeeks(10).minusDays(1));
        assertThat(justertMk.getArbeidsprosent()).isNull();

        OppgittPeriode flyttetFpFørUtsettelse = justertFordeling.getOppgittePerioder().get(2);
        assertThat(flyttetFpFørUtsettelse.getPeriodeType()).isEqualTo(UttakPeriodeType.FELLESPERIODE);
        assertThat(flyttetFpFørUtsettelse.getFom()).isEqualTo(justertFødselsdato.plusWeeks(10));
        assertThat(flyttetFpFørUtsettelse.getTom()).isEqualTo(justertFødselsdato.plusWeeks(11).minusDays(1));

        OppgittPeriode uendretUtsettelse = justertFordeling.getOppgittePerioder().get(3);
        assertThat(uendretUtsettelse.getÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uendretUtsettelse.getFom()).isEqualTo(utsettelsePgaFerie.getFom());
        assertThat(uendretUtsettelse.getTom()).isEqualTo(utsettelsePgaFerie.getTom());

        OppgittPeriode justertFp = justertFordeling.getOppgittePerioder().get(4);
        assertThat(justertFp.getPeriodeType()).isEqualTo(UttakPeriodeType.FELLESPERIODE);
        assertThat(justertFp.getFom()).isEqualTo(fp.getFom());
        assertThat(justertFp.getTom()).isEqualTo(fp.getTom());
    }


    @Test
    public void utsettelses_skal_ikke_flyttes_dersom_fødsel_etter_termin() {
        OppgittPeriode fpff = lagPeriode(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1));
        OppgittPeriode mk = lagPeriode(UttakPeriodeType.MØDREKVOTE, fødselsdato, fødselsdato.plusWeeks(10).minusDays(1));
        OppgittPeriode utsettelsePgaFerie = lagUtsettelse(UttakPeriodeType.FELLESPERIODE, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(14).minusDays(1));
        OppgittPeriode fp = lagPeriode(UttakPeriodeType.FELLESPERIODE, fødselsdato.plusWeeks(14), fødselsdato.plusWeeks(24).minusDays(1));
        OppgittFordeling oppgittFordeling = new OppgittFordelingEntitet(Arrays.asList(fpff, mk, utsettelsePgaFerie, fp), true);


        //Føder en uke før termin
        LocalDate justertFødselsdato = fødselsdato.plusWeeks(1);
        OppgittFordeling justertFordeling = justerPeriodeHelper.juster(oppgittFordeling, fødselsdato, justertFødselsdato);

        //Periodene skal flyttes 1 uke, utsettelse skal være uendret og fpff skal avkortes.
        assertThat(likePerioder(oppgittFordeling, justertFordeling)).isFalse();
        assertThat(justertFordeling.getOppgittePerioder()).hasSize(6);

        OppgittPeriode ekstraFp = justertFordeling.getOppgittePerioder().get(0);
        assertThat(ekstraFp.getPeriodeType()).isEqualTo(UttakPeriodeType.FELLESPERIODE);
        assertThat(ekstraFp.getFom()).isEqualTo(fpff.getFom());
        assertThat(ekstraFp.getTom()).isEqualTo(fpff.getFom().plusWeeks(1).minusDays(1));


        OppgittPeriode justertFpff = justertFordeling.getOppgittePerioder().get(1);
        assertThat(justertFpff.getPeriodeType()).isEqualTo(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL);
        assertThat(justertFpff.getFom()).isEqualTo(fpff.getFom().plusWeeks(1));
        assertThat(justertFpff.getTom()).isEqualTo(justertFødselsdato.minusDays(1));

        OppgittPeriode justertMkFørUtsettelse = justertFordeling.getOppgittePerioder().get(2);
        assertThat(justertMkFørUtsettelse.getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertMkFørUtsettelse.getFom()).isEqualTo(justertFødselsdato);
        assertThat(justertMkFørUtsettelse.getTom()).isEqualTo(justertFødselsdato.plusWeeks(9).minusDays(1));

        OppgittPeriode uendretUtsettelse = justertFordeling.getOppgittePerioder().get(3);
        assertThat(uendretUtsettelse.getÅrsak()).isEqualTo(UtsettelseÅrsak.FERIE);
        assertThat(uendretUtsettelse.getFom()).isEqualTo(utsettelsePgaFerie.getFom());
        assertThat(uendretUtsettelse.getTom()).isEqualTo(utsettelsePgaFerie.getTom());

        OppgittPeriode justertMkEtterUtsettelse = justertFordeling.getOppgittePerioder().get(4);
        assertThat(justertMkEtterUtsettelse.getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE);
        assertThat(justertMkEtterUtsettelse.getFom()).isEqualTo(fødselsdato.plusWeeks(14));
        assertThat(justertMkEtterUtsettelse.getTom()).isEqualTo(fødselsdato.plusWeeks(15).minusDays(1));

        OppgittPeriode justertFp = justertFordeling.getOppgittePerioder().get(5);
        assertThat(justertFp.getPeriodeType()).isEqualTo(UttakPeriodeType.FELLESPERIODE);
        assertThat(justertFp.getFom()).isEqualTo(fødselsdato.plusWeeks(14+1));
        assertThat(justertFp.getTom()).isEqualTo(fp.getTom());
    }

    private OppgittPeriode lagPeriode(UttakPeriodeType uttakPeriodeType, LocalDate fom, LocalDate tom) {
        return OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(uttakPeriodeType)
            .build();
    }

    private OppgittPeriode lagUtsettelse(UttakPeriodeType uttakPeriodeType, LocalDate fom, LocalDate tom) {
        return OppgittPeriodeBuilder.ny()
            .medPeriode(fom, tom)
            .medPeriodeType(uttakPeriodeType)
            .medÅrsak(UtsettelseÅrsak.FERIE)
            .build();
    }

    /**
     * Sammenligning av eksakt match på perioder(sammenligner bare fom-tom). Vanlig equals har "fuzzy" logikk rundt helg, så den kan ikke brukes i dette tilfellet.
     */
    private boolean likePerioder(OppgittFordeling oppgittFordeling1, OppgittFordeling oppgittFordeling2) {
        if (oppgittFordeling1.getOppgittePerioder().size()!=oppgittFordeling2.getOppgittePerioder().size()) {
            return false;
        }
        for (int i = 0; i<oppgittFordeling1.getOppgittePerioder().size(); i++) {
            OppgittPeriode oppgittPeriode1 = oppgittFordeling1.getOppgittePerioder().get(i);
            OppgittPeriode oppgittPeriode2 = oppgittFordeling2.getOppgittePerioder().get(i);
            if (!oppgittPeriode1.getFom().equals(oppgittPeriode2.getFom()) || !oppgittPeriode1.getTom().equals(oppgittPeriode2.getTom())) {
                return false;
            }
        }
        return true;
    }

}
