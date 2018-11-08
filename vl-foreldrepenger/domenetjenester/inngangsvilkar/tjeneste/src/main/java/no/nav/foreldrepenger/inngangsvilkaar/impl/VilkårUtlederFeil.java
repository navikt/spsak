package no.nav.foreldrepenger.inngangsvilkaar.impl;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface VilkårUtlederFeil extends DeklarerteFeil {
    VilkårUtlederFeil FEILFACTORY = FeilFactory.create(VilkårUtlederFeil.class);

    @TekniskFeil(feilkode = "FP-768012", feilmelding = "Støtter ikke stønadtype %s.", logLevel = LogLevel.ERROR)
    Feil støtterIkkeStønadstype(String stønadstype);

    @TekniskFeil(feilkode = "FP-768017", feilmelding = "Kan ikke utlede vilkår for behandlingId %s, da behandlingsmotiv ikke kan avgjøres", logLevel = LogLevel.ERROR)
    Feil behandlingsmotivKanIkkeUtledes(Long behandlingId);

    @TekniskFeil(feilkode = "FP-768018", feilmelding = "Kan ikke utlede vilkår for behandlingId %s. Mangler konfigurasjon for behandlingsmotiv %s", logLevel = LogLevel.ERROR)
    Feil kunneIkkeUtledeVilkårFor(Long behandlingId, String behandlingsmotiv);

}
