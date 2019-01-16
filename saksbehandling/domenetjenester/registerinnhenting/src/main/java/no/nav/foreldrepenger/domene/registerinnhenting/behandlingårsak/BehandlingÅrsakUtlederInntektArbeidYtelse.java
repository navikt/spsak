package no.nav.foreldrepenger.domene.registerinnhenting.behandlingårsak;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørYtelseEndring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.domene.registerinnhenting.startpunkt.GrunnlagRef;

@ApplicationScoped
@GrunnlagRef("InntektArbeidYtelseGrunnlag")
class BehandlingÅrsakUtlederInntektArbeidYtelse implements BehandlingÅrsakUtleder {
    private static final Logger log = LoggerFactory.getLogger(BehandlingÅrsakUtlederInntektArbeidYtelse.class);

    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    private BehandlingÅrsakUtlederInntektsmelding behandlingÅrsakUtlederInntektsmelding;
    private BehandlingÅrsakUtlederAktørArbeid behandlingÅrsakUtlederAktørArbeid;
    private BehandlingÅrsakUtlederAktørInntekt behandlingÅrsakUtlederAktørInntekt;
    private BehandlingÅrsakUtlederAktørYtelse behandlingÅrsakUtlederAktørYtelse;


    @Inject
    public BehandlingÅrsakUtlederInntektArbeidYtelse(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                     BehandlingÅrsakUtlederInntektsmelding behandlingÅrsakUtlederInntektsmelding, BehandlingÅrsakUtlederAktørArbeid behandlingÅrsakUtlederAktørArbeid,
                                                     BehandlingÅrsakUtlederAktørInntekt behandlingÅrsakUtlederAktørInntekt, BehandlingÅrsakUtlederAktørYtelse behandlingÅrsakUtlederAktørYtelse) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.behandlingÅrsakUtlederInntektsmelding = behandlingÅrsakUtlederInntektsmelding;
        this.behandlingÅrsakUtlederAktørArbeid = behandlingÅrsakUtlederAktørArbeid;
        this.behandlingÅrsakUtlederAktørInntekt = behandlingÅrsakUtlederAktørInntekt;
        this.behandlingÅrsakUtlederAktørYtelse = behandlingÅrsakUtlederAktørYtelse;
    }

    @Override
    public Set<BehandlingÅrsakType> utledBehandlingÅrsaker(Behandling behandling, Long grunnlagId1, Long grunnlagId2) {
        String ider = "grunnlagid1: " + grunnlagId1 + ", grunnlagid2: " + grunnlagId2;
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag1 = inntektArbeidYtelseTjeneste.hentInntektArbeidYtelsePåId(grunnlagId1);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag2 = inntektArbeidYtelseTjeneste.hentInntektArbeidYtelsePåId(grunnlagId2);

        return hentAlleBehandlingÅrsakTyperForInntektArbeidYtelse(inntektArbeidYtelseGrunnlag1, inntektArbeidYtelseGrunnlag2, ider);
    }

    private Set<BehandlingÅrsakType> hentAlleBehandlingÅrsakTyperForInntektArbeidYtelse(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag1, InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag2, String ider) {
        Set<BehandlingÅrsakType> behandlingÅrsakTyper = new HashSet<>();

        boolean erAktørArbeidEndret = inntektArbeidYtelseTjeneste.erEndretAktørArbeid(inntektArbeidYtelseGrunnlag1, inntektArbeidYtelseGrunnlag2);
        boolean erAktørInntektEndret = inntektArbeidYtelseTjeneste.erEndretAktørInntekt(inntektArbeidYtelseGrunnlag1, inntektArbeidYtelseGrunnlag2);
        AktørYtelseEndring aktørYtelseEndring = inntektArbeidYtelseTjeneste.endringPåAktørYtelse(inntektArbeidYtelseGrunnlag1, inntektArbeidYtelseGrunnlag2);
        boolean erInntektsmeldingEndret = inntektArbeidYtelseTjeneste.erEndretInntektsmelding(inntektArbeidYtelseGrunnlag1, inntektArbeidYtelseGrunnlag2);
        if (erAktørArbeidEndret) {
            BehandlingÅrsakType behandlingÅrsakTypeAktørArbeid = behandlingÅrsakUtlederAktørArbeid.utledBehandlingÅrsak();
            log.info("Setter behandlingårsak til {}, har endring i aktør arbeid, {}", behandlingÅrsakTypeAktørArbeid, ider); //$NON-NLS-1
            behandlingÅrsakTyper.add(behandlingÅrsakTypeAktørArbeid);
        }
        if (aktørYtelseEndring.erEksterneRegistreEndret()) {
            BehandlingÅrsakType behandlingÅrsakTypeAktørYtelse = behandlingÅrsakUtlederAktørYtelse.utledBehandlingÅrsak();
            log.info("Setter behandlingårsak til {}, har endring i aktør ytelse, {}", behandlingÅrsakTypeAktørYtelse, ider); //$NON-NLS-1
            behandlingÅrsakTyper.add(behandlingÅrsakTypeAktørYtelse);
        }
        if (erAktørInntektEndret) {
            BehandlingÅrsakType behandlingÅrsakTypeAktørInntekt = behandlingÅrsakUtlederAktørInntekt.utledBehandlingÅrsak();
            log.info("Setter behandlingårsak til {}, har endring i aktør inntekt, {}", behandlingÅrsakTypeAktørInntekt, ider); //$NON-NLS-1
            behandlingÅrsakTyper.add(behandlingÅrsakTypeAktørInntekt);
        }
        if (erInntektsmeldingEndret) {
            BehandlingÅrsakType behandlingÅrsakTypeInntektsmelding = behandlingÅrsakUtlederInntektsmelding.utledBehandlingÅrsak();
            log.info("Setter behandlingårsak til {}, har endring i inntektsmelding, {}", behandlingÅrsakTypeInntektsmelding, ider); //$NON-NLS-1
            behandlingÅrsakTyper.add(behandlingÅrsakTypeInntektsmelding);
        }

        return behandlingÅrsakTyper;
    }
}
