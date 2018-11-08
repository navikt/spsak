package no.nav.foreldrepenger.behandling.steg.beregnytelse.es;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface BeregneYtelseFeil extends DeklarerteFeil {

    BeregneYtelseFeil FACTORY = FeilFactory.create(BeregneYtelseFeil.class);

    @TekniskFeil(feilkode = "FP-110704",
        feilmelding = "Kan ikke beregne ytelse. Finner ikke tilstrekkelig behandlingsgrunnlag for behandlingId: '%s'",
        logLevel = LogLevel.WARN)
    Feil beregningsstegIkkeStøttetForSøknadtype(Long behandlingId);

    @FunksjonellFeil(feilkode = "FP-110705",
        feilmelding = "Kan ikke beregne ytelse. Finner ikke barn som har rett til ytelse i behandlingsgrunnlaget.",
        løsningsforslag = "Sjekk avklarte fakta i behandlingen. Oppdater fakta slik at det finnes barn " +
            "med rett til støtte, eller sett behandling til avslått.",
        logLevel = LogLevel.WARN)
    Feil beregningsstegIkkeStøttetForBehandling();

    @TekniskFeil(feilkode = "FP-110706",
        feilmelding = "Kan ikke finne riktig satsdato for behandling: '%s'",
        logLevel = LogLevel.WARN)
    Feil kanIkkeFinneRiktigSatsdato(Long behandlingId);
}
