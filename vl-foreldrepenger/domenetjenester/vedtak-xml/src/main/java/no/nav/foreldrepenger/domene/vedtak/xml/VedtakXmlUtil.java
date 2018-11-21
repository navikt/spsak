package no.nav.foreldrepenger.domene.vedtak.xml;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
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
    private static ObjectFactory FELLES_OBJECT_FACTORY = new ObjectFactory();

    private static final DatatypeFactory DATATYPE_FACTORY;
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private VedtakXmlUtil() {
    }

    public static XMLGregorianCalendar convertToXMLGregorianCalendar(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        return DATATYPE_FACTORY.newXMLGregorianCalendar(gregorianCalendar);
    }
    public static XMLGregorianCalendar convertToXMLGregorianCalendarRemoveTimezone(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return DATATYPE_FACTORY.newXMLGregorianCalendar(
            localDate.getYear(),
            localDate.getMonthValue(),
            localDate.getDayOfMonth(),
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED,
            DatatypeConstants.FIELD_UNDEFINED);
    }

    public static Optional<DateOpplysning> lagDateOpplysning(LocalDate localDate) {
        if (localDate == null) {
            return Optional.empty();
        }
        DateOpplysning dateOpplysning = FELLES_OBJECT_FACTORY.createDateOpplysning();
        dateOpplysning.setValue(convertToXMLGregorianCalendarRemoveTimezone(localDate));
        return Optional.of(dateOpplysning);
    }

    public static StringOpplysning lagStringOpplysning(String str) {
        StringOpplysning stringOpplysning = FELLES_OBJECT_FACTORY.createStringOpplysning();
        stringOpplysning.setValue(str);
        return stringOpplysning;
    }

    /**
     * Lager string representasjon av perioden.
     */
    public static StringOpplysning lagStringOpplysningForperiode(Period periode) {
        StringOpplysning stringOpplysning = FELLES_OBJECT_FACTORY.createStringOpplysning();
        if (Objects.nonNull(periode)) {
            stringOpplysning.setValue(periode.toString());
        }
        return stringOpplysning;
    }

    public static DoubleOpplysning lagDoubleOpplysning(double value) {
        DoubleOpplysning doubleOpplysning = FELLES_OBJECT_FACTORY.createDoubleOpplysning();
        doubleOpplysning.setValue(value);
        return doubleOpplysning;
    }

    public static FloatOpplysning lagFloatOpplysning(float value) {
        FloatOpplysning floatOpplysning = FELLES_OBJECT_FACTORY.createFloatOpplysning();
        floatOpplysning.setValue(value);
        return floatOpplysning;
    }

    public static LongOpplysning lagLongOpplysning(long value) {
        LongOpplysning longOpplysning = FELLES_OBJECT_FACTORY.createLongOpplysning();
        longOpplysning.setValue(value);
        return longOpplysning;
    }

    public static PeriodeOpplysning lagPeriodeOpplysning(LocalDate fom, LocalDate tom) {
        PeriodeOpplysning periodeOpplysning = FELLES_OBJECT_FACTORY.createPeriodeOpplysning();
        periodeOpplysning.setFom(convertToXMLGregorianCalendarRemoveTimezone(fom));
        periodeOpplysning.setTom(convertToXMLGregorianCalendarRemoveTimezone(tom));
        return periodeOpplysning;
    }

    public static KodeverksOpplysning lagKodeverkOpplysning(Kodeliste kodeliste) {
        KodeverksOpplysning kodeverksOpplysning = FELLES_OBJECT_FACTORY.createKodeverksOpplysning();
        kodeverksOpplysning.setKode(kodeliste.getKode());
        kodeverksOpplysning.setValue(kodeliste.getNavn());
        return kodeverksOpplysning;
    }

    public static BooleanOpplysning lagBooleanOpplysning(Boolean bool) {
        if (Objects.isNull(bool)) {
            return null;
        }
        BooleanOpplysning booleanOpplysning = FELLES_OBJECT_FACTORY.createBooleanOpplysning();
        booleanOpplysning.setValue(bool);
        return booleanOpplysning;
    }

    public static IntOpplysning lagIntOpplysning(int value) {
        IntOpplysning intOpplysning = FELLES_OBJECT_FACTORY.createIntOpplysning();
        intOpplysning.setValue(value);
        return intOpplysning;
    }

    public static DecimalOpplysning lagDecimalOpplysning(BigDecimal value) {
        DecimalOpplysning decimalOpplysning = FELLES_OBJECT_FACTORY.createDecimalOpplysning();
        decimalOpplysning.setValue(value);
        return decimalOpplysning;
    }

    public static XMLGregorianCalendar tilCalendar(LocalDate localDate) {
        return convertToXMLGregorianCalendarRemoveTimezone(localDate);
    }
    
    public static KodeverksOpplysning lagKodeverksOpplysning(Kodeliste kodeliste) {
        KodeverksOpplysning kodeverksOpplysning = FELLES_OBJECT_FACTORY.createKodeverksOpplysning();
        kodeverksOpplysning.setValue(kodeliste.getNavn());
        kodeverksOpplysning.setKode(kodeliste.getKode());
        kodeverksOpplysning.setKodeverk(kodeliste.getKodeverk());
        return kodeverksOpplysning;
    }

    public static KodeverksOpplysning lagKodeverksOpplysningForAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        KodeverksOpplysning kodeverksOpplysning = FELLES_OBJECT_FACTORY.createKodeverksOpplysning();
        kodeverksOpplysning.setValue(aksjonspunktDefinisjon.getNavn());
        kodeverksOpplysning.setKode(aksjonspunktDefinisjon.getKode());
        kodeverksOpplysning.setKodeverk("AKSJONSPUNKT_DEF");
        return kodeverksOpplysning;
    }
}
