package no.nav.foreldrepenger.domene.medlem.impl;

import static no.nav.foreldrepenger.domene.medlem.impl.MedlemResultat.AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE;
import static no.nav.foreldrepenger.domene.medlem.impl.Utfall.JA;
import static no.nav.foreldrepenger.domene.medlem.impl.Utfall.NEI;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapAggregat;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapDekningType;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.RegistrertMedlemskapPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;

class AvklarGyldigPeriode {

    private MedlemskapRepository medlemskapRepository;
    private MedlemskapPerioderTjeneste medlemskapPerioderTjeneste;

    AvklarGyldigPeriode(GrunnlagRepositoryProvider repositoryProvider,
                        MedlemskapPerioderTjeneste medlemskapPerioderTjeneste) {
        this.medlemskapRepository = repositoryProvider.getMedlemskapRepository();
        this.medlemskapPerioderTjeneste = medlemskapPerioderTjeneste;
    }

    Optional<MedlemResultat> utled(Behandling behandling, LocalDate vurderingsdato) {
        Optional<Set<RegistrertMedlemskapPerioder>> optPerioder = medlemskapRepository.hentMedlemskap(behandling)
            .map(MedlemskapAggregat::getRegistrertMedlemskapPerioder);

        Set<RegistrertMedlemskapPerioder> medlemskapPerioder = optPerioder.orElse(Collections.emptySet());

        // Har bruker treff i gyldig periode hjemlet i §2-9 bokstav a eller c?
        if (harGyldigMedlemsperiodeMedMedlemskap(vurderingsdato, medlemskapPerioder) == JA) {
            return Optional.empty();
        } else {
            if (harBrukerTreffIMedl(medlemskapPerioder) == NEI) {
                return Optional.empty();
            } else {
                // Har bruker treff i perioder som er under avklaring eller ikke har start eller sluttdato?
                if (harPeriodeUnderAvklaring(vurderingsdato, medlemskapPerioder) == NEI) {
                    return Optional.empty();
                }
                return Optional.of(AVKLAR_GYLDIG_MEDLEMSKAPSPERIODE);
            }
        }
    }

    private Utfall harGyldigMedlemsperiodeMedMedlemskap(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        List<MedlemskapDekningType> medlemskapDekningTyper = medlemskapPerioderTjeneste.finnGyldigeDekningstyper(medlemskapPerioder, vurderingsdato);
        return medlemskapPerioderTjeneste.erRegistrertSomFrivilligMedlem(medlemskapDekningTyper) ? JA : NEI;
    }

    private Utfall harPeriodeUnderAvklaring(LocalDate vurderingsdato, Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        return medlemskapPerioderTjeneste.harPeriodeUnderAvklaring(medlemskapPerioder, vurderingsdato) ? JA : NEI;
    }

    private Utfall harBrukerTreffIMedl(Set<RegistrertMedlemskapPerioder> medlemskapPerioder) {
        return medlemskapPerioder.isEmpty() ? NEI : JA;
    }
}
