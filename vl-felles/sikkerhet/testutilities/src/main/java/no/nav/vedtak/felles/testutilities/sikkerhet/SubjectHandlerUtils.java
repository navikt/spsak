package no.nav.vedtak.felles.testutilities.sikkerhet;

import javax.security.auth.Subject;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.IdentType;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;

/**
 * Utilityclass that provides support for populating and resetting TestSubjectHandlers.
 */
public class SubjectHandlerUtils {

    /**
     * @see TestSubjectHandler#reset()
     */
    public static void reset() {
        ((TestSubjectHandler) SubjectHandler.getSubjectHandler()).reset();
    }

    public static void setInternBruker(String userId) {
        setSubject(new SubjectBuilder(userId, IdentType.InternBruker).getSubject());
    }

    public static void setSubject(Subject subject) {
        ((TestSubjectHandler) SubjectHandler.getSubjectHandler()).setSubject(subject);
    }

    public static void useSubjectHandler(Class<? extends SubjectHandler> subjectHandlerClass) {
        System.setProperty(SubjectHandler.SUBJECTHANDLER_KEY, subjectHandlerClass.getName());
    }

    public static void unsetSubjectHandler() {
        System.clearProperty(SubjectHandler.SUBJECTHANDLER_KEY);
    }

    public static class SubjectBuilder {
        private String userId;
        private IdentType identType;
        private int authLevel;

        public SubjectBuilder(String userId, IdentType identType) {
            this.userId = userId;
            this.identType = identType;
            if (IdentType.InternBruker.equals(identType)) {
                authLevel = 4;
            }
        }

        public SubjectBuilder withAuthLevel(int authLevel) {
            this.authLevel = authLevel;
            return this;
        }

        public Subject getSubject() {
            Subject subject = new Subject();
            subject.getPrincipals().add(new SluttBruker(userId, identType));
            subject.getPublicCredentials().add(new AuthenticationLevelCredential(authLevel));
            subject.getPrincipals().add(new ConsumerId(SubjectHandlerUtils.class.getSimpleName()));
            return subject;
        }
    }
}
