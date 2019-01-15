package no.nav.foreldrepenger.web.app.util;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapUtil {

    private static final Logger logger = LoggerFactory.getLogger(LdapUtil.class);

    private LdapUtil() {
        // SONAR - Add a private constructor to hide the implicit public one
    }

    public static Collection<String> filtrerGrupper(Collection<String> grupper) {
        return grupper.stream()
            .map(LdapUtil::filterDNtoCNvalue)
            .collect(Collectors.toList());
    }

    private static String filterDNtoCNvalue(String value) {
        if(value.toLowerCase(Locale.ROOT).contains("cn=")) {
            try {
                LdapName ldapname = new LdapName(value); //NOSONAR, only used locally
                for (Rdn rdn : ldapname.getRdns()) {
                    if ("CN".equalsIgnoreCase(rdn.getType())) {
                        String cn = rdn.getValue().toString();
                        logger.debug("uid on DN form. Filtered from {} to {}", value, cn); //NOSONAR trusted source, validated SAML-token or LDAP memberOf
                        return cn;
                    }
                }
            } catch (InvalidNameException e) { //NOSONAR
                logger.debug("value not on DN form. Skipping filter. {}", e.getExplanation()); //NOSONAR trusted source
            }
        }
        return value;
    }
}
