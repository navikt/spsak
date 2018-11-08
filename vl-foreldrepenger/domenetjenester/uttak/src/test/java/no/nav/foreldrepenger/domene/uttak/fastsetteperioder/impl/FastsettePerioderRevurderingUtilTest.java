package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatType;
import no.nav.foreldrepenger.behandlingslager.uttak.PeriodeResultatÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskonto;
import no.nav.foreldrepenger.behandlingslager.uttak.StønadskontoType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakArbeidType;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeAktivitetEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPeriodeEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatPerioderEntitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.TrekkdagerUtregningUtil;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Periode;

public class FastsettePerioderRevurderingUtilTest {

    @Test
    public void skalRedusereStønadskontoerBasertPåOpprinneligUttaksresultatBarePerioderFørEndringsdato() {
        UttakResultatPerioderEntitet opprinneligePerioder = new UttakResultatPerioderEntitet();
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();

        UttakResultatPeriodeEntitet periode1 = new UttakResultatPeriodeEntitet.Builder(LocalDate.of(2018, 6, 6),
            LocalDate.of(2018, 6, 20))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet, periode1, StønadskontoType.MØDREKVOTE, 10);
        UttakResultatPeriodeEntitet periode2 = new UttakResultatPeriodeEntitet.Builder(periode1.getTom().plusDays(1), periode1.getTom().plusWeeks(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet, periode2, StønadskontoType.FELLESPERIODE, 7);
        UttakResultatPeriodeEntitet periode3 = new UttakResultatPeriodeEntitet.Builder(periode2.getTom().plusDays(1), periode2.getTom().plusWeeks(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet, periode3, StønadskontoType.FELLESPERIODE, 7);

        opprinneligePerioder.leggTilPeriode(periode1);
        opprinneligePerioder.leggTilPeriode(periode2);
        opprinneligePerioder.leggTilPeriode(periode3);
        UttakResultatEntitet opprinneligUttak = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligePerioder)
            .build();

        LocalDate endringsdato = periode3.getFom();

        Set<Stønadskonto> originaleStønadskontoer = new HashSet<>();
        originaleStønadskontoer.add(new Stønadskonto.Builder().medStønadskontoType(StønadskontoType.MØDREKVOTE).medMaxDager(12).build());
        originaleStønadskontoer.add(new Stønadskonto.Builder().medStønadskontoType(StønadskontoType.FELLESPERIODE).medMaxDager(12).build());
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forFrilans();
        Set<Stønadskonto> resultat = FastsettePerioderRevurderingUtil.reduserteStønadskontoer(opprinneligUttak, endringsdato,
            originaleStønadskontoer, aktivitetIdentifikator);

        assertThat(stønadskontoType(resultat, StønadskontoType.MØDREKVOTE).getMaxDager()).isEqualTo(12 - 10);
        assertThat(stønadskontoType(resultat, StønadskontoType.FELLESPERIODE).getMaxDager()).isEqualTo(12 - 7);
    }

    @Test
    public void skalRedusereStønadskontoerBasertPåOpprinneligUttaksresultatBareTelleForAkrivitet() {
        UttakResultatPerioderEntitet opprinneligePerioder = new UttakResultatPerioderEntitet();
        UttakAktivitetEntitet uttakAktivitet1 = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();
        UttakAktivitetEntitet uttakAktivitet2 = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.SELVSTENDIG_NÆRINGSDRIVENDE)
            .build();

        UttakResultatPeriodeEntitet periode1 = new UttakResultatPeriodeEntitet.Builder(LocalDate.of(2018, 6, 6),
            LocalDate.of(2018, 6, 20))
            .medPeriodeResultat(PeriodeResultatType.AVSLÅTT, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet1, periode1, StønadskontoType.MØDREKVOTE, 12);
        leggTilPeriodeAktivitet(uttakAktivitet2, periode1, StønadskontoType.MØDREKVOTE, 5);
        UttakResultatPeriodeEntitet periode2 = new UttakResultatPeriodeEntitet.Builder(periode1.getTom().plusDays(1), periode1.getTom().plusWeeks(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet1, periode2, StønadskontoType.MØDREKVOTE, 12);
        leggTilPeriodeAktivitet(uttakAktivitet2, periode2, StønadskontoType.MØDREKVOTE, 12);

        opprinneligePerioder.leggTilPeriode(periode1);
        opprinneligePerioder.leggTilPeriode(periode2);
        UttakResultatEntitet opprinneligUttak = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligePerioder)
            .build();

        LocalDate endringsdato = periode2.getFom();

        Set<Stønadskonto> originaleStønadskontoer = new HashSet<>();
        originaleStønadskontoer.add(new Stønadskonto.Builder().medStønadskontoType(StønadskontoType.MØDREKVOTE).medMaxDager(12).build());
        originaleStønadskontoer.add(new Stønadskonto.Builder().medStønadskontoType(StønadskontoType.FELLESPERIODE).medMaxDager(12).build());
        AktivitetIdentifikator aktivitetIdentifikator1 = AktivitetIdentifikator.forFrilans();
        Set<Stønadskonto> resultat1 = FastsettePerioderRevurderingUtil.reduserteStønadskontoer(opprinneligUttak, endringsdato,
            originaleStønadskontoer, aktivitetIdentifikator1);
        AktivitetIdentifikator aktivitetIdentifikator2 = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        Set<Stønadskonto> resultat2 = FastsettePerioderRevurderingUtil.reduserteStønadskontoer(opprinneligUttak, endringsdato,
            originaleStønadskontoer, aktivitetIdentifikator2);

        assertThat(stønadskontoType(resultat1, StønadskontoType.MØDREKVOTE).getMaxDager()).isEqualTo(12 - 12);
        assertThat(stønadskontoType(resultat2, StønadskontoType.MØDREKVOTE).getMaxDager()).isEqualTo(12 - 5);
    }

    @Test
    public void skalSplitteOppUttaksresultatPeriodeHvisEndringsdatoErIPerioden() {
        UttakResultatPerioderEntitet opprinneligePerioder = new UttakResultatPerioderEntitet();
        UttakAktivitetEntitet uttakAktivitet = new UttakAktivitetEntitet.Builder()
            .medUttakArbeidType(UttakArbeidType.FRILANS)
            .build();

        UttakResultatPeriodeEntitet periode1 = new UttakResultatPeriodeEntitet.Builder(LocalDate.of(2018, 6, 6),
            LocalDate.of(2018, 6, 20))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet, periode1, StønadskontoType.MØDREKVOTE, 10);
        UttakResultatPeriodeEntitet periode2 = new UttakResultatPeriodeEntitet.Builder(periode1.getTom().plusDays(1), periode1.getTom().plusWeeks(1))
            .medPeriodeResultat(PeriodeResultatType.INNVILGET, PeriodeResultatÅrsak.UKJENT)
            .build();
        leggTilPeriodeAktivitet(uttakAktivitet, periode2, StønadskontoType.FELLESPERIODE, 10);

        opprinneligePerioder.leggTilPeriode(periode1);
        opprinneligePerioder.leggTilPeriode(periode2);
        UttakResultatEntitet opprinneligUttak = new UttakResultatEntitet.Builder(mock(Behandlingsresultat.class))
            .medOpprinneligPerioder(opprinneligePerioder)
            .build();

        LocalDate endringsdato = periode2.getFom().plusDays(2);
        List<UttakResultatPeriodeEntitet> perioder = FastsettePerioderRevurderingUtil.perioderFørEndringsdato(opprinneligUttak, endringsdato);

        assertThat(perioder).hasSize(2);
        assertThat(perioder.get(0).getFom()).isEqualTo(periode1.getFom());
        assertThat(perioder.get(0).getTom()).isEqualTo(periode1.getTom());
        assertThat(perioder.get(0).getAktiviteter().get(0).getTrekkdager()).isEqualTo(periode1.getAktiviteter().get(0).getTrekkdager());
        assertThat(perioder.get(1).getFom()).isEqualTo(periode2.getFom());
        assertThat(perioder.get(1).getTom()).isEqualTo(endringsdato.minusDays(1));
        int forventetTrekkdagerSplittetPeriode = TrekkdagerUtregningUtil.trekkdagerFor(
            new Periode(perioder.get(1).getFom(), perioder.get(1).getTom()), false, true, BigDecimal.ZERO,
            false
        );
        assertThat(perioder.get(1).getAktiviteter().get(0).getTrekkdager()).isEqualTo(forventetTrekkdagerSplittetPeriode);
    }

    private Stønadskonto stønadskontoType(Set<Stønadskonto> resultat, StønadskontoType stønadskontotype) {
        return resultat.stream()
            .filter(stønadskonto -> stønadskonto.getStønadskontoType().equals(stønadskontotype))
            .findFirst()
            .get();
    }

    private void leggTilPeriodeAktivitet(UttakAktivitetEntitet uttakAktivitet, UttakResultatPeriodeEntitet periode, StønadskontoType stønadskontoType, int trekkdager) {
        UttakResultatPeriodeAktivitetEntitet periodeAktivitet1 = new UttakResultatPeriodeAktivitetEntitet.Builder(periode, uttakAktivitet)
            .medTrekkonto(stønadskontoType)
            .medTrekkdager(trekkdager)
            .medArbeidsprosent(BigDecimal.TEN)
            .build();
        periode.leggTilAktivitet(periodeAktivitet1);
    }

}
