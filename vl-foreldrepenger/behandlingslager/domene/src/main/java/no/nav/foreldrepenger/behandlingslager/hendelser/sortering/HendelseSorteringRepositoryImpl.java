package no.nav.foreldrepenger.behandlingslager.hendelser.sortering;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class HendelseSorteringRepositoryImpl implements HendelseSorteringRepository {

    private EntityManager entityManager;

    HendelseSorteringRepositoryImpl() {
        // CDI
    }

    @Inject
    public HendelseSorteringRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public List<AktørId> hentEksisterendeAktørIderMedSak(List<AktørId> aktørIdListe) {
        Set<AktørId> aktørIdSet = aktørIdListe.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (aktørIdSet.isEmpty()) {
            return Collections.emptyList();
        }

        Query query = getAktørIderMedRelevantSak(aktørIdSet);

        @SuppressWarnings("unchecked")
        List<String> resultList = query.getResultList();
        Stream<AktørId> results = resultList.stream().map(AktørId::new);

        return results
            .sorted()
            .distinct()
            .collect(Collectors.toList());
    }

    private Query getAktørIderMedRelevantSak(Set<AktørId> aktørIdListe) {
        Query query = entityManager.createNativeQuery(
            "(" +
                "SELECT BRUKER.AKTOER_ID FROM BRUKER " +
                "INNER JOIN FAGSAK ON BRUKER.ID = FAGSAK.BRUKER_ID " +
                "WHERE BRUKER.AKTOER_ID IN (:aktørIder) " +
                "AND FAGSAK.FAGSAK_STATUS <> 'AVSLU'" +
                "AND FAGSAK.YTELSE_TYPE <> 'ES'" +
                "UNION ALL " +
                "(SELECT SO_ANNEN_PART.AKTOER_ID FROM SO_ANNEN_PART " +
                "INNER JOIN SO_SOEKNAD ON SO_ANNEN_PART.ID = SO_SOEKNAD.ANNEN_PART_ID " +
                "WHERE SO_ANNEN_PART.AKTOER_ID IN (:aktørIder)) " +
                "UNION ALL " +
                "(SELECT por.TIL_AKTOER_ID From GR_PERSONOPPLYSNING grp " +
                "JOIN PO_INFORMASJON poi ON grp.registrert_informasjon_id = poi.ID " +
                "JOIN PO_RELASJON por ON poi.ID = por.po_informasjon_id " +
                "WHERE grp.aktiv = 'J' " +
                "AND por.relasjonsrolle = :relasjonsRolle " +
                "AND por.TIL_AKTOER_ID IN (:aktørIder))" +
                ")")
            .setParameter("relasjonsRolle", RelasjonsRolleType.BARN.getKode())
            .setParameter("aktørIder", aktørIdListe.stream().map(AktørId::getId).collect(Collectors.toCollection(LinkedHashSet::new)));
        return query;
    }

}
