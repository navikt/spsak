package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.opptjening;

import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;

class RyddOpptjening {

    private final OpptjeningRepository opptjeningRepository;
    private final Behandling behandling;
    private final BehandlingskontrollKontekst kontekst;
    private final InntektArbeidYtelseRepository arbeidYtelseRepository;
    private final BehandlingRepository behandlingRepository;

    RyddOpptjening(BehandlingRepositoryProvider provider, Behandling behandling, BehandlingskontrollKontekst kontekst) {
        this.opptjeningRepository = provider.getOpptjeningRepository();
        this.arbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
        this.behandlingRepository = provider.getBehandlingRepository();
        this.behandling = behandling;
        this.kontekst = kontekst;
    }

    void ryddOpp() {
        Optional<Vilkår> vilkår = ryddOppVilkårsvurderinger();
        if (vilkår.isPresent()) {
            opptjeningRepository.deaktiverOpptjening(behandling);
            arbeidYtelseRepository.tilbakestillOverstyring(behandling);
            tilbakestillOpptjenigsperiodevilkår();
        }
    }

    void ryddOppAktiviteter() {
        Optional<Vilkår> vilkår = ryddOppVilkårsvurderinger();
        if (vilkår.isPresent()) {
            arbeidYtelseRepository.tilbakestillOverstyring(behandling);
        }
    }

    private Optional<Vilkår> ryddOppVilkårsvurderinger() {
        VilkårResultat vilkårResultat = hentVilkårResultat();
        if (vilkårResultat == null) {
            return Optional.empty();
        }
        Optional<Vilkår> opptjeningVilkår = vilkårResultat.getVilkårene().stream()
            .filter(vilkåret -> vilkåret.getVilkårType().equals(VilkårType.OPPTJENINGSVILKÅRET))
            .findFirst();

        if (opptjeningVilkår.isPresent()) {
            VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat)
                .leggTilVilkår(opptjeningVilkår.get().getVilkårType(), IKKE_VURDERT);
            builder.buildFor(behandling);
            behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        }
        return opptjeningVilkår;
    }

    private VilkårResultat hentVilkårResultat() {
        Optional<VilkårResultat> vilkårResultatOpt = Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getVilkårResultat);
        return vilkårResultatOpt.orElse(null);
    }

    private void tilbakestillOpptjenigsperiodevilkår() {
        VilkårResultat vilkårResultat = hentVilkårResultat();
        if (vilkårResultat == null) {
            return;
        }
        Optional<Vilkår> opptjeningPeriodeVilkår = vilkårResultat.getVilkårene().stream()
            .filter(vilkåret -> vilkåret.getVilkårType().equals(VilkårType.OPPTJENINGSPERIODEVILKÅR))
            .findFirst();
        if (opptjeningPeriodeVilkår.isPresent()) {
            VilkårResultat.Builder builder = VilkårResultat.builderFraEksisterende(vilkårResultat)
                .leggTilVilkår(opptjeningPeriodeVilkår.get().getVilkårType(), IKKE_VURDERT);
            builder.buildFor(behandling);
            behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        }
    }

}
