package no.nav.foreldrepenger.behandling.innsyn.impl;

import static no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør.SAKSBEHANDLER;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.innsyn.InnsynTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.InnsynRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

@ApplicationScoped
public class InnsynTjenesteImpl implements InnsynTjeneste {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private InnsynHistorikkTjeneste innsynHistorikkTjeneste;
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private InnsynRepository innsynRepository;

    InnsynTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public InnsynTjenesteImpl(BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                              InnsynHistorikkTjeneste innsynHistorikkTjeneste,
                              BehandlingRepositoryProvider repositoryProvider,
                              BehandlendeEnhetTjeneste behandlendeEnhetTjeneste) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.innsynHistorikkTjeneste = innsynHistorikkTjeneste;
        this.fagsakRepository = repositoryProvider.getFagsakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.innsynRepository = repositoryProvider.getInnsynRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    @Override
    public Behandling opprettManueltInnsyn(Saksnummer saksnummer) {
        Fagsak fagsak = fagsakRepository.hentSakGittSaksnummer(saksnummer)
            .orElseThrow(() -> InnsynFeil.FACTORY.tjenesteFinnerIkkeFagsakForInnsyn(saksnummer).toException());

        BehandlingType behandlingType = kodeverkRepository.finn(BehandlingType.class, BehandlingType.INNSYN);
        Behandling nyBehandling = Behandling.nyBehandlingFor(fagsak, behandlingType).build();
        nyBehandling.setBehandlendeEnhet(behandlendeEnhetTjeneste.finnBehandlendeEnhetFraSøker(nyBehandling));

        //TODO PK-48959 E149421 definere egnet årsak type?
        BehandlingÅrsakType årsakType = BehandlingÅrsakType.UDEFINERT;

        innsynHistorikkTjeneste.opprettHistorikkinnslag(nyBehandling, årsakType, SAKSBEHANDLER);

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(nyBehandling);
        behandlingskontrollTjeneste.opprettBehandling(kontekst, nyBehandling);

        return nyBehandling;
    }

    @Override
    public void lagreVurderInnsynResultat(Behandling behandling, InnsynResultat<?> innsynResultat) {
        InnsynResultatType innsynType = kodeverkRepository.finn(InnsynResultatType.class, innsynResultat.getInnsynResultatType().getKode());
        lagreBehandlingResultat(innsynType, behandling);
        InnsynEntitet innsyn = InnsynEntitet.InnsynBuilder.builder()
            .medMottattDato(innsynResultat.getMottattDato())
            .medInnsynResultatType(innsynType)
            .medBegrunnelse(innsynResultat.getBegrunnelse())
            .buildFor(behandling);
        innsynRepository.lagreInnsyn(behandling, innsyn, innsynResultat.getInnsynDokumenter());
    }

    private void lagreBehandlingResultat(InnsynResultatType innsynResultatType, Behandling behandling) {
        Behandlingsresultat.Builder builder = Behandlingsresultat.builderForInngangsvilkår();
        builder.medBehandlingResultatType(konverterResultatType(innsynResultatType));
        Behandlingsresultat res = builder.buildFor(behandling);
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        behandlingRepository.lagre(res.getVilkårResultat(), lås);
        behandlingRepository.lagre(behandling, lås);
    }

    private static BehandlingResultatType konverterResultatType(InnsynResultatType innsynResultatType) {
        // TODO (Maur): bør unngå to kodeverk for samme, evt. linke med Kodeliste relasjon eller abstrahere med interface
        if (InnsynResultatType.INNVILGET.equals(innsynResultatType)) {
            return BehandlingResultatType.INNSYN_INNVILGET;
        } else if (InnsynResultatType.DELVIS_INNVILGET.equals(innsynResultatType)) {
            return BehandlingResultatType.INNSYN_DELVIS_INNVILGET;
        } else if (InnsynResultatType.AVVIST.equals(innsynResultatType)) {
            return BehandlingResultatType.INNSYN_AVVIST;
        }
        throw new IllegalArgumentException("Utviklerfeil: Ukjent resultat-type");
    }


}
