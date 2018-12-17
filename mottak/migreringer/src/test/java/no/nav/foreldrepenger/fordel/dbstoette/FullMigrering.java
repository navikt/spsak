package no.nav.foreldrepenger.fordel.dbstoette;

import java.util.List;

import org.junit.Test;

import no.nav.vedtak.felles.lokal.dbstoette.DBConnectionProperties;
import no.nav.vedtak.felles.lokal.dbstoette.DatabaseStøtte;

/**
 * Kjøres kun manuelt (aldri automatisk) siden den ikke heter xxTest.
 * Convenience klasse som kan kjøres dersom database er ute av synk med migreringer
 */
public class FullMigrering {

    @Test
    public void skal_utføre_full_migrering() throws Exception {
        //Databaseskjemainitialisering.settOppSkjemaer();
        List<DBConnectionProperties> connectionProperties = DatasourceConfiguration.UNIT_TEST.get();
        DatabaseStøtte.kjørFullMigreringFor(connectionProperties);

    }
}
