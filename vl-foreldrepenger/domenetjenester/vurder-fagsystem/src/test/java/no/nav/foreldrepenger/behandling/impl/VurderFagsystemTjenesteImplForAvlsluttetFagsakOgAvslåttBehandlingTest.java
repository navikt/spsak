package no.nav.foreldrepenger.behandling.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandling.BehandlendeFagsystem;
import no.nav.foreldrepenger.behandling.VurderFagsystem;
import no.nav.foreldrepenger.behandling.VurderFagsystemTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.arbeidsforhold.arbeid.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl.MottatteDokumentTjenesteImpl;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.DokumentPersistererTjenesteImpl;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class VurderFagsystemTjenesteImplForAvlsluttetFagsakOgAvslåttBehandlingTest {

    private static final int FRIST_INNSENDING_UKER = 6;
    public static final Period VENTE_FRIST_AAREG = Period.parse("P1D");
    private final LocalDate DATO_ETTER_FRISTEN = LocalDate.now().minusWeeks(FRIST_INNSENDING_UKER + 2);
    private final LocalDate DATO_FØR_FRISTEN = LocalDate.now().minusWeeks(FRIST_INNSENDING_UKER - 2);

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(entityManager);
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();

    @Test
    public void skalTilManuellVurderingHvisBehandlingErAvslåttPgaManglendeDokOgInnsendtDokErEtterFristForInnsending() {
        //Arrange
        Behandling behandling = opprettBehandling(BehandlingType.FØRSTEGANGSSØKNAD, BehandlingResultatType.AVSLÅTT, Avslagsårsak.MANGLENDE_DOKUMENTASJON, VedtakResultatType.AVSLAG, DATO_ETTER_FRISTEN);
        VurderFagsystem vfData = opprettVurderFagsystem(behandling, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        //Act
        BehandlendeFagsystem resultat = opprettVurderFagsystemTjeneste().vurderFagsystem(vfData);

        //Assert
        assertThat(resultat.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        assertThat(resultat.getSaksnummer()).isEmpty();
    }

    @Test
    public void skalReturnereVedtaksløsningMedSaksnummerVurderingHvisBehandlingErAvslåttPgaManglendeDokOgInnsendtDokErFørFristForInnsending() {
        //Arrange
        Behandling behandling = opprettBehandling(BehandlingType.REVURDERING, BehandlingResultatType.AVSLÅTT, Avslagsårsak.MANGLENDE_DOKUMENTASJON, VedtakResultatType.AVSLAG, DATO_FØR_FRISTEN);
        VurderFagsystem vfData = opprettVurderFagsystem(behandling, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        //Act
        BehandlendeFagsystem resultat = opprettVurderFagsystemTjeneste().vurderFagsystem(vfData);

        //Assert
        assertThat(resultat.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.VEDTAKSLØSNING);
        assertThat(resultat.getSaksnummer()).isEqualTo(Optional.of(behandling.getFagsak().getSaksnummer()));
    }

    @Test
    public void skalTilManuellVurderingHvisBehandlingIkkeErAvslåttPgaManglendeDokOgInnsendtDokErFørFristForInnsending() {
        //Arrange
        Behandling behandling = opprettBehandling(BehandlingType.FØRSTEGANGSSØKNAD, BehandlingResultatType.AVSLÅTT, Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING, VedtakResultatType.AVSLAG, DATO_FØR_FRISTEN);
        VurderFagsystem vfData = opprettVurderFagsystem(behandling, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        //Act
        BehandlendeFagsystem resultat = opprettVurderFagsystemTjeneste().vurderFagsystem(vfData);

        //Assert
        assertThat(resultat.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        assertThat(resultat.getSaksnummer()).isEmpty();
    }

    @Test
    public void skalTilManuellVurderingHvisBehandlingIkkeErAvslåttPgaManglendeDokOgInnsendtDokErEtterFristForInnsending() {
        //Arrange
        Behandling behandling = opprettBehandling(BehandlingType.REVURDERING, BehandlingResultatType.AVSLÅTT, Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING, VedtakResultatType.AVSLAG, DATO_ETTER_FRISTEN);
        VurderFagsystem vfData = opprettVurderFagsystem(behandling, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        //Act
        BehandlendeFagsystem resultat = opprettVurderFagsystemTjeneste().vurderFagsystem(vfData);

        //Assert
        assertThat(resultat.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        assertThat(resultat.getSaksnummer()).isEmpty();
    }

    @Test
    public void skalTilManuellVurderingHvisBehandlingstypeErInnsyn() {
        //Arrange
        Behandling behandling = opprettBehandling(BehandlingType.INNSYN, BehandlingResultatType.AVSLÅTT, Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING, VedtakResultatType.AVSLAG, DATO_FØR_FRISTEN);
        VurderFagsystem vfData = opprettVurderFagsystem(behandling, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        //Act
        BehandlendeFagsystem resultat = opprettVurderFagsystemTjeneste().vurderFagsystem(vfData);

        //Assert
        assertThat(resultat.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        assertThat(resultat.getSaksnummer()).isEmpty();
    }


    @Test
    public void skalTilManuellVurderingHvisBehandlingstypeErKlage() {
        //Arrange
        Behandling behandling = opprettBehandling(BehandlingType.KLAGE, BehandlingResultatType.AVSLÅTT, Avslagsårsak.IKKE_TILSTREKKELIG_OPPTJENING, VedtakResultatType.AVSLAG, DATO_FØR_FRISTEN);
        VurderFagsystem vfData = opprettVurderFagsystem(behandling, BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        //Act
        BehandlendeFagsystem resultat = opprettVurderFagsystemTjeneste().vurderFagsystem(vfData);

        //Assert
        assertThat(resultat.getBehandlendeSystem()).isEqualTo(BehandlendeFagsystem.BehandlendeSystem.MANUELL_VURDERING);
        assertThat(resultat.getSaksnummer()).isEmpty();
    }


    private Behandling opprettBehandling(BehandlingType behandlingType, BehandlingResultatType behandlingResultatType, Avslagsårsak avslagsårsak, VedtakResultatType vedtakResultatType, LocalDate vedtaksdato) {
        ScenarioMorSøkerEngangsstønad scenarioES = ScenarioMorSøkerEngangsstønad.forFødsel()
            .medFagsakId(1234L)
            .medSaksnummer(new Saksnummer("2345"))
            .medBehandlingType(behandlingType);

        Behandling behandling = scenarioES.lagre(repositoryProvider);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(behandlingResultatType)
            .medAvslagsårsak(avslagsårsak)
            .buildFor(behandling);

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);

        BehandlingVedtak originalVedtak = BehandlingVedtak.builder()
            .medVedtaksdato(vedtaksdato)
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtakResultatType(vedtakResultatType)
            .medAnsvarligSaksbehandler("fornavn etternavn")
            .build();

        behandling.getFagsak().setAvsluttet();
        behandling.avsluttBehandling();
        repositoryProvider.getBehandlingVedtakRepository().lagre(originalVedtak, behandlingLås);
        return behandling;
    }

    private VurderFagsystem opprettVurderFagsystem(Behandling behandling, BehandlingTema behandlingTema) {
        LocalDate terminDatdato = LocalDate.of(2017, 7, 1);
        VurderFagsystem vfData = new VurderFagsystem();
        vfData.setBehandlingTema(behandlingTema);
        vfData.setAktørId(new AktørId("123"));
        vfData.setStrukturertSøknad(true);
        vfData.setBarnTermindato(terminDatdato);
        vfData.setSaksnummer(behandling.getFagsak().getSaksnummer());
        return vfData;
    }

    private VurderFagsystemTjeneste opprettVurderFagsystemTjeneste() {

        ArbeidsforholdTjeneste arbeidsforholdTjenesteMock = Mockito.mock(ArbeidsforholdTjeneste.class);
        TpsTjeneste tpsTjenesteMock = Mockito.mock(TpsTjeneste.class);

        MottatteDokumentRepository mottatteDokumentRepository = new MottatteDokumentRepositoryImpl(entityManager);
        DokumentPersistererTjeneste dokumentPersistererTjeneste = new DokumentPersistererTjenesteImpl();

        MottatteDokumentTjenesteImpl mottatteDokumentTjeneste =
            new MottatteDokumentTjenesteImpl(FRIST_INNSENDING_UKER, dokumentPersistererTjeneste, mottatteDokumentRepository, repositoryProvider);

        FagsakTjenesteImpl fagsakTjeneste = new FagsakTjenesteImpl(repositoryProvider, null);
        return new VurderFagsystemTjenesteImpl(VENTE_FRIST_AAREG, fagsakTjeneste, arbeidsforholdTjenesteMock, tpsTjenesteMock, repositoryProvider, mottatteDokumentTjeneste);
    }
}

