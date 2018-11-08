package no.nav.foreldrepenger.domene.feed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class FeedRepositoryImplTest {

    private static final String TYPE2 = "type2";
    private static final String TYPE1 = "type1";
    private static final String AKTØR_ID = "1000000000";
    private static final String AKTØR_ID_2 = "1000000001";
    private static final String KILDE_ID = "kildeId";
    private static final String PAYLOAD = "{\"hello\": \"world\"}";

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private final FeedRepository feedRepository = new FeedRepositoryImpl(repoRule.getEntityManager());
    private VedtakUtgåendeHendelse hendelseAvType1MedAktørId1MedSek1;
    private VedtakUtgåendeHendelse hendelseAvType1MedAktørId2MedSek2;
    private VedtakUtgåendeHendelse hendelseAvType2MedAktørId1MedSek3;
    private VedtakUtgåendeHendelse hendelseMedSekvensnummer99ogKilde;

    @Before
    public void setUp() {
        if (hendelseAvType1MedAktørId1MedSek1 == null) {
            hendelseAvType1MedAktørId1MedSek1 = VedtakUtgåendeHendelse.builder().payload(PAYLOAD).aktørId(AKTØR_ID).type(TYPE1).build();
            hendelseAvType1MedAktørId1MedSek1.setSekvensnummer(1L);
            feedRepository.lagre(hendelseAvType1MedAktørId1MedSek1);
        }

        if (hendelseAvType1MedAktørId2MedSek2 == null) {
            hendelseAvType1MedAktørId2MedSek2 = VedtakUtgåendeHendelse.builder().payload(PAYLOAD).aktørId(AKTØR_ID_2).type(TYPE1).build();
            hendelseAvType1MedAktørId2MedSek2.setSekvensnummer(2L);
            feedRepository.lagre(hendelseAvType1MedAktørId2MedSek2);
        }

        if (hendelseAvType2MedAktørId1MedSek3 == null) {
            hendelseAvType2MedAktørId1MedSek3 = VedtakUtgåendeHendelse.builder().payload(PAYLOAD).aktørId(AKTØR_ID).type(TYPE2).build();
            hendelseAvType2MedAktørId1MedSek3.setSekvensnummer(3L);
            feedRepository.lagre(hendelseAvType2MedAktørId1MedSek3);
        }

        if (hendelseMedSekvensnummer99ogKilde == null) {
            hendelseMedSekvensnummer99ogKilde = VedtakUtgåendeHendelse.builder().payload(PAYLOAD).aktørId("1000000002").type("type3").kildeId(KILDE_ID).build();
            hendelseMedSekvensnummer99ogKilde.setSekvensnummer(99L);
            feedRepository.lagre(hendelseMedSekvensnummer99ogKilde);
        }
        repoRule.getEntityManager().flush();
    }

    @Test
    public void lagre() {
        VedtakUtgåendeHendelse utgåendeHendelse = byggUtgåendeHendelse();
        long id = feedRepository.lagre(utgåendeHendelse);
        assertThat(id).isGreaterThanOrEqualTo(1);
        repoRule.getEntityManager().flush();
        Optional<UtgåendeHendelse> utgåendeHendelse1 = feedRepository.hentUtgåendeHendelse(id);

        assertThat(utgåendeHendelse1.get()).isNotNull();
        assertThat(utgåendeHendelse1.get().getId()).isEqualTo(id);
        assertThat(utgåendeHendelse1.get().getSekvensnummer()).isGreaterThanOrEqualTo(1L);
    }
    
    @Test 
    public void skal_returnere_true_hvis_hendelse_med_kilde_id_eksisterer() {
        assertThat(feedRepository.harHendelseMedKildeId(VedtakUtgåendeHendelse.class, KILDE_ID)).isTrue();
    }
    
    @Test 
    public void skal_returnere_false_hvis_hendelse_med_kilde_id_ikke_eksisterer() {
        assertThat(feedRepository.harHendelseMedKildeId(VedtakUtgåendeHendelse.class, "eksisterer_ikke")).isFalse();
    }
    
    @Test
    public void skal_lagre_hendelse_flushe_sjekke_om_kilde_eksisterer() {
        assertThat(feedRepository.harHendelseMedKildeId(VedtakUtgåendeHendelse.class, "ny_kilde")).isFalse();
        VedtakUtgåendeHendelse utgåendeHendelse = VedtakUtgåendeHendelse.builder().payload(PAYLOAD).aktørId("1000000002").type("type3").kildeId("ny_kilde").build();
        feedRepository.lagre(utgåendeHendelse);
        repoRule.getEntityManager().flush();
        assertThat(feedRepository.harHendelseMedKildeId(VedtakUtgåendeHendelse.class, "ny_kilde")).isTrue();        
    }

    @Test
    public void skal_hente_alle_hendelser() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentAlle(VedtakUtgåendeHendelse.class);

        assertThat(alle).containsOnly(hendelseAvType1MedAktørId1MedSek1, hendelseAvType1MedAktørId2MedSek2,
                hendelseAvType2MedAktørId1MedSek3, hendelseMedSekvensnummer99ogKilde);
    }

    @Test
    public void skal_hente_hendelser_med_type1() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class,
                new HendelseCriteria.Builder().medSisteLestSekvensId(0L).medType(TYPE1).medMaxAntall(100L).build());

        assertThat(alle).containsOnly(hendelseAvType1MedAktørId1MedSek1, hendelseAvType1MedAktørId2MedSek2);
    }

    @Test
    public void skal_hente_alle_hendelser_med_sekvens_id_større_enn_sist_lest() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class,
                new HendelseCriteria.Builder().medSisteLestSekvensId(1L).medMaxAntall(100L).build());

        assertThat(alle).containsOnly(hendelseAvType1MedAktørId2MedSek2, hendelseAvType2MedAktørId1MedSek3, hendelseMedSekvensnummer99ogKilde);
    }

    @Test
    public void skal_returnerer_tom_liste_hvis_result_set_er_tom() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class,
                new HendelseCriteria.Builder().medSisteLestSekvensId(9999L).medMaxAntall(100L).build());

        assertThat(alle).isEmpty();
    }

    @Test
    public void skal_hente_alle_hendelser_med_aktør_id() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class,
                new HendelseCriteria.Builder().medSisteLestSekvensId(0L).medAktørId(AKTØR_ID_2).medMaxAntall(100L).build());

        assertThat(alle).containsOnly(hendelseAvType1MedAktørId2MedSek2);
    }

    @Test
    public void skal_hente_max_antall_1() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class,
                new HendelseCriteria.Builder().medSisteLestSekvensId(0L).medMaxAntall(1L).build());

        assertThat(alle).containsOnly(hendelseAvType1MedAktørId1MedSek1);
    }
    
    @Test
    public void skal_hente_max_antall_4_med_hopp_i_sekvensnummer() {
        List<VedtakUtgåendeHendelse> alle = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class,
                new HendelseCriteria.Builder().medSisteLestSekvensId(0L).medMaxAntall(4L).build());

        assertThat(alle).containsOnly(hendelseAvType1MedAktørId1MedSek1, hendelseAvType1MedAktørId2MedSek2,
                hendelseAvType2MedAktørId1MedSek3, hendelseMedSekvensnummer99ogKilde);
    }

    private static VedtakUtgåendeHendelse byggUtgåendeHendelse() {
        return VedtakUtgåendeHendelse.builder()
                .payload(PAYLOAD)
                .aktørId(AKTØR_ID)
                .type(TYPE1)
                .build();
    }
}
