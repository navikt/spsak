package no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder;

import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.uttak.InnvilgetÅrsak;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.AnnenAktivitetDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.NæringDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.PeriodeDto;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetType;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.erFomRettEtterTomDato;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.likNæring;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.likOptionalStatusOgEqualsHvisFinnes;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.likeAndreAktiviteter;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.likeArbeidsforhold;
import static no.nav.foreldrepenger.dokumentbestiller.doktype.sammenslåperioder.PeriodeMergerVerktøy.sammeStatusOgÅrsak;
import static org.assertj.core.api.Assertions.assertThat;

public class PeriodeMergerVerktøyTest {


    PeriodeDto periodeEn = new PeriodeDto();
    PeriodeDto periodeTo = new PeriodeDto();
    PeriodeDto periodeTre = new PeriodeDto();
    PeriodeDto periodeFire = new PeriodeDto();
    PeriodeDto periodeFem = new PeriodeDto();

    ArbeidsforholdDto arbeidsforholdDto1 = new ArbeidsforholdDto();
    ArbeidsforholdDto arbeidsforholdDto2 = new ArbeidsforholdDto();

    List<PeriodeDto> periodeListe = new ArrayList<>();

    @Before
    public void setup() {
        settOppDefaultVerdier();
    }

    @Test
    public void skal_kjenne_igjen_sammenhengende_datoer() {
        assertThat(erFomRettEtterTomDato(periodeEn, periodeTo)).isTrue();
        assertThat(erFomRettEtterTomDato(periodeTo, periodeEn)).isFalse();
        assertThat(erFomRettEtterTomDato(periodeEn, periodeEn)).isFalse();
        periodeEn.setPeriodeTom(LocalDate.of(2018, 6, 29).toString());
        assertThat(erFomRettEtterTomDato(periodeEn, periodeTo)).isFalse();
        periodeTo.setPeriodeFom(LocalDate.of(2018, 6, 29).toString());
        assertThat(erFomRettEtterTomDato(periodeEn, periodeTo)).isFalse();
    }


    @Test
    public void skal_finne_samme_årsak_og_status() {
        assertThat(sammeStatusOgÅrsak(periodeEn, periodeTo)).isTrue();
        periodeEn.setInnvilget(false);
        assertThat(sammeStatusOgÅrsak(periodeEn, periodeTo)).isFalse();
        settOppDefaultVerdier();
        periodeTo.setÅrsak(Avslagsårsak.SØKER_ER_IKKE_BARNETS_FAR_O.getKode());
        assertThat(sammeStatusOgÅrsak(periodeEn, periodeTo)).isFalse();
    }

    @Test
    public void skal_sammenligne_optionals() {
        assertThat(likOptionalStatusOgEqualsHvisFinnes(Optional.empty(), Optional.empty())).isTrue();
        assertThat(likOptionalStatusOgEqualsHvisFinnes(Optional.empty(), Optional.of(periodeEn))).isFalse();
        assertThat(likOptionalStatusOgEqualsHvisFinnes(Optional.of(periodeEn), Optional.of(periodeEn))).isTrue();
        assertThat(likOptionalStatusOgEqualsHvisFinnes(Optional.of(arbeidsforholdDto1), Optional.of(arbeidsforholdDto2))).isTrue();
        arbeidsforholdDto1.setGradering(false);
        assertThat(likOptionalStatusOgEqualsHvisFinnes(Optional.of(arbeidsforholdDto1), Optional.of(arbeidsforholdDto2))).isFalse();
    }

    @Test
    public void skal_sammenligne_næring_dto() {
        assertThat(likNæring(periodeEn, periodeTo)).isTrue();
        periodeEn.setNæring(opprettNæringDto());
        assertThat(likNæring(periodeEn, periodeTo)).isFalse();
        periodeTo.setNæring(opprettNæringDto());
        assertThat(likNæring(periodeEn, periodeTo)).isTrue();
    }

    @Test
    public void skal_sammenligne_andre_aktiviteter_dto() {
        assertThat(likeAndreAktiviteter(periodeEn, periodeTo)).isTrue();
        periodeEn.leggTilAnnenAktivitet(opprettAnnenAktivitetDtoMedGradering(false));
        assertThat(likeAndreAktiviteter(periodeEn, periodeTo)).isFalse();
        periodeTo.leggTilAnnenAktivitet(opprettAnnenAktivitetDtoMedGradering(true));
        assertThat(likeAndreAktiviteter(periodeEn, periodeTo)).isFalse();
        periodeTo.leggTilAnnenAktivitet(opprettAnnenAktivitetDtoMedGradering(false));
        periodeEn.leggTilAnnenAktivitet(opprettAnnenAktivitetDtoMedGradering(true));
        assertThat(likeAndreAktiviteter(periodeEn, periodeTo)).isTrue();
    }

    @Test
    public void skal_sammenligne_arbeidsforhold_dto() {
        assertThat(likeArbeidsforhold(periodeEn, periodeTo)).isTrue();
        periodeEn.leggTilArbeidsforhold(arbeidsforholdDto1);
        periodeTo.leggTilArbeidsforhold(arbeidsforholdDto2);
        assertThat(likeArbeidsforhold(periodeEn, periodeTo)).isTrue();
        arbeidsforholdDto1.setGradering(false);
        assertThat(likeArbeidsforhold(periodeEn, periodeTo)).isFalse();
        arbeidsforholdDto1.setGradering(true);
        arbeidsforholdDto1.setStillingsprosent(30);
        assertThat(likeArbeidsforhold(periodeEn, periodeTo)).isFalse();
    }

    private NæringDto opprettNæringDto() {
        NæringDto næringDto = new NæringDto();
        næringDto.setGradering(true);
        næringDto.setInntekt1(100000);
        næringDto.setInntekt2(200000);
        næringDto.setInntekt3(300000);
        næringDto.setProsentArbeid(100);
        næringDto.setSistLignedeÅr(LocalDate.now().getYear() - 1);
        næringDto.setUttaksgrad(100);
        return næringDto;
    }

    private AnnenAktivitetDto opprettAnnenAktivitetDtoMedGradering(boolean gradering) {
        AnnenAktivitetDto dto = new AnnenAktivitetDto();
        dto.setGradering(gradering);
        dto.setAktivitetType(AktivitetType.FRILANS.toString());
        dto.setProsentArbeid(100);
        dto.setUttaksgrad(100);
        return dto;
    }

    private void settOppDefaultVerdier() {
        periodeEn.setPeriodeFom(LocalDate.of(2018, 6, 1).toString());
        periodeEn.setPeriodeTom(LocalDate.of(2018, 6, 30).toString());
        periodeTo.setPeriodeFom(LocalDate.of(2018, 7, 1).toString());
        periodeTo.setPeriodeTom(LocalDate.of(2018, 7, 31).toString());
        periodeEn.setInnvilget(true);
        periodeTo.setInnvilget(true);
        periodeEn.setÅrsak(InnvilgetÅrsak.UTTAK_OPPFYLT.getKode());
        periodeTo.setÅrsak(InnvilgetÅrsak.UTTAK_OPPFYLT.getKode());
        periodeTre.setPeriodeFom(LocalDate.of(2018, 1, 1).toString());
        periodeFire.setPeriodeFom(LocalDate.of(2018, 4, 1).toString());
        periodeFem.setPeriodeFom(LocalDate.of(2018, 9, 1).toString());

        periodeListe.add(periodeEn);
        periodeListe.add(periodeTo);
        periodeListe.add(periodeTre);
        periodeListe.add(periodeFire);
        periodeListe.add(periodeFem);

        arbeidsforholdDto1.setGradering(true);
        arbeidsforholdDto1.setUtbetalingsgrad(80);
        arbeidsforholdDto1.setArbeidsgiverNavn("NAV");
        arbeidsforholdDto1.setStillingsprosent(100);
        arbeidsforholdDto1.setUttaksgrad(100);

        arbeidsforholdDto2.setGradering(true);
        arbeidsforholdDto2.setUtbetalingsgrad(80);
        arbeidsforholdDto2.setArbeidsgiverNavn("NAV");
        arbeidsforholdDto2.setStillingsprosent(100);
        arbeidsforholdDto2.setUttaksgrad(100);
    }

}
