package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.hendelse;

import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.kontrakter.feed.felles.FeedElement;
import no.nav.vedtak.felles.integrasjon.rest.JsonMapper;

@ApplicationScoped
public class InfotrygdHendelseMapper {

    public InfotrygdHendelseMapper() {
    }

    public InfotrygdHendelse mapFraFeedTilInfotrygdHendelse(FeedElement feedElement) {
        String payload = JsonMapper.toJson(feedElement.getInnhold());

        Optional<Meldingstype> meldingstype = Arrays.stream(Meldingstype.values()).filter(e -> e.getType().equals(feedElement.getType())).findFirst();

        if (!meldingstype.isPresent()) {
            return InfotrygdHendelse.builder().build();
        }
        Innhold innhold = JsonMapper.fromJson(payload, meldingstype.get().getMeldingsDto());

        return InfotrygdHendelse.builder()
            .medSekvensnummer(feedElement.getSekvensId())
            .medType(feedElement.getType())
            .medAktørId(Long.valueOf(innhold.getAktoerId()))
            .medTypeYtelse(innhold.getTypeYtelse())
            .medFom(innhold.getFom())
            .medIdentDato(innhold.getIdentDato())
            .build();
    }

}
