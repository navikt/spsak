package no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon;

import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettForAksjonspunkt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

@ApplicationScoped
public class AksjonspunktUtlederForForeldrepengerAdopsjon implements AksjonspunktUtleder {

    private FamilieHendelseRepository familieHendelseRepository;

    AksjonspunktUtlederForForeldrepengerAdopsjon (){
    }

    @Inject
    AksjonspunktUtlederForForeldrepengerAdopsjon(BehandlingRepositoryProvider repositoryProvider) {
        this.familieHendelseRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        List<AksjonspunktResultat> aksjonspunktResultater = new ArrayList<>();

        aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON));

        FamilieHendelse søknadVersjon = familieHendelseRepository.hentAggregat(behandling).getSøknadVersjon();
        Optional<Adopsjon> adopsjon = søknadVersjon.getAdopsjon();
        if (!adopsjon.isPresent()) {
            return aksjonspunktResultater;
        }

        Boolean erEktefellesBarn = adopsjon.get().getErEktefellesBarn();
        if (erEktefellesBarn == null || erEktefellesBarn) {
            aksjonspunktResultater.add(opprettForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN));
        }
        return aksjonspunktResultater;
    }

}
