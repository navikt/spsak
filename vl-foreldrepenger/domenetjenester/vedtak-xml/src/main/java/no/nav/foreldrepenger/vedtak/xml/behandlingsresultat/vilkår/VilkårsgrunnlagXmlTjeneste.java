package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
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
    protected FamilieHendelseRepository familieHendelseRepository;
    private KompletthetsjekkerProvider kompletthetsjekkerProvider;

    public VilkårsgrunnlagXmlTjeneste() {
        // For CDI
    }

    public VilkårsgrunnlagXmlTjeneste(SøknadRepository søknadRepository, FamilieHendelseRepository familieHendelseRepository, KompletthetsjekkerProvider kompletthetsjekkerProvider) {
        this.søknadRepository = søknadRepository;
        this.familieHendelseRepository = familieHendelseRepository;
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

    protected boolean erBarnetFødt(Behandling behandling) {
        Optional<Søknad> søknadOptional = søknadRepository.hentSøknadHvisEksisterer(behandling);
        if (søknadOptional.isPresent()) {
            Søknad søknad = søknadOptional.get();
            Optional<LocalDate> fødselsdato1 = søknad.getFamilieHendelse().getFødselsdato();
            if (!fødselsdato1.isPresent()) {
                return false;
            }
            final FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseRepository.hentAggregat(behandling);
            final List<UidentifisertBarn> barna = familieHendelseGrunnlag.getBekreftetVersjon().map(FamilieHendelse::getBarna).orElse(Collections.emptyList());
            return !barna.isEmpty();
        }
        return false;
    }

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
