package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonAdresse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personopplysning;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Personstatus;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Statsborgerskap;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;

@ApplicationScoped
public class PersonopplysningDtoTjenesteImpl implements PersonopplysningDtoTjeneste {

    private PersonopplysningTjeneste personopplysningTjeneste;
    private GrunnlagRepositoryProvider repositoryProvider;

    PersonopplysningDtoTjenesteImpl() {
    }

    @Inject
    public PersonopplysningDtoTjenesteImpl(PersonopplysningTjeneste personopplysningTjeneste, GrunnlagRepositoryProvider repositoryProvider) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.repositoryProvider = repositoryProvider;
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

    private static boolean harVerge(Long behandlingId, GrunnlagRepositoryProvider provider) {
        Optional<VergeAggregat> verge = provider.getVergeGrunnlagRepository().hentAggregat(behandlingId);
        return verge.isPresent();
    }

    @Override
    public Optional<PersonopplysningDto> lagPersonopplysningDto(Long behandlingId, LocalDate tidspunkt) {
        Behandling behandling = repositoryProvider.getBehandlingRepository().hentBehandling(behandlingId);
        Optional<PersonopplysningerAggregat> aggregatOpt = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunktHvisEksisterer(behandling, tidspunkt);

        if (aggregatOpt.isPresent()) {
            PersonopplysningerAggregat aggregat = aggregatOpt.get();
            return Optional.ofNullable(aggregat.getSøker())
                .map(søker -> mapPersonopplysningDto(behandlingId, søker, aggregat));
        }
        return Optional.empty();
    }

    private PersonopplysningDto mapPersonopplysningDto(Long behandlingId, Personopplysning søker,
                                                       PersonopplysningerAggregat aggregat) {

        PersonopplysningDto dto = enkelMapping(søker, aggregat);

        repositoryProvider.getSøknadRepository().hentSøknad(behandlingId);

        if (harVerge(behandlingId, repositoryProvider)) {
            dto.setHarVerge(true);
        }
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
