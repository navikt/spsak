package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;

// TODO: Flytt inn i resultat pakken når kun kan hentes fra eget repository

@Entity(name = "Behandlingsresultat")
@Table(name = "BEHANDLING_RESULTAT")
public class Behandlingsresultat extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_RESULTAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne
    @JoinColumn(name = "inngangsvilkar_resultat_id"
    /* , updatable = false // får ikke satt denne til false, men skal aldri kunne endres dersom satt tidligere */
    /* , nullable=false // kan være null, men når den er satt kan ikke oppdateres */
    )
    private VilkårResultat vilkårResultat;

    /* bruker @ManyToOne siden JPA ikke støtter OneToOne join på non-PK column. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "behandlingsresultat")
    private BehandlingVedtak behandlingVedtak;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_resultat_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + BehandlingResultatType.DISCRIMINATOR + "'"))
    private BehandlingResultatType behandlingResultatType = BehandlingResultatType.IKKE_FASTSATT;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "behandlingsresultat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BehandlingsresultatKonsekvensForYtelsen> konsekvenserForYtelsen = new ArrayList<>();

    @OneToMany(mappedBy = "behandlingsresultat")
    private Set<Uttaksperiodegrense> uttaksperiodegrense = new HashSet<>();

    protected Behandlingsresultat() {
        // for hibernate
    }

    public static Behandlingsresultat opprettFor(Behandling behandling) {
        return builder().buildFor(behandling);
    }

    public static Builder builderForInngangsvilkår() {
        return new Builder(VilkårResultat.builder());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderFraEksisterende(Behandlingsresultat behandlingsresultat) {
        return new Builder(behandlingsresultat, false);
    }

    public static Builder builderEndreEksisterende(Behandlingsresultat behandlingsresultat) {
        return new Builder(behandlingsresultat, true);
    }

    public VilkårResultat medOppdatertVilkårResultat(VilkårResultat nyttResultat) {
        if (nyttResultat != null && vilkårResultat != null && nyttResultat != vilkårResultat) {
            if (!nyttResultat.erLik(vilkårResultat)) {
                this.vilkårResultat = nyttResultat;
            }
        } else {
            this.vilkårResultat = nyttResultat;
        }
        return this.vilkårResultat;
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    /**
     * NB: ikke eksponer settere fra modellen. Skal ha package-scope.
     */
    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    public BehandlingVedtak getBehandlingVedtak() {
        return behandlingVedtak;
    }

    public BehandlingResultatType getBehandlingResultatType() {
        return behandlingResultatType;
    }

    public List<KonsekvensForYtelsen> getKonsekvenserForYtelsen() {
        return konsekvenserForYtelsen.stream().map(BehandlingsresultatKonsekvensForYtelsen::getKonsekvensForYtelsen).collect(Collectors.toList());
    }

    public void leggTilUttaksperiodegrense(Uttaksperiodegrense uttaksperiodegrense) {
        this.uttaksperiodegrense.add(uttaksperiodegrense);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Behandlingsresultat)) {
            return false;
        }
        Behandlingsresultat that = (Behandlingsresultat) o;
        // Behandlingsresultat skal p.t. kun eksisterere dersom parent Behandling allerede er persistert.
        // Det syntaktisk korrekte vil derfor være at subaggregat Behandlingsresultat med 1:1-forhold til parent
        // Behandling har også sin id knyttet opp mot Behandling alene.
        return getBehandling().equals(that.getBehandling());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBehandling());
    }

    public boolean isBehandlingHenlagt() {
        return BehandlingResultatType.getAlleHenleggelseskoder().contains(behandlingResultatType);
    }

    public boolean isBehandlingsresultatAvslåttOrOpphørt() {
        return BehandlingResultatType.AVSLÅTT.equals(behandlingResultatType)
            || BehandlingResultatType.OPPHØR.equals(behandlingResultatType);
    }

    public boolean isBehandlingsresultatAvslått() {
        return BehandlingResultatType.AVSLÅTT.equals(behandlingResultatType);
    }

    public boolean isBehandlingsresultatOpphørt() {
        return BehandlingResultatType.OPPHØR.equals(behandlingResultatType);
    }

    public boolean isBehandlingsresultatInnvilget() {
        return BehandlingResultatType.INNVILGET.equals(behandlingResultatType);
    }

    public boolean isBehandlingsresultatEndret() {
        return BehandlingResultatType.FORELDREPENGER_ENDRET.equals(behandlingResultatType);
    }

    public boolean isVilkårAvslått() {
        return VilkårResultatType.AVSLÅTT.equals(vilkårResultat.getVilkårResultatType());
    }

    public static class Builder {

        private Behandlingsresultat behandlingsresultat = new Behandlingsresultat();
        private VilkårResultat.Builder vilkårResultatBuilder;

        Builder(VilkårResultat.Builder builder) {
            this.vilkårResultatBuilder = builder;
        }

        Builder(Behandlingsresultat gammeltResultat, boolean endreEksisterende) {
            if (endreEksisterende) {
                behandlingsresultat = gammeltResultat;
            }
            if (gammeltResultat != null && gammeltResultat.getVilkårResultat() != null) {
                this.vilkårResultatBuilder = VilkårResultat
                    .builderFraEksisterende(gammeltResultat.getVilkårResultat());
            }
        }

        public Builder() {
            // empty builder
        }

        public Builder medBehandlingResultatType(BehandlingResultatType behandlingResultatType) {
            this.behandlingsresultat.behandlingResultatType = behandlingResultatType;
            return this;
        }

        public Builder leggTilKonsekvensForYtelsen(KonsekvensForYtelsen konsekvensForYtelsen) {
            BehandlingsresultatKonsekvensForYtelsen behandlingsresultatKonsekvensForYtelsen = BehandlingsresultatKonsekvensForYtelsen.builder()
                .medKonsekvensForYtelsen(konsekvensForYtelsen).build(behandlingsresultat);
            this.behandlingsresultat.konsekvenserForYtelsen.add(behandlingsresultatKonsekvensForYtelsen);
            return this;
        }

        public Builder fjernKonsekvenserForYtelsen() {
            this.behandlingsresultat.konsekvenserForYtelsen.clear();
            return this;
        }

        public Behandlingsresultat buildFor(Behandling behandling) {
            behandlingsresultat.setBehandling(behandling);
            if (vilkårResultatBuilder != null) {
                VilkårResultat vilkårResultat = vilkårResultatBuilder.buildFor(behandlingsresultat);
                behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
            }
            return behandlingsresultat;
        }
    }
}
