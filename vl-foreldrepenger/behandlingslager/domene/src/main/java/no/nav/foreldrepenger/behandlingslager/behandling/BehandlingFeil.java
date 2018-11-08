package no.nav.foreldrepenger.behandlingslager.behandling;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface BehandlingFeil extends DeklarerteFeil {

    BehandlingFeil FEILFACTORY = FeilFactory.create(BehandlingFeil.class);

    @TekniskFeil(feilkode = "FP-918665", feilmelding = "Ugyldig antall behandlingsresultat, forventer maks 1 per behandling, men har %s", logLevel = LogLevel.WARN)
    Feil merEnnEttBehandlingsresultat(Integer antall);

    @TekniskFeil(feilkode = "FP-138032", feilmelding = "Behandling har ikke aksjonspunkt for definisjon [%s].", logLevel = LogLevel.ERROR)
    Feil aksjonspunktIkkeFunnet(String kode);

}
