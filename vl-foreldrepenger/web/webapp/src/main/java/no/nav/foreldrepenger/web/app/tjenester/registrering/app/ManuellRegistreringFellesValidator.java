package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.FØR_ELLER_LIK_DAGENS_DATO;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.LIKT_ANTALL_BARN_OG_FØDSELSDATOER;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.MINDRE_ELLER_LIK_LENGDE;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.OPPHOLDSSKJEMA_TOMT;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.PAAKREVD_FELT;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.TERMINBEKREFTELSESDATO_FØR_TERMINDATO;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.TERMINDATO_ELLER_FØDSELSDATO;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.TERMINDATO_OG_FØDSELSDATO;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorTekster.UGYLDIG_FØDSELSNUMMER;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.app.ManuellRegistreringValidatorUtil.Periode;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.AnnenForelderDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtenlandsoppholdDto;
import no.nav.vedtak.util.FPDateUtil;
import no.nav.vedtak.util.StringUtils;

public class ManuellRegistreringFellesValidator {

    private ManuellRegistreringFellesValidator() {
        // Klassen skal ikke instansieres
    }

    public static List<FeltFeilDto> validerOpplysninger(ManuellRegistreringDto registreringDto) {
        List<FeltFeilDto> funnetFeil =
            Stream.of(validerTidligereUtenlandsopphold(registreringDto),
                validerFremtidigUtenlandsopphold(registreringDto),
                validerTerminEllerFødselsdato(registreringDto),
                validerTermindato(registreringDto),
                validerTerminBekreftelsesdato(registreringDto),
                validerTerminBekreftelseAntallBarn(registreringDto),
                validerAntallBarn(registreringDto),
                validerFødselsdato(registreringDto),
                validerOmsorgsovertakelsesdato(registreringDto),
                validerKanIkkeOppgiAnnenForelder(registreringDto),
                validerAnnenForelderUtenlandskFoedselsnummer(registreringDto),
                validerAnnenForelderFødselsnummer(registreringDto),
                validerMottattDato(registreringDto))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return funnetFeil;
    }

    static Optional<FeltFeilDto> validerTidligereUtenlandsopphold(ManuellRegistreringDto registreringDto) {
        String feltnavn = "tidligereOppholdUtenlands";
        if (registreringDto.getHarTidligereOppholdUtenlands()) {
            if (erTomListe(registreringDto.getTidligereOppholdUtenlands())) {
                return Optional.of(new FeltFeilDto(feltnavn, OPPHOLDSSKJEMA_TOMT));
            }
            return validerTidligereUtenlandsoppholdDatoer(registreringDto.getTidligereOppholdUtenlands(), feltnavn);
        }
        return Optional.empty();
    }

    private static Optional<FeltFeilDto> validerTidligereUtenlandsoppholdDatoer(List<UtenlandsoppholdDto> tidligereOppholdUtenlands, String feltnavn) {
        ArrayList<String> feil = new ArrayList<>();
        List<Periode> perioder = tidligereOppholdUtenlands.stream().map(tou -> new Periode(tou.getPeriodeFom(), tou.getPeriodeTom())).collect(Collectors.toList());
        feil.addAll(ManuellRegistreringValidatorUtil.datoIkkeNull(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.startdatoFørSluttdato(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.periodeFørDagensDato(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.overlappendePerioder(perioder));

        if (feil.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeltFeilDto(feltnavn, feil.stream().collect(Collectors.joining(", "))));
    }

    static Optional<FeltFeilDto> validerFremtidigUtenlandsopphold(ManuellRegistreringDto registreringDto) {
        String feltnavn = "fremtidigOppholdUtenlands";
        if (registreringDto.getHarFremtidigeOppholdUtenlands()) {
            if (erTomListe(registreringDto.getFremtidigeOppholdUtenlands())) {
                return Optional.of(new FeltFeilDto(feltnavn, OPPHOLDSSKJEMA_TOMT));
            }
            return validerFremtidigOppholdUtenlandsDatoer(registreringDto.getFremtidigeOppholdUtenlands(), feltnavn);
        }
        return Optional.empty();
    }

    private static Optional<FeltFeilDto> validerFremtidigOppholdUtenlandsDatoer(List<UtenlandsoppholdDto> fremtidigOppholdUtenlands, String feltnavn) {
        ArrayList<String> feil = new ArrayList<>();
        List<Periode> perioder = fremtidigOppholdUtenlands.stream().map(fou -> new Periode(fou.getPeriodeFom(), fou.getPeriodeTom())).collect(Collectors.toList());
        feil.addAll(ManuellRegistreringValidatorUtil.datoIkkeNull(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.startdatoFørSluttdato(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.startdatoFørDagensDato(perioder));
        feil.addAll(ManuellRegistreringValidatorUtil.overlappendePerioder(perioder));
        if (feil.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeltFeilDto(feltnavn, feil.stream().collect(Collectors.joining(", "))));
    }

    //TODO PKMAUR-441(jannilsen) - Ta en grundig gjennomgang av validator for termin og fødselsdato.
    static Optional<FeltFeilDto> validerTerminEllerFødselsdato(ManuellRegistreringDto manuellRegistreringDto) {
        String feltnavn = "terminEllerFoedsel";
        if (erFødsel(manuellRegistreringDto)) {
            boolean harTerminDato = nonNull(manuellRegistreringDto.getTermindato());
            boolean harFødselsDato = !erTomListe(manuellRegistreringDto.getFoedselsDato());
            if (!harTerminDato && !harFødselsDato) {
                return Optional.of(new FeltFeilDto(feltnavn, TERMINDATO_ELLER_FØDSELSDATO));
            }
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerTermindato(ManuellRegistreringDto manuellRegistreringDto) {
        String feltnavn = "terminDato";
        if (erFødsel(manuellRegistreringDto) && !manuellRegistreringDto.getErBarnetFodt()) {
            //Termindato når barnet ikke er født.
            LocalDate terminDato = manuellRegistreringDto.getTermindato();
            if (!nonNull(terminDato)) {
                return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
            }
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerTerminBekreftelsesdato(ManuellRegistreringDto manuellRegistreringDto) {
        String feltnavn = "terminbekreftelseDato";
        if (erFødsel(manuellRegistreringDto) && !manuellRegistreringDto.getErBarnetFodt()) {
            LocalDate terminbekreftelseDato = manuellRegistreringDto.getTerminbekreftelseDato();
            LocalDate termindato = manuellRegistreringDto.getTermindato();
            boolean harFødselsdato = !erTomListe(manuellRegistreringDto.getFoedselsDato());
            boolean harTermindato = nonNull(termindato);
            if (nonNull(terminbekreftelseDato)) {
                Optional<FeltFeilDto> feltFeilDto = validerTerminBekreftelsesdato(terminbekreftelseDato, termindato, harFødselsdato, harTermindato, feltnavn);
                if (feltFeilDto.isPresent()) {
                    return feltFeilDto;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<FeltFeilDto> validerTerminBekreftelsesdato(LocalDate terminbekreftelseDato, LocalDate termindato, boolean harFødselsdato, boolean harTermindato, String feltnavn) {
        if (harFødselsdato) {
            return Optional.of(new FeltFeilDto(feltnavn, TERMINDATO_OG_FØDSELSDATO));
        }
        if (terminbekreftelseDato.isAfter(LocalDate.now(FPDateUtil.getOffset()))) {
            return Optional.of(new FeltFeilDto(feltnavn, FØR_ELLER_LIK_DAGENS_DATO));
        }
        if (harTermindato && !terminbekreftelseDato.isBefore(termindato)) {
            return Optional.of(new FeltFeilDto(feltnavn, TERMINBEKREFTELSESDATO_FØR_TERMINDATO));
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerTerminBekreftelseAntallBarn(ManuellRegistreringDto registreringDto) {
        String feltnavn = "antallBarnFraTerminbekreftelse";
        if (erFødsel(registreringDto) && !registreringDto.getErBarnetFodt()) {
            boolean harFødselsdato = !erTomListe(registreringDto.getFoedselsDato());
            if (harFødselsdato && nonNull(registreringDto.getAntallBarnFraTerminbekreftelse())) {
                return Optional.of(new FeltFeilDto(feltnavn, TERMINDATO_OG_FØDSELSDATO));
            }
            if (nonNull(registreringDto.getTermindato()) && isNull(registreringDto.getAntallBarnFraTerminbekreftelse())) {
                return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
            }
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerAntallBarn(ManuellRegistreringDto registreringDto) {
        String feltnavn = "antallBarn";
        if (erAdopsjonEllerOmsorg(registreringDto) && isNull(registreringDto.getOmsorg().getAntallBarn())) {
            return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
        }
        boolean harFødselsdato = !erTomListe(registreringDto.getFoedselsDato());
        if (harFødselsdato && erFødsel(registreringDto) && isNull(registreringDto.getAntallBarn())) {
            return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
        }
        return Optional.empty();
    }


    static Optional<FeltFeilDto> validerOmsorgsovertakelsesdato(ManuellRegistreringDto registreringDto) {
        String feltnavn = "omsorgsovertakelsesdato";
        if (erAdopsjonEllerOmsorg(registreringDto) && isNull(registreringDto.getOmsorg().getOmsorgsovertakelsesdato())) {
            return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
        }
        return Optional.empty();
    }


    static Optional<FeltFeilDto> validerFødselsdato(ManuellRegistreringDto registreringDto) {
        String feltnavn = "foedselsDato";
        Predicate<LocalDate> pred = d -> d.isAfter(LocalDate.now(FPDateUtil.getOffset()));
        List<LocalDate> fødselsdatoer = hentFødselsdatoer(registreringDto);
        boolean harFødselsdato = !erTomListe(fødselsdatoer);
        if (erAdopsjonEllerOmsorg(registreringDto)) {
            if (!harFødselsdato) {
                return Optional.of(new FeltFeilDto(feltnavn, LIKT_ANTALL_BARN_OG_FØDSELSDATOER));
            }
            Integer antallBarn = registreringDto.getOmsorg().getAntallBarn();
            if (nonNull(antallBarn) && antallBarn != fødselsdatoer.size()) {
                return Optional.of(new FeltFeilDto(feltnavn, LIKT_ANTALL_BARN_OG_FØDSELSDATOER));
            }
            if (fødselsdatoer.stream().anyMatch(pred)) {
                return Optional.of(new FeltFeilDto(feltnavn, FØR_ELLER_LIK_DAGENS_DATO));
            }
        } else if(erFødsel(registreringDto) && (TRUE.equals(registreringDto.getErBarnetFodt()))) {
            if (!harFødselsdato) {
                return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
            }
            if (fødselsdatoer.stream().anyMatch(pred)) {
                return Optional.of(new FeltFeilDto(feltnavn, FØR_ELLER_LIK_DAGENS_DATO));
            }
        }
        return Optional.empty();
    }

    private static List<LocalDate> hentFødselsdatoer(ManuellRegistreringDto registreringDto) {
        List<LocalDate> fødselsdatoer;
        if (erAdopsjonEllerOmsorg(registreringDto)) {
            fødselsdatoer = Optional.ofNullable(registreringDto.getOmsorg().getFoedselsDato()).orElse(emptyList());
        } else {
            fødselsdatoer = Optional.ofNullable(registreringDto.getFoedselsDato()).orElse(emptyList());
        }
        return fødselsdatoer.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    static Optional<FeltFeilDto> validerKanIkkeOppgiAnnenForelder(ManuellRegistreringDto registreringDto) {
        String feltnavn = "arsak";
        AnnenForelderDto annenForelder = registreringDto.getAnnenForelder();

        if (!isNull(annenForelder) && (TRUE.equals(annenForelder.getKanIkkeOppgiAnnenForelder()))) {
            AnnenForelderDto.KanIkkeOppgiBegrunnelse kanIkkeOppgiBegrunnelse = annenForelder.getKanIkkeOppgiBegrunnelse();
            if (isNull(kanIkkeOppgiBegrunnelse) || StringUtils.isBlank(kanIkkeOppgiBegrunnelse.getArsak())) {
                return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
            }
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerAnnenForelderUtenlandskFoedselsnummer(ManuellRegistreringDto registreringDto) {
        String feltnavn = "utenlandskFoedselsnummer";
        short tillattLengde = 20;
        AnnenForelderDto annenForelder = registreringDto.getAnnenForelder();
        if (!isNull(annenForelder) && (TRUE.equals(annenForelder.getKanIkkeOppgiAnnenForelder()))) {
            AnnenForelderDto.KanIkkeOppgiBegrunnelse kanIkkeOppgiBegrunnelse = annenForelder.getKanIkkeOppgiBegrunnelse();
            if(isNull(kanIkkeOppgiBegrunnelse)) {
                //Har vi ikke valgt årsak kan vi heller ikke validere utenlandsk fødselsnummer.
                return Optional.empty();
            }
            String utenlandskFoedselsnummer = kanIkkeOppgiBegrunnelse.getUtenlandskFoedselsnummer();
            if (!StringUtils.isBlank(utenlandskFoedselsnummer) && erStoerreEnnTillatt(tillattLengde, utenlandskFoedselsnummer)) {
                return Optional.of(new FeltFeilDto(feltnavn, MINDRE_ELLER_LIK_LENGDE + tillattLengde));
            }
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerAnnenForelderFødselsnummer(ManuellRegistreringDto registreringDto) {
        String feltnavn = "foedselsnummer";
        AnnenForelderDto annenForelder = registreringDto.getAnnenForelder();
        if (!isNull(annenForelder) && (!TRUE.equals(annenForelder.getKanIkkeOppgiAnnenForelder()))) {
            if (StringUtils.isBlank(annenForelder.getFoedselsnummer())) {
                return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
            }
            if (!PersonIdent.erGyldigFnr(annenForelder.getFoedselsnummer())) {
                return Optional.of(new FeltFeilDto(feltnavn, UGYLDIG_FØDSELSNUMMER));
            }
        }
        return Optional.empty();
    }

    static Optional<FeltFeilDto> validerMottattDato(ManuellRegistreringDto manuellRegistreringDto) {
        String feltnavn = "mottattDato";
        LocalDate mottattDato = manuellRegistreringDto.getMottattDato();
        if (nonNull(mottattDato)) {
            if (mottattDato.isAfter(LocalDate.now(FPDateUtil.getOffset()))) {
                return Optional.of(new FeltFeilDto(feltnavn, FØR_ELLER_LIK_DAGENS_DATO));
            }
        }
        if (isNull(mottattDato)) {
            return Optional.of(new FeltFeilDto(feltnavn, PAAKREVD_FELT));
        }
        return Optional.empty();
    }

    private static boolean erFødsel(ManuellRegistreringDto manuellRegistreringDto) {
        return FamilieHendelseType.FØDSEL.equals(manuellRegistreringDto.getTema());
    }

    private static boolean erAdopsjonEllerOmsorg(ManuellRegistreringDto manuellRegistreringDto) {
        return FamilieHendelseType.ADOPSJON.equals(manuellRegistreringDto.getTema()) ||
            FamilieHendelseType.OMSORG.equals(manuellRegistreringDto.getTema());
    }

    private static boolean erTomListe(List<?> list) {
        return isNull(list) || list.isEmpty();
    }

    private static boolean erStoerreEnnTillatt(short lengde, String verdi) {
        return verdi != null && verdi.trim().length() > lengde;
    }

}
