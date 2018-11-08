package no.nav.foreldrepenger.domene.person.impl;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public final class TpsFødselUtil {

    private TpsFødselUtil() {
    }

    public static DatoIntervallEntitet forventetFødselIntervall(FamilieHendelseGrunnlag grunnlag, Period tidsromFør, Period tidsromEtter, Søknad søknad) {
        Objects.requireNonNull(søknad, "søknad");
        Objects.requireNonNull(grunnlag, "grunnlag");
        Objects.requireNonNull(tidsromFør, "tidsromFør");
        Objects.requireNonNull(tidsromEtter, "tidsromEtter");

        final FamilieHendelse hendelse = grunnlag.getGjeldendeVersjon();
        Optional<LocalDate> funnetFødselsdato = TpsFødselUtil.finnFødselsdato(hendelse);

        if (FamilieHendelseType.ADOPSJON.equals(hendelse.getType()) || FamilieHendelseType.OMSORG.equals(hendelse.getType())) {
            return funnetFødselsdato.map(TpsFødselUtil::lagIntervallForFødsel)
                .orElseThrow(IllegalStateException::new);
        } else if (FamilieHendelseType.FØDSEL.equals(hendelse.getType())) {
            return funnetFødselsdato.map(TpsFødselUtil::lagIntervallForFødsel)
                .orElseGet(() -> finnIntervallForFødselUtenBekreftetFødsel(grunnlag, tidsromFør, tidsromEtter, søknad));
        } else if (FamilieHendelseType.TERMIN.equals(hendelse.getType())) {
            return lagIntervallForTermin(hendelse, tidsromFør, tidsromEtter, søknad);
        } else {
            throw new IllegalArgumentException("Ukjent FamilieHendelseType " + hendelse.getType());
        }
    }

    private static DatoIntervallEntitet finnIntervallForFødselUtenBekreftetFødsel(FamilieHendelseGrunnlag grunnlag, Period tidsromFør, Period tidsromEtter, Søknad søknad) {
        FamilieHendelse hendelse = grunnlag.getGjeldendeVersjon();
        if (hendelse.getTerminbekreftelse().isPresent()) {
            return lagIntervallForTermin(hendelse, tidsromFør, tidsromEtter, søknad);
        } else {
            return finnFødselsdato(grunnlag.getSøknadVersjon()).map(TpsFødselUtil::lagIntervallForFødsel)
                .orElseThrow(IllegalStateException::new);
        }
    }

    public static boolean kanFinneForventetFødselIntervall(FamilieHendelseGrunnlag hendelseGrunnlag, Søknad søknad) {
        if (hendelseGrunnlag == null) {
            return false;
        }
        final FamilieHendelse hendelse = hendelseGrunnlag.getGjeldendeVersjon();
        Optional<LocalDate> funnetFødselsdato = TpsFødselUtil.finnFødselsdato(hendelse);
        return funnetFødselsdato.isPresent() || kanFinneTerminDato(hendelse) && kanFinneSøknadsdato(søknad);
    }

    private static boolean kanFinneSøknadsdato(Søknad søknad) {
        return søknad != null && søknad.getSøknadsdato() != null;
    }

    private static boolean kanFinneTerminDato(FamilieHendelse hendelse) {
        return hendelse.getTerminbekreftelse().map(Terminbekreftelse::getTermindato).isPresent();
    }

    private static DatoIntervallEntitet lagIntervallForTermin(FamilieHendelse hendelse,
                                                              Period tidsromFør,
                                                              Period tidsromEtter,
                                                              Søknad søknad) {
        LocalDate termindato = finnTermindato(hendelse);
        LocalDate søknadsDato = søknad.getSøknadsdato();
        if (søknadsDato.isBefore(termindato)) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(søknadsDato.minus(tidsromFør), termindato.plus(tidsromEtter));
        } else {
            return DatoIntervallEntitet.fraOgMedTilOgMed(termindato.minus(tidsromFør), termindato.plus(tidsromEtter));
        }
    }

    private static DatoIntervallEntitet lagIntervallForFødsel(LocalDate fødselsdato) {
        return DatoIntervallEntitet.fraOgMedTilOgMed(fødselsdato.minusDays(1), fødselsdato.plusDays(1));
    }

    private static Optional<LocalDate> finnFødselsdato(FamilieHendelse hendelse) {
        return hendelse.getBarna().stream().map(UidentifisertBarn::getFødselsdato).findFirst();
    }

    private static LocalDate finnTermindato(FamilieHendelse hendelse) {
        return hendelse.getTerminbekreftelse().map(Terminbekreftelse::getTermindato).orElseThrow(IllegalStateException::new);
    }
}
