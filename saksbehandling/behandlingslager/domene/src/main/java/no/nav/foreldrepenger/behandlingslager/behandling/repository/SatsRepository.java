package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.Sats;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.SatsType;

public interface SatsRepository {

    Sats finnEksaktSats(SatsType type, LocalDate dato);

}
