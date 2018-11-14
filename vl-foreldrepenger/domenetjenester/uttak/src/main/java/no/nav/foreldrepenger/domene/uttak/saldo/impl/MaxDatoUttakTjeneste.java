package no.nav.foreldrepenger.domene.uttak.saldo.impl;

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;

class MaxDatoUttakTjeneste {

    public Optional<LocalDate> beregnMaxDatoUttak(Saldoer saldoer, RelasjonsRolleType relasjonsRolleType, Optional<LocalDate> sisteUttaksdato) {

        if (sisteUttaksdato.isPresent()) {
            if (relasjonsRolleType.equals(RelasjonsRolleType.MORA)) {
                return Optional.of(beregnMaksDato(saldoer, asList(StønadskontoType.MØDREKVOTE,
                    StønadskontoType.FELLESPERIODE,
                    StønadskontoType.FORELDREPENGER), sisteUttaksdato.get()));
            } else {
                return Optional.of(beregnMaksDato(saldoer, asList(StønadskontoType.FEDREKVOTE,
                    StønadskontoType.FELLESPERIODE,
                    StønadskontoType.FORELDREPENGER), sisteUttaksdato.get()));
            }
        }
        return Optional.empty();
    }

    private LocalDate beregnMaksDato(Saldoer saldoer, List<StønadskontoType> gyldigeStønadskontoer, LocalDate sisteUttaksdato) {
        int tilgjengeligeDager = 0;

        for (StønadskontoType stønadskonto : gyldigeStønadskontoer) {
            tilgjengeligeDager += saldoer.saldo(stønadskonto);
        }

        if (tilgjengeligeDager > 0) {
            return Virkedager.plusVirkedager(sisteUttaksdato, tilgjengeligeDager);
        }
        return sisteUttaksdato;
    }

}
