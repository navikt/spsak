package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class BehandlingRelatertInformasjonApplikasjonTjenesteImpl implements BehandlingRelatertInformasjonApplikasjonTjeneste {
    private GrunnlagRepositoryProvider repositoryProvider;
    private Period relaterteYtelserVLPeriode;

    BehandlingRelatertInformasjonApplikasjonTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingRelatertInformasjonApplikasjonTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
                                                                @KonfigVerdi("relaterte.ytelser.vl.periode.start") Instance<Period> periode) {
        this.repositoryProvider = repositoryProvider;
        this.relaterteYtelserVLPeriode = periode.get();
    }

    @Override
    public List<TilgrensendeYtelserDto> hentRelaterteYtelser(final Behandling behandling, final AktørId aktørId, Boolean bareInnvilget) {
        return hentRelaterteYtelserFraVedtaksløsning(aktørId, bareInnvilget);
    }

    private List<TilgrensendeYtelserDto> hentRelaterteYtelserFraVedtaksløsning(final AktørId aktørId, boolean bareInnvilget) {
        final LocalDate periodeFraRelaterteYtelserSøkesIVL = LocalDate.now(FPDateUtil.getOffset()).minus(relaterteYtelserVLPeriode);
        final List<Fagsak> fagsakListe = repositoryProvider.getFagsakRepository().hentForBruker(aktørId);
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        List<TilgrensendeYtelserDto> relatertYtelser = new ArrayList<>();

        for (Fagsak fagsak : fagsakListe) {
            List<BehandlingVedtak> vedtakene = behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsak.getSaksnummer()).stream()
                .map(b -> behandlingRepository.hentResultatHvisEksisterer(b.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Behandlingsresultat::getBehandlingVedtak)
                .filter(Objects::nonNull)
                .filter(behandlingVedtak -> behandlingVedtak.getVedtaksdato().isAfter(periodeFraRelaterteYtelserSøkesIVL))
                .collect(toList());
            if (bareInnvilget) {
                relatertYtelser.addAll(vedtakene.stream()
                    .filter(behandlingVedtak -> behandlingVedtak.getVedtakResultatType().equals(VedtakResultatType.INNVILGET))
                    .map(behandlingVedtak -> BehandlingRelaterteYtelserMapper.mapFraFagsak(fagsak, behandlingVedtak.getVedtaksdato()))
                    .collect(toList()));
            } else {
                relatertYtelser.addAll(vedtakene.stream()
                    .map(behandlingVedtak -> BehandlingRelaterteYtelserMapper.mapFraFagsak(fagsak, behandlingVedtak.getVedtaksdato()))
                    .collect(toList()));
            }
        }
        return relatertYtelser;
    }
}
