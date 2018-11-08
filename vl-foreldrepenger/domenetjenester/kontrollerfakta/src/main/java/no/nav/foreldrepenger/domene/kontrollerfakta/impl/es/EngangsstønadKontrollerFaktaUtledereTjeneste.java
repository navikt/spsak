package no.nav.foreldrepenger.domene.kontrollerfakta.impl.es;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtlederHolder;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.VilkårUtlederFeil;
import no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon.AksjonspunktUtlederForEngangsstønadAdopsjon;
import no.nav.foreldrepenger.domene.kontrollerfakta.fødsel.AksjonspunktUtlederForEngangsstønadFødsel;
import no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap.AksjonspunktutlederForMedlemskapSkjæringstidspunkt;
import no.nav.foreldrepenger.domene.kontrollerfakta.omsorgsovertakelse.AksjonspunktUtlederForOmsorgsovertakelse;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTidligereMottattEngangsstønad;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTilleggsopplysninger;

@ApplicationScoped
@BehandlingTypeRef
@FagsakYtelseTypeRef("ES")
public class EngangsstønadKontrollerFaktaUtledereTjeneste implements KontrollerFaktaUtledereTjeneste {

    private FamilieHendelseRepository familieGrunnlagRepository;

    EngangsstønadKontrollerFaktaUtledereTjeneste() {
        // CDI
    }

    @Inject
    public EngangsstønadKontrollerFaktaUtledereTjeneste(BehandlingRepositoryProvider repositoryProvider) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    @Override
    public List<AksjonspunktUtleder> utledUtledereFor(Behandling behandling) {

        final AksjonspunktUtlederHolder utlederHolder = new AksjonspunktUtlederHolder();

        final Optional<FamilieHendelseGrunnlag> hendelseGrunnlag = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
        if (!hendelseGrunnlag.isPresent()) {
            throw VilkårUtlederFeil.FEILFACTORY.behandlingsmotivKanIkkeUtledes(behandling.getId()).toException();
        }

        FamilieHendelseType familieHendelseType = hendelseGrunnlag.map(FamilieHendelseGrunnlag::getGjeldendeVersjon)
            .map(FamilieHendelse::getType)
            .orElseThrow(() -> new IllegalStateException("Utvikler feil: Hendelse uten type"));

        // Legger til utledere som alltid skal kjøres
        leggTilStandardUtledere(utlederHolder);

        if (FamilieHendelseType.FØDSEL.equals(familieHendelseType) || FamilieHendelseType.TERMIN.equals(familieHendelseType)) {
            utlederHolder.leggTil(AksjonspunktUtlederForEngangsstønadFødsel.class);
        }

        if (FamilieHendelseType.ADOPSJON.equals(familieHendelseType)) {
            utlederHolder.leggTil(AksjonspunktUtlederForEngangsstønadAdopsjon.class);
        }

        if (FamilieHendelseType.OMSORG.equals(familieHendelseType)) {
            utlederHolder.leggTil(AksjonspunktUtlederForOmsorgsovertakelse.class);
        }

        return utlederHolder.getUtledere();
    }

    private void leggTilStandardUtledere(AksjonspunktUtlederHolder utlederHolder) {
        utlederHolder.leggTil(AksjonspunktUtlederForTilleggsopplysninger.class)
            .leggTil(AksjonspunktUtlederForTidligereMottattEngangsstønad.class)
            .leggTil(AksjonspunktutlederForMedlemskapSkjæringstidspunkt.class);
    }
}
