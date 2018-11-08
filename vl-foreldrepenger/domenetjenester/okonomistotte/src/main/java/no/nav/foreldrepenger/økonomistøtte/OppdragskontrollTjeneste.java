package no.nav.foreldrepenger.økonomistøtte;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;

public interface OppdragskontrollTjeneste {

    Long opprettOppdrag(Long behandlingId, Long prosessTaskId);

    Long opprettOppdragSimulering(Long behandlingId, Long prosessTaskId);

    Oppdragskontroll hentOppdragskontroll(Long oppdragskontrollId);

    List<Oppdrag110> hentOppdragForPeriodeOgFagområde(LocalDate fomDato, LocalDate tomDato, String fagområde);
}
