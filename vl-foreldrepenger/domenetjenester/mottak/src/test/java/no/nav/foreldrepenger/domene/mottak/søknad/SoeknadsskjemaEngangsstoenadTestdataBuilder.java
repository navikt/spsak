package no.nav.foreldrepenger.domene.mottak.søknad;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Bruker;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.GrunnlagForAnsvarsovertakelse;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Innsendingsvalg;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.KanIkkeOppgiFar;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.KanIkkeOppgiMor;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Landkoder;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmBarn;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmFar;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmMor;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Periode;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Rettigheter;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Soknadsvalg;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Stoenadstype;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.TilknytningNorge;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Utenlandsopphold;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Vedlegg;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.VedleggListe;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.util.FPDateUtil;

public class SoeknadsskjemaEngangsstoenadTestdataBuilder {

    private static final String PERSONIDENTIFIKATOR_MOR = "07078516261";
    private static final String PERSONIDENTIFIKATOR_FAR = "03038005595";
    private static final String FORNAVN_MOR = "Synt18";
    private static final String ETTERNAVN_MOR = "Hansen";
    private static final String FORNAVN_FAR = "Synt17";
    private static final String ETTERNAVN_FAR = "Olsen";
    private static final LocalDate FØDSELSDATO = LocalDate.now(FPDateUtil.getOffset()).minusDays(30);
    private static final LocalDate OMSORGSOVERTAKELSESDATO = LocalDate.now(FPDateUtil.getOffset()).plusDays(2);
    private static final LocalDate TERMINDATO = LocalDate.now(FPDateUtil.getOffset()).plusDays(30);
    private static final LocalDate TERMINBEKREFTELSESDATO = LocalDate.now(FPDateUtil.getOffset());
    private static final String NAVN_PÅ_TERMINBEKREFTELSE = "Lege Legesen";
    private static final GrunnlagForAnsvarsovertakelse GRUNNLAG_FOR_ANSVARSOVERTAKELSE = GrunnlagForAnsvarsovertakelse.ADOPTERER_ALENE;
    private static final String DOKUMENTTYPEKODE_VEDLEGG = "I000041";

    private SoeknadsskjemaEngangsstoenad soeknadsskjemaEngangsstoenad = new SoeknadsskjemaEngangsstoenad();

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medPersonidentifikator(String personidentifikator) {
        Bruker bruker = new Bruker();
        bruker.setPersonidentifikator(personidentifikator);
        soeknadsskjemaEngangsstoenad.setBruker(bruker);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder fødsel() {
        Soknadsvalg soknadsvalg = getSoknadsvalg();
        soknadsvalg.setFoedselEllerAdopsjon(FoedselEllerAdopsjon.FOEDSEL);
        soeknadsskjemaEngangsstoenad.setSoknadsvalg(soknadsvalg);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder adopsjon() {
        Soknadsvalg soknadsvalg = getSoknadsvalg();
        soknadsvalg.setFoedselEllerAdopsjon(FoedselEllerAdopsjon.ADOPSJON);
        soeknadsskjemaEngangsstoenad.setSoknadsvalg(soknadsvalg);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder engangsstønadMor() {
        Soknadsvalg soknadsvalg = getSoknadsvalg();
        soknadsvalg.setStoenadstype(Stoenadstype.ENGANGSSTOENADMOR);
        soeknadsskjemaEngangsstoenad.setSoknadsvalg(soknadsvalg);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder engangsstønadFar() {
        Soknadsvalg soknadsvalg = getSoknadsvalg();
        soknadsvalg.setStoenadstype(Stoenadstype.ENGANGSSTOENADFAR);
        soeknadsskjemaEngangsstoenad.setSoknadsvalg(soknadsvalg);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medTermindato(LocalDate termindato) {
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        try {
            opplysningerOmBarn.setTermindato(DateUtil.convertToXMLGregorianCalendar(termindato));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medTerminbekreftelsesdato(LocalDate terminbekreftelsesdato) {
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        try {
            opplysningerOmBarn.setTerminbekreftelsedato(DateUtil.convertToXMLGregorianCalendar(terminbekreftelsesdato));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medNavnPåTerminbekreftelse(String navnPåTerminbekreftelse) {
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        opplysningerOmBarn.setNavnPaaTerminbekreftelse(navnPåTerminbekreftelse);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medOmsorgsovertakelsesdato(LocalDate omsorgsovertakelsesdato) {
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        try {
            opplysningerOmBarn.setOmsorgsovertakelsedato(DateUtil.convertToXMLGregorianCalendar(omsorgsovertakelsesdato));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medFødselsdatoer(List<LocalDate> fødselsdatoer) {
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        opplysningerOmBarn.setAntallBarn(fødselsdatoer.size());
        opplysningerOmBarn.getFoedselsdato().clear();
        for (LocalDate fødselsdato : fødselsdatoer) {
            try {
                opplysningerOmBarn.getFoedselsdato().add(DateUtil.convertToXMLGregorianCalendar(fødselsdato));
            } catch (DatatypeConfigurationException e) {
                throw new IllegalArgumentException(e);
            }
        }
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medAntallBarn(int antallBarn) {
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        opplysningerOmBarn.setAntallBarn(antallBarn);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medFarsNavn(String fornavn, String etternavn) {
        OpplysningerOmFar opplysningerOmFar = getOpplysningerOmFar();
        opplysningerOmFar.setFornavn(fornavn);
        opplysningerOmFar.setEtternavn(etternavn);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmFar(opplysningerOmFar);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medMorsNavn(String fornavn, String etternavn) {
        OpplysningerOmMor opplysningerOmMor = getOpplysningerOmMor();
        opplysningerOmMor.setFornavn(fornavn);
        opplysningerOmMor.setEtternavn(etternavn);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmMor(opplysningerOmMor);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medMorsPersonidentifikator(String personidentifikator) {
        OpplysningerOmMor opplysningerOmMor = getOpplysningerOmMor();
        opplysningerOmMor.setPersonidentifikator(personidentifikator);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmMor(opplysningerOmMor);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medFarsPersonidentifikator(String personidentifikator) {
        OpplysningerOmFar opplysningerOmFar = getOpplysningerOmFar();
        opplysningerOmFar.setPersonidentifikator(personidentifikator);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmFar(opplysningerOmFar);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medOppholdNorgeNå(boolean oppholdNorgeNå) {
        TilknytningNorge tilknytningNorge = getTilknytningNorge();
        tilknytningNorge.setOppholdNorgeNaa(oppholdNorgeNå);
        soeknadsskjemaEngangsstoenad.setTilknytningNorge(tilknytningNorge);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medTidligereOppholdNorge(boolean tidligereOppholdNorge) {
        TilknytningNorge tilknytningNorge = getTilknytningNorge();
        tilknytningNorge.setTidligereOppholdNorge(tidligereOppholdNorge);
        soeknadsskjemaEngangsstoenad.setTilknytningNorge(tilknytningNorge);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medFremtidigOppholdNorge(boolean fremtidigOppholdNorge) {
        TilknytningNorge tilknytningNorge = getTilknytningNorge();
        tilknytningNorge.setFremtidigOppholdNorge(fremtidigOppholdNorge);
        soeknadsskjemaEngangsstoenad.setTilknytningNorge(tilknytningNorge);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medUtenlandsopphold(boolean tidligereOpphold) throws DatatypeConfigurationException {
        TilknytningNorge tilknytningNorge = getTilknytningNorge();
        Utenlandsopphold utenlandsopphold = new Utenlandsopphold();
        Landkoder aLandkode = new Landkoder();
        aLandkode.setKode("GGY");
        utenlandsopphold.setLand(aLandkode);
        Periode aPeriode = new Periode();
        aPeriode.setFom(DateUtil.convertToXMLGregorianCalendar(LocalDate.now(FPDateUtil.getOffset()).minusYears(1)));
        aPeriode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.now(FPDateUtil.getOffset()).plusYears(1)));
        utenlandsopphold.setPeriode(aPeriode);
        if (tidligereOpphold){
            tilknytningNorge.getTidligereOppholdUtenlands().getUtenlandsopphold().add(utenlandsopphold);
        } else {
            tilknytningNorge.getFremtidigOppholdUtenlands().getUtenlandsopphold().add(utenlandsopphold);
        }
        soeknadsskjemaEngangsstoenad.setTilknytningNorge(tilknytningNorge);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medKanIkkeOppgiFar() {
        OpplysningerOmFar opplysningerOmFar = getOpplysningerOmFar();
        KanIkkeOppgiFar kanIkkeOppgiFar = new KanIkkeOppgiFar();
        kanIkkeOppgiFar.setAarsak("Ingen årsak");
        kanIkkeOppgiFar.setUtenlandskfnrEllerForklaring("12345");
        Landkoder aLandkode = new Landkoder();
        aLandkode.setKode("BIH");
        kanIkkeOppgiFar.setUtenlandskfnrLand(aLandkode);
        opplysningerOmFar.setKanIkkeOppgiFar(kanIkkeOppgiFar);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmFar(opplysningerOmFar);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medKanIkkeOppgiMor() {
        OpplysningerOmMor opplysningerOmMor = getOpplysningerOmMor();
        KanIkkeOppgiMor kanIkkeOppgiMor = new KanIkkeOppgiMor();
        kanIkkeOppgiMor.setAarsak("Ingen årsak");
        kanIkkeOppgiMor.setBegrunnelse("Noen begrunnelse");
        kanIkkeOppgiMor.setUtenlandskfnrEllerForklaring("12345");
        Landkoder aLandkode = new Landkoder();
        aLandkode.setKode("DNK");
        kanIkkeOppgiMor.setUtenlandskfnrLand(aLandkode);
        opplysningerOmMor.setKanIkkeOppgiMor(kanIkkeOppgiMor);
        soeknadsskjemaEngangsstoenad.setOpplysningerOmMor(opplysningerOmMor);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medGrunnlagForAnsvarsovertakelse(GrunnlagForAnsvarsovertakelse grunnlagForAnsvarsovertakelse) {
        Rettigheter rettigheter = getRettigheter();
        rettigheter.setGrunnlagForAnsvarsovertakelse(grunnlagForAnsvarsovertakelse);
        soeknadsskjemaEngangsstoenad.setRettigheter(rettigheter);
        return this;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medVedleggsliste(List<Vedlegg> vedleggListe) {
        VedleggListe vedleggListe1 = soeknadsskjemaEngangsstoenad.getVedleggListe();
        if(vedleggListe1 == null) {
            soeknadsskjemaEngangsstoenad.setVedleggListe(new VedleggListe());
        }
        soeknadsskjemaEngangsstoenad.getVedleggListe().getVedlegg().clear();
        soeknadsskjemaEngangsstoenad.getVedleggListe().getVedlegg().addAll(vedleggListe);
        return this;
    }

    public Vedlegg opplastetVedlegg() {
        Vedlegg vedlegg = new Vedlegg();
        vedlegg.setSkjemanummer(DOKUMENTTYPEKODE_VEDLEGG);
        vedlegg.setInnsendingsvalg(Innsendingsvalg.LASTET_OPP);
        vedlegg.setErPaakrevdISoeknadsdialog(true);
        return vedlegg;
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder søknadAdopsjonEngangsstønadMor() {
        return this
                .medPersonidentifikator(PERSONIDENTIFIKATOR_MOR)
                .adopsjon()
                .engangsstønadMor()
                .medOppholdNorgeNå(true)
                .medOmsorgsovertakelsesdato(OMSORGSOVERTAKELSESDATO)
                .medFødselsdatoer(Collections.singletonList(FØDSELSDATO))
                .medFarsNavn(FORNAVN_FAR, ETTERNAVN_FAR)
                .medFarsPersonidentifikator(PERSONIDENTIFIKATOR_FAR)
                .medVedleggsliste(Collections.singletonList(opplastetVedlegg()));
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder søknadAdopsjonEngangsstønadFar() {
        return this
                .medPersonidentifikator(PERSONIDENTIFIKATOR_FAR)
                .adopsjon()
                .engangsstønadFar()
                .medOppholdNorgeNå(true)
                .medOmsorgsovertakelsesdato(OMSORGSOVERTAKELSESDATO)
                .medFødselsdatoer(Collections.singletonList(FØDSELSDATO))
                .medMorsNavn(FORNAVN_MOR, ETTERNAVN_MOR)
                .medMorsPersonidentifikator(PERSONIDENTIFIKATOR_MOR)
                .medVedleggsliste(Collections.singletonList(opplastetVedlegg()))
                .medGrunnlagForAnsvarsovertakelse(GRUNNLAG_FOR_ANSVARSOVERTAKELSE);
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder søknadFødselTerminEngangsstønadMor() {
        return this
                .medPersonidentifikator(PERSONIDENTIFIKATOR_MOR)
                .fødsel()
                .engangsstønadMor()
                .medOppholdNorgeNå(true)
                .medTermindato(TERMINDATO)
                .medTerminbekreftelsesdato(TERMINBEKREFTELSESDATO)
                .medNavnPåTerminbekreftelse(NAVN_PÅ_TERMINBEKREFTELSE)
                .medAntallBarn(1)
                .medVedleggsliste(Collections.singletonList(opplastetVedlegg()));
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder søknadFødselEngangsstønadMor() {
        return this
                .medPersonidentifikator(PERSONIDENTIFIKATOR_MOR)
                .fødsel()
                .engangsstønadMor()
                .medOppholdNorgeNå(true)
                .medFødselsdatoer(Collections.singletonList(FØDSELSDATO))
                .medAntallBarn(1)
                .medVedleggsliste(Collections.singletonList(opplastetVedlegg()));
    }

    public SoeknadsskjemaEngangsstoenad build() {
        verifiserOgSettObligatoriskeFelt();
        return soeknadsskjemaEngangsstoenad;
    }

    private void verifiserOgSettObligatoriskeFelt() {
        if (soeknadsskjemaEngangsstoenad.getBruker() == null) {
            this.medPersonidentifikator(PERSONIDENTIFIKATOR_MOR);
        }
        if (soeknadsskjemaEngangsstoenad.getSoknadsvalg() == null) {
            throw new IllegalArgumentException("Søknadsvalg (fødsel/adopsjon og engangsstønad mor/far) må spesifiseres");
        }
        OpplysningerOmBarn opplysningerOmBarn = getOpplysningerOmBarn();
        if (opplysningerOmBarn.getAntallBarn() == 0) {
            this.medAntallBarn(1);
        }
    }

    private Soknadsvalg getSoknadsvalg() {
        return soeknadsskjemaEngangsstoenad.getSoknadsvalg() != null ? soeknadsskjemaEngangsstoenad.getSoknadsvalg() : new Soknadsvalg();
    }

    private OpplysningerOmBarn getOpplysningerOmBarn() {
        return soeknadsskjemaEngangsstoenad.getOpplysningerOmBarn() != null ? soeknadsskjemaEngangsstoenad.getOpplysningerOmBarn() : new OpplysningerOmBarn();
    }

    private OpplysningerOmFar getOpplysningerOmFar() {
        return soeknadsskjemaEngangsstoenad.getOpplysningerOmFar() != null ? soeknadsskjemaEngangsstoenad.getOpplysningerOmFar() : new OpplysningerOmFar();
    }

    private OpplysningerOmMor getOpplysningerOmMor() {
        return soeknadsskjemaEngangsstoenad.getOpplysningerOmMor() != null ? soeknadsskjemaEngangsstoenad.getOpplysningerOmMor() : new OpplysningerOmMor();
    }

    private TilknytningNorge getTilknytningNorge() {
        final TilknytningNorge tilknytningNorge = soeknadsskjemaEngangsstoenad.getTilknytningNorge() != null ? soeknadsskjemaEngangsstoenad.getTilknytningNorge() : new TilknytningNorge();
        tilknytningNorge.setTidligereOppholdUtenlands(new TilknytningNorge.TidligereOppholdUtenlands());
        tilknytningNorge.setFremtidigOppholdUtenlands(new TilknytningNorge.FremtidigOppholdUtenlands());
        return tilknytningNorge;
    }

    private Rettigheter getRettigheter() {
        return soeknadsskjemaEngangsstoenad.getRettigheter() != null ? soeknadsskjemaEngangsstoenad.getRettigheter() : new Rettigheter();
    }

    public SoeknadsskjemaEngangsstoenadTestdataBuilder medTilleggsopplysninger(String tilleggsopplysninger) {
        soeknadsskjemaEngangsstoenad.setTilleggsopplysninger(tilleggsopplysninger);
        return this;
    }
}
