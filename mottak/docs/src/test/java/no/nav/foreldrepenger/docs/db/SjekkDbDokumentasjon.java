package no.nav.foreldrepenger.docs.db;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering;
import no.nav.foreldrepenger.fordel.dbstoette.DatasourceConfiguration;
import no.nav.vedtak.felles.lokal.dbstoette.ConnectionHandler;
import no.nav.vedtak.felles.lokal.dbstoette.DBConnectionProperties;
import no.nav.vedtak.felles.lokal.dbstoette.DatabaseStøtte;

public class SjekkDbDokumentasjon {

    private static final String HJELP = "Du har nylig lagt til en ny tabell eller kolonne som ikke er dokumentert ihht. gjeldende regler for dokumentasjon."
            + "\nVennligst gå over sql scriptene og dokumenter tabellene på korrekt måte.";

    private static DataSource ds;

    @BeforeClass
    public static void setup() throws FileNotFoundException {
        //Databaseskjemainitialisering.settOppSkjemaer();
        List<DBConnectionProperties> connectionProperties = DatasourceConfiguration.UNIT_TEST.get();
        DatabaseStøtte.settOppJndiForDefaultDataSource(connectionProperties);
        DatabaseStøtte.kjørFullMigreringFor(connectionProperties);

        DBConnectionProperties dbconp = DBConnectionProperties.finnDefault(connectionProperties).get();
        ds = ConnectionHandler.opprettFra(dbconp);

        //TODO: Gagan: Må skrive om testene slik at de tar alle skjemaer
    }

    @Test
    public void sjekk_at_alle_tabeller_er_dokumentert() throws Exception {
        String sql = "select table_name from all_tab_comments where (comments is null or comments='') and owner=sys_context('userenv', 'current_schema') and table_name not like 'schema_%'";
        int missing = 0;
        try (Connection conn = ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();) {

            while (rs.next()) {
                missing++;
                System.err.println("Mangler dokumentasjon for tabell: " + rs.getString(1));
            }

        }

        if (missing > 0) {
            Assert.fail("Mangler dokumentasjon for " + missing + " tabeller.\n" + HJELP);
        }
    }

    @Test
    public void sjekk_at_alle_relevant_kolonner_er_dokumentert() throws Exception {
        String sql = "select table_name||'.'||column_name from all_col_comments where (comments is null or comments='') and owner=sys_context('userenv', 'current_schema') and (upper(table_name) not like 'SCHEMA_%')"
                + " and upper(column_name) not in ('OPPRETTET_TID', 'ENDRET_TID', 'OPPRETTET_AV', 'ENDRET_AV', 'VERSJON', 'BESKRIVELSE', 'NAVN')";
        int missing = 0;
        try (Connection conn = ds.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();) {

            while (rs.next()) {
                missing++;
                System.err.println("Mangler dokumentasjon for Kolonne: " + rs.getString(1));
            }

        }

        if (missing > 0) {
            Assert.fail("Mangler dokumentasjon for " + missing + " kolonner.\n" + HJELP);
        }
    }
}
