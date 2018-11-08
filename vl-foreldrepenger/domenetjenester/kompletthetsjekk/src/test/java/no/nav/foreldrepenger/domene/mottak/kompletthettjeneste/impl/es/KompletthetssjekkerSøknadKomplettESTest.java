package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.es;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandling.impl.RegisterInnhentingIntervallEndringTjeneste;
import no.nav.foreldrepenger.behandling.impl.SkjæringstidspunktTjenesteImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon.Builder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.personopplysning.impl.PersonopplysningTjenesteImpl;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnMorsMaksdatoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;

/**
 * Test for kompletthetssjekk for engangsstønad
 */
public class KompletthetssjekkerSøknadKomplettESTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private PersonopplysningRepository personopplysningRepository;
    private Kompletthetsjekker testObjekt;

    @Test
    public void ikke_elektronisk_reg_søknad_skal_behandles_som_komplett_ved_adopsjon_og_mangler_vedlegg() {
        Behandling behandling = lagMocketBehandling(false, false, true, LocalDate.now(), false);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    @Test
    public void ikke_elektronisk_reg_søknad_skal_behandles_som_komplett_ved_adopsjon_og_mangler_ikke_vedlegg() {
        Behandling behandling = lagMocketBehandling(false, false, false, LocalDate.now(), false);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    @Test
    public void ikke_elektronisk_reg_søknad_skal_behandles_som_komplett_ved_fødsel_og_mangler_vedlegg() {
        Behandling behandling = lagMocketBehandling(false, true, true, LocalDate.now(), true);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    @Test
    public void ikke_elektronisk_reg_søknad_skal_behandles_som_komplett_ved_fødsel_og_mangler_ikke_vedlegg() {
        Behandling behandling = lagMocketBehandling(false, true, false, LocalDate.now(), true);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    @Test
    public void elektronisk_reg_søknad_skal_behandles_som_ikke_komplett_ved_adopsjon_og_manglende_vedlegg() {
        Behandling behandling = lagMocketBehandling(true, false, true, LocalDate.now(), false);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isFalse();
    }

    @Test
    public void elektronisk_reg_søknad_skal_behandles_som_komplett_ved_fødsel_og_manglende_vedlegg_hvis_bekrefet_i_TPS() {
        Behandling behandling = lagMocketBehandling(true, true, true, LocalDate.now(), true);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    @Test
    public void elektronisk_reg_søknad_skal_behandles_som_komplett_ved_fødsel_og_barn_finnes_i_tps_og_mangler_ikke_vedlegg() {
        Behandling behandling = lagMocketBehandling(true, true, false, LocalDate.now(), true);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    @Test
    public void elektronisk_reg_søknad_skal_behandles_som_være_komplett_ved_fødsel_og_barn_finnes_ikke_i_tps_og_mangler_ikke_vedlegg() {
        Behandling behandling = lagMocketBehandling(true, true, false, LocalDate.now(), false);
        boolean resultat = testObjekt.erForsendelsesgrunnlagKomplett(behandling);
        assertThat(resultat).isTrue();
    }

    private Behandling lagMocketBehandling(boolean elektroniskRegistrert, boolean gjelderFødsel, boolean manglerVedlegg,
                                           LocalDate fødselsdatoBarn, boolean bekreftetViaTps) {

        if (personopplysningRepository != null) {
            throw new IllegalStateException("Kun et oppsett per test!");
        }

        LocalDate fødselsdato = LocalDate.now();

        AbstractTestScenario<?> scenario;
        if (!gjelderFødsel) {
            scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
            scenario.medSøknadHendelse()
                .medAdopsjon(scenario.medSøknadHendelse().getAdopsjonBuilder()
                    .medOmsorgsovertakelseDato(LocalDate.now()));
        } else {
            scenario = ScenarioFarSøkerEngangsstønad.forFødsel();
            scenario.medSøknadHendelse().medFødselsDato(fødselsdato);
        }

        scenario.medSøknad()
            .medElektroniskRegistrert(elektroniskRegistrert);

        Builder builderForRegisteropplysninger = scenario.opprettBuilderForRegisteropplysninger();

        AktørId barnAktørId = new AktørId("123");
        AktørId søkerAktørId = scenario.getDefaultBrukerAktørId();

        PersonInformasjon fødtBarn = builderForRegisteropplysninger
            .medPersonas()
            .fødtBarn(barnAktørId, fødselsdatoBarn)
            .relasjonTil(søkerAktørId, RelasjonsRolleType.MORA, false)
            .build();

        if (bekreftetViaTps) {
            scenario.medRegisterOpplysninger(fødtBarn);
        }

        PersonInformasjon søker = builderForRegisteropplysninger
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.GIFT, Region.NORDEN)
            .statsborgerskap(Landkoder.NOR)
            .relasjonTil(barnAktørId, RelasjonsRolleType.BARN, false)
            .build();
        scenario.medRegisterOpplysninger(søker);

        Behandling behandling = scenario.lagMocked();

        BehandlingRepositoryProvider behandlingRepositoryProvider = scenario.mockBehandlingRepositoryProvider();
        personopplysningRepository = behandlingRepositoryProvider.getPersonopplysningRepository();
        KodeverkRepository kodeverkRepository = behandlingRepositoryProvider.getKodeverkRepository();

        DokumentArkivTjeneste dokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);
        PersonopplysningTjenesteImpl personopplysningTjeneste = new PersonopplysningTjenesteImpl(behandlingRepositoryProvider,
            null, new NavBrukerRepositoryImpl(repoRule.getEntityManager()), new SkjæringstidspunktTjenesteImpl(behandlingRepositoryProvider,
            new BeregnMorsMaksdatoTjenesteImpl(behandlingRepositoryProvider, new RelatertBehandlingTjenesteImpl(behandlingRepositoryProvider)),
            new RegisterInnhentingIntervallEndringTjeneste(Period.of(1, 0, 0), Period.of(0, 4, 0), Period.of(0, 6, 0), Period.of(1, 0, 0)),
            Period.of(0, 3, 0),
            Period.of(0, 10, 0)));
        testObjekt = spy(new KompletthetsjekkerES(behandlingRepositoryProvider, dokumentArkivTjeneste, kodeverkRepository, personopplysningTjeneste));

        if (!manglerVedlegg) {
            when(testObjekt.utledAlleManglendeVedleggForForsendelse(any(Behandling.class)))
                .thenReturn(emptyList());
        } else {
            when(testObjekt.utledAlleManglendeVedleggForForsendelse(any(Behandling.class)))
                .thenReturn(Collections.singletonList(new ManglendeVedlegg(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL)));
        }

        return behandling;
    }
}
