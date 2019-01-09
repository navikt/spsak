package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.PayloadType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class DokumentmottakTestUtil {

    static InngåendeSaksdokument byggMottattDokument(DokumentTypeId dokumentTypeId, Long fagsakId, String xml, LocalDate mottattDato, boolean elektroniskRegistrert, String journalpostId) {
        InngåendeSaksdokument.Builder builder = new InngåendeSaksdokument.Builder();
        builder.medDokumentTypeId(dokumentTypeId);
        builder.medMottattDato(mottattDato);
        builder.medPayload(PayloadType.XML, xml);
        builder.medFagsakId(fagsakId);
        if (journalpostId != null) {
            builder.medJournalpostId(new JournalpostId(journalpostId));
        }
        return builder.build();
    }

    static Fagsak byggFagsak(AktørId aktørId, Saksnummer saksnummer, FagsakRepository fagsakRepository) {
        NavBruker navBruker = NavBruker.opprettNy(aktørId);
        Fagsak fagsak = FagsakBuilder.nyFagsak()
            .medSaksnummer(saksnummer)
            .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
        return fagsak;
    }

}
