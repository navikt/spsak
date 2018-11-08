package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;

public interface BehandlingsprosessApplikasjonTjeneste {
    Behandling hentBehandling(Long behandlingsId);

    /**
     * Betinget sjekk om innhent registeropplysninger (conditionally) og kjør prosess. Alt gjøres asynkront i form av prosess tasks.
     * Intern sjekk på om hvorvidt registeropplysninger må reinnhentes.
     *
     * @return optional Prosess Task gruppenavn som kan brukes til å sjekke fremdrift
     */
    Optional<String> sjekkOgForberedAsynkInnhentingAvRegisteropplysningerOgKjørProsess(Behandling behandling);

    /**
     * Innhent registeropplysninger (hvis flagg satt) og kjør prosess asynkront.
     *
     * @param skalInnhenteRegisteropplysninger Hvorvidt registeropplysninger skal innhentes
     *
     * @param manuellGjenopptakelse
     * @return Prosess Task gruppenavn som kan brukes til å sjekke fremdrift
     */
    String asynkInnhentingAvRegisteropplysningerOgKjørProsess(Behandling behandling, boolean skalInnhenteRegisteropplysninger, boolean manuellGjenopptakelse);

    /**
     * Kjører prosess, (henter ikke inn registeropplysninger på nytt selv)
     *
     * @return Prosess Task gruppenavn som kan brukes til å sjekke fremdrift
     */
    String asynkKjørProsess(Behandling behandling);

    /** Sjekker om det pågår åpne prosess tasks (for angitt gruppe). Returnerer eventuelt task gruppe for eventuell åpen prosess task gruppe. */
    Optional<AsyncPollingStatus> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe);

    /** Hvorvidt betingelser for å hente inn registeropplysninger på nytt er oppfylt. */
    boolean skalInnhenteRegisteropplysningerPåNytt(Behandling behandling);

    /**
     * Gjenoppta behandling, start innhenting av registeropplysninger på nytt og kjør prosess hvis nødvendig.
     *
     * @return gruppenavn (prosesstask) hvis noe startet asynkront.
     */
    Optional<String> gjenopptaBehandling(Behandling behandling);

    void kanEndreBehandling(Long behandlingId, Long versjon);

    /**
     * Kjør behandlingsprosess asynkront videre.
     *
     * @return ProsessTask gruppe
     */
    String asynkFortsettBehandlingsprosess(Behandling behandling);

    /**
     * Åpner behandlingen for endringer ved å reaktivere inaktive aksjonspunkter før startpunktet
     * og hopper til første startpunkt. Gjøres asynkront.
     *
     * @return ProsessTask gruppe
     */
    String asynkTilbakestillOgÅpneBehandlingForEndringer(Long behandlingsId);
}
