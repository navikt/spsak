package no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class FamilieHendelseRepositoryImplTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private FamilieHendelseRepository repository = new FamilieHendelseRepositoryImpl(repositoryRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repositoryRule.getEntityManager());
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Test
    public void skal_lage_søknad_versjon() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        final FamilieHendelseBuilder hendelseBuilder = repository.opprettBuilderFor(behandling);
        hendelseBuilder.medFødselsDato(LocalDate.now()).medAntallBarn(1);
        repository.lagre(behandling, hendelseBuilder);

        FamilieHendelseGrunnlag familieHendelseGrunnlag = repository.hentAggregat(behandling);

        assertThat(familieHendelseGrunnlag.getOverstyrtVersjon()).isNotPresent();
        assertThat(familieHendelseGrunnlag.getBekreftetVersjon()).isNotPresent();
        assertThat(familieHendelseGrunnlag.getSøknadVersjon()).isNotNull();
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getBarna()).hasSize(1);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getAntallBarn()).isEqualTo(1);

        final FamilieHendelseBuilder hendelseBuilder1 = repository.opprettBuilderFor(behandling);
        hendelseBuilder1.leggTilBarn(LocalDate.now()).medAntallBarn(2);
        repository.lagre(behandling, hendelseBuilder1);

        familieHendelseGrunnlag = repository.hentAggregat(behandling);

        assertThat(familieHendelseGrunnlag.getOverstyrtVersjon()).isNotPresent();
        assertThat(familieHendelseGrunnlag.getBekreftetVersjon()).isPresent();
        assertThat(familieHendelseGrunnlag.getBekreftetVersjon().get().getBarna()).hasSize(2);
        assertThat(familieHendelseGrunnlag.getBekreftetVersjon().get().getAntallBarn()).isEqualTo(2);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon()).isNotNull();
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getBarna()).hasSize(1);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getAntallBarn()).isEqualTo(1);
    }

    @Test
    public void skal_lagre_med_endring_i_vilkår() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        FamilieHendelseBuilder hendelseBuilder = repository.opprettBuilderFor(behandling);
        hendelseBuilder.medAdopsjon(hendelseBuilder.getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(LocalDate.now()))
            .erOmsorgovertagelse()
            .medFødselsDato(LocalDate.now())
            .medAntallBarn(1);
        repository.lagre(behandling, hendelseBuilder);
        FamilieHendelseGrunnlag familieHendelseGrunnlag = repository.hentAggregat(behandling);

        assertThat(familieHendelseGrunnlag.getOverstyrtVersjon()).isNotPresent();
        assertThat(familieHendelseGrunnlag.getBekreftetVersjon()).isNotPresent();
        assertThat(familieHendelseGrunnlag.getSøknadVersjon()).isNotNull();
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getBarna()).hasSize(1);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getAntallBarn()).isEqualTo(1);

        hendelseBuilder = repository.opprettBuilderFor(behandling);
        hendelseBuilder.medAdopsjon(hendelseBuilder.getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_2_LEDD));
        repository.lagreOverstyrtHendelse(behandling, hendelseBuilder);

        familieHendelseGrunnlag = repository.hentAggregat(behandling);

        assertThat(familieHendelseGrunnlag.getSøknadVersjon()).isNotNull();
        assertThat(familieHendelseGrunnlag.getOverstyrtVersjon().get().getAdopsjon().get().getOmsorgovertakelseVilkår())
            .isEqualTo(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_2_LEDD);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getBarna()).hasSize(1);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getAntallBarn()).isEqualTo(1);

        final FamilieHendelseBuilder hendelseBuilder1 = repository.opprettBuilderFor(behandling);
        hendelseBuilder1.medAdopsjon(hendelseBuilder1.getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_4_LEDD));
        repository.lagre(behandling, hendelseBuilder1);

        familieHendelseGrunnlag = repository.hentAggregat(behandling);

        assertThat(familieHendelseGrunnlag.getBekreftetVersjon()).isNotPresent();
        assertThat(familieHendelseGrunnlag.getOverstyrtVersjon().get().getAdopsjon()).isPresent();
        assertThat(familieHendelseGrunnlag.getOverstyrtVersjon().get().getAdopsjon().get().getOmsorgovertakelseVilkår())
            .isEqualTo(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_4_LEDD);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon()).isNotNull();
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getBarna()).hasSize(1);
        assertThat(familieHendelseGrunnlag.getSøknadVersjon().getAntallBarn()).isEqualTo(1);
    }

    @Test
    public void skal_hente_eldste_versjon_av_aggregat() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        FamilieHendelseBuilder hendelseBuilder = repository.opprettBuilderFor(behandling);
        hendelseBuilder.medAdopsjon(hendelseBuilder.getAdopsjonBuilder()
            .medOmsorgsovertakelseDato(LocalDate.now()))
            .erOmsorgovertagelse()
            .medFødselsDato(LocalDate.now())
            .medAntallBarn(1);
        repository.lagre(behandling, hendelseBuilder);

        hendelseBuilder = repository.opprettBuilderFor(behandling);
        hendelseBuilder.medAdopsjon(hendelseBuilder.getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.OMSORGSVILKÅRET));
        repository.lagreOverstyrtHendelse(behandling, hendelseBuilder);

        final FamilieHendelseBuilder hendelseBuilder1 = repository.opprettBuilderFor(behandling);
        hendelseBuilder1.medAdopsjon(hendelseBuilder1.getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_2_LEDD));
        repository.lagre(behandling, hendelseBuilder1);

        final FamilieHendelseBuilder hendelseBuilder2 = repository.opprettBuilderFor(behandling);
        hendelseBuilder2.medAdopsjon(hendelseBuilder2.getAdopsjonBuilder()
            .medOmsorgovertalseVilkårType(OmsorgsovertakelseVilkårType.FORELDREANSVARSVILKÅRET_4_LEDD));
        repository.lagre(behandling, hendelseBuilder2);

        FamilieHendelseGrunnlag familieHendelseGrunnlag = repository.hentAggregat(behandling);
        Optional<FamilieHendelseGrunnlag> førsteVersjonFamilieHendelseAggregat = repository.hentFørsteVersjonAvAggregatHvisEksisterer(behandling);

        assertThat(familieHendelseGrunnlag).isNotEqualTo(førsteVersjonFamilieHendelseAggregat.get());
        assertThat(((FamilieHendelseGrunnlagEntitet) familieHendelseGrunnlag).getErAktivt()).isTrue();
        assertThat(((FamilieHendelseGrunnlagEntitet) førsteVersjonFamilieHendelseAggregat.get()).getErAktivt()).isFalse();
        assertThat((førsteVersjonFamilieHendelseAggregat.get()).getSøknadVersjon().getAdopsjon().get().getOmsorgovertakelseVilkår()).isEqualTo(OmsorgsovertakelseVilkårType.UDEFINERT);
    }
}
