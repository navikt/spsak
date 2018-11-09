package no.nav.foreldrepenger.domene.registerinnhenting.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
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
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingÅrsakType;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.kontrollerfakta.BehandlingÅrsakTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.BasisPersonopplysningTjeneste;
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

    private Period antallDagerOverTerminRestartBehandling;
    private RegisterdataInnhenter registerdataInnhenter;
    private FamilieHendelseRepository familieGrunnlagRepository;
    private TemporalAmount oppdatereRegisterdataTidspunkt;
    private BehandlingRepository behandlingRepository;
    private BasisPersonopplysningTjeneste personopplysningTjeneste;
    private Endringskontroller endringskontroller;
    private EndringsresultatSjekker endringsresultatSjekker;
    private RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste;
    private BehandlingÅrsakTjeneste behandlingÅrsakTjeneste;

    RegisterdataEndringshåndtererImpl() {
        // for CDI proxy
    }

    @Inject
    public RegisterdataEndringshåndtererImpl( // NOSONAR - ingen umiddelbar mulighet for å redusere denne til >= 7 parametere
                                              @KonfigVerdi(value = "aksjonspunkt.dager.etter.termin.sjekk.fødsel") Period antallDagerOverTerminRestartBehandling,
                                              BehandlingRepositoryProvider repositoryProvider,
                                              RegisterdataInnhenter registerdataInnhenter,
                                              @KonfigVerdi("oppdatere.registerdata.tidspunkt") Instance<String> periode,
                                              BasisPersonopplysningTjeneste personopplysningTjeneste,
                                              Endringskontroller endringskontroller,
                                              EndringsresultatSjekker endringsresultatSjekker,
                                              RegisterinnhentingHistorikkinnslagTjeneste historikkinnslagTjeneste, BehandlingÅrsakTjeneste behandlingÅrsakTjeneste) {

        this.antallDagerOverTerminRestartBehandling = antallDagerOverTerminRestartBehandling;
        this.registerdataInnhenter = registerdataInnhenter;
        this.familieGrunnlagRepository = repositoryProvider.getFamilieGrunnlagRepository();
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.personopplysningTjeneste = personopplysningTjeneste;
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

        // TODO (essv): PK-50959 Splitt tjenesten for ENGANGSTØNAD og FORELDREPENGER
        if (FagsakYtelseType.FORELDREPENGER.equals(behandling.getFagsak().getYtelseType())) {
            doOppdaterRegisteropplysningerOgRestartBehandlingVedEndringerES(behandling);
            return;
        }
        doOppdaterRegisteropplysningerOgRestartBehandlingVedEndringerFP(behandling);
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
    public void doOppdaterRegisteropplysningerOgRestartBehandlingVedEndringerES(Behandling behandling) {
        // Ta snapshot av behandlingsgrunnlaget før oppdatering
        EndringsresultatSnapshot grunnlagSnapshot = endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);

        // Innhent evt. nye registeropplysninger
        oppdaterRegisteropplysninger(behandling);

        // Finn alle endringer som registerinnhenting har gjort på behandlingsgrunnlaget
        EndringsresultatDiff endringsresultat = endringsresultatSjekker.finnSporedeEndringerPåBehandlingsgrunnlag(behandling, grunnlagSnapshot);

        // Engangsstønad skal i tillegg til behandlingsgrunnlaget se på betingelse om passering av termindato
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentPersonopplysninger(behandling);
        FamilieHendelse bekreftetFH = familieGrunnlagRepository.hentAggregat(behandling).getBekreftetVersjon().orElse(null);
        boolean gåttOverTerminDatoOgIngenFødselsdato = gåttOverTerminDatoOgIngenFødselsdato(personopplysninger, bekreftetFH, behandling);

        if (endringsresultat.erSporedeFeltEndret() || gåttOverTerminDatoOgIngenFødselsdato) {
            LOGGER.info("Starter behandlingId={} på nytt. gåttOverTerminDatoOgIngenFødselsdato={}, {}", // NOSONAR //$NON-NLS-1$
                behandling.getId(), gåttOverTerminDatoOgIngenFødselsdato, endringsresultat);

            leggTilBehandlingsårsak(behandling, BehandlingÅrsakType.RE_REGISTEROPPLYSNING);
            historikkinnslagTjeneste.opprettHistorikkinnslagForNyeRegisteropplysninger(behandling);
            endringskontroller.spolTilSteg(behandling, BehandlingStegType.KONTROLLER_FAKTA);
        }
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

    private void doOppdaterRegisteropplysningerOgRestartBehandlingVedEndringerFP(Behandling behandling) {
        EndringsresultatSnapshot grunnlagSnapshot = endringsresultatSjekker.opprettEndringsresultatPåBehandlingsgrunnlagSnapshot(behandling);
        oppdaterRegisteropplysninger(behandling, grunnlagSnapshot);
    }

    /**
     * Skal starte behandling på nytt hvis dagens dato er mer enn 25 dager etter termin
     * OG det ikke finnes noen fødselsdato i TPS.
     */
    private boolean gåttOverTerminDatoOgIngenFødselsdato(PersonopplysningerAggregat personopplysningerAggregat,
                                                         FamilieHendelse bekreftet, Behandling behandling) {
        if (ikkeErFødselssak(behandling)) { // NOSONAR
            return false;
        }

        if (bekreftet != null && !bekreftet.getBarna().isEmpty()) {
            return false;
        }

        boolean ingenFødselsdatoFraTPS = personopplysningerAggregat.getBarna().stream().noneMatch(barn -> barn.getFødselsdato() != null);
        Optional<LocalDate> terminDato = finnTerminDato(familieGrunnlagRepository.hentAggregat(behandling));
        return (ingenFødselsdatoFraTPS && terminDato.isPresent()
            && LocalDate.now(FPDateUtil.getOffset()).minus(antallDagerOverTerminRestartBehandling).isAfter(terminDato.get()));
    }

    private boolean ikkeErFødselssak(Behandling behandling) {
        final Optional<FamilieHendelseGrunnlag> familieHendelseGrunnlag = familieGrunnlagRepository.hentAggregatHvisEksisterer(behandling);
        return !familieHendelseGrunnlag.isPresent()
            || !familieHendelseGrunnlag.get().getGjeldendeVersjon().getGjelderFødsel();
    }

    private Optional<LocalDate> finnTerminDato(FamilieHendelseGrunnlag grunnlag) {
        final Optional<Terminbekreftelse> terminbekreftelse = grunnlag.getGjeldendeBekreftetVersjon()
            .flatMap(FamilieHendelse::getTerminbekreftelse);
        if (terminbekreftelse.isPresent() && terminbekreftelse.get().getTermindato() != null) {
            return Optional.of(terminbekreftelse.get().getTermindato());
        }
        Optional<Terminbekreftelse> terminbekreftelseOptional = grunnlag.getSøknadVersjon().getTerminbekreftelse();
        return terminbekreftelseOptional.isPresent()
            ? Optional.ofNullable(terminbekreftelseOptional.get().getTermindato())
            : Optional.empty();
    }

    private void leggTilBehandlingsårsak(Behandling behandling, BehandlingÅrsakType behandlingÅrsak) {
        BehandlingÅrsak.Builder builder = BehandlingÅrsak.builder(behandlingÅrsak);
        builder.buildFor(behandling);
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
