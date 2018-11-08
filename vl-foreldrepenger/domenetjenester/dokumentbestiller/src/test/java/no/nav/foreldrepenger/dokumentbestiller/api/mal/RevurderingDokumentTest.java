package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametereImpl;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.AdvarselKodeKode;
import no.nav.foreldrepenger.integrasjon.dokument.revurdering.YtelseTypeKode;
import org.junit.Before;
import org.junit.Test;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

public class RevurderingDokumentTest {

    private BrevParametereImpl brevParametere;
    private DokumentTypeDto dto;

    @Before
    public void setup() {
        brevParametere = new BrevParametereImpl(1, 2, Period.ofDays(7), Period.ofDays(7));
        dto = new DokumentTypeDto(123l);
        dto.setYtelsesTypeKode(YtelseTypeKode.ES.value());
        dto.setAntallBarn(1);
    }

    @Test
    public void skal_sette_advarselskode_hvis_fritekst_og_finnes_ikke() {
        RevurderingDokument revurderingDokument = new RevurderingDokument(brevParametere, "fritekst", null);
        assertThat(revurderingDokument.getFlettefelter(dto).stream()
            .filter(dto -> dto.getFeltnavn().equals(Flettefelt.ADVARSEL_KODE))
            .findFirst().orElse(null).getFeltverdi())
            .isEqualTo("ANNET");
    }

    @Test
    public void skal_ikke_overstyre_advarselskode() {
        RevurderingDokument revurderingDokument = new RevurderingDokument(brevParametere, "fritekst", AdvarselKodeKode.AKTIVITET.value());
        assertThat(revurderingDokument.getFlettefelter(dto).stream()
            .filter(dto -> dto.getFeltnavn().equals(Flettefelt.ADVARSEL_KODE))
            .findFirst().orElse(null).getFeltverdi())
            .isEqualTo("AKTIVITET");
    }

    @Test
    public void skal_ikke_overstyre_uten_fritekst() {
        RevurderingDokument revurderingDokument = new RevurderingDokument(brevParametere, null, null);
        assertThat(revurderingDokument.getFlettefelter(dto).stream()
            .filter(dto -> dto.getFeltnavn().equals(Flettefelt.ADVARSEL_KODE))
            .findFirst().orElse(null))
            .isNull();
    }
}
