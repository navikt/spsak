package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall.resultat;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.InntektConsumerProducerMock;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl.OpprettOppgaveForBehandlingTask;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarSaksopplysningerDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftBosattVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftErMedlemVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftOppholdVurderingDto.BekreftOppholdsrettVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereYtelseSammeBarnSøkerAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.vedtak.felles.integrasjon.inntekt.InntektConsumerProducer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

/**
Tester verdikjede fra mottak av søknad for Medlemskapsvilkåret inklusiv utføring av aksjonspunkter.

Denne har harde avhengigheter til klassene:
MedlemConsumerProducerMock og
TpsRepo
 */
@RunWith(CdiRunner.class)
public class BehandlingMedlemsvilkåretTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private InntektConsumerProducer inntektConsumerProducer;

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;

    @Inject
    private DokumentmottakTestUtil hjelper;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    private BehandlingskontrollKontekst kontekst;

    @Before
    public void oppsett() {
        ((InntektConsumerProducerMock) inntektConsumerProducer).setReturnerInntekt(false);

        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, behandlingRepository, behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);
    }

    @After
    public void opprydding() {
        ((InntektConsumerProducerMock) inntektConsumerProducer).setReturnerInntekt(false);
    }

    @Test
    public void medlemskap_happy_case() throws URISyntaxException {
        // Arrange
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_ENDELIG_PERIODE_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_ENDELIG_PERIODE_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VurdereYtelseSammeBarnSøkerAksjonspunktDto dto = new VurdereYtelseSammeBarnSøkerAksjonspunktDto("bare tull", true);
        bekreftAksjonspunkt(behandling, dto);


        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
    }

    /*
     * Tester aksjonspunkt 5022 (AVKLAR_FAKTA_FOR_PERSONSTATUS)
     */
    @Test
    public void skal_opprette_aksjonspunkt_for_søker_med_personstatus_uregistrert_og_deretter_henlegge_behandling() throws Exception  {
        // Arrange steg 1
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()), false, false);

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        List<ProsessTaskEntitet> opprettingsprossess = hentProsesstasker(OpprettOppgaveForBehandlingTask.TASKTYPE);
        assertThat(opprettingsprossess).hasSize(1);

        // Arrange steg 2
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        final AvklarSaksopplysningerDto dto = new AvklarSaksopplysningerDto("Bosatt i Norge", "BOSA", false);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, AksjonspunktStatus.UTFØRT),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        Behandling resulat = behandlingRepository.hentBehandling(behandlingId);
        assertThat(resulat.getStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
    }

    @Test
    public void skal_avslå_dersom_søker_er_registrert_som_utvandret() throws URISyntaxException {
        // Arrange
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_UTVANDRET_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_UTVANDRET_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        VurdereYtelseSammeBarnSøkerAksjonspunktDto dto = new VurdereYtelseSammeBarnSøkerAksjonspunktDto("bare tull", true);
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1021));
    }

    /*
     * Tester aksjonspunkt 5020 (AVKLAR_OM_ER_BOSATT)
     */
    @Test
    public void skal_opprette_aksjonpunkt_for_søker_med_utenlandsadresse_og_deretter_avklare_som_ikke_bosatt() throws Exception  {
        // Arrange steg 1
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_UTLAND_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        ((InntektConsumerProducerMock) inntektConsumerProducer).setReturnerInntekt(true);

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();
        Behandling behandling1 = behandlingRepository.hentBehandling(behandlingId);
        VurdereYtelseSammeBarnSøkerAksjonspunktDto dto1 = new VurdereYtelseSammeBarnSøkerAksjonspunktDto("bare tull", true);
        bekreftAksjonspunkt(behandling1, dto1);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT));
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_OM_ER_BOSATT, OPPRETTET),
            resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange steg 2
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftBosattVurderingDto dto = new BekreftBosattVurderingDto("Bosatt i USA", false);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1025));
        assertUtil.assertAksjonspunkter(asList(resultat(AVKLAR_OM_ER_BOSATT, UTFØRT),
            resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            resultat(FORESLÅ_VEDTAK, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    /*
     * Tester aksjonspunkt AVKLAR_OPPHOLDSRETT (5023) for søker som er EØS-borger
     */
    @Test
    public void skal_opprette_aksjonspunkt_for_eøs_borger_og_deretter_avklare_som_ingen_oppholdsrett() throws Exception  {
        // Arrange steg 1
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_OPPHOLDSRETT, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT)));

        // Arrange steg 2
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftOppholdsrettVurderingDto dto = new BekreftOppholdsrettVurderingDto("Bosatt i Polen", false, null, true);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1024));
        assertUtil.assertAksjonspunkter(asList(resultat(AVKLAR_OPPHOLDSRETT, UTFØRT), resultat(FORESLÅ_VEDTAK, OPPRETTET)
            ,resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    /*
     * Tester aksjonspunkt AVKLAR_OPPHOLDSRETT (5023) for søker som avklares fra EØS-borger -> ikke-EØS-borger
     */
    @Test
    public void skal_opprette_aksjonspunkt_for_eøs_borger_og_deretter_avklare_som_ikke_eøs_borger_uten_oppholdsrett() throws Exception  {
        // Arrange steg 1
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_OPPHOLDSRETT, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT)));

        // Arrange steg 2
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        // Overskriver EØS-borger til å være ikke-EØS-borger
        BekreftOppholdsrettVurderingDto dto = new BekreftOppholdsrettVurderingDto("Bosatt i Polen", null,
            false, false);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1023));
        assertUtil.assertAksjonspunkter(asList(resultat(AVKLAR_OPPHOLDSRETT, UTFØRT), resultat(FORESLÅ_VEDTAK, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    /*
     * Tester at aksjonspunkt AVKLAR_OPPHOLDSRETT (5023) ikke opprettes ved inntekt >= 3 mnd
     */
    @Test
    public void skal_innvilge_søknad_for_eøs_borger_med_inntekt_over_3_mnd() {
        // Arrange steg 1
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        ((InntektConsumerProducerMock) inntektConsumerProducer).setReturnerInntekt(true);

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.OPPFYLT)));
    }

    @Test
    public void skal_opprette_aksjonspunkt_for_søker_med_uavklart_periode_og_deretter_avklare_periode_som_unntak() throws Exception {
        // Arrange steg 1
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_UAVKL_PERIODE_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(TpsRepo.KVINNE_MEDL_UAVKL_PERIODE_AKTØRID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        ((InntektConsumerProducerMock) inntektConsumerProducer).setReturnerInntekt(true);

        // Act
        utførProsessSteg(behandlingId);
        kjørProsessTasks();
        Behandling behandling1 = behandlingRepository.hentBehandling(behandlingId);
        VurdereYtelseSammeBarnSøkerAksjonspunktDto dto1 = new VurdereYtelseSammeBarnSøkerAksjonspunktDto("bare tull", true);
        bekreftAksjonspunkt(behandling1, dto1);

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, OPPRETTET),
            resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT));

        // Arrange steg 2
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftErMedlemVurderingDto dto = new BekreftErMedlemVurderingDto("Unntak", MedlemskapManuellVurderingType.UNNTAK);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.AVSLÅTT,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_OPPFYLT, VilkårUtfallMerknad.VM_1020));
        assertUtil.assertAksjonspunkter(asList(resultat(AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE, UTFØRT),
            resultat(FORESLÅ_VEDTAK, OPPRETTET),
            resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }

    private Soeknad søknadEngangstønadFødsel(AktørId søkerAktørid, SøknadTestdataBuilder.EngangsstønadBuilder engangsstønadBuilder) {
        // Defaultverdier, har ingen betydning for Medlemskapsvilkåret
        int antallBarnFraSøknad = 1;
        LocalDate fødselsdato = LocalDate.now();
        LocalDate mottattDato = fødselsdato;

        return new SøknadTestdataBuilder()
            .engangsstønad(engangsstønadBuilder)
            .medMottattdato(mottattDato)
            .medSøker(ForeldreType.MOR, søkerAktørid)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(antallBarnFraSøknad))
            .build();
    }

    private Fagsak byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn) {
        NavBruker navBruker = new NavBrukerBuilder()
            .medAktørId(aktørId)
            .medKjønn(kjønn)
            .build();
        Fagsak fagsak = FagsakBuilder.nyEngangstønad(rolle)
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("123"))
            .build();
        fagsakRepository.opprettNy(fagsak);

        return fagsak;
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);
        repository.flush();
    }

    private List<ProsessTaskEntitet> hentProsesstasker(String tasktype) {
        return repository.hentAlle(ProsessTaskEntitet.class).stream()
            .filter(p -> p.getTaskName().equals(tasktype))
            .collect(Collectors.toList());
    }


    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), asList(dto)));
        kjørProsessTasks();
    }

    private void kjørProsessTasks() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }
}
