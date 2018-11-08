package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface InntektArbeidYtelseRepository extends BehandlingslagerRepository {

    InntektArbeidYtelseGrunnlag hentAggregat(Behandling behandling, LocalDate skjæringstidspunkt);

    Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Behandling behandling, LocalDate skjæringstidspunkt);

    Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Long behandlingId, LocalDate skjæringstidspunkt);

    boolean erEndret(Long grunnlagId, Behandling behandling);

    DiffResult diffResultat(InntektArbeidYtelseGrunnlagEntitet grunnlag1, InntektArbeidYtelseGrunnlagEntitet grunnlag2, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

    Optional<Inntektsmelding> hentInntektsMeldingFor(MottattDokument mottattDokument);

    /**
     * @param behandling      (Behandling)
     * @param versjonType     (REGISTER, SAKSBEHANDLET)
     * @return InntektArbeidYtelseAggregatBuilder
     * <p>
     * NB! bør benytte via InntektArbeidYtelseTjeneste og ikke direkte
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderFor(Behandling behandling, VersjonType versjonType);

    ArbeidsforholdInformasjonBuilder opprettInformasjonBuilderFor(Behandling behandling);

    void lagre(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    void lagre(Behandling behandling, Inntektsmelding inntektsmelding);

    void lagre(Behandling behandling, OppgittOpptjeningBuilder opptjeningBuilder);

    void lagre(Behandling behandling, ArbeidsforholdInformasjonBuilder informasjon);

    boolean erEndring(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    boolean erEndringPåInntektsmelding(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    boolean erEndringPåAktørArbeid(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    boolean erEndringPåAktørInntekt(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    AktørYtelseEndring endringPåAktørYtelse(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    boolean erEndring(Behandling behandling, Behandling nyBehandling, LocalDate skjæringstidspunkt);

    boolean erEndring(InntektArbeidYtelseGrunnlag aggregat, InntektArbeidYtelseGrunnlag nyttAggregat);

    boolean harArbeidsforholdMedArbeidstyperSomAngitt(Behandling behandling, Set<ArbeidType> angitteArbeidtyper, LocalDate skjæringstidspunkt);

    void tilbakestillOverstyring(Behandling behandling);

    void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling);

    InntektArbeidYtelseGrunnlag hentFørsteVersjon(Behandling behandling, LocalDate skjæringstidspunkt);

    Optional<Long> hentIdPåAktivInntektArbeidYtelse(Behandling behandling);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelsePåId(Long aggregatId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Behandling behandling);

    Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmelding(Long behandlingId);

    Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Long inntektArbeidYtelseGrunnlagId);
}
