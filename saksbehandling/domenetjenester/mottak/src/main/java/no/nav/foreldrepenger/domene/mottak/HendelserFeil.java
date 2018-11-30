package no.nav.foreldrepenger.domene.mottak;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface HendelserFeil extends DeklarerteFeil {
    HendelserFeil FACTORY = FeilFactory.create(HendelserFeil.class);

    @TekniskFeil(feilkode = "FP-075453",
        feilmelding = "Behandlingen kan ikke oppdateres. Mangler personopplysning for søker med aktørId %s",
        logLevel = LogLevel.WARN)
    Feil ingenPersonopplysningForEksisterendeBehandling(AktørId aktørID);

    @TekniskFeil(feilkode = "FP-330623",
            feilmelding = "Fagsak allerede koblet, fagsakId: %s %s",
            logLevel = LogLevel.WARN)
    Feil fagsakAlleredeKoblet(Long fagSakIdNr1, Long fagSakIdNr2);

    @TekniskFeil(feilkode = "FP-388501",
            feilmelding = "Familiehendelse uten dato fagsakId=%s",
            logLevel = LogLevel.WARN)
    Feil familiehendelseUtenDato(Long fagSakId);

    @TekniskFeil(feilkode = "FP-059216",
            feilmelding = "Flere mulige fagsaker å koble til for fagsakId=%s",
            logLevel = LogLevel.WARN)
    Feil flereMuligeFagsakerÅKobleTil(Long fagSakId);

    @TekniskFeil(feilkode = "FP-852565",
            feilmelding = "Håndterer ikke barnets familierelasjoner fra TPS fagsakId=%s",
            logLevel = LogLevel.WARN)
    Feil håndtererIkkeAnnenForeldre(Long fagsakId);
}
