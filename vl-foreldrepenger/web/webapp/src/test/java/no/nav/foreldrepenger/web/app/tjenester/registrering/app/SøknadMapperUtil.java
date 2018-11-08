package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.OmsorgDto;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto.AndreYtelserDto;
import no.nav.foreldrepenger.web.app.tjenester.kodeverk.dto.NaringsvirksomhetTypeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.AnnenForelderDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.EgenVirksomhetDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.GraderingDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.RettigheterDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtsettelseDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.VirksomhetDto;

public class SøknadMapperUtil {

    public static final AktørId STD_KVINNE_AKTØR_ID = new AktørId("9000000000036");

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    static EgenVirksomhetDto opprettEgenVirksomhetDto() {
        EgenVirksomhetDto egenVirksomhetDto = new EgenVirksomhetDto();
        egenVirksomhetDto.setVirksomheter(Collections.singletonList(opprettNorskVirksomhetMedEndringUtenRegnskapsfører()));
        return egenVirksomhetDto;
    }

    static void oppdaterDtoForFødsel(ManuellRegistreringDto dto, FamilieHendelseType tema, boolean erBarnetFødt, LocalDate fødselssdato, int antallBarn) {
        dto.setSoker(ForeldreType.MOR);
        dto.setMottattDato(LocalDate.now());
        dto.setFoedselsDato(Collections.singletonList(fødselssdato));
        dto.setTema(tema);
        dto.setAntallBarn(antallBarn);
        dto.setErBarnetFodt(erBarnetFødt);
    }


    static UtsettelseDto opprettUtsettelseDto(LocalDate fraDato, LocalDate tilDato, UttakPeriodeType gradering, boolean erArbeidstaker, String orgNr) {
        UtsettelseDto dto = new UtsettelseDto();
        dto.setArsakForUtsettelse(UtsettelseÅrsak.FERIE);
        dto.setPeriodeFom(fraDato);
        dto.setPeriodeTom(tilDato);
        dto.setOrgNr(orgNr);
        dto.setErArbeidstaker(erArbeidstaker);
        dto.setPeriodeForUtsettelse(gradering);
        return dto;
    }

    static PermisjonPeriodeDto opprettPermisjonPeriodeDto(LocalDate fraDato, LocalDate tilDato, UttakPeriodeType uttakPeriodeType, MorsAktivitet morsAktivitet) {
        PermisjonPeriodeDto dto = new PermisjonPeriodeDto();
        dto.setPeriodeFom(fraDato);
        dto.setPeriodeTom(tilDato);
        dto.setPeriodeType(uttakPeriodeType);
        dto.setMorsAktivitet(morsAktivitet);
        return dto;
    }

    static NavBruker opprettBruker() {
        AktørId aktørId = new AktørId("12345");
        LocalDate fødselsdato = LocalDate.of(1990, 1, 1);
        String fnr = "01019000001";
        Personinfo personinfo = new Personinfo.Builder()
            .medAktørId(aktørId)
            .medPersonIdent(new PersonIdent(fnr))
            .medNavn("Fornavn Etternavn")
            .medFødselsdato(fødselsdato)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medForetrukketSpråk(Språkkode.nb)
            .medLandkode(Landkoder.NOR)
            .build();
        return NavBruker.opprettNy(personinfo);

    }

    static ManuellRegistreringEngangsstonadDto opprettAdosjonDto(FamilieHendelseType tema, LocalDate omsorgsovertakelsesdato, LocalDate fødselssdato, int antallBarn, LocalDate ankomstdato) {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        manuellRegistreringEngangsstonadDto.setTema(tema);
        manuellRegistreringEngangsstonadDto.setSoker(ForeldreType.MOR);

        OmsorgDto omsorgDto = new OmsorgDto();
        omsorgDto.setOmsorgsovertakelsesdato(omsorgsovertakelsesdato);
        omsorgDto.setFoedselsDato(Collections.singletonList(fødselssdato));
        omsorgDto.setAntallBarn(antallBarn);
        omsorgDto.setAnkomstdato(ankomstdato);
        manuellRegistreringEngangsstonadDto.setOmsorg(omsorgDto);

        return manuellRegistreringEngangsstonadDto;
    }

    static ManuellRegistreringEngangsstonadDto opprettOmsorgDto(FamilieHendelseType tema, LocalDate omsorgsovertakelsesdato, RettigheterDto rettighet, int antallBarn, LocalDate fødselsdato) {
        ManuellRegistreringEngangsstonadDto manuellRegistreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        manuellRegistreringEngangsstonadDto.setSoker(ForeldreType.MOR);
        manuellRegistreringEngangsstonadDto.setRettigheter(rettighet);
        manuellRegistreringEngangsstonadDto.setTema(tema);

        OmsorgDto omsorgDto = new OmsorgDto();
        omsorgDto.setOmsorgsovertakelsesdato(omsorgsovertakelsesdato);
        omsorgDto.setAntallBarn(antallBarn);
        omsorgDto.setFoedselsDato(Collections.singletonList(fødselsdato));
        manuellRegistreringEngangsstonadDto.setOmsorg(omsorgDto);

        return manuellRegistreringEngangsstonadDto;
    }

    static AnnenForelderDto opprettAnnenForelderDto(Boolean kanIkkeOppgiAnnenForelder) {
        AnnenForelderDto annenForelderDto = new AnnenForelderDto();
        if (kanIkkeOppgiAnnenForelder) {
            annenForelderDto.setKanIkkeOppgiAnnenForelder(kanIkkeOppgiAnnenForelder);
            AnnenForelderDto.KanIkkeOppgiBegrunnelse kanIkkeOppgiBegrunnelse = new AnnenForelderDto.KanIkkeOppgiBegrunnelse();
            annenForelderDto.setKanIkkeOppgiBegrunnelse(kanIkkeOppgiBegrunnelse);
        }
        return annenForelderDto;
    }


    static GraderingDto opprettGraderingDto(LocalDate fraDato, LocalDate tilDato, BigDecimal prosentandel, UttakPeriodeType uttakPeriodeType, boolean erArbeidstaker, String orgNr) {
        GraderingDto dto = new GraderingDto();
        dto.setPeriodeFom(fraDato);
        dto.setPeriodeTom(tilDato);
        dto.setPeriodeForGradering(uttakPeriodeType);
        dto.setProsentandelArbeid(prosentandel);
        dto.setOrgNr(orgNr);
        dto.setErArbeidstaker(erArbeidstaker);
        dto.setSkalGraderes(true);
        return dto;
    }

    static ArbeidsforholdDto opprettUtenlandskArbeidsforholdDto(String navn, String landKode, LocalDate periodeFom, LocalDate periodeTom) {
        ArbeidsforholdDto dto = new ArbeidsforholdDto();
        dto.setArbeidsgiver(navn);
        dto.setLand(landKode);
        dto.setPeriodeFom(periodeFom);
        dto.setPeriodeTom(periodeTom);
        return dto;
    }

    static VirksomhetDto opprettNorskVirksomhetMedEndringUtenRegnskapsfører() {
        NaringsvirksomhetTypeDto naringsvirksomhetTypeDto = opprettNaringsvirksomhetTypeAnnenNæringsvirksomhet();
        return opprettVirksomhetDto("minarbeidsplass as",
            true, "1234567890",
            "NOR",
            naringsvirksomhetTypeDto,
            true,
            true, LocalDate.now().minusMonths(3), "Ny Lavvo",
            false, null, null,
            false);
    }

    static VirksomhetDto opprettVirksomhetDto(String virksomhetsNavn,
                                               boolean virksomhetRegistrertINorge, String orgNr,
                                               String landJobberFra,
                                               NaringsvirksomhetTypeDto virksomhetstype,
                                               boolean nyoppstartet,
                                               boolean varigEndring, LocalDate endretDato, String beskrivelseAvEndring,
                                               boolean harRegnskapsfører, String navnRegnskapsfører, String tlfRegnskapsfører,
                                               boolean tilknyttetNaringen) {
        VirksomhetDto virksomhetDto = new VirksomhetDto();
        virksomhetDto.setBeskrivelseAvEndring(beskrivelseAvEndring);
        virksomhetDto.setVarigEndringGjeldendeFom(endretDato);
        virksomhetDto.setFamilieEllerVennerTilknyttetNaringen(tilknyttetNaringen);
        virksomhetDto.setHarRegnskapsforer(harRegnskapsfører);
        virksomhetDto.setNavnRegnskapsforer(navnRegnskapsfører);
        virksomhetDto.setTlfRegnskapsforer(tlfRegnskapsfører);
        virksomhetDto.setLandJobberFra(landJobberFra);
        virksomhetDto.setNavn(virksomhetsNavn);
        virksomhetDto.setOrganisasjonsnummer(orgNr);
        virksomhetDto.setErNyoppstartet(nyoppstartet);
        virksomhetDto.setTypeVirksomhet(virksomhetstype);
        virksomhetDto.setHarVarigEndring(varigEndring);
        virksomhetDto.setVirksomhetRegistrertINorge(virksomhetRegistrertINorge);

        return virksomhetDto;
    }

    static NaringsvirksomhetTypeDto opprettNaringsvirksomhetTypeAnnenNæringsvirksomhet() {
        NaringsvirksomhetTypeDto naringsvirksomhetTypeDto = new NaringsvirksomhetTypeDto();
        naringsvirksomhetTypeDto.setAnnen(true);
        return naringsvirksomhetTypeDto;

    }

    static List<AndreYtelserDto> opprettTestdataForAndreYtelser() {
        List<AndreYtelserDto> result = new ArrayList<>();
        result.add(opprettAndreYtelserDto(ArbeidType.SLUTTPAKKE, LocalDate.now().minusWeeks(2), LocalDate.now()));
        result.add(opprettAndreYtelserDto(ArbeidType.LØNN_UNDER_UTDANNING, LocalDate.now().minusWeeks(4), LocalDate.now().minusWeeks(2)));
        result.add(opprettAndreYtelserDto(ArbeidType.MILITÆR_ELLER_SIVILTJENESTE, LocalDate.now().minusWeeks(6), LocalDate.now().minusWeeks(4)));
        return result;

    }
    static AndreYtelserDto opprettAndreYtelserDto(ArbeidType ytelseType, LocalDate periodeFom, LocalDate periodeTom) {
        AndreYtelserDto result = new AndreYtelserDto();
        result.setYtelseType(ytelseType);
        result.setPeriodeFom(periodeFom);
        result.setPeriodeTom(periodeTom);

        return result;
    }

    static TidsromPermisjonDto opprettTidsromPermisjonDto(Boolean harAleneomsorg, Boolean harRettPåForeldrepenger, List<PermisjonPeriodeDto> permisjonsPerioder) {
        TidsromPermisjonDto dto = new TidsromPermisjonDto();
        dto.setSokerHarAleneomsorg(harAleneomsorg);
        dto.setDenAndreForelderenHarRettPaForeldrepenger(harRettPåForeldrepenger);
        dto.setPermisjonsPerioder(permisjonsPerioder);
        return dto;
    }
}
