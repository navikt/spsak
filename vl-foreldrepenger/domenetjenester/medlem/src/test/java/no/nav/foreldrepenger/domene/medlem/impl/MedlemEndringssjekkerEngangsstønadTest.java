package no.nav.foreldrepenger.domene.medlem.impl;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;

public class MedlemEndringssjekkerEngangsstønadTest {
    private MedlemEndringssjekker endringssjekker;

    @Before
    public void before() {
        endringssjekker = new MedlemEndringssjekkerEngangsstønad();
    }


    @Test
    public void skal_finne_endring_når_det_kommer_en_ekstra_medlemskapsperiode() {
        // Arrange
        RegistrertMedlemskapPerioder periode1 = new MedlemskapPerioderBuilder()
            .medMedlId(1L)
            .build();
        RegistrertMedlemskapPerioder periode2 = new MedlemskapPerioderBuilder()
            .medMedlId(2L)
            .build();

        // Act
        boolean endringsresultat = endringssjekker.erEndret(Optional.empty(), asList(periode1), asList(periode1, periode2));

        // Assert
        assertThat(endringsresultat).isTrue();
    }

    @Test
    public void skal_finne_endring_når_det_forsvinner_en_medlemskapsperiode() {
        // Arrange
        RegistrertMedlemskapPerioder periode1 = new MedlemskapPerioderBuilder()
            .medMedlId(1L)
            .build();
        RegistrertMedlemskapPerioder periode2 = new MedlemskapPerioderBuilder()
            .medMedlId(2L)
            .build();

        // Act
        boolean endringsresultat = endringssjekker.erEndret(Optional.empty(), asList(periode1, periode2), asList(periode1));

        // Assert
        assertThat(endringsresultat).isTrue();
    }

    @Test
    public void skal_finne_endring_når_en_medlemskapsperiode_endres() {
        // Arrange
        RegistrertMedlemskapPerioder periode1 = new MedlemskapPerioderBuilder()
            .medMedlId(1L)
            .medErMedlem(true)
            .build();
        RegistrertMedlemskapPerioder periode2 = new MedlemskapPerioderBuilder()
            .medMedlId(1L)
            .medErMedlem(false)
            .build();

        // Act
        boolean endringsresultat = endringssjekker.erEndret(Optional.empty(), asList(periode1), asList(periode2));

        // Assert
        assertThat(endringsresultat).isTrue();
    }


}
