package no.nav.foreldrepenger.vedtak.xml.personopplysninger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonRelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.felles.v2.BooleanOpplysning;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Addresse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Adopsjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Familierelasjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Inntekt;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.PersonopplysningerDvhEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Terminbekreftelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.es.v2.Verge;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Foedsel;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.PersonUidentifiserbar;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Personopplysninger;

@ApplicationScoped
public class DvhPersonopplysningXmlTjenesteEngangsstønad extends PersonopplysningXmlTjeneste {

    private final no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory personopplysningBaseObjectFactory = new no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory();
    private final ObjectFactory personopplysningDvhObjectFactory = new ObjectFactory();
    private FamilieHendelseRepository familieHendelseRepository;
    private VergeRepository vergeRepository;
    private MedlemskapRepository medlemskapRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    public DvhPersonopplysningXmlTjenesteEngangsstønad() {
        //CDI
    }

    @Inject
    public DvhPersonopplysningXmlTjenesteEngangsstønad(TpsTjeneste tpsTjeneste, BehandlingRepositoryProvider provider, PersonopplysningTjeneste personopplysningTjeneste) {
        super(tpsTjeneste, personopplysningTjeneste);
        this.familieHendelseRepository = provider.getFamilieGrunnlagRepository();
        this.vergeRepository = provider.getVergeGrunnlagRepository();
        this.medlemskapRepository = provider.getMedlemskapRepository();
        this.inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
    }

    @Override
    public Personopplysninger lagPersonopplysning(PersonopplysningerAggregat personopplysningerAggregat, Behandling behandling) {
        PersonopplysningerDvhEngangsstoenad personopplysninger = personopplysningDvhObjectFactory.createPersonopplysningerDvhEngangsstoenad();
        Optional<FamilieHendelseGrunnlag> familieHendelseAggregatOptional = familieHendelseRepository.hentAggregatHvisEksisterer(behandling);

        familieHendelseAggregatOptional.ifPresent(familieHendelseGrunnlaghGr -> {
            setAdopsjon(personopplysninger, familieHendelseGrunnlaghGr);
            setFødsel(personopplysninger, familieHendelseGrunnlaghGr);
            setVerge(behandling, personopplysninger);
            setMedlemskapsperioder(behandling, personopplysninger);
            setOmsorgovertakelse(personopplysninger, familieHendelseGrunnlaghGr);
            setTerminbekreftelse(personopplysninger, familieHendelseGrunnlaghGr);
        });

        setAdresse(personopplysninger, personopplysningerAggregat);
        setInntekter(behandling, personopplysninger);
        setBruker(personopplysninger, personopplysningerAggregat);
        setFamilierelasjoner(personopplysninger, personopplysningerAggregat);
        setRelaterteYtelser(behandling, personopplysninger);

        return personopplysninger;
    }

    private void setRelaterteYtelser(Behandling behandling, PersonopplysningerDvhEngangsstoenad personopplysninger) {
        final Collection<Ytelse> ytelser = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null)
            .flatMap(it -> it.getAktørYtelseFørStp(behandling.getAktørId())
                .map(AktørYtelse::getYtelser))
            .orElse(Collections.emptyList());
        if (!ytelser.isEmpty()) {
            PersonopplysningerDvhEngangsstoenad.RelaterteYtelser relaterteYtelse = personopplysningDvhObjectFactory.createPersonopplysningerDvhEngangsstoenadRelaterteYtelser();
            personopplysninger.setRelaterteYtelser(relaterteYtelse);
        }
    }

    private void setFamilierelasjoner(PersonopplysningerDvhEngangsstoenad personopplysninger, PersonopplysningerAggregat aggregat) {
        final Map<AktørId, Personopplysning> aktørPersonopplysningMap = aggregat.getAktørPersonopplysningMap();
        final List<PersonRelasjon> tilPersoner = aggregat.getSøkersRelasjoner();
        if (tilPersoner != null && !tilPersoner.isEmpty()) {
            PersonopplysningerDvhEngangsstoenad.Familierelasjoner familierelasjoner = personopplysningDvhObjectFactory.createPersonopplysningerDvhEngangsstoenadFamilierelasjoner();
            personopplysninger.setFamilierelasjoner(familierelasjoner);
            tilPersoner.forEach(relasjon -> personopplysninger.getFamilierelasjoner().getFamilierelasjon()
                .add(lagRelasjon(relasjon, aktørPersonopplysningMap.get(relasjon.getTilAktørId()), aggregat))
            );
        }
    }

    private Familierelasjon lagRelasjon(PersonRelasjon relasjon, Personopplysning tilPerson, PersonopplysningerAggregat aggregat) {
        Familierelasjon familierelasjon = personopplysningDvhObjectFactory.createFamilierelasjon();
        PersonUidentifiserbar person = lagUidentifiserbarBruker(aggregat, tilPerson);
        familierelasjon.setTilPerson(person);
        familierelasjon.setRelasjon(VedtakXmlUtil.lagKodeverkOpplysning(relasjon.getRelasjonsrolle()));
        return familierelasjon;
    }

    private void setBruker(PersonopplysningerDvhEngangsstoenad personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        PersonUidentifiserbar person = lagUidentifiserbarBruker(personopplysningerAggregat, personopplysningerAggregat.getSøker());
        personopplysninger.setBruker(person);
    }

    private void setInntekter(Behandling behandling, PersonopplysningerDvhEngangsstoenad personopplysninger) {
        inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null).ifPresent(aggregat -> {
            Collection<AktørInntekt> aktørInntekt = aggregat.getAktørInntektForFørStp();
            if (aktørInntekt != null) {
                PersonopplysningerDvhEngangsstoenad.Inntekter inntekterPersonopplysning = personopplysningDvhObjectFactory.createPersonopplysningerDvhEngangsstoenadInntekter();
                aktørInntekt.forEach(inntekt -> inntekterPersonopplysning.getInntekt().addAll(lagInntekt(inntekt)));
                personopplysninger.setInntekter(inntekterPersonopplysning);
            }
        });
    }

    private List<? extends Inntekt> lagInntekt(AktørInntekt aktørInntekt) {
        List<Inntekt> inntektList = new ArrayList<>();
        aktørInntekt.getInntektPensjonsgivende().forEach(inntekt ->
            inntekt.getInntektspost().forEach(inntektspost -> {
                Inntekt inntektXml = personopplysningDvhObjectFactory.createInntekt();
                if (inntekt.getArbeidsgiver() != null) {
                    inntektXml.setArbeidsgiver(VedtakXmlUtil.lagStringOpplysning(inntekt.getArbeidsgiver().getIdentifikator()));
                }
                inntektXml.setBeloep(VedtakXmlUtil.lagDoubleOpplysning(inntektspost.getBeløp().getVerdi().doubleValue()));
                inntektXml.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(inntektspost.getFraOgMed(), inntektspost.getTilOgMed()));
                inntektXml.setMottakerAktoerId(VedtakXmlUtil.lagStringOpplysning(aktørInntekt.getAktørId().getId()));
                inntektXml.setYtelse(VedtakXmlUtil.lagBooleanOpplysning(inntektspost.getInntektspostType().equals(InntektspostType.YTELSE)));
                inntektList.add(inntektXml);
            })
        );
        return inntektList;
    }

    private void setTerminbekreftelse(PersonopplysningerDvhEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        if (familieHendelseGrunnlag.getGjeldendeVersjon().getType().equals(FamilieHendelseType.TERMIN)) {
            Optional<no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse> terminbekreftelseOptional = familieHendelseGrunnlag.getGjeldendeTerminbekreftelse();
            terminbekreftelseOptional.ifPresent(terminbekreftelseFraBehandling -> {
                Terminbekreftelse terminbekreftelse = personopplysningDvhObjectFactory.createTerminbekreftelse();
                terminbekreftelse.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(familieHendelseGrunnlag.getGjeldendeAntallBarn()));

                VedtakXmlUtil.lagDateOpplysning(terminbekreftelseFraBehandling.getUtstedtdato())
                    .ifPresent(terminbekreftelse::setUtstedtDato);

                VedtakXmlUtil.lagDateOpplysning(terminbekreftelseFraBehandling.getTermindato()).ifPresent(terminbekreftelse::setTermindato);

                personopplysninger.setTerminbekreftelse(terminbekreftelse);
            });
        }
    }

    private void setOmsorgovertakelse(PersonopplysningerDvhEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        if (familieHendelseGrunnlag.getGjeldendeVersjon().getType().equals(FamilieHendelseType.OMSORG)) {
            familieHendelseGrunnlag.getGjeldendeAdopsjon().ifPresent(adopsjonFraBehandling -> {
                Omsorgsovertakelse omsorgsovertakelse = personopplysningDvhObjectFactory.createOmsorgsovertakelse();

                VedtakXmlUtil.lagDateOpplysning(adopsjonFraBehandling.getOmsorgsovertakelseDato())
                    .ifPresent(omsorgsovertakelse::setOmsorgsovertakelsesdato);

                personopplysninger.setOmsorgsovertakelse(omsorgsovertakelse);
            });
        }
    }

    private void setMedlemskapsperioder(Behandling behandling, PersonopplysningerDvhEngangsstoenad personopplysninger) {
        medlemskapRepository.hentMedlemskap(behandling).ifPresent(medlemskapperioderFraBehandling -> {
            PersonopplysningerDvhEngangsstoenad.Medlemskapsperioder medlemskapsperioder = personopplysningDvhObjectFactory.createPersonopplysningerDvhEngangsstoenadMedlemskapsperioder();
            personopplysninger.setMedlemskapsperioder(medlemskapsperioder);
            medlemskapperioderFraBehandling.getRegistrertMedlemskapPerioder()
                .forEach(medlemskapsperiode -> personopplysninger.getMedlemskapsperioder().getMedlemskapsperiode()
                    .add(lagMedlemskapPeriode(medlemskapsperiode)));
        });
    }

    private void setVerge(Behandling behandling, PersonopplysningerDvhEngangsstoenad personopplysninger) {
        vergeRepository.hentAggregat(behandling).ifPresent(vergeAggregat -> {
            no.nav.foreldrepenger.behandlingslager.behandling.verge.Verge vergeFraBehandling = vergeAggregat.getVerge();
            Verge verge = personopplysningDvhObjectFactory.createVerge();
            verge.setVergetype(VedtakXmlUtil.lagKodeverkOpplysning(vergeFraBehandling.getVergeType()));
            verge.setGyldighetsperiode(VedtakXmlUtil.lagPeriodeOpplysning(vergeFraBehandling.getGyldigFom(), vergeFraBehandling.getGyldigTom()));
            verge.setMandattekst(VedtakXmlUtil.lagStringOpplysning(vergeFraBehandling.getMandatTekst()));
            verge.setTvungenForvaltning(VedtakXmlUtil.lagBooleanOpplysning(vergeFraBehandling.getStønadMottaker()));
            VedtakXmlUtil.lagDateOpplysning(vergeFraBehandling.getVedtaksdato())
                .ifPresent(verge::setVedtaksdato);

            personopplysninger.setVerge(verge);
        });
    }

    private void setFødsel(PersonopplysningerDvhEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        FamilieHendelse gjeldendeFamilieHendelse = familieHendelseGrunnlag.getGjeldendeVersjon();
        if (Arrays.asList(FamilieHendelseType.FØDSEL, FamilieHendelseType.TERMIN).contains(gjeldendeFamilieHendelse.getType())) {
            Foedsel fødsel = personopplysningBaseObjectFactory.createFoedsel();
            fødsel.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(gjeldendeFamilieHendelse.getAntallBarn()));
            gjeldendeFamilieHendelse.getFødselsdato().ifPresent(fødselsdato ->
                VedtakXmlUtil.lagDateOpplysning(fødselsdato).ifPresent(fødsel::setFoedselsdato)
            );
            personopplysninger.setFoedsel(fødsel);
        }
    }

    private void setAdopsjon(PersonopplysningerDvhEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        familieHendelseGrunnlag.getGjeldendeAdopsjon().ifPresent(adopsjonHendelse -> {
            Adopsjon adopsjon = personopplysningDvhObjectFactory.createAdopsjon();
            if (adopsjonHendelse.getErEktefellesBarn() != null) {
                BooleanOpplysning erEktefellesBarn = VedtakXmlUtil.lagBooleanOpplysning(adopsjonHendelse.getErEktefellesBarn());
                adopsjon.setErEktefellesBarn(erEktefellesBarn);
            }

            familieHendelseGrunnlag.getGjeldendeBarna().forEach(aBarn -> adopsjon.getAdopsjonsbarn().add(leggTilAdopsjonsbarn(aBarn)));
            personopplysninger.setAdopsjon(adopsjon);
        });
    }

    private Adopsjon.Adopsjonsbarn leggTilAdopsjonsbarn(UidentifisertBarn aBarn) {
        Adopsjon.Adopsjonsbarn adopsjonsbarn = personopplysningDvhObjectFactory.createAdopsjonAdopsjonsbarn();
        VedtakXmlUtil.lagDateOpplysning(aBarn.getFødselsdato()).ifPresent(adopsjonsbarn::setFoedselsdato);
        return adopsjonsbarn;
    }

    private void setAdresse(PersonopplysningerDvhEngangsstoenad personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        final Personopplysning personopplysning = personopplysningerAggregat.getSøker();
        List<PersonAdresse> opplysningAdresser = personopplysningerAggregat.getAdresserFor(personopplysning.getAktørId());
        if (opplysningAdresser != null) {
            opplysningAdresser.forEach(adresse -> personopplysninger.getAdresse().add(lagAdresse(adresse)));
        }
    }

    private Addresse lagAdresse(PersonAdresse adresseFraBehandling) {
        Addresse adresse = personopplysningDvhObjectFactory.createAddresse();
        adresse.setAddresseType(VedtakXmlUtil.lagKodeverkOpplysning(adresseFraBehandling.getAdresseType()));
        adresse.setLand(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getLand()));
        adresse.setPostnummer(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getPostnummer()));
        return adresse;
    }
}
