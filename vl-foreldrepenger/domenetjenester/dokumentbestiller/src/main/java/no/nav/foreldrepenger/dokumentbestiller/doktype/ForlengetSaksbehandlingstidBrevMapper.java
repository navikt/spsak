package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.forlenget.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.forlenget.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.forlenget.ForlengetConstants;
import no.nav.foreldrepenger.integrasjon.dokument.forlenget.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.forlenget.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class ForlengetSaksbehandlingstidBrevMapper implements DokumentTypeMapper {

    public ForlengetSaksbehandlingstidBrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(ForlengetConstants.JAXB_CLASS, brevdataTypeJAXBElement, ForlengetConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();
        fagType.setYtelseType(YtelseTypeKode.fromValue(finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe)));
        fagType.setSoknadsdato(finnDatoVerdiAvUtenTidSone(Flettefelt.SOKNAD_DATO, dokumentTypeDataListe));
        Optional<String> uker = finnOptionalVerdiAv(Flettefelt.BEHANDLINGSFRIST_UKER, dokumentTypeDataListe);
        uker.ifPresent(s -> fagType.setBehandlingsfristUker(BigInteger.valueOf(Long.parseLong(s))));
        fagType.setForlengetBehandlingsfrist(Boolean.TRUE.toString().equalsIgnoreCase(finnVerdiAv(Flettefelt.FORLENGET_BEHANDLINGSFRIST, dokumentTypeDataListe)));
        Optional<String> barn = finnOptionalVerdiAv(Flettefelt.ANTALL_BARN, dokumentTypeDataListe);
        barn.ifPresent(s -> fagType.setAntallBarn(BigInteger.valueOf(Long.parseLong(s))));
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
