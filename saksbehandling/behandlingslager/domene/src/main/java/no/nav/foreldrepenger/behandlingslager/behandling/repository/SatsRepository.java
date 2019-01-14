package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.SatsType;

public interface SatsRepository {

    Sats finnEksaktSats(SatsType type, LocalDate dato);

}
