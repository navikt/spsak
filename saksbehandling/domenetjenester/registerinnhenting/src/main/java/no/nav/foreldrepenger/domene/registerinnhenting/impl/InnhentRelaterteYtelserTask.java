package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import static no.nav.foreldrepenger.domene.registerinnhenting.impl.InnhentRelaterteYtelserTask.TASKTYPE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollProsessTask;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTaskTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakProsesstaskRekkefølge;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;

@ApplicationScoped
@ProsessTask(TASKTYPE)
@FagsakProsesstaskRekkefølge(gruppeSekvens = true)
public class InnhentRelaterteYtelserTask extends BehandlingskontrollProsessTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnhentRelaterteYtelserTask.class);
    public static final String TASKTYPE = "innhentsaksopplysninger.relaterteYtelser";
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private RegisterdataInnhenter registerdataInnhenter;

    InnhentRelaterteYtelserTask() {
        // for CDI proxy
    }

    @Inject
    public InnhentRelaterteYtelserTask(GrunnlagRepositoryProvider grunnlagRepositoryProvider,
                                       BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                       BehandlingskontrollTaskTjeneste behandlingskontrollTaskTjeneste,
                                       RegisterdataInnhenter registerdataInnhenter) {
        super(grunnlagRepositoryProvider, behandlingskontrollTjeneste, behandlingskontrollTaskTjeneste);
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.registerdataInnhenter = registerdataInnhenter;
    }

    @Override
    protected void prosesser(Behandling behandling) {
        LOGGER.info("Innhenter IAY for behandling: {}", behandling.getId());
        Personinfo søkerInfo = registerdataInnhenter.innhentSaksopplysningerForSøker(behandling);
        registerdataInnhenter.innhentIAYOpplysninger(behandling, søkerInfo);
        registerdataInnhenter.oppdaterSistOppdatertTidspunkt(behandling);

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandling);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER,kontekst);
    }

}
