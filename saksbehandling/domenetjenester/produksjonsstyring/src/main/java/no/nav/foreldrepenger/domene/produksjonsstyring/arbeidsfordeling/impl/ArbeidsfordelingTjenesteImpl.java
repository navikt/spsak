package no.nav.foreldrepenger.domene.produksjonsstyring.arbeidsfordeling.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.domene.produksjonsstyring.arbeidsfordeling.ArbeidsfordelingTjeneste;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Enhetsstatus;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Oppgavetyper;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Temagrupper;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient.ArbeidsfordelingConsumer;

@Dependent
public class ArbeidsfordelingTjenesteImpl implements ArbeidsfordelingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ArbeidsfordelingTjenesteImpl.class);
    private static final String TEMAGRUPPE = "FMLI";// FMLI = Familie
    private static final String TEMA = "FOR"; // FOR = Foreldre- og svangerskapspenger
    private static final String OPPGAVETYPE = "BEH_SED"; // BEH_SED = behandle sak

    private ArbeidsfordelingConsumer consumer;

    @Inject
    public ArbeidsfordelingTjenesteImpl(ArbeidsfordelingConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public OrganisasjonsEnhet finnBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode, BehandlingTema behandlingTema) {
        FinnBehandlendeEnhetListeRequest request = lagRequestForHentBehandlendeEnhet(behandlingTema, diskresjonskode, geografiskTilknytning);

        try {
            FinnBehandlendeEnhetListeResponse response = consumer.finnBehandlendeEnhetListe(request);
            Organisasjonsenhet valgtEnhet = validerOgVelgBehandlendeEnhet(geografiskTilknytning, diskresjonskode, behandlingTema, response);
            return new OrganisasjonsEnhet(valgtEnhet.getEnhetId(), valgtEnhet.getEnhetNavn());
        } catch (FinnBehandlendeEnhetListeUgyldigInput e) {
            throw ArbeidsfordelingFeil.FACTORY.finnBehandlendeEnhetListeUgyldigInput(e).toException();
        }
    }

    @Override
    public List<OrganisasjonsEnhet> finnAlleBehandlendeEnhetListe(BehandlingTema behandlingTema) {
        // NORG2 og ruting diskriminerer på TEMA, for tiden ikke på BehandlingTEMA
        FinnAlleBehandlendeEnheterListeRequest request = lagRequestForHentAlleBehandlendeEnheter(behandlingTema, Optional.empty());

        try {
            FinnAlleBehandlendeEnheterListeResponse response = consumer.finnAlleBehandlendeEnheterListe(request);
            return tilOrganisasjonsEnhetListe(response, behandlingTema, true);
        } catch (FinnAlleBehandlendeEnheterListeUgyldigInput e) {
            throw ArbeidsfordelingFeil.FACTORY.finnAlleBehandlendeEnheterListeUgyldigInput(e).toException();
        }
    }

    private FinnAlleBehandlendeEnheterListeRequest lagRequestForHentAlleBehandlendeEnheter(BehandlingTema behandlingTema, Optional<String> diskresjonskode) {
        FinnAlleBehandlendeEnheterListeRequest request = new FinnAlleBehandlendeEnheterListeRequest();
        ArbeidsfordelingKriterier kriterier = new ArbeidsfordelingKriterier();

        diskresjonskode.ifPresent(kode -> {
            Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
            diskresjonskoder.setValue(kode);
            kriterier.setDiskresjonskode(diskresjonskoder);
        });

        Temagrupper temagruppe = new Temagrupper();
        temagruppe.setValue(TEMAGRUPPE);
        kriterier.setTemagruppe(temagruppe);

        Tema tema = new Tema();
        tema.setValue(TEMA);
        kriterier.setTema(tema);

        Oppgavetyper oppgavetyper = new Oppgavetyper();
        oppgavetyper.setValue(OPPGAVETYPE);
        kriterier.setOppgavetype(oppgavetyper);

        if (!BehandlingTema.UDEFINERT.equals(behandlingTema)) {
            Behandlingstema behandlingstemaRequestObject = new Behandlingstema();
            behandlingstemaRequestObject.setValue(behandlingTema.getOffisiellKode());
            kriterier.setBehandlingstema(behandlingstemaRequestObject);
        }

        request.setArbeidsfordelingKriterier(kriterier);
        return request;
    }

    private FinnBehandlendeEnhetListeRequest lagRequestForHentBehandlendeEnhet(BehandlingTema behandlingTema, String diskresjonskode,
                                                                               String geografiskTilknytning) {
        FinnBehandlendeEnhetListeRequest request = new FinnBehandlendeEnhetListeRequest();
        ArbeidsfordelingKriterier kriterier = new ArbeidsfordelingKriterier();

        Temagrupper temagruppe = new Temagrupper();
        temagruppe.setValue(TEMAGRUPPE);
        kriterier.setTemagruppe(temagruppe);

        Tema tema = new Tema();
        tema.setValue(TEMA);
        kriterier.setTema(tema);

        if (!BehandlingTema.UDEFINERT.equals(behandlingTema)) {
            Behandlingstema behandlingstemaRequestObject = new Behandlingstema();
            behandlingstemaRequestObject.setValue(behandlingTema.getOffisiellKode());
            kriterier.setBehandlingstema(behandlingstemaRequestObject);
        }

        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(diskresjonskode);
        kriterier.setDiskresjonskode(diskresjonskoder);

        Geografi geografi = new Geografi();
        geografi.setValue(geografiskTilknytning);
        kriterier.setGeografiskTilknytning(geografi);

        request.setArbeidsfordelingKriterier(kriterier);
        return request;
    }

    private Organisasjonsenhet validerOgVelgBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode,
                                                             BehandlingTema behandlingTema, FinnBehandlendeEnhetListeResponse response) {
        List<Organisasjonsenhet> behandlendeEnheter = response.getBehandlendeEnhetListe();

        // Vi forventer å få én behandlende enhet.
        if (behandlendeEnheter == null || behandlendeEnheter.isEmpty()) {
            throw ArbeidsfordelingFeil.FACTORY.finnerIkkeBehandlendeEnhet(geografiskTilknytning, diskresjonskode, behandlingTema).toException();
        }

        // Vi forventer å få én behandlende enhet.
        Organisasjonsenhet valgtBehandlendeEnhet = behandlendeEnheter.get(0);
        if (behandlendeEnheter.size() > 1) {
            List<String> enheter = behandlendeEnheter.stream().map(Organisasjonsenhet::getEnhetId).collect(Collectors.toList());
            ArbeidsfordelingFeil.FACTORY.fikkFlereBehandlendeEnheter(geografiskTilknytning, diskresjonskode, behandlingTema, enheter,
                valgtBehandlendeEnhet.getEnhetId()).log(logger);
        }
        return valgtBehandlendeEnhet;
    }

    private List<OrganisasjonsEnhet> tilOrganisasjonsEnhetListe(FinnAlleBehandlendeEnheterListeResponse response,
                                                                BehandlingTema behandlingTema, boolean medKlage) {
        List<Organisasjonsenhet> responsEnheter = response.getBehandlendeEnhetListe();

        if (responsEnheter == null || responsEnheter.isEmpty()) {
            throw ArbeidsfordelingFeil.FACTORY.finnerIkkeAlleBehandlendeEnheter(behandlingTema).toException();
        }

        List<OrganisasjonsEnhet> organisasjonsEnhetListe = responsEnheter.stream()
            .map(responsOrgEnhet -> new OrganisasjonsEnhet(responsOrgEnhet.getEnhetId(), responsOrgEnhet.getEnhetNavn(),
                responsOrgEnhet.getStatus().name()))
            .collect(Collectors.toList());

        if (medKlage) {
            // Hardkodet inn for Klageinstans da den ikke kommer med i response fra NORG. Fjern dette når det er validert på plass.
            OrganisasjonsEnhet klage = new OrganisasjonsEnhet("4205", "NAV Klageinstans Midt-Norge", Enhetsstatus.AKTIV.value());
            organisasjonsEnhetListe.add(klage);
        }

        return organisasjonsEnhetListe;
    }

    @Override
    public OrganisasjonsEnhet hentEnhetForDiskresjonskode(String kode, BehandlingTema behandlingTema) {

        FinnAlleBehandlendeEnheterListeRequest request = lagRequestForHentAlleBehandlendeEnheter(behandlingTema, Optional.of(kode));

        try {
            FinnAlleBehandlendeEnheterListeResponse response = consumer.finnAlleBehandlendeEnheterListe(request);
            return tilOrganisasjonsEnhetListe(response, behandlingTema, false).get(0);
        } catch (FinnAlleBehandlendeEnheterListeUgyldigInput e) {
            throw ArbeidsfordelingFeil.FACTORY.finnAlleBehandlendeEnheterListeUgyldigInput(e).toException();
        }

    }
}
