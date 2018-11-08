package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;

@FagsakYtelseTypeRef("ES")
@ApplicationScoped
public class BeregningsresultatXmlTjenesteEngangstønad extends BeregningsresultatXmlTjeneste {

    public BeregningsresultatXmlTjenesteEngangstønad() {
    }

    @Inject
    public BeregningsresultatXmlTjenesteEngangstønad(@FagsakYtelseTypeRef("ES") BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste,
                                                     @FagsakYtelseTypeRef("ES") YtelseXmlTjeneste ytelseXmlTjeneste) {
        super(beregningsgrunnlagXmlTjeneste, ytelseXmlTjeneste);
    }

    @Override
    public void setEkstraInformasjonPåBeregningsresultat(Beregningsresultat beregningsresultat, Behandling behandling) {
        //ES trenger ikke noe ekstra iforhold til det som lagres i superklassen.
    }


}
