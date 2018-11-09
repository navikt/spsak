package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;

public class DokumentmottakerSøknadHåndteringVedAvslåttBehandlingTest extends DokumentmottakerTestsupport {

    private Dokumentmottaker dokumentmottakerSøknad;
    private Behandlingsoppretter behandlingsoppretterSpied;

    @Before
    public void setup() {
        this.behandlingsoppretterSpied = Mockito.spy(behandlingsoppretter);
        dokumentmottakerSøknad = new DokumentmottakerSøknad(
            repositoryProvider,
            dokumentmottakerFelles,
            mottatteDokumentTjeneste,
            behandlingsoppretterSpied,
            kompletthetskontroller);
    }

    @Test
    public void gittAvslåttBehandlingForEngangsstønadSkalOppretteNyFørstegangsbehandling() {
        //Arrange
        Mockito.doNothing().when(behandlingsoppretterSpied).opprettNyFørstegangsbehandling(Mockito.any(), Mockito.any(), Mockito.any());

        Behandling behandling = opprettBehandling(
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingResultatType.AVSLÅTT,
            Avslagsårsak.MANGLENDE_DOKUMENTASJON,
            VedtakResultatType.AVSLAG,
            DATO_FØR_INNSENDINGSFRISTEN);
        MottattDokument søknadDokument = dummySøknadDokument(behandling);

        // Act
        dokumentmottakerSøknad.mottaDokument(søknadDokument, behandling.getFagsak(), søknadDokument.getDokumentTypeId(), BehandlingÅrsakType.RE_ANNET);

        // Assert
        Mockito.verify(behandlingsoppretterSpied, Mockito.times(1)).opprettNyFørstegangsbehandling(Mockito.any(), Mockito.any(), Mockito.any());
    }

}
