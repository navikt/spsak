package no.nav.foreldrepenger.behandlingslager.behandling.medlemskap;

import java.util.Collection;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.BehandlingslagerRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;

/**
 * Repository for å håndtere persistering av Medlemskap opplysninger for Behandling.
 * Dette håndterer opplysninger hentet fra registre, satt fra søknad eller bekreftet av saksbehandler som et aggregat
 * (Domain Driven Design Aggregate pattern).
 *
 * Aggregatte knyttes til en Behandling som del av et MedlemskapBehandlingsgrunnlag, og kan således gjenbrukes mellom
 * ulike behandlinger.
 *
 * Som helhet er aggregatet immutable, dvs. at endringer må gjøres ved å skrive ned på en kopi av hele aggregatet.
 */
public interface MedlemskapRepository extends BehandlingslagerRepository {


    /** Henter et aggregat for Medlemskap, hvis det eksisterer. */
    Optional<MedlemskapAggregat> hentMedlemskap(Behandling behandling);

    /** Henter et aggregat for Medlemskap, hvis det eksisterer, basert på kun behandlingId */
    Optional<MedlemskapAggregat> hentMedlemskap(Long behandlingId);

    void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling eksisterendeBehandling, Behandling nyBehandling);

    /** Lagre registrert opplysninger om medlemskap (fra MEDL). Merk at implementasjonen står fritt til å ta en kopi av oppgitte data.*/
    void lagreMedlemskapRegisterOpplysninger(Behandling behandling, Collection<RegistrertMedlemskapPerioder> registrertMedlemskap);

    /** Lagre vurderte opplysninger om meldemskap slik det har blitt gjort av Saksbehandler eller av systemet automatisk. Merk at implementasjonen står fritt til å ta en kopi av oppgitte data.*/
    void lagreMedlemskapVurdering(Behandling behandling, VurdertMedlemskap vurdertMedlemskap);

    boolean erEndret(Long grunnlagId, Behandling behandling);

    /** Kopierer grunnlag fra en tidligere behandling.  Endrer ikke aggregater, en skaper nye referanser til disse. */
    void kopierGrunnlagFraEksisterendeBehandling(Behandling eksisterendeBehandling, Behandling nyBehandling);

    /**
     * Hent kun {@link VurdertMedlemskap} fra Behandling.
     */
    Optional<VurdertMedlemskap> hentVurdertMedlemskap(Behandling behandling);

    /**
     * Slette avklart medlemskapsdata på en Behandling. Sørger for at samtidige oppdateringer på samme Behandling,
     * eller andre Behandlinger
     * på samme Fagsak ikke kan gjøres samtidig.
     *
     * @see BehandlingLås
     */
    void slettAvklarteMedlemskapsdata(Behandling behandling, BehandlingLås lås);

    /** Henter førsteversjon av aggregat for Medlemskap, hvis det eksisterer. */
    Optional<MedlemskapAggregat> hentFørsteVersjonAvMedlemskap(Behandling behandling);

    Optional<Long> hentIdPåAktivMedlemskap(Behandling behandling);

    MedlemskapAggregat hentMedlemskapPåId(Long aggregatId);

    DiffResult diffResultat(Long grunnlagId1, Long grunnlagId2, FagsakYtelseType ytelseType, boolean kunSporedeEndringer);

    /** Lagre vurderte opplysninger om løpende meldemskap slik det har blitt gjort av Saksbehandler eller av systemet automatisk. Merk at implementasjonen står fritt til å ta en kopi av oppgitte data.*/
    void lagreLøpendeMedlemskapVurdering(Behandling behandling, VurdertMedlemskapPeriode løpendeMedlemskap);
}
