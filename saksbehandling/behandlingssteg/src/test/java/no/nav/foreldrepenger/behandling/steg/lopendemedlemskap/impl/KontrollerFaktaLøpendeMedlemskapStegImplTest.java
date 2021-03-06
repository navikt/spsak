package no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.impl;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.behandling.steg.lopendemedlemskap.impl.KontrollerFaktaLøpendeMedlemskapStegImpl.FPSAK_LØPENDE_MEDLEMSKAP;
import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AVKLAR_FORTSATT_MEDLEMSKAP;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.finn.unleash.FakeUnleash;
import no.nav.foreldrepenger.behandlingskontroll.BehandleStegResultat;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapPerioderBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.medlem.api.UtledVurderingsdatoerForMedlemskapTjeneste;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class KontrollerFaktaLøpendeMedlemskapStegImplTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private ResultatRepositoryProvider resultatRepositoryProvider = new ResultatRepositoryProviderImpl(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private MedlemskapRepository medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    private VirksomhetRepository virksomhetRepository = repositoryProvider.getVirksomhetRepository();
    private PersonopplysningRepository personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();

    @Inject
    private UtledVurderingsdatoerForMedlemskapTjeneste utlederTjeneste;
    private KontrollerFaktaLøpendeMedlemskapStegImpl steg;
    private FakeUnleash unleash = new FakeUnleash();

    @Before
    public void setUp() {
        steg = new KontrollerFaktaLøpendeMedlemskapStegImpl(unleash, utlederTjeneste, repositoryProvider);
    }

    @Test
    public void skal_kontrollere_fakta_for_løpende_medlemskap_når_feature_er_på() {
        unleash.enable(FPSAK_LØPENDE_MEDLEMSKAP);
        // Arrange
        LocalDate datoMedEndring = LocalDate.now().plusDays(10);
        LocalDate ettÅrSiden = LocalDate.now().minusYears(1);
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        RegistrertMedlemskapPerioder periode = opprettPeriode(ettÅrSiden, iDag, MedlemskapDekningType.FTL_2_6);
        scenario.leggTilMedlemskapPeriode(periode);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        avslutterBehandlingOgFagsak(behandling);

        Behandling revudering = opprettRevudering(behandling);

        oppdaterMedlem(datoMedEndring, periode, revudering);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(revudering);
        Fagsak fagsak = revudering.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);
        assertThat(behandleStegResultat.getAksjonspunktListe()).containsExactly(AVKLAR_FORTSATT_MEDLEMSKAP);
    }

    @Test
    public void skal_ikke_kontrollere_fakta_for_løpende_medlemskap_når_feature_er_av() {
        unleash.disableAll();
        // Arrange
        LocalDate datoMedEndring = LocalDate.now().plusDays(10);
        LocalDate ettÅrSiden = LocalDate.now().minusYears(1);
        LocalDate iDag = LocalDate.now();
        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør();
        RegistrertMedlemskapPerioder periode = opprettPeriode(ettÅrSiden, iDag, MedlemskapDekningType.FTL_2_6);
        scenario.leggTilMedlemskapPeriode(periode);
        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);
        avslutterBehandlingOgFagsak(behandling);

        Behandling revudering = opprettRevudering(behandling);

        oppdaterMedlem(datoMedEndring, periode, revudering);

        // Act
        BehandlingLås lås = behandlingRepository.taSkriveLås(revudering);
        Fagsak fagsak = revudering.getFagsak();
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(fagsak.getId(), fagsak.getAktørId(), lås);

        BehandleStegResultat behandleStegResultat = steg.utførSteg(kontekst);

        assertThat(behandleStegResultat.getAksjonspunktListe()).isEmpty();
    }

    private RegistrertMedlemskapPerioder opprettPeriode(LocalDate fom, LocalDate tom, MedlemskapDekningType dekningType) {
        RegistrertMedlemskapPerioder periode = new MedlemskapPerioderBuilder()
            .medDekningType(dekningType)
            .medMedlemskapType(MedlemskapType.FORELOPIG)
            .medPeriode(fom, tom)
            .medMedlId(1L)
            .build();
        return periode;
    }

    private Behandling opprettRevudering(Behandling behandling) {
        BehandlingÅrsak.Builder revurderingÅrsak = BehandlingÅrsak.builder(BehandlingÅrsakType.RE_FEIL_ELLER_ENDRET_FAKTA)
            .medOriginalBehandling(behandling);

        Behandling revudering = Behandling.fraTidligereBehandling(behandling, BehandlingType.REVURDERING)
            .medBehandlingÅrsak(revurderingÅrsak).build();

        behandlingRepository.lagre(revudering, behandlingRepository.taSkriveLås(revudering));
        medlemskapRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revudering);
        inntektArbeidYtelseRepository.kopierGrunnlagFraEksisterendeBehandling(behandling, revudering);
        personopplysningRepository.kopierGrunnlagFraEksisterendeBehandlingForRevurdering(behandling, revudering);
        return revudering;
    }

    private void oppdaterMedlem(LocalDate datoMedEndring, RegistrertMedlemskapPerioder periode, Behandling behandling) {
        RegistrertMedlemskapPerioder nyPeriode = new MedlemskapPerioderBuilder()
            .medPeriode(datoMedEndring, null)
            .medDekningType(MedlemskapDekningType.FULL)
            .medMedlemskapType(MedlemskapType.ENDELIG)
            .medMedlId(2L)
            .build();
        medlemskapRepository.lagreMedlemskapRegisterOpplysninger(behandling, asList(periode, nyPeriode));
    }

    private void avslutterBehandlingOgFagsak(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        Behandlingsresultat.Builder behandlingsresultatBuilder = Behandlingsresultat.builderEndreEksisterende(behandlingRepository.hentResultat(behandling.getId()));
        Behandlingsresultat behandlingsresultat = behandlingsresultatBuilder.buildFor(behandling);

        behandlingRepository.lagre(behandlingsresultat.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandlingsresultat, lås);
        resultatRepositoryProvider.getUttakRepository().lagreOpprinneligUttakResultatPerioder(behandlingsresultat, lagUttaksPeriode());

        repositoryProvider.getBehandlingskontrollRepository().avsluttBehandling(behandling.getId());
    }

    private UttakResultatPerioderEntitet lagUttaksPeriode() {
        LocalDate idag = LocalDate.now();
        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(idag, idag.plusDays(6))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .oppdatertOpplysningerNå()
            .medOrgnr("123")
            .medNavn("Statoil").build();
        UttakAktivitetEntitet uttakAktivtet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("321"))
            .build();
        virksomhetRepository.lagre(virksomhet);
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivtet)
            .medUtbetalingsprosent(BigDecimal.valueOf(100L))
            .medArbeidsprosent(BigDecimal.valueOf(100L))
            .medErSøktGradering(true)
            .build();
        periode.leggTilAktivitet(periodeAktivitet);
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(periode);
        return perioder;
    }

}
