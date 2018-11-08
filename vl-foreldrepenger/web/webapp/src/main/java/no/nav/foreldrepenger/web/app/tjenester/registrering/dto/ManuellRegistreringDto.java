package no.nav.foreldrepenger.web.app.tjenester.registrering.dto;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OmsorgDto;
import no.nav.foreldrepenger.web.app.validering.ValidKodeverk;
import no.nav.vedtak.util.InputValideringRegex;

public abstract class ManuellRegistreringDto extends BekreftetAksjonspunktDto {

    @NotNull
    @ValidKodeverk
    private FamilieHendelseType tema;
    @NotNull
    @ValidKodeverk
    private FagsakYtelseType soknadstype;

    @NotNull
    @ValidKodeverk
    private ForeldreType soker;

    @Valid
    private RettigheterDto rettigheter;

    private boolean oppholdINorge;
    private boolean harTidligereOppholdUtenlands;
    private boolean harFremtidigeOppholdUtenlands;

    @Valid
    @Size(max = 1000)
    private List<UtenlandsoppholdDto> tidligereOppholdUtenlands;

    @Valid
    @Size(max = 1000)
    private List<UtenlandsoppholdDto> fremtidigeOppholdUtenlands;

    private boolean erBarnetFodt;
    private LocalDate termindato;
    private LocalDate terminbekreftelseDato;

    @Min(1)
    @Max(9)
    private Integer antallBarnFraTerminbekreftelse;

    @Min(1)
    @Max(9)
    private Integer antallBarn;

    @Size(min = 1, max = 9)
    private List<LocalDate> foedselsDato;

    @Valid
    private AnnenForelderDto annenForelder;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String tilleggsopplysninger;

    @Size(max = 4000)
    @Pattern(regexp = InputValideringRegex.FRITEKST)
    private String kommentarEndring;
    private boolean registrerVerge = Boolean.FALSE;
    private LocalDate mottattDato;
    private boolean ufullstendigSoeknad;

    @Valid
    OmsorgDto omsorg;

    ManuellRegistreringDto() {
        // For Jackson
    }

    public FagsakYtelseType getSoknadstype() {
        return soknadstype;
    }

    public ForeldreType getSoker() {
        return soker;
    }

    public void setSoker(ForeldreType soker) {
        this.soker = soker;
    }

    public void setSoknadstype(FagsakYtelseType soknadstype) {
        this.soknadstype = soknadstype;
    }

    public FamilieHendelseType getTema() {
        return tema;
    }

    public void setTema(FamilieHendelseType tema) {
        this.tema = tema;
    }

    public RettigheterDto getRettigheter() {
        return rettigheter;
    }

    public void setRettigheter(RettigheterDto rettigheter) {
        this.rettigheter = rettigheter;
    }

    public boolean getOppholdINorge() {
        return oppholdINorge;
    }

    public void setOppholdINorge(boolean oppholdINorge) {
        this.oppholdINorge = oppholdINorge;
    }

    public boolean getHarTidligereOppholdUtenlands() {
        return harTidligereOppholdUtenlands;
    }

    public void setHarTidligereOppholdUtenlands(boolean harTidligereOppholdUtenlands) {
        this.harTidligereOppholdUtenlands = harTidligereOppholdUtenlands;
    }

    public boolean getHarFremtidigeOppholdUtenlands() {
        return harFremtidigeOppholdUtenlands;
    }

    public void setHarFremtidigeOppholdUtenlands(boolean harFremtidigeOppholdUtenlands) {
        this.harFremtidigeOppholdUtenlands = harFremtidigeOppholdUtenlands;
    }

    public List<UtenlandsoppholdDto> getTidligereOppholdUtenlands() {
        return tidligereOppholdUtenlands;
    }

    public void setTidligereOppholdUtenlands(List<UtenlandsoppholdDto> tidligereOppholdUtenlands) {
        this.tidligereOppholdUtenlands = tidligereOppholdUtenlands;
    }

    public List<UtenlandsoppholdDto> getFremtidigeOppholdUtenlands() {
        return fremtidigeOppholdUtenlands;
    }

    public void setFremtidigeOppholdUtenlands(List<UtenlandsoppholdDto> fremtidigeOppholdUtenlands) {
        this.fremtidigeOppholdUtenlands = fremtidigeOppholdUtenlands;
    }

    public boolean getErBarnetFodt() {
        return erBarnetFodt;
    }

    public void setErBarnetFodt(boolean erBarnetFodt) {
        this.erBarnetFodt = erBarnetFodt;
    }

    public LocalDate getTermindato() {
        return termindato;
    }

    public void setTermindato(LocalDate termindato) {
        this.termindato = termindato;
    }

    public LocalDate getTerminbekreftelseDato() {
        return terminbekreftelseDato;
    }

    public void setTerminbekreftelseDato(LocalDate terminbekreftelseDato) {
        this.terminbekreftelseDato = terminbekreftelseDato;
    }

    public Integer getAntallBarnFraTerminbekreftelse() {
        return antallBarnFraTerminbekreftelse;
    }

    public void setAntallBarnFraTerminbekreftelse(Integer antallBarnFraTerminbekreftelse) {
        this.antallBarnFraTerminbekreftelse = antallBarnFraTerminbekreftelse;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

    public List<LocalDate> getFoedselsDato() {
        return foedselsDato;
    }

    public void setFoedselsDato(List<LocalDate> foedselsDato) {
        this.foedselsDato = foedselsDato;
    }

    public AnnenForelderDto getAnnenForelder() {
        return annenForelder;
    }

    public void setAnnenForelder(AnnenForelderDto annenForelder) {
        this.annenForelder = annenForelder;
    }

    public String getTilleggsopplysninger() {
        return tilleggsopplysninger;
    }

    public void setTilleggsopplysninger(String tilleggsopplysninger) {
        this.tilleggsopplysninger = tilleggsopplysninger;
    }

    public String getKommentarEndring() {
        return kommentarEndring;
    }

    public void setKommentarEndring(String kommentarEndring) {
        this.kommentarEndring = kommentarEndring;
    }

    public boolean isRegistrerVerge() {
        return registrerVerge;
    }

    public void setRegistrerVerge(boolean registrerVerge) {
        this.registrerVerge = registrerVerge;
    }

    public LocalDate getMottattDato() {
        return mottattDato;
    }

    public void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    public boolean getUfullstendigSoeknad() {
        return ufullstendigSoeknad;
    }

    public void setUfullstendigSoeknad(boolean ufullstendigSoeknad) {
        this.ufullstendigSoeknad = ufullstendigSoeknad;
    }

    public OmsorgDto getOmsorg() {
        return omsorg;
    }

    public void setOmsorg(OmsorgDto omsorg) {
        this.omsorg = omsorg;
    }
}
