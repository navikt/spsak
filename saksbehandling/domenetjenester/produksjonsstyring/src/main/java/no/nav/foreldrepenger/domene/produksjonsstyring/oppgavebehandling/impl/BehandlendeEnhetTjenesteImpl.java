package no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.EnhetsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

@ApplicationScoped
public class BehandlendeEnhetTjenesteImpl implements BehandlendeEnhetTjeneste {

    private TpsTjeneste tpsTjeneste;
    private EnhetsTjeneste enhetsTjeneste;
    private KodeverkRepository kodeverkRepository;

    public BehandlendeEnhetTjenesteImpl() {
        //For CDI
    }

    @Inject
    public BehandlendeEnhetTjenesteImpl(TpsTjeneste tpsTjeneste, EnhetsTjeneste enhetsTjeneste, BehandlingRepositoryProvider provider) {
        this.tpsTjeneste = tpsTjeneste;
        this.enhetsTjeneste = enhetsTjeneste;
        this.kodeverkRepository = provider.getKodeverkRepository();
    }

    private BehandlingTema behandlingTemaFra(Behandling sisteBehandling) {
        return BehandlingTema.fraFagsak(sisteBehandling.getFagsak());
    }

    private BehandlingTema getBehandlingTema(Behandling behandling) {
        final BehandlingTema behandlingTemaKonst = behandlingTemaFra(behandling);
        return kodeverkRepository.finn(BehandlingTema.class, behandlingTemaKonst);
    }

    @Override
    public OrganisasjonsEnhet finnBehandlendeEnhetFraSøker(Fagsak fagsak) {
        return enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(fagsak.getAktørId(), BehandlingTema.fraFagsak(fagsak));
    }

    @Override
    public OrganisasjonsEnhet finnBehandlendeEnhetFraSøker(Behandling behandling) {
        return enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(behandling.getAktørId(), getBehandlingTema(behandling));
    }

    @Override
    public Optional<OrganisasjonsEnhet> endretBehandlendeEnhetFraAndrePersoner(Behandling behandling, List<AktørId> aktører) {
        return enhetsTjeneste.oppdaterEnhetSjekkOppgitte(behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId(), getBehandlingTema(behandling), aktører);
    }

    @Override
    public Optional<OrganisasjonsEnhet> endretBehandlendeEnhetFraAndrePersoner(Behandling behandling, PersonIdent relatert) {
        AktørId aktørId = tpsTjeneste.hentAktørForFnr(relatert).orElse(null);
        if (aktørId == null) {
            return Optional.empty();
        }
        return enhetsTjeneste.oppdaterEnhetSjekkOppgitte(behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId(),
            getBehandlingTema(behandling), Arrays.asList(aktørId));
    }

    @Override
    public Optional<OrganisasjonsEnhet> sjekkEnhetVedGjenopptak(Behandling behandling) {
        return sjekkEnhetForBehandlingMedEvtKobletSak(behandling, behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId());
    }


    @Override
    public OrganisasjonsEnhet sjekkEnhetVedNyAvledetBehandling(Behandling behandling, OrganisasjonsEnhet enhetOpprinneligBehandling) {
        return sjekkEnhetForBehandlingMedEvtKobletSak(behandling, enhetOpprinneligBehandling.getEnhetId()).orElse(enhetOpprinneligBehandling);
    }

    private Optional<OrganisasjonsEnhet> sjekkEnhetForBehandlingMedEvtKobletSak(Behandling behandling, String inputEnhetId) {
        List<AktørId> relatertePersoner = new ArrayList<>();

        Optional<AktørId> kobletPerson = Optional.empty();

        return enhetsTjeneste.oppdaterEnhetSjekkRegistrerteRelasjoner(inputEnhetId, getBehandlingTema(behandling), behandling.getAktørId(), kobletPerson, relatertePersoner);
    }

    @Override
    public boolean gyldigEnhetNfpNk(Fagsak fagsak, String enhetId) {
        return enhetsTjeneste.hentEnhetListe(BehandlingTema.fraFagsak(fagsak)).stream()
            .map(OrganisasjonsEnhet::getEnhetId)
            .filter(Objects::nonNull)
            .anyMatch(enhetId::equals);
    }
}
