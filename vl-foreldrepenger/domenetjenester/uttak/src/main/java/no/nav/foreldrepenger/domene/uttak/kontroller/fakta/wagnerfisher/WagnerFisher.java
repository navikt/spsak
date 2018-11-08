package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasse som finner enkleste måte å transformere en streng om til en annen streng.
 * <p>
 * Den bruker Wagner-Fisher med backtracking for å finne transformasjonene som skal til.
 * <p>
 * Dette brukes til å bestemme hva som vises til beslutter ifbm to-trinnskontroll av Kontroller fakta for uttak
 * <p>
 * se https://en.wikipedia.org/wiki/Wagner%E2%80%93Fischer_algorithm
 */
public class WagnerFisher {

    private WagnerFisher() {
        // Skal ikke ha instanser av denne.
    }

    public static <T extends EditDistanceLetter> List<EditDistanceOperasjon<T>> finnEnklesteSekvens(List<T> orginal, List<T> mål) {
        int[][] cost = calculateEditDistanceCost(orginal, mål);
        return backtrack(orginal, mål, cost);
    }

    private static <T extends EditDistanceLetter> List<EditDistanceOperasjon<T>> backtrack(List<T> orginal, List<T> mål, int[][] cost) {
        int i = orginal.size();
        int j = mål.size();

        List<EditDistanceOperasjon<T>> resultat = new ArrayList<>();
        while (j > 0 && i > 0) {
            if (cost[i][j] == kostMedSettInn(cost, i, j, mål)) {
                resultat.add(new EditDistanceOperasjon<>(null, mål.get(j - 1)));
                j--;
            } else if (cost[i][j] == kostMedSlette(cost, i, j, orginal)) {
                resultat.add(new EditDistanceOperasjon<>(orginal.get(i - 1), null));
                i--;
            } else if (cost[i][j] == cost[i - 1][j - 1]) {
                j--;
                i--;
            } else if (cost[i][j] == kostMedEndring(cost, i, j, orginal, mål)) {
                resultat.add(new EditDistanceOperasjon<>(orginal.get(i - 1), mål.get(j - 1)));
                j--;
                i--;
            } else {
                throw new IllegalArgumentException("Utvikler-feil, kostnadsmatrise stemmer ikke");
            }
        }
        List<EditDistanceOperasjon<T>> sortertResultat = new ArrayList<>(resultat.size());
        for (int x = resultat.size() - 1; x >= 0; x--) {
            sortertResultat.add(resultat.get(x));
        }
        return sortertResultat;
    }

    static <T extends EditDistanceLetter> int finnKost(List<T> orginal, List<T> mål) {
        int[][] cost = calculateEditDistanceCost(orginal, mål);
        return cost[orginal.size()][mål.size()];
    }

    static <T extends EditDistanceLetter> int[][] calculateEditDistanceCost(List<T> s, List<T> t) {
        int m = s.size();
        int n = t.size();
        int[][] cost = zeroArray(m + 1, n + 1);

        for (int i = 0; i < m; i++) {
            cost[i + 1][0] = cost[i][0] + s.get(i).kostnadSlette(); //NOSONAR - konstant i oppslag er OK her
        }
        for (int i = 0; i < n; i++) {
            cost[0][i + 1] = cost[0][i] + t.get(i).kostnadSettInn(); //NOSONAR - konstant i oppslag er OK her
        }
        for (int j = 1; j <= n; j++) {
            for (int i = 1; i <= m; i++) {
                if (s.get(i - 1).lik(t.get(j - 1))) {
                    cost[i][j] = cost[i - 1][j - 1];
                } else {
                    cost[i][j] = min(
                        kostMedSlette(cost, i, j, s),
                        kostMedSettInn(cost, i, j, t),
                        kostMedEndring(cost, i, j, s, t));
                }
            }
        }
        return cost;
    }

    private static <T extends EditDistanceLetter> int kostMedEndring(int[][] cost, int i, int j, List<T> s, List<T> t) {
        return cost[i - 1][j - 1] + t.get(j - 1).kostnadEndre(s.get(i - 1));
    }

    private static <T extends EditDistanceLetter> int kostMedSlette(int[][] cost, int i, int j, List<T> s) {
        return cost[i - 1][j] + s.get(i - 1).kostnadSlette();
    }

    private static <T extends EditDistanceLetter> int kostMedSettInn(int[][] cost, int i, int j, List<T> t) {
        return cost[i][j - 1] + t.get(j - 1).kostnadSettInn();
    }

    private static int min(int... x) {
        int m = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] < m) {
                m = x[i];
            }
        }
        return m;
    }

    private static int[][] zeroArray(int sizeX, int sizeY) {
        int[][] result = new int[sizeX][];
        for (int i = 0; i < sizeX; i++) {
            result[i] = new int[sizeY];
        }
        return result;
    }

}
