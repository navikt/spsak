package no.nav.foreldrepenger.behandling.steg.iverksettevedtak.fp;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.VurderØkonomiOppdrag;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.økonomioppdrag.Oppdragskontroll;
import no.nav.foreldrepenger.økonomistøtte.ØkonomioppdragRepository;

@FagsakYtelseTypeRef("FP")
@ApplicationScoped
public class VurderØkonomiOppdragFPImpl extends VurderØkonomiOppdrag {

    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private ØkonomioppdragRepository økonomioppdragRepository;

    VurderØkonomiOppdragFPImpl() {
        // for CDI proxy
    }

    @Inject
    public VurderØkonomiOppdragFPImpl(BehandlingRepositoryProvider repositoryProvider, ØkonomioppdragRepository økonomioppdragRepository) {
        this.beregningsresultatFPRepository = repositoryProvider.getBeregningsresultatFPRepository();
        this.økonomioppdragRepository = økonomioppdragRepository;
    }

    @Override
    public boolean skalSendeOppdrag(Behandling behandling, BehandlingVedtak behandlingVedtak) {
        if (BehandlingType.REVURDERING.equals(behandling.getType())) {
            return skalSendeOppdragForRevurdering(behandling, behandlingVedtak);
        }
        return skalSendeOppdragForFørstegangsbehandling(behandling, behandlingVedtak);
    }

    private boolean skalSendeOppdragForFørstegangsbehandling(Behandling behandling, BehandlingVedtak behandlingVedtak) {
        return erVedtakInnvilget(behandlingVedtak) && finnesTilkjentYtelse(behandling);
    }

    private boolean skalSendeOppdragForRevurdering(Behandling behandling, BehandlingVedtak behandlingVedtak) {
        if (behandlingVedtak.isBeslutningsvedtak()) {
            return false;
        }
        if (erVedtakAvslag(behandlingVedtak)) {
            return skalSendeOppdragHvisAvslåttVedtak(behandling);
        }
        return erVedtakInnvilget(behandlingVedtak) && finnesTilkjentYtelse(behandling);
    }

    private boolean skalSendeOppdragHvisAvslåttVedtak(Behandling behandling) {
        Fagsak fagsak = behandling.getFagsak();
        boolean gjelderInnvilgetYtelse = gjelderForrigeOppdragInnvilgetYtelse(fagsak);
        return gjelderInnvilgetYtelse || finnesTilkjentYtelse(behandling);
    }

    private boolean finnesTilkjentYtelse(Behandling behandling) {
        Optional<BeregningsresultatFP> beregningsresultatFPOpt = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);
        if (!beregningsresultatFPOpt.isPresent()) {
            return false;
        }
        BeregningsresultatFP beregningsresultatFP = beregningsresultatFPOpt.get();
        return beregningsresultatFP.getBeregningsresultatPerioder().stream()
            .flatMap(brPeriode -> brPeriode.getBeregningsresultatAndelList().stream())
            .anyMatch(andel -> andel.getDagsats() > 0);
    }

    private boolean gjelderForrigeOppdragInnvilgetYtelse(Fagsak fagsak) {
        Optional<Oppdragskontroll> forrigeOppdragOpt = økonomioppdragRepository.finnNyesteOppdragForSak(fagsak.getSaksnummer());
        if (forrigeOppdragOpt.isPresent()) {
            Oppdragskontroll forrigeOppdrag = forrigeOppdragOpt.get();
            return forrigeOppdrag.getOppdrag110Liste().stream()
                .flatMap(oppdrag110 -> oppdrag110.getOppdragslinje150Liste().stream())
                .anyMatch(oppdragslinje150 -> !oppdragslinje150.gjelderOpphør());
        }
        return false;
    }

    private boolean erVedtakAvslag(BehandlingVedtak behandlingVedtak) {
        return VedtakResultatType.AVSLAG.equals(behandlingVedtak.getVedtakResultatType());
    }

    private boolean erVedtakInnvilget(BehandlingVedtak behandlingVedtak) {
        return VedtakResultatType.INNVILGET.equals(behandlingVedtak.getVedtakResultatType());
    }
}
