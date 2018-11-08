package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.fp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Fellesklasse for gjenbrukte metode av subklasser for {@link KompletthetsjekkerFP}.
 * <p>
 *     Favor composition over inheritance
 */
@ApplicationScoped
public class KompletthetsjekkerFPFelles {


    /**
     * Disse konstantene ligger hardkodet (og ikke i KonfigVerdi), da endring i en eller flere av disse vil
     * sannsynnlig kreve kodeendring
     */
    private static final Integer VENTEFRIST_FRAM_I_TID_FRA_MOTATT_DATO_UKER = 3;
    private static final Integer VENTEFRIST_FOR_MANGLENDE_SØKNAD = 4;

    private SøknadRepository søknadRepository;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    KompletthetsjekkerFPFelles() {
        // CDI
    }

    @Inject
    public KompletthetsjekkerFPFelles(BehandlingRepositoryProvider provider, DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste) {
        this.søknadRepository = provider.getSøknadRepository();
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
    }

    public Optional<LocalDateTime> finnVentefristTilForTidligMottattSøknad(Behandling behandling) {
        Objects.requireNonNull(behandling.getId(), "behandlingId må være satt"); // NOSONAR //$NON-NLS-1$
        Søknad søknad = søknadRepository.hentSøknad(behandling);
        Objects.requireNonNull(søknad, "søknad kan ikke være null"); // NOSONAR //$NON-NLS-1$

        final LocalDate ønsketFrist = søknad.getMottattDato().plusWeeks(VENTEFRIST_FRAM_I_TID_FRA_MOTATT_DATO_UKER);
        return finnVentefrist(ønsketFrist);
    }

    public Optional<LocalDateTime> finnVentefrist(LocalDate ønsketFrist) {
        if (ønsketFrist.isAfter(LocalDate.now())) {
            LocalDateTime ventefrist = LocalDateTime.of(ønsketFrist, LocalDateTime.now(FPDateUtil.getOffset()).toLocalTime());
            return Optional.of(ventefrist);
        }
        return Optional.empty();
    }

    public LocalDateTime finnVentefristTilManglendeSøknad() {
        return LocalDateTime.now().plusWeeks(VENTEFRIST_FOR_MANGLENDE_SØKNAD);
    }

    public void sendBrev(Behandling behandling, String dokumentMalType, String årsakskode) {
        if (!dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(behandling.getId(), dokumentMalType)) {
            BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), dokumentMalType, null, årsakskode);
            dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.VEDTAKSLØSNINGEN);
        }
    }
}
