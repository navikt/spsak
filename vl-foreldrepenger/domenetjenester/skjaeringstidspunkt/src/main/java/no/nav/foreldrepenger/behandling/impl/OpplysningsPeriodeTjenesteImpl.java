package no.nav.foreldrepenger.behandling.impl;

import java.time.LocalDate;
import java.time.Period;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.IntervallUtil;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class OpplysningsPeriodeTjenesteImpl implements no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste {

    private Period periodeFørFP;
    private Period periodeEtterFP;
    private Period periodeFørES;
    private Period periodeEtterES;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    OpplysningsPeriodeTjenesteImpl() {
        // CDI
    }

    @Inject
    public OpplysningsPeriodeTjenesteImpl(SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                          @KonfigVerdi(value = "registerinnhenting.fp.opplysningsperiode.før") Period periodeFørFP,
                                          @KonfigVerdi(value = "registerinnhenting.fp.opplysningsperiode.etter") Period periodeEtterFP,
                                          @KonfigVerdi(value = "registerinnhenting.es.opplysningsperiode.før") Period periodeFørES,
                                          @KonfigVerdi(value = "registerinnhenting.es.opplysningsperiode.etter") Period periodeEtterES) {

        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.periodeFørFP = periodeFørFP;
        this.periodeEtterFP = periodeEtterFP;
        this.periodeFørES = periodeFørES;
        this.periodeEtterES = periodeEtterES;
    }

    @Override
    public Interval beregn(Behandling behandling) {
        final LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForRegisterInnhenting(behandling);
        if(behandling.getFagsakYtelseType().gjelderForeldrepenger()) {
            return beregnIntervalFP(skjæringstidspunkt);
        } else if(behandling.getFagsakYtelseType().gjelderEngangsstønad()) {
            return beregnIntervalES(skjæringstidspunkt);
        }
        throw SkjæringstidspunktFeil.FACTORY.kanIkkeUtledeOpplysningsperiodeForBehandling(behandling).toException();
    }

    private Interval beregnIntervalES(LocalDate skjæringstidspunkt) {
        return IntervallUtil.byggIntervall(skjæringstidspunkt.minus(periodeFørES), skjæringstidspunkt.plus(periodeEtterES));
    }

    private Interval beregnIntervalFP(LocalDate skjæringstidspunkt) {
        return IntervallUtil.byggIntervall(skjæringstidspunkt.minus(periodeFørFP), skjæringstidspunkt.plus(periodeEtterFP));
    }
}
