package no.nav.foreldrepenger.vedtak.xml.oppdrag;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;
import no.nav.vedtak.felles.xml.vedtak.oppdrag.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.oppdrag.v2.Oppdrag;
import no.nav.vedtak.felles.xml.vedtak.v2.Vedtak;

@ApplicationScoped
public class OppdragXmlTjeneste {
    private ObjectFactory oppdragObjectFactory;
    private ØkonomioppdragRepository økonomioppdragRepository;

    public OppdragXmlTjeneste() {
        //CDI
    }

    @Inject
    public OppdragXmlTjeneste(ØkonomioppdragRepository økonomioppdragRepository) {
        this.økonomioppdragRepository = økonomioppdragRepository;
        oppdragObjectFactory = new ObjectFactory();
    }

    public void setOppdrag(Vedtak vedtak, Behandling behandling) {
        Optional<Oppdragskontroll> oppdragskontroll = økonomioppdragRepository.finnOppdragForBehandling(behandling.getId());
        if (oppdragskontroll.isPresent()) {
            Oppdrag oppdrag = oppdragObjectFactory.createOppdrag();
            Oppdrag110 oppdrag110 = oppdragskontroll.get().getOppdrag110Liste().get(0);
            oppdrag.setOppdragId(VedtakXmlUtil.lagStringOpplysning(oppdrag110.getId().toString()));
            oppdrag.setFagsystemId(VedtakXmlUtil.lagLongOpplysning(oppdrag110.getFagsystemId()));
            Oppdragslinje150 oppdragslinje150 = oppdrag110.getOppdragslinje150Liste().get(0);
            oppdrag.setLinjeId(VedtakXmlUtil.lagStringOpplysning(oppdragslinje150.getId().toString()));
            oppdrag.setDelytelseId(VedtakXmlUtil.lagStringOpplysning(oppdragslinje150.getDelytelseId().toString()));

            vedtak.setOppdrag(oppdrag);
        }
    }
}
