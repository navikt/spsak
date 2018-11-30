package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface InfotrygdBeregningsgrunnlagTjeneste {
    YtelsesBeregningsgrunnlag hentGrunnlagListeFull(Behandling behandling, String fnr, LocalDate fom);
}
