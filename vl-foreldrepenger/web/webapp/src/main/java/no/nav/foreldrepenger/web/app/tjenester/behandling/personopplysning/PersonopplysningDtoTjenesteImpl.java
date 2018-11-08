package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.FødtBarnInfo;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OppgittAnnenPart;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.domene.person.TpsFamilieTjeneste;
import no.nav.foreldrepenger.domene.person.impl.TpsFødselUtil;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.søknad.SøknadDtoFeil;

@ApplicationScoped
public class PersonopplysningDtoTjenesteImpl implements PersonopplysningDtoTjeneste {

    private PersonopplysningTjeneste personopplysningTjeneste;
    private BehandlingRepositoryProvider repositoryProvider;
    private TpsFamilieTjeneste tpsFamilieTjeneste;

    PersonopplysningDtoTjenesteImpl() {
    }

    @Inject
    public PersonopplysningDtoTjenesteImpl(PersonopplysningTjeneste personopplysningTjeneste, BehandlingRepositoryProvider repositoryProvider, TpsFamilieTjeneste tpsFamilieTjeneste) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.repositoryProvider = repositoryProvider;
        this.tpsFamilieTjeneste = tpsFamilieTjeneste;
    }

    private static List<PersonadresseDto> lagAddresseDto(Personopplysning personopplysning, PersonopplysningerAggregat aggregat) {
        List<PersonAdresse> adresser = aggregat.getAdresserFor(personopplysning.getAktørId());
        return adresser.stream().map(e -> lagDto(e, personopplysning.getNavn())).collect(Collectors.toList());
    }

    private static LandkoderDto lagLandkoderDto(Landkoder landkode) {
        LandkoderDto dto = new LandkoderDto();
        dto.setKode(landkode.getKode());
        dto.setKodeverk(landkode.getKodeverk());
        dto.setNavn(formaterMedStoreOgSmåBokstaver(landkode.getNavn()));
        return dto;
    }

    private static PersonadresseDto lagDto(PersonAdresse adresse, String navn) {
        PersonadresseDto dto = new PersonadresseDto();
        dto.setAdresselinje1(formaterMedStoreOgSmåBokstaver(adresse.getAdresselinje1()));
        dto.setAdresselinje2(formaterMedStoreOgSmåBokstaver(adresse.getAdresselinje2()));
        dto.setAdresselinje3(formaterMedStoreOgSmåBokstaver(adresse.getAdresselinje3()));
        dto.setMottakerNavn(formaterMedStoreOgSmåBokstaver(navn));
        dto.setPoststed(formaterMedStoreOgSmåBokstaver(adresse.getPoststed()));
        dto.setPostNummer(adresse.getPostnummer());
        dto.setLand(adresse.getLand());
        dto.setAdresseType(adresse.getAdresseType());
        return dto;

    }

    private static String formaterMedStoreOgSmåBokstaver(String tekst) {
        if (tekst == null || (tekst = tekst.trim()).isEmpty()) { // NOSONAR
            return null;
        }
        String skilletegnPattern = "(\\s|[()\\-_.,/])";
        char[] tegn = tekst.toLowerCase(Locale.getDefault()).toCharArray();
        boolean nesteSkalHaStorBokstav = true;
        for (int i = 0; i < tegn.length; i++) {
            boolean erSkilletegn = String.valueOf(tegn[i]).matches(skilletegnPattern);
            if (!erSkilletegn && nesteSkalHaStorBokstav) {
                tegn[i] = Character.toTitleCase(tegn[i]);
            }
            nesteSkalHaStorBokstav = erSkilletegn;
        }
        return new String(tegn);
    }

    private static boolean harVerge(Long behandlingId, BehandlingRepositoryProvider provider) {
        Optional<VergeAggregat> verge = provider.getVergeGrunnlagRepository().hentAggregat(behandlingId);
        return verge.isPresent();
    }

    @Override
    public Optional<PersonopplysningDto> lagPersonopplysningDto(Long behandlingId, LocalDate tidspunkt) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
        Optional<PersonopplysningerAggregat> aggregatOpt = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunktHvisEksisterer(behandling, tidspunkt);

        if (aggregatOpt.isPresent()) {
            Optional<FamilieHendelseGrunnlag> familieHendelseAggregat = repositoryProvider.getFamilieGrunnlagRepository()
                .hentAggregatHvisEksisterer(behandlingId);
            PersonopplysningerAggregat aggregat = aggregatOpt.get();
            return Optional.ofNullable(aggregat.getSøker())
                .map(søker -> mapPersonopplysningDto(behandlingId, søker, aggregat, familieHendelseAggregat));
        }
        return Optional.empty();
    }

    private PersonopplysningDto mapPersonopplysningDto(Long behandlingId, Personopplysning søker,
                                                       PersonopplysningerAggregat aggregat, Optional<FamilieHendelseGrunnlag> familieHendelseAggregat) {

        PersonopplysningDto dto = enkelMapping(søker, aggregat);

        dto.setBarn(aggregat.getBarna()
            .stream()
            //.filter(p -> OpplysningsKilde.TPS.equals(p.getOpplysningsKilde()))
            .map(e -> enkelMapping(e, aggregat))
            .collect(Collectors.toList()));

        dto.setBarnSoktFor(Collections.emptyList());

        Søknad søknad = repositoryProvider.getSøknadRepository().hentSøknad(behandlingId);

        if (familieHendelseAggregat.isPresent()) {

            final FamilieHendelseGrunnlag grunnlag = familieHendelseAggregat.get();
            if (TpsFødselUtil.kanFinneForventetFødselIntervall(grunnlag, søknad)) {
                dto.setBarnFraTpsRelatertTilSoknad(hentFødteBarnFraTps(behandlingId, grunnlag));
            }
            dto.setBarnSoktFor(grunnlag.getGjeldendeBarna().stream()
                .map(this::enkelFHMapping)
                .collect(Collectors.toList()));
        }

        Optional<OppgittAnnenPart> oppgittAnnenPart = aggregat.getOppgittAnnenPart();

        if (oppgittAnnenPart.isPresent()) {
            Optional<PersonopplysningDto> annenPart = mapAnnenPart(søker, aggregat, oppgittAnnenPart.get());
            annenPart.ifPresent(dto::setAnnenPart);
        }
        Optional<Personopplysning> ektefelleOpt = aggregat.getEktefelle();
        if (ektefelleOpt.isPresent() && ektefelleOpt.get().equals(søker)) {
            throw SøknadDtoFeil.FACTORY.kanIkkeVæreSammePersonSomSøker().toException();
        }

        if (ektefelleOpt.isPresent()) {
            PersonopplysningDto ektefelle = enkelMapping(ektefelleOpt.get(), aggregat);
            dto.setEktefelle(ektefelle);
        }

        if (harVerge(behandlingId, repositoryProvider)) {
            dto.setHarVerge(true);
        }
        return dto;
    }

    private Optional<PersonopplysningDto> mapAnnenPart(Personopplysning søker, PersonopplysningerAggregat aggregat, OppgittAnnenPart oppgittAnnenPart) {
        if (søker.getAktørId().equals(oppgittAnnenPart.getAktørId())) {
            throw SøknadDtoFeil.FACTORY.kanIkkeVæreBådeFarOgMorTilEtBarn().toException();
        }

        PersonopplysningDto annenPart = null;
        Optional<Personopplysning> annenPartOpt = aggregat.getAnnenPart();

        if (annenPartOpt.isPresent()) {
            annenPart = enkelMapping(annenPartOpt.get(), aggregat);
        } else if (harOppgittLand(oppgittAnnenPart.getUtenlandskFnrLand())) {
            annenPart = enkelUtenlandskAnnenPartMapping(oppgittAnnenPart);
        }

        if (annenPart != null) {
            annenPart.setBarn(aggregat.getFellesBarn().stream()
                .map(e -> enkelMapping(e, aggregat))
                .collect(Collectors.toList()));
        }
        return Optional.ofNullable(annenPart);
    }

    private boolean harOppgittLand(Landkoder utenlandskFnrLand) {
        return utenlandskFnrLand != null && !Landkoder.UDEFINERT.equals(utenlandskFnrLand);
    }

    private List<PersonopplysningDto> hentFødteBarnFraTps(Long behandlingId, FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        BehandlingRepository behandlingRepository = repositoryProvider.getBehandlingRepository();
        List<FødtBarnInfo> fødteBarn = tpsFamilieTjeneste.getFødslerRelatertTilBehandling(behandlingRepository.hentBehandling(behandlingId), familieHendelseGrunnlag);

        return fødteBarn.stream().map(this::enkelFødtBarnMapping).collect(Collectors.toList());
    }

    private PersonopplysningDto enkelFødtBarnMapping(FødtBarnInfo fødtBarn) {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setFnr(fødtBarn.getIdent().getIdent());
        dto.setNavn(fødtBarn.getNavn());
        dto.setNavBrukerKjonn(fødtBarn.getKjønn());
        dto.setFodselsdato(fødtBarn.getFødselsdato());
        fødtBarn.getDødsdato().ifPresent(dto::setDodsdato);
        return dto;
    }


    private PersonopplysningDto enkelUtenlandskAnnenPartMapping(OppgittAnnenPart oppgittAnnenPart) {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setAvklartPersonstatus(new AvklartPersonstatus(PersonstatusType.UREG, PersonstatusType.UREG));
        dto.setPersonstatus(PersonstatusType.UREG);

        dto.setNavBrukerKjonn(NavBrukerKjønn.UDEFINERT);
        if (oppgittAnnenPart.getAktørId() != null) {
            dto.setAktoerId(oppgittAnnenPart.getAktørId());
        }
        dto.setNavn(oppgittAnnenPart.getNavn());
        if (oppgittAnnenPart.getUtenlandskFnrLand() != null) {
            dto.setStatsborgerskap(lagLandkoderDto(oppgittAnnenPart.getUtenlandskFnrLand()));
        }
        return dto;
    }


    private PersonopplysningDto enkelFHMapping(UidentifisertBarn uidentifisertBarn) {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setNummer(uidentifisertBarn.getBarnNummer());
        dto.setOpplysningsKilde(OpplysningsKilde.UDEFINERT);
        dto.setFodselsdato(uidentifisertBarn.getFødselsdato());
        return dto;
    }

    private PersonopplysningDto enkelMapping(Personopplysning personopplysning, PersonopplysningerAggregat aggregat) {
        PersonopplysningDto dto = new PersonopplysningDto();
        dto.setNavBrukerKjonn(personopplysning.getKjønn());
        final Optional<Landkoder> landkoder = aggregat.getStatsborgerskapFor(personopplysning.getAktørId()).stream().findFirst().map(Statsborgerskap::getStatsborgerskap);
        landkoder.ifPresent(landkoder1 -> dto.setStatsborgerskap(lagLandkoderDto(landkoder1)));
        final PersonstatusType gjeldendePersonstatus = aggregat.getPersonstatusFor(personopplysning.getAktørId()).getPersonstatus();
        dto.setPersonstatus(gjeldendePersonstatus);
        final AvklartPersonstatus avklartPersonstatus = new AvklartPersonstatus(aggregat.getOrginalPersonstatusFor(personopplysning.getAktørId())
            .map(Personstatus::getPersonstatus).orElse(gjeldendePersonstatus),
            gjeldendePersonstatus);
        dto.setAvklartPersonstatus(avklartPersonstatus);
        dto.setSivilstand(personopplysning.getSivilstand());

        dto.setAktoerId(personopplysning.getAktørId());
        dto.setNavn(formaterMedStoreOgSmåBokstaver(personopplysning.getNavn()));
        dto.setDodsdato(personopplysning.getDødsdato());
        dto.setAdresser(lagAddresseDto(personopplysning, aggregat));
        dto.setOpplysningsKilde(OpplysningsKilde.TPS);
        if (personopplysning.getRegion() != null) {
            dto.setRegion(personopplysning.getRegion());
        }
        dto.setFodselsdato(personopplysning.getFødselsdato());
        return dto;
    }

}
