package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.INNSYN;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType.KLAGE;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@DokumentGruppeRef("KLAGE")
class DokumentmottakerKlage implements Dokumentmottaker {

    private static final Logger logger = LoggerFactory.getLogger(DokumentmottakerKlage.class);

    private BehandlingRepository behandlingRepository;
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private DokumentmottakerFelles dokumentmottakerFelles;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;
    private KodeverkRepository kodeverkRepository;

    @Inject
    public DokumentmottakerKlage(BehandlingRepositoryProvider repositoryProvider, BehandlingskontrollTjeneste behandlingskontrollTjeneste, DokumentmottakerFelles dokumentmottakerFelles, MottatteDokumentTjeneste mottatteDokumentTjeneste) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.dokumentmottakerFelles = dokumentmottakerFelles;
        this.mottatteDokumentTjeneste = mottatteDokumentTjeneste;
    }

    @Override
    public void mottaDokument(MottattDokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType) {
        Optional<Behandling> sisteYtelsesbehandling = behandlingRepository
            .hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(fagsak.getId(), asList(KLAGE, INNSYN));
        boolean sisteYtelseErUavsluttet = sisteYtelsesbehandling.map(it -> !it.erAvsluttet()).orElse(Boolean.FALSE);

        if (sisteYtelseErUavsluttet && sisteYtelsesbehandling.isPresent()) {
            mottatteDokumentTjeneste.oppdaterMottattDokumentMedBehandling(mottattDokument, sisteYtelsesbehandling.get().getId());
            dokumentmottakerFelles.opprettTaskForÅVurdereDokument(fagsak, sisteYtelsesbehandling.get(), mottattDokument);
        } else {
            startBehandlingAvKlage(mottattDokument, fagsak);
        }
    }

    @Override
    public void mottaDokumentForKøetBehandling(MottattDokument mottattDokument, Fagsak fagsak, DokumentTypeId dokumentTypeId, BehandlingÅrsakType behandlingÅrsakType) {
        throw new UnsupportedOperationException("Ikke implementert mottak av klagedokument for køet behandling");
    }

    void startBehandlingAvKlage(MottattDokument mottattDokument, Fagsak fagsak) {
        opprettKlagebehandling(fagsak).ifPresent(behandling -> {
            mottatteDokumentTjeneste.persisterDokumentinnhold(behandling, mottattDokument, Optional.empty());
            dokumentmottakerFelles.opprettTaskForÅStarteBehandling(behandling);
            dokumentmottakerFelles.opprettHistorikk(behandling, mottattDokument.getJournalpostId());
        });
    }


    private Optional<Behandling> opprettKlagebehandling(Fagsak fagsak) {
        Optional<Behandling> forrigeOpt = behandlingRepository.
            hentSisteBehandlingForFagsakIdEkskluderBehandlingerAvType(fagsak.getId(), asList(KLAGE, INNSYN));
        if (!forrigeOpt.isPresent()) {
            Feilene.FACTORY.finnerIkkeEksisterendeBehandling(fagsak.getSaksnummer().toString()).log(logger);
            return Optional.empty();
        }
        Behandling behandlingKlagenGjelder = forrigeOpt.get();
        BehandlingType behandlingTypeKlage = kodeverkRepository.finn(BehandlingType.class, BehandlingType.KLAGE);
        return Optional.ofNullable(behandlingskontrollTjeneste.opprettNyBehandling(fagsak, behandlingTypeKlage,
            (beh) -> {
                beh.setBehandlingstidFrist(LocalDate.now(FPDateUtil.getOffset()).plusWeeks(behandlingTypeKlage.getBehandlingstidFristUker()));
                beh.setBehandlendeEnhet(dokumentmottakerFelles.utledEnhetFraTidligereBehandling(beh, behandlingKlagenGjelder));
            }));
    }

    interface Feilene extends DeklarerteFeil {
        Feilene FACTORY = FeilFactory.create(Feilene.class);

        @TekniskFeil(feilkode = "FP-683421", feilmelding = "Fant ingen passende behandling for saksnummer '%s'", logLevel = WARN)
        Feil finnerIkkeEksisterendeBehandling(String saksnummer);

    }
}
