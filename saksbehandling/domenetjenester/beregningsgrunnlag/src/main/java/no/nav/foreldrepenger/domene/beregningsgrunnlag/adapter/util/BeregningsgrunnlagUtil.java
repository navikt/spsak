package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util;

import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.ARBEIDSTAKER;
import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.FRILANSER;
import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.KOMBINERT_AT_FL;
import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.KOMBINERT_AT_FL_SN;
import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.KOMBINERT_AT_SN;
import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.KOMBINERT_FL_SN;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;

public final class BeregningsgrunnlagUtil {
    public static final List<AktivitetStatus> ATFL_STATUSER =
        Collections.unmodifiableList(Arrays.asList(ARBEIDSTAKER, FRILANSER, KOMBINERT_AT_FL));
    public static final List<no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus> ATFL_SN_STATUSER =
        Collections.unmodifiableList(Arrays.asList(KOMBINERT_AT_SN, KOMBINERT_FL_SN, KOMBINERT_AT_FL_SN));

    private BeregningsgrunnlagUtil() {
    }

    public static Long nullSafeLong(Long input) {
        if (input != null) {
            return input;
        }
        return 0L;
    }
}
