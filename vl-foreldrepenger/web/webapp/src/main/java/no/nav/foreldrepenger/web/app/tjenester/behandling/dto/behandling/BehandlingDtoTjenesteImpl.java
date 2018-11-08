package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjonRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;
import no.nav.foreldrepenger.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.felles.integrasjon.unleash.FeatureToggle;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * Bygger et sammensatt resultat av BehandlingDto ved å samle data fra ulike tjenester, for å kunne levere dette ut på en REST tjeneste.
 */
@ApplicationScoped
public class BehandlingDtoTjenesteImpl implements BehandlingDtoTjeneste {

    private static final String HENLEGG_ARSAKER_REL = "henlegg-arsaker";

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private UttakRepository uttakRepository;
    private FagsakRelasjonRepository fagsakRelasjonRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private Unleash unleash;

    BehandlingDtoTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingDtoTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                     SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                     @FeatureToggle("fpsak") Unleash unleash) {

        this.beregningsgrunnlagRepository = repositoryProvider.getBeregningsgrunnlagRepository();
        this.uttakRepository = repositoryProvider.getUttakRepository();
        this.fagsakRelasjonRepository = repositoryProvider.getFagsakRelasjonRepository();
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
        this.unleash = unleash;
    }

    private static BehandlingDto lagBehandlingDto(Behandling behandling) {
        BehandlingDto dto = new BehandlingDto();
        setStandardfelter(behandling, dto);
        return dto;
    }

    private static void setStandardfelter(Behandling behandling, BehandlingDto dto) {
        dto.setFagsakId(behandling.getFagsakId());
        dto.setId(behandling.getId());
        dto.setVersjon(behandling.getVersjon());
        dto.setType(behandling.getType());
        dto.setOpprettet(behandling.getOpprettetDato());
        dto.setEndret(behandling.getEndretTidspunkt());
        dto.setAvsluttet(behandling.getAvsluttetDato());
        dto.setStatus(behandling.getStatus());
        dto.setBehandlendeEnhetId(behandling.getBehandlendeOrganisasjonsEnhet().getEnhetId());
        dto.setBehandlendeEnhetNavn(behandling.getBehandlendeOrganisasjonsEnhet().getEnhetNavn());
    }

    private static Optional<String> getFristDatoBehandlingPåVent(Behandling behandling) {
        LocalDate frist = behandling.getFristDatoBehandlingPåVent();
        if (frist != null) {
            return Optional.of(frist.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); //$NON-NLS-1$
        }
        return Optional.empty();
    }

    private static Optional<String> getVenteÅrsak(Behandling behandling) {
        Venteårsak venteårsak = behandling.getVenteårsak();
        if (venteårsak != null) {
            return Optional.of(venteårsak.getKode());
        }
        return Optional.empty();
    }

    private static Språkkode getSpråkkode(Behandling behandling) {
        return behandling.getFagsak().getNavBruker().getSpråkkode();
    }

    private static List<BehandlingÅrsakDto> lagBehandlingÅrsakDto(Behandling behandling) {
        if (!behandling.getBehandlingÅrsaker().isEmpty()) {
            return behandling.getBehandlingÅrsaker().stream().map(årsak -> {
                BehandlingÅrsakDto dto = new BehandlingÅrsakDto();
                dto.setBehandlingArsakType(årsak.getBehandlingÅrsakType());
                return dto;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<BehandlingDto> lagBehandlingDtoer(List<Behandling> behandlinger) {
        return behandlinger.stream().map(BehandlingDtoTjenesteImpl::lagBehandlingDto).collect(Collectors.toList());
    }

    @Override
    public UtvidetBehandlingDto lagUtvidetBehandlingDto(Behandling behandling, AsyncPollingStatus asyncStatus) {
        UtvidetBehandlingDto dto = mapFra(behandling);
        if (asyncStatus != null && !asyncStatus.isPending()) {
            dto.setAsyncStatus(asyncStatus);
        }
        return dto;
    }

    @Override
    public AnnenPartBehandlingDto lagAnnenPartBehandlingDto(Behandling behandling) {
        return AnnenPartBehandlingDto.mapFra(behandling);
    }

    private void settStandardfelterUtvidet(Behandling behandling, UtvidetBehandlingDto dto) {
        // en til en mapping
        Long behandlingId = behandling.getId();
        setStandardfelter(behandling, dto);
        dto.setBehandlingPåVent(behandling.isBehandlingPåVent());
        dto.setBehandlingKøet(behandling.erKøet());
        dto.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        dto.setBehandlingHenlagt(behandling.isBehandlingHenlagt());
        dto.setToTrinnsBehandling(behandling.isToTrinnsBehandling());
        BehandlingIdDto idDto = new BehandlingIdDto(Long.parseLong(behandling.getFagsak().getSaksnummer().getVerdi()), behandling.getId());
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/totrinnskontroll/arsaker", "totrinnskontroll-arsaker", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/totrinnskontroll/arsaker_read_only", "totrinnskontroll-arsaker-readOnly", idDto));

        // mapping ved hjelp av tjenester
        dto.leggTil(ResourceLink.get("/fpsak/api/behandling/aksjonspunkt?behandlingId=" + behandlingId, "aksjonspunkter", null));
        dto.leggTil(ResourceLink.get("/fpsak/api/behandling/vilkar?behandlingId=" + behandlingId, "vilkar", null));

        lagSimuleringResultatLink(dto, idDto);

        // hjelpe metoder
        getFristDatoBehandlingPåVent(behandling).ifPresent(dto::setFristBehandlingPåVent);
        getVenteÅrsak(behandling).ifPresent(dto::setVenteÅrsakKode);
        dto.setSpråkkode(getSpråkkode(behandling));

        dto.setBehandlingArsaker(lagBehandlingÅrsakDto(behandling));
        behandling.getOriginalBehandling().ifPresent(originalBehandling -> dto.setOriginalBehandlingId(originalBehandling.getId()));

        lagBehandlingsresultatDto(behandling).ifPresent(dto::setBehandlingsresultatDto);
    }

    private UtvidetBehandlingDto mapFra(Behandling behandling) {
        if (BehandlingType.INNSYN.equals(behandling.getType())) {
            return lagInnsynsbehandlingDto(behandling);
        } else if (BehandlingType.KLAGE.equals(behandling.getType())) {
            return lagKlagebehandlingDto(behandling);
        }
        return lagDto(behandling);
    }

    private UtvidetBehandlingDto lagKlagebehandlingDto(Behandling behandling) {
        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();
        settStandardfelterUtvidet(behandling, dto);

        dto.leggTil(ResourceLink.get("/fpsak/api/behandling/klage?behandlingId=" + behandling.getId(), "klage-vurdering", null));

        String henleggÅrsakerUrl = "/fpsak/api/kodeverk/henlegg/arsaker/klage";
        dto.leggTil(ResourceLink.get(henleggÅrsakerUrl, HENLEGG_ARSAKER_REL, null));
        return dto;
    }

    private UtvidetBehandlingDto lagInnsynsbehandlingDto(Behandling behandling) {
        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();

        settStandardfelterUtvidet(behandling, dto);

        dto.leggTil(ResourceLink.get("/fpsak/api/behandling/innsyn?behandlingId=" + behandling.getId(), "innsyn", null));

        String henleggÅrsakerUrl = "/fpsak/api/kodeverk/henlegg/arsaker/innsyn";
        dto.leggTil(ResourceLink.get(henleggÅrsakerUrl, HENLEGG_ARSAKER_REL, null));

        return dto;
    }

    private UtvidetBehandlingDto lagDto(Behandling behandling) {
        BehandlingIdDto idDto = new BehandlingIdDto(Long.parseLong(behandling.getFagsak().getSaksnummer().getVerdi()), behandling.getId());

        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();

        settStandardfelterUtvidet(behandling, dto);

        // mapping ved hjelp av tjenester
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/soknad", "soknad", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/familiehendelse", "familiehendelse", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/person/personopplysninger", "soeker-personopplysninger", idDto));
        if (unleash.isEnabled("fpsak.lopende-medlemskap")) {
            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/person/medlemskap-v2", "soeker-medlemskap-v2", idDto));
        }
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/person/medlemskap", "soeker-medlemskap", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/person/verge", "soeker-verge", idDto));

        dto.leggTil(ResourceLink.post("/fpsak/api/brev/varsel/revurdering", "sendt-varsel-om-revurdering", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/brev/maler", "brev-maler", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/brev/mottakere", "brev-mottakere", idDto));

        dto.leggTil(ResourceLink.get("/fpsak/api/kodeverk/henlegg/arsaker", HENLEGG_ARSAKER_REL, null));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/totrinnskontroll/arsaker", "totrinnskontroll-arsaker", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/totrinnskontroll/arsaker_read_only", "totrinnskontroll-arsaker-readOnly", idDto));
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/inntekt-arbeid-ytelse", "inntekt-arbeid-ytelse", idDto));

        if (FagsakYtelseType.ENGANGSTØNAD.equals(behandling.getFagsakYtelseType())) {
            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/beregningsresultat/engangsstonad", "beregningsresultat-engangsstonad", idDto));
        } else {
            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/ytelsefordeling", "ytelsefordeling", idDto));
            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/opptjening", "opptjening", idDto));

            Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
            if (beregningsgrunnlag.isPresent()) {
                dto.leggTil(ResourceLink.post("/fpsak/api/behandling/beregningsgrunnlag", "beregningsgrunnlag", idDto));
            }

            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/uttak/periode-grense", "uttak-periode-grense", idDto));
            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/uttak/kontroller-fakta-perioder", "uttak-kontroller-fakta-perioder", idDto));
            Optional<Stønadskontoberegning> stønadskontoberegning = fagsakRelasjonRepository.finnRelasjonForHvisEksisterer(behandling.getFagsak())
                .flatMap(FagsakRelasjon::getStønadskontoberegning);
            if (stønadskontoberegning.isPresent()) {
                dto.leggTil(ResourceLink.post("/fpsak/api/behandling/uttak/stonadskontoer", "uttak-stonadskontoer", idDto));
            }

            Optional<UttakResultatEntitet> uttakResultatHvisEksisterer = uttakRepository.hentUttakResultatHvisEksisterer(behandling);
            if (uttakResultatHvisEksisterer.isPresent()) {
                dto.leggTil(ResourceLink.post("/fpsak/api/behandling/uttak/resultat-perioder", "uttaksresultat-perioder", idDto));
                dto.leggTil(ResourceLink.post("/fpsak/api/behandling/beregningsresultat/foreldrepenger", "beregningsresultat-foreldrepenger", idDto));
            }
        }

        return dto;
    }

    private Optional<BehandlingsresultatDto> lagBehandlingsresultatDto(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat != null) {
            BehandlingsresultatDto dto = new BehandlingsresultatDto();
            dto.setId(behandlingsresultat.getId());
            dto.setType(behandlingsresultat.getBehandlingResultatType());
            dto.setAvslagsarsak(behandlingsresultat.getAvslagsårsak());
            dto.setAvslagsarsakFritekst(behandlingsresultat.getAvslagarsakFritekst());
            dto.setKonsekvenserForYtelsen(behandlingsresultat.getKonsekvenserForYtelsen());
            dto.setRettenTil(behandlingsresultat.getRettenTil());
            dto.setVedtaksbrev(behandlingsresultat.getVedtaksbrev());
            dto.setOverskrift(behandlingsresultat.getOverskrift());
            dto.setFritekstbrev(behandlingsresultat.getFritekstbrev());
            dto.setSkjaeringstidspunktForeldrepenger(finnSkjæringstidspunktForSøknad(behandling));
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    private LocalDate finnSkjæringstidspunktForSøknad(Behandling behandling) {
        if (!behandling.erYtelseBehandling()) {
            return null;
        }
        return skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
    }

    private void lagSimuleringResultatLink(UtvidetBehandlingDto dto, BehandlingIdDto idDto) {
        if (simulerOppdragToggle()) {
            //gjør instansiering selv, siden da er konfig-verdi påkrevet bare når feature er lansert
            SimuleringApplikasjonUrlTjeneste simuleringUrlTjeneste = CDI.current().select(SimuleringApplikasjonUrlTjeneste.class).get();
            //trenger ikke destroy siden er ApplicationScoped
            String url = simuleringUrlTjeneste.getUrlForSimuleringsresultat();
            ResourceLink simuleringResultatLink = ResourceLink.post(url, "simuleringResultat", idDto);
            dto.leggTil(simuleringResultatLink);
        }
    }

    private boolean simulerOppdragToggle() {
        String saksbehanlerIdent = SubjectHandler.getSubjectHandler().getUid();
        UnleashContext build = UnleashContext.builder()
            .addProperty(ByAnsvarligSaksbehandlerStrategy.SAKSBEHANDLER_IDENT, saksbehanlerIdent)
            .build();
        return unleash.isEnabled("fpsak.simuler-oppdrag", build);
    }

}
