package no.nav.foreldrepenger.domene.arbeidsforhold;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.VurderingsStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;

class OpptjeningAktivitetVurderingVilkår implements OpptjeningAktivitetVurdering {

    private AksjonspunktutlederForVurderOpptjening vurderOpptjening;

    OpptjeningAktivitetVurderingVilkår(AksjonspunktutlederForVurderOpptjening vurderOpptjening) {
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
            return vurderAnnenOpptjening(overstyrtAktivitet);
        } else if (OpptjeningAktivitetType.NÆRING.equals(type)) {
            return vurderNæring(behandling, overstyrtAktivitet);
        } else if (OpptjeningAktivitetType.ARBEID.equals(type)) {
            return vurderArbeid(registerAktivitet, overstyrtAktivitet, behandling);
        }
        return VurderingsStatus.TIL_VURDERING;
    }

    /**
     * @param registerAktivitet  aktiviteten
     * @param overstyrtAktivitet aktiviteten
     * @param behandling
     * @return vurderingsstatus
     */
    private VurderingsStatus vurderArbeid(Yrkesaktivitet registerAktivitet, Yrkesaktivitet overstyrtAktivitet, Behandling behandling) {
        if (vurderOpptjening.girAksjonspunktForArbeidsforhold(behandling, registerAktivitet)) {
            if (overstyrtAktivitet != null) {
                return VurderingsStatus.FERDIG_VURDERT_GODKJENT;
            }
            return VurderingsStatus.FERDIG_VURDERT_UNDERKJENT;
        }
        return VurderingsStatus.TIL_VURDERING;
    }

    /**
     * @param overstyrtAktivitet aktiviteten
     * @return vurderingsstatus
     */
    private VurderingsStatus vurderAnnenOpptjening(Yrkesaktivitet overstyrtAktivitet) {
        if (overstyrtAktivitet != null) {
            return VurderingsStatus.FERDIG_VURDERT_GODKJENT;
        }
        return VurderingsStatus.FERDIG_VURDERT_UNDERKJENT;
    }

    /**
     * @param behandling         behandlingen
     * @param overstyrtAktivitet aktiviteten
     * @return vurderingsstatus
     */
    private VurderingsStatus vurderNæring(Behandling behandling, Yrkesaktivitet overstyrtAktivitet) {
        if (vurderOpptjening.girAksjonspunktForOppgittNæring(behandling)) {
            if (overstyrtAktivitet != null) {
                return VurderingsStatus.FERDIG_VURDERT_GODKJENT;
            }
            return VurderingsStatus.FERDIG_VURDERT_UNDERKJENT;
        }
        return VurderingsStatus.TIL_VURDERING;
    }
}
