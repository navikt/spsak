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
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.BehandlingsTypeType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.BehandlingsresultatType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.InnvilgetConstants;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.PersonstatusKodeType;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class VedtaksbrevMapper implements DokumentTypeMapper {

    public VedtaksbrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(InnvilgetConstants.JAXB_CLASS, brevdataTypeJAXBElement, InnvilgetConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();

        BehandlingsresultatType behandlingsresultatType = new BehandlingsresultatType();
        behandlingsresultatType.setBelop(Float.parseFloat(finnVerdiAv(Flettefelt.BELØP, dokumentTypeDataListe)));
        fagType.setBehandlingsresultat(behandlingsresultatType);

        String behandlingsType = finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe);
        BehandlingsTypeType behandlingsTypeType = fra(behandlingsType);
        fagType.setBehandlingsType(behandlingsTypeType);

        fagType.setKlageFristUker(Integer.parseInt(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe)));
        fagType.setSokersNavn(finnVerdiAv(Flettefelt.SOKERSNAVN, dokumentTypeDataListe));
        fagType.setPersonstatus(PersonstatusKodeType.fromValue(finnVerdiAv(Flettefelt.PERSON_STATUS, dokumentTypeDataListe)));

        return fagType;
    }

    private BehandlingsTypeType fra(String behandlingsType) {
        if ("REVURDERING".equals(behandlingsType)) {
            return BehandlingsTypeType.REVURDERING;
        }
        if ("MEDHOLD".equals(behandlingsType)) {
            return BehandlingsTypeType.MEDHOLD;
        }
        return BehandlingsTypeType.FOERSTEGANGSBEHANDLING;
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        ObjectFactory of = new ObjectFactory();
        BrevdataType brevdataType = of.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return of.createBrevdata(brevdataType);
    }
}
