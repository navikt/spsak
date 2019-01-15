package no.nav.foreldrepenger.web.app.tjenester.behandling.app;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollAsynkTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkBegrunnelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.HistorikkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.domene.registerinnhenting.RegisterdataEndringshåndterer;
import no.nav.foreldrepenger.domene.registerinnhenting.impl.ÅpneBehandlingForEndringerTask;
import no.nav.foreldrepenger.web.app.tjenester.VurderProsessTaskStatusForPollingApi;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjenesteImpl.BehandlingsutredningApplikasjonTjenesteFeil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;
import no.nav.foreldrepenger.web.app.util.LdapUtil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.ldap.LdapBruker;
import no.nav.vedtak.felles.integrasjon.ldap.LdapBrukeroppslag;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class BehandlingsprosessApplikasjonTjenesteImpl implements BehandlingsprosessApplikasjonTjeneste {

    private static final BehandlingsprosessApplikasjonFeil FEIL = FeilFactory.create(BehandlingsprosessApplikasjonFeil.class);

    private BehandlingRepository behandlingRepository;
    private RegisterdataEndringshåndterer registerdataOppdaterer;
    private String gruppenavnSaksbehandler;
    private BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste;
    private HistorikkRepository historikkRepository;

    BehandlingsprosessApplikasjonTjenesteImpl() {
        // for CDI proxy
    }

    // test only
    BehandlingsprosessApplikasjonTjenesteImpl(BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste) {
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
    }

    @Inject
    public BehandlingsprosessApplikasjonTjenesteImpl(
                                                     BehandlingRepository behandlingRepository,
                                                     VilkårKodeverkRepository vilkårKodeverkRepository,
                                                     BehandlingskontrollAsynkTjeneste behandlingskontrollAsynkTjeneste,
                                                     RegisterdataEndringshåndterer registerdataEndringshåndterer,
                                                     @KonfigVerdi(value = "bruker.gruppenavn.saksbehandler") String gruppenavnSaksbehandler,
                                                     HistorikkRepository historikkRepository) {

        Objects.requireNonNull(behandlingRepository, "behandlingRepository");
        Objects.requireNonNull(vilkårKodeverkRepository, "vilkårKodeverkRepository");
        this.behandlingRepository = behandlingRepository;
        this.registerdataOppdaterer = registerdataEndringshåndterer;
        this.gruppenavnSaksbehandler = gruppenavnSaksbehandler;
        this.behandlingskontrollAsynkTjeneste = behandlingskontrollAsynkTjeneste;
        this.historikkRepository = historikkRepository;
    }

    @Override
    public String asynkKjørProsess(Behandling behandling) {
        return asynkInnhentingAvRegisteropplysningerOgKjørProsess(behandling, false, false);
    }

    @Override
    public String asynkFortsettBehandlingsprosess(Behandling behandling) {
        return behandlingskontrollAsynkTjeneste.asynkProsesserBehandling(behandling);
    }

    @Override
    public void kanEndreBehandling(Long behandlingId, Long versjon) {
        Boolean kanEndreBehandling = behandlingRepository.erVersjonUendret(behandlingId, versjon);
        if (!kanEndreBehandling) {
            throw BehandlingsutredningApplikasjonTjenesteFeil.FACTORY.endringerHarForekommetPåBehandlingen().toException();
        }
    }

    @Override
    public boolean skalInnhenteRegisteropplysningerPåNytt(Behandling behandling) {
        BehandlingStatus behandlingStatus = behandling.getStatus();
        return BehandlingStatus.UTREDES.equals(behandlingStatus)
            && !behandling.isBehandlingPåVent()
            && harRolleSaksbehandler()
            && registerdataOppdaterer.skalInnhenteRegisteropplysningerPåNytt(behandling);
    }

    @Override
    public Optional<String> sjekkOgForberedAsynkInnhentingAvRegisteropplysningerOgKjørProsess(Behandling behandling) {
        if (!skalInnhenteRegisteropplysningerPåNytt(behandling)) {
            return Optional.empty();
        }
        // henter alltid registeropplysninger og kjører alltid prosess
        return Optional.of(asynkInnhentingAvRegisteropplysningerOgKjørProsess(behandling, true, false));
    }

    @Override
    public String asynkInnhentingAvRegisteropplysningerOgKjørProsess(Behandling behandling, boolean skalInnhenteRegisteropplysninger, boolean manuellGjenopptakelse) {
        ProsessTaskGruppe gruppe = registerdataOppdaterer.opprettProsessTaskOppdaterRegisteropplysningerOgRestartBehandlingVedEndringer(behandling,
            skalInnhenteRegisteropplysninger, manuellGjenopptakelse);
        String gruppeNavn = behandlingskontrollAsynkTjeneste.lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(behandling.getFagsakId(), behandling.getId(), gruppe);
        return gruppeNavn;
    }

    @Override
    public Optional<String> gjenopptaBehandling(Behandling behandling) {
        opprettHistorikkinnslagForManueltGjenopptakelse(behandling, HistorikkinnslagType.BEH_MAN_GJEN);
        return Optional.of(asynkInnhentingAvRegisteropplysningerOgKjørProsess(behandling, true, true));
    }

    /**
     * Sjekk status på tasks og returner ulike feilkoder og status som GUI kan pynte på visning av.
     */
    @Override
    public Optional<AsyncPollingStatus> sjekkProsessTaskPågårForBehandling(Behandling behandling, String gruppe) {

        Long behandlingId = behandling.getId();

        Map<String, ProsessTaskData> nesteTask = behandlingskontrollAsynkTjeneste.sjekkProsessTaskPågårForBehandling(behandling, gruppe);
        return new VurderProsessTaskStatusForPollingApi(FEIL, behandlingId).sjekkStatusNesteProsessTask(gruppe, nesteTask);

    }
    private boolean harRolleSaksbehandler() {
        String ident = SubjectHandler.getSubjectHandler().getUid();
        LdapBruker ldapBruker = new LdapBrukeroppslag().hentBrukerinformasjon(ident);
        Collection<String> grupper = LdapUtil.filtrerGrupper(ldapBruker.getGroups());
        return grupper.contains(gruppenavnSaksbehandler);
    }

    @Override
    public Behandling hentBehandling(Long behandlingsId) {
        return behandlingRepository.hentBehandling(behandlingsId);
    }

    @Override
    public String asynkTilbakestillOgÅpneBehandlingForEndringer(Long behandlingsId) {
        Behandling behandling = behandlingRepository.hentBehandling(behandlingsId);
        ProsessTaskGruppe gruppe = new ProsessTaskGruppe();

        ProsessTaskData åpneBehandlingForEndringerTask = new ProsessTaskData(ÅpneBehandlingForEndringerTask.TASKTYPE);
        åpneBehandlingForEndringerTask.setBehandling(behandling.getFagsakId(), behandlingsId, behandling.getAktørId().getId());
        gruppe.addNesteSekvensiell(åpneBehandlingForEndringerTask);

        opprettHistorikkinnslagForBehandlingStartetPåNytt(behandling);

        return behandlingskontrollAsynkTjeneste.lagreNyGruppeKunHvisIkkeAlleredeFinnesOgIngenHarFeilet(behandling.getFagsakId(), behandlingsId, gruppe);
    }

    /**
     * På grunn av (nyinnført) async-prosessering videre nedover mister vi informasjon her om at det i dette tilfellet er saksbehandler som
     * ber om gjenopptakelse av behandlingen. Det kommer et historikkinnslag om dette (se {@link no.nav.foreldrepenger.behandlingskontroll.AksjonspunktUtførtEvent})
     * som eies av systembruker. Derfor velger vi her å legge på et innslag til med saksbehandler som eier slik at historikken blir korrekt.
     */
    private void opprettHistorikkinnslagForManueltGjenopptakelse(Behandling behandling,
                                                                 HistorikkinnslagType historikkinnslagType) {
        HistorikkInnslagTekstBuilder builder = new HistorikkInnslagTekstBuilder();
        builder.medHendelse(historikkinnslagType);

        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        historikkinnslag.setType(historikkinnslagType);
        historikkinnslag.setBehandlingId(behandling.getId());
        historikkinnslag.setFagsakId(behandling.getFagsakId());
        builder.build(historikkinnslag);
        historikkRepository.lagre(historikkinnslag);
    }

    private void opprettHistorikkinnslagForBehandlingStartetPåNytt(Behandling behandling) {
        Historikkinnslag historikkinnslag = new Historikkinnslag();
        historikkinnslag.setType(HistorikkinnslagType.BEH_STARTET_PÅ_NYTT);
        historikkinnslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        HistorikkInnslagTekstBuilder historikkInnslagTekstBuilder = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.BEH_STARTET_PÅ_NYTT)
            .medBegrunnelse(HistorikkBegrunnelseType.BEH_STARTET_PA_NYTT);
        historikkInnslagTekstBuilder.build(historikkinnslag);
        historikkinnslag.setBehandling(behandling);
        historikkRepository.lagre(historikkinnslag);
    }
}
