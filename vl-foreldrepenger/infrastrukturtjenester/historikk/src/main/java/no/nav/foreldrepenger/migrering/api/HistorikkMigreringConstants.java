package no.nav.foreldrepenger.migrering.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;

public class HistorikkMigreringConstants {
    // felt i gamle historikkinnslag
    public static final String AARSAK = "aarsak";
    public static final String BEGRUNNELSE = "begrunnelse";
    public static final String ENDREDE_FELTER = "endredeFelter";
    public static final String ENDRET_FELT = "endretFelt";
    public static final String OPPLYSNING = "opplysning";
    public static final String FAKTA_NAVN = "faktaNavn";
    public static final String FRA_VERDI = "fraVerdi";
    public static final String HENDELSE = "hendelse";
    public static final String NAVN = "navn";
    public static final String PUNKT_NAVN = "punktNavn";
    public static final String RESULTAT = "resultat";
    public static final String SKJERMLENKE = "skjermlenke";
    public static final String TIL_VERDI = "tilVerdi";

    // booleans
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    // skjermlenker
    public static final String ADOPSJON = "adopsjon";
    public static final String BEREGNING = "beregning";
    public static final String DEFAULT = "default";
    public static final String FOEDSEL = "foedsel";
    public static final String MEDLEMSKAP = "medlemskap";
    public static final String FORELDREANSVAR = "foreldreansvar";
    public static final String OMSORG = "omsorg";
    public static final String OPPLYSNINGSPLIKT = "opplysningsplikt";
    public static final String SOEKNADSFRIST = "soeknadsfrist";
    public static final String VEDTAK = "vedtak";
    public static final String OPPTJENING = "opptjening";
    public static final String FAKTA_OM_OMSORG = "Fakta om omsorg";
    public static final String KONTROLL_AV_SAKSOPPLYSNINGER = "kontroll av saksopplysninger";
    public static final String KLAGE_NK = "klage nk";
    public static final String KLAGE_NFP = "klage nfp";

    // annet
    public static final String INNTEKT_FRA = "Inntekt fra ";

    public static final Set<HistorikkEndretFeltType> ENDRET_FELT_BEHANDLINGSPUNKT;

    static {
        Set<HistorikkEndretFeltType> set = new HashSet<>();
        set.add(HistorikkEndretFeltType.ADOPSJONSVILKARET);
        set.add(HistorikkEndretFeltType.FODSELSVILKARET);
        set.add(HistorikkEndretFeltType.FORELDREANSVARSVILKARET);
        set.add(HistorikkEndretFeltType.OMSORGSVILKAR);
        ENDRET_FELT_BEHANDLINGSPUNKT = Collections.unmodifiableSet(set);
    }

    private HistorikkMigreringConstants() {
    }
}
