package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingTjeneste;

@ApplicationScoped
class SakOgBehandlingTjenesteImpl implements SakOgBehandlingTjeneste {

    protected final Logger log = LoggerFactory.getLogger(SakOgBehandlingTjenesteImpl.class);

    public SakOgBehandlingTjenesteImpl() {

    }

    @Override
    public void behandlingOpprettet(OpprettetBehandlingStatus opprettetBehandlingStatus) {
        log.warn("Skulle sendt BehandlingOpprettet til Sak&Behandling for behandling: " + opprettetBehandlingStatus.getBehandlingsId());
    }

    @Override
    public void behandlingAvsluttet(AvsluttetBehandlingStatus avsluttetBehandlingStatus) {
        log.warn("Skulle sendt BehandlingAvsluttet til Sak&Behandling for behandling: " + avsluttetBehandlingStatus.getBehandlingsId());
    }

}
