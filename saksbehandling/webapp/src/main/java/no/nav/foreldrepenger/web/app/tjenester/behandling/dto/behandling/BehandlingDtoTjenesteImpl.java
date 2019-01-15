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
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsresultatRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregning.BeregningsresultatPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

/**
 * Bygger et sammensatt resultat av BehandlingDto ved å samle data fra ulike tjenester, for å kunne levere dette ut på en REST tjeneste.
 */
@ApplicationScoped
public class BehandlingDtoTjenesteImpl implements BehandlingDtoTjeneste {

    private static final String HENLEGG_ARSAKER_REL = "henlegg-arsaker";

    private BeregningsgrunnlagRepository beregningsgrunnlagRepository;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    private Unleash unleash;

    private BeregningsresultatRepository beregningsresultatRepository;
    private BehandlingRepository behandlingRepository;

    BehandlingDtoTjenesteImpl() {
        // for CDI proxy
    }

    @Inject
    public BehandlingDtoTjenesteImpl(ResultatRepositoryProvider resultatRepositoryProvider,
                                     SkjæringstidspunktTjeneste skjæringstidspunktTjeneste,
                                     Unleash unleash) {

        this.beregningsgrunnlagRepository = resultatRepositoryProvider.getBeregningsgrunnlagRepository();
        this.beregningsresultatRepository = resultatRepositoryProvider.getBeregningsresultatRepository();
        this.behandlingRepository = resultatRepositoryProvider.getBehandlingRepository();
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

    private void settStandardfelterUtvidet(Behandling behandling, Behandlingsresultat behandlingsresultat, UtvidetBehandlingDto dto) {
        // en til en mapping
        Long behandlingId = behandling.getId();
        setStandardfelter(behandling, dto);
        dto.setBehandlingPåVent(behandling.isBehandlingPåVent());
        dto.setBehandlingKøet(behandling.erKøet());
        dto.setAnsvarligSaksbehandler(behandling.getAnsvarligSaksbehandler());
        dto.setBehandlingHenlagt(behandlingsresultat != null && behandlingsresultat.isBehandlingHenlagt());
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

        lagBehandlingsresultatDto(behandling, behandlingsresultat).ifPresent(dto::setBehandlingsresultatDto);
    }

    private UtvidetBehandlingDto mapFra(Behandling behandling) {
        return lagDto(behandling);
    }

    private UtvidetBehandlingDto lagDto(Behandling behandling) {
        BehandlingIdDto idDto = new BehandlingIdDto(Long.parseLong(behandling.getFagsak().getSaksnummer().getVerdi()), behandling.getId());

        UtvidetBehandlingDto dto = new UtvidetBehandlingDto();
        Optional<Behandlingsresultat> behandlingsresultat = behandlingRepository.hentResultatHvisEksisterer(behandling.getId());
        settStandardfelterUtvidet(behandling, behandlingsresultat.orElse(null), dto);

        // mapping ved hjelp av tjenester
        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/soknad", "soknad", idDto));
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

        dto.leggTil(ResourceLink.post("/fpsak/api/behandling/opptjening", "opptjening", idDto));

        Optional<Beregningsgrunnlag> beregningsgrunnlag = beregningsgrunnlagRepository.hentBeregningsgrunnlag(behandling);
        if (beregningsgrunnlag.isPresent()) {
            dto.leggTil(ResourceLink.post("/fpsak/api/behandling/beregningsgrunnlag", "beregningsgrunnlag", idDto));
        }
        if (behandlingsresultat.isPresent()) {
            Optional<BeregningsresultatPerioder> beregningsresultat = beregningsresultatRepository.hentHvisEksisterer(behandlingsresultat.get());
            if (beregningsresultat.isPresent()) {
                dto.leggTil(ResourceLink.post("/fpsak/api/behandling/beregningsresultat/foreldrepenger", "beregningsresultat-foreldrepenger", idDto));
            }
        }
        return dto;
    }

    private Optional<BehandlingsresultatDto> lagBehandlingsresultatDto(Behandling behandling, Behandlingsresultat behandlingsresultat) {
        if (behandlingsresultat != null) {
            BehandlingsresultatDto dto = new BehandlingsresultatDto();
            dto.setId(behandlingsresultat.getId());
            dto.setType(behandlingsresultat.getBehandlingResultatType());
            dto.setSkjaeringstidspunktForeldrepenger(finnSkjæringstidspunktForSøknad(behandling));
            return Optional.of(dto);
        }
        return Optional.empty();
    }

    private LocalDate finnSkjæringstidspunktForSøknad(Behandling behandling) {
        return skjæringstidspunktTjeneste.utledSkjæringstidspunktFor(behandling);
    }

    private void lagSimuleringResultatLink(UtvidetBehandlingDto dto, BehandlingIdDto idDto) {
        if (simulerOppdragToggle()) {
            // gjør instansiering selv, siden da er konfig-verdi påkrevet bare når feature er lansert
            SimuleringApplikasjonUrlTjeneste simuleringUrlTjeneste = CDI.current().select(SimuleringApplikasjonUrlTjeneste.class).get();
            // trenger ikke destroy siden er ApplicationScoped
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
