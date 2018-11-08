package no.nav.foreldrepenger.regler.uttak.fastsetteperiode;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.Test;

import no.nav.foreldrepenger.regler.uttak.Regelresultat;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.AktivitetIdentifikator;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.ArbeidTidslinje;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Behandlingtype;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlag;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.FastsettePeriodeGrunnlagBuilder;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Manuellbehandlingårsak;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeKilde;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.PeriodeVurderingType;
import no.nav.foreldrepenger.regler.uttak.fastsetteperiode.grunnlag.Utsettelseårsaktype;
import no.nav.foreldrepenger.regler.uttak.felles.grunnlag.Stønadskontotype;
import no.nav.foreldrepenger.regler.uttak.konfig.StandardKonfigurasjon;

public class EndringssøknadRegelTest {

    private static final LocalDate FØRSTE_LOVLIGE_UTTAKSDAG = LocalDate.of(2018, 5, 5);
    private static final LocalDate FAMILIEHENDELSE_DATO = LocalDate.of(2018, 9, 9);
    private FastsettePeriodeRegel regel = new FastsettePeriodeRegel(StandardKonfigurasjon.KONFIGURASJON);

    @Test
    public void endringssøknadMottattdatoFørGradertPeriodeBlirSendtManuellBehandling() {
        LocalDate endringssøknadMottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE,
                        PeriodeKilde.SØKNAD,
                        endringssøknadMottattdato.minusWeeks(1),
                        endringssøknadMottattdato,
                        Collections.singletonList(AktivitetIdentifikator.forSelvstendigNæringsdrivende()),
                        BigDecimal.valueOf(30),
                        PeriodeVurderingType.PERIODE_OK)
                .medEndringssøknadMottattdato(endringssøknadMottattdato)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isNull();
        assertThat(regelresultat.getManuellbehandlingårsak()).isEqualTo(Manuellbehandlingårsak.SØKNADSFRIST);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void endringssøknadMottattdatoEtterGradertPeriodeBlirInnvilget() {
        LocalDate endringssøknadMottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medGradertStønadsPeriode(Stønadskontotype.MØDREKVOTE,
                        PeriodeKilde.SØKNAD,
                        endringssøknadMottattdato.plusDays(1),
                        endringssøknadMottattdato.plusWeeks(1),
                        Collections.singletonList(AktivitetIdentifikator.forSelvstendigNæringsdrivende()),
                        BigDecimal.valueOf(30),
                        PeriodeVurderingType.PERIODE_OK)
                .medEndringssøknadMottattdato(endringssøknadMottattdato)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_GRADERING_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void endringssøknadMottattdatoFørUtsettelseFeriePeriodeBlirAvslått() {
        LocalDate endringssøknadMottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE,
                        PeriodeKilde.SØKNAD,
                        endringssøknadMottattdato.minusWeeks(1),
                        endringssøknadMottattdato,
                        Utsettelseårsaktype.FERIE,
                        PeriodeVurderingType.PERIODE_OK)
                .medEndringssøknadMottattdato(endringssøknadMottattdato)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void endringssøknadMottattdatoEtterUtsettelseFeriePeriodeBlirInnvilget() {
        LocalDate endringssøknadMottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE,
                        PeriodeKilde.SØKNAD,
                        endringssøknadMottattdato.plusDays(1),
                        endringssøknadMottattdato.plusWeeks(1),
                        Utsettelseårsaktype.FERIE,
                        PeriodeVurderingType.PERIODE_OK)
                .medEndringssøknadMottattdato(endringssøknadMottattdato)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_FERIE_ETTER_PERIODEN_HAR_BEGYNT);
    }

    @Test
    public void endringssøknadMottattdatoFørUtsettelseArbeidPeriodeBlirAvslått() {
        LocalDate endringssøknadMottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE,
                        PeriodeKilde.SØKNAD,
                        endringssøknadMottattdato.minusWeeks(1),
                        endringssøknadMottattdato,
                        Utsettelseårsaktype.ARBEID,
                        PeriodeVurderingType.PERIODE_OK)
                .medEndringssøknadMottattdato(endringssøknadMottattdato)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
        assertThat(regelresultat.oppfylt()).isFalse();
        assertThat(regelresultat.isUtbetal()).isFalse();
        assertThat(regelresultat.isTrekkDagerFraSaldo()).isTrue();
    }

    @Test
    public void endringssøknadMottattdatoEtterUtsettelseArbeidPeriodeBlirInnvilget() {
        LocalDate endringssøknadMottattdato = LocalDate.of(2018, 10, 10);
        FastsettePeriodeGrunnlag grunnlag = basicBuilder()
                .medUtsettelsePeriode(Stønadskontotype.MØDREKVOTE,
                        PeriodeKilde.SØKNAD,
                        endringssøknadMottattdato.plusDays(1),
                        endringssøknadMottattdato.plusWeeks(1),
                        Utsettelseårsaktype.ARBEID,
                        PeriodeVurderingType.PERIODE_OK)
                .medEndringssøknadMottattdato(endringssøknadMottattdato)
                .build();

        Regelresultat regelresultat = new Regelresultat(regel.evaluer(grunnlag));

        assertThat(regelresultat.getAvklaringÅrsak()).isNotEqualTo(IkkeOppfyltÅrsak.SØKT_UTSETTELSE_ARBEID_ETTER_PERIODEN_HAR_BEGYNT);
    }

    private FastsettePeriodeGrunnlagBuilder basicBuilder() {
        AktivitetIdentifikator aktivitetIdentifikator = AktivitetIdentifikator.forSelvstendigNæringsdrivende();
        return FastsettePeriodeGrunnlagBuilder.create()
                .medSaldo(AktivitetIdentifikator.forSelvstendigNæringsdrivende(), Stønadskontotype.MØDREKVOTE, 50)
                .medAktivitetIdentifikator(aktivitetIdentifikator)
                .medArbeid(aktivitetIdentifikator, new ArbeidTidslinje.Builder().build())
                .medFørsteLovligeUttaksdag(FØRSTE_LOVLIGE_UTTAKSDAG)
                .medSamtykke(true)
                .medBehandlingType(Behandlingtype.REVURDERING)
                .medFamiliehendelseDato(FAMILIEHENDELSE_DATO);
    }
}