package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.domene.typer.Prosentsats;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class SykemeldingBuilder {

    private final SykemeldingEntitet kladd;

    private SykemeldingBuilder(SykemeldingEntitet sykemeldingerEntitet) {
        this.kladd = sykemeldingerEntitet;
    }

    private static SykemeldingBuilder ny() {
        return new SykemeldingBuilder(new SykemeldingEntitet());
    }

    private static SykemeldingBuilder oppdater(Sykemelding sykemeldinger) {
        return new SykemeldingBuilder(new SykemeldingEntitet(sykemeldinger));
    }

    public static SykemeldingBuilder oppdater(Optional<Sykemelding> sykemeldinger) {
        return sykemeldinger.map(SykemeldingBuilder::oppdater).orElseGet(SykemeldingBuilder::ny);
    }

    public SykemeldingBuilder medGrad(Prosentsats prosentsats) {
        kladd.setGrad(prosentsats);
        return this;
    }

    public SykemeldingBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public SykemeldingBuilder medPeriode(LocalDate fom, LocalDate tom) {
        Objects.requireNonNull(fom, "fom");
        Objects.requireNonNull(tom, "tom");
        kladd.setPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom));
        return this;
    }

    SykemeldingBuilder medEksternReferanse(String eksternReferanse) {
        Objects.requireNonNull(eksternReferanse, "eksternReferanse");
        kladd.setEksternReferanse(eksternReferanse);
        return this;
    }

    public Sykemelding build() {
        Objects.requireNonNull(kladd.getEksternReferanse(), "eksternReferanse");
        Objects.requireNonNull(kladd.getPeriode(), "periode");
        Objects.requireNonNull(kladd.getGrad(), "grad");
        Objects.requireNonNull(kladd.getArbeidsgiver(), "arbeidsgiver");
        return kladd;
    }
}
