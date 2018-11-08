package no.nav.foreldrepenger.økonomistøtte;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;

class OppdragskontrollFeriepengerTestUtil extends OppdragskontrollTjenesteImplFPBaseTest {

    void verifiserOpp150NårEndringGjelderEttFeriepengeår(List<Oppdragslinje150> opp150FeriepengerListe, List<Oppdragslinje150> opp150RevurderingFeriepengerListe,
                                                         boolean erEndringForFørsteÅr) {
        int endretFeriepengeår = erEndringForFørsteÅr ? feriepengeårListe.get(0) : feriepengeårListe.get(1);
        for (Oppdragslinje150 forrigeOpp150 : opp150FeriepengerListe) {
            if (forrigeOpp150.getDatoVedtakFom().getYear() == endretFeriepengeår) {
                assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
                    assertThat(oppdragslinje150.getRefDelytelseId()).isEqualTo(forrigeOpp150.getDelytelseId()));
            }
        }
        assertThat(opp150RevurderingFeriepengerListe).allSatisfy(opp150 -> assertThat(opp150.gjelderOpphør()).isFalse());
        int ix = erEndringForFørsteÅr ? 1 : 0;
        int ikkeEndretFeriepengeår = feriepengeårListe.get(ix);
        Optional<Oppdragslinje150> opp150RevurdFørsteFeriepengeår = opp150RevurderingFeriepengerListe.stream()
            .filter(oppdragslinje150 -> oppdragslinje150.getDatoVedtakFom().getYear() == ikkeEndretFeriepengeår).findFirst();
        assertThat(opp150RevurdFørsteFeriepengeår).isNotPresent();
    }

    void verifiserOppdr150MedEttFeriepengeårKunIRevurdering(List<Oppdragslinje150> opp150RevurderingFeriepengerListe, boolean gjelderFørsteÅr) {
        int åretSomSkalHaFeriepenger = gjelderFørsteÅr ? feriepengeårListe.get(0) : feriepengeårListe.get(1);
        int ix = gjelderFørsteÅr ? 1 : 0;
        int åretSomIkkeSkalHaFeriepenger = feriepengeårListe.get(ix);
        assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
            assertThat(oppdragslinje150.getDatoVedtakFom().getYear()).isEqualTo(åretSomSkalHaFeriepenger));
        assertThat(opp150RevurderingFeriepengerListe).allSatisfy(oppdragslinje150 ->
            assertThat(oppdragslinje150.getDatoVedtakFom().getYear()).isNotEqualTo(åretSomIkkeSkalHaFeriepenger));
    }

    void verifiserOppdr150NårDetIkkeErFeriepengerIRevurdering(Oppdragskontroll forrigeOppdrag, Oppdragskontroll oppdragRevurdering, boolean erOpphørPåFørsteÅr) {
        List<Oppdragslinje150> opp150FeriepengerListe = getOppdragslinje150Feriepenger(forrigeOppdrag);
        assertThat(opp150FeriepengerListe).allSatisfy(oppdragslinje150 -> {
            assertThat(oppdragslinje150.getRefDelytelseId()).isNull();
            assertThat(oppdragslinje150.getRefFagsystemId()).isNull();
        });
        List<Oppdragslinje150> opp150RevurdFeriepengerListe = getOppdragslinje150Feriepenger(oppdragRevurdering);
        int ix = erOpphørPåFørsteÅr ? 0 : 1;
        int feriepengeår = feriepengeårListe.get(ix);
        assertThat(opp150RevurdFeriepengerListe).allSatisfy(oppdragslinje150 -> {
                assertThat(oppdragslinje150.gjelderOpphør()).isTrue();
                assertThat(oppdragslinje150.getDatoVedtakFom().getYear()).isEqualTo(feriepengeår);
            }
        );
    }

    void verifiserOppdr150NårEttFeriepengeårSkalOpphøre(List<Oppdragslinje150> opp150FeriepengerListe, List<Oppdragslinje150> opp150RevurderingFeriepengerListe,
                                                        boolean skalFørsteFPÅrOpphøre) {
        int opphørtFeriepengeår = skalFørsteFPÅrOpphøre ? feriepengeårListe.get(0) : feriepengeårListe.get(1);
        for (Oppdragslinje150 forrigeOpp150 : opp150FeriepengerListe) {
            if (forrigeOpp150.getDatoVedtakFom().getYear() == opphørtFeriepengeår) {
                assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
                    assertThat(oppdragslinje150.getDelytelseId()).isEqualTo(forrigeOpp150.getDelytelseId()));
            } else {
                assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
                    assertThat(oppdragslinje150.getRefDelytelseId()).isEqualTo(forrigeOpp150.getDelytelseId()));
            }
        }
    }

    void verifiserOpp150NårEttFPÅretOpphørerOgAndreIkkeEndrerSeg(List<Oppdragslinje150> opp150FeriepengerListe, List<Oppdragslinje150> opp150RevurderingFeriepengerListe,
                                                                 boolean skalFørsteOpphøre) {
        int oppphørtFeriepengeår = skalFørsteOpphøre ? feriepengeårListe.get(0) : feriepengeårListe.get(1);
        int ix = skalFørsteOpphøre ? 1 : 0;
        int feriepengeåretUtenEndring = feriepengeårListe.get(ix);
        assertThat(opp150RevurderingFeriepengerListe).anySatisfy(opp150 -> {
            assertThat(opp150.gjelderOpphør()).isTrue();
            assertThat(opp150.getDatoVedtakFom().getYear()).isEqualTo(oppphørtFeriepengeår);
        });
        assertThat(opp150RevurderingFeriepengerListe).allSatisfy(opp150 ->
            assertThat(opp150.getDatoVedtakFom().getYear()).isNotEqualTo(feriepengeåretUtenEndring));
        for (Oppdragslinje150 forrigeOpp150 : opp150FeriepengerListe) {
            if (forrigeOpp150.getDatoVedtakFom().getYear() == oppphørtFeriepengeår) {
                assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
                    assertThat(oppdragslinje150.getDelytelseId()).isEqualTo(forrigeOpp150.getDelytelseId()));
            }
        }
    }

    void verifiserOppdr150NårDetErEndringForToFeriepengeår(List<Oppdragslinje150> forrigeOpp150FeriepengerListe, List<Oppdragslinje150> opp150RevurderingFeriepengerListe) {
        assertThat(opp150RevurderingFeriepengerListe).allSatisfy(opp150 -> {
            if (forrigeOpp150FeriepengerListe.isEmpty()) {
                assertThat(opp150.getRefDelytelseId()).isNull();
                assertThat(opp150.getRefFagsystemId()).isNull();
            } else {
                assertThat(opp150.getRefDelytelseId()).isNotNull();
                assertThat(opp150.getRefFagsystemId()).isNotNull();
            }
            assertThat(opp150.gjelderOpphør()).isFalse();
        });
    }

    void verifiserRefDelytelseId(List<Oppdragslinje150> opp150FeriepengerListe, List<Oppdragslinje150> opp150RevurderingFeriepengerListe, boolean skalFørsteÅrKjedes) {
        int kjedetFeriepengeår = skalFørsteÅrKjedes ? feriepengeårListe.get(0) : feriepengeårListe.get(1);
        int ix = skalFørsteÅrKjedes ? 1 : 0;
        int ikkeKjedetFeriepengeår = feriepengeårListe.get(ix);

        for (Oppdragslinje150 forrigeOpp150 : opp150FeriepengerListe) {
            assertThat(forrigeOpp150.getDatoVedtakFom().getYear()).isNotEqualTo(ikkeKjedetFeriepengeår);
            if (forrigeOpp150.getDatoVedtakFom().getYear() == kjedetFeriepengeår) {
                assertThat(opp150RevurderingFeriepengerListe).anySatisfy(oppdragslinje150 ->
                    assertThat(oppdragslinje150.getRefDelytelseId()).isEqualTo(forrigeOpp150.getDelytelseId()));
            }
        }
    }

    void verifiserFeriepengeår(List<Oppdragslinje150> opp150FeriepengerListe) {
        feriepengeårListe.forEach(feriepengeår ->
            assertThat(opp150FeriepengerListe).anySatisfy(oppdragslinje150 ->
                assertThat(oppdragslinje150.getDatoVedtakFom().getYear()).isEqualTo(feriepengeår)));
    }
}
