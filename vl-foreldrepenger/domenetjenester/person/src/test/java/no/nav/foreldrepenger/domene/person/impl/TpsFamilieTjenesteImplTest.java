package no.nav.foreldrepenger.domene.person.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.Familierelasjon;
import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.UnitTestInstanceImpl;

public class TpsFamilieTjenesteImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private TpsTjeneste tpsTjeneste;
    private TpsFamilieTjeneste tpsFamilieTjeneste;

    @Before
    public void setUp() throws Exception {
        tpsTjeneste = mock(TpsTjeneste.class);
        final UnitTestInstanceImpl<Period> periodeFør = new UnitTestInstanceImpl<>(Period.parse("P1W"));
        final UnitTestInstanceImpl<Period> periodeEtter = new UnitTestInstanceImpl<>(Period.parse("P4W"));
        tpsFamilieTjeneste = new TpsFamilieTjenesteImpl(tpsTjeneste, repositoryProvider, periodeFør, periodeEtter);
    }

    @Test
    public void test() {
        final LocalDate mottattDato = LocalDate.now().minusDays(30);
        final int antallBarn = 1;
        final Behandling behandling = opprettOriginalBehandling(antallBarn, BehandlingResultatType.INNVILGET, mottattDato);
        Behandling revurdering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(BehandlingÅrsak.builder(BehandlingÅrsakType.RE_HENDELSE_FØDSEL).medOriginalBehandling(behandling)).build();
        BehandlingLås lås = repositoryProvider.getBehandlingRepository().taSkriveLås(revurdering);
        repositoryProvider.getBehandlingRepository().lagre(revurdering, lås);
        repositoryProvider.getFamilieGrunnlagRepository().kopierGrunnlagFraEksisterendeBehandling(behandling, revurdering);

        final Personinfo personinfo = opprettPersonInfo(behandling.getAktørId(), antallBarn, mottattDato);
        when(tpsTjeneste.hentBrukerForAktør(behandling.getAktørId())).thenReturn(Optional.of(personinfo));
        when(tpsTjeneste.hentFødteBarn(behandling.getAktørId())).thenReturn(genererBarn(personinfo.getFamilierelasjoner()));

        final FamilieHendelseGrunnlag familieHendelseGrunnlag = repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(revurdering);
        final List<FødtBarnInfo> fødslerRelatertTilBehandling = tpsFamilieTjeneste.getFødslerRelatertTilBehandling(revurdering, familieHendelseGrunnlag);

        assertThat(fødslerRelatertTilBehandling).hasSize(antallBarn);
    }

    private List<FødtBarnInfo> genererBarn(Set<Familierelasjon> familierelasjoner) {
        final ArrayList<FødtBarnInfo> barn = new ArrayList<>();
        for (Familierelasjon familierelasjon : familierelasjoner) {
            barn.add(new FødtBarnInfo.Builder()
                .medFødselsdato(familierelasjon.getFødselsdato())
                .medIdent(familierelasjon.getPersonIdent())
                .medNavn("navn")
                .medNavBrukerKjønn(NavBrukerKjønn.MANN)
                .build());
        }
        return barn;
    }

    private Personinfo opprettPersonInfo(AktørId aktørId, int antallBarn, LocalDate startdatoIntervall) {
        final Personinfo.Builder builder = new Personinfo.Builder();
        builder.medAktørId(aktørId)
            .medNavn("Test")
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medFødselsdato(LocalDate.now().minusYears(30))
            .medPersonIdent(new PersonIdent("123123123"))
            .medFamilierelasjon(genererBarn(antallBarn, startdatoIntervall));
        return builder.build();
    }

    private Set<Familierelasjon> genererBarn(int antallBarn, LocalDate startdatoIntervall) {
        final Set<Familierelasjon> set = new HashSet<>();
        LocalDate generertFødselsdag = genererFødselsdag(startdatoIntervall.minusWeeks(1));
        IntStream.range(0, Math.toIntExact(antallBarn))
            .forEach(barnNr -> set.add(new Familierelasjon(new PersonIdent("" + barnNr + 10L), RelasjonsRolleType.BARN, generertFødselsdag, "Adr", true)));
        return set;
    }

    private LocalDate genererFødselsdag(LocalDate startdatoIntervall) {
        final DatoIntervallEntitet datoIntervallEntitet = DatoIntervallEntitet.fraOgMedTilOgMed(startdatoIntervall, LocalDate.now());
        final long l = datoIntervallEntitet.antallDager();

        final double v = Math.random() * l;
        return startdatoIntervall.plusDays((long) v);
    }

    private Behandling opprettOriginalBehandling(int antallBarn, BehandlingResultatType behandlingResultatType, LocalDate mottattDato) {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medDefaultBekreftetTerminbekreftelse();
        scenario.medSøknad().medSøknadsdato(mottattDato).medMottattDato(mottattDato);
        Behandling originalBehandling = scenario.lagre(repositoryProvider);
        Behandlingsresultat originalResultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(behandlingResultatType)
            .buildFor(originalBehandling);

        BehandlingLås behandlingLås = repositoryProvider.getBehandlingRepository().taSkriveLås(originalBehandling);
        repositoryProvider.getBehandlingRepository().lagre(originalBehandling, behandlingLås);

        if (behandlingResultatType.equals(BehandlingResultatType.INNVILGET)) {
            BeregningResultat originalBeregning = opprettBeregning(originalBehandling, antallBarn);
            repositoryProvider.getBeregningRepository().lagre(originalBeregning, behandlingLås);
        }
        BehandlingVedtak originalVedtak = BehandlingVedtak.builder()
            .medVedtaksdato(LocalDate.now())
            .medBehandlingsresultat(originalResultat)
            .medVedtakResultatType(behandlingResultatType.equals(BehandlingResultatType.INNVILGET) ?
                VedtakResultatType.INNVILGET : VedtakResultatType.AVSLAG)
            .medAnsvarligSaksbehandler("asdf")
            .build();

        repositoryProvider.getBehandlingVedtakRepository().lagre(originalVedtak, behandlingLås);
        return originalBehandling;
    }

    private BeregningResultat opprettBeregning(Behandling behandling, long antallBarn) {
        Beregning beregning = new Beregning(1000L, antallBarn, antallBarn * 1000, LocalDateTime.now());
        return BeregningResultat.builder().medBeregning(beregning).buildFor(behandling);
    }
}
