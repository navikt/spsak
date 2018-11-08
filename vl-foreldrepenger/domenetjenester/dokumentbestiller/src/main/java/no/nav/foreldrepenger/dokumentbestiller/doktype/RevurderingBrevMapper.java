package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalDatoVerdiAvUtenTidSone;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnOptionalVerdiAv;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.DokumentTypeFelles.finnVerdiAv;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.Flettefelt;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.AdvarselKodeKode;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.BrevdataType;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.FagType;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.RevurderingConstants;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.YtelseTypeKode;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class RevurderingBrevMapper implements DokumentTypeMapper {

    public RevurderingBrevMapper() {
    }

    @Override
    public String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException {
        FagType fagType = mapFagType(dokumentFelles.getDokumentTypeDataListe(), fellesType);
        JAXBElement<BrevdataType> brevdataTypeJAXBElement = mapintoBrevdataType(fellesType, fagType);
        String brevXmlMedNamespace = JaxbHelper.marshalJaxb(RevurderingConstants.JAXB_CLASS, brevdataTypeJAXBElement);
        return DokumentTypeFelles.fjernNamespaceFra(brevXmlMedNamespace);
    }

    private FagType mapFagType(List<DokumentTypeData> dokumentTypeDataListe, FellesType fellesType) {
        final FagType fagType = new FagType();
        fagType.setAntallBarn(BigInteger.valueOf(Long.parseLong(finnVerdiAv(Flettefelt.ANTALL_BARN, dokumentTypeDataListe))));
        fagType.setFristDato(finnDatoVerdiAvUtenTidSone(Flettefelt.FRIST_DATO, dokumentTypeDataListe));
        fagType.setYtelseType(YtelseTypeKode.fromValue(finnVerdiAv(Flettefelt.YTELSE_TYPE, dokumentTypeDataListe)));

        finnOptionalDatoVerdiAvUtenTidSone(Flettefelt.TERMIN_DATO, dokumentTypeDataListe).ifPresent(fagType::setTerminDato);
        finnOptionalVerdiAv(Flettefelt.ADVARSEL_KODE, dokumentTypeDataListe).map(AdvarselKodeKode::fromValue).ifPresent(fagType::setAdvarselKode);
        Optional<String> fritekst = finnOptionalVerdiAv(Flettefelt.FRITEKST, dokumentTypeDataListe);
        fritekst.ifPresent(fagType::setFritekst);

        fellesType.setAutomatiskBehandlet(!fritekst.isPresent());
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
