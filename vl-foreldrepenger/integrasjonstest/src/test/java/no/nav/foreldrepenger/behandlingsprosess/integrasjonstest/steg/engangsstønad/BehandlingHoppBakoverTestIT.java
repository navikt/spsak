package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.SØKERS_RELASJON_TIL_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType.VURDER_SØKNADSFRISTVILKÅR;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_MANUELT_SATT_PÅ_VENT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_ADOPSJONSDOKUMENTAJON;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FAKTA_FOR_PERSONSTATUS;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OPPHOLDSRETT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_TERMINBEKREFTELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FATTER_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.AVBRUTT;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.MEDLEMSKAPSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.OMSORGSVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKERSOPPLYSNINGSPLIKT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.SØKNADSFRISTVILKÅRET;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad.VM_5007;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.hentFødselsdatoFraFnr;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlagEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørInntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.KjørProsessTasks;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.BehandlingRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftOppholdVurderingDto.BekreftOppholdsrettVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.ForeslaVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SjekkManglendeFodselDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.SoknadsfristAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.GjenopptaBehandlingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.SettBehandlingPaVentDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling.BehandlingDtoTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklartDataBarnDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftDokumentertDatoAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.BekreftTerminbekreftelseAksjonspunktDto;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.konfig.Tid;

@RunWith(CdiRunner.class)
public class BehandlingHoppBakoverTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private BehandlingskontrollKontekst kontekst;

    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private RegisterdataEndringshåndterer registerdataEndringshåndterer;
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
    private PersonopplysningTjeneste personopplysningTjeneste;
    @Inject
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste;
    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;

    private BehandlingRestTjeneste behandlingRestTjeneste;

    @Inject
    private DokumentmottakTestUtil hjelper;

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    @Before
    public void setup() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste,
            behandlingRepository,
            behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);

        behandlingRestTjeneste = new BehandlingRestTjeneste(repositoryProvider,
            behandlingutredningTjeneste,
            behandlingsprosessTjeneste,
            fagsakTjeneste,
            henleggBehandlingTjeneste,
            behandlingDtoTjeneste,
            relatertBehandlingTjeneste);
    }

    @Test
    public void fødselssvilkår_med_manuelt_søknadsfristvilkår_og_revurdering_av_søknadsfristvilkår() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn er registrert i TPS
        LocalDate fødselsdato = hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate mottattDato = fødselsdato.plusMonths(6).plusMonths(1); // > 6 mnd oppfyller ikke søknadsfristvilkåret
        int antallBarn = 1;

        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR,
            TpsRepo.STD_KVINNE_AKTØR_ID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT, VilkårUtfallMerknad.VM_5007)));

        // Arrange trinn 2: Bekrefte søknadsfrist manuelt -> blir sendt til Foreslå vedtak
        boolean vilkårOk = true;
        Behandling behandling = hentOppdatertBehandling(behandlingId);
        SoknadsfristAksjonspunktDto søknadsfristDto = byggSoknadsfristAksjonspunktDto(vilkårOk);

        // Act
        bekreftAksjonspunkt(behandling, søknadsfristDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));

        // Arrange trinn 3: Foreslå vedtak -> blir sendt til beslutter
        ForeslaVedtakAksjonspunktDto foreslåDto = byggForeslåVedtakDto();

        // Act
        bekreftAksjonspunkt(behandling, foreslåDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, UTFØRT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange trinn 4: Beslutter sender tilbake til saksbehandler for avklaring av søknadsfristvilkår
        FatterVedtakAksjonspunktDto fatteDto = byggFatteVedtakDto(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET);

        // Act
        bekreftAksjonspunkt(behandling, fatteDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        Behandling behandling2 = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling2.getAktivtBehandlingSteg()).isEqualTo(VURDER_SØKNADSFRISTVILKÅR);
    }

    @Test
    public void fødselssvilkår_med_manuelt_søknadsfristvilkår_og_revurdering_av_fødselsvilkår() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn ikke er registrert i TPS
        LocalDate fødselsdato = LocalDate.now().minusDays(15); // For å unngå ApDef.VENT_PÅ_FØDSEL
        LocalDate mottattDato = fødselsdato.plusMonths(7); // > 6 mnd oppfyller ikke søknadsfristvilkåret
        int antallBarn = 1;

        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR,
            TpsRepo.STD_KVINNE_AKTØR_ID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange trinn 2: Bekrefte fødsel-> blir sendt til Bekreft søknadsfristvilkår
        Behandling b = hentOppdatertBehandling(behandlingId);
        SjekkManglendeFodselDto fødselDto = byggManglendeFodselDto(fødselsdato, antallBarn);

        // Act
        bekreftAksjonspunkt(b, fødselDto); // Første gang hopper tilbake for registerinnhenting

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT, VM_5007)));

        // Arrange trinn 3: Bekrefte søknadsfrist manuelt -> blir sendt til Foreslå vedtak
        boolean vilkårOk = true;
        SoknadsfristAksjonspunktDto søknadsfristDto = byggSoknadsfristAksjonspunktDto(vilkårOk);

        // Act
        bekreftAksjonspunkt(b, søknadsfristDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, OPPFYLT)));

        // Arrange trinn 4: Foreslå vedtak -> blir sendt til beslutter
        ForeslaVedtakAksjonspunktDto foreslåDto = byggForeslåVedtakDto();

        // Act
        bekreftAksjonspunkt(b, foreslåDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, UTFØRT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange trinn 5: Beslutter sender tilbake til saksbehandler for avklaring av fødselsdata
        FatterVedtakAksjonspunktDto fatteDto = byggFatteVedtakDto(SJEKK_MANGLENDE_FØDSEL);

        // Act
        bekreftAksjonspunkt(b, fatteDto);

        // Assert
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(SØKERS_RELASJON_TIL_BARN);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, AVBRUTT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));
        Behandling oppdatertBehandling = b;
        assertThat(oppdatertBehandling.getAktivtBehandlingSteg()).isEqualTo(SØKERS_RELASJON_TIL_BARN);

        // Arrange trinn 6: Re-bekrefte fødsel-> blir sendt til Bekreft søknadsfristvilkår
        SjekkManglendeFodselDto fødselDto2 = byggManglendeFodselDto(fødselsdato, antallBarn);

        // Act
        bekreftAksjonspunkt(b, fødselDto2);

        // Assert
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getAktivtBehandlingSteg()).isEqualTo(VURDER_SØKNADSFRISTVILKÅR);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(FATTER_VEDTAK, AVBRUTT),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT, VM_5007)));
    }

    private Behandling hentOppdatertBehandling(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId);
    }

    private void kjørProsessTasks() {
       new KjørProsessTasks(prosessTaskRepository).utførTasks();
    }

    @Test
    public void fødselssvilkår_med_manuell_gjenopptakelse_av_behandling() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn ikke er registrert i TPS
        LocalDate fødselsdato = LocalDate.now().minusDays(15); // > 14 dager for å unngå ApDef.VENT_PÅ_FØDSEL
        LocalDate mottattDato = fødselsdato.plusMonths(7); // > 6 mnd oppfyller ikke søknadsfristvilkåret
        int antallBarn = 1;

        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR,
            TpsRepo.STD_KVINNE_AKTØR_ID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge()));
        // Act
        utførProsessSteg(behandlingId);

        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FØDSELSVILKÅRET_MOR, IKKE_VURDERT),
                resultat(MEDLEMSKAPSVILKÅRET, IKKE_VURDERT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT)));

        // Arrange trinn 2: Bekrefte fødsel-> blir sendt til Bekreft søknadsfristvilkår
        Behandling behandling = hentOppdatertBehandling(behandlingId);
        SjekkManglendeFodselDto fødselDto = byggManglendeFodselDto(fødselsdato, antallBarn);

        // Act
        bekreftAksjonspunkt(behandling, fødselDto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(SØKERSOPPLYSNINGSPLIKT, OPPFYLT),
                resultat(FØDSELSVILKÅRET_MOR, OPPFYLT),
                resultat(MEDLEMSKAPSVILKÅRET, OPPFYLT),
                resultat(SØKNADSFRISTVILKÅRET, IKKE_VURDERT, VM_5007)));
        assertThat(repository.hentAlle(FamilieHendelseGrunnlagEntitet.class).stream()
            .filter(FamilieHendelseGrunnlagEntitet::getErAktivt)).hasSize(1);

        // Arrange trinn 3: Behandling settes på vent manuelt
        Behandling oppdatertBehandling = behandling;
        SettBehandlingPaVentDto påVentDto = new SettBehandlingPaVentDto();
        påVentDto.setBehandlingId(oppdatertBehandling.getId());
        påVentDto.setBehandlingVersjon(oppdatertBehandling.getVersjon());
        påVentDto.setVentearsak(Venteårsak.AVV_FODSEL);

        // Act
        behandlingRestTjeneste.settBehandlingPaVent(påVentDto);

        // Assert
        assertThat(hentAktivtBehandlingssteg(behandlingId))
            .isEqualTo(BehandlingStegType.VURDER_SØKNADSFRISTVILKÅR);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, UTFØRT),
            AksjonspunktTestutfall.resultat(MANUELL_VURDERING_AV_SØKNADSFRISTVILKÅRET, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_MANUELT_SATT_PÅ_VENT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange trinn 4: Behandling gjenopptas manuelt, og registerendringer er inntruffet
        simulerAtSøkerOpprinneligHaddeDødsdatoSatt(behandlingId); // Vil trigge re-innhenting
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(personopplysningTjeneste.hentPersonopplysninger(behandling).getSøker().getDødsdato()).isNotNull();

        oppdatertBehandling = behandling;
        GjenopptaBehandlingDto gjenopptaBehandlingDto = new GjenopptaBehandlingDto();
        gjenopptaBehandlingDto.setBehandlingId(oppdatertBehandling.getId());
        gjenopptaBehandlingDto.setBehandlingVersjon(oppdatertBehandling.getVersjon());

        // Act
        behandlingRestTjeneste.gjenopptaBehandling(gjenopptaBehandlingDto);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        FamilieHendelse gjeldendeVersjon = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(oppdatertBehandling).getGjeldendeVersjon();
        assertThat(gjeldendeVersjon).isNotNull();
        assertThat(gjeldendeVersjon.getBarna()).hasSize(1);

        assertThat(personopplysningTjeneste.hentPersonopplysninger(behandling).getSøker().getDødsdato()).isNull();
    }

    @Test
    public void skal_gjenopprette_aksjonspunkter_ved_oppdatering_av_registeropplysninger() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn er registrert i TPS
        int antallBarnFraSøknad = 1;
        LocalDate fødselsdato = LocalDate.now();
        LocalDate mottattDato = fødselsdato;

        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarnFraSøknad, ForeldreType.MOR, TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID,
                new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));
        // Act
        utførProsessSteg(behandlingId);

        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(VilkårType.FØDSELSVILKÅRET_MOR, VilkårUtfallType.OPPFYLT),
                resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.IKKE_VURDERT),
                resultat(VilkårType.SØKNADSFRISTVILKÅRET, VilkårUtfallType.IKKE_VURDERT)));

        // Arrange trinn 2: Bekreft oppholdsrett
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        LocalDateTime opprettetTidspunkt = hentTidspunktForOpprettelseAvAksjonspunkt(behandlingId, AVKLAR_OPPHOLDSRETT);
        BekreftOppholdsrettVurderingDto dto = new BekreftOppholdsrettVurderingDto("Bosatt i Norge", true,
            null, true);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            resultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT));
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange trinn 3: Simuler at behandling åpnes senere, og at registeroppysninger har blitt endret til å ikke
        // kreve aksjonspunkt for oppholdsrett
        behandling = behandlingRepository.hentBehandling(behandlingId);
        simulerAtSøkerOpprinneligHaddeDødsdatoSatt(behandlingId); // Vil trigge re-innhenting

        // Act
        registerdataEndringshåndterer.oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        // Verifiser at aksjonspunkt er blitt gjenopprettet
        LocalDateTime gjenopprettetTidspunkt = hentTidspunktForOpprettelseAvAksjonspunkt(behandlingId, AVKLAR_OPPHOLDSRETT);
        assertThat(gjenopprettetTidspunkt).isAfter(opprettetTidspunkt);
        // Verifiser at barn forblir uendret

        assertThat(personopplysningTjeneste.hentPersonopplysninger(behandling).getBarna()).hasSize(1);
    }

    private void bekreftAksjonspunkt(Behandling behandling, BekreftetAksjonspunktDto dto) throws URISyntaxException {
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandling.getId(), behandling.getVersjon(), singletonList(dto)));
        kjørProsessTasks();
    }

    @Test
    public void sett_avklaring_av_personstatus_på_vent_og_gjenoppta() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn ikke er registrert i TPS
        LocalDate fødselsdato = LocalDate.now().minusDays(15); // > 14 dager for å unngå ApDef.VENT_PÅ_FØDSEL
        LocalDate mottattDato = fødselsdato.plusMonths(7); // > 6 mnd oppfyller ikke søknadsfristvilkåret
        int antallBarn = 1;

        final Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        final Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR, TpsRepo.KVINNE_MEDL_FEIL_PERSONSTATUS_AKTØRID,
                new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, OPPRETTET));

        // Arrange trinn 2: Behandling settes på vent manuelt
        Behandling oppdatertBehandling = hentOppdatertBehandling(behandlingId);
        SettBehandlingPaVentDto påVentDto = new SettBehandlingPaVentDto();
        påVentDto.setBehandlingId(oppdatertBehandling.getId());
        påVentDto.setBehandlingVersjon(oppdatertBehandling.getVersjon());
        påVentDto.setVentearsak(Venteårsak.AVV_FODSEL);

        // Act
        behandlingRestTjeneste.settBehandlingPaVent(påVentDto);

        // Assert
        assertThat(hentAktivtBehandlingssteg(behandlingId))
            .isEqualTo(BehandlingStegType.KONTROLLER_FAKTA);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_MANUELT_SATT_PÅ_VENT, OPPRETTET)));

        // Arrange trinn 3: Behandling gjenopptas, registerendringer er inntruffet - skal fortsatt stoppe på KONTROLLER_FAKTA
        // Simuler at inntekt er endret (Inntektsmock returnerer ingen inntekt)
        simulerAtSøkerHaddeInntektsinformasjon(oppdatertBehandling, fødselsdato);

        oppdatertBehandling = hentOppdatertBehandling(behandlingId);
        GjenopptaBehandlingDto gjenopptaBehandlingDto = new GjenopptaBehandlingDto();
        gjenopptaBehandlingDto.setBehandlingId(oppdatertBehandling.getId());
        gjenopptaBehandlingDto.setBehandlingVersjon(oppdatertBehandling.getVersjon());

        // Act
        behandlingRestTjeneste.gjenopptaBehandling(gjenopptaBehandlingDto);

        kjørProsessTasks();

        // Assert
        assertThat(hentAktivtBehandlingssteg(behandlingId))
            .isEqualTo(BehandlingStegType.KONTROLLER_FAKTA);
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT),
            AksjonspunktTestutfall.resultat(AVKLAR_FAKTA_FOR_PERSONSTATUS, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_MANUELT_SATT_PÅ_VENT, UTFØRT)));
    }

    private void simulerAtSøkerHaddeInntektsinformasjon(Behandling behandling, LocalDate fødselsdato) {
        final InntektArbeidYtelseRepository repository = repositoryProvider.getInntektArbeidYtelseRepository();
        final InntektArbeidYtelseAggregatBuilder aggregatBuilder = repository.opprettBuilderFor(behandling, VersjonType.REGISTER);

        final InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder inntektBuilder = aggregatBuilder.getAktørInntektBuilder(behandling.getAktørId());
        final AktørInntektEntitet.InntektBuilder builder = inntektBuilder.getInntektBuilder(InntektsKilde.INNTEKT_OPPTJENING,
            Opptjeningsnøkkel.forOrgnummer("973093681"));
        final InntektEntitet.InntektspostBuilder postbuilder = builder.getInntektspostBuilder();
        postbuilder.medBeløp(BigDecimal.TEN).medPeriode(fødselsdato.minusDays(30), fødselsdato).medInntektspostType(InntektspostType.LØNN);
        builder.leggTilInntektspost(postbuilder);
        inntektBuilder.leggTilInntekt(builder);
        aggregatBuilder.leggTilAktørInntekt(inntektBuilder);
        repository.lagre(behandling, aggregatBuilder);
    }

    @Test
    public void skal_håndtere_endret_statsborgerskap_og_gjøre_tilbakehopp_ved_oppdatering_av_registeropplysninger() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn er registrert i TPS
        int antallBarnFraSøknad = 1;
        LocalDate fødselsdato = LocalDate.now();
        LocalDate mottattDato = fødselsdato;
        Fagsak fagsak = byggFagsak(TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak,
            søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarnFraSøknad, ForeldreType.MOR, TpsRepo.KVINNE_MEDL_EØSBORGER_BOSATT_NOR_AKTØRID,
                new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapTidligereOppholdUtlandOgFremtidigOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);

        kjørProsessTasks();

        // Arrange trinn 2: Bekreft oppholdsrett
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftOppholdsrettVurderingDto dto = new BekreftOppholdsrettVurderingDto("Bosatt i Norge", true,
            null, true);

        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBeregningResultat().getBeregninger()).isNotEmpty();

        // Arrange trinn 3: Simuler at søker opprinnelig var svensk
        simulerEndringSomVilGiRestart(behandlingId); // Vil trigge re-innhenting

        // Act
        registerdataEndringshåndterer.oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_OPPHOLDSRETT, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        behandling = behandlingRepository.hentBehandling(behandlingId);
        assertThat(behandling.getBehandlingsresultat().getBeregningResultat().getBeregninger()).isEmpty();
        assertThat(behandlingRepository.hentSistOppdatertTidspunkt(behandling).get()).isEqualToIgnoringHours(LocalDateTime.now());
        assertUtil.assertHistorikkinnslag(asList(HistorikkinnslagType.BEH_STARTET,
            HistorikkinnslagType.FAKTA_ENDRET, HistorikkinnslagType.NYE_REGOPPLYSNINGER));
        assertThat(personopplysningTjeneste.hentPersonopplysninger(behandling).getStatsborgerskapFor(behandling.getAktørId()).stream().map(Statsborgerskap::getStatsborgerskap).map(Kodeliste::getKode))
            .contains("POL");

    }

    private LocalDateTime hentOppdatertBehandlingstidspunkt(Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return behandlingRepository.hentSistOppdatertTidspunkt(behandling).get();
    }

    private LocalDateTime hentTidspunktForOpprettelseAvAksjonspunkt(Long behandlingId, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon)
            .map(BaseEntitet::getOpprettetTidspunkt)
            .orElseThrow(() -> new IllegalStateException("Skal ikke kunne havne her"));
    }

    @Test
    public void fødselssvilkår_hvor_endring_i_fødselsdato_trigger_ny_registerinnhenting() throws Exception {
        // Arrange trinn 1: Behandle søknad om fødsel hvor barn ikke er registrert i TPS
        LocalDate fødselsdato = LocalDate.now().minusDays(15); // > 14 dager for å unngå ApDef.VENT_PÅ_FØDSEL
        LocalDate mottattDato = fødselsdato.plusMonths(7); // > 6 mnd oppfyller ikke søknadsfristvilkåret
        int antallBarn = 1;

        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdato, mottattDato, antallBarn, ForeldreType.MOR,
            TpsRepo.STD_KVINNE_AKTØR_ID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange trinn 2: Bekrefte fødsel, men 2 år tilbake i tid -> Trigger ny no.nav.foreldrepenger.domene.registerinnhenting
        LocalDate oppdatertFødselsdato = fødselsdato.minusYears(2);
        final Behandling oppdatertBehandling = hentOppdatertBehandling(behandlingId);
        SjekkManglendeFodselDto fødselDto = byggManglendeFodselDto(oppdatertFødselsdato, antallBarn);
        LocalDateTime opprettetTidspunkt = hentOppdatertBehandlingstidspunkt(behandlingId);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, fødselDto);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        Aksjonspunkt apOppdatert = finnAksjonspunkt(behandlingId, SJEKK_MANGLENDE_FØDSEL);
        assertThat(apOppdatert.isToTrinnsBehandling()).isTrue();
        assertThat(apOppdatert.getBegrunnelse()).isEqualTo(fødselDto.getBegrunnelse());
        // Verifiser at saksbehandlers avklaringer er bevart
        final FamilieHendelseGrunnlag fødsel = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(oppdatertBehandling);
        assertThat(fødsel).isNotNull();
        assertThat(fødsel.getGjeldendeVersjon().getBarna().stream().map(UidentifisertBarn::getFødselsdato).collect(Collectors.toList()))
            .containsExactly(oppdatertFødselsdato);
        // Verifiser at behandlingsgrunnlag er blitt oppdatert
        LocalDateTime gjenopprettetTidspunkt = hentOppdatertBehandlingstidspunkt(behandlingId);
        assertThat(gjenopprettetTidspunkt).isAfter(opprettetTidspunkt);
    }

    @Test
    public void adopsjonsvilkår_hvor_endring_i_omgsorgsovertakelsedato_trigger_ny_registerinnhenting() throws Exception {
        // Arrange trinn 1: Adopsjon happy case
        LocalDate fødselsdato = LocalDate.now().minusDays(10000);
        LocalDate mottattDato = LocalDate.now().plusDays(7L);
        LocalDate omsorgsovertakelseDato = LocalDate.now();

        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadAdopsjon(fødselsdato, mottattDato, omsorgsovertakelseDato, ForeldreType.MOR,
            TpsRepo.STD_KVINNE_AKTØR_ID, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange trinn 2: Bekrefte adopsjon, men 2 år tilbake i tid -> Trigger nyTerminbekreftelse no.nav.foreldrepenger.domene.registerinnhenting
        LocalDate oppdatertOmsorgsovertakelseDato = omsorgsovertakelseDato.minusYears(2);
        BekreftDokumentertDatoAksjonspunktDto adopsjonDto = byggBekreftDokumentertDatoAksjonspunktDto(oppdatertOmsorgsovertakelseDato,
            fødselsdato);
        LocalDateTime opprettetTidspunkt = hentOppdatertBehandlingstidspunkt(behandlingId);
        Behandling b = behandlingRepository.hentBehandling(behandlingId);

        // Act
        bekreftAksjonspunkt(b, adopsjonDto);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_ADOPSJONSDOKUMENTAJON, OPPRETTET),
            AksjonspunktTestutfall.resultat(AVKLAR_OM_ADOPSJON_GJELDER_EKTEFELLES_BARN, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
        Aksjonspunkt apOppdatert = finnAksjonspunkt(behandlingId, AVKLAR_ADOPSJONSDOKUMENTAJON);
        assertThat(apOppdatert.isToTrinnsBehandling()).isTrue();
        assertThat(apOppdatert.getBegrunnelse()).isEqualTo(adopsjonDto.getBegrunnelse());
        final FamilieHendelseGrunnlag fødsel = repositoryProvider.getFamilieGrunnlagRepository()
            .hentAggregat(behandlingRepository.hentBehandling(behandlingId));
        assertThat(fødsel).isNotNull();
        assertThat(fødsel.getGjeldendeVersjon().getAdopsjon()).isPresent();
        assertThat(fødsel.getGjeldendeVersjon().getAdopsjon().get().getOmsorgsovertakelseDato()).isEqualTo(oppdatertOmsorgsovertakelseDato);
        // Verifiser at saksbehandlers avklaringer er bevart
        LocalDateTime gjenopprettetTidspunkt = hentOppdatertBehandlingstidspunkt(behandlingId);
        assertThat(gjenopprettetTidspunkt).isAfter(opprettetTidspunkt);
    }

    @Test
    public void omsorgsvilkår_hvor_endring_i_omsorgsovertakelsesdato_trigger_ny_registerinnhenting() throws Exception {
        // Arrange trinn 1: Omsorgsvilkår happy case
        LocalDate fødselsdato = LocalDate.now().minusDays(10000);
        LocalDate mottattDato = LocalDate.now().plusDays(7L);
        LocalDate omsorgsovertakelseDato = LocalDate.now();
        final AktørId aktørId = TpsRepo.STD_MANN_AKTØR_ID;

        Fagsak fagsak = byggFagsak(aktørId, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadOmsorgsovertakelse(fødselsdato, mottattDato, omsorgsovertakelseDato,
            FarSøkerType.OVERTATT_OMSORG, ForeldreType.MOR, aktørId, new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge()));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange trinn 2: Bekrefte omsorgsovertakelse, men 2 år tilbake i tid -> Trigger nyTerminbekreftelse no.nav.foreldrepenger.domene.registerinnhenting
        Behandling oppdatertBehandling = behandlingRepository.hentBehandling(behandlingId);
        LocalDate oppdatertOmsorgsovertakelseDato = omsorgsovertakelseDato.minusYears(2);
        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto omsorgDto = byggOmsorgovertakelseAksjonspunktDto(fødselsdato, oppdatertOmsorgsovertakelseDato);
        LocalDateTime opprettetTidspunkt = hentOppdatertBehandlingstidspunkt(behandlingId);

        // Act
        bekreftAksjonspunkt(oppdatertBehandling, omsorgDto);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        kjørProsessTasks();

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
        Aksjonspunkt apOppdatert = finnAksjonspunkt(behandlingId, AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE);
        assertThat(apOppdatert.isToTrinnsBehandling()).isTrue();
        assertThat(apOppdatert.getBegrunnelse()).isEqualTo(omsorgDto.getBegrunnelse());
        final FamilieHendelseGrunnlag fødsel = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(oppdatertBehandling);
        assertThat(fødsel).isNotNull();
        assertThat(fødsel.getGjeldendeVersjon().getAdopsjon()).isPresent();
        assertThat(fødsel.getGjeldendeVersjon().getAdopsjon().get().getOmsorgsovertakelseDato()).isEqualTo(oppdatertOmsorgsovertakelseDato);
        assertThat(repository.hentAlle(Behandlingsresultat.class)).hasSize(1);
        // Verifiser at behandlingsgrunnlag er blitt oppdatert
        LocalDateTime gjenopprettetTidspunkt = hentOppdatertBehandlingstidspunkt(behandlingId);
        assertThat(gjenopprettetTidspunkt).isAfter(opprettetTidspunkt);
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
    }

    @Test
    public void søknad_med_termindato_endres_til_fødsel_dersom_termindato_mer_enn_25_dager_tilbake_i_tid() throws Exception {
        LocalDate termindatoFraSøknad = LocalDate.now().plusMonths(1);
        LocalDate utstedtDatoFraSøknad = LocalDate.now().minusDays(30);
        Fagsak fagsak = byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE);
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadTermin(LocalDate.now(), termindatoFraSøknad, utstedtDatoFraSøknad, 1));

        utførProsessSteg(behandlingId);
        new KjørProsessTasks(prosessTaskRepository).utførTasks();

        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        // Arrange: Endrer termindato fremover i tid og bekrefter aksjonspunktet
        BekreftTerminbekreftelseAksjonspunktDto dto = new BekreftTerminbekreftelseAksjonspunktDto(
            "test", termindatoFraSøknad.plusDays(1), utstedtDatoFraSøknad, 1);

        // Act
        bekreftAksjonspunkt(behandling, dto);

        // Assert
        assertUtil.assertAksjonspunkter(asList(
            AksjonspunktTestutfall.resultat(AVKLAR_TERMINBEKREFTELSE, UTFØRT),
            AksjonspunktTestutfall.resultat(FORESLÅ_VEDTAK, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Arrange: Endrer termindato 26 dager bak i tid, skal trigge sjekk fødsel aksjonspunkt
        // og aksjonspunkt fra terminbekreftelse skal ha blitt slettet
        behandling = behandlingRepository.hentBehandling(behandlingId);
        BekreftTerminbekreftelseAksjonspunktDto dto2 = new BekreftTerminbekreftelseAksjonspunktDto(
            "test", LocalDate.now().minusDays(26), utstedtDatoFraSøknad, 1);

        // Act
        bekreftAksjonspunkt(behandling, dto2);

        // Assert
        assertUtil.assertAksjonspunkter(
            AksjonspunktTestutfall.resultat(SJEKK_MANGLENDE_FØDSEL, OPPRETTET),
            AksjonspunktTestutfall.resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));
    }

    private BehandlingStegType hentAktivtBehandlingssteg(Long behandlingId) {
        return behandlingRepository.hentBehandling(behandlingId).getAktivtBehandlingSteg();
    }

    private void simulerAtSøkerOpprinneligHaddeDødsdatoSatt(Long behandlingId) {
        Behandling oppdatertBehandling = hentOppdatertBehandling(behandlingId);

        PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        final PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForRegisterdata(oppdatertBehandling);
        final PersonInformasjonBuilder.PersonopplysningBuilder personopplysningBuilder = builder.getPersonopplysningBuilder(oppdatertBehandling.getAktørId());
        personopplysningBuilder.medDødsdato(LocalDate.now());
        builder.leggTil(personopplysningBuilder);

        personopplysningRepository.lagre(oppdatertBehandling, builder);

        behandlingRepository.oppdaterSistOppdatertTidspunkt(oppdatertBehandling, LocalDateTime.now().minusDays(1)); // For å trigge

        // re-innhenting
        lagreOppdatertBehandlingsgrunnlag(oppdatertBehandling);
    }

    private void simulerEndringSomVilGiRestart(Long behandlingId) {
        Behandling oppdatertBehandling = hentOppdatertBehandling(behandlingId);

        final TpsRepo init = TpsRepo.init();
        final Person person = init.finnPerson(init.finnIdent(oppdatertBehandling.getAktørId()));
        PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
        final PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForRegisterdata(oppdatertBehandling);
        final DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedTilOgMed(DateUtil.convertToLocalDate(person.getFoedselsdato().getFoedselsdato()),
            person.getDoedsdato() != null && person.getDoedsdato().getDoedsdato() != null ? DateUtil.convertToLocalDate(person.getDoedsdato().getDoedsdato()) : Tid.TIDENES_ENDE);
        builder.leggTil(builder.getPersonstatusBuilder(oppdatertBehandling.getAktørId(), periode).medPersonstatus(PersonstatusType.UTVA));

        personopplysningRepository.lagre(oppdatertBehandling, builder);

        // For å trigge re-innhenting
        behandlingRepository.oppdaterSistOppdatertTidspunkt(oppdatertBehandling, LocalDateTime.now().minusDays(1));

        lagreOppdatertBehandlingsgrunnlag(oppdatertBehandling);
    }

    private void lagreOppdatertBehandlingsgrunnlag(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);

    }

    private SoknadsfristAksjonspunktDto byggSoknadsfristAksjonspunktDto(boolean vilkårOk) {
        return new SoknadsfristAksjonspunktDto("Grunn", vilkårOk);
    }

    private AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto byggOmsorgovertakelseAksjonspunktDto(LocalDate fødselsdato, LocalDate omsorgsovertakelseDato) {

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(omsorgsovertakelseDato);
        dto.setVilkårType(OMSORGSVILKÅRET);
        dto.setForeldre(emptyList());

        AvklartDataBarnDto barn = new AvklartDataBarnDto();
        barn.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);
        barn.setFodselsdato(fødselsdato);
        dto.setBarn(singletonList(barn));
        return dto;
    }

    private FatterVedtakAksjonspunktDto byggFatteVedtakDto(AksjonspunktDefinisjon apDef) {

        AksjonspunktGodkjenningDto apGodkjenningDto = new AksjonspunktGodkjenningDto();
        apGodkjenningDto.setArsaker(Stream.of(VurderÅrsak.FEIL_FAKTA)
            .map(VurderÅrsak::new)
            .collect(Collectors.toSet()));
        apGodkjenningDto.setGodkjent(false);
        apGodkjenningDto.setBegrunnelse("Må ha bedre dokumentasjon.");
        apGodkjenningDto.setAksjonspunktKode(apDef);

        return new FatterVedtakAksjonspunktDto("", singletonList(apGodkjenningDto));
    }

    private BekreftDokumentertDatoAksjonspunktDto byggBekreftDokumentertDatoAksjonspunktDto(LocalDate omsorgsovertakelseDato,
                                                                                            LocalDate fødselsdato) {
        Map<Integer, LocalDate> fødselsdatoer = new HashMap<>();
        fødselsdatoer.put(1, fødselsdato);
        return new BekreftDokumentertDatoAksjonspunktDto("Grunn", omsorgsovertakelseDato, fødselsdatoer);
    }

    private SjekkManglendeFodselDto byggManglendeFodselDto(LocalDate fødselsdato, int antallBarn) {
        return new SjekkManglendeFodselDto(
            "begrunnelse",
            true,
            false,
            fødselsdato,
            antallBarn);
    }

    private ForeslaVedtakAksjonspunktDto byggForeslåVedtakDto() {
        return new ForeslaVedtakAksjonspunktDto("begrunnelse", null, null, false);

    }

    private Aksjonspunkt finnAksjonspunkt(Long behandlingId, AksjonspunktDefinisjon apDef) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return behandling.getAksjonspunkter().stream()
            .filter(a -> a.getAksjonspunktDefinisjon().getKode().equals(apDef.getKode()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Fant ikke angitt aksjonspunkt"));
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flushAndClear();
    }

    private Soeknad søknadEngangstønadTermin(LocalDate søknadsdato, LocalDate termindatoFraSøknad, LocalDate utstedtDatoFraSøknad, int antallBarnFraSøknad) {
        return new SøknadTestdataBuilder().søknadEngangsstønadMor()
            .medMottattdato(søknadsdato)
            .medTermin(new SøknadTestdataBuilder.TerminBuilder()
                .medTermindato(termindatoFraSøknad)
                .medUtsteddato(utstedtDatoFraSøknad)
                .medAntallBarn(antallBarnFraSøknad))
            .build();
    }

    private Soeknad søknadEngangstønadFødsel(LocalDate fødselsdato, LocalDate mottattDato, int antallBarnFraSøknad, ForeldreType foreldreType, AktørId aktørId,
                                             SøknadTestdataBuilder.EngangsstønadBuilder engangsstønadBuilder) {
        return new SøknadTestdataBuilder()
            .engangsstønad(engangsstønadBuilder)
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

    private Soeknad søknadEngangstønadAdopsjon(LocalDate fødselsdato, LocalDate mottattDato, LocalDate adopsjonsdato, ForeldreType foreldreType, AktørId aktørId,
                                               SøknadTestdataBuilder.EngangsstønadBuilder engangsstønadBuilder) {
        return new SøknadTestdataBuilder()
            .engangsstønad(engangsstønadBuilder)
            .medMottattdato(mottattDato)
            .medSøker(foreldreType, aktørId)
            .medAdopsjon(new SøknadTestdataBuilder.AdopsjonBuilder()
                .medAdopsjonsdato(adopsjonsdato)
                .medAntallBarn(1)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato)))
            .build();
    }

    private Soeknad søknadEngangstønadOmsorgsovertakelse(LocalDate fødselsdato, LocalDate mottattDato, LocalDate omsorgsovertakelseDato,
                                                         FarSøkerType grunnlagForOvertagelse, ForeldreType foreldreType, AktørId aktørId,
                                                         SøknadTestdataBuilder.EngangsstønadBuilder engangsstønadBuilder) {
        return new SøknadTestdataBuilder()
            .engangsstønad(engangsstønadBuilder)
            .medMottattdato(mottattDato)
            .medSøker(foreldreType, aktørId)
            .medOmsorgsovertakelse(new SøknadTestdataBuilder.OmsorgsovertakelseBuilder()
                .medFoedselsdatoer(Collections.singletonList(fødselsdato))
                .medOmsorgsovertakelseaarsaker(grunnlagForOvertagelse)
                .medOmsorgsovertakelseDato(omsorgsovertakelseDato)
                .medAntallBarn(1))
            .build();
    }
}
