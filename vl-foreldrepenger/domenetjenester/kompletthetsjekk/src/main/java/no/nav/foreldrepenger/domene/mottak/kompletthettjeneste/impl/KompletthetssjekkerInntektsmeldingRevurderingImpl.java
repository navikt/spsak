package no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingTypeRef;
import no.nav.foreldrepenger.behandlingskontroll.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetssjekkerInntektsmelding;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;

@ApplicationScoped
@BehandlingTypeRef("BT-004")
@FagsakYtelseTypeRef("FP")
public class KompletthetssjekkerInntektsmeldingRevurderingImpl implements KompletthetssjekkerInntektsmelding {
    private static final Logger LOGGER = LoggerFactory.getLogger(KompletthetssjekkerInntektsmeldingRevurderingImpl.class);

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private SøknadRepository søknadRepository;

    KompletthetssjekkerInntektsmeldingRevurderingImpl() {
        // CDI
    }

    @Inject
    public KompletthetssjekkerInntektsmeldingRevurderingImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                             BehandlingRepositoryProvider repositoryProvider) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.søknadRepository = repositoryProvider.getSøknadRepository();
    }

    @Override
    public List<ManglendeVedlegg> utledManglendeInntektsmeldinger(Behandling behandling) {
        return doUtledManglendeInntektsmeldinger(behandling, true);
    }

    @Override
    public List<ManglendeVedlegg> utledManglendeInntektsmeldingerFraGrunnlag(Behandling behandling) {
        return doUtledManglendeInntektsmeldinger(behandling, false);
    }

    private List<ManglendeVedlegg> doUtledManglendeInntektsmeldinger(Behandling behandling, boolean brukArkiv) {
        Søknad søknad = søknadRepository.hentSøknad(behandling);

        if (erArbeidstakerOgEndringIFerie(søknad)) {
            // Må finnes IMer for alle arbeidsforhold
            List<ManglendeVedlegg> manglendeVedlegg = (brukArkiv ? inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraArkiv(behandling)
                : inntektArbeidYtelseTjeneste.utledManglendeInntektsmeldingerFraGrunnlag(behandling))
                    .entrySet()
                    .stream()
                    .map(it -> new ManglendeVedlegg(DokumentTypeId.INNTEKTSMELDING, it.getKey()))
                    .collect(Collectors.toList());

            loggManglendeVedlegg(behandling, manglendeVedlegg, "ferie");
            return manglendeVedlegg;
        }

        // Må finnes IMer for arbeidsforholdene som berøres av endringene i søknaden
        List<ManglendeVedlegg> manglendeVedlegg = utledManglendeVedleggForArbeidsforholdBerørtAvEndringssøknad(behandling, søknad);
        loggManglendeVedlegg(behandling, manglendeVedlegg, "arbeid/gradering");
        return manglendeVedlegg;
    }

    private boolean erArbeidstakerOgEndringIFerie(Søknad søknad) {
        Objects.requireNonNull(søknad, "søknad kan ikke være null"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(søknad.getFordeling(), "OppgittFordeling kan ikke være null"); // NOSONAR //$NON-NLS-1$

        List<OppgittPeriode> oppgittePerioder = søknad.getFordeling().getOppgittePerioder();

        for (OppgittPeriode oppgittPeriode : oppgittePerioder) {
            if (oppgittPeriode.getErArbeidstaker() && UtsettelseÅrsak.FERIE.equals(oppgittPeriode.getÅrsak())) {
                return true;
            }
        }
        return false;
    }

    private List<ManglendeVedlegg> utledManglendeVedleggForArbeidsforholdBerørtAvEndringssøknad(Behandling behandling, Søknad søknad) {
        Set<String> virksomheterSomTrengerInntektsmelding = finnVirksomheterSomTrengerInntektsmelding(søknad);
        inntektArbeidYtelseTjeneste.hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(behandling)
            .forEach(im -> virksomheterSomTrengerInntektsmelding.remove(im.getVirksomhet().getOrgnr()));

        return virksomheterSomTrengerInntektsmelding.stream()
            .map(v -> new ManglendeVedlegg(DokumentTypeId.INNTEKTSMELDING, v))
            .collect(Collectors.toList());
    }

    private Set<String> finnVirksomheterSomTrengerInntektsmelding(Søknad søknad) {
        Set<String> virksomheter = new HashSet<>();
        List<OppgittPeriode> oppgittePerioder = søknad.getFordeling().getOppgittePerioder();

        for (OppgittPeriode oppgittPeriode : oppgittePerioder) {
            if (oppgittPeriode.getErArbeidstaker()
                && (oppgittPeriode.getArbeidsprosent() != null || UtsettelseÅrsak.ARBEID.equals(oppgittPeriode.getÅrsak()))) {
                virksomheter.add(oppgittPeriode.getVirksomhet().getOrgnr());
            }
        }
        return virksomheter;
    }

    private void loggManglendeVedlegg(Behandling behandling, List<ManglendeVedlegg> manglendeVedlegg, String endring) {
        if (!manglendeVedlegg.isEmpty()) {
            String arbgivere = manglendeVedlegg.stream().map(ManglendeVedlegg::getArbeidsgiver).collect(Collectors.toList()).toString();
            LOGGER.info("Behandling {} er ikke komplett etter endring i " + endring + " - mangler IM fra arbeidsgivere: {}", behandling.getId(), arbgivere); // NOSONAR //$NON-NLS-1$
        }
    }
}
