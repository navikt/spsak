package no.nav.foreldrepenger.behandling.steg.vedtak;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.vedtak.VedtakTjeneste;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlTjeneste;
import no.nav.foreldrepenger.vedtakslager.LagretVedtakRepository;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class FatteVedtakTjenesteEngangsstønadImpl extends FatteVedtakTjenesteImpl {

    protected FatteVedtakTjenesteEngangsstønadImpl() {
        // for CDI proxy
    }

    @Inject
    FatteVedtakTjenesteEngangsstønadImpl(LagretVedtakRepository vedtakRepository,
                                         @FagsakYtelseTypeRef("ES") VedtakXmlTjeneste vedtakXmlTjeneste, VedtakTjeneste vedtakTjeneste,
                                         @FagsakYtelseTypeRef("ES") RevurderingTjeneste revurderingTjeneste,
                                         OppgaveTjeneste oppgaveTjeneste, TotrinnTjeneste totrinnTjeneste, BehandlingVedtakTjeneste behandlingVedtakTjeneste) {
        super(vedtakRepository, vedtakXmlTjeneste, vedtakTjeneste,
            oppgaveTjeneste, totrinnTjeneste, behandlingVedtakTjeneste);
    }
}
