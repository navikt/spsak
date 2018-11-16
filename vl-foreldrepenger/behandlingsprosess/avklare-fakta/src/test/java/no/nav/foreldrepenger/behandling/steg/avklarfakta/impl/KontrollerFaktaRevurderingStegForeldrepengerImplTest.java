package no.nav.foreldrepenger.behandling.steg.avklarfakta.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandling.steg.avklarfakta.api.KontrollerFaktaSteg;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.AvklarteUttakDatoerEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Region;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Uttaksperiodegrense;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.kontrollerfakta.KontrollerFaktaTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KontrollerFaktaRevurderingStegForeldrepengerImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private Behandling behandling;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private PersonInformasjon.Builder personopplysningBuilder;

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef("BT-004")
    private KontrollerFaktaTjeneste kontrollerFaktaTjeneste;

    @Inject
    @FagsakYtelseTypeRef("FP")
    @BehandlingTypeRef("BT-004")
    private KontrollerFaktaSteg steg;

    // Trenger denne for å sette aktivt steg. Kunne med fordel heller ha vært mulig i scenariobuilder for behandling.
    @Inject
    private InternalManipulerBehandling internalManipulerBehandling;

    @Before
    public void oppsett() {
        LocalDate fødselsdato = LocalDate.now().minusYears(20);
        AktørId aktørId = new AktørId("1");

        ScenarioMorSøkerForeldrepenger førstegangScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .medBehandlingStegStart(BehandlingStegType.KONTROLLER_FAKTA);

        førstegangScenario.removeDodgyDefaultInntektArbeidYTelse();

        AktørId søkerAktørId = førstegangScenario.getDefaultBrukerAktørId();

        PersonInformasjon personInformasjon = førstegangScenario
            .opprettBuilderForRegisteropplysninger()
            .medPersonas()
            .kvinne(søkerAktørId, SivilstandType.SAMBOER).statsborgerskap(Landkoder.USA)
            .build();

        førstegangScenario.medRegisterOpplysninger(personInformasjon);

        personopplysningBuilder = førstegangScenario.opprettBuilderForRegisteropplysninger();
        personopplysningBuilder.leggTilPersonopplysninger(
            Personopplysning.builder().aktørId(aktørId).sivilstand(SivilstandType.GIFT)
                .fødselsdato(fødselsdato).brukerKjønn(NavBrukerKjønn.KVINNE).navn("Marie Curie")
                .region(Region.UDEFINERT)
        ).leggTilAdresser(
            PersonAdresse.builder()
                .adresselinje1("dsffsd 13").aktørId(aktørId).land("USA")
                .adresseType(AdresseType.POSTADRESSE_UTLAND)
                .periode(fødselsdato, LocalDate.now())
        ).leggTilPersonstatus(
            Personstatus.builder().aktørId(aktørId).personstatus(PersonstatusType.UTVA)
                .periode(fødselsdato, LocalDate.now())
        ).leggTilStatsborgerskap(
            Statsborgerskap.builder().aktørId(aktørId)
                .periode(fødselsdato, LocalDate.now())
                .region(Region.UDEFINERT)
                .statsborgerskap(Landkoder.USA)
        );

        førstegangScenario.medRegisterOpplysninger(personopplysningBuilder.build());
        førstegangScenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now(), null));

        Behandling originalBehandling = førstegangScenario.lagre(repositoryProvider);
        // Legg til Uttaksperiodegrense -> dessverre ikke tilgjengelig i scenariobygger
        BehandlingLås lås = behandlingRepository.taSkriveLås(originalBehandling);
        behandlingRepository.lagre(originalBehandling, lås);
        Uttaksperiodegrense uttaksperiodegrense = new Uttaksperiodegrense.Builder(originalBehandling)
            .medFørsteLovligeUttaksdag(LocalDate.now())
            .medMottattDato(LocalDate.now())
            .build();
        repositoryProvider.getUttakRepository().lagreUttaksperiodegrense(originalBehandling, uttaksperiodegrense);
        // Legg til Opptjeningsperidoe -> dessverre ikke tilgjengelig i scenariobygger
        repositoryProvider.getOpptjeningRepository().lagreOpptjeningsperiode(originalBehandling, LocalDate.now().minusYears(1), LocalDate.now());
        //Legg til fordelingsperiode
        OppgittPeriode foreldrepenger = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(LocalDate.now(), LocalDate.now().plusWeeks(20))
            .build();
        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Collections.singletonList(foreldrepenger), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(originalBehandling, fordeling);

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING)
            .medRegisterOpplysninger(personopplysningBuilder.build())
            .medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_MANGLER_FØDSEL);
        revurderingScenario.removeDodgyDefaultInntektArbeidYTelse();

        revurderingScenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now(), null));

        behandling = revurderingScenario.lagre(repositoryProvider);
        //kopierer ytelsefordeling grunnlag
        repositoryProvider.getYtelsesFordelingRepository().kopierGrunnlagFraEksisterendeBehandling(originalBehandling, behandling);

        // Nødvendig å sette aktivt steg for KOFAK revurdering
        internalManipulerBehandling.forceOppdaterBehandlingSteg(behandling, BehandlingStegType.KONTROLLER_FAKTA);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    @Test
    public void skal_fjerne_aksjonspunkter_som_er_utledet_før_startpunktet() {
        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);
        KodeverkTabellRepository kodeverkTabellRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        behandling.setStartpunkt(kodeverkTabellRepository.finnStartpunktType(StartpunktType.UTTAKSVILKÅR.getKode()));

        // Act
        List<AksjonspunktDefinisjon> aksjonspunkter = steg.utførSteg(kontekst).getAksjonspunktListe();

        // Assert
        assertThat(aksjonspunkter).doesNotContain(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
    }

    @Test
    public void skal_ikke_fjerne_aksjonspunkter_som_er_utledet_etter_startpunktet() {
        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);

        // Act
        List<AksjonspunktDefinisjon> aksjonspunkter = steg.utførSteg(kontekst).getAksjonspunktListe();

        // Assert
        assertThat(aksjonspunkter).contains(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
        // Må verifisere at startpunkt er før aksjonpunktet for at assert ovenfor skal ha mening
        assertThat(behandling.getStartpunkt()).isEqualTo(StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT);
    }

    @Test
    public void har_overlappende_periode_med_første_stønadsdag() {
        LocalDate nå = LocalDate.now();
        Behandling behandling = mock(Behandling.class);
        Behandlingsresultat behandlingsresultat = mock(Behandlingsresultat.class);
        when(behandling.getBehandlingsresultat()).thenReturn(behandlingsresultat);

        UttakResultatPerioderEntitet uttakResultatPerioderEntitetEksiterende = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitetEksiterende.leggTilPeriode(byggPeriode(nå.plusWeeks(1).plusDays(1), nå.plusWeeks(3)));
        uttakResultatPerioderEntitetEksiterende.leggTilPeriode(byggPeriode(nå, nå.plusWeeks(1)));

        UttakResultatEntitet uttakResultatEntitetEksisterende = UttakResultatEntitet.builder(behandling)
            .medOpprinneligPerioder(uttakResultatPerioderEntitetEksiterende).build();

        UttakResultatPerioderEntitet uttakResultatPerioderEntitetBerørtAv = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitetBerørtAv.leggTilPeriode(byggPeriode(nå.minusDays(3), nå.plusDays(5)));

        UttakResultatEntitet uttakResultatEntitetBerørtAv = UttakResultatEntitet.builder(behandling)
            .medOpprinneligPerioder(uttakResultatPerioderEntitetBerørtAv).build();

        boolean harOverlappendePeriodeMedFørsteStønadsdag = ((KontrollerFaktaRevurderingStegForeldrepengerImpl) steg)
            .harOverlappendePeriodeMedFørsteStønadsdag(uttakResultatEntitetEksisterende, uttakResultatEntitetBerørtAv);
        assertThat(harOverlappendePeriodeMedFørsteStønadsdag).isTrue();
    }

    @Test
    public void har_ikke_overlappende_periode_med_første_stønadsdag() {
        LocalDate nå = LocalDate.now();
        Behandling behandling = mock(Behandling.class);
        Behandlingsresultat behandlingsresultat = mock(Behandlingsresultat.class);
        when(behandling.getBehandlingsresultat()).thenReturn(behandlingsresultat);

        UttakResultatPerioderEntitet uttakResultatPerioderEntitetEksiterende = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitetEksiterende.leggTilPeriode(byggPeriode(nå.plusWeeks(1).plusDays(1), nå.plusWeeks(3)));
        uttakResultatPerioderEntitetEksiterende.leggTilPeriode(byggPeriode(nå, nå.plusWeeks(1)));

        UttakResultatEntitet uttakResultatEntitetEksisterende = UttakResultatEntitet.builder(behandling)
            .medOpprinneligPerioder(uttakResultatPerioderEntitetEksiterende).build();

        UttakResultatPerioderEntitet uttakResultatPerioderEntitetBerørtAv = new UttakResultatPerioderEntitet();
        uttakResultatPerioderEntitetBerørtAv.leggTilPeriode(byggPeriode(nå.minusWeeks(1), nå.minusDays(1)));

        UttakResultatEntitet uttakResultatEntitetBerørtAv = UttakResultatEntitet.builder(behandling)
            .medOpprinneligPerioder(uttakResultatPerioderEntitetBerørtAv).build();

        boolean harOverlappendePeriodeMedFørsteStønadsdag = ((KontrollerFaktaRevurderingStegForeldrepengerImpl) steg)
            .harOverlappendePeriodeMedFørsteStønadsdag(uttakResultatEntitetEksisterende, uttakResultatEntitetBerørtAv);
        assertThat(harOverlappendePeriodeMedFørsteStønadsdag).isFalse();
    }

    @Test
    public void må_nullstille_fordelingsperiode_hvis_ikke_er_endringssøknad() {

        Fagsak fagsak = behandling.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);

        // Act
        steg.utførSteg(kontekst).getAksjonspunktListe();
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregatHvisEksisterer(behandling);
        assertThat(ytelseFordelingAggregat.isPresent()).isTrue();
        YtelseFordelingAggregat aggregat = ytelseFordelingAggregat.get();
        assertThat(aggregat.getOppgittFordeling()).isNotNull();
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder()).isEmpty();
        assertThat(aggregat.getOppgittFordeling().getErAnnenForelderInformert()).isTrue();
    }

    @Test
    public void må_ikke_nullstille_fordelingsperiode_hvis_er_endringssøknad() {
        LocalDate fom = LocalDate.now();
        LocalDate tom = LocalDate.now().plusWeeks(30);

        Behandling revurdering = opprettRevurderingPgaEndringsSøknad(behandling, fom, tom);

        Fagsak fagsak = revurdering.getFagsak();
        // Arrange
        BehandlingLås lås = behandlingRepository.taSkriveLås(revurdering);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(),fagsak.getAktørId(), lås);

        // Act
        steg.utførSteg(kontekst).getAksjonspunktListe();
        Optional<YtelseFordelingAggregat> ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregatHvisEksisterer(revurdering);
        assertThat(ytelseFordelingAggregat.isPresent()).isTrue();
        YtelseFordelingAggregat aggregat = ytelseFordelingAggregat.get();
        assertThat(aggregat.getOppgittFordeling()).isNotNull();
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder()).isNotEmpty();
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder()).size().isEqualTo(1);
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder().get(0).getFom()).isEqualTo(fom);
        assertThat(aggregat.getOppgittFordeling().getOppgittePerioder().get(0).getTom()).isEqualTo(tom);
        assertThat(aggregat.getOppgittFordeling().getErAnnenForelderInformert()).isTrue();
    }

    private UttakResultatPeriodeEntitet byggPeriode(LocalDate fom, LocalDate tom) {
        return new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
    }

    private Behandling opprettRevurderingPgaEndringsSøknad(Behandling originalBehandling, LocalDate fom, LocalDate tom) {
        repositoryProvider.getOpptjeningRepository().lagreOpptjeningsperiode(originalBehandling, LocalDate.now().minusYears(1), LocalDate.now());

        ScenarioMorSøkerForeldrepenger revurderingScenario = ScenarioMorSøkerForeldrepenger.forFødsel()
            .medBehandlingType(BehandlingType.REVURDERING)
            .medRegisterOpplysninger(personopplysningBuilder.build())
            .medOriginalBehandling(originalBehandling, BehandlingÅrsakType.RE_ENDRING_FRA_BRUKER);
        revurderingScenario.removeDodgyDefaultInntektArbeidYTelse();

        revurderingScenario.medAvklarteUttakDatoer(new AvklarteUttakDatoerEntitet(LocalDate.now(), null));

        Behandling revurdering = revurderingScenario.lagre(repositoryProvider);
        //Legg til fordelingsperiode
        OppgittPeriode foreldrepenger = OppgittPeriodeBuilder.ny()
            .medPeriodeType(UttakPeriodeType.FORELDREPENGER)
            .medPeriode(fom, tom)
            .build();
        OppgittFordelingEntitet fordeling = new OppgittFordelingEntitet(Collections.singletonList(foreldrepenger), true);
        repositoryProvider.getYtelsesFordelingRepository().lagre(revurdering, fordeling);

        // Nødvendig å sette aktivt steg for KOFAK revurdering
        internalManipulerBehandling.forceOppdaterBehandlingSteg(revurdering, BehandlingStegType.KONTROLLER_FAKTA);
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(revurdering);
        behandlingRepository.lagre(revurdering, behandlingLås);

        return revurdering;
    }
}
