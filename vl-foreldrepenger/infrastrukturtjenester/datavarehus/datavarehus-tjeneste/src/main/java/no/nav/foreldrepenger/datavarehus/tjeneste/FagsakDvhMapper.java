package no.nav.foreldrepenger.datavarehus.tjeneste;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.datavarehus.FagsakDvh;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.util.FPDateUtil;

public class FagsakDvhMapper {

    public FagsakDvh map(Fagsak fagsak, Optional<AktørId> annenPartAktørId, Optional<FamilieHendelseType> hendelseType) {
        return FagsakDvh.builder()
            .fagsakId(fagsak.getId())
            .brukerId(fagsak.getNavBruker().getId())
            .brukerAktørId(fagsak.getAktørId().getId())
            .epsAktørId(annenPartAktørId.map(AktørId::getId))
            .opprettetDato(fagsak.getOpprettetTidspunkt().toLocalDate())
            .funksjonellTid(FPDateUtil.nå())
            .endretAv(CommonDvhMapper.finnEndretAvEllerOpprettetAv(fagsak))
            .fagsakStatus(fagsak.getStatus().getKode())
            .fagsakYtelse(fagsak.getYtelseType().getKode())
            .fagsakAarsak(hendelseType.map(Kodeliste::getKode).orElse(FamilieHendelseType.UDEFINERT.getKode()))
            .saksnummer(fagsak.getSaksnummer() != null ? fagsak.getSaksnummer().getVerdi() : null)
            .build();
    }

}
