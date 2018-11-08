package no.nav.foreldrepenger.jsonfeed;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.domene.feed.FeedRepository;
import no.nav.foreldrepenger.domene.feed.HendelseCriteria;
import no.nav.foreldrepenger.domene.feed.VedtakUtgåendeHendelse;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.jsonfeed.dto.ForeldrepengerVedtakDto;
import no.nav.foreldrepenger.kontrakter.feed.felles.FeedElement;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Innhold;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.Meldingstype;
import no.nav.foreldrepenger.kontrakter.feed.vedtak.v1.VedtakMetadata;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

@ApplicationScoped
public class VedtakFattetTjeneste {
    private static final Logger log = LoggerFactory.getLogger(VedtakFattetTjeneste.class);
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Oslo");

    private FeedRepository feedRepository;

    public VedtakFattetTjeneste() {
    }

    @Inject
    public VedtakFattetTjeneste(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public ForeldrepengerVedtakDto hentVedtak(Long sisteLestSekvensId, Long maxAntall, String hendelseType, Optional<AktørId> aktørId) {
        HendelseCriteria.Builder builder = new HendelseCriteria.Builder()
            .medSisteLestSekvensId(sisteLestSekvensId)
            .medType(hendelseType)
            .medMaxAntall(maxAntall + 1); // sender med pluss 1 for å få utledet harFlereElementer

        aktørId.ifPresent(it -> builder.medAktørId(it.getId()));

        HendelseCriteria hendelseCriteria = builder.build();

        List<VedtakUtgåendeHendelse> utgåendeHendelser = feedRepository.hentUtgåendeHendelser(VedtakUtgåendeHendelse.class, hendelseCriteria);

        List<FeedElement> feedElementer = utgåendeHendelser.stream().map(this::mapTilFeedElement).filter(Objects::nonNull).collect(Collectors.toList());
        boolean harFlereElementer = feedElementer.size() > maxAntall;
        if (harFlereElementer) {
            feedElementer.remove(feedElementer.size() - 1); //Ba om 1 ekstra for å få utledet harFlereElementer, så fjerner den fra outputen
        }

        return new ForeldrepengerVedtakDto(harFlereElementer, feedElementer);

    }

    private FeedElement mapTilFeedElement(VedtakUtgåendeHendelse hendelse) {
        Meldingstype type = Meldingstype.fromType(hendelse.getType());
        if (type == null) { //ignorerer ukjente typer
            HendelsePublisererFeil.FACTORY.ukjentHendelseMeldingstype(hendelse.getType(), hendelse.getSekvensnummer()).log(log);
            return null;
        }

        Innhold innhold = JsonMapper.fromJson(hendelse.getPayload(), type.getMeldingsDto());
        return new FeedElement.Builder()
            .medSekvensId(hendelse.getSekvensnummer())
            .medType(hendelse.getType())
            .medInnhold(innhold)
            .medMetadata(new VedtakMetadata.Builder()
                .medOpprettetDato(ZonedDateTime.of(hendelse.getOpprettetTidspunkt(), ZONE_ID)).build())
            .build();
    }
}
