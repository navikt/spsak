package no.nav.foreldrepenger.behandlingslager.diff;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static no.nav.foreldrepenger.behandlingslager.diff.YtelseKode.ES;
import static no.nav.foreldrepenger.behandlingslager.diff.YtelseKode.FP;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.threeten.extra.Interval;

import javassist.Modifier;

/**
 * Denne klassen kan traverse en Entity graph og trekk ut verdier som key/value.
 * <p>
 * Genererte verdier, {@link Id}, {@link Version}, {@link GeneratedValue} vil ignoreres.
 */
public class TraverseEntityGraph {

    /** Final klasser som ikke trenger videre forklaring. For raskest oppslag. */
    private static final Set<Class<?>> LEAVES_FINAL = unmodifiableSet(new HashSet<>(
        asList(String.class, Character.class, Character.TYPE,
            Long.class, Double.class, Integer.class, Short.class, Byte.class, Boolean.class,
            Long.TYPE, Double.TYPE, Integer.TYPE, Short.TYPE, Byte.TYPE, Boolean.TYPE,
            BigInteger.class, BigDecimal.class,
            LocalDate.class, LocalDateTime.class, Interval.class, OffsetDateTime.class, ZonedDateTime.class,
            Instant.class)));

    /**
     * Ikke final - men Interfacer/Abstract klasser som fanger store grupper av LEAF objekter (eks. Temporal --
     * LocalDate, Number -- Long, osv).
     */
    private static final Set<Class<?>> LEAVES_EXTENDABLE = unmodifiableSet(new HashSet<>(
        asList(Number.class, Enum.class, TemporalAccessor.class, TemporalAmount.class, TemporalField.class, TraverseValue.class)));

    /** Rot klasser som ikke skal inspiseres i et hierarki. */
    private static final Set<Class<?>> ROOTS_CLASSES = unmodifiableSet(new HashSet<>(
        asList(Object.class)));

    private Set<Class<?>> leafFinalClasses = LEAVES_FINAL;
    private Set<Class<?>> leafExtendableClasses = LEAVES_EXTENDABLE;
    private Set<Class<?>> rootClasses = ROOTS_CLASSES;

    private boolean ignoreNulls;

    private boolean onlyCheckTrackedFields;

    private Set<YtelseKode> ytelseKoder = unmodifiableSet(new HashSet<>(asList(ES, FP)));

    private final ListPositionEquality listPositionEq = new ListPositionEquality();

    public TraverseEntityGraph() {
    }

    public TraverseResult traverse(Object target, String rootName) {
        Node rootNode = new Node(rootName, null, target);
        TraverseResult result = new TraverseResult();
        result.roots.put(rootNode, target);
        traverseDispatch(rootNode, target, result);

        return result;
    }

    public void addRootClasses(Class<?>... moreRootClasses) {
        this.rootClasses = new HashSet<>(this.rootClasses);
        this.rootClasses.addAll(asList(moreRootClasses));
    }

    public void setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
    }

    public void setOnlyCheckTrackedFields(boolean onlyCheckTrackedFields) {
        this.onlyCheckTrackedFields = onlyCheckTrackedFields;
    }

    public void setYtelseKoder(List<YtelseKode> ytelseKoder) {
        this.ytelseKoder = unmodifiableSet(new HashSet<>(ytelseKoder));
    }

    public TraverseResult traverse(Object target) {
        if (target == null) {
            return new TraverseResult();
        }
        return traverse(target, target.getClass().getSimpleName());
    }

    private void traverseRecursiveInternal(Object obj, Node currentPath, TraverseResult result) {
        try {
            if (obj != null && result.cycleDetector.contains(obj)) {
                return;
            } else if (obj == null) {
                if (!ignoreNulls) {
                    result.values.put(currentPath, null);
                }
                return;
            } else if (isLeaf(obj)) {
                result.values.put(currentPath, obj);
                return;
            }

            result.cycleDetector.add(obj);
        } catch (TraverseEntityGraphException t) {
            throw t;
        } catch (RuntimeException e) {
            throw new TraverseEntityGraphException("Kunne ikke lese grafen [" + currentPath + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (obj instanceof Collection) { // NOSONAR
            traverseCollection(currentPath, (Collection<?>) obj, result);
        } else if (obj instanceof Map) { // NOSONAR
            traverseMap(currentPath, (Map<?, ?>) obj, result);
        } else {
            // hånter alt annet (vanlige felter)
            doTraverseRecursiveInternal(currentPath, result, obj);
        }

    }

    private void doTraverseRecursiveInternal(Node currentPath, TraverseResult result, Object obj) {
        Class<?> targetClass = getClassOf(obj);
        validateEntity(currentPath, targetClass);
        if (obj instanceof HibernateProxy) {
            // PKMANTIS-1395 nødvendig for at lazy children av entitet loades
            obj = Hibernate.unproxy(obj);
        }

        Class<?> currentClass = targetClass;

        while (!isRoot(currentClass)) {
            for (final Field field : currentClass.getDeclaredFields()) {
                if (isTraverseField(field)) {
                    Node newPath = new Node(field.getName(), currentPath, obj);
                    try {
                        field.setAccessible(true);
                        Object value = field.get(obj);
                        traverseDispatch(newPath, value, result);
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(String.valueOf(newPath), e);
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }
    }

    private Class<?> getClassOf(Object obj) {
        if (obj instanceof HibernateProxy) {
            return Hibernate.unproxy(obj).getClass();
        }
        return obj.getClass();
    }

    private boolean isTraverseField(final Field field) {
        return (onlyCheckTrackedFields && isMatchingYtelseKode(field))
            || (!onlyCheckTrackedFields && isMappedField(field));
    }

    /**
     * Håndter recursion for Map, Collection eller vanlige verdier. Skaper stabile nøkler i grafen.
     *
     * @param result
     */
    private void traverseDispatch(Node newPath, Object value, TraverseResult result) {
        // en sjelden grei bruk av instanceof. Garantert å håndtere alle varianter pga else til slutt
        if (value instanceof Collection) { // NOSONAR
            traverseCollection(newPath, (Collection<?>) value, result);
        } else if (value instanceof Map) { // NOSONAR
            traverseMap(newPath, (Map<?, ?>) value, result);
        } else {
            // hånter alt annet (vanlige felter)
            traverseRecursiveInternal(value, newPath, result);
        }
    }

    private void traverseMap(Node newPath, Map<?, ?> map, TraverseResult result) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Node collNode = new Node("{" + (entry.getKey()) + "}", newPath, map); //$NON-NLS-1$ //$NON-NLS-2$
            traverseRecursiveInternal(entry.getValue(), collNode, result);
        }
    }

    private void traverseCollection(Node newPath, Collection<?> value, TraverseResult result) {
        for (Object v : value) {
            String collectionKey;
            if (v instanceof IndexKey) {
                collectionKey = ((IndexKey) v).getIndexKey();
            } else {
                collectionKey = String.valueOf(listPositionEq.getKey(newPath, v));
            }

            Node collNode = new Node("[" + (collectionKey) + "]", newPath, v); //$NON-NLS-1$ //$NON-NLS-2$
            traverseRecursiveInternal(v, collNode, result);
        }
    }

    private boolean isLeaf(Object obj) {
        Class<?> targetClass = obj.getClass();
        if (leafFinalClasses.contains(targetClass)) {
            return true;
        } else {
            for (Class<?> leaf : leafExtendableClasses) {
                if (leaf.isAssignableFrom(targetClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRoot(Class<?> cls) {
        return cls == null || rootClasses.contains(cls);
    }

    private static boolean isMappedField(Field fld) {
        return isExpectedField(fld) && !isSkippedFields(fld);
    }

    private static boolean isExpectedField(Field fld) {
        int mods = fld.getModifiers();
        if (Modifier.isFinal(mods) || Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
            return false;
        }

        // følger bare standard, mappede felter i Entity grafen
        return fld.isAnnotationPresent(Column.class)
            || fld.isAnnotationPresent(JoinColumn.class)
            || fld.isAnnotationPresent(OneToOne.class)
            || fld.isAnnotationPresent(ManyToOne.class)
            || fld.isAnnotationPresent(OneToMany.class)
            || fld.isAnnotationPresent(ManyToMany.class)
            || fld.isAnnotationPresent(Embedded.class);
    }

    private static boolean isSkippedFields(Field fld) {
        return fld.isAnnotationPresent(DiffIgnore.class)
            || (fld.isAnnotationPresent(Id.class) && fld.isAnnotationPresent(GeneratedValue.class))
            || fld.isAnnotationPresent(Version.class)
            || fld.isAnnotationPresent(GeneratedValue.class)
            || fld.isAnnotationPresent(Transient.class);
    }

    private boolean isMatchingYtelseKode(Field fld) {
        return isChangeTrackedField(fld) && Arrays.stream(fld.getAnnotation(ChangeTracked.class).ytelser())
            .anyMatch(ytelseKode -> ytelseKoder.contains(ytelseKode));
    }

    private static boolean isChangeTrackedField(Field fld) {
        return fld.isAnnotationPresent(ChangeTracked.class);
    }

    private static void validateEntity(Node currentPath, Class<?> targetClass) {
        boolean ok = targetClass.isAnnotationPresent(Entity.class)
            || targetClass.isAnnotationPresent(Embeddable.class);
        if (!ok) {
            throw new IllegalArgumentException(
                "target [" + targetClass + "] er ikke en Entity eller Embeddable (mangler annotation):" + currentPath); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @SafeVarargs
    public final void addLeafClasses(Class<?>... leafClasses) {
        List<Class<?>> newLeafClasses = asList(leafClasses);

        this.leafExtendableClasses = new HashSet<>(this.leafExtendableClasses);
        this.leafExtendableClasses.addAll(newLeafClasses);

        this.leafFinalClasses = new HashSet<>(this.leafFinalClasses);
        this.leafFinalClasses.addAll(newLeafClasses);

    }

    public static class TraverseResult {
        Map<Node, Object> values = new LinkedHashMap<>();
        Map<Node, Object> roots = new LinkedHashMap<>();
        Set<Object> cycleDetector = Collections.newSetFromMap(new IdentityHashMap<>());

        public Map<Node, Object> getValues() {
            return values;
        }

        public Map<Node, Object> getRoots() {
            return roots;
        }
    }
}
