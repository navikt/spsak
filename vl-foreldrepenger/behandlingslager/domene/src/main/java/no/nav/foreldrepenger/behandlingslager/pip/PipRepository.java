package no.nav.foreldrepenger.behandlingslager.pip;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class PipRepository {

    private EntityManager entityManager;

    public PipRepository() {
    }

    @Inject
    public PipRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Optional<PipBehandlingsData> hentDataForBehandling(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); //NOSONAR

        String sql = "SELECT " +
            "b.behandling_status behandligStatus, " +
            "b.ansvarlig_saksbehandler ansvarligSaksbehandler, " +
            "f.id fagsakId, " +
            "f.fagsak_status fagsakStatus " +
            "FROM BEHANDLING b " +
            "JOIN FAGSAK f ON b.fagsak_id = f.id " +
            "WHERE b.id = :behandlingId";

        Query query = entityManager.createNativeQuery(sql, "PipDataResult");
        query.setParameter("behandlingId", behandlingId);

        @SuppressWarnings("rawtypes")
        List resultater = query.getResultList();
        if (resultater.isEmpty()) {
            return Optional.empty();
        } else if (resultater.size() == 1) {
            return Optional.of((PipBehandlingsData) resultater.get(0));
        } else {
            throw new IllegalStateException("Forventet 0 eller 1 treff etter søk på behandlingId, fikk flere for behandlingId " + behandlingId);
        }
    }

    public Set<AktørId> hentAktørIdKnyttetTilFagsaker(Collection<Long> fagsakIder) {
        Objects.requireNonNull(fagsakIder, "saksnummer");
        if (fagsakIder.isEmpty()) {
            return Collections.emptySet();
        }
        String sql =
            "SELECT por.AKTOER_ID From Fagsak fag " +
                "JOIN BEHANDLING beh ON fag.ID = beh.FAGSAK_ID " +
                "JOIN GR_PERSONOPPLYSNING grp ON grp.behandling_id = beh.ID " +
                "JOIN PO_INFORMASJON poi ON grp.registrert_informasjon_id = poi.ID " +
                "JOIN PO_PERSONOPPLYSNING por ON poi.ID = por.po_informasjon_id " +
                "WHERE fag.id in (:fagsakIder) AND grp.aktiv = 'J' " +
                " UNION ALL " +  // NOSONAR
                "SELECT br.AKTOER_ID FROM Fagsak fag " +
                "JOIN Bruker br ON fag.BRUKER_ID = br.ID " +
                "WHERE fag.id in (:fagsakIder) AND br.AKTOER_ID IS NOT NULL " +
                " UNION ALL " +  // NOSONAR
                "SELECT sa.AKTOER_ID From Fagsak fag " +
                "JOIN BEHANDLING beh ON fag.ID = beh.FAGSAK_ID " +
                "JOIN GR_PERSONOPPLYSNING grp ON grp.behandling_id = beh.ID " +
                "JOIN SO_ANNEN_PART sa ON grp.so_annen_part_id = sa.ID " +
                "WHERE fag.id in (:fagsakIder) AND grp.aktiv = 'J' AND sa.AKTOER_ID IS NOT NULL ";

        Query query = entityManager.createNativeQuery(sql); //NOSONAR
        query.setParameter("fagsakIder", fagsakIder);

        @SuppressWarnings("unchecked")
        List<String> aktørIdList = query.getResultList();
        return aktørIdList.stream().map(AktørId::new).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings({"unchecked", "cast"})
    public Set<Long> fagsakIdForJournalpostId(Collection<JournalpostId> journalpostId) {
        if (journalpostId.isEmpty()) {
            return Collections.emptySet();
        }
        String sql = "SELECT fagsak_id FROM JOURNALPOST WHERE journalpost_id in (:journalpostId) " +
            " UNION ALL " +  // NOSONAR
            "SELECT f.id from FAGSAK f " +
            "JOIN DOKUMENT_FELLES df on f.saksnummer = df.saksnummer " +
            "WHERE df.journalpost_id in (:journalpostId)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("journalpostId", journalpostId.stream().map(j -> j.getVerdi()).collect(Collectors.toList()));

        List<BigDecimal> result = (List<BigDecimal>) query.getResultList();
        return result.stream().map(BigDecimal::longValue).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings({"unchecked", "cast"})
    public Set<Long> behandlingsIdForOppgaveId(Collection<String> oppgaveIder) {
        if (oppgaveIder.isEmpty()) {
            return Collections.emptySet();
        }
        String sql = "SELECT behandling_id FROM OPPGAVE_BEHANDLING_KOBLING WHERE oppgave_id in (:oppgaveIder)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("oppgaveIder", oppgaveIder);
        List<BigDecimal> result = (List<BigDecimal>) query.getResultList();
        return result.stream().map(BigDecimal::longValue).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings({"unchecked"})
    public Set<String> hentAksjonspunktTypeForAksjonspunktKoder(Collection<String> aksjonspunktKoder) {
        if (aksjonspunktKoder.isEmpty()) {
            return Collections.emptySet();
        }
        String sql = "SELECT k.offisiell_kode" +
            " from AKSJONSPUNKT_DEF def" +
            " join KODELISTE k on (def.aksjonspunkt_type = k.kode and k.kodeverk='AKSJONSPUNKT_TYPE')" +
            " where def.kode in (:aksjonspunktKoder) ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("aksjonspunktKoder", aksjonspunktKoder);
        return new HashSet<>(query.getResultList());
    }

    @SuppressWarnings({"unchecked", "cast"})
    public Set<Long> behandlingsIdForDokumentDataId(Collection<Long> dokumentDataIDer) {
        if (dokumentDataIDer.isEmpty()) {
            return Collections.emptySet();
        }
        String sql = "SELECT behandling_id from DOKUMENT_DATA where id in (:dokumentDataIDer) ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("dokumentDataIDer", dokumentDataIDer);
        List<BigDecimal> result = (List<BigDecimal>) query.getResultList();
        return result.stream().map(BigDecimal::longValue).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings({"unchecked", "cast"})
    public Set<Long> fagsakIderForSøker(Collection<AktørId> aktørId) {
        if (aktørId.isEmpty()) {
            return Collections.emptySet();
        }
        String sql = "SELECT f.id " +
            "from FAGSAK f " +
            "join BRUKER b on (f.bruker_id = b.id) " +
            "where b.aktoer_id in (:aktørId)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("aktørId", aktørId.stream().map(AktørId::getId).collect(Collectors.toList()));
        List<BigDecimal> result = (List<BigDecimal>) query.getResultList();
        return result.stream().map(BigDecimal::longValue).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings({"unchecked", "cast"})
    public Set<Long> fagsakIdForSaksnummer(Collection<String> saksnummre) {
        if (saksnummre.isEmpty()) {
            return Collections.emptySet();
        }
        String sql = "SELECT id from FAGSAK where saksnummer in (:saksnummre) ";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("saksnummre", saksnummre);
        List<BigDecimal> result = (List<BigDecimal>) query.getResultList();
        return result.stream().map(BigDecimal::longValue).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
