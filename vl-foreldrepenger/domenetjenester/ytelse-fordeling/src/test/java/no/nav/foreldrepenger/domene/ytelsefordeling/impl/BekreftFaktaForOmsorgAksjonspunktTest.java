package no.nav.foreldrepenger.domene.ytelsefordeling.impl;

import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PeriodeUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelsesFordelingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftFaktaForOmsorgVurderingAksjonspunktDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;


@RunWith(CdiRunner.class)
public class BekreftFaktaForOmsorgAksjonspunktTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private BehandlingRepository behandlingRepository = new BehandlingRepositoryImpl(repoRule.getEntityManager());
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private YtelsesFordelingRepository ytelsesFordelingRepository = new YtelsesFordelingRepositoryImpl(repoRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repoRule.getEntityManager());
    private Behandling behandling;

    private BekreftFaktaForOmsorgAksjonspunkt bekreftFaktaForOmsorgAksjonspunkt;

    @Before
    public void oppsett() {
        LocalDate iDag = LocalDate.now();
        bekreftFaktaForOmsorgAksjonspunkt = new BekreftFaktaForOmsorgAksjonspunkt(repositoryProvider);
        behandling = opprettBehandling(iDag);
    }


    @Test
    public void skal_lagre_ned_bekreftet_aksjonspunkt_omsorg() {
        LocalDate iDag = LocalDate.now();
        // simulerer svar fra GUI
        List<DatoIntervallEntitet> ikkeOmsorgPerioder = new ArrayList<>();
        DatoIntervallEntitet ikkeOmsorgPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(iDag.minusMonths(2), iDag.minusMonths(1));
        ikkeOmsorgPerioder.add(ikkeOmsorgPeriode);
        BekreftFaktaForOmsorgVurderingAksjonspunktDto dto = new BekreftFaktaForOmsorgVurderingAksjonspunktDto(null, false, ikkeOmsorgPerioder);
        bekreftFaktaForOmsorgAksjonspunkt.oppdater(behandling, dto);

        Optional<PerioderUtenOmsorg> perioderUtenOmsorgOpt = ytelsesFordelingRepository.hentAggregat(behandling).getPerioderUtenOmsorg();
        assertThat(perioderUtenOmsorgOpt).isPresent();
        List<PeriodeUtenOmsorg> periodeUtenOmsorg = perioderUtenOmsorgOpt.get().getPerioder();
        assertThat(periodeUtenOmsorg).hasSize(1);
        assertThat(periodeUtenOmsorg.get(0).getPeriode()).isEqualTo(ikkeOmsorgPeriode);

        //må nullstille etter endret til har omsorg
        dto = new BekreftFaktaForOmsorgVurderingAksjonspunktDto(null, true, null);
        bekreftFaktaForOmsorgAksjonspunkt.oppdater(behandling, dto);
        perioderUtenOmsorgOpt = ytelsesFordelingRepository.hentAggregat(behandling).getPerioderUtenOmsorg();
        assertThat(perioderUtenOmsorgOpt).isPresent();
        periodeUtenOmsorg = perioderUtenOmsorgOpt.get().getPerioder();
        assertThat(periodeUtenOmsorg).hasSize(0);
    }

    @Test
    public void skal_lagre_ned_bekreftet_aksjonspunkt_aleneomsorg() {
        BekreftFaktaForOmsorgVurderingAksjonspunktDto dto = new BekreftFaktaForOmsorgVurderingAksjonspunktDto(false, null, null);
        bekreftFaktaForOmsorgAksjonspunkt.oppdater(behandling, dto);

        Optional<PerioderAleneOmsorg> perioderAleneOmsorgOptional = ytelsesFordelingRepository.hentAggregat(behandling).getPerioderAleneOmsorg();
        assertThat(perioderAleneOmsorgOptional).isPresent();
        List<PeriodeAleneOmsorg> periodeAleneOmsorg = perioderAleneOmsorgOptional.get().getPerioder();
        assertThat(periodeAleneOmsorg).hasSize(0);

        //må legge inn etter endret til har aleneomsorg
        dto = new BekreftFaktaForOmsorgVurderingAksjonspunktDto(true, null, null);
        bekreftFaktaForOmsorgAksjonspunkt.oppdater(behandling, dto);
        perioderAleneOmsorgOptional = ytelsesFordelingRepository.hentAggregat(behandling).getPerioderAleneOmsorg();
        assertThat(perioderAleneOmsorgOptional).isPresent();
        periodeAleneOmsorg = perioderAleneOmsorgOptional.get().getPerioder();
        assertThat(periodeAleneOmsorg).hasSize(1);
        assertThat(periodeAleneOmsorg.get(0).getPeriode()).isEqualTo(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now()));
    }

    private Behandling opprettBehandling(LocalDate iDag) {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("2"))
            .medFødselsdato(iDag.minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12312312314"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        return behandling;
    }
}
