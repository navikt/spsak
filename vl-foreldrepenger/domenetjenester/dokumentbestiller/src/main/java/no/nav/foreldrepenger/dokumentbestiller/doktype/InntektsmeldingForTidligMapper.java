package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnListeMedVerdierAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnStrukturertVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.BrevFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FlettefeltJsonObjectMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.FeriePeriodeDto;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.BehandlingsTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.InntektsmeldingForTidligConstants;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.PeriodeListeType;
import no.nav.foreldrepenger.integrasjon.dokument.inntektsmeldingfortidlig.PeriodeType;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class InntektsmeldingForTidligMapper implements DokumentTypeMapper {

    private ObjectFactory objectFactory;

    public InntektsmeldingForTidligMapper() {
        this.objectFactory = new ObjectFactory();
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(InntektsmeldingForTidligConstants.JAXB_CLASS, brevdataTypeJAXBElement, InntektsmeldingForTidligConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setArbeidsgiverNavn(finnVerdiAv(Flettefelt.ARBEIDSGIVER_NAVN, dokumentTypeDataListe));
        fagType.setBehandlingsType(xmlBehandlingType(dokumentTypeDataListe));
        fagType.setMottattDato(finnDatoVerdiAvUtenTidSone(Flettefelt.MOTTATT_DATO, dokumentTypeDataListe));
        fagType.setSokAntallUkerFor(BigInteger.valueOf(Integer.parseInt(finnVerdiAv(Flettefelt.SOK_ANTALL_UKER_FOR, dokumentTypeDataListe))));
        fagType.setPeriodeListe(konverterPeriodeListe(dokumentTypeDataListe));
        return fagType;
    }

    private PeriodeListeType konverterPeriodeListe(List<DokumentTypeData> dokumentTypeDataListe) {
        PeriodeListeType liste = objectFactory.createPeriodeListeType();
        for (DokumentTypeData data : finnListeMedVerdierAv(Flettefelt.PERIODE_LISTE, dokumentTypeDataListe)) {
            String feltNavnMedIndeks = data.getDoksysId();
            String strukturertFelt = finnStrukturertVerdiAv(feltNavnMedIndeks, dokumentTypeDataListe);
            FeriePeriodeDto dto = FlettefeltJsonObjectMapper.readValue(strukturertFelt, FeriePeriodeDto.class);
            PeriodeType periode = objectFactory.createPeriodeType();
            periode.setPeriodeFom(finnDatoVerdiAvUtenTidSone(dto.getFeriePeriodeFom()));
            periode.setPeriodeTom(finnDatoVerdiAvUtenTidSone(dto.getFeriePeriodeTom()));
            liste.getPeriode().add(periode);
        }
        return liste;
    }

    private BehandlingsTypeKode xmlBehandlingType(List<DokumentTypeData> dokumentTypeDataListe) {
        String vlKode = finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe);
        return mapToXmlBehandlingsType(vlKode);
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        BrevdataType brevdataType = objectFactory.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return objectFactory.createBrevdata(brevdataType);
    }

    static BehandlingsTypeKode mapToXmlBehandlingsType(String vlKode) {
        if (Objects.equals(vlKode, BehandlingType.FØRSTEGANGSSØKNAD.getKode())) {
            return BehandlingsTypeKode.FOERSTEGANGSBEHANDLING;
        } else if (Objects.equals(vlKode, BehandlingType.REVURDERING.getKode())) {
            return BehandlingsTypeKode.REVURDERING;
        }
        throw BrevFeil.FACTORY.innhentDokumentasjonKreverGyldigBehandlingstype(vlKode).toException();
    }
}
