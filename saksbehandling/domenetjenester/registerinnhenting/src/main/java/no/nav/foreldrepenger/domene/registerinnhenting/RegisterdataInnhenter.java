package no.nav.foreldrepenger.domene.registerinnhenting;

import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjonBuilder;

public interface RegisterdataInnhenter {

    List<RegistrertMedlemskapPerioder> innhentMedlemskapsopplysninger(Personinfo søkerInfo, Behandling behandling);

    Personinfo innhentSaksopplysningerForSøker(Behandling behandling);

    Personinfo innhentPersonopplysninger(Behandling behandling);

    void innhentPersonopplysninger(Behandling behandling, Personinfo søkerInfo);

    void innhentIAYOpplysninger(Behandling behandling, Personinfo søkerInfo);

    PersonInformasjonBuilder byggPersonopplysningMedRelasjoner(Personinfo søkerInfo, Behandling behandling);

    void oppdaterSistOppdatertTidspunkt(Behandling behandling);

    void opprettProsesstaskForRelaterteYtelser(Behandling behandling);
}
