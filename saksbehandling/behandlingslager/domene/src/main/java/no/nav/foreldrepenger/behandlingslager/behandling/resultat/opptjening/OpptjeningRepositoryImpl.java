package no.nav.foreldrepenger.behandlingslager.behandling.resultat.opptjening;

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

    private static VilkårResultat validerRiktigBehandling(Behandlingsresultat behandlingresultat) {
        Objects.requireNonNull(behandlingresultat, "behandlingresultat"); //$NON-NLS-1$
        VilkårResultat vilkårResultat = behandlingresultat.getVilkårResultat();
        if (vilkårResultat == null) {
            throw new IllegalArgumentException(
                "Utvikler-feil: kan ikke sette opptjening før VilkårResultat er lagd for Behandlingsresultat:" + behandlingresultat.getId()); //$NON-NLS-1$
        }
        if (!behandlingresultat.getBehandling().equals(vilkårResultat.getOriginalBehandling())) {
            throw new IllegalArgumentException(
                "Utvikler-feil: kan ikke sette opptjening på vilkårResultat fra tidligere behandling. Behandlingsresultat= " + behandlingresultat.getId() //$NON-NLS-1$
                    + ", original=" + vilkårResultat); //$NON-NLS-1$
        }
        return vilkårResultat;
    }

    @Override
    public Optional<Opptjening> finnOpptjening(Behandlingsresultat behandlingresultat) {

        if (behandlingresultat == null) {
            return Optional.empty();
        } else {

            return hentTidligereOpptjening(behandlingresultat, true);
        }

    }

    private Optional<Opptjening> hentTidligereOpptjening(Behandlingsresultat behandlingsresultat, boolean readOnly) {
        // slår opp med HQL istedf. å traverse grafen
        TypedQuery<Opptjening> query = em.createQuery("from Opptjening o where o.behandlingsresultat.id=:id and o.aktiv = true", Opptjening.class); //$NON-NLS-1$
        query.setParameter("id", behandlingsresultat.getId()); //$NON-NLS-1$

        if (readOnly) {
            // returneres read-only, kan kun legge til nye ved skriving uten å oppdatere
            query.setHint(QueryHints.HINT_READONLY, "true"); //$NON-NLS-1$
        }
        return HibernateVerktøy.hentUniktResultat(query);
    }

    private Optional<Opptjening> deaktivereTidligereOpptjening(Behandlingsresultat behandlingsresultat, boolean readOnly) {
        Optional<Opptjening> opptjening = hentTidligereOpptjening(behandlingsresultat, readOnly);
        if (opptjening.isPresent()) {
            Query query = em.createNativeQuery("UPDATE RES_OPPTJENING SET AKTIV = 'N' WHERE ID=:id"); //$NON-NLS-1$
            query.setParameter("id", opptjening.get().getId()); //$NON-NLS-1$
            query.executeUpdate();
            em.flush();
            return opptjening;
        }
        return opptjening;
    }

    @Override
    public Opptjening lagreOpptjeningsperiode(Behandlingsresultat behandlingresultat, LocalDate opptjeningFom, LocalDate opptjeningTom) {

        Opptjening opptjening = lagre(behandlingresultat, (tidligereOpptjening) -> {
            // lager ny opptjening alltid ved ny opptjeningsperiode.
            return new Opptjening(opptjeningFom, opptjeningTom);
        });

        return opptjening;
    }

    @Override
    public void deaktiverOpptjening(Behandlingsresultat behandlingresultat) {
        validerRiktigBehandling(behandlingresultat);

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandlingresultat.getBehandling());
        deaktivereTidligereOpptjening(behandlingresultat, false);
        em.flush();

        behandlingRepository.verifiserBehandlingLås(behandlingLås);
    }

    private Opptjening lagre(Behandlingsresultat behandlingresultat, Function<Opptjening, Opptjening> oppdateringsfunksjon) {
        validerRiktigBehandling(behandlingresultat);

        BehandlingLås behandlingLås = behandlingRepository.taSkriveLås(behandlingresultat.getBehandling());


        Opptjening tidligereOpptjening = null;
        Opptjening opptjening;
        Optional<Opptjening> optTidligereOpptjening = deaktivereTidligereOpptjening(behandlingresultat, false);
        if (optTidligereOpptjening.isPresent()) {
            tidligereOpptjening = optTidligereOpptjening.get();
        }
        opptjening = oppdateringsfunksjon.apply(tidligereOpptjening);

        opptjening.setBehandlingsresultat(behandlingresultat);

        em.persist(opptjening);
        em.flush();

        behandlingRepository.verifiserBehandlingLås(behandlingLås);

        return opptjening;

    }

    @Override
    public Opptjening lagreOpptjeningResultat(Behandlingsresultat behandlingresultat, Period opptjentPeriode,
                                              Collection<OpptjeningAktivitet> opptjeningAktiviteter) {

        Set<OpptjeningAktivitet> kopiListe = duplikatSjekk(opptjeningAktiviteter);

        Function<Opptjening, Opptjening> oppdateringsfunksjon = (tidligereOpptjening) -> {
            Opptjening ny = new Opptjening(tidligereOpptjening);
            ny.setOpptjeningAktivitet(kopiListe);
            ny.setOpptjentPeriode(opptjentPeriode);
            return ny;
        };

        return lagre(behandlingresultat, oppdateringsfunksjon);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling nyBehandling, Behandlingsresultat eksisterende, Behandlingsresultat nytt) {
        // Opptjening er ikke koblet til Behandling gjennom aggregatreferanse. Må derfor kopieres som deep copy
        Opptjening origOpptjening = hentTidligereOpptjening(eksisterende, true)
            .orElseThrow(() -> new IllegalStateException("Original behandling har ikke opptjening."));

        lagreOpptjeningsperiode(nytt, origOpptjening.getFom(), origOpptjening.getTom());
        lagreOpptjeningResultat(nytt, origOpptjening.getOpptjentPeriode(), origOpptjening.getOpptjeningAktivitet());
    }

    private Set<OpptjeningAktivitet> duplikatSjekk(Collection<OpptjeningAktivitet> opptjeningAktiviteter) {
        // ta kopi av alle aktiviteter for å være sikker på at gamle ikke skrives inn.
        if (opptjeningAktiviteter == null) {
            return Collections.emptySet();
        }
        Set<OpptjeningAktivitet> kopiListe = opptjeningAktiviteter.stream().map(OpptjeningAktivitet::new).collect(Collectors.toCollection(LinkedHashSet::new));

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

    private Optional<Long> finnAktivOptjeningId(Behandlingsresultat behandlingsresultat) {
        return finnOpptjening(behandlingsresultat).map(Opptjening::getId);
    }

    //Denne metoden bør legges i Tjeneste
    @Override
    public EndringsresultatSnapshot finnAktivGrunnlagId(Behandlingsresultat behandlingsresultat) {
        Optional<Long> funnetId = Optional.ofNullable(behandlingsresultat).flatMap(this::finnAktivOptjeningId);
        return funnetId
            .map(id -> EndringsresultatSnapshot.medSnapshot(Opptjening.class, id))
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
