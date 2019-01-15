package no.nav.foreldrepenger.web.app.tjenester.behandling.opptjening.aksjonspunkt;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;

public interface OverstyringFeil extends DeklarerteFeil {
    OverstyringFeil FACTORY = FeilFactory.create(OverstyringFeil.class);

    @FunksjonellFeil(feilkode = "FP-093923", feilmelding = "Kan ikke overstyre vilkår. Det må være minst en aktivitet for at opptjeningsvilkåret skal kunne overstyres.",
        løsningsforslag = "Sett på vent til det er mulig og manuelt legge inn aktiviteter ved overstyring.", logLevel = WARN)
    Feil opptjeningPreconditionFailed();

}
