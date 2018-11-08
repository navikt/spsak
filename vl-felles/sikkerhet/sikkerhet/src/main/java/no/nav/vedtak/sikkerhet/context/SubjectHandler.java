package no.nav.vedtak.sikkerhet.context;

import java.util.Set;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import no.nav.vedtak.konfig.PropertyUtil;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;

public abstract class SubjectHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubjectHandler.class);

    public static final String SUBJECTHANDLER_KEY = "no.nav.modig.core.context.subjectHandlerImplementationClass";
    static final String JBOSS_PROPERTY_KEY = JbossSubjectHandler.JBOSS_PROPERTY_KEY;

    public static SubjectHandler getSubjectHandler() {

        String subjectHandlerImplementationClass;

        if (JbossSubjectHandler.runningOnJboss()) {
            subjectHandlerImplementationClass = JbossSubjectHandler.class.getName();
            logger.debug("Detected running on JBoss Application Server. Using: {}", subjectHandlerImplementationClass);
        } else {
            subjectHandlerImplementationClass = resolveProperty(SUBJECTHANDLER_KEY);
        }

        if (subjectHandlerImplementationClass == null) {
            throw new IllegalStateException("Du kjører på noe annet enn JBoss. Om du kjører i jetty og test " +
                    "må du velge hvilken klasse SubjectHandler som skal brukes. Det kan du gjøre ved å kalls SubjectHanderUtils.useSubjectHandler(klasse) " +
                    "eller ved å sette system property " + SUBJECTHANDLER_KEY + " til klassen du ønsker");
        }

        try {
            Class<?> clazz = Class.forName(subjectHandlerImplementationClass);
            return (SubjectHandler) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Klarte ikke å konfigurere plattformavhengig SubjectHandler", e);
        }
    }

    public abstract Subject getSubject();

    public String getUid() {
        return getUid(getSubject());
    }

    public static String getUid(Subject subject) {
        if (subject  == null) {
            return null;
        }

        SluttBruker sluttBruker = getTheOnlyOneInSet(subject.getPrincipals(SluttBruker.class));
        if (sluttBruker != null) {
            return sluttBruker.getName();
        }

        return null;
    }

    public IdentType getIdentType() {
        if (!hasSubject()) {
            return null;
        }

        SluttBruker sluttBruker = getTheOnlyOneInSet(getSubject().getPrincipals(SluttBruker.class));
        if (sluttBruker != null) {
            return sluttBruker.getIdentType();
        }

        return null;
    }

    public String getInternSsoToken() {
        if (!hasSubject()) {
            return null;
        }
        //TODO (u139158): PK-41761 Flyttes til privateCredentials
        OidcCredential tokenCredential = getTheOnlyOneInSet(getSubject().getPublicCredentials(OidcCredential.class));
        return tokenCredential != null ? tokenCredential.getToken() : null;
    }

    public Element getSamlToken() {
        if(!hasSubject()) {
            return null;
        }
        SAMLAssertionCredential samlCredential = getTheOnlyOneInSet(getSubject().getPublicCredentials(SAMLAssertionCredential.class));
        if (samlCredential != null) {
            return samlCredential.getElement();
        }
        return null;
    }

    public Integer getAuthenticationLevel() {
        if (!hasSubject()) {
            return null;
        }

        AuthenticationLevelCredential authenticationLevelCredential = getTheOnlyOneInSet(getSubject().getPublicCredentials(AuthenticationLevelCredential.class));
        if (authenticationLevelCredential != null) {
            return authenticationLevelCredential.getAuthenticationLevel();
        }

        return null;
    }

    public String getConsumerId() {
        return getConsumerId(getSubject());
    }

    public static String getConsumerId(Subject subject) {
        if (subject == null) {
            return null;
        }

        ConsumerId consumerId = getTheOnlyOneInSet(subject.getPrincipals(ConsumerId.class));
        if (consumerId != null) {
            return consumerId.getConsumerId();
        }

        return null;
    }

    private static <T> T getTheOnlyOneInSet(Set<T> set) {
        if (set.isEmpty()) {
            return null;
        }

        T first = set.iterator().next();
        if (set.size() == 1) {
            return first;
        }

        //logging class names to the log to help debug. Cannot log actual objects,
        //since then ID_tokens may be logged
        Set<String> classNames = set.stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.toSet());
        throw SubjectHandlerFeil.FACTORY.forventet0Eller1(set.size(), classNames).toException();
    }

    private static String resolveProperty(String key) {
        String value = PropertyUtil.getProperty(key);
        if (value != null) {
            logger.debug("Setting {}={} from System.properties", key, LoggerUtils.removeLineBreaks(value));  //NOSONAR
        }
        return value;
    }

    private Boolean hasSubject() {
        return getSubject() != null;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
