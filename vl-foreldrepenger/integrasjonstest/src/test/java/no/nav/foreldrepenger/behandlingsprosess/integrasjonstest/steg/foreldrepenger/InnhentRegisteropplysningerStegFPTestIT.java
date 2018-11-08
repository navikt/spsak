package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.steg.foreldrepenger;

import static no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon.AUTO_VENTER_PÅ_KOMPLETT_SØKNAD;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostInntektsmeldingBuilder;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder.journalpostSøknadBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.RegisterKontekst;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder.JournalpostMottakDtoBuilder;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.ArbeidsforholdTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.FørstegangssøknadTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InfotrygdVedtakTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.InntektTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.MeldekortTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett.TpsTestSett;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.tps.TpsPerson;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.apiwrapper.FordelRestTjenesteTestAPI;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.Repository;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

@RunWith(CdiRunner.class)
public class InnhentRegisteropplysningerStegFPTestIT {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Inject
    private BehandlingRepositoryProvider repositoryProvider;

    @Inject
    private RegisterKontekst registerKontekst;
    @Inject
    private FordelRestTjenesteTestAPI fordelRestTjenesteAPI;

    @Before
    public void setup() {
        registerKontekst.intialiser();
        // setter verdien slik at regler blir kjørt
        System.setProperty("dato.for.nye.beregningsregler", "2010-01-01");
    }

    @After
    public void teardown() {
        registerKontekst.nullstill();
        System.setProperty("dato.for.nye.beregningsregler", "2019-01-01");
    }

    @Test
    public void skal_utføre_steget_og_populere_opptjening_tabellene() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innhente registeropplysninger
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();

        // knyttet til mock for OrganisasjonConsumerProducerMock og ArbeidsforholdConsumerProducerMock og InntektConsumerProducerMock
        Optional<InntektArbeidYtelseGrunnlag> aggregat = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null);

        assertThat(aggregat).isPresent();

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = aggregat.get();
        Collection<AktørArbeid> aktørArbeid = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp();

        assertThat(aktørArbeid).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp()).hasSize(1);
        assertThat(inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp().iterator().next().getInntektPensjonsgivende().get(0).getInntektspost()).hasSize(36);
    }

    @Test
    public void skal_populere_ytelse_fra_infotrygd_og_arena() throws Exception {
        // Pre-Arrange: Registerdata + fagsak
        LocalDate fødselsdatoBarn = LocalDate.now().minusDays(5);
        TpsPerson mor = TpsTestSett.morMedBarnOgFellesFar(fødselsdatoBarn).getBruker();
        ArbeidsforholdTestSett.arbeidsforhold100prosent40timer(mor.getFnr());
        InntektTestSett.inntekt36mnd40000kr(mor.getPersonIdent());
        InfotrygdVedtakTestSett.infotrygdsakStandard(mor.getFnr(), 27L);
        MeldekortTestSett.meldekortStandard(mor.getAktørId().getId(), 27L);

        Fagsak fagsak = fordelRestTjenesteAPI.opprettSak(mor.getAktørId(), BehandlingTema.FORELDREPENGER_FØDSEL);

        // Arrange steg 1: Motta søknad -> behandling settes på vent for å vente på inntektsmelding
        Soeknad soeknad = FørstegangssøknadTestSett.morFødselStandardUttak(mor.getAktørId(), fødselsdatoBarn).build();
        JournalpostMottakDtoBuilder journalpostSøknadBuilder = journalpostSøknadBuilder(fagsak, soeknad, repositoryProvider);

        // Act
        Long behandlingId = fordelRestTjenesteAPI.mottaJournalpost(journalpostSøknadBuilder);

        // Assert
        Behandling behandling = repository.hent(Behandling.class, behandlingId);
        assertThat(behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).get(0).getAksjonspunktDefinisjon())
            .isEqualTo(AUTO_VENTER_PÅ_KOMPLETT_SØKNAD);

        // Arrange steg 2: Send inn inntektsmelding -> skal tas av vent og innhente registeropplysninger
        InntektsmeldingM im = InntektsmeldingMTestdataBuilder.inntektsmelding40000kr(mor.getPersonIdent()).build();
        JournalpostMottakDtoBuilder journalpostBuilderIM = journalpostInntektsmeldingBuilder(fagsak, im, repositoryProvider);

        // Act
        fordelRestTjenesteAPI.mottaJournalpost(journalpostBuilderIM);

        // Assert
        InntektArbeidYtelseRepository inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();

        //fra InfotrygdSakConsumerMock
        Optional<InntektArbeidYtelseGrunnlag> aggregat = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling, null);

        assertThat(aggregat).isPresent();

        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = aggregat.get();

        assertThat(inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp()).hasSize(1);
        Collection<Ytelse> ytelser = inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp().iterator().next().getYtelser();
        assertThat(ytelser).hasSize(5);
        assertThat(ytelser.stream().filter(ytelse -> ytelse.getKilde().equals(Fagsystem.INFOTRYGD))).hasSize(4);
        assertThat(ytelser.stream().filter(ytelse -> ytelse.getKilde().equals(Fagsystem.ARENA))).hasSize(1);
    }

}
