package no.nav.foreldrepenger.behandlingskontroll.observer;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon.Type.INNGANG;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon.Type.UTGANG;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegOvergangEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TilbakehoppTest {

    public static final BehandlingStegType STEG_1 = TestBehandlingStegType.STEG_1;
    public static final BehandlingStegType STEG_2 = TestBehandlingStegType.STEG_2;
    public static final BehandlingStegType STEG_3 = TestBehandlingStegType.STEG_3;

    static List<StegTransisjon> transisjoner = new ArrayList<>();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private EntityManager em = repoRule.getEntityManager();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(em);
    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(em, kodeverkRepository);
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(em);
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(em);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(em);

    private BehandlingskontrollTransisjonTilbakeføringEventObserver observer =
        new BehandlingskontrollTransisjonTilbakeføringEventObserver(repositoryProvider, behandlingModellRepository, null);

    private Behandling behandling;
    private BehandlingLås behandlingLås;

    @Test
    public void skal_gjenåpne_aksjonspunkt_som_oppsto_i_steget_det_hoppes_tilbake_til() {
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medAP(identifisertI(STEG_1), løsesI(STEG_3, INNGANG)));
    }

    @Test
    public void skal_gjenåpne_aksjonspunkter_som_oppsto_før_til_steget_og_som_skulle_utføres_i_eller_etter_til_steget() {
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medAP(identifisertI(STEG_1), løsesI(STEG_3, INNGANG)));
    }

    @Test
    public void skal_avbryte_aksjonspunkter_som_oppsto_etter_tilsteget() {
        assertAPAvbrytesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medAP(identifisertI(STEG_2), løsesI(STEG_2, UTGANG)));
        assertAPAvbrytesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medAP(identifisertI(STEG_2), løsesI(STEG_3, INNGANG)));
        assertAPAvbrytesVedTilbakehopp(fra(STEG_3, UTGANG), til(STEG_1), medAP(identifisertI(STEG_3), løsesI(STEG_3, UTGANG)));
    }

    @Test
    public void skal_gjenåpne_aksjonspunkter_som_oppsto_i_steget_det_hoppes_til() {
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medAP(identifisertI(STEG_2), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medAP(identifisertI(STEG_2), løsesI(STEG_3, INNGANG)));
    }

    @Test
    public void skal_ikke_gjøre_noe_med_aksjonspunkt_som_oppsto_og_løstes_før_steget_det_hoppes_til() {
        assertAPUendretVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medAP(identifisertI(STEG_1), løsesI(STEG_1, UTGANG)));
    }

    @Test
    public void skal_ikke_gjøre_noe_med_aksjonspunkt_som_oppsto_før_steget_det_hoppes_til_og_som_løses_etter_punktet_det_hoppes_fra() {
        assertAPUendretVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2),
            medAP(identifisertI(STEG_1), løsesI(STEG_3, UTGANG), medStatus(AksjonspunktStatus.OPPRETTET)));
    }

    @Test
    public void skal_gjenopprette_et_overstyrings_aksjonspunkt_når_det_hoppes() {
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medOverstyringAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medOverstyringAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medOverstyringAP(identifisertI(STEG_2), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medOverstyringAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medOverstyringAP(identifisertI(STEG_2), løsesI(STEG_2, UTGANG)));
    }

    @Test
    public void skal_gjenopprette_et_manuelt_opprettet_aksjonspunkt_når_det_hoppes() {
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medManueltOpprettetAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medManueltOpprettetAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medManueltOpprettetAP(identifisertI(STEG_2), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medManueltOpprettetAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPGjenåpnesVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2), medManueltOpprettetAP(identifisertI(STEG_2), løsesI(STEG_2, UTGANG)));
    }

    @Test
    public void skal_kalle_transisjoner_på_steg_det_hoppes_over() {
        assertThat(transisjonerVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1))).containsOnly(StegTransisjon.hoppTilbakeOver(STEG_1), StegTransisjon.hoppTilbakeOver(STEG_2), StegTransisjon.hoppTilbakeOver(STEG_3));
        assertThat(transisjonerVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2))).containsOnly(StegTransisjon.hoppTilbakeOver(STEG_2), StegTransisjon.hoppTilbakeOver(STEG_3));
        assertThat(transisjonerVedTilbakehopp(fra(STEG_2, UTGANG), til(STEG_2))).containsOnly(StegTransisjon.hoppTilbakeOver(STEG_2));
    }

    @Test
    public void skal_ta_med_transisjon_på_steg_det_hoppes_fra_for_overstyring() {
        assertThat(transisjonerVedOverstyrTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1))).containsOnly(StegTransisjon.hoppTilbakeOver(STEG_1), StegTransisjon.hoppTilbakeOver(STEG_2), StegTransisjon.hoppTilbakeOver(STEG_3));
        assertThat(transisjonerVedOverstyrTilbakehopp(fra(STEG_3, INNGANG), til(STEG_2))).containsOnly(StegTransisjon.hoppTilbakeOver(STEG_2), StegTransisjon.hoppTilbakeOver(STEG_3));
        assertThat(transisjonerVedOverstyrTilbakehopp(fra(STEG_2, UTGANG), til(STEG_2))).containsOnly(StegTransisjon.hoppTilbakeOver(STEG_2));
    }

    @Test
    public void skal_reaktivere_og_gjenåpne_inaktivt_aksjonspunkt_som_hoppes_over() {
        assertAPReaktivertOgGjenåpnetVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medInaktivtOverstyringAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
        assertAPReaktivertOgGjenåpnetVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medInaktivtManueltOpprettetAp(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
    }

    @Test
    public void skal_ikke_reaktivere_inaktive_aksjonspunkter_som_ikke_er_overstyring_eller_manuelt_opprettet() throws Exception {
        assertAPInativtVedTilbakehopp(fra(STEG_3, INNGANG), til(STEG_1), medInaktivtAp(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
    }

    @Before
    public void opprettStatiskModell() {

        // setter opp modell vha SQL. Alternativ med mock er vanskelig å vedlikeholde.
        // Modellen inneholder en sekvens med ett vurderingspunkt per stegs inngang/utgang
        // har et manuelt aksjonspunkt per vurderingspunkt for å støtte å legge aksjonspunkter hvor testene ønsker
        // har et overstyring-aksjonspunkt per vurderingspunkt for å støtte å legge overstyring-aksjonspunkter hvor testene ønsker

        sql("INSERT INTO KODELISTE (id, kodeverk, kode, ekstra_data) values (nextval('seq_kodeliste'), 'BEHANDLING_TYPE', 'BT-TEST', '{behandlingstidFristUker: 3}')");

        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-1', 'test-steg-1', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-2', 'test-steg-2', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-3', 'test-steg-3', 'UTRED', 'test')");

        sql("INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE) VALUES (nextval('SEQ_BEHANDLING_TYPE_STEG_SEKV'), 'BT-TEST', 'STEG-1', 1, 'FP')");
        sql("INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE) VALUES (nextval('SEQ_BEHANDLING_TYPE_STEG_SEKV'), 'BT-TEST', 'STEG-2', 2, 'FP')");
        sql("INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE) VALUES (nextval('SEQ_BEHANDLING_TYPE_STEG_SEKV'), 'BT-TEST', 'STEG-3', 3, 'FP')");

        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-1.INN', 'STEG-1', 'INN', 'STEG-1.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-2.INN', 'STEG-2', 'INN', 'STEG-2.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-3.INN', 'STEG-3', 'INN', 'STEG-3.INN')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-1.UT', 'STEG-1', 'UT', 'STEG-1.UT')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-2.UT', 'STEG-2', 'UT', 'STEG-2.UT')");
        sql("INSERT INTO VURDERINGSPUNKT_DEF (KODE, BEHANDLING_STEG, VURDERINGSPUNKT_TYPE, NAVN) VALUES ('STEG-3.UT', 'STEG-3', 'UT', 'STEG-3.UT')");

        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-1-INN', 'STEG-1-INN', 'STEG-1.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-2-INN', 'STEG-2-INN', 'STEG-2.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-3-INN', 'STEG-3-INN', 'STEG-3.INN', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-1-UT', 'STEG-1-UT', 'STEG-1.UT', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-2-UT', 'STEG-2-UT', 'STEG-2.UT', 'N', '-', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-3-UT', 'STEG-3-UT', 'STEG-3.UT', 'N', '-', '-')");

        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-1-INN-OVST', 'STEG-1-INN-overstyring', 'STEG-1.INN', 'N', '-', 'OVST', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-2-INN-OVST', 'STEG-2-INN-overstyring', 'STEG-2.INN', 'N', '-', 'OVST', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-3-INN-OVST', 'STEG-3-INN-overstyring', 'STEG-3.INN', 'N', '-', 'OVST', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-1-UT-OVST', 'STEG-1-UT-overstyring', 'STEG-1.UT', 'N', '-', 'OVST', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-2-UT-OVST', 'STEG-2-UT-overstyring', 'STEG-2.UT', 'N', '-', 'OVST', '-')");
        sql("INSERT INTO AKSJONSPUNKT_DEF (KODE, NAVN, VURDERINGSPUNKT, TOTRINN_BEHANDLING_DEFAULT, VILKAR_TYPE, AKSJONSPUNKT_TYPE, SKJERMLENKE_TYPE) VALUES ('STEG-3-UT-OVST', 'STEG-3-UT-overstyring', 'STEG-3.UT', 'N', '-', 'OVST', '-')");


        em.flush();
    }

    private void sql(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }

    private void assertAPGjenåpnesVedTilbakehopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(OPPRETTET);
    }

    private void assertAPReaktivertOgGjenåpnetVedTilbakehopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        Aksjonspunkt aksjonspunkt = utførTilbakehoppReturnerAksjonspunkt(fra, til, ap);
        assertThat(aksjonspunkt.erAktivt()).isTrue();
        assertThat(aksjonspunkt.getStatus()).isEqualTo(OPPRETTET);
    }

    private void assertAPInativtVedTilbakehopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        Aksjonspunkt aksjonspunkt = utførTilbakehoppReturnerAksjonspunkt(fra, til, ap);
        assertThat(aksjonspunkt.erAktivt()).isFalse();
    }

    private void assertAPAvbrytesVedTilbakehopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(AVBRUTT);
    }

    private void assertAPUendretVedTilbakehopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        AksjonspunktStatus orginalStatus = ap.getStatus();
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(orginalStatus);
    }

    private AbstractComparableAssert<?, AksjonspunktStatus> assertAPStatusEtterHopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        Aksjonspunkt aksjonspunkt = utførTilbakehoppReturnerAksjonspunkt(fra, til, ap);
        return assertThat(aksjonspunkt.getStatus());
    }

    private List<StegTransisjon> transisjonerVedTilbakehopp(StegPort fra, BehandlingStegType til) {
        //skal ikke spille noen rolle for transisjoner hvilke aksjonspunkter som finnes
        Aksjonspunkt ap = medManueltOpprettetAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG));

        transisjoner.clear();
        utførTilbakehoppReturnerAksjonspunkt(fra, til, ap);
        return transisjoner;
    }

    private List<StegTransisjon> transisjonerVedOverstyrTilbakehopp(StegPort fra, BehandlingStegType til) {
        //skal ikke spille noen rolle for transisjoner hvilke aksjonspunkter som finnes
        Aksjonspunkt ap = medManueltOpprettetAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG));

        transisjoner.clear();
        utførOverstyringTilbakehoppReturnerAksjonspunkt(fra, til, ap);
        return transisjoner;
    }

    private Aksjonspunkt utførTilbakehoppReturnerAksjonspunkt(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        BehandlingStegStatus fraStatus = getBehandlingStegFraStatus(fra);

        StegTilstand fraTilstand = new StegTilstand(fra.getSteg(), fraStatus);
        StegTilstand tilTilstand = new StegTilstand(til, BehandlingStegStatus.UTFØRT);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), behandlingLås);
        BehandlingStegOvergangEvent.BehandlingStegTilbakeføringEvent event =
            new BehandlingStegOvergangEvent.BehandlingStegTilbakeføringEvent(kontekst, Optional.of(fraTilstand), Optional.of(tilTilstand));

        //act
        observer.observerBehandlingSteg(event);

        return ap;
    }

    private Aksjonspunkt utførOverstyringTilbakehoppReturnerAksjonspunkt(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        BehandlingStegStatus fraStatus = getBehandlingStegFraStatus(fra);

        StegTilstand fraTilstand = new StegTilstand(fra.getSteg(), fraStatus);
        StegTilstand tilTilstand = new StegTilstand(til, BehandlingStegStatus.UTFØRT);

        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), behandlingLås);
        BehandlingStegOvergangEvent.BehandlingStegTilbakeføringEvent event =
            new BehandlingStegOvergangEvent.BehandlingStegOverstyringTilbakeføringEvent(kontekst, Optional.of(fraTilstand), Optional.of(tilTilstand));

        //act
        observer.observerBehandlingSteg(event);

        return ap;
    }

    private BehandlingStegStatus getBehandlingStegFraStatus(StegPort fra) {
        BehandlingStegStatus fraStatus;
        String fraPort = fra.getPort().getDbKode();
        if (fraPort.equals(VurderingspunktDefinisjon.Type.INNGANG.getDbKode())) {
            fraStatus = BehandlingStegStatus.INNGANG;
        } else if (fraPort.equals(VurderingspunktDefinisjon.Type.UTGANG.getDbKode())) {
            fraStatus = BehandlingStegStatus.UTGANG;
        } else {
            throw new IllegalStateException("BehandlingStegStatus " + fraPort + " ikke støttet i testen");
        }
        return fraStatus;
    }

    private Aksjonspunkt medOverstyringAP(BehandlingStegType identifisertI, StegPort port) {
        return medAP(identifisertI, port, AksjonspunktType.OVERSTYRING, AksjonspunktStatus.UTFØRT, true);
    }

    private Aksjonspunkt medInaktivtOverstyringAP(BehandlingStegType identifisertI, StegPort port) {
        Aksjonspunkt ap = medOverstyringAP(identifisertI, port);
        aksjonspunktRepository.deaktiver(ap);
        return ap;
    }

    private Aksjonspunkt medInaktivtManueltOpprettetAp(BehandlingStegType identifisertI, StegPort port) {
        Aksjonspunkt ap = medManueltOpprettetAP(identifisertI, port);
        aksjonspunktRepository.deaktiver(ap);
        return ap;
    }

    private Aksjonspunkt medInaktivtAp(BehandlingStegType identifisertI, StegPort port) {
        Aksjonspunkt ap = medAP(identifisertI, port);
        aksjonspunktRepository.deaktiver(ap);
        return ap;
    }

    private Aksjonspunkt medManueltOpprettetAP(BehandlingStegType identifisertI, StegPort port) {
        return medAP(identifisertI, port, AksjonspunktType.MANUELL, AksjonspunktStatus.UTFØRT, true);
    }

    private Aksjonspunkt medAP(BehandlingStegType identifisertI, StegPort port) {
        return medAP(identifisertI, port, AksjonspunktType.MANUELL, AksjonspunktStatus.UTFØRT, false);
    }

    private Aksjonspunkt medAP(BehandlingStegType identifisertI, StegPort port, AksjonspunktStatus status) {
        return medAP(identifisertI, port, AksjonspunktType.MANUELL, status, false);
    }

    private Aksjonspunkt medAP(BehandlingStegType identifisertI, StegPort port, AksjonspunktType type, AksjonspunktStatus status, boolean manueltOpprettet) {
        clearTransisjoner();
        String apKode = port.getSteg().getKode() + "-" + port.getPort().getDbKode() + (AksjonspunktType.OVERSTYRING.equals(type) ? "-OVST" : "");
        AksjonspunktDefinisjon ad = aksjonspunktRepository.finnAksjonspunktDefinisjon(apKode);
        BehandlingStegType idSteg = behandlingRepository.finnBehandlingStegType(identifisertI.getKode());

        Behandling ytelseBehandling = ScenarioMorSøkerEngangsstønad.forDefaultAktør().lagre(repositoryProvider, resultatRepositoryProvider);
        behandling = Behandling.nyBehandlingFor(ytelseBehandling.getFagsak(), TestBehandlingType.TEST).build();
        behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        Aksjonspunkt ap = aksjonspunktRepository.leggTilAksjonspunkt(behandling, ad, idSteg);


        if (status.getKode().equals(AksjonspunktStatus.UTFØRT.getKode())) {
            aksjonspunktRepository.setTilUtført(ap, "ferdig");
        } else if (status.getKode().equals(AksjonspunktStatus.OPPRETTET.getKode())) {
            //dette er default-status ved opprettelse
        } else {
            throw new IllegalArgumentException("Testen støtter ikke status " + status + " du må evt. utvide testen");
        }

        if (manueltOpprettet) {
            aksjonspunktRepository.setTilManueltOpprettet(ap);
        }

        behandlingRepository.lagre(behandling, behandlingLås);

        return ap;
    }

    private void clearTransisjoner() {
        transisjoner.clear();
    }

    static abstract class AbstractTestSteg implements BehandlingSteg {

        private final BehandlingStegType behandlingStegType;

        protected AbstractTestSteg(BehandlingStegType behandlingStegType) {
            this.behandlingStegType = behandlingStegType;
        }

        @Override
        public BehandleStegResultat utførSteg(BehandlingskontrollKontekst kontekst) {
            return null;
        }

        @Override
        public void vedTransisjon(BehandlingskontrollKontekst kontekst, Behandling behandling, BehandlingStegModell modell, TransisjonType transisjonType, BehandlingStegType førsteSteg, BehandlingStegType sisteSteg, TransisjonType inngangUtgang) {
            transisjoner.add(new StegTransisjon(transisjonType, behandlingStegType));
        }

    }

    @BehandlingStegRef(kode = "STEG-1")
    @BehandlingTypeRef("BT-TEST")
    @FagsakYtelseTypeRef("FP")
    public static class TestSteg1 extends AbstractTestSteg {

        public TestSteg1() {
            super(STEG_1);
        }
    }

    @BehandlingStegRef(kode = "STEG-2")
    @BehandlingTypeRef("BT-TEST")
    @FagsakYtelseTypeRef("FP")
    public static class TestSteg2 extends AbstractTestSteg {

        public TestSteg2() {
            super(STEG_2);
        }
    }

    @BehandlingStegRef(kode = "STEG-3")
    @BehandlingTypeRef("BT-TEST")
    @FagsakYtelseTypeRef("FP")
    public static class TestSteg3 extends AbstractTestSteg {

        public TestSteg3() {
            super(STEG_3);
        }
    }

    private BehandlingStegType til(BehandlingStegType steg) {
        return steg;
    }

    private StegPort fra(BehandlingStegType steg, VurderingspunktDefinisjon.Type port) {
        return new StegPort(steg, port);
    }

    private StegPort løsesI(BehandlingStegType steg, VurderingspunktDefinisjon.Type port) {
        return new StegPort(steg, port);
    }

    private BehandlingStegType identifisertI(BehandlingStegType steg) {
        return steg;

    }

    private AksjonspunktStatus medStatus(AksjonspunktStatus status) {
        return status;
    }

    static class StegPort {


        private final BehandlingStegType steg;

        private final VurderingspunktDefinisjon.Type port;

        public StegPort(BehandlingStegType steg, VurderingspunktDefinisjon.Type port) {
            this.steg = steg;
            this.port = port;
        }

        public BehandlingStegType getSteg() {
            return steg;
        }

        public VurderingspunktDefinisjon.Type getPort() {
            return port;
        }

    }

    private static class TestBehandlingType extends BehandlingType {
        static BehandlingType TEST = new TestBehandlingType("BT-TEST");

        protected TestBehandlingType(String kode) {
            super(kode);
        }
    }

    private static class TestBehandlingStegType extends BehandlingStegType {
        static BehandlingStegType STEG_1 = new TestBehandlingStegType("STEG-1");
        static BehandlingStegType STEG_2 = new TestBehandlingStegType("STEG-2");
        static BehandlingStegType STEG_3 = new TestBehandlingStegType("STEG-3");

        public TestBehandlingStegType(String kode) {
            super(kode);
        }
    }
}
