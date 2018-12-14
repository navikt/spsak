package no.nav.foreldrepenger.web.app.metrics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.ReaktiveringStatus;
import no.nav.vedtak.felles.jpa.OracleVersionChecker;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class MetricRepository {

    private EntityManager entityManager;
    private OracleVersionChecker oracleVersionChecker;
    private AksjonspunktRepository aksjonspunktRepository;

    private static final long MAX_DATA_ALDER_MS = 30000;

    private BigDecimal sisteBehandlingIkkeAvsluttet;
    private BigDecimal sisteVentendeBehandlinger;
    private BigDecimal sisteVentendeOppgaver;
    private long sisteBehandlingTidspunktMs = 0;
    private long sisteVentendeTidspunktMs = 0;
    private long sisteOppgaverTidspunktMs = 0;

    MetricRepository() {
        // for CDI proxy
    }

    @Inject
    public MetricRepository(@VLPersistenceUnit EntityManager entityManager, OracleVersionChecker oracleVersionChecker,
                            AksjonspunktRepository aksjonspunktRepository) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        Objects.requireNonNull(oracleVersionChecker, "oracleVersionChecker"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.oracleVersionChecker = oracleVersionChecker;
        this.aksjonspunktRepository = aksjonspunktRepository;
    }

    BigDecimal tellLettereAntallBehandlingerSomIkkeHarBlittAvsluttet() {
        long naaMs = System.currentTimeMillis();
        long alderMs = naaMs - sisteBehandlingTidspunktMs;
        if (alderMs >= MAX_DATA_ALDER_MS) {
            final Query query = entityManager.createNativeQuery("SELECT count(DISTINCT b.ID) " +
                "FROM BEHANDLING b " +
                "WHERE b.BEHANDLING_STATUS != :status");
            query.setParameter("status", BehandlingStatus.AVSLUTTET.getKode());
            sisteBehandlingIkkeAvsluttet = new BigDecimal((BigInteger) query.getSingleResult());
            sisteBehandlingTidspunktMs = System.currentTimeMillis();
        }
        return sisteBehandlingIkkeAvsluttet;
    }

    // Approksimer ved at antall behandlinger ikke er langt unna antall behandling_resultat
    BigDecimal tellLettereAntallVentendeBehandlinger() {
        long naaMs = System.currentTimeMillis();
        long alderMs = naaMs - sisteVentendeTidspunktMs;
        if (alderMs >= MAX_DATA_ALDER_MS) {
            List<String> autopunktKoder = aksjonspunktRepository.hentAksjonspunktDefinisjonAvType(AksjonspunktType.AUTOPUNKT)
                .stream().map(AksjonspunktDefinisjon::getKode).collect(Collectors.toList());
            final Query query = entityManager.createNativeQuery("SELECT count(DISTINCT a.BEHANDLING_ID) " +
                "FROM AKSJONSPUNKT a " +
                "WHERE a.AKSJONSPUNKT_STATUS = :aksjonspunktStatus " +
                "      AND a.REAKTIVERING_STATUS = :reaktiveringsstatus " +
                "      AND a.AKSJONSPUNKT_DEF IN (:aksjonspunktList)");
            query.setParameter("aksjonspunktStatus", AksjonspunktStatus.OPPRETTET.getKode());
            query.setParameter("reaktiveringsstatus", ReaktiveringStatus.AKTIV.getKode());
            query.setParameter("aksjonspunktList", autopunktKoder);

            sisteVentendeBehandlinger = new BigDecimal((BigInteger) query.getSingleResult());
            sisteVentendeTidspunktMs = System.currentTimeMillis();
        }
        return sisteVentendeBehandlinger;
    }

    List<String> hentProsessTaskTyperMedPrefixer(List<String> prefixer) {
        Query query = entityManager.createNativeQuery("SELECT KODE FROM PROSESS_TASK_TYPE");
        @SuppressWarnings("unchecked")
        List<String> alleProsessTaskTyper = query.getResultList();

        List<String> ønskedeProsessTaskTyper = alleProsessTaskTyper.stream().
            filter(ptType -> stringHarEtAvPrefixer(ptType, prefixer)).
            collect(Collectors.toList());

        return ønskedeProsessTaskTyper;
    }

    /**
     * @return Liste av [type/String, status/String, antall/BigDecimal]
     * <p>
     * Tasks med status FERDIG telles ikke.
     */
    List<Object[]> tellAntallProsessTaskerPerTypeOgStatus() {
        final String queryTemplate =
            " select task_type, status, count(*) " +
                "from %s " +
                "group by task_type, status ";
        String queryStr;
        if (oracleVersionChecker.isRunningOnExpressEdition()) {
            // Express Edition mangler støtte for spørring på partition
            queryStr = String.format(queryTemplate, " (select * from prosess_task where status <> 'FERDIG') ");
        } else {
            queryStr =
                String.format(queryTemplate, " prosess_task partition (STATUS_KLAR) ") +
                    " UNION " +
                    String.format(queryTemplate, " prosess_task partition (STATUS_FEILET) ");
        }
        Query query = entityManager.createNativeQuery(queryStr);
        @SuppressWarnings("unchecked")
        List<Object[]> rowList = query.getResultList();
        return rowList;
    }

    /**
     * @return Liste av [status/String, antall/BigDecimal]
     *
     * Tasks med status FERDIG telles ikke.
     */
    List<Object[]> tellAntallProsessTaskerPerStatus() {
        final String queryTemplate =
            " select status, count(*) " +
                "from %s " +
                "group by status ";
        String queryStr;
        if (oracleVersionChecker.isRunningOnExpressEdition()) {
            // Express Edition mangler støtte for spørring på partition
            queryStr = String.format(queryTemplate, " (select * from prosess_task where status <> 'FERDIG') ");
        } else {
            queryStr =
                String.format(queryTemplate, " prosess_task partition (STATUS_KLAR) ") +
                    " UNION " +
                    String.format(queryTemplate, " prosess_task partition (STATUS_FEILET) ");
        }
        Query query = entityManager.createNativeQuery(queryStr);
        @SuppressWarnings("unchecked")
        List<Object[]> rowList = query.getResultList();
        return rowList;
    }

    private boolean stringHarEtAvPrefixer(String s, List<String> prefixer) {
        boolean res = prefixer.stream().
            anyMatch(prefix -> s.startsWith(prefix));
        return res;
    }
}
