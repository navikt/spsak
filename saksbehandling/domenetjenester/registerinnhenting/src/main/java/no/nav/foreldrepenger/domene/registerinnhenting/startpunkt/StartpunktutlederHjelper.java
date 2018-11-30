package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;

class StartpunktutlederHjelper {

    static boolean finnesAktivitetHvorAlleHarDagsatsNull(BeregningsresultatFP beregningsresultat) {
        List<BeregningsresultatPeriode> perioder = beregningsresultat.getBeregningsresultatPerioder();
        for (BeregningsresultatPeriode periode : perioder) {
            // ArbeidforholdNøkkel er synonym for "aktivitet" i domene
            Map<Aktivitetsnøkkel, List<BeregningsresultatAndel>> andelerPerArbeidforhold = indekserAndelerMotArbeidsforhold(periode.getBeregningsresultatAndelList());
            boolean finnesArbeidsforholdHvorAlleHarDagsatsNull = andelerPerArbeidforhold.entrySet().stream()
                .anyMatch(andelerForArbeidforhold -> andelerForArbeidforhold.getValue().stream()
                    // Alle dagsatser for samme arbeidsforhold (dvs aktivtet) er 0
                    .allMatch(andelForArbeidsforhold -> andelForArbeidsforhold.getDagsats() == 0));
            if (finnesArbeidsforholdHvorAlleHarDagsatsNull) {
                return true;
            }
        }
        return false;
    }

    static private Map<Aktivitetsnøkkel, List<BeregningsresultatAndel>> indekserAndelerMotArbeidsforhold(List<BeregningsresultatAndel> andeler) {
        Map<Aktivitetsnøkkel, List<BeregningsresultatAndel>> andelerPerArbeidforhold  = new HashMap<>();
        andeler.stream().forEach(andel -> {
            Aktivitetsnøkkel nøkkel = new Aktivitetsnøkkel(andel);
            List<BeregningsresultatAndel> gruppertAndeler = andelerPerArbeidforhold.get(nøkkel);
            if (gruppertAndeler == null) {
                // Opprett
                List<BeregningsresultatAndel> nyGrupperteAndeler = new ArrayList<>();
                nyGrupperteAndeler.add(andel);
                andelerPerArbeidforhold.put(nøkkel, nyGrupperteAndeler);
            } else {
                // Oppdater
                gruppertAndeler.add(andel);
            }
        });
        return andelerPerArbeidforhold;
    }
}
