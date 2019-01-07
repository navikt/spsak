package no.nav.foreldrepenger.behandling.steg.vedtak;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.impl.FinnAnsvarligSaksbehandler;
import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjeneste;
import no.nav.foreldrepenger.behandling.revurdering.impl.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class BehandlingVedtakTjenesteImpl implements BehandlingVedtakTjeneste {

    private RevurderingTjenesteProvider revurderingTjenesteProvider;
    private BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer;
    private BehandlingVedtakRepository behandlingVedtakRepository;

    BehandlingVedtakTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingVedtakTjenesteImpl(RevurderingTjenesteProvider revurderingTjenesteProvider, BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer, ResultatRepositoryProvider repositoryProvider) {
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
        this.behandlingVedtakEventPubliserer = behandlingVedtakEventPubliserer;
        this.behandlingVedtakRepository = repositoryProvider.getVedtakRepository();
    }

    @Override
    public void opprettBehandlingVedtak(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        RevurderingTjeneste revurderingTjeneste = revurderingTjenesteProvider.finnRevurderingTjenesteFor(behandling.getFagsak());

        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(behandling);
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandling);
        LocalDate vedtaksdato = LocalDate.now(FPDateUtil.getOffset());

        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(vedtakResultatType)
            .medAnsvarligSaksbehandler(ansvarligSaksbehandler)
            .medVedtaksdato(vedtaksdato)
            .medBehandlingsresultat(behandling.getBehandlingsresultat())
            .medBeslutning(revurderingTjeneste.erRevurderingMedUendretUtfall(behandling))
            .build();
        behandlingVedtakRepository.lagre(behandlingVedtak, kontekst.getSkriveLås());
        behandlingVedtakEventPubliserer.fireEvent(behandlingVedtak, behandling);
    }
}
