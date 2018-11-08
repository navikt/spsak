package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENT_PÅ_REGISTEROPPLYSNINGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_FORELDREANSVAR;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.OPPRETTET;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktStatus.UTFØRT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.IKKE_VURDERT;
import static no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType.OPPFYLT;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.AksjonspunktTestutfall.resultat;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.VilkårTestutfall.resultat;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestAssertUtils;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.AksjonspunktRestTjenesteTestAPI;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.BekreftedeAksjonspunkterDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.Foreldreansvarsvilkår1AksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklarFaktaForForeldreansvarAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse.AvklartDataBarnDto;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@Ignore("FIXME: feiler i helgen. Maur eller Termitt")
@RunWith(CdiRunner.class)
public class BehandlingForeldreansvarsvilkårTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingRepository behandlingRepository;

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;
    @Inject
    private AksjonspunktRestTjenesteTestAPI aksjonspunktRestTjenesteAPI;
    @Inject
    private RegisterKontekst registerKontekst;

    private IntegrasjonstestAssertUtils assertUtil = new IntegrasjonstestAssertUtils(repository);

    @Before
    public void setup() {
        registerKontekst.intialiser();
        // setter verdien slik at regler blir kjørt
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }
    @Test
    public void foreldreansvar_mann_happy_case() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(100);
        TpsPerson far = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getMedforelder().get();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(far.getFnr());
        InntektTestSett.inntekt36mnd40000kr(far.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(far.getAktørId(), BehandlingTema.FORELDREPENGER_ADOPSJON);

        // Arrange steg 1: Behandle søknad -> aksjonspunkt for å avklare vilkårtype for omsorgsovertakelse
        LocalDate omsorgsovertakelseDato = LocalDate.now();
        Soeknad søknad = søknadFarOmsorgsovertakelse(fødselsdatoBarn, omsorgsovertakelseDato, far.getAktørId());
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, søknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.isBehandlingPåVent()).isTrue();

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og opprette aksjonspunkter for adopsjon
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(far.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        assertThat(repository.hentAlle(PersonopplysningEntitet.class)).hasSize(2);
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FORELDREANSVARSVILKÅRET_2_LEDD, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(
            resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            resultat(AVKLAR_VILKÅR_FOR_FORELDREANSVAR, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT));

        // Arrange Steg 3: Avklar fakta fra GUI -> Vilkår for omsorgsovertakelse settes til Foreldreansvarsvilkåret
        AvklarFaktaForForeldreansvarAksjonspunktDto aksjonspunktDto = byggOmsorgovertakelseAksjonspunktDto(behandlingId);
        BekreftedeAksjonspunkterDto dto = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(aksjonspunktDto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dto);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.IKKE_FASTSATT,
            asList(resultat(FORELDREANSVARSVILKÅRET_2_LEDD, IKKE_VURDERT)));
        assertUtil.assertAksjonspunkter(asList(
            resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            resultat(AVKLAR_VILKÅR_FOR_FORELDREANSVAR, UTFØRT),
            resultat(MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));

        // Steg 3: Sett omsorgsvilkåret til oppfylt GUI -> Omsorgsvilkåret oppfylles, totrinnsbehandling er trigget.
        Foreldreansvarsvilkår1AksjonspunktDto omsorgDto = byggForeldreansvarsvilkår1AksjonspunktDto(behandlingId, true);
        BekreftedeAksjonspunkterDto dto2 = BekreftedeAksjonspunkterDto.lagDto(behandlingId, behandling.getVersjon(), singletonList(omsorgDto));

        // Act
        aksjonspunktRestTjenesteAPI.bekreft(dto2);

        // Assert
        assertUtil.assertVilkårresultatOgRegelmerknad(VilkårResultatType.INNVILGET,
            asList(resultat(FORELDREANSVARSVILKÅRET_2_LEDD, OPPFYLT)));
        assertUtil.assertAksjonspunkter(asList(
            resultat(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD, UTFØRT),
            resultat(AVKLAR_VILKÅR_FOR_FORELDREANSVAR, UTFØRT),
            resultat(MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD, UTFØRT),
            resultat(MANUELL_KONTROLL_AV_OM_BRUKER_HAR_OMSORG, OPPRETTET),
            resultat(AUTO_VENT_PÅ_REGISTEROPPLYSNINGER, UTFØRT)));
    }


    private Soeknad søknadFarOmsorgsovertakelse(LocalDate fødselsdato,
                                                LocalDate omsorgsovertakelseDato, AktørId aktørId) {
        return new SøknadTestdataBuilder().søknadForeldrepenger()
            .medSøker(ForeldreType.FAR, aktørId)
            .medMottattdato(LocalDate.now())
            .medOmsorgsovertakelse(new SøknadTestdataBuilder.OmsorgsovertakelseBuilder()
                .medOmsorgsovertakelseDato(omsorgsovertakelseDato)
                .medFoedselsdatoer(Collections.singletonList(fødselsdato))
                .medAntallBarn(1)
                .medOmsorgsovertakelseaarsaker(FarSøkerType.OVERTATT_OMSORG))
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(fødselsdato, fødselsdato.plusWeeks(25).minusDays(1),
                    UttakPeriodeType.FORELDREPENGER))
            .build();
    }


    private Foreldreansvarsvilkår1AksjonspunktDto byggForeldreansvarsvilkår1AksjonspunktDto(Long behandlingId, boolean vilkårOk) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Stream<Aksjonspunkt> streammAp = behandling.getAksjonspunkter().stream();
        Long aksjonspunktId = streammAp
            .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon().equals(MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD))
            .map(Aksjonspunkt::getId)
            .findFirst()
            .orElse(null);
        assertThat(aksjonspunktId).isNotNull();

        return new Foreldreansvarsvilkår1AksjonspunktDto("Grunn", vilkårOk, "1008");
    }

    private AvklarFaktaForForeldreansvarAksjonspunktDto byggOmsorgovertakelseAksjonspunktDto(Long behandlingId) {

        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        Stream<Aksjonspunkt> streammAp = behandling.getAksjonspunkter().stream();
        Long aksjonspunktId = streammAp
            .filter(aksjonspunkt -> aksjonspunkt.getAksjonspunktDefinisjon()
                .equals(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_FORELDREANSVAR))
            .map(Aksjonspunkt::getId)
            .findFirst()
            .orElse(null);
        assertThat(aksjonspunktId).isNotNull();
        Map<Long, LocalDate> map = new HashMap<>();
        map.put(1L, LocalDate.now());

        AvklarFaktaForForeldreansvarAksjonspunktDto dto = new AvklarFaktaForForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(LocalDate.now());
        dto.setForeldreansvarDato(LocalDate.now());
        dto.setForeldre(emptyList());

        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        UidentifisertBarn adopsjonBarn = familieHendelseGrunnlag.getGjeldendeBarna().iterator().next();
        AvklartDataBarnDto barn = new AvklartDataBarnDto();
        barn.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);
        barn.setFodselsdato(adopsjonBarn.getFødselsdato());
        dto.setBarn(singletonList(barn));
        return dto;
    }


}
