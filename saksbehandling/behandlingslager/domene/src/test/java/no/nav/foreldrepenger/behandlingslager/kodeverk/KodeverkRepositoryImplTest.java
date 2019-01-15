package no.nav.foreldrepenger.behandlingslager.kodeverk;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.PensjonTrygdType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Diskresjonskode;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Poststed;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.exception.TekniskException;

public class KodeverkRepositoryImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepositoryImpl repo = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_hente_kodeliste_verdi_basert_på_type_og_kode() {
        assertThat(repo.finn(NavBrukerKjønn.class, "M")).isEqualTo(NavBrukerKjønn.MANN);
    }

    @Test
    public void skal_kaste_feil_dersom_kodeliste_verdi_ikke_finnes() {
        // Arrange
        expectedException.expect(TekniskException.class);

        // Act
        repo.finn(NavBrukerKjønn.class, "IKKE_EKSISTERENDE_KODE");
    }

    @Test
    public void test_hent_samme_verdi() {
        Poststed postSted1 = repo.finn(Poststed.class, "0103");
        Poststed postSted2 = repo.finn(Poststed.class, "0103");
        assertThat(postSted1).isEqualTo(postSted2);
    }

    @Test
    public void cache_må_respektere_forskjellige_kodeverk_med_like_koder() {
        final String kode = "-";
        PensjonTrygdType kode1 = repo.finn(PensjonTrygdType.class, kode);
        Landkoder kode2 = repo.finn(Landkoder.class, kode);

        assertThat(kode1.getKode()).isEqualTo(kode);
        assertThat(kode2.getKode()).isEqualTo(kode);
    }

    @Test
    public void test_hent_offisiellverdi_flere_ganger() {
        Poststed postSted1 = repo.finnForKodeverkEiersKode(Poststed.class, "0103");
        Poststed postSted2 = repo.finnForKodeverkEiersKode(Poststed.class, "0103");
        assertThat(postSted1).isEqualTo(postSted2);
    }

    @Test
    public void test_hent_flere_offisielle_koder_samtidig_flere_ganger() {
        List<Poststed> poststeds = repo.finnForKodeverkEiersKoder(Poststed.class, "0103", "0104");
        assertThat(poststeds).hasSize(2);
        List<Poststed> poststeds2 = repo.finnForKodeverkEiersKoder(Poststed.class, "0103", "0104");
        assertThat(poststeds2).hasSize(2);
    }

    @Test
    public void test_hent_flere_koder_samtidig_flere_ganger() {
        List<Poststed> poststeds = repo.finnListe(Poststed.class, asList("0103", "0104"));
        assertThat(poststeds).hasSize(2);
        List<Poststed> poststeds2 = repo.finnListe(Poststed.class, asList("0103", "0104"));
        assertThat(poststeds2).hasSize(2);
    }

    @Test
    public void skal_detach_kodeverk_for_finn() {
        Diskresjonskode pend = repo.finn(Diskresjonskode.class, "PEND");
        EntityManager entityManager = repoRule.getEntityManager();
        assertThat(entityManager.contains(pend)).isFalse();
    }

    @Test
    public void skal_detach_kodeverk_for_finnForKodeverkEiersKode() {
        Diskresjonskode pend = repo.finnForKodeverkEiersKode(Diskresjonskode.class, "PEND");
        EntityManager entityManager = repoRule.getEntityManager();
        assertThat(entityManager.contains(pend)).isFalse();
    }

    @Test
    public void skal_detach_kodeverk_for_finnForKodeverkEiersKoder() {
        List<Diskresjonskode> diskresjonskode = repo.finnForKodeverkEiersKoder(Diskresjonskode.class, "PEND", "MILI");
        Diskresjonskode pend = diskresjonskode.stream().filter(k -> k.getOffisiellKode().equals("PEND")).findFirst().get();
        Diskresjonskode mili = diskresjonskode.stream().filter(k -> k.getOffisiellKode().equals("MILI")).findFirst().get();

        EntityManager entityManager = repoRule.getEntityManager();
        assertThat(entityManager.contains(pend)).isFalse();
        assertThat(entityManager.contains(mili)).isFalse();
    }

    @Test
    public void skal_detach_kodeverk_for_finnListe() {
        List<Diskresjonskode> diskresjonskode = repo.finnListe(Diskresjonskode.class, asList("PEND", "MILI"));
        Diskresjonskode pend = diskresjonskode.stream().filter(k -> k.getOffisiellKode().equals("PEND")).findFirst().get();
        Diskresjonskode mili = diskresjonskode.stream().filter(k -> k.getOffisiellKode().equals("MILI")).findFirst().get();

        EntityManager entityManager = repoRule.getEntityManager();
        assertThat(entityManager.contains(pend)).isFalse();
        assertThat(entityManager.contains(mili)).isFalse();
    }

    @Test
    public void skal_detach_kodeverk_for_hentAlle() {
        List<Diskresjonskode> diskresjonskode = repo.hentAlle(Diskresjonskode.class);
        Diskresjonskode pend = diskresjonskode.stream().filter(k -> k.getOffisiellKode().equals("PEND")).findFirst().get();
        Diskresjonskode mili = diskresjonskode.stream().filter(k -> k.getOffisiellKode().equals("MILI")).findFirst().get();

        EntityManager entityManager = repoRule.getEntityManager();
        assertThat(entityManager.contains(pend)).isFalse();
        assertThat(entityManager.contains(mili)).isFalse();
    }

    @Test
    public void skal_hente_landkoder_til_landkodeiso2_map() throws Exception {
        Map<String, String> resultat = repo.hentLandkoderTilLandkodeISO2Map();
        assertThat(resultat).isNotEmpty();
        resultat.values().forEach(val -> assertThat(val.length()).isEqualTo(2));
        resultat.keySet().forEach(val -> assertThat(val.length()).isEqualTo(3));
    }

    @Test
    public void skal_hente_landkodeiso2_til_landkoder_map() throws Exception {
        Map<String, String> resultat = repo.hentLandkodeISO2TilLandkoderMap();
        assertThat(resultat).isNotEmpty();
        resultat.values().forEach(val -> assertThat(val.length()).isEqualTo(3));
        resultat.keySet().forEach(val -> assertThat(val.length()).isEqualTo(2));
    }

    @Test
    public void skal_bestemme_borger_av_EU(){
        assertThat(repo.brukerErBorgerAvEuLand(Landkoder.SWE.getKode())).isTrue();
    }

    @Test
    public void skal_bestemme_borger_av_EØS(){
        assertThat(repo.brukerErBorgerAvEøsLand(Landkoder.NOR.getKode())).isTrue();
    }

    @Test
    public void skal_bestemme_borger_av_Norden(){
        assertThat(repo.brukerErNordiskStatsborger(Landkoder.NOR.getKode())).isTrue();
    }
}
