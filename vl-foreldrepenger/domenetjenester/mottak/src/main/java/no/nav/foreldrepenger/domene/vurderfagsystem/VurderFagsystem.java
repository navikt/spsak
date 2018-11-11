package no.nav.foreldrepenger.domene.vurderfagsystem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class VurderFagsystem {
    // FIXME(Humle): dette skal inn i kodeverk.  Ikke bruk title-case i koder vær så snill.
    public static final String ÅRSAK_ENDRING = "Endring";
    public static final String ÅRSAK_NY = "Ny";

    private JournalpostId journalpostId;
    private boolean strukturertSøknad;
    private AktørId aktørId;
    private BehandlingTema behandlingTema;
    private List<LocalDate> adopsjonsbarnFodselsdatoer;

    private LocalDate barnTermindato;
    private LocalDate barnFodselsdato;
    private LocalDate omsorgsovertakelsedato;
    private String årsakInnsendingInntektsmelding;

    // TODO PFP-57 Opprydding - Skal fjernes når kontrakt er oppdatert
    private LocalDate forsendelseMottatt;
    private LocalDateTime forsendelseMottattTidspunkt;
    private LocalDate startDatoForeldrepengerInntektsmelding;

    private Saksnummer saksnummer;
    private AktørId annenPart;

    private DokumentTypeId dokumentTypeId;
    private DokumentKategori dokumentKategori;

    private String virksomhetsnummer;
    private String arbeidsforholdsid;


    public Optional<JournalpostId> getJournalpostId() {
        return Optional.ofNullable(journalpostId);
    }

    public void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    public boolean isStrukturertSøknad() {
        return strukturertSøknad;
    }

    public void setStrukturertSøknad(boolean strukturertSøknad) {
        this.strukturertSøknad = strukturertSøknad;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public void setAktørId(AktørId aktørId) {
        this.aktørId = aktørId;
    }

    public BehandlingTema getBehandlingTema() {
        return behandlingTema;
    }

    public void setBehandlingTema(BehandlingTema behandlingTema) {
        this.behandlingTema = behandlingTema;
    }

    public List<LocalDate> getAdopsjonsbarnFodselsdatoer() {
        return adopsjonsbarnFodselsdatoer;
    }

    public void setAdopsjonsbarnFodselsdatoer(List<LocalDate> adopsjonsbarnFodselsdatoer) {
        this.adopsjonsbarnFodselsdatoer = adopsjonsbarnFodselsdatoer;
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

    public void setÅrsakInnsendingInntektsmelding(String årsakInnsendingInntektsmelding) {
        this.årsakInnsendingInntektsmelding = årsakInnsendingInntektsmelding;
    }

    public Optional<Saksnummer> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public void setSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Optional<AktørId> getAnnenPart() {
        return Optional.ofNullable(annenPart);
    }

    public void setAnnenPart(AktørId annenPart) {
        this.annenPart = annenPart;
    }

    public boolean erInntektsmelding() {
        return årsakInnsendingInntektsmelding != null;
    }

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        this.dokumentTypeId = dokumentTypeId;
    }

    public DokumentKategori getDokumentKategori() {
        return dokumentKategori;
    }

    public void setDokumentKategori(DokumentKategori dokumentKategori) {
        this.dokumentKategori = dokumentKategori;
    }

    public Optional<String> getVirksomhetsnummer() {
        return Optional.ofNullable(virksomhetsnummer);
    }

    public void setVirksomhetsnummer(String virksomhetsnummer) {
        this.virksomhetsnummer = virksomhetsnummer;
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
}

