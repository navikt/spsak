package no.nav.foreldrepenger.web.app.tjenester.registrering.app;

import static java.util.Collections.singletonList;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.oppdaterDtoForFødsel;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettBruker;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettEgenVirksomhetDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettGraderingDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettNorskVirksomhetMedEndringUtenRegnskapsfører;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettPermisjonPeriodeDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettTestdataForAndreYtelser;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettTidsromPermisjonDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettUtenlandskArbeidsforholdDto;
import static no.nav.foreldrepenger.web.app.tjenester.registrering.app.SøknadMapperUtil.opprettUtsettelseDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.MottattDokument;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.ForeldreType;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.MorsAktivitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.OppgittDekningsgradEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.UttakPeriodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OppholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.OverføringÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.årsak.UtsettelseÅrsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.søknad.v1.MottattDokumentOversetterSøknad;
import no.nav.foreldrepenger.domene.mottak.dokumentpersiterer.impl.søknad.v1.MottattDokumentWrapperSøknad;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.domene.virksomhet.impl.VirksomhetTjenesteImpl;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.AnnenForelderDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ArbeidsforholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.DekningsgradDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.GraderingDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEndringsøknadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringEngangsstonadDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.ManuellRegistreringForeldrepengerDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.OppholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.OverføringsperiodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.PermisjonPeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.TidsromPermisjonDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtenlandsoppholdDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.UtsettelseDto;
import no.nav.foreldrepenger.web.app.tjenester.registrering.dto.VirksomhetDto;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.OrganisasjonsDetaljer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.organisasjon.OrganisasjonConsumer;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdNorge;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.OppholdUtlandet;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.AnnenOpptjening;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Dekningsgrad;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.EgenNaering;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.NorskOrganisasjon;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.UtenlandskArbeidsforhold;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Gradering;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Oppholdsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Overfoeringsperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Utsettelsesperiode;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Uttaksperiode;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

public class SøknadMapperFPTest {

    public static final AktørId STD_KVINNE_AKTØR_ID = new AktørId("9000000000036");
    private static final String virksomhetsNummer = "910909088";
    private final OrganisasjonConsumer organisasjonConsumer = mock(OrganisasjonConsumer.class);
    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();
    private BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repositoryRule.getEntityManager());
    private VirksomhetRepository virksomhetRepository = new VirksomhetRepositoryImpl(repositoryRule.getEntityManager());
    private BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
    private TpsTjeneste tpsTjeneste;
    private VirksomhetTjeneste virksomhetTjeneste;

    @Before
    public void setUp() throws Exception {
        tpsTjeneste = mock(TpsTjeneste.class);
        reset(tpsTjeneste);
        final Optional<AktørId> stdKvinneAktørId = Optional.of(STD_KVINNE_AKTØR_ID);
        when(tpsTjeneste.hentAktørForFnr(any())).thenReturn(stdKvinneAktørId);
        final Personinfo.Builder builder = new Personinfo.Builder()
            .medAktørId(STD_KVINNE_AKTØR_ID)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medNavn("Espen Utvikler")
            .medPersonIdent(PersonIdent.fra("12345678901"))
            .medFødselsdato(LocalDate.now().minusYears(20));
        final Optional<Personinfo> build = Optional.ofNullable(builder.build());
        when(tpsTjeneste.hentBrukerForAktør(any(AktørId.class))).thenReturn(build);
        when(organisasjonConsumer.hentOrganisasjon(any())).thenReturn(opprettVirksomhetResponse());
        virksomhetTjeneste = new VirksomhetTjenesteImpl(organisasjonConsumer, virksomhetRepository);
    }

    private HentOrganisasjonResponse opprettVirksomhetResponse() throws Exception {
        final HentOrganisasjonResponse hentOrganisasjonResponse = new HentOrganisasjonResponse();
        final Organisasjon value = new Organisasjon();
        final UstrukturertNavn navn = new UstrukturertNavn();
        navn.getNavnelinje().add("Color Line");
        value.setNavn(navn);
        value.setOrgnummer(virksomhetsNummer);
        final OrganisasjonsDetaljer detaljer = new OrganisasjonsDetaljer();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        detaljer.setRegistreringsDato(DatatypeFactory.newInstance().newXMLGregorianCalendar(c));
        value.setOrganisasjonDetaljer(detaljer);
        hentOrganisasjonResponse.setOrganisasjon(value);
        return hentOrganisasjonResponse;
    }

    @Test
    public void test_mapForeldrepenger() {
        ManuellRegistreringForeldrepengerDto registreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        oppdaterDtoForFødsel(registreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now().minusWeeks(3), 1);
        SøknadMapperFP.mapTilForeldrepenger(registreringForeldrepengerDto, opprettBruker(), tpsTjeneste, virksomhetTjeneste);
    }

    @Test
    public void test_mapEndringssøknad_utenFordeling() {
        TidsromPermisjonDto tidsromPermisjonDto = opprettTidsromPermisjonDto(true, true, null);
        ManuellRegistreringEndringsøknadDto manuellRegistreringEndringsøknadDto = new ManuellRegistreringEndringsøknadDto();
        oppdaterDtoForFødsel(manuellRegistreringEndringsøknadDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);

        manuellRegistreringEndringsøknadDto.setTidsromPermisjon(tidsromPermisjonDto);
        Fordeling fordeling = SøknadMapperFP.mapFordelingEndringssøknad(manuellRegistreringEndringsøknadDto);
        assertThat(fordeling).isNotNull();
        assertThat(fordeling.getPerioder()).hasSize(0);
    }

    @Test
    public void test_mapEndringssøknad_medFordeling() {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        BigDecimal prosentAndel = BigDecimal.valueOf(15);
        UttakPeriodeType uttakPeriodeType = UttakPeriodeType.FEDREKVOTE;

        PermisjonPeriodeDto permisjonPeriodeDto = opprettPermisjonPeriodeDto(LocalDate.now(), LocalDate.now().plusWeeks(3), UttakPeriodeType.MØDREKVOTE, null);
        TidsromPermisjonDto tidsromPermisjonDto = opprettTidsromPermisjonDto(true, true, Collections.singletonList(permisjonPeriodeDto));
        ManuellRegistreringEndringsøknadDto manuellRegistreringEndringsøknadDto = new ManuellRegistreringEndringsøknadDto();
        oppdaterDtoForFødsel(manuellRegistreringEndringsøknadDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);

        //Oppretter utsettelseperiode, mapOverfoeringsperiode, mapGraderingsperioder
        GraderingDto graderingDto = opprettGraderingDto(fraDato, tomDato, prosentAndel, uttakPeriodeType, false, null);
        tidsromPermisjonDto.setGraderingPeriode(Collections.singletonList(graderingDto));

        UtsettelseDto utsettelserDto = opprettUtsettelseDto(fraDato, tomDato, uttakPeriodeType, false, null);
        tidsromPermisjonDto.setUtsettelsePeriode(Collections.singletonList(utsettelserDto));

        OverføringÅrsak årsak = OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE;
        OverføringsperiodeDto overføringsperiodeDto = new OverføringsperiodeDto();
        overføringsperiodeDto.setOverforingArsak(årsak);
        overføringsperiodeDto.setFomDato(fraDato);
        overføringsperiodeDto.setTomDato(tomDato);
        tidsromPermisjonDto.setOverforingsperiode(overføringsperiodeDto);

        manuellRegistreringEndringsøknadDto.setTidsromPermisjon(tidsromPermisjonDto);
        Fordeling fordeling = SøknadMapperFP.mapFordelingEndringssøknad(manuellRegistreringEndringsøknadDto);
        assertThat(fordeling).isNotNull();
        assertThat(fordeling.getPerioder()).hasSize(4); //Forventer å ha en periode for hver av: permisjonPeriode, utsettelseperiode, Overfoeringsperiode og Graderingsperiode.
    }

    @Test
    public void testMapperMedlemskapFP_bareOppholdNorge() {

        ManuellRegistreringForeldrepengerDto registreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        registreringForeldrepengerDto.setMottattDato(LocalDate.now());
        registreringForeldrepengerDto.setHarFremtidigeOppholdUtenlands(false);
        registreringForeldrepengerDto.setHarTidligereOppholdUtenlands(false);
        registreringForeldrepengerDto.setOppholdINorge(true);

        no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap = SøknadMapperFP.mapMedlemskapFP(registreringForeldrepengerDto);
        assertThat(medlemskap.getOppholdNorge()).as("Forventer at vi skal ha opphold norge når vi ikke har utenlandsopphold.").hasSize(2);
    }

    @Test
    public void testMapperMedlemskapFP_utenOppholdNorge() {

        ManuellRegistreringForeldrepengerDto registreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        registreringForeldrepengerDto.setHarFremtidigeOppholdUtenlands(true);
        registreringForeldrepengerDto.setHarTidligereOppholdUtenlands(true);
        registreringForeldrepengerDto.setOppholdINorge(true);

        no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap = SøknadMapperFP.mapMedlemskapFP(registreringForeldrepengerDto);
        assertThat(medlemskap.getOppholdNorge()).as("Forventer at vi ikke har opphold norge når vi har utenlandsopphold.").hasSize(0);
    }

    @Test
    public void testMapperMedlemskapFP_med_FremtidigUtenlandsopphold() throws Exception {

        String land = "FRA";
        LocalDate periodeFom = LocalDate.now().plusMonths(2), periodeTom = LocalDate.now().plusMonths(5);

        ManuellRegistreringEngangsstonadDto registreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        registreringEngangsstonadDto.setMottattDato(LocalDate.now());
        registreringEngangsstonadDto.setHarFremtidigeOppholdUtenlands(true);
        registreringEngangsstonadDto.setHarTidligereOppholdUtenlands(false);
        registreringEngangsstonadDto.setOppholdINorge(true);
        UtenlandsoppholdDto utenlandsoppholdDto = new UtenlandsoppholdDto();
        utenlandsoppholdDto.setPeriodeFom(periodeFom);
        utenlandsoppholdDto.setPeriodeTom(periodeTom);
        utenlandsoppholdDto.setLand(land);
        registreringEngangsstonadDto.setFremtidigeOppholdUtenlands(singletonList(utenlandsoppholdDto));

        no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap = SøknadMapperFP.mapMedlemskapFP(registreringEngangsstonadDto);

        //Assert tidligere opphold i norge(siden vi ikke har tidligere utenlandsopphold.)
        List<OppholdNorge> oppholdNorgeListe = medlemskap.getOppholdNorge();
        assertThat(oppholdNorgeListe).isNotNull();
        assertThat(oppholdNorgeListe.size()).isEqualTo(1);

        List<OppholdUtlandet> alleOppholdUtlandet = medlemskap.getOppholdUtlandet();
        assertThat(alleOppholdUtlandet).isNotNull();
        assertThat(alleOppholdUtlandet).hasSize(1);

        OppholdUtlandet oppholdUtlandet = alleOppholdUtlandet.get(0);
        assertThat(oppholdUtlandet.getLand()).isNotNull();
        assertThat(oppholdUtlandet.getLand().getKode()).isEqualTo(land);
        assertThat(oppholdUtlandet.getPeriode()).isNotNull();
        assertThat(oppholdUtlandet.getPeriode().getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeFom));
        assertThat(oppholdUtlandet.getPeriode().getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeTom));
    }

    @Test
    public void testMapperMedlemskapFP_med_TidligereUtenlandsopphold() throws Exception {

        final String land = "FRA";
        LocalDate periodeFom = LocalDate.now().minusMonths(6), periodeTom = LocalDate.now().minusMonths(3);

        ManuellRegistreringEngangsstonadDto registreringEngangsstonadDto = new ManuellRegistreringEngangsstonadDto();
        registreringEngangsstonadDto.setMottattDato(LocalDate.now());
        registreringEngangsstonadDto.setHarFremtidigeOppholdUtenlands(false); //Ikke fremtidige utenlandsopphold, så da får vi fremtidg opphold i norge
        registreringEngangsstonadDto.setHarTidligereOppholdUtenlands(true);
        registreringEngangsstonadDto.setOppholdINorge(true);
        UtenlandsoppholdDto utenlandsoppholdDto = new UtenlandsoppholdDto();
        utenlandsoppholdDto.setPeriodeFom(periodeFom);
        utenlandsoppholdDto.setPeriodeTom(periodeTom);
        utenlandsoppholdDto.setLand(land);
        registreringEngangsstonadDto.setTidligereOppholdUtenlands(singletonList(utenlandsoppholdDto));

        no.nav.vedtak.felles.xml.soeknad.felles.v1.Medlemskap medlemskap = SøknadMapperFP.mapMedlemskapFP(registreringEngangsstonadDto);

        //Assert fremtidg opphold i norge(siden vi ikke har fremtidig utenlandsopphold.
        List<OppholdNorge> oppholdNorgeListe = medlemskap.getOppholdNorge();
        assertThat(oppholdNorgeListe).isNotNull();
        assertThat(oppholdNorgeListe.size()).isEqualTo(1);


        List<OppholdUtlandet> oppholdUtenlandsListe = medlemskap.getOppholdUtlandet();
        assertThat(oppholdUtenlandsListe).isNotNull();
        assertThat(oppholdUtenlandsListe.size()).isEqualTo(1);
        OppholdUtlandet utenlandsopphold = oppholdUtenlandsListe.get(0);
        assertThat(utenlandsopphold.getLand()).isNotNull();
        assertThat(utenlandsopphold.getLand().getKode()).isEqualTo(land);
        assertThat(utenlandsopphold.getPeriode()).isNotNull();
        assertThat(utenlandsopphold.getPeriode().getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeFom));
        assertThat(utenlandsopphold.getPeriode().getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(periodeTom));
    }

    @Test
    public void testMedDekningsgrad() {
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        manuellRegistreringForeldrepengerDto.setDekningsgrad(DekningsgradDto.HUNDRE);

        Dekningsgrad dekningsgrad = SøknadMapperFP.mapDekningsgrad(manuellRegistreringForeldrepengerDto);
        assertThat(dekningsgrad).isNotNull();
        assertThat(dekningsgrad.getDekningsgrad()).isNotNull();
        assertThat(dekningsgrad.getDekningsgrad().getKode()).isEqualTo(DekningsgradDto.HUNDRE.getValue());
    }

    @Test
    public void testUtenDekningsgrad() {
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();

        Dekningsgrad dekningsgrad = SøknadMapperFP.mapDekningsgrad(manuellRegistreringForeldrepengerDto);
        assertThat(dekningsgrad).isNull();
    }

    @Test
    public void test_mapOverfoeringsperiode() {
        OverføringÅrsak årsak = OverføringÅrsak.INSTITUSJONSOPPHOLD_ANNEN_FORELDRE;
        OverføringsperiodeDto overføringsperiode = new OverføringsperiodeDto();
        overføringsperiode.setOverforingArsak(årsak);

        Overfoeringsperiode overfoeringsperiode = SøknadMapperFP.mapOverfoeringsperiode(overføringsperiode, ForeldreType.FAR);
        assertThat(overfoeringsperiode).isNotNull();
        assertThat(overfoeringsperiode.getAarsak().getKode()).isEqualTo(årsak.getKode());
    }

    @Test
    public void test_mapUtsettelsesperiode() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        UttakPeriodeType uttakPeriodeType = UttakPeriodeType.FELLESPERIODE;
        String orgNr = "1234567890";
        UtsettelseDto utsettelserDto = opprettUtsettelseDto(fraDato, tomDato, uttakPeriodeType, true, orgNr);
        Utsettelsesperiode utsettelsesperiode = SøknadMapperFP.mapUtsettelsesperiode(utsettelserDto);
        assertThat(utsettelsesperiode).isNotNull();
        assertThat(utsettelsesperiode.getAarsak().getKode()).isEqualTo(UtsettelseÅrsak.FERIE.getKode());
        assertThat(utsettelsesperiode.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(utsettelsesperiode.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(utsettelsesperiode.getUtsettelseAv().getKode()).isEqualTo(uttakPeriodeType.getKode());
        assertThat(utsettelsesperiode.isErArbeidstaker()).isEqualTo(true);
        assertThat(utsettelsesperiode.getVirksomhetsnummer()).isEqualTo(orgNr);
    }


    @Test
    public void test_mapFedrekvotePeriodeDto() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        PermisjonPeriodeDto fedrekvotePeriodeDto = opprettPermisjonPeriodeDto(fraDato, tomDato, UttakPeriodeType.FEDREKVOTE, null);
        Uttaksperiode uttaksperiode = SøknadMapperFP.mapPermisjonPeriodeDto(fedrekvotePeriodeDto);
        assertThat(uttaksperiode).isNotNull();
        assertThat(uttaksperiode.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(uttaksperiode.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(uttaksperiode.getType().getKode()).isEqualTo(UttakPeriodeType.FEDREKVOTE.getKode());
    }

    @Test
    public void test_mapMødrekvotePeriodeDto() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        PermisjonPeriodeDto fedrekvotePeriodeDto = opprettPermisjonPeriodeDto(fraDato, tomDato, UttakPeriodeType.MØDREKVOTE, null);
        Uttaksperiode uttaksperiode = SøknadMapperFP.mapPermisjonPeriodeDto(fedrekvotePeriodeDto);
        assertThat(uttaksperiode).isNotNull();
        assertThat(uttaksperiode.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(uttaksperiode.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(uttaksperiode.getType().getKode()).isEqualTo(UttakPeriodeType.MØDREKVOTE.getKode());
    }

    @Test
    public void test_mapForeldrepengerFørFødselPeriode() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        PermisjonPeriodeDto fedrekvotePeriodeDto = opprettPermisjonPeriodeDto(fraDato, tomDato, UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, null);
        Uttaksperiode uttaksperiode = SøknadMapperFP.mapPermisjonPeriodeDto(fedrekvotePeriodeDto);
        assertThat(uttaksperiode).isNotNull();
        assertThat(uttaksperiode.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(uttaksperiode.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(uttaksperiode.getType().getKode()).isEqualTo(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL.getKode());
    }

    @Test
    public void test_mapGraderingsperiode() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        BigDecimal prosentAndel = BigDecimal.valueOf(15);
        UttakPeriodeType uttakPeriodeType = UttakPeriodeType.FEDREKVOTE;
        String orgNr = "1234567890";
        GraderingDto graderingDto = opprettGraderingDto(fraDato, tomDato, prosentAndel, uttakPeriodeType, true, orgNr);
        Gradering gradering = ((Gradering) SøknadMapperFP.mapGraderingsperiode(graderingDto));
        assertThat(gradering).isNotNull();
        assertThat(gradering.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(gradering.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(gradering.getType().getKode()).isEqualTo(uttakPeriodeType.getKode());
        assertThat(gradering.getArbeidtidProsent()).isEqualTo(prosentAndel.doubleValue());
        assertThat(gradering.isErArbeidstaker()).isEqualTo(true);
        assertThat(gradering.getVirksomhetsnummer()).isEqualTo(orgNr);
    }

    @Test
    public void test_mapGraderingsperiode_arbeidsprosent_desimal() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        BigDecimal prosentAndel = BigDecimal.valueOf(15.55);
        UttakPeriodeType uttakPeriodeType = UttakPeriodeType.FEDREKVOTE;
        String orgNr = "1234567890";
        GraderingDto graderingDto = opprettGraderingDto(fraDato, tomDato, prosentAndel, uttakPeriodeType, true, orgNr);
        Gradering gradering = ((Gradering) SøknadMapperFP.mapGraderingsperiode(graderingDto));
        assertThat(gradering).isNotNull();
        assertThat(gradering.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(gradering.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(gradering.getType().getKode()).isEqualTo(uttakPeriodeType.getKode());
        assertThat(gradering.getArbeidtidProsent()).isEqualTo(prosentAndel.doubleValue());
        assertThat(gradering.isErArbeidstaker()).isEqualTo(true);
        assertThat(gradering.getVirksomhetsnummer()).isEqualTo(orgNr);
    }

    @Test
    public void test_mapFellesPeriodeDto() throws Exception {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);

        PermisjonPeriodeDto fellesPeriodeDto = opprettPermisjonPeriodeDto(fraDato, tomDato, UttakPeriodeType.FELLESPERIODE, MorsAktivitet.ARBEID);
        Uttaksperiode uttaksperiode = SøknadMapperFP.mapPermisjonPeriodeDto(fellesPeriodeDto);
        assertThat(uttaksperiode).isNotNull();
        assertThat(uttaksperiode.getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((fraDato)));
        assertThat(uttaksperiode.getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar((tomDato)));
        assertThat(uttaksperiode.getType().getKode()).isEqualTo(UttakPeriodeType.FELLESPERIODE.getKode());
        assertThat(uttaksperiode.getMorsAktivitetIPerioden().getKode()).isEqualTo(MorsAktivitet.ARBEID.getKode());
    }

    @Test
    public void test_mapUttaksperioder_gradering() {
        LocalDate fraDato = LocalDate.now();
        LocalDate tomDato = LocalDate.now().plusDays(3);
        BigDecimal prosentAndel = BigDecimal.valueOf(15);
        UttakPeriodeType uttakPeriodeType = UttakPeriodeType.FEDREKVOTE;
        GraderingDto graderingDto = opprettGraderingDto(fraDato, tomDato, prosentAndel, uttakPeriodeType, true, null);

        List<Uttaksperiode> uttaksperiodes = SøknadMapperFP.mapGraderingsperioder(Collections.singletonList(graderingDto));
        assertThat(uttaksperiodes).isNotNull();
        assertThat(uttaksperiodes).hasSize(1);
    }

    @Test
    public void test_mapUttaksperioder_gradering_med_samtidig_uttak() {
        GraderingDto graderingDto = opprettGraderingDto(LocalDate.now(), LocalDate.now().plusDays(3), BigDecimal.valueOf(15),
            UttakPeriodeType.FEDREKVOTE, true, null);
        graderingDto.setHarSamtidigUttak(true);
        graderingDto.setSamtidigUttaksprosent(BigDecimal.TEN);

        List<Uttaksperiode> uttakperioder = SøknadMapperFP.mapGraderingsperioder(Collections.singletonList(graderingDto));
        assertThat(uttakperioder).hasSize(1);
        assertThat(uttakperioder.get(0).isOenskerSamtidigUttak()).isEqualTo(graderingDto.getHarSamtidigUttak());
        assertThat(uttakperioder.get(0).getSamtidigUttakProsent()).isEqualTo(graderingDto.getSamtidigUttaksprosent().doubleValue());
    }

    @Test
    public void test_mapUttaksperioder_samtidig_uttak() {
        PermisjonPeriodeDto periode = opprettPermisjonPeriodeDto(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 1),
            UttakPeriodeType.FELLESPERIODE, MorsAktivitet.ARBEID_OG_UTDANNING);
        periode.setHarSamtidigUttak(true);
        periode.setSamtidigUttaksprosent(BigDecimal.TEN);

        List<Uttaksperiode> uttaksperioder = SøknadMapperFP.mapUttaksperioder(Collections.singletonList(periode));
        assertThat(uttaksperioder).hasSize(1);
        assertThat(uttaksperioder.get(0).isOenskerSamtidigUttak()).isEqualTo(periode.getHarSamtidigUttak());
        assertThat(uttaksperioder.get(0).getSamtidigUttakProsent()).isEqualTo(periode.getSamtidigUttaksprosent().doubleValue());
    }

    @Test
    public void test_mapUttaksperioder_opphold() throws Exception {
        OppholdDto oppholdDto = new OppholdDto();
        oppholdDto.setPeriodeFom(LocalDate.now().minusDays(1));
        oppholdDto.setPeriodeTom(LocalDate.now());
        oppholdDto.setÅrsak(OppholdÅrsak.MØDREKVOTE_ANNEN_FORELDER);

        List<Oppholdsperiode> oppholdsperiode = SøknadMapperFP.mapOppholdsperioder(Collections.singletonList(oppholdDto));
        assertThat(oppholdsperiode).hasSize(1);
        assertThat(oppholdsperiode.get(0).getAarsak().getKode()).isEqualTo(oppholdDto.getÅrsak().getKode());
        assertThat(oppholdsperiode.get(0).getAarsak().getKodeverk()).isEqualTo(oppholdDto.getÅrsak().getKodeverk());
        assertThat(oppholdsperiode.get(0).getFom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(oppholdDto.getPeriodeFom()));
        assertThat(oppholdsperiode.get(0).getTom()).isEqualTo(DateUtil.convertToXMLGregorianCalendar(oppholdDto.getPeriodeTom()));
    }

    @Test
    public void test_mapUttaksperioder_permisjonperioder() {
        //Test av permisjonsperiodene:
        //dato før fødsel -> fødselsdato -> mødrekvote
        LocalDate perminsjonstartFørFødsel = LocalDate.now().minusWeeks(3);
        LocalDate fødselsdato = LocalDate.now();
        LocalDate mødrekvoteSlutt = fødselsdato.plusDays(1).plusWeeks(3);

        PermisjonPeriodeDto permisjonPeriodeFørFødselDto = opprettPermisjonPeriodeDto(perminsjonstartFørFødsel, fødselsdato, UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, null);
        PermisjonPeriodeDto permisjonPeriodeMødrekvote = opprettPermisjonPeriodeDto(LocalDate.now().plusDays(1), mødrekvoteSlutt, UttakPeriodeType.MØDREKVOTE, null);

        List<PermisjonPeriodeDto> permisjonsperioder = new ArrayList<>();
        permisjonsperioder.add(permisjonPeriodeFørFødselDto);
        permisjonsperioder.add(permisjonPeriodeMødrekvote);

        List<Uttaksperiode> uttaksperioder = SøknadMapperFP.mapUttaksperioder(permisjonsperioder);
        assertThat(uttaksperioder).isNotNull();
        assertThat(uttaksperioder).hasSize(2);
        assertThat(uttaksperioder).anySatisfy(uttaksperiode -> UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL.getKode().equals(uttaksperiode.getType().getKode()));
        assertThat(uttaksperioder).anySatisfy(uttaksperiode -> UttakPeriodeType.MØDREKVOTE.getKode().equals(uttaksperiode.getType().getKode()));
    }

    @Test
    public void test_mapUttaksperioder_Fellesperioder() {
        LocalDate mødrekvoteSlutt = LocalDate.now().plusWeeks(3);
        LocalDate fellesperiodeSlutt = mødrekvoteSlutt.plusWeeks(4);

        PermisjonPeriodeDto fellesPeriodeDto = opprettPermisjonPeriodeDto(mødrekvoteSlutt, fellesperiodeSlutt, UttakPeriodeType.FELLESPERIODE, MorsAktivitet.ARBEID_OG_UTDANNING);

        List<Uttaksperiode> uttaksperioder = SøknadMapperFP.mapUttaksperioder(Collections.singletonList(fellesPeriodeDto));
        assertThat(uttaksperioder).isNotNull();
        assertThat(uttaksperioder).hasSize(1);
        assertThat(uttaksperioder).first().satisfies(uttaksperiode -> MorsAktivitet.ARBEID_OG_UTDANNING.getKode().equals(uttaksperiode.getMorsAktivitetIPerioden().getKode()));
        assertThat(uttaksperioder).first().satisfies(uttaksperiode -> UttakPeriodeType.FELLESPERIODE.getKode().equals(uttaksperiode.getType().getKode()));
    }

    @Test
    public void test_map_mors_aktivitet_uføretrygd() {
        LocalDate mødrekvoteSlutt = LocalDate.now().plusWeeks(3);
        LocalDate fellesperiodeSlutt = mødrekvoteSlutt.plusWeeks(4);

        PermisjonPeriodeDto fellesPeriodeDto = opprettPermisjonPeriodeDto(mødrekvoteSlutt, fellesperiodeSlutt, UttakPeriodeType.FELLESPERIODE, MorsAktivitet.UFØRE);

        List<Uttaksperiode> uttaksperioder = SøknadMapperFP.mapUttaksperioder(Collections.singletonList(fellesPeriodeDto));
        assertThat(uttaksperioder).isNotNull();
        assertThat(uttaksperioder).hasSize(1);
        assertThat(uttaksperioder).first().satisfies(uttaksperiode -> MorsAktivitet.UFØRE.getKode().equals(uttaksperiode.getMorsAktivitetIPerioden().getKode()));
        assertThat(uttaksperioder).first().satisfies(uttaksperiode -> UttakPeriodeType.FELLESPERIODE.getKode().equals(uttaksperiode.getType().getKode()));
    }

    @Test
    public void test_mapFordeling_morSøkerMenfarRettPåFedrekvote() {
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        PermisjonPeriodeDto permisjonPeriodeDto = opprettPermisjonPeriodeDto(LocalDate.now().minusWeeks(3), LocalDate.now(), UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, null);
        oppdaterDtoForFødsel(manuellRegistreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);
        manuellRegistreringForeldrepengerDto.setTidsromPermisjon(opprettTidsromPermisjonDto(true, true, Collections.singletonList(permisjonPeriodeDto)));
        Fordeling fordeling = SøknadMapperFP.mapFordeling(manuellRegistreringForeldrepengerDto);
        assertThat(fordeling).isNotNull();
        assertThat(fordeling.getPerioder()).hasSize(1); //Forventer å ha mødrekvote periode basert på forventet permisjon før fødsel
    }

    @Test
    public void test_skal_ta_inn_papirsøknadDTO_mappe_til_xml_så_mappe_til_domenemodell() {
        NavBruker navBruker = opprettBruker();
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        oppdaterDtoForFødsel(manuellRegistreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);
        List<PermisjonPeriodeDto> permisjonPerioder = new ArrayList<>();
        PermisjonPeriodeDto permisjonPeriodeDto = opprettPermisjonPeriodeDto(LocalDate.now().minusWeeks(3), LocalDate.now(), UttakPeriodeType.MØDREKVOTE, null);
        PermisjonPeriodeDto permisjonPeriodeDto2 = opprettPermisjonPeriodeDto(LocalDate.now().minusWeeks(3), LocalDate.now(), UttakPeriodeType.FELLESPERIODE, MorsAktivitet.ARBEID);
        PermisjonPeriodeDto permisjonPeriodeDto3 = opprettPermisjonPeriodeDto(LocalDate.now().minusWeeks(3), LocalDate.now(), UttakPeriodeType.FORELDREPENGER, MorsAktivitet.INNLAGT);
        permisjonPerioder.add(permisjonPeriodeDto);
        permisjonPerioder.add(permisjonPeriodeDto2);
        permisjonPerioder.add(permisjonPeriodeDto3);
        manuellRegistreringForeldrepengerDto.setTidsromPermisjon(opprettTidsromPermisjonDto(false, true, permisjonPerioder));
        manuellRegistreringForeldrepengerDto.setDekningsgrad(DekningsgradDto.HUNDRE);
        manuellRegistreringForeldrepengerDto.setAndreYtelser(opprettTestdataForAndreYtelser());

        // Annen forelder er informert
        AnnenForelderDto annenForelderDto = new AnnenForelderDto();
        annenForelderDto.setAnnenForelderInformert(true);
        manuellRegistreringForeldrepengerDto.setAnnenForelder(annenForelderDto);

        final Soeknad soeknad = SøknadMapperFP.mapTilForeldrepenger(manuellRegistreringForeldrepengerDto, navBruker, tpsTjeneste, virksomhetTjeneste);
        final MottattDokumentOversetterSøknad oversetter = new MottattDokumentOversetterSøknad(repositoryProvider, virksomhetTjeneste, tpsTjeneste);
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, navBruker);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        final MottattDokument.Builder builder1 = new MottattDokument.Builder().medMottattDato(LocalDate.now()).medFagsakId(behandling.getFagsakId()).medElektroniskRegistrert(true);
        oversetter.trekkUtDataOgPersister((MottattDokumentWrapperSøknad) MottattDokumentWrapperSøknad.tilXmlWrapper(soeknad), builder1.build(), behandling, Optional.empty());

        final YtelseFordelingAggregat ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);

        assertThat(ytelseFordelingAggregat.getOppgittRettighet()).isNotNull();
        assertThat(ytelseFordelingAggregat.getOppgittRettighet().getHarAnnenForeldreRett()).isTrue();
        assertThat(ytelseFordelingAggregat.getOppgittRettighet().getHarOmsorgForBarnetIHelePerioden()).isTrue();
        assertThat(ytelseFordelingAggregat.getOppgittDekningsgrad()).isNotNull();
        assertThat(ytelseFordelingAggregat.getOppgittDekningsgrad().getDekningsgrad()).isEqualTo(OppgittDekningsgradEntitet.HUNDRE_PROSENT);
        assertThat(ytelseFordelingAggregat.getOppgittFordeling()).isNotNull();
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).isNotEmpty();
    }

    @Test
    public void test_verdikjeden_fra_papirsøknad_til_domenemodell_mor_søker_med_mødrekvote_periode_før_fødsel_utsettelse() {
        NavBruker navBruker = opprettBruker();
        final LocalDate fødselsdato = LocalDate.now().minusDays(10);
        final LocalDate mødrekvoteSlutt = fødselsdato.plusWeeks(10);
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        oppdaterDtoForFødsel(manuellRegistreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, fødselsdato, 1);
        //Perioder: permisjon før fødsel og mødrekvote
        List<PermisjonPeriodeDto> permisjonsperioder = new ArrayList<>();
        permisjonsperioder.add(opprettPermisjonPeriodeDto(fødselsdato.minusWeeks(3), fødselsdato, UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL, null));
        permisjonsperioder.add(opprettPermisjonPeriodeDto(fødselsdato, mødrekvoteSlutt, UttakPeriodeType.MØDREKVOTE, null));
        TidsromPermisjonDto tidsromPermisjonDto = opprettTidsromPermisjonDto(true, false, permisjonsperioder);

        //Utsettelse
        UtsettelseDto utsettelserDto = opprettUtsettelseDto(mødrekvoteSlutt, mødrekvoteSlutt.plusWeeks(1), UttakPeriodeType.MØDREKVOTE, true, virksomhetsNummer);
        tidsromPermisjonDto.setUtsettelsePeriode(Collections.singletonList(utsettelserDto));

        manuellRegistreringForeldrepengerDto.setTidsromPermisjon(tidsromPermisjonDto);

        //Dekningsgrad
        manuellRegistreringForeldrepengerDto.setDekningsgrad(DekningsgradDto.AATI);

        // Annen forelder er informert
        AnnenForelderDto annenForelderDto = new AnnenForelderDto();
        annenForelderDto.setAnnenForelderInformert(true);
        manuellRegistreringForeldrepengerDto.setAnnenForelder(annenForelderDto);

        final Soeknad soeknad = SøknadMapperFP.mapTilForeldrepenger(manuellRegistreringForeldrepengerDto, navBruker, tpsTjeneste, virksomhetTjeneste);
        final MottattDokumentOversetterSøknad oversetter = new MottattDokumentOversetterSøknad(repositoryProvider, virksomhetTjeneste, tpsTjeneste);
        final Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, navBruker);
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        final Behandling behandling = builder.build();

        repositoryProvider.getFagsakRepository().opprettNy(fagsak);
        behandlingRepository.lagre(behandling, behandlingRepository.taSkriveLås(behandling));

        final MottattDokument.Builder builder1 = new MottattDokument.Builder().medMottattDato(LocalDate.now())
            .medFagsakId(behandling.getFagsakId()).medElektroniskRegistrert(true);
        oversetter.trekkUtDataOgPersister((MottattDokumentWrapperSøknad) MottattDokumentWrapperSøknad.tilXmlWrapper(soeknad), builder1.build(), behandling, Optional.empty());

        final YtelseFordelingAggregat ytelseFordelingAggregat = repositoryProvider.getYtelsesFordelingRepository().hentAggregat(behandling);

        assertThat(ytelseFordelingAggregat.getOppgittRettighet()).isNotNull();
        assertThat(ytelseFordelingAggregat.getOppgittRettighet().getHarAnnenForeldreRett()).isFalse();
        assertThat(ytelseFordelingAggregat.getOppgittRettighet().getHarAleneomsorgForBarnet()).isTrue();
        assertThat(ytelseFordelingAggregat.getOppgittRettighet().getHarOmsorgForBarnetIHelePerioden()).isTrue();

        assertThat(ytelseFordelingAggregat.getOppgittDekningsgrad()).isNotNull();
        assertThat(ytelseFordelingAggregat.getOppgittDekningsgrad().getDekningsgrad()).isEqualTo(OppgittDekningsgradEntitet.ÅTTI_PROSENT);

        assertThat(ytelseFordelingAggregat.getOppgittFordeling()).isNotNull();
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).isNotEmpty();
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).hasSize(3); //Foreldrepenger før fødsel, mødrekvote og utsettelse
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).anySatisfy(periode -> assertThat(periode.getPeriodeType()).isEqualTo(UttakPeriodeType.FORELDREPENGER_FØR_FØDSEL));
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).anySatisfy(periode -> assertThat(periode.getPeriodeType()).isEqualTo(UttakPeriodeType.MØDREKVOTE));
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).anySatisfy(periode -> assertThat(periode.getErArbeidstaker()).isEqualTo(Boolean.TRUE));
        assertThat(ytelseFordelingAggregat.getOppgittFordeling().getOppgittePerioder()).anySatisfy(periode -> assertThat(periode.getVirksomhet().getOrgnr()).isEqualTo(virksomhetsNummer));
    }

    @Test
    public void test_mapFordeling_morAleneomsorg() {
        ManuellRegistreringForeldrepengerDto manuellRegistreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        PermisjonPeriodeDto permisjonPeriodeDto = opprettPermisjonPeriodeDto(LocalDate.now(), LocalDate.now().plusWeeks(3), UttakPeriodeType.MØDREKVOTE, null);
        oppdaterDtoForFødsel(manuellRegistreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now(), 1);
        manuellRegistreringForeldrepengerDto.setTidsromPermisjon(opprettTidsromPermisjonDto(true, false, Collections.singletonList(permisjonPeriodeDto)));

        Fordeling fordeling = SøknadMapperFP.mapFordeling(manuellRegistreringForeldrepengerDto);
        assertThat(fordeling).isNotNull();
    }

    @Test
    public void test_mapOpptjening_andreYtelser() {
        List<AnnenOpptjening> annenOpptjenings = SøknadMapperFP.mapAndreYtelser(opprettTestdataForAndreYtelser());
        assertThat(annenOpptjenings).as("Forventer en opptjening pr angitt ytelse.").hasSize(3);
    }

    @Test
    public void test_mapOpptjening_andreYtelser_null() {
        List<AnnenOpptjening> annenOpptjenings = SøknadMapperFP.mapAndreYtelser(null);
        assertThat(annenOpptjenings).as("Forventer en opptjening pr angitt ytelse.").hasSize(0);
    }

    @Test
    public void test_mapEgenNæring() {
        VirksomhetDto virksomhetDto = opprettNorskVirksomhetMedEndringUtenRegnskapsfører();
        EgenNaering egenNaering = SøknadMapperFP.mapEgenNæring(virksomhetDto, virksomhetTjeneste);
        assertThat(egenNaering).isNotNull();
        assertThat(egenNaering).isInstanceOf(NorskOrganisasjon.class);
        assertThat(egenNaering.getArbeidsland().getKode()).isEqualTo("NOR");
        assertThat(egenNaering.getVirksomhetstype().get(0).getKode()).isEqualTo(VirksomhetType.ANNEN.getKode());
    }

    @Test
    public void test_mapForeldrepengerMedEgenNæring() {
        ManuellRegistreringForeldrepengerDto registreringForeldrepengerDto = new ManuellRegistreringForeldrepengerDto();
        oppdaterDtoForFødsel(registreringForeldrepengerDto, FamilieHendelseType.FØDSEL, true, LocalDate.now().minusWeeks(3), 1);
        registreringForeldrepengerDto.setEgenVirksomhet(opprettEgenVirksomhetDto());
        when(tpsTjeneste.hentAktørForFnr(any())).thenReturn(Optional.ofNullable(STD_KVINNE_AKTØR_ID));
        SøknadMapperFP.mapTilForeldrepenger(registreringForeldrepengerDto, opprettBruker(), tpsTjeneste, virksomhetTjeneste);
    }

    @Test
    public void test_mapUtenlandskArbeidsforhold() {

        LocalDate periodeFom = LocalDate.now();
        LocalDate periodeTom = periodeFom.plusWeeks(10);
        ArbeidsforholdDto arbeidsforholdDto = opprettUtenlandskArbeidsforholdDto("arbg. navn", "FIN", periodeFom, periodeTom);
        List<UtenlandskArbeidsforhold> arbeidsforhold = SøknadMapperFP.mapAlleUtenlandskeArbeidsforhold(Collections.singletonList(arbeidsforholdDto));
        assertThat(arbeidsforhold).isNotNull();
        assertThat(arbeidsforhold).anySatisfy(arbForhold -> assertThat(arbForhold).isInstanceOf(UtenlandskArbeidsforhold.class));
        assertThat(arbeidsforhold).anySatisfy(arbForhold -> assertThat(arbForhold.getArbeidsgiversnavn()).isEqualTo("arbg. navn"));
        assertThat(arbeidsforhold).anySatisfy(arbForhold -> assertThat(arbForhold.getArbeidsland().getKode()).isEqualTo("FIN"));
    }

    @Test
    public void test_mapUtenlandskArbeidsforhold_null_liste() {
        List<UtenlandskArbeidsforhold> arbeidsforhold = SøknadMapperFP.mapAlleUtenlandskeArbeidsforhold(null);
        assertThat(arbeidsforhold).isEmpty();
    }

    @Test
    public void test_mapUtenlandskArbeidsforhold_null_element_i_liste() {
        ArbeidsforholdDto arbeidsforholdDto = new ArbeidsforholdDto();
        List<UtenlandskArbeidsforhold> arbeidsforhold = SøknadMapperFP.mapAlleUtenlandskeArbeidsforhold(Collections.singletonList(arbeidsforholdDto));
        assertThat(arbeidsforhold).isEmpty();
    }
}
