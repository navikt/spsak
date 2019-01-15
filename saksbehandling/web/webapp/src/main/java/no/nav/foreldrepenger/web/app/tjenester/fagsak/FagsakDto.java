package no.nav.foreldrepenger.web.app.tjenester.fagsak;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public class FagsakDto {
    private Long saksnummer;
    private FagsakYtelseType sakstype;
    private FagsakStatus status;
    private PersonDto person;
    private LocalDateTime opprettet;
    private LocalDateTime endret;
    private Boolean kanRevurderingOpprettes;

    public FagsakDto() {
        // Injiseres i test
    }

    public FagsakDto(Fagsak fagsak, PersonDto person, Boolean kanRevurderingOpprettes) {
        this.saksnummer = Long.parseLong(fagsak.getSaksnummer().getVerdi());
        this.sakstype = fagsak.getYtelseType();
        this.status = fagsak.getStatus();
        this.person = person;
        this.opprettet = fagsak.getOpprettetTidspunkt();
        this.endret = fagsak.getEndretTidspunkt();
        this.kanRevurderingOpprettes = kanRevurderingOpprettes;
    }

    public Long getSaksnummer() {
        return saksnummer;
    }

    public FagsakYtelseType getSakstype() {
        return sakstype;
    }

    public FagsakStatus getStatus() {
        return status;
    }

    public PersonDto getPerson() {
        return person;
    }

    public LocalDateTime getOpprettet() {
        return opprettet;
    }

    public LocalDateTime getEndret() {
        return endret;
    }

    public Boolean getKanRevurderingOpprettes() {
        return kanRevurderingOpprettes;
    }

    @Override
    public String toString() {
        return "<saksnummer=" + saksnummer + //$NON-NLS-1$
            ", sakstype=" + sakstype + //$NON-NLS-1$
            ", status=" + status + //$NON-NLS-1$
            ", person=" + person + //$NON-NLS-1$
            ", opprettet=" + opprettet + //$NON-NLS-1$
            ", endret=" + endret + //$NON-NLS-1$
            ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FagsakDto)) return false;

        FagsakDto fagsakDto = (FagsakDto) o;

        if (!saksnummer.equals(fagsakDto.saksnummer)) return false;
        if (!sakstype.equals(fagsakDto.sakstype)) return false;
        if (!status.equals(fagsakDto.status)) return false;
        if (!person.equals(fagsakDto.person)) return false;
        if (opprettet != null ? !opprettet.equals(fagsakDto.opprettet) : fagsakDto.opprettet != null) return false;
        return endret != null ? endret.equals(fagsakDto.endret) : fagsakDto.endret == null;
    }

    @Override
    public int hashCode() {
        int result = saksnummer.hashCode();
        result = 31 * result + sakstype.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + person.hashCode();
        result = 31 * result + (opprettet != null ? opprettet.hashCode() : 0);
        result = 31 * result + (endret != null ? endret.hashCode() : 0);
        return result;
    }

}
