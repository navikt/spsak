package no.nav.foreldrepenger.domene.arbeidsforhold;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OpptjeningsperiodeForSaksbehandling;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;

public interface OpptjeningsperioderTjeneste {

    /** Hent alle opptjeningsaktiv og utleder om noen perioder trenger vurdering av saksbehandler */
    List<OpptjeningsperiodeForSaksbehandling> hentRelevanteOpptjeningAktiveterForSaksbehandling(Behandling behandling);

    List<OpptjeningsperiodeForSaksbehandling> hentRelevanteOpptjeningAktiveterForVilkårVurdering(Behandling behandling);

    /** Hent alle opptjeningsaktiv fra et gitt grunnlag og utleder om noen perioder trenger vurdering av saksbehandler */
    List<OpptjeningsperiodeForSaksbehandling> hentRelevanteOpptjeningAktiveterForSaksbehandling(Behandling behandling, Long inntektArbeidYtelseGrunnlagId);

    Optional<Opptjening> hentOpptjeningHvisFinnes(Behandling behandling);
}
