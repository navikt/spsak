package no.nav.foreldrepenger.domene.uttak.fastsetteperioder.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.AktivitetStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPeriode;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagPrStatusOgAndel;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BeregningsgrunnlagRepository;

public class UttakBeregningsandelTjenesteImplTest {

    @Test
    public void skalHenteBeregningsandeler() {
        BeregningsgrunnlagRepository repository = mock(BeregningsgrunnlagRepository.class);
        Behandling behandling = mock(Behandling.class);
        Beregningsgrunnlag beregningsgrunnlag = Beregningsgrunnlag.builder().medSkjæringstidspunkt(LocalDate.now()).build();
        BeregningsgrunnlagPeriode periode = BeregningsgrunnlagPeriode.builder()
            .medBeregningsgrunnlagPeriode(LocalDate.now(), LocalDate.now())
            .build(beregningsgrunnlag);
        BeregningsgrunnlagPrStatusOgAndel andel = BeregningsgrunnlagPrStatusOgAndel.builder().medAktivitetStatus(AktivitetStatus.ARBEIDSTAKER).build(periode);
        when(repository.hentBeregningsgrunnlag(behandling)).thenReturn(Optional.of(beregningsgrunnlag));
        UttakBeregningsandelTjenesteImpl tjeneste = new UttakBeregningsandelTjenesteImpl(repository);

        assertThat(tjeneste.hentAndeler(behandling)).hasSize(1);
        assertThat(tjeneste.hentAndeler(behandling).get(0)).isEqualTo(andel);
    }
}
