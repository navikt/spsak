package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.exceptions.Valideringsfeil;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEndringsøknadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;

public class ManuellRegistreringValidator {

    private ManuellRegistreringValidator() {
        // Klassen skal ikke instansieres
    }

    public static void validerOpplysninger(ManuellRegistreringDto registreringDto) {
        List<FeltFeilDto> feil = new ArrayList<>();
        if (registreringDto instanceof ManuellRegistreringEndringsøknadDto) {
            //Valideringer på endringssøknaden plugges inn her
            feil.addAll(ManuellRegistreringEndringssøknadValidator.validerOpplysninger((ManuellRegistreringEndringsøknadDto) registreringDto));
        } else {
            //Valider felles felter mellom engangstønad og foreldrepenger
            feil.addAll(ManuellRegistreringFellesValidator.validerOpplysninger(registreringDto));
            if (registreringDto instanceof ManuellRegistreringForeldrepengerDto) {
                feil.addAll(ManuellRegistreringForeldrepengerValidator.validerOpplysninger((ManuellRegistreringForeldrepengerDto) registreringDto));
            }
        }

        if (!feil.isEmpty()) {
            throw new Valideringsfeil(feil);
        }
    }
}
