package no.nav.foreldrepenger.domene.kontrollerfakta.søknad;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;

/**
 * Aksjonspunkt for avklaring v tilleggopplysninger som oppgis i søknad.
 */
@ApplicationScoped
public class AksjonspunktUtlederForTilleggsopplysninger implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private SøknadRepository søknadRepository;

    AksjonspunktUtlederForTilleggsopplysninger() {
    }

    @Inject
    public AksjonspunktUtlederForTilleggsopplysninger(GrunnlagRepositoryProvider repositoryProvider) {
        this.søknadRepository = repositoryProvider.getSøknadRepository();
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        final Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);

        final Optional<String> tilleggsopplysninger = søknad.map(Søknad::getTilleggsopplysninger);
        if (tilleggsopplysninger.isPresent()) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_TILLEGGSOPPLYSNINGER);
        }
        return INGEN_AKSJONSPUNKTER;
    }

}
