package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Dekningsgrad;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnUttaksaldoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app.StønadskontoTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AktivitetFordeligDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AktivitetIdentifikatorDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.StønadskontoDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.StønadskontoerDto;

public class StønadskontoTjenesteImplTest {
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private StønadskontoTjenesteImpl tjeneste;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private UttakRepository uttakRepository = new UttakRepositoryImpl(repositoryRule.getEntityManager());
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste = new RelatertBehandlingTjenesteImpl(repositoryProvider);
    private BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste = new BeregnUttaksaldoTjenesteImpl(repositoryProvider, relatertBehandlingTjeneste);

    private static Stønadskonto lagStønadskonto(StønadskontoType fellesperiode, int maxDager) {
        return Stønadskonto.builder().medMaxDager(maxDager).medStønadskontoType(fellesperiode).build();
    }

    private static Stønadskontoberegning lagStønadskontoberegning(Stønadskonto... stønadskontoer) {
        Stønadskontoberegning.Builder builder = Stønadskontoberegning.builder()
            .medRegelEvaluering("asdf")
            .medRegelInput("asdf");
        Stream.of(stønadskontoer)
            .forEach(builder::medStønadskonto);
        return builder.build();
    }

    @Before
    public void setUp() {
        tjeneste = new StønadskontoTjenesteImpl(uttakRepository, repositoryProvider.getFagsakRelasjonRepository(),
            new VirksomhetRepositoryImpl(repositoryRule.getEntityManager()), relatertBehandlingTjeneste, beregnUttaksaldoTjeneste);
    }

    @Test
    public void skalReturnereDtoHvisAlleForutsetningerErTilstede() {
        Behandling behandling = lagBehandling("42");
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FELLESPERIODE, 5));
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.now().plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(periode);
        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultatPerioder);

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        assertThat(stønadskontoerDto).isPresent();
    }

    private UttakResultatPeriodeAktivitetEntitet aktivitet(UttakAktivitetEntitet uttakAktivitet,
                                                           UttakResultatPeriodeEntitet uttakPeriode,
                                                           int trekkdager,
                                                           StønadskontoType stønadskontoType) {
        return new UttakResultatPeriodeAktivitetEntitet.Builder(uttakPeriode, uttakAktivitet)
            .medTrekkdager(trekkdager)
            .medTrekkonto(stønadskontoType)
            .medArbeidsprosent(BigDecimal.TEN)
            .build();
    }

    @Test
    public void skalReturnereTomHvisManglerStønadskontoberegning() {
        Behandling behandling = lagBehandling("42");
        // Uten stønadskontoberegning

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        assertThat(stønadskontoerDto).isNotPresent();
    }

    @Test
    public void skalReturnereTomHvisManglerStønadskontoer() {
        Behandling behandling = lagBehandling("42");
        lagStønadskontoberegning(); // Uten stønadskontoer

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        assertThat(stønadskontoerDto).isNotPresent();
    }

    @Test
    public void skalInneholdeMaksOgFordeltePerArbeidsgiver() {
        Behandling behandling = lagBehandling("42");
        int maxDager = 5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDager));
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.now().plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(periode);

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("virksomhetId").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet1 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakAktivitetEntitet uttakAktivitet2 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("2"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakAktivitetEntitet uttakAktivitet3 = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitet1 = aktivitet(uttakAktivitet1, periode, 1, StønadskontoType.FELLESPERIODE);
        UttakResultatPeriodeAktivitetEntitet aktivitet2 = aktivitet(uttakAktivitet2, periode, 5, StønadskontoType.FELLESPERIODE);
        UttakResultatPeriodeAktivitetEntitet aktivitet3 = aktivitet(uttakAktivitet3, periode, 10, StønadskontoType.FELLESPERIODE);
        periode.leggTilAktivitet(aktivitet1);
        periode.leggTilAktivitet(aktivitet2);
        periode.leggTilAktivitet(aktivitet3);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultatPerioder);

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        assertThat(stønadskontoerDto).isPresent();
        assertThat(stønadskontoerDto.get().getStonadskontoer().keySet()).hasSize(1);
        StønadskontoDto stønadskontoDto = stønadskontoerDto.get().getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertThat(stønadskontoDto.getAktivitetFordeligDtoList()).hasSize(3);
        assertThat(stønadskontoDto.getMaxDager()).isEqualTo(maxDager);
        assertThat(hentDtoForAktivitet(uttakAktivitet1, stønadskontoDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet1.getTrekkdager());
        assertThat(hentDtoForAktivitet(uttakAktivitet2, stønadskontoDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet2.getTrekkdager());
        assertThat(hentDtoForAktivitet(uttakAktivitet3, stønadskontoDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet3.getTrekkdager());
    }

    @Test
    public void skalReturnereTomDtoHvisBehandlingHarStønadskontoberegningMenIkkeUttaksresultat() {
        Behandling behandling = lagBehandling("42");
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FELLESPERIODE, 5));
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        assertThat(stønadskontoerDto).isPresent();
        assertThat(stønadskontoerDto.get().getStonadskontoer()).hasSize(0);
    }

    @Test
    public void skalSummereFordelteDager() {
        Behandling behandling = lagBehandling("42");
        int maxDager = 5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDager));
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);

        UttakResultatPeriodeEntitet periode1 = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.now().plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPeriodeEntitet periode2 = new UttakResultatPeriodeEntitet.Builder(periode1.getTom().plusDays(1), periode1.getTom().plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .build();

        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(periode1);
        uttakResultatPerioder.leggTilPeriode(periode2);

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("virksomhetId").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhet);

        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitet1 = aktivitet(uttakAktivitet, periode1, 1, StønadskontoType.FELLESPERIODE);
        UttakResultatPeriodeAktivitetEntitet aktivitet2 = aktivitet(uttakAktivitet, periode2, 5, StønadskontoType.FELLESPERIODE);
        periode1.leggTilAktivitet(aktivitet1);
        periode2.leggTilAktivitet(aktivitet2);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultatPerioder);

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        StønadskontoDto stønadskontoDto = stønadskontoerDto.get().getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertThat(stønadskontoDto.getAktivitetFordeligDtoList()).hasSize(1);
        assertThat(hentDtoForAktivitet(uttakAktivitet, stønadskontoDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet1.getTrekkdager() + aktivitet2.getTrekkdager());
    }

    @Test
    public void skalReturnereForAlleStønadskontoTyperSelvOmIkkeBruktIUttak() {
        Behandling behandling = lagBehandling("42");
        int maxDagerFPFF = 5;
        int maxDagerMødrekvote = 10;
        int maxDagerFelles = 60;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMødrekvote), lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFelles));
        repositoryProvider.getFagsakRelasjonRepository().lagre(behandling, stønadskontoberegning);

        UttakResultatPeriodeEntitet periode1 = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.now().plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPeriodeEntitet periode2 = new UttakResultatPeriodeEntitet.Builder(periode1.getTom().plusDays(1), periode1.getTom().plusDays(10))
            .medPeriodeResultat(PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .build();


        UttakResultatPerioderEntitet uttakResultatPerioder = new UttakResultatPerioderEntitet();
        uttakResultatPerioder.leggTilPeriode(periode1);
        uttakResultatPerioder.leggTilPeriode(periode2);
        UttakAktivitetEntitet uttakAktivitet1 = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build();
        UttakAktivitetEntitet uttakAktivitet2 = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitet1 = aktivitet(uttakAktivitet1, periode1, 1, StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        UttakResultatPeriodeAktivitetEntitet aktivitet2 = aktivitet(uttakAktivitet2, periode1, 5, StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        UttakResultatPeriodeAktivitetEntitet aktivitet3 = aktivitet(uttakAktivitet1, periode2, 10, StønadskontoType.MØDREKVOTE);
        UttakResultatPeriodeAktivitetEntitet aktivitet4 = aktivitet(uttakAktivitet2, periode2, 10, StønadskontoType.MØDREKVOTE);
        periode1.leggTilAktivitet(aktivitet1);
        periode1.leggTilAktivitet(aktivitet2);
        periode2.leggTilAktivitet(aktivitet3);
        periode2.leggTilAktivitet(aktivitet4);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(behandling, uttakResultatPerioder);

        Optional<StønadskontoerDto> stønadskontoerDto = tjeneste.lagStønadskontoerDto(behandling);

        StønadskontoDto fpffDto = stønadskontoerDto.get().getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertThat(fpffDto.getAktivitetFordeligDtoList()).hasSize(2);
        assertThat(fpffDto.getMaxDager()).isEqualTo(maxDagerFPFF);
        assertThat(hentDtoForAktivitet(uttakAktivitet1, fpffDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet1.getTrekkdager());
        assertThat(hentDtoForAktivitet(uttakAktivitet2, fpffDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet2.getTrekkdager());

        StønadskontoDto mødrekvoteDto = stønadskontoerDto.get().getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertThat(mødrekvoteDto.getAktivitetFordeligDtoList()).hasSize(2);
        assertThat(mødrekvoteDto.getMaxDager()).isEqualTo(maxDagerMødrekvote);
        assertThat(hentDtoForAktivitet(uttakAktivitet1, mødrekvoteDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet3.getTrekkdager());
        assertThat(hentDtoForAktivitet(uttakAktivitet2, mødrekvoteDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(aktivitet4.getTrekkdager());

        StønadskontoDto fellesPeriodeDto = stønadskontoerDto.get().getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertThat(fellesPeriodeDto.getAktivitetFordeligDtoList()).hasSize(2);
        assertThat(fellesPeriodeDto.getMaxDager()).isEqualTo(maxDagerFelles);
        assertThat(hentDtoForAktivitet(uttakAktivitet1, fellesPeriodeDto.getAktivitetFordeligDtoList()).getFordelteDager()).isZero();
        assertThat(hentDtoForAktivitet(uttakAktivitet2, fellesPeriodeDto.getAktivitetFordeligDtoList()).getFordelteDager()).isZero();
    }

    @Test
    public void skalTesteAnnenPart() {
        Behandling morsBehandling = lagBehandling("42");
        int maxDagerFelles = 16;
        int maxDagerFK = 15;
        int maxDagerMK = 15;

        LocalDate fom = LocalDate.of(2018, Month.MAY, 28);
        LocalDate tom = LocalDate.of(2018, Month.JUNE, 8);

        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFelles),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder1 = new UttakResultatPerioderEntitet();
        uttakResultatPerioder1.leggTilPeriode(periode);

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder().medOrgnr("virksomhetId").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhet);


        UttakAktivitetEntitet uttakAktivitet1 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("1"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakAktivitetEntitet uttakAktivitet2 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("2"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitet1 = aktivitet(uttakAktivitet1, periode, 5, StønadskontoType.FELLESPERIODE);
        UttakResultatPeriodeAktivitetEntitet aktivitet2 = aktivitet(uttakAktivitet2, periode, 10, StønadskontoType.FELLESPERIODE);
        periode.leggTilAktivitet(aktivitet1);
        periode.leggTilAktivitet(aktivitet2);

        Behandlingsresultat behandlingsresultat = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultat).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultat);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioder1);

        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);

        Behandling farsBehandling = lagBehandling("43");
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        repositoryRule.getRepository().flushAndClear();


        UttakResultatPeriodeEntitet periodeFar = new UttakResultatPeriodeEntitet.Builder(fom.plusDays(2), tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        UttakResultatPerioderEntitet uttakResultatPerioder2 = new UttakResultatPerioderEntitet();
        uttakResultatPerioder2.leggTilPeriode(periodeFar);

        UttakAktivitetEntitet uttakAktivitetFar1 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("4"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakAktivitetEntitet uttakAktivitetFar2 = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref("5"))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();

        UttakResultatPeriodeAktivitetEntitet aktivitetFar1 = aktivitet(uttakAktivitetFar1, periode, 4, StønadskontoType.FEDREKVOTE);
        UttakResultatPeriodeAktivitetEntitet aktivitetFar2 = aktivitet(uttakAktivitetFar2, periode, 5, StønadskontoType.FEDREKVOTE);
        periodeFar.leggTilAktivitet(aktivitetFar1);
        periodeFar.leggTilAktivitet(aktivitetFar2);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioder2);

        // Act
        Optional<StønadskontoerDto> resultat = tjeneste.lagStønadskontoerDto(farsBehandling);

        // Assert
        assertThat(resultat).isPresent();
        StønadskontoerDto stønadskontoer = resultat.get();

        StønadskontoDto fellesPeriodeDto = stønadskontoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertThat(fellesPeriodeDto.getAktivitetFordeligDtoList()).hasSize(2);
        assertThat(fellesPeriodeDto.getAktivitetFordelingAnnenPart()).hasSize(2);
        assertThat(fellesPeriodeDto.getMaxDager()).isEqualTo(maxDagerFelles);
        assertThat(hentDtoForAktivitet(uttakAktivitet1, fellesPeriodeDto.getAktivitetFordelingAnnenPart()).getFordelteDager()).isEqualTo(1);
        assertThat(hentDtoForAktivitet(uttakAktivitet2, fellesPeriodeDto.getAktivitetFordelingAnnenPart()).getFordelteDager()).isEqualTo(2);

        StønadskontoDto fedrekvoteDto = stønadskontoer.getStonadskontoer().get(StønadskontoType.FEDREKVOTE.getKode());
        assertThat(fedrekvoteDto.getAktivitetFordeligDtoList()).hasSize(2);
        assertThat(fedrekvoteDto.getAktivitetFordelingAnnenPart()).hasSize(0);
        assertThat(fedrekvoteDto.getMaxDager()).isEqualTo(maxDagerFK);
        assertThat(hentDtoForAktivitet(uttakAktivitetFar1, fedrekvoteDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(4);
        assertThat(hentDtoForAktivitet(uttakAktivitetFar2, fedrekvoteDto.getAktivitetFordeligDtoList()).getFordelteDager()).isEqualTo(5);

        assertThat(stønadskontoer.getMaksDato()).isEqualTo(LocalDate.of(2018, Month.JULY, 16));

    }

    private AktivitetFordeligDto hentDtoForAktivitet(UttakAktivitetEntitet uttakAktivitet, List<AktivitetFordeligDto> aktiviter) {
        return aktiviter
            .stream()
            .filter(s -> Objects.equals(new AktivitetIdentifikatorDto(uttakAktivitet, null), s.getAktivitetIdentifikator()))
            .findFirst()
            .orElse(null);
    }

    private Behandling lagBehandling(String aktørId) {
        final Behandling behandling = Behandling
            .forFørstegangssøknad(Fagsak
                .opprettNy(FagsakYtelseType.FORELDREPENGER,
                    new NavBrukerBuilder()
                        .medAktørId(new AktørId(aktørId))
                        .build()
                ))
            .build();
        Behandlingsresultat.opprettFor(behandling);
        repositoryRule.getEntityManager().persist(behandling.getFagsak().getNavBruker());
        repositoryRule.getEntityManager().persist(behandling.getFagsak());
        repositoryRule.getEntityManager().flush();
        final BehandlingLås lås = repositoryProvider.getBehandlingLåsRepository().taLås(behandling.getId());
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
        return behandling;
    }
}
