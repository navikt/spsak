package no.nav.foreldrepenger.dokumentbestiller.mal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.LovhjemmelJsonHjelper;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.DokumentMalFelles;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.foreldrepenger.integrasjon.dokument.innvilget.foreldrepenger.KonsekvensForYtelseKode;

public class DokumentMalFellesTest {

    private static final LocalDate TERMINDATO = LocalDate.now();
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private Behandling behandling, originalBehandling;
    @Mock
    private Behandlingsresultat behandlingsresultat;
    @Mock
    private Avslagsårsak avslagsårsak;

    @Test
    public void verifiserBehandlingsresultatOgÅrsak() {

        // Arrange 1

        // Act 1
        try {
            DokumentMalFelles.verifiserBehandlingsresultat(behandling);
            fail("Forventet exception");
        } catch (Exception e) {
        }

        // Arrange 2
        when(behandling.getBehandlingsresultat()).thenReturn(behandlingsresultat);

        // Act 2
        DokumentMalFelles.verifiserBehandlingsresultat(behandling);
        try {
            DokumentMalFelles.verifiserAvslagsårsak(behandling);
            fail("Forventet exception");
        } catch (Exception e) {
        }

        // Arrange 3
        when(behandlingsresultat.getAvslagsårsak()).thenReturn(avslagsårsak);

        // Act 3
        try {
            DokumentMalFelles.verifiserAvslagsårsak(behandling);
            fail("Forventet exception");
        } catch (Exception e) {
        }

        // Arrange 4
        when(behandlingsresultat.isBehandlingsresultatAvslåttOrOpphørt()).thenReturn(true);

        // Act 4
        DokumentMalFelles.verifiserAvslagsårsak(behandling);
    }

    @Test
    public void finnTerminDato() {
        // Arrange 1
        FamilieHendelseGrunnlagBuilder aggregatBuilder = FamilieHendelseGrunnlagBuilder.oppdatere(Optional.empty());
        FamilieHendelseBuilder hendelseBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        aggregatBuilder.medSøknadVersjon(hendelseBuilder);
        // Act 1
        final FamilieHendelseGrunnlag aggregat = aggregatBuilder.build();
        Optional<LocalDate> termindatoOpt = DokumentMalFelles.finnTermindato(aggregat);
        // Assert 1
        assertThat(termindatoOpt).isNotPresent();

        // Arrange 2
        aggregatBuilder = FamilieHendelseGrunnlagBuilder.oppdatere(Optional.of(aggregat));
        hendelseBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.BEKREFTET);
        final FamilieHendelseBuilder.TerminbekreftelseBuilder terminbekreftelseBuilder = hendelseBuilder.getTerminbekreftelseBuilder()
            .medNavnPå("asdf").medTermindato(TERMINDATO).medUtstedtDato(LocalDate.now().minusDays(10));
        FamilieHendelseBuilder familieHendelse = hendelseBuilder
            .medTerminbekreftelse(terminbekreftelseBuilder);
        aggregatBuilder.medBekreftetVersjon(familieHendelse);
        termindatoOpt = DokumentMalFelles.finnTermindato(aggregatBuilder.build());
        // Assert 2
        assertThat(termindatoOpt).hasValueSatisfying(termindato ->
            assertThat(termindato).isEqualTo(TERMINDATO)
        );
    }

    @Test
    public void formaterLovhjemlerTest() throws IOException {
        String testCase1 = "{\"fagsakYtelseType\": [{\"FP\": [{\"kategori\": \"FP_VK_34\", \"lovreferanse\": \"21-3,21-7\"}]}]}";
        String uformatertLovhjemmel = LovhjemmelJsonHjelper.findLovhjemmelIJson(FagsakYtelseType.FORELDREPENGER, testCase1, "", "");
        assertLovformatering(uformatertLovhjemmel, "§§ 21-3 og 21-7");

        String testCase2 = "{\"fagsakYtelseType\": [{\"ES\": [{\"kategori\": \"FP_VK4\", \"lovreferanse\": \"§ 14-17 1. ledd\"}, {\"kategori\": \"FP_VK5\", \"lovreferanse\": \"§ 14-17 3. ledd\"}, {\"kategori\": \"FP_VK33\", \"lovreferanse\": \"§ 14-17 2. ledd\"}, {\"kategori\": \"FP_VK33\", \"lovreferanse\": \"§ 14-17 4. ledd\"}]}, {\"FP\": [{\"kategori\": \"FP_VK_8\", \"lovreferanse\": \"14-5\"}]}]}";
        uformatertLovhjemmel = LovhjemmelJsonHjelper.findLovhjemmelIJson(FagsakYtelseType.FORELDREPENGER, testCase2, "", "");
        assertLovformatering(uformatertLovhjemmel, "§ 14-5");

        String testCase3 = "{\"fagsakYtelseType\": [{\"ES\": [{\"kategori\": \"FP_VK4\", \"lovreferanse\": \"14-17\"}, {\"kategori\": \"FP_VK5\", \"lovreferanse\": \"14-17\"}, {\"kategori\": \"FP_VK33\", \"lovreferanse\": \"14-17\"}]}, {\"FP\": {\"lovreferanse\": \"14-5\"}}]}";
        uformatertLovhjemmel = LovhjemmelJsonHjelper.findLovhjemmelIJson(FagsakYtelseType.FORELDREPENGER, testCase3, "", "");
        assertLovformatering(uformatertLovhjemmel, "§ 14-5");

        String testCaseBarnOver15 = "{\"fagsakYtelseType\": [{\"FP\": [{\"kategori\": \"FP_VK_16_1\", \"lovreferanse\": \"14-5\"}]}]}";
        uformatertLovhjemmel = "\n" + LovhjemmelJsonHjelper.findLovhjemmelIJson(FagsakYtelseType.FORELDREPENGER, testCaseBarnOver15, "", "");
        assertLovformatering(uformatertLovhjemmel, "§ 14-5");
    }

    @Test
    public void formaterLovhjemlerBeregningEnkelTest() throws IOException {
        String lovhjemmelFraBeregning = "folketrygdloven § 14-7";
        assertLovformateringBeregning(lovhjemmelFraBeregning, "", false, "§ 14-7");
    }

    @Test
    public void formaterLovhjemlerInnvilgetRevurderingBeregningTest() throws IOException {
        String lovhjemmelFraBeregning = "folketrygdloven § 14-7";
        assertLovformateringBeregning(lovhjemmelFraBeregning, "", true, "§ 14-7 og forvaltningsloven § 35");
    }

    @Test
    public void formaterLovhjemlerRevurderingEndringBeregningTest() throws IOException {
        String lovhjemmelFraBeregning = "folketrygdloven § 14-7";
        assertLovformateringBeregning(lovhjemmelFraBeregning, KonsekvensForYtelseKode.ENDRING_I_BEREGNING.value(), false, "§ 14-7 og forvaltningsloven § 35");
    }

    @Test
    public void formaterLovhjemlerAvslagEnkel() throws IOException {
        DokumentTypeMedPerioderDto dto = new DokumentTypeMedPerioderDto(123l);
        dto.getDokumentBehandlingsresultatDto().leggTilLovhjemmelForAvslag("14-16");
        assertLovFormateringAvslag(dto, "§ 14-16");
    }

    @Test
    public void formaterLovhjemlerAvslagPerioder() throws IOException {
        DokumentTypeMedPerioderDto dto = new DokumentTypeMedPerioderDto(123l);
        dto.leggTilLovhjemmelVurdering("14-16");
        dto.leggTilLovhjemmelVurdering("14-18");
        dto.leggTilLovhjemmelVurdering("14-17");
        assertLovFormateringAvslag(dto, "§§ 14-16, 14-17 og 14-18");
    }

    @Test
    public void formaterLovhjemlerAvslagPerioderMedKonsekvens() throws IOException {
        DokumentTypeMedPerioderDto dto = new DokumentTypeMedPerioderDto(123l);
        dto.getDokumentBehandlingsresultatDto().setKonsekvensForYtelse(KonsekvensForYtelseKode.ENDRING_I_BEREGNING.value());
        dto.leggTilLovhjemmelVurdering("14-16");
        dto.leggTilLovhjemmelVurdering("14-18");
        dto.leggTilLovhjemmelVurdering("14-17");
        assertLovFormateringAvslag(dto, "§§ 14-16, 14-17, 14-18 og forvaltningsloven § 35");
    }

    @Test
    public void skal_formateres_lovhjemler_riktig_selvom_listen_innholder_empty_strings() {
        // tom streng i starten
        String hjemmelListe = "\n21-3\n21-7";
        assertLovformatering(hjemmelListe, "§§ 21-3 og 21-7");

        // tom streng til slutt
        hjemmelListe = "21-3\n ";
        assertLovformatering(hjemmelListe, "§ 21-3");

        // tom streng i starten, midten og til slutt
        hjemmelListe = "\n21-3\n" + " " + "\n21-7\n  \n14-5" + "\n   ";
        assertLovformatering(hjemmelListe, "§§ 21-3, 21-7 og 14-5");
    }

    @Test
    public void skal_formatere_lovhjemmel_uttak_med_forvaltningsloven() {
        Set<String> hjemmelSet = new TreeSet<>();
        hjemmelSet.add("14-16");
        hjemmelSet.add("14-18");
        String resultat = DokumentMalFelles.formaterLovhjemlerUttak(hjemmelSet, "", true);
        assertThat(resultat).isEqualTo("§§ 14-16, 14-18 og forvaltningsloven § 35");
    }

    @Test
    public void skal_formatere_lovhjemmel_uttak() {
        Set<String> hjemmelSet = new TreeSet<>();
        hjemmelSet.add("14-16");
        hjemmelSet.add("14-18");
        hjemmelSet.add("14-17");
        String resultat = DokumentMalFelles.formaterLovhjemlerUttak(hjemmelSet, "", false);
        assertThat(resultat).isEqualTo("§§ 14-16, 14-17 og 14-18");
    }

    @Test
    public void skal_formatere_enkel_lovhjemmel() {
        Set<String> hjemmelSet = new TreeSet<>();
        hjemmelSet.add("14-16");
        String resultat = DokumentMalFelles.formaterLovhjemlerUttak(hjemmelSet, "", false);
        assertThat(resultat).isEqualTo("§ 14-16");
    }

    private void assertLovformatering(String input, String forventetOutput) {
        String lovhjemler = DokumentMalFelles.formaterLovhjemler(input);
        assertThat(lovhjemler).isEqualTo(forventetOutput);
    }

    private void assertLovformateringBeregning(String input, String konsekvensForYtelse, boolean innvilgetRevurdering, String forventetOutput) {
        String lovhjemler = DokumentMalFelles.formaterLovhjemlerForBeregning(input, konsekvensForYtelse, innvilgetRevurdering);
        assertThat(lovhjemler).isEqualTo(forventetOutput);
    }

    private void assertLovFormateringAvslag(DokumentTypeMedPerioderDto dto, String forventetOutput) {
        String lovhjemler = DokumentMalFelles.formaterLovhjemlerForAvslag(dto);
        assertThat(lovhjemler).isEqualTo(forventetOutput);
    }

}
