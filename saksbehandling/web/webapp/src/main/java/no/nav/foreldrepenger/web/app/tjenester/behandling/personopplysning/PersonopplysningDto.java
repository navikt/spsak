package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;

public class PersonopplysningDto extends PersonIdentDto {

    private Integer nummer;
    private NavBrukerKjønn navBrukerKjonn;
    private LandkoderDto statsborgerskap;
    private AvklartPersonstatus avklartPersonstatus;
    private PersonstatusType personstatus;
    private SivilstandType sivilstand;
    private String navn;
    private LocalDate dodsdato;
    private LocalDate fodselsdato;
    private List<PersonadresseDto> adresser = new ArrayList<>();

    private Region region;
    private OpplysningsKilde opplysningsKilde = OpplysningsKilde.UDEFINERT;
    private boolean harVerge;

    public NavBrukerKjønn getNavBrukerKjonn() {
        return navBrukerKjonn;
    }

    void setNavBrukerKjonn(NavBrukerKjønn navBrukerKjonn) {
        this.navBrukerKjonn = navBrukerKjonn;
    }

    public LandkoderDto getStatsborgerskap() {
        return statsborgerskap;
    }

    void setStatsborgerskap(LandkoderDto statsborgerskap) {
        this.statsborgerskap = statsborgerskap;
    }

    public PersonstatusType getPersonstatus() {
        return personstatus;
    }

    void setPersonstatus(PersonstatusType personstatus) {
        this.personstatus = personstatus;
    }

    public SivilstandType getSivilstand() {
        return sivilstand;
    }

    void setSivilstand(SivilstandType sivilstand) {
        this.sivilstand = sivilstand;
    }

    public Integer getNummer() {
        return nummer;
    }

    public void setNummer(Integer nummer) {
        this.nummer = nummer;
    }

    public String getNavn() {
        return navn;
    }

    void setNavn(String navn) {
        this.navn = navn;
    }

    public LocalDate getDodsdato() {
        return dodsdato;
    }

    void setDodsdato(LocalDate dodsdato) {
        this.dodsdato = dodsdato;
    }

    public List<PersonadresseDto> getAdresser() {
        return adresser;
    }

    void setAdresser(List<PersonadresseDto> adresser) {
        this.adresser = adresser;
    }

    public Region getRegion() {
        return region;
    }

    void setRegion(Region region) {
        this.region = region;
    }

    public LocalDate getFodselsdato() {
        return fodselsdato;
    }

    void setFodselsdato(LocalDate fodselsdato) {
        this.fodselsdato = fodselsdato;
    }

    public OpplysningsKilde getOpplysningsKilde() {
        return opplysningsKilde;
    }

    void setOpplysningsKilde(OpplysningsKilde opplysningsKilde) {
        this.opplysningsKilde = opplysningsKilde;
    }

    public boolean isHarVerge() {
        return harVerge;
    }

    void setHarVerge(boolean harVerge) {
        this.harVerge = harVerge;
    }

    public AvklartPersonstatus getAvklartPersonstatus() {
        return avklartPersonstatus;
    }

    public void setAvklartPersonstatus(AvklartPersonstatus avklartPersonstatus) {
        this.avklartPersonstatus = avklartPersonstatus;
    }
}
