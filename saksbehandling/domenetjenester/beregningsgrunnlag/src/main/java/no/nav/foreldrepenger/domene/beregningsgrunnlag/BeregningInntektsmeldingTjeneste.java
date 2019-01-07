package no.nav.foreldrepenger.domene.beregningsgrunnlag;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.domene.typer.Beløp;


public interface BeregningInntektsmeldingTjeneste {

    Beløp totaltRefusjonsbeløpFraInntektsmelding(Behandling behandling, LocalDate dato);

    boolean erTotaltRefusjonskravStørreEnnSeksG(Behandling behandling, Beregningsgrunnlag beregningsgrunnlag, LocalDate dato);

    LocalDate fastsettStartdatoInntektsmelding(Behandling behandling, Inntektsmelding inntektsmelding);
}
