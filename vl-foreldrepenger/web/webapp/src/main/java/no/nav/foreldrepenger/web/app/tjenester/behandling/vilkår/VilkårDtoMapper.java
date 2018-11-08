package no.nav.foreldrepenger.web.app.tjenester.behandling.vilkår;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;


class VilkårDtoMapper {

    private VilkårDtoMapper() {
        // SONAR - Utility classes should not have public constructors
    }

    static List<VilkårDto> lagVilkarDto(Behandling behandling, boolean medVilkårkjøring) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat != null) {
            VilkårResultat vilkårResultat = behandlingsresultat.getVilkårResultat();
            if (vilkårResultat != null) {
                List<VilkårDto> list = vilkårResultat.getVilkårene().stream().map(vilkår -> {
                    VilkårDto dto = new VilkårDto();
                    dto.setAvslagKode(vilkår.getAvslagsårsak() != null ? vilkår.getAvslagsårsak().getKode() : null);
                    dto.setVilkarType(vilkår.getVilkårType());
                    dto.setLovReferanse(vilkår.getVilkårType().getLovReferanse(behandling.getFagsakYtelseType()));
                    dto.setVilkarStatus(vilkår.getGjeldendeVilkårUtfall());
                    dto.setMerknadParametere(vilkår.getMerknadParametere());
                    dto.setOverstyrbar(erOverstyrbar(vilkår, behandling));

                    if (medVilkårkjøring) {
                        dto.setInput(vilkår.getRegelInput());
                        dto.setEvaluering(vilkår.getRegelEvaluering());
                    }

                    return dto;
                }).collect(Collectors.toList());
                return list;
            }
        }
        return Collections.emptyList();
    }

    // Angir om vilkåret kan overstyres (forutsetter at bruker har tilgang til å overstyre)
    private static boolean erOverstyrbar(Vilkår vilkår, Behandling behandling) {
        if (behandling.erÅpnetForEndring()) {
            // Manuelt åpnet for endring, må dermed også tillate overstyring
            return true;
        }
        if (behandling.getStartpunkt().equals(StartpunktType.UDEFINERT)) {
            // Startpunkt for behandling N/A, må dermed også tillate overstyring
            return true;
        }
        boolean vilkårLøstFørStartpunkt = StartpunktType.finnVilkårHåndtertInnenStartpunkt(behandling.getStartpunkt())
            .contains(vilkår.getVilkårType());
        return !vilkårLøstFørStartpunkt;
    }
}
