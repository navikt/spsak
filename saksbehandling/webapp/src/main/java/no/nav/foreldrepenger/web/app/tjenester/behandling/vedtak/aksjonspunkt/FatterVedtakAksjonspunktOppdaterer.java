package no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.aksjonspunkt.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.behandling.aksjonspunkt.DtoTilServiceAdapter;
import no.nav.foreldrepenger.behandling.aksjonspunkt.OppdateringResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.domene.vedtak.VedtakAksjonspunktData;
import no.nav.foreldrepenger.domene.vedtak.impl.FatterVedtakAksjonspunkt;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto.AksjonspunktGodkjenningDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.vedtak.aksjonspunkt.dto.FatterVedtakAksjonspunktDto;

@ApplicationScoped
@DtoTilServiceAdapter(dto = FatterVedtakAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class FatterVedtakAksjonspunktOppdaterer implements AksjonspunktOppdaterer<FatterVedtakAksjonspunktDto> {

    private FatterVedtakAksjonspunkt fatterVedtakAksjonspunkt;
    private AksjonspunktRepository aksjonspunktRepository;

    public FatterVedtakAksjonspunktOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public FatterVedtakAksjonspunktOppdaterer(GrunnlagRepositoryProvider repositoryProvider, FatterVedtakAksjonspunkt fatterVedtakAksjonspunkt) {
        this.fatterVedtakAksjonspunkt = fatterVedtakAksjonspunkt;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
    }

    @Override
    public OppdateringResultat oppdater(FatterVedtakAksjonspunktDto dto, Behandling behandling) {
        Collection<AksjonspunktGodkjenningDto> aksjonspunktGodkjenningDtoList = dto.getAksjonspunktGodkjenningDtos();

        Set<VedtakAksjonspunktData> aksjonspunkter = aksjonspunktGodkjenningDtoList.stream()
                .map(a -> {
                    // map til VedtakAksjonsonspunktData fra DTO
                    AksjonspunktDefinisjon aksDef = aksjonspunktRepository.finnAksjonspunktDefinisjon(a.getAksjonspunktKode());
                    return new VedtakAksjonspunktData(aksDef, a.isGodkjent(), a.getBegrunnelse(), fraDto(a.getArsaker()));
                })
                .collect(Collectors.toSet());

        fatterVedtakAksjonspunkt.oppdater(behandling, aksjonspunkter);

        return OppdateringResultat.utenOveropp();
    }

    private Collection<String> fraDto(Collection<VurderÅrsak> arsaker) {
        if (arsaker == null) {
            // TODO HUMLE/SOMMERFUGL Virker merkelig å ha nullsjekk her, men kreves for at tester i
            // AksjonspunktOppdatererTest skal gå OK
            return Collections.emptySet();
        }
        return arsaker.stream().map(Kodeliste::getKode).collect(Collectors.toSet());
    }
}
