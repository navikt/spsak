package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.domene.arbeidsforhold.OpptjeningAktivitetVurdering;

class OpptjeningAktivitetVurderingAksjonspunkt implements OpptjeningAktivitetVurdering {

    private AksjonspunktutlederForVurderOpptjening vurderOpptjening;

    OpptjeningAktivitetVurderingAksjonspunkt(AksjonspunktutlederForVurderOpptjening vurderOpptjening) {
        this.vurderOpptjening = vurderOpptjening;
    }

    @Override
    public VurderingsStatus vurderStatus(OpptjeningAktivitetType type,
                                         Behandling behandling,
                                         Yrkesaktivitet overstyrtAktivitet,
                                         boolean harVærtSaksbehandlet) {
        return vurderStatus(type, behandling, null, overstyrtAktivitet, harVærtSaksbehandlet);
    }

    @Override
    public VurderingsStatus vurderStatus(OpptjeningAktivitetType type,
                                         Behandling behandling,
                                         Yrkesaktivitet registerAktivitet,
                                         Yrkesaktivitet overstyrtAktivitet,
                                         boolean harVærtSaksbehandlet) {
        if (OpptjeningAktivitetType.ANNEN_OPPTJENING.contains(type)) {
            return vurderAnnenOpptjening(overstyrtAktivitet, harVærtSaksbehandlet);
        } else if (OpptjeningAktivitetType.NÆRING.equals(type)) {
            return vurderNæring(behandling, overstyrtAktivitet, harVærtSaksbehandlet);
        } else if (OpptjeningAktivitetType.ARBEID.equals(type)) {
            return vurderArbeid(registerAktivitet, overstyrtAktivitet, harVærtSaksbehandlet, behandling);
        }
        return VurderingsStatus.GODKJENT;
    }

    /**
     * @param registerAktivitet    aktiviteten
     * @param overstyrtAktivitet   aktiviteten
     * @param harVærtSaksbehandlet har saksbehandler tatt stilling til dette
     * @param behandling
     * @return vurderingsstatus
     */
    private VurderingsStatus vurderArbeid(Yrkesaktivitet registerAktivitet, Yrkesaktivitet overstyrtAktivitet, boolean harVærtSaksbehandlet, Behandling behandling) {
        if (vurderOpptjening.girAksjonspunktForArbeidsforhold(behandling, registerAktivitet)) {
            if (overstyrtAktivitet != null) {
                return VurderingsStatus.GODKJENT;
            }
            if (harVærtSaksbehandlet) {
                return VurderingsStatus.UNDERKJENT;
            }
            return VurderingsStatus.TIL_VURDERING;
        }
        return VurderingsStatus.GODKJENT;
    }

    /**
     * @param overstyrtAktivitet   aktiviteten
     * @param harVærtSaksbehandlet har saksbehandler tatt stilling til dette
     * @return vurderingsstatus
     */
    private VurderingsStatus vurderAnnenOpptjening(Yrkesaktivitet overstyrtAktivitet, boolean harVærtSaksbehandlet) {
        if (overstyrtAktivitet != null) {
            return VurderingsStatus.GODKJENT;
        }
        if (harVærtSaksbehandlet) {
            return VurderingsStatus.UNDERKJENT;
        }
        return VurderingsStatus.TIL_VURDERING;
    }

    /**
     * @param behandling           behandlingen
     * @param overstyrtAktivitet   aktiviteten
     * @param harVærtSaksbehandlet har saksbehandler tatt stilling til dette
     * @return vurderingsstatus
     */
    private VurderingsStatus vurderNæring(Behandling behandling, Yrkesaktivitet overstyrtAktivitet, boolean harVærtSaksbehandlet) {
        if (vurderOpptjening.girAksjonspunktForOppgittNæring(behandling)) {
            if (overstyrtAktivitet != null) {
                return VurderingsStatus.GODKJENT;
            }
            if (harVærtSaksbehandlet) {
                return VurderingsStatus.UNDERKJENT;
            }
            return VurderingsStatus.TIL_VURDERING;
        }
        return VurderingsStatus.GODKJENT;
    }
}
