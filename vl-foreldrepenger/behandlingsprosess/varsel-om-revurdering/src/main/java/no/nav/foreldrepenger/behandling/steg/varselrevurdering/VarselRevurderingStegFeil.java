package no.nav.foreldrepenger.behandling.steg.varselrevurdering;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface VarselRevurderingStegFeil extends DeklarerteFeil {

    VarselRevurderingStegFeil FACTORY = FeilFactory.create(VarselRevurderingStegFeil.class);

    @TekniskFeil(feilkode = "FP-139371", feilmelding = "Manger behandlingsårsak på revurdering", logLevel = LogLevel.ERROR)
    Feil manglerBehandlingsårsakPåRevurdering();
}
