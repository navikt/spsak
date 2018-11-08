package no.nav.foreldrepenger.dokumentbestiller.doktype;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentTypeData;
import no.nav.vedtak.exception.TekniskException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class DokumentTypeFellesTest {
    private static final String FELT_SOM_FINNES = "FeltSomFinnes";
    private static final String FELT_SOM_IKKE_FINNES = "FeltSomIkkeFinnes";
    private List<DokumentTypeData> dokumentTypeDataListe = new ArrayList<>();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private DokumentTypeData finnes;

    @Before
    public void setup() {
        when(finnes.getDoksysId()).thenReturn(FELT_SOM_FINNES);
        when(finnes.getVerdi()).thenReturn("abc");
    }

    @Test
    public void testVerdiFinnes() {
        dokumentTypeDataListe.add(finnes);
        String verdi = DokumentTypeFelles.finnVerdiAv(FELT_SOM_FINNES, dokumentTypeDataListe);
        assertThat(verdi).isNotNull();
    }

    @Test(expected = TekniskException.class)
    public void testVerdiFinnesIkke() {
        dokumentTypeDataListe.add(finnes);
        DokumentTypeFelles.finnVerdiAv(FELT_SOM_IKKE_FINNES, dokumentTypeDataListe);
    }
}