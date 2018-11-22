package no.nav.foreldrepenger.web.app.tjenester.kodeverk;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.APPLIKASJON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.app.HentKodeverkTjeneste;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Api(tags = {"kodeverk"})
@Path("/kodeverk")
@RequestScoped
@Transaction
public class KodeverkRestTjeneste {

    private HentKodeverkTjeneste hentKodeverkTjeneste; // NOSONAR
    private VilkårKodeverkRepository vilkårKodeverkRepository; // NOSONAR

    @Inject
    public KodeverkRestTjeneste(HentKodeverkTjeneste hentKodeverkTjeneste, VilkårKodeverkRepository vilkårKodeverkRepository) {
        this.hentKodeverkTjeneste = hentKodeverkTjeneste;
        this.vilkårKodeverkRepository = vilkårKodeverkRepository;
    }

    public KodeverkRestTjeneste() {
        // for resteasy
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henter kodeliste", notes = ("Returnerer gruppert kodeliste."))
    @BeskyttetRessurs(action = READ, ressurs = APPLIKASJON, sporingslogg = false)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Map<String, Object> hentGruppertKodeliste() {
        Map<String, Object> kodelisterGruppertPåType = new HashMap<>();

        Map<String, List<Kodeliste>> grupperteKodelister = hentKodeverkTjeneste.hentGruppertKodeliste();
        grupperteKodelister.entrySet().forEach(e -> kodelisterGruppertPåType.put(e.getKey(), e.getValue()));

        Map<String, List<Avslagsårsak>> avslagårsakerGruppertPåVilkårType = vilkårKodeverkRepository.finnAvslagårsakerGruppertPåVilkårType()
            .entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getKode(), Map.Entry::getValue));
        kodelisterGruppertPåType.put(Avslagsårsak.class.getSimpleName(), avslagårsakerGruppertPåVilkårType);

        return kodelisterGruppertPåType;
    }

    @GET
    @Path("/behandlende-enheter")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henter liste over behandlende enheter", notes = ("Returnerer alle enheter"))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<OrganisasjonsEnhet> hentBehandlendeEnheter() {
        return hentKodeverkTjeneste.hentBehandlendeEnheter();
    }

    @GET
    @Path("/henlegg/arsaker/klage")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henter liste over årsaker for henleggelse av klage", notes = ("Returnerer alle årsaker for henleggelse av klagebehandling"))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingResultatType> hentHenleggÅrsakerForKlage() {
        return new ArrayList<>(BehandlingResultatType.getHenleggelseskoderForKlage());
    }

    @GET
    @Path("/henlegg/arsaker/innsyn")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henter liste over årsaker for henleggelse av innsyn", notes = ("Returnerer alle årsaker for henleggelse av innsynsbehandling"))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingResultatType> hentHenleggÅrsakerForInnsyn() {
        return new ArrayList<>(BehandlingResultatType.getHenleggelseskoderForInnsyn());
    }

    @GET
    @Path("/henlegg/arsaker")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Henter liste over årsaker for henleggelse", notes = ("Returnerer alle årsaker for henleggelse av behandling"))
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public List<BehandlingResultatType> hentHenleggÅrsaker() {
        return new ArrayList<>(BehandlingResultatType.getHenleggelseskoderForSøknad());
    }
}
