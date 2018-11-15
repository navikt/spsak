package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import static no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagTekstBuilderFormater.formatString;
import static no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeTjeneste.finnSkjermlenkeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.feil.Feil;

public class HistorikkInnslagTekstBuilder {

    private boolean begrunnelseEndret = false;
    private boolean gjeldendeFraSatt = false;

    private HistorikkinnslagDel.Builder historikkinnslagDelBuilder = HistorikkinnslagDel.builder();
    private List<HistorikkinnslagDel> historikkinnslagDeler = new ArrayList<>();
    private int antallEndredeFelter = 0;
    private int antallAksjonspunkter = 0;
    private int antallOpplysninger = 0;

    public HistorikkInnslagTekstBuilder() {
    }

    public List<HistorikkinnslagDel> getHistorikkinnslagDeler() {
        return historikkinnslagDeler;
    }

    public HistorikkInnslagTekstBuilder medHendelse(HistorikkinnslagType historikkInnslagsType) {
        return medHendelse(historikkInnslagsType, null);
    }

    public HistorikkInnslagTekstBuilder medHendelse(HistorikkinnslagType historikkinnslagType, Object verdi) {
        if (!HistorikkinnslagType.FAKTA_ENDRET.equals(historikkinnslagType)
            && !HistorikkinnslagType.OVERSTYRT.equals(historikkinnslagType)
            && !HistorikkinnslagType.OPPTJENING.equals(historikkinnslagType)) { // PKMANTIS-753 FPFEIL-805
            String verdiStr = formatString(verdi);
            HistorikkinnslagFelt.builder()
                .medFeltType(HistorikkinnslagFeltType.HENDELSE)
                .medNavn(historikkinnslagType)
                .medTilVerdi(verdiStr)
                .build(historikkinnslagDelBuilder);
        }
        return this;
    }

    public HistorikkInnslagTekstBuilder medSkjermlenke(AksjonspunktDefinisjon aksjonspunktDefinisjon, Behandling behandling) {
        return medSkjermlenke(finnSkjermlenkeType(aksjonspunktDefinisjon, behandling));
    }

    public HistorikkInnslagTekstBuilder medSkjermlenke(SkjermlenkeType skjermlenkeType) {
        if (SkjermlenkeType.UDEFINERT.equals(skjermlenkeType)) {
            return this;
        }
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.SKJERMLENKE)
            .medTilVerdi(skjermlenkeType)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medNavnOgGjeldendeFra(HistorikkEndretFeltType endretFelt, String navnVerdi, LocalDate gjeldendeFraDato) {
        if (gjeldendeFraDato != null) {
            gjeldendeFraSatt = true;
        }
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.GJELDENDE_FRA)
            .medNavn(endretFelt)
            .medNavnVerdi(navnVerdi)
            .medTilVerdi(formatString(gjeldendeFraDato))
            .build(historikkinnslagDelBuilder);
        return this;
    }


    public HistorikkInnslagTekstBuilder medGjeldendeFra(LocalDate localDate) {
        if (localDate != null) {
            gjeldendeFraSatt = true;
        }
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.GJELDENDE_FRA)
            .medTilVerdi(formatString(localDate))
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medAvklartSøknadperiode(HistorikkAvklartSoeknadsperiodeType endretFeltType, String verdi) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.AVKLART_SOEKNADSPERIODE)
            .medNavn(endretFeltType)
            .medTilVerdi(verdi)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medNavnVerdiOgAvklartSøknadperiode(HistorikkAvklartSoeknadsperiodeType endretFeltType, String navnVerdi, String verdi) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.AVKLART_SOEKNADSPERIODE)
            .medNavn(endretFeltType)
            .medNavnVerdi(navnVerdi)
            .medTilVerdi(verdi)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public <K extends Kodeliste> HistorikkInnslagTekstBuilder medÅrsak(K årsak) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.AARSAK)
            .medTilVerdi(årsak)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medTema(HistorikkEndretFeltType endretFeltType, String verdi) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.ANGÅR_TEMA)
            .medNavn(endretFeltType)
            .medNavnVerdi(verdi)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medResultat(Kodeliste resultat) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.RESULTAT)
            .medTilVerdi(resultat)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(LocalDateInterval begrunnelse) {
        return medBegrunnelse(formatString(begrunnelse), true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(LocalDate begrunnelse) {
        return medBegrunnelse(formatString(begrunnelse), true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(Kodeliste begrunnelse) {
        return medBegrunnelse(begrunnelse, true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(String begrunnelse) {
        String begrunnelseStr = formatString(begrunnelse);
        return medBegrunnelse(begrunnelseStr, true);
    }

    public HistorikkInnslagTekstBuilder medBegrunnelse(String begrunnelse, boolean erBegrunnelseEndret) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.BEGRUNNELSE)
            .medTilVerdi(begrunnelse)
            .build(historikkinnslagDelBuilder);
        this.begrunnelseEndret = erBegrunnelseEndret;
        return this;
    }

    public <K extends Kodeliste> HistorikkInnslagTekstBuilder medBegrunnelse(K begrunnelse, boolean erBegrunnelseEndret) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.BEGRUNNELSE)
            .medTilVerdi(begrunnelse)
            .build(historikkinnslagDelBuilder);
        this.begrunnelseEndret = erBegrunnelseEndret;
        return this;
    }

    public <T> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, String navnVerdi, T fraVerdi, T tilVerdi) {
        String fraVerdiStr = formatString(fraVerdi);
        String tilVerdiStr = formatString(tilVerdi);

        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
            .medNavn(historikkEndretFeltType)
            .medNavnVerdi(navnVerdi)
            .medFraVerdi(fraVerdiStr)
            .medTilVerdi(tilVerdiStr)
            .medSekvensNr(getNesteEndredeFeltSekvensNr())
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public <K extends Kodeliste> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, K fraVerdi, K tilVerdi) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
            .medNavn(historikkEndretFeltType)
            .medFraVerdi(fraVerdi)
            .medTilVerdi(tilVerdi)
            .medSekvensNr(getNesteEndredeFeltSekvensNr())
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public <K extends Kodeliste> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, String navnVerdi, K fraVerdi, K tilVerdi) {
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
            .medNavn(historikkEndretFeltType)
            .medNavnVerdi(navnVerdi)
            .medFraVerdi(fraVerdi)
            .medTilVerdi(tilVerdi)
            .medSekvensNr(getNesteEndredeFeltSekvensNr())
            .build(historikkinnslagDelBuilder);
        return this;
    }

    public <T> HistorikkInnslagTekstBuilder medEndretFelt(HistorikkEndretFeltType historikkEndretFeltType, T fraVerdi, T tilVerdi) {
        if (fraVerdi instanceof Kodeliste || tilVerdi instanceof Kodeliste) {
            Kodeliste fraVerdiKl = (Kodeliste) fraVerdi;
            Kodeliste tilVerdiKl = (Kodeliste) tilVerdi;
            return medEndretFelt(historikkEndretFeltType, fraVerdiKl, tilVerdiKl);
        }
        String fraVerdiStr = formatString(fraVerdi);
        String tilVerdiStr = formatString(tilVerdi);

        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.ENDRET_FELT)
            .medNavn(historikkEndretFeltType)
            .medFraVerdi(fraVerdiStr)
            .medTilVerdi(tilVerdiStr)
            .medSekvensNr(getNesteEndredeFeltSekvensNr())
            .build(historikkinnslagDelBuilder);
        return this;
    }

    private int getNesteEndredeFeltSekvensNr() {
        int neste = antallEndredeFelter;
        antallEndredeFelter++;
        return neste;
    }

    public <T> HistorikkInnslagTekstBuilder medOpplysning(HistorikkOpplysningType opplysningType, T verdi) {
        String tilVerdi = formatString(verdi);
        int sekvensNr = hentNesteOpplysningSekvensNr();
        HistorikkinnslagFelt.builder()
            .medFeltType(HistorikkinnslagFeltType.OPPLYSNINGER)
            .medNavn(opplysningType)
            .medTilVerdi(tilVerdi)
            .medSekvensNr(sekvensNr)
            .build(historikkinnslagDelBuilder);
        return this;
    }

    private int hentNesteOpplysningSekvensNr() {
        int sekvensNr = antallOpplysninger;
        antallOpplysninger++;
        return sekvensNr;
    }

    public HistorikkInnslagTekstBuilder medTotrinnsvurdering(Map<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> vurdering,
                                                             List<HistorikkinnslagTotrinnsvurdering> vurderingUtenVilkar) {
        boolean første = true;
        for (HistorikkinnslagTotrinnsvurdering totrinnsVurdering : vurderingUtenVilkar) {
            if (første) {
                første = false;
            } else {
                ferdigstillHistorikkinnslagDel();
            }
            leggTilTotrinnsvurdering(totrinnsVurdering);
        }

        List<Map.Entry<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>>> sortedList = vurdering.entrySet().stream()
            .sorted(getHistorikkDelComparator()).collect(Collectors.toList());

        for (Map.Entry<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>> lenkeVurdering : sortedList) {
            if (første) {
                første = false;
            } else {
                ferdigstillHistorikkinnslagDel();
            }
            SkjermlenkeType skjermlenkeType = lenkeVurdering.getKey();
            List<HistorikkinnslagTotrinnsvurdering> totrinnsVurderinger = lenkeVurdering.getValue();
            totrinnsVurderinger.sort(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getAksjonspunktSistEndret));
            medSkjermlenke(skjermlenkeType);
            totrinnsVurderinger.forEach(this::leggTilTotrinnsvurdering);
        }
        return this;
    }

    private Comparator<Map.Entry<SkjermlenkeType, List<HistorikkinnslagTotrinnsvurdering>>> getHistorikkDelComparator() {
        return (o1, o2) -> {
            List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger1 = o1.getValue();
            List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger2 = o2.getValue();
            totrinnsvurderinger1.sort(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getAksjonspunktSistEndret));
            totrinnsvurderinger2.sort(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getAksjonspunktSistEndret));
            LocalDateTime date1 = totrinnsvurderinger1.get(0).getAksjonspunktSistEndret();
            LocalDateTime date2 = totrinnsvurderinger2.get(0).getAksjonspunktSistEndret();
            if (date1 == null || date2 == null) {
                return -1;
            }
            return date1.isAfter(date2) ? 1 : -1;
        };
    }


    private HistorikkInnslagTekstBuilder leggTilTotrinnsvurdering(HistorikkinnslagTotrinnsvurdering totrinnsvurdering) {
        int sekvensNr = getNesteAksjonspunktSekvensNr();
        leggTilFelt(HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE, totrinnsvurdering.getBegrunnelse(), sekvensNr);
        leggTilFelt(HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT, totrinnsvurdering.erGodkjent(), sekvensNr);
        leggTilFelt(HistorikkinnslagFeltType.AKSJONSPUNKT_KODE, totrinnsvurdering.getAksjonspunktDefinisjon().getKode(), sekvensNr);
        return this;
    }

    private <T> void leggTilFelt(HistorikkinnslagFeltType feltType, T verdi, int sekvensNr) {
        HistorikkinnslagFelt.builder()
            .medFeltType(feltType)
            .medTilVerdi(verdi != null ? verdi.toString() : null)
            .medSekvensNr(sekvensNr)
            .build(historikkinnslagDelBuilder);
    }

    private int getNesteAksjonspunktSekvensNr() {
        int sekvensNr = antallAksjonspunkter;
        antallAksjonspunkter++;
        return sekvensNr;
    }

    public int antallEndredeFelter() {
        return antallEndredeFelter;
    }

    /**
     * Returnerer om begrunnelse er endret.
     */
    public boolean getErBegrunnelseEndret() {
        return begrunnelseEndret;
    }

    /**
     * Returnerer om gjeldendeFra er satt.
     */
    public boolean getErGjeldendeFraSatt() {
        return gjeldendeFraSatt;
    }

    public HistorikkInnslagTekstBuilder ferdigstillHistorikkinnslagDel() {
        if (!historikkinnslagDelBuilder.harFelt()) {
            return this;
        }
        historikkinnslagDeler.add(historikkinnslagDelBuilder.build());
        historikkinnslagDelBuilder = HistorikkinnslagDel.builder();
        antallEndredeFelter = 0;
        antallAksjonspunkter = 0;
        antallOpplysninger = 0;
        begrunnelseEndret = false;
        return this;
    }

    public List<HistorikkinnslagDel> build(Historikkinnslag historikkinnslag) {
        ferdigstillHistorikkinnslagDel();
        verify(historikkinnslag.getType());
        historikkinnslag.setHistorikkinnslagDeler(historikkinnslagDeler);
        return historikkinnslagDeler;
    }

    /**
     * Sjekker at alle påkrevde felter for gitt historikkinnslagstype er angitt
     *
     * @param historikkinnslagType
     */
    private void verify(HistorikkinnslagType historikkinnslagType) {
        List<Feil> verificationResults = new ArrayList<>();
        historikkinnslagDeler.forEach(del -> {
            Optional<Feil> exception = verify(historikkinnslagType, del);
            exception.ifPresent(verificationResults::add);
        });
        // kast feil dersom alle deler feiler valideringen
        if (verificationResults.size() == historikkinnslagDeler.size()) {
            throw verificationResults.get(0).toException();
        }
    }

    private Optional<Feil> verify(HistorikkinnslagType historikkinnslagType, HistorikkinnslagDel historikkinnslagDel) {
        String type = historikkinnslagType.getMal();

        if (HistorikkinnslagType.MAL_TYPE_1.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE);
        }
        if (HistorikkinnslagType.MAL_TYPE_2.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE, HistorikkinnslagFeltType.SKJERMLENKE);
        }
        if (HistorikkinnslagType.MAL_TYPE_3.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE, HistorikkinnslagFeltType.AKSJONSPUNKT_KODE);
        }
        if (HistorikkinnslagType.MAL_TYPE_4.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE);
        }
        if (HistorikkinnslagType.MAL_TYPE_5.equals(type) || HistorikkinnslagType.MAL_TYPE_7.equals(type) || HistorikkinnslagType.MAL_TYPE_8.equals(type)
            || HistorikkinnslagType.MAL_TYPE_10.equals(type)) {
            return checkAtLeastOnePresent(type, historikkinnslagDel, HistorikkinnslagFeltType.SKJERMLENKE,
                HistorikkinnslagFeltType.HENDELSE,
                HistorikkinnslagFeltType.ENDRET_FELT,
                HistorikkinnslagFeltType.BEGRUNNELSE);
        }
        if (HistorikkinnslagType.MAL_TYPE_6.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.OPPLYSNINGER);
        }
        if (HistorikkinnslagType.MAL_TYPE_9.equals(type)) {
            return checkFieldsPresent(type, historikkinnslagDel, HistorikkinnslagFeltType.HENDELSE, HistorikkinnslagFeltType.ENDRET_FELT);
        }
        throw HistorikkInnsalgFeil.FACTORY.ukjentHistorikkinnslagType(type).toException();
    }

    private Optional<Feil> checkFieldsPresent(String type, HistorikkinnslagDel del, HistorikkinnslagFeltType... fields) {
        List<HistorikkinnslagFeltType> fieldList = Arrays.asList(fields);
        Set<HistorikkinnslagFeltType> harFelt = findFields(del, fieldList).collect(Collectors.toCollection(LinkedHashSet::new));

        // harFelt skal inneholde alle de samme feltene som fieldList
        if (harFelt.size() == fields.length) {
            return Optional.empty();
        } else {
            List<String> feltKoder = fieldList.stream().map(HistorikkinnslagFeltType::getKode).collect(Collectors.toList());
            return Optional.of(HistorikkInnsalgFeil.FACTORY.manglerFeltForHistorikkInnslag(type, feltKoder));
        }
    }

    private Optional<Feil> checkAtLeastOnePresent(String type, HistorikkinnslagDel del, HistorikkinnslagFeltType... fields) {
        List<HistorikkinnslagFeltType> fieldList = Arrays.asList(fields);
        Optional<HistorikkinnslagFeltType> opt = findFields(del, fieldList).findAny();

        if (opt.isPresent()) {
            return Optional.empty();
        } else {
            List<String> feltKoder = fieldList.stream().map(HistorikkinnslagFeltType::getKode).collect(Collectors.toList());
            return Optional.of(HistorikkInnsalgFeil.FACTORY.manglerMinstEtFeltForHistorikkinnslag(type, feltKoder));
        }
    }

    private Stream<HistorikkinnslagFeltType> findFields(HistorikkinnslagDel del, List<HistorikkinnslagFeltType> fieldList) {
        return del.getHistorikkinnslagFelt().stream().map(HistorikkinnslagFelt::getFeltType).filter(fieldList::contains);
    }

    /*
     * https://confluence.adeo.no/display/MODNAV/OMR-13+SF4+Sakshistorikk+-+UX+og+grafisk+design
     *
     * Fem design patterns:
     *
     * +----------------------------+
     * | Type 1 |
     * | BEH_VENT |
     * | BEH_GJEN |
     * | BEH_STARTET |
     * | VEDLEGG_MOTTATT |
     * | BREV_SENT |
     * | REGISTRER_PAPIRSØK |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>
     * <OPTIONAL begrunnelsestekst>
     *
     *
     * +----------------------------+
     * | Type 2 |
     * | FORSLAG_VEDTAK |
     * | VEDTAK_FATTET |
     * | OVERSTYRT (hvis beslutter) |
     * | UENDRET UTFALL |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>: <resultat>
     * <skjermlinke>
     * <OPTIONAL totrinnskontroll>
     *
     *
     * +----------------------------+
     * | Type 3 |
     * | SAK_RETUR |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>
     * <totrinnsvurdering> med <skjermlinke> til vilkåret og liste med <aksjonspunkter>
     *
     *
     * +----------------------------+
     * | Type 4 |
     * | AVBRUTT_BEH |
     * | OVERSTYRT (hvis saksbeh.) |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <hendelse>
     * <årsak>
     * <begrunnelsestekst>
     *
     *
     * +----------------------------+
     * | Type 5 |
     * | FAKTA_ENDRET |
     * +----------------------------+
     * <tidspunkt> // <rolle> <id>
     * <skjermlinke>
     * <feltnavn> er endret <fra-verdi> til <til-verdi>
     * <radiogruppe> er satt til <verdi>
     * <begrunnelsestekst>
     *
     */

}
