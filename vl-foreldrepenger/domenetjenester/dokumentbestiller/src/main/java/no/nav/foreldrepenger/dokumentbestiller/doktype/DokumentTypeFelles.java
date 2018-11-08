package no.nav.foreldrepenger.dokumentbestiller.doktype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.vedtak.feil.FeilFactory;

public class DokumentTypeFelles {

    public static final String LANDSKODE_NORGE = "NOR";

    private DokumentTypeFelles() {
    }

    static Optional<String> finnOptionalVerdiAv(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        Optional<DokumentTypeData> felt = dokumentTypeDataListe.stream()
                .filter(dtd -> feltnavn.equalsIgnoreCase(dtd.getDoksysId()))
                .findFirst();
        return felt.isPresent() && felt.get().getVerdi() != null ? Optional.of(felt.get().getVerdi()) : Optional.empty();
    }

    static String finnVerdiAv(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        Optional<String> res = finnOptionalVerdiAv(feltnavn, dokumentTypeDataListe);
        if (!res.isPresent()) {
            throw FeilFactory.create(DokumentBestillerFeil.class).feltManglerVerdi(feltnavn).toException();
        }
        return res.get();
    }

    private static Optional<String> finnOptionalStrukturertVerdiAv(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        Optional<DokumentTypeData> felt = dokumentTypeDataListe.stream()
                .filter(dtd -> feltnavn.equalsIgnoreCase(dtd.getDoksysId()))
                .findFirst();
        return felt.isPresent() && felt.get().getStrukturertVerdi() != null ? Optional.of(felt.get().getStrukturertVerdi()) : Optional.empty();
    }

    static String finnStrukturertVerdiAv(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        Optional<String> res = finnOptionalStrukturertVerdiAv(feltnavn, dokumentTypeDataListe);
        if (!res.isPresent()) {
            throw FeilFactory.create(DokumentBestillerFeil.class).feltManglerVerdi(feltnavn).toException();
        }
        return res.get();
    }


    static List<DokumentTypeData> finnListeMedVerdierAv(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        return dokumentTypeDataListe.stream()
            .filter(dtd -> feltnavn.equalsIgnoreCase(dtd.getDoksysId().split(":")[0]))
            .collect(Collectors.toList());
    }



    static XMLGregorianCalendar finnDatoVerdiAv(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        String datoString = finnVerdiAv(feltnavn, dokumentTypeDataListe);
        return tilXMLformat(datoString);
    }

    static Optional<XMLGregorianCalendar> finnOptionalDatoVerdiAvUtenTidSone(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        Optional<String> optionalDatoString = finnOptionalVerdiAv(feltnavn, dokumentTypeDataListe);
        if (!optionalDatoString.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(finnDatoVerdiAvUtenTidSone(optionalDatoString.get()));
    }

    public static XMLGregorianCalendar finnDatoVerdiAvUtenTidSone(String datoString) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dato = dateFormat.parse(datoString);
            LocalDate localDate = dato.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), -2147483648, -2147483648, -2147483648, -2147483648, -2147483648);
        } catch (ParseException | DatatypeConfigurationException e) {
            throw FeilFactory.create(DokumentBestillerFeil.class).datokonverteringsfeil(datoString, e).toException();
        }
    }

    static XMLGregorianCalendar finnDatoVerdiAvUtenTidSone(String feltnavn, List<DokumentTypeData> dokumentTypeDataListe) {
        return finnDatoVerdiAvUtenTidSone(finnVerdiAv(feltnavn, dokumentTypeDataListe));
    }

    private static XMLGregorianCalendar tilXMLformat(String datoString) {
        try {
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dato = dateFormat.parse(datoString);
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(dato);
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (ParseException | DatatypeConfigurationException e) {
            throw FeilFactory.create(DokumentBestillerFeil.class).datokonverteringsfeil(datoString, e).toException();
        }
    }

    static String fjernNamespaceFra(String xml) {
        return xml.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
        replaceAll(" xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
        .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
        .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
      }
}
