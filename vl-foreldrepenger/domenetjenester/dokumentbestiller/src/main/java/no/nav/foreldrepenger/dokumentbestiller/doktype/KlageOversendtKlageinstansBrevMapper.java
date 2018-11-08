package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.klage.oversendt.klageinstans.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.klage.oversendt.klageinstans.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.klage.oversendt.klageinstans.KlageOversendtKlageinstansConstants;
import no.nav.foreldrepenger.integrasjon.dokument.klage.oversendt.klageinstans.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.klage.oversendt.klageinstans.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class KlageOversendtKlageinstansBrevMapper implements DokumentTypeMapper {

    public KlageOversendtKlageinstansBrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(KlageOversendtKlageinstansConstants.JAXB_CLASS, brevdataTypeJAXBElement, KlageOversendtKlageinstansConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();
        fagType.setYtelseType(YtelseTypeKode.fromValue(finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe)));
        fagType.setMottattDato(finnDatoVerdiAv(Flettefelt.MOTTATT_DATO, dokumentTypeDataListe));
        fagType.setAntallUker(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(Flettefelt.ANTALL_UKER, dokumentTypeDataListe))));
        fagType.setFritekst(finnVerdiAv(Flettefelt.FRITEKST, dokumentTypeDataListe));
        fagType.setFristDato(finnDatoVerdiAv(Flettefelt.FRIST_DATO, dokumentTypeDataListe));
        return fagType;
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        ObjectFactory of = new ObjectFactory();
        BrevdataType brevdataType = of.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return of.createBrevdata(brevdataType);
    }
}
