package no.nav.foreldrepenger.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag.InfotrygdBeregningsgrunnlagSelftestConsumer;

@ApplicationScoped
public class InfotrygdBeregningsgrunnlagWebServiceHealthCheck extends WebServiceHealthCheck {

    InfotrygdBeregningsgrunnlagSelftestConsumer infotrygdBeregningsgrunnlagSelftestConsumer;

    InfotrygdBeregningsgrunnlagWebServiceHealthCheck(){
        //For CDI proxy
    }

    @Inject
    public InfotrygdBeregningsgrunnlagWebServiceHealthCheck(InfotrygdBeregningsgrunnlagSelftestConsumer infotrygdBeregningsgrunnlagSelftestConsumer){
        this.infotrygdBeregningsgrunnlagSelftestConsumer = infotrygdBeregningsgrunnlagSelftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        infotrygdBeregningsgrunnlagSelftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service InfotrygdBeregningsgrunnlag";
    }

    @Override
    protected String getEndpoint() {
        return infotrygdBeregningsgrunnlagSelftestConsumer.getEndpointUrl();
    }

}
