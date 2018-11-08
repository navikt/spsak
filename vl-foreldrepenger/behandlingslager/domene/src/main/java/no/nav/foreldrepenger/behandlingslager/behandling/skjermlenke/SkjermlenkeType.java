package no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "SkjermlenkeType")
@DiscriminatorValue(SkjermlenkeType.DISCRIMINATOR)
public class SkjermlenkeType extends Kodeliste {

    public static final String DISCRIMINATOR = "SKJERMLENKE_TYPE"; //$NON-NLS-1$

    public static final SkjermlenkeType UDEFINERT = new SkjermlenkeType("-");
    public static final SkjermlenkeType BEREGNING_ENGANGSSTOENAD = new SkjermlenkeType("BEREGNING_ENGANGSSTOENAD");
    public static final SkjermlenkeType BEREGNING_FORELDREPENGER = new SkjermlenkeType("BEREGNING_FORELDREPENGER");
    public static final SkjermlenkeType FAKTA_OM_ADOPSJON = new SkjermlenkeType("FAKTA_OM_ADOPSJON");
    public static final SkjermlenkeType FAKTA_OM_BEREGNING = new SkjermlenkeType("FAKTA_OM_BEREGNING");
    public static final SkjermlenkeType FAKTA_OM_FOEDSEL = new SkjermlenkeType("FAKTA_OM_FOEDSEL");
    public static final SkjermlenkeType FAKTA_OM_MEDLEMSKAP = new SkjermlenkeType("FAKTA_OM_MEDLEMSKAP");
    public static final SkjermlenkeType FAKTA_FOR_OMSORG = new SkjermlenkeType("FAKTA_FOR_OMSORG");
    public static final SkjermlenkeType FAKTA_FOR_OPPTJENING = new SkjermlenkeType("FAKTA_FOR_OPPTJENING");
    public static final SkjermlenkeType FAKTA_OM_OPPTJENING = new SkjermlenkeType("FAKTA_OM_OPPTJENING");
    public static final SkjermlenkeType KLAGE_BEH_NFP = new SkjermlenkeType("KLAGE_BEH_NFP");
    public static final SkjermlenkeType KLAGE_BEH_NK = new SkjermlenkeType("KLAGE_BEH_NK");
    public static final SkjermlenkeType KONTROLL_AV_SAKSOPPLYSNINGER = new SkjermlenkeType("KONTROLL_AV_SAKSOPPLYSNINGER");
    public static final SkjermlenkeType OPPLYSNINGSPLIKT = new SkjermlenkeType("OPPLYSNINGSPLIKT");
    public static final SkjermlenkeType PUNKT_FOR_ADOPSJON = new SkjermlenkeType("PUNKT_FOR_ADOPSJON");
    public static final SkjermlenkeType PUNKT_FOR_FOEDSEL = new SkjermlenkeType("PUNKT_FOR_FOEDSEL");
    public static final SkjermlenkeType PUNKT_FOR_FORELDREANSVAR = new SkjermlenkeType("PUNKT_FOR_FORELDREANSVAR");
    public static final SkjermlenkeType PUNKT_FOR_MEDLEMSKAP = new SkjermlenkeType("PUNKT_FOR_MEDLEMSKAP");
    public static final SkjermlenkeType PUNKT_FOR_OMSORG = new SkjermlenkeType("PUNKT_FOR_OMSORG");
    public static final SkjermlenkeType PUNKT_FOR_OPPTJENING = new SkjermlenkeType("PUNKT_FOR_OPPTJENING");
    public static final SkjermlenkeType SOEKNADSFRIST = new SkjermlenkeType("SOEKNADSFRIST");
    public static final SkjermlenkeType VEDTAK = new SkjermlenkeType("VEDTAK");
    public static final SkjermlenkeType FAKTA_OM_OMSORG_OG_FORELDREANSVAR = new SkjermlenkeType("FAKTA_OM_OMSORG_OG_FORELDREANSVAR");
    public static final SkjermlenkeType FAKTA_OM_UTTAK = new SkjermlenkeType("FAKTA_OM_UTTAK");
    public static final SkjermlenkeType UTTAK = new SkjermlenkeType("UTTAK");
    public static final SkjermlenkeType FAKTA_OM_ARBEIDSFORHOLD = new SkjermlenkeType("FAKTA_OM_ARBEIDSFORHOLD");
    public static final SkjermlenkeType FAKTA_OM_VERGE = new SkjermlenkeType("FAKTA_OM_VERGE");


    public SkjermlenkeType() {
        //
    }

    public SkjermlenkeType(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
