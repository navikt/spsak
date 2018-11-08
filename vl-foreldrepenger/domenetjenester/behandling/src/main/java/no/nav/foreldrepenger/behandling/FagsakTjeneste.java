package no.nav.foreldrepenger.behandling;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface FagsakTjeneste {

    // FIXME Marius ønsker endre interface på denne, men løser slikt inntil videre
    void oppdaterFagsak(Behandling behandling, PersonopplysningerAggregat nyPersonopplysninger, List<Personopplysning> barnSøktStønadFor);

    void opprettFagsak(Fagsak nyFagsak, Personinfo personInfo);

    Optional<Fagsak> finnFagsakGittSaksnummer(Saksnummer saksnummer, boolean taSkriveLås);

    Fagsak finnEksaktFagsak(long fagsakId);

    void oppdaterFagsakMedGsakSaksnummer(Long fagsakId, Saksnummer saksnummer);

    void lagreJournalPost(Journalpost journalpost);

    Optional<Journalpost> hentJournalpost(JournalpostId journalpostId);

}
