package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
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
import no.nav.foreldrepenger.inngangsvilkaar.regelmodell.opptjening.Opptjeningsgrunnlag;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.xml.felles.v2.DateOpplysning;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagAdopsjon;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagFoedsel;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagMedlemskap;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagOpptjening;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagSoekersopplysningsplikt;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.fp.v2.VilkaarsgrunnlagSoeknadsfrist;
import no.nav.vedtak.felles.xml.vedtak.vilkaarsgrunnlag.v2.Vilkaarsgrunnlag;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VilkårsgrunnlagXmlTjenesteForeldrepenger extends VilkårsgrunnlagXmlTjeneste {

    private ObjectFactory vilkårObjectFactory = new ObjectFactory();
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;

    public VilkårsgrunnlagXmlTjenesteForeldrepenger() {
        //For CDI
    }

    @Inject
    public VilkårsgrunnlagXmlTjenesteForeldrepenger(SøknadRepository søknadRepository, KompletthetsjekkerProvider kompletthetsjekkerProvider, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        super(søknadRepository, kompletthetsjekkerProvider);
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    protected Vilkaarsgrunnlag getVilkaarsgrunnlag(Behandling behandling, Vilkår vilkårFraBehandling, Optional<Søknad> søknad) {
        Vilkaarsgrunnlag vilkaarsgrunnlag = null;
        if (VilkårType.FØDSELSVILKÅRET_MOR.equals(vilkårFraBehandling.getVilkårType()) || VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForFødselsvilkåret(behandling, vilkårFraBehandling);
        } else if (VilkårType.ADOPSJONSVILKARET_FORELDREPENGER.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForAdopsjonsvilkåret(vilkårFraBehandling);
        } else if (VilkårType.SØKNADSFRISTVILKÅRET.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForSøknadsfristvilkåret(vilkårFraBehandling);
        } else if (VilkårType.SØKERSOPPLYSNINGSPLIKT.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForSøkersopplysningsplikt(behandling, søknad);
        } else if (VilkårType.MEDLEMSKAPSVILKÅRET.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForMedlemskapsvilkåret(vilkårFraBehandling);
        } else if (VilkårType.OPPTJENINGSVILKÅRET.equals(vilkårFraBehandling.getVilkårType())) {
            vilkaarsgrunnlag = lagVilkaarsgrunnlagForOpptjening(vilkårFraBehandling);
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

        vilkårgrunnlag.setSoekersKjoenn(VedtakXmlUtil.lagKodeverkOpplysning(NavBrukerKjønn.fraKode(grunnlagForVilkår.getSoekersKjonn().getKode())));

        Optional<DateOpplysning> omsorgovertakelsedato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getOmsorgsovertakelsesdato());
        omsorgovertakelsedato.ifPresent(vilkårgrunnlag::setOmsorgsovertakelsesdato);

        vilkårgrunnlag.setMannAdoptererAlene(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isMannAdoptererAlene()));
        vilkårgrunnlag.setEktefellesBarn(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isEktefellesBarn()));

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
        vilkårgrunnlag.setElektroniskSoeknad(VedtakXmlUtil.lagBooleanOpplysning(grunnlagForVilkår.isElektroniskSoeknad()));
        VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getSoeknadMottatDato())
            .ifPresent(vilkårgrunnlag::setSoeknadMottattDato);
        VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getSkjaeringstidspunkt())
            .ifPresent(vilkårgrunnlag::setSkjaeringstidspunkt);

        return vilkårgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForFødselsvilkåret(Behandling behandling, Vilkår vilkårFraBehandling) {
        VilkaarsgrunnlagFoedsel vilkårgrunnlagFødselForeldrepenger = vilkårObjectFactory.createVilkaarsgrunnlagFoedsel();
        if (vilkårFraBehandling.getRegelInput() == null) {
            return vilkårgrunnlagFødselForeldrepenger;
        }
        FødselsvilkårGrunnlag grunnlagForVilkår = getObjectMapper().readValue(
            vilkårFraBehandling.getRegelInput(),
            FødselsvilkårGrunnlag.class
        );
        vilkårgrunnlagFødselForeldrepenger.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(grunnlagForVilkår.getAntallBarn()));
        Optional<DateOpplysning> bekreftetFødselsdato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getBekreftetFoedselsdato());
        bekreftetFødselsdato.ifPresent(vilkårgrunnlagFødselForeldrepenger::setFoedselsdatoBarn);

        if (grunnlagForVilkår.getSoekerRolle() != null) {
            vilkårgrunnlagFødselForeldrepenger.setSoekersRolle(VedtakXmlUtil.lagStringOpplysning(grunnlagForVilkår.getSoekerRolle().getKode()));
        }
        Optional<DateOpplysning> søknadDato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getSoeknadsdato());
        søknadDato.ifPresent(vilkårgrunnlagFødselForeldrepenger::setSoeknadsdato);

        vilkårgrunnlagFødselForeldrepenger.setSokersKjoenn(VedtakXmlUtil.lagStringOpplysning(grunnlagForVilkår.getSoekersKjonn().name()));

        Optional<DateOpplysning> bekreftetTerminDato = VedtakXmlUtil.lagDateOpplysning(grunnlagForVilkår.getBekreftetTermindato());
        bekreftetTerminDato.ifPresent(vilkårgrunnlagFødselForeldrepenger::setTermindato);

        return vilkårgrunnlagFødselForeldrepenger;
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
            Optional.ofNullable(grunnlagForVilkår.getPersonStatusType()).map(PersonStatusType::getKode).orElse("-")
        ));
        return vilkårgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForOpptjening(Vilkår vilkårFraBehandling) {
        VilkaarsgrunnlagOpptjening vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagOpptjening();

        if (vilkårFraBehandling.getRegelInput() == null) {
            return vilkårgrunnlag;
        }

        Opptjeningsgrunnlag opptjeningsgrunnlag = getObjectMapper().readValue(
            vilkårFraBehandling.getRegelInput(),
            Opptjeningsgrunnlag.class
        );

        if (opptjeningsgrunnlag != null) {
            VedtakXmlUtil.lagDateOpplysning(opptjeningsgrunnlag.getBehandlingsTidspunkt()).ifPresent(vilkårgrunnlag::setBehandlingsDato);

            vilkårgrunnlag.setMinsteAntallDagerForVent(VedtakXmlUtil.lagIntOpplysning(opptjeningsgrunnlag.getMinsteAntallDagerForVent()));
            vilkårgrunnlag.setMinsteAntallDagerGodkjent(VedtakXmlUtil.lagIntOpplysning(opptjeningsgrunnlag.getMinsteAntallDagerGodkjent()));
            vilkårgrunnlag.setMinsteAntallMånederGodkjent(VedtakXmlUtil.lagIntOpplysning(opptjeningsgrunnlag.getMinsteAntallMånederGodkjent()));
            LocalDateInterval opptjeningsperiode = opptjeningsgrunnlag.getOpptjeningPeriode();
            vilkårgrunnlag.setOpptjeningperiode(VedtakXmlUtil.lagPeriodeOpplysning(opptjeningsperiode.getFomDato(), opptjeningsperiode.getTomDato()));
            vilkårgrunnlag.setMinsteInntekt(VedtakXmlUtil.lagLongOpplysning(opptjeningsgrunnlag.getMinsteInntekt()));

            vilkårgrunnlag.setMaksMellomliggendePeriodeForArbeidsforhold(VedtakXmlUtil.lagStringOpplysningForperiode(opptjeningsgrunnlag.getMaksMellomliggendePeriodeForArbeidsforhold()));
            vilkårgrunnlag.setMinForegaaendeForMellomliggendePeriodeForArbeidsforhold(VedtakXmlUtil.lagStringOpplysningForperiode(opptjeningsgrunnlag.getMinForegåendeForMellomliggendePeriodeForArbeidsforhold()));
            vilkårgrunnlag.setPeriodeAntattGodkjentForBehandlingstidspunkt(VedtakXmlUtil.lagStringOpplysningForperiode(opptjeningsgrunnlag.getPeriodeAntattGodkjentFørBehandlingstidspunkt()));
        }

        return vilkårgrunnlag;
    }

    private Vilkaarsgrunnlag lagVilkaarsgrunnlagForSøkersopplysningsplikt(Behandling behandling, Optional<Søknad> optionalSøknad) {
        boolean elektroniskSøknad;
        LocalDate mottattDato;
        LocalDate skjæringstidspunkt;
        if (!optionalSøknad.isPresent()) {
            elektroniskSøknad = false;
            mottattDato = null;
            skjæringstidspunkt = null;
        } else {
            Søknad søknad = optionalSøknad.get();
            mottattDato = getMottattDato(behandling);
            elektroniskSøknad = søknad.getElektroniskRegistrert();
            skjæringstidspunkt = getSkjæringstidsunkt(behandling);
        }
        VilkaarsgrunnlagSoekersopplysningsplikt vilkårgrunnlag = vilkårObjectFactory.createVilkaarsgrunnlagSoekersopplysningsplikt();

        vilkårgrunnlag.setElektroniskSoeknad(VedtakXmlUtil.lagBooleanOpplysning(elektroniskSøknad));
        VedtakXmlUtil.lagDateOpplysning(mottattDato).ifPresent(vilkårgrunnlag::setSoeknadMottatDato);
        VedtakXmlUtil.lagDateOpplysning(skjæringstidspunkt).ifPresent(vilkårgrunnlag::setSkjaeringstidspunkt);
        return vilkårgrunnlag;
    }

    private LocalDate getSkjæringstidsunkt(Behandling behandling) {
        return skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
    }
}
