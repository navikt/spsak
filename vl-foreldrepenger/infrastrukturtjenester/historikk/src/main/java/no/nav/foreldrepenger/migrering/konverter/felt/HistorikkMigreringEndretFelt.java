package no.nav.foreldrepenger.migrering.konverter.felt;

import static no.nav.foreldrepenger.migrering.konverter.HistorikkMigreringKonverter.getNullableString;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.migrering.HistorikkMigreringFeil;
import no.nav.foreldrepenger.migrering.api.HistorikkMigreringConstants;

public class HistorikkMigreringEndretFelt {
    private static final Map<String, HistorikkEndretFeltType> ENDRET_FELT_TYPE_MAP;

    static {
        Map<String, HistorikkEndretFeltType> map = new HashMap<>();
        map.put("Adopsjon", HistorikkEndretFeltType.ADOPSJONSVILKARET);
        map.put("AdopsjonVilkarForm.Adopsjon", HistorikkEndretFeltType.ADOPSJONSVILKARET);
        map.put("OmsorgFaktaForm.Aleneomsorg", HistorikkEndretFeltType.ALENEOMSORG);
        map.put("OmsorgOgForeldreansvarFaktaForm.NrOfChildren", HistorikkEndretFeltType.ANTALL_BARN);
        map.put("TermindatoFaktaForm.AntallBarn", HistorikkEndretFeltType.ANTALL_BARN);
        map.put("ClarifyRegisterInformationForm.CheckInformation", HistorikkEndretFeltType.AVKLARSAKSOPPLYSNINGER);
        map.put("Behandling", HistorikkEndretFeltType.BEHANDLING);
        map.put("Behandlende enhet", HistorikkEndretFeltType.BEHANDLENDE_ENHET);
        map.put("ChangeBehandlendeEnhetModal.BehandlendeEnhet", HistorikkEndretFeltType.BEHANDLENDE_ENHET);
        map.put("DokumentasjonFaktaForm.Omsorgsovertakelsesdato", HistorikkEndretFeltType.OMSORGSOVERTAKELSESDATO);
        map.put("EktefelleFaktaForm.ApplicationInformation", HistorikkEndretFeltType.EKTEFELLES_BARN);
        map.put("ErForeldreansvar2LeddVilkaarOppfyltForm.Foreldreansvar", HistorikkEndretFeltType.FORELDREANSVARSVILKARET);
        map.put("ErForeldreansvar4LeddVilkaarOppfyltForm.Foreldreansvar", HistorikkEndretFeltType.FORELDREANSVARSVILKARET);
        map.put("ErOmsorgVilkaarOppfyltForm.Anvendes", HistorikkEndretFeltType.VILKAR_SOM_ANVENDES);
        map.put("Behandlingspunkt.Soknadsfristvilkaret", HistorikkEndretFeltType.SOKNADSFRIST);
        map.put("ErSoknadsfristVilkaretOppfyltForm.ApplicationInformation", HistorikkEndretFeltType.SOKNADSFRISTVILKARET);
        map.put("Beregningsgrunnlag.AarsinntektPanel.Frilansinntekt", HistorikkEndretFeltType.FRILANS_INNTEKT);
        map.put("FodselVilkarForm.Fodsel", HistorikkEndretFeltType.FODSELSVILKARET);
        map.put("FodselOgTilleggsopplysningerForm.Fodselsdato", HistorikkEndretFeltType.FODSELSDATO);
        map.put("Historikk.Template.7.OverstyrtBeregning", HistorikkEndretFeltType.OVERSTYRT_BEREGNING);
        map.put("Historikk.Template.7.OverstyrtVilkar", HistorikkEndretFeltType.OVERSTYRT_VURDERING);
        map.put("MannAdoptererAleneFaktaForm.ApplicationInformation", HistorikkEndretFeltType.MANN_ADOPTERER);
        map.put("MedlemskapInfoPanel.EOSBorgerMedOppholdsrett", HistorikkEndretFeltType.OPPHOLDSRETT_EOS);
        map.put("MedlemskapInfoPanel.ErSokerBosattINorge", HistorikkEndretFeltType.ER_SOKER_BOSATT_I_NORGE);
        map.put("MedlemskapInfoPanel.IkkeEOSBorgerMedLovligOpphold", HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS);
        map.put("OmsorgFaktaForm.Omsorg", HistorikkEndretFeltType.OMSORG);
        map.put("OmsorgOgForeldreansvarFaktaForm.ApplicationInformation", HistorikkEndretFeltType.OMSORGSVILKAR);
        map.put("SjekkFodselDokForm.DokumentasjonForeligger", HistorikkEndretFeltType.DOKUMENTASJON_FORELIGGER);
        map.put("SjekkFodselDokForm.BrukAntallIYtelsesvedtaket", HistorikkEndretFeltType.BRUK_ANTALL_I_VEDTAKET);
        map.put("SjekkFodselDokForm.BrukAntallISoknad", HistorikkEndretFeltType.BRUK_ANTALL_I_SOKNAD);
        map.put("SjekkFodselDokForm.BrukAntallITPS", HistorikkEndretFeltType.BRUK_ANTALL_I_TPS);
        map.put("SokersOpplysningspliktForm.SokersOpplysningsplikt", HistorikkEndretFeltType.SOKERSOPPLYSNINGSPLIKT);
        map.put("TermindatoFaktaForm.Termindato", HistorikkEndretFeltType.TERMINDATO);
        map.put("TermindatoFaktaForm.UtstedtDato", HistorikkEndretFeltType.UTSTEDTDATO);
        ENDRET_FELT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private static final Map<String, Kodeliste> ENDRET_FELT_VERDI_TYPE_MAP;

    static {
        Map<String, Kodeliste> map = new HashMap<>();
        map.put("adopterer alene", HistorikkEndretFeltVerdiType.ADOPTERER_ALENE);
        map.put("adopterer ikke alene", HistorikkEndretFeltVerdiType.ADOPTERER_IKKE_ALENE);
        map.put("Søker har aleneomsorg for barnet", HistorikkEndretFeltVerdiType.ALENEOMSORG);
        map.put("ektefelles barn", HistorikkEndretFeltVerdiType.EKTEFELLES_BARN);
        map.put("Foreldreansvarsvilkåret §14-17 andre ledd", HistorikkEndretFeltVerdiType.FORELDREANSVAR_2_TITTEL);
        map.put("Foreldreansvarsvilkåret §14-17 fjerde ledd", HistorikkEndretFeltVerdiType.FORELDREANSVAR_4_TITTEL);
        map.put("Fortsett behandling", HistorikkEndretFeltVerdiType.FORTSETT_BEHANDLING);
        map.put("Henlegg behandling", HistorikkEndretFeltVerdiType.HENLEGG_BEHANDLING);
        map.put("Søker har ikke aleneomsorg for barnet", HistorikkEndretFeltVerdiType.IKKE_ALENEOMSORG);
        map.put("ikke ektefelles barn", HistorikkEndretFeltVerdiType.IKKE_EKTEFELLES_BARN);
        map.put("Søker har ikke omsorg for barnet", HistorikkEndretFeltVerdiType.IKKE_OMSORG_FOR_BARNET);
        map.put("ikke oppfylt", HistorikkEndretFeltVerdiType.IKKE_OPPFYLT);
        map.put("Ikke relevant periode", HistorikkEndretFeltVerdiType.IKKE_RELEVANT_PERIODE);
        map.put("Ingen varig endring i næring", HistorikkEndretFeltVerdiType.INGEN_VARIG_ENDRING_NAERING);
        map.put("Søker har omsorg for barnet", HistorikkEndretFeltVerdiType.OMSORG_FOR_BARNET);
        map.put("Omsorgsvilkår §14-17 tredje ledd", HistorikkEndretFeltVerdiType.OMSORGSVILKARET_TITTEL);
        map.put("oppfylt", HistorikkEndretFeltVerdiType.OPPFYLT);
        map.put("Periode med medlemskap", HistorikkEndretFeltVerdiType.PERIODE_MEDLEM);
        map.put("Periode med unntak fra medlemskap", HistorikkEndretFeltVerdiType.PERIODE_UNNTAK);
        map.put("Varig endret næring", HistorikkEndretFeltVerdiType.VARIG_ENDRET_NAERING);
        map.put("Vilkåret er ikke oppfylt", HistorikkEndretFeltVerdiType.VILKAR_IKKE_OPPFYLT);
        map.put("Vilkåret er oppfylt", HistorikkEndretFeltVerdiType.VILKAR_OPPFYLT);
        map.put("Aktivt BOSTNR", PersonstatusType.ABNR);
        map.put("Aktivt", PersonstatusType.ADNR);
        map.put("Bosatt", PersonstatusType.BOSA);
        map.put("Død", PersonstatusType.DØD);
        map.put("Forsvunnet/savnet", PersonstatusType.FOSV);
        map.put("Fødselregistrert", PersonstatusType.FØDR);
        map.put("Ufullstendig fødselsnr", PersonstatusType.UFUL);
        map.put("Uregistrert person", PersonstatusType.UREG);
        map.put("Utgått person annullert tilgang Fnr", PersonstatusType.UTAN);
        map.put("Utgått person", PersonstatusType.UTPE);
        map.put("Utvandret", PersonstatusType.UTVA);
        ENDRET_FELT_VERDI_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private HistorikkMigreringEndretFeltType migreringEndretFeltType;
    private HistorikkEndretFeltType historikkEndretFeltType;
    private String endretFeltNavnVerdi;
    private Kodeliste fraVerdiKodeverk;
    private Kodeliste tilVerdiKodeverk;
    private Object fraVerdiObject;
    private Object tilVerdiObject;

    public HistorikkMigreringEndretFelt(JsonObject endretFelt) {
        String navn = getNullableString(endretFelt, HistorikkMigreringConstants.NAVN);
        if (navn == null) {
            migreringEndretFeltType = HistorikkMigreringEndretFeltType.NULL;
            return;
        }
        parseEndretFeltNavn(navn);
        String fraVerdi = getNullableString(endretFelt, HistorikkMigreringConstants.FRA_VERDI);
        String tilVerdi = getNullableString(endretFelt, HistorikkMigreringConstants.TIL_VERDI);
        fraVerdiKodeverk = parseVerdiKodeverk(historikkEndretFeltType, fraVerdi);
        tilVerdiKodeverk = parseVerdiKodeverk(historikkEndretFeltType, tilVerdi);

        if (tilVerdiKodeverk != null) {
            migreringEndretFeltType = HistorikkMigreringEndretFeltType.KODEVERK;
        } else {
            if (endretFeltNavnVerdi != null) {
                migreringEndretFeltType = HistorikkMigreringEndretFeltType.OBJECT_MED_NAVNVERDI;
            } else {
                migreringEndretFeltType = HistorikkMigreringEndretFeltType.OBJECT;
            }
            fraVerdiObject = parseVerdi(fraVerdi);
            tilVerdiObject = parseVerdi(tilVerdi);
        }
    }

    private void parseEndretFeltNavn(String navn) {
        if (navn.startsWith(HistorikkMigreringConstants.INNTEKT_FRA)) {
            historikkEndretFeltType = HistorikkEndretFeltType.INNTEKT_FRA_ARBEIDSFORHOLD;
            endretFeltNavnVerdi = navn.replace(HistorikkMigreringConstants.INNTEKT_FRA, "");
        } else {
            historikkEndretFeltType = ENDRET_FELT_TYPE_MAP.get(navn);
            if (historikkEndretFeltType == null) {
                throw HistorikkMigreringFeil.FACTORY.ukjentEndretFeltNavn(navn).toException();
            }
        }
    }

    private Kodeliste parseVerdiKodeverk(HistorikkEndretFeltType historikkEndretFeltType, String verdi) {
        if (verdi == null) {
            return null;
        }
        if (HistorikkEndretFeltType.OPPHOLDSRETT_EOS.equals(historikkEndretFeltType)) {
            return HistorikkMigreringConstants.TRUE.equals(verdi) ? HistorikkEndretFeltVerdiType.OPPHOLDSRETT : HistorikkEndretFeltVerdiType.IKKE_OPPHOLDSRETT;
        }
        if (HistorikkEndretFeltType.OPPHOLDSRETT_IKKE_EOS.equals(historikkEndretFeltType)) {
            return HistorikkMigreringConstants.TRUE.equals(verdi) ? HistorikkEndretFeltVerdiType.LOVLIG_OPPHOLD : HistorikkEndretFeltVerdiType.IKKE_LOVLIG_OPPHOLD;
        }
        if (HistorikkEndretFeltType.ER_SOKER_BOSATT_I_NORGE.equals(historikkEndretFeltType)) {
            return HistorikkMigreringConstants.TRUE.equals(verdi) ? HistorikkEndretFeltVerdiType.BOSATT_I_NORGE : HistorikkEndretFeltVerdiType.IKKE_BOSATT_I_NORGE;
        }
        return ENDRET_FELT_VERDI_TYPE_MAP.get(verdi);
    }

    private Object parseVerdi(String verdi) {
        if (HistorikkMigreringConstants.TRUE.equals(verdi)) {
            return Boolean.TRUE;
        }
        if (HistorikkMigreringConstants.FALSE.equals(verdi)) {
            return Boolean.FALSE;
        }
        try {
            return Integer.parseInt(verdi);
        } catch (NumberFormatException e) {
            return verdi;
        }
    }

    private HistorikkMigreringEndretFeltType getMigreringEndretFeltType() {
        return migreringEndretFeltType;
    }

    private String getEndretFeltNavnVerdi() {
        return endretFeltNavnVerdi;
    }

    private Object getFraVerdiObject() {
        return fraVerdiObject;
    }

    private Object getTilVerdiObject() {
        return tilVerdiObject;
    }

    public HistorikkEndretFeltType getHistorikkEndretFeltType() {
        return historikkEndretFeltType;
    }

    private Kodeliste getFraVerdiKodeverk() {
        return fraVerdiKodeverk;
    }

    private Kodeliste getTilVerdiKodeverk() {
        return tilVerdiKodeverk;
    }

    public static void parseEndredeFelter(HistorikkInnslagTekstBuilder tekstBuilder, JsonObject tekstObject) {
        List<JsonObject> endredeFelter = getEndredeFelter(tekstObject);
        endredeFelter.forEach(jsonEndretFelt -> {
            HistorikkMigreringEndretFelt felt = new HistorikkMigreringEndretFelt(jsonEndretFelt);
            if (HistorikkMigreringEndretFeltType.KODEVERK.equals(felt.getMigreringEndretFeltType())) {
                tekstBuilder.medEndretFelt(felt.getHistorikkEndretFeltType(), felt.getFraVerdiKodeverk(), felt.getTilVerdiKodeverk());
            } else if (HistorikkMigreringEndretFeltType.OBJECT_MED_NAVNVERDI.equals(felt.getMigreringEndretFeltType())) {
                tekstBuilder.medEndretFelt(felt.getHistorikkEndretFeltType(), felt.getEndretFeltNavnVerdi(), felt.getFraVerdiObject(), felt.getTilVerdiObject());
            } else if (HistorikkMigreringEndretFeltType.OBJECT.equals(felt.getMigreringEndretFeltType())) {
                tekstBuilder.medEndretFelt(felt.getHistorikkEndretFeltType(), felt.getFraVerdiObject(), felt.getTilVerdiObject());
            }
        });
    }

    public static List<JsonObject> getEndredeFelter(JsonObject tekstObject) {
        JsonArray endredeFelterArray = tekstObject.getJsonArray(HistorikkMigreringConstants.ENDREDE_FELTER);
        return endredeFelterArray.getValuesAs(JsonObject.class);
    }
}
