package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;


public interface AksjonspunktOppdaterer<T> {

    default OppdateringResultat oppdater(T dto, Behandling behandling, @SuppressWarnings("unused") VilkårResultat.Builder vilkårBuilder) {
        return oppdater(dto, behandling);
    }

    @SuppressWarnings("unused")
    default boolean skalReinnhenteRegisteropplysninger(Behandling behandling, LocalDate forrigeSkjæringstidspunkt) {
        return false;
    }

    // FIXME (TERMITT): refactor hack
    @SuppressWarnings("unused")
    default OppdateringResultat oppdater(T dto, Behandling behandling) {
        throw new UnsupportedOperationException("Du må implementere en av oss!");
    }
}
