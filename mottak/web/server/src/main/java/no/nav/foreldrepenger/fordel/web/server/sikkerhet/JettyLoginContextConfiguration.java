package no.nav.foreldrepenger.fordel.web.server.sikkerhet;


import java.util.Collections;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.security.auth.login.AppConfigurationEntry;

import no.nav.vedtak.sikkerhet.loginmodule.LoginConfigNames;
import no.nav.vedtak.sikkerhet.loginmodule.LoginContextConfiguration;

@Alternative
@Priority(1)
public class JettyLoginContextConfiguration extends LoginContextConfiguration {

    JettyLoginContextConfiguration() {
        replaceConfiguration(LoginConfigNames.SAML.name(), configSaml());
        replaceConfiguration(LoginConfigNames.TASK_OIDC.name(), configTaskOidc());
    }

    private AppConfigurationEntry[] configSaml() {
        return new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.SamlLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                        Collections.emptyMap()),
                new AppConfigurationEntry(
                        "no.nav.foreldrepenger.fordel.web.server.sikkerhet.JettyLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Collections.singletonMap("password-stacking", "useFirstPass"))
        };
    }

    private AppConfigurationEntry[] configTaskOidc() {
        return new AppConfigurationEntry[]{
                new AppConfigurationEntry(
                        "no.nav.vedtak.sikkerhet.loginmodule.OIDCLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                        Collections.emptyMap()),
                new AppConfigurationEntry(
                        "no.nav.foreldrepenger.fordel.web.server.sikkerhet.JettyLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        Collections.singletonMap("password-stacking", "useFirstPass"))
        };
    }
}
