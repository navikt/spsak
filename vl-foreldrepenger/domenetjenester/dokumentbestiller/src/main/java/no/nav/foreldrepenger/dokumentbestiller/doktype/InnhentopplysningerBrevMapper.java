package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnhenteOpplysningerDokument;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.BehandlingsTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.InnhentopplysningerConstants;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.PersonstatusKode;
import no.nav.foreldrepenger.integrasjon.dokument.innhentopplysninger.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class InnhentopplysningerBrevMapper implements DokumentTypeMapper {

    public InnhentopplysningerBrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(InnhentopplysningerConstants.JAXB_CLASS, brevdataTypeJAXBElement, InnhentopplysningerConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();
        fagType.setFristDato(finnDatoVerdiAv(Flettefelt.FRIST_DATO, dokumentTypeDataListe));
        fagType.setFritekst(finnVerdiAv(Flettefelt.FRITEKST, dokumentTypeDataListe));
        fagType.setSoknadDato(finnDatoVerdiAv(Flettefelt.SØKNAD_DATO, dokumentTypeDataListe));

        fagType.setBehandlingsType(xmlBehandlingType(dokumentTypeDataListe));
        fagType.setPersonstatus(PersonstatusKode.fromValue(finnVerdiAv(Flettefelt.PERSON_STATUS, dokumentTypeDataListe)));
        fagType.setSokersNavn(finnVerdiAv(InnhenteOpplysningerDokument.FLETTEFELT_SOKERS_NAVN, dokumentTypeDataListe));
        fagType.setYtelseType(xmlYtelseType(dokumentTypeDataListe));

        return fagType;
    }

    private YtelseTypeKode xmlYtelseType(List<DokumentTypeData> dokumentTypeDataListe) {
        String vlKode = finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe);
        return YtelseTypeKode.fromValue(vlKode);
    }

    private BehandlingsTypeKode xmlBehandlingType(List<DokumentTypeData> dokumentTypeDataListe) {
        String vlKode = finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe);
        return InnhentopplysningerBrevMapperUtil.mapToXmlBehandlingsType(vlKode);
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        ObjectFactory of = new ObjectFactory();
        BrevdataType brevdataType = of.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return of.createBrevdata(brevdataType);
    }
}
