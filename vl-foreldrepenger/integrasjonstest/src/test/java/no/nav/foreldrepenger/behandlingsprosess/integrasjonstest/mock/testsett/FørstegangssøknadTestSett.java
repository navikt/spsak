package no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.testsett;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingsprosess.integrasjonstest.mock.søknad.SøknadTestdataBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class FørstegangssøknadTestSett {

    public static SøknadTestdataBuilder morFødselStandardUttak(AktørId aktørId, LocalDate fødselsdato) {
        return new SøknadTestdataBuilder().søknadForeldrepenger()
            .medSøker(ForeldreType.MOR, aktørId)
            .medMottattdato(fødselsdato) // For enkelhets skyld. Kan overskrives av kallende klient
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(1))
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                .leggTilPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), UttakPeriodeType.MØDREKVOTE)
                .leggTilPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(25).minusDays(1), UttakPeriodeType.FELLESPERIODE));
    }

    public static SøknadTestdataBuilder morFødselGradertUttak(AktørId aktørId, LocalDate fødselsdato) {
        return new SøknadTestdataBuilder().søknadForeldrepenger()
            .medSøker(ForeldreType.MOR, aktørId)
            .medMottattdato(fødselsdato)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(1))
            .medRettighet(new SøknadTestdataBuilder.RettighetBuilder()
                .harAleneomsorgForBarnet(false)
                .harAnnenForelderRett(true)
                .harOmsorgForBarnetIPeriodene(true))
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(fødselsdato.minusWeeks(3), fødselsdato.minusDays(1), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                .leggTilPeriode(fødselsdato, fødselsdato.plusWeeks(10).minusDays(1), UttakPeriodeType.MØDREKVOTE)
                .setAnnenForelderErInformert(true)
                .leggtilGradertPeriode(fødselsdato.plusWeeks(10), fødselsdato.plusWeeks(25).minusDays(1), UttakPeriodeType.FELLESPERIODE, BigDecimal.valueOf(50), "973093681"));
    }

    public static SøknadTestdataBuilder morTerminStandardUttak(AktørId aktørId, LocalDate søknadsdato, LocalDate termindatoFraSøknad, LocalDate utstedtDatoTerminbekreftelse) {

        return new SøknadTestdataBuilder().søknadForeldrepenger()
            .medMottattdato(søknadsdato)
            .medSøker(ForeldreType.MOR, aktørId)
            .medTermin(new SøknadTestdataBuilder.TerminBuilder()
                .medTermindato(termindatoFraSøknad)
                .medUtsteddato(utstedtDatoTerminbekreftelse))
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(termindatoFraSøknad.minusWeeks(3), termindatoFraSøknad.minusDays(1), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL)
                .leggTilPeriode(termindatoFraSøknad, termindatoFraSøknad.plusWeeks(10).minusDays(1), UttakPeriodeType.MØDREKVOTE)
                .leggTilPeriode(termindatoFraSøknad.plusWeeks(10), termindatoFraSøknad.plusWeeks(25).minusDays(1), UttakPeriodeType.FELLESPERIODE))
            ;
    }

    public static  SøknadTestdataBuilder farFødselStandardUttak(AktørId aktørId, LocalDate fødselsdato, AktørId aktørIdAnnenForelder) {
        return new SøknadTestdataBuilder().søknadForeldrepenger()
            .medSøker(ForeldreType.FAR, aktørId)
            .medMottattdato(fødselsdato)
            .medFødsel(new SøknadTestdataBuilder.FødselBuilder()
                .medFoedselsdato(fødselsdato)
                .medAntallBarn(1))
            .medRettighet(new SøknadTestdataBuilder.RettighetBuilder()
                .harAleneomsorgForBarnet(false)
                .harAnnenForelderRett(true)
                .harOmsorgForBarnetIPeriodene(true))
            .medAnnenForelder(new SøknadTestdataBuilder.AnnenForelderBuilder()
                .medNorskIdent()
                .medPersonIdent(new PersonIdent(aktørIdAnnenForelder.getId())))
            .medFordeling(new SøknadTestdataBuilder.FordelingBuilder()
                .leggTilPeriode(fødselsdato.plusWeeks(25), fødselsdato.plusWeeks(25 + 9), UttakPeriodeType.FEDREKVOTE));
    }

}
