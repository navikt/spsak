package no.nav.vedtak.felles.testutilities.sikkerhet;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.vedtak.sikkerhet.context.JbossSubjectHandler;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.IdentType;

public class SubjectHandlerTest {
    
    @Rule
    public MockitoRule rule = MockitoJUnit.rule().silent();

    private static final String USER_ID = "userId";
    private static final IdentType IDENT_TYPE = IdentType.InternBruker;
    private static final int AUTH_LEVEL = 4;
    private static final String DUMMYSTRING = "test";

    @Before
    public void setUpSubjectHandlerAndSubject() throws Exception {
        SubjectHandlerUtils.useSubjectHandler(ThreadLocalSubjectHandler.class);

        SubjectHandlerUtils.setInternBruker(USER_ID);
    }

    @After
    public void clearSubjectHandler() throws Exception {
        SubjectHandlerUtils.reset();
        SubjectHandlerUtils.unsetSubjectHandler();
    }

    @Test
    public void testGetSubjectHandler() throws Exception {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        assertThat(subjectHandler, CoreMatchers.notNullValue());
        assertThat(subjectHandler, CoreMatchers.instanceOf(ThreadLocalSubjectHandler.class));
    }

    @Test
    public void testGetSubjectHandlerNoProperty() throws Exception {
        SubjectHandlerUtils.unsetSubjectHandler();

        try {
            SubjectHandler.getSubjectHandler();
            Assert.fail("hit skal man ikke komme");
        } catch (RuntimeException e) {
            // try-catch er for å forhindre at tearDown kaster runtimeexception
        }
        SubjectHandlerUtils.useSubjectHandler(ThreadLocalSubjectHandler.class);
    }

    @Test
    public void testGetSubjectHandlerRunningOnJBoss() throws Exception {
        System.setProperty(JbossSubjectHandler.JBOSS_PROPERTY_KEY, DUMMYSTRING);

        assertThat(SubjectHandler.getSubjectHandler(), CoreMatchers.instanceOf(JbossSubjectHandler.class));

        System.clearProperty(JbossSubjectHandler.JBOSS_PROPERTY_KEY);
    }

    @Test
    public void testGetSubject() throws Exception {
        SubjectHandler subjectHandler = SubjectHandler.getSubjectHandler();

        assertThat(subjectHandler, CoreMatchers.notNullValue());
        assertThat(subjectHandler.getUid(), CoreMatchers.is(USER_ID));
        assertThat(subjectHandler.getAuthenticationLevel(), CoreMatchers.is(AUTH_LEVEL));
        assertThat(subjectHandler.getIdentType(), CoreMatchers.is(IDENT_TYPE));
        assertThat(subjectHandler.getConsumerId(), CoreMatchers.is(SubjectHandlerUtils.class.getSimpleName()));
    }

}
