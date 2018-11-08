package no.nav.foreldrepenger.behandlingskontroll;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface BehandlingskontrollFeil extends DeklarerteFeil {

    public static final BehandlingskontrollFeil FACTORY = FeilFactory.create(BehandlingskontrollFeil.class);

    @TekniskFeil(feilkode = "FP-143308", feilmelding = "BehandlingId %s er allerede avsluttet, kan ikke henlegges", logLevel = ERROR)
    Feil kanIkkeHenleggeAvsluttetBehandling(Long behandlingId);

    @TekniskFeil(feilkode = "FP-154409", feilmelding = "BehandlingId %s er satt på vent, må aktiveres før den kan henlegges", logLevel = ERROR)
    Feil kanIkkeHenleggeBehandlingPåVent(Long behandlingId);

    @TekniskFeil(feilkode = "FP-105126", feilmelding = "BehandlingId %s har flere enn et aksjonspunkt, hvor aksjonspunktet fører til tilbakehopp ved gjenopptakelse. Kan ikke gjenopptas.", logLevel = ERROR)
    Feil kanIkkeGjenopptaBehandlingFantFlereAksjonspunkterSomMedførerTilbakehopp(Long behandlingId);

    @TekniskFeil(feilkode = "FP-105127", feilmelding = "Utilfredsstilt avhengighet ved oppslag av behandlingssteg: %s, behandlingType %s, fagsakytelsetype %s.", logLevel = WARN)
    Feil utilfredsstiltAvhengighetVedOppslag(BehandlingStegType behandlingStegType, BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType);

    @TekniskFeil(feilkode = "FP-105128", feilmelding = "Ambivalent avhengighet ved oppslag av behandlingssteg: %s, behandlingType %s, fagsakytelsetype %s.", logLevel = WARN)
    Feil ambivalentAvhengighetVedOppslag(BehandlingStegType behandlingStegType, BehandlingType behandlingType, FagsakYtelseType fagsakYtelseType);

}
