package no.nav.foreldrepenger.datavarehus.tjeneste;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.datavarehus.VedtakUtbetalingDvh;
import no.nav.vedtak.util.FPDateUtil;

class VedtakUtbetalingDvhMapper {

    public VedtakUtbetalingDvh map(String xmlClob, Behandling behandling, BehandlingVedtak behandlingVedtak) {
        return VedtakUtbetalingDvh.builder()
            .xmlClob(xmlClob)
            .fagsakId(behandling.getFagsakId())
            .behandlingId(behandling.getId())
            .vedtakId(behandlingVedtak.getId())
            .vedtakDato(behandlingVedtak.getVedtaksdato())
            .behandlingType(behandling.getType().getOffisiellKode())
            .fagsakType(behandling.getFagsakYtelseType().getKode())
            .funksjonellTid(FPDateUtil.nå())
            .endretAv(CommonDvhMapper.finnEndretAvEllerOpprettetAv(behandlingVedtak))
            .build();
    }
}
