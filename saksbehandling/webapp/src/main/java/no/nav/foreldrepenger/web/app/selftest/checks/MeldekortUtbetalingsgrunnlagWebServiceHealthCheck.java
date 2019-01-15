package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.meldekortutbetalingsgrunnlag.MeldekortUtbetalingsgrunnlagSelftestConsumer;

@ApplicationScoped
public class MeldekortUtbetalingsgrunnlagWebServiceHealthCheck extends WebServiceHealthCheck {

    MeldekortUtbetalingsgrunnlagSelftestConsumer meldekortUtbetalingsgrunnlagSelftestConsumer;

    MeldekortUtbetalingsgrunnlagWebServiceHealthCheck(){
        //For CDI proxy
    }

    @Inject
    public MeldekortUtbetalingsgrunnlagWebServiceHealthCheck(MeldekortUtbetalingsgrunnlagSelftestConsumer meldekortUtbetalingsgrunnlagSelftestConsumer){
        this.meldekortUtbetalingsgrunnlagSelftestConsumer = meldekortUtbetalingsgrunnlagSelftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        meldekortUtbetalingsgrunnlagSelftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service MeldekortUtbetalingsgrunnlag";
    }

    @Override
    protected String getEndpoint() {
        return meldekortUtbetalingsgrunnlagSelftestConsumer.getEndpointUrl();
    }

}
