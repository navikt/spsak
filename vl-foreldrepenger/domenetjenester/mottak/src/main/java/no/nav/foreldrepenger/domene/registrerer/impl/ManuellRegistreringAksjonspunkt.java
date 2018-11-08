package no.nav.foreldrepenger.domene.registrerer.impl;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.registrerer.ManuellRegistreringAksjonspunktDto;

class ManuellRegistreringAksjonspunkt {

    private KodeverkRepository kodeverkRepository;
    private MottatteDokumentRepository mottatteDokumentRepository;
    private AksjonspunktRepository aksjonspunktRepository;
    private OppgaveTjeneste oppgaveTjeneste;
    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;
    private DokumentPersistererTjeneste dokumentPersistererTjeneste;

    ManuellRegistreringAksjonspunkt(BehandlingRepositoryProvider repositoryProvidery,
                                    MottatteDokumentRepository mottatteDokumentRepository,
                                    OppgaveTjeneste oppgaveTjeneste,
                                    OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository, DokumentPersistererTjeneste dokumentPersistererTjeneste) {
        this.kodeverkRepository = repositoryProvidery.getKodeverkRepository();
        this.mottatteDokumentRepository = mottatteDokumentRepository;
        this.aksjonspunktRepository = repositoryProvidery.getAksjonspunktRepository();
        this.oppgaveTjeneste = oppgaveTjeneste;
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.dokumentPersistererTjeneste = dokumentPersistererTjeneste;
    }

    public void oppdater(Behandling behandling, ManuellRegistreringAksjonspunktDto adapter) {

        if (adapter.getErFullstendigSøknad()) {
            MottattDokument dokument = new MottattDokument.Builder()
                .medDokumentTypeId(finnDokumentType(adapter.getDokumentTypeIdKode()))
                .medDokumentKategori(kodeverkRepository.finn(DokumentKategori.class, DokumentKategori.SØKNAD))
                .medElektroniskRegistrert(false)
                .medMottattDato(adapter.getMottattDato())
                .medXmlPayload(adapter.getSøknadsXml())
                .medBehandlingId(behandling.getId())
                .medFagsakId(behandling.getFagsakId())
                .build();
            dokumentPersistererTjeneste.persisterDokumentinnhold(dokument, behandling);

            if (adapter.getErRegistrertVerge()) {
                aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.AVKLAR_VERGE, BehandlingStegType.REGISTRER_SØKNAD);
            }
            mottatteDokumentRepository.lagre(dokument);
        } else {
            avsluttTidligereRegistreringsoppgave(behandling);

            aksjonspunktRepository.leggTilAksjonspunkt(behandling, AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_MANU);
        }
    }


    private DokumentTypeId finnDokumentType(String kode) {
        return kodeverkRepository.finn(DokumentTypeId.class, kode);
    }

    private void avsluttTidligereRegistreringsoppgave(Behandling behandling) {
        List<OppgaveBehandlingKobling> oppgaver = oppgaveBehandlingKoblingRepository.hentOppgaverRelatertTilBehandling(behandling.getId());
        OppgaveBehandlingKobling.getAktivOppgaveMedÅrsak(OppgaveÅrsak.REGISTRER_SØKNAD, oppgaver)
            .ifPresent(aktivOppgave -> oppgaveTjeneste.opprettTaskAvsluttOppgave(behandling, aktivOppgave.getOppgaveÅrsak()));
    }
}
