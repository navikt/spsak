package no.nav.foreldrepenger.uttaksvilkår;

import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.INNVILGET;
import static no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype.MANUELL_BEHANDLING;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FEDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FELLESPERIODE;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FLERBARNSDAGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.FORELDREPENGER_FØR_FØDSEL;
import static no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype.MØDREKVOTE;
import static no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder.ARBEIDSFORHOLD_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.OverføringÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedFulltArbeid;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeMedInnleggelse;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Perioderesultattype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Periodetype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.StønadsPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriode;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.grunnlag.FastsettePeriodeGrunnlagTestBuilder;

public class FastsettePerioderRegelOrkestreringTest extends FastsettePerioderRegelOrkestreringTestBase {

    @Test
    public void skal_innvilge_to_perioder_med_med_mødrekvote_på_under_10_uker() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        assertThat(resultat).hasSize(3);
        resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .forEach(uttakPeriode -> assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET));
    }

    @Test
    public void skal_knekke_mødrekvote_dersom_det_ikke_er_flere_dager_igjen() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 3);
        grunnlag
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(12).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());

        List<UttakPeriode> uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);
        /* Innvilget foreldrepenger før fødsel*/
        assertThat(uttakPerioder.get(0).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(0).getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(uttakPerioder.get(0).getTom()).isEqualTo(fødselsdato.minusDays(1));

        /* Innvilget mødrekvote etter fødsel frem til og med uke 6*/
        assertThat(uttakPerioder.get(1).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(1).getFom()).isEqualTo(fødselsdato);
        assertThat(uttakPerioder.get(1).getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));

        /* Innvilget mødrekvote etter fødsel, etter uke 6 */
        assertThat(uttakPerioder.get(2).getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(uttakPerioder.get(2).getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPerioder.get(2).getTom()).isEqualTo(fødselsdato.plusWeeks(10).minusDays(1));

        /* Avslått mødrekvote, ikke nok dager */
        assertThat(uttakPerioder.get(3).getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(uttakPerioder.get(3).getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(uttakPerioder.get(3).getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(uttakPerioder.get(3).getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
    }

    @Test
    public void mødrekvoteMedUtsattOppstartUtenGyldigGrunnSkalTrekkeDagerPåSaldo() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 8);
        LocalDate sluttGyldigUtsattPeriode = fødselsdato.plusDays(6);
        LocalDate startUgyldigPeriode = fødselsdato.plusDays(7);
        LocalDate sluttUgyldigPeriode = startUgyldigPeriode.plusDays(6);

        grunnlag
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medSøknadstype(Søknadstype.FØDSEL)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medFamiliehendelseDato(fødselsdato)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, sluttUgyldigPeriode.plusDays(1), sluttUgyldigPeriode.plusWeeks(10), PeriodeVurderingType.PERIODE_OK)
                .medGyldigGrunnForTidligOppstartPeriode(fødselsdato, sluttGyldigUtsattPeriode);

        FastsettePeriodeGrunnlag fastsettePeriodeGrunnlag = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(fastsettePeriodeGrunnlag);
        List<UttakPeriode> uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());
        assertThat(uttakPerioder).hasSize(5);

        /* FPFF blir innvilget. */
        UttakPeriode foreldrepengerFørFødselPeriode = uttakPerioder.get(0);
        assertThat(foreldrepengerFørFødselPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(foreldrepengerFørFødselPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(foreldrepengerFørFødselPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(foreldrepengerFørFødselPeriode.getStønadskontotype()).isEqualTo(FORELDREPENGER_FØR_FØDSEL);


        /* Første del av opphold-perioden er gyldig utsettelse, men skal likevel behandles manuelt. */
        UttakPeriode gyldigUtsettelsePeriode = uttakPerioder.get(1);
        assertThat(gyldigUtsettelsePeriode.getTom()).isEqualTo(sluttGyldigUtsattPeriode);
        assertThat(gyldigUtsettelsePeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(gyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(gyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(gyldigUtsettelsePeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MANGLENDE_SØKT_PERIODE);

        /* Resten av periodene må også behandles manuelt, siden periode med gyldig utsettelse skal behandles manuelt. */

        UttakPeriode ugyldigUtsettelsePeriode = uttakPerioder.get(2);
        assertThat(ugyldigUtsettelsePeriode.getFom()).isEqualTo(sluttGyldigUtsattPeriode.plusDays(1));
        assertThat(ugyldigUtsettelsePeriode.getTom()).isEqualTo(sluttUgyldigPeriode);
        assertThat(ugyldigUtsettelsePeriode.getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(ugyldigUtsettelsePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(ugyldigUtsettelsePeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(ugyldigUtsettelsePeriode.getManuellbehandlingårsak()).isNull();

        /* Splittes ved knekkpunkt ved 6 uker pga regelflyt */
        UttakPeriode uttakPeriode1 = uttakPerioder.get(3);
        assertThat(uttakPeriode1.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode1.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(uttakPeriode1.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode1.getFom()).isEqualTo(sluttUgyldigPeriode.plusDays(1));
        assertThat(uttakPeriode1.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(uttakPeriode1.getManuellbehandlingårsak()).isNull();

        UttakPeriode uttakPeriode2 = uttakPerioder.get(4);
        assertThat(uttakPeriode2.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(uttakPeriode2.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(uttakPeriode2.getStønadskontotype()).isEqualTo(MØDREKVOTE);
        assertThat(uttakPeriode2.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(uttakPeriode2.getTom()).isEqualTo(sluttUgyldigPeriode.plusWeeks(10));
        assertThat(uttakPeriode2.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void delvisUgyldigUtsattMødrekvote() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 8);
        LocalDate gyldigUtsettelseStart = fødselsdato.plusDays(5);
        LocalDate gyldigUtsettelseSlutt = fødselsdato.plusDays(10);

        grunnlag
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medSøknadstype(Søknadstype.FØDSEL)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medFamiliehendelseDato(fødselsdato)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, gyldigUtsettelseSlutt.plusDays(1), fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medGyldigGrunnForTidligOppstartPeriode(gyldigUtsettelseStart, gyldigUtsettelseSlutt);

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        List<UttakPeriode> uttakPerioder = resultat.stream()
                .map(FastsettePeriodeResultat::getUttakPeriode)
                .collect(Collectors.toList());

        assertThat(uttakPerioder).hasSize(4);

        // Første del av opphold-perioden blir manuell behandling
        UttakPeriode ugyldigUtsattPeriode = uttakPerioder.get(1);
        assertThat(ugyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(ugyldigUtsattPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.MANGLENDE_SØKT_PERIODE);
        assertThat(ugyldigUtsattPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(ugyldigUtsattPeriode.getTom()).isEqualTo(fødselsdato.plusDays(4));
        assertThat(ugyldigUtsattPeriode.getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(ugyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        // Andre del av opphold-perioden blir manuell behandling fordi tidligere periode ble manuell
        UttakPeriode gyldigUtsattPeriode = uttakPerioder.get(2);
        assertThat(gyldigUtsattPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(gyldigUtsattPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(gyldigUtsattPeriode.getFom()).isEqualTo(gyldigUtsettelseStart);
        assertThat(gyldigUtsattPeriode.getTom()).isEqualTo(gyldigUtsettelseSlutt);
        assertThat(gyldigUtsattPeriode.getPeriodetype()).isEqualTo(Periodetype.OPPHOLD);
        assertThat(gyldigUtsattPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);

        // Uttak av mødrekvote blir manuell behandling fordi tidligere periode ble manuell
        UttakPeriode innvilgetUttakPeriode = uttakPerioder.get(3);
        assertThat(innvilgetUttakPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(innvilgetUttakPeriode.getManuellbehandlingårsak()).isNull();
        assertThat(innvilgetUttakPeriode.getFom()).isEqualTo(gyldigUtsettelseSlutt.plusDays(1));
        assertThat(innvilgetUttakPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(innvilgetUttakPeriode.getPeriodetype()).isEqualTo(Periodetype.STØNADSPERIODE);
        assertThat(innvilgetUttakPeriode.getStønadskontotype()).isEqualTo(MØDREKVOTE);
    }

    @Test
    public void helePeriodenUtenforSøknadsfrist() {
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        grunnlag
                .medFørsteLovligeUttaksdag(fødselsdato.plusWeeks(6))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        // Act
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());


        // Assert
        assertThat(resultat).hasSize(2);

        Optional<UttakPeriode> førFødsel = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> FORELDREPENGER_FØR_FØDSEL.equals(p.getStønadskontotype())).findFirst();
        assertThat(førFødsel).isPresent();
        assertThat(førFødsel.get().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(førFødsel.get().getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);


        Optional<UttakPeriode> mødrekvote = resultat.stream().map(FastsettePeriodeResultat::getUttakPeriode)
                .filter(p -> MØDREKVOTE.equals(p.getStønadskontotype())).findFirst();
        assertThat(mødrekvote).isPresent();
        assertThat(mødrekvote.get().getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(mødrekvote.get().getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void skalKnekkePeriodenVedGrenseForSøknadsfrist() {
        LocalDate fødselsdato = LocalDate.of(2017, 11, 1);
        LocalDate lovligeUttaksdag = fødselsdato.plusWeeks(1);
        grunnlag
                .medFørsteLovligeUttaksdag(lovligeUttaksdag)
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        // Act
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());


        // Assert
        assertThat(resultat).hasSize(3);

        UttakPeriode førFødsel = resultat.get(0).getUttakPeriode();
        assertThat(førFødsel.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(førFødsel.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);


        UttakPeriode mødrekvoteFørKnekk = resultat.get(1).getUttakPeriode();
        assertThat(mødrekvoteFørKnekk.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(mødrekvoteFørKnekk.getFom()).isEqualTo(fødselsdato);
        assertThat(mødrekvoteFørKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(1).minusDays(1));
        assertThat(mødrekvoteFørKnekk.getManuellbehandlingårsak()).isNull();

        UttakPeriode mødrekvoteEtterKnekk = resultat.get(2).getUttakPeriode();
        assertThat(mødrekvoteEtterKnekk.getPerioderesultattype()).isEqualTo(MANUELL_BEHANDLING);
        assertThat(mødrekvoteEtterKnekk.getFom()).isEqualTo(fødselsdato.plusWeeks(1));
        assertThat(mødrekvoteEtterKnekk.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(mødrekvoteEtterKnekk.getManuellbehandlingårsak()).isNull();
    }

    @Test
    public void skal_ikke_innvilge_etter_eller_på_barnets_3årsdag_selv_om_det_er_nok_på_saldoen() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusYears(4), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(MØDREKVOTE, 10000000)
                .medSaldo(FEDREKVOTE, 0)
                .medSaldo(FELLESPERIODE, 0);

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag.build());
        assertThat(resultat).hasSize(4);

        //3 uker før fødsel - innvilges
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode frem til 3-årsdag (eksklusiv) innvilges
        //periode knekkes alltid knekt ved 6 uker pga regelflyt
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(3).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //resten av søknadsperide avslås
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(3));
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(4));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }

    @Test
    public void skal_ikke_innvilge_periode_som_starter_på_barnets_3årsdag_eller_som_starter_senere() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(LocalDate.of(2017, 10, 1))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusYears(3).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusYears(3), fødselsdato.plusYears(4).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusYears(4), fødselsdato.plusYears(5), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(MØDREKVOTE, 10000000)
                .medSaldo(FEDREKVOTE, 0)
                .medSaldo(FELLESPERIODE, 10000)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(resultat).hasSize(5);

        //3 uker før fødsel - innvilges
        assertThat(resultat.get(0).getUttakPeriode().getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(resultat.get(0).getUttakPeriode().getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(resultat.get(0).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode frem til 3-årsdag (eksklusiv) innvilges
        //periode knekkes alltid knekt ved 6 uker pga regelflyt
        assertThat(resultat.get(1).getUttakPeriode().getFom()).isEqualTo(fødselsdato);
        assertThat(resultat.get(1).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(resultat.get(1).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(resultat.get(2).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(resultat.get(2).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(3).minusDays(1));
        assertThat(resultat.get(2).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //periode som starter på 3årsdag avslås
        assertThat(resultat.get(3).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(3));
        assertThat(resultat.get(3).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(4).minusDays(1));
        assertThat(resultat.get(3).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);

        //periode som starter etter 3årsdag avslås
        assertThat(resultat.get(4).getUttakPeriode().getFom()).isEqualTo(fødselsdato.plusYears(4));
        assertThat(resultat.get(4).getUttakPeriode().getTom()).isEqualTo(fødselsdato.plusYears(5));
        assertThat(resultat.get(4).getUttakPeriode().getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
    }


    @Test
    public void skal_avslå_første_del_pga_manglende_omsorg_og_andre_del_pga_tom_på_kvote_siden_dager_trekkes() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(12).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(FEDREKVOTE, 5)
                .medPeriodeUtenOmsorg(fødselsdato, fødselsdato.plusYears(1))
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);

        assertThat(resultat).hasSize(1);

        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(10));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(12).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKER_HAR_IKKE_OMSORG);
        assertThat(stønadsPeriode.getMinimumTrekkdager()).isEqualTo(10);
        assertThat(stønadsPeriode.getMaksimumTrekkdager()).isEqualTo(10);
    }

    @Test
    public void hele_perioden_skal_sendes_til_manuell_behandling_når_det_er_søkt_for_sent() throws Exception {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(fødselsdato.plusYears(2))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusYears(1), fødselsdato.plusYears(1).plusWeeks(6), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(FEDREKVOTE, 15)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(resultat).hasSize(1);

        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusYears(1));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusYears(1).plusWeeks(6));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
    }

    @Test
    public void perioder_som_mangler_konto_skal_avklares_manuelt() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        FastsettePeriodeGrunnlag grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal()
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medPeriodeUtenOmsorg(fødselsdato.minusYears(1), fødselsdato.plusYears(3))
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(FORELDREPENGER, 100)
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(grunnlag);
        assertThat(resultat).hasSize(2);

        //første 3 uker - søkt for sent - trekker dager
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.minusWeeks(3));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);

        //resterende uker - søkt for sent og tomt på konto - håndres senere
        uttakPeriode = resultat.get(1).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato);
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(6).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(stønadsPeriode.getAvkortingårsaktype()).isNull(); //null nå, skal kanskje få egen type senere
    }

    @Test
    public void skal_sette_arbeidsprosent() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8).minusDays(1), BigDecimal.valueOf(44));
        grunnlag
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(MØDREKVOTE, 50)
                .medSaldo(FEDREKVOTE, 50)
                .medSaldo(FELLESPERIODE, 130)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(7), fødselsdato.plusWeeks(8).minusDays(1), PeriodeVurderingType.PERIODE_OK);


        FastsettePeriodeGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build);

        assertThat(resultat).hasSize(1);
        UttakPeriode uttakPeriode = resultat.get(0).getUttakPeriode();
        assertThat(uttakPeriode.getPerioderesultattype()).isEqualTo(INNVILGET);

        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getProsentArbeid(ARBEIDSFORHOLD_1)).isEqualTo(BigDecimal.valueOf(44));
        assertThat(stønadsPeriode.getMinimumTrekkdager()).isEqualTo(5); //trekke fulle dager siden periode ikke er gradert
        assertThat(stønadsPeriode.getMaksimumTrekkdager()).isEqualTo(5); //trekke fulle dager siden periode ikke er gradert
    }

    @Test
    public void søkt_gradering_men_arbeid_mer_enn_100_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = FastsettePeriodeGrunnlagTestBuilder.enGraderingsperiode(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), BigDecimal.valueOf(44));
        grunnlag
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(MØDREKVOTE, 50)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medPeriodeMedFulltArbeid(new PeriodeMedFulltArbeid(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1)))
                .medGradertStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), Collections.singletonList(ARBEIDSFORHOLD_1), BigDecimal.valueOf(44), PeriodeVurderingType.PERIODE_OK, false, false);

        FastsettePeriodeGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build);

        UttakPeriode uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(8).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AVKLAR_ARBEID);

    }

    @Test
    public void søkt_ikke_gradering_men_arbeid_mer_enn_0_til_manuell_behandling() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal();
        grunnlag
                .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
                .medSaldo(MØDREKVOTE, 50)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
                .medPeriodeMedArbeid(new PeriodeMedArbeid(fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1)))
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        FastsettePeriodeGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build);

        UttakPeriode uttakPeriode = resultat.get(2).getUttakPeriode();
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(stønadsPeriode.getTom()).isEqualTo(fødselsdato.plusWeeks(8).minusDays(1));
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.AVKLAR_ARBEID);

    }

    @Test
    public void skal_knekke_periode_og_gå_til_manuell_behandling_ved_ikke_nok_flerbarnsdager() {
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forArbeid("1234", "12345");

        LocalDate fødselsdato = LocalDate.of(2018, 3, 13);
        LocalDate tom = Virkedager.plusVirkedager(fødselsdato.plusWeeks(6), 5);
        FastsettePeriodeGrunnlag periodeGrunnlag = this.grunnlag
                .medSaldo(FLERBARNSDAGER, 5)
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(false)
                .medFarRett(true)
                .medMorRett(true)
                .medSamtykke(true)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), tom, PeriodeVurderingType.PERIODE_OK, true, true)
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart.Builder(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), true, false)
                        .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15, BigDecimal.TEN))
                        .build())
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart.Builder(fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), true, false)
                        .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.MØDREKVOTE, 30, BigDecimal.TEN))
                        .build())
                .medUttakPeriodeForAnnenPart(new FastsattPeriodeAnnenPart.Builder(fødselsdato.plusWeeks(6), tom, true, false)
                        .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(aktivitetIdentifikator, Stønadskontotype.FELLESPERIODE,
                                Virkedager.beregnAntallVirkedager(fødselsdato.plusWeeks(6), tom), BigDecimal.TEN))
                        .build())
                .build();

        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(periodeGrunnlag);
        assertThat(resultat).hasSize(2);
        UttakPeriode førstePeriode = resultat.get(0).getUttakPeriode();
        assertThat(førstePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.INNVILGET);
        assertThat(førstePeriode.getFom()).isEqualTo(fødselsdato.plusWeeks(6));
        assertThat(førstePeriode.getTom()).isEqualTo(tom.minusDays(1));

        UttakPeriode andrePeriode = resultat.get(1).getUttakPeriode();
        assertThat(andrePeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(andrePeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(andrePeriode.getFom()).isEqualTo(tom);
        assertThat(andrePeriode.getTom()).isEqualTo(tom);
    }

    @Test
    public void skalIkkeKasteExceptionVedUtsettelseFraDerSaldoGårUt() {
        LocalDate fødselsdato = LocalDate.of(2018, 8, 20);
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.annenAktivitet();
        FastsettePeriodeGrunnlagBuilder builder = FastsettePeriodeGrunnlagBuilder.create()
                .medAktivitetIdentifikator(aktivitetIdentifikator)
                .medArbeid(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())
                .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
                .medFamiliehendelseDato(fødselsdato)
                .medSøkerMor(true)
                .medSamtykke(true)
                .medSaldo(aktivitetIdentifikator, Stønadskontotype.MØDREKVOTE, 75)
                .medSaldo(aktivitetIdentifikator, Stønadskontotype.FORELDREPENGER_FØR_FØDSEL, 15)
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, LocalDate.of(2018, 7, 30), LocalDate.of(2018, 8, 19),
                        PeriodeVurderingType.PERIODE_OK)
                .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 8, 20), LocalDate.of(2018, 12, 2), PeriodeVurderingType.PERIODE_OK)
                .medUtsettelsePeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31),
                        Utsettelseårsaktype.INNLAGT_HELSEINSTITUSJON, PeriodeVurderingType.PERIODE_OK, false, false)
                .medPeriodeMedInnleggelse(new PeriodeMedInnleggelse(LocalDate.of(2018, 12, 3), LocalDate.of(2018, 12, 31)));

        assertThatCode(() -> fastsettePerioderRegelOrkestrering.fastsettePerioder(builder.build())).doesNotThrowAnyException();
    }

    @Test
    public void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_mor() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal();
        grunnlag
            .medSaldo(FORELDREPENGER_FØR_FØDSEL, 15)
            .medSaldo(MØDREKVOTE, 50)
            .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(true)
            .medSamtykke(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FORELDREPENGER_FØR_FØDSEL, PeriodeKilde.SØKNAD, fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato, fødselsdato.plusWeeks(6).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(8).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(8), fødselsdato.plusWeeks(13).minusDays(1), PeriodeVurderingType.PERIODE_OK);


        FastsettePeriodeGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build);

        UttakPeriode uttakPeriode = resultat.get(4).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.AVSLÅTT);
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_gå_til_avslag_når_søker_er_tom_for_sine_konto_far() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal();
        grunnlag
            .medSaldo(FEDREKVOTE, 15)
            .medSaldo(FELLESPERIODE, 50)
            .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medSamtykke(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(10).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(20).minusDays(1), PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(FELLESPERIODE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(20), fødselsdato.plusWeeks(21).minusDays(1), PeriodeVurderingType.PERIODE_OK);


        FastsettePeriodeGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build);

        UttakPeriode uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til avslag
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.FEDREKVOTE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.STØNADSKONTO_TOM);
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_STØNADSDAGER_IGJEN);
    }

    @Test
    public void skal_gå_til_manuell_behandling_når_søker_er_tom_for_sine_konto_men_søkt_om_overføring() {
        LocalDate fødselsdato = LocalDate.of(2018, 1, 1);
        grunnlag = FastsettePeriodeGrunnlagTestBuilder.normal();
        grunnlag
            .medSaldo(FEDREKVOTE, 15)
            .medFørsteLovligeUttaksdag(førsteLovligeUttaksdag(fødselsdato))
            .medFamiliehendelseDato(fødselsdato)
            .medSøkerMor(false)
            .medSamtykke(true)
            .medMorRett(true)
            .medFarRett(true)
            .medSøknadstype(Søknadstype.FØDSEL)
            .medOverføringAvKvote(MØDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(9), fødselsdato.plusWeeks(11).minusDays(1), OverføringÅrsak.SYKDOM_ELLER_SKADE, PeriodeVurderingType.PERIODE_OK)
            .medStønadsPeriode(FEDREKVOTE, PeriodeKilde.SØKNAD, fødselsdato.plusWeeks(6), fødselsdato.plusWeeks(9).minusDays(1), PeriodeVurderingType.PERIODE_OK);

        FastsettePeriodeGrunnlag build = grunnlag.build();
        List<FastsettePeriodeResultat> resultat = fastsettePerioderRegelOrkestrering.fastsettePerioder(build);

        UttakPeriode uttakPeriode = resultat.get(1).getUttakPeriode(); // henter ut den siste perioden, som skal gå til manuell behandling
        assertTrue(uttakPeriode instanceof StønadsPeriode);
        StønadsPeriode stønadsPeriode = (StønadsPeriode) uttakPeriode;
        assertThat(stønadsPeriode.getStønadskontotype()).isEqualTo(Stønadskontotype.MØDREKVOTE);
        assertThat(stønadsPeriode.getPerioderesultattype()).isEqualTo(Perioderesultattype.MANUELL_BEHANDLING);
        assertThat(stønadsPeriode.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_OVERFØRING);
        assertThat(stønadsPeriode.getÅrsak()).isEqualTo(IkkeOppfyltÅrsak.AKTIVITETSKRAVET);
    }

}
