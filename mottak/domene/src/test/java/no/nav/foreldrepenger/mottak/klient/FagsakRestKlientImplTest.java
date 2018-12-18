package no.nav.foreldrepenger.mottak.klient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTestHelper;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.util.FPDateUtil;

public class FagsakRestKlientImplTest {

    @Mock
    private OidcRestClient oidcRestClient;

    private KodeverkRepository kodeverkRepository = KodeverkTestHelper.getKodeverkRepository();

    private FagsakRestKlientImpl fagsakRestKlientImpl;

    @Test
    public void testInternMapping() {
        LocalDate fødselsdato = FPDateUtil.iDag().plusDays(7);
        List<LocalDate> adopsjonsBarnFødselsdatoer = Arrays.asList(FPDateUtil.iDag().minusYears(1), FPDateUtil.iDag().minusYears(2));
        LocalDate termindato = FPDateUtil.iDag();
        LocalDate omsorgsovertagelsesDato = FPDateUtil.iDag().plusDays(1);
        String saksnummer = "666";
        String aktørId = "0456";
        String årsakInntektsmelding = "Endring";
        String annenPartId = "0789";
        String arbeidsforholdsid = "56654679";
        String virksomhetsnummer = "9825671290712";
        LocalDateTime nå = FPDateUtil.nå();

        oidcRestClient = mock(OidcRestClient.class);
        fagsakRestKlientImpl = new FagsakRestKlientImpl(oidcRestClient, kodeverkRepository, null, null, null, null);
        ArgumentCaptor<VurderFagsystemDto> captor = ArgumentCaptor.forClass(VurderFagsystemDto.class);
        when(oidcRestClient.post(any(), captor.capture(), any())).thenReturn(null);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderVLSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(aktørId);
        dataWrapper.setBarnTermindato(termindato);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setAdopsjonsbarnFodselsdatoer(adopsjonsBarnFødselsdatoer);
        dataWrapper.setBarnTermindato(termindato);
        dataWrapper.setBarnFodselsdato(fødselsdato);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        dataWrapper.setOmsorgsovertakelsedato(omsorgsovertagelsesDato);
        dataWrapper.setÅrsakTilInnsending(årsakInntektsmelding);
        dataWrapper.setAnnenPartId(annenPartId);
        dataWrapper.setSaksnummer(saksnummer);
        dataWrapper.setArbeidsforholdsid(arbeidsforholdsid);
        dataWrapper.setVirksomhetsnummer(virksomhetsnummer);
        dataWrapper.setInntekstmeldingStartdato(fødselsdato);
        dataWrapper.setForsendelseMottattTidspunkt(nå);

        fagsakRestKlientImpl.vurderFagsystem(dataWrapper);
        VurderFagsystemDto dto = captor.getValue();

        assertThat(dto.getAktørId()).isEqualTo(aktørId);
        assertThat(dto.getSaksnummer()).isEqualTo(Optional.of(saksnummer));
        assertThat(dto.getBarnTermindato()).isEqualTo(Optional.of(termindato));
        assertThat(dto.getBarnFodselsdato()).isEqualTo(Optional.of(fødselsdato));
        assertThat(dto.getAdopsjonsBarnFodselsdatoer()).isEqualTo(adopsjonsBarnFødselsdatoer);
        assertThat(dto.getOmsorgsovertakelsedato()).isEqualTo(Optional.of(omsorgsovertagelsesDato));
        assertThat(dto.getÅrsakInnsendingInntektsmelding()).isEqualTo(Optional.of(årsakInntektsmelding));
        assertThat(dto.getAnnenPart()).isEqualTo(Optional.of(annenPartId));
        assertThat(dto.getBehandlingstemaOffisiellKode()).isEqualTo(kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.FORELDREPENGER_ADOPSJON.getKode()).getOffisiellKode());
        assertThat(dto.getVirksomhetsnummer()).isEqualTo(Optional.of(virksomhetsnummer));
        assertThat(dto.getArbeidsforholdsid()).isEqualTo(Optional.of(arbeidsforholdsid));
        assertThat(dto.getStartDatoForeldrepengerInntektsmelding()).isEqualTo(Optional.of(fødselsdato));
        assertThat(dto.getForsendelseMottattTidspunkt()).isEqualTo(Optional.of(nå));
    }

}
