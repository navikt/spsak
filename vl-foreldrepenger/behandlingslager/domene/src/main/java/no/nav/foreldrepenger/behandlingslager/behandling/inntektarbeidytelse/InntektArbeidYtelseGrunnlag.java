package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørYtelse;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.InntektsmeldingSomIkkeKommer;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.domene.typer.AktørId;

public interface InntektArbeidYtelseGrunnlag {

    /**
     * Returnerer en overstyrt versjon av aggregat. Hvis saksbehandler har løst et aksjonspunkt i forbindele med
     * opptjening vil det finnes et overstyrt aggregat, gjelder for FØR første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    Optional<InntektArbeidYtelseAggregat> getSaksbehandletVersjon();

    /**
     * Returnerer en bekrefet versjon av aggregat. Denne versjon inneholder opplysninger hentet fra registere,
     * gjelder for FØR første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    Optional<InntektArbeidYtelseAggregat> getOpplysningerFørSkjæringstidspunkt();

    /**
     * Returnerer en bekrefet versjon av aggregat. Denne versjon inneholder opplysninger hentet fra registere,
     * gjelder for ETTER første dag i permisjonsuttaket (skjæringstidspunktet)
     */
    Optional<InntektArbeidYtelseAggregat> getOpplysningerEtterSkjæringstidspunkt();

    /**
     * Returnere TRUE hvis det finnes en overstyr versjon, sjekker både FØR og ETTER
     */
    boolean harBlittSaksbehandlet();

    /**
     * Returnerer inntekter per aktør FØR skjæringstidspunkt (Stp). Merk at dette kan returnere for flere aktører (eks. søker + annen part)
     */
    Collection<AktørInntekt> getAktørInntektForFørStp();

    /**
     * Returnere inntekter FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     *
     */
    Optional<AktørInntekt> getAktørInntektForFørStp(AktørId aktørId);

    /**
     * Returnere inntekter FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     *
     */
    Optional<AktørInntekt> getAktørInntektForEtterStp(AktørId aktørId);

    /**
     * Returnerer arbeid per aktør FØR skjæringstidspunkt (Stp). Merk at dette kan returnere for flere aktører (eks. søker + annen part)
     */
    Collection<AktørArbeid> getAktørArbeidFørStp();

    /**
     * Returnere arbeid FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     */
    Optional<AktørArbeid> getAktørArbeidFørStp(AktørId aktørId);

    /**
     * Returnere arbeid ETTER skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     */
    Optional<AktørArbeid> getAktørArbeidEtterStp(AktørId aktørId);

    /**
     * Returnerer ytelser per aktør FØR skjæringstidspunkt (Stp) Merk at dette kan returnere for flere aktører (eks. søker + annen part)
     */
    Collection<AktørYtelse> getAktørYtelseFørStp();

    /**
     * Returnere ytelser FØR skjæringstidspunkt (Stp) for angitt aktør id (hvis finnes).
     */
    Optional<AktørYtelse> getAktørYtelseFørStp(AktørId aktørId);

    /**
     * Samme som getAktørYtelseFørStp(AktørId aktørId), men returnerer saksbehandlet versjon foran registerversjon om tilgjengelig
     */
    Optional<AktørYtelse> getAktørYtelseFørStpSaksBehFørReg(AktørId aktørId);

    /**
     * Returnerer alle yrkesaktivteter for en aktør FØR skjæringstidspunkt, kan velge om det skal se i bekrefet eller overstyrt
     */
    Collection<Yrkesaktivitet> hentAlleYrkesaktiviteterFørStpFor(AktørId aktørId, boolean overstyrt);

    /**
     * Returnerer aggregat som holder alle inntektsmeldingene som benyttes i behandlingen.
     */
    Optional<InntektsmeldingAggregat> getInntektsmeldinger();

    Optional<AktørArbeid> getBekreftetAnnenOpptjening();

    /**
     * Returnerer oppgitt opptjening hvis det finnes. (Inneholder opplysninger søker opplyser om i søknaden)
     */
    Optional<OppgittOpptjening> getOppgittOpptjening();

    List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommer();

    List<InntektsmeldingSomIkkeKommer> getInntektsmeldingerSomIkkeKommerFor(Virksomhet virksomhet);

}
