package no.nav.vedtak.felles.integrasjon.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.LimitExceededException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;

public class LdapBrukeroppslagTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    LdapContext context = Mockito.mock(LdapContext.class);
    LdapName baseSearch = new LdapName("ou=ServiceAccounts,dc=test,dc=local");

    public LdapBrukeroppslagTest() throws InvalidNameException {
    }

    @Test
    public void skal_liste_ut_brukernavn_når_det_er_i_resultatet() throws Exception {
        BasicAttributes attributes = new BasicAttributes();
        attributes.put("displayName", "Lars Saksbehandler");
        attributes.put("cn", "L999999");
        attributes.put(new BasicAttribute("memberOf"));
        SearchResult resultat = new SearchResult("CN=L999999,OU=ApplAccounts", null, attributes);

        LdapBrukeroppslag ldap = new LdapBrukeroppslag(context, baseSearch);
        assertThat(ldap.getDisplayName(resultat)).isEqualTo("Lars Saksbehandler");
    }

    @Test
    public void skal_liste_ut_gruppene_når_det_er_i_resultatet() throws Exception {
        BasicAttributes attributes = new BasicAttributes();
        attributes.put("displayName", "Lars Saksbehandler");
        attributes.put("cn", "L999999");
        BasicAttribute memberOf = new BasicAttribute("memberOf");
        memberOf.add("CN=myGroup");
        memberOf.add("OU=ourGroup");
        attributes.put(memberOf);
        SearchResult resultat = new SearchResult("CN=L999999,OU=ApplAccounts", null, attributes);

        LdapBrukeroppslag ldap = new LdapBrukeroppslag(null, null);
        assertThat(ldap.getMemberOf(resultat)).contains("CN=myGroup", "OU=ourGroup");
    }

    @Test
    public void skal_gi_exception_når_søket_gir_ingen_treff() throws Exception {
        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("F-418891:Fikk ingen treff på søk mot LDAP etter ident L999999");

        SearchMock heleResultatet = new SearchMock(Collections.emptyList());
        Mockito.when(context.search(ArgumentMatchers.eq(baseSearch), ArgumentMatchers.eq("(cn=L999999)"), ArgumentMatchers.any(SearchControls.class))).thenReturn(heleResultatet);

        LdapBrukeroppslag ldap = new LdapBrukeroppslag(context, baseSearch);
        ldap.hentBrukerinformasjon("L999999");
    }


    @Test
    public void skal_gi_exception_når_søket_gir_to_treff() throws Exception {
        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("F-137440:Forventet ett unikt resultat på søk mot LDAP etter ident L999999, men fikk flere treff");

        Mockito.when(context.search(ArgumentMatchers.eq(baseSearch), ArgumentMatchers.eq("(cn=L999999)"), ArgumentMatchers.any(SearchControls.class))).thenThrow(new LimitExceededException("This is a test"));

        LdapBrukeroppslag ldap = new LdapBrukeroppslag(context, baseSearch);
        ldap.hentBrukerinformasjon("L999999");
    }

    @Test
    public void skal_gi_exception_når_svaret_mangler_forventet_attibutt() throws Exception {
        expectedException.expect(IntegrasjonException.class);
        expectedException.expectMessage("Resultat fra LDAP manglet påkrevet attributtnavn displayName");

        BasicAttributes attributes = new BasicAttributes();
        attributes.put("cn", "L999999");
        SearchResult resultat = new SearchResult("CN=L999999,OU=ApplAccounts", null, attributes);
        SearchMock heleResultatet = new SearchMock(Collections.singletonList(resultat));
        Mockito.when(context.search(ArgumentMatchers.eq(baseSearch), ArgumentMatchers.eq("(cn=L999999)"), ArgumentMatchers.any(SearchControls.class))).thenReturn(heleResultatet);

        LdapBrukeroppslag ldap = new LdapBrukeroppslag(context, baseSearch);
        ldap.hentBrukerinformasjon("L999999");
    }

    @Test
    public void skal_gi_exception_når_det_søkes_med_spesialtegn() throws Exception {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("F-271934:Mulig LDAP-injection forsøk. Søkte med ugyldig ident 'L999999) or (cn=A*'");

        LdapBrukeroppslag ldap = new LdapBrukeroppslag(context, baseSearch);
        ldap.hentBrukerinformasjon("L999999) or (cn=A*");

    }

    private static class SearchMock implements NamingEnumeration<SearchResult> {

        private int index = 0;
        private List<SearchResult> resultList;

        SearchMock(List<SearchResult> resultList) {
            this.resultList = resultList;
        }

        @Override
        public SearchResult next() throws NamingException {
            throw new IllegalArgumentException("Test---not implemented");
        }

        @Override
        public boolean hasMore() throws NamingException {
            throw new IllegalArgumentException("Test---not implemented");
        }

        @Override
        public void close() throws NamingException {

        }

        @Override
        public boolean hasMoreElements() {
            return index < resultList.size();
        }

        @Override
        public SearchResult nextElement() {
            return resultList.get(index++);
        }
    }
}

