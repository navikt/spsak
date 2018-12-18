package no.nav.foreldrepenger.mottak.klient;

import java.util.Optional;

import no.nav.foreldrepenger.kontrakter.fordel.*;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;

public interface FagsakRestKlient {
    Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto);

    SaksnummerDto opprettSak(OpprettSakDto opprettSakDto);

    void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto);

    VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper dataWrapper);
}
