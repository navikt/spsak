package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.omsorg;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface AksjonspunktUtlederOmsorgFeil extends DeklarerteFeil {
    AksjonspunktUtlederOmsorgFeil FEILFACTORY = FeilFactory.create(AksjonspunktUtlederOmsorgFeil.class);

    @TekniskFeil(feilkode = "FP-753881", feilmelding = "Ikke mulig å sjekke barn har samme bosted som søker", logLevel = LogLevel.ERROR)
    Feil kanIkkeFinneBarnTilSøker();

}
