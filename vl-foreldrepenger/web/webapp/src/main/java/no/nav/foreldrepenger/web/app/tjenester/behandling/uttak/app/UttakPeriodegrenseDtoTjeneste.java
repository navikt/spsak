package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.søknadsfrist.SøknadsfristForeldrepengerTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.UttakPeriodegrenseDto;

@ApplicationScoped
public class UttakPeriodegrenseDtoTjeneste {

    private SøknadRepository søknadRepository;
    private SøknadsfristForeldrepengerTjeneste vurderSøknadsfristTjeneste;

    public UttakPeriodegrenseDtoTjeneste() {
        // For CDI
    }

    @Inject
    public UttakPeriodegrenseDtoTjeneste(SøknadRepository søknadRepository,
                                         SøknadsfristForeldrepengerTjeneste søknadsfristTjeneste) {
        this.vurderSøknadsfristTjeneste = søknadsfristTjeneste;
        this.søknadRepository = søknadRepository;
    }

    public Optional<UttakPeriodegrenseDto> mapFra(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat != null) {
            Optional<Uttaksperiodegrense> gjeldendeUttaksperiodegrense = behandlingsresultat.getGjeldendeUttaksperiodegrense();
            if (gjeldendeUttaksperiodegrense.isPresent()) {
                Uttaksperiodegrense grense = gjeldendeUttaksperiodegrense.get();
                UttakPeriodegrenseDto dto = new UttakPeriodegrenseDto();
                dto.setSoknadsfristForForsteUttaksdato(grense.getFørsteLovligeUttaksdag());
                dto.setMottattDato(grense.getMottattDato());

                Søknad søknad = søknadRepository.hentSøknad(behandling);

                if (søknad.getFordeling() != null) {
                    List<OppgittPeriode> perioder = søknad.getFordeling().getOppgittePerioder().stream()
                        .filter(periode -> UttakPeriodeType.STØNADSPERIODETYPER.contains(periode.getPeriodeType()))
                        .sorted(Comparator.comparing(OppgittPeriode::getFom))
                        .collect(Collectors.toList());

                    if (!perioder.isEmpty()) {
                        LocalDate soknadsperiodeStart = perioder.get(0).getFom();
                        dto.setSoknadsperiodeStart(soknadsperiodeStart);
                        LocalDate søknadsfrist = vurderSøknadsfristTjeneste.finnSøknadsfristForPeriodeMedStart(soknadsperiodeStart);
                        dto.setSoknadsfristForForsteUttaksdato(søknadsfrist);
                        dto.setAntallDagerLevertForSent(DAYS.between(søknadsfrist, søknad.getMottattDato()));
                        dto.setSoknadsperiodeSlutt(perioder.get(perioder.size() - 1).getTom());
                    }
                }
                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }
}
