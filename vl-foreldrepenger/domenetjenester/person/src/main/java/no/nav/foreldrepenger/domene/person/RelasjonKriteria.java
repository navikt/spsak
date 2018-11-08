package no.nav.foreldrepenger.domene.person;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;

public class RelasjonKriteria {
    private Optional<RelasjonsRolleType> relasjonsrolle = Optional.empty();
    private Optional<LocalDate> fødselsdato = Optional.empty();

    private RelasjonKriteria() {
    }

    public static RelasjonKriteria opprett() {
        return new RelasjonKriteria();
    }

    public RelasjonKriteria medRelasjonsrolle(RelasjonsRolleType relasjonsrolle) {
        this.relasjonsrolle = relasjonsrolle == null ? null : Optional.of(relasjonsrolle);
        return this;
    }

    public RelasjonKriteria medFødselsdato(LocalDate fødselsdato) {
        this.fødselsdato = fødselsdato == null ? null : Optional.of(fødselsdato);
        return this;
    }


    public Boolean erOppfyltAv(Familierelasjon familierelasjon) {
        if (relasjonsrolle == null || fødselsdato == null) { // NOSONAR - null-check Optional. Trenger 3 tilstander her, velger null, empty, present
            return Boolean.FALSE;
        }

        Boolean erRollerelasjonOppfylt = relasjonsrolle.map(rolle -> rolle.equals(familierelasjon.getRelasjonsrolle()))
            .orElse(true);
        Boolean erFødselsdatoOppfylt = fødselsdato.map(dato -> dato.equals(familierelasjon.getFødselsdato()))
            .orElse(true);
        return erRollerelasjonOppfylt && erFødselsdatoOppfylt;
    }
}
