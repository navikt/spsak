package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "NaturalYtelseType")
@DiscriminatorValue(NaturalYtelseType.DISCRIMINATOR)
public class NaturalYtelseType extends Kodeliste {
    public static final String DISCRIMINATOR = "NATURAL_YTELSE_TYPE";

    public static final NaturalYtelseType ELEKTRISK_KOMMUNIKASJON = new NaturalYtelseType("ELEKTRISK_KOMMUNIKASJON");
    public static final NaturalYtelseType AKSJER_GRUNNFONDSBEVIS_TIL_UNDERKURS = new NaturalYtelseType("AKSJER_UNDERKURS");
    public static final NaturalYtelseType LOSJI = new NaturalYtelseType("LOSJI");
    public static final NaturalYtelseType KOST_DØGN = new NaturalYtelseType("KOST_DOEGN");
    public static final NaturalYtelseType BESØKSREISER_HJEMMET_ANNET = new NaturalYtelseType("BESOEKSREISER_HJEM");
    public static final NaturalYtelseType KOSTBESPARELSE_I_HJEMMET = new NaturalYtelseType("KOSTBESPARELSE_HJEM");
    public static final NaturalYtelseType RENTEFORDEL_LÅN = new NaturalYtelseType("RENTEFORDEL_LAAN");
    public static final NaturalYtelseType BIL = new NaturalYtelseType("BIL");
    public static final NaturalYtelseType KOST_DAGER = new NaturalYtelseType("KOST_DAGER");
    public static final NaturalYtelseType BOLIG = new NaturalYtelseType("BOLIG");
    public static final NaturalYtelseType SKATTEPLIKTIG_DEL_FORSIKRINGER = new NaturalYtelseType("FORSIKRINGER");
    public static final NaturalYtelseType FRI_TRANSPORT = new NaturalYtelseType("FRI_TRANSPORT");
    public static final NaturalYtelseType OPSJONER = new NaturalYtelseType("OPSJONER");
    public static final NaturalYtelseType TILSKUDD_BARNEHAGEPLASS = new NaturalYtelseType("TILSKUDD_BARNEHAGE");
    public static final NaturalYtelseType ANNET = new NaturalYtelseType("ANNET");
    public static final NaturalYtelseType BEDRIFTSBARNEHAGEPLASS = new NaturalYtelseType("BEDRIFTSBARNEHAGE");
    public static final NaturalYtelseType YRKEBIL_TJENESTLIGBEHOV_KILOMETER = new NaturalYtelseType("YRKESBIL_KILOMETER");
    public static final NaturalYtelseType YRKEBIL_TJENESTLIGBEHOV_LISTEPRIS = new NaturalYtelseType("YRKESBIL_LISTEPRIS");
    public static final NaturalYtelseType INNBETALING_TIL_UTENLANDSK_PENSJONSORDNING = new NaturalYtelseType("UTENLANDSK_PENSJONSORDNING");
    public static final NaturalYtelseType UDEFINERT = new NaturalYtelseType("-");

    NaturalYtelseType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    NaturalYtelseType() {
    }
}
