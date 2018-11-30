package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "HistorikkEndretFeltVerdiType")
@DiscriminatorValue(HistorikkEndretFeltVerdiType.DISCRIMINATOR)
public class HistorikkEndretFeltVerdiType extends Kodeliste {
    public static final String DISCRIMINATOR = "HISTORIKK_ENDRET_FELT_VERDI_TYPE"; //$NON-NLS-1$

    public static final HistorikkEndretFeltVerdiType UDEFINIERT = new HistorikkEndretFeltVerdiType("-");

    public static final HistorikkEndretFeltVerdiType ADOPTERER_ALENE = new HistorikkEndretFeltVerdiType("ADOPTERER_ALENE");
    public static final HistorikkEndretFeltVerdiType ADOPTERER_IKKE_ALENE = new HistorikkEndretFeltVerdiType("ADOPTERER_IKKE_ALENE");
    public static final HistorikkEndretFeltVerdiType ALENEOMSORG = new HistorikkEndretFeltVerdiType("ALENEOMSORG");
    public static final HistorikkEndretFeltVerdiType BOSATT_I_NORGE = new HistorikkEndretFeltVerdiType("BOSATT_I_NORGE");
    public static final HistorikkEndretFeltVerdiType EKTEFELLES_BARN = new HistorikkEndretFeltVerdiType("EKTEFELLES_BARN");
    public static final HistorikkEndretFeltVerdiType FORELDREANSVAR_2_TITTEL = new HistorikkEndretFeltVerdiType("FORELDREANSVAR_2_TITTEL");
    public static final HistorikkEndretFeltVerdiType FORELDREANSVAR_4_TITTEL = new HistorikkEndretFeltVerdiType("FORELDREANSVAR_4_TITTEL");
    public static final HistorikkEndretFeltVerdiType FORTSETT_BEHANDLING = new HistorikkEndretFeltVerdiType("FORTSETT_BEHANDLING");
    public static final HistorikkEndretFeltVerdiType HAR_GYLDIG_GRUNN = new HistorikkEndretFeltVerdiType("HAR_GYLDIG_GRUNN");
    public static final HistorikkEndretFeltVerdiType HAR_IKKE_GYLDIG_GRUNN = new HistorikkEndretFeltVerdiType("HAR_IKKE_GYLDIG_GRUNN");
    public static final HistorikkEndretFeltVerdiType HENLEGG_BEHANDLING = new HistorikkEndretFeltVerdiType("HENLEGG_BEHANDLING");
    public static final HistorikkEndretFeltVerdiType IKKE_ALENEOMSORG = new HistorikkEndretFeltVerdiType("IKKE_ALENEOMSORG");
    public static final HistorikkEndretFeltVerdiType IKKE_BOSATT_I_NORGE = new HistorikkEndretFeltVerdiType("IKKE_BOSATT_I_NORGE");
    public static final HistorikkEndretFeltVerdiType IKKE_EKTEFELLES_BARN = new HistorikkEndretFeltVerdiType("IKKE_EKTEFELLES_BARN");
    public static final HistorikkEndretFeltVerdiType IKKE_LOVLIG_OPPHOLD =  new HistorikkEndretFeltVerdiType("IKKE_LOVLIG_OPPHOLD");
    public static final HistorikkEndretFeltVerdiType IKKE_NY_I_ARBEIDSLIVET = new HistorikkEndretFeltVerdiType("IKKE_NY_I_ARBEIDSLIVET");
    public static final HistorikkEndretFeltVerdiType IKKE_NYOPPSTARTET = new HistorikkEndretFeltVerdiType("IKKE_NYOPPSTARTET");
    public static final HistorikkEndretFeltVerdiType IKKE_OMSORG_FOR_BARNET = new HistorikkEndretFeltVerdiType("IKKE_OMSORG_FOR_BARNET");
    public static final HistorikkEndretFeltVerdiType IKKE_OPPFYLT = new HistorikkEndretFeltVerdiType("IKKE_OPPFYLT");
    public static final HistorikkEndretFeltVerdiType IKKE_OPPHOLDSRETT = new HistorikkEndretFeltVerdiType("IKKE_OPPHOLDSRETT");
    public static final HistorikkEndretFeltVerdiType IKKE_RELEVANT_PERIODE = new HistorikkEndretFeltVerdiType("IKKE_RELEVANT_PERIODE");
    public static final HistorikkEndretFeltVerdiType IKKE_TIDSBEGRENSET_ARBEIDSFORHOLD = new HistorikkEndretFeltVerdiType("IKKE_TIDSBEGRENSET_ARBEIDSFORHOLD");
    public static final HistorikkEndretFeltVerdiType INGEN_VARIG_ENDRING_NAERING = new HistorikkEndretFeltVerdiType("INGEN_VARIG_ENDRING_NAERING");
    public static final HistorikkEndretFeltVerdiType LOVLIG_OPPHOLD =  new HistorikkEndretFeltVerdiType("LOVLIG_OPPHOLD");
    public static final HistorikkEndretFeltVerdiType NY_I_ARBEIDSLIVET = new HistorikkEndretFeltVerdiType("NY_I_ARBEIDSLIVET");
    public static final HistorikkEndretFeltVerdiType NYOPPSTARTET = new HistorikkEndretFeltVerdiType("NYOPPSTARTET");
    public static final HistorikkEndretFeltVerdiType OMSORG_FOR_BARNET = new HistorikkEndretFeltVerdiType("OMSORG_FOR_BARNET");
    public static final HistorikkEndretFeltVerdiType OMSORGSVILKARET_TITTEL = new HistorikkEndretFeltVerdiType("OMSORGSVILKARET_TITTEL");
    public static final HistorikkEndretFeltVerdiType OPPFYLT = new HistorikkEndretFeltVerdiType("OPPFYLT");
    public static final HistorikkEndretFeltVerdiType OPPHOLDSRETT = new HistorikkEndretFeltVerdiType("OPPHOLDSRETT");
    public static final HistorikkEndretFeltVerdiType PERIODE_MEDLEM = new HistorikkEndretFeltVerdiType("PERIODE_MEDLEM");
    public static final HistorikkEndretFeltVerdiType PERIODE_UNNTAK = new HistorikkEndretFeltVerdiType("PERIODE_UNNTAK");
    public static final HistorikkEndretFeltVerdiType TIDSBEGRENSET_ARBEIDSFORHOLD = new HistorikkEndretFeltVerdiType("TIDSBEGRENSET_ARBEIDSFORHOLD");
    public static final HistorikkEndretFeltVerdiType VARIG_ENDRET_NAERING = new HistorikkEndretFeltVerdiType("VARIG_ENDRET_NAERING");
    public static final HistorikkEndretFeltVerdiType VILKAR_IKKE_OPPFYLT = new HistorikkEndretFeltVerdiType("VILKAR_IKKE_OPPFYLT");
    public static final HistorikkEndretFeltVerdiType VILKAR_OPPFYLT = new HistorikkEndretFeltVerdiType("VILKAR_OPPFYLT");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_GRADERING_AVKLARES = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_GRADERING_AVKLARES");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_UTSETTELSE_AVKLARES = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_UTSETTELSE_AVKLARES");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_PERIODEN_AVKLARES_IKKE = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_PERIODEN_AVKLARES_IKKE");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT_IKKE = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT_IKKE");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT_IKKE = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT_IKKE");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_PERIODEN_SYKDOM_DOKUMENTERT");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_PERIODEN_INNLEGGELSEN_DOKUMENTERT");
    public static final HistorikkEndretFeltVerdiType FASTSETT_RESULTAT_ENDRE_SOEKNADSPERIODEN = new HistorikkEndretFeltVerdiType("FASTSETT_RESULTAT_ENDRE_SOEKNADSPERIODEN");
    public static final HistorikkEndretFeltVerdiType DOKUMENTERT = new HistorikkEndretFeltVerdiType("DOKUMENTERT");
    public static final HistorikkEndretFeltVerdiType IKKE_DOKUMENTERT = new HistorikkEndretFeltVerdiType("IKKE_DOKUMENTERT");

    public HistorikkEndretFeltVerdiType() {
        //
    }

    private HistorikkEndretFeltVerdiType(String kode) {
        super(kode, DISCRIMINATOR);
    }

}
