package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.innsyn.InnsynTjeneste;
import no.nav.foreldrepenger.behandlingskontroll.BehandlingskontrollTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultatType;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.exceptions.Valideringsfeil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VurderInnsynDto;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VurderInnsynDto.class, adapter = AksjonspunktOppdaterer.class)
public class VurderInnsynOppdaterer implements AksjonspunktOppdaterer<VurderInnsynDto> {

    private BehandlingskontrollTjeneste behandlingskontrollTjeneste;
    private InnsynTjeneste innsynTjeneste;

    VurderInnsynOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public VurderInnsynOppdaterer(BehandlingskontrollTjeneste behandlingskontrollTjeneste, InnsynTjeneste innsynTjeneste) {
        this.behandlingskontrollTjeneste = behandlingskontrollTjeneste;
        this.innsynTjeneste = innsynTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(VurderInnsynDto dto, Behandling behandling, VilkårResultat.Builder vilkårBuilder) {
        vilkårBuilder.medVilkårResultatType(VilkårResultatType.UDEFINERT);
        innsynTjeneste.lagreVurderInnsynResultat(behandling, dto);

        if (dto.isSattPaVent()) {
            behandlingskontrollTjeneste.settBehandlingPåVent(behandling, AksjonspunktDefinisjon.VENT_PÅ_SCANNING,
                BehandlingStegType.VURDER_INNSYN, frist(dto.getFristDato()), Venteårsak.SCANN);
        }

        return OppdateringResultat.utenOveropp();
    }

    private static LocalDateTime frist(LocalDate frist) {
        if (frist == null) {
            throw new Valideringsfeil(Collections.singleton(new FeltFeilDto("frist", "frist må være satt")));
        }
        return LocalDateTime.of(frist, LocalDateTime.now(FPDateUtil.getOffset()).toLocalTime());
    }
}
