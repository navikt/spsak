package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.domene.dokument.DokumentBestillerTjeneste;
import no.nav.foreldrepenger.domene.dokument.VarselRevurderingAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.VarselRevurderingDto;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = VarselRevurderingDto.class, adapter=AksjonspunktOppdaterer.class)
public class VarselRevurderingOppdaterer implements AksjonspunktOppdaterer<VarselRevurderingDto> {

    private DokumentBestillerTjeneste dokumentTjeneste;
    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;

    VarselRevurderingOppdaterer() {
        // CDI
    }

    @Inject
    public VarselRevurderingOppdaterer(DokumentBestillerTjeneste dokumentTjeneste, HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                       DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste) {
        this.dokumentTjeneste = dokumentTjeneste;
        this.historikkApplikasjonTjeneste = historikkApplikasjonTjeneste;
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(VarselRevurderingDto dto, Behandling behandling) {
        if (dto.isSendVarsel() && !harSendtVarselOmRevurdering(behandling)) {
            final VarselRevurderingAksjonspunktDto adapter = new VarselRevurderingAksjonspunktDto(dto.getFritekst(), dto.getBegrunnelse(), dto.getFrist(), dto.getVentearsak().getKode());
            dokumentTjeneste.aksjonspunktVarselRevurdering(behandling, adapter);
        } else if (!dto.isSendVarsel()) {
            opprettHistorikkinnslagOmIkkeSendtVarselOmRevurdering(behandling, dto, HistorikkAktør.SAKSBEHANDLER);
        }
        return OppdateringResultat.utenOveropp();
    }

    private void opprettHistorikkinnslagOmIkkeSendtVarselOmRevurdering(Behandling behandling, VarselRevurderingDto varselRevurderingDto, HistorikkAktør historikkAktør) {
        HistorikkInnslagTekstBuilder historiebygger = new HistorikkInnslagTekstBuilder()
            .medHendelse(HistorikkinnslagType.VRS_REV_IKKE_SNDT)
            .medBegrunnelse(varselRevurderingDto.getBegrunnelse());
        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(historikkAktør);
        innslag.setType(HistorikkinnslagType.VRS_REV_IKKE_SNDT);
        innslag.setBehandlingId(behandling.getId());
        historiebygger.build(innslag);

        historikkApplikasjonTjeneste.lagInnslag(innslag);
    }

    private Boolean harSendtVarselOmRevurdering(Behandling behandling) {
        return dokumentBestillerApplikasjonTjeneste.erDokumentProdusert(behandling.getId(), DokumentMalType.REVURDERING_DOK);
    }

}
