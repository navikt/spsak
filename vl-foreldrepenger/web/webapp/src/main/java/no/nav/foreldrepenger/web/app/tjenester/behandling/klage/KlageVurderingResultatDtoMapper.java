package no.nav.foreldrepenger.web.app.tjenester.behandling.klage;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;

public class KlageVurderingResultatDtoMapper {

    private KlageVurderingResultatDtoMapper() {
    }

    public static Optional<KlageVurderingResultatDto> mapNFPKlageVurderingResultatDto(Behandling behandling) {
        Optional<KlageVurderingResultat> resultatOpt = behandling.hentKlageVurderingResultat(KlageVurdertAv.NFP);
        return resultatOpt.map(KlageVurderingResultatDtoMapper::lagDto);
    }

    public static Optional<KlageVurderingResultatDto> mapNKKlageVurderingResultatDto(Behandling behandling) {
        Optional<KlageVurderingResultat> resultatOpt = behandling.hentKlageVurderingResultat(KlageVurdertAv.NK);
        return resultatOpt.map(KlageVurderingResultatDtoMapper::lagDto);
    }

    private static KlageVurderingResultatDto lagDto(KlageVurderingResultat klageVurderingResultat) {
        String klageAvvistArsak = klageVurderingResultat.getKlageAvvistÅrsak() == null ? null : klageVurderingResultat.getKlageAvvistÅrsak().getKode();
        String klageAvvistArsakNavn = klageVurderingResultat.getKlageAvvistÅrsak() == null ? null : klageVurderingResultat.getKlageAvvistÅrsak().getNavn();
        String klageMedholdArsak = klageVurderingResultat.getKlageMedholdÅrsak() == null ? null : klageVurderingResultat.getKlageMedholdÅrsak().getKode();
        String klageMedholdArsakNavn = klageVurderingResultat.getKlageMedholdÅrsak() == null ? null : klageVurderingResultat.getKlageMedholdÅrsak().getNavn();
        KlageVurderingResultatDto dto = new KlageVurderingResultatDto();

        dto.setKlageVurdering(klageVurderingResultat.getKlageVurdering().getKode());
        dto.setBegrunnelse(klageVurderingResultat.getBegrunnelse());
        dto.setKlageAvvistArsak(klageAvvistArsak);
        dto.setKlageAvvistArsakNavn(klageAvvistArsakNavn);
        dto.setKlageMedholdArsak(klageMedholdArsak);
        dto.setKlageMedholdArsakNavn(klageMedholdArsakNavn);
        dto.setKlageVurdertAv(klageVurderingResultat.getKlageVurdertAv().getKode());
        dto.setVedtaksdatoPaklagdBehandling(klageVurderingResultat.getVedtaksdatoPåklagdBehandling());
        return dto;
    }
}
