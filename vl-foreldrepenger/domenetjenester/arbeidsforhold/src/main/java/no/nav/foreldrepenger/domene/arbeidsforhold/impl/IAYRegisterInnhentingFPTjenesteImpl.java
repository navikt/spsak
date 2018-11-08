package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.threeten.extra.Interval;

import no.nav.foreldrepenger.behandling.OpplysningsPeriodeTjeneste;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.arbeidsforhold.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
public class IAYRegisterInnhentingFPTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    IAYRegisterInnhentingFPTjenesteImpl() {
        // CDI
    }


    @Inject
    public IAYRegisterInnhentingFPTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                               BehandlingRepositoryProvider repositoryProvider,
                                               VirksomhetTjeneste virksomhetTjeneste,
                                               SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                               InnhentingSamletTjeneste innhentingSamletTjeneste,
                                               BasisPersonopplysningTjeneste personopplysningTjeneste,
                                               OpplysningsPeriodeTjeneste opplysningsPeriodeTjeneste) {
        super(inntektArbeidYtelseTjeneste,
            repositoryProvider,
            virksomhetTjeneste,
            skjæringstidspunktTjeneste,
            innhentingSamletTjeneste,
            personopplysningTjeneste,
            opplysningsPeriodeTjeneste);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Behandling behandling) {
        return true;
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder innhentYtelserForInvolverteParter(Behandling behandling, Interval opplysningsPeriode) {
        return innhentYtelserForInvolverteParter(behandling, opplysningsPeriode, true);
    }

}
