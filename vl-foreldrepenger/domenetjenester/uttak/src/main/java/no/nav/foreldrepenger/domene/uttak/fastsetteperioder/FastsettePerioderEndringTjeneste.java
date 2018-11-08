package no.nav.foreldrepenger.domene.uttak.fastsetteperioder;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.UttakPeriodeEndringDto;

public interface FastsettePerioderEndringTjeneste {
    List<UttakPeriodeEndringDto> finnEndringerMellomOpprinneligOgOverstyrt(Behandling behandling);

    List<UttakPeriodeEndringDto> finnEndringerMellomOpprinneligOgOverstyrt(Behandling behandling, Long uttakResultatId);
}
