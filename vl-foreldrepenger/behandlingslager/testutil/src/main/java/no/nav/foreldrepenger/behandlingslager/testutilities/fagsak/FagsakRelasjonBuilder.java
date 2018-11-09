package no.nav.foreldrepenger.behandlingslager.testutilities.fagsak;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.util.FPDateUtil;

public class FagsakRelasjonBuilder {

    private FagsakYtelseType ytelseType;

    private Dekningsgrad dekningsgrad;

    private LocalDate fødselsdato;
    private LocalDate termindato;
    private LocalDate adopsjonsdato;

    public FagsakRelasjonBuilder(FagsakYtelseType type) {
        ytelseType = type;
    }

    public static FagsakRelasjonBuilder engangsstønad() {
        return new FagsakRelasjonBuilder(FagsakYtelseType.FORELDREPENGER);
    }

    public static FagsakRelasjonBuilder foreldrepenger() {
        return new FagsakRelasjonBuilder(FagsakYtelseType.FORELDREPENGER);
    }

    public FagsakRelasjonBuilder medFødseldato(LocalDate dato) {
        this.fødselsdato = dato;
        return this;
    }

    public FagsakRelasjonBuilder medAdopsjonsdato(LocalDate dato) {
        this.adopsjonsdato = dato;
        return this;
    }

    public FagsakRelasjonBuilder medTermindato(LocalDate dato) {
        this.termindato = dato;
        return this;
    }

    public FagsakRelasjonBuilder medDekningsgrad(int dekningsgrad) {
        this.dekningsgrad = new Dekningsgrad(dekningsgrad);
        return this;
    }

    public LocalDate getTermindato() {
        return termindato;
    }

    public LocalDate getAdopsjonsdato() {
        return adopsjonsdato;
    }

    public LocalDate getFødselsdato() {
        return fødselsdato;
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }

    public FagsakYtelseType getYtelseType() {
        return ytelseType;
    }

    void setDefaults() {
        fødselsdato = LocalDate.now(FPDateUtil.getOffset());
        termindato = fødselsdato;
        if (dekningsgrad == null) {
            dekningsgrad = Dekningsgrad._100;
        }
    }

}
