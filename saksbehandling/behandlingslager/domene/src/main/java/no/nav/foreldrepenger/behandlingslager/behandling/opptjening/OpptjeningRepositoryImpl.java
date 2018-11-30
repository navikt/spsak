package no.nav.foreldrepenger.behandlingslager.behandling.opptjening;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.EndringsresultatSnapshot;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingLås;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.BehandlingRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class OpptjeningRepositoryImpl implements OpptjeningRepository {

    private EntityManager em;
    private BehandlingRepository behandlingRepository;
    private KodeverkRepository kodeverkRepository;

    public OpptjeningRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public OpptjeningRepositoryImpl(@VLPersistenceUnit EntityManager em, BehandlingRepository behandlingRepository, KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(em, "em"); //$NON-NLS-1$
        Objects.requireNonNull(behandlingRepository, "behandlingRepository");
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository");
        this.em = em;
        this.behandlingRepository = behandlingRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    private static VilkårResultat validerRiktigBehandling(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$
        Behandlingsresultat behandlingresultat = behandling.getBehandlingsresultat();
        if (behandlingresultat == null) {
            throw new IllegalArgumentException(
                "Utvikler-feil: kan ikke sette opptjening før Behandlingresultat er lagd for Behandling:" + behandling.getId()); //$NON-NLS-1$
        }
        VilkårResultat vilkårResultat = behandlingresultat.getVilkårResultat();
        if (vilkårResultat == null) {
            throw new IllegalArgumentException(
                "Utvikler-feil: kan ikke sette opptjening før VilkårResultat er lagd for Behandling:" + behandling.getId()); //$NON-NLS-1$
        }
        if (!behandling.equals(vilkårResultat.getOriginalBehandling())) {
            throw new IllegalArgumentException(
                "Utvikler-feil: kan ikke sette opptjening på vilkårResultat fra tidligere behandling. behanlding= " + behandling.getId() //$NON-NLS-1$
                    + ", original=" + vilkårResultat); //$NON-NLS-1$
        }
        return vilkårResultat;
    }

    @Override
    public Optional<Opptjening> finnOpptjening(Behandling behandling) {

        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();

        if (behandlingsresultat == null) {
            return Optional.empty();
        } else {

            VilkårResultat vilkårResultat = behandlingsresultat.getVilkårResultat();
            if (vilkårResultat == null) {
                return Optional.empty();
            }

            return finnOpptjening(vilkårResultat);
        }

    }

    @Override
    public Optional<Opptjening> finnOpptjening(VilkårResultat vilkårResultat) {

        return hentTidligereOpptjening(vilkårResultat, true);
    }

    private Optional<Opptjening> hentTidligereOpptjening(VilkårResultat vilkårResultat, boolean readOnly) {
        // slår opp med HQL istedf. å traverse grafen
        TypedQuery<Opptjening> query = em.createQuery("from Opptjening o where o.vilkårResultat.id=:id and o.aktiv = 'J'", Opptjening.class); //$NON-NLS-1$
        query.setParameter("id", vilkårResultat.getId()); //$NON-NLS-1$

        if (readOnly) {
            // returneres read-only, kan kun legge til nye ved skriving uten å oppdatere
            query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        }
        return HibernateVerktøy.hentUniktResultat(query);
    }

    private Optional<Opptjening> deaktivereTidligereOpptjening(VilkårResultat vilkårResultat, boolean readOnly) {
        Optional<Opptjening> opptjening = hentTidligereOpptjening(vilkårResultat, readOnly);
        if (opptjening.isPresent()) {
            Query query = em.createNativeQuery("UPDATE OPPTJENING SET AKTIV = 'N' WHERE ID=:id"); //$NON-NLS-1$
            query.setParameter("id", opptjening.get().getId()); //$NON-NLS-1$
            query.executeUpdate();
            em.flush();
            return opptjening;
        }
        return opptjening;
    }

    @Override
    public Opptjening lagreOpptjeningsperiode(Behandling behandling, LocalDate opptjeningFom, LocalDate opptjeningTom) {

        Opptjening opptjening = lagre(behandling, (tidligereOpptjening) -> {
            // lager ny opptjening alltid ved ny opptjeningsperiode.
            return new Opptjening(opptjeningFom, opptjeningTom);
        });

        return opptjening;
    }

    @Override
    public void deaktiverOpptjening(Behandling behandling) {
        VilkårResultat vilkårResultat = validerRiktigBehandling(behandling);

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);
        deaktivereTidligereOpptjening(vilkårResultat, false);
        em.flush();

        behandlingRepository.verifiserBehandlingLås(behandlingLås);
    }

    private Opptjening lagre(Behandling behandling, Function<Opptjening, Opptjening> oppdateringsfunksjon) {
        VilkårResultat vilkårResultat = validerRiktigBehandling(behandling);

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandling);


        Opptjening tidligereOpptjening = null;
        Opptjening opptjening;
        Optional<Opptjening> optTidligereOpptjening = deaktivereTidligereOpptjening(vilkårResultat, false);
        if (optTidligereOpptjening.isPresent()) {
            tidligereOpptjening = optTidligereOpptjening.get();
        }
        opptjening = oppdateringsfunksjon.apply(tidligereOpptjening);

        opptjening.setVilkårResultat(behandling.getBehandlingsresultat().getVilkårResultat());

        em.persist(opptjening);
        em.flush();

        behandlingRepository.verifiserBehandlingLås(behandlingLås);

        return opptjening;

    }

    @Override
    public Opptjening lagreOpptjeningResultat(Behandling behandling, Period opptjentPeriode,
                                              Collection<OpptjeningAktivitet> opptjeningAktiviteter) {

        Set<OpptjeningAktivitet> kopiListe = duplikatSjekk(opptjeningAktiviteter);

        Function<Opptjening, Opptjening> oppdateringsfunksjon = (tidligereOpptjening) -> {
            Opptjening ny = new Opptjening(tidligereOpptjening);
            ny.setOpptjeningAktivitet(kopiListe);
            ny.setOpptjentPeriode(opptjentPeriode);
            return ny;
        };

        return lagre(behandling, oppdateringsfunksjon);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling origBehandling, Behandling nyBehandling) {
        // Opptjening er ikke koblet til Behandling gjennom aggregatreferanse. Må derfor kopieres som deep copy
        Opptjening origOpptjening = hentTidligereOpptjening(origBehandling.getBehandlingsresultat().getVilkårResultat(), true)
            .orElseThrow(() -> new IllegalStateException("Original behandling har ikke opptjening."));

        lagreOpptjeningsperiode(nyBehandling, origOpptjening.getFom(), origOpptjening.getTom());
        lagreOpptjeningResultat(nyBehandling, origOpptjening.getOpptjentPeriode(), origOpptjening.getOpptjeningAktivitet());
    }

    private Set<OpptjeningAktivitet> duplikatSjekk(Collection<OpptjeningAktivitet> opptjeningAktiviteter) {
        // ta kopi av alle aktiviteter for å være sikker på at gamle ikke skrives inn.
        if (opptjeningAktiviteter == null) {
            return Collections.emptySet();
        }
        Set<OpptjeningAktivitet> kopiListe = opptjeningAktiviteter.stream().map(oa -> new OpptjeningAktivitet(oa)).collect(Collectors.toCollection(LinkedHashSet::new));

        if (opptjeningAktiviteter.size() > kopiListe.size()) {
            // har duplikater!!
            Set<OpptjeningAktivitet> duplikater = opptjeningAktiviteter.stream()
                .filter(oa -> Collections.frequency(opptjeningAktiviteter, oa) > 1)
                .collect(Collectors.toCollection(LinkedHashSet::new));

            throw new IllegalArgumentException(
                "Utvikler-feil: har duplikate opptjeningsaktiviteter: [" + duplikater + "] i input: " + opptjeningAktiviteter); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return kopiListe;
    }

    private Optional<Long> finnAktivOptjeningId(Behandling behandling){
        return finnOpptjening(behandling).map(Opptjening::getId);
    }

    //Denne metoden bør legges i Tjeneste
    @Override
    public EndringsresultatSnapshot finnAktivGrunnlagId(Behandling behandling){
        Optional<Long> funnetId = finnAktivOptjeningId(behandling);
       return funnetId
           .map(id-> EndringsresultatSnapshot.medSnapshot(Opptjening.class,id))
           .orElse(EndringsresultatSnapshot.utenSnapshot(Opptjening.class));

    }

    @Override
    public OpptjeningAktivitetType getOpptjeningAktivitetTypeForKode(String aktivitetTypeKode) {
        return kodeverkRepository.finn(OpptjeningAktivitetType.class, aktivitetTypeKode);
    }

    @Override
    public OpptjeningAktivitetKlassifisering getOpptjeningAktivitetKlassifisering(String kode) {
        return kodeverkRepository.finn(OpptjeningAktivitetKlassifisering.class, kode);
    }

}
