package no.nav.vedtak.felles.testutilities.cdi;

import java.util.function.Supplier;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionTargetFactory;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.environment.se.Weld;

/**
 * Instantierer Weld (CDI container) for enhetstesting.
 */
public class WeldContext {

    private static WeldContext INSTANCE;

    private final Weld weld;

    public static synchronized WeldContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new WeldContext();
        }
        return INSTANCE;
    }

    public WeldContext() {
        // syntetisk bean
        this.weld = new Weld();
        this.weld.property("org.jboss.weld.se.archive.isolation", false);
        this.weld.initialize();

    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return doWithScope(() -> {
            BeanManager manager = CDI.current().getBeanManager();
            AnnotatedType<T> oat = manager.createAnnotatedType(type);
            BeanAttributes<T> oa = manager.createBeanAttributes(oat);
            InjectionTargetFactory<T> factory = manager.getInjectionTargetFactory(oat);
            Bean<?> bean = manager.createBean(oa, type, factory);

            return (T) bean.create(manager.createCreationalContext(null));
        });
    }

    private RequestContext getContext() {
        return CDI.current().select(RequestContext.class, UnboundLiteral.INSTANCE).get();
    }

    public <V> V doWithScope(Supplier<V> supplier) {
        WeldContext weld = WeldContext.getInstance();

        RequestContext requestContext = weld.getContext();
        if (requestContext.isActive()) {
            return supplier.get();
        } else {
            try {
                requestContext.activate();
                return supplier.get();
            } finally {
                requestContext.invalidate();
                requestContext.deactivate();
            }
        }

    }

}
