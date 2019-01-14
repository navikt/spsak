package no.nav.foreldrepenger.domene.arbeidsforhold;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;

public interface OpptjeningAktivitetVurdering {

    VurderingsStatus vurderStatus(OpptjeningAktivitetType type,
                                  Behandling behandling,
                                  Yrkesaktivitet overstyrtAktivitet,
                                  boolean harVærtSaksbehandlet);

    VurderingsStatus vurderStatus(OpptjeningAktivitetType type,
                                  Behandling behandling,
                                  Yrkesaktivitet registerAktivitet,
                                  Yrkesaktivitet overstyrtAktivitet,
                                  boolean harVærtSaksbehandlet);
}
