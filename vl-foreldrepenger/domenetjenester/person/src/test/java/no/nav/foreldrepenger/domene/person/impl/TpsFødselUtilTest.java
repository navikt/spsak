package no.nav.foreldrepenger.domene.person.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class TpsFødselUtilTest {

    @Test
    public void skal_ikke_gi_nullpointer() {
        final FamilieHendelseGrunnlagBuilder grunnlagBuilder = FamilieHendelseGrunnlagBuilder.oppdatere(Optional.empty());
        final FamilieHendelseBuilder søknadHendelseBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        søknadHendelseBuilder
            .medAntallBarn(1)
            .medTerminbekreftelse(søknadHendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.of(2018, Month.MAY, 6))
                .medUtstedtDato(LocalDate.of(2018, Month.FEBRUARY, 6)));
        grunnlagBuilder.medSøknadVersjon(søknadHendelseBuilder);
        final FamilieHendelseBuilder saksbehandlerHendelseBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.OVERSTYRT);
        saksbehandlerHendelseBuilder
            .medAntallBarn(1)
            .medTerminbekreftelse(saksbehandlerHendelseBuilder.getTerminbekreftelseBuilder()
                .medTermindato(LocalDate.of(2018, Month.MAY, 6))
                .medUtstedtDato(LocalDate.of(2018, Month.FEBRUARY, 6)));
        grunnlagBuilder.medOverstyrtVersjon(saksbehandlerHendelseBuilder);

        final LocalDate mottattDato = LocalDate.of(2018, Month.MARCH, 16);
        final Søknad søknad = new SøknadEntitet.Builder().medMottattDato(mottattDato).medSøknadsdato(mottattDato).build();
        final FamilieHendelseGrunnlag grunnlag = grunnlagBuilder.build();

        final DatoIntervallEntitet intervall = TpsFødselUtil.forventetFødselIntervall(grunnlag, Period.parse("P1W"), Period.parse("P4W"), søknad);

        assertThat(intervall).isEqualByComparingTo(DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2018, Month.MARCH, 9),
            LocalDate.of(2018, Month.JUNE, 3)));
    }
}
