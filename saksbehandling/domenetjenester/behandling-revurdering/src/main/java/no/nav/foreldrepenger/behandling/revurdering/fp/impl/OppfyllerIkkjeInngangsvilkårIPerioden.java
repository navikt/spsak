package no.nav.foreldrepenger.behandling.revurdering.fp.impl;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapVilkårPeriodeGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.resultat.medlemskap.MedlemskapsvilkårPerioder;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårUtfallType;

class OppfyllerIkkjeInngangsvilkårIPerioden {

    private OppfyllerIkkjeInngangsvilkårIPerioden() {
    }

    public static boolean vurder(Optional<MedlemskapVilkårPeriodeGrunnlag> grunnlagOpt, LocalDate endringsdato) {
        return !grunnlagOpt.isPresent() || harIkkjeOppfyltMedlemskapsvilkårPåEndringstidspunktet(grunnlagOpt.get(), endringsdato);
    }

    private static boolean harIkkjeOppfyltMedlemskapsvilkårPåEndringstidspunktet(MedlemskapVilkårPeriodeGrunnlag grunnlag, LocalDate endringsdato) {
        return grunnlag.getMedlemskapsvilkårPeriode().getPerioder().stream()
            .anyMatch(periode -> overlapperMedDatoOgErIkkjeOppfylt(endringsdato, periode));
    }

    private static boolean overlapperMedDatoOgErIkkjeOppfylt(LocalDate endringsdato, MedlemskapsvilkårPerioder periode) {
        return endringsdato != null && !periode.getFom().isAfter(endringsdato) &&
            !periode.getTom().isBefore(endringsdato) &&
            !periode.getVilkårUtfall().equals(VilkårUtfallType.OPPFYLT);
    }

    public static Behandlingsresultat fastsett(Behandling revurdering) {
        return SettOpphørOgIkkeRett.fastsett(revurdering);
    }
}
