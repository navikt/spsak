package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.domene.medlem.api.MedlemskapsperiodeKoder;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.Medlemsperiode;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.GrunnlagstypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.KildeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.KildedokumenttypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.LandkodeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.LovvalgMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.PeriodetypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.TrygdedekningMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class MedlTestSett {


    private static final Map<String, HentPeriodeListeResponse> RESPONSE_MAP = new HashMap<>();

    public static void nullstill() {
        RESPONSE_MAP.clear();
    }

    public static void dekningUavklart(PersonIdent personIdent, LocalDate fom, LocalDate tom, MedlemskapsperiodeKoder.Lovvalg lovvalg) {
        HentPeriodeListeResponse response = new HentPeriodeListeResponse();
        setOppData(response, fom, tom,"Opphor", lovvalg.name());
        RESPONSE_MAP.put(personIdent.getIdent(), response);
    }

    public static HentPeriodeListeResponse finnRespons(String fnr) {
        return RESPONSE_MAP.getOrDefault(fnr, new HentPeriodeListeResponse());
    }

    private static void setOppData(HentPeriodeListeResponse hentPeriodeListeResponse, LocalDate localFom, LocalDate localTom, String dekning, String lovvalg) {
        Medlemsperiode medlemsperiode = new Medlemsperiode();
        try {
            XMLGregorianCalendar fom = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(localFom);
            XMLGregorianCalendar tom = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(localTom);
            medlemsperiode.withFraOgMed(fom);
            medlemsperiode.withTilOgMed(tom);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        TrygdedekningMedTerm trygdedekning = new TrygdedekningMedTerm();
        trygdedekning.setValue(dekning);
        PeriodetypeMedTerm periodetypeMedTerm = new PeriodetypeMedTerm();
        periodetypeMedTerm.setValue("PMMEDSKP");
        LandkodeMedTerm landkodeMedTerm = new LandkodeMedTerm();
        landkodeMedTerm.setValue("NOR");
        LovvalgMedTerm lovvalgMedTerm = new LovvalgMedTerm();
        lovvalgMedTerm.setValue(lovvalg);
        KildeMedTerm kildeMedTerm = new KildeMedTerm();
        KildedokumenttypeMedTerm kildedokumenttypeMedTerm = new KildedokumenttypeMedTerm();
        GrunnlagstypeMedTerm grunnlagstypeMedTerm = new GrunnlagstypeMedTerm();
        kildeMedTerm.setValue("LAANEKASSEN");
        kildedokumenttypeMedTerm.setValue("Dokument");
        grunnlagstypeMedTerm.setValue("MEDFT");

        medlemsperiode.setId(123L);
        medlemsperiode.setTrygdedekning(trygdedekning);
        medlemsperiode.setType(periodetypeMedTerm);
        medlemsperiode.setLand(landkodeMedTerm);
        medlemsperiode.setLovvalg(lovvalgMedTerm);
        medlemsperiode.setKilde(kildeMedTerm);
        medlemsperiode.setKildedokumenttype(kildedokumenttypeMedTerm);
        medlemsperiode.setGrunnlagstype(grunnlagstypeMedTerm);

        hentPeriodeListeResponse.getPeriodeListe().add((medlemsperiode));
    }

}
