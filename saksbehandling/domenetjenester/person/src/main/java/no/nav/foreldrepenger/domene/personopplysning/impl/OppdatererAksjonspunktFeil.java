package no.nav.foreldrepenger.domene.personopplysning.impl;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface OppdatererAksjonspunktFeil extends DeklarerteFeil {
    OppdatererAksjonspunktFeil FACTORY = FeilFactory.create(OppdatererAksjonspunktFeil.class);

    @IntegrasjonFeil(feilkode = "FP-905999", feilmelding = "Verge med fnr ikke funnet i TPS.", logLevel = ERROR)
    Feil vergeIkkeFunnetITPS();

}
