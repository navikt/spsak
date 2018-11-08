package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import static no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType.HENLAGT_FEILOPPRETTET;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningAksjonspunktDto;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarSaksopplysningerDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarSaksopplysningerDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarSaksopplysningerOppdaterer implements AksjonspunktOppdaterer<AvklarSaksopplysningerDto> {

    private PersonopplysningTjeneste personopplysningTjeneste;

    private HistorikkTjenesteAdapter historikkAdapter;

    private AksjonspunktRepository aksjonspunktRepository;

    private KodeverkRepository kodeverkRepository;

    AvklarSaksopplysningerOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarSaksopplysningerOppdaterer(BehandlingRepositoryProvider repositoryProvider, HistorikkTjenesteAdapter historikkAdapter, PersonopplysningTjeneste personopplysningTjeneste) {
        this.historikkAdapter = historikkAdapter;
        this.personopplysningTjeneste = personopplysningTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    @Override
    public OppdateringResultat oppdater(AvklarSaksopplysningerDto dto, Behandling behandling) {
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

    private void håndterEndringHistorikk(AvklarSaksopplysningerDto dto, Behandling behandling, PersonopplysningerAggregat personopplysningerAggregat) {
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
