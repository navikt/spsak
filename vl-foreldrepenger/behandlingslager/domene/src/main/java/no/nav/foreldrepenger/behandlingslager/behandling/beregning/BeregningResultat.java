package no.nav.foreldrepenger.behandlingslager.behandling.beregning;

import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "BeregningResultat")
@Table(name = "BEREGNING_RESULTAT")
public class BeregningResultat extends BaseEntitet {

    @OneToMany(mappedBy = "beregningResultat")
    private Set<Beregning> beregninger = new HashSet<>();

    /**
     * Hvorvidt hele beregningsresultatet er overstyrt av Saksbehandler. (fra SF3).
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "overstyrt", nullable = false)
    private boolean overstyrt = false;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEREGNING_RESULTAT")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "original_behandling_id", nullable = false, updatable = false)
    private Behandling originalBehandling;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public BeregningResultat(Behandling originalBehandling) {
        this.originalBehandling = originalBehandling;
    }

    private BeregningResultat() {
        // for hibernate
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BeregningResultat)) {
            return false;
        }
        BeregningResultat other = (BeregningResultat) obj;
        return Objects.equals(beregninger, other.beregninger);
    }

    public boolean erLik(BeregningResultat annen) {
        // equals for collections out-of-order
        return new HashSet<>(this.getBeregninger()).equals(new HashSet<>(annen.getBeregninger()));
    }

    public boolean isOverstyrt() {
        return overstyrt;
    }

    // Returnerer immuterbar liste
    public List<Beregning> getBeregninger() {
        return new ArrayList<>(beregninger);
    }

    public Optional<Beregning> getSisteBeregning() {
        return beregninger.stream()
            .sorted((b1, b2) -> b2.getBeregnetTidspunkt().compareTo(b1.getBeregnetTidspunkt()))
            .findFirst();
    }

    public Long getId() {
        return id;
    }

    public Behandling getOriginalBehandling() {
        return originalBehandling;
    }

    public Behandlingsresultat getOriginalBehandlingsresultat() {
        return originalBehandling.getBehandlingsresultat();
    }

    @Override
    public int hashCode() {
        return Objects.hash(beregninger);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<>";
    }

    void setBeregninger(Set<Beregning> beregninger) {
        this.beregninger = beregninger;
    }

    void setOriginalBehandlingsresultat(Behandlingsresultat originalBehandlingsresultat) {
        this.originalBehandling = originalBehandlingsresultat.getBehandling();
    }

    void setOverstyrt(boolean overstyrt) {
        this.overstyrt = overstyrt;
    }

    /**
     * Builder for å modifisere et beregningResultat.
     */
    public static class Builder {

        private BeregningResultat eksisterendeResultat;
        private boolean modifisert;
        private Set<Beregning> oppdaterteBeregninger = new HashSet<>();
        private Set<Beregning> originaleBeregninger = new HashSet<>();
        private BeregningResultat resultatMal = new BeregningResultat();
        private boolean overstyrt;

        Builder() {
            super();
        }

        Builder(BeregningResultat eksisterendeResultat) {
            super();
            this.eksisterendeResultat = eksisterendeResultat;
            if (eksisterendeResultat != null) {
                this.originaleBeregninger.addAll(eksisterendeResultat.getBeregninger());
            }
        }

        public BeregningResultat buildFor(Behandling behandling) {
            // Må opprette Behandlingsresultat på Behandling hvis det ikke finnes, før man bygger BeregningResultat
            Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
            if (behandlingsresultat == null) {
                behandlingsresultat = Behandlingsresultat.builderForBeregningResultat().buildFor(behandling);
            }
            return buildFor(behandlingsresultat);
        }

        public BeregningResultat.Builder medBeregning(Beregning beregning) {
            this.modifisert = true;
            this.oppdaterteBeregninger.add(beregning);
            this.overstyrt = beregning.isOverstyrt();
            return this;
        }

        public BeregningResultat.Builder nullstillBeregninger() {
            this.originaleBeregninger.clear();
            return this;
        }

        /**
         * Bygg nytt resultat for angitt behandlingsresultat.
         *
         * @return Returner nytt resultat HVIS det oprettes.
         */
        private BeregningResultat buildFor(Behandlingsresultat behandlingsresultat) {
            if (eksisterendeResultat != null
                && Objects.equals(behandlingsresultat.getId(), eksisterendeResultat.getOriginalBehandlingsresultat().getId())) {
                // samme behandling som originalt, oppdaterer original
                oppdaterBeregninger(eksisterendeResultat);
                return eksisterendeResultat; // samme som før, så returner null
            } else if (eksisterendeResultat != null && !modifisert) {
                return eksisterendeResultat;
            } else {
                oppdaterBeregninger(resultatMal);
                resultatMal.setOriginalBehandlingsresultat(behandlingsresultat);
                behandlingsresultat.medOppdatertBeregningResultat(resultatMal);
                return resultatMal;
            }
        }

        private void oppdaterBeregninger(BeregningResultat resultat) {
            Set<Beregning> nye = oppdaterteBeregninger.stream()
                .map(beregning -> new Beregning(resultat, beregning.getSatsVerdi(), beregning.getAntallBarn(),
                                                beregning.getBeregnetTilkjentYtelse(), beregning.getBeregnetTidspunkt(), beregning.isOverstyrt(), beregning.getOpprinneligBeregnetTilkjentYtelse()))
                .collect(toSet());
            Set<Beregning> urørte = this.originaleBeregninger.stream()
                .filter(beregning -> !oppdaterteBeregninger.contains(beregning))
                .collect(toSet());
            nye.addAll(urørte);

            resultat.setBeregninger(nye);
            resultat.setOverstyrt(this.overstyrt);
        }
    }

    public static BeregningResultat.Builder builder() {
        return new BeregningResultat.Builder();
    }

    public static BeregningResultat.Builder builderFraEksisterende(BeregningResultat eksisterendeResultat) {
        return new BeregningResultat.Builder(eksisterendeResultat);
    }
}
