package no.nav.foreldrepenger.domene.feed;

import java.util.List;
import java.util.Optional;

public interface FeedRepository {

    <V extends UtgåendeHendelse> Long lagre(V utgåendeHendelse);

    Optional<UtgåendeHendelse> hentUtgåendeHendelse(Long hendelseId);
    
    <V extends UtgåendeHendelse> List<V> hentUtgåendeHendelser(Class<V> cls, HendelseCriteria hendelseCriteria);

    <V extends UtgåendeHendelse> List<V> hentAlle(Class<V> cls);

    <V extends UtgåendeHendelse> boolean harHendelseMedKildeId(Class<V> cls, String kildeId);
}
