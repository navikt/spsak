package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.Årsak;

public interface OppgittPeriode {

    UttakPeriodeType getPeriodeType();

    LocalDate getFom();

    LocalDate getTom();

    BigDecimal getArbeidsprosent();

    Årsak getÅrsak();

    MorsAktivitet getMorsAktivitet();

    Optional<String> getBegrunnelse();

    boolean getErArbeidstaker();

    Virksomhet getVirksomhet();

    UttakPeriodeVurderingType getPeriodeVurderingType();

    boolean isSamtidigUttak();

    boolean isFlerbarnsdager();

    FordelingPeriodeKilde getPeriodeKilde();

    BigDecimal getSamtidigUttaksprosent();
}
