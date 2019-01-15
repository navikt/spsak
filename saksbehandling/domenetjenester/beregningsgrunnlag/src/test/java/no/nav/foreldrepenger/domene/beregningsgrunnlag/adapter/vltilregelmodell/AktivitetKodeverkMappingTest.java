package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.vltilregelmodell;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening.OpptjeningAktivitetType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class AktivitetKodeverkMappingTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository repo = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Test
    public void skal_verifisere_at_beregningsreglene_kjenner_alle_opptjeningsaktiviteter_i_kodeverk() {
        for (OpptjeningAktivitetType kode : repo.hentAlle(OpptjeningAktivitetType.class)) {
            if (!OpptjeningAktivitetType.UDEFINERT.equals(kode)) {
                MapBeregningsgrunnlagFraVLTilRegel.mapTilRegelmodell(kode);
            }
        }
    }
}
