package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsattPeriodeAnnenPart;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Søknadstype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.UttakPeriodeAktivitet;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Virkedager;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class RevurderingTest {

    private static final LocalDate FØRSTE_LOVLIGE_UTTAKSDAG = LocalDate.of(2018, 5, 5);
    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);
    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void revurderingSøknadUtenSamtykkeOgOverlappendePerioderSkalTilManuellBehandling() {
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medSamtykke(false)
                .medUttakPeriodeForAnnenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                    FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, true, false))
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.IKKE_SAMTYKKE);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarInnvilgetUtsettelseSkalAvslås() {
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
            .medSamtykke(true)
            .medUttakPeriodeForAnnenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, true, false))
            .medBehandlingType(Behandlingtype.REVURDERING_BERØRT_SAK)
            .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_UTSETTELSE);
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslås() {
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
            .medSamtykke(true)
            .medUttakPeriodeForAnnenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false, false))
            .medBehandlingType(Behandlingtype.REVURDERING_BERØRT_SAK)
            .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarUtbetalingOver0MenIkkeSamtidigUttakSkalAvslåsOgKnekkes() {
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
            .medSamtykke(true)
            .medUttakPeriodeForAnnenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false, false))
            .medBehandlingType(Behandlingtype.REVURDERING_BERØRT_SAK)
            .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.OPPHOLD_IKKE_SAMTIDIG_UTTAK);
    }

    @Test
    public void revurderingsøknadAvBerørtSakHvorDenAndrePartenHarUtbetalingOver0OgSamtidigUttakSkalAvslås() {
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
            .medSamtykke(true)
            .medUttakPeriodeForAnnenPart(lagPeriode(Stønadskontotype.FELLESPERIODE, FAMILIEHENDELSE_DATO.plusWeeks(10),
                FAMILIEHENDELSE_DATO.plusWeeks(12), BigDecimal.TEN, false, true))
            .medBehandlingType(Behandlingtype.REVURDERING_BERØRT_SAK)
            .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.VURDER_SAMTIDIG_UTTAK);
    }

    private FastsattPeriodeAnnenPart lagPeriode(Stønadskontotype stønadskontotype, LocalDate fom, LocalDate tom,
                                                BigDecimal utbetalingsgrad, boolean innvilgetUtsettelse, boolean samtidigUttak) {
        return new FastsattPeriodeAnnenPart.Builder(fom, tom, samtidigUttak, innvilgetUtsettelse)
            .medUttakPeriodeAktivitet(new UttakPeriodeAktivitet(AktivitetIdentifikator.forArbeid("000000003", null),
                stønadskontotype, Virkedager.beregnAntallVirkedager(fom, tom), utbetalingsgrad))
            .build();
    }

    private FastsettePeriodeGrunnlagBuilder basicBuilder() {
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.annenAktivitet();
        return FastsettePeriodeGrunnlagBuilder.create()
                //.medAktivitetIdentifikator(AktivitetIdentifikator.forSelvstendigNæringsdrivende())
                .medSøknadstype(Søknadstype.FØDSEL)
                .medStønadsPeriode(Stønadskontotype.FELLESPERIODE, PeriodeKilde.SØKNAD, FAMILIEHENDELSE_DATO.plusWeeks(10), FAMILIEHENDELSE_DATO.plusWeeks(12), PeriodeVurderingType.PERIODE_OK)
                .medSaldo(aktivitetIdentifikator, Stønadskontotype.MØDREKVOTE, 50)
                .medSaldo(aktivitetIdentifikator, Stønadskontotype.FELLESPERIODE, 13 * 5)
                .medFørsteLovligeUttaksdag(FØRSTE_LOVLIGE_UTTAKSDAG)
                .medFamiliehendelseDato(FAMILIEHENDELSE_DATO);
    }
}