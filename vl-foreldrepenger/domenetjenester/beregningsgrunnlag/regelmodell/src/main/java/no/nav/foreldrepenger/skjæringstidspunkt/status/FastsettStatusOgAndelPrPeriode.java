package no.nav.foreldrepenger.skjæringstidspunkt.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.Aktivitet;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.AktivitetStatus;
import no.nav.foreldrepenger.beregningsgrunnlag.regelmodell.grunnlag.inntekt.Arbeidsforhold;
import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivPeriode;
import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.AktivitetStatusModell;
import no.nav.foreldrepenger.skjæringstidspunkt.regelmodell.BeregningsgrunnlagPrStatus;
import no.nav.fpsak.nare.doc.RuleDocumentation;
import no.nav.fpsak.nare.evaluation.Evaluation;
import no.nav.fpsak.nare.specification.LeafSpecification;

@RuleDocumentation(FastsettKombinasjoner.ID)
public class FastsettStatusOgAndelPrPeriode extends LeafSpecification<AktivitetStatusModell> {

    static final String ID = "FP_BR_19_2";
    static final String BESKRIVELSE = "Fastsett status per andel og periode";

    FastsettStatusOgAndelPrPeriode() {
        super(ID, BESKRIVELSE);
    }

    @Override
    public Evaluation evaluate(AktivitetStatusModell regelmodell) {
        opprettAktivitetStatuser(regelmodell);
        oppdaterVedTY(regelmodell.getAktivitetStatuser());

        Map<String, Object> resultater = new HashMap<>();
        regelmodell.getAktivitetStatuser()
            .forEach(as -> resultater.put("Aktivitetstatus." + as.name(), as.getBeskrivelse()));
        regelmodell.getBeregningsgrunnlagPrStatusListe()
            .forEach(bgps -> resultater.put("BeregningsgrunnlagPrStatus." + bgps.getAktivitetStatus().name(), bgps.getAktivitetStatus().getBeskrivelse()));
        return beregnet(resultater);
    }

    private void opprettAktivitetStatuser(AktivitetStatusModell regelmodell) {
        LocalDate skjæringtidspktForBeregning = regelmodell.getSkjæringstidspunktForBeregning();
        List<AktivPeriode> aktivePerioder = regelmodell.getAktivePerioder();
        List<AktivPeriode> aktivPerioderVedSkjæringtidspunkt = hentAktivePerioderVedSkjæringtidspunkt(skjæringtidspktForBeregning, aktivePerioder);
        for (AktivPeriode ap : aktivPerioderVedSkjæringtidspunkt) {
            AktivitetStatus aktivitetStatus = mapAktivitetTilStatus(ap.getAktivitet());
            regelmodell.leggTilAktivitetStatus(aktivitetStatus);

            if (!AktivitetStatus.TY.equals(aktivitetStatus)) {
                BeregningsgrunnlagPrStatus bgPrStatus = new BeregningsgrunnlagPrStatus(aktivitetStatus);
                if (AktivitetStatus.ATFL.equals(aktivitetStatus) && ap.getArbeidsforhold() != null) {
                    leggTilArbeidsforholdForArbeidstaker(bgPrStatus, ap.getArbeidsforhold());
                }
                regelmodell.leggTilBeregningsgrunnlagPrStatus(bgPrStatus);
            }
        }
    }

    private void oppdaterVedTY(List<AktivitetStatus> aktivitetStatuser) {
        if (aktivitetStatuser.contains(AktivitetStatus.TY)) {
            List<AktivitetStatus> beholdes = Collections.singletonList(AktivitetStatus.TY);
            List<AktivitetStatus> fjernes = aktivitetStatuser.stream().filter(ap -> !beholdes.contains(ap)).collect(Collectors.toList());
            aktivitetStatuser.removeAll(fjernes);
        }
    }

    private void leggTilArbeidsforholdForArbeidstaker(BeregningsgrunnlagPrStatus bgPrStatus, Arbeidsforhold arbeidsforhold) {
        if (!bgPrStatus.getArbeidsforholdList().contains(arbeidsforhold)) {
            bgPrStatus.getArbeidsforholdList().add(arbeidsforhold);
        }
    }

    private AktivitetStatus mapAktivitetTilStatus(Aktivitet aktivitet) {
        List<Aktivitet> arbeistaker = Arrays.asList(Aktivitet.ARBEIDSTAKERINNTEKT, Aktivitet.FRILANSINNTEKT,
            Aktivitet.VARTPENGER, Aktivitet.VENTELØNN, Aktivitet.ETTERLØNN, Aktivitet.VIDERE_ETTERUTDANNING, Aktivitet.SLUTTPAKKE,
            Aktivitet.UTDANNINGSPERMISJON);
        List<Aktivitet> tilstøtendeYtelse = Arrays.asList(Aktivitet.SYKEPENGER_MOTTAKER, Aktivitet.FORELDREPENGER_MOTTAKER,
            Aktivitet.PLEIEPENGER_MOTTAKER, Aktivitet.SVANGERSKAPSPENGER_MOTTAKER, Aktivitet.OPPLÆRINGSPENGER,
            Aktivitet.OMSORGSPENGER);
        AktivitetStatus aktivitetStatus;

        if (Aktivitet.NÆRINGSINNTEKT.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.SN;
        } else if (Aktivitet.DAGPENGEMOTTAKER.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.DP;
        } else if (Aktivitet.AAP_MOTTAKER.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.AAP;
        } else if (Aktivitet.MILITÆR_ELLER_SIVILTJENESTE.equals(aktivitet)) {
            aktivitetStatus = AktivitetStatus.MS;
        } else if (tilstøtendeYtelse.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.TY;
        } else if (arbeistaker.contains(aktivitet)) {
            aktivitetStatus = AktivitetStatus.ATFL;
        } else {
            aktivitetStatus = AktivitetStatus.UDEFINERT;
        }

        return aktivitetStatus;
    }

    private List<AktivPeriode> hentAktivePerioderVedSkjæringtidspunkt(LocalDate dato, List<AktivPeriode> aktivePerioder) {
        return aktivePerioder.stream().filter(ap -> ap.inneholder(dato.minusDays(1))).collect(Collectors.toList());
    }
}
