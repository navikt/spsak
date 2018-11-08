package no.nav.foreldrepenger.behandling.impl;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.VurderOmSakSkalTilInfotrygdTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class VurderOmSakSkalTilInfotrygdTjenesteImpl implements VurderOmSakSkalTilInfotrygdTjeneste {

    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private Instance<LocalDate> nyeBeregningsregler;

    VurderOmSakSkalTilInfotrygdTjenesteImpl() {
        // CDI
    }

    @Inject
    public VurderOmSakSkalTilInfotrygdTjenesteImpl(SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                   @KonfigVerdi("dato.for.nye.beregningsregler") Instance<LocalDate> nyeBeregningsregler) {
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.nyeBeregningsregler = nyeBeregningsregler;
    }

    @Override
    public boolean skalForeldrepengersakBehandlesAvInfotrygd(Behandling behandling) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);
        return skjæringstidspunkt.isBefore(nyeBeregningsregler.get());
    }
}
