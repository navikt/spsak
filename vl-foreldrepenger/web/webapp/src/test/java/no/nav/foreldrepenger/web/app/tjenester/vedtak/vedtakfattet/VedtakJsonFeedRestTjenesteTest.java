package no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.jsonfeed.VedtakFattetTjeneste;
import no.nav.foreldrepenger.jsonfeed.dto.ForeldrepengerVedtakDto;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedDto;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Meldingstype;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.AktørParam;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.HendelseTypeParam;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.MaxAntallParam;
import no.nav.foreldrepenger.web.app.tjenester.vedtak.vedtakfattet.dto.SekvensIdParam;
import no.nav.vedtak.sikkerhet.ContextPathHolder;

public class VedtakJsonFeedRestTjenesteTest {

    private VedtakJsonFeedRestTjeneste tjeneste;
    private VedtakFattetTjeneste vedtakFattetTjeneste;

    @Before
    public void setUp() throws Exception {
        ContextPathHolder.instance("/fpsak");
        vedtakFattetTjeneste = mock(VedtakFattetTjeneste.class);
        tjeneste = new VedtakJsonFeedRestTjeneste(vedtakFattetTjeneste);
    }   

    @Test
    public void skal_delegere_til_hent_vedtak_tjeneste() {
        SekvensIdParam sisteLestSekvensIdParam = new SekvensIdParam("1");
        MaxAntallParam maxAntallParam = new MaxAntallParam("100");
        HendelseTypeParam hendelseTypeParam = new HendelseTypeParam(Meldingstype.FORELDREPENGER_ENDRET.getType());
        AktørParam aktørParam = new AktørParam("123");
        when(vedtakFattetTjeneste.hentVedtak(1L, 100L, "ForeldrepengerEndret_v1", Optional.of(new AktørId("123")))).thenReturn(new ForeldrepengerVedtakDto(true, new ArrayList<>()));

        FeedDto feed = tjeneste.vedtakHendelser(sisteLestSekvensIdParam, maxAntallParam, hendelseTypeParam, aktørParam);
        assertThat(feed.getTittel()).isEqualTo("ForeldrepengerVedtak_v1");
        assertThat(feed.getInneholderFlereElementer()).isEqualTo(true);
        assertThat(feed.getElementer()).isEqualTo(new ArrayList<>());
    }
    
    @Test
    public void skal_delegere_til_hent_vedtak_tjeneste_med_default_params() {
        SekvensIdParam sisteLestSekvensIdParam = new SekvensIdParam("1");
        MaxAntallParam maxAntallParam = new MaxAntallParam("100");
        HendelseTypeParam hendelseTypeParam = new HendelseTypeParam("");
        AktørParam aktørParam = new AktørParam("");
        
        Optional<AktørId> emptyAktørParam = Optional.empty();
        when(vedtakFattetTjeneste.hentVedtak(1L, 100L, null, emptyAktørParam)).thenReturn(new ForeldrepengerVedtakDto(true, new ArrayList<>()));

        FeedDto feed = tjeneste.vedtakHendelser(sisteLestSekvensIdParam, maxAntallParam, hendelseTypeParam, aktørParam);
        assertThat(feed.getTittel()).isEqualTo("ForeldrepengerVedtak_v1");
        assertThat(feed.getInneholderFlereElementer()).isEqualTo(true);
        assertThat(feed.getElementer()).isEqualTo(new ArrayList<>());
    }
}
