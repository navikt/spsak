package no.nav.foreldrepenger.domene.dokument.impl;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAktør;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageAvvistÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageMedholdÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdering;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerApplikasjonTjeneste;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.BestillBrevDto;
import no.nav.foreldrepenger.domene.dokument.KlageVurderingAksjonspunktDto;

class KlageVurderingAksjonspunkt {

    private DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;

    KlageVurderingAksjonspunkt(DokumentBestillerApplikasjonTjeneste dokumentBestillerApplikasjonTjeneste,
                               BehandlingRepositoryProvider repositoryProvider) {
        this.dokumentBestillerApplikasjonTjeneste = dokumentBestillerApplikasjonTjeneste;
        this.behandlingRepository = repositoryProvider.getBehandlingRepository();
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository();
    }

    void oppdater(Behandling behandling, KlageVurderingAksjonspunktDto adapter) {
        byggOgLagreKlageVurderingResultat(behandling, adapter);
        settBehandlingResultatTypeBasertPaaUtfall(behandling, adapter);
    }

    private void byggOgLagreKlageVurderingResultat(Behandling behandling, KlageVurderingAksjonspunktDto adapter) {
        KlageVurdering klageVurdering = kodeverkRepository.finn(KlageVurdering.class, adapter.getKlageVurderingKode());
        KlageVurderingResultat.Builder klageVurderingResultatBuilder = new KlageVurderingResultat.Builder()
            .medBegrunnelse(adapter.getBegrunnelse())
            .medVedtaksdatoPåklagdBehandling(adapter.getVedtaksdatoPaklagdBehandling())
            .medKlageVurdering(klageVurdering)
            .medKlageVurdertAv(adapter.getErNfpAksjonspunkt() ? KlageVurdertAv.NFP : KlageVurdertAv.NK)
            .medBehandling(behandling);

        Optional<String> klageAvvistÅrsak = adapter.getKlageAvvistArsakKode();
        klageAvvistÅrsak.ifPresent(avvistÅrsak -> klageVurderingResultatBuilder
            .medKlageAvvistÅrsak(kodeverkRepository.finn(KlageAvvistÅrsak.class, avvistÅrsak)));

        Optional<String> klageMedholdÅrsak = adapter.getKlageMedholdArsakKode();
        klageMedholdÅrsak.ifPresent(medholdÅrsak -> klageVurderingResultatBuilder
            .medKlageMedholdÅrsak(kodeverkRepository.finn(KlageMedholdÅrsak.class, medholdÅrsak)));

        BehandlingLås lås = behandlingRepository.taSkriveLås(behandling);
        final KlageVurderingResultat klageVurderingResultat = klageVurderingResultatBuilder.build();
        behandlingRepository.lagre(klageVurderingResultat, lås);
        behandling.leggTilKlageVurderingResultat(klageVurderingResultat);
    }

    private void settBehandlingResultatTypeBasertPaaUtfall(Behandling behandling, KlageVurderingAksjonspunktDto adapter) {
        KlageVurdering klageVurdering = kodeverkRepository.finn(KlageVurdering.class, adapter.getKlageVurderingKode());
        if (adapter.getErNfpAksjonspunkt() && klageVurdering.equals(KlageVurdering.STADFESTE_YTELSESVEDTAK)) {
            BestillBrevDto bestillBrevDto = new BestillBrevDto(behandling.getId(), DokumentMalType.KLAGE_OVERSENDT_KLAGEINSTANS_DOK);
            dokumentBestillerApplikasjonTjeneste.bestillDokument(bestillBrevDto, HistorikkAktør.SAKSBEHANDLER);
        }
        if(behandling.getBehandlingsresultat() == null) {
            Behandlingsresultat.opprettFor(behandling);
        }
        Behandlingsresultat.builderEndreEksisterende(behandling.getBehandlingsresultat()).medBehandlingResultatType(BehandlingResultatType.tolkBehandlingResultatType(klageVurdering));
    }
}
