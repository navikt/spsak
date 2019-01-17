package no.nav.foreldrepenger.kontrakter.fordel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.util.InputValideringRegex;

public class VurderFagsystemDto implements AbacDto {

    @Digits(integer = 18, fraction = 0)
    private String journalpostId;
    @NotNull
    private boolean strukturertSøknad;
    @NotNull
    @Digits(integer = 19, fraction = 0)
    private String aktørId;
    @NotNull
    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String behandlingstemaOffisiellKode;

    @Size(min = 1, max = 9)
    private List<LocalDate> adopsjonsBarnFodselsdatoer;
    private LocalDate barnTermindato;
    private LocalDate barnFodselsdato;
    private LocalDate omsorgsovertakelsedato;
    @Size(max = 7) // Endring.length == 7
    @Pattern(regexp = InputValideringRegex.NAVN)
    private String årsakInnsendingInntektsmelding;

    @Size(max = 30)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String saksnummer;

    @Digits(integer = 19, fraction = 0)
    private String annenPart;

    @Pattern(regexp = "\\d{9}|\\d{13}")
    private String virksomhetsnummer;
    private String arbeidsgiverAktørId;

    private String arbeidsforholdsid;
    // TODO PFP-57 Opprydding - Fjerne denne fra kontrakten
    private LocalDate forsendelseMottatt;
    private LocalDateTime forsendelseMottattTidspunkt;
    private LocalDate startDatoForeldrepengerInntektsmelding;

    @Size(max = 8)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String dokumentTypeIdOffisiellKode;

    @Size(max = 25)
    @Pattern(regexp = InputValideringRegex.KODEVERK)
    private String dokumentKategoriOffisiellKode;


    public VurderFagsystemDto() {  // For Jackson
    }

    public VurderFagsystemDto(String journalpostId, boolean strukturertSøknad, String aktørId, String behandlingstemaOffisiellKode) {
        this.journalpostId = journalpostId;
        this.strukturertSøknad = strukturertSøknad;
        this.aktørId = aktørId;
        this.behandlingstemaOffisiellKode = behandlingstemaOffisiellKode;
    }

    public Optional<String> getJournalpostId() {
        return Optional.ofNullable(journalpostId);
    }

    public boolean isStrukturertSøknad() {
        return strukturertSøknad;
    }

    public String getAktørId() {
        return aktørId;
    }

    public String getBehandlingstemaOffisiellKode() {
        return behandlingstemaOffisiellKode;
    }

    public List<LocalDate> getAdopsjonsBarnFodselsdatoer() {
        if (adopsjonsBarnFodselsdatoer == null) {
            return Collections.emptyList();
        }
        return adopsjonsBarnFodselsdatoer;
    }

    public void setAdopsjonsBarnFodselsdatoer(List<LocalDate> adopsjonsBarnFodselsdatoer) {
        this.adopsjonsBarnFodselsdatoer = adopsjonsBarnFodselsdatoer;
    }

    public Optional<LocalDate> getBarnTermindato() {
        return Optional.ofNullable(barnTermindato);
    }

    public void setBarnTermindato(LocalDate barnTermindato) {
        this.barnTermindato = barnTermindato;
    }

    public Optional<LocalDate> getBarnFodselsdato() {
        return Optional.ofNullable(barnFodselsdato);
    }

    public void setBarnFodselsdato(LocalDate barnFodselsdato) {
        this.barnFodselsdato = barnFodselsdato;
    }

    public Optional<LocalDate> getOmsorgsovertakelsedato() {
        return Optional.ofNullable(omsorgsovertakelsedato);
    }

    public void setOmsorgsovertakelsedato(LocalDate omsorgsovertakelsedato) {
        this.omsorgsovertakelsedato = omsorgsovertakelsedato;
    }

    public Optional<String> getÅrsakInnsendingInntektsmelding() {
        return Optional.ofNullable(årsakInnsendingInntektsmelding);
    }

    public String getDokumentTypeIdOffisiellKode() {
        return dokumentTypeIdOffisiellKode;
    }

    public void setDokumentTypeIdOffisiellKode(String dokumentTypeIdOffisiellKode) {
        this.dokumentTypeIdOffisiellKode = dokumentTypeIdOffisiellKode;
    }

    public String getDokumentKategoriOffisiellKode() {
        return dokumentKategoriOffisiellKode;
    }

    public void setDokumentKategoriOffisiellKode(String dokumentKategoriOffisiellKode) {
        this.dokumentKategoriOffisiellKode = dokumentKategoriOffisiellKode;
    }

    public void setÅrsakInnsendingInntektsmelding(String årsakInnsendingInntektsmelding) {
        this.årsakInnsendingInntektsmelding = årsakInnsendingInntektsmelding;
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Optional<String> getAnnenPart() {
        return Optional.ofNullable(annenPart);
    }

    public void setAnnenPart(String annenPart) {
        this.annenPart = annenPart;
    }

    public Optional<String> getVirksomhetsnummer() {
        return Optional.ofNullable(virksomhetsnummer);
    }

    public void setVirksomhetsnummer(String virksomhetsnummer) {
        this.virksomhetsnummer = virksomhetsnummer;
    }

    public Optional<String> getArbeidsgiverAktørId() {
        return Optional.ofNullable(arbeidsgiverAktørId);

    }

    public void setArbeidsgiverAktørId(String arbeidsgiverAktørId) {
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    public Optional<String> getArbeidsforholdsid() {
        return Optional.ofNullable(arbeidsforholdsid);
    }

    public void setArbeidsforholdsid(String arbeidsforholdsid) {
        this.arbeidsforholdsid = arbeidsforholdsid;
    }

    public Optional<LocalDate> getForsendelseMottatt() {
        return Optional.ofNullable(forsendelseMottatt);
    }

    public void setForsendelseMottatt(LocalDate forsendelseMottatt) {
        this.forsendelseMottatt = forsendelseMottatt;
    }

    public Optional<LocalDateTime> getForsendelseMottattTidspunkt() {
        return Optional.ofNullable(forsendelseMottattTidspunkt);
    }

    public void setForsendelseMottattTidspunkt(LocalDateTime forsendelseMottattTidspunkt) {
        this.forsendelseMottattTidspunkt = forsendelseMottattTidspunkt;
    }

    public Optional<LocalDate> getStartDatoForeldrepengerInntektsmelding() {
        return Optional.ofNullable(startDatoForeldrepengerInntektsmelding);
    }

    public void setStartDatoForeldrepengerInntektsmelding(LocalDate startDatoForeldrepengerInntektsmelding) {
        this.startDatoForeldrepengerInntektsmelding = startDatoForeldrepengerInntektsmelding;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        AbacDataAttributter abacDataAttributter = AbacDataAttributter.opprett()
                .leggTilAktørId(aktørId);

        if (journalpostId != null) {
            abacDataAttributter.leggTilJournalPostId(journalpostId, false);
        }

        if (saksnummer != null) {
            abacDataAttributter.leggTilSaksnummer(saksnummer);
        }
        return abacDataAttributter;
    }

}
