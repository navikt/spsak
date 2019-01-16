package no.nav.foreldrepenger.web.app.tjenester.fordeling.sak;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.aktør.BrukerTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSAktor;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakRequest;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSOpprettSakResponse;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSak;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSakEksistererAlleredeException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandlesak.v2.WSUgyldigInputException;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.felles.integrasjon.behandlesak.klient.BehandleSakConsumer;
import no.nav.vedtak.felles.integrasjon.sak.SakConsumer;
import no.nav.vedtak.felles.jpa.Transaction;

@ApplicationScoped
@Transaction
/* HACK (u139158): Transaksjonsgrensen her er flyttet hit fra webservice'en OpprettSakService
 * Dette er ikke i henhold til standard og kan ikke gjøres uten godkjenning fra sjefsarkitekt.
 * Grunnen for at det er gjort her er for å sikre at de tre kallene går i separate transaksjoner.
 * Se https://jira.adeo.no/browse/PKHUMLE-359 for detaljer.
 */
public class OpprettSakTjenesteImpl implements OpprettSakTjeneste {

    private Logger logger = LoggerFactory.getLogger(OpprettSakTjenesteImpl.class);

    private TpsTjeneste tpsTjeneste;
    private FagsakTjeneste fagsakTjeneste;
    private BehandleSakConsumer behandleSakConsumer;
    private SakConsumer sakConsumer;
    private BrukerTjeneste brukerTjeneste;

    public OpprettSakTjenesteImpl() {
        //For CDI
    }

    @Inject
    public OpprettSakTjenesteImpl(TpsTjeneste tpsTjeneste, FagsakTjeneste fagsakTjeneste,
                                  BehandleSakConsumer behandleSakConsumer, SakConsumer sakConsumer, BrukerTjeneste brukerTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
        this.fagsakTjeneste = fagsakTjeneste;
        this.behandleSakConsumer = behandleSakConsumer;
        this.sakConsumer = sakConsumer;
        this.brukerTjeneste = brukerTjeneste;
    }

    @Override
    public Fagsak opprettSakVL(AktørId aktørId) {
        Optional<Personinfo> personinfoOptional = tpsTjeneste.hentBrukerForAktør(aktørId);

        if (!personinfoOptional.isPresent()) {
            throw OpprettSakFeil.FACTORY.finnerIkkePersonMedAktørId(aktørId).toException();
        }
        Personinfo personinfo = personinfoOptional.get();

        NavBruker bruker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        Fagsak fagsak = Fagsak.opprettNy(bruker);
        fagsakTjeneste.opprettFagsak(fagsak, personinfo);

        return fagsak;
    }

    @Override
    public Fagsak opprettSakVL(AktørId aktørId, JournalpostId journalpostId) {
        Optional<Personinfo> personinfoOptional = tpsTjeneste.hentBrukerForAktør(aktørId);

        if (!personinfoOptional.isPresent()) {
            throw OpprettSakFeil.FACTORY.finnerIkkePersonMedAktørId(aktørId).toException();
        }
        Personinfo personinfo = personinfoOptional.get();

        NavBruker bruker = brukerTjeneste.hentEllerOpprettFraAktorId(personinfo);
        Fagsak fagsak = Fagsak.opprettNy(bruker);
        fagsakTjeneste.opprettFagsak(fagsak, personinfo);
        knyttFagsakOgJournalpost(fagsak.getId(), journalpostId);

        return fagsak;
    }

    @Override
    public Saksnummer opprettSakIGsak(Long fagsakId, AktørId aktorId) {
        Optional<Personinfo> personinfoOptional = tpsTjeneste.hentBrukerForAktør(aktorId);

        if (!personinfoOptional.isPresent()) {
            throw OpprettSakFeil.FACTORY.finnerIkkePersonMedAktørId(aktorId).toException();
        }
        Personinfo personinfo = personinfoOptional.get();

        WSSak sak = new WSSak();
        sak.setFagomrade(FORELDREPENGER_KODE);
        sak.setSaktype(MED_FAGSAK_KODE);

        WSAktor aktoer = new WSAktor();
        aktoer.setIdent(personinfo.getPersonIdent().getIdent());
        sak.getGjelderBrukerListe().add(aktoer);

        sak.setFagsystem(VL_FAGSYSTEM_KODE);

        WSOpprettSakRequest opprettSakRequest = new WSOpprettSakRequest();
        opprettSakRequest.setSak(sak);

        try {
            WSOpprettSakResponse response = behandleSakConsumer.opprettSak(opprettSakRequest);
            logger.info(removeLineBreaks("Sak opprettet i GSAK med saksnummer: {}"), removeLineBreaks(response.getSakId())); //NOSONAR
            return new Saksnummer(response.getSakId());
        } catch (WSSakEksistererAlleredeException e) {
            throw OpprettSakFeil.FACTORY.kanIkkeOppretteIGsakFordiSakAlleredeEksisterer(e).toException();
        } catch (WSUgyldigInputException opprettSakUgyldigInput) {
            throw OpprettSakFeil.FACTORY.kanIkkeOppretteIGsakFordiInputErUgyldig(opprettSakUgyldigInput).toException();
        } catch (WSSikkerhetsbegrensningException e) {
            throw OpprettSakFeil.FACTORY.opprettSakSikkerhetsbegrensning(e).toException();
        }
    }

    @Override
    public Optional<Saksnummer> finnGsak(Long fagsakId) {
        FinnSakRequest finnSakRequest = new FinnSakRequest();

        no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagsystemer fagsystem = new no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagsystemer();
        fagsystem.setValue(VL_FAGSYSTEM_KODE);
        finnSakRequest.setFagsystem(fagsystem);

        String saksnummer = VL_FAGSYSTEM_KODE + fagsakId;
        finnSakRequest.setFagsystemSakId(saksnummer);

        try {
            FinnSakResponse response = sakConsumer.finnSak(finnSakRequest);
            int size = response.getSakListe().size();
            switch (size) {
                case 0:
                    return Optional.empty();
                case 1:
                    return Optional.of(new Saksnummer(response.getSakListe().get(0).getSakId()));
                default:
                    // Skal ikke kunne oppstå siden Saksnummer er unik
                    throw OpprettSakFeil.FACTORY.finnSakIkkeUniktResultat(saksnummer, size).toException();
            }
        } catch (FinnSakForMangeForekomster finnSakForMangeForekomster) {
            // Skal ikke kunne oppstå siden Saksnummer er unik
            throw OpprettSakFeil.FACTORY.finnSakForMangeForekomster(saksnummer, finnSakForMangeForekomster).toException();
        } catch (FinnSakUgyldigInput finnSakUgyldigInput) {
            // Skal ikke kunne oppstå her siden vi setter fagsystem og Saksnummer
            throw OpprettSakFeil.FACTORY.finnSakUgyldigInput(finnSakUgyldigInput).toException();
        }
    }

    @Override
    public void oppdaterFagsakMedGsakSaksnummer(Long fagsakId, Saksnummer saksnummer) {
        fagsakTjeneste.oppdaterFagsakMedGsakSaksnummer(fagsakId, saksnummer);
    }

    @Override
    public void knyttSakOgJournalpost(Saksnummer saksnummer, JournalpostId journalPostId) {

        //Sjekk om det allerede finnes knytning.
        Optional<Journalpost> journalpost = fagsakTjeneste.hentJournalpost(journalPostId);
        if (journalpost.isPresent()) {
            Saksnummer knyttetTilSaksnummer = journalpost.get().getFagsak().getSaksnummer();
            Long fagsakId = journalpost.get().getFagsak().getId();
            if (knyttetTilSaksnummer.equals(saksnummer)) {
                //Vi har knytning mot samme sak. Vi er HAPPY og returnerer herfra.
                return;
            } else {
                //Knyttet til en annen fagsak
                throw OpprettSakFeil.FACTORY.JournalpostAlleredeKnyttetTilAnnenFagsak(journalPostId, fagsakId).toException();
            }
        }

        //HER: Finnes ikke knytnign mellom journalpost og sak. La oss oprpette en:
        Optional<Fagsak> fagsak = fagsakTjeneste.finnFagsakGittSaksnummer(saksnummer, true);
        if (fagsak.isPresent()) {
            fagsakTjeneste.lagreJournalPost(new Journalpost(journalPostId, fagsak.get()));
        } else {
            throw OpprettSakFeil.FACTORY.finnerIkkeFagsakMedSaksnummer(saksnummer).toException();
        }
    }

    private void knyttFagsakOgJournalpost(Long fagsakId, JournalpostId journalpostId) {
        Optional<Journalpost> journalpost = fagsakTjeneste.hentJournalpost(journalpostId);
        if (journalpost.isPresent()) {
            if (journalpost.get().getFagsak().getId().equals(fagsakId)) {
                //Vi har knytning mot samme sak. Vi er HAPPY og returnerer herfra.
                return;
            } else {
                //Knyttet til en annen fagsak
                throw OpprettSakFeil.FACTORY.JournalpostAlleredeKnyttetTilAnnenFagsak(journalpostId, fagsakId).toException();
            }
        }

        //HER: Finnes ikke knytning mellom journalpost og sak. La oss opprette en:
        Fagsak fagsak = fagsakTjeneste.finnEksaktFagsak(fagsakId);
        fagsakTjeneste.lagreJournalPost(new Journalpost(journalpostId, fagsak));
    }

}
