package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.uendretutfall.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.uendretutfall.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.uendretutfall.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.uendretutfall.UendretutfallConstants;
import no.nav.foreldrepenger.integrasjon.dokument.uendretutfall.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class UendretutfallBrevMapper implements DokumentTypeMapper {

    public UendretutfallBrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(UendretutfallConstants.JAXB_CLASS, brevdataTypeJAXBElement, UendretutfallConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();
        fagType.setYtelseType(YtelseTypeKode.fromValue(finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe)));
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
