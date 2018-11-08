package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.util;

import java.time.LocalDate;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKobling;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveBehandlingKoblingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InnhentDokumentTjeneste;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

@Dependent
public class DokumentmottakTestUtil {

    private static final String BEHANDLENDE_ENHET = "1234";

    private BehandlingRepository behandlingRepository;


    private OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository;

    private InnhentDokumentTjeneste innhentDokumentTjeneste;

    private MottatteDokumentRepository mottatteDokumentRepository;

    public DokumentmottakTestUtil() {
        // For CDI
    }

    @Inject
    public DokumentmottakTestUtil(BehandlingRepository behandlingRepository,
                                  OppgaveBehandlingKoblingRepository oppgaveBehandlingKoblingRepository,
                                  InnhentDokumentTjeneste innhentDokumentTjeneste,
                                  MottatteDokumentRepository mottatteDokumentRepository) {
        this.behandlingRepository = behandlingRepository;
        this.oppgaveBehandlingKoblingRepository = oppgaveBehandlingKoblingRepository;
        this.innhentDokumentTjeneste = innhentDokumentTjeneste;
        this.mottatteDokumentRepository = mottatteDokumentRepository;
    }

    public Long byggBehandling(Fagsak fagsak, Soeknad søknad) {
        // Settes til false for å bypasse vilkår om søkers opplysningsplikt. Bør vurdere heller å oppgi komplett søknad
        boolean elektroniskSøknad = Boolean.FALSE;
        return byggBehandling(fagsak, søknad, true, elektroniskSøknad);
    }

    public Long byggBehandling(Fagsak fagsak, Soeknad søknad, boolean lagreOppgaveBehandlingKobling, boolean elektroniskSøknad) {
        String xml = IntegrasjonstestUtils.lagSøknadXml(søknad);
        LocalDate mottattDato = DateUtil.convertToLocalDate(søknad.getMottattDato());
        Behandling behandling = opprettDokumentOgSendTilMottak(fagsak, xml, elektroniskSøknad, mottattDato);

        if (lagreOppgaveBehandlingKobling) {
            lagreOppgaveBehandlingKobling(fagsak, behandling);
        }
        setBehandlendeEnhet(behandling);
        return behandling.getId();
    }

    public Long byggBehandlingGammeltSøknadsformat(Fagsak fagsak, SoeknadsskjemaEngangsstoenad søknad) {
        String xml = IntegrasjonstestUtils.lagSøknadXml(søknad);
        LocalDate mottattDato = DateUtil.convertToLocalDate(søknad.getOpplysningerOmBarn().getFoedselsdato().get(0));

        Behandling behandling = opprettDokumentOgSendTilMottak(fagsak, xml, false, mottattDato);

        lagreOppgaveBehandlingKobling(fagsak, behandling);
        setBehandlendeEnhet(behandling);

        return behandling.getId();
    }

    private Behandling opprettDokumentOgSendTilMottak(Fagsak fagsak, String xml, boolean elektroniskSøknad, LocalDate mottattDato) {
        MottattDokument mottattDokument = mottatteDokumentRepository.lagre(lagMottattDokument(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, fagsak.getId(), xml,
            mottattDato, elektroniskSøknad, null));
        innhentDokumentTjeneste.utfør(mottattDokument, BehandlingÅrsakType.UDEFINERT);

        Behandling behandling = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId())
            .orElseThrow(() -> new IllegalStateException("Feilet oppretting av behandling ved testoppsett for mottak av søknad"));

        return behandling;
    }

    private void lagreOppgaveBehandlingKobling(Fagsak fagsak, Behandling behandling) {
        Saksnummer saksnummer = fagsak.getSaksnummer();
        OppgaveBehandlingKobling oppgaveBehandlingKobling = new OppgaveBehandlingKobling(OppgaveÅrsak.BEHANDLE_SAK, "1234", saksnummer, behandling);
        oppgaveBehandlingKoblingRepository.lagre(oppgaveBehandlingKobling);
    }

    private void setBehandlendeEnhet(Behandling behandling) {
        // Må settes for å generere gyldig vedtak.xml
        behandling.setBehandlendeEnhet(new OrganisasjonsEnhet(BEHANDLENDE_ENHET, null));
        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, behandlingLås);
    }

    public static MottattDokument lagMottattDokument(DokumentTypeId dokumentTypeId, Long fagsakId, String xml, LocalDate mottattDato, boolean elektroniskRegistrert, String journalpostId) {
        MottattDokument.Builder builder = new MottattDokument.Builder();
        builder.medDokumentTypeId(dokumentTypeId);
        builder.medMottattDato(mottattDato);
        builder.medXmlPayload(xml);
        builder.medElektroniskRegistrert(elektroniskRegistrert);
        builder.medFagsakId(fagsakId);
        if (journalpostId != null) {
            builder.medJournalPostId(new JournalpostId(journalpostId));
        }
        return builder.build();
    }
}
