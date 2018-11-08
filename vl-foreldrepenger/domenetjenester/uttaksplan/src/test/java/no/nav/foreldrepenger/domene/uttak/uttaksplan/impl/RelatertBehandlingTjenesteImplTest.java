package no.nav.foreldrepenger.domene.uttak.uttaksplan.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class RelatertBehandlingTjenesteImplTest {
    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private Repository repository = repoRule.getRepository();

    private RelatertBehandlingTjeneste relatertBehandlingTjeneste = new RelatertBehandlingTjenesteImpl(repositoryProvider);

    @Test
    public void finnesIngenRelatertFagsakReturnererOptionalEmpty() {
        // Arrange
        Behandling farsBehandling = ScenarioFarSøkerForeldrepenger.forFødsel().lagre(repositoryProvider);

        // Act
        Optional<UttakResultatEntitet> uttakresultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(farsBehandling);

        // Assert
        assertThat(uttakresultat).isEmpty();


    }

    @Test
    public void finnesIngenRelatertVedtattBehandlingReturnererOptionalEmpty() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling morsBehandling = scenario.lagre(repositoryProvider);

        Behandling farsBehandling = ScenarioFarSøkerForeldrepenger.forFødsel().lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(morsBehandling.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());

        repository.flushAndClear();

        // Act
        Optional<UttakResultatEntitet> uttakresultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(farsBehandling);

        // Assert
        assertThat(uttakresultat).isEmpty();
    }

    @Test
    public void finnesIngenUttaksplanPåRelatertBehandlingSomErAvslått() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling morsBehandling = scenario.lagre(repositoryProvider);

        Behandlingsresultat behandlingsresultat = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.AVSLÅTT);
        repository.lagre(behandlingsresultat);
        morsBehandling.avsluttBehandling();
        repository.lagre(morsBehandling);

        Behandling farsBehandling = ScenarioFarSøkerForeldrepenger.forFødsel().lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(morsBehandling.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());

        repository.flushAndClear();

        // Act
        Optional<UttakResultatEntitet> uttakresultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(farsBehandling);

        // Assert
        assertThat(uttakresultat).isEmpty();
    }

    @Test
    public void finnerUttaksplanTilRelatertBehandling() {
        // Arrange
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødsel();
        Behandling morsBehandling = scenario.lagre(repositoryProvider);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("1111").oppdatertOpplysningerNå().build();
        repository.lagre(virksomhet);

        UttakAktivitetEntitet aktivitet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID).medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1111"))
            .build();

        LocalDate start = LocalDate.of(2018, 5, 14);

        // Uttak mødrekvote
        UttakResultatPeriodeEntitet uttakMødrekvote = new UttakResultatPeriodeEntitet.Builder(start, start.plusWeeks(6).minusDays(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        UttakResultatPeriodeAktivitetEntitet.builder(uttakMødrekvote, aktivitet)
            .medTrekkdager(30)
            .medTrekkonto(StønadskontoType.MØDREKVOTE)
            .medArbeidsprosent(BigDecimal.ZERO).build();

        perioder.leggTilPeriode(uttakMødrekvote);


        // Uttak fellesperiode
        UttakResultatPeriodeEntitet uttakFellesperiode = new UttakResultatPeriodeEntitet.Builder(start.plusWeeks(6), start.plusWeeks(10).minusDays(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();

        UttakResultatPeriodeAktivitetEntitet.builder(uttakFellesperiode, aktivitet)
            .medTrekkdager(20)
            .medTrekkonto(StønadskontoType.FELLESPERIODE)
            .medArbeidsprosent(BigDecimal.ZERO).build();

        perioder.leggTilPeriode(uttakFellesperiode);

        Behandlingsresultat behandlingsresultat = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repository.lagre(behandlingsresultat);

        repositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(morsBehandling, perioder);

        morsBehandling.avsluttBehandling();
        repository.lagre(morsBehandling);

        Behandling farsBehandling = ScenarioFarSøkerForeldrepenger.forFødsel().lagre(repositoryProvider);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(morsBehandling.getFagsak(), Dekningsgrad._100);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());

        repository.flushAndClear();

        // Act
        Optional<UttakResultatEntitet> uttakresultat = relatertBehandlingTjeneste.hentAnnenPartsGjeldendeUttaksplan(farsBehandling);

        // Assert
        assertThat(uttakresultat).isPresent();
        assertThat(uttakresultat.get().getGjeldendePerioder().getPerioder()).hasSize(2);
    }
}
