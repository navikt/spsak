package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;

public class SykefraværGrunnlagBuilder {

    private final SykefraværGrunnlagEntitet kladd;

    private SykefraværGrunnlagBuilder(SykefraværGrunnlagEntitet sykefraværGrunnlagEntitet) {
        this.kladd = sykefraværGrunnlagEntitet;
    }

    private static SykefraværGrunnlagBuilder ny() {
        return new SykefraværGrunnlagBuilder(new SykefraværGrunnlagEntitet());
    }

    private static SykefraværGrunnlagBuilder oppdater(SykefraværGrunnlagEntitet grunnlag) {
        return new SykefraværGrunnlagBuilder(new SykefraværGrunnlagEntitet(grunnlag));
    }

    public static SykefraværGrunnlagBuilder oppdater(Optional<SykefraværGrunnlagEntitet> grunnlagEntitet) {
        return grunnlagEntitet.map(SykefraværGrunnlagBuilder::oppdater).orElseGet(SykefraværGrunnlagBuilder::ny);
    }

    public SykefraværGrunnlagBuilder medSykefravær(SykefraværBuilder builder) {
        kladd.setSykefravær(builder.build());
        return this;
    }

    public SykefraværGrunnlagBuilder medSykemeldinger(SykemeldingerBuilder builder) {
        kladd.setSykemeldinger(builder.build());
        return this;
    }

    public SykefraværGrunnlag build() {
        return kladd;
    }

}
