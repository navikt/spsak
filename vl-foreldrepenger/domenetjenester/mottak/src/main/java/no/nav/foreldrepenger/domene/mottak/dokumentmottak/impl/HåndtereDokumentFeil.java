package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public interface HåndtereDokumentFeil extends DeklarerteFeil {

    HåndtereDokumentFeil FACTORY = FeilFactory.create(HåndtereDokumentFeil.class);

    @TekniskFeil(feilkode = "FP-842786", feilmelding = "Ugyldig payload - feil ved parsing. '%s' -> '%s'", logLevel = LogLevel.WARN)
    Feil feilVedParsingAvInngåendeSaksdokument(ProsessTaskData data, IOException e);

    @TekniskFeil(feilkode = "FP-980324", feilmelding = "Klarte ikke parse til json. '%s' -> '%s'", logLevel = LogLevel.WARN)
    Feil feilVedParsingAvInngåendeSaksdokument(InngåendeSaksdokument saksdokument, JsonProcessingException e);
}
