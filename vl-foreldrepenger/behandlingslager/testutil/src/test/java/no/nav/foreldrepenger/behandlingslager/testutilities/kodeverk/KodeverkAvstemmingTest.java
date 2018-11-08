package no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.dbstoette.DatasourceConfiguration;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.lokal.dbstoette.ConnectionHandler;
import no.nav.vedtak.felles.lokal.dbstoette.DBConnectionProperties;

@RunWith(Parameterized.class)
public class KodeverkAvstemmingTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager em = repoRule.getEntityManager();

    private String kodeverk;

    @org.junit.runners.Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() throws Exception {
        List<DBConnectionProperties> connectionProperties = DatasourceConfiguration.UNIT_TEST.get();

        DBConnectionProperties dbconp = DBConnectionProperties.finnDefault(connectionProperties).get();
        DataSource ds = ConnectionHandler.opprettFra(dbconp);

        List<Object[]> params = new ArrayList<>();

        try (Connection conn = ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement("select kode from kodeverk");
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                params.add(new Object[] { rs.getString(1) });
            }
        }
        return params;
    }

    public KodeverkAvstemmingTest(String kodeverk) {
        this.kodeverk = kodeverk;
    }

    @Test
    public void sjekk_at_kodeliste_konstanter_finnes_også_i_database() throws Exception {
        String feilFantIkke = "Fant ikke verdi av felt";

        Query query = em.createQuery(" from " + Kodeliste.class.getName() + " where kodeverk=:kodeverk");
        query.setParameter("kodeverk", kodeverk);
        @SuppressWarnings("unchecked")
        List<Kodeliste> resultList = query.getResultList();

        Map<Class<?>, List<Kodeliste>> gruppert = resultList.stream().collect(Collectors.groupingBy(Object::getClass));

        assertThat(gruppert).isNotEmpty();

        for (Map.Entry<Class<?>, List<Kodeliste>> entry : gruppert.entrySet()) {
            Map<String, Kodeliste> koder = entry.getValue().stream()
                .collect(Collectors.toMap(Kodeliste::getKode, Function.identity()));

            Class<?> cls = entry.getKey();
            Arrays.asList(cls.getDeclaredFields()).stream()
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> f.getType().equals(cls))
                .forEach(f -> {
                    Kodeliste k;
                    try {
                        k = (Kodeliste) f.get(null);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new AssertionError(feilFantIkke + f, e);
                    }
                    assertThat(koder).containsKey(k.getKode());
                });
        }
    }
}
