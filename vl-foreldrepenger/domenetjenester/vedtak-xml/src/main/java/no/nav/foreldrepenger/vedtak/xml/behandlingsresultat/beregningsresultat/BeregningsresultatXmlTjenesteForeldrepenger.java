package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjeneste;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;

// FIXME SP: Trengs denne? må håndtere perioder evt. (uttak) tidligere brukte den UttakXmlResultatTjeneste...
@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class BeregningsresultatXmlTjenesteForeldrepenger extends BeregningsresultatXmlTjeneste {

    public BeregningsresultatXmlTjenesteForeldrepenger() {
        //For CDI
    }

    @Inject
    public BeregningsresultatXmlTjenesteForeldrepenger(@FagsakYtelseTypeRef("FP") BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste,
                                                       @FagsakYtelseTypeRef("FP") YtelseXmlTjeneste ytelseXmlTjeneste) {
        super(beregningsgrunnlagXmlTjeneste, ytelseXmlTjeneste);
    }

    @Override
    public void setEkstraInformasjonPåBeregningsresultat(Beregningsresultat beregningsresultat, Behandling behandling) {
    }

}
