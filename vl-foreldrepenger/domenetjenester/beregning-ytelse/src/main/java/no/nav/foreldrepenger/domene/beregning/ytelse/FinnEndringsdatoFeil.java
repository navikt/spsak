package no.nav.foreldrepenger.domene.beregning.ytelse;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface FinnEndringsdatoFeil extends DeklarerteFeil {

    FinnEndringsdatoFeil FACTORY = FeilFactory.create(FinnEndringsdatoFeil.class);

    @TekniskFeil(feilkode = "FP-655541", feilmelding = "Fant ikke beregningsresultat for behandling med id %s", logLevel = ERROR)
    Feil manglendeBeregningsresultat(Long behandlingId);

    @TekniskFeil(feilkode = "FP-655544", feilmelding = "Behandlingen med id %s er ikke en revurdering", logLevel = ERROR)
    Feil behandlingErIkkeEnRevurdering(Long behandlingId);

    @TekniskFeil(feilkode = "FP-655545", feilmelding = "Fant ikke en original behandling for revurdering med id %s", logLevel = ERROR)
    Feil manglendeOriginalBehandling(Long behandlingId);

    @TekniskFeil(feilkode = "FP-655542", feilmelding = "Fant ikke beregningsresultatperiode for beregningsresultat med id %s", logLevel = ERROR)
    Feil manglendeBeregningsresultatPeriode(Long beregningsresultatId);

    @TekniskFeil(feilkode = "FP-655543", feilmelding = "Fant ikke andel for beregningsresultatperiode med id %s", logLevel = ERROR)
    Feil manglendeBeregningsresultatPeriodeAndel(Long beregningsresultatPeriodeId);

    @TekniskFeil(feilkode = "FP-655546", feilmelding = "Fant flere korresponderende andeler for andel med id %s", logLevel = ERROR)
    Feil fantFlereKorresponderendeAndelerFeil(Long beregningsresultatAndelId);

}
