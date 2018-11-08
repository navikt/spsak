package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.klage.avvist.AvvistGrunnKode;
import no.nav.foreldrepenger.integrasjon.dokument.klage.avvist.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.klage.avvist.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.klage.avvist.KlageAvvistConstants;
import no.nav.foreldrepenger.integrasjon.dokument.klage.avvist.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.klage.avvist.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class KlageAvvistBrevMapper implements DokumentTypeMapper {

    private KodeverkRepository kodeverkRepository;

    public KlageAvvistBrevMapper(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalJaxb(KlageAvvistConstants.JAXB_CLASS, brevdataTypeJAXBElement);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = new FagType();
        fagType.setYtelseType(YtelseTypeKode.fromValue(finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe)));
        fagType.setAvvistGrunn(tilAvvistGrunn(finnVerdiAv(Flettefelt.AVVIST_GRUNN, dokumentTypeDataListe)));
        fagType.setKlageFristUker(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe))));
        return fagType;
    }

    private AvvistGrunnKode tilAvvistGrunn(String avvistÅrsak) {
        KlageAvvistÅrsak årsakKode = kodeverkRepository.finn(KlageAvvistÅrsak.class, avvistÅrsak);
        return KlageAvvistÅrsak.KLAGET_FOR_SENT.equals(årsakKode) ? AvvistGrunnKode.ETTER_6_UKER : AvvistGrunnKode.KLAGEUGYLDIG;
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        ObjectFactory of = new ObjectFactory();
        BrevdataType brevdataType = of.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return of.createBrevdata(brevdataType);
    }
}
