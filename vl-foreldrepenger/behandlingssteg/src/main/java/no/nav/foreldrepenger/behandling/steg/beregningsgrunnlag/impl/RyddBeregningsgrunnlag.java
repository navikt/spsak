package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

class RyddBeregningsgrunnlag {

    private BehandlingRepository behandlingRepository;
    private final Behandling behandling;
    private final BehandlingskontrollKontekst kontekst;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    RyddBeregningsgrunnlag(BehandlingRepositoryProvider repositoryProvider, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.behandling = behandling;
        this.kontekst = kontekst;
    }

    void ryddKontrollFaktaBeregningVedTilbakeføring() {
        beregningsgrunnlagRepository.deaktiverBeregningsgrunnlagGrunnlagEntitet(behandling);
    }

    void ryddForeslåBeregningsgrunnlagVedTilbakeføring() {
        beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.KOFAKBER_UT);
    }

    void gjenopprettFørsteBeregningsgrunnlag() {
        beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.OPPRETTET);
    }

    void ryddFastsettBeregningsgrunnlagVedTilbakeføring() {
        ryddOppVilkårsvurdering();
        nullstillVedtaksresultat();
        beregningsgrunnlagRepository.reaktiverBeregningsgrunnlagGrunnlagEntitet(behandling, BeregningsgrunnlagTilstand.FORESLÅTT);
    }

    private void ryddOppVilkårsvurdering() {
        Optional<VilkårResultat> vilkårResultatOpt = Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getVilkårResultat);
        if (!vilkårResultatOpt.isPresent()) {
            return;
        }
        VilkårResultat vilkårResultat = vilkårResultatOpt.get();
        Optional<Vilkår> beregningsvilkåret = vilkårResultat.getVilkårene().stream()
            .filter(vilkår -> vilkår.getVilkårType().equals(VilkårType.BEREGNINGSGRUNNLAGVILKÅR))
            .findFirst();
        if (!beregningsvilkåret.isPresent()) {
            return;
        }
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat)
            .leggTilVilkår(beregningsvilkåret.get().getVilkårType(), IKKE_VURDERT);
        builder.buildFor(behandling);
    }

    private void nullstillVedtaksresultat() {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat == null || Objects.equals(behandlingsresultat.getBehandlingResultatType(), BehandlingResultatType.IKKE_FASTSATT)) {
            return;
        }
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT);
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

}
