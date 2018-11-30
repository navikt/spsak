package no.nav.foreldrepenger.behandlingslager.behandling;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.vedtak.util.Objects.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;

// Bruker en primitiv variant av Composite for å kunne vurderes enkeltvis (løvnode) og sammensatt (rotnode)
public class EndringsresultatDiff {

    private Long grunnlagId1;
    private Long grunnlagId2;
    private Class<?> grunnlagKlasse;
    private boolean støtterSporingsendringer;
    private boolean erSporedeFeltEndret;
    private DiffResult diffResult = null;

    private List<EndringsresultatDiff> children = emptyList();

    // Brukes som Composite-rotnode
    private EndringsresultatDiff(boolean støtterSporingsendringer) {
        this.grunnlagKlasse = this.getClass(); // rot
        this.støtterSporingsendringer = støtterSporingsendringer;
        children = new ArrayList<>();
    }

    // Brukes som Composite-løvnode
    private EndringsresultatDiff(Class<?> grunnlagKlasse, Long grunnlagId1, Long grunnlagId2, boolean støtterSporingsendringer, boolean erSporedeFeltEndret, DiffResult diffResultat) {
        this.grunnlagKlasse = grunnlagKlasse;
        this.grunnlagId1 = grunnlagId1;
        this.grunnlagId2 = grunnlagId2;
        this.støtterSporingsendringer = støtterSporingsendringer;
        this.erSporedeFeltEndret = erSporedeFeltEndret;
        this.diffResult = diffResultat;
    }

    // Oppretter Composite-rotnode
    public static EndringsresultatDiff opprett() {
        boolean støtterSporingsendringer = false;
        return new EndringsresultatDiff(støtterSporingsendringer);
    }

    public static EndringsresultatDiff opprettForSporingsendringer() {
        boolean støtterSporingsendringer = true;
        return new EndringsresultatDiff(støtterSporingsendringer);
    }

    // Oppretter Composite-løvnode
    public static EndringsresultatDiff medDiff(Class<?> grunnlagKlasse, Long grunnlagId1, Long grunnlagId2) {
        boolean støtterSporingsendringer = false;
        return new EndringsresultatDiff(grunnlagKlasse, grunnlagId1, grunnlagId2, støtterSporingsendringer, false, null);
    }

    public static EndringsresultatDiff medDiffPåSporedeFelt(EndringsresultatDiff diff, boolean erSporedeFeltEndret, DiffResult diffResultat) {
        boolean støtterSporingsendringer = true;
        return new EndringsresultatDiff(diff.grunnlagKlasse, diff.grunnlagId1, diff.grunnlagId2, støtterSporingsendringer, erSporedeFeltEndret, diffResultat);
    }

    public boolean erIdEndret() {
        return !Objects.equals(grunnlagId1, grunnlagId2)  || getChildren().stream().anyMatch(EndringsresultatDiff::erIdEndret);
    }

    public boolean erSporedeFeltEndret() {
        check(støtterSporingsendringer, "Utviklerfeil: ikke satt opp til å støtte sporing på felter"); //$NON-NLS-1$
        return erSporedeFeltEndret  || getChildren().stream().anyMatch(EndringsresultatDiff::erSporedeFeltEndret);
    }

    private List<EndringsresultatDiff> getChildren() {
        return children;
    }

    public List<EndringsresultatDiff> hentDelresultater() {
        return children.isEmpty() ? singletonList(this) : children;
    }

    public Optional<EndringsresultatDiff> hentDelresultat(Class<?> grunnlagKlasse) {
        return getChildren().stream()
            .filter(it -> it.getGrunnlag().equals(grunnlagKlasse))
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    public <C> Class<C> getGrunnlag() {
        return (Class<C>) grunnlagKlasse;
    }

    public EndringsresultatDiff leggTilIdDiff(EndringsresultatDiff endringsresultat) {
        getChildren().add(endringsresultat);
        return this;
    }


    public EndringsresultatDiff leggTilSporetEndring(EndringsresultatDiff endringsresultat, Supplier<DiffResult> sporedeFeltSjekkSupplier) {
        boolean erSporetFeltEndret;
        DiffResult diffResultat = null;
        Long id1 = endringsresultat.getGrunnlagId1();
        Long id2 = endringsresultat.getGrunnlagId2();

        if (Objects.equals(id1, id2)) {
            // Sporede felt kan ikke være endret dersom id-er er like
            erSporetFeltEndret = false;
        } else if ((id1 == null && id2 != null) || (id1 != null && id2 == null)) { // NOSONAR - false positive
            // Grunnlaget har gått fra å ikke eksistere til å eksistere -> antas alltid å være en sporbar endring
            erSporetFeltEndret = true;
        } else {
            // Id-er er forskjellige -> deleger endringssjekk på sporede felt (@ChangeTracked) til domenetjenestene
            diffResultat = sporedeFeltSjekkSupplier.get();
            erSporetFeltEndret = !diffResultat.isEmpty();
        }

        EndringsresultatDiff diff = EndringsresultatDiff.medDiffPåSporedeFelt(endringsresultat, erSporetFeltEndret, diffResultat);
        getChildren().add(diff);
        return this;
    }

    @Override
    public String toString() {
        return "Endringer{" +
            "grunnlagKlasse='" + grunnlagKlasse.getSimpleName() + '\'' +
            ", grunnlagId1=" + grunnlagId1 +
            ", grunnlagId2=" + grunnlagId2 +
            ", erSporedeFeltEndret=" + erSporedeFeltEndret +
            ", førsteFeltOppdatering=" + Optional.ofNullable(diffResult)
                .map(DiffResult::getLeafDifferences)
                .filter(diff -> !diff.isEmpty())
                .map(diff -> diff.entrySet().iterator().next().toString())
                .orElse("ingen_oppdatering") +
            ", antallFeltEndringer=" + Optional.ofNullable(diffResult)
                .map(diff -> diff.getLeafDifferences().size())
                .orElse(0) +
            ", type=" + (children.isEmpty() ? "løvnode" : "rotnode") +
            (children.isEmpty() ? "" : (", children=" + children)) +
            '}' + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndringsresultatDiff)) return false;

        EndringsresultatDiff that = (EndringsresultatDiff) o;

        return Objects.equals(grunnlagKlasse, that.grunnlagKlasse)
            && Objects.equals(grunnlagId1, that.grunnlagId1)
            && Objects.equals(grunnlagId2, that.grunnlagId2)
            && Objects.equals(erSporedeFeltEndret, that.erSporedeFeltEndret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(grunnlagKlasse, grunnlagId1, grunnlagId2, erSporedeFeltEndret);
    }

    public Long getGrunnlagId1() {
        return grunnlagId1;
    }

    public Long getGrunnlagId2() {
        return grunnlagId2;
    }
}
