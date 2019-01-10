package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;

class RyddRegisterData {
    private final BehandlingStegModell modell;
    private final BehandlingRepository behandlingRepository;
    private final Behandling behandling;
    private final BehandlingskontrollKontekst kontekst;
    private final AksjonspunktRepository aksjonspunktRepository;
    private MedlemskapRepository medlemskapRepository;

    RyddRegisterData(BehandlingStegModell modell, GrunnlagRepositoryProvider repositoryProvider, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.modell = modell;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.behandling = behandling;
        this.kontekst = kontekst;
    }

    void ryddRegisterdataEngangsstønad() {
        nullstillRegisterdata();
        fjernAksjonspunktSomOppstårEtterKontrollerFaktaES();
    }

    void ryddRegisterdataForeldrepenger() {
        nullstillRegisterdata();
    }

    private void nullstillRegisterdata() {
        // Sletter avklarte data, men ikke Fødsel/Adopsjon/Omsorg, da dette må ivaretas hvis registerdata re-innhentes
        medlemskapRepository.slettAvklarteMedlemskapsdata(behandling, kontekst.getSkriveLås());
        behandling.nullstillToTrinnsBehandling();
    }

    /**
     * @deprecated Det er et unntak at aksjonspunkter håndteres på denne måten.
     * Statusoppdatering på aksjonspunkt skal vanligvis skje gjennom behandlingskontroll
     */
    @Deprecated
    private void fjernAksjonspunktSomOppstårEtterKontrollerFaktaES() {
        Optional<Behandlingsresultat> behandlingsresultatOpt = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if (behandlingsresultatOpt.isEmpty()) {
            return;
        }
        BehandlingStegType førsteSteg = BehandlingStegType.KONTROLLER_FAKTA;
        Set<String> skalSlettes = modell.getBehandlingModell().finnAksjonspunktDefinisjonerFraOgMed(førsteSteg, false);


        Set<Aksjonspunkt> aksjonspunkter = behandling.getAksjonspunkter();
        Set<Aksjonspunkt> slettes = aksjonspunkter.stream()
            .filter(a -> skalSlettes.contains(a.getAksjonspunktDefinisjon().getKode()))
            .filter(Aksjonspunkt::getSlettesVedRegisterinnhenting)
            .collect(toSet());
        slettes.forEach(a -> aksjonspunktRepository.fjernAksjonspunkt(behandling, a.getAksjonspunktDefinisjon()));

        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }
}
