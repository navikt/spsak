package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.app;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.totrinn.Totrinnsvurdering;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.totrinn.dto.TotrinnsArbeidsforholdDto;

@ApplicationScoped
public class TotrinnArbeidsforholdDtoTjeneste {
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;

    protected TotrinnArbeidsforholdDtoTjeneste() {
        // for CDI proxy
    }


    @Inject
    public TotrinnArbeidsforholdDtoTjeneste(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
    }

    List<TotrinnsArbeidsforholdDto> hentArbeidsforhold(Totrinnsvurdering aksjonspunkt, Behandling behandling, Optional<Long> inntektArbeidYtelseGrunnlagId) {
        if (aksjonspunkt.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD)) {
            Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjonOptional;

            if (inntektArbeidYtelseGrunnlagId.isPresent()) {
                arbeidsforholdInformasjonOptional = inntektArbeidYtelseTjeneste.hentInformasjon(inntektArbeidYtelseGrunnlagId.get());
            } else {
                arbeidsforholdInformasjonOptional = inntektArbeidYtelseTjeneste.hentInformasjon(behandling);
            }

            if (arbeidsforholdInformasjonOptional.isPresent()) {
                ArbeidsforholdInformasjon arbeidsforholdInformasjon = arbeidsforholdInformasjonOptional.get();
                List<ArbeidsforholdOverstyringEntitet> overstyringer = arbeidsforholdInformasjon.getOverstyringer();
                return overstyringer.stream()
                    .map(b -> new TotrinnsArbeidsforholdDto(b.getArbeidsgiver().getVirksomhet().getNavn(), b.getArbeidsgiver().getVirksomhet().getOrgnr(), b.getArbeidsforholdRef().getReferanse(), b.getHandling()))
                    .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
