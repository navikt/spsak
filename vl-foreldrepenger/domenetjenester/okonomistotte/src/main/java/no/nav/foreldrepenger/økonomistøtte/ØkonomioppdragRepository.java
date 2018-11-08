package no.nav.foreldrepenger.økonomistøtte;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public interface ØkonomioppdragRepository extends BehandlingslagerRepository {

    Oppdragskontroll hentOppdragskontroll(long oppdragskontrollId);

    Oppdragskontroll finnVentendeOppdrag(long behandlingId);

    Optional<Oppdragskontroll> finnOppdragForBehandling(long behandlingId);

    Optional<Oppdragskontroll> finnNyesteOppdragForSak(Saksnummer saksnr);

    long lagre(Oppdragskontroll oppdragskontroll);

    List<Oppdrag110> hentOppdrag110ForPeriodeOgFagområde(LocalDate fomDato, LocalDate tomDato, String fagområde);

    List<Oppdragskontroll> finnAlleOppdragForSak(Saksnummer saksnr);
}
