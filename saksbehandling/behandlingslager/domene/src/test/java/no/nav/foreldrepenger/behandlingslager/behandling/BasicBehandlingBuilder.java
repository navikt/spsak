package no.nav.foreldrepenger.behandlingslager.behandling;

import static java.time.Month.JANUARY;
import static no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn.KVINNE;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryImpl;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

/** Enkel builder for å lage en enkel behandling for internt bruk her.*/
public class BasicBehandlingBuilder {

    private EntityManager em;
    private final BehandlingRepository behandlingRepository;

    public BasicBehandlingBuilder(EntityManager em) {
        this.em = em;
        behandlingRepository = new BehandlingRepositoryImpl(em);
    }

    public Behandling opprettOgLagreFørstegangssøknad(FagsakYtelseType ytelse) {
        Fagsak fagsak = opprettFagsak(ytelse);
        return opprettOgLagreFørstegangssøknad(fagsak);
    }

    public Behandling opprettOgLagreFørstegangssøknad(Fagsak fagsak) {
        final Behandling.Builder builder = Behandling.forFørstegangssøknad(fagsak);
        Behandling behandling = builder.build();

        lagreBehandling(behandling);

        em.flush();
        return behandling;
    }

    private void lagreBehandling(Behandling behandling) {
        BehandlingLås lås = taSkriveLås(behandling);
        behandlingRepository.lagre(behandling, lås);
    }

    private BehandlingLås taSkriveLås(Behandling behandling) {
        return behandlingRepository.taSkriveLås(behandling);
    }

    public Fagsak opprettFagsak(FagsakYtelseType ytelse) {
        NavBruker bruker = NavBruker.opprettNy(
            new Personinfo.Builder()
                .medAktørId(new AktørId("200"))
                .medPersonIdent(new PersonIdent("12345678901"))
                .medNavn("Kari Nordmann")
                .medFødselsdato(LocalDate.of(1990, JANUARY, 1))
                .medForetrukketSpråk(Språkkode.nb)
                .medNavBrukerKjønn(KVINNE)
                .build());

        em.persist(bruker);
        
        // Opprett fagsak
        Fagsak fagsak = Fagsak.opprettNy(bruker, new Saksnummer("1000"));
        em.persist(fagsak);
        em.flush();
        return fagsak;
    }

    public VilkårResultat leggTilTomtVilkårResultat(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = Behandlingsresultat.opprettFor(behandling);
        BehandlingLås lås = taSkriveLås(behandling);
        behandlingRepository.lagre(behandlingsresultat, lås);
        VilkårResultat vilkårResultat = VilkårResultat.builder().buildFor(behandlingsresultat);
        behandlingRepository.lagre(vilkårResultat, lås);
        lagreBehandling(behandling);
        return vilkårResultat;
    }
}
