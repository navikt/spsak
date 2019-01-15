package no.nav.foreldrepenger.behandlingslager.behandling.virksomhet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class VirksomhetRepositoryImplTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private VirksomhetRepository repository = new VirksomhetRepositoryImpl(repositoryRule.getEntityManager());

    @Test
    public void skal_lagre_virksomheten() {
        VirksomhetEntitet.Builder builder = new VirksomhetEntitet.Builder();
        final String orgnr = "123123123";
        final Virksomhet virksomheten = builder.medOrgnr(orgnr).medNavn("Virksomheten").medOppstart(LocalDate.now()).oppdatertOpplysningerNå().build();
        repository.lagre(virksomheten);

        Optional<Virksomhet> hent = repository.hentForEditering(orgnr);
        assertThat(hent).isPresent();

        Virksomhet virksomhet = hent.get();
        final LocalDateTime opplysningerOppdatertTidspunkt = ((VirksomhetEntitet) virksomhet).getOpplysningerOppdatertTidspunkt();
        assertThat(((BaseEntitet) virksomhet).getOpprettetTidspunkt()).isNotNull();
        assertThat(opplysningerOppdatertTidspunkt).isNotNull();

        builder = new VirksomhetEntitet.Builder(virksomhet);
        builder.oppdatertOpplysningerNå();

        repository.lagre(builder.build());

        hent = repository.hent(orgnr);
        assertThat(hent).isPresent();

        virksomhet = hent.get();
        assertThat(((BaseEntitet) virksomhet).getOpprettetTidspunkt()).isNotNull();
    }
}
