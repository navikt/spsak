package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Typer av arbeidsforhold.
 * <p>
 * <h3>Kilde: NAV kodeverk</h3>
 * https://modapp.adeo.no/kodeverksklient/viskodeverk/Arbeidsforholdstyper/2
 * <p>
 * <h3>Tjeneste(r) som returnerer dette:</h3>
 * <ul>
 * <li>https://confluence.adeo.no/display/SDFS/tjeneste_v3%3Avirksomhet%3AArbeidsforhold_v3</li>
 * </ul>
 * <h3>Tjeneste(r) som konsumerer dete:</h3>
 * <ul>
 * <li></li>
 * </ul>
 */
@Entity(name = "ArbeidType")
@DiscriminatorValue(ArbeidType.DISCRIMINATOR)
public class ArbeidType extends ArbeidTypeKode {
    public static final String DISCRIMINATOR = "ARBEID_TYPE"; //$NON-NLS-1$

    public static final ArbeidType UDEFINERT = new ArbeidType("-"); //$NON-NLS-1$
    public static final ArbeidType ORDINÆRT_ARBEIDSFORHOLD = new ArbeidType("ORDINÆRT_ARBEIDSFORHOLD"); //$NON-NLS-1$
    public static final ArbeidType FORENKLET_OPPGJØRSORDNING = new ArbeidType("FORENKLET_OPPGJØRSORDNING"); //$NON-NLS-1$
    // FRILANSER er en syntetisk type for overordnet aktivitet som frilanser. F_O_M_M brukes til enkeltoppdrag fra Inntektskomponent og arbeidsforhold avledet derfra
    public static final ArbeidType FRILANSER = new ArbeidType("FRILANSER"); //$NON-NLS-1$
    public static final ArbeidType FRILANSER_OPPDRAGSTAKER_MED_MER = new ArbeidType("FRILANSER_OPPDRAGSTAKER"); //$NON-NLS-1$
    public static final ArbeidType MARITIMT_ARBEIDSFORHOLD = new ArbeidType("MARITIMT_ARBEIDSFORHOLD"); //$NON-NLS-1$
    public static final ArbeidType SELVSTENDIG_NÆRINGSDRIVENDE = new ArbeidType("NÆRING"); //$NON-NLS-1$
    public static final ArbeidType PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD = new ArbeidType("PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD"); //$NON-NLS-1$
    public static final ArbeidType MILITÆR_ELLER_SIVILTJENESTE = new ArbeidType("MILITÆR_ELLER_SIVILTJENESTE"); //$NON-NLS-1$
    public static final ArbeidType VENTELØNN = new ArbeidType("VENTELØNN"); //$NON-NLS-1$
    public static final ArbeidType VENTELØNN_VARTPENGER = new ArbeidType("VENTELØNN_VARTPENGER"); //$NON-NLS-1$
    public static final ArbeidType ETTERLØNN_ARBEIDSGIVER = new ArbeidType("ETTERLØNN_ARBEIDSGIVER"); //$NON-NLS-1$
    public static final ArbeidType LØNN_UNDER_UTDANNING = new ArbeidType("LØNN_UNDER_UTDANNING"); //$NON-NLS-1$
    public static final ArbeidType SLUTTPAKKE = new ArbeidType("SLUTTPAKKE"); //$NON-NLS-1$
    public static final ArbeidType VARTPENGER = new ArbeidType("VARTPENGER"); //$NON-NLS-1$
    public static final ArbeidType ETTERLØNN_SLUTTPAKKE = new ArbeidType("ETTERLØNN_SLUTTPAKKE"); //$NON-NLS-1$
    public static final ArbeidType UTENLANDSK_ARBEIDSFORHOLD = new ArbeidType("UTENLANDSK_ARBEIDSFORHOLD");

    public static final Set<ArbeidType> AA_REGISTER_TYPER = Stream.of(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD,
        ArbeidType.MARITIMT_ARBEIDSFORHOLD, ArbeidType.FORENKLET_OPPGJØRSORDNING).collect(Collectors.toSet());

    private ArbeidType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public ArbeidType() {
        // hibernate
    }

    public boolean erAnnenOpptjening() {
        String skalVises = getJsonField("gui"); //$NON-NLS-1$
        return Boolean.parseBoolean(skalVises);
    }
}
