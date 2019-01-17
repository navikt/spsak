package no.nav.foreldrepenger.mottak.queue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MottakAsyncJmsConsumer { /*extends InternalQueueConsumer {

    private MeldingsFordeler meldingsFordeler;
    private DefaultDatabaseOppePreconditionChecker preconditionChecker;

    public MottakAsyncJmsConsumer() {
        // CDI
    }

    @Inject
    public MottakAsyncJmsConsumer(MeldingsFordeler meldingsFordeler,
                                  DefaultDatabaseOppePreconditionChecker preconditionChecker,
                                  @Named("mottak") JmsKonfig konfige) {
        super(konfige);
        this.meldingsFordeler = meldingsFordeler;
        this.preconditionChecker = preconditionChecker;
    }

    @Override
    public PreconditionChecker getPreconditionChecker() {
        return preconditionChecker;
    }

    @Override
    public void handle(Message message) throws JMSException {
        if (message instanceof TextMessage) {
            String messageText = ((TextMessage) message).getText();
            Forsendelsesinformasjon forsendelsesinfo = parseMessage(messageText);
            meldingsFordeler.execute(forsendelsesinfo);
        } else {
            FeilFactory.create(Feilene.class).ikkestøttetMessage(message.getClass()).log(log);
        }
    }

    interface Feilene extends DeklarerteFeil {
        @TekniskFeil(feilkode = "FP-476872", feilmelding = "Mottok på ikkestøttet message av klasse %s. Kø-elementet ble ignorert", logLevel = WARN)
        Feil ikkestøttetMessage(Class<? extends Message> klasse);
    }

    public static Forsendelsesinformasjon parseMessage(String messageText) {

        Forsendelsesinformasjon forsendelsesinfo;
        try {
            forsendelsesinfo = JaxbHelper.unmarshalAndValidateXMLWithStAX(Forsendelsesinformasjon.class, messageText,
                    "xsd/dokumentnotifikasjon/dokumentnotifikasjon-v1.xsd");
        } catch (JAXBException e) {
            throw MottakMeldingFeil.FACTORY.uventetFeilVedProsesseringAvForsendelsesInfoXMLMedJaxb(e).toException();
        } catch (SAXException | XMLStreamException e) {
            throw MottakMeldingFeil.FACTORY.uventetFeilVedProsesseringAvForsendelsesInfoXML(e).toException();
        }
        return forsendelsesinfo;
    }

    @Override
    @Resource(mappedName = "jms/ConnectionFactory")
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        super.setConnectionFactory(connectionFactory);
    }

    @Override
    @Resource(mappedName = "jms/QueueMottak")
    public void setQueue(Queue queue) {
        super.setQueue(queue);
    }*/
}
