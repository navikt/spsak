package no.nav.foreldrepenger.domene.dokumentarkiv.journal.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.sykepenger.spsak.dbstoette.UnittestRepositoryRule;

public class HentDokumentTypeTest {

    @Rule
    public UnittestRepositoryRule repositoryRule = new UnittestRepositoryRule();

    private KodeverkRepository kodeverkRepo = new KodeverkRepositoryImpl(repositoryRule.getEntityManager());

    @Test
    public void skal_klare_å_hente_innkommende_dokument_type_som_dokument_type_id() throws Exception {
        String eksternKode = "I000067";
        DokumentTypeId type = HentDokumentType.slåOppInngåendeDokumentType(kodeverkRepo, eksternKode);

        assertThat(type.getKode()).isEqualTo(DokumentTypeId.INNTEKTSMELDING.getKode());
    }

    @Test
    public void skal_ikke_klare_å_hente_dokument_type_som_dokument_type_id() throws Exception {
        String eksternKode = "000049";

        DokumentTypeId type = HentDokumentType.slåOppInngåendeDokumentType(kodeverkRepo, eksternKode);

        assertThat(type.getKode()).isEqualTo(DokumentTypeId.UDEFINERT.getKode());
    }
}
