package no.nav.foreldrepenger.dokumentbestiller.api.mal.dto;

import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;

import java.time.LocalDate;
import java.util.Optional;

public class DokumentTypeDto {
    private Long behandlingId;
    private LocalDate mottattDato;
    private LocalDate mottattKlageDato;
    private String ytelsesTypeKode;
    private String behandlingsTypeAvslagVedtak;
    private String behandlingsTypePositivtVedtak;
    private String brukerKjønnKode;
    private String relasjonsKode;
    private int behandlingsfristIUker;
    private String fritekstKlageOversendt;
    private Boolean gjelderFødsel;
    private Integer antallBarn;
    private LocalDate termindato;
    private LocalDate termindatoFraOriginalBehandling;
    private Long halvG;
    private boolean barnErFødt;

    // Internal state indikatorer
    private boolean harIkkeBehandlingsResultat;
    private String søkersNavn;
    private String personstatus;

    //Avslag og opphør
    private String klageAvvistÅrsakKode;
    private LocalDate sisteDagIFellesPeriode;
    private Integer ukerEtterFellesPeriode;
    private DokumentBehandlingsresultatDto dokumentBehandlingsresultatDto;
    private DokumentBeregningsgrunnlagDto dokumentBeregningsgrunnlagDto;
    private DokumentBeregningsresultatDto dokumentBeregningsresultatDto;

    public DokumentTypeDto(Long behandlingId) {
        this.behandlingId = behandlingId;
        this.dokumentBehandlingsresultatDto = new DokumentBehandlingsresultatDto();
        this.dokumentBeregningsgrunnlagDto = new DokumentBeregningsgrunnlagDto();
        this.dokumentBeregningsresultatDto = new DokumentBeregningsresultatDto();
    }

    public LocalDate getMottattDato() {
        return Optional.ofNullable(mottattDato).orElseThrow(() -> DokumentBestillerFeil.FACTORY.harIkkeSøknadMottattDato(getBehandlingId()).toException());
    }

    public void setMottattDato(LocalDate mottattDato) {
        this.mottattDato = mottattDato;
    }

    public String getBrukerKjønnKode() {
        return brukerKjønnKode;
    }

    public void setBrukerKjønnKode(String brukerKjønnKode) {
        this.brukerKjønnKode = brukerKjønnKode;
    }

    public Integer getAntallBarn() {
        return antallBarn;
    }

    public void setAntallBarn(Integer antallBarn) {
        this.antallBarn = antallBarn;
    }

    public String getBehandlingsTypeAvslagVedtak() {
        return behandlingsTypeAvslagVedtak;
    }

    public void setBehandlingsTypeForAvslagVedtak(String behandlingsType) {
        this.behandlingsTypeAvslagVedtak = behandlingsType;
    }

    public Boolean getGjelderFødsel() {
        return gjelderFødsel;
    }

    public void setGjelderFødsel(Boolean gjelderFødsel) {
        this.gjelderFødsel = gjelderFødsel;
    }

    public String getRelasjonsKode() {
        return relasjonsKode;
    }

    public void setRelasjonsKode(String relasjonsKode) {
        this.relasjonsKode = relasjonsKode;
    }

    public String getYtelsesTypeKode() {
        return ytelsesTypeKode;
    }

    public void setYtelsesTypeKode(String ytelsesTypeKode) {
        this.ytelsesTypeKode = ytelsesTypeKode;
    }

    public int getBehandlingsfristIUker() {
        return behandlingsfristIUker;
    }

    public void setBehandlingsfristIUker(int behandlingsfristIUker) {
        this.behandlingsfristIUker = behandlingsfristIUker;
    }

    public Optional<LocalDate> getTermindato() {
        return Optional.ofNullable(termindato);
    }

    public void setTermindato(LocalDate termindato) {
        this.termindato = termindato;
    }

    public String getBehandlingsTypePositivtVedtak() {
        return behandlingsTypePositivtVedtak;
    }

    public void setBehandlingsTypePositivtVedtak(String behandlingsTypePositivtVedtak) {
        this.behandlingsTypePositivtVedtak = behandlingsTypePositivtVedtak;
    }

    public Optional<String> getFritekstKlageOversendt() {
        return Optional.ofNullable(fritekstKlageOversendt);
    }

    public void setFritekstKlageOversendt(String fritekstKlageOversendt) {
        this.fritekstKlageOversendt = fritekstKlageOversendt;
    }

    public boolean getHarIkkeBehandlingsResultat() {
        return harIkkeBehandlingsResultat;
    }

    public void setHarIkkeBehandlingsResultat(boolean harIkkeBehandlingsResultat) {
        this.harIkkeBehandlingsResultat = harIkkeBehandlingsResultat;
    }

    public LocalDate getMottattKlageDato() {
        return mottattKlageDato;
    }

    public void setMottattKlageDato(LocalDate mottattKlageDato) {
        this.mottattKlageDato = mottattKlageDato;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Optional<String> getKlageAvvistÅrsakKode() {
        return Optional.ofNullable(klageAvvistÅrsakKode);
    }

    public void setKlageAvvistÅrsakKode(String klageAvvistÅrsakKode) {
        this.klageAvvistÅrsakKode = klageAvvistÅrsakKode;
    }

    public Optional<LocalDate> getTermindatoFraOriginalBehandling() {
        return Optional.ofNullable(termindatoFraOriginalBehandling);
    }

    public void setTermindatoFraOriginalBehandling(LocalDate termindatoFraOriginalBehandling) {
        this.termindatoFraOriginalBehandling = termindatoFraOriginalBehandling;
    }

    public void setSøkersNavn(String søkersNavn) {
        this.søkersNavn = søkersNavn;
    }

    public String getSøkersNavn() {
        return søkersNavn;
    }

    public void setPersonstatus(String personstatus) {
        this.personstatus = personstatus;
    }

    public String getPersonstatus() {
        return personstatus;
    }

    public LocalDate getSisteDagIFellesPeriode() {
        return sisteDagIFellesPeriode;
    }

    public void setSisteDagIFellesPeriode(LocalDate sisteDagIFellesPeriode) {
        this.sisteDagIFellesPeriode = sisteDagIFellesPeriode;
    }

    public Integer getUkerEtterFellesPeriode() {
        return ukerEtterFellesPeriode;
    }

    public void setUkerEtterFellesPeriode(Integer ukerEtterFellesPeriode) {
        this.ukerEtterFellesPeriode = ukerEtterFellesPeriode;
    }

    public Long getHalvG() {
        return halvG;
    }

    public void setHalvG(Long halvG) {
        this.halvG = halvG;
    }

    public boolean getBarnErFødt() {
        return barnErFødt;
    }

    public void setBarnErFødt(boolean barnErFødt) {
        this.barnErFødt = barnErFødt;
    }

    public DokumentBehandlingsresultatDto getDokumentBehandlingsresultatDto() {
        return dokumentBehandlingsresultatDto;
    }

    public DokumentBeregningsgrunnlagDto getDokumentBeregningsgrunnlagDto() {
        return dokumentBeregningsgrunnlagDto;
    }

    public DokumentBeregningsresultatDto getDokumentBeregningsresultatDto() {
        return dokumentBeregningsresultatDto;
    }
}
