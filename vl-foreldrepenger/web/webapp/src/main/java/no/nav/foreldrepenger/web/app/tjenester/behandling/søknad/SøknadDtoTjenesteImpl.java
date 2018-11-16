package no.nav.foreldrepenger.web.app.tjenester.behandling.søknad;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.Kompletthetsjekker;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.KompletthetsjekkerProvider;
import no.nav.foreldrepenger.domene.mottak.kompletthettjeneste.ManglendeVedlegg;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.web.app.tjenester.behandling.SøknadType;

@ApplicationScoped
public class SøknadDtoTjenesteImpl implements SøknadDtoTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private KompletthetsjekkerProvider kompletthetsjekkerProvider;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private TpsTjeneste tpsTjeneste;

    SøknadDtoTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public SøknadDtoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                 SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                 KompletthetsjekkerProvider kompletthetsjekkerProvider,
                                 TpsTjeneste tpsTjeneste) {
        this.repositoryProvider = repositoryProvider;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.kompletthetsjekkerProvider = kompletthetsjekkerProvider;
        this.tpsTjeneste = tpsTjeneste;
        this.fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();
    }

    @Override
    public Optional<SoknadDto> mapFra(Behandling behandling) {
        Optional<Søknad> søknadOpt = repositoryProvider.getSøknadRepository().hentSøknadHvisEksisterer(behandling);
        if (søknadOpt.isPresent()) {
            Søknad søknad = søknadOpt.get();
        }
        return Optional.empty();
    }

    private Optional<SoknadDto> lagSoknadFodselDto(Søknad søknad, Behandling behandling) {
        SoknadFodselDto soknadFodselDto = new SoknadFodselDto();
        soknadFodselDto.setMottattDato(søknad.getMottattDato());
        soknadFodselDto.setTilleggsopplysninger(søknad.getTilleggsopplysninger());
        soknadFodselDto.setSoknadType(SøknadType.FØDSEL);
        soknadFodselDto.setBegrunnelseForSenInnsending(søknad.getBegrunnelseForSenInnsending());
        soknadFodselDto.setFarSokerType(søknad.getFarSøkerType());
        soknadFodselDto.setOppgittTilknytning(OppgittTilknytningDto.mapFra(søknad));
        soknadFodselDto.setOppgittRettighet(OppgittRettighetDto.mapFra(søknad));
        soknadFodselDto.setOppgittFordeling(OppgittFordelingDto.mapFra(søknad, hentOppgittStartdatoForPermisjon(behandling)));
        soknadFodselDto.setManglendeVedlegg(genererManglendeVedlegg(behandling));
        soknadFodselDto.setDekningsgrad(hentDekningsgrad(behandling).orElse(null));

        return Optional.of(soknadFodselDto);
    }

    private List<ManglendeVedleggDto> genererManglendeVedlegg(Behandling behandling) {
        Kompletthetsjekker kompletthetsjekker = kompletthetsjekkerProvider.finnKompletthetsjekkerFor(behandling);

        final List<ManglendeVedlegg> alleManglendeVedlegg = kompletthetsjekker.utledAlleManglendeVedleggForForsendelse(behandling);
        final List<ManglendeVedlegg> vedleggSomIkkeKommer = kompletthetsjekker.utledAlleManglendeVedleggSomIkkeKommer(behandling);

        // Fjerner slik at det ikke blir dobbelt opp, og for å markere korrekt hvilke som ikke vil komme
        alleManglendeVedlegg.removeIf(e -> vedleggSomIkkeKommer.stream().anyMatch(it -> it.getArbeidsgiver().equals(e.getArbeidsgiver())));
        alleManglendeVedlegg.addAll(vedleggSomIkkeKommer);

        return alleManglendeVedlegg.stream().map(this::mapTilManglendeVedleggDto).collect(Collectors.toList());
    }

    private ManglendeVedleggDto mapTilManglendeVedleggDto(ManglendeVedlegg mv) {
        final ManglendeVedleggDto dto = new ManglendeVedleggDto();
        dto.setDokumentType(repositoryProvider.getKodeverkRepository().finn(DokumentTypeId.class, mv.getDokumentType().getKode()));
        if (mv.getDokumentType().equals(DokumentTypeId.INNTEKTSMELDING)) {
            dto.setArbeidsgiver(mapTilArbeidsgiverDto(mv.getArbeidsgiver()));
            dto.setBrukerHarSagtAtIkkeKommer(mv.getBrukerHarSagtAtIkkeKommer());
        }
        return dto;
    }

    private VirksomhetDto mapTilArbeidsgiverDto(String arbeidsgiverIdent) {
        final Optional<Virksomhet> arbeidsgiver = repositoryProvider.getVirksomhetRepository().hent(arbeidsgiverIdent);
        final VirksomhetDto dto = new VirksomhetDto();
        if (arbeidsgiver.isPresent()) {
            final Virksomhet virksomhet = arbeidsgiver.get();
            dto.setNavn(virksomhet.getNavn());
            dto.setOrganisasjonsnummer(virksomhet.getOrgnr());
        } else {
            Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(new AktørId(arbeidsgiverIdent));
            dto.setOrganisasjonsnummer(arbeidsgiverIdent);
            if (personinfo.isPresent()) {
                dto.setOrganisasjonsnummer(personinfo.get().getPersonIdent().getIdent());
                dto.setNavn(personinfo.get().getNavn());
            }
        }
        return dto;
    }

    private Optional<SoknadDto> lagSoknadAdopsjonDto(Søknad søknad, Behandling behandling) {
        SoknadAdopsjonDto soknadAdopsjonDto = new SoknadAdopsjonDto();
        soknadAdopsjonDto.setMottattDato(søknad.getMottattDato());
        soknadAdopsjonDto.setTilleggsopplysninger(søknad.getTilleggsopplysninger());
        soknadAdopsjonDto.setSoknadType(SøknadType.ADOPSJON);
        soknadAdopsjonDto.setFarSokerType(søknad.getFarSøkerType());
        soknadAdopsjonDto.setBegrunnelseForSenInnsending(søknad.getBegrunnelseForSenInnsending());
        soknadAdopsjonDto.setOppgittTilknytning(OppgittTilknytningDto.mapFra(søknad));
        soknadAdopsjonDto.setOppgittRettighet(OppgittRettighetDto.mapFra(søknad));
        soknadAdopsjonDto.setOppgittFordeling(OppgittFordelingDto.mapFra(søknad, hentOppgittStartdatoForPermisjon(behandling)));
        soknadAdopsjonDto.setDekningsgrad(hentDekningsgrad(behandling).orElse(null));
        soknadAdopsjonDto.setManglendeVedlegg(genererManglendeVedlegg(behandling));
        return Optional.of(soknadAdopsjonDto);
    }

    private Optional<LocalDate> hentOppgittStartdatoForPermisjon(Behandling behandling) {
        return Optional.of(skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling));
    }

    private Optional<Integer> hentDekningsgrad(Behandling behandling) {
        Optional<FagsakRelasjon> fagsakRelasjon = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak());
        if (fagsakRelasjon.isPresent()) {
            return Optional.ofNullable(fagsakRelasjon.get().getDekningsgrad().getVerdi());
        }
        return Optional.empty();
    }
}
