package no.nav.foreldrepenger.domene.mottak.hendelser;

import no.nav.foreldrepenger.domene.mottak.hendelser.kontrakt.ForretningshendelseDto;

public interface ForretningshendelseMottak {

    void mottaForretningshendelse(ForretningshendelseDto forretningshendelse);

    void håndterHendelsePåFagsak(Long fagsakId, String hendelseTypeKode);
}
