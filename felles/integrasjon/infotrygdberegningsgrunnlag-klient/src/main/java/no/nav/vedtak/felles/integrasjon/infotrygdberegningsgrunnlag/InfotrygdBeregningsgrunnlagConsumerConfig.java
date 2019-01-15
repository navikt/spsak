package no.nav.vedtak.felles.integrasjon.infotrygdberegningsgrunnlag;

import no.nav.tjeneste.virksomhet.infotrygdberegningsgrunnlag.v1.binding.InfotrygdBeregningsgrunnlagV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

@Dependent
public class InfotrygdBeregningsgrunnlagConsumerConfig {
    private static final String WSDL = "nav-infotrygdBeregningsgrunnlag-v1-tjenestespesifikasjon/wsdl/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/infotrygdBeregningsgrunnlag/v1/Binding";
    private static final QName SERVICE = new QName(NAMESPACE, "infotrygdBeregningsgrunnlag_v1");
    private static final QName PORT = new QName(NAMESPACE, "infotrygdBeregningsgrunnlag_v1Port");

    private String endpointUrl;

    @Inject
    public InfotrygdBeregningsgrunnlagConsumerConfig(@KonfigVerdi("InfotrygdBeregningsgrunnlag_v1.url") String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    InfotrygdBeregningsgrunnlagV1 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(InfotrygdBeregningsgrunnlagV1.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(InfotrygdBeregningsgrunnlagV1.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }
}
