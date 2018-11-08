package no.nav.foreldrepenger.pep;

import java.util.Collection;
import java.util.List;

import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface PdpRequestBuilderFeil extends DeklarerteFeil {

    PdpRequestBuilderFeil FACTORY = FeilFactory.create(PdpRequestBuilderFeil.class);

    @TekniskFeil(feilkode = "FP-621834", feilmelding = "Ugyldig input. Støtter bare 0 eller 1 behandling, men har %s", logLevel = LogLevel.WARN)
    Feil ugyldigInputFlereBehandlingIder(Collection<Long> behandlingId);

    @ManglerTilgangFeil(feilkode = "FP-634829", feilmelding = "Ugyldig input. Sendte inn følgende journalpostId-er, minst en av de finnes ikke i systemet: %s", logLevel = LogLevel.WARN)
    Feil ugyldigInputPåkrevdJournalpostIdFinnesIkke(Collection<JournalpostId> journalPostIder);

    @ManglerTilgangFeil(feilkode = "FP-634830", feilmelding = "Ugyldig input. journalpostId er merket Utgår: %s", logLevel = LogLevel.WARN)
    Feil ugyldigInputJournalpostIdUtgått(String journalPostId);

    @ManglerTilgangFeil(feilkode = "FP-280301", feilmelding = "Ugyldig input. Ikke samsvar mellom behandlingId %s og fagsakId %s", logLevel = LogLevel.WARN)
    Feil ugyldigInputManglerSamsvarBehandlingFagsak(Long behandlingId, List<Long> fagsakIder);
}
