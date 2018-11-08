package no.nav.foreldrepenger.domene.personopplysning;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.verge.BrevMottaker;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class VergeAksjonpunktDto {

    private PersonIdent fnr;
    private LocalDate fom;
    private LocalDate tom;
    private String vergeTypeKode;
    private BrevMottaker brevMottaker;
    private LocalDate vedtaksDato;
    private String mandatTekst;
    private boolean søkerErUnderTvungenForvaltning;

    public VergeAksjonpunktDto(PersonIdent fnr, LocalDate fom, LocalDate tom, String vergeTypeKode,
                               BrevMottaker brevMottaker, LocalDate vedtaksDato,
                               String mandatTekst, boolean søkerErUnderTvungenForvaltning) {
        this.fnr = fnr;
        this.fom = fom;
        this.tom = tom;
        this.vergeTypeKode = vergeTypeKode;
        this.brevMottaker = brevMottaker;
        this.vedtaksDato = vedtaksDato;
        this.mandatTekst = mandatTekst;
        this.søkerErUnderTvungenForvaltning = søkerErUnderTvungenForvaltning;
    }

    public PersonIdent getFnr() {
        return fnr;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getTom() {
        return tom;
    }

    public String getVergeTypeKode() {
        return vergeTypeKode;
    }

    public BrevMottaker getBrevMottaker() {
        return brevMottaker;
    }

    public LocalDate getVedtaksDato() {
        return vedtaksDato;
    }

    public String getMandatTekst() {
        return mandatTekst;
    }

    public boolean getErSøkerErUnderTvungenForvaltning() {
        return søkerErUnderTvungenForvaltning;
    }
}
