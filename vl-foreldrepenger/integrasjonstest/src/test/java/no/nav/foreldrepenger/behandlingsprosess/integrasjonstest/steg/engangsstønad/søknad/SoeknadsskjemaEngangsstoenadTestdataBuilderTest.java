package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad.søknad;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class SoeknadsskjemaEngangsstoenadTestdataBuilderTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void skal_opprette_søknadsskjema_for_adopsjon_engangsstønad_mor() throws Exception {
        // Act
        SoeknadsskjemaEngangsstoenad søknadsskjema = new SoeknadsskjemaEngangsstoenadTestdataBuilder()
                .søknadAdopsjonEngangsstønadMor()
                .build();

        // Assert
        assertThat(søknadsskjema.getBruker()).isNotNull();
        assertThat(søknadsskjema.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(1);
        assertThat(søknadsskjema.getOpplysningerOmBarn().getOmsorgsovertakelsedato()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(LocalDate.now().plusDays(2)));
        assertThat(søknadsskjema.getVedleggListe().getVedlegg().size()).isEqualTo(1);
    }

    @Test
    public void skal_oppdatere_antall_barn() throws Exception {
        // Act
        SoeknadsskjemaEngangsstoenad søknadsskjema = new SoeknadsskjemaEngangsstoenadTestdataBuilder()
                .søknadAdopsjonEngangsstønadMor()
                .medFødselsdatoer(Arrays.asList(LocalDate.now(), LocalDate.now().plusDays(1)))
                .build();

        // Assert
        assertThat(søknadsskjema.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(2);
    }

    @Test
    public void skal_sette_antall_barn_til_1_hvis_ikke_annet_er_spesifisert() throws Exception {
        // Act
        SoeknadsskjemaEngangsstoenad søknadsskjema = new SoeknadsskjemaEngangsstoenadTestdataBuilder()
                .fødsel()
                .engangsstønadMor()
                .build();

        // Assert
        assertThat(søknadsskjema.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(1);
    }

    @Test
    public void skal_opprette_søknadsskjema_for_fødsel_engangsstønad_mor() throws Exception {
        // Act
        SoeknadsskjemaEngangsstoenad søknadsskjema = new SoeknadsskjemaEngangsstoenadTestdataBuilder()
                .søknadFødselTerminEngangsstønadMor()
                .build();

        // Assert
        assertThat(søknadsskjema.getBruker()).isNotNull();
        assertThat(søknadsskjema.getOpplysningerOmBarn().getAntallBarn()).isEqualTo(1);
        assertThat(søknadsskjema.getVedleggListe().getVedlegg().size()).isEqualTo(1);
    }

    @Test
    public void skal_gi_feil_hvis_søknadsvalg_ikke_er_satt() throws Exception {
        // Assert
        expectedException.expect(IllegalArgumentException.class);

        // Act
        new SoeknadsskjemaEngangsstoenadTestdataBuilder().build();
    }
}
