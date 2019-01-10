package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto;

import java.time.LocalDate;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.util.InputValideringRegex;

@JsonTypeName(VergeDto.AKSJONSPUNKT_KODE)
public class VergeDto extends BekreftetAksjonspunktDto {
    static final String AKSJONSPUNKT_KODE = "5030";

    @Size(max = 100)
    @Pattern(regexp = InputValideringRegex.NAVN)
    private String navn;
    @Digits(integer = 11, fraction = 0)
    private String fnr;
    private LocalDate gyldigFom;
    private LocalDate gyldigTom;

    @NotNull
    @ValidKodeverk
    private VergeType vergeType;
    
    private LocalDate vedtaksDato;
    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String mandatTekst;

    private boolean sokerErUnderTvungenForvaltning;
    private boolean sokerErKontaktPerson;
    private boolean vergeErKontaktPerson;

    public VergeDto() { //NOSONAR
    }

    public void setNavn(String navn) {
        this.navn = navn;
    }

    public void setGyldigFom(LocalDate gyldigFom) {
        this.gyldigFom = gyldigFom;
    }

    public void setGyldigTom(LocalDate gyldigTom) {
        this.gyldigTom = gyldigTom;
    }

    public void setVergeType(VergeType vergeType) {
        this.vergeType = vergeType;
    }

    public void setVedtaksDato(LocalDate vedtaksDato) {
        this.vedtaksDato = vedtaksDato;
    }

    public void setMandatTekst(String mandatTekst) {
        this.mandatTekst = mandatTekst;
    }

    public void setSokerErUnderTvungenForvaltning(boolean sokerErUnderTvungenForvaltning) {
        this.sokerErUnderTvungenForvaltning = sokerErUnderTvungenForvaltning;
    }

    public void setSokerErKontaktPerson(boolean sokerErKontaktPerson) {
        this.sokerErKontaktPerson = sokerErKontaktPerson;
    }

    public void setVergeErKontaktPerson(boolean vergeErKontaktPerson) {
        this.vergeErKontaktPerson = vergeErKontaktPerson;
    }

    public String getNavn() {
        return navn;
    }

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }

    public LocalDate getGyldigFom() {
        return gyldigFom;
    }

    public LocalDate getGyldigTom() {
        return gyldigTom;
    }

    public VergeType getVergeType() {
        return vergeType;
    }

    public LocalDate getVedtaksDato() {
        return vedtaksDato;
    }

    public String getMandatTekst() {
        return mandatTekst;
    }

    public boolean getSokerErUnderTvungenForvaltning() {
        return sokerErUnderTvungenForvaltning;
    }

    public boolean getSokerErKontaktPerson() {
        return sokerErKontaktPerson;
    }

    public boolean getVergeErKontaktPerson() {
        return vergeErKontaktPerson;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
                .leggTilFødselsnummer(fnr);
    }

    @Override
    public String getKode() {
        return AKSJONSPUNKT_KODE;
    }
}
