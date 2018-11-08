package no.nav.foreldrepenger.domene.registerinnhenting;

import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;

public interface RegisterdataInnhenter {

    List<RegistrertMedlemskapPerioder> innhentMedlemskapsopplysninger(Personinfo søkerInfo, Behandling behandling);

    Optional<Personinfo> innhentSaksopplysningerForMedSøker(Behandling behandling);

    Personinfo innhentSaksopplysningerForSøker(Behandling behandling);

    Personinfo innhentPersonopplysninger(Behandling behandling);

    void innhentPersonopplysninger(Behandling behandling, Personinfo søkerInfo,
                                   Optional<Personinfo> medsøkerInfo);

    void innhentIAYOpplysninger(Behandling behandling, Personinfo søkerInfo);

    PersonInformasjonBuilder byggPersonopplysningMedRelasjoner(Personinfo søkerInfo, Optional<Personinfo> medsøkerInfo, Behandling behandling);

    void oppdaterSistOppdatertTidspunkt(Behandling behandling);

    void opprettProsesstaskForRelaterteYtelser(Behandling behandling);
}
