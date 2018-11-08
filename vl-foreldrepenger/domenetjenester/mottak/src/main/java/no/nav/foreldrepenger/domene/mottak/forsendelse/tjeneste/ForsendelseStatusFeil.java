package no.nav.foreldrepenger.domene.mottak.forsendelse.tjeneste;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.UUID;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ForsendelseStatusFeil extends DeklarerteFeil {

    ForsendelseStatusFeil FACTORY = FeilFactory.create(ForsendelseStatusFeil.class);

    @TekniskFeil(feilkode = "FP-760821", feilmelding = "finnes ikke mottat dokument for forsendelse ID %s", logLevel = WARN)
    Feil finnesIkkeMottatDokument(UUID forsendelseId);

    @TekniskFeil(feilkode = "FP-760822", feilmelding = "Mer enn en mottat dokument funnet for forsendelse ID %s", logLevel = WARN)
    Feil flereMotattDokument(UUID forsendelseId);

    @TekniskFeil(feilkode = "FP-760823", feilmelding = "Ugyldig behandlingsresultat for forsendlese ID %s", logLevel = WARN)
    Feil ugyldigBehandlingResultat(UUID forsendelseId);
}

