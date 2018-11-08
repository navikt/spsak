package no.nav.foreldrepenger.behandlingskontroll;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.TekniskRepository;
import no.nav.vedtak.felles.jpa.savepoint.Work;
import no.nav.vedtak.util.MdcExtendedLogContext;

/**
 * Tekniske oppsett ved kjøring av et steg:<br>
 * <ul>
 * <li>Setter savepoint slik at dersom steg feiler så beholdes tidligere resultater.</li>
 * <li>Setter LOG_CONTEXT slik at ytterligere detaljer blir med i logging.</li>
 * </ul>
 */
public class TekniskBehandlingStegVisitor implements BehandlingModellVisitor {

    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    private final TekniskRepository tekniskRepository;
    private final BehandlingStegVisitor stegVisitor;
    private final BehandlingskontrollKontekst kontekst;

    public TekniskBehandlingStegVisitor(BehandlingRepositoryProvider repositoryProvider, BehandlingStegVisitor stegVisitor,
                                        BehandlingskontrollKontekst kontekst) {
        this.stegVisitor = stegVisitor;
        this.kontekst = kontekst;
        this.tekniskRepository = new TekniskRepository(repositoryProvider);
    }

    @Override
    public BehandlingStegProsesseringResultat prosesser(BehandlingStegModell steg) {
        LOG_CONTEXT.add("fagsak", kontekst.getFagsakId()); // NOSONAR //$NON-NLS-1$
        LOG_CONTEXT.add("behandling", kontekst.getBehandlingId()); // NOSONAR //$NON-NLS-1$
        LOG_CONTEXT.add("steg", steg.getBehandlingStegType().getKode()); // NOSONAR //$NON-NLS-1$

        class DoInSavepoint implements Work<BehandlingStegProsesseringResultat> {
            @Override
            public BehandlingStegProsesseringResultat doWork() {
                BehandlingStegProsesseringResultat resultat = stegVisitor.prosesser(steg);
                return resultat;
            }
        }
        
        // kjøres utenfor savepoint. Ellers står vi nakne, med kun utførte steg
        stegVisitor.markerOvergangTilNyttSteg(steg.getBehandlingStegType());
        
        BehandlingStegProsesseringResultat resultat = tekniskRepository.doWorkInSavepoint(new DoInSavepoint());

        /*
         * NB: nullstiller her og ikke i finally block, siden det da fjernes før vi får logget det.
         * Hele settet fjernes så i MDCFilter eller tilsvarende uansett. Steg er del av koden så fanges uansett i
         * stacktrace men trengs her for å kunne ta med i log eks. på DEBUG/INFO/WARN nivå.
         * 
         * behandling og fagsak kan være satt utenfor, så nullstiller ikke de i log context her
         */
        LOG_CONTEXT.remove("steg"); // NOSONAR //$NON-NLS-1$

        return resultat;
    }
}
