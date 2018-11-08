package no.nav.foreldrepenger.økonomistøtte.queue.consumer;

import static no.nav.vedtak.feil.LogLevel.WARN;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Mmel;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragSkjemaConstants;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsLinje150;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomiKvittering;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.foreldrepenger.økonomistøtte.queue.ØkonomioppdragMeldingFeil;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.integrasjon.jms.InternalQueueConsumer;
import no.nav.vedtak.felles.integrasjon.jms.JmsKonfig;
import no.nav.vedtak.felles.integrasjon.jms.precond.DefaultDatabaseOppePreconditionChecker;
import no.nav.vedtak.felles.integrasjon.jms.precond.PreconditionChecker;

@ApplicationScoped
public class ØkonomioppdragAsyncJmsConsumer extends InternalQueueConsumer {
    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;
    private DefaultDatabaseOppePreconditionChecker preconditionChecker;

    public ØkonomioppdragAsyncJmsConsumer() {
    }

    @Inject
    public ØkonomioppdragAsyncJmsConsumer(ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste,
                                          DefaultDatabaseOppePreconditionChecker preconditionChecker,
                                          @Named("økonomioppdragjmsconsumerkonfig") JmsKonfig konfig) {
        super(konfig);
        this.økonomioppdragApplikasjonTjeneste = økonomioppdragApplikasjonTjeneste;
        this.preconditionChecker = preconditionChecker;
    }

    @Override
    public PreconditionChecker getPreconditionChecker() {
        return preconditionChecker;
    }

    @Override
    public void handle(Message message) throws JMSException {
        log.debug("Mottar melding");
        if (message instanceof TextMessage) {
            handle(((TextMessage) message).getText());
        } else {
            FeilFactory.create(Feilene.class).ikkestøttetMessage(message.getClass()).log(log);
        }
    }

    public void handle(String message) {
        try {
            Oppdrag kvitteringsmelding = unmarshalOgKorriger(message);
            ØkonomiKvittering kvittering = fraKvitteringsmelding(kvitteringsmelding);
            økonomioppdragApplikasjonTjeneste.behandleKvittering(kvittering);
        } catch (SAXException | JAXBException e) { // NOSONAR
            throw ØkonomioppdragMeldingFeil.FACTORY.uventetFeilVedProsesseringAvForsendelsesInfoXMLMedJaxb(message, e).toException();
        } catch (XMLStreamException e) { // NOSONAR
            throw ØkonomioppdragMeldingFeil.FACTORY.uventetFeilVedProsesseringAvForsendelsesInfoXML(e).toException();
        }
    }

    private Oppdrag unmarshalOgKorriger(String message) throws JAXBException, XMLStreamException, SAXException {
        Oppdrag kvitteringsmelding;
        try {
            kvitteringsmelding = JaxbHelper.unmarshalAndValidateXMLWithStAX(OppdragSkjemaConstants.JAXB_CLASS, message, OppdragSkjemaConstants.XSD_LOCATION);
        } catch (UnmarshalException e) { // NOSONAR
            String editedMessage = message
                .replace("<oppdrag ", "<xml_1:oppdrag ")
                .replace("xmlns=", "xmlns:xml_1=")
                .replace("</oppdrag>", "</xml_1:oppdrag>")
                .replace("</ns2:oppdrag>", "</xml_1:oppdrag>");
            kvitteringsmelding = JaxbHelper.unmarshalAndValidateXMLWithStAX(OppdragSkjemaConstants.JAXB_CLASS, editedMessage, OppdragSkjemaConstants.XSD_LOCATION);
        }
        return kvitteringsmelding;
    }

    private ØkonomiKvittering fraKvitteringsmelding(Oppdrag melding) {
        ØkonomiKvittering kvittering = new ØkonomiKvittering();
        fraMmel(kvittering, melding.getMmel(), melding.getOppdrag110().getFagsystemId());
        fraOppdragLinje150(kvittering, melding.getOppdrag110().getOppdragsLinje150().get(0));
        return kvittering;
    }

    private void fraOppdragLinje150(ØkonomiKvittering kvittering, OppdragsLinje150 oppdragsLinje150) {
        kvittering.setBehandlingId(Long.valueOf(oppdragsLinje150.getHenvisning()));
    }

    private void fraMmel(ØkonomiKvittering kvittering, Mmel mmel, String fagsystemId) {
        kvittering.setAlvorlighetsgrad(mmel.getAlvorlighetsgrad());
        kvittering.setMeldingKode(mmel.getKodeMelding());
        kvittering.setBeskrMelding(mmel.getBeskrMelding());
        kvittering.setFagsystemId(Long.parseLong(fagsystemId));
    }

    @Override
    @Resource(mappedName = ØkonomioppdragJmsConsumerKonfig.JNDI_JMS_CONNECTION_FACTORY)
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        super.setConnectionFactory(connectionFactory);
    }

    @Override
    @Resource(mappedName = ØkonomioppdragJmsConsumerKonfig.JNDI_QUEUE)
    public void setQueue(Queue queue) {
        super.setQueue(queue);
    }

    interface Feilene extends DeklarerteFeil {
        @TekniskFeil(feilkode = "FP-832935", feilmelding = "Mottok på ikkestøttet message av klasse %s. Kø-elementet ble ignorert", logLevel = WARN)
        Feil ikkestøttetMessage(Class<? extends Message> klasse);
    }
}
