package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.util.Collection;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;

public interface KodeverkTabellRepository {

    BehandlingStegType finnBehandlingStegType(String kode);

    Venteårsak finnVenteårsak(String kode);

    MedlemskapManuellVurderingType finnMedlemskapManuellVurderingType(String kode);

    Set<VurderÅrsak> finnVurderÅrsaker(Collection<String> koder);

    StartpunktType finnStartpunktType(String kode);

    AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode);
}
