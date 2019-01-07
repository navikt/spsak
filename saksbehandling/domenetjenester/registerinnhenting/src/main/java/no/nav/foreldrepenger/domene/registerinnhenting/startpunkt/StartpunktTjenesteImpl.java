package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.util.Comparator;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.domene.kontrollerfakta.StartpunktTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;

@Dependent
public class StartpunktTjenesteImpl implements StartpunktTjeneste {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartpunktTjenesteImpl.class);

    private KodeverkTabellRepository kodeverkTabellRepository;
    private Instance<StartpunktUtleder> utledere;
    private EndringsresultatSjekker endringsresultatSjekker;

    StartpunktTjenesteImpl() {
        // For CDI
    }

    @Inject
    StartpunktTjenesteImpl(@Any Instance<StartpunktUtleder> utledere, GrunnlagRepositoryProvider repositoryProvider, EndringsresultatSjekker endringsresultatSjekker) {
        this.utledere = utledere;
        this.kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.endringsresultatSjekker = endringsresultatSjekker;
    }

    @Override
    public StartpunktType utledStartpunktMotOriginalBehandling(Behandling revurdering) {
        Behandling origBehandling = revurdering.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Original behandling mangler på revurdering - skal ikke skje"));

        EndringsresultatSnapshot snapshotOriginalBehandling = endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(origBehandling);
        EndringsresultatDiff diff = endringsresultatSjekker.finnSporedeEndringerPåBehandlingsgrunnlag(revurdering, snapshotOriginalBehandling);
        LOGGER.info("Endringsresultat ved revurdering={} er: {}", revurdering.getId(), diff);// NOSONAR //$NON-NLS-1$
        StartpunktType startpunktType = utledStartpunktForDiffBehandlingsgrunnlag(revurdering, diff);

        return kodeverkTabellRepository.finnStartpunktType(startpunktType.getKode());
    }

    @Override
    public StartpunktType utledStartpunktForDiffBehandlingsgrunnlag(Behandling behandling, EndringsresultatDiff differanse) {
        StartpunktType startpunktType = differanse.hentDelresultater().stream()
            .filter(EndringsresultatDiff::erSporedeFeltEndret)
            .map(diff -> finnUtleder(diff.getGrunnlag())
                .utledStartpunkt(behandling, diff.getGrunnlagId1(), diff.getGrunnlagId2()))
            .map(it -> kodeverkTabellRepository.finnStartpunktType(it.getKode())) // Må oppfriskes fra Hibernate
            .min(Comparator.comparing(StartpunktType::getRangering))
            .orElse(StartpunktType.UDEFINERT);
        return kodeverkTabellRepository.finnStartpunktType(startpunktType.getKode());
    }

    private StartpunktUtleder finnUtleder(Class<?> aggregat) {
        String aggrNavn = aggregat.getSimpleName();
        Instance<StartpunktUtleder> selected = utledere.select(new GrunnlagRef.GrunnlagRefLiteral(aggrNavn));
        if (selected.isAmbiguous()) {
            throw new IllegalArgumentException("Mer enn en implementasjon funnet for startpunktutleder:" + aggrNavn);
        } else if (selected.isUnsatisfied()) {
            throw new IllegalArgumentException("Ingen implementasjoner funnet for startpunktutleder:" + aggrNavn);
        }
        StartpunktUtleder minInstans = selected.get();
        if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
            throw new IllegalStateException("Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
        }
        return minInstans;
    }

}
