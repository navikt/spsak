package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.OpptjeningsUtils.lagOpptjeningsnøkkel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Fagsystem;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningAktivitetPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningInntektPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Opptjeningsnøkkel;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningsperiodeForSaksbehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Ytelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetKlassifisering;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.kodeverk.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningInntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningsperioderTjeneste;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class OpptjeningInntektArbeidYtelseTjenesteImpl implements OpptjeningInntektArbeidYtelseTjeneste {

    private static final Set<OpptjeningAktivitetKlassifisering> GODKJENT_KLASSIFISERING = Collections
        .unmodifiableSet(new TreeSet<>(Arrays.asList(
            OpptjeningAktivitetKlassifisering.BEKREFTET_GODKJENT,
            OpptjeningAktivitetKlassifisering.ANTATT_GODKJENT)));

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private OpptjeningRepository opptjeningRepository;
    private OpptjeningsperioderTjeneste opptjeningsperioderTjeneste;

    OpptjeningInntektArbeidYtelseTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public OpptjeningInntektArbeidYtelseTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                     ResultatRepositoryProvider provider, OpptjeningsperioderTjeneste opptjeningsperioderTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.opptjeningRepository = provider.getOpptjeningRepository();
        this.opptjeningsperioderTjeneste = opptjeningsperioderTjeneste;
    }

    @Override
    public Opptjening hentOpptjening(Behandling behandling) {
        Optional<Opptjening> optional = opptjeningRepository.finnOpptjening(behandling.getBehandlingsresultat());
        return optional
            .orElseThrow(() -> new IllegalStateException("Utvikler-feil: Mangler Opptjening for Behandling: " + behandling.getId()));
    }

    @Override
    public Optional<Opptjening> hentOpptjeningHvisEksisterer(Behandling behandling) {
        return opptjeningRepository.finnOpptjening(behandling.getBehandlingsresultat());
    }

    @Override
    public List<OpptjeningInntektPeriode> hentRelevanteOpptjeningInntekterForVilkårVurdering(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);

        if (grunnlagOpt.isPresent()) {
            InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = grunnlagOpt.get();
            Collection<AktørInntekt> aktørInntekt = inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp();

            return aktørInntekt.stream()
                .filter(ai -> ai.getAktørId().equals(behandling.getAktørId()))
                .map(AktørInntekt::getInntektPensjonsgivende)
                .flatMap(Collection::stream)
                .filter(inntekt -> inntekt.getArbeidsgiver() != null)
                .map(this::mapInntektPeriode)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<Ytelse> hentSisteInfotrygdYtelseFørSkjæringstidspunktForOpptjening(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!grunnlagOpt.isPresent()) {
            return Optional.empty();
        }
        Optional<Opptjening> opptjeningOpt = opptjeningRepository.finnOpptjening(behandling.getBehandlingsresultat());
        if (!opptjeningOpt.isPresent()) {
            return Optional.empty();
        }

        LocalDate skjæringstidspunktForOpptjening = opptjeningOpt.get().getTom();
        Optional<AktørYtelse> ytelserOpt = grunnlagOpt.get().getAktørYtelseFørStpSaksBehFørReg(behandling.getAktørId());
        if (ytelserOpt.isPresent()) {
            return ytelserOpt.get().getYtelser().stream()
                .filter(y -> !y.getPeriode().getFomDato().isAfter(skjæringstidspunktForOpptjening))
                .filter(y -> Fagsystem.INFOTRYGD.equals(y.getKilde()))
                .max(Comparator.comparing(y -> y.getPeriode().getFomDato()));
        }
        return Optional.empty();
    }

    @Override
    public List<Ytelse> hentSammenhengendeInfotrygdYtelserFørSkjæringstidspunktForOppjening(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = inntektArbeidYtelseTjeneste.hentAggregatHvisEksisterer(behandling);
        if (!grunnlagOpt.isPresent()) {
            return Collections.emptyList();
        }
        Optional<Opptjening> opptjeningOpt = opptjeningRepository.finnOpptjening(behandling.getBehandlingsresultat());
        if (!opptjeningOpt.isPresent()) {
            return Collections.emptyList();
        }

        LocalDate skjæringstidspunktForOpptjening = opptjeningOpt.get().getTom();
        Optional<AktørYtelse> aktørYtelseFørStpOpt = grunnlagOpt.get().getAktørYtelseFørStpSaksBehFørReg(behandling.getAktørId());
        if (!aktørYtelseFørStpOpt.isPresent()) {
            return Collections.emptyList();
        }
        return finnSammenhengendeInfotrygdYtelser(aktørYtelseFørStpOpt.get().getYtelser(), skjæringstidspunktForOpptjening);
    }

    private List<Ytelse> finnSammenhengendeInfotrygdYtelser(Collection<Ytelse> ytelser, LocalDate skjæringstidspunktForOpptjening) {
        List<Ytelse> ytelserFørSkjæringstidspunkt = ytelser.stream()
            .filter(y -> !y.getPeriode().getFomDato().isAfter(skjæringstidspunktForOpptjening))
            .filter(y -> Fagsystem.INFOTRYGD.equals(y.getKilde()))
            .sorted(Comparator.comparing((Ytelse y) -> y.getPeriode().getFomDato()).reversed())
            .collect(Collectors.toList());

        if (ytelserFørSkjæringstidspunkt.isEmpty()) {
            return ytelserFørSkjæringstidspunkt;
        }

        List<Ytelse> sammenhengende = new ArrayList<>(ytelserFørSkjæringstidspunkt.subList(0, 1));

        for (int i = 0; i < ytelserFørSkjæringstidspunkt.size() - 1; i++) {
            if (erPeriodeneSammenhengende(ytelserFørSkjæringstidspunkt.get(i).getPeriode(), ytelserFørSkjæringstidspunkt.get(i + 1).getPeriode())) {
                Ytelse nesteSammenhengendeYtelse = ytelserFørSkjæringstidspunkt.get(i + 1);
                sammenhengende.add(nesteSammenhengendeYtelse);
            } else {
                return sammenhengende;
            }
        }
        return sammenhengende;

    }

    private boolean erPeriodeneSammenhengende(DatoIntervallEntitet periode1, DatoIntervallEntitet periode2) {
        return !periode1.getFomDato().isAfter(periode2.getTomDato().plusDays(1));
    }

    @Override
    public List<OpptjeningAktivitet> hentGodkjentAktivitetTyper(Behandling behandling, LocalDate dato) {
        Optional<Opptjening> optional = opptjeningRepository.finnOpptjening(behandling.getBehandlingsresultat());

        if (!optional.isPresent()) {
            return Collections.emptyList();
        } else {
            Opptjening opptjening = optional.get();

            return opptjening.getOpptjeningAktivitet().stream()
                .filter(oa -> oa.getFom().isBefore(dato.plusDays(1)))
                .filter(oa -> !oa.getAktivitetType().equals(OpptjeningAktivitetType.UTENLANDSK_ARBEIDSFORHOLD))
                .filter(oa -> GODKJENT_KLASSIFISERING.contains(oa.getKlassifisering()))
                .collect(Collectors.toList());
        }
    }

    @Override
    public List<OpptjeningAktivitetPeriode> hentRelevanteOpptjeningAktiveterForVilkårVurdering(Behandling behandling) {
        final List<OpptjeningsperiodeForSaksbehandling> perioder = opptjeningsperioderTjeneste.hentRelevanteOpptjeningAktiveterForVilkårVurdering(behandling);

        return perioder.stream().map(this::mapTilPerioder).collect(Collectors.toList());
    }

    private OpptjeningAktivitetPeriode mapTilPerioder(OpptjeningsperiodeForSaksbehandling periode) {
        final OpptjeningAktivitetPeriode.Builder builder = OpptjeningAktivitetPeriode.Builder.ny();
        builder.medPeriode(periode.getPeriode())
            .medOpptjeningAktivitetType(periode.getOpptjeningAktivitetType())
            .medOrgnr(periode.getOrgnr())
            .medOpptjeningsnøkkel(periode.getOpptjeningsnøkkel())
            .medStillingsandel(periode.getStillingsprosent())
            .medVurderingsStatus(periode.getVurderingsStatus())
            .medBegrunnelse(periode.getBegrunnelse());
        return builder.build();
    }

    private List<OpptjeningInntektPeriode> mapInntektPeriode(Inntekt inntektTmp) {
        final Arbeidsgiver arbeidsgiver = inntektTmp.getArbeidsgiver();
        return inntektTmp.getInntektspost().stream().map(inntektspost -> {
            Opptjeningsnøkkel opptjeningsnøkkel = lagOpptjeningsnøkkel(arbeidsgiver, null);
            return new OpptjeningInntektPeriode(inntektspost, opptjeningsnøkkel);
        }).collect(Collectors.toList());
    }
}
