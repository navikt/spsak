package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding;

import java.util.Optional;

public class SykemeldingerBuilder {

    private SykemeldingerEntitet kladd;

    private SykemeldingerBuilder(SykemeldingerEntitet sykemeldingerEntitet) {
        kladd = sykemeldingerEntitet;
    }

    private static SykemeldingerBuilder ny() {
        return new SykemeldingerBuilder(new SykemeldingerEntitet());
    }

    private static SykemeldingerBuilder oppdater(Sykemeldinger sykemeldinger) {
        return new SykemeldingerBuilder(new SykemeldingerEntitet(sykemeldinger));
    }

    public static SykemeldingerBuilder oppdater(Optional<Sykemeldinger> sykemeldinger) {
        return sykemeldinger.map(SykemeldingerBuilder::oppdater).orElseGet(SykemeldingerBuilder::ny);
    }

    public SykemeldingerBuilder medSykemelding(SykemeldingBuilder builder) {
        Sykemelding build = builder.build();
        kladd.leggTil(build);
        return this;
    }

    public Sykemeldinger build() {
        return kladd;
    }

    public SykemeldingBuilder sykemeldingBuilder(String eksternReferanse) {
        SykemeldingBuilder oppdater = SykemeldingBuilder.oppdater(kladd.getSykemeldinger()
            .stream()
            .filter(sm -> sm.getEksternReferanse().equals(eksternReferanse))
            .findFirst());
        oppdater.medEksternReferanse(eksternReferanse);
        return oppdater;
    }

}
