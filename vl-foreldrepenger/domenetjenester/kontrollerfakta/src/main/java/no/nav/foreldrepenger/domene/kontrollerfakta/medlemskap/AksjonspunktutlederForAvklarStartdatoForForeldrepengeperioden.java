package no.nav.foreldrepenger.domene.kontrollerfakta.medlemskap;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektsmeldingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;

@ApplicationScoped
public class AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;


    AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden() {
    }

    @Inject
    AksjonspunktutlederForAvklarStartdatoForForeldrepengeperioden(BehandlingRepositoryProvider repositoryProvider,
                                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));

        if (!inntektArbeidYtelseGrunnlagOptional.isPresent()) {
            return INGEN_AKSJONSPUNKTER;
        }

        Optional<InntektsmeldingAggregat> inntektsmeldingerOptional = inntektArbeidYtelseGrunnlagOptional.get().getInntektsmeldinger();
        if (!inntektsmeldingerOptional.isPresent()) {
            return INGEN_AKSJONSPUNKTER;
        }

        Optional<AktørArbeid> aktørArbeidOptional = inntektArbeidYtelseGrunnlagOptional.get().getAktørArbeidFørStp(behandling.getAktørId());
        if (!aktørArbeidOptional.isPresent()) {
            return INGEN_AKSJONSPUNKTER;
        }

        LocalDate startdatoOppgittAvBruker = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);

        if (samsvarerStartdatoerFraInntektsmeldingOgBruker(startdatoOppgittAvBruker, inntektsmeldingerOptional.get()) == NEI) {
            if (erMinstEttArbeidsforholdLøpende(aktørArbeidOptional.get()) == JA) {
                if (samsvarerAlleLøpendeArbeidsforholdMedStartdatoFraBruker(aktørArbeidOptional.get(), inntektsmeldingerOptional.get(), startdatoOppgittAvBruker) == NEI) {
                    return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.AVKLAR_LOVLIG_OPPHOLD);
                }
            }
        }

        return INGEN_AKSJONSPUNKTER;
    }

    private Utfall samsvarerAlleLøpendeArbeidsforholdMedStartdatoFraBruker(AktørArbeid aktørArbeid, InntektsmeldingAggregat inntektsmeldingAggregat, LocalDate startdatoOppgittAvBruker) {
        return aktørArbeid.getYrkesaktiviteter()
            .stream()
            .filter(Yrkesaktivitet::erArbeidsforhold)
            .anyMatch(yrkesaktivitet -> yrkesaktivitet.getAktivitetsAvtaler()
                .stream()
                .filter(AktivitetsAvtale::getErLøpende)
                .anyMatch(aktivitetsAvtale ->
                    inntektsmeldingAggregat.getInntektsmeldingerFor(yrkesaktivitet.getArbeidsgiver().getVirksomhet()).stream()
                        .anyMatch(inntektsmelding -> !endreDatoHvisLørdagEllerSøndag(inntektsmelding.getStartDatoPermisjon()).equals(endreDatoHvisLørdagEllerSøndag(startdatoOppgittAvBruker))))) ? NEI : JA;
    }

    Utfall erMinstEttArbeidsforholdLøpende(AktørArbeid aktørArbeid) {
        return aktørArbeid.getYrkesaktiviteter().stream()
            .filter(Yrkesaktivitet::erArbeidsforhold)
            .map(Yrkesaktivitet::getAnsettelsesPeriode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(AktivitetsAvtale::getErLøpende) ? JA : NEI;
    }

    Utfall samsvarerStartdatoerFraInntektsmeldingOgBruker(LocalDate startdatoOppgittAvBruker, InntektsmeldingAggregat inntektsmeldingAggregat) {
        return inntektsmeldingAggregat.getInntektsmeldinger().stream()
            .anyMatch(im -> {
                LocalDate imDato = endreDatoHvisLørdagEllerSøndag(im.getStartDatoPermisjon());
                LocalDate startdatoOppgitt = endreDatoHvisLørdagEllerSøndag(startdatoOppgittAvBruker);
                return !imDato.equals(startdatoOppgitt);
            }) ? NEI : JA;

    }

    LocalDate endreDatoHvisLørdagEllerSøndag(LocalDate dato) {
        if (dato.getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            return dato.plusDays(2L);
        } else if (dato.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            return dato.plusDays(1L);
        }
        return dato;
    }

}
