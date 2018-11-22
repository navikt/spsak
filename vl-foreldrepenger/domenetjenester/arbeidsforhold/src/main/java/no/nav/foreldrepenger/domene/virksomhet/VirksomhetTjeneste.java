package no.nav.foreldrepenger.domene.virksomhet;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;

public interface VirksomhetTjeneste {

    /**
     * Henter informasjon fra Enhetsregisteret hvis applikasjonen ikke kjenner til orgnr eller har data som er eldre enn 24 timer.
     *
     * @param orgNummer  orgnummeret
     * @return relevant informasjon om virksomheten.
     * @throws IllegalArgumentException ved forespørsel om orgnr som ikke finnes i enhetsreg
     */
    Virksomhet hentOgLagreOrganisasjon(String orgNummer);


    /**
     * Henter informasjon fra databasen til VL.
     * Benyttes til DTO-tjenester.
     *
     * @param orgNummer orgnummeret
     * @return relevant informasjon om virksomheten.
     */
    Optional<Virksomhet> finnOrganisasjon(String orgNummer);
}
