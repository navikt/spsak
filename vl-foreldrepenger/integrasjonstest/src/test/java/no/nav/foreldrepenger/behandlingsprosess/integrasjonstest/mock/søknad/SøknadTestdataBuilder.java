package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad;

import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderUtenNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdUtlandet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Periode;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.UkjentForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Dekningsgrad;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Brukerroller;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Dekningsgrader;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Land;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Omsorgsovertakelseaarsaker;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Uttaksperiodetyper;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Gradering;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Uttaksperiode;
import no.nav.vedtak.felles.xml.soeknad.v1.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.util.FPDateUtil;

public class SøknadTestdataBuilder {

    private static final LocalDate MOTTATTDATO = LocalDate.now(FPDateUtil.getOffset());

    private static final AktørId STD_KVINNE_AKTØR_ID = new AktørId("9000000000036");
    private static final AktørId STD_MANN_AKTØR_ID = new AktørId("9000000000035");

    private Soeknad søknad = new Soeknad();

    private SoekersRelasjonTilBarnet relasjonTilBarnet; //Fødsel eller adopsjon
    private Ytelse ytelsestype;
    private Rettigheter rettigheter;
    private Dekningsgrad dekningsgrad;
    private Fordeling fordeling;
    private no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap;
    private AnnenForelder annenForelder;

    public SøknadTestdataBuilder søknadForeldrepenger() {
        return this
            .medMottattdato(MOTTATTDATO)
            .foreldrepenger(new ForeldrepengerBuilder().medMedlemskap().medSøkersRettighet().medDekningsgrad("100"));
    }

    public SøknadTestdataBuilder endringssøknadForeldrepenger() {
        return this
            .medMottattdato(MOTTATTDATO)
            .endringssøknad(new EndringssøknadBuilder());
    }

    public SøknadTestdataBuilder søknadEngangsstønadMor() {
        return this
            .medMottattdato(MOTTATTDATO)
            .engangsstønad(new EngangsstønadBuilder().medMedlemskapOppholdNorge())
            .medSøker(ForeldreType.MOR, STD_KVINNE_AKTØR_ID);
    }

    public SøknadTestdataBuilder søknadEngangsstønadFar() {
        return this
            .medMottattdato(MOTTATTDATO)
            .engangsstønad(new EngangsstønadBuilder().medMedlemskapOppholdNorge())
            .medSøker(ForeldreType.FAR, STD_MANN_AKTØR_ID);
    }

    public SøknadTestdataBuilder medMottattdato(LocalDate mottattDato) {
        søknad.setMottattDato(konverterDato(mottattDato));
        return this;
    }

    public SøknadTestdataBuilder medPåkrevdeVedleggListe(List<Vedlegg> vedlegg) {
        søknad.getPaakrevdeVedlegg().addAll(vedlegg);
        return this;
    }

    public SøknadTestdataBuilder medTillegsopplysninger(String tilleggsopplysninger) {
        søknad.setTilleggsopplysninger(tilleggsopplysninger);
        return this;
    }

    public SøknadTestdataBuilder medFødsel(FødselBuilder builder) {
        relasjonTilBarnet = builder.build();
        return this;
    }

    public SøknadTestdataBuilder medTermin(TerminBuilder builder) {
        relasjonTilBarnet = builder.build();
        return this;
    }

    public SøknadTestdataBuilder medAdopsjon(AdopsjonBuilder adopsjonBuilder) {
        relasjonTilBarnet = adopsjonBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder medOmsorgsovertakelse(OmsorgsovertakelseBuilder omsorgsovertakelseBuilder) {
        relasjonTilBarnet = omsorgsovertakelseBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder medRettighet(RettighetBuilder rettighetBuilder) {
        rettigheter = rettighetBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder medDekningsgrad(DekningsgradBuilder dekningsgradBuilder) {
        dekningsgrad = dekningsgradBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder medFordeling(FordelingBuilder fordelingBuilder) {
        fordeling = fordelingBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder medMedlemskap(MedlemskapBuilder medlemskapBuilder) {
        this.medlemskap = medlemskapBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder medAnnenForelder(AnnenForelderBuilder annenForelderBuilder) {
        this.annenForelder = annenForelderBuilder.build();
        return this;
    }

    public SøknadTestdataBuilder engangsstønad(EngangsstønadBuilder builder) {
        ytelsestype = builder.build();
        return this;
    }

    public SøknadTestdataBuilder foreldrepenger(ForeldrepengerBuilder builder) {
        ytelsestype = builder.build();
        return this;
    }

    public SøknadTestdataBuilder endringssøknad(EndringssøknadBuilder builder) {
        ytelsestype = builder.build();
        return this;
    }

    public SøknadTestdataBuilder medSøker(ForeldreType type, AktørId aktørId) {
        Brukerroller brukerroller = new Brukerroller();
        brukerroller.setKode(type.getKode());
        Bruker bruker = new Bruker();
        bruker.setAktoerId(aktørId.getId());
        bruker.setSoeknadsrolle(brukerroller);
        søknad.setSoeker(bruker);
        return this;
    }

    public Soeknad build() {
        if (ytelsestype instanceof Engangsstønad) {
            final Engangsstønad engangsstønad = (Engangsstønad) this.ytelsestype;
            engangsstønad.setSoekersRelasjonTilBarnet(relasjonTilBarnet);
            OmYtelse omYtelse = new OmYtelse();
            omYtelse.getAny().add(new no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.ObjectFactory().createEngangsstønad(engangsstønad));
            søknad.setOmYtelse(omYtelse);
        } else if (ytelsestype instanceof Foreldrepenger) {
            Foreldrepenger ytelseForeldrepenger = (Foreldrepenger) this.ytelsestype;
            ytelseForeldrepenger.setRelasjonTilBarnet(relasjonTilBarnet);
            // Overskriv defaultverdier dersom deres respektive testbuildere er blitt anvendt
            Optional.ofNullable(dekningsgrad).ifPresent(ytelseForeldrepenger::setDekningsgrad);
            Optional.ofNullable(rettigheter).ifPresent(ytelseForeldrepenger::setRettigheter);
            Optional.ofNullable(fordeling).ifPresent(ytelseForeldrepenger::setFordeling);
            Optional.ofNullable(medlemskap).ifPresent(ytelseForeldrepenger::setMedlemskap);
            Optional.ofNullable(annenForelder).ifPresent(ytelseForeldrepenger::setAnnenForelder);
            OmYtelse omYtelse = new OmYtelse();
            omYtelse.getAny().add(new no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.ObjectFactory().createForeldrepenger(ytelseForeldrepenger));
            søknad.setOmYtelse(omYtelse);
        } else if (ytelsestype instanceof Endringssoeknad) {
            Endringssoeknad ytelseEndringssoeknad = (Endringssoeknad) this.ytelsestype;
            Optional.ofNullable(fordeling).ifPresent(ytelseEndringssoeknad::setFordeling);
            OmYtelse omYtelse = new OmYtelse();
            omYtelse.getAny().add(new no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.ObjectFactory().createEndringssoeknad(ytelseEndringssoeknad));
            søknad.setOmYtelse(omYtelse);
        }
        return søknad;
    }

    private static XMLGregorianCalendar konverterDato(LocalDate dato) {
        if (isNull(dato)) {
            return null;
        }
        try {
            return DateUtil.convertToXMLGregorianCalendar(dato);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static class FødselBuilder {
        private LocalDate foedselsdato;
        private int antallBarn;

        public FødselBuilder medFoedselsdato(LocalDate foedselsdato) {
            this.foedselsdato = foedselsdato;
            return this;
        }

        public FødselBuilder medAntallBarn(int antallBarn) {
            this.antallBarn = antallBarn;
            return this;
        }

        public Foedsel build() {
            Foedsel foedsel = new Foedsel();
            foedsel.setFoedselsdato(konverterDato(foedselsdato));
            foedsel.setAntallBarn(antallBarn);
            return foedsel;
        }
    }

    public static class OmsorgsovertakelseBuilder {
        private LocalDate omsorgsovertakelsesdato;
        private List<LocalDate> foedselsdatoer;
        private int antallBarn;
        private Omsorgsovertakelseaarsaker omsorgsovertakelseaarsaker;

        public OmsorgsovertakelseBuilder medOmsorgsovertakelseDato(LocalDate omsorgsovertakelsesdato) {
            this.omsorgsovertakelsesdato = omsorgsovertakelsesdato;
            return this;
        }

        public OmsorgsovertakelseBuilder medOmsorgsovertakelseaarsaker(FarSøkerType farSøkerType) {
            Omsorgsovertakelseaarsaker årsak = new Omsorgsovertakelseaarsaker();
            årsak.setKode(farSøkerType.getKode());
            this.omsorgsovertakelseaarsaker = årsak;
            return this;
        }

        public OmsorgsovertakelseBuilder medFoedselsdatoer(List<LocalDate> foedselsdatoer) {
            this.foedselsdatoer = foedselsdatoer;
            return this;
        }

        public OmsorgsovertakelseBuilder medAntallBarn(int antallBarn) {
            this.antallBarn = antallBarn;
            return this;
        }

        public Omsorgsovertakelse build() {
            Omsorgsovertakelse omsorgsovertakelse = new Omsorgsovertakelse();
            omsorgsovertakelse.setAntallBarn(antallBarn);
            for (LocalDate dato : foedselsdatoer) {
                omsorgsovertakelse.getFoedselsdato().add(konverterDato(dato));
            }
            omsorgsovertakelse.setOmsorgsovertakelsesdato(konverterDato(omsorgsovertakelsesdato));
            omsorgsovertakelse.setOmsorgsovertakelseaarsak(omsorgsovertakelseaarsaker);

            return omsorgsovertakelse;
        }
    }

    public static class AdopsjonBuilder {
        private LocalDate omsorgsovertakelsesdato;
        private List<LocalDate> foedselsdatoer;
        private int antallBarn;
        private boolean ektefellesBarn;

        public AdopsjonBuilder medAdopsjonsdato(LocalDate omsorgsovertakelsesdato) {
            this.omsorgsovertakelsesdato = omsorgsovertakelsesdato;
            return this;
        }

        public AdopsjonBuilder medFoedselsdatoer(List<LocalDate> foedselsdatoer) {
            this.foedselsdatoer = foedselsdatoer;
            return this;
        }

        public AdopsjonBuilder medEktefellesBarn(boolean ektefellesBarn) {
            this.ektefellesBarn = ektefellesBarn;
            return this;
        }

        public AdopsjonBuilder medAntallBarn(int antallBarn) {
            this.antallBarn = antallBarn;
            return this;
        }

        public Adopsjon build() {
            Adopsjon adopsjon = new Adopsjon();
            for (LocalDate dato : foedselsdatoer) {
                adopsjon.getFoedselsdato().add(konverterDato(dato));
            }
            adopsjon.setOmsorgsovertakelsesdato(konverterDato(omsorgsovertakelsesdato));
            adopsjon.setAdopsjonAvEktefellesBarn(ektefellesBarn);

            adopsjon.setAntallBarn(antallBarn);
            return adopsjon;
        }
    }

    public static class TerminBuilder {
        private LocalDate termindato;
        private LocalDate utsteddato;
        private int antallBarn;

        public TerminBuilder medTermindato(LocalDate termindato) {
            this.termindato = termindato;
            return this;
        }

        public TerminBuilder medUtsteddato(LocalDate utsteddato) {
            this.utsteddato = utsteddato;
            return this;
        }

        public TerminBuilder medAntallBarn(int antallBarn) {
            this.antallBarn = antallBarn;
            return this;
        }

        public Termin build() {
            Termin termin = new Termin();
            termin.setTermindato(konverterDato(termindato));
            termin.setAntallBarn(antallBarn);
            termin.setUtstedtdato(konverterDato(utsteddato));
            return termin;
        }
    }

    static class EndringssøknadBuilder {

        public Endringssoeknad build() {
            Endringssoeknad endringssoeknad = new Endringssoeknad();
            return endringssoeknad;
        }
    }

    static class ForeldrepengerBuilder {
        private no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap;
        private Rettigheter rettighet;
        private Dekningsgrad dekningsgrad;

        //TODO: Opprett medlemskap mapper
        public ForeldrepengerBuilder medMedlemskap() {
            medlemskap = new no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap();
            OppholdNorge oppholdNorge = new OppholdNorge();
            Periode periode = new Periode();
            periode.setFom(konverterDato(LocalDate.now(FPDateUtil.getOffset())));
            periode.setTom(konverterDato(LocalDate.now(FPDateUtil.getOffset())));
            oppholdNorge.setPeriode(periode);
            medlemskap.getOppholdNorge().add(oppholdNorge);
            medlemskap.setINorgeVedFoedselstidspunkt(true);
            return this;
        }

        public ForeldrepengerBuilder medSøkersRettighet() {
            rettighet = new Rettigheter();
            rettighet.setHarAleneomsorgForBarnet(false);
            rettighet.setHarAnnenForelderRett(false);
            rettighet.setHarOmsorgForBarnetIPeriodene(false);
            return this;
        }

        public ForeldrepengerBuilder medDekningsgrad(String dekningsgradType) {
            dekningsgrad = new Dekningsgrad();
            Dekningsgrader dekningsgrader = new Dekningsgrader();
            dekningsgrader.setKode(dekningsgradType);
            dekningsgrad.setDekningsgrad(dekningsgrader);
            return this;
        }

        public Foreldrepenger build() {

            Foreldrepenger foreldrepenger = new Foreldrepenger();
            foreldrepenger.setRettigheter(rettighet);
            foreldrepenger.setMedlemskap(medlemskap);
            foreldrepenger.setDekningsgrad(dekningsgrad);
            return foreldrepenger;
        }
    }


    public static class EngangsstønadBuilder {
        private no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap;
        private AnnenForelder annenForelder;

        public EngangsstønadBuilder medUkjentAnnenForelder() {
            annenForelder = new UkjentForelder();
            return this;
        }

        public EngangsstønadBuilder medAnnenForelderMedNorskIdent(AktørId aktørId) {
            AnnenForelderMedNorskIdent annenForelderMedNorskIdent = new AnnenForelderMedNorskIdent();
            annenForelderMedNorskIdent.setAktoerId(aktørId.getId());
            annenForelder = annenForelderMedNorskIdent;
            return this;
        }

        public EngangsstønadBuilder medMedlemskapOppholdNorge() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(true).medTidligereOppholdNorge(true).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public EngangsstønadBuilder medMedlemskapFremtidigOppholdUtlandOgTidligereOppholdNorge() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(false).medTidligereOppholdNorge(true).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public EngangsstønadBuilder medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(true).medTidligereOppholdNorge(false).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public EngangsstønadBuilder medMedlemskapOppholdUtland() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(false).medTidligereOppholdNorge(false).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public Engangsstønad build() {
            Engangsstønad engangsstønad = new Engangsstønad();
            engangsstønad.setMedlemskap(medlemskap);
            engangsstønad.setAnnenForelder(annenForelder);
            return engangsstønad;
        }

        private static class EngangstønadMedlemskapBuilder {
            private boolean oppholdNorgeNå;
            private boolean tidligereOppholdNorge;
            private boolean fremtidigOppholdNorge;

            public EngangstønadMedlemskapBuilder medOppholdNorgeNå(boolean oppholdNorgeNå) {
                this.oppholdNorgeNå = oppholdNorgeNå;
                return this;
            }

            public EngangstønadMedlemskapBuilder medTidligereOppholdNorge(boolean tidligereOppholdNorge) {
                this.tidligereOppholdNorge = tidligereOppholdNorge;
                return this;
            }

            public EngangstønadMedlemskapBuilder medFremtidigOppholdNorge(boolean fremtidigOppholdNorge) {
                this.fremtidigOppholdNorge = fremtidigOppholdNorge;
                return this;
            }

            public Medlemskap build() {
                Medlemskap medlemskap = new Medlemskap();
                medlemskap.setINorgeVedFoedselstidspunkt(oppholdNorgeNå);
                if (tidligereOppholdNorge) {
                    OppholdNorge oppholdNorgeSistePeriode = new OppholdNorge();
                    Periode periode = new Periode();
                    periode.setFom(konverterDato(LocalDate.now(FPDateUtil.getOffset()).minusYears(1)));
                    periode.setTom(konverterDato(LocalDate.now(FPDateUtil.getOffset())));
                    oppholdNorgeSistePeriode.setPeriode(periode);
                    medlemskap.getOppholdNorge().add(oppholdNorgeSistePeriode);
                } else {
                    OppholdUtlandet nyttOpphold = new OppholdUtlandet();
                    nyttOpphold.setLand(getLandkode("FRA"));
                    Periode periode = new Periode();
                    periode.setFom(konverterDato(LocalDate.now(FPDateUtil.getOffset()).minusYears(1)));
                    periode.setTom(konverterDato(LocalDate.now(FPDateUtil.getOffset())));
                    nyttOpphold.setPeriode(periode);
                    medlemskap.getOppholdUtlandet().add(nyttOpphold);
                }

                if (fremtidigOppholdNorge) {
                    OppholdNorge oppholdNorgeNestePeriode = new OppholdNorge();
                    Periode periode = new Periode();
                    periode.setFom(konverterDato(LocalDate.now(FPDateUtil.getOffset())));
                    periode.setTom(konverterDato(LocalDate.now(FPDateUtil.getOffset()).plusYears(1)));
                    oppholdNorgeNestePeriode.setPeriode(periode);
                    medlemskap.getOppholdNorge().add(oppholdNorgeNestePeriode);
                } else {
                    OppholdUtlandet nyttOpphold = new OppholdUtlandet();
                    nyttOpphold.setLand(getLandkode("FRA"));
                    Periode periode = new Periode();
                    periode.setFom(konverterDato(LocalDate.now(FPDateUtil.getOffset())));
                    periode.setTom(konverterDato(LocalDate.now(FPDateUtil.getOffset()).plusYears(1)));
                    nyttOpphold.setPeriode(periode);
                    medlemskap.getOppholdUtlandet().add(nyttOpphold);
                }

                return medlemskap;
            }

            private static Land getLandkode(String land) {
                Land landkode = new Land();
                landkode.setKode(land);
                return landkode;
            }
        }
    }

    public static class RettighetBuilder {

        private boolean harAleneomsorgForBarnet;
        private boolean harOmsorgForBarnetIPeriodene;
        private boolean harAnnenForelderRett;

        public RettighetBuilder harAleneomsorgForBarnet(boolean harAleneomsorgForBarnet) {
            this.harAleneomsorgForBarnet = harAleneomsorgForBarnet;
            return this;
        }

        public RettighetBuilder harOmsorgForBarnetIPeriodene(boolean harOmsorgForBarnetIPeriodene) {
            this.harOmsorgForBarnetIPeriodene = harOmsorgForBarnetIPeriodene;
            return this;
        }

        public RettighetBuilder harAnnenForelderRett(boolean harAnnenForelderRett) {
            this.harAnnenForelderRett = harAnnenForelderRett;
            return this;
        }

        private Rettigheter build() {
            Rettigheter rettigheter = new Rettigheter();
            rettigheter.setHarAleneomsorgForBarnet(harAleneomsorgForBarnet);
            rettigheter.setHarOmsorgForBarnetIPeriodene(harOmsorgForBarnetIPeriodene);
            rettigheter.setHarAnnenForelderRett(harAnnenForelderRett);
            return rettigheter;
        }
    }

    public static class DekningsgradBuilder {

        String kode = "100";

        public DekningsgradBuilder med80() {
            this.kode = "80";
            return this;
        }

        public DekningsgradBuilder med100() {
            this.kode = "100";
            return this;
        }

        public Dekningsgrad build() {
            Dekningsgrad dekningsgrad = new Dekningsgrad();
            Dekningsgrader dekningsgrader = new Dekningsgrader();
            dekningsgrader.setKode(kode);
            dekningsgrad.setDekningsgrad(dekningsgrader);
            return dekningsgrad;
        }
    }

    public static class FordelingBuilder {
        List<LukketPeriodeMedVedlegg> lukketPerioder = new ArrayList<>();
        boolean annenForelderErInformert;

        public FordelingBuilder leggTilPeriode(LocalDate fom, LocalDate tom, UttakPeriodeType uttakPeriodeType) {

            Uttaksperiode uttaksperiode = new Uttaksperiode();
            uttaksperiode.setFom(konverterDato(fom));
            uttaksperiode.setTom(konverterDato(tom));

            Uttaksperiodetyper value = new Uttaksperiodetyper();
            value.setKode(uttakPeriodeType.getKode());
            uttaksperiode.setType(value);
            uttaksperiode.setOenskerSamtidigUttak(false);
            lukketPerioder.add(uttaksperiode);
            return this;
        }

        public FordelingBuilder setAnnenForelderErInformert(boolean annenForelderErInformert) {
            this.annenForelderErInformert = annenForelderErInformert;
            return this;
        }

        public Fordeling build() {
            Fordeling fordeling = new Fordeling();
            fordeling.getPerioder().addAll(lukketPerioder);
            fordeling.setAnnenForelderErInformert(annenForelderErInformert);
            return fordeling;
        }

        public FordelingBuilder leggtilGradertPeriode(LocalDate fom, LocalDate tom, UttakPeriodeType uttakPeriodeType, BigDecimal arbeidsprosent, String gradertOrgnr) {
            Gradering gradering = new Gradering();
            gradering.setFom(konverterDato(fom));
            gradering.setTom(konverterDato(tom));
            gradering.setArbeidtidProsent(arbeidsprosent.doubleValue());
            gradering.setVirksomhetsnummer(gradertOrgnr);
            gradering.setErArbeidstaker(true);
            gradering.setArbeidsforholdSomSkalGraderes(true);

            Uttaksperiodetyper value = new Uttaksperiodetyper();
            value.setKode(uttakPeriodeType.getKode());
            gradering.setType(value);
            lukketPerioder.add(gradering);
            return this;
        }
    }

    public static class MedlemskapBuilder {
        private Boolean iNorgeVedFoedselstidspunkt = true;
        private Boolean boddINorgeSiste12Mnd = true;
        private Boolean borINorgeNeste12Mnd = true;
        private LocalDate foedslesTidspunkt = LocalDate.now(FPDateUtil.getOffset());

        public MedlemskapBuilder mediNorgeVedFoedselstidspunkt(boolean iNorgeVedFoedselstidspunkt) {
            this.iNorgeVedFoedselstidspunkt = iNorgeVedFoedselstidspunkt;
            return this;
        }

        public MedlemskapBuilder medBoddINorgeSiste12Mnd(boolean boddINorgeSiste12Mnd) {
            this.boddINorgeSiste12Mnd = boddINorgeSiste12Mnd;
            return this;
        }

        public MedlemskapBuilder medBorINorgeNeste12Mnd(boolean borINorgeNeste12Mnd) {
            this.borINorgeNeste12Mnd = borINorgeNeste12Mnd;
            return this;
        }

        public MedlemskapBuilder medMottattSøknadsdato(LocalDate foedslesTidspunkt) {
            this.foedslesTidspunkt = foedslesTidspunkt;
            return this;
        }

        public no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap build() {
            no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap = new no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap();
            medlemskap.setINorgeVedFoedselstidspunkt(iNorgeVedFoedselstidspunkt);
            medlemskap.setBoddINorgeSiste12Mnd(boddINorgeSiste12Mnd);
            medlemskap.setBorINorgeNeste12Mnd(boddINorgeSiste12Mnd);
            if (boddINorgeSiste12Mnd) {
                OppholdNorge oppholdNorgeSistePeriode = new OppholdNorge();
                Periode periode = new Periode();
                periode.setFom(konverterDato(foedslesTidspunkt.minusYears(1)));
                periode.setTom(konverterDato(foedslesTidspunkt));
                oppholdNorgeSistePeriode.setPeriode(periode);
                medlemskap.getOppholdNorge().add(oppholdNorgeSistePeriode);
            } else {
                OppholdUtlandet nyttOpphold = new OppholdUtlandet();
                nyttOpphold.setLand(getLandkode("FRA"));
                Periode periode = new Periode();
                periode.setFom(konverterDato(foedslesTidspunkt.minusYears(1)));
                periode.setTom(konverterDato(foedslesTidspunkt));
                nyttOpphold.setPeriode(periode);
                medlemskap.getOppholdUtlandet().add(nyttOpphold);
            }

            if (borINorgeNeste12Mnd) {
                OppholdNorge oppholdNorgeNestePeriode = new OppholdNorge();
                Periode periode = new Periode();
                periode.setFom(konverterDato(foedslesTidspunkt));
                periode.setTom(konverterDato(foedslesTidspunkt.plusYears(1)));
                oppholdNorgeNestePeriode.setPeriode(periode);
                medlemskap.getOppholdNorge().add(oppholdNorgeNestePeriode);
            } else {
                OppholdUtlandet nyttOpphold = new OppholdUtlandet();
                nyttOpphold.setLand(getLandkode("FRA"));
                Periode periode = new Periode();
                periode.setFom(konverterDato(foedslesTidspunkt));
                periode.setTom(konverterDato(foedslesTidspunkt.plusYears(1)));
                nyttOpphold.setPeriode(periode);
                medlemskap.getOppholdUtlandet().add(nyttOpphold);
            }

            return medlemskap;
        }
    }

    public static class AnnenForelderBuilder {
        private boolean annenForeldreHarNorskIdent;
        private PersonIdent personIdent = new PersonIdent("123");
        private boolean erUkjentForelder = true;

        public AnnenForelderBuilder medNorskIdent() {
            this.annenForeldreHarNorskIdent = true;
            this.erUkjentForelder = false;
            return this;
        }

        public AnnenForelderBuilder medPersonIdent(PersonIdent personIdent) {
            this.personIdent = personIdent;
            return this;
        }

        public AnnenForelderBuilder utenNorskIdent() {
            this.annenForeldreHarNorskIdent = false;
            this.erUkjentForelder = false;
            return this;
        }

        public AnnenForelder build() {
            if (annenForeldreHarNorskIdent) {
                AnnenForelderMedNorskIdent annenForelder = new AnnenForelderMedNorskIdent();
                annenForelder.setAktoerId(personIdent.getIdent());
                return annenForelder;
            }
            if (erUkjentForelder) {
                return new UkjentForelder();
            }
            AnnenForelderUtenNorskIdent annenForelder = new AnnenForelderUtenNorskIdent();
            annenForelder.setUtenlandskPersonidentifikator(personIdent.getIdent());
            annenForelder.setLand(getLandkode("FRA"));
            return annenForelder;
        }
    }

    private static Land getLandkode(String land) {
        Land landkode = new Land();
        landkode.setKode(land);
        return landkode;
    }
}
