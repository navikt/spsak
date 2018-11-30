package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.domene.typer.Stillingsprosent;


public interface YtelseGrunnlag {

    Optional<Arbeidskategori> getArbeidskategori();

    Optional<Stillingsprosent> getDekningsgradProsent();

    Optional<Stillingsprosent> getGraderingProsent();

    Optional<Stillingsprosent> getInntektsgrunnlagProsent();

    Optional<LocalDate> getOpprinneligIdentdato();

    List<YtelseStørrelse> getYtelseStørrelse();
}
