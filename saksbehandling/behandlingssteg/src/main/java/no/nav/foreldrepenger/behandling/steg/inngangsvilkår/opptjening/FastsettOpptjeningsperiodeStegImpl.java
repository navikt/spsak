package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.opptjening;

import static java.util.Collections.singletonList;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.inngangsvilkår.InngangsvilkårStegImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelOrkestrerer;
import no.nav.foreldrepenger.domene.inngangsvilkaar.RegelResultat;
import no.nav.foreldrepenger.domene.inngangsvilkaar.regelmodell.opptjening.OpptjeningsPeriode;


@BehandlingStegRef(kode = "VURDER_OPPTJ_PERIODE")
@BehandlingTypeRef
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class FastsettOpptjeningsperiodeStegImpl extends InngangsvilkårStegImpl {

    private static List<VilkårType> STØTTEDE_VILKÅR = singletonList(
        VilkårType.OPPTJENINGSPERIODEVILKÅR
    );
    private final GrunnlagRepositoryProvider repositoryProvider;

    private OpptjeningRepository opptjeningRepository;
    private ResultatRepositoryProvider resultatRepositoryProvider;

    @Inject
    public FastsettOpptjeningsperiodeStegImpl(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider,
                                              RegelOrkestrerer regelOrkestrerer) {
        super(repositoryProvider, regelOrkestrerer, BehandlingStegType.FASTSETT_OPPTJENINGSPERIODE);
        this.repositoryProvider = repositoryProvider;
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
        this.resultatRepositoryProvider = resultatRepositoryProvider;
    }

    @Override
    protected void utførtRegler(BehandlingskontrollKontekst kontekst, Behandling behandling, RegelResultat regelResultat) {
        OpptjeningsPeriode op = (OpptjeningsPeriode) regelResultat.getEkstraResultater().get(VilkårType.OPPTJENINGSPERIODEVILKÅR);
        if (op == null) {
            throw new IllegalArgumentException(
                "Utvikler-feil: finner ikke resultat etter evaluering av Inngangsvilkår/Opptjening:" + behandling.getId());
        }
        Opptjening opptjening = opptjeningRepository.lagreOpptjeningsperiode(behandling.getBehandlingsresultat(), op.getOpptjeningsperiodeFom(), op.getOpptjeningsperiodeTom());
        if (opptjening == null) {
            throw new IllegalArgumentException(
                "Utvikler-feil: får ikke persistert ny opptjeningsperiode:" + behandling.getId());
        }
    }

    @Override
    public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType skalTil) {
        if (transisjonType.equals(TransisjonType.HOPP_OVER_BAKOVER)) {
            if (!(førsteSteg!= null && førsteSteg.equals(BehandlingStegType.FASTSETT_OPPTJENINGSPERIODE) && skalTil.equals(TransisjonType.ETTER_UTGANG))) {
                new RyddOpptjening(repositoryProvider, resultatRepositoryProvider, behandling, kontekst).ryddOpp();
            }
        }
    }

    @Override
    public void vedHoppOverBakover(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, BehandlingStegType tilSteg, BehandlingStegType fraSteg) {
        new RyddOpptjening(repositoryProvider, resultatRepositoryProvider, behandling, kontekst).ryddOpp();
    }

    @Override
    public List<VilkårType> vilkårHåndtertAvSteg() {
        return STØTTEDE_VILKÅR;
    }
}
