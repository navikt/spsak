package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
public class KompletthetssjekkerSøknadRevurdering extends AbstractKompletthetssjekkerSøknad {
    private static final Logger LOGGER = LoggerFactory.getLogger(KompletthetssjekkerSøknadRevurdering.class);

    private SøknadRepository søknadRepository;
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepository;

    @Inject
    public KompletthetssjekkerSøknadRevurdering(DokumentArkivTjeneste dokumentArkivTjeneste,
                                                  BehandlingRepositoryProvider repositoryProvider,
                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                  @KonfigVerdi("ventefrist.uker.ved.tidlig.fp.soeknad") Integer antallUkerVentefristVedForTidligSøknad) {
        super(repositoryProvider.getKodeverkRepository(),
            skjæringstidspunktTjeneste, antallUkerVentefristVedForTidligSøknad, repositoryProvider.getSøknadRepository());
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    }

    /**
     * Spør Joark om dokumentliste og sjekker det som finnes i vedleggslisten på søknaden mot det som ligger i Joark.
     * I tillegg sjekkes endringssøknaden for påkrevde vedlegg som følger av utsettelse.
     * Alle dokumenter må være mottatt etter vedtaksdatoen på gjeldende innvilgede vedtak.
     *
     * @param behandling
     * @return Liste over manglende vedlegg
     */
    @Override
    public List<ManglendeVedlegg> utledManglendeVedleggForSøknad(Behandling behandling) {
        Objects.requireNonNull(behandling.getId(), "behandlingId må være satt"); // NOSONAR //$NON-NLS-1$

        final Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);

        LocalDate vedtaksdato = behandlingVedtakRepository.hentBehandlingVedtakFraRevurderingensOriginaleBehandling(behandling).getVedtaksdato();

        Set<DokumentTypeId> arkivDokumentTypeIds = dokumentArkivTjeneste.hentDokumentTypeIdForSak(behandling.getFagsak().getSaksnummer(), vedtaksdato, Collections.emptySet());

        final List<ManglendeVedlegg> manglendeVedlegg = identifiserManglendeVedlegg(søknad, arkivDokumentTypeIds);

        if (!manglendeVedlegg.isEmpty()) {
            LOGGER.info("Behandling {} er ikke komplett - mangler følgende vedlegg til søknad: {}", behandling.getId(),
                lagDokumentTypeString(manglendeVedlegg)); // NOSONAR //$NON-NLS-1$
        }
        return manglendeVedlegg;
    }

    private String lagDokumentTypeString(List<ManglendeVedlegg> manglendeVedlegg) {
        return manglendeVedlegg.stream().map(mv -> mv.getDokumentType().getKode()).collect(toList()).toString();
    }
}
