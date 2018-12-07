package no.nav.foreldrepenger.behandlingslager.behandling.virksomhet;

import java.util.Optional;

public interface VirksomhetRepository {

    /**
     * Henter ut virksomheten detached fra context
     *
     * @param orgnr orgnr
     * @return virksomheten
     */
    Optional<Virksomhet> hent(String orgnr);

    /**
     * Henter ut elementet attachet til context
     *
     * @param orgnr orgnr
     * @return virksomheten
     */
    Optional<Virksomhet> hentForEditering(String orgnr);

    void lagre(Virksomhet virksomhet);
}
