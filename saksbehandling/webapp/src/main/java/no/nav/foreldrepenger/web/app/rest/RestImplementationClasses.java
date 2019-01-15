package no.nav.foreldrepenger.web.app.rest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import no.nav.foreldrepenger.batch.rest.BatchRestTjeneste;
import no.nav.foreldrepenger.behandling.historikk.rest.HistorikkRestTjeneste;
import no.nav.foreldrepenger.web.app.selftest.IntegrasjonstatusRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.BeregningsgrunnlagRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.BeregningsresultatRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.InntektArbeidYtelseRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening.OpptjeningRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning.PersonRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.søknad.SøknadRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.TotrinnskontrollRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vilkår.VilkårRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.brev.BrevRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.dokument.DokumentRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.FagsakRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.FordelRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.konfig.KonfigRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.saksbehandler.FeatureToggleRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.saksbehandler.NavAnsattRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

class RestImplementationClasses {

    Collection<Class<?>> getImplementationClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(FagsakRestTjeneste.class);
        classes.add(NavAnsattRestTjeneste.class);
        classes.add(FeatureToggleRestTjeneste.class);
        classes.add(BehandlingRestTjeneste.class);
        classes.add(BeregningsgrunnlagRestTjeneste.class);
        classes.add(AksjonspunktRestTjeneste.class);
        classes.add(DokumentRestTjeneste.class);
        classes.add(HistorikkRestTjeneste.class);
        classes.add(KodeverkRestTjeneste.class);
        classes.add(KonfigRestTjeneste.class);
        classes.add(BatchRestTjeneste.class);
        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(FordelRestTjeneste.class);
        classes.add(BeregningsresultatRestTjeneste.class);
        classes.add(TotrinnskontrollRestTjeneste.class);
        classes.add(PersonRestTjeneste.class);
        classes.add(SøknadRestTjeneste.class);
        classes.add(OpptjeningRestTjeneste.class);
        classes.add(InntektArbeidYtelseRestTjeneste.class);
        classes.add(VilkårRestTjeneste.class);
        classes.add(IntegrasjonstatusRestTjeneste.class);
        classes.add(BrevRestTjeneste.class);
        return Set.copyOf(classes);
    }
}
