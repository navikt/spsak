package no.nav.foreldrepenger.beregningsgrunnlag;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.VurderOmSakSkalTilInfotrygdTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveSendTilInfotrygdTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
public class BeregningInfotrygdsakTjeneste {
    private ProsessTaskRepository prosessTaskRepository;
    private FagsakRepository fagsakRepository;
    private VurderOmSakSkalTilInfotrygdTjeneste vurderOmSakSkalTilInfotrygdTjeneste;

    protected BeregningInfotrygdsakTjeneste() {
        // for CDI proxy
    }

    @Inject
    public BeregningInfotrygdsakTjeneste(BehandlingRepositoryProvider repositoryProvider,
                                            VurderOmSakSkalTilInfotrygdTjeneste vurderOmSakSkalTilInfotrygdTjeneste,
                                            ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.vurderOmSakSkalTilInfotrygdTjeneste = vurderOmSakSkalTilInfotrygdTjeneste;
    }

    public Optional<BehandleStegResultat> vurderOgOppdaterSakSomBehandlesAvInfotrygd(Behandling behandling) {
        if (vurderOmSakSkalTilInfotrygdTjeneste.skalForeldrepengersakBehandlesAvInfotrygd(behandling)) {
            fagsakRepository.fagsakSkalBehandlesAvInfotrygd(behandling.getFagsakId());
            oppdaterBeregningsgrunnlagvilkår(behandling);
            ProsessTaskData data = new ProsessTaskData(OpprettOppgaveSendTilInfotrygdTask.TASKTYPE);
            data.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            prosessTaskRepository.lagre(data);
            return Optional.of(BehandleStegResultat.fremoverført(FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK));
        }
        return Optional.empty();
    }

    public void oppdaterBeregningsgrunnlagvilkår(Behandling behandling) {
        VilkårResultat.Builder builder = VilkårResultat
            .builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            .medVilkårResultatType(VilkårResultatType.AVSLÅTT)
            .leggTilVilkårResultat(
                VilkårType.BEREGNINGSGRUNNLAGVILKÅR,
                VilkårUtfallType.IKKE_OPPFYLT,
                null,
                null,
                Avslagsårsak.INGEN_BEREGNINGSREGLER_TILGJENGELIG_I_LØSNINGEN,
                false,
                false,
                null,
                null);
        builder.buildFor(behandling);
    }
}
