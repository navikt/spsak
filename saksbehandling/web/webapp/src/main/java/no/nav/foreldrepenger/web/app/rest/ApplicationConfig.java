package no.nav.foreldrepenger.web.app.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.config.BeanConfig;
import no.nav.foreldrepenger.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.web.app.tjenester.batch.BatchRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsgrunnlag.BeregningsgrunnlagRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.beregningsresultat.BeregningsresultatRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.InntektArbeidYtelseRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening.OpptjeningRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning.PersonRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.søknad.SøknadRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.TotrinnskontrollRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vilkår.VilkårRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.brev.BrevRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.dokument.DokumentRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.FagsakRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.fordeling.FordelRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.historikk.HistorikkRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.integrasjonstatus.IntegrasjonstatusRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.KodeverkRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.konfig.KonfigRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.saksbehandler.FeatureToggleRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.saksbehandler.NavAnsattRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;


@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        if (utviklingServer()) {
            beanConfig.setSchemes(new String[]{"http"});
        } else {
            beanConfig.setSchemes(new String[]{"https"});
        }
        beanConfig.setBasePath("/fpsak/api");
        beanConfig.setResourcePackage("no.nav");
        beanConfig.setTitle("Saksbehandling - Sykepenger");
        beanConfig.setDescription("REST grensesnitt for Saksbehandling - Sykepenger.");
        beanConfig.setScan(true);
    }

    /**
     * Finner ut av om vi kjører utviklingsserver. Settes i JettyDevServer#konfigurerMiljø()
     *
     * @return true dersom utviklingsserver.
     */
    private boolean utviklingServer() {
        return Boolean.getBoolean("develop-local");
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>(new RestImplementationClasses().getImplementationClasses());
        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.add(GeneralRestExceptionMapper.class);
        classes.add(JacksonJsonConfig.class);

        return Collections.unmodifiableSet(classes);
    }
}
