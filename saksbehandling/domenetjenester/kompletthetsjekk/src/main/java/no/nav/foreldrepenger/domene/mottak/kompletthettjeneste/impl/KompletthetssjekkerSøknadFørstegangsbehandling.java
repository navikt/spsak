package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@BehandlingTypeRef("BT-002")
@FagsakYtelseTypeRef("FP")
public class KompletthetssjekkerSøknadFørstegangsbehandling extends AbstractKompletthetssjekkerSøknad {
    private static final Logger LOGGER = LoggerFactory.getLogger(KompletthetssjekkerSøknadFørstegangsbehandling.class);

    private SøknadRepository søknadRepository;
    private DokumentArkivTjeneste dokumentArkivTjeneste;

    @Inject
    public KompletthetssjekkerSøknadFørstegangsbehandling(DokumentArkivTjeneste dokumentArkivTjeneste,
                                                            BehandlingRepositoryProvider repositoryProvider,
                                                            SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                            @KonfigVerdi("ventefrist.uker.ved.tidlig.fp.soeknad") Integer antallUkerVentefristVedForTidligSøknad) {
        super(repositoryProvider.getKodeverkRepository(),
            skjæringstidspunktTjeneste, antallUkerVentefristVedForTidligSøknad, repositoryProvider.getSøknadRepository());
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
    }

    /**
     * Spør Joark om dokumentliste og sjekker det som finnes i vedleggslisten på søknaden mot det som ligger i Joark.
     * Vedleggslisten på søknaden regnes altså i denne omgang som fasit på hva som er påkrevd.
     *
     * @param behandling
     * @return Liste over manglende vedlegg
     */
    @Override
    public List<ManglendeVedlegg> utledManglendeVedleggForSøknad(Behandling behandling) {
        final Optional<Søknad> søknad = søknadRepository.hentSøknadHvisEksisterer(behandling);
        Set<DokumentTypeId> dokumentTypeIds = dokumentArkivTjeneste.hentDokumentTypeIdForSak(behandling.getFagsak().getSaksnummer(), LocalDate.MIN, Collections.emptySet());
        List<ManglendeVedlegg> manglendeVedlegg = identifiserManglendeVedlegg(søknad, dokumentTypeIds);

        if (!manglendeVedlegg.isEmpty()) {
            LOGGER.info("Behandling {} er ikke komplett - mangler følgende vedlegg til søknad: {}", behandling.getId(),
                lagDokumentTypeString(manglendeVedlegg)); // NOSONAR //$NON-NLS-1$
        }

        return manglendeVedlegg;
    }

    private String lagDokumentTypeString(List<ManglendeVedlegg> manglendeVedlegg) {
        return manglendeVedlegg.stream().map(mv -> mv.getDokumentType().getKode()).collect(Collectors.toList()).toString();
    }
}
