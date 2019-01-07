package no.nav.foreldrepenger.domene.registerinnhenting.startpunkt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.AktørYtelseEndring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.domene.arbeidsforhold.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.domene.arbeidsforhold.VurderArbeidsforholdTjeneste;

@ApplicationScoped
@GrunnlagRef("InntektArbeidYtelseGrunnlag")
class StartpunktUtlederInntektArbeidYtelse implements StartpunktUtleder {

    private String klassenavn = this.getClass().getSimpleName();
    private InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste;
    private KodeverkTabellRepository kodeverkTabellRepository;
    private StartpunktUtlederInntektsmelding startpunktUtlederInntektsmelding;
    private StartpunktUtlederAktørArbeid startpunktUtlederAktørArbeid;
    private StartpunktUtlederAktørInntekt startpunktUtlederAktørInntekt;
    private StartpunktUtlederAktørYtelse startpunktUtlederAktørYtelse;
    private VurderArbeidsforholdTjeneste vurderArbeidsforholdTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private BehandlingRepository behandlingRepository;

    public StartpunktUtlederInntektArbeidYtelse() {
        // For CDI
    }

    @Inject
    StartpunktUtlederInntektArbeidYtelse(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste, // NOSONAR - ingen enkel måte å unngå mange parametere her
                                         KodeverkTabellRepository kodeverkTabellRepository,
                                         GrunnlagRepositoryProvider repositoryProvider,
                                         StartpunktUtlederInntektsmelding startpunktUtlederInntektsmelding, StartpunktUtlederAktørArbeid startpunktUtlederAktørArbeid,
                                         StartpunktUtlederAktørInntekt startpunktUtlederAktørInntekt, StartpunktUtlederAktørYtelse startpunktUtlederAktørYtelse,
                                         VurderArbeidsforholdTjeneste vurderArbeidsforholdTjeneste) {
        this.inntektArbeidYtelseTjeneste = inntektArbeidYtelseTjeneste;
        this.kodeverkTabellRepository = kodeverkTabellRepository;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.startpunktUtlederInntektsmelding = startpunktUtlederInntektsmelding;
        this.startpunktUtlederAktørArbeid = startpunktUtlederAktørArbeid;
        this.startpunktUtlederAktørInntekt = startpunktUtlederAktørInntekt;
        this.startpunktUtlederAktørYtelse = startpunktUtlederAktørYtelse;
        this.vurderArbeidsforholdTjeneste = vurderArbeidsforholdTjeneste;
    }

    @Override
    public StartpunktType utledStartpunkt(Behandling behandling, Long grunnlagId1, Long grunnlagId2) {
        return hentAlleStartpunktForInntektArbeidYtelse(behandling, grunnlagId1, grunnlagId2).stream()
            .map(it -> kodeverkTabellRepository.finnStartpunktType(it.getKode())) // Må oppfriskes
            .min(Comparator.comparing(StartpunktType::getRangering))
            .orElse(StartpunktType.UDEFINERT);
    }

    private List<StartpunktType> hentAlleStartpunktForInntektArbeidYtelse(Behandling behandling,
                                                                          Long grunnlagId1, Long grunnlagId2) {
        List<StartpunktType> startpunkter = new ArrayList<>();
        InntektArbeidYtelseGrunnlag grunnlag1 = inntektArbeidYtelseTjeneste.hentInntektArbeidYtelsePåId(grunnlagId1);
        InntektArbeidYtelseGrunnlag grunnlag2 = inntektArbeidYtelseTjeneste.hentInntektArbeidYtelsePåId(grunnlagId2);

        boolean erPåkrevdManuelleAvklaringer = !vurderArbeidsforholdTjeneste.vurder(behandling).isEmpty();
        boolean erAktørArbeidEndret = inntektArbeidYtelseTjeneste.erEndretAktørArbeid(grunnlag1, grunnlag2);
        boolean erAktørInntektEndret = inntektArbeidYtelseTjeneste.erEndretAktørInntekt(grunnlag1, grunnlag2);
        boolean erInntektsmeldingEndret = inntektArbeidYtelseTjeneste.erEndretInntektsmelding(grunnlag1, grunnlag2);
        AktørYtelseEndring aktørYtelseEndring = inntektArbeidYtelseTjeneste.endringPåAktørYtelse(grunnlag1, grunnlag2);

        if (erPåkrevdManuelleAvklaringer) {
            leggTilStartpunkt(startpunkter, grunnlagId1, grunnlagId2, StartpunktType.KONTROLLER_ARBEIDSFORHOLD, "manuell vurdering av arbeidsforhold");
        } else {
            ryddOppAksjonspunktHvisEksisterer(behandling);
        }
        if (erAktørArbeidEndret) {
            leggTilStartpunkt(startpunkter, grunnlagId1, grunnlagId2, startpunktUtlederAktørArbeid.utledStartpunkt(), "aktørarbeid");
        }
        if (aktørYtelseEndring.erEndret()) {
            if (aktørYtelseEndring.erEksterneRegistreEndret()) {
                leggTilStartpunkt(startpunkter, grunnlagId1, grunnlagId2, startpunktUtlederAktørYtelse.utledStartpunkt(), "aktør ytelse");
            } else {
                leggTilStartpunkt(startpunkter, grunnlagId1, grunnlagId2, StartpunktType.UDEFINERT, "aktør ytelse som ikke stammer fra registre");
            }
        }
        if (erAktørInntektEndret) {
            leggTilStartpunkt(startpunkter, grunnlagId1, grunnlagId2, startpunktUtlederAktørInntekt.utledStartpunkt(), "aktør inntekt");
        }
        if (erInntektsmeldingEndret) {
            leggTilStartpunkt(startpunkter, grunnlagId1, grunnlagId2, startpunktUtlederInntektsmelding.utledStartpunkt(behandling, grunnlag1, grunnlag2), "inntektsmelding");
        }

        return startpunkter;
    }

    /*
    Kontroller arbeidsforhold skal ikke lenger være aktiv hvis tilstanden i saken ikke tilsier det
    Setter dermed aksjonspunktet til utført hvis det står til opprettet.
     */
    private void ryddOppAksjonspunktHvisEksisterer(Behandling behandling) {
        final Optional<Aksjonspunkt> aksjonspunkt = behandling.getAksjonspunkter().stream()
            .filter(ap -> ap.getAksjonspunktDefinisjon().equals(AksjonspunktDefinisjon.VURDER_ARBEIDSFORHOLD))
            .findFirst();
        if (aksjonspunkt.isPresent() && aksjonspunkt.get().erÅpentAksjonspunkt()) {
            aksjonspunktRepository.setTilUtført(aksjonspunkt.get(), null);
            behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));
        }
    }

    private void leggTilStartpunkt(List<StartpunktType> startpunkter, Long grunnlagId1, Long grunnlagId2, StartpunktType startpunkt, String endringLoggtekst) {
        startpunkter.add(startpunkt);
        FellesStartpunktUtlederLogger.loggEndringSomFørteTilStartpunkt(klassenavn, startpunkt, endringLoggtekst, grunnlagId1, grunnlagId2);
    }
}
