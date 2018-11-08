package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioFarSøkerEngangsstønad;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.DokumentPersistererTjeneste;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.MottattDokumentWrapper;
import no.nav.foreldrepenger.domene.mottak.søknad.SoeknadsskjemaEngangsstoenadTestdataBuilder;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.SoeknadsskjemaEngangsstoenadContants;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.ObjectFactory;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;

public class MottatteDokumentTjenesteTest {
    private MottatteDokumentRepository mottatteDokumentRepository;
    private MottatteDokumentTjeneste mottatteDokumentTjeneste;

    @Before
    public void before() {
        final int FRIST_INNSENDING_UKER = 6;
        mottatteDokumentRepository = mock(MottatteDokumentRepository.class);
        DokumentPersistererTjeneste dokumentPersistererTjeneste = mock(DokumentPersistererTjeneste.class);
        BehandlingRepositoryProvider behandlingRepositoryProviderMock = Mockito.mock(BehandlingRepositoryProvider.class);
        mottatteDokumentTjeneste = new MottatteDokumentTjenesteImpl(FRIST_INNSENDING_UKER, dokumentPersistererTjeneste, mottatteDokumentRepository, behandlingRepositoryProviderMock);

        MottattDokumentWrapper<?, ?> dokumentWrapper = mock(MottattDokumentWrapper.class);
        when(dokumentPersistererTjeneste.xmlTilWrapper(any(MottattDokument.class))).thenReturn(dokumentWrapper);
        when(dokumentWrapper.getVedleggSkjemanummer()).thenReturn(emptyList());
    }

    @Test
    public void skal_lagre_dokument_i_mottatte_dokument() throws Exception {
        // Arrange
        ScenarioFarSøkerEngangsstønad scenario = ScenarioFarSøkerEngangsstønad.forAdopsjon();
        Behandling behandling = scenario.lagMocked();
        LocalDate enDag = LocalDate.of(2017, 1, 1);
        long fagsakId = scenario.getFagsak().getId();

        SoeknadsskjemaEngangsstoenad skjema = new SoeknadsskjemaEngangsstoenadTestdataBuilder()
            .søknadAdopsjonEngangsstønadFar()
            .medOppholdNorgeNå(true)
            .medFarsNavn("Fornavn", "Etternavn")
            .medOmsorgsovertakelsesdato(enDag)
            .medFødselsdatoer(singletonList(enDag))
            .build();
        String søknadXml;
        try {
            søknadXml = JaxbHelper.marshalAndValidateJaxb(SoeknadsskjemaEngangsstoenadContants.JAXB_CLASS,
                new ObjectFactory().createSoeknadsskjemaEngangsstoenad(skjema),
                SoeknadsskjemaEngangsstoenadContants.XSD_LOCATION);
        } catch (JAXBException | SAXException e) {
            throw new IllegalStateException("Ugyldig marshalling (skal ikke kunne havne her.)", e);
        }
        JournalpostId journalpostId = new JournalpostId("123");
        MottattDokument mottattDokument = new MottattDokument.Builder()
            .medFagsakId(fagsakId)
            .medDokumentTypeId(DokumentTypeId.DOKUMENTASJON_AV_OMSORGSOVERTAKELSE)
            .medJournalPostId(journalpostId)
            .medMottattDato(enDag)
            .medXmlPayload(søknadXml)
            .medElektroniskRegistrert(true)
            .build();

        when(mottatteDokumentRepository.lagre(mottattDokument)).thenReturn(new MottattDokument.Builder(mottattDokument).medId(987L).build());
        when(mottatteDokumentRepository.hentMottattDokument(987L)).thenReturn(Optional.of(new MottattDokument.Builder(mottattDokument).medId(987L).build()));

        // Act
        Long dokumentId = mottatteDokumentTjeneste.lagreMottattDokumentPåFagsak(fagsakId, mottattDokument);

        Optional<MottattDokument> mottattDokument1 = mottatteDokumentTjeneste.hentMottattDokument(dokumentId);

        // Assert
        assertThat(mottattDokument1.get().getJournalpostId()).isEqualTo(journalpostId);
        assertThat(mottattDokument1.get().getDokumentTypeId()).isEqualTo(DokumentTypeId.DOKUMENTASJON_AV_OMSORGSOVERTAKELSE);
        assertThat(mottattDokument1.get().getMottattDato()).isEqualTo(enDag);
        assertThat(mottattDokument1.get().getPayloadXml().replaceAll("\\s+","")).isEqualTo("<?xmlversion=\"1.0\"encoding=\"UTF-8\"standalone=\"yes\"?><ns2:soeknadsskjemaEngangsstoenadxmlns:ns2=\"http://nav.no/foreldrepenger/soeknadsskjema/engangsstoenad/v1\"><brukerxsi:type=\"ns2:Bruker\"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><personidentifikator>03038005595</personidentifikator></bruker><soknadsvalg><foedselEllerAdopsjon>ADOPSJON</foedselEllerAdopsjon><stoenadstype>ENGANGSSTOENADFAR</stoenadstype></soknadsvalg><rettigheter><grunnlagForAnsvarsovertakelse>adoptererAlene</grunnlagForAnsvarsovertakelse></rettigheter><tilknytningNorge><oppholdNorgeNaa>true</oppholdNorgeNaa><fremtidigOppholdUtenlands/><tidligereOppholdUtenlands/></tilknytningNorge><opplysningerOmBarn><omsorgsovertakelsedato>2017-01-01+01:00</omsorgsovertakelsedato><foedselsdato>2017-01-01+01:00</foedselsdato><antallBarn>1</antallBarn></opplysningerOmBarn><opplysningerOmFar><fornavn>Fornavn</fornavn><etternavn>Etternavn</etternavn></opplysningerOmFar><opplysningerOmMor><fornavn>Synt18</fornavn><etternavn>Hansen</etternavn><personidentifikator>07078516261</personidentifikator></opplysningerOmMor><vedleggListe><Vedlegg><skjemanummer>I000041</skjemanummer><innsendingsvalg>LASTET_OPP</innsendingsvalg><erPaakrevdISoeknadsdialog>true</erPaakrevdISoeknadsdialog></Vedlegg></vedleggListe></ns2:soeknadsskjemaEngangsstoenad>");
        assertThat(mottattDokument1.get().getElektroniskRegistrert()).isTrue();
    }
}
