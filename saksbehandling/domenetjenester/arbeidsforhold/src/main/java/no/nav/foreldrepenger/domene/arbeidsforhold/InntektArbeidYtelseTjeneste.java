package no.nav.foreldrepenger.domene.arbeidsforhold;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørYtelseEndring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.ArbeidsforholdRef;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.arbeidsforhold.aksjonspunkt.BekreftOpptjeningPeriodeDto;
import no.nav.foreldrepenger.domene.typer.JournalpostId;

public interface InntektArbeidYtelseTjeneste {
    /**
     * @param behandling
     * @return henter aggregat, kaster feil hvis det ikke finnes.
     */
    InntektArbeidYtelseGrunnlag hentAggregat(Behandling behandling);

    /**
     * @param behandling
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Behandling behandling);

    /**
     * @param aggregatId
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentAggregatPåIdHvisEksisterer(Long aggregatId);

    /**
     * @param behandlingId
     * @return henter optional aggregat
     */
    Optional<InntektArbeidYtelseGrunnlag> hentAggregatHvisEksisterer(Long behandlingId);

    /**
     * @param behandling
     * @return Register inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til registeropplysning)
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(Behandling behandling);

    /**
     * @param behandling
     * @return Saksbehanldet inntekt og arbeid før skjæringstidspunktet (Opprett for å endre eller legge til saksbehanldet)
     */
    InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(Behandling behandling);

    /**
     * @param behandling
     * @param inntektArbeidYtelseAggregatBuilder lagrer ned aggregat (builder bestemmer hvilke del av treet som blir lagret)
     */
    void lagre(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    void lagre(Behandling behandling, Inntektsmelding inntektsmelding);

    boolean erEndret(Behandling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder);

    Map<String, Set<String>> utledManglendeInntektsmeldingerFraArkiv(Behandling behandling);

    Map<String, Set<String>> utledManglendeInntektsmeldingerFraGrunnlag(Behandling behandling);

    Collection<Yrkesaktivitet> hentYrkesaktiviteterForSøker(Behandling behandling, boolean overstyrt);

    void bekreftPeriodeAksjonspunkt(Behandling behandling, List<BekreftOpptjeningPeriodeDto> adapter);

    InntektArbeidYtelseGrunnlag hentFørsteVersjon(Behandling behandling);

    boolean erEndret(Behandling origBehandling, Behandling nyBehandling);

    boolean erEndretInntektsmelding(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    boolean erEndretAktørArbeid(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    boolean erEndretAktørInntekt(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    AktørYtelseEndring endringPåAktørYtelse(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå);

    EndringsresultatSnapshot finnAktivAggregatId(Behandling behandling);

    InntektArbeidYtelseGrunnlag hentInntektArbeidYtelsePåId(Long aggregatId);

    DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

    ArbeidsforholdRef finnReferanseFor(Behandling behandling, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi);

    boolean søkerHarOppgittEgenNæring(Behandling behandling);

    Map<JournalpostId, Set<Long>> hentBehandlingerPerInntektsmeldingFor(Long fagsakId);

    Optional<Inntektsmelding> hentInnteksmeldingFor(JournalpostId journalpostId);

    List<Inntektsmelding> hentAlleInntektsmeldinger(Behandling behandling);

    List<Inntektsmelding> hentAlleInntektsmeldingerMottattEtterGjeldendeVedtak(Behandling behandling);

    List<InntektsmeldingSomIkkeKommer> hentAlleInntektsmeldingerSomIkkeKommer(Behandling behandling);

    List<Inntektsmelding> hentAlleInntektsmeldingerForFagsak(Long behandlingId);

    Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmelding(Long behandlingId);

    Optional<ArbeidsforholdInformasjon> hentInformasjon(Behandling behandling);

    Optional<ArbeidsforholdInformasjon> hentInformasjon(Long inntektArbeidYtelseGrunnlagId);
}
