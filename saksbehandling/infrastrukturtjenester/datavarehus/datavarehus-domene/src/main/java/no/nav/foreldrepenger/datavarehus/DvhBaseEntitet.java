package no.nav.foreldrepenger.datavarehus;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import java.util.Objects;

import no.nav.vedtak.util.FPDateUtil;

@MappedSuperclass
public class DvhBaseEntitet implements Serializable {

    @Column(name = "TRANS_TID", nullable = false)
    private LocalDateTime transTid;

    @Column(name = "FUNKSJONELL_TID", nullable = false)
    private LocalDateTime funksjonellTid;

    @Column(name = "ENDRET_AV", nullable = true)
    private String endretAv;

    @PrePersist
    protected void onCreate() {
        this.transTid = LocalDateTime.now(FPDateUtil.getOffset());
        if (this.funksjonellTid == null) {
            this.funksjonellTid = LocalDateTime.now(FPDateUtil.getOffset());
        }
    }

    public LocalDateTime getFunksjonellTid() {
        return funksjonellTid;
    }

    public void setFunksjonellTid(LocalDateTime funksjonellTid) {
        this.funksjonellTid = funksjonellTid;
    }

    public String getEndretAv() {
        return endretAv;
    }

    public void setEndretAv(String endretAv) {
        this.endretAv = endretAv;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DvhBaseEntitet)) {
            return false;
        }
        DvhBaseEntitet other = (DvhBaseEntitet) obj;
        return Objects.equals(transTid, other.transTid)
                && Objects.equals(funksjonellTid, other.funksjonellTid)
                && Objects.equals(endretAv, other.endretAv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transTid, funksjonellTid, endretAv);
    }
}
