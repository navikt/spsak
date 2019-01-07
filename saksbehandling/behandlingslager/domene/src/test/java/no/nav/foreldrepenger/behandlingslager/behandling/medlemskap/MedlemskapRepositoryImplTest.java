package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class MedlemskapRepositoryImplTest {

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    private MedlemskapRepository repository = new MedlemskapRepositoryImpl(repositoryRule.getEntityManager());
    private FagsakRepository fagsakRepository = new FagsakRepositoryImpl(repositoryRule.getEntityManager());
    private GrunnlagRepositoryProvider repositoryProvider = new GrunnlagRepositoryProviderImpl(repositoryRule.getEntityManager());
    private final BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Test
    public void skal_hente_eldste_versjon_av_aggregat() {
        Behandling behandling = lagBehandling();
        RegistrertMedlemskapPerioder perioder = new MedlemskapPerioderBuilder().medMedlemskapType(MedlemskapType.FORELOPIG).build();
        repository.lagreMedlemskapRegisterOpplysninger(behandling, Arrays.asList(perioder));

        perioder = new MedlemskapPerioderBuilder().medMedlemskapType(MedlemskapType.ENDELIG).build();
        repository.lagreMedlemskapRegisterOpplysninger(behandling, Arrays.asList(perioder));

        Optional<MedlemskapAggregat> medlemskapAggregat = repository.hentMedlemskap(behandling);
        Optional<MedlemskapAggregat> førsteVersjonMedlemskapAggregat = repository.hentFørsteVersjonAvMedlemskap(behandling);

        MedlemskapPerioderEntitet perioderEntitet = (MedlemskapPerioderEntitet) medlemskapAggregat.get().getRegistrertMedlemskapPerioder()
                .stream().findFirst().get();
        MedlemskapPerioderEntitet førstePerioderEntitet = (MedlemskapPerioderEntitet) førsteVersjonMedlemskapAggregat.get()
                .getRegistrertMedlemskapPerioder().stream().findFirst().get();

        assertThat(medlemskapAggregat.get()).isNotEqualTo(førsteVersjonMedlemskapAggregat.get());
        assertThat(perioderEntitet.getMedlemskapType()).isEqualTo(MedlemskapType.ENDELIG);
        assertThat(førstePerioderEntitet.getMedlemskapType()).isEqualTo(MedlemskapType.FORELOPIG);
    }

    @Test
    public void skal_lagre_vurdering_av_løpende_medlemskap() {
        Behandling behandling = lagBehandling();
        LocalDate vurderingsdato = LocalDate.now();
        VurdertMedlemskapPeriodeEntitet.VurdertMedlemskapPeriodeEntitetBuilder builder = new VurdertMedlemskapPeriodeEntitet.VurdertMedlemskapPeriodeEntitetBuilder();
        VurdertLøpendeMedlemskapBuilder løpendeMedlemskapBuilder = builder.getBuilderFor(vurderingsdato);

        løpendeMedlemskapBuilder.medBosattVurdering(true);
        løpendeMedlemskapBuilder.medVurderingsdato(LocalDate.now());

        builder.leggTil(løpendeMedlemskapBuilder);

        VurdertMedlemskapPeriodeEntitet hvaSkalLagres = builder.build();
        repository.lagreLøpendeMedlemskapVurdering(behandling, hvaSkalLagres);

        Optional<MedlemskapAggregat> medlemskapAggregat = repository.hentMedlemskap(behandling.getId());
        assertThat(medlemskapAggregat).isPresent();
        assertThat(medlemskapAggregat.get().getVurderingLøpendeMedlemskap()).contains(hvaSkalLagres);
    }

    private Behandling lagBehandling() {
        final Personinfo personinfo = new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
        final Fagsak fagsak = Fagsak.opprettNy(NavBruker.opprettNy(personinfo));
        fagsakRepository.opprettNy(fagsak);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        return behandling;
    }
}
