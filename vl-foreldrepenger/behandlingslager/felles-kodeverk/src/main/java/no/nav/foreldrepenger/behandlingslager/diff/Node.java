package no.nav.foreldrepenger.behandlingslager.diff;

import java.util.Objects;

/**
 * Representere tre-struktur vha. Lenket liste, der hver node peker tilbake mot rot av treet. Svært effektivt for
 * bygge unike stier uten å bruke mye minne eller tid.
 */
public class Node implements Comparable<Node>{
    Node parent;
    String localName;
    String fullName;
    Object object;

    private int cachedHashCode = -1;

    Node(String localName, Node parent, Object object) {
        this.localName = localName;
        this.parent = parent;
        this.object = object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof Node)) {
            return false;
        }
        Node other = (Node) obj;
        return Objects.equals(localName, other.localName)
                && Objects.equals(parent, other.parent);
    }

    @Override
    public int hashCode() {
        // immutable, så kan beregne en gang.
        if (cachedHashCode == -1) {
            cachedHashCode = Objects.hash(localName, parent);
        }
        return cachedHashCode;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public String getLocalName() {
        return this.localName;
    }

    public Node getParent() {
        return parent;
    }
    
    public String getFullName() {
        if (fullName == null) {
            if (parent == null) {
                fullName = getLocalName();
            } else {
                fullName = String.join(".", parent.getFullName(), getLocalName());
            }
        }
        return fullName;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public int compareTo(Node o) {
        return getFullName().compareTo(o.getFullName());
    }
}
