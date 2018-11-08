package no.nav.foreldrepenger.behandlingslager.behandling.klage;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "KlageVurderingResultat")
@Table(name = "KLAGE_VURDERING_RESULTAT")
public class KlageVurderingResultat extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KLAGE_VURDERING_RESULTAT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "klage_vurdert_av", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KlageVurdertAv.DISCRIMINATOR
                    + "'")) })
    private KlageVurdertAv klageVurdertAv;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "klagevurdering", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KlageVurdering.DISCRIMINATOR
                    + "'")) })
    private KlageVurdering klageVurdering;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "klage_avvist_aarsak", referencedColumnName = "kode")),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KlageAvvistÅrsak.DISCRIMINATOR
                    + "'")) })
    private KlageAvvistÅrsak klageAvvistÅrsak = KlageAvvistÅrsak.UDEFINERT;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "klage_medhold_aarsak", referencedColumnName = "kode")),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KlageMedholdÅrsak.DISCRIMINATOR
                    + "'")) })
    private KlageMedholdÅrsak klageMedholdÅrsak = KlageMedholdÅrsak.UDEFINERT;

    @Column(name = "begrunnelse", nullable = false)
    private String begrunnelse;

    @Column(name = "vedtaksdato_paklagd_behandling", nullable = false)
    private LocalDate vedtaksdatoPåklagdBehandling;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    public KlageVurderingResultat() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public KlageVurdertAv getKlageVurdertAv() {
        return klageVurdertAv;
    }

    public KlageVurdering getKlageVurdering() {
        return klageVurdering;
    }

    public KlageAvvistÅrsak getKlageAvvistÅrsak() {
        return Objects.equals(klageAvvistÅrsak, KlageAvvistÅrsak.UDEFINERT) ? null : klageAvvistÅrsak;
    }

    public KlageMedholdÅrsak getKlageMedholdÅrsak() {
        return Objects.equals(klageMedholdÅrsak, KlageMedholdÅrsak.UDEFINERT) ? null : klageMedholdÅrsak;
    }

    public String getBegrunnelse() {
        return begrunnelse;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof KlageVurderingResultat)) {
            return false;
        }
        KlageVurderingResultat other = (KlageVurderingResultat) obj;
        return Objects.equals(this.getKlageVurdertAv(), other.getKlageVurdertAv())
                && Objects.equals(this.getKlageVurdering(), other.getKlageVurdering())
                && Objects.equals(this.getKlageAvvistÅrsak(), other.getKlageAvvistÅrsak())
                && Objects.equals(this.getKlageMedholdÅrsak(), other.getKlageMedholdÅrsak())
                && Objects.equals(this.begrunnelse, other.begrunnelse);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKlageVurdertAv(), getKlageVurdering(), getKlageAvvistÅrsak(), begrunnelse);
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDate getVedtaksdatoPåklagdBehandling() {
        return vedtaksdatoPåklagdBehandling;
    }

    public static class Builder {
        private KlageVurderingResultat klageVurderingResultatMal;

        public Builder() {
            klageVurderingResultatMal = new KlageVurderingResultat();
        }

        public Builder medKlageVurdertAv(KlageVurdertAv klageVurdertAv) {
            klageVurderingResultatMal.klageVurdertAv = klageVurdertAv;
            return this;
        }

        public Builder medKlageVurdering(KlageVurdering klageVurdering) {
            klageVurderingResultatMal.klageVurdering = klageVurdering;
            return this;
        }

        public Builder medKlageAvvistÅrsak(KlageAvvistÅrsak klageAvvistÅrsak) {
            klageVurderingResultatMal.klageAvvistÅrsak = klageAvvistÅrsak == null ? KlageAvvistÅrsak.UDEFINERT : klageAvvistÅrsak;
            return this;
        }

        public Builder medKlageMedholdÅrsak(KlageMedholdÅrsak klageMedholdÅrsak) {
            klageVurderingResultatMal.klageMedholdÅrsak = klageMedholdÅrsak == null ? KlageMedholdÅrsak.UDEFINERT : klageMedholdÅrsak;
            return this;
        }

        public Builder medBegrunnelse(String begrunnelse) {
            klageVurderingResultatMal.begrunnelse = begrunnelse;
            return this;
        }

        public Builder medVedtaksdatoPåklagdBehandling(LocalDate vedtaksdatoPåklagdBehandling) {
            klageVurderingResultatMal.vedtaksdatoPåklagdBehandling = vedtaksdatoPåklagdBehandling;
            return this;
        }


        public Builder medBehandling(Behandling behandling) {
            klageVurderingResultatMal.behandling = behandling;
            return this;
        }

        public KlageVurderingResultat build() {
            verifyStateForBuild();
            klageVurderingResultatMal.behandling.leggTilKlageVurderingResultat(klageVurderingResultatMal);
            return klageVurderingResultatMal;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(klageVurderingResultatMal.klageVurdertAv, "klageVurdertAv");
            Objects.requireNonNull(klageVurderingResultatMal.klageVurdering, "klageVurdering");
            Objects.requireNonNull(klageVurderingResultatMal.begrunnelse, "begrunnelse");
            Objects.requireNonNull(klageVurderingResultatMal.behandling, "behandling");
            Objects.requireNonNull(klageVurderingResultatMal.vedtaksdatoPåklagdBehandling, "vedtaksdatoPåklagdBehandling");
            if (klageVurderingResultatMal.klageVurdering.equals(KlageVurdering.AVVIS_KLAGE)) {
                Objects.requireNonNull(klageVurderingResultatMal.klageAvvistÅrsak, "klageAvvistÅrsak");
            }
            if (klageVurderingResultatMal.klageVurdering.equals(KlageVurdering.MEDHOLD_I_KLAGE)) {
                Objects.requireNonNull(klageVurderingResultatMal.klageMedholdÅrsak, "klageMedholdÅrsak");
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + //$NON-NLS-1$
                (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "klageVurdertAv=" + getKlageVurdertAv() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "klageVurdering=" + getKlageVurdering() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "klageAvvistÅrsak=" + getKlageAvvistÅrsak() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "klageMedholdÅrsak=" + getKlageMedholdÅrsak() + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + "begrunnelse=" + begrunnelse + ", " //$NON-NLS-1$ //$NON-NLS-2$
                + ">"; //$NON-NLS-1$
    }
}
