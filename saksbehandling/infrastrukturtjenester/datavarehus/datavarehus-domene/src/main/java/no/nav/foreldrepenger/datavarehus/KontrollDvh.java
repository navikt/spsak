package no.nav.foreldrepenger.datavarehus;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

import java.util.Objects;

@Entity(name = "KontrollDvh")
@Table(name = "KONTROLL_DVH")
public class KontrollDvh implements Serializable {

    @Id
    private Long id;

    @Column(name = "FAGSAK_TRANS_ID_MAX", nullable = true)
    private Long fagsakTransIdMax;

    @Column(name = "BEH_TRANS_ID_MAX", nullable = true)
    private Long behandlingTransIdMax;

    @Column(name = "BEH_STEG_TRANS_ID_MAX", nullable = true)
    private Long behandllingStegTransIdMax;

    @Column(name = "BEH_AKSJONS_TRANS_ID_MAX", nullable = true)
    private Long behandlingAksjonTransIdMax;

    @Column(name = "BEH_VEDTAKS_TRANS_ID_MAX", nullable = true)
    private Long behandlingVedtakTransIdMax;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "LAST_FLAGG")
    private Boolean lastFlagg;

    KontrollDvh() {
        // Hibernate
    }

    private KontrollDvh(Builder builder) {
        this.id = 1L;
        this.fagsakTransIdMax = builder.fagsakTransIdMax;
        this.behandlingTransIdMax = builder.behandlingTransIdMax;
        this.behandllingStegTransIdMax = builder.behandllingStegTransIdMax;
        this.behandlingAksjonTransIdMax = builder.behandlingAksjonTransIdMax;
        this.behandlingVedtakTransIdMax = builder.behandlingVedtakTransIdMax;
        this.lastFlagg = builder.lastFlagg;
    }

    public Long getId() {
        return id;
    }

    public Long getFagsakTransIdMax() {
        return fagsakTransIdMax;
    }

    public Long getBehandlingTransIdMax() {
        return behandlingTransIdMax;
    }

    public Long getBehandllingStegTransIdMax() {
        return behandllingStegTransIdMax;
    }

    public Long getBehandlingAksjonTransIdMax() {
        return behandlingAksjonTransIdMax;
    }

    public Long getBehandlingVedtakTransIdMax() {
        return behandlingVedtakTransIdMax;
    }

    public Boolean getLastFlagg() {
        return lastFlagg;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KontrollDvh)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        KontrollDvh other = (KontrollDvh) obj;
        return Objects.equals(fagsakTransIdMax, other.fagsakTransIdMax)
                && Objects.equals(behandlingTransIdMax, other.behandlingTransIdMax)
                && Objects.equals(behandllingStegTransIdMax, other.behandllingStegTransIdMax)
                && Objects.equals(behandlingAksjonTransIdMax, other.behandlingAksjonTransIdMax)
                && Objects.equals(behandlingVedtakTransIdMax, other.behandlingVedtakTransIdMax)
                && Objects.equals(lastFlagg, other.lastFlagg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fagsakTransIdMax, behandlingTransIdMax, behandllingStegTransIdMax, behandlingAksjonTransIdMax,
                behandlingVedtakTransIdMax, lastFlagg);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long fagsakTransIdMax;
        private Long behandlingTransIdMax;
        private Long behandllingStegTransIdMax;
        private Long behandlingAksjonTransIdMax;
        private Long behandlingVedtakTransIdMax;
        private Boolean lastFlagg;

        public Builder fagsakTransIdMax(Long fagsakTransIdMax) {
            this.fagsakTransIdMax = fagsakTransIdMax;
            return this;
        }

        public Builder behandlingTransIdMax(Long behandlingTransIdMax) {
            this.behandlingTransIdMax = behandlingTransIdMax;
            return this;
        }

        public Builder behandllingStegTransIdMax(Long behandllingStegTransIdMax) {
            this.behandllingStegTransIdMax = behandllingStegTransIdMax;
            return this;
        }

        public Builder behandlingAksjonTransIdMax(Long behandlingAksjonTransIdMax) {
            this.behandlingAksjonTransIdMax = behandlingAksjonTransIdMax;
            return this;
        }

        public Builder behandlingVedtakTransIdMax(Long behandlingVedtakTransIdMax) {
            this.behandlingVedtakTransIdMax = behandlingVedtakTransIdMax;
            return this;
        }

        public Builder lastFlagg(Boolean lastFlagg) {
            this.lastFlagg = lastFlagg;
            return this;
        }

        public KontrollDvh build() {
            return new KontrollDvh(this);
        }
    }
}
