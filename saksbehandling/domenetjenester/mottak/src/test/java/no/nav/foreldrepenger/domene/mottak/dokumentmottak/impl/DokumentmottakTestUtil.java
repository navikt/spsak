package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.PayloadType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class DokumentmottakTestUtil {

    public static BehandlingskontrollTjeneste lagBehandlingskontrollTjenesteMock(BehandlingRepositoryProvider repositoryProvider, BehandlingModellRepository behandlingModellRepository) {
        BehandlingskontrollTjeneste behandlingskontrollTjeneste = new BehandlingskontrollTjenesteImpl(repositoryProvider, behandlingModellRepository,
            null) {
            @Override
            protected void fireEventBehandlingStegOvergang(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                           Optional<BehandlingStegTilstand> forrigeTilstand, Optional<BehandlingStegTilstand> nyTilstand, boolean erOverstyring) {
                // NOOP
            }
            @Override
            public BehandlingStegTilstand prosesserBehandling(BehandlingskontrollKontekst kontekst) {
                // NOOP
                return null;
            }
        };
        return behandlingskontrollTjeneste;
    }

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

    static InngåendeSaksdokument byggMottattPapirsøknad(DokumentTypeId dokumentTypeId, Long fagsakId, String xml, LocalDate mottattDato, boolean elektroniskRegistrert, String journalpostId) {
        InngåendeSaksdokument.Builder builder = new InngåendeSaksdokument.Builder();
        builder.medDokumentTypeId(dokumentTypeId);
        builder.medDokumentKategori(DokumentKategori.SØKNAD);
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

    public static BehandlingVedtak oppdaterVedtaksresultat(Behandling origBehandling, VedtakResultatType vedtakResultatType) {
        BehandlingVedtak vedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(vedtakResultatType)
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(origBehandling.getBehandlingsresultat())
            .medAnsvarligSaksbehandler("Severin Saksbehandler")
            .build();

        return vedtak;
    }
}
