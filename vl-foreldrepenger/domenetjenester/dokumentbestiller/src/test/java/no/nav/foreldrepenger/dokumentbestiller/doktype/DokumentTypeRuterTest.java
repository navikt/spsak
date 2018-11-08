package no.nav.foreldrepenger.dokumentbestiller.doktype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentData;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.vedtak.exception.TekniskException;

public class DokumentTypeRuterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DokumentMalType ukjentDokumentMalType;
    @Mock
    private DokumentMalType positivtVedtakDok;
    @Mock
    private DokumentData dokumentData;
    @Mock
    private KodeverkRepository kodeverkRepository;

    @Before
    public void setup() {
        when(ukjentDokumentMalType.getKode()).thenReturn("012345");
    }

    @Test
    public void testVedtaksbrev() throws InstantiationException, IllegalAccessException {
        when(positivtVedtakDok.getKode()).thenReturn(DokumentMalType.POSITIVT_VEDTAK_DOK);
        when(dokumentData.getDokumentMalType()).thenReturn(positivtVedtakDok);
        DokumentTypeMapper mapper = DokumentTypeRuter.dokumentTypeMapper(dokumentData, kodeverkRepository);
        assertThat(mapper).isNotNull();
    }

    @Test (expected=TekniskException.class)
    public void testUkjentDokument() throws InstantiationException, IllegalAccessException {
        when(dokumentData.getDokumentMalType()).thenReturn(ukjentDokumentMalType);
        DokumentTypeRuter.dokumentTypeMapper(dokumentData, kodeverkRepository);
    }

}
