package no.nav.foreldrepenger.behandlingslager.behandling.etterkontroll;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class BehandlingEtterkontrollRepositoryImpl implements BehandlingEtterkontrollRepository {

    private EntityManager entityManager;

    public BehandlingEtterkontrollRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingEtterkontrollRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public Long lagre(EtterkontrollLogg etterkontrollLogg, BehandlingLås lås) {
        getEntityManager().persist(etterkontrollLogg);
        verifiserBehandlingLås(lås);
        getEntityManager().flush();
        return etterkontrollLogg.getId();
    }

    @Override
    public List<Behandling> finnKandidaterForAutomatiskEtterkontroll(Period etterkontrollTidTilbake) {
        final Query query = getEntityManager().createNativeQuery(
            "SELECT b.fagsak_id, b.id " +
                "FROM BEHANDLING b " +
                "INNER JOIN BEHANDLING_RESULTAT br ON br.behandling_id = b.id " +
                "INNER JOIN BEHANDLING_VEDTAK bv ON bv.behandling_resultat_id = br.id " +
                "INNER JOIN GR_FAMILIE_HENDELSE fhg ON fhg.behandling_id = b.id " +
                "LEFT JOIN FH_FAMILIE_HENDELSE bfh ON fhg.bekreftet_familie_hendelse_id = bfh.id " +
                "LEFT JOIN FH_FAMILIE_HENDELSE ofh ON fhg.overstyrt_familie_hendelse_id = ofh.id " +
                "LEFT JOIN FH_TERMINBEKREFTELSE btb ON btb.familie_hendelse_id = bfh.id " +
                "LEFT JOIN FH_TERMINBEKREFTELSE otb ON otb.familie_hendelse_id = ofh.id " +
                "LEFT JOIN ETTERKONTROLL_LOGG el ON el.behandling_id = b.id " +
                "LEFT JOIN BEHANDLING_ARSAK baa ON baa.behandling_id = b.id " +
                "WHERE el.id IS NULL " +
                "AND (baa.behandling_id IS NULL OR baa.behandling_arsak_type IN (:behandlingAarsakType)) " +
                "AND (btb.termindato <= :periodeTilbake OR otb.termindato <= :periodeTilbake)" +
                "AND br.behandling_resultat_type = :innvilgetKode " +
                "AND NOT EXISTS (SELECT f.id FROM FH_UIDENTIFISERT_BARN f WHERE f.familie_hendelse_id IN (bfh.id, ofh.id))" +
                "AND NOT EXISTS " +
                "  (SELECT beha.id " +
                "  FROM Behandling beha " +
                "  INNER JOIN BEHANDLING_ARSAK behaaa ON behaaa.behandling_id = beha.id " +
                "  WHERE beha.behandling_type = :revurderingType " +
                "  AND behaaa.behandling_arsak_type IN (:behandlingRevurderingAarsakType) " +
                "  AND beha.fagsak_id = b.fagsak_id  " +
                "  ) " +
                "ORDER BY bv.vedtak_dato DESC")
            .setHint(QueryHints.HINT_READONLY, "true");

        LocalDate datoTilbakeITid = LocalDate.now(FPDateUtil.getOffset()).minus(etterkontrollTidTilbake);
        query.setParameter("innvilgetKode", BehandlingResultatType.INNVILGET.getKode());
        query.setParameter("behandlingAarsakType", Kodeliste.kodeVerdier(BehandlingÅrsakType.RE_FEIL_I_LOVANDVENDELSE,
            BehandlingÅrsakType.RE_FEIL_PROSESSUELL, BehandlingÅrsakType.RE_FEIL_ELLER_ENDRET_FAKTA, BehandlingÅrsakType.RE_ANNET));
        query.setParameter("behandlingRevurderingAarsakType", Kodeliste.kodeVerdier(BehandlingÅrsakType.årsakerForAutomatiskRevurdering()));
        query.setParameter("periodeTilbake", datoTilbakeITid);
        query.setParameter("revurderingType", BehandlingType.REVURDERING.getKode());

        // Plukker i dag ut flere behandlinger på samme fagsak, fant ikke bedre
        @SuppressWarnings("unchecked")
        List<BigDecimal[]> result = query.getResultList();

        Collection<Long> ids = getSisteBehandlingIdPerFagsakId(result);

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Behandling> resultList = getEntityManager().createQuery("from Behandling where id in (:ids)", Behandling.class)
            .setParameter("ids", ids)
            .getResultList();

        return filtrerUtFlereBehandlingerPåSammeFagsak(resultList);
    }

    // sjekk lås og oppgrader til skriv
    protected void verifiserBehandlingLås(BehandlingLås lås) {
        BehandlingLåsRepositoryImpl låsHåndterer = new BehandlingLåsRepositoryImpl(getEntityManager());
        låsHåndterer.oppdaterLåsVersjon(lås);
    }

    private List<Behandling> filtrerUtFlereBehandlingerPåSammeFagsak(List<Behandling> behandlinger) {
        Map<Long, Behandling> behandlingMap = new HashMap<>();
        behandlinger.forEach(behandling -> behandlingMap.putIfAbsent(behandling.getFagsak().getId(), behandling));
        return new ArrayList<>(behandlingMap.values());
    }

    /**
     * Antar idx=0 er fagsak, 1 er behandling.
     */
    protected Collection<Long> getSisteBehandlingIdPerFagsakId(List<BigDecimal[]> result) {
        Map<Long, Long> fagsakIdToBehandlingId = new LinkedHashMap<>();
        for (Object[] bds : result) {
            Long fagsakId = ((BigDecimal) bds[0]).longValue(); // NOSONAR
            Long behandlingId = ((BigDecimal) bds[1]).longValue(); // NOSONAR
            fagsakIdToBehandlingId.putIfAbsent(fagsakId, behandlingId);
        }

        return fagsakIdToBehandlingId.values();
    }
}
