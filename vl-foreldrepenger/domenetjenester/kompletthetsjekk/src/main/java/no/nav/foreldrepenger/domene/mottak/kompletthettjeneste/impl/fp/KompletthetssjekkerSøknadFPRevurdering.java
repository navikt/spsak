package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.fp;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokumentarkiv.DokumentArkivTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
public class KompletthetssjekkerSøknadFPRevurdering extends KompletthetssjekkerSøknadFP {
    private static final Logger LOGGER = LoggerFactory.getLogger(KompletthetssjekkerSøknadFPRevurdering.class);

    private SøknadRepository søknadRepository;
    private DokumentArkivTjeneste dokumentArkivTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private KodeverkRepository kodeverkRepository;
    private MottatteDokumentRepository mottatteDokumentRepository;

    @Inject
    public KompletthetssjekkerSøknadFPRevurdering(DokumentArkivTjeneste dokumentArkivTjeneste,
                                                  BehandlingRepositoryProvider repositoryProvider,
                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                                  @KonfigVerdi("ventefrist.uker.ved.tidlig.fp.soeknad") Integer antallUkerVentefristVedForTidligSøknad) {
        super(repositoryProvider.getKodeverkRepository(),
            skjæringstidspunktTjeneste, antallUkerVentefristVedForTidligSøknad, repositoryProvider.getSøknadRepository(), repositoryProvider.getMottatteDokumentRepository());
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.dokumentArkivTjeneste = dokumentArkivTjeneste;
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.mottatteDokumentRepository = repositoryProvider.getMottatteDokumentRepository();
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
        List<DokumentTypeId> mottatteDokumentTypeIder = mottatteDokumentRepository.hentMottatteDokumentVedleggPåBehandlingId(behandling.getId())
            .stream().map(MottattDokument::getDokumentTypeId).collect(toList());

        Set<DokumentTypeId> arkivDokumentTypeIds = dokumentArkivTjeneste.hentDokumentTypeIdForSak(behandling.getFagsak().getSaksnummer(), vedtaksdato, mottatteDokumentTypeIder);

        final List<ManglendeVedlegg> manglendeVedlegg = identifiserManglendeVedlegg(søknad, arkivDokumentTypeIds);
        final List<ManglendeVedlegg> manglendeVedleggUtsettelse = identifiserManglendeVedleggSomFølgerAvUtsettelse(søknad, arkivDokumentTypeIds);
        manglendeVedlegg.addAll(manglendeVedleggUtsettelse);

        if (!manglendeVedlegg.isEmpty()) {
            LOGGER.info("Behandling {} er ikke komplett - mangler følgende vedlegg til søknad: {}", behandling.getId(),
                lagDokumentTypeString(manglendeVedlegg)); // NOSONAR //$NON-NLS-1$
        }
        return manglendeVedlegg;
    }

    private List<ManglendeVedlegg> identifiserManglendeVedleggSomFølgerAvUtsettelse(Optional<Søknad> søknad, Set<DokumentTypeId> dokumentTypeIdSet) {
        if (!søknad.isPresent() || søknad.get().getFordeling() == null) {
            return emptyList();
        }

        List<ManglendeVedlegg> manglendeVedlegg = new ArrayList<>();
        List<OppgittPeriode> oppgittePerioder = søknad.get().getFordeling().getOppgittePerioder();

        oppgittePerioder.stream().map(OppgittPeriode::getÅrsak).forEach(årsak -> {
            if (UtsettelseÅrsak.SYKDOM.equals(årsak) && !dokumentTypeIdSet.contains(DokumentTypeId.LEGEERKLÆRING)) {
                manglendeVedlegg.add(new ManglendeVedlegg(finnDokumentTypeId(DokumentTypeId.LEGEERKLÆRING)));
            } else if ((UtsettelseÅrsak.INSTITUSJON_SØKER.equals(årsak) || UtsettelseÅrsak.INSTITUSJON_BARN.equals(årsak))
                && !dokumentTypeIdSet.contains(DokumentTypeId.DOK_INNLEGGELSE)) {
                manglendeVedlegg.add(new ManglendeVedlegg(finnDokumentTypeId(DokumentTypeId.DOK_INNLEGGELSE)));
            }
        });

        return manglendeVedlegg;
    }

    private DokumentTypeId finnDokumentTypeId(DokumentTypeId dokTypeId) {
        DokumentTypeId dokumentTypeId;
        try {
            dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, dokTypeId.getKode());
        } catch (NoResultException e) { //NOSONAR
            // skal tåle dette
            dokumentTypeId = kodeverkRepository.finn(DokumentTypeId.class, DokumentTypeId.UDEFINERT);
        }
        return dokumentTypeId;
    }

    private String lagDokumentTypeString(List<ManglendeVedlegg> manglendeVedlegg) {
        return manglendeVedlegg.stream().map(mv -> mv.getDokumentType().getKode()).collect(toList()).toString();
    }
}
