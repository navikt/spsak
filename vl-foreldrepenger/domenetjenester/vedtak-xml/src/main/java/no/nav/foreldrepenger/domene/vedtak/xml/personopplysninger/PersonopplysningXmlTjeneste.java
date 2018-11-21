package no.nav.foreldrepenger.domene.vedtak.xml.personopplysninger;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.felles.v2.DateOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.KodeverksOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.StringOpplysning;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.PersonopplysningerForeldrepenger;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Medlemskapsperiode;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.PersonIdentifiserbar;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.PersonUidentifiserbar;
import no.nav.vedtak.felles.xml.vedtak.personopplysninger.v2.Personopplysninger;
import no.nav.vedtak.felles.xml.vedtak.v2.Vedtak;

public abstract class PersonopplysningXmlTjeneste {

    private final ObjectFactory personopplysningObjectFactory = new ObjectFactory();
    private TpsTjeneste tpsTjeneste;
    private PersonopplysningTjeneste personopplysningTjeneste;

    public PersonopplysningXmlTjeneste() {
        // For CDI
    }

    public PersonopplysningXmlTjeneste(TpsTjeneste tpsTjeneste, PersonopplysningTjeneste personopplysningTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    public abstract Personopplysninger lagPersonopplysning(PersonopplysningerAggregat personopplysningerAggregat, Behandling behandling);

    public void setPersonopplysninger(Vedtak vedtak, Behandling behandling) {
        Personopplysninger personopplysninger = null;
        Optional<PersonopplysningerAggregat> personopplysningerAggregat = personopplysningTjeneste.hentPersonopplysningerHvisEksisterer(behandling);
        if (personopplysningerAggregat.isPresent()) {
            personopplysninger = lagPersonopplysning(personopplysningerAggregat.get(), behandling);//Implementeres i hver subklasse
        }
        no.nav.vedtak.felles.xml.vedtak.v2.Personopplysninger personopplysninger1 = new no.nav.vedtak.felles.xml.vedtak.v2.Personopplysninger();
        personopplysninger1.getAny().add(new no.nav.vedtak.felles.xml.vedtak.personopplysninger.fp.v2.ObjectFactory().createPersonopplysningerForeldrepenger((PersonopplysningerForeldrepenger) personopplysninger));
        vedtak.setPersonOpplysninger(personopplysninger1);
    }

    protected Medlemskapsperiode lagMedlemskapPeriode(RegistrertMedlemskapPerioder medlemskapPeriodeIn) {
        Medlemskapsperiode medlemskapsPeriode = personopplysningObjectFactory.createMedlemskapsperiode();

        Optional<DateOpplysning> beslutningDato = VedtakXmlUtil.lagDateOpplysning(medlemskapPeriodeIn.getBeslutningsdato());
        beslutningDato.ifPresent(medlemskapsPeriode::setBeslutningsdato);

        medlemskapsPeriode.setDekningtype(VedtakXmlUtil.lagKodeverkOpplysning(medlemskapPeriodeIn.getDekningType()));
        medlemskapsPeriode.setErMedlem(VedtakXmlUtil.lagBooleanOpplysning(medlemskapPeriodeIn.getErMedlem()));
        medlemskapsPeriode.setLovvalgsland(VedtakXmlUtil.lagKodeverkOpplysning(medlemskapPeriodeIn.getLovvalgLand()));
        medlemskapsPeriode.setMedlemskaptype(VedtakXmlUtil.lagKodeverkOpplysning(medlemskapPeriodeIn.getMedlemskapType()));
        medlemskapsPeriode.setPeriode(VedtakXmlUtil.lagPeriodeOpplysning(medlemskapPeriodeIn.getFom(), medlemskapPeriodeIn.getTom()));
        return medlemskapsPeriode;
    }

    protected String hentVergeNavn(AktørId aktørId) {
        return tpsTjeneste.hentBrukerForAktør(aktørId).map(Personinfo::getNavn).orElse("Ukjent navn"); //$NON-NLS-1$
    }

    protected PersonIdentifiserbar lagBruker(PersonopplysningerAggregat aggregat, Personopplysning personopplysning) {
        PersonIdentifiserbar person = personopplysningObjectFactory.createPersonIdentifiserbar();

        Optional<DateOpplysning> dødsdato = VedtakXmlUtil.lagDateOpplysning(personopplysning.getDødsdato());
        dødsdato.ifPresent(person::setDoedsdato);

        Optional<DateOpplysning> fødseldato = VedtakXmlUtil.lagDateOpplysning(personopplysning.getFødselsdato());
        fødseldato.ifPresent(person::setFoedselsdato);

        NavBrukerKjønn kjønn = personopplysning.getKjønn();
        person.setKjoenn(VedtakXmlUtil.lagStringOpplysning(kjønn.getNavn()));

        PersonstatusType personstatus = Optional.ofNullable(aggregat.getPersonstatusFor(personopplysning.getAktørId()))
            .map(Personstatus::getPersonstatus).orElse(PersonstatusType.UDEFINERT);
        person.setPersonstatus(VedtakXmlUtil.lagKodeverkOpplysning(personstatus));

        Landkoder statsborgerskap = aggregat.getStatsborgerskapFor(personopplysning.getAktørId()).stream().findFirst()
            .map(Statsborgerskap::getStatsborgerskap).orElse(Landkoder.UDEFINERT);
        person.setStatsborgerskap(VedtakXmlUtil.lagKodeverkOpplysning(statsborgerskap));

        StringOpplysning navn = VedtakXmlUtil.lagStringOpplysning(personopplysning.getNavn());
        person.setNavn(navn);

        if (personopplysning.getAktørId() != null) {
            Optional<PersonIdent> norskIdent = tpsTjeneste.hentFnr(personopplysning.getAktørId());
            person.setNorskIdent(VedtakXmlUtil.lagStringOpplysning(norskIdent.map(PersonIdent::getIdent).orElse(null)));
        }

        if (personopplysning.getRegion() != null) {
            person.setRegion(VedtakXmlUtil.lagStringOpplysning(personopplysning.getRegion().getNavn()));
        }

        KodeverksOpplysning sivilstand = VedtakXmlUtil.lagKodeverkOpplysning(personopplysning.getSivilstand());
        person.setSivilstand(sivilstand);

        return person;
    }

    protected PersonUidentifiserbar lagUidentifiserbarBruker(PersonopplysningerAggregat aggregat, Personopplysning personopplysning) {
        PersonUidentifiserbar person = personopplysningObjectFactory.createPersonUidentifiserbar();

        Optional<DateOpplysning> dødsdato = VedtakXmlUtil.lagDateOpplysning(personopplysning.getDødsdato());
        dødsdato.ifPresent(person::setDoedsdato);

        Optional<DateOpplysning> fødseldato = VedtakXmlUtil.lagDateOpplysning(personopplysning.getFødselsdato());
        fødseldato.ifPresent(person::setFoedselsdato);

        NavBrukerKjønn kjønn = personopplysning.getKjønn();
        person.setKjoenn(VedtakXmlUtil.lagStringOpplysning(kjønn.getNavn()));

        PersonstatusType personstatus = Optional.ofNullable(aggregat.getPersonstatusFor(personopplysning.getAktørId()))
            .map(Personstatus::getPersonstatus).orElse(PersonstatusType.UDEFINERT);
        person.setPersonstatus(VedtakXmlUtil.lagKodeverkOpplysning(personstatus));

        Landkoder statsborgerskap = aggregat.getStatsborgerskapFor(personopplysning.getAktørId()).stream().findFirst()
            .map(Statsborgerskap::getStatsborgerskap).orElse(Landkoder.UDEFINERT);
        person.setStatsborgerskap(VedtakXmlUtil.lagKodeverkOpplysning(statsborgerskap));

        if (personopplysning.getRegion() != null) {
            person.setRegion(VedtakXmlUtil.lagStringOpplysning(personopplysning.getRegion().getNavn()));
        }

        if (personopplysning.getAktørId() != null) {
            person.setAktoerId(VedtakXmlUtil.lagStringOpplysning(personopplysning.getAktørId().getId()));
        }

        KodeverksOpplysning sivilstand = VedtakXmlUtil.lagKodeverkOpplysning(personopplysning.getSivilstand());
        person.setSivilstand(sivilstand);

        return person;
    }
}
