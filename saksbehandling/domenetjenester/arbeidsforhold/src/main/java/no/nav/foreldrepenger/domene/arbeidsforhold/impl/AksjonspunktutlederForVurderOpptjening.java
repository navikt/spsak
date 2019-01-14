package no.nav.foreldrepenger.domene.arbeidsforhold.impl;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat.opprettListeForAksjonspunkt;
import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening.Utfall.JA;
import static no.nav.foreldrepenger.domene.arbeidsforhold.impl.AksjonspunktutlederForVurderOpptjening.Utfall.NEI;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktUtleder;
import no.nav.foreldrepenger.behandlingskontroll.AksjonspunktResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktivitetsAvtale;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørArbeid;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.AktørInntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntekt;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Inntektspost;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektspostType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.Opptjening;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Beløp;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class AksjonspunktutlederForVurderOpptjening implements AksjonspunktUtleder {

    private static final List<AksjonspunktResultat> INGEN_AKSJONSPUNKTER = emptyList();
    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;
    private OpptjeningRepository opptjeningRepository;
    private KodeverkRepository kodeverkRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private BehandlingRepository behandlingRepository;

    AksjonspunktutlederForVurderOpptjening() {
        // CDI
    }

    @Inject
    public AksjonspunktutlederForVurderOpptjening(GrunnlagRepositoryProvider repositoryProvider, ResultatRepositoryProvider resultatRepositoryProvider,
                                                  SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.inntektArbeidYtelseRepository = repositoryProvider.getInntektArbeidYtelseRepository();
        this.opptjeningRepository = resultatRepositoryProvider.getOpptjeningRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public List<AksjonspunktResultat> utledAksjonspunkterFor(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        Optional<Opptjening> fastsattOpptjeningOptional = hentOpptjeningHvisEksisterer(behandling);
        if (inntektArbeidYtelseGrunnlagOptional.isEmpty() || fastsattOpptjeningOptional.isEmpty()) {
            return INGEN_AKSJONSPUNKTER;
        }
        OppgittOpptjening oppgittOpptjening = inntektArbeidYtelseGrunnlagOptional.get().getOppgittOpptjening().orElse(null);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseGrunnlagOptional.get();
        DatoIntervallEntitet opptjeningPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(fastsattOpptjeningOptional.get().getFom(), fastsattOpptjeningOptional.get().getTom());

        if (harBrukerOppgittPerioderMed(oppgittOpptjening, opptjeningPeriode, finnRelevanteKoder(kodeverkRepository)) == JA) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
        }

        if (finnesDetArbeidsforholdMedStillingsprosentHøyereEnn0(inntektArbeidYtelseGrunnlag, opptjeningPeriode, behandling.getAktørId()) == JA) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
        }

        if (harBrukerOppgittArbeidsforholdMed(ArbeidType.UTENLANDSK_ARBEIDSFORHOLD, opptjeningPeriode, oppgittOpptjening) == JA) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
        }

        if (harBrukerOppgittPerioderMed(oppgittOpptjening, opptjeningPeriode, Collections.singletonList(ArbeidType.FRILANSER)) == JA
            || finnesDetBekreftetFrilans(inntektArbeidYtelseGrunnlag, opptjeningPeriode, behandling.getAktørId()) == JA) {
            return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
        }

        if (harBrukerOppgittÅVæreSelvstendigNæringsdrivende(oppgittOpptjening, opptjeningPeriode) == JA) {
            // Det siste ferdiglignede år vil alltid være året før behandlingstidspunktet
            // Bruker LocalDate.now() her etter avklaring med funksjonell.
            int sistFerdiglignetÅr = LocalDate.now(FPDateUtil.getOffset()).minusYears(1L).getYear();
            if (inneholderSisteFerdiglignendeÅrNæringsinntekt(behandling, inntektArbeidYtelseGrunnlag, sistFerdiglignetÅr, opptjeningPeriode) == NEI) {
                if (erDetRegistrertNæringEtterSisteFerdiglignendeÅr(oppgittOpptjening, sistFerdiglignetÅr) == NEI) {
                    return opprettListeForAksjonspunkt(AksjonspunktDefinisjon.VURDER_PERIODER_MED_OPPTJENING);
                }
            }
        }
        return INGEN_AKSJONSPUNKTER;
    }

    private List<ArbeidType> finnRelevanteKoder(KodeverkRepository kodeverkRepository) {
        return kodeverkRepository.hentAlle(ArbeidType.class)
            .stream()
            .filter(ArbeidType::erAnnenOpptjening)
            .collect(Collectors.toList());
    }

    private Utfall harBrukerOppgittArbeidsforholdMed(ArbeidType annenOpptjeningType, DatoIntervallEntitet opptjeningPeriode, OppgittOpptjening oppgittOpptjening) {
        if (oppgittOpptjening == null) {
            return NEI;
        }

        for (OppgittArbeidsforhold oppgittArbeidsforhold : oppgittOpptjening.getOppgittArbeidsforhold()) {
            if (oppgittArbeidsforhold.getArbeidType().equals(annenOpptjeningType) && opptjeningPeriode.overlapper(oppgittArbeidsforhold.getPeriode())) {
                return JA;
            }
        }
        return NEI;
    }


    private Utfall harBrukerOppgittPerioderMed(OppgittOpptjening oppgittOpptjening, DatoIntervallEntitet opptjeningPeriode, List<ArbeidType> annenOpptjeningType) {
        if (oppgittOpptjening == null) {
            return NEI;
        }

        for (AnnenAktivitet annenAktivitet : oppgittOpptjening.getAnnenAktivitet()) {
            if (annenOpptjeningType.contains(annenAktivitet.getArbeidType()) && opptjeningPeriode.overlapper(annenAktivitet.getPeriode())) {
                return JA;
            }
        }
        return NEI;
    }

    private Utfall finnesDetArbeidsforholdMedStillingsprosentHøyereEnn0(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, DatoIntervallEntitet opptjeningPeriode, AktørId søker) {
        Optional<AktørArbeid> aktørArbeidOpt = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(søker);
        if (aktørArbeidOpt.isPresent()) {
            AktørArbeid aktørArbeid = aktørArbeidOpt.get();
            for (Yrkesaktivitet yrkesaktivitet : aktørArbeid.getYrkesaktiviteter().stream().filter(it -> it.getArbeidType().equals(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD)).collect(Collectors.toList())) {
                if (girAksjonspunkt(opptjeningPeriode, yrkesaktivitet)) {
                    return JA;
                }
            }
        }
        return NEI;
    }

    private boolean girAksjonspunkt(DatoIntervallEntitet opptjeningPeriode, Yrkesaktivitet yrkesaktivitet) {
        for (AktivitetsAvtale aktivitetsAvtale : yrkesaktivitet.getAktivitetsAvtaler()) {
            if ((aktivitetsAvtale.getProsentsats() == null || aktivitetsAvtale.getProsentsats().getVerdi().compareTo(BigDecimal.ZERO) == 0)
                && opptjeningPeriode.overlapper(aktivitetsAvtale.getPeriode())) {
                return true;
            }
        }
        return false;
    }

    private Utfall finnesDetBekreftetFrilans(InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, DatoIntervallEntitet opptjeningPeriode, AktørId søker) {
        Optional<AktørArbeid> aktørArbeidOpt = inntektArbeidYtelseGrunnlag.getAktørArbeidFørStp(søker);
        if (aktørArbeidOpt.isPresent()) {
            AktørArbeid aktørArbeid = aktørArbeidOpt.get();
            for (Yrkesaktivitet yrkesaktivitet : aktørArbeid.getFrilansOppdrag()) {
                for (AktivitetsAvtale aktivitetsAvtale : yrkesaktivitet.getAktivitetsAvtaler()) {
                    if (opptjeningPeriode.overlapper(aktivitetsAvtale.getPeriode())) {
                        return JA;
                    }
                }
            }
        }
        return NEI;
    }

    boolean girAksjonspunktForOppgittNæring(Behandling behandling) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlagOptional = inntektArbeidYtelseRepository.hentAggregatHvisEksisterer(behandling,
            skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
        Optional<Opptjening> fastsattOpptjeningOptional = hentOpptjeningHvisEksisterer(behandling);
        if (inntektArbeidYtelseGrunnlagOptional.isEmpty() || fastsattOpptjeningOptional.isEmpty()) {
            return false;
        }
        OppgittOpptjening oppgittOpptjening = inntektArbeidYtelseGrunnlagOptional.get().getOppgittOpptjening().orElse(null);
        InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag = inntektArbeidYtelseGrunnlagOptional.get();
        DatoIntervallEntitet opptjeningPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(fastsattOpptjeningOptional.get().getFom(), fastsattOpptjeningOptional.get().getTom());
        if (harBrukerOppgittÅVæreSelvstendigNæringsdrivende(oppgittOpptjening, opptjeningPeriode) == JA) {
            // Det siste ferdiglignede år vil alltid være året før behandlingstidspunktet
            // Bruker LocalDate.now() her etter avklaring med funksjonell.
            int sistFerdiglignetÅr = LocalDate.now(FPDateUtil.getOffset()).minusYears(1L).getYear();
            if (inneholderSisteFerdiglignendeÅrNæringsinntekt(behandling, inntektArbeidYtelseGrunnlag, sistFerdiglignetÅr, opptjeningPeriode) == NEI) {
                return erDetRegistrertNæringEtterSisteFerdiglignendeÅr(oppgittOpptjening, sistFerdiglignetÅr) == NEI;
            }
        }
        return false;
    }

    private Utfall harBrukerOppgittÅVæreSelvstendigNæringsdrivende(OppgittOpptjening oppgittOpptjening, DatoIntervallEntitet opptjeningPeriode) {
        if (oppgittOpptjening == null) {
            return NEI;
        }

        for (EgenNæring egenNæring : oppgittOpptjening.getEgenNæring()) {
            if (opptjeningPeriode.overlapper(egenNæring.getPeriode())) {
                return JA;
            }
        }
        return NEI;
    }

    private Utfall inneholderSisteFerdiglignendeÅrNæringsinntekt(Behandling behandling, InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, int sistFerdiglignetÅr,
                                                                 DatoIntervallEntitet opptjeningPeriode) {
        Optional<AktørInntekt> aktørInntekt = utledAktørInntekt(behandling, inntektArbeidYtelseGrunnlag, opptjeningPeriode, sistFerdiglignetÅr);
        return aktørInntekt.filter(aktørInntekt1 -> aktørInntekt1.getBeregnetSkatt().stream()
            .map(Inntekt::getInntektspost)
            .flatMap(java.util.Collection::stream)
            .filter(inntektspost -> inntektspost.getInntektspostType().equals(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE)
                || inntektspost.getInntektspostType().equals(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE))
            .anyMatch(harInntektI(sistFerdiglignetÅr))).map(aktørInntekt1 -> JA).orElse(NEI);

    }

    private Optional<AktørInntekt> utledAktørInntekt(Behandling behandling, InntektArbeidYtelseGrunnlag inntektArbeidYtelseGrunnlag, DatoIntervallEntitet opptjeningPeriode, int sistFerdiglignetÅr) {
        if (opptjeningPeriode.getTomDato().getYear() >= sistFerdiglignetÅr) {
            return inntektArbeidYtelseGrunnlag.getAktørInntektForFørStp(behandling.getAktørId());
        }
        return inntektArbeidYtelseGrunnlag.getAktørInntektForEtterStp(behandling.getAktørId());
    }

    private Predicate<Inntektspost> harInntektI(int sistFerdiglignetÅr) {
        return inntektspost -> inntektspost.getTilOgMed().getYear() == sistFerdiglignetÅr &&
            inntektspost.getBeløp().compareTo(Beløp.ZERO) > 0;
    }

    private Utfall erDetRegistrertNæringEtterSisteFerdiglignendeÅr(OppgittOpptjening oppgittOpptjening, int sistFerdiglignetÅr) {
        if (oppgittOpptjening == null) {
            return NEI;
        }

        return oppgittOpptjening.getEgenNæring().stream()
            .anyMatch(egenNæring -> erRegistrertNæring(egenNæring, sistFerdiglignetÅr)) ? JA : NEI;
    }

    private boolean erRegistrertNæring(EgenNæring eg, int sistFerdiglignetÅr) {
        return eg.getVirksomhet() != null && eg.getVirksomhet().getRegistrert().getYear() > sistFerdiglignetÅr;
    }

    boolean girAksjonspunktForArbeidsforhold(Behandling behandling, Yrkesaktivitet registerAktivitet) {
        final Optional<Opptjening> opptjening = hentOpptjeningHvisEksisterer(behandling);
        if (!opptjening.isPresent() || registerAktivitet == null) {
            return false;
        }
        final DatoIntervallEntitet opptjeningPeriode = DatoIntervallEntitet.fraOgMedTilOgMed(opptjening.get().getFom(), opptjening.get().getTom());
        return girAksjonspunkt(opptjeningPeriode, registerAktivitet);
    }

    private Optional<Opptjening> hentOpptjeningHvisEksisterer(Behandling behandling) {
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        if (behandlingsresultat.isEmpty()) {
            return Optional.empty();
        }
        return opptjeningRepository.finnOpptjening(behandlingsresultat.get());
    }
    
    enum Utfall {
        JA,
        NEI
    }

}
