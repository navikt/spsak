package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.AdopsjonsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.FødselsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.MedlemskapsvilkårGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.grunnlag.SoeknadsfristvilkarGrunnlag;
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.konstanter.PersonStatusType;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.felles.v2.DateOpplysning;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Adopsjon;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagAdopsjon;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagFoedsel;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagMedlemskap;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagSoekersopplysningsplikt;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.es.v2.VilkaarsgrunnlagSoeknadsfrist;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.v2.Vilkaarsgrunnlag;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class VilkårsgrunnlagXmlTjenesteEngangsstønad extends VilkårsgrunnlagXmlTjeneste {

    private ObjectFactory vilkårObjectFactory = new ObjectFactory();


    public VilkårsgrunnlagXmlTjenesteEngangsstønad() {
        //For CDI
    }

    @Inject
    public VilkårsgrunnlagXmlTjenesteEngangsstønad(SøknadRepository søknadRepository, FamilieHendelseRepository familieHendelseRepository, KompletthetsjekkerProvider kompletthetsjekkerProvider) {
        super(søknadRepository, familieHendelseRepository, kompletthetsjekkerProvider);

    }

    @Override
    protected Vilkaarsgrunnlag getVilkaarsgrunnlag(Behandling behandling, Vilkår vilkårFraBehandling, Optional<Søknad> søknad) {
        Vilkaarsgrunnlag vilkaarsgrunnlag = null;
        if (VilkårType.SØKERSOPPLYSNINGSPLIKT.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForSøkersopplysningsplikt(behandling, søknad);
        } else if (VilkårType.MEDLEMSKAPSVILKÅRET.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForMedlemskapsvilkåret(vilkårFraBehandling);
        } else if (VilkårType.FØDSELSVILKÅRET_MOR.equals(vilkårFraBehandling.getVilkårType()) || VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForFødselsvilkåret(vilkårFraBehandling);
        } else if (VilkårType.SØKNADSFRISTVILKÅRET.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForSøknadsfristvilkåret(vilkårFraBehandling);
        } else if ((VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD.equals(vilkårFraBehandling.getVilkårType())) || (VilkårType.ADOPSJONSVILKARET_FORELDREPENGER.equals(vilkårFraBehandling.getVilkårType()))) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForAdopsjonsvilkåret(vilkårFraBehandling);
        }
        return vilkaarsgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForAdopsjonsvilkåret(Vilkår vilkårFraBehandling) {
        VilkaarsgrunnlagAdopsjon vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagAdopsjon();
        if (vilkårFraBehandling.getRegelInput() == null) {
            return vilkårgrunnlag;
        }
        AdopsjonsvilkårGrunnlag grunnlagForVilkår = getObjectMapper().readValue(
            vilkårFraBehandling.getRegelInput(),
            AdopsjonsvilkårGrunnlag.class
        );
        vilkårgrunnlag.setSoekersKjoenn(VedtakXmlUtil.lagStringOpplysning(grunnlagForVilkår.getSoekersKjonn().name()));
        Adopsjon adopsjon = new Adopsjon();

        Optional<DateOpplysning> omsorgOvertakelseDato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getOmsorgsovertakelsesdato());
        omsorgOvertakelseDato.ifPresent(adopsjon::setOmsorgsovertakelsesdato);

        adopsjon.setErMannAdoptererAlene(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isMannAdoptererAlene()));

        adopsjon.setErEktefellesBarn(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isEktefellesBarn()));
        vilkårgrunnlag.setAdopsjon(adopsjon);
        return vilkårgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForSøknadsfristvilkåret(Vilkår vilkårFraBehandling) {
        VilkaarsgrunnlagSoeknadsfrist vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagSoeknadsfrist();
        if (vilkårFraBehandling.getRegelInput() == null) {
            return vilkårgrunnlag;
        }
        SoeknadsfristvilkarGrunnlag grunnlagForVilkår = getObjectMapper().readValue(
            vilkårFraBehandling.getRegelInput(),
            SoeknadsfristvilkarGrunnlag.class
        );
        VilkaarsgrunnlagSoeknadsfrist vilkargrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagSoeknadsfrist();
        vilkargrunnlag.setElektroniskSoeknad(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isElektroniskSoeknad()));

        Optional<DateOpplysning> skjæringstidspunkt = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getSkjaeringstidspunkt());
        skjæringstidspunkt.ifPresent(vilkargrunnlag::setSkjaeringstidspunkt);

        Optional<DateOpplysning> søknadMotattDato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getSoeknadMottatDato());
        søknadMotattDato.ifPresent(vilkargrunnlag::setSoeknadMottattDato);

        return vilkargrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForFødselsvilkåret(Vilkår vilkårFraBehandling) {
        VilkaarsgrunnlagFoedsel vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagFoedsel();
        if (vilkårFraBehandling.getRegelInput() == null) {
            return vilkårgrunnlag;
        }
        FødselsvilkårGrunnlag grunnlagForVilkår = getObjectMapper().readValue(
            vilkårFraBehandling.getRegelInput(),
            FødselsvilkårGrunnlag.class
        );
        vilkårgrunnlag.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(grunnlagForVilkår.getAntallBarn()));
        Optional<DateOpplysning> bekreftetFødselsdato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getBekreftetFoedselsdato());
        bekreftetFødselsdato.ifPresent(vilkårgrunnlag::setFoedselsdatoBarn);

        if (grunnlagForVilkår.getSoekerRolle() != null) {
            vilkårgrunnlag.setSoekersRolle(VedtakXmlUtil.lagStringOpplysning(grunnlagForVilkår.getSoekerRolle().getKode()));
        }
        Optional<DateOpplysning> søknadDato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getSoeknadsdato());
        søknadDato.ifPresent(vilkårgrunnlag::setSoeknadsdato);

        vilkårgrunnlag.setSokersKjoenn(VedtakXmlUtil.lagStringOpplysning(grunnlagForVilkår.getSoekersKjonn().name()));

        Optional<DateOpplysning> bekreftetTerminDato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getBekreftetTermindato());
        bekreftetTerminDato.ifPresent(vilkårgrunnlag::setTermindato);

        return vilkårgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForMedlemskapsvilkåret(Vilkår vilkårFraBehandling) {
        VilkaarsgrunnlagMedlemskap vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagMedlemskap();
        if (vilkårFraBehandling.getRegelInput() == null) {
            return vilkårgrunnlag;
        }
        MedlemskapsvilkårGrunnlag grunnlagForVilkår = getObjectMapper().readValue(
            vilkårFraBehandling.getRegelInput(),
            MedlemskapsvilkårGrunnlag.class
        );
        vilkårgrunnlag.setErBrukerBorgerAvEUEOS(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerBorgerAvEUEOS()));
        vilkårgrunnlag.setHarBrukerLovligOppholdINorge(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerAvklartLovligOppholdINorge()));
        vilkårgrunnlag.setHarBrukerOppholdsrett(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerAvklartOppholdsrett()));
        vilkårgrunnlag.setErBrukerBosatt(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerAvklartBosatt()));
        vilkårgrunnlag.setErBrukerNordiskstatsborger(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerNorskNordisk()));
        vilkårgrunnlag.setErBrukerPliktigEllerFrivilligMedlem(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerAvklartPliktigEllerFrivillig()));
        vilkårgrunnlag.setErBrukerMedlem(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isBrukerErMedlem()));
        vilkårgrunnlag.setPersonstatus(VedtakXmlUtil.lagStringOpplysning(
            Optional.ofNullable(grunnlagForVilkår.getPersonStatusType()).map(PersonStatusType::getKode).orElse("-")));

        return vilkårgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForSøkersopplysningsplikt(Behandling behandling, Optional<Søknad> optionalSøknad) {
        boolean komplettSøknad;
        boolean elektroniskSøknad;
        boolean erBarnetFødt;
        if (!optionalSøknad.isPresent()) {
            komplettSøknad = false;
            elektroniskSøknad = false;
            erBarnetFødt = false;
        } else {
            Søknad søknad = optionalSøknad.get();
            komplettSøknad = erKomplettSøknad(behandling);
            elektroniskSøknad = søknad.getElektroniskRegistrert();
            erBarnetFødt = erBarnetFødt(behandling);
        }
        VilkaarsgrunnlagSoekersopplysningsplikt vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagSoekersopplysningsplikt();
        vilkårgrunnlag.setErSoeknadenKomplett(VedtakXmlUtil.lagBooleanOpplysning(komplettSøknad)); //Denne er unødvendig fo dvh.
        vilkårgrunnlag.setElektroniskSoeknad(VedtakXmlUtil.lagBooleanOpplysning(elektroniskSøknad));
        vilkårgrunnlag.setErBarnetFoedt(VedtakXmlUtil.lagBooleanOpplysning(erBarnetFødt));
        return vilkårgrunnlag;
    }
}
