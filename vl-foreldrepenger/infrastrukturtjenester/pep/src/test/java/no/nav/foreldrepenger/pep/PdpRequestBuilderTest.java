package no.nav.foreldrepenger.pep;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.pip.PipBehandlingsData;
import no.nav.foreldrepenger.behandlingslager.pip.PipRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journalposttyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumer;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;

public class PdpRequestBuilderTest {

    private static final String DUMMY_ID_TOKEN = "dummyheader.dymmypayload.dummysignaturee";

    private static final Long FAGSAK_ID = 10001L;
    private static final Long FAGSAK_ID_2 = 10002L;
    private static final Long BEHANDLING_ID = 333L;
    private static final JournalpostId JOURNALPOST_ID = new JournalpostId("444");
    private static final JournalpostId JOURNALPOST_ID_UGYLDIG = new JournalpostId("555");
    private static final String SAKSNUMMER = "7777";

    private static final AktørId AKTØR_0 = new AktørId("1");
    private static final AktørId AKTØR_1 = new AktørId("11");
    private static final AktørId AKTØR_2 = new AktørId("22");

    private static final String PERSON_0 = "00000000000";
    private static final String PERSON_1 = "11111111111";
    private static final String PERSON_2 = "22222222222";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private PipRepository pipRepository = Mockito.mock(PipRepository.class);
    private AktørConsumerMedCache aktørConsumer = Mockito.mock(AktørConsumerMedCache.class);
    private JournalConsumer journalConsumer = Mockito.mock(JournalConsumer.class);

    private PdpRequestBuilderImpl requestBuilder = new PdpRequestBuilderImpl(pipRepository, aktørConsumer, journalConsumer);

    @Test
    public void skal_hente_saksstatus_og_behandlingsstatus_når_behandlingId_er_input() throws Exception {
        AbacAttributtSamling attributter = byggAbacAttributtSamling().leggTil(AbacDataAttributter.opprett()
            .leggTilBehandlingsId(BEHANDLING_ID));

        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.hentAktørIdKnyttetTilFagsaker(Collections.singleton(FAGSAK_ID))).thenReturn(Collections.singleton(AKTØR_1));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));
        String behandligStatus = BehandlingStatus.OPPRETTET.getKode();
        String ansvarligSaksbehandler = "Z123456";
        String fagsakStatus = FagsakStatus.UNDER_BEHANDLING.getKode();
        when(pipRepository.hentDataForBehandling(BEHANDLING_ID)).thenReturn(Optional.of(new PipBehandlingsData(behandligStatus, ansvarligSaksbehandler, BigDecimal.valueOf(FAGSAK_ID), fagsakStatus)));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getFnr()).containsOnly(PERSON_1);
        assertThat(request.getAnsvarligSaksbehandler().get()).isEqualTo(ansvarligSaksbehandler);
        assertThat(request.getXacmlBehandlingStatus().get()).isEqualTo(AbacBehandlingStatus.OPPRETTET.getEksternKode());
        assertThat(request.getXacmlSakstatus().get()).isEqualTo(AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
    }

    @Test
    public void skal_hente_fnr_gitt_journalpost_id_som_input() throws HentKjerneJournalpostListeSikkerhetsbegrensning, HentKjerneJournalpostListeUgyldigInput {
        AbacAttributtSamling attributter = byggAbacAttributtSamling().leggTil(AbacDataAttributter.opprett()
            .leggTilJournalPostId(JOURNALPOST_ID.getVerdi(), false));
        final HentKjerneJournalpostListeResponse mockJournalResponse = initJournalMockResponse(false);

        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.fagsakIdForSaksnummer(Collections.singleton(SAKSNUMMER))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.hentAktørIdKnyttetTilFagsaker(Collections.singleton(FAGSAK_ID))).thenReturn(Collections.singleton(AKTØR_1));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));
        when(journalConsumer.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(mockJournalResponse); // NOSONAR

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getFnr()).containsOnly(PERSON_1);
    }

    @Test(expected = ManglerTilgangException.class )
    public void skal_feile_uten_saksnummer() throws HentKjerneJournalpostListeSikkerhetsbegrensning, HentKjerneJournalpostListeUgyldigInput {
        AbacAttributtSamling attributter = byggAbacAttributtSamling().leggTil(AbacDataAttributter.opprett()
            .leggTilJournalPostId(JOURNALPOST_ID.getVerdi(), true));
        final HentKjerneJournalpostListeResponse mockJournalResponse = initJournalMockResponse(false);

        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID))).thenReturn(Collections.emptySet());
        when(journalConsumer.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(mockJournalResponse); // NOSONAR

        requestBuilder.lagPdpRequest(attributter);
    }

    @Test
    public void skal_feile_hvis_journalpost_utgaatt() throws HentKjerneJournalpostListeSikkerhetsbegrensning, HentKjerneJournalpostListeUgyldigInput {
        AbacAttributtSamling attributter = byggAbacAttributtSamling().leggTil(AbacDataAttributter.opprett().leggTilSaksnummer(SAKSNUMMER)
            .leggTilJournalPostId(JOURNALPOST_ID.getVerdi(), true));
        final HentKjerneJournalpostListeResponse mockJournalResponse = initJournalMockResponse(true);

        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID))).thenReturn(Collections.emptySet());
        when(pipRepository.fagsakIdForSaksnummer(Collections.singleton(SAKSNUMMER))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.hentAktørIdKnyttetTilFagsaker(Collections.singleton(FAGSAK_ID))).thenReturn(Collections.singleton(AKTØR_1));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));
        when(journalConsumer.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(mockJournalResponse); // NOSONAR

        expectedException.expect(ManglerTilgangException.class);
        expectedException.expectMessage("Ugyldig input. journalpostId er merket Utgår: 444");

        requestBuilder.lagPdpRequest(attributter);
    }

    @Test
    public void skal_feile_hvis_journalpost_ikke_finnes_for_sak() throws HentKjerneJournalpostListeSikkerhetsbegrensning, HentKjerneJournalpostListeUgyldigInput {
        AbacAttributtSamling attributter = byggAbacAttributtSamling().leggTil(AbacDataAttributter.opprett().leggTilSaksnummer(SAKSNUMMER)
            .leggTilJournalPostId(JOURNALPOST_ID_UGYLDIG.getVerdi(), true));
        final HentKjerneJournalpostListeResponse mockJournalResponse = initJournalMockResponse(false);

        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID_UGYLDIG))).thenReturn(Collections.emptySet());
        when(pipRepository.fagsakIdForSaksnummer(Collections.singleton(SAKSNUMMER))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.hentAktørIdKnyttetTilFagsaker(Collections.singleton(FAGSAK_ID))).thenReturn(Collections.singleton(AKTØR_1));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));
        when(journalConsumer.hentKjerneJournalpostListe(any(HentKjerneJournalpostListeRequest.class))).thenReturn(mockJournalResponse); // NOSONAR

        expectedException.expect(ManglerTilgangException.class);
        expectedException.expectMessage("Ugyldig input. Sendte inn følgende journalpostId-er, minst en av de finnes ikke i systemet: [JournalpostId<555>]");

        requestBuilder.lagPdpRequest(attributter);
    }


    @Test
    public void skal_hente_fnr_fra_alle_tilknyttede_saker_når_det_kommer_inn_søk_etter_saker_for_fnr() {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilFnrForSøkeEtterSaker(PERSON_0));

        when(aktørConsumer.hentAktørIdForPersonIdentSet(any())).thenReturn(Collections.singleton(AKTØR_0.getId()));

        Set<Long> fagsakIder = new HashSet<>();
        fagsakIder.add(FAGSAK_ID);
        fagsakIder.add(FAGSAK_ID_2);
        when(pipRepository.fagsakIderForSøker(Collections.singleton(AKTØR_0))).thenReturn(fagsakIder);
        Set<AktørId> aktører = new HashSet<>();
        aktører.add(AKTØR_0);
        aktører.add(AKTØR_1);
        aktører.add(AKTØR_2);
        when(pipRepository.hentAktørIdKnyttetTilFagsaker(fagsakIder)).thenReturn(aktører);

        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_0.getId())).thenReturn(Optional.of(PERSON_0));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_2.getId())).thenReturn(Optional.of(PERSON_2));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getFnr()).containsOnly(PERSON_0, PERSON_1, PERSON_2);
    }

    @Test
    public void skal_bare_sende_fnr_vider_til_pdp() {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilFødselsnummer(PERSON_0));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getFnr()).containsOnly(PERSON_0);
    }

    @Test
    public void skal_ta_inn_aksjonspunkt_id_og_sende_videre_aksjonspunkt_typer() throws Exception {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTilAksjonspunktKode("0000")
            .leggTilAksjonspunktKode("0001")
        );

        Set<String> koder = new HashSet<>();
        koder.add("0000");
        koder.add("0001");
        Set<String> svar = new HashSet<>();
        svar.add("Overstyring");
        svar.add("Manuell");
        Mockito.when(pipRepository.hentAksjonspunktTypeForAksjonspunktKoder(koder)).thenReturn(svar);

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getXacmlAksjonspunktType()).containsOnly("Overstyring", "Manuell");
    }

    @Test
    public void skal_slå_opp_og_sende_videre_fnr_når_aktør_id_er_input() throws Exception {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilAktørId(AKTØR_1.getId()));

        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getFnr()).containsOnly(PERSON_1);
    }

    @Test
    public void skal_ta_inn_dokumentDataID_slå_opp_behandling_id_og_sende_videre_samme_som_for_behandling() throws Exception {
        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilDokumentDataId(123L));

        when(pipRepository.behandlingsIdForDokumentDataId(Collections.singleton(123L))).thenReturn(Collections.singleton(BEHANDLING_ID));
        when(pipRepository.fagsakIdForJournalpostId(Collections.singleton(JOURNALPOST_ID))).thenReturn(Collections.singleton(FAGSAK_ID));
        when(pipRepository.hentAktørIdKnyttetTilFagsaker(Collections.singleton(FAGSAK_ID))).thenReturn(Collections.singleton(AKTØR_1));
        when(aktørConsumer.hentPersonIdentForAktørId(AKTØR_1.getId())).thenReturn(Optional.of(PERSON_1));

        String behandligStatus = BehandlingStatus.OPPRETTET.getKode();
        String ansvarligSaksbehandler = "Z123456";
        String fagsakStatus = FagsakStatus.UNDER_BEHANDLING.getKode();
        when(pipRepository.hentDataForBehandling(BEHANDLING_ID)).thenReturn(Optional.of(new PipBehandlingsData(behandligStatus, ansvarligSaksbehandler, BigDecimal.valueOf(FAGSAK_ID), fagsakStatus)));

        PdpRequest request = requestBuilder.lagPdpRequest(attributter);
        assertThat(request.getFnr()).containsOnly(PERSON_1);
        assertThat(request.getAnsvarligSaksbehandler().get()).isEqualTo(ansvarligSaksbehandler);
        assertThat(request.getXacmlBehandlingStatus().get()).isEqualTo(AbacBehandlingStatus.OPPRETTET.getEksternKode());
        assertThat(request.getXacmlSakstatus().get()).isEqualTo(AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
    }

    @Test
    public void skal_ikke_godta_at_det_sendes_inn_fagsak_id_og_behandling_id_som_ikke_stemmer_overens() throws Exception {

        expectedException.expect(ManglerTilgangException.class);
        expectedException.expectMessage("Ugyldig input. Ikke samsvar mellom behandlingId 1234 og fagsakId [123]");

        AbacAttributtSamling attributter = byggAbacAttributtSamling();
        attributter.leggTil(AbacDataAttributter.opprett().leggTilFagsakId(123L));
        attributter.leggTil(AbacDataAttributter.opprett().leggTilBehandlingsId(1234L));

        when(pipRepository.hentDataForBehandling(1234L)).thenReturn(Optional.of(new PipBehandlingsData(BehandlingStatus.OPPRETTET.getKode(), "Z1234", BigDecimal.valueOf(666), FagsakStatus.OPPRETTET.getKode())));

        requestBuilder.lagPdpRequest(attributter);

    }

    private AbacAttributtSamling byggAbacAttributtSamling() {
        AbacAttributtSamling attributtSamling = AbacAttributtSamling.medJwtToken(DUMMY_ID_TOKEN);
        attributtSamling.setActionType(BeskyttetRessursActionAttributt.READ);
        attributtSamling.setResource(BeskyttetRessursResourceAttributt.FAGSAK);
        return attributtSamling;
    }

    private HentKjerneJournalpostListeResponse initJournalMockResponse(boolean utgått) {
        HentKjerneJournalpostListeResponse response = new HentKjerneJournalpostListeResponse();
        Journalpost dummy = new Journalpost();
        dummy.setJournalpostId(JOURNALPOST_ID.getVerdi());
        dummy.setJournaltilstand(utgått ? Journaltilstand.UTGAAR : Journaltilstand.ENDELIG);
        Journalposttyper retning = new Journalposttyper();
        retning.setValue("I");
        dummy.setJournalposttype(retning);
        response.getJournalpostListe().add(dummy);
        return response;
    }


}
