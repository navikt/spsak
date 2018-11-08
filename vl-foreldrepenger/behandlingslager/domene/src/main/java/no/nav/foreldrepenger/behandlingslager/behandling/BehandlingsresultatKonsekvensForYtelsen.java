package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "BehandlingsresultatKonsekvensForYtelsen")
@Table(name = "BR_KONSEKVENS_YTELSE")
public class BehandlingsresultatKonsekvensForYtelsen extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BR_KONSEKVENS_YTELSE")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BEHANDLING_RESULTAT_ID", nullable = false, updatable = false, unique = true)
    private Behandlingsresultat behandlingsresultat;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "konsekvens_ytelse", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + KonsekvensForYtelsen.DISCRIMINATOR + "'"))
    private KonsekvensForYtelsen konsekvensForYtelsen = KonsekvensForYtelsen.UDEFINERT;

    public Long getId() {
        return id;
    }

    public Behandlingsresultat getBehandlingsresultat() {
        return behandlingsresultat;
    }

    KonsekvensForYtelsen getKonsekvensForYtelsen() {
        return konsekvensForYtelsen;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BehandlingsresultatKonsekvensForYtelsen)) {
            return false;
        }
        BehandlingsresultatKonsekvensForYtelsen that = (BehandlingsresultatKonsekvensForYtelsen) o;
        return Objects.equals(behandlingsresultat, that.behandlingsresultat) &&
            Objects.equals(konsekvensForYtelsen, that.konsekvensForYtelsen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingsresultat, konsekvensForYtelsen);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BehandlingsresultatKonsekvensForYtelsen behandlingsresultatKonsekvensForYtelsen;

        public Builder() {
            behandlingsresultatKonsekvensForYtelsen = new BehandlingsresultatKonsekvensForYtelsen();
        }

        BehandlingsresultatKonsekvensForYtelsen.Builder medKonsekvensForYtelsen(KonsekvensForYtelsen konsekvens) {
            behandlingsresultatKonsekvensForYtelsen.konsekvensForYtelsen = konsekvens;
            return this;
        }

        public BehandlingsresultatKonsekvensForYtelsen build(Behandlingsresultat behandlingsresultat) {
            behandlingsresultatKonsekvensForYtelsen.behandlingsresultat = behandlingsresultat;
            return behandlingsresultatKonsekvensForYtelsen;
        }
    }
}
