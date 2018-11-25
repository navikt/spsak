package no.nav.foreldrepenger.behandling.steg.foreslåvedtak;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.revurdering.fp.impl.RevurderingFPBehandlingsresultatutleder;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.Vedtaksbrev;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
class ForeslåVedtakTjenesteForeldrepengerImpl extends ForeslåVedtakTjenesteImpl {

    private UttakRepository uttakRepository;
    private RevurderingFPBehandlingsresultatutleder revurderingFPBehandlingsresultatutleder;
    private BehandlingRepository behandlingRepository;

    protected ForeslåVedtakTjenesteForeldrepengerImpl() {
        //CDI proxy
    }

    @Inject
    ForeslåVedtakTjenesteForeldrepengerImpl(BehandlingRepositoryProvider repositoryProvider, SjekkMotEksisterendeOppgaverTjeneste sjekkMotEksisterendeOppgaverTjeneste,
                                            RevurderingFPBehandlingsresultatutleder revurderingFPBehandlingsresultatutleder) {
        super(repositoryProvider, sjekkMotEksisterendeOppgaverTjeneste);
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.revurderingFPBehandlingsresultatutleder = revurderingFPBehandlingsresultatutleder;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    protected boolean sjekkVilkårAvslått(Behandlingsresultat behandlingsresultat) {
        return behandlingsresultat.isVilkårAvslått() || !minstEnGyldigUttaksPeriode(behandlingsresultat);
    }

    private boolean minstEnGyldigUttaksPeriode(Behandlingsresultat behandlingsresultat) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandlingsresultat.getBehandling());
        return uttakResultat.isPresent() && uttakResultat.get().getGjeldendePerioder().getPerioder().stream().anyMatch(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()));
    }

    @Override
    protected void foreslåAutomatisertVedtak(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        boolean erVarselOmRevurderingSendt = false; // TODO: Hente ut om varsel er sendt
        if (sjekkVilkårAvslått(behandlingsresultat)) {
            vilkårAvslått(behandling, behandlingsresultat, erVarselOmRevurderingSendt);
        } else {
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
            // Må nullstille avslagårsak (for symmetri med setting avslagsårsak ovenfor, hvor avslagårsak kopieres fra et vilkår)
            Optional.ofNullable(behandlingsresultat.getAvslagsårsak()).ifPresent(ufjernetÅrsak -> behandlingsresultat.setAvslagsårsak(Avslagsårsak.UDEFINERT));
            if (behandling.erRevurdering()) {
                revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(behandling, erVarselOmRevurderingSendt);
            }
        }
        BehandlingLås skriveLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, skriveLås);
    }

    private void vilkårAvslått(Behandling behandling, Behandlingsresultat behandlingsresultat, boolean erVarselOmRevurderingSendt) {
        Optional<Vilkår> ikkeOppfyltVilkår = behandlingsresultat.getVilkårResultat().hentIkkeOppfyltVilkår();
        ikkeOppfyltVilkår.ifPresent(vilkår -> behandlingsresultat.setAvslagsårsak(finnAvslagsårsak(vilkår)));
        if (behandling.erRevurdering()) {
            if (behandling.getFagsak().getSkalTilInfotrygd()) {
                Behandlingsresultat.builderEndreEksisterende(behandlingsresultat)
                    .medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
            } else {
                revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(behandling, erVarselOmRevurderingSendt);
            }
        } else {
            if (behandling.getFagsak().getSkalTilInfotrygd()) {
                Behandlingsresultat.builderEndreEksisterende(behandlingsresultat)
                    .medVedtaksbrev(Vedtaksbrev.INGEN)
                    .medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
            } else {
                Behandlingsresultat.builderEndreEksisterende(behandlingsresultat)
                    .medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
            }
        }
    }
}
