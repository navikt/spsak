package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.es;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.Innsendingsvalg;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedlegg;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadVedleggEntitet;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerEngangsstønad;
import no.nav.foreldrepenger.behandlingslager.testutilities.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class KompletthetssjekkerManglendeVedleggESTest {

    private static final Saksnummer SAKSNUMMER  = new Saksnummer("123");

    private final DokumentArkivTjeneste dokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);
    private final KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();
    private final SøknadRepository søknadRepository = mock(SøknadRepository.class);
    private KompletthetsjekkerES kompletthetssjekker = lagKompletthetssjekkerES(dokumentArkivTjeneste, søknadRepository, kodeverkRepository);

    private DokumentTypeId dokumentTypeIdDokumentasjonAvTerminEllerFødsel = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
    private DokumentTypeId dokumentTypeIdDokumentasjonAvOmsorgsovertakelse = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.DOKUMENTASJON_AV_OMSORGSOVERTAKELSE);
    private DokumentTypeId dokumentTypeIdUdefinert = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.UDEFINERT);

    private static KompletthetsjekkerES lagKompletthetssjekkerES(DokumentArkivTjeneste dokumentArkivTjeneste, SøknadRepository søknadRepository, KodeverkRepository kodeverkRepository) {
        BehandlingRepositoryProvider repositoryProvider = mock(BehandlingRepositoryProvider.class);
        PersonopplysningTjeneste personopplysningTjeneste = mock(PersonopplysningTjeneste.class);
        when(repositoryProvider.getSøknadRepository()).thenReturn(søknadRepository);
        return new KompletthetsjekkerES(repositoryProvider, dokumentArkivTjeneste, kodeverkRepository, personopplysningTjeneste);
    }

    @Test
    public void skal_regne_søknaden_som_komplett_når_JournalTjeneste_har_alle_dokumentene() {
        // Arrange
        Set<DokumentTypeId> dokumentTypeIds = new HashSet<>();
        dokumentTypeIds.add(dokumentTypeIdDokumentasjonAvTerminEllerFødsel);
        dokumentTypeIds.add(dokumentTypeIdUdefinert);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(), any(), anyList())).thenReturn(dokumentTypeIds);

        Behandling behandling = lagBehandling();

        SøknadVedlegg påkrevdSøknadVedlegg = new SøknadVedleggEntitet.Builder()
            .medSkjemanummer(dokumentTypeIdDokumentasjonAvTerminEllerFødsel.getKode())
            .medErPåkrevdISøknadsdialog(true)
            .medInnsendingsvalg(Innsendingsvalg.LASTET_OPP)
            .build();
        SøknadVedlegg annetSøknadVedlegg = new SøknadVedleggEntitet.Builder()
            .medSkjemanummer(dokumentTypeIdUdefinert.getKode())
            .medErPåkrevdISøknadsdialog(false)
            .medInnsendingsvalg(Innsendingsvalg.LASTET_OPP)
            .build();
        Søknad søknad = new SøknadEntitet.Builder().leggTilVedlegg(påkrevdSøknadVedlegg).leggTilVedlegg(annetSøknadVedlegg).build();
        reset(søknadRepository);
        when(søknadRepository.hentSøknad(behandling)).thenReturn(søknad);
        when(søknadRepository.hentSøknadHvisEksisterer(behandling)).thenReturn(java.util.Optional.ofNullable(søknad));

        // Act
        final List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledAlleManglendeVedleggForForsendelse(behandling);

        // Assert
        assertThat(manglendeVedlegg).isEmpty();
    }

    @Test
    public void skal_regne_søknaden_som_ukomplett_når_JournalTjeneste_ikke_har_alle_dokumentene() {
        // Arrange
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(), any(), anyList())).thenReturn(Collections.singleton(dokumentTypeIdUdefinert));

        Behandling behandling = lagBehandling();

        SøknadVedlegg påkrevdSøknadVedlegg = new SøknadVedleggEntitet.Builder()
            .medSkjemanummer(dokumentTypeIdDokumentasjonAvTerminEllerFødsel.getOffisiellKode())
            .medErPåkrevdISøknadsdialog(true)
            .medInnsendingsvalg(Innsendingsvalg.SEND_SENERE)
            .build();
        SøknadVedlegg annetSøknadVedlegg = new SøknadVedleggEntitet.Builder()
            .medSkjemanummer(dokumentTypeIdUdefinert.getOffisiellKode())
            .medErPåkrevdISøknadsdialog(false)
            .medInnsendingsvalg(Innsendingsvalg.LASTET_OPP)
            .build();
        Søknad søknad = new SøknadEntitet.Builder().medElektroniskRegistrert(true).leggTilVedlegg(påkrevdSøknadVedlegg).leggTilVedlegg(annetSøknadVedlegg).build();
        reset(søknadRepository);
        when(søknadRepository.hentSøknad(behandling)).thenReturn(søknad);
        when(søknadRepository.hentSøknadHvisEksisterer(behandling)).thenReturn(java.util.Optional.ofNullable(søknad));
        // Act
        final List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledAlleManglendeVedleggForForsendelse(behandling);

        // Assert
        assertThat(manglendeVedlegg).isNotEmpty();
    }

    @Test
    public void skal_også_håndtere_at_dokumentene_kommer_i_motsatt_rekkefølge_på_søknaden() {
        // Arrange
        Set<DokumentTypeId> dokumentTypeIds = new HashSet<>();
        dokumentTypeIds.add(dokumentTypeIdDokumentasjonAvTerminEllerFødsel);
        dokumentTypeIds.add(dokumentTypeIdUdefinert);
        when(dokumentArkivTjeneste.hentDokumentTypeIdForSak(any(), any(), anyList())).thenReturn(dokumentTypeIds);

        Behandling behandling = lagBehandling();

        SøknadVedlegg påkrevdSøknadVedlegg1 = new SøknadVedleggEntitet.Builder()
            .medSkjemanummer(dokumentTypeIdDokumentasjonAvTerminEllerFødsel.getOffisiellKode())
            .medErPåkrevdISøknadsdialog(true)
            .medInnsendingsvalg(Innsendingsvalg.SEND_SENERE)
            .build();
        SøknadVedlegg påkrevdSøknadVedlegg2 = new SøknadVedleggEntitet.Builder()
            .medSkjemanummer(dokumentTypeIdDokumentasjonAvOmsorgsovertakelse.getOffisiellKode())
            .medErPåkrevdISøknadsdialog(true)
            .medInnsendingsvalg(Innsendingsvalg.LASTET_OPP)
            .build();
        Søknad søknad = new SøknadEntitet.Builder().medElektroniskRegistrert(true).leggTilVedlegg(påkrevdSøknadVedlegg2).leggTilVedlegg(påkrevdSøknadVedlegg1).build();
        reset(søknadRepository);
        when(søknadRepository.hentSøknad(behandling)).thenReturn(søknad);
        when(søknadRepository.hentSøknadHvisEksisterer(behandling)).thenReturn(java.util.Optional.ofNullable(søknad));
        // Act
        final List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledAlleManglendeVedleggForForsendelse(behandling);

        // Assert
        assertThat(manglendeVedlegg).isNotEmpty();
    }

    @Test
    public void skal_regne_søknaden_som_komplett_hvis_den_ikke_inneholder_vedlegg() {
        // Arrange
        Behandling behandling = lagBehandling();
        Søknad søknad = new SøknadEntitet.Builder().build();
        reset(søknadRepository);
        when(søknadRepository.hentSøknad(behandling)).thenReturn(søknad);
        when(søknadRepository.hentSøknadHvisEksisterer(behandling)).thenReturn(java.util.Optional.ofNullable(søknad));
        // Act
        final List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekker.utledAlleManglendeVedleggForForsendelse(behandling);

        // Assert
        assertThat(manglendeVedlegg).isEmpty();
    }

    private Behandling lagBehandling() {
        ScenarioMorSøkerEngangsstønad scenario = ScenarioMorSøkerEngangsstønad
            .forFødsel()
            .medSaksnummer(SAKSNUMMER);
        return scenario.lagMocked();
    }
}
