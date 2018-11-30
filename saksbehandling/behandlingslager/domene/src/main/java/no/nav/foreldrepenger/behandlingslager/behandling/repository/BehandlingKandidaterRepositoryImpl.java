package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.ReaktiveringStatus;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Ulike spesialmetoder for å hente opp behandlinger som er kandidater for videre spesiell prosessering, slik som
 * etterkontroll gjenopptagelse av behandlinger på vent og lignende.
 * <p>
 * Disse vil bil brukt i en trigging av videre prosessering, behandling, kontroll, evt. henlegging eller avslutting.
 */

@ApplicationScoped
public class BehandlingKandidaterRepositoryImpl implements BehandlingKandidaterRepository {

    private EntityManager entityManager;

    BehandlingKandidaterRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingKandidaterRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public List<Behandling> finnBehandlingerMedUtløptBehandlingsfrist() {
        Set<BehandlingType> behandlingTyperMedVarselBrev = hentBehandlingTyperMedBehandlingstidVarselBrev();

        TypedQuery<Behandling> query = entityManager.createQuery(
            "SELECT behandling FROM Behandling behandling " +
                "WHERE NOT (behandling_status = 'AVSLU') " +
                "AND behandlingstid_frist < :idag " +
                "AND behandling.behandlingType in (:list)", //$NON-NLS-1$
            Behandling.class);

        query.setParameter("idag", LocalDate.now(FPDateUtil.getOffset())); //$NON-NLS-1$
        query.setParameter("list", behandlingTyperMedVarselBrev);
        query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        return query.getResultList();
    }

    private Set<BehandlingType> hentBehandlingTyperMedBehandlingstidVarselBrev() {
        TypedQuery<BehandlingType> query = getEntityManager().createQuery(
            "select bt from BehandlingType bt", BehandlingType.class);
        List<BehandlingType> resultList = query.getResultList();
        return resultList.stream()
            .filter(BehandlingType::isBehandlingstidVarselbrev)
            .collect(Collectors.toSet());
    }

    @Override
    public List<Behandling> finnBehandlingerForAutomatiskGjenopptagelse() {

        List<String> aapneAksjonspunktKoder = AksjonspunktStatus.getÅpneAksjonspunktKoder();
        String autopunktKode = AksjonspunktType.AUTOPUNKT.getKode();
        String køetKode = AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING.getKode();
        String reaktivertKode = ReaktiveringStatus.AKTIV.getKode();
        LocalDateTime naa = LocalDateTime.now(FPDateUtil.getOffset());

        TypedQuery<Behandling> query = getEntityManager().createQuery(
            " SELECT DISTINCT b " +
                " FROM Aksjonspunkt ap " +
                " INNER JOIN ap.behandling b " +
                " WHERE " +
                " ap.status.kode IN :aapneAksjonspunktKoder AND " +
                " ap.reaktiveringStatus.kode = :reaktiveringStatus AND " +
                " ap.aksjonspunktDefinisjon.aksjonspunktType.kode = :autopunktKode AND " +
                " ap.aksjonspunktDefinisjon.kode != :køetKode AND " +
                " ap.fristTid < :naa ",
            Behandling.class);
        query.setHint(QueryHints.HINT_READONLY, "true");
        query.setParameter("aapneAksjonspunktKoder", aapneAksjonspunktKoder);
        query.setParameter("reaktiveringStatus", reaktivertKode);
        query.setParameter("autopunktKode", autopunktKode);
        query.setParameter("køetKode", køetKode);
        query.setParameter("naa", naa);

        return query.getResultList();
    }
}
