package no.nav.foreldrepenger.behandlingslager.behandling.repository;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.medlemskap.MedlemskapVilkårPeriodeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.opptjening.OpptjeningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.PersonopplysningRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.SøknadRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vedtak.BehandlingVedtakRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.verge.VergeRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakLåsRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakRepository;

public interface BehandlingRepositoryProvider {

    KodeverkRepository getKodeverkRepository();

    PersonopplysningRepository getPersonopplysningRepository();

    MedlemskapVilkårPeriodeRepository getMedlemskapVilkårPeriodeRepository();

    BehandlingLåsRepository getBehandlingLåsRepository();

    FagsakRepository getFagsakRepository();

    AksjonspunktRepository getAksjonspunktRepository();

    VilkårKodeverkRepository getVilkårKodeverkRepository();

    BehandlingsgrunnlagKodeverkRepository getBehandlingsgrunnlagKodeverkRepository();

    BehandlingRepository getBehandlingRepository();

    HistorikkRepository getHistorikkRepository();

    MedlemskapRepository getMedlemskapRepository();

    SøknadRepository getSøknadRepository();

    VergeRepository getVergeGrunnlagRepository();

    InnsynRepository getInnsynRepository();

    BeregningsgrunnlagRepository getBeregningsgrunnlagRepository();

    UttakRepository getUttakRepository();

    InntektArbeidYtelseRepository getInntektArbeidYtelseRepository();

    VirksomhetRepository getVirksomhetRepository();

    BeregningRepository getBeregningRepository();

    BehandlingVedtakRepository getBehandlingVedtakRepository();

    OpptjeningRepository getOpptjeningRepository();

    BeregningsresultatFPRepository getBeregningsresultatFPRepository();

    MottatteDokumentRepository getMottatteDokumentRepository();

    BehandlingRevurderingRepository getBehandlingRevurderingRepository();

    FagsakLåsRepository getFagsakLåsRepository();
}
