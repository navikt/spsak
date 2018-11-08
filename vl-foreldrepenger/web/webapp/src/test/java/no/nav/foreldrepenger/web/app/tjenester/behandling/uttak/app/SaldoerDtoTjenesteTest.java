package no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
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
import no.nav.foreldrepenger.domene.uttak.saldo.StønadskontoSaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.saldo.impl.StønadskontoSaldoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.BeregnUttaksaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.BeregnUttaksaldoTjenesteImpl;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.impl.RelatertBehandlingTjenesteImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TrekkdagerUtregningUtil;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.dto.AktivitetIdentifikatorDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.AktivitetSaldoDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.StønadskontoDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.uttak.stønadskonto.dto.SaldoerDto;
import no.nav.vedtak.util.Tuple;

public class SaldoerDtoTjenesteTest {


    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private SaldoerDtoTjenesteImpl tjeneste;
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private UttakRepository uttakRepository = new UttakRepositoryImpl(repositoryRule.getEntityManager());
    private RelatertBehandlingTjeneste relatertBehandlingTjeneste = new RelatertBehandlingTjenesteImpl(repositoryProvider);
    private BeregnUttaksaldoTjeneste beregnUttaksaldoTjeneste = new BeregnUttaksaldoTjenesteImpl(repositoryProvider, relatertBehandlingTjeneste);
    private StønadskontoSaldoTjeneste stønadskontoSaldoTjeneste = new StønadskontoSaldoTjenesteImpl(repositoryProvider, beregnUttaksaldoTjeneste, relatertBehandlingTjeneste);

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
        tjeneste = new SaldoerDtoTjenesteImpl(stønadskontoSaldoTjeneste, repositoryProvider);
    }

    @Test
    public void riktig_saldo_for_mors_uttaksplan() {

        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("48", RelasjonsRolleType.MORA);
        VirksomhetEntitet virksomhetForMor = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor);
        UttakAktivitetEntitet uttakAktivitetForMor = lagUttakAktivitet(virksomhetForMor);
        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.plusWeeks(6), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);


        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(morsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (6*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (10*5));
        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(16 /* forbrukte uker */ + 9 /* saldo MK */ + 6 /* saldo FP */).minusDays(1));
    }


    @Test
    public void riktig_saldo_for_mors_dersom_for_mange_dager_blir_trukket() {

        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("48", RelasjonsRolleType.MORA);
        VirksomhetEntitet virksomhetForMor = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor);
        UttakAktivitetEntitet uttakAktivitetForMor = lagUttakAktivitet(virksomhetForMor);
        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato, fødseldato.plusWeeks(15).minusDays(1), StønadskontoType.MØDREKVOTE);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.plusWeeks(15), fødseldato.plusWeeks(15+17).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);

        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(morsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (15*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (17*5));
        //Info: ikke relevant å teste maxdato da denne datoen ikke skal være satt dersom det finnes manuelle perioder som er eneste måten en kan få en negativ saldo.
    }

    @Test
    public void riktig_saldo_for_mors_uttaksplan_ved_flerbarnsdager() {

        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("48", RelasjonsRolleType.MORA);
        VirksomhetEntitet virksomhetForMor = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor);
        UttakAktivitetEntitet uttakAktivitetForMor = lagUttakAktivitet(virksomhetForMor);
        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.plusWeeks(6), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE, true, true);

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 33*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        int maxDagerFlerbarn = 17 * 5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK),
            lagStønadskonto(StønadskontoType.FLERBARNSDAGER, maxDagerFlerbarn));

        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);


        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(morsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (6*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (10*5));
        StønadskontoDto fbDto = saldoer.getStonadskontoer().get(StønadskontoType.FLERBARNSDAGER.getKode());
        assertKonto(fbDto, maxDagerFlerbarn, maxDagerFlerbarn - (10*5));
        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(16 /* forbrukte uker */ + 9 /* saldo MK */ + 23 /* saldo FP */).minusDays(1));
    }

    @Test
    public void riktig_saldo_for_mors_uttaksplan_med_flere_arbeidsforhold() {

        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("48", RelasjonsRolleType.MORA);

        VirksomhetEntitet virksomhetForMor1 = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor1);
        VirksomhetEntitet virksomhetForMor2 = new VirksomhetEntitet.Builder().medOrgnr("456").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor2);
        UttakAktivitetEntitet uttakAktivitetForMor1 = lagUttakAktivitet(virksomhetForMor1);
        UttakAktivitetEntitet uttakAktivitetForMor2 = lagUttakAktivitet(virksomhetForMor2);

        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();
        lagPeriode(uttakResultatPerioderForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL, false, false,
            new Tuple(uttakAktivitetForMor1, Optional.empty()), new Tuple(uttakAktivitetForMor2, Optional.empty()));
        lagPeriode(uttakResultatPerioderForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE, false, false,
            new Tuple(uttakAktivitetForMor1, Optional.empty()), new Tuple(uttakAktivitetForMor2, Optional.empty()));
        lagPeriode(uttakResultatPerioderForMor, fødseldato.plusWeeks(6), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE, false, false,
            new Tuple(uttakAktivitetForMor1, Optional.of(25)), new Tuple(uttakAktivitetForMor2, Optional.empty()));

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);

        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(morsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertThat(fpffDto.getMaxDager()).isEqualTo(maxDagerFPFF);
        assertThat(fpffDto.getAktivitetSaldoDtoList()).hasSize(2);
        assertThat(fpffDto.getAktivitetSaldoDtoList().get(0).getSaldo()).isEqualTo(0);
        assertThat(fpffDto.getAktivitetSaldoDtoList().get(1).getSaldo()).isEqualTo(0);

        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertThat(mkDto.getMaxDager()).isEqualTo(maxDagerMK);
        assertThat(mkDto.getAktivitetSaldoDtoList()).hasSize(2);
        assertThat(mkDto.getAktivitetSaldoDtoList().get(0).getSaldo()).isEqualTo(maxDagerMK - (6*5));
        assertThat(mkDto.getAktivitetSaldoDtoList().get(1).getSaldo()).isEqualTo(maxDagerMK - (6*5));

        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertThat(fpDto.getMaxDager()).isEqualTo(maxDagerFP);
        assertThat(fpDto.getAktivitetSaldoDtoList()).hasSize(2);
        Optional<AktivitetSaldoDto> aktivitetSaldo1 = finnRiktigAktivitetSaldo(fpDto.getAktivitetSaldoDtoList(), uttakAktivitetForMor1);
        Optional<AktivitetSaldoDto> aktivitetSaldo2 = finnRiktigAktivitetSaldo(fpDto.getAktivitetSaldoDtoList(), uttakAktivitetForMor2);
        assertThat(aktivitetSaldo1).isPresent();
        assertThat(aktivitetSaldo2).isPresent();
        assertThat(aktivitetSaldo1.get().getSaldo()).isEqualTo(maxDagerFP - 25);
        assertThat(aktivitetSaldo2.get().getSaldo()).isEqualTo(maxDagerFP - (10*5));

        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(16 /* forbrukte uker */ + 9 /* saldo MK */ + 11 /* saldo FP */).minusDays(1));
    }

    @Test
    public void riktig_saldo_med_uttak_på_både_mor_og_fars_uten_overlapp() {
        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("46", RelasjonsRolleType.MORA);
        VirksomhetEntitet virksomhetForMor = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor);
        UttakAktivitetEntitet uttakAktivitetForMor = lagUttakAktivitet(virksomhetForMor);
        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE);

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);

        //
        // --- Fars behandling
        //
        Behandling farsBehandling = lagBehandling("47", RelasjonsRolleType.FARA);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        VirksomhetEntitet virksomhetForFar = new VirksomhetEntitet.Builder().medOrgnr("456").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForFar);

        UttakAktivitetEntitet uttakAktivitetForFar = lagUttakAktivitet(virksomhetForFar);
        UttakResultatPerioderEntitet uttakResultatPerioderForFar = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForFar, uttakAktivitetForFar, fødseldato.plusWeeks(6), fødseldato.plusWeeks(18).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForFar = farsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForFar).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForFar);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);
        farsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(farsBehandling);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);



        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(farsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (6*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (12*5));
        StønadskontoDto fkDto = saldoer.getStonadskontoer().get(StønadskontoType.FEDREKVOTE.getKode());
        assertKonto(fkDto, maxDagerFK, maxDagerFK);
        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(18 /* forbrukte uker */ + 4 /* saldo FP */ + 15 /* saldo FK*/).minusDays(1));
    }

    @Test
    public void riktig_saldo_med_uttak_på_både_mor_og_fars_med_overlapp() {
        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("46", RelasjonsRolleType.MORA);
        VirksomhetEntitet virksomhetForMor = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor);
        UttakAktivitetEntitet uttakAktivitetForMor = lagUttakAktivitet(virksomhetForMor);
        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.plusWeeks(6), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);

        //
        // --- Fars behandling
        //
        Behandling farsBehandling = lagBehandling("47", RelasjonsRolleType.FARA);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        VirksomhetEntitet virksomhetForFar = new VirksomhetEntitet.Builder().medOrgnr("456").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForFar);

        UttakAktivitetEntitet uttakAktivitetForFar = lagUttakAktivitet(virksomhetForFar);
        UttakResultatPerioderEntitet uttakResultatPerioderForFar = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForFar, uttakAktivitetForFar, fødseldato.plusWeeks(11), fødseldato.plusWeeks(21).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForFar = farsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForFar).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForFar);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);
        farsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(farsBehandling);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);



        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(farsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (6*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (15*5));
        StønadskontoDto fkDto = saldoer.getStonadskontoer().get(StønadskontoType.FEDREKVOTE.getKode());
        assertKonto(fkDto, maxDagerFK, maxDagerFK);
        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(21 /* forbrukte uker */ + 1 /* saldo FP */ + 15 /* saldo FK*/).minusDays(1));
    }

    @Test
    public void riktig_saldo_med_uttak_på_både_mor_og_fars_med_overlapp_og_samtidig_uttak() {
        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("46", RelasjonsRolleType.MORA);
        VirksomhetEntitet virksomhetForMor = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor);
        UttakAktivitetEntitet uttakAktivitetForMor = lagUttakAktivitet(virksomhetForMor);
        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE);
        lagPeriode(uttakResultatPerioderForMor, uttakAktivitetForMor, fødseldato.plusWeeks(6), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);

        //
        // --- Fars behandling
        //
        Behandling farsBehandling = lagBehandling("47", RelasjonsRolleType.FARA);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        VirksomhetEntitet virksomhetForFar = new VirksomhetEntitet.Builder().medOrgnr("456").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForFar);

        UttakAktivitetEntitet uttakAktivitetForFar = lagUttakAktivitet(virksomhetForFar);
        UttakResultatPerioderEntitet uttakResultatPerioderForFar = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForFar, uttakAktivitetForFar, fødseldato.plusWeeks(11), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE, true, false);

        Behandlingsresultat behandlingsresultatForFar = farsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForFar).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForFar);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);
        farsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(farsBehandling);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);

        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(farsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (6*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (15*5));
        StønadskontoDto fkDto = saldoer.getStonadskontoer().get(StønadskontoType.FEDREKVOTE.getKode());
        assertKonto(fkDto, maxDagerFK, maxDagerFK);
        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(16 /* forbrukte uker */ + 1 /* saldo FP */ + 15 /* saldo FK*/).minusDays(1));
    }

    @Test
    public void riktig_saldo_med_uttak_på_både_mor_og_fars_med_overlapp_og_gradering_på_motpart() {
        LocalDate fødseldato = LocalDate.of(2018, Month.MAY, 1);

        //
        // --- Mors behandling
        //
        Behandling morsBehandling = lagBehandling("46", RelasjonsRolleType.MORA);

        VirksomhetEntitet virksomhetForMor1 = new VirksomhetEntitet.Builder().medOrgnr("123").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor1);
        VirksomhetEntitet virksomhetForMor2 = new VirksomhetEntitet.Builder().medOrgnr("789").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForMor2);
        UttakAktivitetEntitet uttakAktivitetForMor1 = lagUttakAktivitet(virksomhetForMor1);
        UttakAktivitetEntitet uttakAktivitetForMor2 = lagUttakAktivitet(virksomhetForMor2);

        UttakResultatPerioderEntitet uttakResultatPerioderForMor = new UttakResultatPerioderEntitet();
        lagPeriode(uttakResultatPerioderForMor, fødseldato.minusWeeks(3), fødseldato.minusDays(1), StønadskontoType.FORELDREPENGER_FØR_FØDSEL, false, false,
            new Tuple(uttakAktivitetForMor1, Optional.empty()), new Tuple(uttakAktivitetForMor2, Optional.empty()));
        lagPeriode(uttakResultatPerioderForMor, fødseldato, fødseldato.plusWeeks(6).minusDays(1), StønadskontoType.MØDREKVOTE, false, false,
            new Tuple(uttakAktivitetForMor1, Optional.empty()), new Tuple(uttakAktivitetForMor2, Optional.empty()));
        lagPeriode(uttakResultatPerioderForMor, fødseldato.plusWeeks(6), fødseldato.plusWeeks(16).minusDays(1), StønadskontoType.FELLESPERIODE, false, false,
            new Tuple(uttakAktivitetForMor1, Optional.of(10)), new Tuple(uttakAktivitetForMor2, Optional.empty()));


        Behandlingsresultat behandlingsresultatForMor = morsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForMor).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForMor);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(morsBehandling, uttakResultatPerioderForMor);
        morsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(morsBehandling);

        //
        // --- Fars behandling
        //
        Behandling farsBehandling = lagBehandling("47", RelasjonsRolleType.FARA);
        repositoryProvider.getFagsakRelasjonRepository().kobleFagsaker(morsBehandling.getFagsak(), farsBehandling.getFagsak());
        VirksomhetEntitet virksomhetForFar = new VirksomhetEntitet.Builder().medOrgnr("456").oppdatertOpplysningerNå().build();
        repositoryRule.getRepository().lagre(virksomhetForFar);

        UttakAktivitetEntitet uttakAktivitetForFar = lagUttakAktivitet(virksomhetForFar);
        UttakResultatPerioderEntitet uttakResultatPerioderForFar = new UttakResultatPerioderEntitet();

        lagPeriode(uttakResultatPerioderForFar, uttakAktivitetForFar, fødseldato.plusWeeks(11), fødseldato.plusWeeks(21).minusDays(1), StønadskontoType.FELLESPERIODE);

        Behandlingsresultat behandlingsresultatForFar = farsBehandling.getBehandlingsresultat();
        Behandlingsresultat.builderEndreEksisterende(behandlingsresultatForFar).medBehandlingResultatType(BehandlingResultatType.INNVILGET);
        repositoryRule.getRepository().lagre(behandlingsresultatForFar);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);
        farsBehandling.avsluttBehandling();
        repositoryRule.getRepository().lagre(farsBehandling);

        uttakRepository.lagreOpprinneligUttakResultatPerioder(farsBehandling, uttakResultatPerioderForFar);


        //
        // --- Stønadskontoer
        //
        int maxDagerFPFF = 3*5;
        int maxDagerFP = 16*5;
        int maxDagerFK = 15*5;
        int maxDagerMK = 15*5;
        final Stønadskontoberegning stønadskontoberegning = lagStønadskontoberegning(
            lagStønadskonto(StønadskontoType.FORELDREPENGER_FØR_FØDSEL, maxDagerFPFF),
            lagStønadskonto(StønadskontoType.FELLESPERIODE, maxDagerFP),
            lagStønadskonto(StønadskontoType.FEDREKVOTE, maxDagerFK),
            lagStønadskonto(StønadskontoType.MØDREKVOTE, maxDagerMK));
        repositoryProvider.getFagsakRelasjonRepository().lagre(morsBehandling, stønadskontoberegning);



        // Act
        SaldoerDto saldoer = tjeneste.lagStønadskontoerDto(farsBehandling);

        // Assert
        StønadskontoDto fpffDto = saldoer.getStonadskontoer().get(StønadskontoType.FORELDREPENGER_FØR_FØDSEL.getKode());
        assertKonto(fpffDto, maxDagerFPFF, 0);
        StønadskontoDto mkDto = saldoer.getStonadskontoer().get(StønadskontoType.MØDREKVOTE.getKode());
        assertKonto(mkDto, maxDagerMK, maxDagerMK - (6*5));
        StønadskontoDto fpDto = saldoer.getStonadskontoer().get(StønadskontoType.FELLESPERIODE.getKode());
        assertKonto(fpDto, maxDagerFP, maxDagerFP - (5 /* gradert uttak 20% 5 uker */ + 50 /* fullt uttak 10 uker */));
        StønadskontoDto fkDto = saldoer.getStonadskontoer().get(StønadskontoType.FEDREKVOTE.getKode());
        assertKonto(fkDto, maxDagerFK, maxDagerFK);
        assertThat(saldoer.getMaksDatoUttak().isPresent()).isTrue();
        assertThat(saldoer.getMaksDatoUttak().get()).isEqualTo(fødseldato.plusWeeks(21 /* forbrukte uker */ + 5 /* saldo FP */ + 15 /* saldo FK */).minusDays(1));
    }

    private Optional<AktivitetSaldoDto> finnRiktigAktivitetSaldo(List<AktivitetSaldoDto> aktivitetSaldoer, UttakAktivitetEntitet aktivitetEntitet) {
        return aktivitetSaldoer.stream().filter(as -> {
            AktivitetIdentifikatorDto aktId = as.getAktivitetIdentifikator();
            return aktId.getUttakArbeidType().equals(aktivitetEntitet.getUttakArbeidType()) &&
                aktId.getArbeidsforholdOrgnr().equals(aktivitetEntitet.getArbeidsforholdOrgnr()) &&
                aktId.getArbeidsforholdId().equals(aktivitetEntitet.getArbeidsforholdId());
        }).findFirst();
    }



    private void assertKonto(StønadskontoDto stønadskontoDto, int maxDager, int saldo) {
        assertThat(stønadskontoDto.getMaxDager()).isEqualTo(maxDager);
        assertThat(stønadskontoDto.getAktivitetSaldoDtoList()).hasSize(1);
        assertThat(stønadskontoDto.getSaldo()).isEqualTo(saldo);
        assertThat(stønadskontoDto.getAktivitetSaldoDtoList().get(0).getSaldo()).isEqualTo(saldo);

    }

    private void lagPeriode(UttakResultatPerioderEntitet uttakResultatPerioder,
                            UttakAktivitetEntitet uttakAktivitet,
                            LocalDate fom, LocalDate tom,
                            StønadskontoType stønadskontoType) {
        lagPeriode(uttakResultatPerioder, uttakAktivitet, fom, tom, stønadskontoType, false, false);

    }

    private void lagPeriode(UttakResultatPerioderEntitet uttakResultatPerioder,
                            UttakAktivitetEntitet uttakAktivitet,
                            LocalDate fom, LocalDate tom,
                            StønadskontoType stønadskontoType,
                            boolean samtidigUttak,
                            boolean flerbarnsdager) {
        lagPeriode(uttakResultatPerioder, fom, tom, stønadskontoType, samtidigUttak, flerbarnsdager, new Tuple(uttakAktivitet, Optional.empty()));
    }

    private void lagPeriode(UttakResultatPerioderEntitet uttakResultatPerioder,
                            LocalDate fom, LocalDate tom,
                            StønadskontoType stønadskontoType,
                            boolean samtidigUttak,
                            boolean flerbarnsdager,
                            Tuple<UttakAktivitetEntitet, Optional<Integer>>... aktiviteter) {

        UttakResultatPeriodeEntitet periode = new UttakResultatPeriodeEntitet.Builder(fom, tom)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .medSamtidigUttak(samtidigUttak)
            .medFlerbarnsdager(flerbarnsdager)
            .build();
        uttakResultatPerioder.leggTilPeriode(periode);

        for (Tuple<UttakAktivitetEntitet, Optional<Integer>> aktivitetTuple : aktiviteter) {
            int trekkdager;
            if (aktivitetTuple.getElement2().isPresent()) {
                trekkdager = aktivitetTuple.getElement2().get();
            } else {
                trekkdager = TrekkdagerUtregningUtil.trekkdagerFor(
                    new Periode(periode.getFom(), periode.getTom()),
                    false,
                    true,
                    BigDecimal.ZERO,
                    false);
            }

            UttakResultatPeriodeAktivitetEntitet aktivitet = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, aktivitetTuple.getElement1())
                .medTrekkdager(trekkdager)
                .medTrekkonto(stønadskontoType)
                .medArbeidsprosent(BigDecimal.ZERO)
                .build();
            periode.leggTilAktivitet(aktivitet);

        }
    }

    private UttakAktivitetEntitet lagUttakAktivitet(VirksomhetEntitet virksomhet) {
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medArbeidsforhold(virksomhet, ArbeidsforholdRef.ref(UUID.randomUUID().toString()))
            .medUttakArbeidType(UttakArbeidType.ORDINÆRT_ARBEID)
            .build();
        return uttakAktivitet;
    }

    private Behandling lagBehandling(String aktørId, RelasjonsRolleType relasjonsRolleType) {
        NavBruker navBruker = new NavBrukerBuilder().medAktørId(new AktørId(aktørId)).build();
        repositoryRule.getEntityManager().persist(navBruker);
        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, navBruker, relasjonsRolleType);
        final Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        Behandlingsresultat.opprettFor(behandling);
        repositoryRule.getEntityManager().persist(behandling.getFagsak());
        repositoryRule.getEntityManager().flush();
        final BehandlingLås lås = repositoryProvider.getBehandlingLåsRepository().taLås(behandling.getId());
        repositoryProvider.getBehandlingRepository().lagre(behandling, lås);
        repositoryProvider.getFagsakRelasjonRepository().opprettRelasjon(behandling.getFagsak(), Dekningsgrad._100);
        return behandling;
    }

}
