package no.nav.foreldrepenger.inngangsvilkaar.regelmodell.adapter;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface RegelintegrasjonFeil extends DeklarerteFeil {
    RegelintegrasjonFeil FEILFACTORY = FeilFactory.create(RegelintegrasjonFeil.class);

    @TekniskFeil(feilkode = "FP-384251", feilmelding = "Ikke mulig å utlede gyldig vilkårsresultat fra enkeltvilkår", logLevel = LogLevel.WARN)
    Feil kanIkkeUtledeVilkårsresultatFraRegelmotor();

    @TekniskFeil(feilkode = "FP-384255", feilmelding = "Ikke mulig å oversette adopsjonsgrunnlag til regelmotor for behandlingId %s", logLevel = LogLevel.WARN)
    Feil kanIkkeOversetteAdopsjonsgrunnlag(String behandlingId);

    @TekniskFeil(feilkode = "FP-384256", feilmelding = "Ikke mulig å oversette adopsjonsgrunnlag til regelmotor for behandlingId %s", logLevel = LogLevel.WARN)
    Feil kanIkkeFinnneSkjæringstidspunkt(String behandlingId);

    @TekniskFeil(feilkode = "FP-384257", feilmelding = "Kunne ikke serialisere regelinput for vilkår: %s", logLevel = LogLevel.WARN)
    Feil kanIkkeSerialisereRegelinput(String vilkårType, Exception e);

}
