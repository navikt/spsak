package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.FØRSTEGANGSSØKNAD;
import static no.nav.foreldrepenger.dokumentbestiller.DokumentMapperKonstanter.REVURDERING;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.AarsakListeType;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.AvslagsAarsakType;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.BehandlingsTypeKode;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.OpphørConstants;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.PersonstatusKode;
import no.nav.foreldrepenger.integrasjon.dokument.opphor.RelasjonskodeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class OpphørbrevMapper implements DokumentTypeMapper {
    private ObjectFactory objectFactory;

    public OpphørbrevMapper() {
        this.objectFactory = new ObjectFactory();
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe());
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalAndValidateJaxb(OpphørConstants.JAXB_CLASS, brevdataTypeJAXBElement, OpphørConstants.XSD_LOCATION);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe) {
        final FagType fagType = objectFactory.createFagType();
        fagType.setBehandlingsType(fra(finnVerdiAv(Flettefelt.BEHANDLINGSTYPE, dokumentTypeDataListe)));
        fagType.setSokersNavn(finnVerdiAv(Flettefelt.SOKERSNAVN, dokumentTypeDataListe));
        fagType.setPersonstatus(PersonstatusKode.fromValue(finnVerdiAv(Flettefelt.PERSON_STATUS, dokumentTypeDataListe)));
        fagType.setRelasjonskode(tilRelasjonskodeType(finnVerdiAv(Flettefelt.RELASJONSKODE, dokumentTypeDataListe)));
        fagType.setGjelderFoedsel(Boolean.TRUE.toString().equals(finnVerdiAv(Flettefelt.GJELDER_FØDSEL, dokumentTypeDataListe)));
        fagType.setAntallBarn(new BigInteger(finnVerdiAv(Flettefelt.ANTALL_BARN, dokumentTypeDataListe)));
        fagType.setSkjaeringstidspunktPassert(Boolean.TRUE.toString().equals(finnVerdiAv(Flettefelt.FØDSELSDATO_PASSERT, dokumentTypeDataListe)));
        fagType.setHalvG(Long.parseLong(finnVerdiAv(Flettefelt.HALV_G, dokumentTypeDataListe)));
        fagType.setAarsakListe(tilAarsakListe(dokumentTypeDataListe));
        fagType.setKlageFristUker(new BigInteger(finnVerdiAv(Flettefelt.KLAGE_FRIST_UKER, dokumentTypeDataListe)));
        fagType.setLovhjemmelForAvslag(finnVerdiAv(Flettefelt.LOV_HJEMMEL_FOR_AVSLAG, dokumentTypeDataListe));

        //Ikke obligatoriske felter
        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.STONADSDATO_FOM, dokumentTypeDataListe).ifPresent(fagType::setFomStonadsdato);
        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.STONADSDATO_TOM, dokumentTypeDataListe).ifPresent(fagType::setTomStonadsdato);
        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.OPPHORDATO, dokumentTypeDataListe).ifPresent(fagType::setOpphorDato);
        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.DODSDATO, dokumentTypeDataListe).ifPresent(fagType::setDodsdato);
        return fagType;
    }

    private BehandlingsTypeKode fra(String behandlingsType) {
        if (REVURDERING.equals(behandlingsType)) {
            return BehandlingsTypeKode.REVURDERING;
        }
        if (FØRSTEGANGSSØKNAD.equals(behandlingsType)) {
            return BehandlingsTypeKode.FOERSTEGANGSBEHANDLING;
        }
        return BehandlingsTypeKode.SØKNAD;
    }

    private AarsakListeType tilAarsakListe(List<DokumentTypeData> dokumentTypeDataListe) {
        final AarsakListeType aarsakListeType = objectFactory.createAarsakListeType();
        final String[] årsaksListe = finnVerdiAv(Flettefelt.AVSLAGSAARSAK, dokumentTypeDataListe).split(",");
        Arrays.stream(årsaksListe).forEach(årsak -> {
            AvslagsAarsakType avslagsAarsakType = objectFactory.createAvslagsAarsakType();
            avslagsAarsakType.setAvslagsAarsakKode(årsak);
            aarsakListeType.getAvslagsAarsak().add(avslagsAarsakType);
        });
        return aarsakListeType;
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
