package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.BeregningsresultatXmlTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class BehandlingsresultatXmlTjenesteForeldrepenger extends BehandlingsresultatXmlTjeneste {

    public BehandlingsresultatXmlTjenesteForeldrepenger() {
    }

    @Inject
    public BehandlingsresultatXmlTjenesteForeldrepenger(@FagsakYtelseTypeRef("FP") BeregningsresultatXmlTjeneste beregningsresultatXmlTjeneste,
                                                        @FagsakYtelseTypeRef("FP") VilkårsgrunnlagXmlTjeneste vilkårsgrunnlagXmlTjeneste) {
        super(beregningsresultatXmlTjeneste, vilkårsgrunnlagXmlTjeneste);
    }
}
