package no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;

public interface OpptjeningRepository {

    /** Finn gjeldende opptjening for denne behandlingen. */
    Optional<Opptjening> finnOpptjening(Behandlingsresultat behandlingresultat);

    /*Opptjening* Lagre opptjeningresultat (opptjent periode og aktiviteter).*/
    Opptjening lagreOpptjeningResultat(Behandlingsresultat behandlingresultat, Period opptjentPeriode, Collection<OpptjeningAktivitet> opptjeningAktiviteter);

    /** Lagre Opptjeningsperiode (fom, tom) for en gitt behandling. */
    Opptjening lagreOpptjeningsperiode(Behandlingsresultat behandlingresultat, LocalDate opptjeningFom, LocalDate opptjeningTom);

    void deaktiverOpptjening(Behandlingsresultat behandlingresultat);

    /** Kopier over grunnlag til ny behandling */
    void kopierGrunnlagFraEksisterendeBehandling(Behandling behandling, Behandlingsresultat eksisterende, Behandlingsresultat nytt);

    /** Finn type for angitt kode. */
    OpptjeningAktivitetType getOpptjeningAktivitetTypeForKode(String aktivitetType);

    /** Finn klassifiering for angitt kode. */
    OpptjeningAktivitetKlassifisering getOpptjeningAktivitetKlassifisering(String kode);


    EndringsresultatSnapshot finnAktivGrunnlagId(Behandlingsresultat behandlingsresultat);
}
