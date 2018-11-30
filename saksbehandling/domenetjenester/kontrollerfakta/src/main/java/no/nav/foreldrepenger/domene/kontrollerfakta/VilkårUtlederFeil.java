package no.nav.foreldrepenger.domene.kontrollerfakta;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

// TODO: PK-49128: Rename og ta ut overflødige metoder
public interface VilkårUtlederFeil extends DeklarerteFeil {
    VilkårUtlederFeil FEILFACTORY = FeilFactory.create(VilkårUtlederFeil.class);

    @TekniskFeil(feilkode = "FP-768019", feilmelding = "Kan ikke utlede vilkår for behandlingId %s, da behandlingsmotiv ikke kan avgjøres", logLevel = LogLevel.ERROR)
    Feil behandlingsmotivKanIkkeUtledes(Long behandlingId);
}
