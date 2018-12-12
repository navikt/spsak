package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import java.time.Period;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Definerer mulige Aksjonspunkter inkludert hvilket Vurderingspunkt de må løses i.
 * Inkluderer også konstanter for å enklere kunne referere til dem i eksisterende logikk.
 */
@Entity(name = "AksjonspunktDef")
@Table(name = "AKSJONSPUNKT_DEF")
public class AksjonspunktDefinisjon extends KodeverkTabell {

    /**
     * NB: Kun kodeverdi skal defineres på konstanter, ingen ekstra felter som skal ligge i databasen som frist eller
     * annet. Disse brukes kun til skriving.
     */

    public static final AksjonspunktDefinisjon AUTO_MANUELT_SATT_PÅ_VENT = new AksjonspunktDefinisjon("7001"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_VENTER_PÅ_KOMPLETT_SØKNAD = new AksjonspunktDefinisjon("7003"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_SATT_PÅ_VENT_REVURDERING = new AksjonspunktDefinisjon("7005"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_VENT_PÅ_OPPTJENINGSOPPLYSNINGER = new AksjonspunktDefinisjon("7006"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_VENT_KOMPLETT_OPPDATERING = new AksjonspunktDefinisjon("7009"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_VENT_PÅ_REGISTEROPPLYSNINGER = new AksjonspunktDefinisjon("7010"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_KØET_BEHANDLING = new AksjonspunktDefinisjon("7011"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AUTO_VENT_PÅ_INNTEKT_RAPPORTERINGSFRIST  = new AksjonspunktDefinisjon("7014"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon AVKLAR_FAKTA_FOR_PERSONSTATUS = new AksjonspunktDefinisjon("5022"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE = new AksjonspunktDefinisjon("5021"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_LOVLIG_OPPHOLD = new AksjonspunktDefinisjon("5019"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_OM_ER_BOSATT = new AksjonspunktDefinisjon("5020"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_OPPHOLDSRETT = new AksjonspunktDefinisjon("5023"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_TILLEGGSOPPLYSNINGER = new AksjonspunktDefinisjon("5009"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_VERGE = new AksjonspunktDefinisjon("5030"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE = new AksjonspunktDefinisjon("5031"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon AVKLAR_FORTSATT_MEDLEMSKAP = new AksjonspunktDefinisjon("5053"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon FATTER_VEDTAK = new AksjonspunktDefinisjon("5016"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon FORESLÅ_VEDTAK = new AksjonspunktDefinisjon("5015"); // $NON-NL //$NON-NLS-1$
    public static final AksjonspunktDefinisjon FORESLÅ_VEDTAK_MANUELT = new AksjonspunktDefinisjon("5028");


    public static final AksjonspunktDefinisjon MANUELL_VURDERING_AV_MEDLEMSKAP = new AksjonspunktDefinisjon("5010"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon OVERSTYRING_AV_BEREGNING = new AksjonspunktDefinisjon("6007"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET = new AksjonspunktDefinisjon("6005"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon OVERSTYRING_AV_OPPTJENINGSVILKÅRET = new AksjonspunktDefinisjon("6011"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon SØKERS_OPPLYSNINGSPLIKT_MANU = new AksjonspunktDefinisjon("5017"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon SØKERS_OPPLYSNINGSPLIKT_OVST = new AksjonspunktDefinisjon("6002"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VARSEL_REVURDERING_ETTERKONTROLL = new AksjonspunktDefinisjon("5025"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon VARSEL_REVURDERING_MANUELL = new AksjonspunktDefinisjon("5026"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon KONTROLLER_REVURDERINGSBEHANDLING = new AksjonspunktDefinisjon("5055"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING = new AksjonspunktDefinisjon("5056"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VEDTAK_UTEN_TOTRINNSKONTROLL = new AksjonspunktDefinisjon("5018"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VENT_PÅ_SCANNING = new AksjonspunktDefinisjon("7007"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon VENT_PGA_FOR_TIDLIG_SØKNAD = new AksjonspunktDefinisjon("7008"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon VENT_PÅ_SØKNAD = new AksjonspunktDefinisjon("7013"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VURDERE_ANNEN_YTELSE_FØR_VEDTAK = new AksjonspunktDefinisjon("5033"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon VURDERE_DOKUMENT_FØR_VEDTAK = new AksjonspunktDefinisjon("5034"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VURDER_OM_VILKÅR_FOR_SYKDOM_OPPFYLT = new AksjonspunktDefinisjon("5044");

    public static final AksjonspunktDefinisjon MANUELL_VURDERING_AV_KLAGE_NFP = new AksjonspunktDefinisjon("5035"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon MANUELL_VURDERING_AV_KLAGE_NK = new AksjonspunktDefinisjon("5036"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon VURDER_INNSYN = new AksjonspunktDefinisjon("5037"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon FASTSETT_BEREGNINGSGRUNNLAG_ARBEIDSTAKER_FRILANS = new AksjonspunktDefinisjon("5038"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon FASTSETT_BEREGNINGSGRUNNLAG_TIDSBEGRENSET_ARBEIDSFORHOLD = new AksjonspunktDefinisjon("5047"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon FASTSETT_BEREGNINGSGRUNNLAG_FOR_SN_NY_I_ARBEIDSLIVET = new AksjonspunktDefinisjon("5049"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VURDER_VARIG_ENDRET_ELLER_NYOPPSTARTET_NÆRING_SELVSTENDIG_NÆRINGSDRIVENDE = new AksjonspunktDefinisjon("5039");
    public static final AksjonspunktDefinisjon VURDER_FAKTA_FOR_ATFL_SN = new AksjonspunktDefinisjon("5058");

    public static final AksjonspunktDefinisjon FASTSETT_BEREGNINGSGRUNNLAG_SELVSTENDIG_NÆRINGSDRIVENDE = new AksjonspunktDefinisjon("5042"); //$NON-NLS-1$

    public static final AksjonspunktDefinisjon VURDER_PERIODER_MED_OPPTJENING = new AksjonspunktDefinisjon("5051"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon VURDER_ARBEIDSFORHOLD = new AksjonspunktDefinisjon("5080"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET = new AksjonspunktDefinisjon("5007"); //$NON-NLS-1$
    public static final AksjonspunktDefinisjon MANUELL_VURDERING_AV_SØKNADSFRIST_FORELDREPENGER = new AksjonspunktDefinisjon("5043");

    // SP - Utenfor MVP - Ikke implementert enda
    public static final AksjonspunktDefinisjon VURDER_ANDRE_YTELSER = new AksjonspunktDefinisjon("5081"); //$NON-NLS-1$

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "aksjonspunkt_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + AksjonspunktType.DISCRIMINATOR
            + "'"))})
    private AksjonspunktType aksjonspunktType = AksjonspunktType.UDEFINERT;

    /**
     * Definerer hvorvidt Aksjonspunktet default krever totrinnsbehandling. Dvs. Beslutter må godkjenne hva
     * Saksbehandler har utført.
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TOTRINN_BEHANDLING_DEFAULT", nullable = false)
    private boolean defaultTotrinnBehandling = false;

    /**
     * Definerer hvorvidt Aksjonspunktet skal lage historikk.
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "LAG_UTEN_HISTORIKK", nullable = false)
    private boolean lagUtenHistorikk = false;

    /**
     * Hvorvidt aksjonspunktet har en frist før det må være løst. Brukes i forbindelse med når Behandling er lagt til
     * Vent.
     */
    @Column(name = "frist_periode")
    private String fristPeriode;

    @ManyToOne()
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "vilkar_type", referencedColumnName = "kode", nullable = true)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VilkårType.DISCRIMINATOR
            + "'"))})
    private VilkårType vilkårType = VilkårType.UDEFINERT;

    @ManyToOne()
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "skjermlenke_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + SkjermlenkeType.DISCRIMINATOR
            + "'"))})
    private SkjermlenkeType skjermlenkeType = SkjermlenkeType.UDEFINERT;

    @ManyToOne
    @JoinColumn(name = "vurderingspunkt", nullable = false)
    private VurderingspunktDefinisjon vurderingspunktDefinisjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "TILBAKEHOPP_VED_GJENOPPTAKELSE", nullable = false)
    private boolean tilbakehoppVedGjenopptakelse;

    AksjonspunktDefinisjon() {
        // for hibernate
    }

    private AksjonspunktDefinisjon(final String kode) {
        super(kode);
    }


    public SkjermlenkeType getSkjermlenkeType() {
        return skjermlenkeType;
    }

    public AksjonspunktType getAksjonspunktType() {
        return Objects.equals(AksjonspunktType.UDEFINERT, aksjonspunktType) ? null : aksjonspunktType;
    }

    public boolean getDefaultTotrinnBehandling() {
        return defaultTotrinnBehandling;
    }

    public boolean getLagUtenHistorikk() {
        return lagUtenHistorikk;
    }

    public String getFristPeriode() {
        return fristPeriode;
    }

    public Period getFristPeriod() {
        return fristPeriode == null ? null : Period.parse(fristPeriode);
    }

    public VilkårType getVilkårType() {
        return Objects.equals(VilkårType.UDEFINERT, vilkårType) ? null : vilkårType;
    }

    public VurderingspunktDefinisjon getVurderingspunktDefinisjon() {
        return vurderingspunktDefinisjon;
    }

    public boolean tilbakehoppVedGjenopptakelse() {
        return tilbakehoppVedGjenopptakelse;
    }

}
