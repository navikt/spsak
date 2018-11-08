package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.AvslagConstants;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.BehandlingstypeType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.RelasjonskodeType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.VilkaartypeType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class AvslagsbrevMapper implements DokumentTypeMapper {

    public AvslagsbrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(AvslagConstants.JAXB_CLASS, brevdataTypeJAXBElement, AvslagConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();
        fagType.setBehandlingsType(BehandlingstypeType.fromValue(finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe)));
        fagType.setRelasjonsKode(tilRelasjonskodeType(finnVerdiAv(Flettefelt.RELASJONSKODE, dokumentTypeDataListe), finnVerdiAv(Flettefelt.KJØNN, dokumentTypeDataListe)));
        fagType.setGjelderFoedsel("true".equals(finnVerdiAv(Flettefelt.GJELDER_FØDSEL, dokumentTypeDataListe)));
        fagType.setAntallBarn(Integer.parseInt(finnVerdiAv(Flettefelt.ANTALL_BARN, dokumentTypeDataListe)));
        fagType.setSkjaeringstidspunktPassert("true".equals(finnVerdiAv(Flettefelt.FØDSELSDATO_PASSERT, dokumentTypeDataListe)));
        fagType.setAvslagsAarsak(finnVerdiAv(Flettefelt.AVSLAGSAARSAK, dokumentTypeDataListe));
        Optional<String> fritekst = finnOptionalVerdiAv(Flettefelt.FRITEKST, dokumentTypeDataListe);
        fritekst.ifPresent(fagType::setFritekst);
        fagType.setKlageFristUker(Integer.parseInt(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe)));
        fagType.setVilkaarType(VilkaartypeType.fromValue(finnVerdiAv(Flettefelt.VILKÅR_TYPE, dokumentTypeDataListe)));
        return fagType;
    }

    private RelasjonskodeType tilRelasjonskodeType(String brukerRolle, String navBrukerKjønn) {
        if (RelasjonsRolleType.MORA.getKode().equals(brukerRolle)) {
            return RelasjonskodeType.MOR;
        } else if (NavBrukerKjønn.MANN.getKode().equals(navBrukerKjønn)) {
            return RelasjonskodeType.FAR;
        } else {
            return RelasjonskodeType.MEDMOR;
        }
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        ObjectFactory of = new ObjectFactory();
        BrevdataType brevdataType = of.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return of.createBrevdata(brevdataType);
    }
}
