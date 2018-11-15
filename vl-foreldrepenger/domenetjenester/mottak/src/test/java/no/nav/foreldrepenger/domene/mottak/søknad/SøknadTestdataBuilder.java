package no.nav.foreldrepenger.domene.mottak.søknad;

import static java.util.Objects.isNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdUtlandet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Periode;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.UkjentForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Dekningsgrad;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Brukerroller;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Dekningsgrader;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Land;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.v1.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.util.FPDateUtil;

public class SøknadTestdataBuilder {

    private static final LocalDate MOTTATTDATO = LocalDate.now(FPDateUtil.getOffset());

    private static final AktørId STD_KVINNE_AKTØR_ID = new AktørId("9000000000036");
    private static final AktørId STD_MANN_AKTØR_ID = new AktørId("9000000000035");

    private Soeknad søknad = new Soeknad();

    private SoekersRelasjonTilBarnet relasjonTilBarnet; // Fødsel eller adopsjon
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

    public SøknadTestdataBuilder engangsstønad(EngangsstønadBuilder builder) {
        ytelsestype = builder.build();
        return this;
    }

    public SøknadTestdataBuilder foreldrepenger(ForeldrepengerBuilder builder) {
        ytelsestype = builder.build();
        return this;
    }

    public SøknadTestdataBuilder medSøker(ForeldreType type, AktørId aktørId) {
        Brukerroller brukerroller = new Brukerroller();
        brukerroller.setKode(type.getKode());
        Bruker bruker = new Bruker();
        bruker.setAktoerId(String.valueOf(aktørId));
        bruker.setSoeknadsrolle(brukerroller);
        søknad.setSoeker(bruker);
        return this;
    }

    public Soeknad build() {
        if (ytelsestype instanceof Engangsstønad) {
            ((Engangsstønad) ytelsestype).setSoekersRelasjonTilBarnet(relasjonTilBarnet);
        } else if (ytelsestype instanceof Foreldrepenger) {
            Foreldrepenger ytelseForeldrepenger = (Foreldrepenger) this.ytelsestype;
            ytelseForeldrepenger.setRelasjonTilBarnet(relasjonTilBarnet);
            // Overskriv defaultverdier dersom deres respektive testbuildere er blitt anvendt
            Optional.ofNullable(dekningsgrad).ifPresent(ytelseForeldrepenger::setDekningsgrad);
            Optional.ofNullable(rettigheter).ifPresent(ytelseForeldrepenger::setRettigheter);
            Optional.ofNullable(fordeling).ifPresent(ytelseForeldrepenger::setFordeling);
            Optional.ofNullable(medlemskap).ifPresent(ytelseForeldrepenger::setMedlemskap);
            Optional.ofNullable(annenForelder).ifPresent(ytelseForeldrepenger::setAnnenForelder);
        } else if (ytelsestype instanceof Endringssoeknad) {
            Endringssoeknad ytelseEndringssoeknad = (Endringssoeknad) this.ytelsestype;
            Optional.ofNullable(fordeling).ifPresent(ytelseEndringssoeknad::setFordeling);

        }
        søknad.setOmYtelse(mapOmYtelse(ytelsestype));
        return søknad;
    }

    private OmYtelse mapOmYtelse(Ytelse ytelse) {
        OmYtelse omYtelse = new OmYtelse();
        omYtelse.getAny().add(ytelse);
        return omYtelse;
    }

    private static XMLGregorianCalendar konverterDato(LocalDate dato) {
        if (isNull(dato)) {
            return null;
        }
        return DateUtil.convertToXMLGregorianCalendar(dato);
    }

    static class ForeldrepengerBuilder {
        private no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap;
        private Rettigheter rettighet;
        private Dekningsgrad dekningsgrad;

        // TODO: Opprett medlemskap mapper
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

    static class EngangsstønadBuilder {
        private no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap;
        private AnnenForelder annenForelder;

        public EngangsstønadBuilder medUkjentAnnenForelder() {
            annenForelder = new UkjentForelder();
            return this;
        }

        public EngangsstønadBuilder medAnnenForelderMedNorskIdent(AktørId aktørId) {
            AnnenForelderMedNorskIdent annenForelderMedNorskIdent = new AnnenForelderMedNorskIdent();
            annenForelderMedNorskIdent.setAktoerId(String.valueOf(aktørId));
            annenForelder = annenForelderMedNorskIdent;
            return this;
        }

        public EngangsstønadBuilder medMedlemskapOppholdNorge() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(true)
                .medTidligereOppholdNorge(true).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public EngangsstønadBuilder medMedlemskapFremtidigOppholdUtlandOgTidligereOppholdNorge() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(false)
                .medTidligereOppholdNorge(true).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public EngangsstønadBuilder medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(true)
                .medTidligereOppholdNorge(false).medOppholdNorgeNå(true);
            medlemskap = engangstønadMedlemskapBuilder.build();
            return this;
        }

        public EngangsstønadBuilder medMedlemskapOppholdUtland() {
            EngangstønadMedlemskapBuilder engangstønadMedlemskapBuilder = new EngangstønadMedlemskapBuilder().medFremtidigOppholdNorge(false)
                .medTidligereOppholdNorge(false).medOppholdNorgeNå(true);
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

}
