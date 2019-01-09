package no.nav.foreldrepenger.web.server.jetty;

import org.junit.Assert;
import org.junit.Test;

public class DataSourceKonfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void rolePrefixMedFnuttSkalFeile() {
        initVaultDBMedVaultRolePrefix("rolle'");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rolePrefixMedSemikolonSkalFeile() {
        initVaultDBMedVaultRolePrefix("rolle;");
    }

    @Test
    public void greitRolePrefixSkalIkkeGiIllegalArgumentException() {
        try {
            initVaultDBMedVaultRolePrefix("eksempeldatabase-q1");
        } catch (RuntimeException re) {
            Assert.assertFalse("IllegalArgumentException ved OK rollePrefix gjør at de andre (rolePrefix*SkalFeile) testene ikke lenger tester det de skal", re instanceof IllegalArgumentException);
        }
        Assert.assertTrue("bare en assert", true);
    }


    private void initVaultDBMedVaultRolePrefix(String rolePrefix) {
        System.setProperty("defaultDS.url", "jdbc:postgresql://localhost:1111e/eksempeldatabase-q1");
        System.setProperty("defaultDS.vault.enable", "true");
        System.setProperty("defaultDS.vault.roleprefix", rolePrefix);
        System.setProperty("defaultDS.vault.mountpath", "database/integrationtest"); //"postgresql/preprod");
        new DataSourceKonfig();
    }

}
