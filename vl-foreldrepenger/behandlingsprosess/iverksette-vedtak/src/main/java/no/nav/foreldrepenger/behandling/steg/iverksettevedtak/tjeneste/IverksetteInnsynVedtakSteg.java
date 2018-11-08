package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.tjeneste;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.IverksetteVedtakSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;

@BehandlingStegRef(kode = "IVEDSTEG")
@BehandlingTypeRef("BT-006") //Innsyn
@FagsakYtelseTypeRef
@ApplicationScoped
public class IverksetteInnsynVedtakSteg implements IverksetteVedtakSteg {

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerTjeneste;
    private BehandlingRepository behandlingRepository;

    IverksetteInnsynVedtakSteg() {
        // for CDI proxy
    }

    @Inject
    IverksetteInnsynVedtakSteg(DokumentBestillerApplikasjonTjeneste dokumentBestillerTjeneste, BehandlingRepositoryProvider repositoryProvider) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.dokumentBestillerTjeneste = dokumentBestillerTjeneste;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        bestillVedtaksbrev(kontekst);
        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void bestillVedtaksbrev(BehandlingskontrollKontekst kontekst) {
        dokumentBestillerTjeneste.bestillDokument(brevDto(kontekst), HistorikkAktør.VEDTAKSLØSNINGEN);
    }

    private BestillBrevDto brevDto(BehandlingskontrollKontekst kontekst) {
        Aksjonspunkt ap = behandlingRepository.hentBehandling(kontekst.getBehandlingId()).getAksjonspunktFor(AksjonspunktDefinisjon.FORESLÅ_VEDTAK);
        String begrunnelse = ap.getBegrunnelse();
        String fritekst = nullOrEmpty(begrunnelse) ? " " : begrunnelse;
        return new BestillBrevDto(kontekst.getBehandlingId(), DokumentMalType.INNSYNSKRAV_SVAR, fritekst);
    }

    private boolean nullOrEmpty(String begrunnelse) {
        return Objects.isNull(begrunnelse) || Objects.equals(begrunnelse, "");
    }

}
