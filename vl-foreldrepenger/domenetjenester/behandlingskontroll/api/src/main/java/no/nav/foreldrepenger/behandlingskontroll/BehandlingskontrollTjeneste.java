package no.nav.foreldrepenger.behandlingskontroll;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

public interface BehandlingskontrollTjeneste {

    /**
     * Initier ny Behandlingskontroll, oppretter kontekst som brukes til sikre at parallle behandlinger og kjøringer går
     * i tur og orden. Dette skjer gjennom å opprette en {@link BehandlingLås} som legges ved ved lagring.
     *
     * @param behandlingId
     *            - må være med
     */
    BehandlingskontrollKontekst initBehandlingskontroll(Long behandlingId);

    /**
     * Initierer ny behandlingskontroll for en ny behandling, som ikke er lagret i behandlingsRepository
     * og derfor ikke har fått tildelt behandlingId
     *
     * @param behandling
     *            - må være med
     */
    BehandlingskontrollKontekst initBehandlingskontroll(Behandling behandling);

    /**
     * Prosesser behandling fra dit den sist har kommet.
     * Avhengig av vurderingspunkt (inngang- og utgang-kriterier) vil steget kjøres på nytt.
     *
     * @param kontekst
     *            - kontekst for prosessering. Opprettes gjennom {@link #initBehandlingskontroll(Long)}
     */
    void prosesserBehandling(BehandlingskontrollKontekst kontekst);

    /**
     * Prosesser behandling enten fra akitvt steg eller steg angitt av aksjonspunktDefinsjonerKoder dersom noen er eldre
     *
     * @see #prosesserBehandling(BehandlingskontrollKontekst)
     */
    void behandlingTilbakeføringTilTidligsteAksjonspunkt(BehandlingskontrollKontekst kontekst, Collection<String> endredeAksjonspunkt, boolean erOverstyring);

    boolean behandlingTilbakeføringHvisTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst,
                                                               BehandlingStegType tidligereStegType);

    /**
     * FLytt prosesen til et tidlligere steg.
     *
     * @throws IllegalstateException
     *             dersom tidligereSteg er etter aktivt steg i behandlingen (i følge BehandlingsModell for gitt
     *             BehandlingType).
     */
    void behandlingTilbakeføringTilTidligereBehandlingSteg(BehandlingskontrollKontekst kontekst, BehandlingStegType tidligereStegType);

    int sammenlignRekkefølge(Behandling behandling, BehandlingStegType stegA, BehandlingStegType stegB);

    /**
     * Flytt prosessen til senere steg. Hopper over eventuelt mellomliggende steg.
     *
     * Alle mellomliggende steg og aksjonspunkt vil bli satt til AVBRUTT når dette skjer. Prosessen vil ikke kjøres.
     * Det gjelder også dersom neste steg er det definerte neste steget i prosessen (som normalt skulle blitt kalt
     * gjennom {@link #prosesserBehandling(BehandlingskontrollKontekst)}.
     *
     * @throws IllegalstateException
     *             dersom senereSteg er før eller lik aktivt steg i behandlingen (i følge BehandlingsModell for gitt
     *             BehandlingType).
     */
    void behandlingFramføringTilSenereBehandlingSteg(BehandlingskontrollKontekst kontekst, BehandlingStegType senereSteg);

    /**
     * Markerer ett eller flere aksjonspunkter som utført og oppdaterer status og steg på bakgrunn av det.
     * <p>
     * Bør bare kalles dersom status eller begrunnelse for Utført endres. Bruk retur verdi fra
     * {@link Aksjonspunkt#settTilUtført(String)} til å sjekke det
     *
     * @param behandlingStegType
     *            - steg utført i.
     */
    void aksjonspunkterUtført(BehandlingskontrollKontekst kontekst, List<Aksjonspunkt> aksjonspunkter,
                              BehandlingStegType behandlingStegType);

    /**
     * Markerer at ett eller flere aksjonspunkter er funnet i et gitt behandlingsteg
     */
    void aksjonspunkterFunnet(BehandlingskontrollKontekst kontekst, BehandlingStegType behandlingStegType,
                              List<Aksjonspunkt> aksjonspunkter);

    /**
     * Lagrer en ny behandling i behandlingRepository og fyrer av event om at en Behandling er opprettet
     */
    void opprettBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling);

    void avsluttBehandling(BehandlingskontrollKontekst kontekst, Behandling behandling);

    /**
     * Oppretter eller oppdaterer en behandling.
     * <p>
     * <ul>
     * <li>Oppdaterer eksisterende behandling hvis det finnes en åpen behandling.</li>
     * <li>Oppretter ny behanding dersom det ikke finnes eksisterende åpen behandling.</li>
     * <li>Benytter grunnlag fra tidligere behandling hvis det finnes eksisterende avsluttede behandlinger.</li>
     * </ul>
     *
     * @param fagsak
     *            - fagsak med eller uten eksisterende behandling
     * @param behandlingType
     *            - type behandling
     * @param behandlingOppdaterer
     *            - funksjon for å oppdatere grunnlag eller behandling under arbeid
     * @return Behandling - opprettet eller oppdatert, og lagret.
     */
    Behandling opprettNyEllerOppdaterEksisterendeBehandling(Fagsak fagsak, BehandlingType behandlingType,
                                                            Consumer<Behandling> behandlingOppdaterer);

    /**
     * Opprett ny behandling for gitt fagsak og BehandlingType.
     * <p>
     * Vil alltid opprette ny behandling, selv om det finnes eksisterende åpen behandling på fagsaken.
     *
     * @param fagsak
     *            - fagsak med eller uten eksisterende behandling
     * @param behandlingType
     *            - type behandling
     * @param behandlingOppdaterer
     *            - funksjon for oppdatering av grunnlag
     * @return Behandling - nylig opprettet og lagret.
     */
    Behandling opprettNyBehandling(Fagsak fagsak, BehandlingType behandlingType, Consumer<Behandling> behandlingOppdaterer);

    /**
     * Setter behandlingen på vent.
     *
     * @param behandling
     * @param aksjonspunktDefinisjon hvilket Aksjonspunkt skal holde i 'ventingen'
     * @param fristTid Frist før Behandlingen å adresseres
     * @param venteårsak Årsak til ventingen.
     *
     */
    Aksjonspunkt settBehandlingPåVentUtenSteg(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, LocalDateTime fristTid,
                                              Venteårsak venteårsak);

    /**
     * Setter behandlingen på vent med angitt hvilket steg det står i.
     *
     * @param behandling
     * @param aksjonspunktDefinisjon hvilket Aksjonspunkt skal holde i 'ventingen'
     * @param BehandlingStegType stegType aksjonspunktet står i.
     * @param fristTid Frist før Behandlingen å adresseres
     * @param venteårsak Årsak til ventingen.
     *
     */
    Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingStegType stegType, LocalDateTime fristTid,
                                      Venteårsak venteårsak);

    /**
     * Setter autopunkter til utført (som en del av å gjenoppta behandlingen). Dette klargjør kun behandligen for
     * prosessering, men vil ikke drive prosessen videre.
     * Bruk {@link #prosesserBehandling(BehandlingskontrollKontekst)} el. tilsvarende for det.
     */
    void settAutopunkterTilUtført(BehandlingskontrollKontekst kontekst, boolean erHenleggelse);

    /**
     * Setter autopunkter av en spessifik aksjonspunktdefinisjon til utført. Dette klargjør kun behandligen for
     * prosessering, men vil ikke drive prosessen videre.
     *
     * @param aksjonspunktDefinisjon Aksjonspunktdefinisjon til de aksjonspunktene som skal lukkes
     *            Bruk {@link #prosesserBehandling(BehandlingskontrollKontekst)} el. tilsvarende for det.
     */
    void settAutopunktTilUtført(AksjonspunktDefinisjon aksjonspunktDefinisjon, BehandlingskontrollKontekst kontekst);

    /**
     * Setter tilbake aktivt steg hvis autopunkt er av type fødsel AUTO_VENT_PÅ_FØDSELREGISTRERING.
     * Dette er et spesialtilfelle som krever at steget kjøres om igjen (eneste kjente tilfelle)
     */
    void taBehandlingAvVent(Behandling behandling, BehandlingskontrollKontekst kontekst);

    /** Henlegg en behandling. */
    void henleggBehandling(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsakKode);

    BehandlingStegKonfigurasjon getBehandlingStegKonfigurasjon();

    Set<String> finnAksjonspunktDefinisjonerFraOgMed(Behandling behandling, BehandlingStegType steg, boolean medInngangOgså);

    /** Oppdaterer behandling. (lagrer) */
    void oppdaterBehandling(Behandling behandling, BehandlingskontrollKontekst kontekst);

    void henleggBehandlingFraSteg(BehandlingskontrollKontekst kontekst, BehandlingResultatType årsak);

    boolean erStegPassert(Behandling behandling, BehandlingStegType behandlingSteg);

    /**
     * Sjekker i behandlingsmodellen om aksjonspunktet skal løses i eller etter det angitte steget.
     *
     * @param behandling
     * @param behandlingSteg steget som aksjonspunktet skal sjekkes mot
     * @param aksjonspunktDefinisjon aksjonspunktet som skal sjekkes
     * @return true dersom aksjonspunktet skal løses i eller etter det angitte steget.
     */
    boolean skalAksjonspunktReaktiveresIEllerEtterSteg(Behandling behandling, BehandlingStegType behandlingSteg, AksjonspunktDefinisjon aksjonspunktDefinisjon);

    void lagHistorikkinnslagForHenleggelse(Long behandlingsId, HistorikkinnslagType historikkinnslagType, BehandlingResultatType aarsak, String begrunnelse, HistorikkAktør aktør);

    void fremoverTransisjon(TransisjonIdentifikator transisjonId, BehandlingskontrollKontekst kontekst);

    boolean inneholderSteg(Behandling behandling, BehandlingStegType registrerSøknad);
}
