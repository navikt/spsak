package no.nav.vedtak.sikkerhet.context;

import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;

import javax.security.auth.Subject;

/**
 * <p>
 * A SubjectHandler that holds the Subject in a static field. It also has a default Subject.
 * </p>
 * <p>
 * <p>
 * Use this SubjectHandler if you just need a Subjecthandler and don't care about the Subject or if you need it shared across
 * threads.
 * </p>
 *
 * @see ThreadLocalSubjectHandler
 */
public class StaticSubjectHandler extends TestSubjectHandler {
    private static final Subject DEFAULT_SUBJECT;

    static {
        DEFAULT_SUBJECT = new Subject();
        DEFAULT_SUBJECT.getPrincipals().add(SluttBruker.internBruker("StaticSubjectHandlerUserId"));
        DEFAULT_SUBJECT.getPrincipals().add(new ConsumerId("StaticSubjectHandlerConsumerId"));
        DEFAULT_SUBJECT.getPublicCredentials().add(new AuthenticationLevelCredential(4));
    }

    private static Subject subject = DEFAULT_SUBJECT;

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public void setSubject(Subject newSubject) {
        subject = newSubject;
    }

    /**
     * Sets the Subject to the default Subject
     */
    @Override
    public void reset() {
        setSubject(DEFAULT_SUBJECT);
    }
}