package no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.beregningsresultat.BeregningsresultatXmlTjeneste;
import no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.vilkår.VilkårsgrunnlagXmlTjeneste;

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
