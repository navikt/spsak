package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktApplikasjonFeil.FACTORY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandling.steg.iverksettevedtak.HenleggBehandlingTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.transisjoner.TransisjonIdentifikator;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat.Builder;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring.Overstyringshåndterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AksjonspunktKode;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.BekreftetAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsprosessApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class AksjonspunktApplikasjonTjenesteImpl implements AksjonspunktApplikasjonTjeneste {
    private static final Logger LOGGER = LoggerFactory.getLogger(AksjonspunktApplikasjonTjenesteImpl.class);

    private BehandlingRepository behandlingRepository;

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;

    private AksjonspunktRepository aksjonspunktRepository;

    private HenleggBehandlingTjeneste henleggBehandlingTjeneste;

    private HistorikkTjenesteAdapter historikkTjenesteAdapter;

    private BehandlingsprosessApplikasjonTjeneste behandlingsprosessApplikasjonTjeneste;

    private EndringsresultatSjekker endringsresultatSjekker;

    public AksjonspunktApplikasjonTjenesteImpl() {
        // CDI proxy
    }

    @Inject
    public AksjonspunktApplikasjonTjenesteImpl(GrunnlagRepositoryProvider repositoryProvider,
                                               BehandlingskontrollTjeneste behandlingskontrollTjeneste,
                                               BehandlingsprosessApplikasjonTjeneste behandlingsprosessApplikasjonTjeneste,
                                               HistorikkTjenesteAdapter historikkTjenesteAdapter,
                                               HenleggBehandlingTjeneste henleggBehandlingTjeneste,
                                               EndringsresultatSjekker endringsresultatSjekker) {

        this.behandlingsprosessApplikasjonTjeneste = behandlingsprosessApplikasjonTjeneste;
        this.historikkTjenesteAdapter = historikkTjenesteAdapter;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.henleggBehandlingTjeneste = henleggBehandlingTjeneste;
        this.endringsresultatSjekker = endringsresultatSjekker;

        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();

    }

    @Override
    public void bekreftAksjonspunkter(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);

        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);
        setAnsvarligSaksbehandler(bekreftedeAksjonspunktDtoer, behandling);

        spoolTilbakeTilTidligsteAksjonspunkt(bekreftedeAksjonspunktDtoer, kontekst, false);

        OverhoppResultat overhoppResultat = bekreftAksjonspunkter(kontekst, bekreftedeAksjonspunktDtoer, behandling);

        historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);

        behandlingRepository.lagre(behandling.getBehandlingsresultat().getVilkårResultat(), kontekst.getSkriveLås());
        behandlingRepository.lagre(behandling, kontekst.getSkriveLås());

        håndterOverhopp(overhoppResultat, kontekst);

        if (behandling.isBehandlingPåVent()) {
            // Skal ikke fortsette behandling dersom behandling ble satt på vent
            return;
        }
        behandlingsprosessApplikasjonTjeneste.asynkKjørProsess(behandling);// skal ikke reinnhente her, avgjøres i steg?

    }

    protected void setAnsvarligSaksbehandler(Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Behandling behandling) {
        if (bekreftedeAksjonspunktDtoer.stream().anyMatch(dto -> dto instanceof FatterVedtakAksjonspunktDto)) {
            return;
        }
        behandling.setAnsvarligSaksbehandler(getCurrentUserId());
    }

    String getCurrentUserId() {
        return SubjectHandler.getSubjectHandler().getUid();
    }

    private void spoolTilbakeTilTidligsteAksjonspunkt(Collection<? extends AksjonspunktKode> aksjonspunktDtoer,
                                                      BehandlingskontrollKontekst kontekst,
                                                      boolean erOverstyring) {
        // NB: Første løsning på tilbakeføring ved endring i GUI (når aksjonspunkter tilhørende eldre enn aktivt steg
        // sendes inn spoles prosessen
        // tilbake). Vil utvides etter behov når regler for spoling bakover blir klarere.
        // Her sikres at behandlingskontroll hopper tilbake til aksjonspunktenes tidligste "løsesteg" dersom aktivt
        // behandlingssteg er lenger fremme i sekvensen
        List<String> bekreftedeApKoder = aksjonspunktDtoer.stream()
            .map(dto -> dto.getKode())
            .collect(toList());

        behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligsteAksjonspunkt(kontekst, bekreftedeApKoder, erOverstyring);
    }

    private void håndterOverhopp(OverhoppResultat overhoppResultat, BehandlingskontrollKontekst kontekst) {
        // TODO (essv): PKMANTIS-1992 Skrive om alle overhopp til å bruke transisjon (se fremoverTransisjon nedenfor)
        Optional<OppdateringResultat> funnetHenleggelse = overhoppResultat.finnHenleggelse();
        if (funnetHenleggelse.isPresent()) {
            OppdateringResultat henleggelse = funnetHenleggelse.get();
            henleggBehandlingTjeneste.henleggBehandling(kontekst.getBehandlingId(),
                henleggelse.getHenleggelseResultat(), henleggelse.getHenleggingsbegrunnelse());
            return;
        }

        Optional<BehandlingStegType> nesteTilbakesteg = overhoppResultat.finnTilbakehoppSteg();
        if (nesteTilbakesteg.isPresent()) {
            behandlingskontrollTjeneste.behandlingTilbakeføringTilTidligereBehandlingSteg(kontekst, nesteTilbakesteg.get());
            return;
        }

        Optional<TransisjonIdentifikator> fremoverTransisjon = overhoppResultat.finnFremoverTransisjon();
        if (fremoverTransisjon.isPresent()) {
            behandlingskontrollTjeneste.fremoverTransisjon(fremoverTransisjon.get(), kontekst);
            return;
        }
    }

    @Override
    public void overstyrAksjonspunkter(Collection<OverstyringAksjonspunktDto> overstyrteAksjonspunkter, Long behandlingId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        BehandlingskontrollKontekst kontekst = behandlingskontrollTjeneste.initBehandlingskontroll(behandlingId);

        OverhoppResultat overhoppForOverstyring = overstyrVilkårEllerBeregning(overstyrteAksjonspunkter, behandling, kontekst);

        List<Aksjonspunkt> utførteAksjonspunkter = lagreHistorikkInnslag(behandling);

        behandlingskontrollTjeneste.aksjonspunkterUtført(kontekst, utførteAksjonspunkter, behandling.getAktivtBehandlingSteg());

        // Fremoverhopp hvis vilkår settes til AVSLÅTT
        håndterOverhopp(overhoppForOverstyring, kontekst);

        if (behandling.isBehandlingPåVent()) {
            // Skal ikke fortsette behandling dersom behandling ble satt på vent
            return;
        }
        behandlingsprosessApplikasjonTjeneste.asynkKjørProsess(behandling);// skal ikke reinnhente her, avgjøres i steg?
    }

    private boolean harVilkårResultat(Behandling behandling) {
        return behandling.getBehandlingsresultat() != null &&
            behandling.getBehandlingsresultat().getVilkårResultat() != null;
    }

    @SuppressWarnings("unchecked")
    private OverhoppResultat overstyrVilkårEllerBeregning(Collection<OverstyringAksjonspunktDto> overstyrteAksjonspunkter,
                                                          Behandling behandling, BehandlingskontrollKontekst kontekst) {
        OverhoppResultat overhoppResultat = OverhoppResultat.tomtResultat();

        // oppdater for overstyring
        overstyrteAksjonspunkter.forEach(dto -> {
            EndringsresultatSnapshot snapshotFør = endringsresultatSjekker.opprettEndringsresultatIdPåBehandlingSnapshot(behandling);

            @SuppressWarnings("rawtypes")
            Overstyringshåndterer overstyringshåndterer = finnOverstyringshåndterer(dto);
            OppdateringResultat oppdateringResultat = overstyringshåndterer.håndterOverstyring(dto, behandling, kontekst);
            overhoppResultat.leggTil(oppdateringResultat);

            settToTrinnPåOverstyrtAksjonspunktHvisEndring(behandling, dto, snapshotFør);
        });

        // Tilbakestill gjeldende steg før fremføring
        spoolTilbakeTilTidligsteAksjonspunkt(overstyrteAksjonspunkter, kontekst, true);

        // legg til overstyring aksjonspunkt (normalt vil være utført) og historikk
        overstyrteAksjonspunkter.forEach(dto -> {
            @SuppressWarnings("rawtypes")
            Overstyringshåndterer overstyringshåndterer = finnOverstyringshåndterer(dto);
            overstyringshåndterer.håndterAksjonspunktForOverstyring(dto, behandling);
        });

        return overhoppResultat;
    }

    private List<Aksjonspunkt> lagreHistorikkInnslag(Behandling behandling) {

        // TODO(FC): Kan vi flytte spesielhåndtering av SØKERS_OPPLYSNINGSPLIKT_OVST ned til
        // SøkersOpplysningspliktOverstyringshåndterer?
        // vis vi aldri sender inn mer enn en overstyring kan historikk opprettes også der.

        List<Aksjonspunkt> utførteAksjonspunkter = behandling
            .getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon.SØKERS_OPPLYSNINGSPLIKT_OVST)
            .map(Collections::singletonList)
            .orElse(emptyList());
        if (utførteAksjonspunkter.isEmpty()) {
            historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.OVERSTYRT);
        } else {
            // SØKERS_OPPLYSNINGSPLIKT_OVST skal gi et "vanlig" historikkinnslag
            historikkTjenesteAdapter.opprettHistorikkInnslag(behandling, HistorikkinnslagType.FAKTA_ENDRET);
        }

        return utførteAksjonspunkter;
    }

    private OverhoppResultat bekreftAksjonspunkter(BehandlingskontrollKontekst kontekst,
                                                   Collection<BekreftetAksjonspunktDto> bekreftedeAksjonspunktDtoer, Behandling behandling) {

        List<Aksjonspunkt> utførteAksjonspunkter = new ArrayList<>();
        OverhoppResultat overhoppResultat = OverhoppResultat.tomtResultat();

        VilkårResultat.Builder vilkårBuilder = harVilkårResultat(behandling)
            ? VilkårResultat.builderFraEksisterende(behandling.getBehandlingsresultat().getVilkårResultat())
            : VilkårResultat.builder();

        bekreftedeAksjonspunktDtoer.forEach(dto -> bekreftAksjonspunkt(behandling, vilkårBuilder, utførteAksjonspunkter, overhoppResultat, dto));

        VilkårResultat vilkårResultat = vilkårBuilder.buildFor(behandling);
        behandlingRepository.lagre(vilkårResultat, kontekst.getSkriveLås());

        behandlingskontrollTjeneste.oppdaterBehandling(behandling, kontekst);

        behandlingskontrollTjeneste.aksjonspunkterUtført(kontekst, utførteAksjonspunkter, behandling.getAktivtBehandlingSteg());

        return overhoppResultat;
    }

    private void bekreftAksjonspunkt(Behandling behandling, Builder vilkårBuilder, List<Aksjonspunkt> utførteAksjonspunkter, OverhoppResultat overhoppResultat, BekreftetAksjonspunktDto dto) {
        // Endringskontroll for aksjonspunkt

        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aksjonspunktDefinisjon);

        Instance<Object> instance = finnAksjonspunktOppdaterer(dto.getClass());

        OppdateringResultat delresultat;
        if (instance.isUnsatisfied()) {
            throw FACTORY.kanIkkeFinneAksjonspunktUtleder(dto.getKode()).toException();
        } else {
            EndringsresultatSnapshot snapshotFør = endringsresultatSjekker.opprettEndringsresultatIdPåBehandlingSnapshot(behandling);

            Object minInstans = instance .get();
            if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                    "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
            }

            @SuppressWarnings("unchecked")
            AksjonspunktOppdaterer<BekreftetAksjonspunktDto> oppdaterer = (AksjonspunktOppdaterer<BekreftetAksjonspunktDto>) minInstans;
            delresultat = oppdaterer.oppdater(dto, behandling, vilkårBuilder);
            overhoppResultat.leggTil(delresultat);

            settToTrinnHvisRevurderingOgEndring(behandling, aksjonspunkt, dto.getBegrunnelse(), snapshotFør);
        }

        if (kanUtføreAksjonspunkt(aksjonspunkt, delresultat)) {
            if (aksjonspunktRepository.setTilUtført(aksjonspunkt, dto.getBegrunnelse())) {
                utførteAksjonspunkter.add(aksjonspunkt);
            }
        }
    }

    private boolean kanUtføreAksjonspunkt(Aksjonspunkt aksjonspunkt, OppdateringResultat delresultat) {
        return !(aksjonspunkt.erBehandletAksjonspunkt() || aksjonspunkt.erAvbrutt()) && delresultat.skalUtføreAksjonspunkt();
    }

    private Instance<Object> finnAksjonspunktOppdaterer(Class<?> dtoClass) {
        return finnAdapter(dtoClass, AksjonspunktOppdaterer.class);
    }

    private Instance<Object> finnAdapter(Class<?> cls, final Class<?> targetAdapter) {
        CDI<Object> cdi = CDI.current();
        Instance<Object> instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, targetAdapter));

        // hvis unsatisfied, søk parent
        while (instance.isUnsatisfied() && !Objects.equals(Object.class, cls)) {
            cls = cls.getSuperclass();
            instance = cdi.select(new DtoTilServiceAdapter.Literal(cls, targetAdapter));
            if (!instance.isUnsatisfied()) {
                return instance;
            }
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    private <V extends OverstyringAksjonspunktDto> Overstyringshåndterer<V> finnOverstyringshåndterer(V dto) {
        // TODO (FC): Dette Krever overstyringshåndterer definert per OverstyringAksjonspunktDto. Bør relaxes? Mest
        // sannsynlig utvikler-feil hvis savnes

        Instance<Object> instance = finnAdapter(dto.getClass(), Overstyringshåndterer.class);

        if (instance.isUnsatisfied()) {
            throw FACTORY.kanIkkeFinneOverstyringshåndterer(dto.getClass().getSimpleName()).toException();
        } else {
            Object minInstans = instance.get();
            if (minInstans.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                    "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: " + minInstans.getClass());
            }
            return (Overstyringshåndterer<V>) minInstans;
        }
    }

    private void settToTrinnPåOverstyrtAksjonspunktHvisEndring(Behandling behandling, OverstyringAksjonspunktDto dto, EndringsresultatSnapshot snapshotFør) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        if (behandling.harAksjonspunktMedType(aksjonspunktDefinisjon)) {
            Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aksjonspunktDefinisjon);
            settToTrinnHvisRevurderingOgEndring(behandling, aksjonspunkt, dto.getBegrunnelse(), snapshotFør);
        }
    }

    private void settToTrinnHvisRevurderingOgEndring(Behandling behandling, Aksjonspunkt aksjonspunkt,
                                                     String nyBegrunnelse, EndringsresultatSnapshot snapshotFør) {
        if (aksjonspunkt.erRevurdering() && aksjonspunktStøtterTotrinn(aksjonspunkt) && !aksjonspunkt.isToTrinnsBehandling()) {
            EndringsresultatDiff endringsresultatDiff = endringsresultatSjekker.finnIdEndringerPåBehandling(behandling, snapshotFør);
            boolean idEndret = endringsresultatDiff.erIdEndret();
            boolean begrunnelseEndret = begrunnelseErEndret(aksjonspunkt, nyBegrunnelse);
            if (idEndret || begrunnelseEndret) {
                LOGGER.info("Revurdert aksjonspunkt {} på Behandling {} har endring (id={}, begrunnelse={}) - setter totrinnskontroll", //$NON-NLS-1$
                    aksjonspunkt.getAksjonspunktDefinisjon().getKode(), behandling.getId(), idEndret, begrunnelseEndret); // NOSONAR
                aksjonspunktRepository.setToTrinnsBehandlingKreves(aksjonspunkt);
            }
        }
    }

    private boolean aksjonspunktStøtterTotrinn(Aksjonspunkt aksjonspunkt) {
        // TODO MAUR en mer generell måte. Ikke alle som endrer grunnlaget som støttes i totrinn
        return !asList(AksjonspunktDefinisjon.FORESLÅ_VEDTAK,
            AksjonspunktDefinisjon.KONTROLL_AV_MANUELT_OPPRETTET_REVURDERINGSBEHANDLING)
            .contains(aksjonspunkt.getAksjonspunktDefinisjon());
    }

    private boolean begrunnelseErEndret(Aksjonspunkt aksjonspunkt, String nyBegrunnelse) {
        return !Objects.equals(aksjonspunkt.getBegrunnelse(), nyBegrunnelse);
    }
}
