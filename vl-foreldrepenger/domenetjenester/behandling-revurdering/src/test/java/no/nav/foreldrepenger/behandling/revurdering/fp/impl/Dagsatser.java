package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import static no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingBehandlingsresultatutlederTest.TOTAL_ANDEL_NORMAL;
import static no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingBehandlingsresultatutlederTest.TOTAL_ANDEL_OPPJUSTERT;

import java.math.BigDecimal;
import java.math.RoundingMode;

class Dagsatser {
    private BigDecimal dagsatsBruker;
    private BigDecimal dagsatsArbeidstaker;

    Dagsatser(boolean medOppjustertDagsat, boolean skalDeleAndelMellomArbeidsgiverOgBruker) {
        BigDecimal aktuellDagsats = medOppjustertDagsat ? TOTAL_ANDEL_OPPJUSTERT : TOTAL_ANDEL_NORMAL;
        this.dagsatsBruker = skalDeleAndelMellomArbeidsgiverOgBruker ? aktuellDagsats.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP) : aktuellDagsats;
        this.dagsatsArbeidstaker = skalDeleAndelMellomArbeidsgiverOgBruker ? aktuellDagsats.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getDagsatsBruker() {
        return dagsatsBruker;
    }

    public BigDecimal getDagsatsArbeidstaker() {
        return dagsatsArbeidstaker;
    }
}
