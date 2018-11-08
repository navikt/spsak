package no.nav.foreldrepenger.regler.uttak.grunnlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.fpsak.tidsserie.LocalDateInterval;

public class FastsettePeriodeGrunnlagTestBuilder {

    public static final AktivitetIdentifikator ARBEIDSFORHOLD_1 = AktivitetIdentifikator.forArbeid("000000001", null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_3 = AktivitetIdentifikator.forArbeid("000000003", null);
    public static final AktivitetIdentifikator ARBEIDSFORHOLD_4 = AktivitetIdentifikator.forArbeid("000000004", null);

    public static FastsettePeriodeGrunnlagBuilder create() {
        return normal();
    }

    public static FastsettePeriodeGrunnlagBuilder normal() {
        ArbeidTidslinje tidslinje = new ArbeidTidslinje.Builder()
                .build();
        return FastsettePeriodeGrunnlagBuilder.create()
                .medAktivitetIdentifikator(ARBEIDSFORHOLD_1)
                .medArbeid(ARBEIDSFORHOLD_1, tidslinje);
    }

    public static FastsettePeriodeGrunnlagBuilder enGraderingsperiode(LocalDate fom, LocalDate tom, BigDecimal arbeidsprosent) {
        return enGraderingsperiode(fom, tom, Arbeid.forGradertOrdinærtArbeid(arbeidsprosent, BigDecimal.valueOf(100)), Collections.singletonList(ARBEIDSFORHOLD_1));
    }

    public static FastsettePeriodeGrunnlagBuilder enGraderingsperiodeMedFlereAktiviteter(LocalDate fom, LocalDate tom,
                                                                                         BigDecimal arbeidsprosent, List<AktivitetIdentifikator> aktivititeter) {
        return enGraderingsperiode(fom, tom, Arbeid.forGradertOrdinærtArbeid(arbeidsprosent, BigDecimal.valueOf(100)), aktivititeter);
    }

    private static FastsettePeriodeGrunnlagBuilder enGraderingsperiode(LocalDate fom, LocalDate tom, Arbeid arbeid, List<AktivitetIdentifikator> aktiviteter) {
        ArbeidTidslinje tidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(fom, tom, arbeid)
                .medArbeid(tom.plusDays(1), LocalDateInterval.TIDENES_ENDE, Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100), null))
                .build();
        FastsettePeriodeGrunnlagBuilder grunnlagBuilder = FastsettePeriodeGrunnlagBuilder.create()
                .medAktivitetIdentifikatorer(aktiviteter);
        for (AktivitetIdentifikator aktivitetIdentifikator : aktiviteter) {
            grunnlagBuilder.medArbeid(aktivitetIdentifikator, tidslinje);
        }
        return grunnlagBuilder;
    }


}
