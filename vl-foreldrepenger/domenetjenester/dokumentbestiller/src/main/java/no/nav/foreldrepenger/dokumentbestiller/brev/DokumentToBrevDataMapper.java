package no.nav.foreldrepenger.dokumentbestiller.brev;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentAdresse;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeRuter;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.IdKodeType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.MottakerAdresseType;
import no.nav.foreldrepenger.integrasjon.dokument.felles.MottakerType;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class DokumentToBrevDataMapper {
    private KodeverkRepository kodeverkRepository;

    public DokumentToBrevDataMapper() {
        // for cdi proxy
    }

    @Inject
    public DokumentToBrevDataMapper(BehandlingRepositoryProvider repositoryProvider) {
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    public Element mapTilBrevdata(DokumentData dokumentData, DokumentFelles dokumentFelles) {
        Element brevXmlElement;
        try {
            FellesType fellesType = mapFellesType(dokumentFelles);
            String brevXml = DokumentTypeRuter.dokumentTypeMapper(dokumentData, kodeverkRepository).mapTilBrevXML(fellesType, dokumentFelles);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(brevXml));
            Document doc = db.parse(is);
            brevXmlElement = doc.getDocumentElement();
        } catch (JAXBException | SAXException | ParserConfigurationException | IOException e) {
            throw FeilFactory.create(no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil.class).xmlgenereringsfeil(dokumentData.getId(), e).toException();
        } catch (InstantiationException | IllegalAccessException e) {
            throw FeilFactory.create(DokumentBestillerFeil.class).annentekniskfeil(dokumentData.getId(), e).toException();
        }
        return brevXmlElement;
    }

    public FellesType mapFellesType(final DokumentFelles dokumentFelles) {
        final FellesType fellesType = new FellesType();
        fellesType.setSpraakkode(DokumentBestillerTjenesteUtil.mapSpråkkode(dokumentFelles.getSpråkkode()));
        fellesType.setFagsaksnummer(dokumentFelles.getSaksnummer().getVerdi());
        if (dokumentFelles.getSignerendeSaksbehandlerNavn() != null) {
            fellesType.setSignerendeSaksbehandler(DokumentBestillerTjenesteUtil.lageSignerendeSaksbehandlerType(dokumentFelles));
        }
        fellesType.setAutomatiskBehandlet(dokumentFelles.getAutomatiskBehandlet());
        fellesType.setSakspart(DokumentBestillerTjenesteUtil.lageSakspartType(dokumentFelles));
        if (dokumentFelles.getSignerendeBeslutterNavn() != null) {
            fellesType.setSignerendeBeslutter(DokumentBestillerTjenesteUtil.lageSignerendeBeslutterType(dokumentFelles));
        }
        fellesType.setMottaker(lageMottakerType(dokumentFelles));
        fellesType.setNavnAvsenderEnhet(dokumentFelles.getNavnAvsenderEnhet());
        fellesType.setNummerAvsenderEnhet(dokumentFelles.getNummerAvsenderEnhet());
        fellesType.setKontaktInformasjon(DokumentBestillerTjenesteUtil.lageKontaktInformasjonType(dokumentFelles));

        try {
            fellesType.setDokumentDato(DateUtil.convertToXMLGregorianCalendarRemoveTimezone(LocalDate.now(FPDateUtil.getOffset())));
        } catch (DatatypeConfigurationException e) {
            throw FeilFactory.create(DokumentBestillerFeil.class).datokonverteringsfeil(LocalDate.now(FPDateUtil.getOffset()).toString(), e).toException();
        }

        return fellesType;
    }

    private MottakerType lageMottakerType(DokumentFelles dokumentFelles) {
        MottakerType mottakerType = new MottakerType();
        mottakerType.setMottakerId(dokumentFelles.getMottakerId());
        mottakerType.setMottakerTypeKode(IdKodeType.PERSON);
        mottakerType.setMottakerNavn(dokumentFelles.getMottakerNavn());
        MottakerAdresseType mottakerAdresseType = new MottakerAdresseType();
        final DokumentAdresse mottakerAdresse = dokumentFelles.getMottakerAdresse();
        mottakerAdresseType.setAdresselinje1(mottakerAdresse.getAdresselinje1());
        mottakerAdresseType.setAdresselinje2(mottakerAdresse.getAdresselinje2());
        mottakerAdresseType.setAdresselinje3(mottakerAdresse.getAdresselinje3());
        mottakerAdresseType.setPostNr(mottakerAdresse.getPostnummer());
        mottakerAdresseType.setPoststed(mottakerAdresse.getPoststed());
        Landkoder land = mottakerAdresse.getLand() == null ? Landkoder.NOR : kodeverkRepository.finn(Landkoder.class, mottakerAdresse.getLand());
        mottakerAdresseType.setLand(land.getNavn());
        mottakerType.setMottakerAdresse(mottakerAdresseType);
        return mottakerType;
    }
}
