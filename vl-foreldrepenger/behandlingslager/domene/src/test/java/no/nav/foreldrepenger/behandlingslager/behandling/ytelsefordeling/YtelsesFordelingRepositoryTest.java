package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class YtelsesFordelingRepositoryTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());

    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repositoryRule.getEntityManager());
    private FagsakLåsRepository fagsakLåsRepository = new FagsakLåsRepositoryImpl(repositoryRule.getEntityManager());
    private YtelsesFordelingRepository fordelingRepository = new YtelsesFordelingRepositoryImpl(repositoryRule.getEntityManager());

    @Test
    public void skal_lagre_grunnlaget() {
        final Behandling behandling = opprettBehandlingMedFordelingPerioder();

        //Endre periode for å teste overstyring
        final OppgittPeriode periode_12 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(10).plusDays(1), LocalDate.now())
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        final OppgittPeriode periode_22 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(20).plusDays(1), LocalDate.now().minusDays(10))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();

        fordelingRepository.lagreOverstyrtFordeling(behandling, new OppgittFordelingEntitet(Arrays.asList(periode_12, periode_22), true));

        final YtelseFordelingAggregat aggregat = fordelingRepository.hentAggregat(behandling);

        assertThat(aggregat).isNotNull();
        assertThat(aggregat.getOppgittDekningsgrad()).isNotNull();
        assertThat(aggregat.getOppgittRettighet()).isNotNull();
        assertThat(aggregat.getOppgittFordeling()).isNotNull();
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder()).isNotEmpty();
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder()).hasSize(3);
        assertThat(aggregat.getOverstyrtFordeling()).isNotNull();
        assertThat(aggregat.getOverstyrtFordeling().get().getOppgittePerioder()).isNotEmpty();
        assertThat(aggregat.getOverstyrtFordeling().get().getOppgittePerioder()).hasSize(2);

    }

    @Test
    public void nullstillerOverstyring() {
        // Arrange
        Behandling behandling = opprettBehandlingMedFordelingPerioder();

        // Overstyr periodene
        LocalDate idag = LocalDate.now();
        LocalDate fom = idag.minusDays(10).plusDays(1);
        OppgittPeriode overstyrtPeriode = OppgittPeriodeBuilder.ny()
            .medPeriode(fom, idag)
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medÅrsak(UtsettelseÅrsak.SYKDOM)
            .build();

        //Legg til dokumentasjonsperiode for uttak
        PerioderUttakDokumentasjonEntitet perioderUttakDokumentasjonEntitet = new PerioderUttakDokumentasjonEntitet();
        perioderUttakDokumentasjonEntitet.leggTil(new PeriodeUttakDokumentasjonEntitet(fom, idag, UttakDokumentasjonType.SYK_SØKER));
        fordelingRepository.lagreOverstyrtFordeling(behandling, new OppgittFordelingEntitet(Collections.singletonList(overstyrtPeriode), true), perioderUttakDokumentasjonEntitet);

        repositoryRule.getRepository().flushAndClear();

        // Assert
        YtelseFordelingAggregat ytelseFordelingAggregat = fordelingRepository.hentAggregat(behandling);
        assertThat(ytelseFordelingAggregat.getOverstyrtFordeling()).isPresent();
        assertThat(ytelseFordelingAggregat.getOverstyrtFordeling().get().getOppgittePerioder()).hasSize(1);
        assertThat(ytelseFordelingAggregat.getPerioderUttakDokumentasjon()).isPresent();
        assertThat(ytelseFordelingAggregat.getPerioderUttakDokumentasjon().get().getPerioder()).hasSize(1);

        // Act
        fordelingRepository.tilbakestillOverstyringOgDokumentasjonsperioder(behandling);
        repositoryRule.getRepository().flushAndClear();

        YtelseFordelingAggregat oppdatertAggregat = fordelingRepository.hentAggregat(behandling);
        assertThat(oppdatertAggregat.getOverstyrtFordeling()).isEmpty();
        assertThat(oppdatertAggregat.getPerioderUttakDokumentasjon()).isEmpty();
        assertThat(oppdatertAggregat.getOppgittDekningsgrad()).isNotNull();
        assertThat(oppdatertAggregat.getOppgittRettighet()).isNotNull();
        assertThat(oppdatertAggregat.getOppgittFordeling()).isNotNull();
        assertThat(oppdatertAggregat.getOppgittFordeling().getOppgittePerioder()).hasSize(3);
    }

    private Behandling opprettBehandlingMedFordelingPerioder() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(fagsak, Dekningsgrad._100);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        final OppgittRettighet oppgittRettighetEntitet = new OppgittRettighetEntitet(true, true, false);
        fordelingRepository.lagre(behandling, oppgittRettighetEntitet);
        fordelingRepository.lagre(behandling, OppgittDekningsgradEntitet.bruk80());

        final OppgittPeriode periode_1 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(10), LocalDate.now())
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        final OppgittPeriode periode_2 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(20), LocalDate.now().minusDays(10))
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .build();
        final OppgittPeriode periode_3 = OppgittPeriodeBuilder.ny()
            .medPeriode(LocalDate.now().minusDays(20), LocalDate.now().minusDays(10))
            .medPeriodeType(UttakPeriodeType.ANNET)
            .medÅrsak(OppholdÅrsak.KVOTE_FELLESPERIODE_ANNEN_FORELDER)
            .build();
        fordelingRepository.lagre(behandling, new OppgittFordelingEntitet(Arrays.asList(periode_1, periode_2, periode_3), true));
        return behandling;
    }
}
