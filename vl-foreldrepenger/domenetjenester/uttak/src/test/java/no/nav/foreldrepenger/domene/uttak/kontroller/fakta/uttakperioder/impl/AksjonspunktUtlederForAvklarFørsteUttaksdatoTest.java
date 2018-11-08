package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.UttakRevurderingTestUtil;

public class AksjonspunktUtlederForAvklarFørsteUttaksdatoTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());

    private AksjonspunktUtlederForAvklarFørsteUttaksdato avklarFørsteUttaksdato;
    private UttakRevurderingTestUtil testUtil;

    @Before
    public void before() {
        avklarFørsteUttaksdato = new AksjonspunktUtlederForAvklarFørsteUttaksdato(repositoryProvider);
        testUtil = new UttakRevurderingTestUtil(repoRule, repositoryProvider);
    }

    @Test
    public void aksjonspunkt_dersom_manuelt_satt_uttaksdato_diff_fra_original_behandling_vedtak() {
        Behandling revurdering = testUtil.opprettRevurdering();
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_KLAGE_UTEN_END_INNTEKT)
            .medOriginalBehandling(revurdering.getOriginalBehandling().get())
            .medManueltOpprettet(true);
        revurderingÅrsak.buildFor(revurdering);
        repositoryProvider.getBehandlingRepository().lagre(revurdering, repositoryProvider.getBehandlingRepository().taSkriveLås(revurdering));

        LocalDate manuellSattDato = getFørsteUttakDatoIGjeldendeBehandling(revurdering).plusDays(3);
        repositoryProvider.getYtelsesFordelingRepository().lagre(revurdering, new AvklarteUttakDatoerEntitet(manuellSattDato, LocalDate.now().plusWeeks(1)));
        OppgittPeriode periode = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE)
            .medPeriode(manuellSattDato.minusDays(1), manuellSattDato.plusWeeks(7))
            .build();
        repositoryProvider.getYtelsesFordelingRepository().lagre(revurdering, new OppgittFordelingEntitet(Collections.singletonList(periode), true));

        // Act
        List<AksjonspunktResultat> aksjonspunkter = avklarFørsteUttaksdato.utledAksjonspunkterFor(revurdering);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.AVKLAR_FØRSTE_UTTAKSDATO);

    }

    @Test
    public void aksjonspunkt_dersom_manuelt_satt_uttaksdato_diff_fra_søknaden_for_førstgangs() {
        ScenarioMorSøkerForeldrepenger scenarioMorSøkerForeldrepenger = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)))
            .medFordeling(new OppgittFordelingEntitet(Collections.singletonList(OppgittPeriodeBuilder.ny()
                .medPeriodeType(UttakPeriodeType.FEDREKVOTE)
                .medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(12))
                .build()), true));

        Behandling behandling = scenarioMorSøkerForeldrepenger.lagre(repositoryProvider);

        // Act
        List<AksjonspunktResultat> aksjonspunkter = avklarFørsteUttaksdato.utledAksjonspunkterFor(behandling);

        // Assert
        assertThat(aksjonspunkter.stream()
            .map(AksjonspunktResultat::getAksjonspunktDefinisjon)
            .collect(Collectors.toList()))
            .contains(AksjonspunktDefinisjon.AVKLAR_FØRSTE_UTTAKSDATO);

    }

    private LocalDate getFørsteUttakDatoIGjeldendeBehandling(Behandling behandling) {
        Optional<Behandling> originalBehandling = behandling.getOriginalBehandling();
        if (originalBehandling.isPresent()) {
            Optional<UttakResultatEntitet> gjeldendeUttakResultat = repositoryProvider.getUttakRepository().hentUttakResultatHvisEksisterer(originalBehandling.get());
            if (gjeldendeUttakResultat.isPresent()) {
                Optional<UttakResultatPeriodeEntitet> førsteUttaksdatoGjeldendeVedtak = gjeldendeUttakResultat.get().getGjeldendePerioder().getPerioder()
                    .stream()
                    .min(Comparator.comparing(UttakResultatPeriodeEntitet::getFom));
                return førsteUttaksdatoGjeldendeVedtak.get().getFom();
            }
        }
        return LocalDate.now();
    }

}
