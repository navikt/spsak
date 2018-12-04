package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KodeverkSynkroniseringRepositoryImpl implements KodeverkSynkroniseringRepository {

    private static final String KODEVERK_EIER = "Kodeverkforvaltning";
    private EntityManager entityManager;

    KodeverkSynkroniseringRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public KodeverkSynkroniseringRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public List<Kodeverk> hentKodeverkForSynkronisering() {
        TypedQuery<Kodeverk> query = entityManager.createQuery(
            "from Kodeverk k where (k.synkNyeKoderFraKodeverEier = 'J' or k.synkEksisterendeKoderFraKodeverkEier = 'J') and k.kodeverkEier=:kodeverkEier",
            Kodeverk.class)
            .setParameter("kodeverkEier", KODEVERK_EIER);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getResultList();
    }

    @Override
    public List<Kodeliste> hentKodeliste(String kodeverkNavn) {
        TypedQuery<Kodeliste> query = entityManager.createQuery(
            "from Kodeliste where kodeverk=:kodeverkNavn", Kodeliste.class)
            .setParameter("kodeverkNavn", kodeverkNavn);
        return query.getResultList();
    }

    @Override
    public Map<String, String> hentKodeverkEierNavnMap() {
        TypedQuery<Kodeverk> query = entityManager.createQuery(
            "from Kodeverk k where k.kodeverkEierNavn is not null and k.kodeverkEier=:kodeverkEier",
            Kodeverk.class)
            .setParameter("kodeverkEier", KODEVERK_EIER);
        query.setHint(QueryHints.HINT_READONLY, "true");
        return query.getResultList().stream()
            .collect(Collectors.toMap(Kodeverk::getKodeverkEierNavn, Kodeverk::getKode));
    }

    @Override
    public void opprettNyKode(String kodeverk, String kode, String offisiellKode, String navn, LocalDate fom, LocalDate tom) {
        Query query = entityManager.createNativeQuery(
            "INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, gyldig_fom, gyldig_tom) " +
                " VALUES (nextval('seq_kodeliste'), ?, ?, ?, ?, ?)");
        query.setParameter(1, kodeverk);
        query.setParameter(2, kode);
        query.setParameter(3, offisiellKode);
        query.setParameter(4, Date.valueOf(fom));
        query.setParameter(5, Date.valueOf(tom));
        query.executeUpdate();
        Query query2 = entityManager.createNativeQuery(
            "INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn) " +
                " VALUES (nextval('seq_kodeliste_navn_i18n'), ?, ?, ?, ?)");
        query2.setParameter(1, kodeverk);
        query2.setParameter(2, kode);
        query2.setParameter(3, "NB");
        query2.setParameter(4, navn);
        query2.executeUpdate();
    }

    @Override
    public void oppdaterEksisterendeKodeVerk(String kodeverk, String versjon, String uri) {
        Query query = entityManager.createNativeQuery(
            "UPDATE KODEVERK SET  kodeverk_eier_ver=?, kodeverk_eier_ref=? " +
                " WHERE kode=? ");
        query.setParameter(1, versjon);
        query.setParameter(2, uri);
        query.setParameter(3, kodeverk);
        query.executeUpdate();
    }

    @Override
    public void oppdaterEksisterendeKode(String kodeverk, String kode, String offisiellKode, String navn, LocalDate fom, LocalDate tom) {
        Query query = entityManager.createNativeQuery(
            "UPDATE KODELISTE SET  offisiell_kode=?, gyldig_fom=?, gyldig_tom=? " +
                " WHERE kodeverk=? AND kode=?");
        query.setParameter(1, offisiellKode);
        query.setParameter(2, Date.valueOf(fom));
        query.setParameter(3, Date.valueOf(tom));
        query.setParameter(4, kodeverk);
        query.setParameter(5, kode);
        query.executeUpdate();
        Query query2 = entityManager.createNativeQuery(
            "UPDATE KODELISTE_NAVN_I18N SET  navn=? " +
                " WHERE kl_kodeverk=? AND kl_kode=? AND sprak=?");
        query2.setParameter(1, navn);
        query2.setParameter(2, kodeverk);
        query2.setParameter(3, kode);
        query2.setParameter(4, "NB");
        query2.executeUpdate();
    }

    @Override
    public void opprettNyKodeRelasjon(String kodeverk1, String kode1, String kodeverk2, String kode2, LocalDate fom, LocalDate tom) {
        Query query = entityManager.createNativeQuery(
            "INSERT INTO KODELISTE_RELASJON (id, kodeverk1, kode1, kodeverk2, kode2, gyldig_fom, gyldig_tom) " +
                " VALUES (nextval('seq_kodeliste_relasjon'), ?, ?, ?, ?, ?, ?)");
        query.setParameter(1, kodeverk1);
        query.setParameter(2, kode1);
        query.setParameter(3, kodeverk2);
        query.setParameter(4, kode2);
        query.setParameter(5, Date.valueOf(fom));
        query.setParameter(6, Date.valueOf(tom));
        query.executeUpdate();
    }

    @Override
    public boolean eksistererKode(String kodeverk, String kode) {
        Query query = entityManager.createNativeQuery(
            "SELECT 1 FROM KODELISTE WHERE kodeverk=? AND kode=?");
        query.setParameter(1, kodeverk);
        query.setParameter(2, kode);
        return !query.getResultList().isEmpty();
    }

    @Override
    public void oppdaterEksisterendeKodeRelasjon(String kodeverk1, String kode1, String kodeverk2, String kode2, LocalDate fom, LocalDate tom) {
        Query query = entityManager.createNativeQuery(
            "UPDATE KODELISTE_RELASJON SET  gyldig_fom=?, gyldig_tom=? " +
                " WHERE kodeverk1=? AND kode1=? AND kodeverk2=? AND kode2=?");
        query.setParameter(1, Date.valueOf(fom));
        query.setParameter(2, Date.valueOf(tom));
        query.setParameter(3, kodeverk1);
        query.setParameter(4, kode1);
        query.setParameter(5, kodeverk2);
        query.setParameter(6, kode2);
        query.executeUpdate();
    }

    /**
     * Henter kodelisterelasjoner med et hierarkisk query hvor angitt
     * kodeverk og kode er rot og de øvrige entiteter som returneres
     * hører innunder denne. Henter bare relasjoner med
     * gyldighetsdatoer som dekker sysdate.
     *
     */
    @Override
    public List<KodelisteRelasjon> hentKodelisteRelasjoner(String kodeverk1, String kode1) {
        Query query = entityManager.createNativeQuery(
            "SELECT kodeverk1, kode1, kodeverk2, kode2, gyldig_fom, gyldig_tom " +
                "FROM kodeliste_relasjon " +
                "WHERE gyldig_fom <= SYSDATE AND gyldig_tom > SYSDATE " +
            "START WITH kodeverk1=? AND kode1=? " +
            "CONNECT BY PRIOR kodeverk2 = kodeverk1 AND PRIOR kode2 = kode1");
        query.setParameter(1, kodeverk1);
        query.setParameter(2, kode1);

        int kodeverk1Nr = 0;
        int kode1Nr = 1;
        int kodeverk2Nr = 2;
        int kode2Nr = 3;
        int fomNr = 4;
        int tomNr = 5;

        List<KodelisteRelasjon> retval = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> koderelasjoner = query.getResultList();

        for (Object[] kr : koderelasjoner) {
            retval.add(new KodelisteRelasjon((String)kr[kodeverk1Nr], (String)kr[kode1Nr],
                (String)kr[kodeverk2Nr], (String)kr[kode2Nr],
                ((Timestamp)kr[fomNr]).toLocalDateTime().toLocalDate(),
                ((Timestamp)kr[tomNr]).toLocalDateTime().toLocalDate()));
        }
        return retval;
    }

    @Override
    public List<KodelisteRelasjon> hentKodelisteRelasjonFor(String kodeverk) {
        Query query = entityManager.createNativeQuery(
                "SELECT kodeverk1, kode1, kodeverk2, kode2, gyldig_fom, gyldig_tom " +
                        "FROM kodeliste_relasjon " +
                        "WHERE gyldig_fom <= SYSDATE AND gyldig_tom > SYSDATE " +
                        "AND KODEVERK1 = ?");

        query.setParameter(1, kodeverk);

        int kodeverk1Nr = 0;
        int kode1Nr = 1;
        int kodeverk2Nr = 2;
        int kode2Nr = 3;
        int fomNr = 4;
        int tomNr = 5;

        List<KodelisteRelasjon> retval = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> koderelasjoner = query.getResultList();

        for (Object[] kr : koderelasjoner) {
            retval.add(new KodelisteRelasjon((String)kr[kodeverk1Nr], (String)kr[kode1Nr],
                    (String)kr[kodeverk2Nr], (String)kr[kode2Nr],
                    ((Timestamp)kr[fomNr]).toLocalDateTime().toLocalDate(),
                    ((Timestamp)kr[tomNr]).toLocalDateTime().toLocalDate()));
        }
        return retval;
    }

    @Override
    public void lagre(){
        entityManager.flush();
    }
}
