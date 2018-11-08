package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_ADOPSJONSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_BEREGNING;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_FØDSELSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_OPPFYLT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.hentFødselsdatoFraFnr;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;

import java.net.URISyntaxException;
import java.time.LocalDate;
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

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.BeregningTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.OverstyrteAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftEktefelleAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAdopsjonsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringFødselsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringMedlemskapsvilkåretDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftDokumentertDatoAksjonspunktDto;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Vedlegg;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingOverstyringTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;

    @Inject
    private DokumentmottakTestUtil hjelper;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private Fagsak fagsak;
    private BehandlingskontrollKontekst kontekst;

    @Before
    public void before() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, behandlingRepository, behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);
    }

    @Test
    public void overstyring_av_medlemskapsvilkåret_og_adopsjonsvilkåret() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate omsorgsovertakelseDato = fødselsdato;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadMorAdopsjon(fødselsdato, fødselsdato, omsorgsovertakelseDato));

        // Act steg 1: Starter prosessen
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Act steg 2: Avklar fakta fra GUI -> Søknadsfristvilkåret innvilges
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, omsorgsovertakelseDato);
        applikasjonstjeneste.bekreftAksjonspunkter(aksjonspunktDtoer, behandlingId);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));

        // Act steg 3: Overstyrer medlemskapsvilkåret til ikke oppfylt
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        OverstyringMedlemskapsvilkåretDto overstyringMedlemskap = new OverstyringMedlemskapsvilkåretDto(false, "Er utvandret", "1021");
        overstyrAksjonspunkt(oppdatertBehandling, overstyringMedlemskap);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_OPPFYLT, Avslagsårsak.SØKER_ER_UTVANDRET, true),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Act steg 4: Overstyrer ombestemmer seg og setter medlemskapsvilkåret som oppfylt likevel
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        overstyringMedlemskap = new OverstyringMedlemskapsvilkåretDto(true, "Tok feil!", null);
        overstyrAksjonspunkt(oppdatertBehandling, overstyringMedlemskap);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT, true),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));

        // Act steg 5: Overstyrer adopsjonsvilkåret til ikke oppfylt
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        OverstyringAdopsjonsvilkåretDto overstyringAdopsjonsvilkåret = new OverstyringAdopsjonsvilkåretDto(false,
                "Dette er ektefelles barn", "1005");
        overstyrAksjonspunkt(oppdatertBehandling, overstyringAdopsjonsvilkåret);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_ADOPSJONSVILKÅRET, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_MEDLEMSKAPSVILKÅRET, AksjonspunktStatus.AVBRUTT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, IKKE_OPPFYLT, Avslagsårsak.EKTEFELLES_SAMBOERS_BARN, true),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT, true),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    @Test
    public void skal_rangere_overstyrt_utfall_over_automatisk_regelutfall_dersom_vilkår_rekjøres_etter_tilbakehopp() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn er registrert i TPS, men fødselsdato matcher ikke
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR).minusMonths(1); // Gir AP: 5027 Sjekk fødsel
        LocalDate mottattDato = fødselsdato;
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadMorFødsel(fødselsdato, mottattDato, antallBarnFraSøknad, fagsak.getAktørId(), emptyList()));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange steg 2: Avklarer fødsel som antall barn = 0 => fødselsvilkår blir av regelmotor automatisk AVSLÅTT
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        SjekkManglendeFodselDto fødselDto = byggManglendeFodselDto(fødselsdato, 0);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, fødselDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1026),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange steg 3: Overstyrer vurdering av fødselsvilkår til OPPFYLT
        OverstyringFødselsvilkåretDto overstyringFødselvilkårDto = new OverstyringFødselsvilkåretDto(true, "ovesrtyrt ok", null);

        // Act
        overstyrAksjonspunkt(oppdatertBehandling, overstyringFødselvilkårDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_FØDSELSVILKÅRET, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT, true),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));

        // Arrange steg 4: Hopp tilbake FØR fødselvilkår - overstyrt vilkårsutfall skal trumfe manuelt
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        fødselDto = byggManglendeFodselDto(fødselsdato, 0);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, fødselDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_FØDSELSVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AVBRUTT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT, VilkårUtfallMerknad.VM_1026,true),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    @Test
    public void overstyring_av_beregning() throws Exception {
        // Arrange
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate omsorgsovertakelseDato = fødselsdato;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadMorAdopsjon(fødselsdato, fødselsdato, omsorgsovertakelseDato));

        // Act steg 1: Starter prosessen
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Act steg 2: Avklar fakta fra GUI -> Søknadsfristvilkåret innvilges
        List<BekreftetAksjonspunktDto> aksjonspunktDtoer = byggAvklarFaktaDtoer(behandlingId, omsorgsovertakelseDato);
        applikasjonstjeneste.bekreftAksjonspunkter(aksjonspunktDtoer, behandlingId);
        utførProsessSteg(behandlingId);

        Behandling oppdatertBehandling = repository.hent(Behandling.class, behandlingId);
        Long opprinneligBeregnetTilkjentYtelse = oppdatertBehandling.getBehandlingsresultat().getBeregningResultat().getSisteBeregning().get().getBeregnetTilkjentYtelse();

        // Assert
        assertUtil.assertSisteBeregning(BeregningTestutfall.resultat(behandlingId, opprinneligBeregnetTilkjentYtelse, null, false));
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårUtfallType.OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));

        Long overstyrtTilkjentYtelse = 1L;

        // Act steg 3: Overstyrer beregningen
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        OverstyringBeregningDto overstyringBeregning = new OverstyringBeregningDto(overstyrtTilkjentYtelse, "Korrigert beløp");

        overstyrAksjonspunkt(oppdatertBehandling, overstyringBeregning);

        // Assert
        assertUtil.assertSisteBeregning(BeregningTestutfall.resultat(behandlingId, overstyrtTilkjentYtelse, opprinneligBeregnetTilkjentYtelse, true));
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_BEREGNING, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));

        // Act steg 4: Overstyrer adopsjonsvilkåret til ikke oppfylt, forventer at den tidligere overstyrte beregningen slettes
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        OverstyringAdopsjonsvilkåretDto overstyringAdopsjonsvilkåret = new OverstyringAdopsjonsvilkåretDto(false, "Barnet er over 15 år",
                "1004");

        overstyrAksjonspunkt(oppdatertBehandling, overstyringAdopsjonsvilkåret);

        // Assert
        assertUtil.assertSisteBeregning(BeregningTestutfall.resultat(behandlingId, null, null, false));
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_ADOPSJONSVILKÅRET, AksjonspunktStatus.UTFØRT),
            AksjonspunktTestutfall.resultat(OVERSTYRING_AV_BEREGNING, AksjonspunktStatus.AVBRUTT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AksjonspunktStatus.OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(ADOPSJONSVILKÅRET_ENGANGSSTØNAD, IKKE_OPPFYLT, Avslagsårsak.BARN_OVER_15_ÅR, true),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }


    private void byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("123"))
            .build();
        fagsakRepository.opprettNy(fagsak);
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        repository.flushAndClear();
    }

    private Soeknad søknadMorAdopsjon(LocalDate fødselsdato, LocalDate søknadsdato, LocalDate adopsjonsdato) {
        return new SøknadTestdataBuilder()
            .medMottattdato(søknadsdato)
            .engangsstønad(new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge())
            .medSøker(ForeldreType.MOR, TpsRepo.STD_KVINNE_AKTØR_ID)
            .medAdopsjon(new SøknadTestdataBuilder.AdopsjonBuilder()
                .medAdopsjonsdato(adopsjonsdato)
                .medAntallBarn(1)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato)))
            .build();
    }

    private List<BekreftetAksjonspunktDto> byggAvklarFaktaDtoer(Long behandlingId,
                                                                LocalDate omsorgsovertakelseDato) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        Map<Integer, LocalDate> fødselsdatoer = new HashMap<>();
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        Optional<UidentifisertBarn> søknadAdopsjonBarn = familieHendelseGrunnlag.getGjeldendeBarna().stream().findFirst();
        if (søknadAdopsjonBarn.isPresent()) {
            int barnId = søknadAdopsjonBarn.get().getBarnNummer();
            fødselsdatoer.put(barnId, LocalDate.now());
            return asList(
                    new BekreftDokumentertDatoAksjonspunktDto("Grunn", omsorgsovertakelseDato, fødselsdatoer),
                    new BekreftEktefelleAksjonspunktDto("Grunn", false));
        }
        return emptyList();
    }


    private void overstyrAksjonspunkt(Behandling oppdatertBehandling, OverstyringAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.overstyr(OverstyrteAksjonspunkterDto.lagDto(oppdatertBehandling.getId(), oppdatertBehandling.getVersjon(),
                singletonList(dto)));
        kjørProsessTasks();
    }

    private Soeknad søknadEngangstønadMorFødsel(LocalDate fødselsdato, LocalDate mottattDato, int antallBarnFraSøknad, AktørId søkerAktørid,
                                                List<Vedlegg> vedlegg) {
        return new SøknadTestdataBuilder()
            .medPåkrevdeVedleggListe(vedlegg) // Kan denne defaultes?
            .engangsstønad(new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge())
            .medMottattdato(mottattDato)
            .medSøker(ForeldreType.MOR, søkerAktørid)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(antallBarnFraSøknad))
            .build();
    }

    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(),
            singletonList(dto)));
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

    private SjekkManglendeFodselDto byggManglendeFodselDto(LocalDate fødselsdato, int antallBarn) {
        return new SjekkManglendeFodselDto(
            "begrunnelse",
            true,
            false,
            fødselsdato,
            antallBarn);
    }

    private void kjørProsessTasks() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }
}
