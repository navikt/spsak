package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class SjekkMotEksisterendeOppgaverTjeneste {

    private HistorikkRepository historikkRepository;

    SjekkMotEksisterendeOppgaverTjeneste() {
        //CDI proxy
    }

    @Inject
    public SjekkMotEksisterendeOppgaverTjeneste(HistorikkRepository historikkRepository) {
        this.historikkRepository = historikkRepository;
    }

    public List<AksjonspunktDefinisjon> sjekkMotEksisterendeGsakOppgaver(AktørId aktørid, Behandling behandling) {

        if (!måVurdereAnnenYtelseFørVedtak(behandling) && !måVurdereDokumentFørVedtak(behandling)) {
            return new ArrayList<>();
        }

        List<Historikkinnslag> historikkInnslagFraRepo = historikkRepository.hentHistorikk(behandling.getId());
        List<AksjonspunktDefinisjon> aksjonspunktliste = new ArrayList<>();

        if (måVurdereAnnenYtelseFørVedtak(behandling)) {
            aksjonspunktliste.add(AksjonspunktDefinisjon.VURDERE_ANNEN_YTELSE_FØR_VEDTAK);
            opprettHistorikkinnslagOmVurderingFørVedtak(behandling, "Vurder konsekvens for annen ytelse", historikkInnslagFraRepo);
        }
        if (måVurdereDokumentFørVedtak(behandling)) {
            aksjonspunktliste.add(AksjonspunktDefinisjon.VURDERE_DOKUMENT_FØR_VEDTAK);
            opprettHistorikkinnslagOmVurderingFørVedtak(behandling, "Vurder dokument", historikkInnslagFraRepo);
        }
        return aksjonspunktliste;
    }

    private boolean måVurdereAnnenYtelseFørVedtak(Behandling behandling) {
        return behandling.getAksjonspunkter()
            .stream()
            .anyMatch(ap -> harAksjonspunktSomIkkeErUtført(ap, AksjonspunktDefinisjon.VURDERE_ANNEN_YTELSE_FØR_VEDTAK));
    }

    private boolean måVurdereDokumentFørVedtak(Behandling behandling) {
        return behandling.getAksjonspunkter()
            .stream()
            .anyMatch(ap -> harAksjonspunktSomIkkeErUtført(ap, AksjonspunktDefinisjon.VURDERE_DOKUMENT_FØR_VEDTAK));
    }

    private boolean harAksjonspunktSomIkkeErUtført(Aksjonspunkt ap, AksjonspunktDefinisjon vurdereAnnenYtelseFørVedtak) {
        return ap.getAksjonspunktDefinisjon().equals(vurdereAnnenYtelseFørVedtak) && !ap.erUtført();
    }

    private void opprettHistorikkinnslagOmVurderingFørVedtak(Behandling behandling, String begrunnelse, List<Historikkinnslag> historikkInnslagFraRepo) {
        // finne historikkinnslag hvor vi har en begrunnelse?
        List<Historikkinnslag> eksisterendeVurderHistInnslag = historikkInnslagFraRepo.stream()
            .filter(historikkinnslag -> {
                List<HistorikkinnslagDel> historikkinnslagDeler = historikkinnslag.getHistorikkinnslagDeler();
                return historikkinnslagDeler.stream().anyMatch(del -> del.getBegrunnelse().isPresent());
            })
            .collect(Collectors.toList());

        if (eksisterendeVurderHistInnslag.isEmpty()) {
            Historikkinnslag vurderFørVedtakInnslag = new Historikkinnslag();
            vurderFørVedtakInnslag.setType(HistorikkinnslagType.BEH_AVBRUTT_VUR);
            vurderFørVedtakInnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
            HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = new HistorikkInnslagTekstBuilder()
                .medHendelse(HistorikkinnslagType.BEH_AVBRUTT_VUR)
                .medBegrunnelse(begrunnelse);
            historikkInnslagTekstBuilder.build(vurderFørVedtakInnslag);
            vurderFørVedtakInnslag.setBehandling(behandling);
            historikkRepository.lagre(vurderFørVedtakInnslag);
        }
    }
}
