package no.nav.foreldrepenger.behandling.steg.innhentsaksopplysninger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.innhentregisteropplysninger.api.InnhentRegisteropplysningerSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.registerinnhenting.impl.SaksopplysningerFeil;

@BehandlingStegRef(kode = "INPER")
@BehandlingTypeRef("BT-006") //Innsyn
@FagsakYtelseTypeRef
@ApplicationScoped
public class InnhentPersonopplysningStegImpl implements InnhentRegisteropplysningerSteg {

    private BehandlingRepository behandlingRepository;
    private RegisterdataInnhenter registerdataInnhenter;

    InnhentPersonopplysningStegImpl() {
        // for CDI proxy
    }

    @Inject
    public InnhentPersonopplysningStegImpl(GrunnlagRepositoryProvider repositoryProvider,
                                           RegisterdataInnhenter registerdataInnhenter) {
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.registerdataInnhenter = registerdataInnhenter;
    }

    @Override
    public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
        long behandlingId = kontekst.getBehandlingId();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        // TODO (essv): Avklare om vi må hente inn mer info om søker (medsøker, barn, +++)
        Personinfo søkerInfo = registerdataInnhenter.innhentSaksopplysningerForSøker(behandling);
        validerSøkerinfo(behandling, søkerInfo);

        return BehandleStegResultat.utførtUtenAksjonspunkter();
    }

    private void validerSøkerinfo(Behandling behandling, Personinfo søkerInfo) {
        if (søkerInfo == null) {
            throw SaksopplysningerFeil.FACTORY.feilVedOppslagITPS(behandling.getFagsak().getNavBruker().getAktørId().toString())
                .toException();
        }
    }

}
