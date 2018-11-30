package no.nav.foreldrepenger.behandling.revurdering.impl;


import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.testutilities.fagsak.FagsakBuilder;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class FagsakRevurderingTest {

    private BehandlingRepository behandlingRepository;
    private Behandling behandling;
    private Behandling nyesteBehandling;
    private Behandling eldreBehandling;
    private Fagsak fagsak;
    private Saksnummer fagsakSaksnummer  = new Saksnummer("1");

    private Fagsak fagsakMedFlereBehandlinger;
    private Saksnummer fagsakMedFlereBehSaksnr  = new Saksnummer("2");

    @Before
    public void setup(){
        behandlingRepository = mock(BehandlingRepository.class);
    }

    @Before
    public void opprettBehandlinger() {
        fagsak = FagsakBuilder.nyFagsak().medSaksnummer(fagsakSaksnummer).build();
        behandling = Behandling.forFørstegangssøknad(fagsak).build();
        behandling = Behandling.forFørstegangssøknad(fagsak).build();

        fagsakMedFlereBehandlinger = FagsakBuilder.nyFagsak().medSaksnummer(fagsakMedFlereBehSaksnr).build();
        nyesteBehandling = Behandling.forFørstegangssøknad(fagsakMedFlereBehandlinger)
            .medAvsluttetDato(LocalDateTime.now())
            .build();
        eldreBehandling = Behandling.forFørstegangssøknad(fagsakMedFlereBehandlinger)
            .medAvsluttetDato(LocalDateTime.now().minusDays(1))
            .build();

        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsakSaksnummer))
            .thenReturn(singletonList(behandling));
        when(behandlingRepository.hentAbsoluttAlleBehandlingerForSaksnummer(fagsakMedFlereBehSaksnr))
            .thenReturn(asList(nyesteBehandling, eldreBehandling));
    }

    @Test
    public void kanIkkeOppretteRevurderingNårÅpenBehandling() throws Exception {
        Behandlingsresultat.opprettFor(behandling);
        when(behandlingRepository.hentBehandlingerSomIkkeErAvsluttetForFagsakId(anyLong())).thenReturn(Arrays.asList(behandling));

        FagsakRevurdering tjeneste = new FagsakRevurdering(behandlingRepository);
        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsak);
        assertThat(kanRevurderingOpprettes).isFalse();
    }

    @Test
    public void kanIkkeOppretteRevurderingNårBehandlingErHenlagt() {
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.HENLAGT_SØKNAD_TRUKKET).buildFor(behandling);

        FagsakRevurdering tjeneste = new FagsakRevurdering(behandlingRepository);
        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsak);

        assertThat(kanRevurderingOpprettes).isFalse();
    }

    @Test
    public void kanOppretteRevurderingNårEnBehandlingErVedtattMenSisteBehandlingErHenlagt() {
        eldreBehandling.avsluttBehandling();
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.HENLAGT_FEILOPPRETTET).buildFor(nyesteBehandling);
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.INNVILGET).buildFor(eldreBehandling);

        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.SØKERSOPPLYSNINGSPLIKT,
            VilkårUtfallType.OPPFYLT, null, null,
            null, false, false, null, null).buildFor(eldreBehandling);

        FagsakRevurdering tjeneste = new FagsakRevurdering(behandlingRepository);
        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsakMedFlereBehandlinger);

        assertThat(kanRevurderingOpprettes).isTrue();
    }

    @Test
    public void kanOppretteRevurderingNårFlereBehandlingerErVedtattOgSisteKanRevurderes() {
        eldreBehandling.avsluttBehandling();
        nyesteBehandling.avsluttBehandling();
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(eldreBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.SØKERSOPPLYSNINGSPLIKT,
            VilkårUtfallType.IKKE_OPPFYLT, null, null,
            null, false, false, null, null).buildFor(eldreBehandling);

        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(nyesteBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.SØKERSOPPLYSNINGSPLIKT,
            VilkårUtfallType.OPPFYLT, null, null,
            null, false, false, null, null).buildFor(nyesteBehandling);

        FagsakRevurdering tjeneste = new FagsakRevurdering(behandlingRepository);
        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsakMedFlereBehandlinger);

        assertThat(kanRevurderingOpprettes).isTrue();
    }

    @Test
    public void kanIkkeOppretteRevurderingNårFlereBehandlingerErVedtattOgSisteIkkeKanRevurderes() {
        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(nyesteBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.SØKERSOPPLYSNINGSPLIKT,
            VilkårUtfallType.IKKE_OPPFYLT, null, null,
            null, false, false, null, null).buildFor(nyesteBehandling);

        Behandlingsresultat.builder().medBehandlingResultatType(BehandlingResultatType.AVSLÅTT).buildFor(eldreBehandling);
        VilkårResultat.builder().leggTilVilkårResultat(VilkårType.SØKERSOPPLYSNINGSPLIKT,
            VilkårUtfallType.OPPFYLT, null, null,
            null, false, false, null, null).buildFor(eldreBehandling);

        FagsakRevurdering tjeneste = new FagsakRevurdering(behandlingRepository);
        Boolean kanRevurderingOpprettes = tjeneste.kanRevurderingOpprettes(fagsakMedFlereBehandlinger);

        assertThat(kanRevurderingOpprettes).isFalse();
    }

    @Test
    public void behandlingerSkalSorteresSynkendePåAvsluttetDato(){
        Fagsak fagsak = FagsakBuilder.nyFagsak().build();
        LocalDateTime now = LocalDateTime.now();
        Behandling nyBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(now).build();
        Behandling gammelBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(now.minusDays(1)).build();

        FagsakRevurdering.BehandlingAvsluttetDatoComparator behandlingAvsluttetDatoComparator = new FagsakRevurdering.BehandlingAvsluttetDatoComparator();

        List<Behandling> behandlinger = asList(nyBehandling, gammelBehandling);
        List<Behandling> sorterteBehandlinger = behandlinger.stream().sorted(behandlingAvsluttetDatoComparator).collect(Collectors.toList());

        assertThat(sorterteBehandlinger.get(0).getAvsluttetDato()).isEqualTo(now);
    }

    @Test
    public void behandlingerSkalSorteresSynkendePåOpprettetDatoNårAvsluttetDatoErNull(){
        Fagsak fagsak = FagsakBuilder.nyFagsak().build();
        LocalDateTime now = LocalDateTime.now();
        Behandling nyBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(null).medOpprettetDato(now).build();
        Behandling gammelBehandling = Behandling.forFørstegangssøknad(fagsak).medAvsluttetDato(now).medOpprettetDato(now.minusDays(1)).build();

        FagsakRevurdering.BehandlingAvsluttetDatoComparator behandlingAvsluttetDatoComparator = new FagsakRevurdering.BehandlingAvsluttetDatoComparator();

        List<Behandling> behandlinger = asList(nyBehandling, gammelBehandling);
        List<Behandling> sorterteBehandlinger = behandlinger.stream().sorted(behandlingAvsluttetDatoComparator).collect(Collectors.toList());

        assertThat(sorterteBehandlinger.get(0).getAvsluttetDato()).isNull();
        assertThat(sorterteBehandlinger.get(0).getOpprettetDato()).isEqualTo(now);
    }
}
