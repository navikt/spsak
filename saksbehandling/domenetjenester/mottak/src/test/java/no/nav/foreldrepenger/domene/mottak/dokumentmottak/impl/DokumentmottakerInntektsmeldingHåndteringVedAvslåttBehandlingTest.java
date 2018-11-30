package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;

public class DokumentmottakerInntektsmeldingHåndteringVedAvslåttBehandlingTest extends DokumentmottakerTestsupport {

    private DokumentmottakerInntektsmelding dokumentmottakerInntektsmelding;
    private Behandlingsoppretter behandlingsoppretterSpied;
    private DokumentmottakerFelles dokumentmottakerFellesSpied;

    @Before
    public void setup() {
        this.behandlingsoppretterSpied = Mockito.spy(behandlingsoppretter);
        this.dokumentmottakerFellesSpied = Mockito.spy(dokumentmottakerFelles);

        dokumentmottakerInntektsmelding = new DokumentmottakerInntektsmelding(
            dokumentmottakerFellesSpied,
            mottatteDokumentTjeneste,
            behandlingsoppretterSpied,
            kompletthetskontroller,
            repositoryProvider);
    }

    @Test
    public void gittAvslåttBehandlingPgaManglendeDokMedIkkeUtløptFristForInnsendingSkalOppretteNyFørstegangsbehandling() {
        //Arrange
        Mockito.doNothing().when(behandlingsoppretterSpied).opprettNyFørstegangsbehandling(Mockito.any(), Mockito.any(), Mockito.any());
        Behandling behandling = opprettBehandling(
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingResultatType.AVSLÅTT,
            Avslagsårsak.MANGLENDE_DOKUMENTASJON,
            VedtakResultatType.AVSLAG,
            DATO_FØR_INNSENDINGSFRISTEN);
        InngåendeSaksdokument inntektsmelding = dummyInntektsmeldingDokument(behandling);

        // Act
        dokumentmottakerInntektsmelding.mottaDokument(inntektsmelding, behandling.getFagsak(), inntektsmelding.getDokumentTypeId(), BehandlingÅrsakType.RE_ANNET);

        // Assert
        Mockito.verify(behandlingsoppretterSpied, Mockito.times(1)).opprettNyFørstegangsbehandling(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void gittAvslåttBehandlingPgaManglendeDokMedUtløptFristForInnsendingSkalOppretteTaskForÅVurdereDokument() {
        //Arrange
        Mockito.doNothing().when(dokumentmottakerFellesSpied).opprettTaskForÅVurdereDokument(Mockito.any(), Mockito.any(), Mockito.any());
        Behandling behandling = opprettBehandling(
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingResultatType.AVSLÅTT,
            Avslagsårsak.MANGLENDE_DOKUMENTASJON,
            VedtakResultatType.AVSLAG,
            DATO_ETTER_INNSENDINGSFRISTEN);
        InngåendeSaksdokument inntektsmelding = dummyInntektsmeldingDokument(behandling);

        // Act
        dokumentmottakerInntektsmelding.mottaDokument(inntektsmelding, behandling.getFagsak(), inntektsmelding.getDokumentTypeId(), BehandlingÅrsakType.RE_ANNET);

        // Assert
        Mockito.verify(behandlingsoppretterSpied, Mockito.never()).opprettNyFørstegangsbehandling(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dokumentmottakerFellesSpied, Mockito.times(1)).opprettTaskForÅVurdereDokument(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void gittAvslåttBehandlingMenIkkePgaManglendeDokMedSkalOppretteTaskForÅVurdereDokument() {
        //Arrange
        Mockito.doNothing().when(dokumentmottakerFellesSpied).opprettTaskForÅVurdereDokument(Mockito.any(), Mockito.any(), Mockito.any());
        Behandling behandling = opprettBehandling(
            FagsakYtelseType.FORELDREPENGER,
            BehandlingType.FØRSTEGANGSSØKNAD,
            BehandlingResultatType.AVSLÅTT,
            Avslagsårsak.FOR_LAVT_BEREGNINGSGRUNNLAG,
            VedtakResultatType.AVSLAG,
            DATO_ETTER_INNSENDINGSFRISTEN);
        InngåendeSaksdokument inntektsmelding = dummyInntektsmeldingDokument(behandling);

        // Act
        dokumentmottakerInntektsmelding.mottaDokument(inntektsmelding, behandling.getFagsak(), inntektsmelding.getDokumentTypeId(), BehandlingÅrsakType.RE_ANNET);

        // Assert
        Mockito.verify(behandlingsoppretterSpied, Mockito.never()).opprettNyFørstegangsbehandling(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.verify(dokumentmottakerFellesSpied, Mockito.times(1)).opprettTaskForÅVurdereDokument(Mockito.any(), Mockito.any(), Mockito.any());
    }

}
