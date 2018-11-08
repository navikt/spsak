package no.nav.foreldrepenger.web.app.validering;

import java.util.Objects;

import javax.validation.ConstraintValidatorContext;

import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;

public class KodeverkTabellValidator extends KodeverkValidator<KodeverkTabell> {

    @Override
    public boolean isValid(KodeverkTabell kodeverkTabell, ConstraintValidatorContext context) {
        if(Objects.equals(null, kodeverkTabell)) {
            return true;
        }
        boolean ok = true;

        if(!gyldigKode(kodeverkTabell.getKode())) {
            context.buildConstraintViolationWithTemplate(invKode);
            ok = false;
        }

        if(!gyldigNavn(kodeverkTabell.getNavn())) {
            context.buildConstraintViolationWithTemplate(invNavn);
            ok = false;
        }


        return ok;
    }
}
