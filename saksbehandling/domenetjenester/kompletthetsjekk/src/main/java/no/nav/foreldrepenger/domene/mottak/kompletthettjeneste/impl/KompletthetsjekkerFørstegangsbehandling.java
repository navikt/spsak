package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetResultat;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerSøknad;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;

@ApplicationScoped
@BehandlingTypeRef("BT-002")
@FagsakYtelseTypeRef("FP")
public class KompletthetsjekkerFørstegangsbehandling implements Kompletthetsjekker {
    private static final Logger LOGGER = LoggerFactory.getLogger(KompletthetsjekkerFørstegangsbehandling.class);

    private static final Integer TIDLIGST_VENTEFRIST_FØR_UTTAKSDATO_UKER = 3;
    private static final Integer VENTEFRIST_ETTER_MOTATT_DATO_UKER = 1;

    private KompletthetssjekkerSøknad kompletthetssjekkerSøknad;
    private KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private KompletthetsjekkerFelles fellesUtil;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private SøknadRepository søknadRepository;

    KompletthetsjekkerFørstegangsbehandling() {
        // CDI
    }

    @Inject
    public KompletthetsjekkerFørstegangsbehandling(@FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-002") KompletthetssjekkerSøknad kompletthetssjekkerSøknad,
                                                   @FagsakYtelseTypeRef("FP") @BehandlingTypeRef("BT-002") KompletthetssjekkerInntektsmelding kompletthetssjekkerInntektsmelding,
                                                   InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, KompletthetsjekkerFelles fellesUtil,
                                                   SkjæringstidspunktTjeneste skjæringstidspunktTjeneste, SøknadRepository søknadRepository) {
        this.kompletthetssjekkerSøknad = kompletthetssjekkerSøknad;
        this.kompletthetssjekkerInntektsmelding = kompletthetssjekkerInntektsmelding;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.fellesUtil = fellesUtil;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.søknadRepository = søknadRepository;
    }

    @Override
    public KompletthetResultat vurderSøknadMottatt(Behandling behandling) {
        if (!kompletthetssjekkerSøknad.erSøknadMottatt(behandling)) {
            // Litt implisitt forutsetning her, men denne sjekken skal bare ha bli kalt dersom søknad eller IM er mottatt
            LOGGER.info("Behandling {} er ikke komplett - søknad er ikke mottatt", behandling.getId()); // NOSONAR //$NON-NLS-1$
            return KompletthetResultat.ikkeOppfylt(fellesUtil.finnVentefristTilManglendeSøknad(), Venteårsak.AVV_DOK);
        }
        return KompletthetResultat.oppfylt();
    }

    @Override
    public KompletthetResultat vurderSøknadMottattForTidlig(Behandling behandling) {
        Optional<LocalDateTime> forTidligFrist = kompletthetssjekkerSøknad.erSøknadMottattForTidlig(behandling);
        if (forTidligFrist.isPresent()) {
            return KompletthetResultat.ikkeOppfylt(forTidligFrist.get(), Venteårsak.FOR_TIDLIG_SOKNAD);
        }
        return KompletthetResultat.oppfylt();
    }

    @Override
    public KompletthetResultat vurderForsendelseKomplett(Behandling behandling) {
        List<ManglendeVedlegg> manglendeInntektsmeldinger = kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldinger(behandling);
        if (!manglendeInntektsmeldinger.isEmpty()) {
            loggManglendeInntektsmeldinger(behandling.getId(), manglendeInntektsmeldinger);
            Optional<LocalDateTime> ventefristManglendeIM = finnVentefristTilManglendeInntektsmelding(behandling);
            return ventefristManglendeIM
                .map(frist -> KompletthetResultat.ikkeOppfylt(frist, Venteårsak.AVV_DOK))
                .orElse(KompletthetResultat.fristUtløpt());
        }
        if (!kompletthetssjekkerSøknad.utledManglendeVedleggForSøknad(behandling).isEmpty()) {
            Optional<LocalDateTime> ventefristTidligMottattSøknad = fellesUtil.finnVentefristTilForTidligMottattSøknad(behandling);
            return ventefristTidligMottattSøknad
                .map(frist -> KompletthetResultat.ikkeOppfylt(frist, Venteårsak.AVV_DOK))
                .orElse(KompletthetResultat.fristUtløpt());
        }
        return KompletthetResultat.oppfylt();
    }

    private void loggManglendeInntektsmeldinger(Long behandlingId, List<ManglendeVedlegg> manglendeInntektsmeldinger) {
        String arbgivere = manglendeInntektsmeldinger.stream().map(ManglendeVedlegg::getArbeidsgiver).collect(Collectors.toList()).toString();
        LOGGER.info("Behandling {} er ikke komplett - mangler IM fra arbeidsgivere: {}", behandlingId, arbgivere); // NOSONAR //$NON-NLS-1$
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
        manglendeVedlegg.addAll(kompletthetssjekkerInntektsmelding.utledManglendeInntektsmeldingerFraGrunnlag(behandling));
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

    private Optional<LocalDateTime> finnVentefristTilManglendeInntektsmelding(Behandling behandling) {
        LocalDate permisjonsstart = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);
        final LocalDate muligFrist = permisjonsstart.minusWeeks(TIDLIGST_VENTEFRIST_FØR_UTTAKSDATO_UKER);
        final LocalDate annenMuligFrist = søknadRepository.hentSøknad(behandling).getMottattDato().plusWeeks(VENTEFRIST_ETTER_MOTATT_DATO_UKER);
        final LocalDate ønsketFrist = muligFrist.isAfter(annenMuligFrist) ? muligFrist : annenMuligFrist;
        return fellesUtil.finnVentefrist(ønsketFrist);
    }
}
