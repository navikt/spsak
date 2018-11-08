package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.FarSøkerType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.AnnenForelderDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.RettigheterDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderUtenNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Periode;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.UkjentForelder;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Brukerroller;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Land;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v1.Omsorgsovertakelseaarsaker;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;
import no.nav.vedtak.util.StringUtils;

public class SøknadMapperFelles {

    private SøknadMapperFelles() {
    }

    static Soeknad mapSøknad(ManuellRegistreringDto registreringDto, NavBruker navBruker) {
        Soeknad søknad = new Soeknad();
        søknad.setMottattDato(konverterDato(registreringDto.getMottattDato()));
        søknad.setSoeker(mapBruker(registreringDto.getSoker(), navBruker)); //Stønadstype Søker fnr og søkertype(mor/far/annen/medmor)
        søknad.setTilleggsopplysninger(registreringDto.getTilleggsopplysninger());
        søknad.setBegrunnelseForSenSoeknad(registreringDto.getBegrunnelse());
        return søknad;
    }

    static AnnenForelder mapAnnenForelder(ManuellRegistreringDto registreringDto, TpsTjeneste tpsTjeneste) {
        AnnenForelderDto annenForelderDto = registreringDto.getAnnenForelder();
        if (annenForelderDto == null) {
            return null;
        }
        if (TRUE.equals(annenForelderDto.getKanIkkeOppgiAnnenForelder())) {
            AnnenForelderDto.KanIkkeOppgiBegrunnelse oppgittBegrunnelse = annenForelderDto.getKanIkkeOppgiBegrunnelse();
            String utenlandskFoedselsnummer = oppgittBegrunnelse.getUtenlandskFoedselsnummer();
            if (!StringUtils.isBlank(utenlandskFoedselsnummer)) {
                AnnenForelderUtenNorskIdent annenForelderUtenNorskIdent = new AnnenForelderUtenNorskIdent();
                annenForelderUtenNorskIdent.setUtenlandskPersonidentifikator(utenlandskFoedselsnummer);
                if (!StringUtils.isBlank(oppgittBegrunnelse.getLand())) {
                    annenForelderUtenNorskIdent.setLand(getLandkode(oppgittBegrunnelse.getLand()));
                }
                return annenForelderUtenNorskIdent;
            } else {
                return new UkjentForelder();
            }
        }

        AnnenForelderMedNorskIdent annenForelderMedNorskIdent = new AnnenForelderMedNorskIdent();
        AktørId aktørId = tpsTjeneste.hentAktørForFnr(PersonIdent.fra(annenForelderDto.getFoedselsnummer()))
            .orElseThrow(() -> FeilFactory.create(ManuellRegistreringFeil.class).feilVedhentingAvAktørId(annenForelderDto.getFoedselsnummer()).toException());
        annenForelderMedNorskIdent.setAktoerId(aktørId.getId());

        return annenForelderMedNorskIdent;

    }

    static Rettigheter mapRettigheter(ManuellRegistreringForeldrepengerDto registreringDto) {
        TidsromPermisjonDto tidsromPermisjon = registreringDto.getTidsromPermisjon();
        if (!isNull(tidsromPermisjon)) {
            Rettigheter rettighet = new Rettigheter();
            rettighet.setHarAleneomsorgForBarnet(TRUE.equals(tidsromPermisjon.getSokerHarAleneomsorg()));
            rettighet.setHarAnnenForelderRett(TRUE.equals(tidsromPermisjon.getDenAndreForelderenHarRettPaForeldrepenger()));
            rettighet.setHarOmsorgForBarnetIPeriodene(true); //TODO: Hardkodet til true i avvente på avklaring. Se pkt 41: https://confluence.adeo.no/display/MODNAV/05g.+Avklaringer
            return rettighet;
        }
        return null;
    }

    public static List<OppholdNorge> opprettOppholdNorge(final LocalDate mottattDato, boolean fremtidigOppholdNorge, boolean tidligereOppholdNorge) {
        List<OppholdNorge> oppholdNorgeListe = new ArrayList<>();
        if (nonNull(mottattDato)) {
            if (tidligereOppholdNorge) {
                OppholdNorge oppholdNorgeSistePeriode = new OppholdNorge();
                Periode periode = new Periode();
                periode.setFom(konverterDato(mottattDato.minusYears(1)));
                periode.setTom(konverterDato(mottattDato));
                oppholdNorgeSistePeriode.setPeriode(periode);
                oppholdNorgeListe.add(oppholdNorgeSistePeriode);
            }
            if (fremtidigOppholdNorge) {
                OppholdNorge oppholdNorgeNestePeriode = new OppholdNorge();
                Periode periode = new Periode();
                periode.setFom(konverterDato(mottattDato));
                periode.setTom(konverterDato(mottattDato.plusYears(1)));
                oppholdNorgeNestePeriode.setPeriode(periode);
                oppholdNorgeListe.add(oppholdNorgeNestePeriode);
            }
        }

        return oppholdNorgeListe;
    }

    static Bruker mapBruker(ForeldreType søker, NavBruker navBruker) {
        Bruker bruker = new Bruker();
        bruker.setAktoerId(navBruker.getAktørId().getId());
        Brukerroller brukerroller = new Brukerroller();

        //TODO PKMANTIS-2092 Her må annen omsorgsperson håndteres, eller xsd-utvides med ANDRE, se kodeverk.xsd
        String brukerRolleKode = søker.getKode().equals("ANDRE") ? "IKKE_RELEVANT" : søker.getKode();

        brukerroller.setKode(brukerRolleKode);
        bruker.setSoeknadsrolle(brukerroller);
        return bruker;
    }

    static SoekersRelasjonTilBarnet mapRelasjonTilBarnet(ManuellRegistreringDto registreringDto) {
        // Hvis det er gjort et valg av rettigheter knyttet til omsorgovertakelse for far skal saken opprettes som en omsorgsovertakelse.
        boolean rettigheterRelatertTilOmsorgErSatt = registreringDto.getRettigheter() != null
            && !RettigheterDto.MANN_ADOPTERER_ALENE.equals(registreringDto.getRettigheter());
        if (rettigheterRelatertTilOmsorgErSatt) {
            return mapOmsorgsovertakelse(registreringDto);
        }
        //SøkersRelasjonTilBarnet = adopsjon, fødsel, termin eller omsorg
        FamilieHendelseType tema = registreringDto.getTema();
        if (erSøknadVedAdopsjon(tema)) {
            return mapAdopsjon(registreringDto);
        } else if (erSøknadVedFødsel(registreringDto.getErBarnetFodt(), registreringDto.getTema())) {
            return mapFødsel(registreringDto);
        } else if (erSøknadVedTermin(registreringDto.getErBarnetFodt(), registreringDto.getTema())) {
            return mapTermin(registreringDto);
        } else {
            throw new IllegalArgumentException(String.format("Ugyldig temakode: %s ", tema));
        }
    }

    static Foedsel mapFødsel(ManuellRegistreringDto registreringDto) {
        Foedsel fødsel = new Foedsel();
        if (harFødselsdato(registreringDto)) {
            List<LocalDate> foedselsDato = registreringDto.getFoedselsDato();
            if (foedselsDato.size() != 1) {
                throw new IllegalArgumentException("Støtter bare 1 fødselsdato på fødsel");
            }
            fødsel.setFoedselsdato(konverterDato(registreringDto.getFoedselsDato().get(0)));
        }
        fødsel.setAntallBarn(registreringDto.getAntallBarn());
        return fødsel;
    }


    static Termin mapTermin(ManuellRegistreringDto registreringDto) {
        Termin termin = new Termin();
        if (harTermindato(registreringDto)) {
            termin.setTermindato(konverterDato(registreringDto.getTermindato()));
            termin.setAntallBarn(registreringDto.getAntallBarnFraTerminbekreftelse());
            termin.setUtstedtdato(konverterDato(registreringDto.getTerminbekreftelseDato()));
        }
        return termin;
    }

    static Adopsjon mapAdopsjon(ManuellRegistreringDto registreringDto) {
        Adopsjon adopsjon = new Adopsjon();
        adopsjon.setOmsorgsovertakelsesdato(konverterDato(registreringDto.getOmsorg().getOmsorgsovertakelsesdato()));
        adopsjon.setAnkomstdato(konverterDato(registreringDto.getOmsorg().getAnkomstdato()));
        List<LocalDate> foedselsdatoer = registreringDto.getOmsorg().getFoedselsDato();
        for (LocalDate dato : foedselsdatoer) {
            adopsjon.getFoedselsdato().add(konverterDato(dato));
        }

        adopsjon.setAntallBarn(registreringDto.getOmsorg().getAntallBarn());
        adopsjon.setAdopsjonAvEktefellesBarn(registreringDto.getOmsorg().isErEktefellesBarn());
        return adopsjon;
    }

    static Omsorgsovertakelse mapOmsorgsovertakelse(ManuellRegistreringDto registreringDto) {
        Omsorgsovertakelse omsorgsovertakelse = new Omsorgsovertakelse();

        omsorgsovertakelse.setOmsorgsovertakelseaarsak(mapOmsorgsovertakelseaarsaker(registreringDto));
        omsorgsovertakelse.setOmsorgsovertakelsesdato(konverterDato(registreringDto.getOmsorg().getOmsorgsovertakelsesdato()));

        List<LocalDate> foedselsdatoer = registreringDto.getOmsorg().getFoedselsDato();
        for (LocalDate dato : foedselsdatoer) {
            omsorgsovertakelse.getFoedselsdato().add(konverterDato(dato));
        }

        omsorgsovertakelse.setAntallBarn(registreringDto.getOmsorg().getAntallBarn());
        return omsorgsovertakelse;
    }

    private static Omsorgsovertakelseaarsaker mapOmsorgsovertakelseaarsaker(ManuellRegistreringDto registreringDto) {
        Omsorgsovertakelseaarsaker omsorgsovertakelseaarsaker = new Omsorgsovertakelseaarsaker();
        FarSøkerType farSøkerType;
        switch (registreringDto.getRettigheter()) {
            case ANNEN_FORELDER_DOED:
                farSøkerType = FarSøkerType.ANDRE_FORELDER_DØD;
                break;
            case MANN_ADOPTERER_ALENE:
                farSøkerType = FarSøkerType.ADOPTERER_ALENE;
                break;
            case OVERTA_FORELDREANSVARET_ALENE:
            default:
                farSøkerType = (erSøknadVedFødsel(registreringDto.getErBarnetFodt(), registreringDto.getTema())
                    ? FarSøkerType.OVERTATT_OMSORG_F
                    : FarSøkerType.OVERTATT_OMSORG
                );
        }
        omsorgsovertakelseaarsaker.setKode(farSøkerType.getKode());
        return omsorgsovertakelseaarsaker;
    }

    private static boolean harTermindato(ManuellRegistreringDto registreringDto) {
        return nonNull(registreringDto.getTermindato());
    }

    private static boolean harFødselsdato(ManuellRegistreringDto registreringDto) {
        return !erTomListe(registreringDto.getFoedselsDato());
    }

    private static boolean erSøknadVedFødsel(Boolean erBarnetFødt, FamilieHendelseType tema) {
        boolean fødsel = FamilieHendelseType.FØDSEL.getKode().equals(tema.getKode());
        return (fødsel && (TRUE.equals(erBarnetFødt)));
    }

    private static boolean erSøknadVedTermin(Boolean erBarnetFødt, FamilieHendelseType tema) {
        boolean fødsel = FamilieHendelseType.FØDSEL.getKode().equals(tema.getKode());
        return (fødsel && !(TRUE.equals(erBarnetFødt))); //Barnet er ikke født ennå, termin.
    }

    private static boolean erSøknadVedAdopsjon(FamilieHendelseType tema) {
        return FamilieHendelseType.ADOPSJON.getKode().equals(tema.getKode());
    }

    static Land getLandkode(String land) {
        Land landkode = new Land();
        landkode.setKode(land);
        return landkode;
    }

    static XMLGregorianCalendar konverterDato(LocalDate dato) {
        if (isNull(dato)) {
            return null;
        }
        try {
            return DateUtil.convertToXMLGregorianCalendar(dato);
        } catch (DatatypeConfigurationException e) {
            throw ManuellRegistreringFeil.FACTORY.xmlGregorianCalendarParsingFeil(e).toException();
        }
    }

    static boolean erTomListe(List<?> liste) {
        return liste == null || liste.isEmpty();
    }
}
