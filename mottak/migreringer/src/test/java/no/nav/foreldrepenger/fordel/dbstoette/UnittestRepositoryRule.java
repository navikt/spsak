package no.nav.foreldrepenger.fordel.dbstoette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class UnittestRepositoryRule extends RepositoryRule {

    private static final Logger log = LoggerFactory.getLogger(UnittestRepositoryRule.class);

    static {
        Databaseskjemainitialisering.kjørMigreringHvisNødvendig();
        Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag();
    }

    public UnittestRepositoryRule() {
        super();
    }

    public UnittestRepositoryRule(boolean transaksjonell) {
        super(transaksjonell);
    }

    public UnittestRepositoryRule(String persistenceUnitKey) {
        super(persistenceUnitKey);
    }

    public UnittestRepositoryRule(String persistenceUnitKey, boolean transaksjonell) {
        super(persistenceUnitKey, transaksjonell);
    }

    @Override
    protected void init() {
    }

}
