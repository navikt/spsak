package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public class FastsettePeriodeRegelOrkestreringToParterTest {

    protected FastsettePerioderRegelOrkestrering fastsettePerioderRegelOrkestrering = new FastsettePerioderRegelOrkestrering();
    private static final int UKER_FPFF = 3;
    private static final int UKER_MK = 15;
    private static final int UKER_FK = 15;
    private static final int UKER_FP = 16;

    private FastsettePeriodeGrunnlagBuilder leggPåKvoter(FastsettePeriodeGrunnlagBuilder builder) {
        return builder
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, UKER_FPFF * 5)
                .medSaldo(MØDREKVOTE, UKER_MK * 5)
                .medSaldo(FEDREKVOTE, UKER_FK * 5)
                .medSaldo(FELLESPERIODE, UKER_FP * 5);
    }


    private LocalDate førsteLovligeDato = LocalDate.of(2017, 10, 1);
    private LocalDate fødselsdato = LocalDate.of(2018, 1, 1);

    private static final AktivitetIdentifikator FAR_ARBEIDSFORHOLD = FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_3;

    @Test
    public void far_har_uttak_og_mor_søker_før_og_etter_fars_uttak() throws Exception {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        LocalDate fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        LocalDate tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP).minusDays(1);
        LocalDate tomMorsFPsøknad = fødselsdato.plusWeeks(50);

        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeDato)
                .medUttakPeriodeForAnnenPart(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFP))
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fomMorsFP, tomMorsFPsøknad, PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(5);
        StønadsPeriode p0 = (StønadsPeriode) resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p1a = (StønadsPeriode) resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p1b = (StønadsPeriode) resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p2 = (StønadsPeriode) resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        StønadsPeriode p3 = (StønadsPeriode) resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    public void far_har_uttak_og_mor_søker_om_uttak_før_fars_uttak_slutter_slik_at_mor_tar_dager_fra_far() throws Exception {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFPorginal = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        LocalDate fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        LocalDate tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP).minusDays(1);
        LocalDate tomMorsFPsøknad = fødselsdato.plusWeeks(50);

        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeDato)
                .medUttakPeriodeForAnnenPart(lagPeriodeForFar(FELLESPERIODE, fomFarsFP, tomFarsFPorginal))
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fomMorsFP, tomMorsFPsøknad, PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(5);
        StønadsPeriode p0 = (StønadsPeriode) resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p1a = (StønadsPeriode) resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p1b = (StønadsPeriode) resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p2 = (StønadsPeriode) resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        StønadsPeriode p3 = (StønadsPeriode) resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    @Test
    public void når_far_har_uttak_og_gradert_i_ett_arbeidsforhold_ser_mors_tilgjengelige_dager_kun_det_arbeidsforholdet_for_far_med_minst_forbruk() {
        LocalDate fomFarsFP = fødselsdato.plusWeeks(UKER_MK);
        LocalDate tomFarsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2).minusDays(1);
        LocalDate fomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP / 2);
        LocalDate tomMorsFP = fødselsdato.plusWeeks(UKER_MK + UKER_FP + 7).minusDays(1); //7 ekstra uker pga fars gradering
        LocalDate tomMorsFPsøknad = fødselsdato.plusWeeks(50);
        AktivitetIdentifikator farArbeidsforhold2 = FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_4;

        FastsettePeriodeGrunnlagBuilder grunnlag = FastsettePeriodeGrunnlagTestBuilder.create();
        leggPåKvoter(grunnlag)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeDato)
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart.Builder(fomFarsFP, tomFarsFP, true, false)
                        .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(FAR_ARBEIDSFORHOLD, FELLESPERIODE, Virkedager.beregnAntallVirkedager(fomFarsFP, tomFarsFP), BigDecimal.TEN))
                        .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(farArbeidsforhold2, FELLESPERIODE, 5, BigDecimal.valueOf(87.5))).build())
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(UKER_FPFF), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(UKER_MK).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fomMorsFP, tomMorsFPsøknad, PeriodeVurderingType.PERIODE_OK)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(5);
        StønadsPeriode p0 = (StønadsPeriode) resultat.get(0).getUttakPeriode();
        assertThat(p0.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);
        assertThat(p0.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p1a = (StønadsPeriode) resultat.get(1).getUttakPeriode();
        assertThat(p1a.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1a.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p1b = (StønadsPeriode) resultat.get(2).getUttakPeriode();
        assertThat(p1b.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(p1b.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        StønadsPeriode p2 = (StønadsPeriode) resultat.get(3).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p2.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(p2.getFom()).isEqualTo(fomMorsFP);
        assertThat(p2.getTom()).isEqualTo(tomMorsFP);
        StønadsPeriode p3 = (StønadsPeriode) resultat.get(4).getUttakPeriode();
        assertThat(p2.getStønadskontotype()).isEqualTo(FELLESPERIODE);
        assertThat(p3.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(p3.getFom()).isEqualTo(tomMorsFP.plusDays(1));
        assertThat(p3.getTom()).isEqualTo(tomMorsFPsøknad);
    }

    private FastsattPeriodeAnnenPart lagPeriodeForFar(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom) {
        return lagPeriode(stønadskontotype, fom, tom, FAR_ARBEIDSFORHOLD, Virkedager.beregnAntallVirkedager(fom, tom));
    }

    private FastsattPeriodeAnnenPart lagPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom, AktivitetIdentifikator aktivitet, int trekkdsager) {
        return new FastsattPeriodeAnnenPart.Builder(fom, tom, true, false)
                .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitet, stønadskontotype, trekkdsager, BigDecimal.TEN))
                .build();
    }


}
