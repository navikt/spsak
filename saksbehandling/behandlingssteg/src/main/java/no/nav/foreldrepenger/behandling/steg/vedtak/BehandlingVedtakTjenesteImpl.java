package no.nav.foreldrepenger.behandling.steg.vedtak;

import java.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.FinnAnsvarligSaksbehandler;
import no.nav.foreldrepenger.behandling.revurdering.RevurderingTjenesteProvider;
import no.nav.foreldrepenger.behandling.revurdering.fp.RevurderingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingVedtakEventPubliserer;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
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
    private BehandlingRepository behandlingRepository;

    BehandlingVedtakTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingVedtakTjenesteImpl(RevurderingTjenesteProvider revurderingTjenesteProvider, BehandlingVedtakEventPubliserer behandlingVedtakEventPubliserer, ResultatRepositoryProvider repositoryProvider) {
        this.revurderingTjenesteProvider = revurderingTjenesteProvider;
        this.behandlingVedtakEventPubliserer = behandlingVedtakEventPubliserer;
        this.behandlingVedtakRepository = repositoryProvider.getVedtakRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Override
    public void opprettBehandlingVedtak(BehandlingskontrollKontekst kontekst, Behandling behandling) {
        RevurderingTjeneste revurderingTjeneste = revurderingTjenesteProvider.finnRevurderingTjenesteFor(behandling.getFagsak());

        VedtakResultatType vedtakResultatType = UtledVedtakResultatType.utled(behandlingRepository, behandling);
        String ansvarligSaksbehandler = FinnAnsvarligSaksbehandler.finn(behandling);
        LocalDate vedtaksdato = LocalDate.now(FPDateUtil.getOffset());

        BehandlingVedtak behandlingVedtak = BehandlingVedtak.builder()
            .medVedtakResultatType(vedtakResultatType)
            .medAnsvarligSaksbehandler(ansvarligSaksbehandler)
            .medVedtaksdato(vedtaksdato)
            .medBehandlingsresultat(behandlingRepository.hentResultat(behandling.getId()))
            .medBeslutning(revurderingTjeneste.erRevurderingMedUendretUtfall(behandling))
            .build();
        behandlingVedtakRepository.lagre(behandlingVedtak, kontekst.getSkriveLås());
        behandlingVedtakEventPubliserer.fireEvent(behandlingVedtak, behandling);
    }
}
