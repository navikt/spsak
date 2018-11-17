package no.nav.foreldrepenger.behandlingslager.behandling.totrinn;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.VurderÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepositoryProviderImpl;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRepository;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

public class TotrinnRepositoryImplTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager entityManager = repoRule.getEntityManager();
    private final BehandlingRepositoryProvider repositoryProvider = new BehandlingRepositoryProviderImpl(repoRule.getEntityManager());
    private final TotrinnRepository totrinnRepository = new TotrinnRepositoryImpl(entityManager);
    private FagsakRepository fagsakRepository;
    private BehandlingRepository behandlingRepository;

    @Before
    public void setup() {
        fagsakRepository = repositoryProvider.getFagsakRepository();
        behandlingRepository = repositoryProvider.getBehandlingRepository();
    }

    @Test
    public void skal_finne_ett_inaktivt_totrinnsgrunnlag_og_ett_aktivt_totrinnsgrunnlag() {

        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(lagPerson()));
        fagsakRepository.opprettNy(fagsak);

        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        behandlingRepository.lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        Totrinnresultatgrunnlag gammeltTotrinnresultatgrunnlag = new Totrinnresultatgrunnlag(behandling,
            null, null, null);

        Totrinnresultatgrunnlag nyttTotrinnresultatgrunnlag = new Totrinnresultatgrunnlag(behandling,
            null, null, null);

        totrinnRepository.lagreOgFlush(behandling, gammeltTotrinnresultatgrunnlag);
        totrinnRepository.lagreOgFlush(behandling, nyttTotrinnresultatgrunnlag);

        // Hent ut aktiv totrinnsgrunnlag
        Optional<Totrinnresultatgrunnlag> optionalNyttTotrinnresultatgrunnlag = totrinnRepository.hentTotrinngrunnlag(behandling);

        // Hent ut inaktive totrinnsgrunnlag
        TypedQuery<Totrinnresultatgrunnlag> query = entityManager.createQuery(
            "SELECT trg FROM Totrinnresultatgrunnlag trg WHERE trg.behandling.id = :behandling_id AND trg.aktiv = 'N'", //$NON-NLS-1$
            Totrinnresultatgrunnlag.class);
        query.setParameter("behandling_id", behandling.getId()); //$NON-NLS-1$
        List<Totrinnresultatgrunnlag> inaktive = query.getResultList();

        assertThat(inaktive.size()).isEqualTo(1);
        assertThat(inaktive.get(0)).isEqualToComparingFieldByField(gammeltTotrinnresultatgrunnlag);
        assertThat(optionalNyttTotrinnresultatgrunnlag).isPresent();
        assertThat(optionalNyttTotrinnresultatgrunnlag.get().getId()).isNotEqualTo(gammeltTotrinnresultatgrunnlag.getId());
        assertThat(optionalNyttTotrinnresultatgrunnlag.get().isAktiv()).isTrue();

    }

    @Test
    public void skal_finne_flere_inaktive_totrinnsvurderinger_og_flere_aktive_totrinnsvurdering() {

        Fagsak fagsak = Fagsak.opprettNy(FagsakYtelseType.FORELDREPENGER, NavBruker.opprettNy(lagPerson()));
        fagsakRepository.opprettNy(fagsak);

        Behandling behandling = Behandling.forFørstegangssøknad(fagsak).build();
        behandlingRepository.lagre(behandling, repositoryProvider.getBehandlingRepository().taSkriveLås(behandling));

        // Opprett vurderinger som skal være inaktive
        Totrinnsvurdering inaktivTotrinnsvurdering1 = lagTotrinnsvurdering(behandling,
            AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, true, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering inaktivTotrinnsvurdering2 = lagTotrinnsvurdering(behandling,
            AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, true, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering inaktivTotrinnsvurdering3 = lagTotrinnsvurdering(behandling,
            AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, true, "", VurderÅrsak.FEIL_FAKTA);

        List<Totrinnsvurdering> inaktivTotrinnsvurderingList = new ArrayList<>();
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering1);
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering2);
        inaktivTotrinnsvurderingList.add(inaktivTotrinnsvurdering3);
        totrinnRepository.lagreOgFlush(behandling, inaktivTotrinnsvurderingList);

        // Opprett vurderinger som skal være aktive
        Totrinnsvurdering aktivTotrinnsvurdering1 = lagTotrinnsvurdering(behandling,
            AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, false, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering aktivTotrinnsvurdering2 = lagTotrinnsvurdering(behandling,
            AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, false, "", VurderÅrsak.FEIL_FAKTA);
        Totrinnsvurdering aktivTotrinnsvurdering3 = lagTotrinnsvurdering(behandling,
            AksjonspunktDefinisjon.SJEKK_MANGLENDE_FØDSEL, false, "", VurderÅrsak.FEIL_FAKTA);

        List<Totrinnsvurdering> aktivTotrinnsvurderingList = new ArrayList<>();
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering1);
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering2);
        aktivTotrinnsvurderingList.add(aktivTotrinnsvurdering3);
        totrinnRepository.lagreOgFlush(behandling, aktivTotrinnsvurderingList);

        // Hent aktive vurderinger etter flush
        Collection<Totrinnsvurdering> repoAktiveTotrinnsvurderinger = totrinnRepository.hentTotrinnaksjonspunktvurderinger(behandling);

        // Hent inaktive vurderinger etter flush
        TypedQuery<Totrinnsvurdering> query = entityManager.createQuery(
            "SELECT tav FROM Totrinnsvurdering tav WHERE tav.behandling.id = :behandling_id AND tav.aktiv = 'N'", //$NON-NLS-1$
            Totrinnsvurdering.class);
        query.setParameter("behandling_id", behandling.getId()); //$NON-NLS-1$
        List<Totrinnsvurdering> repoInaktiveTotrinnsvurderinger = query.getResultList();

        // Sjekk lagrede aktive vurderinger
        assertThat(repoAktiveTotrinnsvurderinger.size()).isEqualTo(3);
        repoAktiveTotrinnsvurderinger.forEach(totrinnsvurdering -> assertThat(totrinnsvurdering.isAktiv()).isTrue());

        // Sjekk lagrede inaktive vurderinger
        assertThat(repoInaktiveTotrinnsvurderinger.size()).isEqualTo(3);
        repoInaktiveTotrinnsvurderinger.forEach(totrinnsvurdering -> assertThat(totrinnsvurdering.isAktiv()).isFalse());

    }

    private Totrinnsvurdering lagTotrinnsvurdering(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                   boolean godkjent, String begrunnelse, VurderÅrsak vurderÅrsak){
        return new Totrinnsvurdering.Builder(behandling, aksjonspunktDefinisjon)
            .medGodkjent(godkjent)
            .medBegrunnelse(begrunnelse)
            .medVurderÅrsak(vurderÅrsak)
            .build();
    }

    private Personinfo lagPerson() {
        return new Personinfo.Builder()
            .medNavn("Navn navnesen")
            .medAktørId(new AktørId("123"))
            .medFødselsdato(LocalDate.now().minusYears(20))
            .medLandkode(Landkoder.NOR)
            .medNavBrukerKjønn(NavBrukerKjønn.KVINNE)
            .medPersonIdent(new PersonIdent("12345678901"))
            .medForetrukketSpråk(Språkkode.nb)
            .build();
    }

}
