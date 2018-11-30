package no.nav.foreldrepenger.behandlingslager.behandling;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.Node;
import no.nav.foreldrepenger.behandlingslager.diff.Pair;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;

public class RegisterdataDiffsjekker {
    private DiffEntity diffEntity;
    private TraverseEntityGraph traverseEntityGraph;

    public RegisterdataDiffsjekker() {
        this(false);
    }
    
    public RegisterdataDiffsjekker(YtelseKode ytelseKode) {
        this(ytelseKode, true);
    }

    public RegisterdataDiffsjekker(boolean onlyCheckTrackedFields) {
        this(null, onlyCheckTrackedFields);
    }
    
    public RegisterdataDiffsjekker(YtelseKode ytelseKode, boolean onlyCheckTrackedFields) {
        traverseEntityGraph = TraverseEntityGraphFactory.build(onlyCheckTrackedFields, ytelseKode);
        diffEntity = new DiffEntity(traverseEntityGraph);
    }
    
    public  <T extends Comparable<? super T>> boolean erForskjellPå(List<T> list1, List<T> list2) {
        Map<Node, Pair> leafDifferences = finnForskjellerPåLister(list1, list2);
        return leafDifferences.size() > 0;
    }

    public <T extends Comparable<? super T>> Map<Node, Pair> finnForskjellerPåLister(List<T> list1, List<T> list2) {
        Collections.sort(list1);
        Collections.sort(list2);
        return finnForskjellerPå(list1, list2);
    }
    
    public boolean erForskjellPå(Object object1, Object object2) {
        return !finnForskjellerPå(object1, object2).isEmpty();
    }

    public Map<Node, Pair> finnForskjellerPå(Object object1, Object object2) {
        DiffResult diff = diffEntity.diff(object1, object2);
        return diff.getLeafDifferences();
    }

    public DiffEntity getDiffEntity() {
        return diffEntity;
    }

    public static Optional<Boolean> eksistenssjekkResultat(Optional<?> eksisterende, Optional<?> nytt) {
        if (!eksisterende.isPresent() && !nytt.isPresent()) {
            return Optional.of(Boolean.FALSE);
        }
        if (eksisterende.isPresent() && !nytt.isPresent()) {
            return Optional.of(Boolean.TRUE);
        }
        if (!eksisterende.isPresent() && nytt.isPresent()) { // NOSONAR - "redundant" her er false pos.
            return Optional.of(Boolean.TRUE);
        }
        return Optional.empty();
    }
}
