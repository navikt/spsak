package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnStrukturertVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.FritekstbrevConstants;
import no.nav.foreldrepenger.integrasjon.dokument.fritekstbrev.ObjectFactory;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class FritekstbrevMapper implements DokumentTypeMapper {
    private ObjectFactory objectFactory;

    public FritekstbrevMapper() {
        this.objectFactory = new ObjectFactory();
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(FritekstbrevConstants.JAXB_CLASS, brevdataTypeJAXBElement, FritekstbrevConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setHovedoverskrift(finnVerdiAv(Flettefelt.HOVED_OVERSKRIFT, dokumentTypeDataListe));
        fagType.setBrødtekst(formaterStrukturertVerdi(finnStrukturertVerdiAv(Flettefelt.BRØDTEKST, dokumentTypeDataListe)));
        return fagType;
    }

    private String formaterStrukturertVerdi(String strukturertVerdi) {
        return strukturertVerdi.replaceAll("(\\\\r)?\\\\n", "\n").replaceAll("^\"|\"$", "");
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        BrevdataType brevdataType = objectFactory.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return objectFactory.createBrevdata(brevdataType);
    }
}
