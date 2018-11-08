package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.sortering;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class LovhjemmelComparator implements Comparator<String>, Serializable {
    private static final long serialVersionUID = 7736396250271405587L;

    public int compare(String hjemmel1, String hjemmel2) {
        String[] lovOgParagraf1 = hjemmel1.split("-");
        String[] lovOgParagraf2 = hjemmel2.split("-");
        if (!gyldigLovOgParagraf(lovOgParagraf1)) {
            return -1;
        } else if (!gyldigLovOgParagraf(lovOgParagraf2)) {
            return 1;
        }
        int lovResultat = Integer.compare(Integer.parseInt(lovOgParagraf1[0]), Integer.parseInt(lovOgParagraf2[0]));
        if (lovResultat == 0) {
            return Integer.compare(Integer.parseInt(lovOgParagraf1[1]), Integer.parseInt(lovOgParagraf2[1]));
        } else {
            return lovResultat;
        }
    }

    private boolean gyldigLovOgParagraf(String... hjemmel) {
        return hjemmel.length == 2 && Arrays.stream(hjemmel).allMatch(s -> s.matches("-?\\d+"));
    }
}
