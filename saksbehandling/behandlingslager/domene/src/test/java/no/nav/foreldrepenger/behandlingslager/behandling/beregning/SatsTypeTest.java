package no.nav.foreldrepenger.behandlingslager.behandling.beregning;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class SatsTypeTest {
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final Repository repository = repoRule.getRepository();
    private final BeregningRepository beregningRepository = repositoryProvider.getBeregningRepository();

    @Test
    public void skal_teste_verdier_for_sats_gbeløp_og_gsnitt() {
        List<Sats> grunnbeløpListe = repository.hentAlle(Sats.class).stream().filter(sats -> sats.getSatsType().equals(SatsType.GRUNNBELØP)).collect(Collectors.toList());
        List<Sats> gsnittListe = repository.hentAlle(Sats.class).stream().filter(sats -> sats.getSatsType().equals(SatsType.GSNITT)).collect(Collectors.toList());

        assertThat(grunnbeløpListe).isNotEmpty();
        assertThat(gsnittListe).isNotEmpty();
    }

    @Test
    public void skal_teste_gsnitt_fom_tom_er_1jan_og_31des() {
        List<Sats> gsnittListe = repository.hentAlle(Sats.class).stream().filter(sats -> sats.getSatsType().equals(SatsType.GSNITT)).collect(Collectors.toList());

        for (Sats sats : gsnittListe) {
            final DatoIntervallEntitet satsPeriode = sats.getPeriode();
            assertThat(satsPeriode.getFomDato()).isEqualTo(satsPeriode.getFomDato().getYear() + "-01-01");
            assertThat(satsPeriode.getTomDato()).isEqualTo(satsPeriode.getFomDato().getYear() + "-12-31");
        }
    }

    @Test
    public void skal_teste_hvert_år_mellom_1964_2018_har_gverdi_for_feb15() {
        for (int i = 1967; i <= 2018; i++) {
            final Sats sats = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, LocalDate.of(i, 2, 15));
            assertThat(sats).isNotNull();
            assertThat(sats.getVerdi()).isGreaterThan(5300);
        }
    }

    @Test
    public void skal_teste_gverdi_stiger_hvert_år() {
        for (int i = 1967; i <= 2016; i++) {
            final LocalDate localDate = LocalDate.of(i, 9, 15);
            final Sats sats = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, localDate);
            final Sats satsNestÅr = beregningRepository.finnEksaktSats(SatsType.GRUNNBELØP, localDate.plusYears(1));

            assertThat(satsNestÅr.getVerdi()).isGreaterThan(sats.getVerdi());
        }
    }
}
