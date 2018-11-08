package no.nav.foreldrepenger.domene.ytelsefordeling.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUttakDokumentasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUttakDokumentasjonEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftFaktaForOmsorgVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftStartdatoForPerioden;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;

@ApplicationScoped
public class YtelseFordelingTjenesteImpl implements YtelseFordelingTjeneste {

    private BehandlingRepositoryProvider behandlingRepositoryProvider;
    private YtelsesFordelingRepository ytelsesFordelingRepository;

    YtelseFordelingTjenesteImpl() {
        //CDI
    }

    @Inject
    public YtelseFordelingTjenesteImpl(BehandlingRepositoryProvider behandlingRepositoryProvider) {
        this.behandlingRepositoryProvider = behandlingRepositoryProvider;
        this.ytelsesFordelingRepository = behandlingRepositoryProvider.getYtelsesFordelingRepository();
    }

    @Override
    public YtelseFordelingAggregat hentAggregat(Behandling behandling) {
        return ytelsesFordelingRepository.hentAggregat(behandling);
    }

    @Override
    public Optional<YtelseFordelingAggregat> hentAggregatHvisEksisterer(Behandling behandling) {
        return ytelsesFordelingRepository.hentAggregatHvisEksisterer(behandling);
    }

    @Override
    public void aksjonspunktBekreftFaktaForOmsorg(Behandling behandling, BekreftFaktaForOmsorgVurderingAksjonspunktDto adapter) {
        new BekreftFaktaForOmsorgAksjonspunkt(behandlingRepositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public void overstyrSøknadsperioder(Behandling behandling,
                                        List<OppgittPeriode> overstyrteSøknadsperioder,
                                        List<PeriodeUttakDokumentasjon> dokumentasjonsperioder) {
        OppgittFordelingEntitet oppgittFordelingEntitet = new OppgittFordelingEntitet(overstyrteSøknadsperioder, ytelsesFordelingRepository.hentAggregat(behandling).getOppgittFordeling().getErAnnenForelderInformert());
        if (dokumentasjonsperioder.isEmpty()) {
            ytelsesFordelingRepository.lagreOverstyrtFordeling(behandling, oppgittFordelingEntitet, null);
        } else {
            PerioderUttakDokumentasjonEntitet perioderUttakDokumentasjon = new PerioderUttakDokumentasjonEntitet();
            dokumentasjonsperioder.forEach(perioderUttakDokumentasjon::leggTil);
            ytelsesFordelingRepository.lagreOverstyrtFordeling(behandling, oppgittFordelingEntitet, perioderUttakDokumentasjon);
        }
    }

    @Override
    public boolean erEndret(Behandling origBehandling, Behandling nyBehandling) {
        return ytelsesFordelingRepository.erEndring(origBehandling, nyBehandling);
    }

    @Override
    public EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling) {
        Optional<Long> funnetId = ytelsesFordelingRepository.hentIdPåAktivYtelsesFordeling(behandling);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(YtelseFordelingAggregat.class, id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(YtelseFordelingAggregat.class));
    }

    @Override
    public DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer) {
        Objects.requireNonNull(idDiff.getGrunnlagId1(), "kan ikke diffe når id1 ikke er oppgitt");
        Objects.requireNonNull(idDiff.getGrunnlagId2(), "kan ikke diffe når id2 ikke er oppgitt");

        return ytelsesFordelingRepository.diffResultat(idDiff.getGrunnlagId1(), idDiff.getGrunnlagId2(), ytelseType, kunSporedeEndringer);
    }

    @Override
    public void aksjonspunktAvklarStartdatoForPerioden(Behandling behandling, BekreftStartdatoForPerioden adapter) {
        new BekreftStartdatoForPeriodenAksjonspunkt(behandlingRepositoryProvider).oppdater(behandling, adapter);
    }
}
