package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl.fp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerSøknad;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;

@ApplicationScoped
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
public class KompletthetsjekkerFPRevurdering implements Kompletthetsjekker {
    private static final Logger LOGGER = LoggerFactory.getLogger(KompletthetsjekkerFPRevurdering.class);

    private KompletthetssjekkerSøknad kompletthetssjekkerSøknad;
    private KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding;
    private KompletthetsjekkerFPFelles fellesUtil;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SøknadRepository søknadRepository;
    private BehandlingVedtakRepository behandlingVedtakRepository;

    KompletthetsjekkerFPRevurdering() {
        // CDI
    }

    @Inject
    public KompletthetsjekkerFPRevurdering(@FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") KompletthetssjekkerSøknad kompletthetssjekkerSøknad,
                                           @FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-004") KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding,
                                           KompletthetsjekkerFPFelles fellesUtil,
                                           InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                           BehandlingRepositoryProvider repositoryProvider) {
        this.kompletthetssjekkerSøknad = kompletthetssjekkerSøknad;
        this.kompletthetssjekkerInntektsmelding = kompletthetssjekkerInntektsmelding;
        this.fellesUtil = fellesUtil;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.søknadRepository = repositoryProvider.getSøknadRepository();
        this.behandlingVedtakRepository = repositoryProvider.getBehandlingVedtakRepository();
    }

    @Override
    public KompletthetResultat vurderSøknadMottatt(Behandling behandling) {
        // Ikke relevant for revurdering - denne kontrollen er allerede håndtert av førstegangsbehandlingen
        return KompletthetResultat.oppfylt();
    }

    @Override
    public KompletthetResultat vurderSøknadMottattForTidlig(Behandling behandling) {
        // Ikke relevant for revurdering - denne kontrollen er allerede håndtert av førstegangsbehandlingen
        return KompletthetResultat.oppfylt();
    }

    @Override
    public KompletthetResultat vurderForsendelseKomplett(Behandling behandling) {
        if (behandling.erBerørtBehandling()) {
            return KompletthetResultat.oppfylt();
        }

        List<Inntektsmelding> inntektsmeldinger = inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(behandling);
        if (!inntektsmeldinger.isEmpty()) {
            return vurderKompletthetForInntektsmeldingUtenSøknad(behandling, inntektsmeldinger);
        }

        // Når verken endringssøknad eller inntektsmelding er mottatt har vi ikke noe å sjekke kompletthet mot
        // og behandlingen slippes igjennom. Dette gjelder for eksempel ved fødselshendelse.
        return KompletthetResultat.oppfylt();
    }

    @Override
    public boolean erForsendelsesgrunnlagKomplett(Behandling behandling) {
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekkerSøknad.utledManglendeVedleggForSøknad(behandling);
        manglendeVedlegg.addAll(kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldingerFraGrunnlag(behandling));
        return manglendeVedlegg.isEmpty();
    }

    @Override
    public List<ManglendeVedlegg> utledAlleManglendeVedleggForForsendelse(Behandling behandling) {
        List<ManglendeVedlegg> manglendeVedlegg = kompletthetssjekkerSøknad.utledManglendeVedleggForSøknad(behandling);
        manglendeVedlegg.addAll(kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldinger(behandling));
        return manglendeVedlegg;
    }

    @Override
    public List<ManglendeVedlegg> utledAlleManglendeVedleggSomIkkeKommer(Behandling behandling) {
        return inntektArbeidYtelseTjeneste
            .hentAlleInntektsmeldingerSomIkkeKommer(behandling)
            .stream()
            .map(e -> new ManglendeVedlegg(DokumentTypeId.INNTEKTSMELDING, e.getArbeidsgiver().getIdentifikator(), true))
            .collect(Collectors.toList());
    }

    private List<ManglendeVedlegg> hentManglendeInntektsmeldinger(Behandling behandling) {
        List<ManglendeVedlegg> manglendeInntektsmeldinger = kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldinger(behandling);
        if (!manglendeInntektsmeldinger.isEmpty()) {
            loggManglendeInntektsmeldinger(behandling.getId(), manglendeInntektsmeldinger);
        }
        return manglendeInntektsmeldinger;
    }

    private void loggManglendeInntektsmeldinger(Long behandlingId, List<ManglendeVedlegg> manglendeInntektsmeldinger) {
        String arbgivere = manglendeInntektsmeldinger.stream().map(ManglendeVedlegg::getArbeidsgiver).collect(Collectors.toList()).toString();
        LOGGER.info("Behandling {} er ikke komplett - mangler IM fra arbeidsgivere: {}", behandlingId, arbgivere); // NOSONAR //$NON-NLS-1$
    }

    private KompletthetResultat opprettKompletthetResultatMedVentefrist(Behandling behandling) {
        Optional<LocalDateTime> ventefristTidligMottattSøknad = fellesUtil.finnVentefristTilForTidligMottattSøknad(behandling);
        return ventefristTidligMottattSøknad
            .map(frist -> KompletthetResultat.ikkeOppfylt(frist, Venteårsak.AVV_DOK))
            .orElse(KompletthetResultat.fristUtløpt());
    }

    private KompletthetResultat vurderKompletthetForInntektsmeldingUtenSøknad(Behandling revurdering, List<Inntektsmelding> inntektsmeldinger) {
        finnOriginalBehandling(revurdering);

        boolean graderingEndret = false;
        boolean arbeidEndret = false;
        boolean ferieEndret = false;

        for (Inntektsmelding inntektsmelding : inntektsmeldinger) {
            // TODO SP: ??
        }


        if (graderingEndret || arbeidEndret || ferieEndret) {
            LOGGER.info("Behandling {} er ikke komplett for IM uten søknad: graderingEndret={}, arbeidEndret={}, ferieEndret={}", revurdering.getId(), graderingEndret, arbeidEndret, ferieEndret); // NOSONAR //$NON-NLS-1$
        }

        if (graderingEndret || arbeidEndret) {
            fellesUtil.sendBrev(revurdering, DokumentMalType.REVURDERING_DOK);
            return KompletthetResultat.ikkeOppfylt(fellesUtil.finnVentefristTilManglendeSøknad(), Venteårsak.AVV_DOK);
        } else if (ferieEndret) {
            fellesUtil.sendBrev(revurdering, DokumentMalType.INNTEKTSMELDING_FOR_TIDLIG_DOK);
            return KompletthetResultat.ikkeOppfylt(fellesUtil.finnVentefristTilManglendeSøknad(), Venteårsak.AVV_DOK);
        } else {
            return KompletthetResultat.oppfylt();
        }
    }

    private Behandling finnOriginalBehandling(Behandling revurdering) {
        return revurdering.getOriginalBehandling()
            .orElseThrow(() -> new IllegalStateException("Utviklerfeil: Original behandling mangler på revurdering - skal ikke skje"));
    }
}
