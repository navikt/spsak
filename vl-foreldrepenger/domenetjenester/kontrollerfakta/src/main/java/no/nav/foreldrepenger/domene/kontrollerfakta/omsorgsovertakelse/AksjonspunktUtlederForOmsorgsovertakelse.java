package no.nav.foreldrepenger.domene.kontrollerfakta.omsorgsovertakelse;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;

/**
 * Aksjonspunkter for søknad om engangsstønad og betinget vilkår gjelder omsorgsovertakelse.
 * MERK: Betinget vilkår blir ikke satt før det er manuelt satt av saksbehandler.
 */
@ApplicationScoped
public class AksjonspunktUtlederForOmsorgsovertakelse implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private FamilieHendelseRepository familieGrunnlagRepository;

    AksjonspunktUtlederForOmsorgsovertakelse() {
    }

    @Inject
    public AksjonspunktUtlederForOmsorgsovertakelse(BehandlingRepositoryProvider repositoryProvider) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        if (farAdoptererAlene(behandling) == NEI) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE);
        }
        return INGEN_AKSJONSPUNKTER;
    }

    private Utfall farAdoptererAlene(Behandling behandling) {
        return familieGrunnlagRepository.hentAggregat(behandling).getGjeldendeAdopsjon()
            .map(Adopsjon::getAdoptererAlene).orElse(false) ? JA : NEI;
    }
}
