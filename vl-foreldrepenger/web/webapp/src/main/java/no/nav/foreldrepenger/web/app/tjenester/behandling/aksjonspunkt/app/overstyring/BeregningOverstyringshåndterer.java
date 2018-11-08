package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.overstyring;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollKontekst;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.beregning.Beregning;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.ytelse.beregning.BeregnYtelseTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.overstyring.OverstyringBeregningDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = OverstyringBeregningDto.class, adapter = Overstyringshåndterer.class)
public class BeregningOverstyringshåndterer extends AbstractOverstyringshåndterer<OverstyringBeregningDto> {

    private BeregnYtelseTjeneste beregnTjeneste;

    BeregningOverstyringshåndterer() {
        // for CDI proxy
    }

    @Inject
    public BeregningOverstyringshåndterer(BehandlingRepositoryProvider repositoryProvider,
            HistorikkTjenesteAdapter historikkAdapter,
            BeregnYtelseTjeneste beregnTjeneste) {
        super(repositoryProvider, historikkAdapter, AksjonspunktDefinisjon.OVERSTYRING_AV_BEREGNING);
        this.beregnTjeneste = beregnTjeneste;
    }

    @Override
    public OppdateringResultat håndterOverstyring(OverstyringBeregningDto dto, Behandling behandling,
            BehandlingskontrollKontekst kontekst) {
        beregnTjeneste.overstyrTilkjentYtelseForEngangsstønad(behandling, dto.getBeregnetTilkjentYtelse());
        return OppdateringResultat.utenOveropp();
    }

    @Override
    protected void lagHistorikkInnslag(Behandling behandling, OverstyringBeregningDto dto) {
       lagHistorikkInnslagForOverstyrtBeregning(behandling, dto.getBegrunnelse(), dto.getBeregnetTilkjentYtelse());
    }

    private void lagHistorikkInnslagForOverstyrtBeregning(Behandling behandling, String begrunnelse, Long tilBeregning) {
        Optional<Beregning> sisteBeregning = behandling.getBehandlingsresultat().getBeregningResultat().getSisteBeregning();
        if (sisteBeregning.isPresent()) {
            Long fraBeregning = sisteBeregning.get().getOpprinneligBeregnetTilkjentYtelse();
            getHistorikkAdapter().tekstBuilder()
                .medHendelse(HistorikkinnslagType.OVERSTYRT)
                .medBegrunnelse(begrunnelse)
                .medSkjermlenke(SkjermlenkeType.BEREGNING_ENGANGSSTOENAD)
                .medEndretFelt(HistorikkEndretFeltType.OVERSTYRT_BEREGNING, fraBeregning, tilBeregning);
        }
    }


}
