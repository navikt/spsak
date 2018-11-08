package no.nav.foreldrepenger.vedtakslager;


import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@VLPersistenceUnit()
public class LagretVedtakRepositoryRule extends UnittestRepositoryRule {

    public LagretVedtakRepositoryRule() {
        super(findDefaultPersistenceUnit(LagretVedtakRepositoryRule.class));
    }
}
