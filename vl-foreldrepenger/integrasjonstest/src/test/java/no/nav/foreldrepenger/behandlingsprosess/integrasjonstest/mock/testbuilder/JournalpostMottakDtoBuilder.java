package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testbuilder;

import static no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId.FLEKSIBELT_UTTAK_FORELDREPENGER;
import static no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId.KLAGE_DOKUMENT;
import static no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util.IntegrasjonstestUtils.lagSøknadXml;

import java.time.LocalDate;
import java.util.Objects;

import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;

public class JournalpostMottakDtoBuilder {

    private static int journalpostId = 0;
    private final KodeverkRepository kodeverkRepository;
    private String dokumenttype;
    private String behandlingTema;
    private String dokumentkategori;
    private String enhetfradok;
    private String søknad;
    private LocalDate forsendelseMottatt = LocalDate.now();
    private Saksnummer saksnummer;

    private JournalpostMottakDtoBuilder(BehandlingRepositoryProvider repositoryProvider) {
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        journalpostId++;
    }

    public static JournalpostMottakDtoBuilder builder(BehandlingRepositoryProvider repositoryProvider) {
        return new JournalpostMottakDtoBuilder(repositoryProvider);
    }

    // Hjelper for inntektsmelding
    public static JournalpostMottakDtoBuilder journalpostInntektsmeldingBuilder(Fagsak fagsak, InntektsmeldingM inntektsmelding, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medDokumentTypeId(INNTEKTSMELDING)
            .medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medSaksnummer(fagsak.getSaksnummer())
            .medSoknad(lagInntektXml(inntektsmelding));
    }

    // Hjelper for søknad
    public static JournalpostMottakDtoBuilder journalpostSøknadBuilder(Fagsak fagsak, Soeknad soeknad, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL)
            .medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medSaksnummer(fagsak.getSaksnummer())
            .medSoknad(lagSøknadXml(soeknad));
    }

    // Hjelper for papirsøknad
    public static JournalpostMottakDtoBuilder journalpostPapirSøknadBuilder(Fagsak fagsak, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL)
            .medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medSaksnummer(fagsak.getSaksnummer());
    }

    // Hjelper for klage
    public static JournalpostMottakDtoBuilder journalpostKlageBuilder(Fagsak fagsak, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medDokumentTypeId(KLAGE_DOKUMENT)
            .medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medSaksnummer(fagsak.getSaksnummer());
    }

    // Hjelper for annet dokument
    public static JournalpostMottakDtoBuilder journalpostUtenMetadataBuilder(Fagsak fagsak, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medSaksnummer(fagsak.getSaksnummer());
    }

    // Hjelper for endringssøknad
    public static JournalpostMottakDtoBuilder journalpostEndringssøknadBuilder(Fagsak fagsak, Soeknad soeknad, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medDokumentTypeId(FORELDREPENGER_ENDRING_SØKNAD)
            .medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medSaksnummer(fagsak.getSaksnummer())
            .medSoknad(lagSøknadXml(soeknad));
    }

    // Hjelper for endringssøknad
    public static JournalpostMottakDtoBuilder journalpostFleksibeltUttakSøknadBuilder(Fagsak fagsak, BehandlingRepositoryProvider repoProvider) {
        return JournalpostMottakDtoBuilder.builder(repoProvider)
            .medDokumentTypeId(FLEKSIBELT_UTTAK_FORELDREPENGER)
            .medBehandlingsTema(BehandlingTema.FORELDREPENGER)
            .medSaksnummer(fagsak.getSaksnummer());
    }

    private static String lagInntektXml(InntektsmeldingM inntektsmeldingM) {
        String xml;
        try {
            xml = JaxbHelper.marshalAndValidateJaxb(InntektsmeldingConstants.JAXB_CLASS,
                new no.seres.xsd.nav.inntektsmelding_m._20180924.ObjectFactory().createMelding(inntektsmeldingM), InntektsmeldingConstants.XSD_LOCATION);
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException("Ugyldig marshalling (skal ikke kunne havne her.)", e);
        }
        return xml;
    }

    public JournalpostMottakDtoBuilder medDokumentTypeId(DokumentTypeId dokumentTypeId) {
        // Kodeverk må oppfriskes fra repo
        DokumentTypeId type = kodeverkRepository.finn(DokumentTypeId.class, dokumentTypeId.getKode());
        Objects.requireNonNull(type);

        this.dokumenttype = type.getOffisiellKode();
        return this;
    }

    public JournalpostMottakDtoBuilder medBehandlingsTema(BehandlingTema behandlingTema) {
        // Kodeverk må oppfriskes fra repo
        this.behandlingTema = kodeverkRepository.finn(BehandlingTema.class, behandlingTema.getKode()).getOffisiellKode();
        return this;
    }

    public JournalpostMottakDto build() {
        JournalpostMottakDto ny = new JournalpostMottakDto(saksnummer.getVerdi(), String.valueOf(journalpostId),
            behandlingTema, dokumenttype, forsendelseMottatt, søknad);
        ny.setJournalForendeEnhet(enhetfradok);
        ny.setDokumentKategoriOffisiellKode(dokumentkategori);
        return ny;
    }

    public JournalpostMottakDtoBuilder medSoknad(String søknad) {
        this.søknad = søknad;
        return this;
    }

    public JournalpostMottakDtoBuilder medSaksnummer(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
        return this;
    }

    public JournalpostMottakDtoBuilder medDokumentKategori(DokumentKategori dokumentKategori) {
        this.dokumentkategori = kodeverkRepository.finn(DokumentKategori.class, dokumentKategori.getKode()).getOffisiellKode();
        return this;
    }

    public JournalpostMottakDtoBuilder medEnhet(String enhet) {
        this.enhetfradok = enhet;
        return this;
    }

    public JournalpostMottakDtoBuilder medForsendelseMottatt(LocalDate forsendelseMottatt) {
        this.forsendelseMottatt = forsendelseMottatt;
        return this;
    }
}
