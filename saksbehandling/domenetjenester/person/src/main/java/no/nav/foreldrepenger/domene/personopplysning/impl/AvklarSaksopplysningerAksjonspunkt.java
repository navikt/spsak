package no.nav.foreldrepenger.domene.personopplysning.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.PersonstatusType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingsgrunnlagKodeverkRepository;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningAksjonspunktDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

class AvklarSaksopplysningerAksjonspunkt {
    private BehandlingsgrunnlagKodeverkRepository kodeverkRepository;
    private PersonopplysningRepository personopplysningRepository;

    AvklarSaksopplysningerAksjonspunkt(BehandlingRepositoryProvider repositoryProvider) {
        this.kodeverkRepository = repositoryProvider.getBehandlingsgrunnlagKodeverkRepository();
        this.personopplysningRepository = repositoryProvider.getPersonopplysningRepository();
    }

    void oppdater(Behandling behandling, PersonopplysningAksjonspunktDto adapter) {
        PersonInformasjonBuilder builder = personopplysningRepository.opprettBuilderForOverstyring(behandling);

        LocalDate fom = adapter.getPersonstatusTypeKode().get().getGyldigFom();
        LocalDate tom = adapter.getPersonstatusTypeKode().get().getGyldigTom();
        DatoIntervallEntitet intervall = DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom);

        final PersonstatusType status = settPersonstatusType(adapter.getPersonstatusTypeKode().map(PersonopplysningAksjonspunktDto.PersonstatusPeriode::getPersonstatus));
        if (status != null) {
            PersonInformasjonBuilder.PersonstatusBuilder medPersonstatus = builder.getPersonstatusBuilder(behandling.getAktørId(), intervall)
                .medAktørId(behandling.getAktørId())
                .medPeriode(intervall)
                .medPersonstatus(status);
            builder.leggTil(medPersonstatus);

            personopplysningRepository.lagre(behandling, builder);
        }
    }

    private PersonstatusType settPersonstatusType(Optional<String> personstatus) {
        if (personstatus.isPresent()) {
            List<PersonstatusType> personstatusType = kodeverkRepository.personstatusTyperFortsattBehandling();
            final String personstatusen = personstatus.get();
            for (PersonstatusType type : personstatusType) {
                if (type.getKode().equals(personstatusen)) {
                    return type;
                }
            }
        }
        return null;
    }

}
