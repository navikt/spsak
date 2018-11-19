package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
class ForeslåVedtakTjenesteEngangsstønadImpl extends ForeslåVedtakTjenesteImpl {

    protected ForeslåVedtakTjenesteEngangsstønadImpl() {
        //CDI proxy
    }

    @Inject
    ForeslåVedtakTjenesteEngangsstønadImpl(BehandlingRepositoryProvider provider, SjekkMotEksisterendeOppgaverTjeneste sjekkMotEksisterendeOppgaverTjeneste) {
        super(provider, sjekkMotEksisterendeOppgaverTjeneste);
    }

    @Override
    protected boolean sjekkVilkårAvslått(Behandlingsresultat behandlingsresultat) {
        return behandlingsresultat.isVilkårAvslått();
    }

    @Override
    protected void foreslåAutomatisertVedtak(Behandling behandling) {
        if (behandling.erKlage()) {
            return;
        }
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (sjekkVilkårAvslått(behandlingsresultat)) {
            Optional<Vilkår> ikkeOppfyltVilkår = behandlingsresultat.getVilkårResultat().hentIkkeOppfyltVilkår();
            ikkeOppfyltVilkår.ifPresent(vilkår -> behandlingsresultat.setAvslagsårsak(finnAvslagsårsak(vilkår)));
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
        } else {
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
            // Må nullstille avslagårsak (for symmetri med setting avslagsårsak ovenfor, hvor avslagårsak kopieres fra et vilkår)
            Optional.ofNullable(behandlingsresultat.getAvslagsårsak()).ifPresent(ufjernetÅrsak -> behandlingsresultat.setAvslagsårsak(Avslagsårsak.UDEFINERT));
        }    }
}
