package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;
import no.nav.vedtak.felles.xml.vedtak.v2.TilkjentYtelse;
import no.nav.vedtak.felles.xml.vedtak.ytelse.es.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.vedtak.ytelse.es.v2.YtelseEngangsstoenad;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class YtelseXmlTjenesteEngangsstønad implements YtelseXmlTjeneste {

    private ObjectFactory ytelseObjectFactory;
    private BeregningRepository beregningRepository;

    public YtelseXmlTjenesteEngangsstønad() {
        //For Cdi
    }

    @Inject
    public YtelseXmlTjenesteEngangsstønad(BehandlingRepositoryProvider repositoryProvider) {
        this.beregningRepository = repositoryProvider.getBeregningRepository();
        ytelseObjectFactory = new ObjectFactory();
    }

    @Override
    public void setYtelse(Beregningsresultat beregningsresultat, Behandling behandling) {
        YtelseEngangsstoenad engangstoenadYtelse = ytelseObjectFactory.createYtelseEngangsstoenad();
        Optional<Beregning> sisteBeregning = beregningRepository.getSisteBeregning(behandling.getId());
        sisteBeregning.ifPresent(beregning -> engangstoenadYtelse.setBeloep(beregning.getBeregnetTilkjentYtelse()));
        TilkjentYtelse tilkjentYtelse = new TilkjentYtelse();
        tilkjentYtelse.getAny().add(engangstoenadYtelse);
        beregningsresultat.setTilkjentYtelse(tilkjentYtelse);

    }
}
