package no.nav.foreldrepenger.økonomistøtte;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Grad170;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Attestant180;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Avstemming115;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Oppdrag110;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragSkjemaConstants;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsEnhet120;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.OppdragsLinje150;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Refusjonsinfo156;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TfradragTillegg;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.TkodeStatusLinje;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.ØkonomistøtteFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class ØkonomioppdragMapper {
    private Oppdragskontroll oppdragskontroll;
    private ObjectFactory objectFactory;

    public ØkonomioppdragMapper(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll okoOppdragskontroll) {
        this.oppdragskontroll = okoOppdragskontroll;
        this.objectFactory = new ObjectFactory();
    }

    static String tilSpesialkodetDatoOgKlokkeslett(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        String pattern = "yyyy-MM-dd-HH.mm.ss.SSS";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return dt.format(dtf);
    }

    public Oppdrag mapVedtaksDataToOppdrag(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110 okoOppdrag110) {
        final Oppdrag oppdrag = objectFactory.createOppdrag();
        oppdrag.setOppdrag110(mapOppdrag110(okoOppdrag110));
        return oppdrag;
    }

    public List<String> generateOppdragXML() {
        List<no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110> okoOppdrag110liste = oppdragskontroll.getOppdrag110Liste();
        List<String> oppdragXmlListe = new ArrayList<>();
        String oppdragXml;
        for (no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110 okoOppdrag110 : okoOppdrag110liste) {
            Oppdrag oppdrag = mapVedtaksDataToOppdrag(okoOppdrag110);
            try {
                oppdragXml = JaxbHelper.marshalAndValidateJaxb(OppdragSkjemaConstants.JAXB_CLASS, oppdrag, OppdragSkjemaConstants.XSD_LOCATION);
            } catch (JAXBException | SAXException e) {
                throw ØkonomistøtteFeil.FACTORY.xmlgenereringsfeil(oppdrag.getOppdrag110().getOppdragsId(), e).toException();
            }
            oppdragXmlListe.add(oppdragXml);
        }
        return oppdragXmlListe;
    }

    private Oppdrag110 mapOppdrag110(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110 okoOppdrag110) {
        final Oppdrag110 oppdrag110 = objectFactory.createOppdrag110();
        // TODO (TOPAS): Løsningsbeskrivelse viser at Oppdrag110 er en liste men Økonomi Oppdrag tar bare et Oppdrag110
        String kode = okoOppdrag110.getKodeFagomrade();
        oppdrag110.setKodeAksjon(okoOppdrag110.getKodeAksjon());
        oppdrag110.setKodeEndring(okoOppdrag110.getKodeEndring());
        // TODO (TOPAS): Sjekk vis dette må være enum eller ikke
        oppdrag110.setKodeFagomraade(okoOppdrag110.getKodeFagomrade());
        oppdrag110.setFagsystemId(String.valueOf(okoOppdrag110.getFagsystemId()));
        oppdrag110.setUtbetFrekvens(okoOppdrag110.getUtbetFrekvens());
        oppdrag110.setOppdragGjelderId(okoOppdrag110.getOppdragGjelderId());
        oppdrag110.setSaksbehId(String.valueOf(okoOppdrag110.getSaksbehId()));
        oppdrag110.setAvstemming115(mapAvstemming115(okoOppdrag110.getAvstemming115()));
        oppdrag110.getOppdragsEnhet120().add(mapOppdragsEnhet120(okoOppdrag110.getOppdragsenhet120Liste().get(0)));
        oppdrag110.getOppdragsLinje150().addAll(mapOppdragsLinje150(okoOppdrag110.getOppdragslinje150Liste(), kode));
        oppdrag110.setDatoOppdragGjelderFom(toXmlGregCal(okoOppdrag110.getDatoOppdragGjelderFom()));

        return oppdrag110;
    }

    private Avstemming115 mapAvstemming115(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115 okoAvstemming115) {
        final Avstemming115 avstemming115 = objectFactory.createAvstemming115();

        avstemming115.setKodeKomponent(okoAvstemming115.getKodekomponent());
        avstemming115.setNokkelAvstemming(tilSpesialkodetDatoOgKlokkeslett(okoAvstemming115.getNokkelAvstemming()));
        avstemming115.setTidspktMelding(tilSpesialkodetDatoOgKlokkeslett(okoAvstemming115.getTidspnktMelding()));

        return avstemming115;
    }

    private OppdragsEnhet120 mapOppdragsEnhet120(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragsenhet120 okoOppdragsenhet120) {
        final OppdragsEnhet120 oppdragsEnhet120 = objectFactory.createOppdragsEnhet120();

        oppdragsEnhet120.setTypeEnhet(okoOppdragsenhet120.getTypeEnhet());
        oppdragsEnhet120.setEnhet(okoOppdragsenhet120.getEnhet());
        oppdragsEnhet120.setDatoEnhetFom(toXmlGregCal(okoOppdragsenhet120.getDatoEnhetFom()));

        return oppdragsEnhet120;
    }

    private List<OppdragsLinje150> mapOppdragsLinje150(List<Oppdragslinje150> okoOppdrlinje150Liste, String kode) {
        List<OppdragsLinje150> oppdragsLinje150Liste = new ArrayList<>();
        for (Oppdragslinje150 okoOppdrlinje150 : okoOppdrlinje150Liste) {
            OppdragsLinje150 oppdragsLinje150 = objectFactory.createOppdragsLinje150();
            oppdragsLinje150.setKodeEndringLinje(okoOppdrlinje150.getKodeEndringLinje());
            if (okoOppdrlinje150.gjelderOpphør()) {
                oppdragsLinje150.setKodeStatusLinje(TkodeStatusLinje.fromValue(okoOppdrlinje150.getKodeStatusLinje()));
            }
            if (okoOppdrlinje150.getDatoStatusFom() != null) {
                oppdragsLinje150.setDatoStatusFom(toXmlGregCal(okoOppdrlinje150.getDatoStatusFom()));
            }
            oppdragsLinje150.setVedtakId(okoOppdrlinje150.getVedtakId());
            oppdragsLinje150.setDelytelseId(String.valueOf(okoOppdrlinje150.getDelytelseId()));
            oppdragsLinje150.setKodeKlassifik(okoOppdrlinje150.getKodeKlassifik());
            oppdragsLinje150.setDatoVedtakFom(toXmlGregCal(okoOppdrlinje150.getDatoVedtakFom()));
            oppdragsLinje150.setDatoVedtakTom(toXmlGregCal(okoOppdrlinje150.getDatoVedtakTom()));
            oppdragsLinje150.setSats(new BigDecimal(okoOppdrlinje150.getSats()));
            oppdragsLinje150.setFradragTillegg(TfradragTillegg.fromValue(okoOppdrlinje150.getFradragTillegg()));
            oppdragsLinje150.setTypeSats(okoOppdrlinje150.getTypeSats());
            oppdragsLinje150.setBrukKjoreplan(okoOppdrlinje150.getBrukKjoreplan());
            oppdragsLinje150.setSaksbehId(okoOppdrlinje150.getSaksbehId());
            oppdragsLinje150.setUtbetalesTilId(okoOppdrlinje150.getUtbetalesTilId());
            oppdragsLinje150.setHenvisning(String.valueOf(okoOppdrlinje150.getHenvisning()));
            if (okoOppdrlinje150.getRefFagsystemId() != null) {
                oppdragsLinje150.setRefFagsystemId(String.valueOf(okoOppdrlinje150.getRefFagsystemId()));
            }
            if (okoOppdrlinje150.getRefDelytelseId() != null) {
                oppdragsLinje150.setRefDelytelseId(String.valueOf(okoOppdrlinje150.getRefDelytelseId()));
            }
            oppdragsLinje150.getAttestant180().add(mapAttestant180(okoOppdrlinje150.getAttestant180Liste().get(0)));
            if (!kode.equals(ØkonomiKodeFagområde.REFUTG.name())) {
                setGrad170OgRefusjonsinfo156(kode, okoOppdrlinje150, oppdragsLinje150);
            }
            oppdragsLinje150Liste.add(oppdragsLinje150);
        }
        return oppdragsLinje150Liste;
    }

    private void setGrad170OgRefusjonsinfo156(String kode, Oppdragslinje150 okoOppdrlinje150, OppdragsLinje150 oppdragsLinje150) {
        if (!okoOppdrlinje150.getGrad170Liste().isEmpty()) {
            oppdragsLinje150.getGrad170().add(mapGrad170(okoOppdrlinje150.getGrad170Liste().get(0)));
        }
        if (kode.equals(ØkonomiKodeFagområde.FPREF.name())) {
            oppdragsLinje150.setRefusjonsinfo156(mapRefusjonInfo156(okoOppdrlinje150.getRefusjonsinfo156()));
        }
    }

    private Refusjonsinfo156 mapRefusjonInfo156(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Refusjonsinfo156 okoRefusjonsInfo156) {
        final Refusjonsinfo156 refusjonsinfo156 = objectFactory.createRefusjonsinfo156();

        refusjonsinfo156.setMaksDato(toXmlGregCal(okoRefusjonsInfo156.getMaksDato()));
        refusjonsinfo156.setDatoFom(toXmlGregCal(okoRefusjonsInfo156.getDatoFom()));
        refusjonsinfo156.setRefunderesId(okoRefusjonsInfo156.getRefunderesId());

        return refusjonsinfo156;
    }

    private no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Grad170 mapGrad170(Grad170 okoGrad170) {
        final no.nav.foreldrepenger.integrasjon.økonomistøtte.oppdrag.Grad170 grad170 = objectFactory.createGrad170();

        grad170.setGrad(BigInteger.valueOf(okoGrad170.getGrad()));
        grad170.setTypeGrad(okoGrad170.getTypeGrad());

        return grad170;
    }

    private Attestant180 mapAttestant180(no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Attestant180 okoAttestant180) {
        final Attestant180 attestant180 = objectFactory.createAttestant180();

        attestant180.setAttestantId(okoAttestant180.getAttestantId());

        return attestant180;
    }

    private XMLGregorianCalendar toXmlGregCal(LocalDate dato) {
        return dato != null ? DateUtil.convertToXMLGregorianCalendarRemoveTimezone(dato) : null;
    }
}
