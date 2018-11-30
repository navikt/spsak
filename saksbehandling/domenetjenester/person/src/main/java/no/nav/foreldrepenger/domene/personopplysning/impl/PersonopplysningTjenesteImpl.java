package no.nav.foreldrepenger.domene.personopplysning.impl;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandling.SkjæringstidspunktTjeneste;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatDiff;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonInformasjon;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.person.TpsAdapter;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningAksjonspunktDto;
import no.nav.foreldrepenger.domene.personopplysning.PersonopplysningTjeneste;
import no.nav.foreldrepenger.domene.personopplysning.VergeAksjonpunktDto;

@ApplicationScoped
public class PersonopplysningTjenesteImpl extends AbstractPersonopplysningTjenesteImpl implements PersonopplysningTjeneste {

    private BehandlingRepositoryProvider repositoryProvider;
    private TpsAdapter tpsAdapter;
    private NavBrukerRepository navBrukerRepository;

    PersonopplysningTjenesteImpl() {
        super();
        // CDI
    }

    @Inject
    public PersonopplysningTjenesteImpl(BehandlingRepositoryProvider repositoryProvider,
                                        TpsAdapter tpsAdapter,
                                        NavBrukerRepository navBrukerRepository,
                                        SkjæringstidspunktTjeneste skjæringstidspunktTjeneste) {
        super(repositoryProvider, skjæringstidspunktTjeneste);
        this.repositoryProvider = repositoryProvider;
        this.tpsAdapter = tpsAdapter;
        this.navBrukerRepository = navBrukerRepository;
    }

    @Override
    public void aksjonspunktVergeOppdaterer(Behandling behandling, VergeAksjonpunktDto adapter) {
        new VergeOppdatererAksjonspunkt(repositoryProvider, tpsAdapter, navBrukerRepository).oppdater(behandling, adapter);
    }

    @Override
    public void aksjonspunktAvklarSaksopplysninger(Behandling behandling, PersonopplysningAksjonspunktDto adapter) {
        new AvklarSaksopplysningerAksjonspunkt(repositoryProvider).oppdater(behandling, adapter);
    }

    @Override
    public EndringsresultatSnapshot finnAktivGrunnlagId(Behandling behandling) {
        Optional<Long> funnetId = getPersonopplysningRepository().hentIdPåAktivPersonopplysninger(behandling);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(PersonInformasjon.class, id))
            .orElse(EndringsresultatSnapshot.utenSnapshot(PersonInformasjon.class));
    }

    @Override
    public DiffResult diffResultat(EndringsresultatDiff idDiff, FagsakYtelseType ytelseType, boolean kunSporedeEndringer) {
        PersonopplysningGrunnlag grunnlag1 = getPersonopplysningRepository().hentPersonopplysningerPåId(idDiff.getGrunnlagId1());
        PersonopplysningGrunnlag grunnlag2 = getPersonopplysningRepository().hentPersonopplysningerPåId(idDiff.getGrunnlagId2());
        return getPersonopplysningRepository().diffResultat(grunnlag1, grunnlag2, ytelseType, kunSporedeEndringer);
    }

}
