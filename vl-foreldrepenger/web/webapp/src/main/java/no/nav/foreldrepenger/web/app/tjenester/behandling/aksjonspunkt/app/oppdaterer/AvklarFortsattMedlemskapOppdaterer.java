package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.skjermlenke.SkjermlenkeType;
import no.nav.foreldrepenger.domene.medlem.api.AvklarFortsattMedlemskapAksjonspunktDto;
import no.nav.foreldrepenger.domene.medlem.api.MedlemTjeneste;
import no.nav.foreldrepenger.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.web.app.exceptions.Valideringsfeil;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.AvklarFortsattMedlemskapDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = AvklarFortsattMedlemskapDto.class, adapter = AksjonspunktOppdaterer.class)
public class AvklarFortsattMedlemskapOppdaterer implements AksjonspunktOppdaterer<AvklarFortsattMedlemskapDto> {

    private MedlemTjeneste medlemTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;
    private HistorikkTjenesteAdapter historikkAdapter;
    private SkjæringstidspunktTjeneste skjæringstidspunktTjeneste;
    AvklarFortsattMedlemskapOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public AvklarFortsattMedlemskapOppdaterer(MedlemTjeneste medlemTjeneste,
                                              AksjonspunktRepository aksjonspunktRepository, HistorikkTjenesteAdapter historikkAdapter, SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        this.medlemTjeneste = medlemTjeneste;
        this.aksjonspunktRepository = aksjonspunktRepository;
        this.historikkAdapter = historikkAdapter;
        this.skjæringstidspunktTjeneste = skjæringstidspunktTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(AvklarFortsattMedlemskapDto dto, Behandling behandling) {
        LocalDate skjæringstidspunkt = skjæringstidspunktTjeneste.utledSkjæringstidspunktForForeldrepenger(behandling);
        validerDato(dto.getFomDato(), skjæringstidspunkt);

        boolean gjelderEndringIPersonopplysninger = medlemTjeneste.søkerHarEndringerIPersonopplysninger(behandling).harEndringer();
        final AvklarFortsattMedlemskapAksjonspunktDto adapter = new AvklarFortsattMedlemskapAksjonspunktDto(dto.getFomDato(), gjelderEndringIPersonopplysninger);

        håndterEndringHistorikk(dto, behandling);

        medlemTjeneste.aksjonspunktAvklarFortsattMedlemskap(behandling, adapter);
        return OppdateringResultat.utenOveropp();
    }

    private void håndterEndringHistorikk(AvklarFortsattMedlemskapDto dto, Behandling behandling) {
        Optional<MedlemskapAggregat> medlemskap = medlemTjeneste.hentMedlemskap(behandling);
        LocalDate orginalFmoDato = null;
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());

        if (oppdaterVedEndretVerdi(dto, orginalFmoDato)) {
            aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
        }

        historikkAdapter.tekstBuilder()
            .medBegrunnelse(dto.getBegrunnelse(),
                aksjonspunktRepository.sjekkErBegrunnelseForAksjonspunktEndret(behandling, aksjonspunktDefinisjon,
                    dto.getBegrunnelse()))
            .medSkjermlenke(SkjermlenkeType.FAKTA_OM_MEDLEMSKAP);
        historikkAdapter.tekstBuilder().medGjeldendeFra(dto.getFomDato());
    }

    private static void validerDato(LocalDate gjelderFra, LocalDate skjæringstidspunkt) {
        if (gjelderFra == null) {
            throw new Valideringsfeil(Collections.singleton(new FeltFeilDto("fomDato", "gjeldende fra må være satt")));
        }
        if(gjelderFra.isBefore(skjæringstidspunkt)){
            throw new Valideringsfeil(Collections.singleton(new FeltFeilDto("fomDato", "gjeldende fra kan ikke være før første uttaksdato")));
        }
    }

    private boolean oppdaterVedEndretVerdi(AvklarFortsattMedlemskapDto dto, LocalDate orginalFmoDato) {
        return !Objects.equals(dto.getFomDato(), orginalFmoDato);
    }
}
