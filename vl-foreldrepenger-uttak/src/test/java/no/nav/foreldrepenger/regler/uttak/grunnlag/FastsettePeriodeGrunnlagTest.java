package no.nav.foreldrepenger.regler.uttak.grunnlag;

import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeidsprosenter;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeBehandler;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeBehandlerImpl;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.GyldigGrunnPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.InnvilgetÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;

public class FastsettePeriodeGrunnlagTest {


    @Test
    public void skal_kunne_godkjenne_alle_perioder() {
        LocalDate termindato = LocalDate.of(2017, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medSaldo(Stønadskontotype.MØDREKVOTE, 10 * 5)
            .medSaldo(Stønadskontotype.FELLESPERIODE, 26 * 5)
            .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, termindato.minusWeeks(3), termindato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, termindato, termindato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, termindato.plusWeeks(10), termindato.plusWeeks(10).plusWeeks(30).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .build();

        Arbeidsprosenter arbeidsprosenter = new Arbeidsprosenter();
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder().medArbeid(termindato.minusWeeks(3),
                termindato.plusWeeks(30), Arbeid.forOrdinærtArbeid(BigDecimal.ZERO, BigDecimal.valueOf(100), null)).build();
        arbeidsprosenter.leggTil(ARBEIDSFORHOLD_1, arbeidTidslinje);

        FastsettePeriodeBehandler fastsettePeriodeBehandler = new FastsettePeriodeBehandlerImpl(grunnlag);
        int teller = 0;
        Optional<UttakPeriode> aktuellPeriode = grunnlag.getAktuellPeriode();
        while (aktuellPeriode.isPresent()) {
            fastsettePeriodeBehandler.innvilgAktuellPeriode(null, InnvilgetÅrsak.FORELDREPENGER_FØR_FØDSEL, false, null, arbeidsprosenter, true);
            grunnlag.nestePeriode();
            aktuellPeriode = grunnlag.getAktuellPeriode();
            teller++;
        }

        assertThat(teller).isEqualTo(3);
    }


    @Test
    public void skal_kunne_gi_aktuelle_gyldige_grunner_perioder_for_tidlig_oppstart_sortert_på_fom_dato() {
        LocalDate termindato = LocalDate.of(2017, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
            .medSaldo(Stønadskontotype.FEDREKVOTE, 10 * 5)
            .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, termindato.plusWeeks(3), termindato.plusWeeks(10), PeriodeVurderingType.PERIODE_OK)
            .medFamiliehendelseDato(termindato)
            .medGyldigGrunnForTidligOppstartPeriode(termindato, termindato.plusWeeks(2))
            .medGyldigGrunnForTidligOppstartPeriode(termindato.plusWeeks(3), termindato.plusWeeks(3).plusDays(4))
            .medGyldigGrunnForTidligOppstartPeriode(termindato.plusWeeks(4), termindato.plusWeeks(4).plusDays(4))
            .medGyldigGrunnForTidligOppstartPeriode(termindato.plusWeeks(5), termindato.plusWeeks(5).plusDays(4))
            .build();

        List<GyldigGrunnPeriode> aktuelleGyldigeGrunnerForTidligOppstartPerioder = grunnlag.getAktuelleGyldigeGrunnPerioder();

        assertThat(aktuelleGyldigeGrunnerForTidligOppstartPerioder.size()).isEqualTo(3);
        assertThat(aktuelleGyldigeGrunnerForTidligOppstartPerioder.get(0).getFom()).isEqualTo(termindato.plusWeeks(3));
        assertThat(aktuelleGyldigeGrunnerForTidligOppstartPerioder.get(1).getFom()).isEqualTo(termindato.plusWeeks(4));
        assertThat(aktuelleGyldigeGrunnerForTidligOppstartPerioder.get(2).getFom()).isEqualTo(termindato.plusWeeks(5));
    }


}
