package no.nav.foreldrepenger.migrering;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface HistorikkMigreringFeil extends DeklarerteFeil {

    HistorikkMigreringFeil FACTORY = FeilFactory.create(HistorikkMigreringFeil.class);

    @TekniskFeil(feilkode = "FP-840399", feilmelding = "Maltype er ikke støttet: %s", logLevel = LogLevel.WARN)
    Feil malTypeIkkeStottet(String mal);

    @TekniskFeil(feilkode = "FP-840398", feilmelding = "Uventet JsonValue.valueType: %s", logLevel = LogLevel.WARN)
    Feil uventetJsonValueType(String s);

    @TekniskFeil(feilkode = "FP-840397", feilmelding = "Ukjent endretFelt navn: %s", logLevel = LogLevel.WARN)
    Feil ukjentEndretFeltNavn(String navn);
    
    @TekniskFeil(feilkode = "FP-252668", feilmelding = "Feil ved konverterting av historikkinnslag: %s", logLevel = LogLevel.ERROR)
    Feil kanIkkeKonvertereHistorikkInnslag(Long historikkinnslagId, Exception e);
}
