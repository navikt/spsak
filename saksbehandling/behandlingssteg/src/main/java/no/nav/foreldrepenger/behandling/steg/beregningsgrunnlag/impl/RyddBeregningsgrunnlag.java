package no.nav.foreldrepenger.behandling.steg.beregningsgrunnlag.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

class RyddBeregningsgrunnlag {

    private BehandlingRepository behandlingRepository;
    private final Behandling behandling;
    private final BehandlingskontrollKontekst kontekst;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;

    RyddBeregningsgrunnlag(BehandlingRepository behandlingRepository, BeregningsgrunnlagRepository beregningsgrunnlagRepository, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.behandlingRepository = behandlingRepository;
        this.beregningsgrunnlagRepository = beregningsgrunnlagRepository;
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
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if(behandlingsresultat.isEmpty()) {
            return;
        }
        Optional<VilkårResultat> vilkårResultatOpt = behandlingsresultat
            .map(Behandlingsresultat::getVilkårResultat);
        if (vilkårResultatOpt.isEmpty()) {
            return;
        }
        VilkårResultat vilkårResultat = vilkårResultatOpt.get();
        Optional<Vilkår> beregningsvilkåret = vilkårResultat.getVilkårene().stream()
            .filter(vilkår -> vilkår.getVilkårType().equals(VilkårType.BEREGNINGSGRUNNLAGVILKÅR))
            .findFirst();
        if (beregningsvilkåret.isEmpty()) {
            return;
        }
        Behandlingsresultat behandlingsresultat1 = behandlingsresultat.get();
        VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat)
            .leggTilVilkår(beregningsvilkåret.get().getVilkårType(), IKKE_VURDERT);
        builder.buildFor(behandlingsresultat1);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat1.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat1, lås);
    }

    private void nullstillVedtaksresultat() {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if (behandlingsresultat.isEmpty() || Objects.equals(behandlingsresultat.get().getBehandlingResultatType(), BehandlingResultatType.IKKE_FASTSATT)) {
            return;
        }
        Behandlingsresultat behandlingsresultat1 = behandlingsresultat.get();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat1).medBehandlingResultatType(BehandlingResultatType.IKKE_FASTSATT);
        behandlingRepository.lagre(behandlingsresultat1, kontekst.getSkriveLås());
    }

}
