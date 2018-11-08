package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.fpsak.nare.specification.Specification;

public class FastsettePeriodeRegelTest {

    @Test
    public void regel_skal_kunne_instansieres_via_default_constructor_for_dokumentasjonsgenerering() {
        FastsettePeriodeRegel fastsettePeriodeRegel = new FastsettePeriodeRegel();
        Specification<FastsettePeriodeGrunnlag> specification = fastsettePeriodeRegel.getSpecification();

        assertThat(specification).isNotNull();
    }

}