package no.nav.foreldrepenger.behandling.impl;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class RegisterInnhentingIntervallEndringTjeneste {

    private Period grenseverdiFørES;
    private Period grenseverdiFørFP;
    private Period grenseverdiEtterES;
    private Period grenseverdiEtterFP;

    RegisterInnhentingIntervallEndringTjeneste() {
        // CDI
    }

    @Inject
    public RegisterInnhentingIntervallEndringTjeneste(@KonfigVerdi("registerinnhenting.es.avvik.periode.før") Period grenseverdiFørES,
                                                      @KonfigVerdi("registerinnhenting.fp.avvik.periode.før") Period grenseverdiFørFP,
                                                      @KonfigVerdi("registerinnhenting.es.avvik.periode.etter") Period grenseverdiEtterES,
                                                      @KonfigVerdi("registerinnhenting.fp.avvik.periode.etter") Period grenseverdiEtterFP) {
        this.grenseverdiFørES = grenseverdiFørES;
        this.grenseverdiFørFP = grenseverdiFørFP;
        this.grenseverdiEtterES = grenseverdiEtterES;
        this.grenseverdiEtterFP = grenseverdiEtterFP;
    }

    boolean erEndringIPerioden(LocalDate oppgittSkjæringstidspunkt, LocalDate bekreftetSkjæringstidspunkt, FagsakYtelseType ytelseType) {
        Objects.requireNonNull(ytelseType, "ytelseType");
        if (bekreftetSkjæringstidspunkt == null) {
            return false;
        }
        if (ytelseType.gjelderForeldrepenger()) {
            return vurderEndringIPeriodenForeldrepenger(oppgittSkjæringstidspunkt, bekreftetSkjæringstidspunkt);
        } else if (ytelseType.gjelderEngangsstønad()) {
            return vurderEndringIPeriodenEngangsstønad(oppgittSkjæringstidspunkt, bekreftetSkjæringstidspunkt);
        }
        throw new IllegalStateException("Ukjent ytelsetype" + ytelseType);
    }

    private boolean vurderEndringIPeriodenEngangsstønad(LocalDate oppgittSkjæringstidspunkt, LocalDate bekreftetSkjæringstidspunkt) {
        return vurderEndringFør(oppgittSkjæringstidspunkt, bekreftetSkjæringstidspunkt, grenseverdiFørES)
            || vurderEndringEtter(bekreftetSkjæringstidspunkt, oppgittSkjæringstidspunkt, grenseverdiEtterES);
    }

    private boolean vurderEndringIPeriodenForeldrepenger(LocalDate oppgittSkjæringstidspunkt, LocalDate bekreftetSkjæringstidspunkt) {
        return vurderEndringFør(oppgittSkjæringstidspunkt, bekreftetSkjæringstidspunkt, grenseverdiFørFP)
            || vurderEndringEtter(bekreftetSkjæringstidspunkt, oppgittSkjæringstidspunkt, grenseverdiEtterFP);
    }

    private boolean vurderEndringEtter(LocalDate oppgittSkjæringstidspunkt, LocalDate bekreftetSkjæringstidspunkt, Period grenseverdiEtter) {
        final Period avstand = Period.between(oppgittSkjæringstidspunkt, bekreftetSkjæringstidspunkt);
        return !avstand.isNegative() && PeriodeCompareUtil.størreEnn(avstand, grenseverdiEtter);
    }

    private boolean vurderEndringFør(LocalDate oppgittSkjæringstidspunkt, LocalDate bekreftetSkjæringstidspunkt, Period grenseverdiFør) {
        final Period avstand = Period.between(bekreftetSkjæringstidspunkt, oppgittSkjæringstidspunkt);
        return !avstand.isNegative() && PeriodeCompareUtil.størreEnn(avstand, grenseverdiFør);
    }
}
