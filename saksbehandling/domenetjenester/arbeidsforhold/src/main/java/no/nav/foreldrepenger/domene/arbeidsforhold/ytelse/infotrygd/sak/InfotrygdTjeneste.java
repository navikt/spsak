package no.nav.foreldrepenger.domene.arbeidsforhold.ytelse.infotrygd.sak;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;

public interface InfotrygdTjeneste {
    List<InfotrygdSak> finnSakListe(Behandling behandling, String fnr, LocalDate fom);
}
