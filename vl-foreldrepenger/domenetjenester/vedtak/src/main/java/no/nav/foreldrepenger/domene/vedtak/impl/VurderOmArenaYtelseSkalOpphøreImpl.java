package no.nav.foreldrepenger.domene.vedtak.impl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatFP;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.BeregningsresultatPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatFPRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.OppgaveTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.vedtak.VurderOmArenaYtelseSkalOpphøre;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.vedtak.felles.jpa.tid.ÅpenDatoIntervallEntitet;

@ApplicationScoped
public class VurderOmArenaYtelseSkalOpphøreImpl implements VurderOmArenaYtelseSkalOpphøre {
    private static final Logger log = LoggerFactory.getLogger(VurderOmArenaYtelseSkalOpphøreImpl.class);

    private static final long HALV_MELDEKORT_PERIODE = 8;

    private BeregningsresultatFPRepository beregningsresultatFPRepository;
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private BehandlingVedtakRepository behandlingVedtakRepository;
    private OppgaveTjeneste oppgaveTjeneste;

    VurderOmArenaYtelseSkalOpphøreImpl() {
        // for CDI proxy
    }

    @Inject
    public VurderOmArenaYtelseSkalOpphøreImpl(BeregningsresultatFPRepository beregningsresultatFPRepository,
                                              InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                              BehandlingVedtakRepository behandlingVedtakRepository,
                                              OppgaveTjeneste oppgaveTjeneste) {
        this.beregningsresultatFPRepository = beregningsresultatFPRepository;
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.behandlingVedtakRepository = behandlingVedtakRepository;
        this.oppgaveTjeneste = oppgaveTjeneste;
    }

    @Override
    public void opprettOppgaveHvisArenaytelseSkalOpphøre(String aktørId, Behandling behandling) {
        boolean arenaYtelseSkalOpphøre = vurder(behandling, aktørId);
        if (arenaYtelseSkalOpphøre) {
            List<BeregningsresultatPeriode> beregningsresultatPeriodeList = beregningsresultatFPRepository
                .hentBeregningsresultatFP(behandling)
                .map(BeregningsresultatFP::getBeregningsresultatPerioder)
                .orElseThrow(() -> new IllegalStateException(String.format("Finner ikke beregningsresultat for behandling %d", behandling.getId())));
            LocalDate førsteUttaksdato = finnFørsteUttaksdato(beregningsresultatPeriodeList);
            String oppgaveId = oppgaveTjeneste.opprettOppgaveStopUtbetalingAvARENAYtelse(behandling.getId(), førsteUttaksdato);
            log.info("Oppgave opprettet i GSAK slik at Arena kan behandle saken videre. Oppgavenummer: {}", oppgaveId);
        }
    }

    @Override
    public boolean vurder(Behandling behandling, String aktørId) {
        // har bruker ytelser i ARENA?
        List<Ytelse> arenaYtelser = finnArenaYtelser(behandling, aktørId);
        if (arenaYtelser.isEmpty()) {
            return false;
        }
        // Hvis behandling er innvilget, finner man en startdato
        if (BehandlingResultatType.INNVILGET.equals(behandling.getBehandlingsresultat().getBehandlingResultatType())) {
            Optional<BeregningsresultatFP> beregningsresultatFP = beregningsresultatFPRepository.hentBeregningsresultatFP(behandling);

            LocalDate startdatoFP = finnForeldrepengerStartdato(beregningsresultatFP, behandling.getId());
            LocalDate vedtaksdato = behandlingVedtakRepository.hentBehandlingvedtakForBehandlingId(behandling.getId())
                .map(BehandlingVedtak::getVedtaksdato)
                .orElseThrow(() -> new IllegalStateException(String.format("Finner ikke vedtaksdato for behandling %d", behandling.getId())));
            LocalDate sisteYtelseFørVedtaksdato = finnSisteUtbetalingsdatoArenaFørVedtaksdato(arenaYtelser, vedtaksdato);
            LocalDate ytelseEtterVedtaksdato = finnFørsteUtbetalingsdatoArenaEtterVedtaksdato(arenaYtelser, vedtaksdato);
            LocalDateInterval ytelseInterval = new LocalDateInterval(sisteYtelseFørVedtaksdato, ytelseEtterVedtaksdato);

            // Ingen ARENA ytelse før vedtaksdato
            if (sisteYtelseFørVedtaksdato == null) {
                return false;
            } else if (ytelseEtterVedtaksdato == null) {
                // sjekk om Arena ytelse interval er før vedtaksdatoen og foreldrepenger interval overlapper Arena ytelse
                LocalDate sluttdatoFP = finnForeldrepengerSluttdato(beregningsresultatFP, behandling.getId());
                return sjekkOverlappendeArenaYtelserFørVedtaksdato(arenaYtelser, startdatoFP, sluttdatoFP);
            } else if (startdatoFP.isBefore(sisteYtelseFørVedtaksdato) ||
                ytelseInterval.encloses(startdatoFP) && vedtaksdato.isAfter(ytelseEtterVedtaksdato.minusDays(HALV_MELDEKORT_PERIODE))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LocalDate finnFørsteUttaksdato(List<BeregningsresultatPeriode> beregningsresultatPeriodeList) {
        return beregningsresultatPeriodeList.stream()
            .sorted(Comparator.comparing(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom))
            .filter(brp -> brp.getBeregningsresultatAndelList().stream().anyMatch(a -> a.getDagsats() > 0))
            .findFirst()
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom).orElse(null);
    }

    private List<Ytelse> finnArenaYtelser(Behandling behandling, String aktørId) {
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseTjeneste.hentAggregat(behandling);
        Optional<AktørYtelse> aktørYtelse = inntektArbeidYtelseGrunnlag.getAktørYtelseFørStp(new AktørId(aktørId));
        return aktørYtelse.map(y -> y.getYtelser().stream()).orElse(Stream.empty())
            .filter(ytelse -> ytelse.getKilde().equals(Fagsystem.ARENA))
            .collect(Collectors.toList());
    }

    private LocalDate finnForeldrepengerStartdato(Optional<BeregningsresultatFP> beregningsresultatFP, Long behandlingId) {
        return beregningsresultatFP.map(BeregningsresultatFP::getBeregningsresultatPerioder).orElse(Collections.emptyList())
            .stream()
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeFom)
            .min(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException(String.format("Finner ikke startdato for behandling %d", behandlingId)));
    }

    private LocalDate finnForeldrepengerSluttdato(Optional<BeregningsresultatFP> beregningsresultatFP, Long behandlingId) {
        return beregningsresultatFP.map(BeregningsresultatFP::getBeregningsresultatPerioder).orElse(Collections.emptyList())
            .stream()
            .map(BeregningsresultatPeriode::getBeregningsresultatPeriodeTom)
            .max(Comparator.comparing(Function.identity()))
            .orElseThrow(() -> new IllegalStateException(String.format("Finner ikke startdato for behandling %d", behandlingId)));
    }

    private boolean sjekkOverlappendeArenaYtelserFørVedtaksdato(Collection<Ytelse> ytelseCollection, LocalDate startdatoFP, LocalDate sluttdatoFP) {
        ÅpenDatoIntervallEntitet datoIntervall = ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(startdatoFP, sluttdatoFP);
        return ytelseCollection.stream()
            .anyMatch(ytelse -> datoIntervall.overlapper(ytelse.getPeriode()));
    }

    private LocalDate finnSisteUtbetalingsdatoArenaFørVedtaksdato(Collection<Ytelse> ytelseCollection, LocalDate vedtaksdato) {
        return ytelseCollection.stream()
            .map(ytelse -> ytelse.getPeriode().getTomDato())
            .filter(periode -> periode.isBefore(vedtaksdato))
            .max(Comparator.comparing(Function.identity()))
            .orElse(null);
    }

    private LocalDate finnFørsteUtbetalingsdatoArenaEtterVedtaksdato(Collection<Ytelse> ytelseCollection, LocalDate vedtaksdato) {
        return ytelseCollection.stream()
            .map(ytelse -> ytelse.getPeriode().getFomDato())
            .filter(periode -> periode.isAfter(vedtaksdato))
            .min(Comparator.comparing(Function.identity()))
            .orElse(null);
    }

}
