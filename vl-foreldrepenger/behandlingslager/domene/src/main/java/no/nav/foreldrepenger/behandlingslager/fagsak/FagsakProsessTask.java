package no.nav.foreldrepenger.behandlingslager.fagsak;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "FagsakProsessTask")
@Table(name = "FAGSAK_PROSESS_TASK")
class FagsakProsessTask extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAGSAK_PROSESS_TASK")
    private Long id;

    @Column(name = "fagsak_id", nullable = false, updatable = false, columnDefinition = "NUMERIC")
    private Long fagsakId;

    @Column(name = "prosess_task_id", nullable = false, updatable = false, columnDefinition = "NUMERIC")
    private Long prosessTaskId;

    @Column(name = "behandling_id", updatable = false, columnDefinition = "NUMERIC")
    private Long behandlingId;

    @Column(name = "gruppe_sekvensnr", updatable = false, columnDefinition = "NUMERIC")
    private Long gruppeSekvensNr;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    FagsakProsessTask() {
        // Hibernate trenger en
    }

    public FagsakProsessTask(Long fagsakId, Long prosessTaskId, Long behandlingId) {
        this(fagsakId, prosessTaskId, behandlingId, null);
    }

    public FagsakProsessTask(Long fagsakId, Long prosessTaskId, Long behandlingId, Long gruppeSekvensNr) {
        this.fagsakId = fagsakId;
        this.prosessTaskId = prosessTaskId;
        this.behandlingId = behandlingId;
        this.gruppeSekvensNr = gruppeSekvensNr;
    }

    public FagsakProsessTask(Long fagsakId, Long prosessTaskId) {
        this(fagsakId, prosessTaskId, null);
    }

    public Long getFagsakId() {
        return fagsakId;
    }

    public Long getProsessTaskId() {
        return prosessTaskId;
    }

    public Long getBehandlingId() {
        return behandlingId;
    }

    public Long getGruppeSekvensNr() {
        return gruppeSekvensNr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof FagsakProsessTask)) {
            return false;
        }
        FagsakProsessTask other = (FagsakProsessTask) obj;
        return Objects.equals(prosessTaskId, other.prosessTaskId)
            && Objects.equals(fagsakId, other.fagsakId)
            && Objects.equals(behandlingId, other.behandlingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prosessTaskId, fagsakId, behandlingId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" //$NON-NLS-1$
            + "prosessTask=" + prosessTaskId //$NON-NLS-1$
            + ", fagsak=" + fagsakId //$NON-NLS-1$
            + (behandlingId == null ? "" : ", behandling=" + behandlingId) //$NON-NLS-1$ //$NON-NLS-2$
            + (gruppeSekvensNr == null ? "" : ", gruppeSekvensNr=" + gruppeSekvensNr)//$NON-NLS-1$ //$NON-NLS-2$
            + ">"; //$NON-NLS-1$
    }
}
