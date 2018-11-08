package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import static java.util.Collections.singletonList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;

public class KompletthetssjekkerTestUtil {

    public static final AktørId AKTØR_ID  = new AktørId("1000");
    public static final String ARBGIVER1 = "123456789";
    public static final String ARBGIVER2 = "234567890";

    private UnittestRepositoryRule repoRule;
    private BehandlingRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private FagsakRepository fagsakRepository;

    public KompletthetssjekkerTestUtil(UnittestRepositoryRule repoRule, BehandlingRepositoryProvider repositoryProvider) {
        this.repoRule = repoRule;
        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
    }

    public ScenarioMorSøkerForeldrepenger opprettRevurderingsscenarioForMor() {
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        Behandling førstegangsbehandling = opprettOgAvsluttFørstegangsbehandling(scenario);
        settRelasjonPåFagsak(førstegangsbehandling.getFagsakId(), RelasjonsRolleType.MORA);

        return ScenarioMorSøkerForeldrepenger.forFødsel(false, AKTØR_ID)
            .medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.RE_HENDELSE_FØDSEL)
            .medBehandlingType(BehandlingType.REVURDERING);
    }

    public ScenarioFarSøkerForeldrepenger opprettRevurderingsscenarioForFar() {
        ScenarioFarSøkerForeldrepenger scenario = ScenarioFarSøkerForeldrepenger.forFødselMedGittAktørId(AKTØR_ID);
        Behandling førstegangsbehandling = opprettOgAvsluttFørstegangsbehandling(scenario);
        settRelasjonPåFagsak(førstegangsbehandling.getFagsakId(), RelasjonsRolleType.FARA);

        return ScenarioFarSøkerForeldrepenger.forFødsel(false, AKTØR_ID)
            .medOriginalBehandling(førstegangsbehandling, BehandlingÅrsakType.RE_HENDELSE_FØDSEL)
            .medBehandlingType(BehandlingType.REVURDERING);
    }

    private Behandling opprettOgAvsluttFørstegangsbehandling(AbstractTestScenario<?> scenario) {
        scenario.medBehandlingVedtak()
            .medVedtaksdato(LocalDate.now().minusDays(7))
            .medVedtakResultatType(VedtakResultatType.INNVILGET)
            .medAnsvarligSaksbehandler("Nav Navsdotter")
            .build();
        Behandling førstegangsbehandling = scenario.lagre(repositoryProvider);
        avsluttBehandlingOgFagsak(førstegangsbehandling);
        return førstegangsbehandling;
    }

    private void avsluttBehandlingOgFagsak(Behandling behandling) {
        behandling.avsluttBehandling();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        fagsakRepository.oppdaterFagsakStatus(behandling.getFagsakId(), FagsakStatus.LØPENDE);
    }

    private void settRelasjonPåFagsak(Long fagsakId, RelasjonsRolleType relasjonsRolleType) {
        fagsakRepository.oppdaterRelasjonsRolle(fagsakId, relasjonsRolleType);
    }

    public void byggOgLagreSøknadMedNyOppgittFordeling(Behandling behandling, boolean erEndringssøknad) {
        byggOppgittFordeling(behandling, UtsettelseÅrsak.ARBEID, BigDecimal.valueOf(100), true);
        byggOgLagreSøknadMedEksisterendeOppgittFordeling(behandling, erEndringssøknad);
    }

    public void byggOgLagreSøknadMedEksisterendeOppgittFordeling(Behandling behandling, boolean erEndringssøknad) {
        OppgittFordeling oppgittFordeling = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling).getOppgittFordeling();
        Objects.requireNonNull(oppgittFordeling, "OppgittFordeling må være lagret på forhånd"); // NOSONAR //$NON-NLS-1$

        FamilieHendelse familieHendelse = byggFamilieHendelse(behandling);
        Søknad søknad = new SøknadEntitet.Builder().medElektroniskRegistrert(true)
            .medFordeling(oppgittFordeling)
            .medFamilieHendelse(familieHendelse)
            .medSøknadsdato(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .medErEndringssøknad(erEndringssøknad)
            .build();
        repositoryProvider.getSøknadRepository().lagreOgFlush(behandling, søknad);
    }

    public void byggOppgittFordeling(Behandling behandling, Årsak utsettelseÅrsak, BigDecimal arbeidsprosent, boolean erArbeidstaker) {
        Virksomhet virksomhet = new VirksomhetEntitet.Builder()
            .medOrgnr(ARBGIVER1)
            .medNavn("Virksomhet1")
            .medRegistrert(LocalDate.now().minusYears(10L))
            .medOppstart(LocalDate.now().minusYears(10L))
            .oppdatertOpplysningerNå()
            .build();
        repoRule.getEntityManager().persist(virksomhet);

        OppgittPeriodeBuilder builder = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(10).minusDays(1))
            .medErArbeidstaker(erArbeidstaker)
            .medVirksomhet(virksomhet);

        if (utsettelseÅrsak != null) {
            builder.medÅrsak(utsettelseÅrsak);
        }
        if (arbeidsprosent != null) {
            builder.medArbeidsprosent(arbeidsprosent);
        }

        OppgittPeriode fpPeriode = builder.build();
        OppgittFordelingEntitet oppgittFordeling = new OppgittFordelingEntitet(singletonList(fpPeriode), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(behandling, oppgittFordeling);
    }

    private FamilieHendelse byggFamilieHendelse(Behandling behandling) {
        FamilieHendelseBuilder søknadHendelse = repositoryProvider.getFamilieGrunnlagRepository().opprettBuilderFor(behandling)
            .medAntallBarn(1)
            .medFødselsDato(LocalDate.now().minusDays(1));
        repositoryProvider.getFamilieGrunnlagRepository().lagre(behandling, søknadHendelse);
        return repositoryProvider.getFamilieGrunnlagRepository().hentAggregat(behandling).getSøknadVersjon();
    }
}
