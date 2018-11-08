package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.validering;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.uttak.fastsetteperioder.UttakResultatPerioder;
import no.nav.vedtak.feil.FeilFactory;

class EndringerBareEtterEndringsdatoValidering implements OverstyrUttakPerioderValidering {

    @Override
    public void utfør(UttakResultatPerioder nyePerioder) {

        if (nyePerioder.getEndringsdato().isPresent()) {
            LocalDate endringsdato = nyePerioder.getEndringsdato().get();
            nyePerioder.getPerioder().stream().forEach(p -> {
                if (endringsdato.isAfter(p.getTidsperiode().getFomDato())) {
                    throw FeilFactory.create(OverstyrUttakValideringFeil.class).perioderFørEndringsdatoKanIkkeEndres().toException();
                }
            });
        }
    }

}
