package no.nav.foreldrepenger.grensesnittavstemming;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Avstemming115;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdrag110;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.OppdragKvittering;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragslinje150;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.AksjonType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Aksjonsdata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.AvstemmingType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Avstemmingsdata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.DetaljType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Detaljdata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Fortegn;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.GrensesnittavstemmingSkjemaConstants;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Grunnlagsdata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.KildeType;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.ObjectFactory;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Periodedata;
import no.nav.foreldrepenger.integrasjon.økonomistøtte.grensesnittavstemming.Totaldata;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeFagområde;
import no.nav.foreldrepenger.økonomistøtte.api.kodeverk.ØkonomiKodeKomponent;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class GrensesnittavstemmingMapper {
    private ObjectFactory objectFactory;
    private List<Oppdrag110> oppdragsliste;
    private String avstemmingId;
    protected static final String BRUKER_ID_FOR_VEDTAKSLØSNINGEN = "VL";
    private static final int DETALJER_PR_MELDING = 70;
    private String fagområde;

    public GrensesnittavstemmingMapper(List<Oppdrag110> oppdragsliste, String fagområde) {
        if (oppdragsliste == null || oppdragsliste.isEmpty()) {
            throw new IllegalStateException("Grensesnittavstemming uten oppdragsliste er ikke mulig");
        }
        if (!(ØkonomiKodeFagområde.FPREF.name().equals(fagområde) ||
            ØkonomiKodeFagområde.FP.name().equals(fagområde) ||
            ØkonomiKodeFagområde.REFUTG.name().equals(fagområde))) {
            throw new IllegalStateException("Grensesnittavstemming uten fagområde er ikke mulig");
        }

        this.objectFactory = new ObjectFactory();
        this.avstemmingId = encodeUUIDBase64(UUID.randomUUID());
        this.fagområde = fagområde;
        this.oppdragsliste = oppdragsliste.stream().filter(opp -> opp.getKodeFagomrade().equals(fagområde)).collect(Collectors.toList());
    }

    private static String encodeUUIDBase64(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().encodeToString(bb.array()).substring(0, 22);
    }

    public String lagStartmelding() {
        return lagEnkeltAvstemmingsmelding(AksjonType.START);
    }

    public String lagSluttmelding() {
        return lagEnkeltAvstemmingsmelding(AksjonType.AVSL);
    }

    private String lagEnkeltAvstemmingsmelding(AksjonType aksjonType) {
        Avstemmingsdata avstemmingsdata = lagAvstemmingsdataFelles(aksjonType);
        return lagXmlMelding(avstemmingsdata);
    }

    public List<String> lagDatameldinger() {
        List<Avstemmingsdata> avstemmingsdataListe = lagAvstemmingsdataListe();
        List<String> xmlMeldinger = new ArrayList<>(avstemmingsdataListe.size());
        for (Avstemmingsdata avstemmingsdata : avstemmingsdataListe) {
            xmlMeldinger.add(lagXmlMelding(avstemmingsdata));
        }
        return xmlMeldinger;
    }

    List<Avstemmingsdata> lagAvstemmingsdataListe() {
        List<Avstemmingsdata> liste = new ArrayList<>();
        int nesteOppdrag = 0;
        Totaldata totaldata = opprettTotaldata();
        Periodedata periodedata = opprettPeriodedata();
        Grunnlagsdata grunnlagsdata = opprettGrunnlagsdata();
        boolean første = true;
        while (nesteOppdrag < oppdragsliste.size()) {
            Avstemmingsdata avstemmingsdata = lagAvstemmingsdataFelles(AksjonType.DATA);
            if (første) {
                avstemmingsdata.setTotal(totaldata);
                avstemmingsdata.setPeriode(periodedata);
                avstemmingsdata.setGrunnlag(grunnlagsdata);
            }
            nesteOppdrag = opprettDetaljer(avstemmingsdata, nesteOppdrag);
            liste.add(avstemmingsdata);
            første = false;
        }
        return liste;
    }

    Avstemmingsdata lagAvstemmingsdataFelles(AksjonType aksjonType) {
        Avstemmingsdata avstemmingsdata = objectFactory.createAvstemmingsdata();
        avstemmingsdata.setAksjon(tilAksjonsdata(aksjonType));
        return avstemmingsdata;
    }

    private String lagXmlMelding(Avstemmingsdata avstemmingsdata) {
        try {
            return JaxbHelper.marshalAndValidateJaxb(GrensesnittavstemmingSkjemaConstants.JAXB_CLASS, avstemmingsdata, GrensesnittavstemmingSkjemaConstants.XSD_LOCATION);
        } catch (JAXBException | SAXException e) {
            throw GrensesnittavstemmingFeil.FACTORY.xmlgenereringsfeil(e).toException();
        }
    }

    private Totaldata opprettTotaldata() {
        long totalBelop = 0L;
        for (Oppdrag110 oppdrag : oppdragsliste) {
            totalBelop += getBelop(oppdrag);
        }
        Totaldata totaldata = objectFactory.createTotaldata();
        totaldata.setTotalAntall(oppdragsliste.size());
        totaldata.setTotalBelop(BigDecimal.valueOf(totalBelop));
        totaldata.setFortegn(tilFortegn(totalBelop));
        return totaldata;
    }

    private Fortegn tilFortegn(long belop) {
        return belop >= 0 ? Fortegn.T : Fortegn.F;
    }

    private long getBelop(Oppdrag110 oppdrag) {
        long belop = 0L;
        for (Oppdragslinje150 oppdragslinje : oppdrag.getOppdragslinje150Liste()) {
            belop += oppdragslinje.getSats();
        }
        return belop;
    }

    private Periodedata opprettPeriodedata() {
        Periodedata periodedata = objectFactory.createPeriodedata();
        periodedata.setDatoAvstemtFom(tilPeriodeData(finnAvstemming115MedLavestNokkelAvstemmingsDato(oppdragsliste).getTidspnktMelding()));
        periodedata.setDatoAvstemtTom(tilPeriodeData(finnAvstemming115MedHøyestNokkelAvstemmingsDato(oppdragsliste).getTidspnktMelding()));
        return periodedata;
    }

    private Grunnlagsdata opprettGrunnlagsdata() {
        int godkjentAntall = 0;
        long godkjentBelop = 0L;
        int varselAntall = 0;
        long varselBelop = 0L;
        int avvistAntall = 0;
        long avvistBelop = 0L;
        int manglerAntall = 0;
        long manglerBelop = 0L;
        for (Oppdrag110 oppdrag : oppdragsliste) {
            long belop = getBelop(oppdrag);
            List<OppdragKvittering> kvitteringListe = getOppdragKvitteringer(oppdrag);
            String alvorlighetsgrad = !kvitteringListe.isEmpty() ? kvitteringListe.get(0).getAlvorlighetsgrad() : null;
            if (oppdrag.getOppdragskontroll().getVenterKvittering()) {
                manglerBelop += belop;
                manglerAntall++;
            } else if ("00".equals(alvorlighetsgrad)) {
                godkjentBelop += belop;
                godkjentAntall++;
            } else if ("04".equals(alvorlighetsgrad)) {
                varselBelop += belop;
                varselAntall++;
            } else {
                avvistBelop += belop;
                avvistAntall++;
            }
        }
        Grunnlagsdata grunnlagsdata = objectFactory.createGrunnlagsdata();

        grunnlagsdata.setGodkjentAntall(godkjentAntall);
        grunnlagsdata.setGodkjentBelop(BigDecimal.valueOf(godkjentBelop));
        grunnlagsdata.setGodkjentFortegn(tilFortegn(godkjentBelop));

        grunnlagsdata.setVarselAntall(varselAntall);
        grunnlagsdata.setVarselBelop(BigDecimal.valueOf(varselBelop));
        grunnlagsdata.setVarselFortegn(tilFortegn(varselBelop));

        grunnlagsdata.setAvvistAntall(avvistAntall);
        grunnlagsdata.setAvvistBelop(BigDecimal.valueOf(avvistBelop));
        grunnlagsdata.setAvvistFortegn(tilFortegn(avvistBelop));

        grunnlagsdata.setManglerAntall(manglerAntall);
        grunnlagsdata.setManglerBelop(BigDecimal.valueOf(manglerBelop));
        grunnlagsdata.setManglerFortegn(tilFortegn(manglerBelop));

        return grunnlagsdata;
    }

    private int opprettDetaljer(Avstemmingsdata avstemmingsdata, int nesteOppdrag) {
        int oppdragNr = nesteOppdrag;
        while (DETALJER_PR_MELDING > avstemmingsdata.getDetalj().size() && oppdragNr < oppdragsliste.size()) {
            Oppdrag110 oppdrag = oppdragsliste.get(oppdragNr);
            List<OppdragKvittering> kvitteringListe = getOppdragKvitteringer(oppdrag);
            String alvorlighetsgrad = !kvitteringListe.isEmpty() ? kvitteringListe.get(0).getAlvorlighetsgrad() : null;
            if (oppdrag.getOppdragskontroll().getVenterKvittering()) {
                opprettDetalj(avstemmingsdata, oppdrag, DetaljType.MANG, alvorlighetsgrad);
            } else if ("00".equals(alvorlighetsgrad)) { //$NON-NLS-1$
            } else if ("04".equals(alvorlighetsgrad)) {
                opprettDetalj(avstemmingsdata, oppdrag, DetaljType.VARS, alvorlighetsgrad);
            } else {
                opprettDetalj(avstemmingsdata, oppdrag, DetaljType.AVVI, alvorlighetsgrad);
            }
            oppdragNr++;
        }
        return oppdragNr;
    }

    private void opprettDetalj(Avstemmingsdata avstemmingsdata, Oppdrag110 oppdrag110, DetaljType detaljType, String alvorlighetsgrad) {
        List<OppdragKvittering> kvitteringListe = getOppdragKvitteringer(oppdrag110);
        String meldingKode = !kvitteringListe.isEmpty() ? kvitteringListe.get(0).getMeldingKode() : null;
        String beskrMelding = !kvitteringListe.isEmpty() ? kvitteringListe.get(0).getBeskrMelding() : null;
        Detaljdata detaljdata = objectFactory.createDetaljdata();
        detaljdata.setDetaljType(detaljType);
        detaljdata.setOffnr(oppdrag110.getOppdragGjelderId());
        detaljdata.setAvleverendeTransaksjonNokkel(String.valueOf(oppdrag110.getFagsystemId()));
        detaljdata.setMeldingKode(meldingKode);
        detaljdata.setAlvorlighetsgrad(alvorlighetsgrad);
        detaljdata.setTekstMelding(beskrMelding);
        detaljdata.setTidspunkt(tilSpesialkodetDatoOgKlokkeslett(oppdrag110.getAvstemming115().getTidspnktMelding()));
        avstemmingsdata.getDetalj().add(detaljdata);
    }

    private Aksjonsdata tilAksjonsdata(AksjonType aksjonType) {
        Aksjonsdata aksjonsdata = objectFactory.createAksjonsdata();
        aksjonsdata.setAksjonType(aksjonType);
        aksjonsdata.setKildeType(KildeType.AVLEV);
        aksjonsdata.setAvstemmingType(AvstemmingType.GRSN);
        aksjonsdata.setAvleverendeKomponentKode(ØkonomiKodeKomponent.VLFP.getKodeKomponent());
        aksjonsdata.setMottakendeKomponentKode(ØkonomiKodeKomponent.OS.getKodeKomponent());
        aksjonsdata.setUnderkomponentKode(fagområde);
        aksjonsdata.setNokkelFom(tilSpesialkodetDatoOgKlokkeslett(finnAvstemming115MedLavestNokkelAvstemmingsDato(oppdragsliste).getNokkelAvstemming()));
        Avstemming115 senestAvstemming115 = finnAvstemming115MedHøyestNokkelAvstemmingsDato(oppdragsliste);
        aksjonsdata.setNokkelTom(tilSpesialkodetDatoOgKlokkeslett(senestAvstemming115.getNokkelAvstemming()));
        aksjonsdata.setTidspunktAvstemmingTom(tilSpesialkodetDatoOgKlokkeslett(senestAvstemming115.getTidspnktMelding()));
        aksjonsdata.setAvleverendeAvstemmingId(avstemmingId);
        aksjonsdata.setBrukerId(BRUKER_ID_FOR_VEDTAKSLØSNINGEN);
        return aksjonsdata;
    }

    private Avstemming115 finnAvstemming115MedLavestNokkelAvstemmingsDato(List<Oppdrag110> oppdragsliste) {
        return oppdragsliste.stream()
            .map(Oppdrag110::getAvstemming115)
            .min(Comparator.comparing(Avstemming115::getNokkelAvstemming))
            .orElseThrow(() -> new IllegalStateException("Kan ikke finne NokkelFom for Avstemming."));
    }

    private Avstemming115 finnAvstemming115MedHøyestNokkelAvstemmingsDato(List<Oppdrag110> oppdragsliste) {
        return oppdragsliste.stream()
            .map(Oppdrag110::getAvstemming115)
            .max(Comparator.comparing(Avstemming115::getNokkelAvstemming))
            .orElseThrow(() -> new IllegalStateException("Kan ikke finne NokkelTom for Avstemming."));
    }

    private String tilPeriodeData(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        String pattern = "yyyyMMddHH";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dtf);
    }

    static String tilSpesialkodetDatoOgKlokkeslett(LocalDateTime dt) {
        if (dt == null) {
            return null;
        }
        String pattern = "yyyy-MM-dd-HH.mm.ss.SSS";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return dt.format(dtf);
    }

    private List<OppdragKvittering> getOppdragKvitteringer(Oppdrag110 oppdrag) {
        return oppdrag.getOppdragKvitteringListe();
    }

    private Avstemming115 getOppdragAvstemming115(Oppdrag110 oppdrag) {
        return oppdrag.getAvstemming115();
    }

    public String getAvstemmingId() {
        return avstemmingId;
    }

    private interface GrensesnittavstemmingFeil extends DeklarerteFeil {
        GrensesnittavstemmingFeil FACTORY = FeilFactory.create(GrensesnittavstemmingFeil.class);

        @TekniskFeil(feilkode = "FP-531167", feilmelding = "Kan ikke opprette avstemmingsmelding. Problemer ved generering av xml", logLevel = LogLevel.ERROR)
        Feil xmlgenereringsfeil(Exception cause);

    }
}
