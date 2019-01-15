package no.nav.foreldrepenger.web.server.pdp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.pip.PipBehandlingsData;
import no.nav.foreldrepenger.behandlingslager.pip.PipRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.JournalpostId;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.journal.v3.JournalConsumer;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.AbacFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.util.MdcExtendedLogContext;

@Dependent
@Alternative
@Priority(2)  // HACK - FORDI DENNE KOLLIDERER MED DUMMYREQUESTBUILDER FRA FELLES-SIKKERHET-TESTUTILITIES NÅR VI KJØRER JETTY
public class PdpRequestBuilderImpl implements PdpRequestBuilder {
    private PipRepository pipRepository;
    private AktørConsumerMedCache aktørConsumer;
    private JournalConsumer journalConsumer;
    private static final MdcExtendedLogContext MDC_EXTENDED_LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    public PdpRequestBuilderImpl() {
    }

    @Inject
    public PdpRequestBuilderImpl(PipRepository pipRepository, AktørConsumerMedCache aktørConsumer, JournalConsumer journalConsumer) {
        this.pipRepository = pipRepository;
        this.aktørConsumer = aktørConsumer;
        this.journalConsumer = journalConsumer;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        Optional<Long> behandlingIder = utledBehandlingIder(attributter);
        Optional<PipBehandlingsData> behandlingData = behandlingIder.isPresent()
            ? pipRepository.hentDataForBehandling(behandlingIder.get())
            : Optional.empty();
        Set<Long> fagsakIder = behandlingData.isPresent()
            ? utledFagsakIder(attributter, behandlingData.get())
            : utledFagsakIder(attributter);

        if (behandlingData.isPresent()) {
            validerSamsvarBehandlingOgFagsak(behandlingIder.get(), behandlingData.get().getFagsakId(), fagsakIder);
        }
        if (!fagsakIder.isEmpty()) {
            MDC_EXTENDED_LOG_CONTEXT.remove("fagsak");
            MDC_EXTENDED_LOG_CONTEXT.add("fagsak", fagsakIder.size() == 1 ? fagsakIder.iterator().next().toString() : fagsakIder.toString());
        }
        behandlingIder.ifPresent(behId -> {
            MDC_EXTENDED_LOG_CONTEXT.remove("behandling");
            MDC_EXTENDED_LOG_CONTEXT.add("behandling", behId);
        });

        Set<AktørId> aktørIder = utledAktørIder(attributter, fagsakIder);
        Set<String> fnr = hentAktuelleFødselsnumre(attributter, aktørIder);
        Set<String> aksjonspunktType = pipRepository.hentAksjonspunktTypeForAksjonspunktKoder(attributter.getAksjonspunktKode());
        return behandlingData.isPresent()
            ? lagPdpRequest(attributter, fnr, aksjonspunktType, behandlingData.get())
            : lagPdpRequest(attributter, fnr, aksjonspunktType);
    }

    private static void validerSamsvarBehandlingOgFagsak(Long behandlingId, Long fagsakId, Set<Long> fagsakIder) {
        List<Long> fagsakerSomIkkeErForventet = fagsakIder.stream()
            .filter(f -> !fagsakId.equals(f))
            .collect(Collectors.toList());
        if (!fagsakerSomIkkeErForventet.isEmpty()) {
            throw FeilFactory.create(PdpRequestBuilderFeil.class).ugyldigInputManglerSamsvarBehandlingFagsak(behandlingId, fagsakerSomIkkeErForventet).toException();
        }
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> fnr, Collection<String> aksjonspunktType) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.setToken(attributter.getIdToken());
        pdpRequest.setAction(attributter.getActionType());
        pdpRequest.setResource(attributter.getResource());
        pdpRequest.setFnr(fnr);
        pdpRequest.setAksjonspunktType(aksjonspunktType);
        return pdpRequest;
    }

    private PdpRequest lagPdpRequest(AbacAttributtSamling attributter, Set<String> fnr, Collection<String> aksjonspunktType, PipBehandlingsData behandlingData) {
        PdpRequest pdpRequest = lagPdpRequest(attributter, fnr, aksjonspunktType);
        oversettBehandlingStatus(behandlingData.getBehandligStatus()).ifPresent(pdpRequest::setBehandlingStatus);
        oversettFagstatus(behandlingData.getFagsakStatus()).ifPresent(pdpRequest::setSakstatus);
        behandlingData.getAnsvarligSaksbehandler().ifPresent(pdpRequest::setAnsvarligSaksbehandler);
        return pdpRequest;
    }

    private static Optional<AbacFagsakStatus> oversettFagstatus(String kode) {
        if (kode.equals(FagsakStatus.OPPRETTET.getKode())) {
            return Optional.of(AbacFagsakStatus.OPPRETTET);
        } else if (kode.equals(FagsakStatus.UNDER_BEHANDLING.getKode())) {
            return Optional.of(AbacFagsakStatus.UNDER_BEHANDLING);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<AbacBehandlingStatus> oversettBehandlingStatus(String kode) {
        if (kode.equals(BehandlingStatus.OPPRETTET.getKode())) {
            return Optional.of(AbacBehandlingStatus.OPPRETTET);
        } else if (kode.equals(BehandlingStatus.UTREDES.getKode())) {
            return Optional.of(AbacBehandlingStatus.UTREDES);
        } else if (kode.equals(BehandlingStatus.FATTER_VEDTAK.getKode())) {
            return Optional.of(AbacBehandlingStatus.FATTE_VEDTAK);
        } else {
            return Optional.empty();
        }
    }


    private Optional<Long> utledBehandlingIder(AbacAttributtSamling attributter) {
        Set<Long> behandlingsIder = new HashSet<>();
        behandlingsIder.addAll(attributter.getBehandlingsIder());
        behandlingsIder.addAll(pipRepository.behandlingsIdForOppgaveId(attributter.getOppgaveIder()));
        behandlingsIder.addAll(pipRepository.behandlingsIdForDokumentDataId(attributter.getDokumentDataIDer()));

        if (behandlingsIder.isEmpty()) {
            return Optional.empty();
        } else if (behandlingsIder.size() == 1) {
            return Optional.of(behandlingsIder.iterator().next());
        }
        throw FeilFactory.create(PdpRequestBuilderFeil.class).ugyldigInputFlereBehandlingIder(behandlingsIder).toException();
    }

    private Set<Long> utledFagsakIder(AbacAttributtSamling attributter, PipBehandlingsData behandlingData) {
        Set<Long> fagsaker = utledFagsakIder(attributter);
        fagsaker.add(behandlingData.getFagsakId());
        return fagsaker;
    }

    private Set<Long> utledFagsakIder(AbacAttributtSamling attributter) {
        Set<Long> fagsakIder = new HashSet<>();
        fagsakIder.addAll(attributter.getFagsakIder());
        fagsakIder.addAll(pipRepository.fagsakIderForSøker(tilAktørId(attributter.getFnrForSøkEtterSaker())));
        fagsakIder.addAll(pipRepository.fagsakIdForSaksnummer(attributter.getSaksnummre()));

        Set<JournalpostId> ikkePåkrevdeJournalpostId = attributter.getJournalpostIder(false).stream().map(JournalpostId::new).collect(Collectors.toSet());
        fagsakIder.addAll(pipRepository.fagsakIdForJournalpostId(ikkePåkrevdeJournalpostId));

        Set<JournalpostId> påkrevdJournalpostId = attributter.getJournalpostIder(true).stream().map(JournalpostId::new).collect(Collectors.toSet());
        fagsakIder.addAll(hentOgSjekkAtFinnes(attributter.getSaksnummre(), påkrevdJournalpostId));
        return fagsakIder;
    }

    private Set<Long> hentOgSjekkAtFinnes(Collection<String> saksnumre, Collection<JournalpostId> journalpostIder) {
        Set<Long> resultat = pipRepository.fagsakIdForJournalpostId(journalpostIder);
        if (resultat.size() == journalpostIder.size()) {
            return resultat;
        }  else {
            validerJournalpostIdMotSaksnummer(saksnumre, journalpostIder);
            return Collections.emptySet();
        }
    }

    private Set<AktørId> utledAktørIder(AbacAttributtSamling attributter, Set<Long> fagsakIder) {
        Set<AktørId> aktørIder = new HashSet<>();
        aktørIder.addAll(attributter.getAktørIder().stream().map(AktørId::new).collect(Collectors.toSet()));
        aktørIder.addAll(pipRepository.hentAktørIdKnyttetTilFagsaker(fagsakIder));
        return aktørIder;
    }

    private Collection<AktørId> tilAktørId(Set<String> fødselsnumre) {
        return aktørConsumer.hentAktørIdForPersonIdentSet(fødselsnumre).stream().map(id -> new AktørId(id)).collect(Collectors.toSet());
    }

    private Set<String> hentAktuelleFødselsnumre(AbacAttributtSamling attributter, Set<AktørId> aktørIder) {
        Set<String> resultat = new HashSet<>();
        resultat.addAll(attributter.getFødselsnumre());
        for (AktørId aktørId : aktørIder) {
            aktørConsumer.hentPersonIdentForAktørId(aktørId.getId()).ifPresent(resultat::add);
        }
        return resultat;
    }
    private Set<Long> validerJournalpostIdMotSaksnummer(Collection<String> saksnumre, Collection<JournalpostId> journalposter) {
        if (journalposter.isEmpty()) {
            return Collections.emptySet();
        }
        if (saksnumre.isEmpty()) {
            throw PdpRequestBuilderFeil.FACTORY.ugyldigInputPåkrevdJournalpostIdFinnesIkke(journalposter).toException();
        }

        HentKjerneJournalpostListeRequest hentKjerneJournalpostListeRequest = new HentKjerneJournalpostListeRequest();

        for (String saksnummer : saksnumre) {
            ArkivSak journalSak = new ArkivSak();
            journalSak.setArkivSakSystem(Fagsystem.GOSYS.getOffisiellKode());
            journalSak.setArkivSakId(saksnummer);
            hentKjerneJournalpostListeRequest.getArkivSakListe().add(journalSak);
        }

        try {
            HentKjerneJournalpostListeResponse hentKjerneJournalpostListeResponse = journalConsumer.hentKjerneJournalpostListe(hentKjerneJournalpostListeRequest);
            for (JournalpostId journalpost : journalposter) {
                Journalpost journalpost1 = hentKjerneJournalpostListeResponse.getJournalpostListe()
                    .stream().filter(jp -> journalpost.equals(new JournalpostId(jp.getJournalpostId()))).findFirst().orElse(null);
                if (journalpost1 == null ) {
                    throw PdpRequestBuilderFeil.FACTORY.ugyldigInputPåkrevdJournalpostIdFinnesIkke(journalposter).toException();
                } else if (Journaltilstand.UTGAAR.equals(journalpost1.getJournaltilstand())) {
                    throw PdpRequestBuilderFeil.FACTORY.ugyldigInputJournalpostIdUtgått(journalpost.getVerdi()).toException();
                }
            }
            return Collections.emptySet();
        } catch (HentKjerneJournalpostListeSikkerhetsbegrensning|HentKjerneJournalpostListeUgyldigInput sikkerhetsbegrensning)  { // NOSONAR
            throw PdpRequestBuilderFeil.FACTORY.ugyldigInputPåkrevdJournalpostIdFinnesIkke(journalposter).toException();
        }
    }

}
