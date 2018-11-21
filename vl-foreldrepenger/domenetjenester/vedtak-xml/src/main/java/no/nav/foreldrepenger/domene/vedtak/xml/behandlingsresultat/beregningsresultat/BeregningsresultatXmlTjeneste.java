package no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.beregningsresultat;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.beregningsresultat.beregningsgrunnlag.BeregningsgrunnlagXmlTjeneste;
import no.nav.foreldrepenger.domene.vedtak.xml.behandlingsresultat.beregningsresultat.ytelse.YtelseXmlTjeneste;
import no.nav.vedtak.felles.xml.vedtak.v2.Behandlingsresultat;
import no.nav.vedtak.felles.xml.vedtak.v2.Beregningsresultat;
import no.nav.vedtak.felles.xml.vedtak.v2.ObjectFactory;

public abstract class BeregningsresultatXmlTjeneste {

    private ObjectFactory v2ObjectFactory = new ObjectFactory();
    private BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste;
    private YtelseXmlTjeneste ytelseXmlTjeneste;


    protected BeregningsresultatXmlTjeneste() {
        //For CDI
    }

    public BeregningsresultatXmlTjeneste(BeregningsgrunnlagXmlTjeneste beregningsgrunnlagXmlTjeneste, YtelseXmlTjeneste ytelseXmlTjeneste) {
        this.beregningsgrunnlagXmlTjeneste = beregningsgrunnlagXmlTjeneste;
        this.ytelseXmlTjeneste = ytelseXmlTjeneste;
    }

    public void setBeregningsresultat(Behandlingsresultat behandlingsresultat, Behandling behandling) {
        Beregningsresultat beregningsresultat = v2ObjectFactory.createBeregningsresultat();
        beregningsgrunnlagXmlTjeneste.setBeregningsgrunnlag(beregningsresultat, behandling);
        ytelseXmlTjeneste.setYtelse(beregningsresultat, behandling);

        //Ta inn elementer fra subklasser
        setEkstraInformasjonPåBeregningsresultat(beregningsresultat, behandling);

        behandlingsresultat.setBeregningsresultat(beregningsresultat);
    }

    public abstract void setEkstraInformasjonPåBeregningsresultat(Beregningsresultat beregningsresultat, Behandling behandling);
}
