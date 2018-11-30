package no.nav.foreldrepenger.behandling.aksjonspunkt;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import static no.nav.vedtak.feil.LogLevel.WARN;

public interface AksjonspunktUtlederFeil extends DeklarerteFeil {

    AksjonspunktUtlederFeil FACTORY = FeilFactory.create(AksjonspunktUtlederFeil.class);

    @TekniskFeil(feilkode = "FP-985832", feilmelding = "Ukjent aksjonspunktutleder %s", logLevel = WARN)
    Feil fantIkkeAksjonspunktUtleder(String className);

    @TekniskFeil(feilkode = "FP-191205", feilmelding = "Mer enn en implementasjon funnet for aksjonspunktutleder %s", logLevel = WARN)
    Feil flereImplementasjonerAvAksjonspunktUtleder(String className);
}
