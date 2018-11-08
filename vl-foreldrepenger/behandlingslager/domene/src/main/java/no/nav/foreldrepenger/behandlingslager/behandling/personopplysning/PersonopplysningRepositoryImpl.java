package no.nav.foreldrepenger.behandlingslager.behandling.personopplysning;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.behandlingslager.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.behandlingslager.diff.DiffEntity;
import no.nav.foreldrepenger.behandlingslager.diff.DiffResult;
import no.nav.foreldrepenger.behandlingslager.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.behandlingslager.diff.YtelseKode;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

/**
 * Dette er et Repository for håndtering av alle persistente endringer i en Personopplysning graf.
 * Personopplysning graf har en rot, representert ved Søkers Personopplysning innslag. Andre innslag kan være Barn eller
 * Partner.
 * <p>
 * Hent opp og lagre innhentende Personopplysning data, fra søknad, register (TPS) eller som avklart av Saksbehandler.
 * Ved hver endring kopieres Personopplysning grafen (inklusiv Familierelasjon) som et felles
 * Aggregat (ref. Domain Driven Design - Aggregat pattern)
 * <p>
 * Søkers Personopplysning representerer rot i grafen. Denne linkes til Behandling gjennom et
 * PersonopplysningGrunnlagEntitet.
 * <p>
 * Merk: standard regler - et Grunnlag eies av en Behandling. Et Aggregat (Søkers Personopplysning graf) har en
 * selvstenig livssyklus og vil kopieres ved hver endring.
 * Ved multiple endringer i et grunnlat for en Behandling vil alltid kun et innslag i grunnlag være aktiv for angitt
 * Behandling.
 */
@ApplicationScoped
public class PersonopplysningRepositoryImpl implements PersonopplysningRepository {
    private EntityManager entityManager;

    PersonopplysningRepositoryImpl() {
        // CDI
    }

    @Inject
    public PersonopplysningRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Behandling eksisterendeBehandling, Behandling nyBehandling) {
        Optional<PersonopplysningGrunnlagEntitet> eksisterendeGrunnlag = getAktivtGrunnlag(eksisterendeBehandling);

        final PersonopplysningGrunnlagBuilder builder = PersonopplysningGrunnlagBuilder.oppdatere(eksisterendeGrunnlag);

        lagreOgFlush(nyBehandling, builder);
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandlingForRevurdering(Behandling eksisterendeBehandling, Behandling nyBehandling) {
        Optional<PersonopplysningGrunnlagEntitet> eksisterendeGrunnlag = getAktivtGrunnlag(eksisterendeBehandling);

        final PersonopplysningGrunnlagBuilder builder = PersonopplysningGrunnlagBuilder.oppdatere(eksisterendeGrunnlag);
        builder.medOverstyrtVersjon(null);

        lagreOgFlush(nyBehandling, builder);
    }

    private DiffEntity personopplysningDiffer() {
        TraverseEntityGraph traverser = TraverseEntityGraphFactory.build();
        return new DiffEntity(traverser);
    }

    @Override
    public PersonopplysningGrunnlag hentPersonopplysninger(Behandling behandling) {
        return hentPersonopplysningerHvisEksisterer(behandling).orElse(null);
    }

    @Override
    public DiffResult diffResultat(PersonopplysningGrunnlag grunnlag1, PersonopplysningGrunnlag grunnlag2, FagsakYtelseType ytelseType, boolean onlyCheckTrackedFields) {
        return new RegisterdataDiffsjekker(YtelseKode.valueOf(ytelseType.getKode()), onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    @Override
    public Optional<PersonopplysningGrunnlag> hentPersonopplysningerHvisEksisterer(Behandling behandling) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$ //NOSONAR
        Optional<PersonopplysningGrunnlagEntitet> pbg = getAktivtGrunnlag(behandling);
        if (pbg.isPresent()) {
            return Optional.of(pbg.get());
        }
        return Optional.empty();
    }

    private Optional<PersonopplysningGrunnlagEntitet> getAktivtGrunnlag(Behandling behandling) {
        Long behandlingId = behandling.getId();
        TypedQuery<PersonopplysningGrunnlagEntitet> query = entityManager.createQuery(
            "SELECT pbg FROM PersonopplysningGrunnlagEntitet pbg WHERE pbg.behandling.id = :behandling_id AND pbg.aktiv = true", //$NON-NLS-1$
            PersonopplysningGrunnlagEntitet.class)
            .setParameter("behandling_id", behandlingId); //$NON-NLS-1$

        return HibernateVerktøy.hentUniktResultat(query);
    }

    private void lagreOgFlush(Behandling behandling, PersonopplysningGrunnlagBuilder grunnlagBuilder) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$ //NOSONAR
        Objects.requireNonNull(grunnlagBuilder, "grunnlagBuilder"); //$NON-NLS-1$ //NOSONAR

        final Optional<PersonopplysningGrunnlagEntitet> aktivtGrunnlag = getAktivtGrunnlag(behandling);

        final DiffEntity diffEntity = personopplysningDiffer();

        final PersonopplysningGrunnlag build = grunnlagBuilder.build();
        ((PersonopplysningGrunnlagEntitet) build).setBehandling(behandling);

        if (diffEntity.areDifferent(aktivtGrunnlag.orElse(null), build)) {
            aktivtGrunnlag.ifPresent(grunnlag -> {
                // setter gammelt grunnlag inaktiv. Viktig å gjøre før nye endringer siden vi kun
                // tillater ett aktivt grunnlag per behandling
                grunnlag.setAktiv(false);
                entityManager.persist(grunnlag);
                entityManager.flush();
            });

            Optional.ofNullable(build.getRegisterVersjon()).ifPresent(this::persisterPersonInformasjon);
            build.getOverstyrtVersjon().ifPresent(this::persisterPersonInformasjon);
            build.getOppgittAnnenPart().ifPresent(oppgittAnnenPart -> entityManager.persist(oppgittAnnenPart));

            entityManager.persist(build);
            entityManager.flush();
        }
    }

    private void persisterPersonInformasjon(PersonInformasjon registerVersjon) {
        entityManager.persist(registerVersjon);
        for (PersonAdresse entitet : registerVersjon.getAdresser()) {
            entityManager.persist(entitet);
        }
        for (PersonRelasjon entitet : registerVersjon.getRelasjoner()) {
            entityManager.persist(entitet);
        }
        for (Personstatus entitet : registerVersjon.getPersonstatus()) {
            entityManager.persist(entitet);
        }
        for (Statsborgerskap entitet : registerVersjon.getStatsborgerskap()) {
            entityManager.persist(entitet);
        }
        for (Personopplysning entitet : registerVersjon.getPersonopplysninger()) {
            entityManager.persist(entitet);
        }
    }

    @Override
    public void lagre(Behandling behandling, PersonInformasjonBuilder builder) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$ //NOSONAR
        Objects.requireNonNull(builder, "søknadAnnenPartBuilder"); //$NON-NLS-1$

        final PersonopplysningGrunnlagBuilder nyttGrunnlag = getGrunnlagBuilderFor(behandling);

        if (builder.getType().equals(PersonopplysningVersjonType.REGISTRERT)) {
            nyttGrunnlag.medRegistrertVersjon(builder);
        }
        if (builder.getType().equals(PersonopplysningVersjonType.OVERSTYRT)) {
            nyttGrunnlag.medOverstyrtVersjon(builder);
        }

        lagreOgFlush(behandling, nyttGrunnlag);
    }

    private PersonopplysningGrunnlagBuilder getGrunnlagBuilderFor(Behandling behandling) {
        final Optional<PersonopplysningGrunnlagEntitet> aktivtGrunnlag = getAktivtGrunnlag(behandling);
        return PersonopplysningGrunnlagBuilder.oppdatere(aktivtGrunnlag);
    }

    @Override
    public void lagre(Behandling behandling, OppgittAnnenPartBuilder oppgittAnnenPart) {
        Objects.requireNonNull(behandling, "behandling"); //$NON-NLS-1$ //NOSONAR
        Objects.requireNonNull(oppgittAnnenPart, "oppgittAnnenPart"); //$NON-NLS-1$

        final PersonopplysningGrunnlagBuilder nyttGrunnlag = getGrunnlagBuilderFor(behandling);

        nyttGrunnlag.medOppgittAnnenPart(oppgittAnnenPart.build());

        lagreOgFlush(behandling, nyttGrunnlag);
    }

    @Override
    public PersonInformasjonBuilder opprettBuilderForRegisterdata(Behandling behandling) {
        final Optional<PersonopplysningGrunnlagEntitet> aktivtGrunnlag = getAktivtGrunnlag(behandling);
        return PersonInformasjonBuilder.oppdater(aktivtGrunnlag.map(PersonopplysningGrunnlagEntitet::getRegisterVersjon),
            PersonopplysningVersjonType.REGISTRERT);
    }

    @Override
    public PersonInformasjonBuilder opprettBuilderForOverstyring(Behandling behandling) {
        final PersonopplysningGrunnlagEntitet aktivtGrunnlag = getAktivtGrunnlag(behandling).orElseThrow(IllegalStateException::new);
        return PersonInformasjonBuilder.oppdater(aktivtGrunnlag.getOverstyrtVersjon(),
            PersonopplysningVersjonType.OVERSTYRT);
    }


    private Optional<PersonopplysningGrunnlagEntitet> getInitiellVersjonAvPersonopplysningBehandlingsgrunnlag(
        Behandling behandling) {
        // må også sortere på id da opprettetTidspunkt kun er til nærmeste millisekund og ikke satt fra db.
        TypedQuery<PersonopplysningGrunnlagEntitet> query = entityManager.createQuery(
            "SELECT pbg FROM PersonopplysningGrunnlagEntitet pbg WHERE pbg.behandling.id = :behandling_id order by pbg.opprettetTidspunkt, pbg.id", //$NON-NLS-1$
            PersonopplysningGrunnlagEntitet.class)
            .setParameter("behandling_id", behandling.getId()); //$NON-NLS-1$

        return query.getResultList().stream().findFirst();
    }

    @Override
    public PersonopplysningGrunnlag hentFørsteVersjonAvPersonopplysninger(Behandling behandling) {
        Optional<PersonopplysningGrunnlagEntitet> optGrunnlag = getInitiellVersjonAvPersonopplysningBehandlingsgrunnlag(
            behandling);
        return optGrunnlag.orElse(null);
    }

    @Override
    public Optional<Long> hentIdPåAktivPersonopplysninger(Behandling behandling) {
        return getAktivtGrunnlag(behandling)
            .map(PersonopplysningGrunnlagEntitet::getId);
    }

    @Override
    public PersonopplysningGrunnlag hentPersonopplysningerPåId(Long aggregatId) {
        Optional<PersonopplysningGrunnlagEntitet> optGrunnlag = getVersjonAvPersonopplysningBehandlingsgrunnlagPåId(
            aggregatId);

        return optGrunnlag.orElse(null);
    }

    private Optional<PersonopplysningGrunnlagEntitet> getVersjonAvPersonopplysningBehandlingsgrunnlagPåId(
        Long aggregatId) {
        TypedQuery<PersonopplysningGrunnlagEntitet> query = entityManager.createQuery(
            "SELECT pbg FROM PersonopplysningGrunnlagEntitet pbg WHERE pbg.id = :aggregatId", //$NON-NLS-1$
            PersonopplysningGrunnlagEntitet.class)
            .setParameter("aggregatId", aggregatId); //$NON-NLS-1$

        return query.getResultList().stream().findFirst();
    }
}
