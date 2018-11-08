package no.nav.foreldrepenger.økonomistøtte;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.økonomistøtte.es.OppdragskontrollEngangsstønad;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollEndringFP;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollFørstegangFP;
import no.nav.foreldrepenger.økonomistøtte.fp.OppdragskontrollOpphørFP;

@ApplicationScoped
public class OppdragskontrollManagerFactory {

    private OppdragskontrollFørstegangFP oppdragskontrollFørstegangFP;
    private OppdragskontrollEndringFP oppdragskontrollEndringFP;
    private OppdragskontrollOpphørFP oppdragskontrollOpphørFP;
    private OppdragskontrollEngangsstønad oppdragskontrollEngangsstønad;
    private UttakRepository uttakRepository;

    OppdragskontrollManagerFactory() {

    }

    @Inject
    public OppdragskontrollManagerFactory(OppdragskontrollEngangsstønad oppdragskontrollEngangsstønad,
                                          OppdragskontrollFørstegangFP oppdragskontrollFørstegangFP,
                                          OppdragskontrollEndringFP oppdragskontrollEndringFP,
                                          OppdragskontrollOpphørFP oppdragskontrollOpphørFP,
                                          UttakRepository uttakRepository) {
        this.oppdragskontrollEngangsstønad = oppdragskontrollEngangsstønad;
        this.oppdragskontrollFørstegangFP = oppdragskontrollFørstegangFP;
        this.oppdragskontrollEndringFP = oppdragskontrollEndringFP;
        this.oppdragskontrollOpphørFP = oppdragskontrollOpphørFP;
        this.uttakRepository = uttakRepository;

    }

    public OppdragskontrollManager getManager(Behandling behandling, boolean eksistererForrigeOppdrag) {
        if (erOppdragForFP(behandling)) {
            return finnFPManager(behandling, eksistererForrigeOppdrag);
        } else {
            return oppdragskontrollEngangsstønad;
        }
    }

    private Boolean erOppdragForFP(Behandling behandling) {
        return behandling.getFagsakYtelseType().gjelderForeldrepenger();
    }

    private OppdragskontrollManager finnFPManager(Behandling behandling, boolean eksistererForrigeOppdrag) {
        if (skalOpphørsoppdragOpprettes(behandling)) {
            return erOpphørEtterSkjæringstidspunktet(behandling) ? oppdragskontrollEndringFP : oppdragskontrollOpphørFP;
        }
        if (skalEndringsoppdragOpprettes(behandling, eksistererForrigeOppdrag)) {
            return oppdragskontrollEndringFP;
        }
        return oppdragskontrollFørstegangFP;
    }

    private boolean skalEndringsoppdragOpprettes(Behandling behandling, boolean eksistererForrigeOppdrag) {
        return (BehandlingResultatType.FORELDREPENGER_ENDRET.equals(behandling.getBehandlingsresultat().getBehandlingResultatType()))
            || (eksistererForrigeOppdrag && BehandlingResultatType.INNVILGET.equals(behandling.getBehandlingsresultat().getBehandlingResultatType()));
    }

    private boolean skalOpphørsoppdragOpprettes(Behandling behandling) {
        return BehandlingResultatType.OPPHØR.equals(behandling.getBehandlingsresultat().getBehandlingResultatType());
    }

    private boolean erOpphørEtterSkjæringstidspunktet(Behandling behandling) {
        Optional<UttakResultatEntitet> uttak = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
        if (!uttak.isPresent()) {
            return false;
        }
        return uttak.get().getGjeldendePerioder().getPerioder().stream()
            .anyMatch(periode -> PeriodeResultatType.INNVILGET.equals(periode.getPeriodeResultatType()));
    }
}
