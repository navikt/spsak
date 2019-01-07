package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentGruppe;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRevurderingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;

@Dependent
public class InnhentDokumentTjenesteImpl implements InnhentDokumentTjeneste {

    private static Map<DokumentTypeId, DokumentGruppe> DOKUMENTTYPE_TIL_GRUPPE = new HashMap<>();
    private static Map<DokumentKategori, DokumentGruppe> DOKUMENTKATEGORI_TIL_GRUPPE = new HashMap<>();

    static {
        // Søknad
        DOKUMENTTYPE_TIL_GRUPPE.put(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, DokumentGruppe.SØKNAD);
        DOKUMENTTYPE_TIL_GRUPPE.put(DokumentTypeId.INNTEKTSMELDING, DokumentGruppe.INNTEKTSMELDING);
        // Endringssøknad
        DOKUMENTTYPE_TIL_GRUPPE.put(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, DokumentGruppe.ENDRINGSSØKNAD);
    }

    static {
        DOKUMENTKATEGORI_TIL_GRUPPE.put(DokumentKategori.SØKNAD, DokumentGruppe.SØKNAD);
        DOKUMENTKATEGORI_TIL_GRUPPE.put(DokumentKategori.KLAGE_ELLER_ANKE, DokumentGruppe.KLAGE);
    }

    private Instance<Dokumentmottaker> mottakere;

    private FagsakRepository fagsakRepository;
    private BehandlingRevurderingRepository revurderingRepository;

    @Inject
    public InnhentDokumentTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
                                       @Any Instance<Dokumentmottaker> mottakere,
                                       BehandlingRevurderingRepository revurderingRepository) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.mottakere = mottakere;
        this.revurderingRepository = revurderingRepository;
    }

    @Override
    public void utfør(InngåendeSaksdokument mottattDokument, BehandlingÅrsakType behandlingÅrsakType) {
        Fagsak fagsak = fagsakRepository.finnEksaktFagsak(mottattDokument.getFagsakId());
        DokumentTypeId dokumentTypeId = mottattDokument.getDokumentTypeId();

        DokumentGruppe dokumentGruppe = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ?
            DOKUMENTKATEGORI_TIL_GRUPPE.getOrDefault(mottattDokument.getDokumentKategori(), DokumentGruppe.VEDLEGG) :
            DOKUMENTTYPE_TIL_GRUPPE.getOrDefault(dokumentTypeId, DokumentGruppe.VEDLEGG);

        Dokumentmottaker dokumentmottaker = finnMottaker(dokumentGruppe);
        if (finnesÅpenBehandlingSomErBerørt(fagsak)) {
            dokumentmottaker.mottaDokumentForKøetBehandling(mottattDokument, fagsak, dokumentTypeId, behandlingÅrsakType);
            return;
        }
        dokumentmottaker.mottaDokument(mottattDokument, fagsak, dokumentTypeId, behandlingÅrsakType);
    }

    private boolean finnesÅpenBehandlingSomErBerørt(Fagsak fagsak) {
        Optional<Behandling> åpenBehandling = revurderingRepository.finnÅpenYtelsesbehandling(fagsak.getId());
        if (åpenBehandling.isPresent()) {
            return åpenBehandling
                .filter(beh -> beh.getBehandlingÅrsaker().stream()
                    .anyMatch(bå -> BehandlingÅrsakType.BERØRT_BEHANDLING.equals(bå.getBehandlingÅrsakType())))
                .isPresent();
        }
        return false;
    }

    private Dokumentmottaker finnMottaker(DokumentGruppe dokumentGruppe) {
        String dokumentgruppeKode = dokumentGruppe.getKode();
        Instance<Dokumentmottaker> selected = mottakere.select(new DokumentGruppeRef.DokumentGruppeRefLiteral(dokumentgruppeKode));
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for Dokumentmottaker: " + dokumentgruppeKode);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for Dokumentmottaker: " + dokumentgruppeKode);
        }
        Dokumentmottaker minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }

}
