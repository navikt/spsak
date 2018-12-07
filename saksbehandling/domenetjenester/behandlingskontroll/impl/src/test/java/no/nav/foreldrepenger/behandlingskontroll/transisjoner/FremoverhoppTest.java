package no.nav.foreldrepenger.behandlingskontroll.transisjoner;

import static no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon.Type.INNGANG;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon.Type.UTGANG;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.assertj.core.api.AbstractComparableAssert;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepository;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingModellRepositoryImpl;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegModell;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingStegRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTransisjonEvent;
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
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderingspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;

public class FremoverhoppTest {

    public static final BehandlingStegType STEG_1 = TestBehandlingStegType.STEG_1;
    public static final BehandlingStegType STEG_2 = TestBehandlingStegType.STEG_2;
    public static final BehandlingStegType STEG_3 = TestBehandlingStegType.STEG_3;

    static List<no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon> transisjoner = new ArrayList<>();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private EntityManager em = repoRule.getEntityManager();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(em);
    private AksjonspunktRepository aksjonspunktRepository = new AksjonspunktRepositoryImpl(em, kodeverkRepository);
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(em);
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private BehandlingModellRepository behandlingModellRepository = new BehandlingModellRepositoryImpl(em);

    private BehandlingskontrollFremoverhoppTransisjonEventObserver observer =
        new BehandlingskontrollFremoverhoppTransisjonEventObserver(repositoryProvider, behandlingModellRepository);

    private Behandling behandling;
    private BehandlingLås behandlingLås;

    @Test
    public void skal_avbryte_aksjonspunkt_som_skulle_vært_håndtert_i_mellomliggende_steg() {
        assertAPAvbrytesVedFremoverhopp(fra(STEG_1, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG)));
        assertAPAvbrytesVedFremoverhopp(fra(STEG_1, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
    }

    @Test
    public void skal_ikke_gjøre_noe_med_aksjonspunkt_som_oppsto_og_løstes_før_steget_det_hoppes_fra() {
        assertAPUendretVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_1, UTGANG), medStatus(UTFØRT)));
    }

    @Test
    public void skal_ikke_gjøre_noe_med_aksjonspunkt_som_løstes_ved_inngang_til_steget_når_det_hoppes_fra_utgang_av_steget() {
        assertAPUendretVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG), medStatus(UTFØRT)));
    }

    @Test
    public void skal_avbryte_aksjonspunkt_i_utgang_av_frasteget_når_frasteget_ikke_er_ferdig() {
        assertAPAvbrytesVedFremoverhopp(fra(STEG_2, INNGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
        assertAPAvbrytesVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_2, UTGANG)));
    }

    @Test
    public void skal_ikke_gjøre_noe_med_aksjonspunkt_som_skal_løses_i_steget_det_hoppes_til() {
        assertAPUendretVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_3, INNGANG)));
        assertAPUendretVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_1), løsesI(STEG_3, UTGANG)));
        assertAPUendretVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_2), løsesI(STEG_3, UTGANG)));
        assertAPUendretVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3), medAP(identifisertI(STEG_2), løsesI(STEG_3, UTGANG)));
    }

    @Test
    public void skal_kalle_transisjoner_på_steg_det_hoppes_over() throws Exception {
        assertThat(transisjonerVedFremoverhopp(fra(STEG_1, INNGANG), til(STEG_3))).containsOnly(no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon.hoppFremoverOver(STEG_1), no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon.hoppFremoverOver(STEG_2));
        assertThat(transisjonerVedFremoverhopp(fra(STEG_1, UTGANG), til(STEG_3))).containsOnly(no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon.hoppFremoverOver(STEG_2));
        assertThat(transisjonerVedFremoverhopp(fra(STEG_2, INNGANG), til(STEG_3))).containsOnly(no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon.hoppFremoverOver(STEG_2));
        assertThat(transisjonerVedFremoverhopp(fra(STEG_2, UTGANG), til(STEG_3))).isEmpty();
    }

    @Before
    public void opprettStatiskModell() throws Exception {
        sql("INSERT INTO KODELISTE (id, kodeverk, kode, ekstra_data) values (nextval('seq_kodeliste'), 'BEHANDLING_TYPE', 'BT-TEST2', '{behandlingstidFristUker: 3}')");

        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-1', 'test-steg-1', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-2', 'test-steg-2', 'UTRED', 'test')");
        sql("INSERT INTO BEHANDLING_STEG_TYPE (KODE, NAVN, BEHANDLING_STATUS_DEF, BESKRIVELSE) VALUES ('STEG-3', 'test-steg-3', 'UTRED', 'test')");

        sql("INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE) VALUES (nextval('SEQ_BEHANDLING_TYPE_STEG_SEKV'), 'BT-TEST2', 'STEG-1', 1, 'FP')");
        sql("INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE) VALUES (nextval('SEQ_BEHANDLING_TYPE_STEG_SEKV'), 'BT-TEST2', 'STEG-2', 2, 'FP')");
        sql("INSERT INTO BEHANDLING_TYPE_STEG_SEKV (ID, BEHANDLING_TYPE, BEHANDLING_STEG_TYPE, SEKVENS_NR, FAGSAK_YTELSE_TYPE) VALUES (nextval('SEQ_BEHANDLING_TYPE_STEG_SEKV'), 'BT-TEST2', 'STEG-3', 3, 'FP')");

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

        em.flush();
    }

    private void sql(String sql) {
        em.createNativeQuery(sql).executeUpdate();
    }

    private void assertAPAvbrytesVedFremoverhopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(AVBRUTT);
    }

    private void assertAPUendretVedFremoverhopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        AksjonspunktStatus orginalStatus = ap.getStatus();
        assertAPStatusEtterHopp(fra, til, ap).isEqualTo(orginalStatus);
    }

    private List<no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon> transisjonerVedFremoverhopp(StegPort fra, BehandlingStegType til) {
        //skal ikke spille noen rolle for transisjoner hvilke aksjonspunkter som finnes
        Aksjonspunkt ap = medAP(identifisertI(STEG_1), løsesI(STEG_2, INNGANG));

        transisjoner.clear();
        utførFremoverhoppReturnerAksjonspunkt(fra, til, ap);
        return transisjoner;
    }

    private AbstractComparableAssert<?, AksjonspunktStatus> assertAPStatusEtterHopp(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {
        Aksjonspunkt aksjonspunkt = utførFremoverhoppReturnerAksjonspunkt(fra, til, ap);
        return Assertions.assertThat(aksjonspunkt.getStatus());
    }

    private Aksjonspunkt utførFremoverhoppReturnerAksjonspunkt(StegPort fra, BehandlingStegType til, Aksjonspunkt ap) {


        BehandlingStegStatus fraStatus;
        String fraPort = fra.getPort().getDbKode();
        if (fraPort.equals(VurderingspunktDefinisjon.Type.INNGANG.getDbKode())) {
            fraStatus = BehandlingStegStatus.INNGANG;
        } else if (fraPort.equals(VurderingspunktDefinisjon.Type.UTGANG.getDbKode())) {
            fraStatus = BehandlingStegStatus.UTGANG;
        } else {
            throw new IllegalStateException("BehandlingStegStatus " + fraPort + " ikke støttet i testen");
        }

        StegTilstand fraTilstand = new StegTilstand(fra.getSteg(), fraStatus);
        //BehandlingStegTilstand tilTilstand = new BehandlingStegTilstand(behandling, til, BehandlingStegStatus.VENTER);
        Fagsak fagsak = behandling.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), behandlingLås);
        /*BehandlingStegOvergangEvent.BehandlingStegOverhoppEvent behandlingEvent =
            new BehandlingStegOvergangEvent.BehandlingStegOverhoppEvent(kontekst, Optional.of(fraTilstand), Optional.of(tilTilstand));*/
        BehandlingTransisjonEvent transisjonEvent = new BehandlingTransisjonEvent(kontekst, FREMHOPP_TIL_FORESLÅ_VEDTAK, Optional.of(fraTilstand), til, true);


        //act
        observer.observerBehandlingSteg(transisjonEvent);

        return ap;
    }


    private Aksjonspunkt medAP(BehandlingStegType identifisertI, StegPort port) {
        return medAP(identifisertI, port, AksjonspunktStatus.OPPRETTET);
    }

    private Aksjonspunkt medAP(BehandlingStegType identifisertI, StegPort port, AksjonspunktStatus status) {
        String apKode = port.getSteg().getKode() + "-" + port.getPort().getDbKode();

        AksjonspunktDefinisjon ad = aksjonspunktRepository.finnAksjonspunktDefinisjon(apKode);
        BehandlingStegType idSteg = behandlingRepository.finnBehandlingStegType(identifisertI.getKode());

        Behandling ytelseBehandling = ScenarioMorSøkerEngangsstønad.forDefaultAktør().lagre(repositoryProvider);
        behandling = Behandling.nyBehandlingFor(ytelseBehandling.getFagsak(), TestBehandlingType.TEST).build();
        behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
        Aksjonspunkt ap = aksjonspunktRepository.leggTilAksjonspunkt(behandling, ad, idSteg);

        if (status.getKode().equals(UTFØRT.getKode())) {
            aksjonspunktRepository.setTilUtført(ap, "ferdig");
        } else if (status.getKode().equals(AksjonspunktStatus.OPPRETTET.getKode())) {
            //dette er default-status ved opprettelse
        } else {
            throw new IllegalArgumentException("Testen støtter ikke status " + status + " du må evt. utvide testen");
        }


        behandlingRepository.lagre(behandling, behandlingLås);

        return ap;
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
            transisjoner.add(new no.nav.foreldrepenger.behandlingskontroll.observer.StegTransisjon(transisjonType, behandlingStegType));
        }

    }

    @BehandlingStegRef(kode = "STEG-1")
    @BehandlingTypeRef("BT-TEST2")
    @FagsakYtelseTypeRef("FP")
    public static class TestSteg1 extends AbstractTestSteg {
        public TestSteg1() {
            super(STEG_1);
        }
    }

    @BehandlingStegRef(kode = "STEG-2")
    @BehandlingTypeRef("BT-TEST2")
    @FagsakYtelseTypeRef("FP")
    public static class TestSteg2 extends AbstractTestSteg {
        public TestSteg2() {
            super(STEG_2);
        }

    }

    @BehandlingStegRef(kode = "STEG-3")
    @BehandlingTypeRef("BT-TEST2")
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
        static BehandlingType TEST = new TestBehandlingType("BT-TEST2");

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
