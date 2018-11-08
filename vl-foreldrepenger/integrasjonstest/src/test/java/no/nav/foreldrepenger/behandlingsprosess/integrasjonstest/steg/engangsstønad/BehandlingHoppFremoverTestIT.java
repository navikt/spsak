package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ER_BOSATT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarSaksopplysningerDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.HenleggBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingHoppFremoverTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    @Inject
    private FagsakRepository fagsakRepository;

    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingutredningTjeneste;

    @Inject
    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessTjeneste;

    @Inject
    private FagsakTjeneste fagsakTjeneste;

    @Inject
    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    @Inject
    private BehandlingDtoTjeneste behandlingDtoTjeneste;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Inject
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;

    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private BehandlingRestTjeneste behandlingRestTjeneste;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    @Inject
    private DokumentmottakTestUtil hjelper;
    private BehandlingskontrollKontekst kontekst;

    @Before
    public void setup() {

        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, behandlingRepository, behandlingutredningTjeneste, totrinnTjeneste);
        behandlingRestTjeneste = new BehandlingRestTjeneste(repositoryProvider,
            behandlingutredningTjeneste,
            behandlingsprosessTjeneste,
            fagsakTjeneste,
            henleggBehandlingTjeneste,
            behandlingDtoTjeneste,
            relatertBehandlingTjeneste);
    }

    @Test
    public void skal_henlegge_behandling_gjennom_behandlingsmeny() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn ikke er registrert i TPS
        LocalDate fødselsdato = LocalDate.now().minusDays(15); // For å unngå ApDef.VENT_PÅ_FØDSEL
        LocalDate mottattDato = LocalDate.now();
        int antallBarn = 1;

        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR, TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        assertThat(hentBehandling(behandlingId).getAktivtBehandlingSteg())
            .isEqualTo(BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        // Arrange trinn 2: Henlegg behandling fra fødselssteget
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        simulerAtMedlemskapErAvklart(behandlingId); // Medlemsskapssteget kommer etter fødselssteget

        assertThat(repository.hentAlle(VurdertMedlemskapEntitet.class)).hasSize(1);
        HenleggBehandlingDto dto = byggHenleggBehandlingDto(oppdatertBehandling);

        // Act
        behandlingRestTjeneste.henleggBehandling(dto);

        // Assert
        Optional<MedlemskapAggregat> medlemskap = repositoryProvider.getMedlemskapRepository().hentMedlemskap(behandlingId);
        assertThat(medlemskap).isPresent();

        assertThat(medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap)).isNotPresent();

        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, AVBRUTT),
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, AVBRUTT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    @Test
    public void skal_henlegge_behandling_gjennom_aksjonspunkt_for_ugyldig_personstatus() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor personstatus må avklares, og barn ikke er registrert i TPS
        LocalDate fødselsdato = LocalDate.now().minusDays(15); // For å unngå ApDef.VENT_PÅ_FØDSEL
        LocalDate mottattDato = LocalDate.now();
        int antallBarn = 1;

        final AktørId kvinneMedlFeilPersonstatusAktørid = TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID;
        Fagsak fagsak = byggFagsak(kvinneMedlFeilPersonstatusAktørid, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR, kvinneMedlFeilPersonstatusAktørid));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange trinn 2: Bekrefte personstatus
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        boolean fortsettBehandling = true;
        AvklarSaksopplysningerDto avklaringDto = byggAvklarSaksopplysningerDto(fortsettBehandling);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, avklaringDto);

        // Assert
        assertThat(hentBehandling(behandlingId).getAktivtBehandlingSteg())
            .isEqualTo(BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ER_BOSATT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_LOVLIG_OPPHOLD, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange trinn 3: Bekrefte fødsel
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        SjekkManglendeFodselDto fødselDto = byggFødselDto(fødselsdato, antallBarn);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, fødselDto);

        // Assert
        assertThat(hentBehandling(behandlingId).getAktivtBehandlingSteg())
            .isEqualTo(BehandlingStegType.VURDER_MEDLEMSKAPVILKÅR);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, UTFØRT),
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ER_BOSATT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_LOVLIG_OPPHOLD, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange trinn 4: Henlegge pga personstatus (som ble satt ok i trinn 2)
        fortsettBehandling = false;
        oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        avklaringDto = byggAvklarSaksopplysningerDto(fortsettBehandling);

        bekreftAksjonspunkt(oppdatertBehandling, avklaringDto);

        // Assert
        assertThat(hentBehandling(behandlingId).getAktivtBehandlingSteg()).isNull();
        assertThat(hentBehandling(behandlingId).getStatus()).isEqualTo(BehandlingStatus.AVSLUTTET);
        assertThat(hentBehandling(behandlingId).getBehandlingsresultat().getBehandlingResultatType().erHenlagt()).isTrue();

        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, UTFØRT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
    }

    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto avklaringDto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), asList(avklaringDto)));
        kjørProsessTasks();
    }

    private void kjørProsessTasks() {
        new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

    private Behandling hentBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private HenleggBehandlingDto byggHenleggBehandlingDto(Behandling behandling) {
        HenleggBehandlingDto dto = new HenleggBehandlingDto();
        dto.setBehandlingId(behandling.getId());
        dto.setBehandlingVersjon(behandling.getVersjon());
        dto.setÅrsakKode("HENLAGT_SØKNAD_TRUKKET");
        dto.setBegrunnelse("Søknad trukket");
        return dto;
    }

    private AvklarSaksopplysningerDto byggAvklarSaksopplysningerDto(boolean fortsettBehandling) {
        return new AvklarSaksopplysningerDto("Bosatt i Norge", "BOSA", fortsettBehandling);
    }

    private SjekkManglendeFodselDto byggFødselDto(LocalDate fødselsdato, int antallBarn) {
        return new SjekkManglendeFodselDto(
            "begrunnelse",
            true,
            false,
            fødselsdato,
            antallBarn);
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flushAndClear();
    }

    private Soeknad søknadEngangstønadFødsel(LocalDate fødselsdato, LocalDate mottattDato, int antallBarnFraSøknad, ForeldreType foreldreType, AktørId aktørId) {
        return new SøknadTestdataBuilder()
            .engangsstønad(new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge())
            .medMottattdato(mottattDato)
            .medSøker(foreldreType, aktørId)
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

    private void simulerAtMedlemskapErAvklart(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        VurdertMedlemskap vurdertMedlemskap = new VurdertMedlemskapBuilder()
            .medOppholdsrettVurdering(true)
            .medLovligOppholdVurdering(true)
            .medBosattVurdering(true)
            .build();

        MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();

        // Act
        medlemskapRepository.lagreMedlemskapVurdering(behandling, vurdertMedlemskap);
    }

}
