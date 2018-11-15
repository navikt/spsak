package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.EndringsdatoRevurderingUtleder;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingFeil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapsvilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.vedtak.feil.FeilFactory;

@Dependent
public class RevurderingFPBehandlingsresultatutleder {

    private UttakRepository uttakRepository;
    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder;
    private MedlemskapVilkårPeriodeRepository medlemskapVilkårPeriodeRepository;

    @Inject
    public RevurderingFPBehandlingsresultatutleder(BehandlingRepositoryProvider repositoryProvider,
                                                   EndringsdatoRevurderingUtleder endringsdatoRevurderingUtleder) {
        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.endringsdatoRevurderingUtleder = endringsdatoRevurderingUtleder;
        this.medlemskapVilkårPeriodeRepository = repositoryProvider.getMedlemskapVilkårPeriodeRepository();
    }

    public Behandlingsresultat bestemBehandlingsresultatForRevurdering(Behandling revurdering, boolean erVarselOmRevurderingSendt) {
        if (!revurdering.getType().equals(BehandlingType.REVURDERING)) {
            throw new IllegalStateException("Utviklerfeil: Skal ikke kunne havne her uten en revurderingssak");
        }
        Optional<Behandling> originalBehandlingOptional = revurdering.getOriginalBehandling();
        if (!originalBehandlingOptional.isPresent()) {
            throw FeilFactory.create(RevurderingFeil.class).revurderingManglerOriginalBehandling(revurdering.getId()).toException();
        }
        Behandling originalBehandling = originalBehandlingOptional.get();

        LocalDate endringsdato = endringsdatoRevurderingUtleder.utledEndringsdato(revurdering);
        Optional<UttakResultatEntitet> uttakresultatRevurderingOpt = uttakRepository.hentUttakResultatHvisEksisterer(revurdering);
        Optional<UttakResultatEntitet> uttakresultatOriginalOpt = uttakRepository.hentUttakResultatHvisEksisterer(originalBehandling);

        boolean erEndringIUttakFraEndringstidspunkt = ErEndringIUttakFraEndringsdato.vurder(endringsdato, uttakresultatRevurderingOpt, uttakresultatOriginalOpt);

        if (OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt.vurder(revurdering)) {
            return OppfyllerIkkjeInngangsvilkårPåSkjæringstidspunkt.fastsett(revurdering);
        }

        Optional<MedlemskapsvilkårPeriodeGrunnlag> medlemskapsvilkårPeriodeGrunnlag = medlemskapVilkårPeriodeRepository.hentAggregatHvisEksisterer(revurdering);
        if (OppfyllerIkkjeInngangsvilkårIPerioden.vurder(medlemskapsvilkårPeriodeGrunnlag, endringsdato)) {
            return OppfyllerIkkjeInngangsvilkårIPerioden.fastsett(revurdering);
        }
        if (ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.vurder(uttakresultatRevurderingOpt, erEndringIUttakFraEndringstidspunkt)) {
            return ErSisteUttakAvslåttMedÅrsakOgHarEndringIUttak.fastsett(revurdering);
        }
        Optional<Beregningsgrunnlag> revurderingsGrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(revurdering);
        Optional<Beregningsgrunnlag> originalGrunnlagOpt = beregningsgrunnlagRepository.hentBeregningsgrunnlag(originalBehandling);

        boolean erEndringIBeregning = ErEndringIBeregning.vurder(revurderingsGrunnlagOpt, originalGrunnlagOpt);
        boolean erKunEndringIFordelingAvYtelsen = ErKunEndringIFordelingAvYtelsen.vurder(erEndringIBeregning, erEndringIUttakFraEndringstidspunkt, revurderingsGrunnlagOpt, originalGrunnlagOpt);
        return FastsettBehandlingsresultatVedEndring.fastsett(revurdering,
            erEndringIBeregning,
            erEndringIUttakFraEndringstidspunkt,
            erVarselOmRevurderingSendt,
            erKunEndringIFordelingAvYtelsen, endringsdato, uttakresultatOriginalOpt);
    }
}
