package no.nav.foreldrepenger.vedtak.xml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Objects;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.felles.v2.BooleanOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.DateOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.DecimalOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.DoubleOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.FloatOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.IntOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.KodeverksOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.LongOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.ObjectFactory;
import no.nav.vedtak.felles.xml.felles.v2.PeriodeOpplysning;
import no.nav.vedtak.felles.xml.felles.v2.StringOpplysning;

public class VedtakXmlUtil {
    private static ObjectFactory fellesObjectFactory = new ObjectFactory();

    private VedtakXmlUtil() {
    }


    public static Optional<DateOpplysning> lagDateOpplysning(LocalDate localDate) {
        if (localDate == null) {
            return Optional.empty();
        }
        DateOpplysning dateOpplysning = fellesObjectFactory.createDateOpplysning();
        dateOpplysning.setValue(localDate);
        return Optional.of(dateOpplysning);
    }

    public static StringOpplysning lagStringOpplysning(String str) {
        StringOpplysning stringOpplysning = fellesObjectFactory.createStringOpplysning();
        stringOpplysning.setValue(str);
        return stringOpplysning;
    }

    /**
     * Lager string representasjon av perioden.
     */
    public static StringOpplysning lagStringOpplysningForperiode(Period periode) {
        StringOpplysning stringOpplysning = fellesObjectFactory.createStringOpplysning();
        if (Objects.nonNull(periode)) {
            stringOpplysning.setValue(periode.toString());
        }
        return stringOpplysning;
    }

    public static DoubleOpplysning lagDoubleOpplysning(double value) {
        DoubleOpplysning doubleOpplysning = fellesObjectFactory.createDoubleOpplysning();
        doubleOpplysning.setValue(value);
        return doubleOpplysning;
    }

    public static FloatOpplysning lagFloatOpplysning(float value) {
        FloatOpplysning floatOpplysning = fellesObjectFactory.createFloatOpplysning();
        floatOpplysning.setValue(value);
        return floatOpplysning;
    }

    public static LongOpplysning lagLongOpplysning(long value) {
        LongOpplysning longOpplysning = fellesObjectFactory.createLongOpplysning();
        longOpplysning.setValue(value);
        return longOpplysning;
    }

    public static PeriodeOpplysning lagPeriodeOpplysning(LocalDate fom, LocalDate tom) {
        PeriodeOpplysning periodeOpplysning = fellesObjectFactory.createPeriodeOpplysning();
        periodeOpplysning.setFom(fom);
        periodeOpplysning.setTom(tom);
        return periodeOpplysning;
    }

    public static KodeverksOpplysning lagKodeverkOpplysning(Kodeliste kodeliste) {
        KodeverksOpplysning kodeverksOpplysning = fellesObjectFactory.createKodeverksOpplysning();
        kodeverksOpplysning.setKode(kodeliste.getKode());
        kodeverksOpplysning.setValue(kodeliste.getNavn());
        return kodeverksOpplysning;
    }


    public static BooleanOpplysning lagBooleanOpplysning(Boolean bool) {
        if (Objects.isNull(bool)) {
            return null;
        }
        BooleanOpplysning booleanOpplysning = fellesObjectFactory.createBooleanOpplysning();
        booleanOpplysning.setValue(bool);
        return booleanOpplysning;
    }

    public static IntOpplysning lagIntOpplysning(int value) {
        IntOpplysning intOpplysning = fellesObjectFactory.createIntOpplysning();
        intOpplysning.setValue(value);
        return intOpplysning;
    }

    public static DecimalOpplysning lagDecimalOpplysning(BigDecimal value) {
        DecimalOpplysning decimalOpplysning = fellesObjectFactory.createDecimalOpplysning();
        decimalOpplysning.setValue(value);
        return decimalOpplysning;
    }

    public static Calendar tilCalendar(LocalDate localDate) {
        try {
            return DateUtil.convertToXMLGregorianCalendarRemoveTimezone(localDate).toGregorianCalendar();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException("Ugyldig argument for konvertering til calendar", e);
        }
    }

    public static KodeverksOpplysning lagKodeverksOpplysning(Kodeliste kodeliste) {
        KodeverksOpplysning kodeverksOpplysning = fellesObjectFactory.createKodeverksOpplysning();
        kodeverksOpplysning.setValue(kodeliste.getNavn());
        kodeverksOpplysning.setKode(kodeliste.getKode());
        kodeverksOpplysning.setKodeverk(kodeliste.getKodeverk());
        return kodeverksOpplysning;
    }

    public static KodeverksOpplysning lagKodeverksOpplysningForAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        KodeverksOpplysning kodeverksOpplysning = fellesObjectFactory.createKodeverksOpplysning();
        kodeverksOpplysning.setValue(aksjonspunktDefinisjon.getNavn());
        kodeverksOpplysning.setKode(aksjonspunktDefinisjon.getKode());
        kodeverksOpplysning.setKodeverk("AKSJONSPUNKT_DEF");
        return kodeverksOpplysning;
    }
}
