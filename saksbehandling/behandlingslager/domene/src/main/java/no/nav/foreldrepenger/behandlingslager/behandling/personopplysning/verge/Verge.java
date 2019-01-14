package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;

public interface Verge {

    VergeType getVergeType();

    LocalDate getVedtaksdato();

    LocalDate getGyldigFom();

    LocalDate getGyldigTom();

    String getMandatTekst();

    NavBruker getBruker();

    boolean getStønadMottaker();

    BrevMottaker getBrevMottaker();

}
