package no.nav.foreldrepenger.jsonfeed;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.domene.feed.FeedRepository;
import no.nav.foreldrepenger.domene.feed.HendelseCriteria;
import no.nav.foreldrepenger.domene.feed.VedtakUtgåendeHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.jsonfeed.dto.ForeldrepengerVedtakDto;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.ForeldrepengerInnvilget;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Meldingstype;

public class VedtakFattetTjenesteTest {

    private static final String PAYLOAD = "{}";
    private static final AktørId AKTØR_ID = new AktørId("3");
    private static final String HENDELSE_TYPE = Meldingstype.FORELDREPENGER_INNVILGET.getType();
    private static final long MAX_ANTALL = 2L;
    private static final long SIST_LEST_SEKVENSID = 1L;
    private VedtakFattetTjeneste tjeneste;
    private FeedRepository feedRepository;

    @Before
    public void setUp() {
        feedRepository = Mockito.mock(FeedRepository.class);
        tjeneste = new VedtakFattetTjeneste(feedRepository);
    }

    @Test
    public void skal_delegere_til_repository_metode_for_søk_hendelser() throws Exception {
        ArgumentCaptor<HendelseCriteria> captor = ArgumentCaptor.forClass(HendelseCriteria.class);
        VedtakUtgåendeHendelse hendelse = mockHendelse(SIST_LEST_SEKVENSID + 1);

        when(feedRepository.hentUtgåendeHendelser(eq(VedtakUtgåendeHendelse.class), any(HendelseCriteria.class))).thenReturn(asList(hendelse));

        ForeldrepengerVedtakDto dto = tjeneste.hentVedtak(SIST_LEST_SEKVENSID, MAX_ANTALL, HENDELSE_TYPE, Optional.of(AKTØR_ID));

        verify(feedRepository).hentUtgåendeHendelser(eq(VedtakUtgåendeHendelse.class), captor.capture());
        HendelseCriteria criteria = captor.getValue();
        assertThat(criteria.getAktørId()).isEqualTo(AKTØR_ID.getId());
        assertThat(criteria.getMaxAntall()).isEqualTo(MAX_ANTALL + 1);
        assertThat(criteria.getSisteLestSekvensId()).isEqualTo(SIST_LEST_SEKVENSID);
        assertThat(criteria.getType()).isEqualTo(HENDELSE_TYPE);
        assertThat(dto.isHarFlereElementer()).isFalse();
        assertThat(dto.getElementer()).hasSize(1);
        assertThat(dto.getElementer().get(0).getType()).isEqualTo(HENDELSE_TYPE);
        assertThat(dto.getElementer().get(0).getInnhold()).isInstanceOf(ForeldrepengerInnvilget.class);
        assertThat(dto.getElementer().get(0).getSekvensId()).isEqualTo(SIST_LEST_SEKVENSID + 1);
    }


    @Test
    public void hent_hendelser_skal_returnere_at_det_er_flere_hendelser_å_lese() throws Exception {
        VedtakUtgåendeHendelse hendelse = mockHendelse(SIST_LEST_SEKVENSID + 1);
        VedtakUtgåendeHendelse hendelse2 = mockHendelse(SIST_LEST_SEKVENSID + 2);
        when(feedRepository.hentUtgåendeHendelser(eq(VedtakUtgåendeHendelse.class), any(HendelseCriteria.class))).thenReturn(asList(hendelse, hendelse2));

        ForeldrepengerVedtakDto dto = tjeneste.hentVedtak(SIST_LEST_SEKVENSID, 1L, HENDELSE_TYPE, Optional.of(AKTØR_ID));

        assertThat(dto.isHarFlereElementer()).isTrue();
    }

    @Test
    public void skal_ignorere_ukjent_type() throws Exception {
        VedtakUtgåendeHendelse hendelse = mockHendelse(SIST_LEST_SEKVENSID + 1);
        when(hendelse.getType()).thenReturn("Ukjent");
        when(feedRepository.hentUtgåendeHendelser(eq(VedtakUtgåendeHendelse.class), any(HendelseCriteria.class))).thenReturn(asList(hendelse));

        ForeldrepengerVedtakDto dto = tjeneste.hentVedtak(SIST_LEST_SEKVENSID, 1L, HENDELSE_TYPE, Optional.of(AKTØR_ID));

        assertThat(dto.getElementer()).isEmpty();
    }

    private VedtakUtgåendeHendelse mockHendelse(Long sekvensenummer) {
        VedtakUtgåendeHendelse hendelse = mock(VedtakUtgåendeHendelse.class);
        when(hendelse.getType()).thenReturn(HENDELSE_TYPE);
        when(hendelse.getSekvensnummer()).thenReturn(sekvensenummer);
        when(hendelse.getPayload()).thenReturn(PAYLOAD);
        when(hendelse.getOpprettetTidspunkt()).thenReturn(LocalDateTime.now());
        return hendelse;
    }

}
