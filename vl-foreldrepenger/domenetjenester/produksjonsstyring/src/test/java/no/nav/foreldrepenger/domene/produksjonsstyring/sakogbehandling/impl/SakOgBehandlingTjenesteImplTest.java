package no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.AvsluttetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.OpprettetBehandlingStatus;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.SakOgBehandlingAdapter;
import no.nav.foreldrepenger.domene.produksjonsstyring.sakogbehandling.impl.SakOgBehandlingTjenesteImpl;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SakOgBehandlingTjenesteImplTest {

    private SakOgBehandlingTjenesteImpl tjeneste; // objektet vi tester
    private SakOgBehandlingAdapter mockAdapter;

    @Before
    public void setup() {
        mockAdapter = mock(SakOgBehandlingAdapter.class);
        tjeneste = new SakOgBehandlingTjenesteImpl(mockAdapter);
    }

    @Test
    public void test_ctor0() {
        tjeneste = new SakOgBehandlingTjenesteImpl();
    }

    @Test
    public void test_behandlingOpprettet() {

        OpprettetBehandlingStatus status = new OpprettetBehandlingStatus();
        tjeneste.behandlingOpprettet(status);

        verify(mockAdapter).behandlingOpprettet(same(status));
    }

    @Test
    public void test_behandlingAvsluttet() {

        AvsluttetBehandlingStatus status = new AvsluttetBehandlingStatus();
        tjeneste.behandlingAvsluttet(status);

        verify(mockAdapter).behandlingAvsluttet(same(status));
    }
}
