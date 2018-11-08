package no.nav.foreldrepenger.domene.mottak.forsendelse;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForsendelseStatusDataDTO {
    private ForsendelseStatus forsendelseStatus;

    /** Joark journalpostid. */
    private Long journalpostId;

    /** GSAK Saksnummer. (samme som Fagsak#saksnummer). */
    private Long saksnummer;

    public ForsendelseStatusDataDTO(ForsendelseStatus forsendelseStatus) {
        this.forsendelseStatus = forsendelseStatus;
    }

    public ForsendelseStatus getForsendelseStatus() {
        return forsendelseStatus;
    }

    public void setForsendelseStatus(ForsendelseStatus forsendelseStatus) {
        this.forsendelseStatus = forsendelseStatus;
    }

    public Long getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(Long journalpostId) {
        this.journalpostId = journalpostId;
    }

    public Long getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(Long saksnummer) {
        this.saksnummer = saksnummer;
    }
}
