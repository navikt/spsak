package no.nav.foreldrepenger.dokumentbestiller.doktype;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentFelles;
import no.nav.foreldrepenger.integrasjon.dokument.felles.FellesType;

public interface DokumentTypeMapper {

    String mapTilBrevXML(FellesType fellesType, DokumentFelles dokumentFelles) throws JAXBException, SAXException;

}
