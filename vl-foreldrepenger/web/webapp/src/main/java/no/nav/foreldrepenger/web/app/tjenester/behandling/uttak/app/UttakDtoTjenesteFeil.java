package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface UttakDtoTjenesteFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "FP-676202", feilmelding = "Alle arbeidsforhold i en periodegruppe må ha samme perioderesultat. Behandlingsid %s", logLevel = ERROR)
    Feil inkonsistentePeriodeResultatFraRepository(Long behandlingId);

    @TekniskFeil(feilkode = "FP-985423", feilmelding = "Alle arbeidsforhold i en periodegruppe må ha samme begrunnelse. Behandlingsid %s", logLevel = ERROR)
    Feil inkonsistenteBegrunnelserFraRepository(Long behandlingId);

    @TekniskFeil(feilkode = "FP-871234", feilmelding = "Alle arbeidsforhold i en periodegruppe må ha samme tom. Behandlingsid %s", logLevel = ERROR)
    Feil inkonsistenteTomFraRepository(Long behandlingId);
}
