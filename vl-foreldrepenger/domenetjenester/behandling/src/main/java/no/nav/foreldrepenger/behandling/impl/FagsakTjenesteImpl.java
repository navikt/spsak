package no.nav.foreldrepenger.behandling.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.FagsakTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKodeverkRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonRelasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEventPubliserer;
import no.nav.foreldrepenger.behandlingslager.fagsak.Journalpost;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@ApplicationScoped
public class FagsakTjenesteImpl implements FagsakTjeneste {


    private FagsakRepository fagsakRepository;
    private FagsakStatusEventPubliserer fagsakStatusEventPubliserer;
    private NavBrukerKodeverkRepository navBrukerKodeverkRepository;
    private SøknadRepository søknadRepository;

    FagsakTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public FagsakTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                              FagsakStatusEventPubliserer fagsakStatusEventPubliserer) {
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.fagsakStatusEventPubliserer = fagsakStatusEventPubliserer;
        this.navBrukerKodeverkRepository = new NavBrukerKodeverkRepositoryImpl(repositoryProvider.getKodeverkRepository());
        this.søknadRepository = repositoryProvider.getSøknadRepository();
    }


    /**
     * @deprecated Skal ikke lenger oppdatere fagsaks relasjonsrolle basert på registerdata, men sette det ut fra
     * oppgitte data i søknad (steg { {@link BehandlingStegType#REGISTRER_SØKNAD}} )
     * Likevel finnes et unntak som gjør at metoden ikke kan fjernes: gammelt søknadformat angir ikke relasjonsrolle.
     */
    @Deprecated
    @Override
    public void oppdaterFagsak(Behandling behandling, PersonopplysningerAggregat personopplysninger, List<Personopplysning> barnSøktStønadFor) {

        Fagsak fagsak = behandling.getFagsak();
        validerEksisterendeFagsak(fagsak);

        // Oppdatering basert på søkers oppgitte relasjon til barn
        Optional<RelasjonsRolleType> oppgittRelasjonsRolle = søknadRepository.hentSøknadHvisEksisterer(behandling)
            .map(Søknad::getRelasjonsRolleType);
        if (oppgittRelasjonsRolle.isPresent()) {
            fagsakRepository.oppdaterRelasjonsRolle(fagsak.getId(), oppgittRelasjonsRolle.get());
            return;
        }

        // Oppdatering basert på søkers registrerte relasjon til barn
        Optional<PersonRelasjon> funnetRelasjon = finnBarnetsRelasjonTilSøker(barnSøktStønadFor, personopplysninger);
        if (funnetRelasjon.isPresent()) {
            Optional<RelasjonsRolleType> brukerRolle = navBrukerKodeverkRepository.finnBrukerRolle(funnetRelasjon.get().getRelasjonsrolle().getKode());
            if(brukerRolle.isPresent()) {
                fagsakRepository.oppdaterRelasjonsRolle(fagsak.getId(), brukerRolle.get());
            }
        }
    }

    @Override
    public void opprettFagsak(Fagsak nyFagsak, Personinfo personInfo) {
        validerNyFagsak(nyFagsak);
        fagsakRepository.opprettNy(nyFagsak);
        if (fagsakStatusEventPubliserer != null) {
            fagsakStatusEventPubliserer.fireEvent(nyFagsak, nyFagsak.getStatus());
        }
    }

    private void validerNyFagsak(Fagsak fagsak) {
        if (fagsak.getId() != null || !Objects.equals(fagsak.getStatus(), FagsakStatus.OPPRETTET)) {
            throw new IllegalArgumentException("Kan ikke kalle opprett fagsak med eksisterende: " + fagsak); //$NON-NLS-1$
        }
    }

    private void validerEksisterendeFagsak(Fagsak fagsak) {
        if (fagsak.getId() == null || Objects.equals(fagsak.getStatus(), FagsakStatus.OPPRETTET)) {
            throw new IllegalArgumentException("Kan ikke kalle oppdater med ny fagsak: " + fagsak); //$NON-NLS-1$
        }
    }

    /**
     * kun til test bruk .
     */
    void oppdaterFagsak(Fagsak fagsak) {
        validerEksisterendeFagsak(fagsak);

        fagsakRepository.opprettNy(fagsak);
    }

    @Override
    public Optional<Fagsak> finnFagsakGittSaksnummer(Saksnummer saksnummer, boolean taSkriveLås) {
        return fagsakRepository.hentSakGittSaksnummer(saksnummer, taSkriveLås);
    }

    @Override
    public Fagsak finnEksaktFagsak(long fagsakId) {
        return fagsakRepository.finnEksaktFagsak(fagsakId);
    }

    @Override
    public void oppdaterFagsakMedGsakSaksnummer(Long fagsakId, Saksnummer saksnummer) {
        fagsakRepository.oppdaterSaksnummer(fagsakId, saksnummer);
    }

    @Override
    public void lagreJournalPost(Journalpost journalpost) {
        fagsakRepository.lagre(journalpost);
    }

    @Override
    public Optional<Journalpost> hentJournalpost(JournalpostId journalpostId) {
        return fagsakRepository.hentJournalpost(journalpostId);
    }

    private Optional<PersonRelasjon> finnBarnetsRelasjonTilSøker(List<Personopplysning> barnaSøktStøtteFor, PersonopplysningerAggregat personopplysningerAggregat) {

        Optional<Personopplysning> barn = personopplysningerAggregat.getBarna().stream()
            .filter(e -> barnaSøktStøtteFor.stream()
                .anyMatch(kandidat -> kandidat.getAktørId().equals(e.getAktørId()))).findFirst();

        if(barn.isPresent()) {
            return personopplysningerAggregat.finnRelasjon(barn.get().getAktørId(), personopplysningerAggregat.getSøker().getAktørId());
        }
        return Optional.empty();
    }

}
