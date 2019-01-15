package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import static no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus.SELVSTENDIG_NÆRINGSDRIVENDE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Inntektskategori;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;

@ApplicationScoped
public class FastsettInntektskategoriFraSøknadTjeneste {

    private KodeverkRepository kodeverkRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;


    FastsettInntektskategoriFraSøknadTjeneste() {
        // for CDI proxy
    }

    @Inject
    public FastsettInntektskategoriFraSøknadTjeneste(GrunnlagRepositoryProvider repositoryProvider, InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    public void fastsettInntektskategori(Beregningsgrunnlag beregningsgrunnlag, Behandling behandling) {
        beregningsgrunnlag.getBeregningsgrunnlagPerioder().stream()
            .flatMap(p -> p.getBeregningsgrunnlagPrStatusOgAndelList().stream())
            .forEach(andel ->
                BeregningsgrunnlagPrStatusOgAndel.builder(andel).medInntektskategori(finnInntektskategoriForStatus(andel.getAktivitetStatus(), behandling)));
    }

    public Optional<Inntektskategori> finnHøgastPrioriterteInntektskategoriForSN(List<Inntektskategori> inntektskategorier) {
        if (inntektskategorier.isEmpty()) {
            return Optional.empty();
        }
        if (inntektskategorier.size() == 1) {
            return Optional.of(inntektskategorier.get(0));
        }
        if (inntektskategorier.contains(Inntektskategori.FISKER)) {
            return Optional.of(Inntektskategori.FISKER);
        }
        if (inntektskategorier.contains(Inntektskategori.JORDBRUKER)) {
            return Optional.of(Inntektskategori.JORDBRUKER);
        }
        if (inntektskategorier.contains(Inntektskategori.DAGMAMMA)) {
            return Optional.of(Inntektskategori.DAGMAMMA);
        }
        return Optional.of(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
    }

    private Inntektskategori finnInntektskategoriForStatus(AktivitetStatus aktivitetStatus, Behandling behandling) {
        if (SELVSTENDIG_NÆRINGSDRIVENDE.equals(aktivitetStatus)) {
            return finnInntektskategoriForSelvstendigNæringsdrivende(behandling);
        }
        Map<AktivitetStatus, Set<Inntektskategori>> map = kodeverkRepository.hentKodeRelasjonForKodeverk(AktivitetStatus.class, Inntektskategori.class);
        return map.getOrDefault(aktivitetStatus, Collections.singleton(Inntektskategori.UDEFINERT))
            .stream().findFirst().orElse(Inntektskategori.UDEFINERT);
    }

    private Inntektskategori finnInntektskategoriForSelvstendigNæringsdrivende(Behandling behandling) {
        Optional<OppgittOpptjening> oppgittOpptjening = inntektArbeidYtelseTjeneste.hentAggregat(behandling).getOppgittOpptjening();
        if (oppgittOpptjening.isPresent() && !oppgittOpptjening.get().getEgenNæring().isEmpty()) {
            Set<VirksomhetType> virksomhetTypeSet = oppgittOpptjening.get().getEgenNæring().stream()
                .map(EgenNæring::getVirksomhetType)
                .distinct()
                .collect(Collectors.toSet());

            Map<VirksomhetType, Set<Inntektskategori>> map = kodeverkRepository.hentKodeRelasjonForKodeverk(VirksomhetType.class, Inntektskategori.class);

            List<Inntektskategori> inntektskategorier = virksomhetTypeSet.stream()
                .flatMap(virksomhetType ->
                    map.getOrDefault(virksomhetType, Collections.singleton(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE)).stream())
                .collect(Collectors.toList());

            return finnHøgastPrioriterteInntektskategoriForSN(inntektskategorier).orElse(Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE);
        }
        return Inntektskategori.SELVSTENDIG_NÆRINGSDRIVENDE;
    }
}
