package no.nav.foreldrepenger.domene.kontrollerfakta.impl.fp;

import java.util.List;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtlederHolder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap.AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden;
import no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap.AksjonspunktutlederForMedlemskapSkjæringstidspunkt;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTidligereMottattForeldrepenger;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTilleggsopplysninger;


public abstract class KontrollerFaktaUtledereTjenesteFP implements KontrollerFaktaUtledereTjeneste {


    KontrollerFaktaUtledereTjenesteFP(BehandlingRepositoryProvider repositoryProvider) {
    }

    // Legg til aksjonspunktutledere som er felles for Førstegangsbehandling og Revurdering
    protected List<AksjonspunktUtleder> leggTilFellesutledere(Behandling behandling) {
        final AksjonspunktUtlederHolder utlederHolder = new AksjonspunktUtlederHolder();
        leggTilStandardUtledere(utlederHolder);
        return utlederHolder.getUtledere();
    }

    private void leggTilStandardUtledere(AksjonspunktUtlederHolder utlederHolder) {
        utlederHolder.leggTil(AksjonspunktutlederForMedlemskapSkjæringstidspunkt.class)
            .leggTil(AksjonspunktUtlederForTilleggsopplysninger.class)
            .leggTil(AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden.class)
            .leggTil(AksjonspunktUtlederForTidligereMottattForeldrepenger.class);
    }
}
