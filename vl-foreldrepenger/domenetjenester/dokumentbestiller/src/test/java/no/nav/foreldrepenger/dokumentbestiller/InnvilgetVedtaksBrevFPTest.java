package no.nav.foreldrepenger.dokumentbestiller;

import static no.nav.foreldrepenger.dokumentbestiller.DokumentBestillerApplikasjonTjenesteTestUtil.buildBeregningsresultatFP;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.w3c.dom.Node;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.InnvilgelseForeldrepengerDokument;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillVedtakBrevDto;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastRequest;
import no.nav.tjeneste.virksomhet.dokumentproduksjon.v2.meldinger.ProduserDokumentutkastResponse;
import no.nav.vedtak.felles.testutilities.Whitebox;

public class InnvilgetVedtaksBrevFPTest extends DokumentBestillerApplikasjonTjenesteImplFPOppsett {

    @Test
    public void forhandsvisInnvilgetFPVedtaksbrevTest() {
        // arrange
        Whitebox.setInternalState(behandling, "behandlingType", BehandlingType.FØRSTEGANGSSØKNAD);
        // arrange

        FagsakRelasjon fagsakRelasjon = mock(FagsakRelasjon.class);
        when(fagsakRelasjonRepository.finnRelasjonFor(Mockito.any())).thenReturn(fagsakRelasjon);
        when(fagsakRelasjon.getStønadskontoberegning()).thenReturn(Optional.empty());

        UttakResultatEntitet uttakResultat = new UttakResultatEntitet.Builder(scenario.getBehandling().getBehandlingsresultat()).build();

        UttakResultatPeriodeEntitet uttakResultatPeriode = new UttakResultatPeriodeEntitet.Builder(SKJÆRINGSTIDSPUNKT_BEREGNING, SKJÆRINGSTIDSPUNKT_BEREGNING.plusMonths(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT).build();
        UttakResultatPerioderEntitet perioder = new UttakResultatPerioderEntitet();
        perioder.leggTilPeriode(uttakResultatPeriode);
        uttakResultat.setOverstyrtPerioder(perioder);

        when(uttakRepository.hentUttakResultatHvisEksisterer(behandling)).thenReturn(Optional.of(uttakResultat));
        when(beregningsresultatFPRepository.hentBeregningsresultatFP(behandling)).thenReturn(Optional.of(buildBeregningsresultatFP(true, virksomhet)));
        when(beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling)).thenReturn(Optional.of(beregningsgrunnlag));
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(behandling);
        DokumentMalType dokumentMalMock = mock(DokumentMalType.class);
        when(dokumentMalMock.getKode()).thenReturn(DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK);
        when(dokumentRepository.hentDokumentMalType(DokumentMalType.INNVILGELSE_FORELDREPENGER_DOK)).thenReturn(dokumentMalMock);

        mockHentDokumentData(new InnvilgelseForeldrepengerDokument(brevParametere));

        ProduserDokumentutkastResponse produserDokumentutkastResponse = new ProduserDokumentutkastResponse();
        ArgumentCaptor<ProduserDokumentutkastRequest> captor = ArgumentCaptor
            .forClass(ProduserDokumentutkastRequest.class);
        when(dokumentproduksjonConsumer.produserDokumentutkast(captor.capture()))
            .thenReturn(produserDokumentutkastResponse);

        // act
        BestillVedtakBrevDto bestillVedtakBrevDto = new BestillVedtakBrevDto(DOKUMENT_DATA_ID, "vedtak fritekst");
        bestillVedtakBrevDto.setSkalBrukeOverstyrendeFritekstBrev(false);
        dokumentBestillerApplikasjonTjeneste.forhandsvisVedtaksbrev(bestillVedtakBrevDto, (b -> false));

        // assert
        document = ((Node) captor.getValue().getBrevdata()).getOwnerDocument();
        assertDokumentFelles();
        checkDokumentValue("klageFristUker", "6");
    }
}
