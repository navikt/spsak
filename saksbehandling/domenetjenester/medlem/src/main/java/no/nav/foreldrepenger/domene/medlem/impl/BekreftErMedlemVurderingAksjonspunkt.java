package no.nav.foreldrepenger.domene.medlem.impl;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapManuellVurderingType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskap;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.VurdertMedlemskapBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabellRepository;
import no.nav.foreldrepenger.domene.medlem.api.BekreftErMedlemVurderingAksjonspunktDto;

class BekreftErMedlemVurderingAksjonspunkt {

    private KodeverkTabellRepository kodeverkRepository;
    private MedlemskapRepository medlemskapRepository;

    BekreftErMedlemVurderingAksjonspunkt(GrunnlagRepositoryProvider repositoryProvider) {
        this.kodeverkRepository = repositoryProvider.getKodeverkRepository().getKodeverkTabellRepository();
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
    }

    void oppdater(Behandling behandling, BekreftErMedlemVurderingAksjonspunktDto adapter) {

        MedlemskapManuellVurderingType medlemskapManuellVurderingType = kodeverkRepository
            .finnMedlemskapManuellVurderingType(adapter.getManuellVurderingTypeKode());

        Optional<VurdertMedlemskap> vurdertMedlemskap = medlemskapRepository.hentVurdertMedlemskap(behandling);

        VurdertMedlemskap nytt = new VurdertMedlemskapBuilder(vurdertMedlemskap)
            .medMedlemsperiodeManuellVurdering(medlemskapManuellVurderingType)
            .build();

        medlemskapRepository.lagreMedlemskapVurdering(behandling, nytt);
    }

}
