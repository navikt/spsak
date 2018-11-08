package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.engangsstønad;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.FORESLÅ_VEDTAK;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FØDSELSVILKÅRET_MOR;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.TotrinnTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsRepo;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.DokumentmottakTestUtil;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.AksjonspunktRestTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurdereYtelseSammeBarnSøkerAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@RunWith(CdiRunner.class)
public class BehandlingMottattStønadTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();
    @Inject
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    @Inject
    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    @Inject
    private FagsakRepository fagsakRepository;
    @Inject
    private MottatteDokumentRepository mottatteDokumentRepository;
    @Inject
    private BehandlingRepository behandlingRepository;
    @Inject
    private BehandlingRepositoryProvider repositoryProvider;
    @Inject
    private TotrinnTjeneste totrinnTjeneste;

    @Inject
    private KodeverkRepository kodeverkRepository;
    @Inject
    private AksjonspunktApplikasjonTjeneste applikasjonstjeneste;
    @Inject
    private DokumentmottakTestUtil hjelper;
    private AksjonspunktRestTjeneste aksjonspunktRestTjeneste;
    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);
    private Fagsak fagsak;
    private BehandlingskontrollKontekst kontekst;

    @Before
    public void setup() {
        aksjonspunktRestTjeneste = new AksjonspunktRestTjeneste(applikasjonstjeneste, behandlingRepository,
                behandlingsutredningApplikasjonTjeneste, totrinnTjeneste);
    }

    @Test
    @Ignore // TODO PK-48761 - Henger sammen med sjekk om søker har mottatt stønad fra før
    public void søknad_hvor_søker_har_mottatt_stønad_før() throws Exception  {
        // Arrange
        simulereAtDetFinnesEnAvsluttetBehandling();
        AktørId farAktørId = TpsRepo.STD_MANN_AKTØR_ID;
        String avslagskode = "1002";

        LocalDate fødselsdato = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate søknadsdato = fødselsdato;
        int antallBarnFraSøknad = 1;

        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdato, søknadsdato, antallBarnFraSøknad, fagsak.getAktørId(), farAktørId));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, OPPRETTET));

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        VurdereYtelseSammeBarnSøkerAksjonspunktDto dto = new VurdereYtelseSammeBarnSøkerAksjonspunktDto("bare tull",
                false);
        dto.setAvslagskode(avslagskode);
        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), asList(dto)));

        assertUtil.assertAksjonspunkter(asList(
                resultat(AVKLAR_OM_SØKER_HAR_MOTTATT_STØTTE, UTFØRT),
                resultat(FORESLÅ_VEDTAK, OPPRETTET),
                resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, OPPRETTET))
        );

        List<VilkårResultat> vilkårResultater = repository.hentAlle(VilkårResultat.class);
        Optional<VilkårResultat> vilkårResultatOpt = vilkårResultater.stream()
                .filter(i -> i.getOriginalBehandling().equals(behandling))
                .findFirst();

        assertThat(vilkårResultatOpt).hasValueSatisfying(vilkårResultat -> {
            Optional<Vilkår> vilkarOpt = vilkårResultat.getVilkårene().stream().filter(v -> v.getVilkårType().equals(FØDSELSVILKÅRET_MOR)).findFirst();
            assertThat(vilkarOpt).hasValueSatisfying(vilkar ->
                assertThat(vilkar.getAvslagsårsak().getKode()).isEqualTo(avslagskode)
            );
        });
    }

    @Test
    @Ignore // TODO PK-48761 - Henger sammen med sjekk om søker har mottatt stønad fra før
    public void søknad_hvor_annen_forelder_har_mottatt_stønad_før() throws Exception  {
        // Arrange
        // avsluttet behandling på annen foreldre:
        simulereAtDetFinnesEnAvsluttetBehandlingPåAnnenForelder();
        String avslagskode = "1002";

        // nåværende behandling på søker:
        Saksnummer saksnummer  = new Saksnummer("456");
        AktørId farAktørId = TpsRepo.STD_MANN_AKTØR_ID;
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE, saksnummer);
        LocalDate fødselsdato = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate søknadsdato = fødselsdato;
        int antallBarnFraSøknad = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdato, søknadsdato, antallBarnFraSøknad, fagsak.getAktørId(), farAktørId));

        // Act
        utførProsessSteg(behandlingId);
        behandlingskontrollTjeneste.settAutopunktTilUtført(AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, kontekst);
        utførProsessSteg(behandlingId);

        // Assert
        assertUtil.assertAksjonspunkter(resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, OPPRETTET),
            resultat(AVKLAR_OM_ANNEN_FORELDRE_HAR_MOTTATT_STØTTE, OPPRETTET));

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto dto = new VurdereYtelseSammeBarnAnnenForelderAksjonspunktDto("bare tull", false);
        dto.setAvslagskode(avslagskode);

        aksjonspunktRestTjeneste.bekreft(BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), asList(dto)));

        List<VilkårResultat> vilkårResultater = repository.hentAlle(VilkårResultat.class);
        Optional<VilkårResultat> vilkårResultatOpt = vilkårResultater.stream()
                .filter(i -> i.getOriginalBehandling().equals(behandling))
                .findFirst();

        assertThat(vilkårResultatOpt).hasValueSatisfying(vilkårResultat -> {
            Optional<Vilkår> vilkarOpt = vilkårResultat.getVilkårene().stream().filter(v -> v.getVilkårType().equals(FØDSELSVILKÅRET_MOR)).findFirst();
            assertThat(vilkarOpt).hasValueSatisfying(vilkar ->
                assertThat(vilkar.getAvslagsårsak().getKode()).isEqualTo(avslagskode)
            );
        });
    }

    private void simulereAtDetFinnesEnAvsluttetBehandlingPåAnnenForelder() { // denne bygger også fagsak
        Saksnummer saksnummer = new Saksnummer("123");
        AktørId farAktørId = TpsRepo.STD_MANN_AKTØR_ID;
        byggFagsak(farAktørId, RelasjonsRolleType.FARA, NavBrukerKjønn.MANN, saksnummer);

        LocalDate fødselsdatoTidligere = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate søknadsdatoTidligere = fødselsdatoTidligere.minusDays(5L);
        int antallBarnFraSøknadTidligere = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdatoTidligere, søknadsdatoTidligere, antallBarnFraSøknadTidligere, fagsak.getAktørId(), farAktørId));

        Behandling ferdigBehandlet = behandlingRepository.hentBehandling(behandlingId);
        utførProsessSteg(ferdigBehandlet.getId());

        InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        YtelseBuilder ytelse = byggRelatertYtelse(new Saksnummer("22"), farAktørId);
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = inntektArbeidYtelseRepository.opprettBuilderFor(ferdigBehandlet, VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder ytelserBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(farAktørId);
        ytelserBuilder.leggTilYtelse(ytelse);
        inntektArbeidYtelseAggregatBuilder.leggTilAktørYtelse(ytelserBuilder);
        inntektArbeidYtelseRepository.lagre(ferdigBehandlet, inntektArbeidYtelseAggregatBuilder);

        ferdigBehandlet.avsluttBehandling();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(ferdigBehandlet.getId());
        behandlingRepository.lagre(ferdigBehandlet, kontekst.getSkriveLås());
    }

    private Soeknad søknadEngangstønadFødsel(LocalDate fødselsdato, LocalDate mottattDato, int antallBarnFraSøknad, AktørId søkerAktørid, AktørId farAktørId) {
        return new SøknadTestdataBuilder()
                .engangsstønad(new SøknadTestdataBuilder.EngangsstønadBuilder().medMedlemskapOppholdNorge().medAnnenForelderMedNorskIdent(farAktørId))
                .medMottattdato(mottattDato)
                .medSøker(ForeldreType.MOR, søkerAktørid)
                .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                        .medFoedselsdato(fødselsdato)
                        .medAntallBarn(antallBarnFraSøknad))
                .build();
    }

    private void simulereAtDetFinnesEnAvsluttetBehandling() {
        Saksnummer saksnummer  = new Saksnummer("123");
        AktørId farAktørId = TpsRepo.STD_MANN_AKTØR_ID;
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE, saksnummer);

        // tidligere behandling:
        LocalDate fødselsdatoTidligere = IntegrasjonstestUtils.hentFødselsdatoFraFnr(TpsRepo.STD_BARN_FNR);
        LocalDate søknadsdatoTidligere = fødselsdatoTidligere.minusDays(5L);
        int antallBarnFraSøknadTidligere = 1;
        Long behandlingId = hjelper.byggBehandling(fagsak, søknadEngangstønadFødsel(fødselsdatoTidligere, søknadsdatoTidligere, antallBarnFraSøknadTidligere, fagsak.getAktørId(), farAktørId));
        Behandling ferdigBehandlet = behandlingRepository.hentBehandling(behandlingId);
        utførProsessSteg(ferdigBehandlet.getId());

        YtelseBuilder ytelse = byggRelatertYtelse(new Saksnummer("22"), farAktørId);
        InntektArbeidYtelseRepository inntekt = repositoryProvider.getInntektArbeidYtelseRepository();
        InntektArbeidYtelseAggregatBuilder aggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder ytelserBuilder = aggregatBuilder.getAktørYtelseBuilder(farAktørId
        );
        ytelserBuilder.leggTilYtelse(ytelse);
        aggregatBuilder.leggTilAktørYtelse(ytelserBuilder);
        inntekt.lagre(ferdigBehandlet, aggregatBuilder);

        ferdigBehandlet.avsluttBehandling();
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(ferdigBehandlet.getId());
        behandlingRepository.lagre(ferdigBehandlet, kontekst.getSkriveLås());
    }

    private YtelseBuilder byggRelatertYtelse(Saksnummer sakId, AktørId aktørId) {
        InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder ytelserBuilder = inntektArbeidYtelseAggregatBuilder.getAktørYtelseBuilder(aktørId);

        return ytelserBuilder.getYtelselseBuilderForType(Fagsystem.FPSAK, RelatertYtelseType.FORELDREPENGER, sakId)
            .medKilde(Fagsystem.INFOTRYGD)
            .medYtelseType(RelatertYtelseType.FORELDREPENGER)
            .medStatus(RelatertYtelseTilstand.AVSLUTTET)
            .medPeriode(DatoIntervallEntitet.fraOgMed(LocalDate.now().minusDays(3L)));
    }

    private void utførProsessSteg(Long behandlingId) {
        kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        behandlingskontrollTjeneste.prosesserBehandling(kontekst);

        repository.flush();
    }

    @Test
    public void testMottattForeldrepengerSøknadOmFødsel_med_ny_søknads_xml() {
        LocalDate mottattDato = LocalDate.now();

        Saksnummer saksnummer  = new Saksnummer("123");
        byggFagsak(TpsRepo.STD_KVINNE_AKTØR_ID, RelasjonsRolleType.MORA, NavBrukerKjønn.KVINNE, saksnummer);

        Soeknad søknad = new SøknadTestdataBuilder().søknadForeldrepenger()
            .medSøker(ForeldreType.MOR, TpsRepo.STD_KVINNE_AKTØR_ID)
            .medAdopsjon(new SøknadTestdataBuilder.AdopsjonBuilder()
                .medAdopsjonsdato(mottattDato)
                .medFoedselsdatoer(Collections.singletonList(mottattDato)))
            .build();
        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.FØRSTEGANGSSØKNAD);
        behandlingskontrollTjeneste.opprettNyBehandling(fagsak, behandlingType, (be) -> {});

        boolean elektroniskSøknad = false; // Settes til false for å bypasse vilkår om søkers opplysningsplikt. Bør vurdere heller å oppgi komplett søknad
        MottattDokument dokument = DokumentmottakTestUtil.lagMottattDokument(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, fagsak.getId(), IntegrasjonstestUtils.lagSøknadXml(søknad), mottattDato, elektroniskSøknad, null);
        mottatteDokumentRepository.lagre(dokument);
    }

    private void byggFagsak(AktørId aktørId, RelasjonsRolleType rolle, NavBrukerKjønn kjønn, Saksnummer saksnummer) {
        NavBruker navBruker = new NavBrukerBuilder()
                .medAktørId(aktørId)
                .medKjønn(kjønn)
                .medForetrukketSpråk(Språkkode.nb)
                .build();
        fagsak = FagsakBuilder.nyEngangstønad(rolle)
                .medSaksnummer(saksnummer)
                .medBruker(navBruker).build();
        fagsakRepository.opprettNy(fagsak);
    }
}
