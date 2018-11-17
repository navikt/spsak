package no.nav.foreldrepenger.web.app.tjenester.dokument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavPersoninfoBuilder;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivDokument;
import no.nav.foreldrepenger.domene.dokumentarkiv.ArkivJournalPost;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.web.app.tjenester.dokument.dto.DokumentDto;
import no.nav.foreldrepenger.web.app.tjenester.fagsak.dto.SaksnummerDto;
import no.nav.vedtak.felles.testutilities.Whitebox;

@SuppressWarnings("deprecation")
public class DokumentRestTjenesteTest {

    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private FagsakRepository fagsakRepository;
    private MottatteDokumentRepository mottatteDokumentRepository;
    private DokumentRestTjeneste tjeneste;

    @Before
    public void setUp() throws Exception {
        dokumentArkivTjeneste = mock(DokumentArkivTjeneste.class);
        inntektArbeidYtelseTjeneste = mock(InntektArbeidYtelseTjeneste.class);
        fagsakRepository = mock(FagsakRepository.class);
        mottatteDokumentRepository = mock(MottatteDokumentRepository.class);
        tjeneste = new DokumentRestTjeneste(dokumentArkivTjeneste, inntektArbeidYtelseTjeneste, fagsakRepository, mottatteDokumentRepository);
    }

    @Test
    public void skal_gi_tom_liste_ved_ikkeeksisterende_saksnummer() throws Exception {
        when(fagsakRepository.hentSakGittSaksnummer(any())).thenReturn(Optional.empty());
        final Collection<DokumentDto> response = tjeneste.hentAlleDokumenterForSak(new SaksnummerDto("123456"));
        assertThat(response).isEmpty();
    }


    @Test
    public void skal_returnere_to_dokument() throws Exception {
        Long fagsakId = 5L;
        Long behandlingId = 150L;
        new PersonIdent("12345678901");
        AktørId aktørId = new AktørId("1");
        Personinfo personinfo = new NavPersoninfoBuilder().medAktørId(aktørId).medDiskresjonskode("6").medPersonstatusType(PersonstatusType.DØD).build();
        NavBruker navBruker = new NavBrukerBuilder().medPersonInfo(personinfo).build();
        Fagsak fagsak = FagsakBuilder.nyForeldrepengerForMor()
            .medBruker(navBruker)
            .medSaksnummer(new Saksnummer("123456"))
            .build();
        Whitebox.setInternalState(fagsak, "id", fagsakId);
        when(fagsakRepository.hentSakGittSaksnummer(any())).thenReturn(Optional.of(fagsak));

        ArkivDokument søknad = new ArkivDokument();
        søknad.setTittel("Søknad");
        søknad.setDokumentId("456");
        søknad.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        ArkivJournalPost søknadJP = new ArkivJournalPost();
        søknadJP.setJournalpostId(new JournalpostId("123"));
        søknadJP.setTidspunkt(LocalDate.now().minusDays(6));
        søknadJP.setHovedDokument(søknad);

        ArkivDokument vedlegg = new ArkivDokument();
        søknad.setTittel("vedlegg");
        søknad.setDokumentId("123");
        søknad.setDokumentTypeId(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
        ArkivJournalPost søknadV = new ArkivJournalPost();
        søknadV.setJournalpostId(new JournalpostId("125"));
        søknadV.setHovedDokument(vedlegg);

        ArkivDokument im = new ArkivDokument();
        im.setTittel("Inntektsmelding");
        im.setDokumentId("789");
        im.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        ArkivJournalPost imJP = new ArkivJournalPost();
        imJP.setJournalpostId(new JournalpostId("124"));
        imJP.setTidspunkt(LocalDate.now().minusDays(4));
        imJP.setHovedDokument(im);

        when(dokumentArkivTjeneste.hentAlleDokumenterForVisning(any())).thenReturn(Arrays.asList(søknadJP, søknadV, imJP));

        MottattDokument mds = new MottattDokument.Builder().medId(1001L).medJournalPostId(new JournalpostId("123"))
            .medDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).medFagsakId(fagsakId).medBehandlingId(behandlingId).build();
        MottattDokument mdim = new MottattDokument.Builder().medId(1002L).medJournalPostId(new JournalpostId("124"))
            .medDokumentTypeId(DokumentTypeId.INNTEKTSMELDING).medFagsakId(fagsakId).medBehandlingId(behandlingId).build();
        when(mottatteDokumentRepository.hentMottatteDokumentMedFagsakId(fagsakId)).thenReturn(Arrays.asList(mdim, mds));

        String vnavn = "Sinsen Septik og Snarmat";
        VirksomhetEntitet sinsen = new VirksomhetEntitet.Builder().medNavn(vnavn).medOrgnr("789101112").build();
        Inntektsmelding imelda = InntektsmeldingBuilder.builder().medVirksomhet(sinsen).medMottattDokument(mdim).medInnsendingstidspunkt(LocalDateTime.now()).build();

        when(inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerForFagsak(any())).thenReturn(Collections.singletonList(imelda));


        final Collection<DokumentDto> response = tjeneste.hentAlleDokumenterForSak(new SaksnummerDto("123456"));
        assertThat(response).hasSize(3);

        assertThat(response.iterator().next().getTidspunkt()).isNull();
        Optional<DokumentDto> imdto = response.stream().filter(dto -> dto.getGjelderFor() != null).findAny();
        assertThat(imdto).isPresent();
        assertThat(imdto.get().getGjelderFor()).isEqualTo(vnavn);
        assertThat(imdto.get().getBehandlinger()).hasSize(1);
    }


}
