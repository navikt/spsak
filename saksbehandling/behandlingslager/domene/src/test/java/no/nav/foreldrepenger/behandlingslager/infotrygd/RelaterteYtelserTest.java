package no.nav.foreldrepenger.behandlingslager.infotrygd;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.TemaUnderkategori;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseSakstype;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTilstand;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseType;
import no.nav.vedtak.felles.testutilities.db.Repository;

public class RelaterteYtelserTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private Repository repository = repoRule.getRepository();

    @Test
    public void relatertYtelseTemaFinnesTest() {
        List<RelatertYtelseTema> resultat = repository.hentAlle(RelatertYtelseTema.class);

        assertThat(resultat).hasAtLeastOneElementOfType(RelatertYtelseTema.class);
    }
    @Test
    public void temaUnderkategoriFinnesTest() {
        List<TemaUnderkategori> resultat = repository.hentAlle(TemaUnderkategori.class);

        assertThat(resultat).hasAtLeastOneElementOfType(TemaUnderkategori.class);
    }

    @Test
    public void relatertYtelseSakstypeFinnesTest() {
        List<RelatertYtelseSakstype> resultat = repository.hentAlle(RelatertYtelseSakstype.class);

        assertThat(resultat).hasAtLeastOneElementOfType(RelatertYtelseSakstype.class);
    }

    @Test
    public void relatertYtelseSakstatusFinnesTest() {
        List<RelatertYtelseStatus> resultat = repository.hentAlle(RelatertYtelseStatus.class);

        assertThat(resultat).hasAtLeastOneElementOfType(RelatertYtelseStatus.class);
    }

    @Test
    public void relatertYtelseResultatFinnesTest() {
        List<RelatertYtelseResultat> resultat = repository.hentAlle(RelatertYtelseResultat.class);

        assertThat(resultat).hasAtLeastOneElementOfType(RelatertYtelseResultat.class);
    }

    @Test
    public void relatertYtelseTypeFinnesTest() {
        List<RelatertYtelseType> resultat = repository.hentAlle(RelatertYtelseType.class);

        assertThat(resultat).hasAtLeastOneElementOfType(RelatertYtelseType.class);
    }

    @Test
    public void relatertYtelseTilstandFinnesTest() {
        List<RelatertYtelseTilstand> resultat = repository.hentAlle(RelatertYtelseTilstand.class);

        assertThat(resultat).hasAtLeastOneElementOfType(RelatertYtelseTilstand.class);
    }
}
