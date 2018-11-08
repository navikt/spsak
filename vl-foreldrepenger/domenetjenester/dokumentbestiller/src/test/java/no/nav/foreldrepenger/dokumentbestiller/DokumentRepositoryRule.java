package no.nav.foreldrepenger.dokumentbestiller;

import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@VLPersistenceUnit()
public class DokumentRepositoryRule extends UnittestRepositoryRule {

    public DokumentRepositoryRule() {
        super(findDefaultPersistenceUnit(DokumentRepositoryRule.class));
    }
}
