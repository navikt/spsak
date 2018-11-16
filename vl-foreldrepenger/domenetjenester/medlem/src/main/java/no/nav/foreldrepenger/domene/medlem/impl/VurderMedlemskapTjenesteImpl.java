package no.nav.foreldrepenger.domene.medlem.impl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.domene.medlem.api.MedlemskapPerioderTjeneste;
import no.nav.foreldrepenger.domene.medlem.api.VurderMedlemskapTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;

@ApplicationScoped
public class VurderMedlemskapTjenesteImpl implements VurderMedlemskapTjeneste {

    private AvklarOmErBosatt avklarOmErBosatt;
    private AvklarGyldigPeriode avklarGyldigPeriode;
    private AvklarOmSøkerOppholderSegINorge avklarOmSøkerOppholderSegINorge;
    private AvklaringFaktaMedlemskap avklaringFaktaMedlemskap;
    private BehandlingRepository behandlingRepository;

    VurderMedlemskapTjenesteImpl() {
        //CDI
    }

    @Inject
    public VurderMedlemskapTjenesteImpl(BehandlingRepositoryProvider provider,
                                        MedlemskapPerioderTjeneste medlemskapPerioderTjeneste,
                                        PersonopplysningTjeneste personopplysningTjeneste) {
        this.behandlingRepository = provider.getBehandlingRepository();
        this.avklarOmErBosatt = new AvklarOmErBosatt(provider, medlemskapPerioderTjeneste, personopplysningTjeneste);
        this.avklarGyldigPeriode = new AvklarGyldigPeriode(provider, medlemskapPerioderTjeneste);
        this.avklarOmSøkerOppholderSegINorge = new AvklarOmSøkerOppholderSegINorge(provider, personopplysningTjeneste);
        this.avklaringFaktaMedlemskap = new AvklaringFaktaMedlemskap(provider, medlemskapPerioderTjeneste, personopplysningTjeneste);
    }

    @Override
    public Set<MedlemResultat> vurderMedlemskap(Long behandlingId, LocalDate vurderingsdato) {
        Set<MedlemResultat> resultat = new HashSet<>();
        Behandling behandling = behandlingRepository.hentBehandling(behandlingId);
        avklarOmErBosatt.utled(behandling, vurderingsdato).ifPresent(resultat::add);
        avklarGyldigPeriode.utled(behandling, vurderingsdato).ifPresent(resultat::add);
        avklarOmSøkerOppholderSegINorge.utled(behandling, vurderingsdato).ifPresent(resultat::add);
        avklaringFaktaMedlemskap.utled(behandling, vurderingsdato).ifPresent(resultat::add);
        return resultat;
    }
}
