package no.nav.foreldrepenger.datavarehus.tjeneste;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.datavarehus.FagsakDvh;
import no.nav.vedtak.util.FPDateUtil;

public class FagsakDvhMapper {

    public FagsakDvh map(Fagsak fagsak) {
        return FagsakDvh.builder()
            .fagsakId(fagsak.getId())
            .brukerId(fagsak.getNavBruker().getId())
            .brukerAktørId(fagsak.getAktørId().getId())
            .opprettetDato(fagsak.getOpprettetTidspunkt().toLocalDate())
            .funksjonellTid(FPDateUtil.nå())
            .endretAv(CommonDvhMapper.finnEndretAvEllerOpprettetAv(fagsak))
            .fagsakStatus(fagsak.getStatus().getKode())
            .fagsakYtelse(fagsak.getYtelseType().getKode())
            .saksnummer(fagsak.getSaksnummer() != null ? fagsak.getSaksnummer().getVerdi() : null)
            .build();
    }

}
