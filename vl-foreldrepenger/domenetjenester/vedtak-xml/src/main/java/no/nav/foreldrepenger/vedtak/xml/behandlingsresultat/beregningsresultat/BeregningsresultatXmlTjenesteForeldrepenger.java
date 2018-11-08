package no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.uttak.UttakXmlTjenesteForeldrepenger;
import no.nav.foreldrepenger.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class BeregningsresultatXmlTjenesteForeldrepenger extends BeregningsresultatXmlTjeneste {

    private UttakXmlTjenesteForeldrepenger uttakXmlTjeneste;

    public BeregningsresultatXmlTjenesteForeldrepenger() {
        //For CDI
    }

    @Inject
    public BeregningsresultatXmlTjenesteForeldrepenger(@FagsakYtelseTypeRef("FP") BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste,
                                                       @FagsakYtelseTypeRef("FP") YtelseXmlTjeneste ytelseXmlTjeneste,
                                                       @FagsakYtelseTypeRef("FP") UttakXmlTjenesteForeldrepenger uttakXmlTjeneste) {
        super(beregningsgrunnlagXmlTjeneste, ytelseXmlTjeneste);
        this.uttakXmlTjeneste = uttakXmlTjeneste;
    }

    @Override
    public void setEkstraInformasjonPåBeregningsresultat(Beregningsresultat beregningsresultat, Behandling behandling) {
        uttakXmlTjeneste.setUttak(beregningsresultat, behandling);

    }

}
