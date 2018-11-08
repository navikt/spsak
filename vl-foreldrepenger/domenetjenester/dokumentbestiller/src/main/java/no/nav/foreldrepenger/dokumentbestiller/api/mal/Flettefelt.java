package no.nav.foreldrepenger.dokumentbestiller.api.mal;

public class Flettefelt {
    //Felles
    public static final String BEHANDLINGSTYPE = "behandlingstype";
    public static final String SOKERSNAVN = "sokersNavn";
    public static final String PERSON_STATUS = "personstatus";
    public static final String MOTTATT_DATO = "mottattDato";
    public static final String RELASJONSKODE = "relasjonskode";
    public static final String GJELDER_FØDSEL = "gjelderFoedsel";
    public static final String ANTALL_BARN = "antallBarn";
    public static final String HALV_G = "halvG";
    public static final String SISTE_DAG_I_FELLES_PERIODE = "sisteDagIFellesPeriode";
    public static final String YTELSE_TYPE = "ytelseType";
    public static final String FØDSELSDATO_PASSERT = "foedseldatoPassert";
    public static final String FRITEKST = "fritekst";
    public static final String VILKÅR_TYPE = "vilkaarType";
    public static final String KJØNN = "kjoenn";
    public static final String FRIST_DATO = "fristDato";
    public static final String BELØP = "belop";
    //Perioder
    public static final String PERIODE = "periode";
    //Avslag, opphør
    public static final String AVSLAGSAARSAK = "avslagsAarsak";
    public static final String LOV_HJEMMEL_FOR_AVSLAG = "lovhjemmelForAvslag";
    public static final String STONADSDATO_FOM = "fomStonadsdato";
    public static final String STONADSDATO_TOM = "tomStonadsdato";
    public static final String OPPHORDATO = "opphorDato";
    public static final String DODSDATO = "dodsdato";
    //Konfig
    public static final String KLAGE_FRIST_UKER = "klageFristUker";
    public static final String UKER_ETTER_FELLES_PERIODE = "ukerEtterfellesPeriode";
    //Innhentopplysninger
    public static final String SØKNAD_DATO = "soknadDato";
    //Forlenget
    public static final String SOKNAD_DATO = "soknadsdato";
    public static final String BEHANDLINGSFRIST_UKER = "behandlingsfristUker";
    public static final String FORLENGET_BEHANDLINGSFRIST = "forlengetBehandlingsfrist";
    //Innsynskrav svar
    public static final String INNSYN_RESULTAT_TYPE = "innsynResultatType";
    //Inntektsmelding kommet før søknad
    public static final String ARBEIDSGIVER_NAVN = "arbeidsgiverNavn";
    public static final String PERIODE_LISTE = "periodeListe";
    public static final String SOK_ANTALL_UKER_FOR = "sokAntallUkerFor";
    //Klage
    public static final String AVVIST_GRUNN = "avvistGrunn";
    public static final String ANTALL_UKER = "antallUker";
    //Revurdering
    public static final String TERMIN_DATO = "terminDato";
    public static final String ADVARSEL_KODE = "advarselKode";
    public static final String ENDRING_I_FREMTID = "endringIFremtid";
    //Fritekstbrev
    public static final String HOVED_OVERSKRIFT = "hovedoverskrift";
    public static final String BRØDTEKST = "brødtekst";

    private String feltnavn;
    private String feltverdi;
    private boolean strukturert;

    public void setFeltnavn(String feltnavn) {
        this.feltnavn = feltnavn;
    }

    public void setFeltverdi(String feltverdi) {
        this.feltverdi = feltverdi;
        this.strukturert = false;
    }

    public String getFeltnavn() {
        return feltnavn;
    }

    public String getFeltverdi() {
        return feltverdi;
    }

    public <T> T getStrukturertVerdi(Class<T> targetClass) {
        return FlettefeltJsonObjectMapper.readValue(feltverdi, targetClass);
    }

    public void setStukturertVerdi(Object stukturertVerdi) {
        this.feltverdi = toJson(stukturertVerdi);
        this.strukturert = true;
    }

    private String toJson(Object stukturertVerdi) {
        return FlettefeltJsonObjectMapper.toJson(stukturertVerdi);
    }

    public boolean isStrukturert() {
        return strukturert;
    }
}
