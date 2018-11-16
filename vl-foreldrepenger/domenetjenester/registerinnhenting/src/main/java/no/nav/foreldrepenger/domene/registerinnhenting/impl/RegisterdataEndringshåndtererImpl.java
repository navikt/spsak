package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingskontroll.task.FortsettBehandlingTaskProperties;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.kontrollerfakta.BehandlingÅrsakTjeneste;
import no.nav.foreldrepenger.domene.registerinnhenting.Endringskontroller;
import no.nav.foreldrepenger.domene.registerinnhenting.EndringsresultatSjekker;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataInnhenter;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterinnhentingHistorikkinnslagTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Oppdaterer registeropplysninger for engangsstønader og skrur behandlingsprosessen tilbake
 * til innhent-steget hvis det har skjedd endringer siden forrige innhenting.
 */
@ApplicationScoped
public class RegisterdataEndringshåndtererImpl implements RegisterdataEndringshåndterer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterdataEndringshåndtererImpl.class);

    private RegisterdataInnhenter registerdataInnhenter;
    private TemporalAmount oppdatereRegisterdataTidspunkt;
    private BehandlingRepository behandlingRepository;
    private Endringskontroller endringskontroller;
    private EndringsresultatSjekker endringsresultatSjekker;
    private RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste;
    private BehandlingÅrsakTjeneste behandlingÅrsakTjeneste;

    RegisterdataEndringshåndtererImpl() {
        // for CDI proxy
    }

    @Inject
    public RegisterdataEndringshåndtererImpl( // NOSONAR - ingen umiddelbar mulighet for å redusere denne til >= 7 parametere
                                              BehandlingRepositoryProvider repositoryProvider,
                                              RegisterdataInnhenter registerdataInnhenter,
                                              @KonfigVerdi("oppdatere.registerdata.tidspunkt") Instance<String> periode,
                                              Endringskontroller endringskontroller,
                                              EndringsresultatSjekker endringsresultatSjekker,
                                              RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste, BehandlingÅrsakTjeneste behandlingÅrsakTjeneste) {

        this.registerdataInnhenter = registerdataInnhenter;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.endringskontroller = endringskontroller;
        this.endringsresultatSjekker = endringsresultatSjekker;
        this.historikkinnslagTjeneste = historikkinnslagTjeneste;
        this.behandlingÅrsakTjeneste = behandlingÅrsakTjeneste;
        if (periode != null && periode.get() != null) {
            this.oppdatereRegisterdataTidspunkt = Duration.parse(periode.get());
        }
    }

    @Override
    public boolean skalInnhenteRegisteropplysningerPåNytt(Behandling behandling) {
        LocalDateTime midnatt = LocalDate.now(FPDateUtil.getOffset()).atStartOfDay();
        Optional<LocalDateTime> opplysningerOppdatertTidspunkt = behandlingRepository.hentSistOppdatertTidspunkt(behandling);
        if (oppdatereRegisterdataTidspunkt == null) {
            // konfig-verdien er ikke satt
            return erOpplysningerOppdatertTidspunktFør(midnatt, opplysningerOppdatertTidspunkt);
        }
        LocalDateTime nårOppdatereRegisterdata = LocalDateTime.now(FPDateUtil.getOffset()).minus(oppdatereRegisterdataTidspunkt);
        if (nårOppdatereRegisterdata.isAfter(midnatt)) {
            // konfigverdien er etter midnatt, da skal midnatt gjelde
            return erOpplysningerOppdatertTidspunktFør(midnatt, opplysningerOppdatertTidspunkt);
        }
        // konfigverdien er før midnatt, da skal konfigverdien gjelde
        return erOpplysningerOppdatertTidspunktFør(nårOppdatereRegisterdata, opplysningerOppdatertTidspunkt);
    }

    boolean erOpplysningerOppdatertTidspunktFør(LocalDateTime nårOppdatereRegisterdata,
                                                Optional<LocalDateTime> opplysningerOppdatertTidspunkt) {
        return opplysningerOppdatertTidspunkt.isPresent() && opplysningerOppdatertTidspunkt.get().isBefore(nårOppdatereRegisterdata);
    }

    @Override
    public void oppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(Behandling behandling) {
        if (!skalInnhenteRegisteropplysningerPåNytt(behandling)) {
            return;
        }

        doOppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling);
    }

    @Override
    public EndringsresultatDiff oppdaterRegisteropplysninger(Behandling behandling, EndringsresultatSnapshot grunnlagSnapshot) {
        // Innhent evt. nye registeropplysninger
        oppdaterRegisteropplysninger(behandling);

        // Finn alle endringer som registerinnhenting har gjort på behandlingsgrunnlaget
        EndringsresultatDiff endringsresultat = endringsresultatSjekker.finnSporedeEndringerPåBehandlingsgrunnlag(behandling, grunnlagSnapshot);

        if (endringsresultat.erSporedeFeltEndret()) {
            LOGGER.info("Starter behandlingId={} på nytt. {}", behandling.getId(), endringsresultat);// NOSONAR //$NON-NLS-1$
            Set<BehandlingÅrsakType> behandlingÅrsakTyper = leggTilBehandlingsårsaker(behandling, endringsresultat);
            boolean lagGeneriskHistorikkinnslag = true;
            if (behandlingÅrsakTyper.contains(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD)) {
                historikkinnslagTjeneste.opprettHistorikkinnslagForBehandlingMedNyeOpplysninger(behandling, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_DØD);
                lagGeneriskHistorikkinnslag = false;
            }
            if (behandlingÅrsakTyper.contains(BehandlingÅrsakType.RE_OPPLYSNINGER_OM_YTELSER)) {
                historikkinnslagTjeneste.opprettHistorikkinnslagForBehandlingMedNyeOpplysninger(behandling, BehandlingÅrsakType.RE_OPPLYSNINGER_OM_YTELSER);
                lagGeneriskHistorikkinnslag = false;
            }
            if (lagGeneriskHistorikkinnslag) {
                historikkinnslagTjeneste.opprettHistorikkinnslagForNyeRegisteropplysninger(behandling);
            }
            endringskontroller.spolTilStartpunkt(behandling, endringsresultat);
        }
        return endringsresultat;
    }

    @Override
    public ProsessTaskGruppe opprettProsessTaskOppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(Behandling behandling,
                                                                                                           boolean innhentRegisteropplysninger, boolean manuellGjenopptakelse) {
        ProsessTaskGruppe gruppe = new ProsessTaskGruppe();
        if (innhentRegisteropplysninger) {
            ProsessTaskData registerdataOppdatererTask = new ProsessTaskData(RegisterdataOppdatererTask.TASKTYPE);
            registerdataOppdatererTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
            gruppe.addNesteSekvensiell(registerdataOppdatererTask);
        }
        ProsessTaskData fortsettBehandlingTask = new ProsessTaskData(FortsettBehandlingTaskProperties.TASKTYPE);
        fortsettBehandlingTask.setBehandling(behandling.getFagsakId(), behandling.getId(), behandling.getAktørId().getId());
        fortsettBehandlingTask.setProperty(FortsettBehandlingTaskProperties.MANUELL_FORTSETTELSE, String.valueOf(manuellGjenopptakelse));
        gruppe.addNesteSekvensiell(fortsettBehandlingTask);
        return gruppe;
    }

    private void oppdaterRegisteropplysninger(Behandling behandling) {
        Personinfo søkerInfo = registerdataInnhenter.innhentPersonopplysninger(behandling);
        registerdataInnhenter.innhentIAYOpplysninger(behandling, søkerInfo);

        // oppdater alltid tidspunktet grunnlagene ble oppdater eller forsøkt oppdatert!
        behandlingRepository.oppdaterSistOppdatertTidspunkt(behandling, LocalDateTime.now(FPDateUtil.getOffset()));
    }

    private void doOppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(Behandling behandling) {
        EndringsresultatSnapshot grunnlagSnapshot = endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);
        oppdaterRegisteropplysninger(behandling, grunnlagSnapshot);
    }

    private Set<BehandlingÅrsakType> leggTilBehandlingsårsaker(Behandling behandling, EndringsresultatDiff endringsresultat) {
        Set<BehandlingÅrsakType> behandlingÅrsaker = behandlingÅrsakTjeneste.utledBehandlingÅrsakerBasertPåDiff(behandling, endringsresultat);
        behandlingÅrsaker.add(BehandlingÅrsakType.RE_REGISTEROPPLYSNING);
        BehandlingÅrsak.Builder builder = BehandlingÅrsak.builder(new ArrayList<>(behandlingÅrsaker));
        behandling.getOriginalBehandling().ifPresent(builder::medOriginalBehandling);
        builder.buildFor(behandling);
        return behandlingÅrsaker;
    }
}
