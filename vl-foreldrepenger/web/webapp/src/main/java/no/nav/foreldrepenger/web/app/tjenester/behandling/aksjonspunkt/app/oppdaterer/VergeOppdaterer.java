package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.BrevMottaker;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.VergeAksjonpunktDto;
import no.nav.foreldrepenger.domene.produksjonsstyring.oppgavebehandling.BehandlendeEnhetTjeneste;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VergeDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VergeDto.class, adapter=AksjonspunktOppdaterer.class)
class VergeOppdaterer implements AksjonspunktOppdaterer<VergeDto> {

    private PersonopplysningTjeneste personopplysningTjeneste;
    private BehandlendeEnhetTjeneste behandlendeEnhetTjeneste;
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    private HistorikkTjenesteAdapter historikkAdapter;
    private AksjonspunktRepository aksjonspunktRepository;
    private VergeRepository vergeRepository;
    private TpsTjeneste tpsTjeneste;

    VergeOppdaterer() {
        // CDI
    }

    @Inject
    public VergeOppdaterer(PersonopplysningTjeneste personopplysningTjeneste, BehandlendeEnhetTjeneste behandlendeEnhetTjeneste,
                           BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste,
                           HistorikkTjenesteAdapter historikkAdapter,
                           BehandlingRepositoryProvider behandlingRepositoryProvider,
                           TpsTjeneste tpsTjeneste) {
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.behandlendeEnhetTjeneste = behandlendeEnhetTjeneste;
        this.behandlingsutredningApplikasjonTjeneste = behandlingsutredningApplikasjonTjeneste;
        this.historikkAdapter = historikkAdapter;
        this.aksjonspunktRepository = behandlingRepositoryProvider.getAksjonspunktRepository();
        this.vergeRepository = behandlingRepositoryProvider.getVergeGrunnlagRepository();
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(VergeDto dto, Behandling behandling) {
        PersonIdent fnr = dto.getFnr()==null?null:new PersonIdent(dto.getFnr());
        final VergeAksjonpunktDto adapter = new VergeAksjonpunktDto(fnr, dto.getGyldigFom(), dto.getGyldigTom(), dto.getVergeType().getKode(),
            utledBrevMottaker(dto), dto.getVedtaksDato(), dto.getMandatTekst(), dto.getSokerErUnderTvungenForvaltning());

        byggHistorikkinnslag(dto, behandling);

        personopplysningTjeneste.aksjonspunktVergeOppdaterer(behandling, adapter);
        behandlendeEnhetTjeneste.endretBehandlendeEnhetFraAndrePersoner(behandling, fnr).ifPresent(organisasjonsEnhet -> {
            behandlingsutredningApplikasjonTjeneste.byttBehandlendeEnhet(behandling.getId(), organisasjonsEnhet, "", HistorikkAktør.VEDTAKSLØSNINGEN);
        });
        return OppdateringResultat.utenOveropp();
    }

    //TODO(OJR) må fikses når GUI benytter seg av BrevMottaker
    private BrevMottaker utledBrevMottaker(VergeDto dto) {
        if (dto.getSokerErKontaktPerson() && dto.getVergeErKontaktPerson()) {
            return BrevMottaker.BEGGE;
        } else if (dto.getVergeErKontaktPerson()) {
            return BrevMottaker.VERGE;
        }
        return BrevMottaker.SØKER;
    }

    private void byggHistorikkinnslag(VergeDto dto, Behandling behandling) {
        Optional<VergeAggregat> vergeAggregatOpt = vergeRepository.hentAggregat(behandling);
        if(!vergeAggregatOpt.isPresent()) {
            HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder()
                .medSkjermlenke(SkjermlenkeType.FAKTA_OM_VERGE);
            lagHistorikkinnslag(behandling.getId(), tekstBuilder, HistorikkinnslagType.REGISTRER_OM_VERGE);
        } else {
            opprettHistorikkinnslagForEndring(dto, behandling, vergeAggregatOpt.get());
        }
    }

    private void opprettHistorikkinnslagForEndring(VergeDto dto, Behandling behandling, VergeAggregat vergeAggregat) {
        HistorikkInnslagTekstBuilder tekstBuilder = new HistorikkInnslagTekstBuilder();
        Optional<Personinfo> personinfo = tpsTjeneste.hentBrukerForAktør(vergeAggregat.getAktørId());
        if(personinfo.isPresent()) {
            oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.NAVN, personinfo.get().getNavn(), dto.getNavn());
            oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.FNR, personinfo.get().getPersonIdent().getIdent(), dto.getFnr());
        }
        oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.MANDAT, vergeAggregat.getVerge().getMandatTekst(), dto.getMandatTekst());
        oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.PERIODE_FOM, vergeAggregat.getVerge().getGyldigFom(), dto.getGyldigFom());
        oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.PERIODE_TOM, vergeAggregat.getVerge().getGyldigTom(), dto.getGyldigTom());
        oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.TYPE_VERGE, vergeAggregat.getVerge().getVergeType(), dto.getVergeType());
        oppdaterVedEndretVerdi(tekstBuilder, HistorikkEndretFeltType.KONTAKTPERSON, vergeAggregat.getBrevMottaker(), utledBrevMottaker(dto));
        if(!Objects.equals(vergeAggregat.getVerge().getStønadMottaker(), dto.getSokerErUnderTvungenForvaltning())) {
            tekstBuilder.medEndretFelt(HistorikkEndretFeltType.BRUKER_TVUNGEN, null, dto.getSokerErUnderTvungenForvaltning());
        }
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        tekstBuilder
            .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(SkjermlenkeType.FAKTA_OM_VERGE);
        lagHistorikkinnslag(behandling.getId(), tekstBuilder, HistorikkinnslagType.FAKTA_ENDRET);
    }

    private <T> void oppdaterVedEndretVerdi(HistorikkInnslagTekstBuilder tekstBuilder, HistorikkEndretFeltType historikkEndretFeltType, T original, T bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            tekstBuilder.medEndretFelt(historikkEndretFeltType, original, bekreftet);
        }
    }

    private void lagHistorikkinnslag(Long behandlingId, HistorikkInnslagTekstBuilder tekstBuilder, HistorikkinnslagType innslagType) {
        Historikkinnslag innslag = new Historikkinnslag();

        innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        innslag.setBehandlingId(behandlingId);
        innslag.setType(innslagType);
        if(HistorikkinnslagType.REGISTRER_OM_VERGE.equals(innslagType)) {
            tekstBuilder.medHendelse(innslagType);
        }
        tekstBuilder.build(innslag);
        historikkAdapter.lagInnslag(innslag);
    }
}
