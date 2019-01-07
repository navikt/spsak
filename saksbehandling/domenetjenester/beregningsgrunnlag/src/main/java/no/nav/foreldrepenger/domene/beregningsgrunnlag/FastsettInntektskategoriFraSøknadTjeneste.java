package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;

public interface FastsettInntektskategoriFraSøknadTjeneste {

    void fastsettInntektskategori(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling);

    Optional<Inntektskategori> finnHøgastPrioriterteInntektskategoriForSN(List<Inntektskategori> inntektskategorier);
}
