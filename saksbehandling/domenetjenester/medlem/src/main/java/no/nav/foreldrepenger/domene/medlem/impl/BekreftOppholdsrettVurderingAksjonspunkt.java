package no.nav.foreldrepenger.domene.medlem.impl;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.BekreftOppholdVurderingAksjonspunktDto;

class BekreftOppholdsrettVurderingAksjonspunkt {

    private MedlemskapRepository medlemskapRepository;

    BekreftOppholdsrettVurderingAksjonspunkt(GrunnlagRepositoryProvider repositoryProvider) {
        medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    }

    void oppdater(Behandling behandling, BekreftOppholdVurderingAksjonspunktDto adapter) {
        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);
        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskap.flatMap(MedlemskapAggregat::getVurdertMedlemskap);

        VurdertMedlemskap nytt = new VurdertMedlemskapBuilder(vurdertMedlemskap)
            .medOppholdsrettVurdering(adapter.getOppholdsrettVurdering())
            .medLovligOppholdVurdering(adapter.getLovligOppholdVurdering())
            .medErEosBorger(adapter.getErEosBorger())
            .build();

        medlemskapRepository.lagreMedlemskapVurdering(behandling, nytt);
    }
}
