package no.nav.foreldrepenger.domene.uttak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Aktivitet;
import no.nav.foreldrepenger.domene.uttak.saldo.Saldoer;
import no.nav.foreldrepenger.domene.uttak.saldo.StønadskontoSaldoTjeneste;
import no.nav.foreldrepenger.domene.uttak.uttaksplan.RelatertBehandlingTjeneste;

@RunWith(Enclosed.class)
public class InfoOmResterendeDagerTjenesteImplTest {

    private static final int FPFF_SALDO = 1;
    private static final int MØDREKVOTE_SALDO = 2;
    private static final int FELLESPERIODE_SALDO = 4;
    private static final int FEDREKVOTE_SALDO = 8;
    private static final int FORELDREPENGER_SALDO = 16;

    private static Behandling behandling;
    private static InfoOmResterendeDagerTjeneste infoOmResterendeDager;
    private static StønadskontoSaldoTjeneste saldoTjeneste;

    private static void initSaldoTjeneste() {
        if (saldoTjeneste == null) {
            saldoTjeneste = mockSaldoTjeneste();
        }
    }

    public static class getDisponibleDager {
        @Before
        public void oppsett() {
            initSaldoTjeneste();
            infoOmResterendeDager = new InfoOmResterendeDagerTjenesteImpl(saldoTjeneste, null, null);
            behandling = mock(Behandling.class);
        }

        @Test
        public void skal_returnere_summen_av_resterende_dager_fra_ulike_kontoer_avhengig_om_det_er_mor_eller_far_og_om_begge_har_rett_på_FP() {
            when(behandling.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.MORA);
            int disponibleDager = infoOmResterendeDager.getDisponibleDager(behandling, false, true); // ikke aleneomsorg og far rett på FP
            assertThat(disponibleDager).isEqualTo(3); // summert saldo for FORELDREPENGER_FØR_FØDSEL og MØDREKVOTE

            disponibleDager = infoOmResterendeDager.getDisponibleDager(behandling, true, false); // aleneomsorg og far ikke rett
            assertThat(disponibleDager).isEqualTo(17); // summert saldo for FORELDREPENGER og FORELDREPENGER_FØR_FØDSEL
            assertThat(disponibleDager).isEqualTo(infoOmResterendeDager.getDisponibleDager(behandling, false, false));

            when(behandling.getRelasjonsRolleType()).thenReturn(RelasjonsRolleType.FARA); // og når søker er far...
            disponibleDager = infoOmResterendeDager.getDisponibleDager(behandling, false, true);
            assertThat(disponibleDager).isEqualTo(8); // saldo på konto FEDREKVOTE

            disponibleDager = infoOmResterendeDager.getDisponibleDager(behandling, true, false);
            assertThat(disponibleDager).isEqualTo(16); // saldo på konto FORELDREPENGER
            assertThat(disponibleDager).isEqualTo(infoOmResterendeDager.getDisponibleDager(behandling, false, false));
        }
    }

    public static class getDisponibleFellesDager {
        @Before
        public void oppsett() {
            initSaldoTjeneste();
            infoOmResterendeDager = new InfoOmResterendeDagerTjenesteImpl(saldoTjeneste, null, null);
        }

        @Test
        public void skal_returnere_resterende_dager_igjen_på_Fellesperiode_kontoen() {
            int disponibleFellesDager = infoOmResterendeDager.getDisponibleFellesDager(behandling);
            assertThat(disponibleFellesDager).isEqualTo(4); // saldo på konto FELLESPERIODE
        }
    }


    public static class getSisteDagAvSistePeriodeTilAnnenForelder {
        private UttakRepository uttakRepository;
        private RelatertBehandlingTjeneste relatertBehandlingTjeneste;
        private Behandling behandling;
        private Fagsak løpendeFagsak;
        private Fagsak fagsakUnderBehandling;

        @Before
        public void oppsett() {
            uttakRepository = mockUttakRepository();
            relatertBehandlingTjeneste = mock(RelatertBehandlingTjeneste.class);
            behandling = mock(Behandling.class);
            løpendeFagsak = mockFagsak(RelasjonsRolleType.MORA);
            fagsakUnderBehandling = mockFagsak(RelasjonsRolleType.FARA);
            infoOmResterendeDager = new InfoOmResterendeDagerTjenesteImpl(saldoTjeneste, uttakRepository, relatertBehandlingTjeneste);
        }

        @Test
        public void skal_finne_relatert_behandling_og_returnere_siste_dag_i_annen_forelders_løpende_sak() {
            when(relatertBehandlingTjeneste.hentAnnenPartsGjeldendeBehandling(any())).thenReturn(Optional.of(behandling));

            when(behandling.getFagsak()).thenReturn(løpendeFagsak);
            Optional<LocalDate> sisteDagAvSistePeriodeTilAnnenForelder = infoOmResterendeDager.getSisteDagAvSistePeriodeTilAnnenForelder(behandling);
            assertThat(sisteDagAvSistePeriodeTilAnnenForelder.get()).isEqualTo(LocalDate.MAX);

            when(behandling.getFagsak()).thenReturn(fagsakUnderBehandling);
            sisteDagAvSistePeriodeTilAnnenForelder = infoOmResterendeDager.getSisteDagAvSistePeriodeTilAnnenForelder(behandling);
            assertThat(sisteDagAvSistePeriodeTilAnnenForelder).isEmpty();
    }
    }

    private static StønadskontoSaldoTjeneste mockSaldoTjeneste() {
        StønadskontoSaldoTjeneste saldoTjeneste = mock(StønadskontoSaldoTjeneste.class);
        Saldoer saldoer = mockSaldoer();
        when(saldoTjeneste.finnSaldoer(any())).thenReturn(saldoer);
        return saldoTjeneste;
    }

    private static Saldoer mockSaldoer() {
        return new Saldoer() {
            @Override
            public int saldo(StønadskontoType stønadskonto, Aktivitet aktivitet) {
                return 0;
            }
            @Override
            public int saldo(StønadskontoType stønadskonto) {
                if (stønadskonto.equals(StønadskontoType.FORELDREPENGER_FØR_FØDSEL)) return FPFF_SALDO;
                if (stønadskonto.equals(StønadskontoType.MØDREKVOTE)) return MØDREKVOTE_SALDO;
                if (stønadskonto.equals(StønadskontoType.FELLESPERIODE)) return FELLESPERIODE_SALDO;
                if (stønadskonto.equals(StønadskontoType.FEDREKVOTE)) return FEDREKVOTE_SALDO;
                if (stønadskonto.equals(StønadskontoType.FORELDREPENGER)) return FORELDREPENGER_SALDO;
                return 1000000000;
            }
            @Override
            public Set<Aktivitet> aktiviteterForSøker() {
                return null;
            }
            @Override
            public Set<StønadskontoType> stønadskontoer() {
                return null;
            }
            @Override
            public int getMaxDager(StønadskontoType stønadskonto) {
                return 0;
            }
            @Override
            public Optional<LocalDate> getMaksDatoUttak() {
                return Optional.empty();
            }
        };
    }

    private static Fagsak mockFagsak(RelasjonsRolleType søkersRelasjon) {
        Fagsak mock = mock(Fagsak.class);
        when(mock.getRelasjonsRolleType()).thenReturn(søkersRelasjon);
        when(mock.getStatus()).thenReturn(søkersRelasjon.equals(RelasjonsRolleType.MORA) ? FagsakStatus.LØPENDE : FagsakStatus.UNDER_BEHANDLING);
        return mock;
    }

    private static UttakRepository mockUttakRepository() {
        UttakRepository mock = mock(UttakRepository.class);
        UttakResultatEntitet uttakResultatMock = mockUttakResultat();
        when(mock.hentUttakResultatHvisEksisterer(any())).thenReturn(Optional.of(uttakResultatMock));
        return mock;
    }

    private static UttakResultatEntitet mockUttakResultat() {
        UttakResultatEntitet mock = mock(UttakResultatEntitet.class);
        UttakResultatPerioderEntitet uttakResultatPerioderMock = mockPerioder();
        when(mock.getGjeldendePerioder()).thenReturn(uttakResultatPerioderMock);
        return mock;
    }

    private static UttakResultatPerioderEntitet mockPerioder() {
        UttakResultatPerioderEntitet mock = mock(UttakResultatPerioderEntitet.class);

        List<UttakResultatPeriodeEntitet> periodeListe = new ArrayList<>();
        UttakResultatPeriodeEntitet periodeEn = new UttakResultatPeriodeEntitet.Builder(LocalDate.MIN, LocalDate.now())
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();
        UttakResultatPeriodeEntitet periodeTo = new UttakResultatPeriodeEntitet.Builder(LocalDate.now(), LocalDate.MAX)
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();

        periodeListe.add(periodeEn);
        periodeListe.add(periodeTo);

        when(mock.getPerioder()).thenReturn(periodeListe);
        return mock;
    }
}
