package no.nav.foreldrepenger.web.app.validering;

import java.util.Objects;

import javax.validation.ConstraintValidatorContext;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

public class KodelisteValidator extends KodeverkValidator<Kodeliste> {

    @Override
    public boolean isValid(Kodeliste kodeliste, ConstraintValidatorContext context) {
        if (Objects.equals(null, kodeliste)) {
            return true;
        }
        boolean ok = true;

        if (!gyldigKode(kodeliste.getKode())) {
            context.buildConstraintViolationWithTemplate(invKode);
            ok = false;
        }

        if (!gyldigKodeverk(kodeliste.getKodeverk())) {
            context.buildConstraintViolationWithTemplate(invNavn);
            ok = false;
        }

        return ok;
    }
}
