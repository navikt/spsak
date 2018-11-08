package no.nav.foreldrepenger.uttaksvilkår;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Arbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class KnekkpunktIdentifisererTest {

    @Test
    public void skal_finne_knekkpunkter_for_søknad_ved_fødsel() throws Exception {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSøknadstype(Søknadstype.FØDSEL)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode) //ifbm søknadsfrist
                .build();


        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                fødselsdato.minusWeeks(12), //tidligste mulige uttak
                førsteLovligeSøknadsperiode,//ifbm søknadsfrist
                fødselsdato.minusWeeks(3),  //foreldrepenger før fødsel
                fødselsdato,
                fødselsdato.plusWeeks(6),   //slutt på periode reservert mor
                fødselsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_finne_knekkpunkter_ved_adopsjon() throws Exception {
        LocalDate adopsjonsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSøknadstype(Søknadstype.ADOPSJON)
                .medFamiliehendelseDato(adopsjonsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode) //ifbm søknadsfrist
                .build();


        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
                førsteLovligeSøknadsperiode,  //ifbm søknadsfrist
                adopsjonsdato,
                adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_lage_knekkpunkt_ved_start_og_dagen_etter_periode_medslutt_av_() throws Exception {
        LocalDate adopsjonsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSøknadstype(Søknadstype.ADOPSJON)
                .medFamiliehendelseDato(adopsjonsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode) //ifbm søknadsfrist
                .medPeriodeUtenOmsorg(adopsjonsdato.plusDays(100), adopsjonsdato.plusDays(300))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
                førsteLovligeSøknadsperiode,  //ifbm søknadsfrist
                adopsjonsdato,

                adopsjonsdato.plusDays(100), //første dag uten omsorg
                adopsjonsdato.plusDays(301), //første dag med omsorg

                adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_lage_knekkpunkt_ved_start_og_dagen_etter_periode_for_alle_perioder_som_ikke_er_sammenhengende() throws Exception {
        LocalDate adopsjonsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSøknadstype(Søknadstype.ADOPSJON)
                .medFamiliehendelseDato(adopsjonsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode) //ifbm søknadsfrist
                .medPeriodeUtenOmsorg(adopsjonsdato.plusDays(100), adopsjonsdato.plusDays(200))
                .medPeriodeUtenOmsorg(adopsjonsdato.plusDays(300), adopsjonsdato.plusDays(400))
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).containsOnly(
                adopsjonsdato.minusWeeks(12), //tidligste mulige uttak?
                førsteLovligeSøknadsperiode,  //ifbm søknadsfrist
                adopsjonsdato,

                adopsjonsdato.plusDays(100), //første dag uten omsorg
                adopsjonsdato.plusDays(201), //første dag med omsorg
                adopsjonsdato.plusDays(300), //første dag uten omsorg
                adopsjonsdato.plusDays(401), //første dag med omsorg

                adopsjonsdato.plusYears(3)    //siste mulige uttak for foreldrepenger
        );
    }

    @Test
    public void skal_finne_knekkpunkter_ved_endring_i_arbeidsprosent() throws Exception {
        LocalDate fødselsdato = LocalDate.of(2018, 2, 22);
        LocalDate førsteLovligeSøknadsperiode = LocalDate.of(2017, 12, 1);
        AktivitetIdentifikator arbeidsforhold1 = AktivitetIdentifikator.forArbeid("000000000", null);
        LocalDate tirsdagUke18 = LocalDate.of(2018, 5, 1);
        LocalDate fredagUke18 = LocalDate.of(2018, 5, 4);
        LocalDate mandagUke19 = LocalDate.of(2018, 5, 7);
        LocalDate torsdagUke19 = LocalDate.of(2018, 5, 10);
        LocalDate fredagUke19 = LocalDate.of(2018, 5, 11);
        LocalDate fredagUke20 = LocalDate.of(2018, 5, 18);
        ArbeidTidslinje arbeidTidslinje = new ArbeidTidslinje.Builder()
                .medArbeid(tirsdagUke18, fredagUke18, Arbeid.forOrdinærtArbeid(BigDecimal.valueOf(50), BigDecimal.ZERO, null))
                .medArbeid(mandagUke19, torsdagUke19, Arbeid.forOrdinærtArbeid(BigDecimal.valueOf(50), BigDecimal.ZERO, null))
                .medArbeid(fredagUke19, fredagUke20, Arbeid.forOrdinærtArbeid(BigDecimal.valueOf(99), BigDecimal.ZERO, null))
                .build();


        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSøknadstype(Søknadstype.FØDSEL)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode) //ifbm søknadsfrist
                .medArbeid(arbeidsforhold1, arbeidTidslinje)
                .medBehandlingType(Behandlingtype.REVURDERING)
                .build();


        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));

        assertThat(knekkpunkter).containsOnly(
                tirsdagUke18, //starter her
                fredagUke19, //endrer fra torsdag-fredag, fredag er den første i perioden
                fredagUke20.plusDays(3) //mandag er den første i neste periode
        );
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart1() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 6, 1);
        LocalDate fødselsdato = uttakStartdato.minusMonths(1);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        LocalDate knekkdato = uttakStartdato.plusDays(1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(uttakStartdato, uttakStartdato.plusDays(10), true, false))
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, knekkdato, uttakStartdato.plusDays(10), PeriodeVurderingType.PERIODE_OK, true, true)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(uttakStartdato, knekkdato);
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperStart2() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 10, 1);
        LocalDate fødselsdato = uttakStartdato.minusWeeks(7);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        LocalDate knekkdato = uttakStartdato.plusDays(1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(uttakStartdato.plusDays(1), uttakStartdato.plusDays(10), true, false))
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, uttakStartdato, knekkdato, PeriodeVurderingType.PERIODE_OK, true, true)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(knekkdato, uttakStartdato, uttakStartdato.plusDays(11), knekkdato.plusDays(1));
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperMidtI() {
        final LocalDate uttakStartdato = LocalDate.of(2018, 10, 1);
        LocalDate fødselsdato = uttakStartdato.minusWeeks(7);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(uttakStartdato.plusDays(1), uttakStartdato.plusDays(5), true, false))
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, uttakStartdato, uttakStartdato.plusDays(6), PeriodeVurderingType.PERIODE_OK, true, true)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(uttakStartdato, uttakStartdato.plusDays(7), uttakStartdato.plusDays(1), uttakStartdato.plusDays(6));
    }

    @Test
    public void finnerKnekkpunktVedOverlappIUttakperioderMedAnnenPart_overlapperSluttAvPeriode() {
        final LocalDate stønadsperiodeFom = LocalDate.of(2018, 10, 1);
        LocalDate stønadsperiodeTom = stønadsperiodeFom.plusDays(6);
        LocalDate fødselsdato = stønadsperiodeFom.minusWeeks(7);
        LocalDate førsteLovligeSøknadsperiode = fødselsdato.minusWeeks(12);

        LocalDate annenPartPeriodeFom = stønadsperiodeFom.plusDays(4);
        LocalDate annenPartPeriodeTom = stønadsperiodeFom.plusDays(12);

        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(annenPartPeriodeFom, annenPartPeriodeTom, true, false))
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, stønadsperiodeFom, stønadsperiodeTom, PeriodeVurderingType.PERIODE_OK, true, true)
                .medFamiliehendelseDato(fødselsdato)
                .medFørsteLovligeUttaksdag(førsteLovligeSøknadsperiode)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);
        knekkpunkter.removeAll(standardKnekkpunktFødsel(fødselsdato, førsteLovligeSøknadsperiode));
        assertThat(knekkpunkter).containsExactlyInAnyOrder(stønadsperiodeFom, stønadsperiodeTom.plusDays(1), annenPartPeriodeFom, annenPartPeriodeTom.plusDays(1));
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisGraderingStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(LocalDate.of(2018, 5, 5))
                .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                .medEndringssøknadMottattdato(mottattdato)
                .medBehandlingType(Behandlingtype.REVURDERING)
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato.minusMonths(1),
                        mottattdato.minusWeeks(2), Collections.singletonList(gradertArbeidsforhold),
                        BigDecimal.valueOf(30), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisGraderingStarterPåMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(LocalDate.of(2018, 5, 5))
                .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                .medEndringssøknadMottattdato(mottattdato)
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato, mottattdato.plusWeeks(2),
                        Collections.singletonList(gradertArbeidsforhold), BigDecimal.valueOf(30), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerIkkeKnekkPåEndringssøknadMottattdatoHvisGraderingStarterEtterMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        AktivitetIdentifikator gradertArbeidsforhold = AktivitetIdentifikator.forFrilans();
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(LocalDate.of(2018, 5, 5))
                .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                .medEndringssøknadMottattdato(mottattdato)
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato.plusWeeks(1),
                        mottattdato.plusWeeks(2), Collections.singletonList(gradertArbeidsforhold),
                        BigDecimal.valueOf(30), PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).doesNotContain(mottattdato);
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterFørMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(LocalDate.of(2018, 5, 5))
                .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                .medEndringssøknadMottattdato(mottattdato)
                .medBehandlingType(Behandlingtype.REVURDERING)
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato.minusWeeks(2),
                        mottattdato.minusWeeks(1), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterPåMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(LocalDate.of(2018, 5, 5))
                .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                .medEndringssøknadMottattdato(mottattdato)
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato,
                        mottattdato.plusWeeks(2), Utsettelseårsaktype.ARBEID, PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).contains(mottattdato);
    }

    @Test
    public void finnerIkkeKnekkPåEndringssøknadMottattdatoHvisUtsettelseFerieArbeidStarterEtterMottattdato() {
        LocalDate mottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(LocalDate.of(2018, 5, 5))
                .medFørsteLovligeUttaksdag(LocalDate.of(2018, 5, 5))
                .medEndringssøknadMottattdato(mottattdato)
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, mottattdato.plusWeeks(1),
                        mottattdato.plusWeeks(2), Utsettelseårsaktype.FERIE, PeriodeVurderingType.PERIODE_OK, false, false)
                .build();

        Set<LocalDate> knekkpunkter = KnekkpunktIdentifiserer.finnKnekkpunkter(grunnlag, StandardKonfigurasjon.KONFIGURASJON);

        assertThat(knekkpunkter).doesNotContain(mottattdato);
    }

    private List<LocalDate> standardKnekkpunktFødsel(LocalDate fødselsdato, LocalDate førsteLovligeSøknadsperiode) {
        return Arrays.asList(
                fødselsdato.minusWeeks(12), //tidligste mulige uttak
                førsteLovligeSøknadsperiode,//ifbm søknadsfrist
                fødselsdato.minusWeeks(3),  //foreldrepenger før fødsel
                fødselsdato,
                fødselsdato.plusWeeks(6),   //slutt på periode reservert mor
                fødselsdato.plusYears(3));  //siste mulige uttak for foreldrepenger
    }

}


