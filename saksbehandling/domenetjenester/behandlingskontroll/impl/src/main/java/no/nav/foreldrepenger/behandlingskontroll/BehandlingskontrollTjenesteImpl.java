package no.nav.foreldrepenger.behandlingskontroll;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.behandlingskontroll.transisjoner.FellesTransisjoner.FREMHOPP_TIL_IVERKSETT_VEDTAK;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEvent.AvsluttetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEvent.ExceptionEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEvent.StartetEvent;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollEvent.StoppetEvent;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.StegTransisjon;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingskontrollTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.InternalManipulerBehandlingImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.StegTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLåsRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

/**
 * Håndterer tilstandsendringer i behandlingskontroll (det som driver prosessen fremover eller bakover).
 */
@RequestScoped // må være RequestScoped sålenge ikke nøstet prosessering støttes.
public class BehandlingskontrollTjenesteImpl implements BehandlingskontrollTjeneste {

    private AksjonspunktRepository aksjonspunktRepository;
    private GrunnlagRepositoryProvider repositoryProvider;
    private BehandlingRepository behandlingRepository;
    private BehandlingModellRepository behandlingModellRepository;
    private InternalManipulerBehandling manipulerInternBehandling;
    private BehandlingskontrollEventPubliserer eventPubliserer = BehandlingskontrollEventPubliserer.NULL_EVENT_PUB;
    private BehandlingStegKonfigurasjon behandlingStegKonfigurasjon;
    private HistorikkRepository historikkRepository;
    private FagsakLåsRepository fagsakLåsRepository;
    private BehandlingLåsRepository behandlingLåsRepository;

    /**
     * Sjekker om vi allerede kjører Behandlingskontroll, og aborter forsøk på nøsting av kall (støttes ikke p.t.).
     * <p>
     * Funker sålenge denne tjenesten er en {@link RequestScoped} bean.
     */
    private AtomicBoolean nøstetProsseringGuard = new AtomicBoolean();
    private BehandlingskontrollRepository behandlingskontrollRepository;

    BehandlingskontrollTjenesteImpl() {
        // for CDI proxy
    }

    /**
     * SE KOMMENTAR ØVERST
     */
    @Inject
    public BehandlingskontrollTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
                                           BehandlingModellRepository behandlingModellRepository,
                                           BehandlingskontrollEventPubliserer eventPubliserer) {

        this.repositoryProvider = repositoryProvider;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollRepository = repositoryProvider.getBehandlingskontrollRepository();
        this.behandlingModellRepository = behandlingModellRepository;
        this.manipulerInternBehandling = new InternalManipulerBehandlingImpl(repositoryProvider);
        this.behandlingStegKonfigurasjon = behandlingModellRepository.getBehandlingStegKonfigurasjon();
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.historikkRepository = repositoryProvider.getHistorikkRepository();
        this.fagsakLåsRepository = repositoryProvider.getFagsakLåsRepository();
        this.behandlingLåsRepository = repositoryProvider.getBehandlingLåsRepository();
        if (eventPubliserer != null) {
            this.eventPubliserer = eventPubliserer;
        }
    }

    private static void postconditionUtfallLagret(Long behandlingId, StegTilstand tilstand, BehandlingStegUtfall utfall, BehandlingModell modell) {
        if (utfall == null) {
            if (tilstand != null && erIkkeSisteSteg(tilstand, modell)) {
                throw new IllegalStateException("Fant tilstand " + tilstand + ", men har utfall null, på behandlingId=" + behandlingId);
            }
        } else {
            if (tilstand == null) {
                throw new IllegalStateException("Fant utfall " + utfall + ", men har lagret tilstand null, på behandlingId=" + behandlingId);
            } else {
                if (Objects.equals(tilstand.getStegType(), utfall.getStegType()) && Objects.equals(tilstand.getStatus(), utfall.getStatus())) {
                    // OK
                } else {
                    throw new IllegalStateException("Fant utfall " + utfall + ", men har lagret tilstand " + tilstand + ", på behandlingId=" + behandlingId);
                }
            }

        }
    }

    private static boolean erIkkeSisteSteg(StegTilstand tilstand, BehandlingModell modell) {
        return modell.finnNesteSteg(tilstand.getStegType()) != null;
    }

    @Override
    public StegTilstand prosesserBehandling(BehandlingskontrollKontekst kontekst) {
        var behandling = hentBehandling(kontekst);
        if (Objects.equals(BehandlingStatus.AVSLUTTET.getKode(), behandling.getStatus().getKode())) {
            return null;
        }

        var behandlingId = kontekst.getBehandlingId();
        var utfall = prosesserBehandling(kontekst, behandling);

        // post-condition sjekk tilstand oppdatert
        var tilstand = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId);
        var stegTilstand = StegTilstand.fra(tilstand).orElse(null);
        BehandlingModell modell = behandlingModellRepository.getModell(behandling.getType(), behandling.getFagsakYtelseType());
        postconditionUtfallLagret(behandlingId, stegTilstand, utfall, modell);

        return stegTilstand;
    }

    private BehandlingStegUtfall prosesserBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        Long behandlingId = kontekst.getBehandlingId();

        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());

        validerOgFlaggStartetProsessering();
        BehandlingStegUtfall behandlingStegUtfall;
        try {
            fyrEventBehandlingskontrollStartet(kontekst, behandling, modell);
            var aktivtBehandlingSteg = finnAktivtBehandlingSteg(behandlingId);
            behandlingStegUtfall = doProsesserBehandling(kontekst, modell, behandling, aktivtBehandlingSteg);
            fyrEventBehandlingskontrollStoppet(kontekst, behandling, modell);
        } catch (RuntimeException e) {
            fyrEventBehandlingskontrollException(kontekst, behandling, modell, e);
            throw e;
        } finally {
            ferdigProsessering();
        }
        return behandlingStegUtfall;
    }

    @Override
    public void behandlingTilbakeføringTilTidligsteAksjonspunkt(BehandlingskontrollKontekst kontekst,
                                                                Collection<String> oppdaterteAksjonspunkter, boolean erOverstyring) {

        if (oppdaterteAksjonspunkter == null || oppdaterteAksjonspunkter.isEmpty()) {
            return;
        }

        var behandlingId = kontekst.getBehandlingId();
        var stegType = finnAktivtBehandlingSteg(behandlingId);

        var behandling = behandlingRepository.hentBehandling(behandlingId);

        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());

        validerOgFlaggStartetProsessering();
        try {
            doTilbakeføringTilTidligsteAksjonspunkt(behandling, stegType, modell, oppdaterteAksjonspunkter);
        } finally {
            ferdigProsessering();
        }

    }

    @Override
    public boolean behandlingTilbakeføringHvisTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                                      BehandlingStegType tidligereStegType) {

        if (!erSenereSteg(kontekst, tidligereStegType)) {
            behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, tidligereStegType);
            return true;
        }
        return false;
    }

    @Override
    public void behandlingTilbakeføringTilTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                                  BehandlingStegType tidligereStegType) {

        var startStatusForNyttSteg = getStatusKonfigurasjon().getInngang();
        var behandlingId = kontekst.getBehandlingId();
        var behandling = behandlingRepository.hentBehandling(behandlingId);

        var stegType = finnAktivtBehandlingSteg(behandlingId);

        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());

        validerOgFlaggStartetProsessering();
        try {
            doTilbakeføringTilTidligereBehandlingSteg(behandling, modell, tidligereStegType, stegType, startStatusForNyttSteg);
        } finally {
            ferdigProsessering();
        }

    }

    @Override
    public int sammenlignRekkefølge(Behandling behandling, BehandlingStegType stegA, BehandlingStegType stegB) {
        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());
        return modell.erStegAFørStegB(stegA, stegB) ? -1
            : modell.erStegAFørStegB(stegB, stegA) ? 1
            : 0;
    }

    @Override
    public void behandlingFramføringTilSenereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                            BehandlingStegType senereSteg) {

        var statusInngang = getStatusKonfigurasjon().getInngang();
        var behandlingId = kontekst.getBehandlingId();
        var behandling = behandlingRepository.hentBehandling(behandlingId);

        var inneværendeSteg = finnAktivtBehandlingSteg(behandlingId);

        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());

        validerOgFlaggStartetProsessering();
        try {
            doFramføringTilSenereBehandlingSteg(senereSteg, statusInngang, behandling, inneværendeSteg, modell);
        } finally {
            ferdigProsessering();
        }

    }

    @Override
    public BehandlingskontrollKontekst initBehandlingskontroll(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); //$NON-NLS-1$
        // først lås
        BehandlingLås lås = behandlingLåsRepository.taLås(behandlingId);
        // så les
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        return new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
    }

    @Override
    public BehandlingskontrollKontekst initBehandlingskontroll(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$
        // først lås
        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);

        // så les
        return new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
    }

    @Override
    public void aksjonspunkterUtført(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter,
                                     BehandlingStegType behandlingStegType) {
        if (!aksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunktUtførtEvent(kontekst, aksjonspunkter, behandlingStegType));
        }
    }

    // for symmetri med aksjonspunkterUtført
    @Override
    public void aksjonspunkterFunnet(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                                     List<Aksjonspunkt> aksjonspunkter) {
        // handlinger som skal skje når funnet
        if (!aksjonspunkter.isEmpty()) {
            eventPubliserer.fireEvent(new AksjonspunkterFunnetEvent(kontekst, aksjonspunkter, behandlingStegType));
        }
    }

    @Override
    public BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon() {
        return behandlingStegKonfigurasjon;
    }

    @Override
    public void opprettBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        var fagsakLås = fagsakLåsRepository.taLås(behandling.getFagsak());
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
        fagsakLåsRepository.oppdaterLåsVersjon(fagsakLås);
        eventPubliserer.fireEvent(kontekst, null, behandling.getStatus());
    }

    @Override
    public Behandling opprettNyBehandling(Fagsak fagsak, BehandlingType behandlingType, Consumer<Behandling> behandlingOppdaterer) {
        var behandlingBuilder = Behandling.nyBehandlingFor(fagsak, behandlingType);
        var nyBehandling = behandlingBuilder.build();
        behandlingOppdaterer.accept(nyBehandling);

        var kontekst = this.initBehandlingskontroll(nyBehandling);
        this.opprettBehandling(kontekst, nyBehandling);
        return nyBehandling;
    }

    @Override
    public Behandling opprettNyEllerOppdaterEksisterendeBehandling(Fagsak fagsak, BehandlingType behandlingType,
                                                                   Consumer<Behandling> behandlingOppdaterer) {
        var behandlingOpt = behandlingRepository.hentSisteBehandlingForFagsakId(fagsak.getId(), behandlingType);
        if (behandlingOpt.isPresent()) {
            Behandling behandling = behandlingOpt.get();
            if (behandling.erSaksbehandlingAvsluttet()) {
                return opprettNyFraTidligereBehandling(behandling, behandlingType, behandlingOppdaterer);
            } else {
                oppdaterEksisterendeBehandling(behandling, behandlingOppdaterer);
                return behandlingRepository.hentBehandling(behandling.getId());
            }
        } else {
            return opprettNyBehandling(fagsak, behandlingType, behandlingOppdaterer);
        }
    }

    @Override
    public void avsluttBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        BehandlingStatus gammelStatus = behandling.getStatus();
        behandlingskontrollRepository.avsluttBehandling(behandling.getId());
        eventPubliserer.fireEvent(kontekst, gammelStatus, behandling.getStatus());

    }

    @Override
    public Aksjonspunkt settBehandlingPåVentUtenSteg(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                                     LocalDateTime fristTid, Venteårsak venteårsak) {
        return settBehandlingPåVent(behandling, aksjonspunktDefinisjonIn, null, fristTid, venteårsak);
    }

    @Override
    public Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                             BehandlingStegType stegType, LocalDateTime fristTid, Venteårsak venteårsak) {
        var lås = behandlingRepository.taSkriveLås(behandling);
        var aksjonspunkt = aksjonspunktRepository.settBehandlingPåVent(behandling, aksjonspunktDefinisjonIn, stegType, fristTid, venteårsak);
        behandlingRepository.lagre(behandling, lås);
        if (aksjonspunkt != null) {
            BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), lås);
            aksjonspunkterFunnet(kontekst, aksjonspunkt.getBehandlingStegFunnet(), Arrays.asList(aksjonspunkt));
        }
        return aksjonspunkt;
    }

    @Override
    public void settAutopunkterTilUtført(BehandlingskontrollKontekst kontekst, boolean erHenleggelse) {
        var behandling = hentBehandling(kontekst);
        var åpneAutopunkter = behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT);
        åpneAutopunkter.forEach(autopunkt -> aksjonspunktRepository.setTilUtført(autopunkt, null));
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());

        if (!erHenleggelse) {
            // Publiser at aksjonspunktet er "normalt" utført
            var behandlingId = kontekst.getBehandlingId();
            var aktivtBehandlingSteg = finnAktivtBehandlingSteg(behandlingId);
            aksjonspunkterUtført(kontekst, åpneAutopunkter, aktivtBehandlingSteg);
        }
    }

    @Override
    public void settAutopunktTilUtført(AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingskontrollKontekst kontekst) {
        var behandling = hentBehandling(kontekst);
        var aksjonspunktMedDefinisjonOptional = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);

        aksjonspunktMedDefinisjonOptional.ifPresent(aksjonspunkt -> {
            if (aksjonspunkt.erÅpentAksjonspunkt()) {
                aksjonspunktRepository.setTilUtført(aksjonspunkt, null);
                behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
                var behandlingId = kontekst.getBehandlingId();
                var aktivtBehandlingSteg = finnAktivtBehandlingSteg(behandlingId);
                aksjonspunkterUtført(kontekst, singletonList(aksjonspunkt), aktivtBehandlingSteg);
            }
        });
    }

    @Override
    public void taBehandlingAvVent(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        // Kjør steget på nytt ved gjenopptakelse fra venting, når aksjonspunktet er markert for dette
        var aksjonspunkterSomMedførerTilbakehopp = behandling.getÅpneAksjonspunkter().stream()
            .filter(Aksjonspunkt::tilbakehoppVedGjenopptakelse)
            .collect(Collectors.toList());

        if (aksjonspunkterSomMedførerTilbakehopp.size() > 1) {
            throw BehandlingskontrollFeil.FACTORY.kanIkkeGjenopptaBehandlingFantFlereAksjonspunkterSomMedførerTilbakehopp(behandling.getId()).toException();
        }
        if (aksjonspunkterSomMedførerTilbakehopp.size() == 1) {
            Aksjonspunkt ap = aksjonspunkterSomMedførerTilbakehopp.get(0);
            BehandlingStegType behandlingStegFunnet = ap.getBehandlingStegFunnet();
            aksjonspunktRepository.setTilUtført(ap, ap.getBegrunnelse());
            eventPubliserer.fireEvent(new AksjonspunktUtførtEvent(kontekst, singletonList(ap), behandlingStegFunnet));
            behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, behandlingStegFunnet);
        }
    }

    @Override
    public void henleggBehandling(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        // valider invarianter
        Objects.requireNonNull(årsak, "årsak"); //$NON-NLS-1$
        var behandling = hentBehandling(kontekst);
        var behandlingId = kontekst.getBehandlingId();
        var tilstandNå = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId);

        doHenleggBehandling(behandling, årsak);

        // FIXME (MAUR): Bør løses via FellesTransisjoner og unngå hardkoding av BehandlingStegType her.
        // må fremoverføres for å trigge riktig events for opprydding
        behandlingFramføringTilSenereBehandlingSteg(kontekst, BehandlingStegType.IVERKSETT_VEDTAK);

        publiserFremhoppTransisjon(kontekst, StegTilstand.fra(tilstandNå), BehandlingStegType.IVERKSETT_VEDTAK);

        // sett Avsluttet og fyr status
        avsluttBehandling(kontekst, behandling);
    }

    @Override
    public void henleggBehandlingFraSteg(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak) {
        // valider invarianter
        Objects.requireNonNull(årsak, "årsak"); //$NON-NLS-1$
        var behandling = hentBehandling(kontekst);
        var behandlingId = kontekst.getBehandlingId();
        var tilstandNå = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId);

        doHenleggBehandling(behandling, årsak);

        publiserFremhoppTransisjon(kontekst, StegTilstand.fra(tilstandNå), BehandlingStegType.IVERKSETT_VEDTAK);

        // sett Avsluttet og fyr status
        avsluttBehandling(kontekst, behandling);
    }

    private Behandling doHenleggBehandling(Behandling behandling, BehandlingResultatType årsak) {
        if (behandling.erSaksbehandlingAvsluttet()) {
            throw BehandlingskontrollFeil.FACTORY.kanIkkeHenleggeAvsluttetBehandling(behandling.getId()).toException();
        }
        if (behandling.isBehandlingPåVent()
            // && !behandling.getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).equals(behandling.getÅpneAksjonspunkter())
        ) {
            throw BehandlingskontrollFeil.FACTORY.kanIkkeHenleggeBehandlingPåVent(behandling.getId()).toException();
        }

        // sett årsak
        var eksisterende = behandling.getBehandlingsresultat();
        if (eksisterende == null) {
            Behandlingsresultat.builder().medBehandlingResultatType(årsak).buildFor(behandling);
        } else {
            Behandlingsresultat.builderEndreEksisterende(eksisterende).medBehandlingResultatType(årsak);
        }

        // avbryt aksjonspunkt
        behandling.getÅpneAksjonspunkter().forEach(aksjonspunktRepository::setTilAvbrutt);
        return behandling;
    }

    private void publiserFremhoppTransisjon(BehandlingskontrollKontekst kontekst, Optional<StegTilstand> stegTilstandFør,
                                            BehandlingStegType stegEtter) {
        // Publiser tranisjonsevent (eventobserver(e) håndterer tilhørende tranisjonsregler)
        boolean erOverhopp = true;
        BehandlingTransisjonEvent event = new BehandlingTransisjonEvent(kontekst, FREMHOPP_TIL_IVERKSETT_VEDTAK, stegTilstandFør, stegEtter, erOverhopp);
        eventPubliserer.fireEvent(event);
    }

    @Override
    public boolean erStegPassert(Behandling behandling, BehandlingStegType behandlingSteg) {

        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());

        var behandlingId = behandling.getId();
        var aktivtSteg = finnAktivtBehandlingSteg(behandlingId);

        return !modell.erStegAFørStegB(aktivtSteg, behandlingSteg) && !aktivtSteg.equals(behandlingSteg);
    }

    @Override
    public boolean skalAksjonspunktReaktiveresIEllerEtterSteg(Behandling behandling, BehandlingStegType behandlingSteg,
                                                              AksjonspunktDefinisjon apDef) {

        BehandlingModell modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());
        BehandlingStegType apLøsesteg = Optional.ofNullable(modell
            .finnTidligsteStegForAksjonspunktDefinisjon(singletonList(apDef.getKode())))
            .map(BehandlingStegModell::getBehandlingStegType)
            .orElse(null);
        if (apLøsesteg == null) {
            // AksjonspunktDefinisjon finnes ikke på stegene til denne behandlingstypen. Ap kan derfor ikke løses.
            return false;
        }

        return behandlingSteg.equals(apLøsesteg) || modell.erStegAFørStegB(behandlingSteg, apLøsesteg);
    }

    // TODO: (PK-49128) Midlertidig løsning for å filtrere aksjonspunkter til høyre for steg i hendelsemodul
    @Override
    public Set<String> finnAksjonspunktDefinisjonerFraOgMed(FagsakYtelseType ytelseType, BehandlingType behandlingType, BehandlingStegType steg,
                                                            boolean medInngangOgså) {
        var modell = getModell(behandlingType, ytelseType);
        return modell.finnAksjonspunktDefinisjonerFraOgMed(steg, medInngangOgså);
    }

    @Override
    public BehandlingskontrollTilstand getBehandlingskontrollTilstand(Long behandlingId) {
        return behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId);
    }

    public boolean erSenereSteg(BehandlingskontrollKontekst kontekst, BehandlingStegType tidligereStegType) {
        var behandlingId = kontekst.getBehandlingId();
        var behandling = behandlingRepository.hentBehandling(behandlingId);
        var aktivtBehandlingSteg = finnAktivtBehandlingSteg(behandlingId);
        var modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());
        return erSenereSteg(modell, aktivtBehandlingSteg, tidligereStegType);
    }

    protected BehandlingStegUtfall doProsesserBehandling(BehandlingskontrollKontekst kontekst, BehandlingModell modell,
                                                         Behandling behandling,
                                                         BehandlingStegType startFraBehandlingStegType) {
        if (Objects.equals(BehandlingStatus.AVSLUTTET.getKode(), behandling.getStatus().getKode())) {
            throw new IllegalStateException("Utviklerfeil: Kan ikke prosessere avsluttet behandling"); //$NON-NLS-1$
        }
        var behandlingId = kontekst.getBehandlingId();
        var tilstand = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId);
        BehandlingModellVisitor visitor = nyStegVisitor(kontekst, modell, behandling, tilstand);

        BehandlingStegUtfall behandlingStegUtfall = modell.prosesserFra(startFraBehandlingStegType, visitor);

        if (behandlingStegUtfall == null) {
            avsluttBehandling(kontekst, behandling);
        }
        return behandlingStegUtfall;
    }

    private BehandlingModellVisitor nyStegVisitor(BehandlingskontrollKontekst kontekst, BehandlingModell modell, Behandling behandling,
                                                  BehandlingskontrollTilstand tilstand) {
        BehandlingStegVisitor visitor = new BehandlingStegVisitor(repositoryProvider, behandling, this, modell, kontekst, eventPubliserer, tilstand);
        return new TekniskBehandlingStegVisitor(repositoryProvider, visitor, kontekst);
    }

    protected void doFramføringTilSenereBehandlingSteg(BehandlingStegType senereSteg, final BehandlingStegStatus startStatusForNyttSteg,
                                                       Behandling behandling, BehandlingStegType inneværendeSteg, BehandlingModell modell) {
        if (!erSenereSteg(modell, inneværendeSteg, senereSteg)) {
            throw new IllegalStateException(
                "Kan ikke angi steg [" + senereSteg + "] som er før eller lik inneværende steg [" + inneværendeSteg + "]" + "for behandlingId " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + behandling.getId());
        }
        oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, senereSteg, startStatusForNyttSteg,
            BehandlingStegStatus.AVBRUTT);
    }

    protected void doTilbakeføringTilTidligereBehandlingSteg(Behandling behandling,
                                                             BehandlingModell modell,
                                                             final BehandlingStegType tidligereSteg,
                                                             final BehandlingStegType stegType,
                                                             final BehandlingStegStatus startStatusForNyttSteg) {
        if (!erLikEllerTidligereSteg(modell, stegType, tidligereSteg)) {
            throw new IllegalStateException(
                "Kan ikke angi steg [" + tidligereSteg + "] som er etter [" + stegType + "]" + "for behandlingId " + behandling.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        var behandlingId = behandling.getId();
        var stegTilstand = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId).getStegStatus();
        if (tidligereSteg.equals(stegType)
            && stegTilstand != null
            && stegTilstand.erVedInngang()) {
            // Her står man allerede på steget man skal tilbakeføres, på inngang -> ingen tilbakeføring gjennomføres.
            return;
        }
        oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, tidligereSteg, startStatusForNyttSteg,
            BehandlingStegStatus.TILBAKEFØRT);
    }

    protected void doTilbakeføringTilTidligsteAksjonspunkt(Behandling behandling, BehandlingStegType stegType, BehandlingModell modell,
                                                           Collection<String> oppdaterteAksjonspunkter) {
        Consumer<BehandlingStegType> oppdaterBehandlingStegStatus = (bst) -> {
            var stegStatus = modell.finnStegStatusFor(bst, oppdaterteAksjonspunkter);
            if (stegStatus.isPresent()) {
                var behandlingId = behandling.getId();
                var tilstand = behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId);
                var status = stegStatus.get();

                if (!tilstand.erStegStatus(bst, status)) {
                    // er på starten av steg med endret aksjonspunkt. Ikke kjør steget her, kun oppdater
                    oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(behandling, bst, stegStatus.get(),
                        BehandlingStegStatus.TILBAKEFØRT);
                }
            }
        };

        BehandlingStegModell førsteAksjonspunktSteg = modell
            .finnTidligsteStegForAksjonspunktDefinisjon(oppdaterteAksjonspunkter);

        BehandlingStegType aksjonspunktStegType = førsteAksjonspunktSteg == null ? null
            : førsteAksjonspunktSteg.getBehandlingStegType();

        if (Objects.equals(stegType, aksjonspunktStegType)) {
            // samme steg, kan ha ny BehandlingStegStatus
            oppdaterBehandlingStegStatus.accept(stegType);
        } else {
            // tilbakeføring til tidligere steg
            BehandlingStegModell revidertStegType = modell.finnFørsteSteg(stegType, aksjonspunktStegType);
            oppdaterBehandlingStegStatus.accept(revidertStegType.getBehandlingStegType());
        }
    }

    protected void fireEventBehandlingStatus(BehandlingskontrollKontekst kontekst, Optional<StegTilstand> forrigeTilstand,
                                             Optional<StegTilstand> nyTilstand) {

        BehandlingStatus gammelStatus = forrigeTilstand.map(StegTilstand::getStegType).map(BehandlingStegType::getDefinertBehandlingStatus).orElse(null);
        BehandlingStatus nyStatus = nyTilstand.map(StegTilstand::getStegType).map(BehandlingStegType::getDefinertBehandlingStatus).orElse(null);
        if (!Objects.equals(gammelStatus, nyStatus)) {
            eventPubliserer.fireEvent(kontekst, gammelStatus, nyStatus);
        }
    }

    private void fireEventBehandlingStegTilstandEndring(BehandlingskontrollKontekst kontekst,
                                                        Optional<StegTilstand> stegFør, Optional<StegTilstand> stegEtter) {
        BehandlingStegTilstandEndringEvent event = new BehandlingStegTilstandEndringEvent(kontekst, stegFør);
        event.setNyTilstand(stegEtter);
        getEventPubliserer().fireEvent(event);
    }

    protected void oppdaterEksisterendeBehandlingStegStatusVedFramføringEllerTilbakeføring(Behandling behandling, BehandlingStegType revidertStegType,
                                                                                           BehandlingStegStatus behandlingStegStatus,
                                                                                           BehandlingStegStatus sluttStatusForAndreÅpneSteg) {
        oppdaterEksisterendeBehandling(behandling,
            (beh) -> manipulerInternBehandling.forceOppdaterBehandlingSteg(behandling, revidertStegType, behandlingStegStatus,
                sluttStatusForAndreÅpneSteg));
    }

    protected Behandling hentBehandling(BehandlingskontrollKontekst kontekst) {
        Objects.requireNonNull(kontekst, "kontekst"); //$NON-NLS-1$
        Long behandlingId = kontekst.getBehandlingId();
        return behandlingRepository.hentBehandling(behandlingId);
    }

    protected BehandlingskontrollEventPubliserer getEventPubliserer() {
        return eventPubliserer;
    }

    protected BehandlingModell getModell(BehandlingType behandlingType, FagsakYtelseType ytelseType) {
        return behandlingModellRepository.getModell(behandlingType, ytelseType);
    }

    private void fyrEventBehandlingskontrollException(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                      BehandlingModell modell, RuntimeException e) {
        BehandlingskontrollEvent.ExceptionEvent stoppetEvent = new ExceptionEvent(kontekst, behandling, modell, e);
        eventPubliserer.fireEvent(stoppetEvent);
    }

    private void fyrEventBehandlingskontrollStoppet(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                    BehandlingModell modell) {
        BehandlingskontrollEvent event;
        if (behandling.erAvsluttet()) {
            event = new AvsluttetEvent(kontekst, behandling, modell);
        } else {
            event = new StoppetEvent(kontekst, behandling, modell);
        }
        eventPubliserer.fireEvent(event);
    }

    private void fyrEventBehandlingskontrollStartet(BehandlingskontrollKontekst kontekst, Behandling behandling,
                                                    BehandlingModell modell) {
        BehandlingskontrollEvent.StartetEvent startetEvent = new StartetEvent(kontekst, behandling, modell);
        eventPubliserer.fireEvent(startetEvent);
    }

    private void oppdaterEksisterendeBehandling(Behandling behandling,
                                                Consumer<Behandling> behandlingOppdaterer) {

        Long behandlingId = behandling.getId();
        BehandlingStatus statusFør = behandling.getStatus();
        Optional<StegTilstand> stegFør = StegTilstand.fra(behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId));

        // Oppdater behandling og lagre
        behandlingOppdaterer.accept(behandling);
        BehandlingLås skriveLås = behandlingRepository.taSkriveLås(behandling);
        BehandlingskontrollKontekst kontekst = new BehandlingskontrollKontekst(behandling.getFagsakId(), behandling.getAktørId(), skriveLås);
        behandlingRepository.lagre(behandling, skriveLås);

        // Publiser oppdatering
        BehandlingStatus statusEtter = behandling.getStatus();
        Optional<StegTilstand> stegEtter = StegTilstand.fra(behandlingskontrollRepository.getBehandlingskontrollTilstand(kontekst.getBehandlingId()));
        fireEventBehandlingStegTilstandEndring(kontekst, stegFør, stegEtter);
        fireEventBehandlingStatus(kontekst, stegFør, stegEtter);
        eventPubliserer.fireEvent(kontekst, statusFør, statusEtter);
    }

    @Override
    public void fremoverTransisjon(TransisjonIdentifikator transisjonId, BehandlingskontrollKontekst kontekst) {
        Behandling behandling = behandlingRepository.hentBehandling(kontekst.getBehandlingId());

        Optional<StegTilstand> stegTilstandFør = StegTilstand.fra(behandlingskontrollRepository.getBehandlingskontrollTilstand(kontekst.getBehandlingId()));
        BehandlingStegType fraSteg = stegTilstandFør.isPresent() ? stegTilstandFør.get().getStegType() : null;

        // Flytt behandlingssteg-peker fremover
        BehandlingModell modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());
        StegTransisjon transisjon = modell.finnTransisjon(transisjonId);
        BehandlingStegModell fraStegModell = modell.finnSteg(fraSteg);
        BehandlingStegModell tilStegModell = transisjon.nesteSteg(fraStegModell);
        BehandlingStegType tilSteg = tilStegModell.getBehandlingStegType();

        behandlingFramføringTilSenereBehandlingSteg(kontekst, tilSteg);

        // Publiser tranisjonsevent (eventobserver(e) håndterer tilhørende tranisjonsregler)
        BehandlingTransisjonEvent event = new BehandlingTransisjonEvent(kontekst, transisjonId, stegTilstandFør, tilSteg, transisjon.erFremoverhopp());
        eventPubliserer.fireEvent(event);
    }

    @Override
    public boolean inneholderSteg(Behandling behandling, BehandlingStegType behandlingStegType) {
        BehandlingModell modell = getModell(behandling.getType(), behandling.getFagsakYtelseType());
        return modell.hvertSteg()
            .anyMatch(steg -> steg.getBehandlingStegType().equals(behandlingStegType));
    }

    private BehandlingStegKonfigurasjon getStatusKonfigurasjon() {
        if (behandlingStegKonfigurasjon == null) {
            behandlingStegKonfigurasjon = behandlingModellRepository.getBehandlingStegKonfigurasjon();
        }
        return behandlingStegKonfigurasjon;
    }

    private boolean erSenereSteg(BehandlingModell modell, BehandlingStegType inneværendeSteg,
                                 BehandlingStegType forventetSenereSteg) {
        return modell.erStegAFørStegB(inneværendeSteg, forventetSenereSteg);
    }

    private boolean erLikEllerTidligereSteg(BehandlingModell modell, BehandlingStegType inneværendeSteg,
                                            BehandlingStegType forventetTidligereSteg) {
        // TODO (BIXBITE) skal fjernes når innlegging av papirsøknad er inn i et steg
        if (inneværendeSteg == null) {
            return false;
        }
        if (Objects.equals(inneværendeSteg, forventetTidligereSteg)) {
            return true;
        } else {
            BehandlingStegType førsteSteg = modell.finnFørsteSteg(inneværendeSteg, forventetTidligereSteg).getBehandlingStegType();
            return Objects.equals(forventetTidligereSteg, førsteSteg);
        }
    }

    private Behandling opprettNyFraTidligereBehandling(Behandling gammelBehandling, BehandlingType behandlingType,
                                                       Consumer<Behandling> behandlingOppdaterer) {
        // ta skrive lås på gammel behandling før vi gjør noe annet
        initBehandlingskontroll(gammelBehandling);

        Behandling nyBehandling = behandlingRepository.opprettNyBehandlingBasertPåTidligere(gammelBehandling, behandlingType, repositoryProvider);
        behandlingOppdaterer.accept(nyBehandling);

        BehandlingskontrollKontekst kontekst = this.initBehandlingskontroll(nyBehandling);
        this.opprettBehandling(kontekst, nyBehandling);

        return nyBehandling;
    }

    @Override
    public void oppdaterBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst) {
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());
    }

    private BehandlingStegType finnAktivtBehandlingSteg(Long behandlingId) {
        return behandlingskontrollRepository.getBehandlingskontrollTilstand(behandlingId).getAktivtStegType();
    }

    private void validerOgFlaggStartetProsessering() {
        if (nøstetProsseringGuard.get()) {
            throw new IllegalStateException("Støtter ikke nøstet prosessering i " + getClass().getSimpleName());
        } else {
            nøstetProsseringGuard.set(true);
        }
    }

    private void ferdigProsessering() {
        nøstetProsseringGuard.set(false);
    }

    @Override
    public void lagHistorikkinnslagForHenleggelse(Long behandlingsId,
                                                  HistorikkinnslagType historikkinnslagType,
                                                  BehandlingResultatType aarsak,
                                                  String begrunnelse, HistorikkAktør aktør) {
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder()
            .medHendelse(historikkinnslagType)
            .medÅrsak(aarsak)
            .medBegrunnelse(begrunnelse);
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(historikkinnslagType);
        historikkinnslag.setBehandlingId(behandlingsId);
        builder.build(historikkinnslag);
        historikkinnslag.setAktør(aktør);
        historikkRepository.lagre(historikkinnslag);
    }
}
