package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.util.Lists;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingType;
import no.nav.foreldrepenger.behandlingslager.dokumentbestiller.DokumentMalType;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.FeriePeriodeDto;
import no.nav.vedtak.exception.TekniskException;

public class InntektsmeldingForTidligDokumentTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void exception_hvis_ugyldig_behandlingstype() {

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString("FP-875840"));

        BehandlingType type = BehandlingType.KLAGE;  // bare Førstegangsbehandlign og Revurdering.
        
        @SuppressWarnings("unused")
        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .build();
    }

    @Test
    public void exception_hvis_ikke_dokument_type_med_perioder_dto() {
        BehandlingType type = BehandlingType.FØRSTEGANGSSØKNAD;
        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .build();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("DokumentTypeDto må være av " + DokumentTypeMedPerioderDto.class.getTypeName());

        dokument.getFlettefelter(emptyStandardDto());
    }

    @Test
    public void exception_hvis_obligatorisk_felt_arbeidsgiverNavn_mangler_verdi() {

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage(CoreMatchers.containsString("FP-220913"));

        BehandlingType type = BehandlingType.FØRSTEGANGSSØKNAD;  // bare Førstegangsbehandlign og Revurdering.
        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .build();

        dokument.getFlettefelter(emptyPerioderDto());
    }

    @Test
    public void skal_sette_behandligstype_til_førstegangsbehandling() {
        BehandlingType type = BehandlingType.FØRSTEGANGSSØKNAD;

        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .build();


        DokumentTypeMedPerioderDto dto = emptyPerioderDto();
        when(dto.getArbeidsgiversNavn()).thenReturn("STATOIL");

        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(dto), Flettefelt.BEHANDLINGSTYPE))
            .isEqualTo(type.getKode());
    }

    @Test
    public void skal_sette_behandligstype_til_revurderinng() {
        BehandlingType type = BehandlingType.REVURDERING;
        Integer sokAntallUkerFor = 4;

        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .medSokAntallUker(sokAntallUkerFor)
            .build();


        DokumentTypeMedPerioderDto dto = emptyPerioderDto();
        when(dto.getArbeidsgiversNavn()).thenReturn("STATOIL");
        when(dto.getMottattInntektsmelding()).thenReturn(null);

        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(dto), Flettefelt.BEHANDLINGSTYPE))
            .isEqualTo(type.getKode());
        assertThat(finnFlettefeltVerdi(dokument.getFlettefelter(dto), Flettefelt.SOK_ANTALL_UKER_FOR))
            .isEqualTo(String.valueOf(sokAntallUkerFor));
    }

    @Test
    public void skal_sette_arebidsgiverNavn() {
        BehandlingType type = BehandlingType.REVURDERING;
        Integer sokAntallUkerFor = 4;
        String arbeidsgiverNavn = "STATOIL";

        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .medSokAntallUker(sokAntallUkerFor)
            .build();


        DokumentTypeMedPerioderDto dto = emptyPerioderDto();
        when(dto.getArbeidsgiversNavn()).thenReturn(arbeidsgiverNavn);
        when(dto.getMottattInntektsmelding()).thenReturn(null);

        List<Flettefelt> flettefelter = dokument.getFlettefelter(dto);
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.BEHANDLINGSTYPE))
            .isEqualTo(type.getKode());
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.SOK_ANTALL_UKER_FOR))
            .isEqualTo(String.valueOf(sokAntallUkerFor));
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.ARBEIDSGIVER_NAVN))
            .isEqualTo(arbeidsgiverNavn);

        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.MOTTATT_DATO)).isNull();
    }

    @Test
    public void skal_sette_mottatDato() {
        BehandlingType type = BehandlingType.REVURDERING;
        Integer sokAntallUkerFor = 4;
        String arbeidsgiverNavn = "STATOIL";
        LocalDate mottatDato = LocalDate.now();

        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .medSokAntallUker(sokAntallUkerFor)
            .build();


        DokumentTypeMedPerioderDto dto = emptyPerioderDto();
        when(dto.getArbeidsgiversNavn()).thenReturn(arbeidsgiverNavn);
        when(dto.getMottattInntektsmelding()).thenReturn(mottatDato);

        List<Flettefelt> flettefelter = dokument.getFlettefelter(dto);
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.BEHANDLINGSTYPE))
            .isEqualTo(type.getKode());
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.SOK_ANTALL_UKER_FOR))
            .isEqualTo(String.valueOf(sokAntallUkerFor));
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.ARBEIDSGIVER_NAVN))
            .isEqualTo(arbeidsgiverNavn);

        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.MOTTATT_DATO)).isEqualTo(String.valueOf(mottatDato));
    }

    @Test
    public void skal_sette_ferie_perioder() {
        BehandlingType type = BehandlingType.REVURDERING;
        Integer sokAntallUkerFor = 4;
        String arbeidsgiverNavn = "STATOIL";
        LocalDate mottatDato = LocalDate.now();

        List<FeriePeriodeDto> feriePeriodeDtos = Lists.newArrayList();
        FeriePeriodeDto feriePeriode = new FeriePeriodeDto();
        String feriePeriodeFom = LocalDate.now().minusDays(5).toString();
        feriePeriode.setFeriePeriodeFom(feriePeriodeFom);
        String feriePeriodeTom = LocalDate.now().plusDays(5).toString();
        feriePeriode.setFeriePeriodeTom(feriePeriodeTom);
        feriePeriodeDtos.add(feriePeriode);

        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(type)
            .medSokAntallUker(sokAntallUkerFor)
            .build();


        DokumentTypeMedPerioderDto dto = emptyPerioderDto();
        when(dto.getArbeidsgiversNavn()).thenReturn(arbeidsgiverNavn);
        when(dto.getMottattInntektsmelding()).thenReturn(mottatDato);
        when(dto.getFeriePerioder()).thenReturn(feriePeriodeDtos);

        List<Flettefelt> flettefelter = dokument.getFlettefelter(dto);
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.BEHANDLINGSTYPE))
            .isEqualTo(type.getKode());
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.SOK_ANTALL_UKER_FOR))
            .isEqualTo(String.valueOf(sokAntallUkerFor));
        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.ARBEIDSGIVER_NAVN))
            .isEqualTo(arbeidsgiverNavn);

        assertThat(finnFlettefeltVerdi(flettefelter, Flettefelt.MOTTATT_DATO)).isEqualTo(String.valueOf(mottatDato));

        assertThat(flettefelter.size()).isEqualTo(5);
    }

    @Test
    public void skal_returnere_riktig_dokument_mal_type() {
        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .build();

        assertThat(dokument.getDokumentMalType()).isEqualTo(DokumentMalType.INNTEKTSMELDING_FOR_TIDLIG_DOK);
    }


    @Test
    public void skal_returnere_riktig_has_perioder_flag() {
        InntektsmeldingForTidligDokument dokument = builder()
            .medBehandlingType(BehandlingType.FØRSTEGANGSSØKNAD)
            .build();

        assertThat(dokument.harPerioder()).isEqualTo(true);
    }

    private DokumentTypeDto emptyStandardDto() {
        return mock(DokumentTypeDto.class);
    }

    private DokumentTypeMedPerioderDto emptyPerioderDto() {
        return mock(DokumentTypeMedPerioderDto.class);
    }

    private InntektsmeldingForTidligDokumentTest.InntektsmeldingForTidligDokumentBuilder builder() {
        return new InntektsmeldingForTidligDokumentTest.InntektsmeldingForTidligDokumentBuilder();
    }

    private String finnFlettefeltVerdi(List<Flettefelt> flettefelter, String feltnavn) {
        return flettefelter
            .stream()
            .filter(flettefelt -> Objects.equals(flettefelt.getFeltnavn(), feltnavn))
            .findFirst()
            .map(Flettefelt::getFeltverdi)
            .orElse(null);
    }

    static List<Flettefelt> finnListeMedVerdierAv(String feltnavn, List<Flettefelt> dokumentTypeDataListe) {
        return dokumentTypeDataListe.stream()
            .filter(dtd -> dtd.getFeltnavn().equalsIgnoreCase(feltnavn.split(":")[0]))
            .collect(Collectors.toList());
    }

    private class InntektsmeldingForTidligDokumentBuilder {
        private BehandlingType behandlingType = BehandlingType.FØRSTEGANGSSØKNAD;
        private Integer sokAntallUker = 1;

        public InntektsmeldingForTidligDokument build() {
            return new InntektsmeldingForTidligDokument(new BrevParametere() {
                @Override
                public Integer getKlagefristUker() {
                    return 1;
                }

                @Override
                public Integer getKlagefristUkerInnsyn() {
                    return 1;
                }

                @Override
                public Integer getSvarfristDager() {
                    return 1;
                }

                @Override
                public Integer getSøkAntallUker() {
                    return sokAntallUker;
                }

            }, behandlingType);
        }

        InntektsmeldingForTidligDokumentTest.InntektsmeldingForTidligDokumentBuilder medBehandlingType(BehandlingType type) {
            this.behandlingType = type;
            return this;
        }

        InntektsmeldingForTidligDokumentTest.InntektsmeldingForTidligDokumentBuilder medSokAntallUker(Integer sokAntallUker) {
            this.sokAntallUker = sokAntallUker;
            return this;
        }
    }

}
