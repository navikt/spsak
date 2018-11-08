package no.nav.vedtak.sikkerhet.context;

import org.jboss.security.SecurityContextAssociation;

import javax.security.auth.Subject;

public class JbossSubjectHandler extends SubjectHandler {
    
    public static final String JBOSS_PROPERTY_KEY = "jboss.home.dir";
    
    @Override
    public Subject getSubject() {
        return SecurityContextAssociation.getSubject();
    }

    public static boolean runningOnJboss() {
        return existsInProperties(JBOSS_PROPERTY_KEY);
    }

    private static boolean existsInProperties(String key) {
        return System.getProperties().containsKey(key);
    }
}
