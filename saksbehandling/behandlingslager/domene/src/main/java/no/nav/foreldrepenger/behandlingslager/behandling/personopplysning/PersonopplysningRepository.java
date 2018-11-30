package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

public interface PersonopplysningRepository extends BehandlingslagerRepository {

    void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling eksisterendeBehandling, Behandling nyBehandling);

    DiffResult diffResultat(PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2, FagsakYtelseType fagsakYtelseType, boolean onlyCheckTrackedFields);

    Optional<PersonopplysningGrunnlag> hentPersonopplysningerHvisEksisterer(Behandling behandling);

    PersonopplysningGrunnlag hentPersonopplysninger(Behandling behandling);

    void lagre(Behandling behandling, PersonInformasjonBuilder builder);

    PersonInformasjonBuilder opprettBuilderForOverstyring(Behandling behandling);

    PersonInformasjonBuilder opprettBuilderForRegisterdata(Behandling behandling);

    /**
     * Kopierer grunnlag fra en tidligere behandling.  Endrer ikke aggregater, en skaper nye referanser til disse.
     */
    void kopierGrunnlagFraEksisterendeBehandling(Behandling gammelBehandling, Behandling nyBehandling);

    PersonopplysningGrunnlag hentFørsteVersjonAvPersonopplysninger(Behandling behandling);

    Optional<Long> hentIdPåAktivPersonopplysninger(Behandling behandling);

    PersonopplysningGrunnlag hentPersonopplysningerPåId(Long aggregatId);
}
