package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerBuilder;

public interface SykefraværTestScenario<S> {

    SykemeldingerBuilder getSykemeldingerBuilder();

    SykefraværBuilder getSykefraværBuilder();

    S medSykefravær(SykefraværBuilder sykefraværBuilder);

    S medSykemeldinger(SykemeldingerBuilder sykemeldingerBuilder);

}
