package no.nav.foreldrepenger.behandling.steg.klage.impl;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.klage.KlageNfpSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.datavarehus.tjeneste.DatavarehusTjeneste;

@BehandlingStegRef(kode = "KLAGEUI")
@BehandlingTypeRef
@FagsakYtelseTypeRef
@ApplicationScoped
public class KlageNfpStegImpl implements KlageNfpSteg {

    private BehandlingRepository behandlingRepository;
    private DatavarehusTjeneste datavarehusTjeneste;
    private HistorikkRepository historikkRepository;

    public KlageNfpStegImpl(){
        // For CDI proxy
    }

    @Inject
    public KlageNfpStegImpl(BehandlingRepositoryProvider repositoryProvider, DatavarehusTjeneste datavarehusTjeneste,
                            HistorikkRepository historikkRepository){
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.datavarehusTjeneste = datavarehusTjeneste;
        this.historikkRepository = historikkRepository;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        List<AksjonspunktDefinisjon> aksjonspunktDefinisjons = singletonList(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NFP);

        return BehandleStegResultat.utførtMedAksjonspunkter(aksjonspunktDefinisjons);
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell,
                              TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType inngangUtgang) {
        if (Objects.equals(TransisjonType.HOPP_OVER_BAKOVER, transisjonType)) {
            behandlingRepository.slettKlageVurderingResultat(behandling, kontekst.getSkriveLås(), KlageVurdertAv.NFP);
            endreAnsvarligEnhetTilNFPVedTilbakeføringOgLagreHistorikkinnslag(behandling);
        }

    }

    private void endreAnsvarligEnhetTilNFPVedTilbakeføringOgLagreHistorikkinnslag(Behandling behandling) {

        Behandling sisteFørstegangsbehandling = behandlingRepository.hentSisteBehandlingForFagsakId(behandling.getFagsakId(),
            BehandlingType.FØRSTEGANGSSØKNAD).orElseThrow(() -> new IllegalStateException("Fant ingen behandling som passet for saksnummer: "
            + behandling.getFagsak().getSaksnummer()));

        if (behandling.getBehandlendeEnhet() != null && behandling.getBehandlendeEnhet().equals(sisteFørstegangsbehandling.getBehandlendeEnhet())) {
            return;
        }
        lagHistorikkInnslagForByttBehandlendeEnhet(behandling, sisteFørstegangsbehandling.getBehandlendeOrganisasjonsEnhet().getEnhetId(),
            sisteFørstegangsbehandling.getBehandlendeOrganisasjonsEnhet().getEnhetNavn());

        behandling.setBehandlendeEnhet(sisteFørstegangsbehandling.getBehandlendeOrganisasjonsEnhet());

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        //Lagre den oppdaterte behandlingen i datavarehus datavarehus.
        //Dette er et spesialtilfelle, DVH skal normalt oppdateres via DatavarehusEventObserver
        datavarehusTjeneste.lagreNedBehandling(behandling);
    }

    private void lagHistorikkInnslagForByttBehandlendeEnhet(Behandling behandling, String enhetId, String enhetNavn) {
        Historikkinnslag endreEnhetInnslag = new Historikkinnslag();
        endreEnhetInnslag.setType(HistorikkinnslagType.BYTT_ENHET);
        endreEnhetInnslag.setAktør(HistorikkAktør.VEDTAKSLØSNINGEN);
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        builder.medHendelse(HistorikkinnslagType.BYTT_ENHET);
        builder.medEndretFelt(HistorikkEndretFeltType.BEHANDLENDE_ENHET,
            behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId() + " " + behandling.getBehandlendeOrganisasjonsEnhet().getEnhetNavn(),
            enhetId + " " + enhetNavn);
        builder.medBegrunnelse("");
        builder.build(endreEnhetInnslag);

        endreEnhetInnslag.setBehandling(behandling);
        historikkRepository.lagre(endreEnhetInnslag);
    }
}
