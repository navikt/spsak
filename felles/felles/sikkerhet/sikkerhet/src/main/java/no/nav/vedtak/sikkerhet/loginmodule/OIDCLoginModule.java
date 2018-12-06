package no.nav.vedtak.sikkerhet.loginmodule;

import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.domene.SluttBruker;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.OidcLogin;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * <p>LoginModule that will use an OIDC ID Token and add NAV Principals and Credentials.</p>
 * <p>Depends on either the invoker or another LoginModule in the chain to actually set the SecurityContext.</p>
 *
 * @see org.jboss.security.ClientLoginModule
 */
public class OIDCLoginModule extends LoginModuleBase {

    private static final Logger logger = LoggerFactory.getLogger(OIDCLoginModule.class);
    private static final int AUTHENTICATION_LEVEL_INTERN_BRUKER = 4;

    // Set during initialize()
    private Subject subject;
    private CallbackHandler callbackHandler;

    // Set during login()
    private String ssoToken;
    private SluttBruker sluttBruker;

    // Set during commit()
    private ConsumerId consumerId;
    private AuthenticationLevelCredential authenticationLevelCredential;
    private OidcCredential oidcCredential;

    public OIDCLoginModule() {
        super(logger);
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        logger.trace("Initialize loginmodule");
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        logger.trace("Enter login method");
        ssoToken = getSSOToken();

        String issuer = JwtUtil.getIssuser(ssoToken);
        OidcTokenValidator tokenValidator = OidcTokenValidatorProvider.instance().getValidator(issuer);

        OidcLogin oidcLogin = new OidcLogin(Optional.of(ssoToken), tokenValidator);
        OidcLogin.LoginResult loginResult = oidcLogin.doLogin();
        if (loginResult == OidcLogin.LoginResult.SUCCESS) {
            sluttBruker = SluttBruker.internBruker(oidcLogin.getSubject());
            setLoginSuccess(true);
            logger.trace("Login successful for user {}", sluttBruker);
            return true;
        }
        if (loginResult == OidcLogin.LoginResult.ID_TOKEN_EXPIRED) {
            throw new CredentialExpiredException();
        }
        if (oidcLogin.getErrorMessage() != null) {
            throw new LoginException(oidcLogin.getErrorMessage());
        }
        throw new LoginException(loginResult.name());
    }

    @Override
    public void doCommit() throws LoginException {
        authenticationLevelCredential = new AuthenticationLevelCredential(AUTHENTICATION_LEVEL_INTERN_BRUKER);
        String mdcConsumerId = MDCOperations.getConsumerId();
        if(mdcConsumerId != null){
            this.consumerId = new ConsumerId(mdcConsumerId);
        } else {
            this.consumerId = new ConsumerId();
        }
        oidcCredential = new OidcCredential(ssoToken);

        subject.getPrincipals().add(sluttBruker);
        subject.getPrincipals().add(this.consumerId);
        subject.getPublicCredentials().add(authenticationLevelCredential);
        //TODO (u139158): PK-41761 flyttes til privateCredentials
        subject.getPublicCredentials().add(oidcCredential);

        logger.trace("Login committed for user {}", sluttBruker);
    }

    @Override
    protected void cleanUpSubject() {
        if (!subject.isReadOnly()) {
            subject.getPrincipals().remove(sluttBruker);
            subject.getPrincipals().remove(consumerId);
            subject.getPublicCredentials().remove(authenticationLevelCredential);
            //TODO (u139158): PK-41761 flyttes til privateCredentials
            subject.getPublicCredentials().remove(oidcCredential);
        }
    }

    @Override
    protected void cleanUpLocalState() throws LoginException {
        // Set during login()
        ssoToken = null;

        if (sluttBruker != null) {
            sluttBruker.destroy();
        }
        sluttBruker = null;

        // Set during commit()
        if (consumerId != null) {
            consumerId.destroy();
        }
        consumerId = null;

        if (authenticationLevelCredential != null) {
            authenticationLevelCredential.destroy();
        }
        authenticationLevelCredential = null;

        if (oidcCredential != null) {
            oidcCredential.destroy();
        }
        oidcCredential = null;
    }

    /*
     * Called by login() to acquire the ID Token.
     */
    protected String getSSOToken() throws LoginException {
        logger.trace("Getting the SSO-token from callback");

        if (callbackHandler == null) {
            throw new LoginException("No callbackhandler provided");
        }

        // The prompt will never be seen by the user, we trigger the module by code with "password" set programatically
        PasswordCallback pc = new PasswordCallback("Input ID token", false);

        Callback[] callbacks = {pc};

        String tokenString;
        try {
            callbackHandler.handle(callbacks);
            tokenString = new String(pc.getPassword());
        } catch (IOException | UnsupportedCallbackException e) {
            logger.debug("Error while handling getting token from callbackhandler: ", e);
            LoginException le = new LoginException();
            le.initCause(e);
            throw le;
        }
        return tokenString;
    }

}