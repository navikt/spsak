package no.nav.foreldrepenger.behandlingslager.behandling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelse;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.behandling.grunnlag.UidentifisertBarn;

public class AdopsjonTest {

    @Test
    public void skal_merge_fra_gammel_til_ny_adopsjon() {
        final LocalDate now = LocalDate.now();

        final FamilieHendelseGrunnlag hendelseAggregat = byggAggregat(now);
        final FamilieHendelse søknadVersjon = hendelseAggregat.getGjeldendeVersjon();

        assertThat(søknadVersjon.getAntallBarn()).isEqualTo(2);
        assertThat(søknadVersjon.getBarna()).hasSize(2);
        assertThat(søknadVersjon.getBarna().stream().map(UidentifisertBarn::getFødselsdato)).containsExactly(now, now.minusDays(2));
        assertThat(søknadVersjon.getType()).isEqualTo(FamilieHendelseType.ADOPSJON);
        assertThat(søknadVersjon.getAdopsjon()).isPresent();
        assertThat(søknadVersjon.getAdopsjon().get().getOmsorgsovertakelseDato()).isEqualTo(now);
    }

    @Test
    public void skal_merge_fra_gammel_til_ny_adopsjon_med_oppdaterte_verdier() {
        final LocalDate now = LocalDate.now();
        final FamilieHendelseGrunnlag hendelseAggregat = byggAggregat(now);
        final FamilieHendelse søknadVersjon = hendelseAggregat.getGjeldendeVersjon();

        assertThat(hendelseAggregat.getHarBekreftedeData()).isFalse();
        assertThat(søknadVersjon.getAntallBarn()).isEqualTo(2);
        assertThat(søknadVersjon.getBarna()).hasSize(2);
        assertThat(søknadVersjon.getBarna().stream().map(UidentifisertBarn::getFødselsdato)).containsExactly(now, now.minusDays(2));
        assertThat(søknadVersjon.getType()).isEqualTo(FamilieHendelseType.ADOPSJON);
        assertThat(søknadVersjon.getAdopsjon()).isPresent();
        assertThat(søknadVersjon.getAdopsjon().get().getOmsorgsovertakelseDato()).isEqualTo(now);

        final FamilieHendelseGrunnlagBuilder oppdatere = FamilieHendelseGrunnlagBuilder.oppdatere(Optional.of(hendelseAggregat));
        final FamilieHendelseBuilder oppdatertHendelse = FamilieHendelseBuilder.oppdatere(Optional.of(hendelseAggregat.getSøknadVersjon()), HendelseVersjonType.SØKNAD);

        oppdatertHendelse.tilbakestillBarn();
        oppdatertHendelse.leggTilBarn(now.minusYears(1));
        oppdatertHendelse.leggTilBarn(now.minusYears(2));
        final FamilieHendelseGrunnlag oppdatertHendelseAggregat = oppdatere.medBekreftetVersjon(oppdatertHendelse).build();
        final FamilieHendelse gjellendeVersjon = oppdatertHendelseAggregat.getGjeldendeVersjon();

        assertThat(oppdatertHendelseAggregat.getHarBekreftedeData()).isTrue();
        assertThat(gjellendeVersjon.getAntallBarn()).isEqualTo(2);
        assertThat(gjellendeVersjon.getBarna()).hasSize(2);
        assertThat(gjellendeVersjon.getBarna().stream().map(UidentifisertBarn::getFødselsdato)).containsExactly(now.minusYears(1), now.minusYears(2));
        assertThat(gjellendeVersjon.getType()).isEqualTo(FamilieHendelseType.ADOPSJON);
        assertThat(gjellendeVersjon.getAdopsjon()).isPresent();
        assertThat(gjellendeVersjon.getAdopsjon().get().getOmsorgsovertakelseDato()).isEqualTo(now);
    }

    private FamilieHendelseGrunnlag byggAggregat(LocalDate now) {
        Map<Integer, LocalDate> fødselsdatoer = new HashMap<>();
        fødselsdatoer.put(1, now);
        fødselsdatoer.put(2, now.minusDays(2));

        final FamilieHendelseGrunnlagBuilder oppdatere = FamilieHendelseGrunnlagBuilder.oppdatere(Optional.empty());
        FamilieHendelseBuilder builder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        final FamilieHendelseBuilder.AdopsjonBuilder adopsjonBuilder = builder.getAdopsjonBuilder();
        adopsjonBuilder.medOmsorgsovertakelseDato(now);
        for (LocalDate localDate : fødselsdatoer.values()) {
            builder.leggTilBarn(localDate);
        }
        builder.medAdopsjon(adopsjonBuilder);
        return oppdatere.medSøknadVersjon(builder).build();
    }
}
