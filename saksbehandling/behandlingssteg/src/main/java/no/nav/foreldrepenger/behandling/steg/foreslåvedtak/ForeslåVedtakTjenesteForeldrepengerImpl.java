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
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
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
        // CDI proxy
    }

    @Inject
    ForeslåVedtakTjenesteForeldrepengerImpl(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider,
                                            SjekkMotEksisterendeOppgaverTjeneste sjekkMotEksisterendeOppgaverTjeneste,
                                            RevurderingFPBehandlingsresultatutleder revurderingFPBehandlingsresultatutleder) {
        super(repositoryProvider, sjekkMotEksisterendeOppgaverTjeneste);
        this.uttakRepository = resultatRepositoryProvider.getUttakRepository();
        this.revurderingFPBehandlingsresultatutleder = revurderingFPBehandlingsresultatutleder;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    protected boolean sjekkVilkårAvslått(Behandlingsresultat behandlingsresultat) {
        return behandlingsresultat.isVilkårAvslått() || !minstEnGyldigUttaksPeriode(behandlingsresultat);
    }

    private boolean minstEnGyldigUttaksPeriode(Behandlingsresultat behandlingsresultat) {
        Optional<UttakResultatEntitet> uttakResultat = uttakRepository.hentUttakResultatHvisEksisterer(behandlingsresultat.getBehandling());
        return uttakResultat.isPresent() && uttakResultat.get().getGjeldendePerioder().getPerioder().stream()
            .anyMatch(p -> PeriodeResultatType.INNVILGET.equals(p.getPeriodeResultatType()));
    }

    @Override
    protected void foreslåAutomatisertVedtak(Behandling behandling) {
        final Behandlingsresultat behandlingsresultat = behandlingRepository.hentResultat(behandling.getId());
        boolean erVarselOmRevurderingSendt = false; // TODO: Hente ut om varsel er sendt
        BehandlingLås skriveLås = behandlingRepository.taSkriveLås(behandling);
        if (sjekkVilkårAvslått(behandlingsresultat)) {
            vilkårAvslått(behandling, behandlingsresultat, erVarselOmRevurderingSendt, skriveLås);
        } else {
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
            if (behandling.erRevurdering()) {
                Behandlingsresultat endeligResultat = revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(behandling, erVarselOmRevurderingSendt);
                behandlingRepository.lagre(endeligResultat, skriveLås);
            } else {
                behandlingRepository.lagre(behandlingsresultat, skriveLås);
            }
        }
        behandlingRepository.lagre(behandling, skriveLås);
    }

    private void vilkårAvslått(Behandling behandling, Behandlingsresultat behandlingsresultat, boolean erVarselOmRevurderingSendt, BehandlingLås skriveLås) {
        if (behandling.erRevurdering()) {
            Behandlingsresultat endeligResultat = revurderingFPBehandlingsresultatutleder.bestemBehandlingsresultatForRevurdering(behandling, erVarselOmRevurderingSendt);
            behandlingRepository.lagre(endeligResultat, skriveLås);
        } else {
            Behandlingsresultat.builderEndreEksisterende(behandlingsresultat)
                .medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
        behandlingRepository.lagre(behandlingsresultat, skriveLås);
        }
    }
}
