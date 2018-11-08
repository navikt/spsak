package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class MottatteDokumentTjenesteImpl implements MottatteDokumentTjeneste {

    private Integer fristForInnsendingAvDokumentasjon;

    private DokumentPersistererTjeneste dokumentPersistererTjeneste;
    private MottatteDokumentRepository mottatteDokumentRepository;
    private BehandlingRepositoryProvider behandlingRepositoryProvider;

    MottatteDokumentTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public MottatteDokumentTjenesteImpl(@KonfigVerdi("sak.frist.innsending.dok.uker") Integer fristForInnsendingAvDokumentasjon, DokumentPersistererTjeneste dokumentPersistererTjeneste,
                                        MottatteDokumentRepository mottatteDokumentRepository, BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.fristForInnsendingAvDokumentasjon = fristForInnsendingAvDokumentasjon;
        this.dokumentPersistererTjeneste = dokumentPersistererTjeneste;
        this.mottatteDokumentRepository = mottatteDokumentRepository;
        this.behandlingRepositoryProvider = behandlingRepositoryProvider;
    }

    @Override
    public void persisterDokumentinnhold(Behandling behandling, MottattDokument dokument, Optional<LocalDate> gjelderFra){
        oppdaterMottattDokumentMedBehandling(dokument, behandling.getId());
        if (dokument.getPayloadXml() != null) {
            @SuppressWarnings("rawtypes")
            MottattDokumentWrapper dokumentWrapper = dokumentPersistererTjeneste.xmlTilWrapper(dokument);
            dokumentPersistererTjeneste.persisterDokumentinnhold(dokumentWrapper, dokument, behandling, gjelderFra);
        }
    }

    @Override
    public Long lagreMottattDokumentPåFagsak(Long fagsakId, MottattDokument dokument){
        MottattDokument mottattDokument = mottatteDokumentRepository.lagre(dokument);
        return mottattDokument.getId();
    }

    @Override
    public List<MottattDokument> hentMottatteDokument(Long behandlingId) {
        return mottatteDokumentRepository.hentMottatteDokument(behandlingId);
    }

    @Override
    public boolean harMottattDokumentSet(Long behandlingId, Set<DokumentTypeId> dokumentTypeIdSet) {
        return hentMottatteDokument(behandlingId).stream().anyMatch( dok -> dokumentTypeIdSet.contains(dok.getDokumentTypeId()));
    }

    @Override
    public boolean harMottattDokumentKat(Long behandlingId, DokumentKategori dokumentKategori) {
        return hentMottatteDokument(behandlingId).stream().anyMatch( dok -> dok.getDokumentKategori().equals(dokumentKategori));
    }

    @Override
    public List<MottattDokument> hentMottatteDokumentVedlegg(Long behandlingId) {
        return mottatteDokumentRepository.hentMottatteDokumentAndreTyperPåBehandlingId(behandlingId);
    }

    @Override
    public void oppdaterMottattDokumentMedBehandling(MottattDokument mottattDokument, Long behandlingId) {
        mottatteDokumentRepository.oppdaterMedBehandling(mottattDokument, behandlingId);
    }

    @Override
    public Optional<MottattDokument> hentMottattDokument(Long mottattDokumentId) {
        return mottatteDokumentRepository.hentMottattDokument(mottattDokumentId);
    }

    @Override
    public boolean erSisteYtelsesbehandlingAvslåttPgaManglendeDokumentasjon(Fagsak sak) {
        Objects.requireNonNull(sak, "Fagsak");
        Optional<Behandling> behandling = behandlingRepositoryProvider.getBehandlingRepository().finnSisteAvsluttedeIkkeHenlagteBehandling(sak.getId());
        if (behandling.isPresent()) {
            return erAvsluttetPgaManglendeDokumentasjon(behandling.get());
        }
        return false;
    }

    /**
     * Beregnes fra vedtaksdato
     */
    @Override
    public boolean harFristForInnsendingAvDokGåttUt(Fagsak sak) {
        Objects.requireNonNull(sak, "Fagsak");
        Optional<Behandling> behandlingOptional = behandlingRepositoryProvider.getBehandlingRepository().finnSisteAvsluttedeIkkeHenlagteBehandling(sak.getId());
        Behandling behandling = behandlingOptional.get(); // NOSONAR
        Optional<BehandlingVedtak> behandlingVedtak = behandlingRepositoryProvider.getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(behandling.getId());
        if (behandlingVedtak.isPresent()) {
            return behandlingVedtak.get().getVedtaksdato().isBefore(LocalDate.now().minusWeeks(fristForInnsendingAvDokumentasjon));
        }
        return false;
    }

    private boolean erAvsluttetPgaManglendeDokumentasjon(Behandling behandling) {
        Objects.requireNonNull(behandling, "Behandling");
        Optional<BehandlingVedtak> behandlingVedtak = behandlingRepositoryProvider.getBehandlingVedtakRepository().hentBehandlingvedtakForBehandlingId(behandling.getId());
        if (behandlingVedtak.isPresent()) {
            return Avslagsårsak.MANGLENDE_DOKUMENTASJON.equals(behandlingVedtak.get().getBehandlingsresultat().getAvslagsårsak());
        }
        return false;
    }

}
