package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.SykefraværRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;

/**
 * Repository provider for grunnlagsdata
 */
public interface GrunnlagRepositoryProvider {

    KodeverkRepository getKodeverkRepository();

    PersonopplysningRepository getPersonopplysningRepository();

    BehandlingLåsRepository getBehandlingLåsRepository();
    
    BehandlingskontrollRepository getBehandlingskontrollRepository();

    FagsakRepository getFagsakRepository();

    AksjonspunktRepository getAksjonspunktRepository();

    VilkårKodeverkRepository getVilkårKodeverkRepository();

    BehandlingsgrunnlagKodeverkRepository getBehandlingsgrunnlagKodeverkRepository();

    BehandlingRepository getBehandlingRepository();

    HistorikkRepository getHistorikkRepository();

    MedlemskapRepository getMedlemskapRepository();

    SøknadRepository getSøknadRepository();

    VergeRepository getVergeGrunnlagRepository();

    InntektArbeidYtelseRepository getInntektArbeidYtelseRepository();

    VirksomhetRepository getVirksomhetRepository();

    SatsRepository getSatsRepository();

    BehandlingRevurderingRepository getBehandlingRevurderingRepository();

    FagsakLåsRepository getFagsakLåsRepository();

    SykefraværRepository getSykefraværRepository();
}
