package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingAdapter;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingTjeneste;

@Dependent
public class SakOgBehandlingTjenesteImpl implements SakOgBehandlingTjeneste {

    private SakOgBehandlingAdapter adapter;

    public SakOgBehandlingTjenesteImpl(){
        //for CDI
    }

    @Inject
    public SakOgBehandlingTjenesteImpl(SakOgBehandlingAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void behandlingOpprettet(OpprettetBehandlingStatus status) {
        adapter.behandlingOpprettet(status);
    }

    @Override
    public void behandlingAvsluttet(AvsluttetBehandlingStatus status) {
        adapter.behandlingAvsluttet(status);
    }

}
