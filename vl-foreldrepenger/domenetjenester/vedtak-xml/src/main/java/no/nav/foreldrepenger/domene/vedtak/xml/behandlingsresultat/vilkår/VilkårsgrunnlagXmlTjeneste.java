package no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.vilkår;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårJsonObjectMapper;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;
import no.nav.vedtak.felles.xml.vedtak.v2.Vilkaar;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.v2.Vilkaarsgrunnlag;

public abstract class VilkårsgrunnlagXmlTjeneste {

    private VilkårJsonObjectMapper objectMapper = new VilkårJsonObjectMapper();
    private SøknadRepository søknadRepository;
    private KompletthetsjekkerProvider kompletthetsjekkerProvider;

    public VilkårsgrunnlagXmlTjeneste() {
        // For CDI
    }

    public VilkårsgrunnlagXmlTjeneste(SøknadRepository søknadRepository, KompletthetsjekkerProvider kompletthetsjekkerProvider) {
        this.søknadRepository = søknadRepository;
        this.kompletthetsjekkerProvider = kompletthetsjekkerProvider;
    }

    protected VilkårJsonObjectMapper getObjectMapper() {
        return objectMapper;
    }


    public void setVilkårsgrunnlag(Behandling behandling, Vilkår vilkårFraBehandling, Vilkaar vilkår) {
        Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);
        Vilkaarsgrunnlag vilkaarsgrunnlag = getVilkaarsgrunnlag(behandling, vilkårFraBehandling, søknad); //Må implementeres i hver subklasse

        no.nav.vedtak.felles.xml.vedtak.v2.Vilkaarsgrunnlag vilkaarsgrunnlag1 = new no.nav.vedtak.felles.xml.vedtak.v2.Vilkaarsgrunnlag();
        vilkaarsgrunnlag1.getAny().add(new no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.v2.ObjectFactory().createVilkaarsgrunnlag(vilkaarsgrunnlag));
        vilkår.setVilkaarsgrunnlag(vilkaarsgrunnlag1);
    }

    protected abstract Vilkaarsgrunnlag getVilkaarsgrunnlag(Behandling behandling, Vilkår vilkårFraBehandling, Optional<Søknad> søknad);

    protected boolean erKomplettSøknad(Behandling behandling) {
        return kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling).erForsendelsesgrunnlagKomplett(behandling);
    }

    protected LocalDate getMottattDato(Behandling behandling) {
        Optional<Søknad> søknadOptional = søknadRepository.hentSøknadHvisEksisterer(behandling);
        if (søknadOptional.isPresent()) {
            Søknad søknad = søknadOptional.get();
            return søknad.getMottattDato();
        }
        return null;
    }

}
