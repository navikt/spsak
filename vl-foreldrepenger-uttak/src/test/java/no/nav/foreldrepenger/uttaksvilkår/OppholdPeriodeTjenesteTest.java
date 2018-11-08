package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Oppholdårsaktype.MANGLENDE_SØKT_PERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OppholdPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.LukketPeriode;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;
import no.nav.foreldrepenger.regler.uttak.konfig.Konfigurasjon;
import no.nav.foreldrepenger.regler.uttak.konfig.Parametertype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class OppholdPeriodeTjenesteTest {

    private final Konfigurasjon konfigurasjon = StandardKonfigurasjon.KONFIGURASJON;
    private final int mødrekvoteDager = konfigurasjon.getParameter(Parametertype.MØDREKVOTE_DAGER_100_PROSENT, LocalDate.of(2018, 06, 01));
    private final int fedrekvoteDager = konfigurasjon.getParameter(Parametertype.FEDREKVOTE_DAGER_100_PROSENT, LocalDate.of(2018, 06, 01));
    private final int fellesperiodDedager = konfigurasjon.getParameter(Parametertype.FELLESPERIODE_100_PROSENT_BEGGE_RETT_DAGER, LocalDate.of(2018, 06, 01));
    private final int førFødselDager = konfigurasjon.getParameter(Parametertype.FORELDREPENGER_FØR_FØDSEL, LocalDate.of(2018, 06, 01));


    @Test
    public void skalFinneHullPåBegynnelsenAvEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                // !!! Skal finne et hull her (f.o.m. i dag t.o.m. i dag) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), start.plusDays(1), false, true), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, false, true)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(start);
        assertThat(hull.get(0).getTom()).isEqualTo(start);
    }

    @Test
    public void skalFinneEttHullInniEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, false, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et hull her (f.o.m. i morgen t.o.m. i morgen) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, false, true) // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
        );

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(hull.get(0).getTom()).isEqualTo(start.plusDays(1));
    }

    @Test
    public void skalFinneFlereHullInniEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(4);

        /* Fem dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, false, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                // !!! Skal finne et hull her (f.o.m. i morgen t.o.m. i morgen) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(2), start.plusDays(2), false, true), // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                // !!! Skal finne et hull her (f.o.m. om tre dager t.o.m. om tre dager) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, false, true) // En uttaksperiode f.o.m. om fire dager t.o.m. om fire dager
        );

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(start.plusDays(1));
        assertThat(hull.get(0).getTom()).isEqualTo(start.plusDays(1));
        assertThat(hull.get(1).getFom()).isEqualTo(start.plusDays(3));
        assertThat(hull.get(1).getTom()).isEqualTo(start.plusDays(3));
    }

    @Test
    public void skalFinneHullPåSluttenAvEnPeriodOfInterest() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, false, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), start.plusDays(1), false, true) // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                // !!! Skal finne et hull her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(slutt);
        assertThat(hull.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    public void skalUtledeOppholdIForeldrepengerFørFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(familiehendelsesDato)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medSøknadstype(Søknadstype.FØDSEL)
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdFørFødsel = oppholdPerioder.stream()
                .filter(oppholdPeriode -> oppholdPeriode.getStønadskontotype().equals(FORELDREPENGER_FØR_FØDSEL))
                .findFirst();

        assertThat(oppholdFørFødsel).isPresent();
        oppholdFørFødsel.ifPresent(opphold -> {
            assertThat(opphold.getOppholdårsaktype()).isEqualTo(MANGLENDE_SØKT_PERIODE);
            assertThat(opphold.getFom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato));
            assertThat(opphold.getTom()).isEqualTo(sluttForeldrepengerFørFødsel(familiehendelsesDato));
        });
    }

    @Test
    public void skalIkkeUtledeOppholdIForeldrepengerFørFødselDersomSøknadstypeErAdopsjon() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(familiehendelsesDato)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medSøknadstype(Søknadstype.ADOPSJON)
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdFørFødsel = oppholdPerioder.stream()
                .filter(oppholdPeriode -> oppholdPeriode.getStønadskontotype().equals(FORELDREPENGER_FØR_FØDSEL))
                .findFirst();

        assertThat(oppholdFørFødsel).isNotPresent();
    }

    @Test
    public void skalUtledeOppholdIFellesperiodeFørFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4);


        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medFamiliehendelseDato(familiehendelsesDato)
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD,
                        startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(5), startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .build();


        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdFørFødsel = oppholdPerioder.stream()
                .filter(oppholdPeriode -> oppholdPeriode.getStønadskontotype().equals(FELLESPERIODE))
                .findFirst();

        assertThat(oppholdFørFødsel).isPresent();
        oppholdFørFødsel.ifPresent(opphold -> {
            assertThat(opphold.getOppholdårsaktype()).isEqualTo(MANGLENDE_SØKT_PERIODE);
            assertThat(opphold.getFom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusWeeks(2).plusDays(1));
            assertThat(opphold.getTom()).isEqualTo(startForeldrepengerFørFødsel(familiehendelsesDato).minusDays(1));
        });
    }

    @Test
    public void skalUtledeOppholdMødrekvoteEtterFødsel() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medFamiliehendelseDato(familiehendelsesDato)
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isNotEmpty();

        Optional<OppholdPeriode> oppholdEtterFødsel = oppholdPerioder.stream()
                .filter(opphold -> opphold.getStønadskontotype().equals(MØDREKVOTE))
                .findFirst();

        assertThat(oppholdEtterFødsel).isPresent();

        oppholdEtterFødsel.ifPresent(opphold -> {
            assertThat(opphold.getOppholdårsaktype()).isEqualTo(MANGLENDE_SØKT_PERIODE);
            assertThat(opphold.getFom()).isEqualTo(startMødrekvoteEtterFødsel(familiehendelsesDato));
            assertThat(opphold.getTom()).isEqualTo(sluttMødrekvoteEtterFødsel(familiehendelsesDato).minusDays(1));
        });
    }

    @Test
    public void skalIkkeUtledeOppholdIPerioderFørEndringsdato() {
        LocalDate familiehendelsesDato = LocalDate.of(2018, 6, 4).plusWeeks(4);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medFamiliehendelseDato(familiehendelsesDato)
                .medRevurderingEndringsdato(LocalDate.of(2019, 6, 4))
                .build();

        List<OppholdPeriode> oppholdPerioder = OppholdPeriodeTjeneste.utledOppholdForMorFraOppgittePerioder(grunnlag, konfigurasjon);

        assertThat(oppholdPerioder).isEmpty();
    }

    @Test
    public void skalIkkeFinneHullHvisDetIkkeErNoenHull() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Arrays.asList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start, start, false, true), // En uttaksperiode f.o.m. i dag t.o.m. i dag
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), start.plusDays(1), false, true), // En uttaksperiode f.o.m. i morgen t.o.m. i morgen
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, slutt, slutt, false, true)  // En uttaksperiode f.o.m. overimorgen t.o.m. overimorgen
                /// !!! Skal ikke finne noen hull !!!
        );


        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).isEmpty();
    }

    @Test
    public void skalHåndterePerioderSomBegynnnerFørPoi() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Collections.singletonList(
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.minusDays(1), slutt.minusDays(1), false, true) // En uttaksperiode f.o.m. i går t.o.m. i morgen
                // !!! Skal finne et hull her (f.o.m. overimorgen t.o.m. overimorgen) !!!
        );

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(slutt);
        assertThat(hull.get(0).getTom()).isEqualTo(slutt);
    }

    @Test
    public void skalHåndterePerioderSomSlutterEtterPoi() {
        LocalDate start = LocalDate.of(2018, 6, 4);
        LocalDate slutt = start.plusDays(2);

        /* Tre dagers "period of interest" f.o.m. i dag */
        LukketPeriode poi = new LukketPeriode(start, slutt);
        List<LukketPeriode> perioder = Collections.singletonList(
                // !!! Skal finne et hull her (f.o.m. i dag t.o.m. i dag) !!!
                new StønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, start.plusDays(1), slutt.plusDays(1), false, true) // En uttaksperiode f.o.m. i morgen t.o.m. om tre dager
        );

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullInnenforKontrollPeriode(perioder, poi);

        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(start);
        assertThat(hull.get(0).getTom()).isEqualTo(start);
    }

    @Test
    public void finnerHullMellomSøktePerioderOgAnnenPartsUttakperioder() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate hullDato = fødselsdato.plusWeeks(6);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medFamiliehendelseDato(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, hullDato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(hullDato.plusDays(1), fødselsdato.plusWeeks(10), true, false))
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullISøktePerioderOgFastsattePerioderTilAnnenPart(grunnlag);
        assertThat(hull).hasSize(1);
        assertThat(hull.get(0).getFom()).isEqualTo(hullDato);
        assertThat(hull.get(0).getTom()).isEqualTo(hullDato);
    }

    @Test
    public void finnerIkkeHullFørRevurderingEndringsdato() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate hullDato = fødselsdato.plusWeeks(6);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medFamiliehendelseDato(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, hullDato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(hullDato.plusDays(1), fødselsdato.plusWeeks(10), true, false))
                .medRevurderingEndringsdato(LocalDate.of(2019, 1, 1))
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullISøktePerioderOgFastsattePerioderTilAnnenPart(grunnlag);
        assertThat(hull).isEmpty();
    }


    @Test
    public void overlappendePerioderMedAnnenPartUtenHull() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medFamiliehendelseDato(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(10), PeriodeVurderingType.PERIODE_OK)
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(fødselsdato.plusWeeks(7).plusDays(1), fødselsdato.plusWeeks(8), true, false))
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(fødselsdato.plusWeeks(9).plusDays(1), fødselsdato.plusWeeks(11), true, false))
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullISøktePerioderOgFastsattePerioderTilAnnenPart(grunnlag);
        assertThat(hull).hasSize(0);
    }

    @Test
    public void helgErIkkeHull() {
        LocalDate fødselsdato = LocalDate.of(2018, 6, 6);
        LocalDate mødrekvoteSlutt = LocalDate.of(2018, 7, 20);
        LocalDate annenPartStart = LocalDate.of(2018, 7, 23);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.create()
                .medSaldo(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, førFødselDager)
                .medSaldo(Stønadskontotype.MØDREKVOTE, mødrekvoteDager)
                .medSaldo(Stønadskontotype.FELLESPERIODE, fellesperiodDedager)
                .medSaldo(Stønadskontotype.FEDREKVOTE, fedrekvoteDager)
                .medFamiliehendelseDato(fødselsdato)
                .medStønadsPeriode(Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(Stønadskontotype.MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, mødrekvoteSlutt, PeriodeVurderingType.PERIODE_OK) // Mødrekvote slutter på en fredag
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart(annenPartStart, annenPartStart.plusWeeks(10), true, false)) // Annen part starter mandagen etter
                .build();

        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullISøktePerioderOgFastsattePerioderTilAnnenPart(grunnlag);
        assertThat(hull).isEmpty();
    }

    @Test
    public void enDagPerioderSkalIkkeGiHull() {
        LukketPeriode førPeriode = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        LukketPeriode enDagPeriode = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));
        LukketPeriode etterPeriode = new LukketPeriode(LocalDate.of(2019, 1, 2), LocalDate.of(2019, 1, 10));
        List<LukketPeriode> perioder = Arrays.asList(førPeriode, enDagPeriode, etterPeriode);
        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullIPerioder(perioder);

        assertThat(hull).isEmpty();
    }

    @Test
    public void skalFinneFlereHull() {
        LukketPeriode periode1 = new LukketPeriode(LocalDate.of(2018, 12, 27), LocalDate.of(2018, 12, 31));
        LukketPeriode periode2 = new LukketPeriode(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2));
        LukketPeriode periode3 = new LukketPeriode(LocalDate.of(2019, 1, 3), LocalDate.of(2019, 1, 10));
        LukketPeriode periode4 = new LukketPeriode(LocalDate.of(2019, 1, 15), LocalDate.of(2019, 1, 20));
        LukketPeriode periode5 = new LukketPeriode(LocalDate.of(2019, 1, 25), LocalDate.of(2019, 1, 30));
        List<LukketPeriode> perioder = Arrays.asList(periode1, periode2, periode3, periode4, periode5);
        List<OppholdPeriode> hull = OppholdPeriodeTjeneste.finnHullIPerioder(perioder);

        assertThat(hull).hasSize(2);
        assertThat(hull.get(0).getFom()).isEqualTo(LocalDate.of(2019, 1, 11));
        assertThat(hull.get(0).getTom()).isEqualTo(LocalDate.of(2019, 1, 14));
        assertThat(hull.get(1).getFom()).isEqualTo(LocalDate.of(2019, 1, 21));
        assertThat(hull.get(1).getTom()).isEqualTo(LocalDate.of(2019, 1, 24));
    }

    /* Hjelpemetoder */
    private LocalDate startForeldrepengerFørFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.minusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_FELLESPERIODE_FØR_FØDSEL_UKER, familiehendelsesDato));
    }

    private LocalDate sluttForeldrepengerFørFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.minusDays(1);
    }

    private LocalDate startMødrekvoteEtterFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato;
    }

    private LocalDate sluttMødrekvoteEtterFødsel(LocalDate familiehendelsesDato) {
        return familiehendelsesDato.plusWeeks(konfigurasjon.getParameter(Parametertype.UTTAK_MØDREKVOTE_ETTER_FØDSEL_UKER, familiehendelsesDato));
    }

}
