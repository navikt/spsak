package no.nav.foreldrepenger.domene.mottak.dokumentmottak.impl;

import static java.time.LocalDate.now;

import java.time.LocalDate;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingskontrollRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.ResultatRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.BehandlingVedtak;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak.VedtakResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.AbstractTestScenario;
import no.nav.foreldrepenger.behandlingslager.testutilities.behandling.ScenarioMorSøkerForeldrepenger;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.mottak.Behandlingsoppretter;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.InngåendeSaksdokument;
import no.nav.foreldrepenger.domene.mottak.dokumentmottak.MottatteDokumentTjeneste;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public abstract class DokumentmottakerTestsupport {

    protected static final int FRIST_INNSENDING_UKER = 6;
    protected final LocalDate DATO_ETTER_INNSENDINGSFRISTEN = LocalDate.now().minusWeeks(FRIST_INNSENDING_UKER + 2);
    protected final LocalDate DATO_FØR_INNSENDINGSFRISTEN = LocalDate.now().minusWeeks(FRIST_INNSENDING_UKER - 2);

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    protected DokumentmottakerFelles dokumentmottakerFelles;
    @Inject
    protected MottatteDokumentTjeneste mottatteDokumentTjeneste;
    @Inject
    protected Behandlingsoppretter behandlingsoppretter;
    @Inject
    protected Kompletthetskontroller kompletthetskontroller;
    @Inject
    protected GrunnlagRepositoryProvider repositoryProvider;
    @Inject
    protected ResultatRepositoryProvider resultatRepositoryProvider;
    
    @Inject 
    protected BehandlingskontrollRepository behandlingskontrollRepository;

    protected Behandling opprettBehandling(FagsakYtelseType fagsakYtelseType, BehandlingType behandlingType, BehandlingResultatType behandlingResultatType, Avslagsårsak avslagsårsak, VedtakResultatType vedtakResultatType, LocalDate vedtaksdato) {

        ScenarioMorSøkerForeldrepenger scenario = ScenarioMorSøkerForeldrepenger.forDefaultAktør()
            .medFagsakId(1234L)
            .medSaksnummer(new Saksnummer("2345"))
            .medBehandlingType(behandlingType);
        return opprettBehandling(scenario, behandlingType, behandlingResultatType, avslagsårsak, vedtakResultatType, vedtaksdato);
    }

    private Behandling opprettBehandling(AbstractTestScenario<?> scenario, BehandlingType behandlingType, BehandlingResultatType behandlingResultatType, Avslagsårsak avslagsårsak, VedtakResultatType vedtakResultatType, LocalDate vedtaksdato) {

        Behandling behandling = scenario.lagre(repositoryProvider, resultatRepositoryProvider);

        Behandlingsresultat behandlingsresultat = Behandlingsresultat.builder()
            .medBehandlingResultatType(behandlingResultatType)
            .medAvslagsårsak(avslagsårsak)
            .buildFor(behandling);

        BehandlingLås behandlingLås = repositoryProvider.getBehandlingRepository().taSkriveLås(behandling);
        repositoryProvider.getBehandlingRepository().lagre(behandling, behandlingLås);

        BehandlingVedtak originalVedtak = BehandlingVedtak.builder()
            .medVedtaksdato(vedtaksdato)
            .medBehandlingsresultat(behandlingsresultat)
            .medVedtakResultatType(vedtakResultatType)
            .medAnsvarligSaksbehandler("fornavn etternavn")
            .build();

        
        resultatRepositoryProvider.getVedtakRepository().lagre(originalVedtak, behandlingLås);

        VilkårResultat vilkårResultat = VilkårResultat.builder()
            .leggTilVilkår(VilkårType.SØKERSOPPLYSNINGSPLIKT, VilkårUtfallType.IKKE_OPPFYLT)
            .buildFor(behandling);
        repositoryProvider.getBehandlingRepository().lagre(vilkårResultat, behandlingLås);

        scenario.avsluttBehandling(repositoryProvider, behandling);

        return behandling;
    }

    protected InngåendeSaksdokument dummyInntektsmeldingDokument(Behandling behandling) {
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        return DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "123");
    }

    protected InngåendeSaksdokument dummyVedleggDokument(Behandling behandling) {
        DokumentTypeId dokumentTypeId = DokumentTypeId.LEGEERKLÆRING;
        return DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "456");
    }

    protected InngåendeSaksdokument dummySøknadDokument(Behandling behandling) {
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        return DokumentmottakTestUtil.byggMottattDokument(dokumentTypeId, behandling.getFagsakId(), "", now(), true, "456");
    }

}
