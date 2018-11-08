package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderAleneOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.PerioderUtenOmsorg;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.YtelseFordelingAggregat;
import no.nav.foreldrepenger.domene.ytelsefordeling.BekreftFaktaForOmsorgVurderingAksjonspunktDto;
import no.nav.foreldrepenger.domene.ytelsefordeling.YtelseFordelingTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.PeriodeKonverter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto.BekreftAleneomsorgVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto.BekreftOmsorgVurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.omsorg.BekreftFaktaForOmsorgVurderingDto.PeriodeDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

public abstract class BekreftFaktaForOmsorgOppdaterer implements AksjonspunktOppdaterer<BekreftFaktaForOmsorgVurderingDto> {

    private BehandlingRepositoryProvider behandlingRepository;

    private AksjonspunktRepository aksjonspunktRepository;

    private HistorikkTjenesteAdapter historikkAdapter;

    private YtelseFordelingTjeneste ytelseFordelingTjeneste;

    BekreftFaktaForOmsorgOppdaterer() {
        // for CDI proxy
    }

    protected BekreftFaktaForOmsorgOppdaterer(BehandlingRepositoryProvider behandlingRepositoryProvider, HistorikkTjenesteAdapter historikkAdapter,
                                              YtelseFordelingTjeneste ytelseFordelingTjeneste) {
        this.behandlingRepository = behandlingRepositoryProvider;
        this.aksjonspunktRepository = behandlingRepositoryProvider.getAksjonspunktRepository();
        this.historikkAdapter = historikkAdapter;
        this.ytelseFordelingTjeneste = ytelseFordelingTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(BekreftFaktaForOmsorgVurderingDto dto, Behandling behandling) {

        YtelseFordelingAggregat ytelseFordelingAggregat = behandlingRepository.getYtelsesFordelingRepository().hentAggregat(behandling);
        Optional<PerioderUtenOmsorg> perioderUtenOmsorg = ytelseFordelingAggregat.getPerioderUtenOmsorg();
        Optional<PerioderAleneOmsorg> perioderAleneOmsorg = ytelseFordelingAggregat.getPerioderAleneOmsorg();
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        List<PeriodeDto> periodeUtenOmsorgListe = new ArrayList<>();

        boolean erEndret = false;
        boolean avkreftet = false;
        if (dto.getOmsorg() != null) {
            Boolean harOmsorgForBarnetSokVersjon = ytelseFordelingAggregat
                .getOppgittRettighet().getHarOmsorgForBarnetIHelePerioden();

            Boolean harOmsorgForBarnetBekreftetVersjon = null;
            if (perioderUtenOmsorg.isPresent()) {
                harOmsorgForBarnetBekreftetVersjon = perioderUtenOmsorg.get().getPerioder().isEmpty();
                periodeUtenOmsorgListe = PeriodeKonverter.mapUtenOmsorgperioder(perioderUtenOmsorg.get().getPerioder());
            }

            erEndret = opprettHistorikkInnslagForOmsorg(dto, periodeUtenOmsorgListe, harOmsorgForBarnetBekreftetVersjon);

            avkreftet = avkrefterBrukersOpplysninger(harOmsorgForBarnetSokVersjon, dto.getOmsorg());
        }

        if (dto.getAleneomsorg() != null) {
            Boolean aleneomsorgForBarnetSokVersjon = ytelseFordelingAggregat
                .getOppgittRettighet().getHarAleneomsorgForBarnet();

            Boolean aleneomsorgForBarnetBekreftetVersjon = null;
            if (perioderAleneOmsorg.isPresent()) {
                aleneomsorgForBarnetBekreftetVersjon = !perioderAleneOmsorg.get().getPerioder().isEmpty();
            }

            erEndret = oppdaterVedEndretVerdi(HistorikkEndretFeltType.ALENEOMSORG,
                konvertBooleanTilVerdiForAleneomsorgForBarnet(aleneomsorgForBarnetBekreftetVersjon),
                konvertBooleanTilVerdiForAleneomsorgForBarnet(dto.getAleneomsorg()), null);

            avkreftet = avkrefterBrukersOpplysninger(aleneomsorgForBarnetSokVersjon, dto.getAleneomsorg());
        }

        if (setToTrinns(perioderAleneOmsorg, perioderUtenOmsorg, erEndret, avkreftet, dto)) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }

        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(),
                aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon, dto.getBegrunnelse()))
            .medSkjermlenke(aksjonspunktDefinisjon, behandling);

        final BekreftFaktaForOmsorgVurderingAksjonspunktDto adapter = new BekreftFaktaForOmsorgVurderingAksjonspunktDto(dto.getAleneomsorg(),
            dto.getOmsorg(), PeriodeKonverter.mapIkkeOmsorgsperioder(dto.getIkkeOmsorgPerioder(), dto.getOmsorg()));
        ytelseFordelingTjeneste.aksjonspunktBekreftFaktaForOmsorg(behandling, adapter);

        return OppdateringResultat.utenOveropp();
    }

    private boolean opprettHistorikkInnslagForOmsorg(BekreftFaktaForOmsorgVurderingDto dto, List<PeriodeDto> periodeUtenOmsorgListe,
                                                     Boolean harOmsorgForBarnetBekreftetVersjon) {
        boolean erEndretTemp;

        if (Boolean.FALSE.equals(dto.getOmsorg())) {
            if (!periodeUtenOmsorgListe.isEmpty()) {
                erEndretTemp = oppdaterVedEndretVerdi(HistorikkEndretFeltType.IKKE_OMSORG_PERIODEN,
                    PeriodeKonverter.konvertPerioderTilString(periodeUtenOmsorgListe), PeriodeKonverter.konvertPerioderTilString(dto.getIkkeOmsorgPerioder()),
                    null);
            } else {
                erEndretTemp = oppdaterVedEndretVerdi(HistorikkEndretFeltType.OMSORG,
                    konverterBooleanTilVerdiForOmsorgForBarnet(harOmsorgForBarnetBekreftetVersjon),
                    konverterBooleanTilVerdiForOmsorgForBarnet(dto.getOmsorg()), PeriodeKonverter.konvertPerioderTilString(dto.getIkkeOmsorgPerioder()));
            }
        } else {
            erEndretTemp = oppdaterVedEndretVerdi(HistorikkEndretFeltType.OMSORG,
                konverterBooleanTilVerdiForOmsorgForBarnet(harOmsorgForBarnetBekreftetVersjon),
                konverterBooleanTilVerdiForOmsorgForBarnet(dto.getOmsorg()), null);
        }
        return erEndretTemp;
    }

    private boolean setToTrinns(Optional<PerioderAleneOmsorg> perioderAleneOmsorg, Optional<PerioderUtenOmsorg> perioderUtenOmsorg, boolean erEndret,
                                boolean avkreftet, BekreftFaktaForOmsorgVurderingDto dto) {
        // Totrinns er sett hvis saksbehandler avkreftet først gang eller endret etter han bekreftet
        if (dto.getOmsorg() != null) {
            return avkreftet || (erEndret && perioderUtenOmsorg.isPresent());
        }
        if (dto.getAleneomsorg() != null) {
            return avkreftet || (erEndret && perioderAleneOmsorg.isPresent());
        }
        return false;
    }

    private HistorikkEndretFeltVerdiType konvertBooleanTilVerdiForAleneomsorgForBarnet(Boolean aleneomsorgForBarnet) {
        if (aleneomsorgForBarnet == null) {
            return null;
        }
        return aleneomsorgForBarnet ? HistorikkEndretFeltVerdiType.ALENEOMSORG : HistorikkEndretFeltVerdiType.IKKE_ALENEOMSORG;
    }

    private String konverterBooleanTilVerdiForOmsorgForBarnet(Boolean omsorgForBarnet) {
        if (omsorgForBarnet == null) {
            return null;
        }
        // TODO SOMMERFUGL midlertidig løsning. Inntil en løsning for å støtte dette
        return omsorgForBarnet ? "Søker har omsorg for barnet" : "Søker har ikke omsorg for barnet";
    }

    private <T> boolean oppdaterVedEndretVerdi(HistorikkEndretFeltType historikkEndretFeltType, T original, T bekreftet, String perioder) {
        if (!Objects.equals(bekreftet, original)) {
            if (perioder == null) {
                historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet);
            } else {
                // TODO SOMMERFUGL midlertidig løsning. Inntil en løsning for å støtte dette
                historikkAdapter.tekstBuilder().medEndretFelt(historikkEndretFeltType, original, bekreftet.toString().concat(" i perioden ").concat(perioder));
            }
            return true;
        }
        return false;
    }

    private boolean avkrefterBrukersOpplysninger(Object original, Object bekreftet) {
        return !Objects.equals(bekreftet, original);
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = BekreftAleneomsorgVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class BekreftAleneomsorgOppdaterer extends BekreftFaktaForOmsorgOppdaterer {

        BekreftAleneomsorgOppdaterer() {
            // for CDI proxy
        }

        @Inject
        public BekreftAleneomsorgOppdaterer(BehandlingRepositoryProvider behandlingRepositoryProvider, HistorikkTjenesteAdapter historikkAdapter,
                                            YtelseFordelingTjeneste ytelseFordelingTjeneste) {
            super(behandlingRepositoryProvider, historikkAdapter, ytelseFordelingTjeneste);
        }
    }

    @ApplicationScoped
    @DtoTilServiceAdapter(dto = BekreftOmsorgVurderingDto.class, adapter = AksjonspunktOppdaterer.class)
    public static class BekreftOmsorgOppdaterer extends BekreftFaktaForOmsorgOppdaterer {

        BekreftOmsorgOppdaterer() {
            // for CDI proxy
        }

        @Inject
        public BekreftOmsorgOppdaterer(BehandlingRepositoryProvider behandlingRepositoryProvider, HistorikkTjenesteAdapter historikkAdapter,
                                       YtelseFordelingTjeneste ytelseFordelingTjeneste) {
            super(behandlingRepositoryProvider, historikkAdapter, ytelseFordelingTjeneste);
        }
    }

}
