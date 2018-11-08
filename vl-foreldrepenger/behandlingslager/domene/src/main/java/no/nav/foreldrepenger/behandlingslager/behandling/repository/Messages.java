package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "no.nav.foreldrepenger.behandlingslager.behandling.repository.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) { // NOSONAR
            return '!' + key + '!'; // NOSONAR
        }
    }
}
