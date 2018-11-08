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
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
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
    private SøknadRepository søknadRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private FamilieHendelseRepository familieGrunnlagRepository;

    public BehandlendeEnhetTjenesteImpl() {
        //For CDI
    }

    @Inject
    public BehandlendeEnhetTjenesteImpl(TpsTjeneste tpsTjeneste, EnhetsTjeneste enhetsTjeneste, BehandlingRepositoryProvider provider) {
        this.tpsTjeneste = tpsTjeneste;
        this.enhetsTjeneste = enhetsTjeneste;
        this.kodeverkRepository = provider.getKodeverkRepository();
        this.søknadRepository = provider.getSøknadRepository();
        this.fagsakRelasjonRepository = provider.getFagsakRelasjonRepository();
        this.familieGrunnlagRepository = provider.getFamilieGrunnlagRepository();
    }

    private BehandlingTema behandlingTemaFra(Behandling sisteBehandling) {
        final Optional<FamilieHendelseGrunnlag> grunnlag = familieGrunnlagRepository.hentAggregatHvisEksisterer(sisteBehandling);
        return BehandlingTema.fraFagsak(sisteBehandling.getFagsak(), grunnlag.map(FamilieHendelseGrunnlag::getSøknadVersjon).orElse(null));
    }

    private BehandlingTema getBehandlingTema(Behandling behandling) {
        final BehandlingTema behandlingTemaKonst = behandlingTemaFra(behandling);
        return kodeverkRepository.finn(BehandlingTema.class, behandlingTemaKonst);
    }

    @Override
    public OrganisasjonsEnhet finnBehandlendeEnhetFraSøker(Fagsak fagsak) {
        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(fagsak.getAktørId(), BehandlingTema.fraFagsak(fagsak, null));
        return sjekkMotKobletSak(fagsak, enhet);
    }

    @Override
    public OrganisasjonsEnhet finnBehandlendeEnhetFraSøker(Behandling behandling) {
        OrganisasjonsEnhet enhet = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(behandling.getAktørId(), getBehandlingTema(behandling));
        return sjekkMotKobletSak(behandling.getFagsak(), enhet);
    }

    private OrganisasjonsEnhet sjekkMotKobletSak(Fagsak sak, OrganisasjonsEnhet enhet) {
        FagsakRelasjon relasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(sak).orElse(null);
        if (relasjon == null || !relasjon.getFagsakNrTo().isPresent()) {
            return enhet;
        }
        if (relasjon.getFagsakNrEn().getId().equals(sak.getId())) {
            Fagsak sak2 = relasjon.getFagsakNrTo().get();
            OrganisasjonsEnhet enhetFS2 = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(sak2.getAktørId(), BehandlingTema.fraFagsak(sak2, null));
            return enhetsTjeneste.enhetsPresedens(enhet, enhetFS2, true);
        } else {
            Fagsak sak1 = relasjon.getFagsakNrEn();
            OrganisasjonsEnhet enhetFS1 = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(sak1.getAktørId(), BehandlingTema.fraFagsak(sak1, null));
            return enhetsTjeneste.enhetsPresedens(enhetFS1, enhet, false);
        }
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
    public Optional<OrganisasjonsEnhet> endretBehandlendeEnhetFraOppgittAnnenPart(Behandling behandling) {
        List<AktørId> annenPart = new ArrayList<>();
        finnAktørAnnenPart(behandling).ifPresent(annenPart::add);
        if (!annenPart.isEmpty()) {
            return endretBehandlendeEnhetFraAndrePersoner(behandling, annenPart);
        }
        return Optional.empty();
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

        fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak()).ifPresent(relasjon -> {
            if (relasjon.getFagsakNrEn().getId().equals(behandling.getFagsakId())) {
                relasjon.getFagsakNrTo().ifPresent(sak -> relatertePersoner.add(sak.getAktørId()));
            } else {
                relatertePersoner.add(relasjon.getFagsakNrEn().getAktørId());
            }
        });
        Optional<AktørId> kobletPerson = Optional.empty();
        if (!relatertePersoner.isEmpty()) {
            kobletPerson = Optional.of(relatertePersoner.get(0));
            relatertePersoner.clear();
        }
        finnAktørAnnenPart(behandling).ifPresent(relatertePersoner::add);

        return enhetsTjeneste.oppdaterEnhetSjekkRegistrerteRelasjoner(inputEnhetId, getBehandlingTema(behandling), behandling.getAktørId(), kobletPerson, relatertePersoner);
    }

    private Optional<AktørId> finnAktørAnnenPart(Behandling behandling) {
        Søknad søknad = søknadRepository.hentSøknadHvisEksisterer(behandling).orElse(null);
        if (søknad == null || søknad.getSøknadAnnenPart() == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(søknad.getSøknadAnnenPart().getAktørId());
    }


    @Override
    public Optional<OrganisasjonsEnhet> endretBehandlendeEnhetEtterFagsakKobling(Behandling behandling, FagsakRelasjon kobling) {
        Fagsak fagsak2 = kobling.getFagsakNrTo().orElse(null);
        Optional<OrganisasjonsEnhet> organisasjonsEnhet = Optional.empty();
        if (fagsak2 == null) {
            return organisasjonsEnhet;
        }

        if (behandling.getFagsakId().equals(kobling.getFagsakNrEn().getId())) {
            // Behandling = FS1 og enhet er styrende. Beholder enhet med mindre Fagsak 2 tilsier endring. Skal normalt sett ikke komme hit.
            OrganisasjonsEnhet enhetFS1 = behandling.getBehandlendeOrganisasjonsEnhet();
            OrganisasjonsEnhet enhetFS2 = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(fagsak2.getAktørId(), BehandlingTema.fraFagsak(fagsak2, null));
            OrganisasjonsEnhet presedens = enhetsTjeneste.enhetsPresedens(enhetFS1, enhetFS2, true);
            if (!presedens.getEnhetId().equals(enhetFS1.getEnhetId())) {
                organisasjonsEnhet = Optional.of(presedens);
            }
        } else {
            // Behandling = FS2 men bruker fra FS1 er styrende - med mindre vi tar presedens. Oppdatere Behandling ved behov.
            OrganisasjonsEnhet enhetFS1 = enhetsTjeneste.hentEnhetSjekkRegistrerteRelasjoner(kobling.getFagsakNrEn().getAktørId(), BehandlingTema.fraFagsak(kobling.getFagsakNrEn(), null));
            OrganisasjonsEnhet enhetFS2 = behandling.getBehandlendeOrganisasjonsEnhet();
            OrganisasjonsEnhet presedens = enhetsTjeneste.enhetsPresedens(enhetFS1, enhetFS2, false);
            if (!presedens.getEnhetId().equals(enhetFS2.getEnhetId())) {
                organisasjonsEnhet = Optional.of(presedens);
            }
        }
        return organisasjonsEnhet;
    }

    @Override
    public boolean gyldigEnhetNfpNk(Fagsak fagsak, String enhetId) {
        return enhetsTjeneste.hentEnhetListe(BehandlingTema.fraFagsak(fagsak, null)).stream()
            .map(OrganisasjonsEnhet::getEnhetId)
            .filter(Objects::nonNull)
            .anyMatch(enhetId::equals);
    }
}
