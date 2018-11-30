package no.nav.foreldrepenger.domene.beregningsgrunnlag.wrapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class IdentifisertePeriodeÅrsaker {

    private SortedMap<LocalDate, Set<PeriodeSplittData>> periodeMap = new TreeMap<>();

    public IdentifisertePeriodeÅrsaker() {
        //tom constructor
    }

    public Map<LocalDate, Set<PeriodeSplittData>> getPeriodeMap() {
        return Collections.unmodifiableMap(periodeMap);
    }

    public void leggTilPeriodeÅrsak(LocalDate dato, PeriodeSplittData splittData) {
        if (periodeMap.containsKey(dato)) {
            Set<PeriodeSplittData> data = periodeMap.get(dato);
            data.add(splittData);
        } else {
            Set<PeriodeSplittData> set = new HashSet<>();
            set.add(splittData);
            periodeMap.put(dato, set);
        }
    }
}
