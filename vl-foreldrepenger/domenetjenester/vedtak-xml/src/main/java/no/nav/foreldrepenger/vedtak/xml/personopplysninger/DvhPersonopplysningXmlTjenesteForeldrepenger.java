package no.nav.foreldrepenger.vedtak.xml.personopplysninger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
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
import no.nav.vedtak.felles.xml.felles.v2.BooleanOpplysning;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Addresse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Adopsjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.FamilieHendelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Familierelasjon;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Inntekt;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Inntektspost;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Medlemskap;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.PersonopplysningerDvhForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.RelatertYtelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Verge;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Virksomhet;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseStorrelse;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Foedsel;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.PersonUidentifiserbar;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Personopplysninger;

@ApplicationScoped
public class DvhPersonopplysningXmlTjenesteForeldrepenger extends PersonopplysningXmlTjeneste {

    private final no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory personopplysningBaseObjectFactory = new no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory();
    private final ObjectFactory personopplysningDvhObjectFactory = new ObjectFactory();
    private FamilieHendelseRepository familieHendelseRepository;
    private VergeRepository vergeRepository;
    private MedlemskapRepository medlemskapRepository;
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;


    public DvhPersonopplysningXmlTjenesteForeldrepenger() {
        // CDI
    }

    @Inject
    public DvhPersonopplysningXmlTjenesteForeldrepenger(TpsTjeneste tpsTjeneste, BehandlingRepositoryProvider provider, PersonopplysningTjeneste personopplysningTjeneste) {
        super(tpsTjeneste, personopplysningTjeneste);
        this.familieHendelseRepository = provider.getFamilieGrunnlagRepository();
        this.vergeRepository = provider.getVergeGrunnlagRepository();
        this.medlemskapRepository = provider.getMedlemskapRepository();
        this.inntektArbeidYtelseRepository = provider.getInntektArbeidYtelseRepository();
    }

    @Override
    public Personopplysninger lagPersonopplysning(PersonopplysningerAggregat personopplysningerAggregat, Behandling behandling) {
        PersonopplysningerDvhForeldrepenger personopplysninger = personopplysningDvhObjectFactory.createPersonopplysningerDvhForeldrepenger();
        FamilieHendelse familieHendelse = personopplysningDvhObjectFactory.createFamilieHendelse();
        personopplysninger.setFamiliehendelse(familieHendelse);

        familieHendelseRepository.hentAggregatHvisEksisterer(behandling).ifPresent(familieHendelseGrunnlag -> {
            setAdopsjon(personopplysninger.getFamiliehendelse(), familieHendelseGrunnlag);
            setFødsel(personopplysninger.getFamiliehendelse(), familieHendelseGrunnlag);
            setVerge(behandling, personopplysninger);
            setMedlemskapsperioder(behandling, personopplysninger);
            setTerminbekreftelse(personopplysninger.getFamiliehendelse(), familieHendelseGrunnlag);
        });

        setAdresse(personopplysninger, personopplysningerAggregat);
        setInntekter(behandling, personopplysninger);
        setBruker(personopplysninger, personopplysningerAggregat);
        setFamilierelasjoner(personopplysninger, personopplysningerAggregat);
        setRelaterteYtelser(behandling, personopplysninger);

        return personopplysninger;
    }

    private void setRelaterteYtelser(Behandling behandling, PersonopplysningerDvhForeldrepenger personopplysninger) {
        final Collection<Ytelse> ytelser = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null)
            .flatMap(it -> it.getAktørYtelseFørStp(behandling.getAktørId())
                .map(AktørYtelse::getYtelser))
            .orElse(Collections.emptyList());
        if (!ytelser.isEmpty()) {
            PersonopplysningerDvhForeldrepenger.RelaterteYtelser relaterteYtelser = personopplysningDvhObjectFactory.createPersonopplysningerDvhForeldrepengerRelaterteYtelser();
            ytelser.stream().forEach(ytelse -> relaterteYtelser.getRelatertYtelse().add(konverterFraDomene(ytelse)));
            personopplysninger.setRelaterteYtelser(relaterteYtelser);
        }
    }

    private RelatertYtelse konverterFraDomene(Ytelse ytelse) {
        RelatertYtelse relatertYtelse = personopplysningDvhObjectFactory.createRelatertYtelse();
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

    private void setYtelsesStørrelse(no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.RelatertYtelse relatertYtelseKontrakt, Optional<YtelseGrunnlag> ytelseGrunnlagDomene) {
        if (ytelseGrunnlagDomene.isPresent()) {
            YtelseGrunnlag ytelseGrunnlag = ytelseGrunnlagDomene.get();
            List<YtelseStorrelse> ytelseStorrelser = ytelseGrunnlag.getYtelseStørrelse().stream().map(ys -> konverterFraDomene(ys)).collect(Collectors.toList());
            relatertYtelseKontrakt.getYtelsesstorrelse().addAll(ytelseStorrelser);
        }
    }

    private no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseStorrelse konverterFraDomene(YtelseStørrelse domene) {
        no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseStorrelse kontrakt = personopplysningDvhObjectFactory.createYtelseStorrelse();
        domene.getVirksomhet().ifPresent(virksomhet -> kontrakt.setVirksomhet(tilVirksomhet(virksomhet)));
        kontrakt.setBeloep(VedtakXmlUtil.lagDecimalOpplysning(domene.getBeløp().getVerdi()));
        kontrakt.setHyppighet(VedtakXmlUtil.lagKodeverkOpplysning(domene.getHyppighet()));
        return kontrakt;
    }

    private Virksomhet tilVirksomhet(no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet domene) {
        Virksomhet kontrakt = personopplysningDvhObjectFactory.createVirksomhet();
        kontrakt.setNavn(VedtakXmlUtil.lagStringOpplysning(domene.getNavn()));
        kontrakt.setOrgnr(VedtakXmlUtil.lagStringOpplysning(domene.getOrgnr()));
        return kontrakt;
    }
    private void setYtelsesgrunnlag(no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.RelatertYtelse relatertYtelseKontrakt, Optional<YtelseGrunnlag> ytelseGrunnlagDomene) {
        Optional<no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseGrunnlag> ytelseGrunnlagOptional = ytelseGrunnlagDomene.map(yg -> konverterFraDomene(yg));
        ytelseGrunnlagOptional.ifPresent(ytelseGrunnlag -> {
            relatertYtelseKontrakt.setYtelsesgrunnlag(ytelseGrunnlag);
        });
    }

    private no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseGrunnlag konverterFraDomene(YtelseGrunnlag domene) {
        no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseGrunnlag kontrakt = personopplysningDvhObjectFactory.createYtelseGrunnlag();
        domene.getArbeidskategori().ifPresent(arbeidskategori -> kontrakt.setArbeidtype(VedtakXmlUtil.lagKodeverkOpplysning(arbeidskategori)));
        domene.getDekningsgradProsent().ifPresent(dp -> kontrakt.setDekningsgradprosent(VedtakXmlUtil.lagDecimalOpplysning(dp.getVerdi())));

        domene.getGraderingProsent().ifPresent(graderingsProsent -> kontrakt.setGraderingprosent(VedtakXmlUtil.lagDecimalOpplysning(graderingsProsent.getVerdi())));
        domene.getInntektsgrunnlagProsent().ifPresent(inntektsGrunnlagProsent -> kontrakt.setInntektsgrunnlagprosent(VedtakXmlUtil.lagDecimalOpplysning(inntektsGrunnlagProsent.getVerdi())));


        return kontrakt;
    }

    private void setYtelseAnvist(no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.RelatertYtelse relatertYtelseKontrakt, Collection<YtelseAnvist> ytelseAnvistDomene) {
        List<no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseAnvist> alleYtelserAnvist = ytelseAnvistDomene.stream().map(ytelseAnvist -> konverterFraDomene(ytelseAnvist)).collect(Collectors.toList());
        relatertYtelseKontrakt.getYtelseanvist().addAll(alleYtelserAnvist);
    }

    private no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseAnvist konverterFraDomene(YtelseAnvist domene) {
        no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.YtelseAnvist kontrakt = personopplysningDvhObjectFactory.createYtelseAnvist();
        domene.getBeløp().ifPresent(beløp -> kontrakt.setBeloep(VedtakXmlUtil.lagDecimalOpplysning(beløp.getVerdi())));
        domene.getDagsats().ifPresent(dagsats -> kontrakt.setDagsats(VedtakXmlUtil.lagDecimalOpplysning(dagsats)));
        kontrakt.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(domene.getAnvistFOM(), domene.getAnvistTOM()));
        domene.getUtbetalingsgradProsent().ifPresent(prosent -> kontrakt.setUtbetalingsgradprosent(VedtakXmlUtil.lagDecimalOpplysning(prosent)));

        return kontrakt;
    }

    private void setFamilierelasjoner(PersonopplysningerDvhForeldrepenger personopplysninger, PersonopplysningerAggregat aggregat) {
        final Map<AktørId, Personopplysning> aktørPersonopplysningMap = aggregat.getAktørPersonopplysningMap();
        final List<PersonRelasjon> tilPersoner = aggregat.getSøkersRelasjoner();
        if (tilPersoner != null && !tilPersoner.isEmpty()) {
            PersonopplysningerDvhForeldrepenger.Familierelasjoner familierelasjoner = personopplysningDvhObjectFactory.createPersonopplysningerDvhForeldrepengerFamilierelasjoner();
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

    private void setBruker(PersonopplysningerDvhForeldrepenger personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        PersonUidentifiserbar person = lagUidentifiserbarBruker(personopplysningerAggregat, personopplysningerAggregat.getSøker());
        personopplysninger.setBruker(person);
    }

    private void setInntekter(Behandling behandling, PersonopplysningerDvhForeldrepenger personopplysninger) {
        inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null).ifPresent(aggregat -> {
            Collection<AktørInntekt> aktørInntekt = aggregat.getAktørInntektForFørStp();
            if (aktørInntekt != null) {
                PersonopplysningerDvhForeldrepenger.Inntekter inntekterPersonopplysning = personopplysningDvhObjectFactory.createPersonopplysningerDvhForeldrepengerInntekter();
                aktørInntekt.forEach(inntekt -> inntekterPersonopplysning.getInntekt().addAll(lagInntekt(inntekt)));
                personopplysninger.setInntekter(inntekterPersonopplysning);
            }
        });
    }

    private List<? extends Inntekt> lagInntekt(AktørInntekt aktørInntekt) {
        List<Inntekt> inntektList = new ArrayList<>();
        List<Inntektspost> inntektspostList = new ArrayList<>();

        aktørInntekt.getInntektPensjonsgivende().forEach(inntekt -> {
            Inntekt inntektXml = personopplysningDvhObjectFactory.createInntekt();
            inntekt.getInntektspost().forEach(inntektspost -> {
                Inntektspost inntektspostXml = personopplysningDvhObjectFactory.createInntektspost();
                if (inntekt.getArbeidsgiver() != null) {
                    inntektXml.setArbeidsgiver(VedtakXmlUtil.lagStringOpplysning(inntekt.getArbeidsgiver().getIdentifikator()));
                }
                inntektspostXml.setBeloep(VedtakXmlUtil.lagDoubleOpplysning(inntektspost.getBeløp().getVerdi().doubleValue()));
                inntektspostXml.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(inntektspost.getFraOgMed(), inntektspost.getTilOgMed()));
                inntektspostXml.setYtelsetype(VedtakXmlUtil.lagStringOpplysning(inntektspost.getInntektspostType().getKode()));

                inntektXml.getInntektsposter().add(inntektspostXml);
                inntektXml.setMottaker(VedtakXmlUtil.lagStringOpplysning(aktørInntekt.getAktørId().getId()));
                inntektspostList.add(inntektspostXml);
            });
            inntektList.add(inntektXml);
        });
        return inntektList;
    }

    private void setTerminbekreftelse(FamilieHendelse familieHendelse, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        if (familieHendelseGrunnlag.getGjeldendeVersjon().getType().equals(FamilieHendelseType.TERMIN)) {
            Optional<Terminbekreftelse> terminbekreftelseOptional = familieHendelseGrunnlag.getGjeldendeTerminbekreftelse();
            terminbekreftelseOptional.ifPresent(terminbekreftelseFraBehandling -> {
                no.nav.vedtak.felles.xml.vedtak.personopplysninger.dvh.fp.v2.Terminbekreftelse terminbekreftelse = personopplysningDvhObjectFactory.createTerminbekreftelse();
                terminbekreftelse.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(familieHendelseGrunnlag.getGjeldendeAntallBarn()));

                VedtakXmlUtil.lagDateOpplysning(terminbekreftelseFraBehandling.getUtstedtdato())
                    .ifPresent(terminbekreftelse::setUtstedtDato);

                VedtakXmlUtil.lagDateOpplysning(terminbekreftelseFraBehandling.getTermindato()).ifPresent(terminbekreftelse::setTermindato);

                familieHendelse.setTerminbekreftelse(terminbekreftelse);
            });
        }
    }

    private void setMedlemskapsperioder(Behandling behandling, PersonopplysningerDvhForeldrepenger personopplysninger) {
        medlemskapRepository.hentMedlemskap(behandling).ifPresent(medlemskapperioderFraBehandling -> {
            Medlemskap medlemskap = personopplysningDvhObjectFactory.createMedlemskap();
            personopplysninger.setMedlemskap(medlemskap);

            medlemskapperioderFraBehandling.getRegistrertMedlemskapPerioder()
                .forEach(medlemskapPeriode -> personopplysninger.getMedlemskap().getMedlemskapsperiode()
                    .add(lagMedlemskapPeriode(medlemskapPeriode)));
        });
    }

    private void setVerge(Behandling behandling, PersonopplysningerDvhForeldrepenger personopplysninger) {
        vergeRepository.hentAggregat(behandling).ifPresent(vergeAggregat -> {
            no.nav.foreldrepenger.behandlingslager.behandling.verge.Verge vergeFraBehandling = vergeAggregat.getVerge();
            Verge verge = personopplysningDvhObjectFactory.createVerge();
            verge.setVergetype(VedtakXmlUtil.lagKodeverkOpplysning(vergeFraBehandling.getVergeType()));
            verge.setGyldighetsperiode(VedtakXmlUtil.lagPeriodeOpplysning(vergeFraBehandling.getGyldigFom(), vergeFraBehandling.getGyldigTom()));
            verge.setMandattekst(VedtakXmlUtil.lagStringOpplysning(vergeFraBehandling.getMandatTekst()));
            VedtakXmlUtil.lagDateOpplysning(vergeFraBehandling.getVedtaksdato())
                .ifPresent(verge::setVedtaksdato);

            personopplysninger.setVerge(verge);
        });
    }

    private void setFødsel(FamilieHendelse familieHendelse, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse gjeldendeFamilieHendelse = familieHendelseGrunnlag.getGjeldendeVersjon();
        if (Arrays.asList(FamilieHendelseType.FØDSEL, FamilieHendelseType.TERMIN).contains(gjeldendeFamilieHendelse.getType())) {
            Foedsel fødsel = personopplysningBaseObjectFactory.createFoedsel();
            fødsel.setAntallBarn(VedtakXmlUtil.lagIntOpplysning(gjeldendeFamilieHendelse.getAntallBarn()));
            gjeldendeFamilieHendelse.getFødselsdato().ifPresent(fødselsdato -> {
                VedtakXmlUtil.lagDateOpplysning(fødselsdato).ifPresent(fødsel::setFoedselsdato);
            });
            familieHendelse.setFoedsel(fødsel);
        }
    }

    private void setAdopsjon(FamilieHendelse familieHendelse, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        familieHendelseGrunnlag.getGjeldendeAdopsjon().ifPresent(adopsjonHendelse -> {
            Adopsjon adopsjon = personopplysningDvhObjectFactory.createAdopsjon();
            if (adopsjonHendelse.getErEktefellesBarn() != null) {
                BooleanOpplysning erEktefellesBarn = VedtakXmlUtil.lagBooleanOpplysning(adopsjonHendelse.getErEktefellesBarn());
                adopsjon.setErEktefellesBarn(erEktefellesBarn);
            }

            familieHendelseGrunnlag.getGjeldendeBarna().forEach(aBarn -> adopsjon.getAdopsjonsbarn().add(leggTilAdopsjonsbarn(aBarn)));
            familieHendelse.setAdopsjon(adopsjon);
        });
    }

    private Adopsjon.Adopsjonsbarn leggTilAdopsjonsbarn(UidentifisertBarn aBarn) {
        Adopsjon.Adopsjonsbarn adopsjonsbarn = personopplysningDvhObjectFactory.createAdopsjonAdopsjonsbarn();
        VedtakXmlUtil.lagDateOpplysning(aBarn.getFødselsdato()).ifPresent(adopsjonsbarn::setFoedselsdato);
        return adopsjonsbarn;
    }

    private void setAdresse(PersonopplysningerDvhForeldrepenger personopplysninger, PersonopplysningerAggregat personopplysningerAggregat) {
        final Personopplysning personopplysning = personopplysningerAggregat.getSøker();
        personopplysningerAggregat.getAdresserFor(personopplysning.getAktørId()).stream()
            .forEach(adresse -> personopplysninger.getAdresse().add(lagAdresse(adresse)));
    }

    private Addresse lagAdresse(PersonAdresse adresseFraBehandling) {
        Addresse adresse = personopplysningDvhObjectFactory.createAddresse();
        adresse.setAdressetype(VedtakXmlUtil.lagKodeverkOpplysning(adresseFraBehandling.getAdresseType()));
        adresse.setLand(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getLand()));
        adresse.setPostnummer(VedtakXmlUtil.lagStringOpplysning(adresseFraBehandling.getPostnummer()));
        return adresse;
    }
}
