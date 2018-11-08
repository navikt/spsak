package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps;

public class TpsRelasjon {

    public String fnr;
    public String relasjonFnr;
    public String relasjonsType;
    public String fornavn = "Fornavn";
    public String etternavn = "Etternavn";

    public TpsRelasjon() {
    }


    @Override
    public String toString() {
        return "TpsRelasjon{" +
                "fnr='" + fnr + '\'' +
                ", relasjonFnr='" + relasjonFnr + '\'' +
                ", relasjonsType='" + relasjonsType + '\'' +
                ", fornavn='" + fornavn + '\'' +
                ", etternavn='" + etternavn + '\'' +
                '}';
    }
}
