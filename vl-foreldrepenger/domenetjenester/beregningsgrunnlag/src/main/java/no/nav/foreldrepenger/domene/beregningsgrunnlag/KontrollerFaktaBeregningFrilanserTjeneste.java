package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;

public interface KontrollerFaktaBeregningFrilanserTjeneste {

    boolean erNyoppstartetFrilanser(Behandling behandling);

    Set<Arbeidsgiver> brukerErArbeidstakerOgFrilanserISammeOrganisasjon(Behandling behandling);

    boolean erBrukerArbeidstakerOgFrilanserISammeOrganisasjon(Behandling behandling);

    boolean harOverstyrtFrilans(Behandling behandling);
}
