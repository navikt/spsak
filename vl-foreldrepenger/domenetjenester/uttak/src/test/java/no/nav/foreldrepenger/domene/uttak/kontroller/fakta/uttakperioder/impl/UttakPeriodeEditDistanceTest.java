package no.nav.foreldrepenger.domene.uttak.kontroller.fakta.uttakperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittPeriodeBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.EditDistanceOperasjon;
import no.nav.foreldrepenger.domene.uttak.kontroller.fakta.wagnerfisher.WagnerFisher;

public class UttakPeriodeEditDistanceTest {

    private LocalDate startDato = LocalDate.of(2018, 4, 19);

    @Test
    public void ingenEndringerPåPeriodene() {
        List<UttakPeriodeEditDistance> opprinneligePerioder = opprettUttaksperioder();
        List<UttakPeriodeEditDistance> gjeldendeUttaksperioder = opprettUttaksperioder();

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).isEmpty();
    }

    @Test
    public void avklareDokumentasjonAvUtsettelseErEndring() {
        List<UttakPeriodeEditDistance> opprinneligePerioder = opprettUttaksperioder();
        List<UttakPeriodeEditDistance> gjeldendeUttaksperioder = opprettUttaksperioder();

        gjeldendeUttaksperioder.get(2).setPeriodeErDokumentert(false);

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).hasSize(1);
        assertThat(operasjoner.get(0).erEndreOperasjon()).isTrue();

        gjeldendeUttaksperioder.get(5).setPeriodeErDokumentert(true);
        operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).hasSize(2);
        assertThat(operasjoner.get(0).erEndreOperasjon()).isTrue();
        assertThat(operasjoner.get(1).erEndreOperasjon()).isTrue();
    }

    @Test
    public void utsettelsePgaArbeidErTilpassetInntektsmelding() {
        List<UttakPeriodeEditDistance> opprinneligePerioder = opprettUttaksperioder();
        List<UttakPeriodeEditDistance> gjeldendeUttaksperioder = opprettUttaksperioder();

        //Endrer dato på foregående periode
        gjeldendeUttaksperioder.set(3, UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(12), startDato.plusWeeks(17).minusDays(1))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE).build())
            .build());

        //Endrer dato på periode og setter til dokumentert
        gjeldendeUttaksperioder.set(4, UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(17), startDato.plusWeeks(19).minusDays(1))
            .medPeriodeType(UttakPeriodeType.ANNET)
            .medÅrsak(UtsettelseÅrsak.ARBEID).build())
            .medPeriodeErDokumentert(true)
            .build());

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).hasSize(2);
        assertThat(operasjoner.get(0).erEndreOperasjon()).isTrue();
        assertThat(operasjoner.get(1).erEndreOperasjon()).isTrue();
    }

    @Test
    public void sletterUtsettelsePeriodeOgSetterInnFellesperiode() {
        List<UttakPeriodeEditDistance> opprinneligePerioder = opprettUttaksperioder();
        List<UttakPeriodeEditDistance> gjeldendeUttaksperioder = opprettUttaksperioder();

        //Endrer type på periode og setter til dokumentert
        gjeldendeUttaksperioder.set(4, UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(17), startDato.plusWeeks(19).minusDays(1))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE).build())
            .build());

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).hasSize(2);
        // Utsettelseperiode har blitt slettet
        assertThat(operasjoner.stream()
            .anyMatch(operasjon -> operasjon.erSletteOperasjon() && UtsettelseÅrsak.ARBEID.equals(operasjon.getFør().getPeriode().getÅrsak()))).isTrue();

        // Fellesperiode har blitt lagt til
        assertThat(operasjoner.stream()
            .anyMatch(operasjon -> operasjon.erSettInnOperasjon() && operasjon.getNå().getPeriode().getPeriodeType().equals(UttakPeriodeType.FELLESPERIODE))).isTrue();
    }

    @Test
    public void endrerUttaksperiodetypeFraFellesperiodeTilMødrekvote() {
        List<UttakPeriodeEditDistance> opprinneligePerioder = opprettUttaksperioder();
        List<UttakPeriodeEditDistance> gjeldendeUttaksperioder = opprettUttaksperioder();

        //Endrer type på periode
        gjeldendeUttaksperioder.set(3,
            UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
                .medPeriode(startDato.plusWeeks(12), startDato.plusWeeks(16).minusDays(1))
                .medPeriodeType(UttakPeriodeType.MØDREKVOTE).build())
                .build());

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).hasSize(1);
        assertThat(operasjoner.get(0).erEndreOperasjon()).isTrue();
    }

    @Test
    public void setterInnNyPeriodeOgJustererDatoPåPeriodenEtter() {
        List<UttakPeriodeEditDistance> opprinneligePerioder = opprettUttaksperioder();
        List<UttakPeriodeEditDistance> gjeldendeUttaksperioder = opprettUttaksperioder();

        // Endrer startdato på eksisterende periode
        gjeldendeUttaksperioder.set(3,
            UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
                .medPeriode(startDato.plusWeeks(13), startDato.plusWeeks(16).minusDays(1))
                .medPeriodeType(UttakPeriodeType.FELLESPERIODE).build())
                .build());

        //Setter inn ny periode
        gjeldendeUttaksperioder.add(3,
            UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
                .medPeriode(startDato.plusWeeks(12), startDato.plusWeeks(13).minusDays(1))
                .medPeriodeType(UttakPeriodeType.MØDREKVOTE).build())
                .build());

        gjeldendeUttaksperioder = gjeldendeUttaksperioder.stream().sorted(Comparator.comparing(p -> p.getPeriode().getFom())).collect(Collectors.toList());

        List<EditDistanceOperasjon<UttakPeriodeEditDistance>> operasjoner = WagnerFisher.finnEnklesteSekvens(opprinneligePerioder, gjeldendeUttaksperioder);
        assertThat(operasjoner).hasSize(2);
        assertThat(operasjoner.stream().anyMatch(EditDistanceOperasjon::erSettInnOperasjon)).isTrue();
        assertThat(operasjoner.stream().anyMatch(EditDistanceOperasjon::erEndreOperasjon)).isTrue();
    }


    private List<UttakPeriodeEditDistance> opprettUttaksperioder() {
        List<UttakPeriodeEditDistance> perioder = new ArrayList<>();

        perioder.add(
            UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
                .medPeriode(startDato, startDato.plusWeeks(3).minusDays(1))
                .medPeriodeType(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL).build())
                .build());

        perioder.add(UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(3), startDato.plusWeeks(9).minusDays(1))
            .medPeriodeType(UttakPeriodeType.MØDREKVOTE).build())
            .build());

        perioder.add(UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(9), startDato.plusWeeks(12).minusDays(1))
            .medPeriodeType(UttakPeriodeType.ANNET)
            .medÅrsak(UtsettelseÅrsak.FERIE).build())
            .build());

        perioder.add(UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(12), startDato.plusWeeks(16).minusDays(1))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE).build())
            .build());

        perioder.add(UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(16), startDato.plusWeeks(19).minusDays(1))
            .medPeriodeType(UttakPeriodeType.ANNET)
            .medÅrsak(UtsettelseÅrsak.ARBEID).build())
            .build());

        perioder.add(UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(16), startDato.plusWeeks(19).minusDays(1))
            .medPeriodeType(UttakPeriodeType.ANNET)
            .medÅrsak(UtsettelseÅrsak.INSTITUSJON_SØKER).build())
            .build());

        perioder.add(UttakPeriodeEditDistance.builder(OppgittPeriodeBuilder.ny()
            .medPeriode(startDato.plusWeeks(19), startDato.plusWeeks(25).minusDays(1))
            .medPeriodeType(UttakPeriodeType.FELLESPERIODE).build())
            .build());

        return perioder;
    }

}
