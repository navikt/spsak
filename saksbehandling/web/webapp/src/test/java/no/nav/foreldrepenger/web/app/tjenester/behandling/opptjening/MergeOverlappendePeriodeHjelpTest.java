package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;

public class MergeOverlappendePeriodeHjelpTest {

    @Test
    public void skal_slå_sammen_perioder_riktig_2_godkjent_og_2_underkjent() {
        LocalDate iDag = LocalDate.now();
        OpptjeningAktivitet akt1 = new OpptjeningAktivitet(iDag.minusMonths(10), iDag, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt2 = new OpptjeningAktivitet(iDag.minusMonths(6), iDag.plusMonths(4), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt3 = new OpptjeningAktivitet(iDag.minusMonths(4), iDag.minusMonths(2), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        OpptjeningAktivitet akt4 = new OpptjeningAktivitet(iDag.plusMonths(5), iDag.plusMonths(6), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);

        List<FastsattOpptjeningAktivitetDto> aktiviteter = MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(asList(akt1, akt2, akt3, akt4));

        assertThat(aktiviteter).hasSize(2);
        FastsattOpptjeningAktivitetDto godkjent = aktiviteter.get(0);
        FastsattOpptjeningAktivitetDto underkjent = aktiviteter.get(1);

        assertThat(godkjent.getFom()).isEqualTo(iDag.minusMonths(10));
        assertThat(godkjent.getTom()).isEqualTo(iDag.plusMonths(4));
        assertThat(underkjent.getFom()).isEqualTo(iDag.plusMonths(5));
        assertThat(underkjent.getTom()).isEqualTo(iDag.plusMonths(6));
    }

    @Test
    public void skal_slå_sammen_perioder_riktig_3_godkjente_og_1_mellomliggende() {
        LocalDate iDag = LocalDate.now();
        OpptjeningAktivitet akt1 = new OpptjeningAktivitet(iDag.minusMonths(10), iDag, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt3 = new OpptjeningAktivitet(iDag.minusMonths(10), iDag.minusMonths(5), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt4 = new OpptjeningAktivitet(iDag.minusMonths(4), iDag, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt5 = new OpptjeningAktivitet(iDag.minusMonths(5), iDag.minusMonths(4), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE);

        List<FastsattOpptjeningAktivitetDto> aktiviteter = MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(asList(akt1, akt3, akt4, akt5));

        assertThat(aktiviteter).hasSize(1);
        FastsattOpptjeningAktivitetDto godkjent = aktiviteter.get(0);

        assertThat(godkjent.getFom()).isEqualTo(iDag.minusMonths(10));
        assertThat(godkjent.getTom()).isEqualTo(iDag);
    }

    @Test
    public void skal_håndtere_perioder_som_ikke_henger_sammen() {
        LocalDate iDag = LocalDate.now();
        OpptjeningAktivitet akt1 = new OpptjeningAktivitet(iDag.minusMonths(3), iDag, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt3 = new OpptjeningAktivitet(iDag.minusMonths(10), iDag.minusMonths(8), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt4 = new OpptjeningAktivitet(iDag.minusMonths(4), iDag.minusMonths(2), OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);

        List<FastsattOpptjeningAktivitetDto> aktiviteter = MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(asList(akt1, akt3, akt4));
        assertThat(aktiviteter).hasSize(2);

        FastsattOpptjeningAktivitetDto godkjent1 = aktiviteter.get(0);
        FastsattOpptjeningAktivitetDto godkjent2 = aktiviteter.get(1);

        assertThat(godkjent1.getFom()).isEqualTo(iDag.minusMonths(10));
        assertThat(godkjent1.getTom()).isEqualTo(iDag.minusMonths(8));
        assertThat(godkjent2.getFom()).isEqualTo(iDag.minusMonths(4));
        assertThat(godkjent2.getTom()).isEqualTo(iDag);
    }

    @Test
    public void skal_slå_sammen_perioder_riktig_3_godkjente_og_1_mellomliggende_der_mellomliggende_blir_avkortet() {
        LocalDate iDag = LocalDate.now();
        OpptjeningAktivitet akt1 = new OpptjeningAktivitet(iDag.minusMonths(4), iDag.minusMonths(2), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt2 = new OpptjeningAktivitet(iDag.minusMonths(2), iDag, OpptjeningAktivitetType.MILITÆR_ELLER_SIVILTJENESTE, OpptjeningAktivitetKlassifisering.MELLOMLIGGENDE_PERIODE);
        OpptjeningAktivitet akt3 = new OpptjeningAktivitet(iDag.minusMonths(1), iDag.plusMonths(1), OpptjeningAktivitetType.ARBEIDSAVKLARING, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);

        List<FastsattOpptjeningAktivitetDto> aktiviteter = MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(asList(akt1, akt3, akt2));
        assertThat(aktiviteter).hasSize(3);
        FastsattOpptjeningAktivitetDto godkjent1 = aktiviteter.get(0);
        FastsattOpptjeningAktivitetDto mellom1 = aktiviteter.get(1);
        FastsattOpptjeningAktivitetDto godkjent2 = aktiviteter.get(2);

        assertThat(godkjent1.getFom()).isEqualTo(iDag.minusMonths(4));
        assertThat(godkjent1.getTom()).isEqualTo(iDag.minusMonths(2));
        assertThat(mellom1.getFom()).isEqualTo(iDag.minusMonths(2).plusDays(1));
        assertThat(mellom1.getTom()).isEqualTo(iDag.minusMonths(1).minusDays(1));
        assertThat(godkjent2.getFom()).isEqualTo(iDag.minusMonths(1));
        assertThat(godkjent2.getTom()).isEqualTo(iDag.plusMonths(1));
    }

    @Test
    public void skal_bevare_bekreftet_avvist_som_ikke_overlapper() {
        OpptjeningAktivitet akt1 = new OpptjeningAktivitet(LocalDate.of(2017,8, 9),
            LocalDate.of(2018,4, 30), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        OpptjeningAktivitet akt2 = new OpptjeningAktivitet(LocalDate.of(2018,3, 5),
            LocalDate.of(2018,3, 21), OpptjeningAktivitetType.SVANGERSKAPSPENGER, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt3 = new OpptjeningAktivitet(LocalDate.of(2018,3, 22),
            LocalDate.of(2018,4, 6), OpptjeningAktivitetType.SVANGERSKAPSPENGER, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt4 = new OpptjeningAktivitet(LocalDate.of(2018,4, 7),
            LocalDate.of(2018,6, 8), OpptjeningAktivitetType.SVANGERSKAPSPENGER, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt5 = new OpptjeningAktivitet(LocalDate.of(2018,5, 1),
            LocalDate.of(2018,6, 8), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetKlassifisering.ANTATT_GODKJENT);

        List<FastsattOpptjeningAktivitetDto> aktiviteter = MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(asList(akt1, akt2, akt3, akt4, akt5));
        assertThat(aktiviteter).hasSize(2);
        FastsattOpptjeningAktivitetDto underkjent = aktiviteter.get(0);
        FastsattOpptjeningAktivitetDto godkjent = aktiviteter.get(1);

        assertThat(underkjent.getFom()).isEqualTo(LocalDate.of(2017, 8, 9));
        assertThat(underkjent.getTom()).isEqualTo(LocalDate.of(2018, 3, 4));
        assertThat(godkjent.getFom()).isEqualTo(LocalDate.of(2018, 3, 5));
        assertThat(godkjent.getTom()).isEqualTo(LocalDate.of(2018, 6, 8));
    }

    @Test
    public void skal_bevare_bekreftet_avvist_som_ikke_overlapper_når_det_ligger_mellom_godkjent_periode() {
        OpptjeningAktivitet akt1 = new OpptjeningAktivitet(LocalDate.of(2018,1, 1),
            LocalDate.of(2018,2, 1), OpptjeningAktivitetType.ARBEID, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt2 = new OpptjeningAktivitet(LocalDate.of(2018,4, 1),
            LocalDate.of(2018,5, 1), OpptjeningAktivitetType.SVANGERSKAPSPENGER, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt3 = new OpptjeningAktivitet(LocalDate.of(2018,7, 1),
            LocalDate.of(2018,8, 1), OpptjeningAktivitetType.SVANGERSKAPSPENGER, OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        OpptjeningAktivitet akt4 = new OpptjeningAktivitet(LocalDate.of(2017,12, 1),
            LocalDate.of(2018,9, 1), OpptjeningAktivitetType.SVANGERSKAPSPENGER, OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);

        List<FastsattOpptjeningAktivitetDto> aktiviteter = MergeOverlappendePeriodeHjelp.mergeOverlappenePerioder(asList(akt1, akt2, akt3, akt4));
        assertThat(aktiviteter).hasSize(7);
        FastsattOpptjeningAktivitetDto avvist1 = aktiviteter.get(0);
        FastsattOpptjeningAktivitetDto godkjent1 = aktiviteter.get(1);
        FastsattOpptjeningAktivitetDto avvist2 = aktiviteter.get(2);
        FastsattOpptjeningAktivitetDto godkjent2 = aktiviteter.get(3);
        FastsattOpptjeningAktivitetDto avvist3 = aktiviteter.get(4);
        FastsattOpptjeningAktivitetDto godkjent3 = aktiviteter.get(5);
        FastsattOpptjeningAktivitetDto avvist4 = aktiviteter.get(6);

        assertThat(avvist1.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        assertThat(avvist1.getFom()).isEqualTo(LocalDate.of(2017, 12, 1));
        assertThat(avvist1.getTom()).isEqualTo(LocalDate.of(2017, 12, 31));

        assertThat(godkjent1.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        assertThat(godkjent1.getFom()).isEqualTo(LocalDate.of(2018, 1, 1));
        assertThat(godkjent1.getTom()).isEqualTo(LocalDate.of(2018, 2, 1));

        assertThat(avvist2.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        assertThat(avvist2.getFom()).isEqualTo(LocalDate.of(2018, 2, 2));
        assertThat(avvist2.getTom()).isEqualTo(LocalDate.of(2018, 3, 31));

        assertThat(godkjent2.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        assertThat(godkjent2.getFom()).isEqualTo(LocalDate.of(2018, 4, 1));
        assertThat(godkjent2.getTom()).isEqualTo(LocalDate.of(2018, 5, 1));

        assertThat(avvist3.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        assertThat(avvist3.getFom()).isEqualTo(LocalDate.of(2018, 5, 2));
        assertThat(avvist3.getTom()).isEqualTo(LocalDate.of(2018, 6, 30));

        assertThat(godkjent3.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT);
        assertThat(godkjent3.getFom()).isEqualTo(LocalDate.of(2018, 7, 1));
        assertThat(godkjent3.getTom()).isEqualTo(LocalDate.of(2018, 8, 1));

        assertThat(avvist4.getKlasse()).isEqualTo(OpptjeningAktivitetKlassifisering.BEKREFTET_AVVIST);
        assertThat(avvist4.getFom()).isEqualTo(LocalDate.of(2018, 8, 2));
        assertThat(avvist4.getTom()).isEqualTo(LocalDate.of(2018, 9, 1));
    }
}
