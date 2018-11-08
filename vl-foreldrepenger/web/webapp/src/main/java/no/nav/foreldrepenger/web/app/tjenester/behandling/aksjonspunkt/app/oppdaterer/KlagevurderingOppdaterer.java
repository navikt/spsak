package no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.oppdaterer;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkInnslagTekstBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.Historikkinnslag;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkinnslagType;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.dokument.DokumentBestillerTjeneste;
import no.nav.foreldrepenger.domene.dokument.KlageVurderingAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.AksjonspunktOppdaterer;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.DtoTilServiceAdapter;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.app.OppdateringResultat;
import no.nav.foreldrepenger.web.app.tjenester.behandling.aksjonspunkt.dto.KlageVurderingResultatAksjonspunktDto;
import no.nav.foreldrepenger.web.app.tjenester.behandling.app.BehandlingsutredningApplikasjonTjeneste;
import no.nav.foreldrepenger.web.app.tjenester.historikk.app.HistorikkTjenesteAdapter;

@ApplicationScoped
@DtoTilServiceAdapter(dto = KlageVurderingResultatAksjonspunktDto.class, adapter = AksjonspunktOppdaterer.class)
public class KlagevurderingOppdaterer implements AksjonspunktOppdaterer<KlageVurderingResultatAksjonspunktDto> {
    private BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste;
    private HistorikkTjenesteAdapter historikkApplikasjonTjeneste;
    private KodeverkRepository kodeverkRepository;
    private DokumentBestillerTjeneste dokumentTjeneste;
    private AksjonspunktRepository aksjonspunktRepository;

    KlagevurderingOppdaterer() {
        // for CDI proxy
    }

    @Inject
    public KlagevurderingOppdaterer(BehandlingRepositoryProvider repositoryProvider,
                                    HistorikkTjenesteAdapter historikkApplikasjonTjeneste,
                                    DokumentBestillerTjeneste dokumentTjeneste,
                                    BehandlingsutredningApplikasjonTjeneste behandlingsutredningApplikasjonTjeneste) {
        this.historikkApplikasjonTjeneste = historikkApplikasjonTjeneste;
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
        this.dokumentTjeneste = dokumentTjeneste;
        this.aksjonspunktRepository = repositoryProvider.getAksjonspunktRepository();
        this.behandlingsutredningApplikasjonTjeneste = behandlingsutredningApplikasjonTjeneste;
    }

    @Override
    public OppdateringResultat oppdater(KlageVurderingResultatAksjonspunktDto dto, Behandling behandling) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(dto.getKode());
        håndterToTrinnsBehandling(behandling, aksjonspunktDefinisjon, dto.getKlageVurdering());

        håndterKlageVurdering(dto, behandling, aksjonspunktDefinisjon);

        opprettHistorikkinnslag(behandling, aksjonspunktDefinisjon, dto);
        oppdatereDatavarehus(dto, behandling, aksjonspunktDefinisjon);

        return OppdateringResultat.utenOveropp();
    }

    private void håndterKlageVurdering(KlageVurderingResultatAksjonspunktDto dto, Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        final KlageVurderingAksjonspunktDto adapter = new KlageVurderingAksjonspunktDto(dto.getKlageVurdering().getKode(),
            dto.getBegrunnelse(), dto.getVedtaksdatoPaklagdBehandling(), getKlageAvvistÅrsak(dto),
            getKlageMedholdÅrsak(dto), erNfpAksjonspunkt(aksjonspunktDefinisjon));

        dokumentTjeneste.aksjonspunktKlageVurdering(behandling, adapter);
    }

    private void håndterToTrinnsBehandling(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, KlageVurdering klageVurdering) {
        if (erNfpAksjonspunkt(aksjonspunktDefinisjon)) {
            if (!KlageVurdering.STADFESTE_YTELSESVEDTAK.getKode()
                    .equals(klageVurdering.getKode())) {
                aksjonspunktRepository.setToTrinnsBehandlingKreves(behandling, aksjonspunktDefinisjon);
            } else {
                // Må fjerne totrinnsbehandling i tilfeller hvor totrinn er satt for NFP (klagen ikke er innom NK),
                // beslutter sender behandlingen tilbake til NFP, og NFP deretter gjør et valgt som sender
                // behandlingen til NK. Da skal ikke aksjonspunkt NFP totrinnsbehandles.
                fjernToTrinnsBehandling(behandling, aksjonspunktDefinisjon);
            }
        }
    }
    private void fjernToTrinnsBehandling(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aksjonspunktDefinisjon);
        if (aksjonspunkt.isToTrinnsBehandling()) {
            aksjonspunktRepository.fjernToTrinnsBehandlingKreves(aksjonspunkt);
        }
    }

    private String getKlageAvvistÅrsak(KlageVurderingResultatAksjonspunktDto dto) {
        return dto.getKlageAvvistArsak() == null ? null : dto.getKlageAvvistArsak().getKode();
    }

    private String getKlageMedholdÅrsak(KlageVurderingResultatAksjonspunktDto dto) {
        return dto.getKlageMedholdArsak() == null ? null : dto.getKlageMedholdArsak().getKode();
    }

    private void opprettHistorikkinnslag(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon, KlageVurderingResultatAksjonspunktDto dto) {
        KlageVurdering klageVurdering = kodeverkRepository.finn(KlageVurdering.class, dto.getKlageVurdering().getKode());
        boolean erNfpAksjonspunkt = erNfpAksjonspunkt(aksjonspunktDefinisjon);
        HistorikkinnslagType historikkinnslagType = erNfpAksjonspunkt ? HistorikkinnslagType.KLAGE_BEH_NFP : HistorikkinnslagType.KLAGE_BEH_NK;
        Kodeliste årsak = null;
        if (dto.getKlageMedholdArsak() != null) {
            årsak = dto.getKlageMedholdArsak();
        } else if (dto.getKlageAvvistArsak() != null) {
            årsak = dto.getKlageAvvistArsak();
        }

        HistorikkResultatType resultat = konverterKlageVurderingTilResultatType(klageVurdering, erNfpAksjonspunkt);
        HistorikkInnslagTekstBuilder historiebygger = new HistorikkInnslagTekstBuilder()
            .medHendelse(historikkinnslagType)
            .medResultat(resultat)
            .medÅrsak(årsak)
            .medBegrunnelse(dto.getBegrunnelse())
            .medSkjermlenke(aksjonspunktDefinisjon,behandling);

        Historikkinnslag innslag = new Historikkinnslag();
        innslag.setAktør(HistorikkAktør.SAKSBEHANDLER);
        innslag.setType(historikkinnslagType);
        innslag.setBehandlingId(behandling.getId());
        historiebygger.build(innslag);

        historikkApplikasjonTjeneste.lagInnslag(innslag);
    }

    private HistorikkResultatType konverterKlageVurderingTilResultatType(KlageVurdering vurdering, boolean erNfpAksjonspunkt) {
        if (KlageVurdering.AVVIS_KLAGE.equals(vurdering)) {
            return HistorikkResultatType.AVVIS_KLAGE;
        }
        if (KlageVurdering.MEDHOLD_I_KLAGE.equals(vurdering)) {
            return HistorikkResultatType.MEDHOLD_I_KLAGE;
        }
        if (KlageVurdering.OPPHEVE_YTELSESVEDTAK.equals(vurdering)) {
            return HistorikkResultatType.OPPHEVE_VEDTAK;
        }
        if (KlageVurdering.STADFESTE_YTELSESVEDTAK.equals(vurdering)) {
            if (erNfpAksjonspunkt) {
                return HistorikkResultatType.OPPRETTHOLDT_VEDTAK;
            }
            return HistorikkResultatType.STADFESTET_VEDTAK;
        }
        return null;
    }

    private void oppdatereDatavarehus(KlageVurderingResultatAksjonspunktDto dto, Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        KlageVurdering klageVurdering = kodeverkRepository.finn(KlageVurdering.class, dto.getKlageVurdering().getKode());
        if (erNfpAksjonspunkt(aksjonspunktDefinisjon) && klageVurdering.equals(KlageVurdering.STADFESTE_YTELSESVEDTAK)) {

            // TODO(FLUORITT): Midlertidig hardkodet inn for Klageinstans da den ikke kommer med i response fra NORG. Fjern dette når det er på plass.
            behandlingsutredningApplikasjonTjeneste.byttBehandlendeEnhet(behandling.getId(),
                new OrganisasjonsEnhet("4205", "NAV Klageinstans Midt-Norge"),
                "", //Det er ikke behov for en begrunnelse i dette tilfellet.
                HistorikkAktør.VEDTAKSLØSNINGEN);
        }
    }

    private boolean erNfpAksjonspunkt(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return Objects.equals(aksjonspunktDefinisjon.getKode(), AksjonspunktDefinisjon.MANUELL_VURDERING_AV_KLAGE_NFP.getKode());
    }
}
