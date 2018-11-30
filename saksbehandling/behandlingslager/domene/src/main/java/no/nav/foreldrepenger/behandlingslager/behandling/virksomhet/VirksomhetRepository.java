package no.nav.foreldrepenger.behandlingslager.behandling.virksomhet;

import java.util.Optional;

public interface VirksomhetRepository {

    Optional<Virksomhet> hent(String orgnr);

    void lagre(Virksomhet virksomhet);
}
