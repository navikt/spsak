package no.nav.foreldrepenger.domene.mottak.forsendelse.tjeneste;

import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseIdDto;
import no.nav.foreldrepenger.domene.mottak.forsendelse.ForsendelseStatusDataDTO;

public interface ForsendelseStatusTjeneste {
    ForsendelseStatusDataDTO getStatusInformasjon(ForsendelseIdDto forsendelseIdDto);
}
