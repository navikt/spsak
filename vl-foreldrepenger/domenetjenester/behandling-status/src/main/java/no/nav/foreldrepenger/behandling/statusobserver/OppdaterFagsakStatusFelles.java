package no.nav.foreldrepenger.behandling.statusobserver;

import java.time.LocalDate;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatus;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakStatusEventPubliserer;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class OppdaterFagsakStatusFelles {

    private FagsakRepository fagsakRepository;
    private FagsakStatusEventPubliserer fagsakStatusEventPubliserer;
    private BehandlingRepository behandlingRepository;
    private Integer foreldelsesfristFP;

    OppdaterFagsakStatusFelles() {
    }

    @Inject
    public OppdaterFagsakStatusFelles(BehandlingRepositoryProvider provider,
                                      FagsakStatusEventPubliserer fagsakStatusEventPubliserer,
                                      @KonfigVerdi("foreldelsesfrist.foreldrenger.år") Integer foreldelsesfristFP) {
        this.fagsakRepository = provider.getFagsakRepository();
        this.fagsakStatusEventPubliserer = fagsakStatusEventPubliserer;
        this.behandlingRepository = provider.getBehandlingRepository();
        this.foreldelsesfristFP = foreldelsesfristFP;
    }

    void oppdaterFagsakStatus(Behandling behandling, FagsakStatus nyStatus) {
        Fagsak fagsak = behandling.getFagsak();
        FagsakStatus gammelStatus = fagsak.getStatus();
        Long fagsakId = fagsak.getId();
        fagsakRepository.oppdaterFagsakStatus(fagsakId, nyStatus);

        if (fagsakStatusEventPubliserer != null) {
            fagsakStatusEventPubliserer.fireEvent(fagsak, behandling, gammelStatus, nyStatus);
        }
    }

    public boolean ingenLøpendeYtelsesvedtak(Behandling behandling) {
        Optional<Behandling> sisteInnvilgedeBehandling = behandlingRepository.finnSisteAvsluttedeIkkeHenlagteBehandling(behandling.getFagsakId());

        if (sisteInnvilgedeBehandling.isPresent()) {
            Behandling sisteBehandling = sisteInnvilgedeBehandling.get();
            Optional<LocalDate> fødselsdato = Optional.of(LocalDate.now()); // FIXME SP - Fjernet FamilieHendelse, trenger erstattning?
            Optional<LocalDate> omsorgsovertalsesdato = Optional.of(LocalDate.now()); // FIXME SP - Fjernet FamilieHendelse, trenger erstattning?

            Optional<LocalDate> maksDatoUttak = Optional.of(LocalDate.now()); // FIXME SP - har fjernet uttak, trenger erstatning?
            
            return erDatoUtløpt(maksDatoUttak, LocalDate.now())
                || erDatoUtløpt(fødselsdato, LocalDate.now().minusYears(foreldelsesfristFP))
                || erDatoUtløpt(omsorgsovertalsesdato, LocalDate.now().minusYears(foreldelsesfristFP))
                || erVedtakResultat(sisteBehandling, VedtakResultatType.AVSLAG)
                || erVedtakResultat(sisteBehandling, VedtakResultatType.OPPHØR);
        }
        return true;
    }

    private boolean erDatoUtløpt(Optional<LocalDate> dato, LocalDate grensedato) {
        if (!dato.isPresent()) {
            // Kan ikke avgjøre om dato er utløpt
            return false;
        }
        return dato.get().isBefore(grensedato);
    }

    private boolean erVedtakResultat(Behandling behandling, VedtakResultatType vedtakResultat) {
        return Optional.ofNullable(behandling.getBehandlingsresultat())
            .map(Behandlingsresultat::getBehandlingVedtak)
            .map(vedtak -> vedtak.getVedtakResultatType().equals(vedtakResultat))
            .orElse(Boolean.FALSE);
    }

}
