package no.nav.foreldrepenger.behandling.revurdering.fp;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.EndringsdatoRevurderingUtleder;
import no.nav.foreldrepenger.behandling.revurdering.RevurderingFeil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.vedtak.feil.FeilFactory;

@Dependent
public class RevurderingBehandlingsresultatutleder {

    private BehandlingRepository behandlingRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;

    @Inject
    public RevurderingBehandlingsresultatutleder(ResultatRepositoryProvider resultatRepositoryProvider,
                                                   EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder) {
        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.endringsdatoRevurderingUtleder = endringsdatoRevurderingUtleder;
        this.medlemskapVilkårPeriodeRepository = resultatRepositoryProvider.getMedlemskapVilkårPeriodeRepository();
        this.behandlingRepository = resultatRepositoryProvider.getBehandlingRepository();
    }

    public Behandlingsresultat bestemBehandlingsresultatForRevurdering(Behandling revurdering, boolean erVarselOmRevurderingSendt) {
        if (!revurdering.getType().equals(BehandlingType.REVURDERING)) {
            throw new IllegalStateException("Utviklerfeil: Skal ikke kunne havne her uten en revurderingssak");
        }
        Optional<Behandling> originalBehandlingOptional = revurdering.getOriginalBehandling();
        if (originalBehandlingOptional.isEmpty()) {
            throw FeilFactory.create(RevurderingFeil.class).revurderingManglerOriginalBehandling(revurdering.getId()).toException();
        }
        Behandling originalBehandling = originalBehandlingOptional.get();

        Behandlingsresultat revurderingsResultat = behandlingRepository.hentResultat(revurdering.getId());
        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurderingsResultat);

        if (OppfyllerIkkeInngangsvilkårPåSkjæringstidspunkt.vurder(revurderingsResultat)) {
            return OppfyllerIkkeInngangsvilkårPåSkjæringstidspunkt.fastsett(revurdering, revurderingsResultat);
        }

        Optional<MedlemskapVilkårPeriodeGrunnlag> medlemskapsvilkårPeriodeGrunnlag = medlemskapVilkårPeriodeRepository.hentHvisEksisterer(revurderingsResultat);
        if (OppfyllerIkkeInngangsvilkårIPerioden.vurder(medlemskapsvilkårPeriodeGrunnlag, endringsdato)) {
            return OppfyllerIkkeInngangsvilkårIPerioden.fastsett(revurdering, revurderingsResultat);
        }
        Optional<Beregningsgrunnlag> revurderingsGrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(revurdering);
        Optional<Beregningsgrunnlag> originalGrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(originalBehandling);

        boolean erEndringIBeregning = ErEndringIBeregning.vurder(revurderingsGrunnlagOpt, originalGrunnlagOpt);
        return FastsettBehandlingsresultatVedEndring.fastsett(revurdering, erEndringIBeregning, revurderingsResultat);
    }
}
