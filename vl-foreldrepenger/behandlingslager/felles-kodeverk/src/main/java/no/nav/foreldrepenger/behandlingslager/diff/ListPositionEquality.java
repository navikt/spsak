package no.nav.foreldrepenger.behandlingslager.diff;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class ListPositionEquality {
    private final Map<Node, AtomicInteger> equalsNodeCounter = new HashMap<>();
    private final Map<Object, NodeWrap> equalsMap = new HashMap<>();

    int getKey(Node node, Object o) {
        AtomicInteger counter = equalsNodeCounter.computeIfAbsent(node, n -> new AtomicInteger());
        return equalsMap.computeIfAbsent(o, v -> new NodeWrap(node, counter.getAndIncrement())).getPos();
    }

    static class NodeWrap {
        private final Node root;
        private final int pos;

        NodeWrap(Node root, int pos) {
            this.root = root;
            this.pos = pos;
        }

        int getPos() {
            return pos;
        }
        
        Node getRoot() {
            return root;
        }
    }
}
