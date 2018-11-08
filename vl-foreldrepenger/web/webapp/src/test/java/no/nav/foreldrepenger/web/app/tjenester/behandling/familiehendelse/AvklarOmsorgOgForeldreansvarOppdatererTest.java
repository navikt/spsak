package no.nav.foreldrepenger.web.app.tjenester.behandling.familiehendelse;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Adopsjon;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.OmsorgsovertakelseVilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagDel;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagFelt;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.OmsorghendelseTjeneste;
import no.nav.foreldrepenger.domene.familiehendelse.omsorg.impl.OmsorghendelseTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer.AvklarOmsorgOgForeldreansvarOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public class AvklarOmsorgOgForeldreansvarOppdatererTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    @Rule
    public final UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private final HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final SkjæringstidspunktTjeneste skjæringstidspunktTjeneste = new SkjæringstidspunktTjenesteImpl(repositoryProvider,
        new BeregnMorsMaksdatoTjenesteImpl(repositoryProvider, new RelatertBehandlingTjenesteImpl(repositoryProvider)),
        new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
        Period.of(0, 3, 0),
        Period.of(0, 10, 0));
    private final LocalDate nå = LocalDate.now();
    private VilkårResultat.Builder vilkårBuilder = VilkårResultat.builder();
    private OmsorghendelseTjeneste omsorghendelseTjeneste = new OmsorghendelseTjenesteImpl(repositoryProvider);

    @Test
    public void skal_oppdatere_vilkår_for_omsorg() {
        // Arrange
        AktørId forelderId = new AktørId("1");
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));

        PersonInformasjon forelder = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builderMedDefaultVerdier(forelderId)
                    .navn("Forelder"))
            .build();

        scenario.medRegisterOpplysninger(forelder);
        scenario.leggTilAksjonspunkt(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, BehandlingStegType.KONTROLLER_FAKTA);

        Behandling behandling = scenario.lagre(repositoryProvider);

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(2);
        dto.setOmsorgsovertakelseDato(nå);
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);

        avklarOmsorgOgForeldreansvar(behandling, dto);
        vilkårBuilder.buildFor(behandling);

        // Assert
        final FamilieHendelse gjellendeVersjon = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling)
            .getGjeldendeVersjon();
        final Optional<Adopsjon> adopsjon = gjellendeVersjon.getAdopsjon();
        assertThat(gjellendeVersjon.getAntallBarn()).isEqualTo(2);
        assertThat(adopsjon).hasValueSatisfying(value -> {
            assertThat(value.getOmsorgsovertakelseDato()).as("omsorgsovertakelsesDato").isEqualTo(nå);
            assertThat(value.getOmsorgovertakelseVilkår()).as("omsorgsovertakelsesVilkår").isEqualTo(OmsorgsovertakelseVilkårType.OMSORGSVILKÅRET);
        });
    }

    @Test
    public void skal_legge_til_nytt_barn_dersom_id_er_tom_i_dto() {
        // Arrange
        // Behandlingsgrunnlag UTEN eksisterende bekreftet barn
        AktørId forelderId = new AktørId("1");
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));
        scenario.medSøknad();

        PersonInformasjon forelder = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builderMedDefaultVerdier(forelderId)
                    .navn("Forelder"))
            .build();

        scenario.medRegisterOpplysninger(forelder);
        scenario.leggTilAksjonspunkt(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, BehandlingStegType.KONTROLLER_FAKTA);

        Behandling behandling = scenario.lagre(repositoryProvider);

        AvklartDataBarnDto barn1 = new AvklartDataBarnDto();
        barn1.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);
        barn1.setFodselsdato(nå);

        AvklartDataForeldreDto forelder1 = new AvklartDataForeldreDto();
        forelder1.setAktorId(forelderId);
        forelder1.settOpplysningsKilde(OpplysningsKilde.TPS);

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(nå);
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);
        dto.setForeldre(singletonList(forelder1));
        dto.setBarn(singletonList(barn1));

        // Act
        avklarOmsorgOgForeldreansvar(behandling, dto);

        // Assert
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        final FamilieHendelse gjeldendeVersjon = familieHendelseGrunnlag.getGjeldendeVersjon();
        assertThat(gjeldendeVersjon.getBarna()).hasSize(1);
    }

    @Test
    public void skal_oppdatere_eksisterende_barn_dersom_id_er_oppgitt_i_dto() {
        // Arrange
        AktørId forelderId = new AktørId("1337");
        LocalDate fødselsdato = nå;
        LocalDate oppdatertFødselsdato = fødselsdato.plusDays(1);

        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));

        PersonInformasjon forelder = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builderMedDefaultVerdier(forelderId)
                    .navn("Forelder"))
            .build();

        scenario.medSøknad();
        scenario.medRegisterOpplysninger(forelder);
        scenario.leggTilAksjonspunkt(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, BehandlingStegType.KONTROLLER_FAKTA);

        Behandling behandling = scenario.lagre(repositoryProvider);
        final no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon personopplysning = getSøkerPersonopplysning(behandling);
        AvklartDataBarnDto barn1 = new AvklartDataBarnDto();
        barn1.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);
        barn1.setFodselsdato(oppdatertFødselsdato);

        AvklartDataForeldreDto forelder1 = new AvklartDataForeldreDto();
        forelder1.setAktorId(personopplysning.getPersonopplysninger().stream()
            .filter(e -> e.getAktørId().equals(forelderId))
            .findFirst().get().getAktørId());
        forelder1.settOpplysningsKilde(OpplysningsKilde.TPS);

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(nå);
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);
        dto.setForeldre(Collections.singletonList(forelder1));
        dto.setBarn(Collections.singletonList(barn1));

        // Act
        avklarOmsorgOgForeldreansvar(behandling, dto);

        // Assert
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        final FamilieHendelse gjeldendeVersjon = familieHendelseGrunnlag.getGjeldendeVersjon();
        assertThat(gjeldendeVersjon.getBarna()).hasSize(1);
        assertThat(gjeldendeVersjon.getAntallBarn()).isEqualTo(1);
        assertThat(gjeldendeVersjon.getBarna().stream().map(UidentifisertBarn::getFødselsdato)
            .collect(Collectors.toList())).contains(oppdatertFødselsdato);
    }

    private void avklarOmsorgOgForeldreansvar(Behandling behandling, AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto) {
        new AvklarOmsorgOgForeldreansvarOppdaterer(repositoryProvider, skjæringstidspunktTjeneste, omsorghendelseTjeneste, lagMockHistory())
            .oppdater(dto, behandling, vilkårBuilder);
    }

    @Test
    public void skal_fjerne_eksisterende_barn_dersom_antall_barn_reduseres() {
        // Arrange
        AktørId forelderId = new AktørId("3");
        LocalDate fødselsdato1 = nå;

        // Behandlingsgrunnlag MED eksisterende 2 bekreftede barn
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse().medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder().medOmsorgsovertakelseDato(LocalDate.now()));

        PersonInformasjon forelder = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builderMedDefaultVerdier(forelderId)
                    .navn("Forelder"))
            .build();

        scenario.medSøknad();
        scenario.medRegisterOpplysninger(forelder);
        scenario.leggTilAksjonspunkt(AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandling = scenario.lagre(repositoryProvider);
        final no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon personopplysning = getSøkerPersonopplysning(behandling);
        // Kun 1 barn fra DTO
        AvklartDataBarnDto barnDto1 = new AvklartDataBarnDto();
        barnDto1.setFodselsdato(nå);
        barnDto1.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);

        AvklartDataForeldreDto forelder1 = new AvklartDataForeldreDto();
        forelder1.setAktorId(personopplysning.getPersonopplysninger().stream()
            .filter(e -> e.getAktørId().equals(forelderId))
            .findFirst().get().getAktørId());
        forelder1.settOpplysningsKilde(OpplysningsKilde.SAKSBEH);

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(nå);
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);
        dto.setForeldre(singletonList(forelder1));
        dto.setBarn(singletonList(barnDto1));

        // Act
        avklarOmsorgOgForeldreansvar(behandling, dto);

        // Assert
        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling);
        final FamilieHendelse gjeldendeVersjon = familieHendelseGrunnlag.getGjeldendeVersjon();
        assertThat(gjeldendeVersjon.getBarna()).hasSize(1);
        assertThat(gjeldendeVersjon.getAntallBarn()).isEqualTo(1);
        assertThat(gjeldendeVersjon.getBarna().stream().map(UidentifisertBarn::getFødselsdato)
            .collect(Collectors.toList())).containsOnly(fødselsdato1);
    }

    private no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon getSøkerPersonopplysning(Behandling behandling) {
        PersonopplysningGrunnlag grunnlag = getPersonopplysninger(behandling);
        return grunnlag.getGjeldendeVersjon();
    }

    private PersonopplysningGrunnlag getPersonopplysninger(Behandling behandling) {
        return repositoryProvider.getPersonopplysningRepository()
            .hentPersonopplysninger(behandling);
    }

    @Test
    public void skal_sette_andre_aksjonspunkter_knyttet_til_omsorgsvilkåret_som_utført() {
        // Arrange
        AktørId forelderId = new AktørId("1");
        LocalDate dødsdato = nå;
        LocalDate oppdatertDødsdato = dødsdato.plusDays(1);
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now()));
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE,
            BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_2_LEDD,
            BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_FORELDREANSVARSVILKÅRET_4_LEDD,
            BehandlingStegType.SØKERS_RELASJON_TIL_BARN);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_OMSORGSVILKÅRET,
            BehandlingStegType.SØKERS_RELASJON_TIL_BARN);

        PersonInformasjon forelder = scenario.opprettBuilderForRegisteropplysninger()
            .leggTilPersonopplysninger(
                Personopplysning.builderMedDefaultVerdier(forelderId)
                    .dødsdato(dødsdato)
                    .navn("Navn"))
            .build();

        scenario.medRegisterOpplysninger(forelder);
        Behandling behandling = scenario.lagre(repositoryProvider);

        AvklartDataForeldreDto forelderDto = new AvklartDataForeldreDto();
        forelderDto.setAktorId(forelderId);
        forelderDto.settOpplysningsKilde(OpplysningsKilde.TPS);
        forelderDto.setDødsdato(oppdatertDødsdato);

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(nå);
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);
        dto.setForeldre(singletonList(forelderDto));

        avklarOmsorgOgForeldreansvar(behandling, dto);

        // Assert
        assertThat(behandling.getAksjonspunkter().stream().filter(Aksjonspunkt::erAvbrutt))
            .anySatisfy(ap -> assertThat(AksjonspunktDefinisjon.MANUELL_VURDERING_AV_OMSORGSVILKÅRET).isEqualTo(ap.getAksjonspunktDefinisjon()));

        assertThat(behandling.getAksjonspunkter().stream().filter(Aksjonspunkt::erAvbrutt))
            .allMatch(ap -> !Objects.equals(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, ap.getAksjonspunktDefinisjon()));

    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_omsorgsovertakelsesdato() {
        // Arrange
        LocalDate omsorgsovertakelsesdatoOppgitt = LocalDate.of(2019, 3, 4);
        LocalDate omsorgsovertakelsesdatoBekreftet = omsorgsovertakelsesdatoOppgitt.plusDays(1);

        // Behandling
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknad()
            .medFarSøkerType(FarSøkerType.OVERTATT_OMSORG);
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(omsorgsovertakelsesdatoOppgitt));
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, BehandlingStegType.KONTROLLER_FAKTA);
        scenario.lagre(repositoryProvider);

        Behandling behandling = scenario.getBehandling();

        // Dto
        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(1);
        dto.setOmsorgsovertakelseDato(omsorgsovertakelsesdatoBekreftet);
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);

        avklarOmsorgOgForeldreansvar(behandling, dto);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslagDeler = this.tekstBuilder.build(historikkinnslag);

        // Assert
        assertThat(historikkInnslagDeler).hasSize(1);
        List<HistorikkinnslagFelt> feltList = historikkInnslagDeler.get(0).getEndredeFelt();
        HistorikkinnslagFelt felt = feltList.get(0);
        assertThat(felt.getNavn()).as("navn").isEqualTo(HistorikkEndretFeltType.OMSORGSOVERTAKELSESDATO.getKode());
        assertThat(felt.getFraVerdi()).as("fraVerdi").isEqualTo("04.03.2019");
        assertThat(felt.getTilVerdi()).as("tilVerdi").isEqualTo("05.03.2019");
    }

    @Test
    public void skal_generere_historikkinnslag_ved_avklaring_av_ved_omsorgsovertakelse_antall_barn() {
        // Arrange
        int antallBarnFraSøknad = 2;
        int antallBarnBekreftet = 1;

        // Behandling
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        scenario.medSøknad()
            .medFarSøkerType(FarSøkerType.OVERTATT_OMSORG);
        scenario.medSøknadHendelse()
            .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                .medOmsorgsovertakelseDato(LocalDate.now()))
            .medAntallBarn(antallBarnFraSøknad);
        scenario.leggTilAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_VILKÅR_FOR_OMSORGSOVERTAKELSE, BehandlingStegType.KONTROLLER_FAKTA);
        Behandling behandling = scenario.lagre(repositoryProvider);

        // Dto
        AvklartDataBarnDto barn = new AvklartDataBarnDto();
        barn.setFodselsdato(LocalDate.now());
        barn.setOpplysningsKilde(OpplysningsKilde.SAKSBEH);

        AvklartDataForeldreDto forelder1 = new AvklartDataForeldreDto();
        forelder1.settOpplysningsKilde(OpplysningsKilde.SAKSBEH);

        AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto dto = new AvklarFaktaForOmsorgOgForeldreansvarAksjonspunktDto();
        dto.setAntallBarn(antallBarnBekreftet);
        dto.setOmsorgsovertakelseDato(LocalDate.now());
        dto.setVilkårType(VilkårType.OMSORGSVILKÅRET);
        dto.setForeldre(Collections.singletonList(forelder1));
        dto.setBarn(Collections.singletonList(barn));

        avklarOmsorgOgForeldreansvar(behandling, dto);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.FAKTA_ENDRET);
        List<HistorikkinnslagDel> historikkInnslagDeler = this.tekstBuilder.build(historikkinnslag);

        // Assert
        assertHistorikkinnslag(historikkInnslagDeler, HistorikkEndretFeltType.ANTALL_BARN, Integer.toString(antallBarnFraSøknad),
            Integer.toString(antallBarnBekreftet));
    }

    private void assertHistorikkinnslag(List<HistorikkinnslagDel> historikkInnslagDeler, HistorikkEndretFeltType endretFeltType, String fraVerdi,
                                        String tilVerdi) {
        assertThat(historikkInnslagDeler).hasSize(1);
        HistorikkinnslagDel del = historikkInnslagDeler.get(0);
        Optional<HistorikkinnslagFelt> feltOpt = del.getEndretFelt(endretFeltType);
        assertThat(feltOpt).as("endretFelt").hasValueSatisfying(felt -> {
            assertThat(felt.getNavn()).as("navn").isEqualTo(endretFeltType.getKode());
            assertThat(felt.getFraVerdi()).as("fraVerdi").isEqualTo(fraVerdi);
            assertThat(felt.getTilVerdi()).as("tilVerdi").isEqualTo(tilVerdi);
        });
    }

    private HistorikkTjenesteAdapter lagMockHistory() {
        HistorikkTjenesteAdapter mockHistory = Mockito.mock(HistorikkTjenesteAdapter.class);
        Mockito.when(mockHistory.tekstBuilder()).thenReturn(tekstBuilder);
        return mockHistory;
    }

}
