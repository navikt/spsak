package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.innsyn.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.innsyn.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.innsyn.InnsynConstants;
import no.nav.foreldrepenger.integrasjon.dokument.innsyn.InnsynResultatTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.innsyn.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.innsyn.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class InnsynskravSvarBrevMapper implements DokumentTypeMapper {

    public InnsynskravSvarBrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(InnsynConstants.JAXB_CLASS,
            brevdataTypeJAXBElement, InnsynConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();

        fagType.setKlageFristUker(new BigInteger(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe)));
        fagType.setFritekst(finnVerdiAv(Flettefelt.FRITEKST, dokumentTypeDataListe));
        fagType.setInnsynResultatType(map(finnVerdiAv(Flettefelt.INNSYN_RESULTAT_TYPE, dokumentTypeDataListe)));
        fagType.setYtelseType(xmlYtelseType(dokumentTypeDataListe));

        return fagType;
    }

    private InnsynResultatTypeKode map(String flettefeltValue) {
        if (Objects.equals(flettefeltValue, InnsynResultatType.INNVILGET.getKode())) {
            return InnsynResultatTypeKode.INNVILGET;
        } else if (Objects.equals(flettefeltValue, InnsynResultatType.DELVIS_INNVILGET.getKode())) {
            return InnsynResultatTypeKode.DELVISINNVILGET;
        } else if (Objects.equals(flettefeltValue, InnsynResultatType.AVVIST.getKode())) {
            return InnsynResultatTypeKode.AVVIST;
        }
        throw BrevFeil.FACTORY.innsynskravSvarHarUkjentResultatType(flettefeltValue).toException();
    }

    private YtelseTypeKode xmlYtelseType(List<DokumentTypeData> dokumentTypeDataListe) {
        String vlKode = finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe);
        return YtelseTypeKode.fromValue(vlKode);
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        ObjectFactory of = new ObjectFactory();
        BrevdataType brevdataType = of.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return of.createBrevdata(brevdataType);
    }
}
