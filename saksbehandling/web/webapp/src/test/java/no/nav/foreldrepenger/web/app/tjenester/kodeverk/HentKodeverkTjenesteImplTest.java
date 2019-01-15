package no.nav.foreldrepenger.web.app.tjenester.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.EnhetsTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.HentKodeverkTjenesteImpl;


public class HentKodeverkTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepositoryImpl repo = new KodeverkRepositoryImpl(repoRule.getEntityManager());
    private EnhetsTjeneste enhetsTjeneste = new EnhetsTjenesteImpl();

    @Test
    public void skal_filtere_arbeidtyper() {
        HentKodeverkTjenesteImpl kodeverk = new HentKodeverkTjenesteImpl(repo, enhetsTjeneste);

        Map<String, List<Kodeliste>> resultat = kodeverk.hentGruppertKodeliste();

        List<Kodeliste> arbeidType = resultat.get("ArbeidType");
        assertThat(arbeidType).hasSize(6);
    }
}
