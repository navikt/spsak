
package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.JA;
import static no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall.NEI;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.behandling.aksjonspunkt.Utfall;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.OppgittTilknytning;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningerAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;

class AvklarOmErBosatt {
    private PersonopplysningTjeneste personopplysningTjeneste;
    private MedlemskapRepository medlemskapRepository;
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;

    AvklarOmErBosatt(BehandlingRepositoryProvider repositoryProvider,
                     MedlemskapPerioderTjeneste medlemskapPerioderTjeneste,
                     PersonopplysningTjeneste personopplysningTjeneste) {
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.medlemskapPerioderTjeneste = medlemskapPerioderTjeneste;
        this.personopplysningTjeneste = personopplysningTjeneste;
    }

    Optional<MedlemResultat> utled(Behandling behandling, LocalDate vurderingsdato) {
        if (harBrukerTilknytningHjemland(behandling) == NEI) {
            return Optional.of(MedlemResultat.AVKLAR_OM_ER_BOSATT);
        } else if (harBrukerUtenlandskPostadresseITps(behandling, vurderingsdato) == NEI) {
            return Optional.empty();
        } else {
            if (erFrivilligMedlemEllerIkkeMedlem(behandling, vurderingsdato) == NEI) {
                return Optional.of(MedlemResultat.AVKLAR_OM_ER_BOSATT);
            } else {
                return Optional.empty();
            }
        }
    }

    private Utfall harBrukerUtenlandskPostadresseITps(Behandling behandling, LocalDate vurderingsdato) {
        PersonopplysningerAggregat personopplysninger = personopplysningTjeneste.hentGjeldendePersoninformasjonPåTidspunkt(behandling, vurderingsdato);

        if (personopplysninger.getAdresserFor(behandling.getAktørId()).stream().anyMatch(adresse -> AdresseType.POSTADRESSE_UTLAND.equals(adresse.getAdresseType()) ||
            !Landkoder.erNorge(adresse.getLand()))) {
            return JA;
        }
        return NEI;
    }

    //TODO(OJR) må denne endres?
    private Utfall harBrukerTilknytningHjemland(Behandling behandling) {
        final Optional<MedlemskapAggregat> medlemskapAggregat = medlemskapRepository.hentMedlemskap(behandling);
        final OppgittTilknytning oppgittTilknytning = medlemskapAggregat.flatMap(MedlemskapAggregat::getOppgittTilknytning)
            .orElseThrow(IllegalStateException::new);

        int antallNei = 0;
        if (!oppgittTilknytning.isOppholdINorgeSistePeriode())
            antallNei++;
        if (!oppgittTilknytning.isOppholdNå())
            antallNei++;
        if (!oppgittTilknytning.isOppholdINorgeNestePeriode())
            antallNei++;

        if (antallNei >= 2) {
            return NEI;
        }
        return JA;
    }

    private Utfall erFrivilligMedlemEllerIkkeMedlem(Behandling behandling, LocalDate vurderingsdato) {


        Optional<MedlemskapAggregat> medlemskap = medlemskapRepository.hentMedlemskap(behandling);

        Collection<RegistrertMedlemskapPerioder> medlemskapsPerioder = medlemskap.isPresent()
            ? medlemskap.get().getRegistrertMedlemskapPerioder()
            : Collections.emptyList();
        List<MedlemskapDekningType> medlemskapDekningTyper = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapsPerioder, vurderingsdato);

        boolean erRegistrertSomIkkeMedlem = medlemskapPerioderTjeneste.erRegistrertSomIkkeMedlem(medlemskapDekningTyper);
        boolean erRegistrertSomFrivilligMedlem = medlemskapPerioderTjeneste.erRegistrertSomFrivilligMedlem(medlemskapDekningTyper);
        return erRegistrertSomIkkeMedlem || erRegistrertSomFrivilligMedlem ? JA : NEI;
    }
}
