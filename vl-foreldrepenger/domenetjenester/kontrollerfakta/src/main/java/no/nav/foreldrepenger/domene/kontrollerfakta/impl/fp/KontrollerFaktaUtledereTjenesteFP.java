package no.nav.foreldrepenger.domene.kontrollerfakta.impl.fp;

import static no.nav.foreldrepenger.domene.kontrollerfakta.VilkårUtlederFeil.FEILFACTORY;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtlederHolder;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaUtledereTjeneste;
import no.nav.foreldrepenger.domene.kontrollerfakta.adopsjon.AksjonspunktUtlederForForeldrepengerAdopsjon;
import no.nav.foreldrepenger.domene.kontrollerfakta.fødsel.AksjonspunktUtlederForForeldrepengerFødsel;
import no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap.AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden;
import no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap.AksjonspunktutlederForMedlemskapSkjæringstidspunkt;
import no.nav.foreldrepenger.domene.kontrollerfakta.omsorg.AksjonspunktUtlederForForeldreansvar;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTidligereMottattForeldrepenger;
import no.nav.foreldrepenger.domene.kontrollerfakta.søknad.AksjonspunktUtlederForTilleggsopplysninger;


public abstract class KontrollerFaktaUtledereTjenesteFP implements KontrollerFaktaUtledereTjeneste {

    private FamilieHendelseRepository familieGrunnlagRepository;

    KontrollerFaktaUtledereTjenesteFP(BehandlingRepositoryProvider repositoryProvider) {
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
    }

    // Legg til aksjonspunktutledere som er felles for Førstegangsbehandling og Revurdering
    protected List<AksjonspunktUtleder> leggTilFellesutledere(Behandling behandling) {
        final AksjonspunktUtlederHolder utlederHolder = new AksjonspunktUtlederHolder();

        final Optional<FamilieHendelseGrunnlag> hendelseGrunnlag = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
        if (!hendelseGrunnlag.isPresent()) {
            throw FEILFACTORY.behandlingsmotivKanIkkeUtledes(behandling.getId()).toException();
        }

        FamilieHendelseType familieHendelseType = hendelseGrunnlag.map(FamilieHendelseGrunnlag::getGjeldendeVersjon)
            .map(FamilieHendelse::getType)
            .orElseThrow(() -> new IllegalStateException("Utvikler feil: Hendelse uten type"));

        // Legger til utledere som alltid skal kjøres
        leggTilStandardUtledere(utlederHolder);

        if (FamilieHendelseType.FØDSEL.equals(familieHendelseType) || FamilieHendelseType.TERMIN.equals(familieHendelseType)) {
            utlederHolder.leggTil(AksjonspunktUtlederForForeldrepengerFødsel.class);
        }

        if (FamilieHendelseType.ADOPSJON.equals(familieHendelseType)) {
            utlederHolder.leggTil(AksjonspunktUtlederForForeldrepengerAdopsjon.class);
        }

        if (FamilieHendelseType.OMSORG.equals(familieHendelseType)) {
            utlederHolder.leggTil(AksjonspunktUtlederForForeldreansvar.class);
        }

        return utlederHolder.getUtledere();
    }

    private void leggTilStandardUtledere(AksjonspunktUtlederHolder utlederHolder) {
        utlederHolder.leggTil(AksjonspunktutlederForMedlemskapSkjæringstidspunkt.class)
            .leggTil(AksjonspunktUtlederForTilleggsopplysninger.class)
            .leggTil(AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden.class)
            .leggTil(AksjonspunktUtlederForTidligereMottattForeldrepenger.class);
    }
}
