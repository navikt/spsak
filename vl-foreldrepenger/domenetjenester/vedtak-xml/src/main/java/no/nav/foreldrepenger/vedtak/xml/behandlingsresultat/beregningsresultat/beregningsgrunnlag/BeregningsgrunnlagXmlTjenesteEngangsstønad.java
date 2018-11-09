package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.vedtak.xml.VedtakXmlUtil;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.es.v2.BeregningsgrunnlagEngangsstoenad;
import no.nav.vedtak.felles.xml.vedtak.beregningsgrunnlag.es.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsgrunnlag;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class BeregningsgrunnlagXmlTjenesteEngangsstønad implements BeregningsgrunnlagXmlTjeneste {

    private ObjectFactory beregningObjectFactory;
    private BeregningRepository beregningRepository;

    public BeregningsgrunnlagXmlTjenesteEngangsstønad() {
        //For CDI
    }

    @Inject
    public BeregningsgrunnlagXmlTjenesteEngangsstønad(BehandlingRepositoryProvider repositoryProvider) {
        this.beregningRepository = repositoryProvider.getBeregningRepository();
        this.beregningObjectFactory = new ObjectFactory();
    }

    @Override
    public void setBeregningsgrunnlag(Beregningsresultat beregningsresultat, Behandling behandling) {
        BeregningsgrunnlagEngangsstoenad beregningsgrunnlag = beregningObjectFactory.createBeregningsgrunnlagEngangsstoenad();
        Optional<Beregning> sisteBeregning = beregningRepository.getSisteBeregning(behandling.getId());
        if (sisteBeregning.isPresent()) {
            beregningsgrunnlag.setAntallBarn(VedtakXmlUtil.lagIntOpplysning((int) sisteBeregning.get().getAntallBarn()));
            beregningsgrunnlag.setSats(VedtakXmlUtil.lagLongOpplysning(sisteBeregning.get().getSatsVerdi()));
        }
        Beregningsgrunnlag beregningsgrunnlag1 = new Beregningsgrunnlag();
        beregningsgrunnlag1.getAny().add(beregningsgrunnlag);
        beregningsresultat.setBeregningsgrunnlag(beregningsgrunnlag1);
    }
}
