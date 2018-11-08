package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.BeregningsresultatXmlTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class BehandlingsresultatXmlTjenesteEngangsstønad extends BehandlingsresultatXmlTjeneste {

    public BehandlingsresultatXmlTjenesteEngangsstønad() {
        //For CDI
    }

    @Inject
    public BehandlingsresultatXmlTjenesteEngangsstønad(@FagsakYtelseTypeRef("ES") BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste,
                                                       @FagsakYtelseTypeRef("ES") VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste) {
        super(beregningsresultatXmlTjeneste, vilkårsgrunnlagXmlTjeneste);
    }
}
