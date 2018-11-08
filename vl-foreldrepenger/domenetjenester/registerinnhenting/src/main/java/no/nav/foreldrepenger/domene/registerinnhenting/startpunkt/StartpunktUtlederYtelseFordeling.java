package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

@ApplicationScoped
@GrunnlagRef("YtelseFordelingAggregat")
class StartpunktUtlederYtelseFordeling implements StartpunktUtleder {

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private YtelsesFordelingRepository ytelsesFordelingRepository;
    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    StartpunktUtlederYtelseFordeling() {
        // For CDI
    }

    @Inject
    StartpunktUtlederYtelseFordeling(SkjæringstidspunktTjeneste skjæringstidspunktTjeneste, YtelsesFordelingRepository ytelsesFordelingRepository,
                                     BeregningsresultatFPRepository beregningsresultatFPRepository) {
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.ytelsesFordelingRepository = ytelsesFordelingRepository;
        this.beregningsresultatFPRepository = beregningsresultatFPRepository;
    }

    @Override
    public StartpunktType utledStartpunkt(Behandling behandling, Long grunnlagId1, Long grunnlagId2) {
        Behandling originalBehandling = behandling.getOriginalBehandling()
            .orElse(null);
        if (originalBehandling == null) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.UTTAKSVILKÅR, "ikke revurdering", grunnlagId1, grunnlagId2);
            return StartpunktType.UTTAKSVILKÅR;
        }
        if (erStartpunktBeregning(behandling, originalBehandling)) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.BEREGNING, "Søkt om gradert periode", grunnlagId1, grunnlagId2);
            return StartpunktType.BEREGNING;
        }
        if (erSkjæringsdatoUendret(behandling, originalBehandling)) {
            FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.UTTAKSVILKÅR, "ingen endring i skjæringsdato", grunnlagId1, grunnlagId2);
            return StartpunktType.UTTAKSVILKÅR;
        }
        FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(this.getClass().getSimpleName(), StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT, "", grunnlagId1, grunnlagId2);
        return StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT;
    }

    private boolean erSkjæringsdatoUendret(Behandling behandling, Behandling originalBehandling) {
        LocalDate originalSkjæringsdato = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(originalBehandling);
        LocalDate nySkjæringsdato = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);

        return Objects.equals(originalSkjæringsdato, nySkjæringsdato);
    }


    private boolean erStartpunktBeregning(Behandling behandling, Behandling origBehandling){
        List<OppgittPeriode> perioderFraEndringssøknad = ytelsesFordelingRepository.hentAggregat(behandling).getOppgittFordeling().getOppgittePerioder();
        List<OppgittPeriode> gradertePerioderFraEndringssøknad = finnGradertePerioder(perioderFraEndringssøknad);

        if(gradertePerioderFraEndringssøknad.isEmpty()){
            return false;
        }

        if (origBehandling.getBehandlingsresultat().isBehandlingsresultatAvslåttOrOpphørt()) {
            return false;
        }

        Optional<BeregningsresultatFP> beregningsresultat = beregningsresultatFPRepository.hentBeregningsresultatFP(origBehandling);

        if (!beregningsresultat.isPresent()) {
            return false;
        }

        return StartpunktutlederHjelper.finnesAktivitetHvorAlleHarDagsatsNull(beregningsresultat.get());
    }

    private List<OppgittPeriode> finnGradertePerioder(List<OppgittPeriode> perioder) {
        if(perioder == null) {
            throw new IllegalStateException("Utviklerfeil: forventer at behandling har oppgitte perioder");
        }
        return perioder.stream()
            .filter(this::periodeErGradert)
            .collect(toList());
    }

    private boolean periodeErGradert(OppgittPeriode periode){
        return !(periode.getArbeidsprosent() == null || periode.getArbeidsprosent() == BigDecimal.ZERO);
    }

}
