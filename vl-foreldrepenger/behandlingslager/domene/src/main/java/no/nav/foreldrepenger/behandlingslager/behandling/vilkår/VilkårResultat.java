package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
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

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "VilkarResultat")
@Table(name = "VILKAR_RESULTAT")
public class VilkårResultat extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_VILKAR_RESULTAT")
    private Long id;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_resultat", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårResultatType.DISCRIMINATOR + "'"))
    private VilkårResultatType vilkårResultatType = VilkårResultatType.IKKE_FASTSATT;

    // CascadeType.ALL + orphanRemoval=true må til for at Vilkår skal bli slettet fra databasen ved fjerning fra HashSet
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "vilkårResultat")
    private Set<Vilkår> vilkårne = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "original_behandling_id", updatable = false
    /* , nullable=false // får ikke satt false pga binary relasjon mellom Behandling og VilkårResultat */
    )
    private Behandling originalBehandling;

    /**
     * Hvorvidt hele vilkårresultatet er overstyrt av Saksbehandler. (fra SF3).
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "overstyrt", nullable = false)
    private boolean erOverstyrt = false;

    private VilkårResultat() {
        // for hibernate
    }

    public Long getId() {
        return id;
    }

    public VilkårResultatType getVilkårResultatType() {
        return Objects.equals(VilkårResultatType.UDEFINERT, vilkårResultatType) ? null : vilkårResultatType;
    }

    void setVilkårResultatType(VilkårResultatType vilkårResultatType) {
        this.vilkårResultatType = vilkårResultatType == null ? VilkårResultatType.UDEFINERT : vilkårResultatType;
    }

    /**
     * Returnerer kopi liste av vilkår slik at denne ikke kan modifiseres direkte av klåfingrede utviklere.
     */
    public List<Vilkår> getVilkårene() {
        return Collections.unmodifiableList(new ArrayList<>(vilkårne));
    }

    public boolean erOverstyrt() {
        return erOverstyrt;
    }

    void fjernVilkårene(Set<Vilkår> fjernede) {
        vilkårne.removeAll(fjernede);
    }

    void setVilkårene(Set<Vilkår> nyeVilkår) {
        this.vilkårne.clear();
        nyeVilkår.forEach(v -> v.setVilkårResultat(this));
        this.vilkårne.addAll(nyeVilkår);
    }

    void setOriginalBehandlingsresultat(Behandlingsresultat originalBehandlingsresultat) {
        this.originalBehandling = originalBehandlingsresultat.getBehandling();
    }

    Behandlingsresultat getOriginalBehandlingsresultat() {
        return originalBehandling.getBehandlingsresultat();
    }

    /**
     * Original behandling der denne instansen av {@link VilkårResultat} resultat først ble behandlet. Senere
     * endringer som ikke påvirker innngangsvilkårresutlate vil bare gjenbruke denne i et {@link Behandlingsresultat}.
     */
    public Behandling getOriginalBehandling() {
        return originalBehandling;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "id=" + id +
            ", versjon=" + versjon +
            ", vilkårResultatType=" + getVilkårResultatType() +
            ", vilkårne=" + vilkårne +
            ", originalBehandling=" + originalBehandling +
            ", vilkår={" + vilkårne.stream().map(Vilkår::toString).collect(joining("},{")) + "}" +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof VilkårResultat)) {
            return false;
        }
        VilkårResultat other = (VilkårResultat) obj;
        return Objects.equals(getVilkårResultatType(), other.getVilkårResultatType()) &&
            Objects.equals(vilkårne, other.vilkårne);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVilkårResultatType(), getVilkårene());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderFraEksisterende(VilkårResultat eksisterendeResultat) {
        return new Builder(eksisterendeResultat);
    }

    public boolean erLik(VilkårResultat annen) {
        // equals for collections out-of-order
        // diff her, baserer oss ikke på equals da den matcher kun på vilkårtype (som den skal)

        // det gir en deep equals av vilkårene inklusiv alle felter som er mappet til tuple list.
        Map<VilkårType, ?> vilkårThis = toTuples(this.vilkårne);
        Map<VilkårType, ?> vilkårAnnen = toTuples(annen.vilkårne);
        return vilkårThis.equals(vilkårAnnen);
    }

    private static Map<VilkårType, ?> toTuples(Set<Vilkår> vilkårene) {
        // Mapperut hvert vilkår til en liste av verdier for senere sammenligning (tuples) slik at en hver endring
        // vil slå ut som forskjell.
        return vilkårene.stream()
            .map(v -> new SimpleEntry<>(v.getVilkårType(), v.tuples()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static class VilkårUtfall {
        VilkårUtfallType vilkårUtfallType;
        VilkårUtfallMerknad vilkårUtfallMerknad;
        Properties merknadParametere;
        Avslagsårsak avslagsårsak;
        boolean erManueltVurdert;
        boolean erOverstyrt;
        String regelEvaluering;
        String regelInput;
        VilkårUtfallType utfallManuelt;
        VilkårUtfallType utfallOverstyrt;

        VilkårUtfall(VilkårUtfallType vilkårUtfallType, VilkårUtfallMerknad vilkårUtfallMerknad, Properties merknadParametere,
                     Avslagsårsak avslagsårsak, boolean erManueltVurdert, boolean erOverstyrt, RegelInputOgEvaluering inputOgEvaluering,
                     VilkårUtfallType utfallManuelt, VilkårUtfallType utfallOverstyrt) {
            this.vilkårUtfallType = vilkårUtfallType;
            this.vilkårUtfallMerknad = vilkårUtfallMerknad;
            this.merknadParametere = merknadParametere;
            this.avslagsårsak = avslagsårsak;
            this.erManueltVurdert = erManueltVurdert;
            this.erOverstyrt = erOverstyrt;
            this.regelEvaluering = inputOgEvaluering.getRegelEvaluering();
            this.regelInput = inputOgEvaluering.getRegelInput();
            this.utfallManuelt = utfallManuelt;
            this.utfallOverstyrt = utfallOverstyrt;
        }
    }

    /**
     * Builder for å modifisere et vilkårResultat.
     */
    public static class Builder {

        private VilkårResultat resultatKladd = new VilkårResultat();
        private Map<VilkårType, VilkårUtfall> oppdaterteUtfall = new TreeMap<>();
        private Set<VilkårType> fjernedeVilkårTyper = new HashSet<>();
        private Set<Vilkår> opprinneligeVilkår = new HashSet<>();
        private VilkårResultat eksisterendeResultat;
        private VilkårResultatType vilkårResultatType;
        private VilkårUtfallType utfallManuelt = VilkårUtfallType.UDEFINERT;
        private VilkårUtfallType utfallOverstyrt = VilkårUtfallType.UDEFINERT;
        private boolean modifisert;

        Builder() {
            super();
        }

        Builder(VilkårResultat eksisterendeResultat) {
            super();
            this.eksisterendeResultat = eksisterendeResultat;
            if (eksisterendeResultat != null) {
                this.opprinneligeVilkår.addAll(eksisterendeResultat.getVilkårene());
            }
        }

        public Builder leggTilVilkårResultat(VilkårType vilkårType, VilkårUtfallType vilkårUtfall,
                                             VilkårUtfallMerknad vilkårUtfallMerknad, Properties merknadParametere,
                                             Avslagsårsak avslagsårsak, boolean erManueltVurdert,
                                             boolean erOverstyrt, String regelEvaluering, String regelInput) {
            this.modifisert = true;
            RegelInputOgEvaluering inputOgEvaluering = new RegelInputOgEvaluering(regelEvaluering, regelInput);
            if (erManueltVurdert && utfallManuelt.equals(VilkårUtfallType.UDEFINERT)) {
                utfallManuelt = vilkårUtfall;
            }
            if (erOverstyrt && utfallOverstyrt.equals(VilkårUtfallType.UDEFINERT)) {
                utfallOverstyrt = vilkårUtfall;
            }

            this.oppdaterteUtfall.put(vilkårType, new VilkårUtfall(vilkårUtfall, vilkårUtfallMerknad,
                merknadParametere, avslagsårsak, erManueltVurdert, erOverstyrt, inputOgEvaluering, utfallManuelt, utfallOverstyrt));
            return this;
        }

        public Builder leggTilVilkårResultatManueltOppfylt(VilkårType vilkårType) {
            return leggTilVilkårResultat(vilkårType, VilkårUtfallType.OPPFYLT, null, new Properties(), null, true, false, null, null);
        }

        public Builder leggTilVilkårResultatManueltIkkeVurdert(VilkårType vilkårType) {
            return leggTilVilkårResultat(vilkårType, VilkårUtfallType.IKKE_VURDERT, null, new Properties(), null, true, false, null, null);
        }

        public Builder leggTilVilkårResultatManueltIkkeOppfylt(VilkårType vilkårType,
                                                               Avslagsårsak avslagsårsak) {
            return leggTilVilkårResultatManueltIkkeOppfylt(vilkårType, null, avslagsårsak);
        }

        public Builder leggTilVilkårResultatManueltIkkeOppfylt(VilkårType vilkårType, VilkårUtfallMerknad vilkårUtfallMerknad,
                                                               Avslagsårsak avslagsårsak) {
            leggTilVilkårResultat(vilkårType, VilkårUtfallType.IKKE_OPPFYLT, vilkårUtfallMerknad, new Properties(), avslagsårsak, true,
                false, null, null);
            return medVilkårResultatType(VilkårResultatType.AVSLÅTT);
        }

        public Builder leggTilVilkår(VilkårType vilkårType, VilkårUtfallType utfallType) {
            return leggTilVilkårResultat(vilkårType, utfallType, null, null,
                Avslagsårsak.UDEFINERT, false, false, null, null);
        }

        public Builder nullstillVilkår(VilkårType vilkårType, VilkårUtfallType utfallOverstyrt) {
            Builder builder = leggTilVilkårResultat(vilkårType, VilkårUtfallType.IKKE_VURDERT, null, null,
                Avslagsårsak.UDEFINERT, false, false, null, null);

            // Overstyrt utfall må beholdes selv ved nullstilling
            VilkårUtfall vilkårUtfall = oppdaterteUtfall.get(vilkårType);
            // TODO (essv) PKMANTIS-1988 finne bedre måte å sette overstyrt utfall
            vilkårUtfall.utfallOverstyrt = utfallOverstyrt;
            return builder;
        }

        public Builder overstyrVilkår(VilkårType vilkårType, VilkårUtfallType utfallType, Avslagsårsak avslagsårsak) {
            return leggTilVilkårResultat(vilkårType, utfallType, null, null,
                avslagsårsak, true, true, null, null);
        }

        public Builder fjernVilkår(VilkårType vilkårType) {
            this.modifisert = true;
            fjernedeVilkårTyper.add(vilkårType);
            return this;
        }

        public Builder medVilkårResultatType(VilkårResultatType vilkårResultatType) {
            this.modifisert = true;
            Objects.requireNonNull(vilkårResultatType, "vilkårResultatType");
            this.vilkårResultatType = vilkårResultatType;
            return this;
        }

        public Builder medUtfallManuelt(VilkårUtfallType utfallManuelt) {
            this.modifisert = true;
            Objects.requireNonNull(utfallManuelt, "utfallManuelt");
            this.utfallManuelt = utfallManuelt;
            return this;
        }

        public Builder medUtfallOverstyrt(VilkårUtfallType utfallOverstyrt) {
            this.modifisert = true;
            Objects.requireNonNull(utfallOverstyrt, "utfallOverstyrt");
            this.utfallOverstyrt = utfallOverstyrt;
            return this;
        }

        /**
         * Bygg nytt resultat for angitt behandlingsresultat.
         *
         * @return Returner nytt resultat HVIS det opprettes.
         */
        public VilkårResultat buildFor(Behandlingsresultat behandlingsresultat) {
            if (eksisterendeResultat != null
                && Objects.equals(behandlingsresultat.getId(), eksisterendeResultat.getOriginalBehandlingsresultat().getId())) {
                // samme behandling som originalt, oppdaterer original
                oppdaterVilkår(eksisterendeResultat);
                return eksisterendeResultat; // samme som før, så returner null
            } else if (eksisterendeResultat != null && !modifisert) {
                return eksisterendeResultat;
            } else {
                oppdaterVilkår(resultatKladd);
                resultatKladd.setOriginalBehandlingsresultat(behandlingsresultat);
                behandlingsresultat.medOppdatertVilkårResultat(resultatKladd);
                return resultatKladd;
            }
        }

        public VilkårResultat buildFor(Behandling behandling) {
            // Må opprette Behandlingsresultat på Behandling hvis det ikke finnes, før man bygger VilkårResultat
            Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
            if (behandlingsresultat == null) {
                behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
            }
            return buildFor(behandlingsresultat);
        }

        private void oppdaterVilkår(VilkårResultat eksisterende) {
            List<VilkårType> eksisterendeTyper = eksisterende.vilkårne.stream().map(Vilkår::getVilkårType).collect(Collectors.toList());

            fjernVilkårSomSkalFjernes(eksisterende);
            oppdaterVilkårSomSkalOppdateres(eksisterendeTyper);
            leggTilNyeVilkår();

            eksisterende.setVilkårene(opprinneligeVilkår);
            if (vilkårResultatType != null) {
                eksisterende.setVilkårResultatType(vilkårResultatType);
            }
        }

        private void fjernVilkårSomSkalFjernes(VilkårResultat eksisterende) {
            Set<Vilkår> fjernede = opprinneligeVilkår.stream()
                .filter(v -> fjernedeVilkårTyper.stream().anyMatch(fjernet -> fjernet.equals(v.getVilkårType())))
                .collect(toSet());
            opprinneligeVilkår.removeAll(fjernede);
            eksisterende.fjernVilkårene(fjernede);
        }

        private void oppdaterVilkårSomSkalOppdateres(List<VilkårType> eksisterendeTyper) {
            for (Vilkår vilkår : opprinneligeVilkår) {
                for (Map.Entry<VilkårType, VilkårUtfall> entry : oppdaterteUtfall.entrySet()) {
                    if (vilkår.getVilkårType().equals(entry.getKey()) && eksisterendeTyper.contains(entry.getKey())) {
                        mapFraVilkårUtfallTilVilkår(entry.getValue(), vilkår);
                    }
                }
            }
        }

        private void mapFraVilkårUtfallTilVilkår(VilkårUtfall vilkårUtfall, Vilkår vilkår) {
            if (vilkårUtfall.erOverstyrt ) {
                // Sett også samlet inngangsvilkårutfall som overstyrt
                eksisterendeResultat.erOverstyrt = true;
            }
            // Overstyring skal aldri nullstilles, må derfor beholde gammelt utfall dersom uendret
            vilkår.setVilkårUtfallOverstyrt(vilkårUtfall.utfallOverstyrt.equals(VilkårUtfallType.UDEFINERT) ? vilkår.getVilkårUtfallOverstyrt() : vilkårUtfall.utfallOverstyrt);
            vilkår.setVilkårUtfall(vilkårUtfall.vilkårUtfallType);
            vilkår.setVilkårUtfallMerknad(vilkårUtfall.vilkårUtfallMerknad);
            vilkår.setMerknadParametere(vilkårUtfall.merknadParametere);
            vilkår.setAvslagsårsak(vilkårUtfall.avslagsårsak);
            vilkår.setVilkårUtfallManuelt(vilkårUtfall.utfallManuelt);
            vilkår.setRegelEvaluering(vilkårUtfall.regelEvaluering);
            vilkår.setRegelInput(vilkårUtfall.regelInput);
        }

        private void leggTilNyeVilkår() {
            for (Map.Entry<VilkårType, VilkårUtfall> entry : oppdaterteUtfall.entrySet()) {
                if (!opprinneligeVilkår.stream().map(Vilkår::getVilkårType).collect(toList()).contains(entry.getKey())) {
                    VilkårUtfall utfall = entry.getValue();
                    opprinneligeVilkår.add(
                        new VilkårBuilder()
                            .medVilkårType(entry.getKey())
                            .medAvslagsårsak(utfall.avslagsårsak)
                            .medVilkårUtfall(utfall.vilkårUtfallType)
                            .medVilkårUtfallMerknad(utfall.vilkårUtfallMerknad)
                            .medMerknadParametere(utfall.merknadParametere)
                            //.medManueltVurdert(utfall.erManueltVurdert)
                            .medUtfallManuell(utfall.utfallManuelt)
                            //.medErOverstyrt(utfall.erOverstyrt)
                            .medUtfallOverstyrt(utfall.utfallOverstyrt)
                            .medRegelEvaluering(utfall.regelEvaluering)
                            .medRegelInput(utfall.regelInput).build());
                }
            }
        }

    }

    public Optional<Vilkår> hentIkkeOppfyltVilkår() {
        return vilkårne.stream().filter(v -> VilkårUtfallType.IKKE_OPPFYLT.equals(v.getGjeldendeVilkårUtfall())).findFirst();
    }
}
