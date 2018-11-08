package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.domene.familiehendelse.dødsfall.OpplysningerOmDødEndringIdentifiserer;

@ApplicationScoped
public class AksjonspunktUtlederForAvklarHendelse implements AksjonspunktUtleder {

    private OpplysningerOmDødEndringIdentifiserer opplysningerOmDødEndringIdentifiserer;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;

    AksjonspunktUtlederForAvklarHendelse() {
        // For CDI
    }

    @Inject
    public AksjonspunktUtlederForAvklarHendelse(OpplysningerOmDødEndringIdentifiserer opplysningerOmDødEndringIdentifiserer,
                                                MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository) {
        this.opplysningerOmDødEndringIdentifiserer = opplysningerOmDødEndringIdentifiserer;
        this.medlemskapVilkårPeriodeRepository = medlemskapVilkårPeriodeRepository;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        List<AksjonspunktResultat> resultat = new ArrayList<>();

        // #1
        if (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_KLAGE_UTEN_END_INNTEKT)
            || behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_KLAGE_MED_END_INNTEKT)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_REALITETSBEHANDLING_ELLER_KLAGE));
        }

        // #2
        if (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_MEDLEMSKAP)
            || behandlingHarIkkeOppfylteMedlemskapsvilkårsperioder(behandling)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_MEDLEMSKAP));
        }

        // #3
        if (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_FORDELING)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_FORDELING_AV_STØNADSPERIODEN));
        }

        // #4
        if (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)
            || opplysningerOmDødEndringIdentifiserer.erEndret(behandling)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_DØD));
        }

        // #5
        if (behandling.erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_SØKNAD_FRIST)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_OPPLYSNINGER_OM_SØKNADSFRIST));
        }

        // #6
        if (behandling.harBehandlingÅrsak(BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_INNVILGET)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_TILSTØTENDE_YTELSER_INNVILGET));
        }

        // #7
        if (behandling.harBehandlingÅrsak(BehandlingÅrsakType.RE_TILSTØTENDE_YTELSE_OPPHØRT)) {
            resultat.add(AksjonspunktResultat.opprettForAksjonspunkt(AksjonspunktDefinisjon.KONTROLLER_TILSTØTENDE_YTELSER_OPPHØRT));
        }

        return resultat;
    }

    private boolean behandlingHarIkkeOppfylteMedlemskapsvilkårsperioder(Behandling behandling) {
        Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlagOriginalBehandling = Optional.empty();
        if (behandling.erRevurdering()) {
            Behandling origBehandling = behandling.getOriginalBehandling()
                .orElseThrow(() -> new IllegalStateException("Original behandling mangler på revurdering - skal ikke skje"));
            grunnlagOriginalBehandling = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(origBehandling);
        }

        Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlag = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(behandling);
        boolean originalBehandlingHarPeriodeUtenMedlemskap = harPeriodeUtenMedlemskap(grunnlagOriginalBehandling);
        boolean behandlingHarPeriodeUtenMedlemskap = harPeriodeUtenMedlemskap(grunnlag);

        return originalBehandlingHarPeriodeUtenMedlemskap || behandlingHarPeriodeUtenMedlemskap;
    }

    private boolean harPeriodeUtenMedlemskap(Optional<MedlemskapsvilkårPeriodeGrunnlag> grunnlag) {
        return grunnlag.isPresent() && grunnlag.get().getMedlemskapsvilkårPeriode().getPerioder().stream()
            .anyMatch(mvp -> VilkårUtfallType.IKKE_OPPFYLT.equals(mvp.getVilkårUtfall()));
    }
}
