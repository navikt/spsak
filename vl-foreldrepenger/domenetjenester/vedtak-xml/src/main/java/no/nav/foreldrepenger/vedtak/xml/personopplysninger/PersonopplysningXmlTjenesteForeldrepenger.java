package no.nav.foreldrepenger.vedtak.xml.personopplysninger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseAnvist;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.YtelseStørrelse;
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
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Addresse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Familierelasjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Inntekt;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Inntektspost;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Medlemskap;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.PersonopplysningerForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.RelatertYtelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Verge;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.Virksomhet;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseStorrelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.PersonIdentifiserbar;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Personopplysninger;

@ApplicationScoped
public class PersonopplysningXmlTjenesteForeldrepenger extends PersonopplysningXmlTjeneste {
    private final ObjectFactory personopplysningObjectFactory = new ObjectFactory();

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private MedlemskapRepository medlemskapRepository;
    private VergeRepository vergeRepository;

    public PersonopplysningXmlTjenesteForeldrepenger() {
        //For CDI
    }

    @Inject
    public PersonopplysningXmlTjenesteForeldrepenger(TpsTjeneste tpsTjeneste, BehandlingRepositoryProvider provider, PersonopplysningTjeneste personopplysningTjeneste) {
        super(tpsTjeneste, personopplysningTjeneste);
        this.inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
        this.medlemskapRepository = provider.getMedlemskapRepository();
        this.vergeRepository = provider.getVergeGrunnlagRepository();
    }

    @Override
    public Personopplysninger lagPersonopplysning(PersonopplysningerAggregat personopplysningerAggregat, Behandling behandling) {
        PersonopplysningerForeldrepenger personopplysninger = personopplysningObjectFactory.createPersonopplysningerForeldrepenger();
        setVerge(behandling, personopplysninger);
        setMedlemskapsperioder(behandling, personopplysninger);
        setAdresse(personopplysninger, personopplysningerAggregat);
        setInntekter(behandling, personopplysninger);
        setBruker(personopplysninger, personopplysningerAggregat);
        setFamilierelasjoner(personopplysninger, personopplysningerAggregat);
        setRelaterteYtelser(behandling, personopplysninger);

        return personopplysninger;
    }

    private void setRelaterteYtelser(Behandling behandling, PersonopplysningerForeldrepenger personopplysninger) {
        final Collection<Ytelse> ytelser = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null)
            .flatMap(it -> it.getAktørYtelseFørStp(behandling.getAktørId()).map(AktørYtelse::getYtelser)).orElse(Collections.emptyList());
        if (!ytelser.isEmpty()) {
            PersonopplysningerForeldrepenger.RelaterteYtelser relaterteYtelser = personopplysningObjectFactory.createPersonopplysningerForeldrepengerRelaterteYtelser();
            ytelser.stream().forEach(ytelse -> relaterteYtelser.getRelatertYtelse().add(konverterFraDomene(ytelse)));
            personopplysninger.setRelaterteYtelser(relaterteYtelser);
        }
    }

    private RelatertYtelse konverterFraDomene(Ytelse ytelse) {
        RelatertYtelse relatertYtelse = personopplysningObjectFactory.createRelatertYtelse();
        relatertYtelse.setBehandlingstema(VedtakXmlUtil.lagKodeverkOpplysning(ytelse.getBehandlingsTema()));
        relatertYtelse.setKilde(VedtakXmlUtil.lagKodeverkOpplysning(ytelse.getKilde()));
        Optional.ofNullable(ytelse.getPeriode()).ifPresent(periode -> relatertYtelse.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(periode.getFomDato(), periode.getTomDato())));
        Optional.ofNullable(ytelse.getSaksnummer()).ifPresent(saksnummer -> relatertYtelse.setSaksnummer(VedtakXmlUtil.lagStringOpplysning(saksnummer.getVerdi())));
        relatertYtelse.setStatus(VedtakXmlUtil.lagKodeverkOpplysning(ytelse.getStatus()));
        relatertYtelse.setTemaUnderkategori(VedtakXmlUtil.lagKodeverkOpplysning(ytelse.getFagsystemUnderkategori()));
        relatertYtelse.setType(VedtakXmlUtil.lagKodeverkOpplysning(ytelse.getRelatertYtelseType()));

        setYtelseAnvist(relatertYtelse, ytelse.getYtelseAnvist());
        setYtelsesgrunnlag(relatertYtelse, ytelse.getYtelseGrunnlag());
        setYtelsesStørrelse(relatertYtelse, ytelse.getYtelseGrunnlag());
        return relatertYtelse;
    }

    private void setYtelsesStørrelse(RelatertYtelse relatertYtelseKontrakt, Optional<YtelseGrunnlag> ytelseGrunnlagDomene) {
        if (ytelseGrunnlagDomene.isPresent()) {
            YtelseGrunnlag ytelseGrunnlag = ytelseGrunnlagDomene.get();
            List<YtelseStorrelse> ytelseStorrelser = ytelseGrunnlag.getYtelseStørrelse().stream().map(ys -> konverterFraDomene(ys)).collect(Collectors.toList());
            relatertYtelseKontrakt.getYtelsesstorrelse().addAll(ytelseStorrelser);
        }
    }

    private no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseStorrelse konverterFraDomene(YtelseStørrelse domene) {
        no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseStorrelse kontrakt = personopplysningObjectFactory.createYtelseStorrelse();
        domene.getVirksomhet().ifPresent(virksomhet -> kontrakt.setVirksomhet(tilVirksomhet(virksomhet)));
        kontrakt.setBeloep(VedtakXmlUtil.lagDecimalOpplysning(domene.getBeløp().getVerdi()));
        kontrakt.setHyppighet(VedtakXmlUtil.lagKodeverkOpplysning(domene.getHyppighet()));
        return kontrakt;
    }

    private Virksomhet tilVirksomhet(no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet domene) {
        Virksomhet kontrakt = personopplysningObjectFactory.createVirksomhet();
        kontrakt.setNavn(VedtakXmlUtil.lagStringOpplysning(domene.getNavn()));
        kontrakt.setOrgnr(VedtakXmlUtil.lagStringOpplysning(domene.getOrgnr()));
        return kontrakt;
    }
    private void setYtelsesgrunnlag(RelatertYtelse relatertYtelseKontrakt, Optional<YtelseGrunnlag> YtelseGrunnlagDomene) {
        Optional<no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseGrunnlag> ytelseGrunnlagOptional = YtelseGrunnlagDomene.map(yg -> konverterFraDomene(yg));
        ytelseGrunnlagOptional.ifPresent(ytelseGrunnlag -> {
            relatertYtelseKontrakt.setYtelsesgrunnlag(ytelseGrunnlag);
        });
    }

    private no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseGrunnlag konverterFraDomene(YtelseGrunnlag domene) {
        no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseGrunnlag kontrakt = personopplysningObjectFactory.createYtelseGrunnlag();
        domene.getArbeidskategori().ifPresent(arbeidskategori -> kontrakt.setArbeidtype(VedtakXmlUtil.lagKodeverkOpplysning(arbeidskategori)));
        domene.getDekningsgradProsent().ifPresent(dp -> kontrakt.setDekningsgradprosent(VedtakXmlUtil.lagDecimalOpplysning(dp.getVerdi())));

        domene.getGraderingProsent().ifPresent(graderingsProsent -> kontrakt.setGraderingprosent(VedtakXmlUtil.lagDecimalOpplysning(graderingsProsent.getVerdi())));
        domene.getInntektsgrunnlagProsent().ifPresent(inntektsGrunnlagProsent -> kontrakt.setInntektsgrunnlagprosent(VedtakXmlUtil.lagDecimalOpplysning(inntektsGrunnlagProsent.getVerdi())));


        return kontrakt;
    }

    private void setYtelseAnvist(RelatertYtelse relatertYtelseKontrakt, Collection<YtelseAnvist> ytelseAnvistDomene) {
        List<no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseAnvist> alleYtelserAnvist = ytelseAnvistDomene.stream().map(ytelseAnvist -> konverterFraDomene(ytelseAnvist)).collect(Collectors.toList());
        relatertYtelseKontrakt.getYtelseanvist().addAll(alleYtelserAnvist);
    }

    private no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseAnvist konverterFraDomene(YtelseAnvist domene) {
        no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.YtelseAnvist kontrakt = personopplysningObjectFactory.createYtelseAnvist();
        domene.getBeløp().ifPresent(beløp -> kontrakt.setBeloep(VedtakXmlUtil.lagDecimalOpplysning(beløp.getVerdi())));
        domene.getDagsats().ifPresent(dagsats -> kontrakt.setDagsats(VedtakXmlUtil.lagDecimalOpplysning(dagsats)));
        kontrakt.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(domene.getAnvistFOM(), domene.getAnvistTOM()));
        domene.getUtbetalingsgradProsent().ifPresent(prosent -> kontrakt.setUtbetalingsgradprosent(VedtakXmlUtil.lagDecimalOpplysning(prosent)));

        return kontrakt;
    }

    private void setFamilierelasjoner(PersonopplysningerForeldrepenger personopplysninger, PersonopplysningerAggregat aggregat) {
        final Map<AktørId, Personopplysning> aktørPersonopplysningMap = aggregat.getAktørPersonopplysningMap();
        final List<PersonRelasjon> tilPersoner = aggregat.getSøkersRelasjoner();
        if (tilPersoner != null && !tilPersoner.isEmpty()) {
            PersonopplysningerForeldrepenger.Familierelasjoner familierelasjoner = personopplysningObjectFactory.createPersonopplysningerForeldrepengerFamilierelasjoner();
            personopplysninger.setFamilierelasjoner(familierelasjoner);
            tilPersoner.forEach(relasjon -> personopplysninger.getFamilierelasjoner().getFamilierelasjon()
                .add(lagRelasjon(relasjon, aktørPersonopplysningMap.get(relasjon.getTilAktørId()), aggregat)));
        }
    }

    private Familierelasjon lagRelasjon(PersonRelasjon relasjon, Personopplysning tilPerson, PersonopplysningerAggregat aggregat) {
        Familierelasjon familierelasjon = personopplysningObjectFactory.createFamilierelasjon();
        PersonIdentifiserbar person = lagBruker(aggregat, tilPerson);
        familierelasjon.setTilPerson(person);
        familierelasjon.setRelasjon(VedtakXmlUtil.lagKodeverkOpplysning(relasjon.getRelasjonsrolle()));
        return familierelasjon;
    }

    private void setInntekter(Behandling behandling, PersonopplysningerForeldrepenger personopplysninger) {
        inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null).ifPresent(aggregat -> {
            Collection<AktørInntekt> aktørInntekt = aggregat.getAktørInntektForFørStp();
            if (aktørInntekt != null) {
                PersonopplysningerForeldrepenger.Inntekter inntekter = personopplysningObjectFactory.createPersonopplysningerForeldrepengerInntekter();
                aktørInntekt.forEach(inntekt -> {
                    inntekter.getInntekt().addAll(lagInntekt(inntekt));
                    personopplysninger.setInntekter(inntekter);
                });
            }
        });
    }

    private Collection<? extends Inntekt> lagInntekt(AktørInntekt aktørInntekt) {
        List<Inntekt> inntektList = new ArrayList<>();
        List<Inntektspost> inntektspostList = new ArrayList<>();

        aktørInntekt.getInntektPensjonsgivende().forEach(inntekt -> {
            Inntekt inntektXML = personopplysningObjectFactory.createInntekt();
            inntekt.getInntektspost().forEach(inntektspost -> {
                Inntektspost inntektspostXML = personopplysningObjectFactory.createInntektspost();
                if (inntekt.getArbeidsgiver() != null) {
                    inntektXML.setArbeidsgiver(VedtakXmlUtil.lagStringOpplysning(inntekt.getArbeidsgiver().getIdentifikator()));
                }
                inntektspostXML.setBeloep(VedtakXmlUtil.lagDoubleOpplysning(inntektspost.getBeløp().getVerdi().doubleValue()));
                inntektspostXML.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(inntektspost.getFraOgMed(), inntektspost.getTilOgMed()));
                inntektspostXML.setYtelsetype(VedtakXmlUtil.lagStringOpplysning(inntektspost.getInntektspostType().getKode()));

                inntektXML.getInntektsposter().add(inntektspostXML);
                inntektXML.setMottaker(VedtakXmlUtil.lagStringOpplysning(aktørInntekt.getAktørId().getId()));
                inntektspostList.add(inntektspostXML);
            });
            inntektList.add(inntektXML);
        });
        return inntektList;
    }

    private void setBruker(PersonopplysningerForeldrepenger personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        PersonIdentifiserbar person = lagBruker(personopplysningerAggregat, personopplysningerAggregat.getSøker());
        personopplysninger.setBruker(person);
    }

    private void setAdresse(PersonopplysningerForeldrepenger personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        final Personopplysning personopplysning = personopplysningerAggregat.getSøker();
        List<PersonAdresse> opplysningAdresser = personopplysningerAggregat.getAdresserFor(personopplysning.getAktørId());
        if (opplysningAdresser != null) {
            opplysningAdresser.forEach(adresse -> personopplysninger.getAdresse().add(lagAdresse(personopplysning, adresse)));
        }
    }

    private Addresse lagAdresse(Personopplysning personopplysning, PersonAdresse adresseFraBehandling) {
        Addresse adresse = personopplysningObjectFactory.createAddresse();
        adresse.setAdressetype(VedtakXmlUtil.lagKodeverkOpplysning(adresseFraBehandling.getAdresseType()));
        adresse.setAddresselinje1(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje1()));
        if (adresseFraBehandling.getAdresselinje2() != null) {
            adresse.setAddresselinje2(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje2()));
        }
        if (adresseFraBehandling.getAdresselinje3() != null) {
            adresse.setAddresselinje3(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje3()));
        }
        if (adresseFraBehandling.getAdresselinje4() != null) {
            adresse.setAddresselinje4(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getAdresselinje4()));
        }
        adresse.setLand(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getLand()));
        adresse.setMottakersNavn(VedtakXmlUtil.lagStringOpplysning(personopplysning.getNavn()));
        adresse.setPostnummer(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getPostnummer()));
        return adresse;
    }

    private void setMedlemskapsperioder(Behandling behandling, PersonopplysningerForeldrepenger personopplysninger) {
        medlemskapRepository.hentMedlemskap(behandling).ifPresent(medlemskapAggregat -> {
            Medlemskap medlemskap = personopplysningObjectFactory.createMedlemskap();
            personopplysninger.setMedlemskap(medlemskap);

            medlemskapAggregat.getRegistrertMedlemskapPerioder()
                .forEach(medlemskapPeriode -> personopplysninger.getMedlemskap().getMedlemskapsperiode()
                    .add(lagMedlemskapPeriode(medlemskapPeriode)));
        });
    }

    private void setVerge(Behandling behandling, PersonopplysningerForeldrepenger personopplysninger) {
        vergeRepository.hentAggregat(behandling).ifPresent(vergeAggregat -> {
            no.nav.foreldrepenger.behandlingslager.behandling.verge.Verge vergeFraBehandling = vergeAggregat.getVerge();
            Verge verge = personopplysningObjectFactory.createVerge();

            verge.setNavn(VedtakXmlUtil.lagStringOpplysning(hentVergeNavn(vergeAggregat.getAktørId())));
            verge.setVergetype(VedtakXmlUtil.lagKodeverkOpplysning(vergeFraBehandling.getVergeType()));
            verge.setGyldighetsperiode(VedtakXmlUtil.lagPeriodeOpplysning(vergeFraBehandling.getGyldigFom(), vergeFraBehandling.getGyldigTom()));
            verge.setMandattekst(VedtakXmlUtil.lagStringOpplysning(vergeFraBehandling.getMandatTekst()));
            VedtakXmlUtil.lagDateOpplysning(vergeFraBehandling.getVedtaksdato()).ifPresent(verge::setVedtaksdato);

            personopplysninger.setVerge(verge);
        });
    }

}
