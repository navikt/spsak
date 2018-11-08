package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface KompletthetssjekkerSøknad {

    List<ManglendeVedlegg> utledManglendeVedleggForSøknad(Behandling behandling);

    Optional<LocalDateTime> erSøknadMottattForTidlig(Behandling behandling);

    Boolean erSøknadMottatt(Behandling behandling);
}
