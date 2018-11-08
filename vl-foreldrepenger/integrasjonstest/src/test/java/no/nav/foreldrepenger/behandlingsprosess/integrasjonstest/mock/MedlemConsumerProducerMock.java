package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock;

import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.KVINNE_MEDL_ENDELIG_PERIODE_FNR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.KVINNE_MEDL_UAVKL_PERIODE_FNR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo.KVINNE_MEDL_USA_PERIODE_FNR;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MedlTestSett;
import no.nav.tjeneste.virksomhet.medlemskap.v2.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.medlemskap.v2.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.Medlemsperiode;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.GrunnlagstypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.KildeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.KildedokumenttypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.LandkodeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.LovvalgMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.PeriodetypeMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.informasjon.kodeverk.TrygdedekningMedTerm;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeListeResponse;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeRequest;
import no.nav.tjeneste.virksomhet.medlemskap.v2.meldinger.HentPeriodeResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.medl.MedlemConsumer;
import no.nav.vedtak.felles.integrasjon.medl.MedlemConsumerProducer;

@Dependent
@Alternative
@Priority(1)
public class MedlemConsumerProducerMock extends MedlemConsumerProducer {
    private static List<String> LISTE_DEKNING_B = Arrays.asList(KVINNE_MEDL_ENDELIG_PERIODE_FNR,
        KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_PERIODE_FNR,
        KVINNE_MEDL_EØSBORGER_BOSATT_NOR_FNR);
    private static List<String> LISTE_DEKNING_A = Arrays.asList(KVINNE_MEDL_USA_PERIODE_FNR);
    private static List<String> LISTE_DEKNING_UAVK = Arrays.asList(KVINNE_MEDL_UAVKL_PERIODE_FNR);

    private RegisterKontekst registerKontekst;

    @Inject
    public MedlemConsumerProducerMock(RegisterKontekst registerKontekst) {
        this.registerKontekst = registerKontekst;
    }

    @Override
    public MedlemConsumer medlemConsumer() {
        class MedlemConsumerMock implements MedlemConsumer {

            @Override
            public HentPeriodeResponse hentPeriode(HentPeriodeRequest hentPeriodeRequest) throws Sikkerhetsbegrensning {
                return new HentPeriodeResponse();
            }

            @Override
            public HentPeriodeListeResponse hentPeriodeListe(HentPeriodeListeRequest hentPeriodeListeRequest)
                    throws PersonIkkeFunnet, Sikkerhetsbegrensning {

                String fnr = hentPeriodeListeRequest.getIdent().getValue();
                HentPeriodeListeResponse hentPeriodeListeResponse = new HentPeriodeListeResponse();
                if (registerKontekst.erInitalisert()) {
                    return MedlTestSett.finnRespons(fnr);
                }


                if (LISTE_DEKNING_B.contains(fnr)) {
//                    setOppData(hentPeriodeListeResponse, null, "ENDL");
                } else if (LISTE_DEKNING_A.contains(fnr)) {
                    //setOppData(hentPeriodeListeResponse, "Unntatt", "ENDL");
                } else if (LISTE_DEKNING_UAVK.contains(fnr)) {
                    setOppData(hentPeriodeListeResponse, "Opphor", "UAVK");
                }

                return hentPeriodeListeResponse;
            }
        }
        return new MedlemConsumerMock();
    }

    private void setOppData(HentPeriodeListeResponse hentPeriodeListeResponse, String dekning, String lovvalg) {
        Medlemsperiode medlemsperiode = new Medlemsperiode();
        try {
            XMLGregorianCalendar fom = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(LocalDate.now().minusYears(1));
            XMLGregorianCalendar tom = DateUtil.convertToXMLGregorianCalendarRemoveTimezone(LocalDate.now().plusYears(1));
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
