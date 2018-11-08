package no.nav.foreldrepenger.økonomistøtte;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.økonomistøtte.api.SimulerOppdragApplikasjonTjeneste;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class SimulerOppdragApplikasjonTjenesteImpl implements SimulerOppdragApplikasjonTjeneste {

    private OppdragskontrollTjeneste oppdragskontrollTjeneste;

    private static final Logger log = LoggerFactory.getLogger(SimulerOppdragApplikasjonTjenesteImpl.class);

    SimulerOppdragApplikasjonTjenesteImpl() {
        // for CDI
    }

    @Inject
    public SimulerOppdragApplikasjonTjenesteImpl(OppdragskontrollTjeneste oppdragskontrollTjeneste) {
        this.oppdragskontrollTjeneste = oppdragskontrollTjeneste;
    }

    @Override
    public List<String> simulerOppdrag(Long behandlingId, Long ventendeTaskId) {
        log.info("Oppretter simuleringsoppdrag for behandling: {}", behandlingId); //$NON-NLS-1$

        Long oppdragskontrollId = oppdragskontrollTjeneste.opprettOppdragSimulering(behandlingId, ventendeTaskId);

        Oppdragskontroll oppdrag = oppdragskontrollTjeneste.hentOppdragskontroll(oppdragskontrollId);
        ØkonomioppdragMapper mapper = new ØkonomioppdragMapper(oppdrag);
        List<String> oppdragXMLListe = mapper.generateOppdragXML();
        return oppdragXMLListe;
    }
}
