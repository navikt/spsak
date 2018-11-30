package no.nav.foreldrepenger.domene.medlem.impl;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.AvklarFortsattMedlemskapAksjonspunktDto;

class AvklarFortsattMedlemskapAksjonspunkt {

    private MedlemskapRepository medlemskapRepository;

    AvklarFortsattMedlemskapAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    }

    void oppdater(Behandling behandling, AvklarFortsattMedlemskapAksjonspunktDto adapter) {

        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);

        VurdertMedlemskapBuilder builder = new VurdertMedlemskapBuilder(vurdertMedlemskap)
            .medFom(adapter.getFomDato());

        // TODO Termitt Aksjonspunkt 5053 bør splittes med eget aksjonspunktspunkt for endring i personopplysninger
        // TODO Termitt Og bruke overstyrt versjon av personopplysninger i stedet for MedlemskapManuellVurderingType
        if (adapter.isGjelderEndringIPersonopplysninger()) {
            builder.medMedlemsperiodeManuellVurdering(MedlemskapManuellVurderingType.SAKSBEHANDLER_SETTER_OPPHØR_AV_MEDL_PGA_ENDRINGER_I_TPS);
        }

        medlemskapRepository.lagreMedlemskapVurdering(behandling, builder.build());
    }
}
