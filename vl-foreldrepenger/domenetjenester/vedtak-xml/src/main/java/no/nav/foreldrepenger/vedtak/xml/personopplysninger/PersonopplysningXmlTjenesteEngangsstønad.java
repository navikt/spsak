package no.nav.foreldrepenger.vedtak.xml.personopplysninger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonRelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.felles.v2.BooleanOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.DateOpplysning;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Addresse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Adopsjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Familierelasjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.PersonopplysningerEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Terminbekreftelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Verge;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Foedsel;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.PersonIdentifiserbar;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Personopplysninger;

@ApplicationScoped
public class PersonopplysningXmlTjenesteEngangsstønad extends PersonopplysningXmlTjeneste {

    private final no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory personopplysningBaseObjectFactory = new no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory();
    private final ObjectFactory personopplysningObjectFactory = new ObjectFactory();
    private FamilieHendelseRepository familieHendelseRepository;
    private VergeRepository vergeRepository;
    private MedlemskapRepository medlemskapRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    public PersonopplysningXmlTjenesteEngangsstønad() {
        //For CDI
    }

    @Inject
    public PersonopplysningXmlTjenesteEngangsstønad(TpsTjeneste tpsTjeneste, BehandlingRepositoryProvider provider, PersonopplysningTjeneste personopplysningTjeneste) {
        super(tpsTjeneste, personopplysningTjeneste);
        this.familieHendelseRepository = provider.getFamilieGrunnlagRepository();
        this.vergeRepository = provider.getVergeGrunnlagRepository();
        this.medlemskapRepository = provider.getMedlemskapRepository();
        this.inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
    }

    @Override
    public Personopplysninger lagPersonopplysning(PersonopplysningerAggregat personopplysningerAggregat, Behandling behandling) {
        PersonopplysningerEngangsstoenad personopplysninger = personopplysningObjectFactory.createPersonopplysningerEngangsstoenad();
        Optional<FamilieHendelseGrunnlag> familieHendelseAggregatOptional = familieHendelseRepository.hentAggregatHvisEksisterer(behandling);
        if (familieHendelseAggregatOptional.isPresent()) {
            FamilieHendelseGrunnlag familieHendelseGrunnlag = familieHendelseAggregatOptional.get();
            setAdopsjon(personopplysninger, familieHendelseGrunnlag, personopplysningerAggregat);
            setFoedsel(personopplysninger, familieHendelseGrunnlag);
            setVerge(behandling, personopplysninger);
            setMedlemskapsperioder(behandling, personopplysninger);
            setOmsorgsovertakelse(personopplysninger, familieHendelseGrunnlag);
            setTerminbekreftelse(personopplysninger, familieHendelseGrunnlag);
        }
        setAdresse(personopplysninger, personopplysningerAggregat);
        setInntekter(behandling, personopplysninger);
        setBruker(personopplysninger, personopplysningerAggregat);
        setFamilierelasjoner(personopplysninger, personopplysningerAggregat);
        setRelaterteYtelser(behandling, personopplysninger);
        return personopplysninger;
    }

    private void setRelaterteYtelser(Behandling behandling, PersonopplysningerEngangsstoenad personopplysninger) {
        final Collection<Ytelse> ytelser = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null).flatMap
            (it -> it.getAktørYtelseFørStp(behandling.getAktørId()).map(AktørYtelse::getYtelser)).orElse(Collections.emptyList());
        if (!ytelser.isEmpty()) {
            PersonopplysningerEngangsstoenad.RelaterteYtelser relatertYtelse = personopplysningObjectFactory.createPersonopplysningerEngangsstoenadRelaterteYtelser();
            personopplysninger.setRelaterteYtelser(relatertYtelse);
        }
    }

    private void setTerminbekreftelse(PersonopplysningerEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        if (familieHendelseGrunnlag.getGjeldendeVersjon().getType().equals(FamilieHendelseType.TERMIN)) {
            Optional<no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse> terminbekreftelseOptional = familieHendelseGrunnlag.getGjeldendeVersjon().getTerminbekreftelse();
            if (terminbekreftelseOptional.isPresent()) {
                no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse terminbekreftelseFraBehandling = terminbekreftelseOptional.get();
                Terminbekreftelse terminbekreftelse = personopplysningObjectFactory.createTerminbekreftelse();
                terminbekreftelse.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(familieHendelseGrunnlag.getGjeldendeAntallBarn()));

                Optional<DateOpplysning> utstedtDato = VedtakXmlUtil.lagDateOpplysning(terminbekreftelseFraBehandling.getUtstedtdato());
                utstedtDato.ifPresent(terminbekreftelse::setUtstedtDato);

                Optional<DateOpplysning> terminDato = VedtakXmlUtil.lagDateOpplysning(terminbekreftelseFraBehandling.getTermindato());
                terminDato.ifPresent(terminbekreftelse::setTermindato);

                personopplysninger.setTerminbekreftelse(terminbekreftelse);
            }
        }
    }

    private void setOmsorgsovertakelse(PersonopplysningerEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        if (familieHendelseGrunnlag.getGjeldendeVersjon().getType().equals(FamilieHendelseType.OMSORG)) {
            Optional<no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon> adopsjonOptional = familieHendelseGrunnlag.getGjeldendeVersjon().getAdopsjon();
            if (adopsjonOptional.isPresent()) {
                no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon adopsjonFraBehandling = adopsjonOptional.get();
                Omsorgsovertakelse omsorgsovertakelse = personopplysningObjectFactory.createOmsorgsovertakelse();

                Optional<DateOpplysning> omsorgsovertakelsesDato = VedtakXmlUtil.lagDateOpplysning(adopsjonFraBehandling.getOmsorgsovertakelseDato());
                omsorgsovertakelsesDato.ifPresent(omsorgsovertakelse::setOmsorgsovertakelsesdato);
                personopplysninger.setOmsorgsovertakelse(omsorgsovertakelse);
            }
        }
    }

    private void setMedlemskapsperioder(Behandling behandling, PersonopplysningerEngangsstoenad personopplysninger) {
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);


        if (medlemskap.isPresent()) {
            MedlemskapAggregat medlemskapPerioderFraBehandling = medlemskap.get();

            PersonopplysningerEngangsstoenad.Medlemskapsperioder medlemskapsperioder = personopplysningObjectFactory.createPersonopplysningerEngangsstoenadMedlemskapsperioder();
            personopplysninger.setMedlemskapsperioder(medlemskapsperioder);
            medlemskapPerioderFraBehandling.getRegistrertMedlemskapPerioder()
                .forEach(medlemskapPeriode -> personopplysninger.getMedlemskapsperioder().getMedlemskapsperiode()
                    .add(lagMedlemskapPeriode(medlemskapPeriode)));
        }
    }

    private void setVerge(Behandling behandling, PersonopplysningerEngangsstoenad personopplysninger) {
        Optional<VergeAggregat> vergeAggregatOptional = vergeRepository.hentAggregat(behandling);
        if (vergeAggregatOptional.isPresent()) {
            VergeAggregat vergeAggregat = vergeAggregatOptional.get();
            no.nav.foreldrepenger.behandlingslager.behandling.verge.Verge vergeFraBehandling = vergeAggregat.getVerge();
            Verge verge = personopplysningObjectFactory.createVerge();
            String vergeNavn = hentVergeNavn(vergeAggregat.getAktørId());
            verge.setNavn(VedtakXmlUtil.lagStringOpplysning(vergeNavn));
            verge.setVergetype(VedtakXmlUtil.lagKodeverkOpplysning(vergeFraBehandling.getVergeType()));
            verge.setGyldighetsperiode(VedtakXmlUtil.lagPeriodeOpplysning(vergeFraBehandling.getGyldigFom(), vergeFraBehandling.getGyldigTom()));
            verge.setMandattekst(VedtakXmlUtil.lagStringOpplysning(vergeFraBehandling.getMandatTekst()));
            // Hvis vergen er stønadsmottaker er det tvungen forvaltning
            verge.setTvungenForvaltning(VedtakXmlUtil.lagBooleanOpplysning(vergeFraBehandling.getStønadMottaker()));
            Optional<DateOpplysning> vedtaksDato = VedtakXmlUtil.lagDateOpplysning(vergeFraBehandling.getVedtaksdato());
            vedtaksDato.ifPresent(verge::setVedtaksdato);

            personopplysninger.setVerge(verge);
        }
    }

    private void setFoedsel(PersonopplysningerEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        FamilieHendelse gjeldendeFamiliehendelse = familieHendelseGrunnlag.getGjeldendeVersjon();
        if (Arrays.asList(FamilieHendelseType.FØDSEL, FamilieHendelseType.TERMIN).contains(gjeldendeFamiliehendelse.getType())) {

            Foedsel fødsel = personopplysningBaseObjectFactory.createFoedsel();

            fødsel.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(gjeldendeFamiliehendelse.getAntallBarn()));
            Optional<LocalDate> fødselsdatoOptional = gjeldendeFamiliehendelse.getFødselsdato();
            if (fødselsdatoOptional.isPresent()) {
                Optional<DateOpplysning> fødselsDato = VedtakXmlUtil.lagDateOpplysning(fødselsdatoOptional.get());
                fødselsDato.ifPresent(fødsel::setFoedselsdato);
            }

            personopplysninger.setFoedsel(fødsel);
        }
    }

    private void setFamilierelasjoner(PersonopplysningerEngangsstoenad personopplysninger, PersonopplysningerAggregat aggregat) {
        final Map<AktørId, Personopplysning> aktørPersonopplysningMap = aggregat.getAktørPersonopplysningMap();
        final List<PersonRelasjon> tilPersoner = aggregat.getSøkersRelasjoner();
        if (tilPersoner != null && !tilPersoner.isEmpty()) {

            PersonopplysningerEngangsstoenad.Familierelasjoner familierelasjoner = personopplysningObjectFactory
                .createPersonopplysningerEngangsstoenadFamilierelasjoner();
            personopplysninger.setFamilierelasjoner(familierelasjoner);
            tilPersoner.forEach(relasjon -> personopplysninger.getFamilierelasjoner().getFamilierelasjon()
                .add(lagRelasjon(relasjon, aktørPersonopplysningMap.get(relasjon.getTilAktørId()), aggregat)));
        }
    }

    private void setBruker(PersonopplysningerEngangsstoenad personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        PersonIdentifiserbar person = lagBruker(personopplysningerAggregat, personopplysningerAggregat.getSøker());
        personopplysninger.setBruker(person);
    }

    private void setInntekter(Behandling behandling, PersonopplysningerEngangsstoenad personopplysninger) {
        Optional<InntektArbeidYtelseGrunnlag> aggregatOpt = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null);
        if (aggregatOpt.isPresent()) {
            InntektArbeidYtelseGrunnlag aggregat = aggregatOpt.get();
            Collection<AktørInntekt> aktørInntekt = aggregat.getAktørInntektForFørStp();
            if (aktørInntekt != null) {
                PersonopplysningerEngangsstoenad.Inntekter inntekterPersonopplysning = personopplysningObjectFactory.createPersonopplysningerEngangsstoenadInntekter();
                aktørInntekt.forEach(inntekt -> inntekterPersonopplysning.getInntekt().addAll(lagInntekt(inntekt)));
                personopplysninger.setInntekter(inntekterPersonopplysning);
            }
        }
    }

    private List<no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Inntekt> lagInntekt(AktørInntekt aktørInntekt) {
        List<no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Inntekt> inntektList = new ArrayList<>();
        aktørInntekt.getInntektPensjonsgivende().forEach(inntekt -> inntekt.getInntektspost().forEach(inntektspost -> {
            no.nav.vedtak.felles.xml.vedtak.personopplysninger.es.v2.Inntekt inntektXML = personopplysningObjectFactory.createInntekt(); // NOSONAR
            if (inntekt.getArbeidsgiver() != null) {
                inntektXML.setArbeidsgiver(VedtakXmlUtil.lagStringOpplysning(inntekt.getArbeidsgiver().getIdentifikator()));
            }
            inntektXML.setBeloep(VedtakXmlUtil.lagDoubleOpplysning(inntektspost.getBeløp().getVerdi().doubleValue()));
            inntektXML.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(inntektspost.getFraOgMed(), inntektspost.getTilOgMed()));
            inntektXML.setMottakerAktoerId(VedtakXmlUtil.lagStringOpplysning(String.valueOf(aktørInntekt.getAktørId())));
            inntektXML.setYtelse(VedtakXmlUtil.lagBooleanOpplysning(inntektspost.getInntektspostType().equals(InntektspostType.YTELSE)));
            inntektList.add(inntektXML);
        }));
        return inntektList;
    }

    private void setAdresse(PersonopplysningerEngangsstoenad personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        final Personopplysning personopplysning = personopplysningerAggregat.getSøker();
        List<PersonAdresse> opplysningAdresser = personopplysningerAggregat.getAdresserFor(personopplysning.getAktørId());
        if (opplysningAdresser != null) {
            opplysningAdresser.forEach(addresse -> personopplysninger.getAdresse().add(lagAdresse(personopplysning, addresse)));
        }
    }

    private Addresse lagAdresse(Personopplysning personopplysning, PersonAdresse adresseFraBehandling) {
        Addresse adresse = personopplysningObjectFactory.createAddresse();
        adresse.setAddresseType(VedtakXmlUtil.lagKodeverkOpplysning(adresseFraBehandling.getAdresseType()));
        adresse.setAddresselinje1(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje1()));
        if (adresseFraBehandling.getAdresselinje2() != null) {
            adresse.setAddresselinje2(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje2()));
        }
        if (adresseFraBehandling.getAdresselinje3() != null) {
            adresse.setAddresselinje3(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje3()));
        }
        adresse.setLand(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getLand()));
        adresse.setMottakersNavn(VedtakXmlUtil.lagStringOpplysning(personopplysning.getNavn()));
        adresse.setPostnummer(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getPostnummer()));
        return adresse;
    }

    private void setAdopsjon(PersonopplysningerEngangsstoenad personopplysninger, FamilieHendelseGrunnlag familieHendelseGrunnlag, PersonopplysningerAggregat personopplysningerAggregat) {

        Optional<no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon> adopsjonhendelseOptional = familieHendelseGrunnlag.getGjeldendeAdopsjon();
        if (adopsjonhendelseOptional.isPresent()) {

            Adopsjon adopsjon = personopplysningObjectFactory.createAdopsjon();
            no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon adopsjonhendelse = adopsjonhendelseOptional.get();
            if (adopsjonhendelse.getErEktefellesBarn() != null) {
                BooleanOpplysning erEktefellesBarn = VedtakXmlUtil.lagBooleanOpplysning(adopsjonhendelse.getErEktefellesBarn());
                adopsjon.setErEktefellesBarn(erEktefellesBarn);
            }

            familieHendelseGrunnlag.getGjeldendeBarna()
                .forEach(aBarn -> adopsjon.getAdopsjonsbarn().add(leggTilAdopsjonsbarn(aBarn)));

            boolean erMann = NavBrukerKjønn.MANN.equals(personopplysningerAggregat.getSøker().getKjønn());
            if (erMann && adopsjonhendelse.getAdoptererAlene() != null) {
                adopsjon.setErMannAdoptererAlene(VedtakXmlUtil.lagBooleanOpplysning(adopsjonhendelse.getAdoptererAlene()));

            }
            if (adopsjonhendelse.getOmsorgsovertakelseDato() != null) {
                Optional<DateOpplysning> omsorgOvertakelsesDato = VedtakXmlUtil.lagDateOpplysning(adopsjonhendelse.getOmsorgsovertakelseDato());
                omsorgOvertakelsesDato.ifPresent(adopsjon::setOmsorgsovertakelsesdato);
            }
            personopplysninger.setAdopsjon(adopsjon);
        }
    }

    private Adopsjon.Adopsjonsbarn leggTilAdopsjonsbarn(UidentifisertBarn aBarn) {
        Adopsjon.Adopsjonsbarn adopsjonAdopsjonsbarn = personopplysningObjectFactory.createAdopsjonAdopsjonsbarn();
        Optional<DateOpplysning> dateOpplysning = VedtakXmlUtil.lagDateOpplysning(aBarn.getFødselsdato());
        dateOpplysning.ifPresent(adopsjonAdopsjonsbarn::setFoedselsdato);
        return adopsjonAdopsjonsbarn;
    }

    private Familierelasjon lagRelasjon(PersonRelasjon relasjon, Personopplysning tilPerson, PersonopplysningerAggregat aggregat) {
        Familierelasjon familierelasjon = personopplysningObjectFactory.createFamilierelasjon();
        PersonIdentifiserbar person = lagBruker(aggregat, tilPerson);
        familierelasjon.setTilPerson(person);
        familierelasjon.setRelasjon(VedtakXmlUtil.lagKodeverkOpplysning(relasjon.getRelasjonsrolle()));
        return familierelasjon;
    }

}
