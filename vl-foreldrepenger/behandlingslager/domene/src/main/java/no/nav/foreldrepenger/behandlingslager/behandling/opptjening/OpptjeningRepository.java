package no.nav.foreldrepenger.behandlingslager.behandling.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;

public interface OpptjeningRepository {

    /** Finn gjeldende opptjening for denne behandlingen. */
    Optional<Opptjening> finnOpptjening(Behandling behandling);

    /** Finn gjeldende behandling for et angitt vilkårresultat. */
    Optional<Opptjening> finnOpptjening(VilkårResultat vilkårResultat);

    /*Opptjening* Lagre opptjeningresultat (opptjent periode og aktiviteter).*/
    Opptjening lagreOpptjeningResultat(Behandling behandling, Period opptjentPeriode, Collection<OpptjeningAktivitet> opptjeningAktiviteter);

    /** Lagre Opptjeningsperiode (fom, tom) for en gitt behandling. */
    Opptjening lagreOpptjeningsperiode(Behandling behandling, LocalDate opptjeningFom, LocalDate opptjeningTom);

    void deaktiverOpptjening(Behandling behandling);

    /** Kopier over grunnlag til ny behandling */
    void kopierGrunnlagFraEksisterendeBehandling(Behandling origBehandling, Behandling behandling);

    /** Finn type for angitt kode. */
    OpptjeningAktivitetType getOpptjeningAktivitetTypeForKode(String aktivitetType);

    /** Finn klassifiering for angitt kode. */
    OpptjeningAktivitetKlassifisering getOpptjeningAktivitetKlassifisering(String kode);


    EndringsresultatSnapshot finnAktivGrunnlagId(Behandling behandling);
}
