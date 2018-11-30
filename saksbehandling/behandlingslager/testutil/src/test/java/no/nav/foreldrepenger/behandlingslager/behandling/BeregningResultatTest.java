package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallMerknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.felles.testutilities.db.Repository;

/* BeregningResultat regnes som et selvstendig aggregat, men har to overliggende nivåer for aggregat:
        Behandling -> Behandlingsresultat -> Beregningsresultat
  Denne testklassen fokuserer på at aggregatet (Beregningsresultat) bygges opp korrekt over suksessive transaksjoner
    som er forventet i use-caser.
 */
public class BeregningResultatTest {

    private final LocalDateTime nå = LocalDateTime.now();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private final FagsakRepository fagsakReposiory = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private BeregningRepository beregningRepository = new BeregningRepositoryImpl(repoRule.getEntityManager());

    private Fagsak fagsak = FagsakBuilder.nyFagsak().medBruker(NavBruker.opprettNy(new AktørId("909"))).build();
    private Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsak);
    private Behandling behandling1;
    private final long sats = 1L;
    private final long antallBarn = 1L;
    private final long tilkjentYtelse = 2L;

    @Before
    public void setup() {
        fagsakReposiory.opprettNy(fagsak);
        behandling1 = behandlingBuilder.build();
        lagreBehandling(behandling1);
    }

    @Test
    public void skal_opprette_nytt_beregningsresultat_uten_beregning_dersom_ikke_finnes_fra_før() {
        // Act
        // TX_1: Opprette nytt beregningsresultat
        BeregningResultat beregningResultat = BeregningResultat.builder().buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat);

        // Assert
        Behandling hentetBehandling = repository.hent(Behandling.class, behandling1.getId());
        BeregningResultat hentetResultat = hentetBehandling.getBehandlingsresultat().getBeregningResultat();
        assertThat(hentetResultat).isNotNull();
        assertThat(hentetResultat.getOriginalBehandling()).isEqualTo(behandling1);
    }

    @Test
    public void skal_koble_beregning_til_beregningsresultat() {
        Beregning beregning = new Beregning(1000L, antallBarn, antallBarn*1000, nå);
        assertThat(beregning.getBeregningResultat()).isNull();
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling1);
        assertThat(beregning.getBeregningResultat()).isNull();

        assertThat(beregningResultat.getBeregninger()).hasSize(1);
        assertThat(beregningResultat.getBeregninger().get(0).getBeregningResultat()).isNotNull();
    }

    private void lagreBeregningResultat(Behandling behandling, BeregningResultat beregningResultat) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        beregningRepository.lagre(beregningResultat, lås);
    }

    @Test
    public void skal_opprette_nytt_beregningsresultat_med_beregning_dersom_ikke_finnes_fra_før() {
        // Act
        // TX_1: Opprette nytt beregningsresultat med beregningsinfo
        Beregning beregning = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat = BeregningResultat.builder()
                .medBeregning(beregning)
                .buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat);

        // Assert
        Behandling hentetBehandling = repository.hent(Behandling.class, behandling1.getId());
        BeregningResultat hentetResultat = hentetBehandling.getBehandlingsresultat().getBeregningResultat();
        assertThat(hentetResultat.getBeregninger()).hasSize(1);
        assertThat(hentetResultat.getBeregninger().get(0)).isEqualTo(beregning);
    }

    @Test
    public void skal_oppdatere_beregningsresultat_uten_å_modifisere_original_behandling_id() {
        // Act
        // TX_1: Opprette nytt beregningsresultat
        BeregningResultat beregningResultat1 = BeregningResultat.builder().buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat1);

        // TX_2: Oppdatere beregningsresultat
        behandling1 = repository.hent(Behandling.class, behandling1.getId());
        Beregning beregning = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat2 = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat2);

        // Assert
        BeregningResultat hentet = repository.hent(BeregningResultat.class, beregningResultat2.getId());
        assertThat(hentet.getBeregninger().get(0)).isEqualTo(beregning);
        assertThat(hentet.getOriginalBehandling()).isEqualTo(behandling1);
    }

    @Test
    public void skal_gjenbruke_beregningsresultat_fra_tidligere_behandling_ved_opprettelse_av_ny_behandling() {
        // Act
        // TX_1: Opprette nytt beregningsresultat
        Beregning beregning = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning).buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat);

        // TX_2: Opprette nyTerminbekreftelse behandling fra tidligere behandling
        behandling1 = repository.hent(Behandling.class, behandling1.getId());
        Behandling behandling2 = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING)
            .medKopiAvForrigeBehandlingsresultat()
            .build();
        lagreBeregningResultat(behandling2, behandling2.getBehandlingsresultat().getBeregningResultat());
        lagreBehandling(behandling2);

        // Assert
        behandling2 = repository.hent(Behandling.class, behandling2.getId());
        assertThat(behandling2.getBehandlingsresultat()).isNotSameAs(behandling1.getBehandlingsresultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat())
                .isSameAs(behandling1.getBehandlingsresultat().getBeregningResultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat())
                .isEqualTo(behandling1.getBehandlingsresultat().getBeregningResultat());
    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

    @Test
    public void skal_opprette_nytt_beregningsresultat_dersom_gjenbrukt_resultat_fra_tidligere_behandling_oppdateres() {
        // Act
        // TX_1: Opprette nytt beregningsresultat
        Beregning beregning1 = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning1).buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat);

        // TX_2: Opprette nyTerminbekreftelse behandling fra tidligere behandling
        behandling1 = repository.hent(Behandling.class, behandling1.getId());
        Behandling behandling2 = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING)
            .medKopiAvForrigeBehandlingsresultat()
            .build();
        lagreBeregningResultat(behandling2, behandling2.getBehandlingsresultat().getBeregningResultat());
        lagreBehandling(behandling2);

        // TX_3: Oppdatere nyTerminbekreftelse behandling med beregning
        behandling2 = repository.hent(Behandling.class, behandling2.getId());
        Beregning beregning2 = new Beregning(sats + 1, antallBarn, tilkjentYtelse, nå);
        BeregningResultat.builder().medBeregning(beregning2).buildFor(behandling2);
        lagreBeregningResultat(behandling2, behandling2.getBehandlingsresultat().getBeregningResultat());

        // Assert
        behandling2 = repository.hent(Behandling.class, behandling2.getId());
        assertThat(behandling2.getBehandlingsresultat()).isNotSameAs(behandling1.getBehandlingsresultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat())
                .isNotSameAs(behandling1.getBehandlingsresultat().getBeregningResultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat())
                .isNotEqualTo(behandling1.getBehandlingsresultat().getBeregningResultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat().getBeregninger().get(0))
                .isEqualTo(beregning2);
    }

    @Test
    public void skal_ikke_opprette_nytt_beregningsresultat_dersom_resultat_fra_tidligere_behandling_allerede_er_oppdatert() {
        // Act
        // TX_1: Opprette nytt beregningsresultat
        Beregning beregning1 = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat = BeregningResultat.builder().medBeregning(beregning1).buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat);

        // TX_2: Opprette nyTerminbekreftelse behandling fra tidligere behandling
        behandling1 = repository.hent(Behandling.class, behandling1.getId());
        Behandling behandling2 = Behandling.fraTidligereBehandling(behandling1, BehandlingType.REVURDERING)
            .medKopiAvForrigeBehandlingsresultat()
            .build();
        lagreBeregningResultat(behandling2, behandling2.getBehandlingsresultat().getBeregningResultat());
        lagreBehandling(behandling2);

        // TX_3: Oppdatere nyTerminbekreftelse behandling med beregning
        behandling2 = repository.hent(Behandling.class, behandling2.getId());
        Beregning beregning2 = new Beregning(sats + 1, antallBarn, tilkjentYtelse, nå);
        BeregningResultat.builder().medBeregning(beregning2).buildFor(behandling2);
        lagreBeregningResultat(behandling2, behandling2.getBehandlingsresultat().getBeregningResultat());

        // TX_4: Oppdatere nyTerminbekreftelse behandling med beregning (samme som TX_3, men nyTerminbekreftelse beregning med nyTerminbekreftelse verdi)
        behandling2 = repository.hent(Behandling.class, behandling2.getId());
        Beregning beregning3 = new Beregning(sats + 2, antallBarn, tilkjentYtelse, nå);
        BeregningResultat.builder().medBeregning(beregning3).buildFor(behandling2);
        lagreBeregningResultat(behandling2, behandling2.getBehandlingsresultat().getBeregningResultat());

        // Assert
        behandling2 = repository.hent(Behandling.class, behandling2.getId());
        assertThat(behandling2.getBehandlingsresultat()).isNotSameAs(behandling1.getBehandlingsresultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat())
                .isNotSameAs(behandling1.getBehandlingsresultat().getBeregningResultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat())
                .isNotEqualTo(behandling1.getBehandlingsresultat().getBeregningResultat());
        assertThat(behandling2.getBehandlingsresultat().getBeregningResultat().getBeregninger().get(0))
                .isEqualTo(beregning3);
    }

    @Test
    public void skal_bevare_vilkårresultat_ved_oppdatering_av_beregingsresultat() {
        // Act
        // TX_1: Opprette Behandlingsresultat med Beregningsresultat
        Beregning beregning1 = new Beregning(sats, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat1 = BeregningResultat.builder().medBeregning(beregning1).buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat1);

        // TX_2: Oppdatere Behandlingsresultat med VilkårResultat
        behandling1 = repository.hent(Behandling.class, behandling1.getId());
        VilkårResultat vilkårResultat = VilkårResultat.builder()
                .leggTilVilkårResultat(VilkårType.MEDLEMSKAPSVILKÅRET, VilkårUtfallType.OPPFYLT, VilkårUtfallMerknad.VM_1001, new Properties(), null, false, false, null, null)
                .buildFor(behandling1);

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling1);
        behandlingRepository.lagre(vilkårResultat, lås);

        // TX_3: Oppdatere Behandlingsresultat med BeregningResultat
        behandling1 = repository.hent(Behandling.class, behandling1.getId());
        Beregning oppdatertBeregning = new Beregning(sats + 1, antallBarn, tilkjentYtelse, nå);
        BeregningResultat beregningResultat2 = BeregningResultat.builder().medBeregning(oppdatertBeregning).buildFor(behandling1);
        lagreBeregningResultat(behandling1, beregningResultat2);

        // Assert
        Behandling hentetBehandling = repository.hent(Behandling.class, behandling1.getId());
        assertThat(hentetBehandling.getBehandlingsresultat().getVilkårResultat())
                .isEqualTo(vilkårResultat);
    }
}
