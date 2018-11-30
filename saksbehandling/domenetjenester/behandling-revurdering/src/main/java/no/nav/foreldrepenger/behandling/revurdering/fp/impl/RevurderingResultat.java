package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.Beregningsgrunnlag;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakResultatEntitet;

class RevurderingResultat {
    private LocalDate endringsdato;
    private boolean varselOmRevurderingSendt;
    private Optional<UttakResultatEntitet> uttakresultatRevurderingOpt;
    private Optional<UttakResultatEntitet> uttakresultatOriginalOpt;
    private Optional<Beregningsgrunnlag> revurderingGrunnlagOpt;
    private Optional<Beregningsgrunnlag> originalGrunnlagOpt;

    RevurderingResultat(LocalDate endringsdato, boolean varselOmRevurderingSendt,
                        Optional<UttakResultatEntitet> uttakresultatRevurderingOpt, Optional<UttakResultatEntitet> uttakresultatOriginalOpt,
                        Optional<Beregningsgrunnlag> revurderingGrunnlagOpt, Optional<Beregningsgrunnlag> originalGrunnlagOpt) {
        this.endringsdato = endringsdato;
        this.varselOmRevurderingSendt = varselOmRevurderingSendt;
        this.uttakresultatRevurderingOpt = uttakresultatRevurderingOpt;
        this.uttakresultatOriginalOpt = uttakresultatOriginalOpt;
        this.revurderingGrunnlagOpt = revurderingGrunnlagOpt;
        this.originalGrunnlagOpt = originalGrunnlagOpt;
    }

    Optional<UttakResultatEntitet> getUttakresultatRevurderingOpt() {
        return uttakresultatRevurderingOpt;
    }

    Optional<UttakResultatEntitet> getUttakresultatOriginalOpt() {
        return uttakresultatOriginalOpt;
    }

    Optional<Beregningsgrunnlag> getRevurderingGrunnlagOpt() {
        return revurderingGrunnlagOpt;
    }

    Optional<Beregningsgrunnlag> getOriginalGrunnlagOpt() {
        return originalGrunnlagOpt;
    }

    LocalDate getEndringsdato() {
        return endringsdato;
    }

    boolean erVarselOmRevurderingSendt() {
        return varselOmRevurderingSendt;
    }
}
