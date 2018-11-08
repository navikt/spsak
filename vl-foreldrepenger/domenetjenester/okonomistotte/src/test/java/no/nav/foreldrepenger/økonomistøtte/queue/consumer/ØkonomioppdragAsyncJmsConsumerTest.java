package no.nav.foreldrepenger.økonomistøtte.queue.consumer;

import no.nav.foreldrepenger.økonomistøtte.api.ØkonomiKvittering;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomioppdragApplikasjonTjeneste;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;
import no.nav.vedtak.felles.integrasjon.jms.precond.DefaultDatabaseOppePreconditionChecker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ØkonomioppdragAsyncJmsConsumerTest {

    private static final long BEHANDLINGID = 802L;
    @Rule
    public final LogSniffer logSniffer = new LogSniffer();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private ØkonomioppdragApplikasjonTjeneste økonomioppdragApplikasjonTjeneste;

    private ArgumentCaptor<ØkonomiKvittering> captor;
    private ØkonomioppdragAsyncJmsConsumer økonomioppdragAsyncJmsConsumer;

    @Before
    public void setUp() throws Exception {
        final DefaultDatabaseOppePreconditionChecker mockDefaultDatabaseOppePreconditionChecker = mock(DefaultDatabaseOppePreconditionChecker.class);
        final BaseJmsKonfig jmsKonfig = new BaseJmsKonfig("qu");
        jmsKonfig.setQueueName("asdf");
        jmsKonfig.setQueueManagerChannelName("asdf");
        jmsKonfig.setQueueManagerHostname("asdf");
        økonomioppdragAsyncJmsConsumer = new ØkonomioppdragAsyncJmsConsumer(økonomioppdragApplikasjonTjeneste, mockDefaultDatabaseOppePreconditionChecker, jmsKonfig);
        captor = ArgumentCaptor.forClass(ØkonomiKvittering.class);
    }

    @Test(expected = TekniskException.class)
    public void testHandleMessageWithUnparseableMessage() throws JMSException, IOException, URISyntaxException {
        // Arrange
        TextMessage message = opprettKvitteringXml("parsingFeil.xml");

        // Act
        økonomioppdragAsyncJmsConsumer.handle(message);
    }

    @Test
    public void testHandleMessageWithStatusOk() throws JMSException, IOException, URISyntaxException {
        // Arrange
        TextMessage message = opprettKvitteringXml("statusOk.xml");

        // Act
        økonomioppdragAsyncJmsConsumer.handle(message);

        // Assert
        verify(økonomioppdragApplikasjonTjeneste).behandleKvittering(captor.capture());
        ØkonomiKvittering kvittering = captor.getValue();
        assertThat(kvittering).isNotNull();
        verifiserKvittering(kvittering, "00", null, BEHANDLINGID, "Oppdrag behandlet");
    }

    @Test
    public void testHandleMessageWithStatusFeil() throws JMSException, IOException, URISyntaxException {
        // Arrange
        TextMessage message = opprettKvitteringXml("statusFeil.xml");

        // Act
        økonomioppdragAsyncJmsConsumer.handle(message);

        // Assert
        verify(økonomioppdragApplikasjonTjeneste).behandleKvittering(captor.capture());
        ØkonomiKvittering kvittering = captor.getValue();
        assertThat(kvittering).isNotNull();
        verifiserKvittering(kvittering, "08", "B110006F", 341L, "UTBET-FREKVENS har en ugyldig verdi: ENG");
    }

    private TextMessage opprettKvitteringXml(String filename) throws JMSException, IOException, URISyntaxException {
        TextMessage textMessage = mock(TextMessage.class);
        String xml = getInputXML("xml/" + filename);
        when(textMessage.getText()).thenReturn(xml);
        return textMessage;
    }

    private String getInputXML(String filename) throws IOException, URISyntaxException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private void verifiserKvittering(ØkonomiKvittering kvittering, String alvorlighetsgrad, String meldingKode, Long behandlingId, String beskrMelding) {
        assertThat(kvittering.getAlvorlighetsgrad()).isEqualTo(alvorlighetsgrad);
        assertThat(kvittering.getMeldingKode()).isEqualTo(meldingKode);
        assertThat(kvittering.getBehandlingId()).isEqualTo(behandlingId);
        assertThat(kvittering.getBeskrMelding()).isEqualTo(beskrMelding);
    }
}
