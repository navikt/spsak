package no.nav.foreldrepenger.behandlingslager.aktør;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.geografisk.PoststedKodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.geografisk.PoststedKodeverkRepositoryImpl;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class AdresseTypeKodeverkTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private PoststedKodeverkRepository repo = new PoststedKodeverkRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_verifisere_at_alle_kjente_adresse_typer_finnes_i_repo() {
        for (AdresseType adresseType : AdresseType.kjentePostadressetyper) {
            Optional<AdresseType> lestFraRepo = repo.finnAdresseType(adresseType.getKode());
            assertThat(lestFraRepo).isPresent();
            assertThat(lestFraRepo.get()).isEqualTo(adresseType);
        }
    }
}
