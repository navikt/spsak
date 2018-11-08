package no.nav.vedtak.felles.integrasjon.felles.ws;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.qos.logback.classic.Level;
import no.nav.modig.core.test.LogSniffer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class VLFaultListenerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public LogSniffer sniffer = new LogSniffer(Level.WARN);

    private VLFaultListener faultListener = new VLFaultListener();


    @Test
    public void skal_logge_deklarert_exception_med_error() throws Exception {
        faultListener.faultOccurred(FeilFactory.create(TestFeil.class).alvorligFeil().toException(), "baz", null);

        sniffer.assertHasErrorMessage("TESTFEIL-1:Noe er alvorlig galt!");
    }

    @Test
    public void skal_logge_deklarert_exception_med_warning() throws Exception {
        faultListener.faultOccurred(FeilFactory.create(TestFeil.class).ganskeFeil().toException(), "baz", null);

        sniffer.assertHasWarnMessage("TESTFEIL-2:Noe er galt!");
    }

    @Test
    public void skal_logge_uventet_exception() throws Exception {
        faultListener.faultOccurred(new IllegalArgumentException("foobar"), "baz", null);

        sniffer.assertHasErrorMessage("Uventet exception: baz", IllegalArgumentException.class);
    }

    @Test
    public void skal_ikke_logge_exception_som_har_type_som_er_unntatt_loggging() throws Exception {
        faultListener.leggTilUnntak(new UnntakKonfigurasjon(IllegalArgumentException.class));
        faultListener.faultOccurred(new IllegalArgumentException("foobar"), "baz", null);

        sniffer.assertNoErrorsOrWarnings();
    }

    @Test
    public void skal_ikke_være_lov_å_unnta_noe_som_arver_fra_VLException_da_er_det_bedre_å_justere_loglevel() throws Exception {
        expectedException.expect(IllegalArgumentException.class);

        faultListener.leggTilUnntak(new UnntakKonfigurasjon(TekniskException.class));
    }

    interface TestFeil extends DeklarerteFeil {
        @TekniskFeil(feilkode = "TESTFEIL-1", feilmelding = "Noe er alvorlig galt!", logLevel = LogLevel.ERROR)
        Feil alvorligFeil();

        @TekniskFeil(feilkode = "TESTFEIL-2", feilmelding = "Noe er galt! ", logLevel = LogLevel.WARN)
        Feil ganskeFeil();
    }

    private static class UnntakKonfigurasjon extends VLFaultListenerUnntakKonfigurasjon {
        UnntakKonfigurasjon(Class<? extends Exception> klasse) {
            super(klasse);
        }
    }


}
