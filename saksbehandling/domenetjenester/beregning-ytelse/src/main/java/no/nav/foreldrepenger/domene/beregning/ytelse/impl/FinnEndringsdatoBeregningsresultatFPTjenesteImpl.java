package no.nav.foreldrepenger.domene.beregning.ytelse.impl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.domene.beregning.ytelse.FinnEndringsdatoBeregningsresultatFPTjeneste;
import no.nav.foreldrepenger.domene.beregning.ytelse.FinnEndringsdatoFeil;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.feil.FeilFactory;

@ApplicationScoped
public class FinnEndringsdatoBeregningsresultatFPTjenesteImpl implements FinnEndringsdatoBeregningsresultatFPTjeneste {

    private BeregningsresultatRepository beregningsresultatFPRepository;

    FinnEndringsdatoBeregningsresultatFPTjenesteImpl() {
        //NOSONAR
    }

    @Inject
    public FinnEndringsdatoBeregningsresultatFPTjenesteImpl(ResultatRepositoryProvider repositoryProvider) {
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatRepository();
    }

    @Override
    public Optional<LocalDate> finnEndringsdato(Behandling behandling, BeregningsresultatPerioder revurderingBeregningsresultat) {
        if (behandling.getType().equals(BehandlingType.REVURDERING)) {
            return finnEndringsdatoForRevurdering(behandling, revurderingBeregningsresultat);
        } else {
            throw FeilFactory.create(FinnEndringsdatoFeil.class).behandlingErIkkeEnRevurdering(behandling.getId()).toException();
        }
    }

    private Optional<LocalDate> finnEndringsdatoForRevurdering(Behandling revurdering, BeregningsresultatPerioder revurderingBeregningsresultat) {
        Behandling originalBehandling = revurdering.getOriginalBehandling()
            .orElseThrow(() -> FeilFactory.create(FinnEndringsdatoFeil.class).manglendeOriginalBehandling(revurdering.getId()).toException());
        BeregningsresultatPerioder beregningsresultatForOriginalBehandling = beregningsresultatFPRepository.hentHvisEksisterer(originalBehandling)
            .orElseThrow(() -> FeilFactory.create(FinnEndringsdatoFeil.class).manglendeBeregningsresultat(originalBehandling.getId()).toException());
        List<BeregningsresultatPeriode> originalePerioder = beregningsresultatForOriginalBehandling.getBeregningsresultatPerioder();
        if (originalePerioder.isEmpty()) {
            Long id = beregningsresultatForOriginalBehandling.getId();
            throw FeilFactory.create(FinnEndringsdatoFeil.class).manglendeBeregningsresultatPeriode(id).toException();
        }
        List<BeregningsresultatPeriode> revurderingPerioder = revurderingBeregningsresultat.getBeregningsresultatPerioder();
        if (revurderingPerioder.isEmpty()) {
            throw FeilFactory.create(FinnEndringsdatoFeil.class).manglendeBeregningsresultatPeriode(revurderingBeregningsresultat.getId()).toException();
        }
        return sjekkForEndringAvPerioderIBeregninsgsresultater(revurderingPerioder, originalePerioder);
    }

    private Optional<LocalDate> sjekkForEndringAvPerioderIBeregninsgsresultater(List<BeregningsresultatPeriode> revurderingPerioder, List<BeregningsresultatPeriode> originalePerioder) {
        LocalDateTimeline<TidslinjePeriodeWrapper> union = opprettTidslinjeUnion(revurderingPerioder, originalePerioder);
        Optional<LocalDateSegment<TidslinjePeriodeWrapper>> first = union.toSegments().stream()
            .sorted(Comparator.comparing(LocalDateSegment::getFom))
            .filter(wrapper -> {
                BeregningsresultatPeriode nyPeriode = wrapper.getValue().getRevurderingPeriode();
                BeregningsresultatPeriode gammelPeriode = wrapper.getValue().getOriginalPeriode();
                if (nyPeriode == null || gammelPeriode == null) {
                    return true;
                }
                if (gammelPeriode.getBeregningsresultatAndelList().isEmpty()) {
                    throw FeilFactory.create(FinnEndringsdatoFeil.class).manglendeBeregningsresultatPeriodeAndel(gammelPeriode.getId()).toException();
                }
                if (nyPeriode.getBeregningsresultatAndelList().isEmpty()) {
                    throw FeilFactory.create(FinnEndringsdatoFeil.class).manglendeBeregningsresultatPeriodeAndel(nyPeriode.getId()).toException();
                }
                return harIkkeSammePeriodeFOM(nyPeriode, gammelPeriode) || inneholderIkkeSammeAndeler(nyPeriode, gammelPeriode);

            })
            .findFirst();
        return first.map(LocalDateSegment::getFom);
    }

    private boolean harIkkeSammePeriodeFOM(BeregningsresultatPeriode nyPeriode, BeregningsresultatPeriode gammelPeriode) {
        return !nyPeriode.getBeregningsresultatPeriodeFom().isEqual(gammelPeriode.getBeregningsresultatPeriodeFom());
    }

    private boolean inneholderIkkeSammeAndeler(BeregningsresultatPeriode nyPeriode, BeregningsresultatPeriode gammelPeriode) {
        List<BeregningsresultatAndel> nyeAndeler = nyPeriode.getBeregningsresultatAndelList();
        List<BeregningsresultatAndel> gamleAndeler = gammelPeriode.getBeregningsresultatAndelList();
        if (nyeAndeler.size() != gamleAndeler.size()) {
            return true;
        }
        return nyeAndeler.stream().noneMatch(nyAndel -> finnKorresponderendeAndel(nyAndel, gamleAndeler));
    }

    private boolean finnKorresponderendeAndel(BeregningsresultatAndel nyAndel, List<BeregningsresultatAndel> gamleAndeler) {
        long antallAndelerSomKorresponderer = gamleAndeler.stream().filter(gammelAndel ->
            Objects.equals(nyAndel.erBrukerMottaker(), gammelAndel.erBrukerMottaker()) &&
                Objects.equals(nyAndel.getVirksomhet(), gammelAndel.getVirksomhet()) &&
                Objects.equals(nyAndel.getArbeidsforholdRef(), gammelAndel.getArbeidsforholdRef()) &&
                Objects.equals(nyAndel.getAktivitetStatus(), gammelAndel.getAktivitetStatus()) &&
                Objects.equals(nyAndel.getInntektskategori(), gammelAndel.getInntektskategori()) &&
                Objects.equals(nyAndel.getDagsats(), gammelAndel.getDagsats())).count();
        if (antallAndelerSomKorresponderer > 1) {
            throw FeilFactory.create(FinnEndringsdatoFeil.class).fantFlereKorresponderendeAndelerFeil(nyAndel.getId()).toException();
        }
        return antallAndelerSomKorresponderer == 1;
    }

    private LocalDateTimeline<TidslinjePeriodeWrapper> opprettTidslinjeUnion(List<BeregningsresultatPeriode> revurderingPerioder, List<BeregningsresultatPeriode> originalePerioder) {
        LocalDateTimeline<BeregningsresultatPeriode> revurderingTidslinje = new LocalDateTimeline<>(revurderingPerioder.stream()
            .sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(p -> new LocalDateSegment<>(p.getBeregningsresultatPeriodeFom(), p.getBeregningsresultatPeriodeTom(), p))
            .collect(Collectors.toList()));
        LocalDateTimeline<BeregningsresultatPeriode> originalTidslinje = new LocalDateTimeline<>(originalePerioder.stream()
            .sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .map(p -> new LocalDateSegment<>(p.getBeregningsresultatPeriodeFom(), p.getBeregningsresultatPeriodeTom(), p))
            .collect(Collectors.toList()));
        return revurderingTidslinje.union(originalTidslinje, (interval, revurderingSegment, originalSegment) -> {
            BeregningsresultatPeriode revurderingSegmentVerdi = revurderingSegment != null ? revurderingSegment.getValue() : null;
            BeregningsresultatPeriode originalSegmentVerdi = originalSegment != null ? originalSegment.getValue() : null;
            TidslinjePeriodeWrapper wrapper = new TidslinjePeriodeWrapper(revurderingSegmentVerdi, originalSegmentVerdi);
            return new LocalDateSegment<>(interval, wrapper);
        });
    }

    private class TidslinjePeriodeWrapper {

        private BeregningsresultatPeriode revurderingPeriode;
        private BeregningsresultatPeriode originalPeriode;

        TidslinjePeriodeWrapper(BeregningsresultatPeriode revurderingPeriode, BeregningsresultatPeriode originalPeriode) {
            this.revurderingPeriode = revurderingPeriode;
            this.originalPeriode = originalPeriode;
        }

        BeregningsresultatPeriode getRevurderingPeriode() {
            return revurderingPeriode;
        }

        BeregningsresultatPeriode getOriginalPeriode() {
            return originalPeriode;
        }

    }

}
