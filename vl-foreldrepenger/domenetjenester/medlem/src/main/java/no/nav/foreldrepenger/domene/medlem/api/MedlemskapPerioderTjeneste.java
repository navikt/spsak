package no.nav.foreldrepenger.domene.medlem.api;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;

public interface MedlemskapPerioderTjeneste {
    boolean brukerMaskineltAvklartSomIkkeMedlem(Behandling behandling, PersonopplysningerAggregat personopplysningerAggregat,
                                                FamilieHendelseGrunnlag familieHendelseGrunnlag,
                                                Set<RegistrertMedlemskapPerioder> medlemskapPerioder);

    boolean erNySkjæringsdatoUtenforInnhentetMedlemskapsintervall(Behandling behandling, LocalDate nyDato,
                                                                  FamilieHendelseGrunnlag familieHendelseGrunnlag);

    boolean erRegistrertSomUnntatt(List<MedlemskapDekningType> dekningTyper);

    boolean erRegistrertSomIkkeMedlem(List<MedlemskapDekningType> dekningTyper);

    boolean brukerMaskineltAvklartSomFrivilligEllerPliktigMedlem(Behandling behandling,
                                                                 FamilieHendelseGrunnlag familieHendelseGrunnlag,
                                                                 Set<RegistrertMedlemskapPerioder> medlemskapPerioder);

    boolean erRegistrertSomFrivilligMedlem(List<MedlemskapDekningType> dekningTyper);

    boolean erRegistrertSomAvklartMedlemskap(List<MedlemskapDekningType> dekningTyper);

    boolean erRegistrertSomUavklartMedlemskap(List<MedlemskapDekningType> dekningTyper);

    List<MedlemskapDekningType> finnGyldigeDekningstyper(Collection<RegistrertMedlemskapPerioder> medlemskapPerioder, LocalDate skjæringsdato);

    boolean harStatsborgerskapUsaEllerPng(PersonopplysningerAggregat personopplysningerAggregat);

    boolean erStatusUtvandret(PersonopplysningerAggregat bruker);

    boolean harPeriodeUnderAvklaring(Set<RegistrertMedlemskapPerioder> medlemskapPerioder, LocalDate skjæringsdato);
}
