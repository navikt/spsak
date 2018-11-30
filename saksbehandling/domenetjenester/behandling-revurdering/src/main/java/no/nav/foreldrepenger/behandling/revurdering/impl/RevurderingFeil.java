package no.nav.foreldrepenger.behandling.revurdering.impl;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface RevurderingFeil extends DeklarerteFeil {
    RevurderingFeil FACTORY = FeilFactory.create(RevurderingFeil.class);

    @TekniskFeil(feilkode = "FP-317517", feilmelding = "finner ingen behandling som kan revurderes for fagsak: %s", logLevel = LogLevel.WARN)
    Feil tjenesteFinnerIkkeBehandlingForRevurdering(Long fagsakId);

    @TekniskFeil(feilkode = "FP-186234", feilmelding = "Revurdering med id %s har ikke original behandling", logLevel = LogLevel.ERROR)
    Feil revurderingManglerOriginalBehandling(Long behandlingId);

    @TekniskFeil(feilkode = "FP-818307", feilmelding = "Behandling med id %s mangler beregning", logLevel = LogLevel.ERROR)
    Feil behandlingManglerBeregning(Long behandlingId);

    @TekniskFeil(feilkode = "FP-912967", feilmelding = "Mer enn en implementasjon funnet av RevurderingTjeneste for fagsakYtelseType=%s ", logLevel = WARN)
    Feil flereImplementasjonerAvRevurderingtjeneste(String fagsakYtelseType );

    @TekniskFeil(feilkode = "FP-912965", feilmelding = "Fant ingen implementasjon av RevurderingTjeneste for fagsakYtelseType=%s ", logLevel = WARN)
    Feil ingenImplementasjonerAvRevurderingtjeneste(String fagsakYtelseType);
}
