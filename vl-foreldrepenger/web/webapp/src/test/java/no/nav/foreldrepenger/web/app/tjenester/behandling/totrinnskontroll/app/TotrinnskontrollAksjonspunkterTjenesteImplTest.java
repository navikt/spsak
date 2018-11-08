package no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnresultatgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto.TotrinnskontrollAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.totrinnskontroll.dto.TotrinnskontrollSkjermlenkeContextDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class TotrinnskontrollAksjonspunkterTjenesteImplTest {

    private static final BehandlingStegType STEG_KONTROLLER_FAKTA = BehandlingStegType.KONTROLLER_FAKTA;
    private static final BehandlingStegType STEG_FATTE_VEDTAK = BehandlingStegType.FATTE_VEDTAK;

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private TotrinnskontrollAksjonspunkterTjeneste totrinnskontrollAksjonspunkterTjeneste;
    private TotrinnTjeneste totrinnTjeneste = Mockito.mock(TotrinnTjeneste.class);
    private TotrinnsaksjonspunktDtoTjeneste totrinnsaksjonspunktDtoTjeneste = Mockito.mock(TotrinnsaksjonspunktDtoTjeneste.class);
    private Behandling behandling;
    private Totrinnresultatgrunnlag totrinnresultatgrunnlag;

    @Inject
    private AksjonspunktRepository aksjonspunktRepository;

    @Inject
    private InternalManipulerBehandling manipulerInternBehandling;

    @Before
    public void oppsett() {
        totrinnskontrollAksjonspunkterTjeneste = new TotrinnskontrollAksjonspunkterTjenesteImpl(totrinnsaksjonspunktDtoTjeneste, totrinnTjeneste);
        totrinnresultatgrunnlag = new Totrinnresultatgrunnlag(behandling, null,
            null, null, null);
    }

    // ------------------------------------------------------------ //
    // Tester for metoden 'hentTotrinnsSkjermlenkeContext'          //
    // ------------------------------------------------------------ //

    @Test
    public void skal_hente_tom_skjermlenkecontext_for_behandling_med_ikke_status_FATTER_VEDTAK_og_ingen_totrinnsvurdering_og_ingen_aksjonspunkter(){
        // Arrange
        opprettBehandlingForFP(Optional.empty());
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_KONTROLLER_FAKTA);
        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.empty(), Optional.empty());
        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.emptyList());
        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
        // Assert
        assertThat(context).isEmpty();
    }

    @Test
    public void skal_hente_tom_skjermlenkecontext_for_behandling_med_status_FATTER_VEDTAK_og_ingen_totrinnsvurdering_og_ingen_aksjonspunkter(){
        // Arrange
        opprettBehandlingForFP(Optional.empty());
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_FATTE_VEDTAK);
        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.empty(), Optional.empty());
        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.emptyList());
        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
        // Assert
        assertThat(context).isEmpty();
    }

    @Test
    public void skal_hente_tom_skjermlenkecontext_for_behandling_med_ikke_status_FATTER_VEDTAK_og_med_totrinnsvurdering_og_ingen_aksjonspunkter(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        boolean ttvGodkjent = false;

        opprettBehandlingForFP(Optional.empty());
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_KONTROLLER_FAKTA);

        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.empty(), Optional.empty());
        Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
        // Assert
        assertThat(context).isEmpty();

    }

    @Test
    public void skal_hente_tom_skjermlenkecontext_for_behandling_med_status_FATTER_VEDTAK_og_med_totrinnsvurdering_og_ingen_aksjonspunkter(){
        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        boolean ttvGodkjent = false;

        opprettBehandlingForFP(Optional.empty());
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_FATTE_VEDTAK);

        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.empty(), Optional.empty());
        Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
        // Assert
        assertThat(context).isEmpty();
    }

    @Test
    public void skal_hente_en_skjermlenketype_og_ett_totrinnskontrollaksjonspunkt_for_behandling_med_en_totrinnsvurdering_og_ett_aksjonspunkt_som_ikke_omhandler_mottat_stotte_eller_omsorgsovertakelse(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        boolean ttvGodkjent = false;
        boolean apAvbrutt = false;

        opprettBehandlingForFP(Optional.empty());

        Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);
        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.of(ttv));
        opprettAksjonspunkt(behandling, aksjonspunktDefinisjon, apAvbrutt);

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);

        // Assert
        assertThat(context).hasSize(1);

        TotrinnskontrollSkjermlenkeContextDto totrinnskontrollSkjermlenkeContextDto = context.get(0);
        assertThat(totrinnskontrollSkjermlenkeContextDto.getSkjermlenkeType()).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode());

        List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter = totrinnskontrollSkjermlenkeContextDto.getTotrinnskontrollAksjonspunkter();
        assertThat(totrinnskontrollAksjonspunkter).hasSize(1);

        TotrinnskontrollAksjonspunkterDto enesteTotrinnskontrollAksjonspunkt = totrinnskontrollAksjonspunkter.get(0);
        assertThat(enesteTotrinnskontrollAksjonspunkt.getAksjonspunktKode()).isEqualTo(aksjonspunktDefinisjon.getKode());
        assertThat(enesteTotrinnskontrollAksjonspunkt.getTotrinnskontrollGodkjent()).isFalse();

    }

    @Test
    public void skal_hente_en_skjermlenketype_og_ett_totrinnskontrollaksjonspunkt_for_behandling_med_en_totrinnsvurdering_og_ett_aksjonspunkt_som_omhandler_mottat_stotte(){

        // Arrange
        List<AksjonspunktDefinisjon> aksjonspunktDefinisjons = new ArrayList<>();
        aksjonspunktDefinisjons.add(AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE);
        aksjonspunktDefinisjons.add(AksjonspunktDefinisjon.AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE);
        boolean ttvGodkjent = false;
        boolean apAvbrutt = false;

        Map<VilkårType, SkjermlenkeType> vilkårTypeSkjermlenkeTypeMap = new HashMap<>();
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FØDSELSVILKÅRET_MOR, SkjermlenkeType.PUNKT_FOR_FOEDSEL);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR, SkjermlenkeType.PUNKT_FOR_FOEDSEL);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, SkjermlenkeType.PUNKT_FOR_ADOPSJON);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, SkjermlenkeType.PUNKT_FOR_ADOPSJON);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.OMSORGSVILKÅRET, SkjermlenkeType.PUNKT_FOR_OMSORG);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD, SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD, SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR);

        for (AksjonspunktDefinisjon aksjonspunktDefinisjon : aksjonspunktDefinisjons) {
            vilkårTypeSkjermlenkeTypeMap.keySet().forEach(vilkårType -> {

                opprettBehandlingForFP(Optional.of(vilkårType));

                Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);
                TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.of(ttv));
                opprettAksjonspunkt(behandling, aksjonspunktDefinisjon, apAvbrutt);

                setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

                // Act
                List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);

                // Arrange
                assertThat(context).hasSize(1);

                TotrinnskontrollSkjermlenkeContextDto totrinnskontrollSkjermlenkeContextDto = context.get(0);
                assertThat(totrinnskontrollSkjermlenkeContextDto.getSkjermlenkeType()).isEqualTo(vilkårTypeSkjermlenkeTypeMap.get(vilkårType).getKode());

                List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter = totrinnskontrollSkjermlenkeContextDto.getTotrinnskontrollAksjonspunkter();
                assertThat(totrinnskontrollAksjonspunkter).hasSize(1);

                TotrinnskontrollAksjonspunkterDto enesteTotrinnskontrollAksjonspunkt = totrinnskontrollAksjonspunkter.get(0);
                assertThat(enesteTotrinnskontrollAksjonspunkt.getAksjonspunktKode()).isEqualTo(aksjonspunktDefinisjon.getKode());
                assertThat(enesteTotrinnskontrollAksjonspunkt.getTotrinnskontrollGodkjent()).isFalse();

            });
        }

    }

    @Test
    public void skal_hente_en_skjermlenketype_og_ett_totrinnskontrollaksjonspunkt_for_behandling_men_en_totrinnsvurdering_og_ett_aksjonspunkt_som_omhander_omsorgsovertakelse(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE;
        boolean ttvGodkjent = true;
        boolean apAvbrutt = false;

        Map<FagsakYtelseType, SkjermlenkeType> fagsakYtelseTypeSkjermlenkeTypeMap = new HashMap<>();
        fagsakYtelseTypeSkjermlenkeTypeMap.put(FagsakYtelseType.ENGANGSTØNAD, SkjermlenkeType.FAKTA_OM_OMSORG_OG_FORELDREANSVAR);
        fagsakYtelseTypeSkjermlenkeTypeMap.put(FagsakYtelseType.FORELDREPENGER, SkjermlenkeType.FAKTA_FOR_OMSORG);
        fagsakYtelseTypeSkjermlenkeTypeMap.put(FagsakYtelseType.ENDRING_FORELDREPENGER, SkjermlenkeType.FAKTA_FOR_OMSORG);

        fagsakYtelseTypeSkjermlenkeTypeMap.keySet().forEach(fagsakYtelseType -> {

            if (fagsakYtelseType.gjelderEngangsstønad()) {
                opprettBehandlingForEngangsstønad();
            } else {
                opprettBehandlingForFP(Optional.empty());
            }

            Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);
            TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.of(ttv));
            opprettAksjonspunkt(behandling, aksjonspunktDefinisjon, apAvbrutt);

            setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

            // Act
            List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);

            // Arrange
            assertThat(context).hasSize(1);

            TotrinnskontrollSkjermlenkeContextDto totrinnskontrollSkjermlenkeContextDto = context.get(0);
            assertThat(totrinnskontrollSkjermlenkeContextDto.getSkjermlenkeType()).isEqualTo(fagsakYtelseTypeSkjermlenkeTypeMap.get(fagsakYtelseType).getKode());

            List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter = totrinnskontrollSkjermlenkeContextDto.getTotrinnskontrollAksjonspunkter();
            assertThat(totrinnskontrollAksjonspunkter).hasSize(1);

            TotrinnskontrollAksjonspunkterDto enesteTotrinnskontrollAksjonspunkt = totrinnskontrollAksjonspunkter.get(0);
            assertThat(enesteTotrinnskontrollAksjonspunkt.getAksjonspunktKode()).isEqualTo(aksjonspunktDefinisjon.getKode());
            assertThat(enesteTotrinnskontrollAksjonspunkt.getTotrinnskontrollGodkjent()).isTrue();

        });

    }

    @Test
    public void skal_hente_tom_skjermlenkecontext_for_behandling_med_en_totrinnsvurdering_og_ett_aksjonspunkt_som_omhandler_mottate_stotte_men_hvor_skjermlenketypen_blir_underfinert(){

        // Arrange
        List<AksjonspunktDefinisjon> aksjonspunktDefinisjons = new ArrayList<>();
        aksjonspunktDefinisjons.add(AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE);
        aksjonspunktDefinisjons.add(AksjonspunktDefinisjon.AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE);
        boolean ttvGodkjent = false;
        boolean apAvbrutt = false;

        Map<VilkårType, SkjermlenkeType> vilkårTypeSkjermlenkeTypeMap = new HashMap<>();
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.SØKERSOPPLYSNINGSPLIKT, SkjermlenkeType.UDEFINERT);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.MEDLEMSKAPSVILKÅRET, SkjermlenkeType.UDEFINERT);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.UDEFINERT, SkjermlenkeType.UDEFINERT);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.SØKNADSFRISTVILKÅRET, SkjermlenkeType.UDEFINERT);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.OPPTJENINGSVILKÅRET, SkjermlenkeType.UDEFINERT);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.OPPTJENINGSPERIODEVILKÅR, SkjermlenkeType.UDEFINERT);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.BEREGNINGSGRUNNLAGVILKÅR, SkjermlenkeType.UDEFINERT);

        for (AksjonspunktDefinisjon aksjonspunktDefinisjon : aksjonspunktDefinisjons) {
            vilkårTypeSkjermlenkeTypeMap.keySet().forEach(vilkårType -> {

                opprettBehandlingForFP(Optional.of(vilkårType));

                Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);
                TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.of(ttv));
                opprettAksjonspunkt(behandling, aksjonspunktDefinisjon, apAvbrutt);

                setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

                // Act
                List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);
                // Arrange
                assertThat(context).hasSize(0);

            });
        }

    }

    @Test
    public void skal_hente_en_skjermlenketype_og_ett_totrinnskontrollaksjonspunkt_for_behandling_med_status_FATTE_VEDTAK_og_ingen_totrinnsvurdering_og_ett_aksjonspunkt(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        boolean apAvbrutt = false;

        opprettBehandlingForFP(Optional.empty());
        manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, STEG_FATTE_VEDTAK);

        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.empty());
        opprettAksjonspunkt(behandling, aksjonspunktDefinisjon, apAvbrutt);

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.emptyList());

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);

        // Assert
        assertThat(context).hasSize(1);
        assertThat(context.get(0).getSkjermlenkeType()).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode());
        List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter = context.get(0).getTotrinnskontrollAksjonspunkter();
        assertThat(totrinnskontrollAksjonspunkter).hasSize(1);
        assertThat(totrinnskontrollAksjonspunkter.get(0).getAksjonspunktKode()).isEqualTo(AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT.getKode());

    }

    @Test
    public void skal_hente_en_skjermlenketype_og_ett_ikke_godkjent_totrinnskontrollaksjonspunkt_for_behandling_med_en_godkjent_totrinnsvurdering_og_ett_aksjonspunkt_som_ikke_har_samme_aksjonspunktdefinisjon(){

        AksjonspunktDefinisjon adFraAksjonspunkt = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        AksjonspunktDefinisjon adFraTotrinnvurdering = AksjonspunktDefinisjon.VENT_PÅ_FØDSEL;
        boolean ttvGodkjent = true;
        boolean apAvbrutt = false;

        opprettBehandlingForFP(Optional.empty());

        Totrinnsvurdering ttvFraBehandling = opprettTotrinnsvurdering(behandling, adFraTotrinnvurdering, ttvGodkjent);
        Totrinnsvurdering ttvOpprettetAvMetode = opprettTotrinnsvurdering(behandling, adFraAksjonspunkt, !ttvGodkjent);
        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(adFraAksjonspunkt), Optional.of(ttvOpprettetAvMetode));
        opprettAksjonspunkt(behandling, adFraAksjonspunkt, apAvbrutt);

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttvFraBehandling));

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);

        // Assert
        assertThat(context).hasSize(1);
        assertThat(context.get(0).getSkjermlenkeType()).isEqualTo(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP.getKode());

        List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter = context.get(0).getTotrinnskontrollAksjonspunkter();
        assertThat(totrinnskontrollAksjonspunkter).hasSize(1);

        TotrinnskontrollAksjonspunkterDto enesteTotrinnskontrollAksjonspunkt = totrinnskontrollAksjonspunkter.get(0);
        assertThat(enesteTotrinnskontrollAksjonspunkt.getAksjonspunktKode()).isEqualTo(adFraAksjonspunkt.getKode());
        assertThat(enesteTotrinnskontrollAksjonspunkt.getTotrinnskontrollGodkjent()).isFalse();

    }

    @Test
    public void skal_hente_en_tom_skjermlenkecontext_for_behandling_med_en_totrinnsvurdering_og_ett_avbrutt_aksjonspunkt(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        boolean ttvGodkjent = false;
        boolean apAvbrutt = true;

        opprettBehandlingForFP(Optional.empty());

        Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);
        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.of(ttv));
        opprettAksjonspunkt(behandling, aksjonspunktDefinisjon, apAvbrutt);

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsSkjermlenkeContext(behandling);

        // Assert
        assertThat(context).hasSize(0);

    }

    // ------------------------------------------------------------ //
    // Tester for metoden 'hentTotrinnsvurderingSkjermlenkeContext' //
    // ------------------------------------------------------------ //

    @Test
    public void skal_hente_en_tom_skjermlenkecontext_for_en_behandling_med_en_totrinnsvurdering_med_et_aksjonspunktdefinisjon_som_gir_en_undefinert_skjermlenketype(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon = AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
        boolean ttvGodkjent = false;

        opprettBehandlingForFP(Optional.empty());

        Totrinnsvurdering ttv = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon, ttvGodkjent);
        TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon), Optional.of(ttv));

        setFelleseMockMetoder(totrinnskontrollAksjonspunkterDto, Collections.singletonList(ttv));

        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling);

        // Assert
        assertThat(context).hasSize(0);

    }

    @Test
    public void skal_hente_en_tom_skjermlenkecontext_for_en_behandling_med_ingen_totrinnaksjonspunktvurdering(){
        // Arrange
        opprettBehandlingForFP(Optional.empty());
        when(totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling)).thenReturn(Collections.emptyList());
        // Act
        List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling);
        // Assert
        assertThat(context).hasSize(0);
    }

    @Test
    public void skal_hente_en_skjermlenketype_og_to_totrinnskontrollaksjonspunkt_for_behandling_med_to_totrinnsvurdering_med_aksjonspunktdefinisjoner_som_omhandler_mottat_stotte(){

        // Arrange
        AksjonspunktDefinisjon aksjonspunktDefinisjon1 = AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE;
        AksjonspunktDefinisjon aksjonspunktDefinisjon2 = AksjonspunktDefinisjon.AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE;
        boolean ttv1Godkjent = false;
        boolean ttv2Godkjent = true;

        Map<VilkårType, SkjermlenkeType> vilkårTypeSkjermlenkeTypeMap = new HashMap<>();
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FØDSELSVILKÅRET_MOR, SkjermlenkeType.PUNKT_FOR_FOEDSEL);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR, SkjermlenkeType.PUNKT_FOR_FOEDSEL);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, SkjermlenkeType.PUNKT_FOR_ADOPSJON);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.ADOPSJONSVILKARET_FORELDREPENGER, SkjermlenkeType.PUNKT_FOR_ADOPSJON);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.OMSORGSVILKÅRET, SkjermlenkeType.PUNKT_FOR_OMSORG);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD, SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR);
        vilkårTypeSkjermlenkeTypeMap.put(VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD, SkjermlenkeType.PUNKT_FOR_FORELDREANSVAR);

        vilkårTypeSkjermlenkeTypeMap.keySet().forEach(vilkårType -> {

            opprettBehandlingForFP(Optional.of(vilkårType));

            Totrinnsvurdering ttv1 = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon1, ttv1Godkjent);
            TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto1 = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon1), Optional.of(ttv1));

            Totrinnsvurdering ttv2 = opprettTotrinnsvurdering(behandling, aksjonspunktDefinisjon2, ttv2Godkjent);
            TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto2 = opprettTotrinnskontrollAksjonspunkterDto(Optional.of(aksjonspunktDefinisjon2), Optional.of(ttv2));

            when(totrinnTjeneste.hentTotrinngrunnlagHvisEksisterer(behandling)).thenReturn(Optional.of(totrinnresultatgrunnlag));
            when(totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling)).thenReturn(Arrays.asList(ttv1, ttv2));
            when(totrinnsaksjonspunktDtoTjeneste.lagTotrinnskontrollAksjonspunktDto(eq(ttv1), eq(behandling), eq(Optional.of(totrinnresultatgrunnlag))))
                .thenReturn(totrinnskontrollAksjonspunkterDto1);
            when(totrinnsaksjonspunktDtoTjeneste.lagTotrinnskontrollAksjonspunktDto(eq(ttv2), eq(behandling), eq(Optional.of(totrinnresultatgrunnlag))))
                .thenReturn(totrinnskontrollAksjonspunkterDto2);

            // Act
            List<TotrinnskontrollSkjermlenkeContextDto> context = totrinnskontrollAksjonspunkterTjeneste.hentTotrinnsvurderingSkjermlenkeContext(behandling);

            // Arrange
            assertThat(context).hasSize(1);

            TotrinnskontrollSkjermlenkeContextDto totrinnskontrollSkjermlenkeContextDto = context.get(0);
            assertThat(totrinnskontrollSkjermlenkeContextDto.getSkjermlenkeType()).isEqualTo(vilkårTypeSkjermlenkeTypeMap.get(vilkårType).getKode());

            List<TotrinnskontrollAksjonspunkterDto> totrinnskontrollAksjonspunkter = totrinnskontrollSkjermlenkeContextDto.getTotrinnskontrollAksjonspunkter();
            assertThat(totrinnskontrollAksjonspunkter).hasSize(2);

            TotrinnskontrollAksjonspunkterDto førsteTotrinnskontrollAksjonspunkt = totrinnskontrollAksjonspunkter.get(0);
            assertThat(førsteTotrinnskontrollAksjonspunkt.getAksjonspunktKode()).isEqualTo(aksjonspunktDefinisjon1.getKode());
            assertThat(førsteTotrinnskontrollAksjonspunkt.getTotrinnskontrollGodkjent()).isFalse();

            TotrinnskontrollAksjonspunkterDto andreTotrinnskontrollAksjonspunkt = totrinnskontrollAksjonspunkter.get(1);
            assertThat(andreTotrinnskontrollAksjonspunkt.getAksjonspunktKode()).isEqualTo(aksjonspunktDefinisjon2.getKode());
            assertThat(andreTotrinnskontrollAksjonspunkt.getTotrinnskontrollGodkjent()).isTrue();

        });

    }

    // ------------------------------------------------------------ //
    // PRIVATE METODER                                              //
    // ------------------------------------------------------------ //

    private void opprettBehandlingForFP(Optional<VilkårType> vilkårTypeOpt) {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        vilkårTypeOpt.ifPresent(vt -> scenario.leggTilVilkår(vt, VilkårUtfallType.UDEFINERT));
        behandling = scenario.lagMocked();
    }

    private void opprettBehandlingForEngangsstønad() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad.forFødsel();
        behandling = scenario.lagMocked();
    }

    private void setFelleseMockMetoder(TotrinnskontrollAksjonspunkterDto totrinnskontrollAksjonspunkterDto, List<Totrinnsvurdering> ttv) {
        when(totrinnTjeneste.hentTotrinnaksjonspunktvurderinger(behandling)).thenReturn(ttv);
        when(totrinnTjeneste.hentTotrinngrunnlagHvisEksisterer(behandling)).thenReturn(Optional.of(totrinnresultatgrunnlag));
        when(totrinnsaksjonspunktDtoTjeneste.lagTotrinnskontrollAksjonspunktDto(any(), eq(behandling), eq(Optional.of(totrinnresultatgrunnlag))))
            .thenReturn(totrinnskontrollAksjonspunkterDto);
    }

    private TotrinnskontrollAksjonspunkterDto opprettTotrinnskontrollAksjonspunkterDto(Optional<AksjonspunktDefinisjon> aksjonspunktDefinisjonOpt, Optional<Totrinnsvurdering> ttvOpt) {
        TotrinnskontrollAksjonspunkterDto.Builder builder = new TotrinnskontrollAksjonspunkterDto.Builder();
        aksjonspunktDefinisjonOpt.ifPresent(ad -> builder.medAksjonspunktKode(ad.getKode()));
        ttvOpt.ifPresent(ttv -> builder.medTotrinnskontrollGodkjent(ttv.isGodkjent()));
        return  builder.build();
    }

    private Totrinnsvurdering opprettTotrinnsvurdering(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, boolean godkjent) {
        return new Totrinnsvurdering.Builder(behandling, aksjonspunktDefinisjon)
            .medGodkjent(godkjent)
            .build();
    }

    private void opprettAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, boolean erAvbrutt) {
        Aksjonspunkt aksjonspunkt = aksjonspunktRepository.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon);
        aksjonspunktRepository.setToTrinnsBehandlingKreves(aksjonspunkt);
        if (erAvbrutt) {
            aksjonspunktRepository.setTilAvbrutt(aksjonspunkt);
        }
    }

}
