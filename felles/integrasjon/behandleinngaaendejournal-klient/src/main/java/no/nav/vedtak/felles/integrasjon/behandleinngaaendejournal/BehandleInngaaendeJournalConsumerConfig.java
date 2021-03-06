package no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.CallIdOutInterceptor;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
public class BehandleInngaaendeJournalConsumerConfig {

    private static final String WSDL = "wsdl/no/nav/tjeneste/virksomhet/behandleInngaaendeJournal/v1/Binding.wsdl";
    private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1/Binding";
    private static final QName SERVICE = new QName(NAMESPACE, "BehandleInngaaendeJournal_v1");
    private static final QName PORT = new QName(NAMESPACE, "BehandleInngaaendeJournal_v1Port");

    // TODO (u139158): Gjør verdi påkrevd når får innslag i Fasit
    @Inject
    @KonfigVerdi(value = "BehandleInngaaendeJournal_v1.url", required = false)
    private String endpointUrl; // NOSONAR

    BehandleInngaaendeJournalV1 getPort() {
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(WSDL);
        factoryBean.setServiceName(SERVICE);
        factoryBean.setEndpointName(PORT);
        factoryBean.setServiceClass(BehandleInngaaendeJournalV1.class);
        factoryBean.setAddress(endpointUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());
        return factoryBean.create(BehandleInngaaendeJournalV1.class);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

}
