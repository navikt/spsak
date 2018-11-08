package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.InnsynResultatType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;

public class InnsynskravSvarDokumentTest {

    @Test
    public void skalSetteInnsynResultatTypeFletteFelt() {
        InnsynResultatType type = InnsynResultatType.DELVIS_INNVILGET;
        InnsynskravSvarDokument dokument = builder()
            .medInnsynType(type)
            .build();

        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(emptyDto()), Flettefelt.INNSYN_RESULTAT_TYPE))
            .isEqualTo(type.getKode());
    }

    @Test
    public void skalSetteYtelseTypeFletteFelt() {
        String type = "ytelse2";
        InnsynskravSvarDokument dokument = builder().build();

        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(dtoWithYtelseType(type)), Flettefelt.YTELSE_TYPE))
            .isEqualTo(type);
    }

    @Test
    public void skalSetteFritekstFletteFelt() {
        String fritekst = "dette er fritekst";
        InnsynskravSvarDokument dokument = builder().withFritekst(fritekst).build();

        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(emptyDto()), Flettefelt.FRITEKST))
            .isEqualTo(fritekst);
    }

    @Test
    public void skalSetteKlageFristFletteFelt() {
        Integer klageFristUker = new Random().nextInt(5);
        InnsynskravSvarDokument dokument = builder().withKlageFristUker(klageFristUker).build();

        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(emptyDto()), Flettefelt.KLAGE_FRIST_UKER))
            .isEqualTo(String.valueOf(klageFristUker));
    }

    private DokumentTypeDto emptyDto() {
        return mock(DokumentTypeDto.class);
    }

    private InnsynskravSvarDokumentBuilder builder() {
        return new InnsynskravSvarDokumentBuilder();
    }

    private DokumentTypeDto dtoWithYtelseType(String type) {
        DokumentTypeDto mock = mock(DokumentTypeDto.class);
        when(mock.getYtelsesTypeKode()).thenReturn(type);
        return mock;
    }

    private String finnFlettefeltVerdi(List<Flettefelt> flettefelter, String feltnavn) {
        return flettefelter
            .stream()
            .filter(flettefelt -> Objects.equals(flettefelt.getFeltnavn(), feltnavn))
            .findFirst()
            .map(Flettefelt::getFeltverdi)
            .orElse(null);
    }

    private class InnsynskravSvarDokumentBuilder {
        private InnsynResultatType innsynType = InnsynResultatType.DELVIS_INNVILGET;
        private String fritekst = "fritekst";
        private Integer klageFristUker = 1;
        private Integer klageFristUkerInnsyn = 1;

        public InnsynskravSvarDokument build() {
            return new InnsynskravSvarDokument(new BrevParametere() {
                @Override
                public Integer getKlagefristUker() {
                    return klageFristUker;
                }

                @Override
                public Integer getKlagefristUkerInnsyn() {
                    return klageFristUkerInnsyn;
                }

                @Override
                public Integer getSvarfristDager() {
                    return 1;
                }

                @Override
                public Integer getSøkAntallUker() {
                    return 2;
                }

            }, fritekst, innsynType);
        }

        InnsynskravSvarDokumentBuilder medInnsynType(InnsynResultatType type) {
            this.innsynType = type;
            return this;
        }

        InnsynskravSvarDokumentBuilder withFritekst(String fritekst) {
            this.fritekst = fritekst;
            return this;
        }

        InnsynskravSvarDokumentBuilder withKlageFristUker(Integer klageFristUker) {
            this.klageFristUkerInnsyn = klageFristUker;
            return this;
        }
    }
}
