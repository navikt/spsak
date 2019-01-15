package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning.aksjonspunkt;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType.HENLAGT_FEILOPPRETTET;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandling.historikk.HistorikkTjenesteAdapter;
import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningAksjonspunktDto;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarPersonstatusDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarPersonstatusOppdaterer implements AksjonspunktOppdaterer<AvklarPersonstatusDto> {

    private PersonopplysningTjeneste personopplysningTjeneste;

    private HistorikkTjenesteAdapter historikkAdapter;

    private AksjonspunktRepository aksjonspunktRepository;

    private KodeverkRepository kodeverkRepository;

    AvklarPersonstatusOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarPersonstatusOppdaterer(GrunnlagRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter, PersonopplysningTjeneste personopplysningTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    @Override
    public OppdateringResultat oppdater(AvklarPersonstatusDto dto, Behandling behandling) {
        final PersonopplysningerAggregat personopplysningerAggregat = personopplysningTjeneste.hentPersonopplysninger(behandling);
        håndterEndringHistorikk(dto, behandling, personopplysningerAggregat);

        if (dto.isFortsettBehandling()) {
            PersonopplysningAksjonspunktDto.PersonstatusPeriode personstatusPeriode =
                new PersonopplysningAksjonspunktDto.PersonstatusPeriode(dto.getPersonstatus(), personopplysningerAggregat.getPersonstatusFor(behandling.getAktørId()).getPeriode());
            personopplysningTjeneste.aksjonspunktAvklarSaksopplysninger(behandling, new PersonopplysningAksjonspunktDto(personstatusPeriode));
            return OppdateringResultat.utenOveropp();
        } else {
            return OppdateringResultat.medHenleggelse(HENLAGT_FEILOPPRETTET, dto.getBegrunnelse());
        }
    }

    private void håndterEndringHistorikk(AvklarPersonstatusDto dto, Behandling behandling, PersonopplysningerAggregat personopplysningerAggregat) {
        if (dto.isFortsettBehandling()) {
            PersonstatusType bekreftetPersonstatus = kodeverkRepository.finn(PersonstatusType.class, dto.getPersonstatus());
            PersonstatusType forrigePersonstatus = personopplysningerAggregat.getPersonstatusFor(behandling.getAktørId()).getPersonstatus();
            boolean endretVerdi = oppdaterVedEndretVerdi(HistorikkEndretFeltType.AVKLARSAKSOPPLYSNINGER, forrigePersonstatus, bekreftetPersonstatus);

            AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
            historikkAdapter.tekstBuilder().medEndretFelt(HistorikkEndretFeltType.BEHANDLING, null, HistorikkEndretFeltVerdiType.FORTSETT_BEHANDLING)
                .medBegrunnelse(dto.getBegrunnelse(), aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
                .medSkjermlenke(SkjermlenkeType.KONTROLL_AV_SAKSOPPLYSNINGER);
            if (endretVerdi) {
                aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
            }
        } else {
            historikkAdapter.tekstBuilder().medBegrunnelse(dto.getBegrunnelse())
                .medSkjermlenke(SkjermlenkeType.KONTROLL_AV_SAKSOPPLYSNINGER)
                .medEndretFelt(HistorikkEndretFeltType.BEHANDLING, null, HistorikkEndretFeltVerdiType.HENLEGG_BEHANDLING);
        }
    }

    private <K extends Kodeliste> boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, K original, K bekreftet) {
        if (!Objects.equals(bekreftet, original)) {
            historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            return true;
        }
        return false;
    }
}
