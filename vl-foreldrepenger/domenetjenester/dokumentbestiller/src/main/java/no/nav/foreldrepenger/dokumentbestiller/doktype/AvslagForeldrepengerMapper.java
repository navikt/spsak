package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.REVURDERING;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.SØKNAD;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnStrukturertVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.FlettefeltJsonObjectMapper;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.AarsakListeType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.AvslagForeldrepengerConstants;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.AvslagsAarsakType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.BehandlingsTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.PersonstatusKode;
import no.nav.foreldrepenger.integrasjon.dokument.avslag.foreldrepenger.RelasjonskodeKode;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class AvslagForeldrepengerMapper implements DokumentTypeMapper {
    //Brukt i avslag og opphør brev

    private ObjectFactory objectFactory;

    AvslagForeldrepengerMapper() {
        objectFactory = new ObjectFactory();
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalJaxb(AvslagForeldrepengerConstants.JAXB_CLASS, brevdataTypeJAXBElement);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setBehandlingsType(fra(finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe)));
        fagType.setSokersNavn(finnVerdiAv(Flettefelt.SOKERSNAVN, dokumentTypeDataListe));
        fagType.setPersonstatus(PersonstatusKode.fromValue(finnVerdiAv(Flettefelt.PERSON_STATUS, dokumentTypeDataListe)));
        fagType.setRelasjonskode(tilRelasjonskodeType(finnVerdiAv(Flettefelt.RELASJONSKODE, dokumentTypeDataListe)));
        fagType.setMottattDato(finnDatoVerdiAv(Flettefelt.MOTTATT_DATO, dokumentTypeDataListe));
        fagType.setGjelderFoedsel("true".equals(finnVerdiAv(Flettefelt.GJELDER_FØDSEL, dokumentTypeDataListe)));
        fagType.setAntallBarn(new BigInteger(finnVerdiAv(Flettefelt.ANTALL_BARN, dokumentTypeDataListe)));
        fagType.setBarnErFødt("true".equals(finnVerdiAv(Flettefelt.FØDSELSDATO_PASSERT, dokumentTypeDataListe)));
        fagType.setHalvG(Long.parseLong(finnVerdiAv(Flettefelt.HALV_G, dokumentTypeDataListe)));
        fagType.setAarsakListe(tilAarsakListe(dokumentTypeDataListe));
        fagType.setKlageFristUker(new BigInteger(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe)));
        fagType.setLovhjemmelForAvslag(finnVerdiAv(Flettefelt.LOV_HJEMMEL_FOR_AVSLAG, dokumentTypeDataListe));

        //ikke obligatoriske felter
        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.SISTE_DAG_I_FELLES_PERIODE, dokumentTypeDataListe).ifPresent(fagType::setSisteDagIFellesPeriode);
        finnOptionalVerdiAv(Flettefelt.UKER_ETTER_FELLES_PERIODE, dokumentTypeDataListe).map(BigInteger::new).ifPresent(fagType::setUkerEtterfellesPeriode);
        return fagType;
    }

    private BehandlingsTypeKode fra(String behandlingsType) {
        if (REVURDERING.equals(behandlingsType)) {
            return BehandlingsTypeKode.REVURDERING;
        }
        if (SØKNAD.equals(behandlingsType)) {
            return BehandlingsTypeKode.SØKNAD;
        }
        return BehandlingsTypeKode.FOERSTEGANGSBEHANDLING;
    }


    private AarsakListeType tilAarsakListe(List<DokumentTypeData> dokumentTypeDataListe) {
        final AarsakListeType liste = objectFactory.createAarsakListeType();
        List<PeriodeDto> periodeDtos = new ArrayList<>();
        for (DokumentTypeData data : DokumentTypeFelles.finnListeMedVerdierAv(Flettefelt.PERIODE, dokumentTypeDataListe)) {
            String feltNavnMedIndeks = data.getDoksysId();
            String strukturertFelt = finnStrukturertVerdiAv(feltNavnMedIndeks, dokumentTypeDataListe);
            PeriodeDto dto = FlettefeltJsonObjectMapper.readValue(strukturertFelt, PeriodeDto.class);
            periodeDtos.add(dto);
        }
        if (!periodeDtos.isEmpty()) {
            mapForPerioder(liste, periodeDtos);
        } else {
            mapÅrsakerUtenPerioder(dokumentTypeDataListe, liste);
        }

        return liste;
    }

    private void mapÅrsakerUtenPerioder(List<DokumentTypeData> dokumentTypeDataListe, AarsakListeType liste) {
        final String[] årsaksListe = finnVerdiAv(Flettefelt.AVSLAGSAARSAK, dokumentTypeDataListe).split(",");
        Arrays.stream(årsaksListe)
            .distinct()
            .forEach(årsak -> {
                AvslagsAarsakType avslagsAarsakType = objectFactory.createAvslagsAarsakType();
                avslagsAarsakType.setAvslagsAarsakKode(årsak);
                liste.getAvslagsAarsak().add(avslagsAarsakType);
            });
    }

    private void mapForPerioder(AarsakListeType liste, List<PeriodeDto> periodeDtos) {
        periodeDtos.forEach(dto -> {
            AvslagsAarsakType periode = objectFactory.createAvslagsAarsakType();
            periode.setAvslagsAarsakKode(dto.getÅrsak());
            periode.setPeriodeFom(finnDatoVerdiAvUtenTidSone(dto.getPeriodeFom()));
            periode.setPeriodeTom(finnDatoVerdiAvUtenTidSone(dto.getPeriodeTom()));
            liste.getAvslagsAarsak().add(periode);
        });
    }

    private RelasjonskodeKode tilRelasjonskodeType(String brukerRolle) {
        if (RelasjonsRolleType.MORA.getKode().equals(brukerRolle)) {
            return RelasjonskodeKode.MOR;
        } else if (RelasjonsRolleType.FARA.getKode().equals(brukerRolle)) {
            return RelasjonskodeKode.FAR;
        } else if (RelasjonsRolleType.MEDMOR.getKode().equals(brukerRolle)) {
            return RelasjonskodeKode.MEDMOR;
        }
        return RelasjonskodeKode.ANNET;
    }

    private JAXBElement<BrevdataType> mapintoBrevdataType(FellesType fellesType, FagType fagType) {
        BrevdataType brevdataType = objectFactory.createBrevdataType();
        brevdataType.setFag(fagType);
        brevdataType.setFelles(fellesType);
        return objectFactory.createBrevdata(brevdataType);
    }

}
