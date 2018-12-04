package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Type;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;

@Entity(name = "Behandlingsresultat")
@Table(name = "BEHANDLING_RESULTAT")
public class Behandlingsresultat extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_RESULTAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false, columnDefinition = "NUMERIC", length = 19)
    private long versjon;

    @ManyToOne
    @JoinColumn(name = "inngangsvilkar_resultat_id"
        /* , updatable = false // får ikke satt denne til false, men skal aldri kunne endres dersom satt tidligere */
        /* , nullable=false // kan være null, men når den er satt kan ikke oppdateres */
    )
    private VilkårResultat vilkårResultat;

    @ManyToOne()
    @JoinColumn(name = "beregning_resultat_id"
        /* , updatable = false // får ikke satt denne til false, men skal aldri kunne endres dersom satt tidligere */
        /* , nullable=false // kan være null, men når den er satt kan ikke oppdateres */
    )
    private BeregningResultat beregningResultat;

    /* bruker @ManyToOne siden JPA ikke støtter OneToOne join på non-PK column. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "behandlingsresultat")
    private BehandlingVedtak behandlingVedtak;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_resultat_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + BehandlingResultatType.DISCRIMINATOR + "'"))
    private BehandlingResultatType behandlingResultatType = BehandlingResultatType.IKKE_FASTSATT;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "avslag_arsak", referencedColumnName = "kode"))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Avslagsårsak.DISCRIMINATOR + "'"))
    private Avslagsårsak avslagsårsak = Avslagsårsak.UDEFINERT;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "retten_til", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + RettenTil.DISCRIMINATOR + "'"))
    private RettenTil rettenTil = RettenTil.UDEFINERT;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "behandlingsresultat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BehandlingsresultatKonsekvensForYtelsen> konsekvenserForYtelsen = new ArrayList<>();

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "vedtaksbrev", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'"
        + Vedtaksbrev.DISCRIMINATOR + "'"))
    private Vedtaksbrev vedtaksbrev = Vedtaksbrev.UDEFINERT;

    @Column(name = "avslag_arsak_fritekst")
    private String avslagarsakFritekst;

    @OneToMany(mappedBy = "behandlingsresultat")
    private Set<Uttaksperiodegrense> uttaksperiodegrense = new HashSet<>();

    @Column(name = "overskrift")
    private String overskrift;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "fritekstbrev")
    private String fritekstbrev;

    protected Behandlingsresultat() {
        // for hibernate
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

    public BeregningResultat medOppdatertBeregningResultat(BeregningResultat nyttResultat) {
        if (nyttResultat != null && beregningResultat != null && nyttResultat != beregningResultat) {
            if (!nyttResultat.erLik(beregningResultat)) {
                this.beregningResultat = nyttResultat;
            }
        } else {
            this.beregningResultat = nyttResultat;
        }
        return this.beregningResultat;
    }

    public Long getId() {
        return id;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public VilkårResultat getVilkårResultat() {
        return vilkårResultat;
    }

    public BeregningResultat getBeregningResultat() {
        return beregningResultat;
    }

    /**
     * NB: ikke eksponer settere fra modellen. Skal ha package-scope.
     */
    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    public BehandlingVedtak getBehandlingVedtak() {
        return behandlingVedtak;
    }

    public BehandlingResultatType getBehandlingResultatType() {
        return behandlingResultatType;
    }

    public Avslagsårsak getAvslagsårsak() {
        return Objects.equals(avslagsårsak, Avslagsårsak.UDEFINERT) ? null : avslagsårsak;
    }

    public void setAvslagsårsak(Avslagsårsak avslagsårsak) {
        this.avslagsårsak = Optional.ofNullable(avslagsårsak).orElse(Avslagsårsak.UDEFINERT);
    }

    public String getAvslagarsakFritekst() {
        return avslagarsakFritekst;
    }

    public void setAvslagarsakFritekst(String avslagarsakFritekst) {
        this.avslagarsakFritekst = avslagarsakFritekst;
    }

    public String getOverskrift() {
        return overskrift;
    }

    public String getFritekstbrev() {
        return fritekstbrev;
    }

    public RettenTil getRettenTil() {
        return rettenTil;
    }

    public List<KonsekvensForYtelsen> getKonsekvenserForYtelsen() {
        return konsekvenserForYtelsen.stream().map(BehandlingsresultatKonsekvensForYtelsen::getKonsekvensForYtelsen).collect(Collectors.toList());
    }

    public Vedtaksbrev getVedtaksbrev() {
        return vedtaksbrev;
    }

    public void leggTilUttaksperiodegrense(Uttaksperiodegrense uttaksperiodegrense) {
        this.uttaksperiodegrense.add(uttaksperiodegrense);
    }

    public Set<Uttaksperiodegrense> getAlleUttaksperiodegrenser() {
        return Collections.unmodifiableSet(uttaksperiodegrense);
    }

    public Optional<Uttaksperiodegrense> getGjeldendeUttaksperiodegrense() {
        return uttaksperiodegrense.stream().filter(Uttaksperiodegrense::getErAktivt).findFirst();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<>";
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

    public static Builder builderForBeregningResultat() {
        return new Builder(BeregningResultat.builder());
    }

    public static Builder builderFraEksisterende(Behandlingsresultat behandlingsresultat) {
        return new Builder(behandlingsresultat, false);
    }

    public static Builder builderEndreEksisterende(Behandlingsresultat behandlingsresultat) {
        return new Builder(behandlingsresultat, true);
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

    public static class Builder {

        private Behandlingsresultat behandlingsresultat = new Behandlingsresultat();
        private BeregningResultat.Builder beregningResultatBuilder;
        private VilkårResultat.Builder vilkårResultatBuilder;

        Builder(VilkårResultat.Builder builder) {
            this.vilkårResultatBuilder = builder;
        }

        Builder(BeregningResultat.Builder builder) {
            this.beregningResultatBuilder = builder;
        }

        Builder(Behandlingsresultat gammeltResultat, boolean endreEksisterende) {
            if (endreEksisterende) {
                behandlingsresultat = gammeltResultat;
            }
            if (gammeltResultat != null && gammeltResultat.getVilkårResultat() != null) {
                this.vilkårResultatBuilder = VilkårResultat
                    .builderFraEksisterende(gammeltResultat.getVilkårResultat());
            }
            if (gammeltResultat != null && gammeltResultat.getBeregningResultat() != null) {
                this.beregningResultatBuilder = BeregningResultat
                    .builderFraEksisterende(gammeltResultat.getBeregningResultat());
            }
        }

        public Builder() {
            // empty builder
        }

        public Builder medBehandlingResultatType(BehandlingResultatType behandlingResultatType) {
            this.behandlingsresultat.behandlingResultatType = behandlingResultatType;
            return this;
        }

        public Builder medRettenTil(RettenTil rettenTil) {
            this.behandlingsresultat.rettenTil = rettenTil;
            return this;
        }

        public Builder leggTilKonsekvensForYtelsen(KonsekvensForYtelsen konsekvensForYtelsen) {
            BehandlingsresultatKonsekvensForYtelsen behandlingsresultatKonsekvensForYtelsen =
                BehandlingsresultatKonsekvensForYtelsen.builder().medKonsekvensForYtelsen(konsekvensForYtelsen).build(behandlingsresultat);
            this.behandlingsresultat.konsekvenserForYtelsen.add(behandlingsresultatKonsekvensForYtelsen);
            return this;
        }

        public Builder fjernKonsekvenserForYtelsen() {
            this.behandlingsresultat.konsekvenserForYtelsen.clear();
            return this;
        }

        public Builder medVedtaksbrev(Vedtaksbrev vedtaksbrev) {
            this.behandlingsresultat.vedtaksbrev = vedtaksbrev;
            return this;
        }

        public Builder medAvslagsårsak(Avslagsårsak avslagsårsak) {
            this.behandlingsresultat.avslagsårsak = Optional.ofNullable(avslagsårsak).orElse(Avslagsårsak.UDEFINERT);
            return this;
        }

        public Builder medAvslagarsakFritekst(String avslagarsakFritekst) {
            this.behandlingsresultat.avslagarsakFritekst = avslagarsakFritekst;
            return this;
        }

        public Builder medOverskrift(String overskrift) {
            this.behandlingsresultat.overskrift = overskrift;
            return this;
        }

        public Builder medFritekstbrev(String fritekstbrev) {
            this.behandlingsresultat.fritekstbrev = fritekstbrev;
            return this;
        }

        public Behandlingsresultat buildFor(Behandling behandling) {
            behandling.setBehandlingresultat(behandlingsresultat);
            if (vilkårResultatBuilder != null) {
                VilkårResultat vilkårResultat = vilkårResultatBuilder.buildFor(behandlingsresultat);
                behandlingsresultat.medOppdatertVilkårResultat(vilkårResultat);
            }
            if (beregningResultatBuilder != null) {
                BeregningResultat beregningResultat = beregningResultatBuilder.buildFor(behandling);
                behandlingsresultat.medOppdatertBeregningResultat(beregningResultat);
            }
            return behandlingsresultat;
        }
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

    public boolean isBehandlingsresultatForeldrepengerEndret() {
        return BehandlingResultatType.FORELDREPENGER_ENDRET.equals(behandlingResultatType);
    }

    public boolean isVilkårAvslått() {
        return VilkårResultatType.AVSLÅTT.equals(vilkårResultat.getVilkårResultatType());
    }
}
