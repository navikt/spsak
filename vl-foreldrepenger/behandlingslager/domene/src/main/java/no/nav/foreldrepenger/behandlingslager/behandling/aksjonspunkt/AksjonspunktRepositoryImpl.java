package no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt;

import static no.nav.vedtak.util.Objects.check;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepositoryImpl;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Håndter all endring av aksjonspunkt.
 */
@ApplicationScoped
public class AksjonspunktRepositoryImpl implements AksjonspunktRepository {

    private static final Logger log = LoggerFactory.getLogger(AksjonspunktRepositoryImpl.class);

    private EntityManager entityManager;
    private KodeverkRepository kodeverkRepository;

    AksjonspunktRepositoryImpl() {
        // CDI
    }

    public AksjonspunktRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        if (entityManager != null) {
            this.kodeverkRepository = new KodeverkRepositoryImpl(entityManager);
        }
    }

    @Inject
    public AksjonspunktRepositoryImpl(@VLPersistenceUnit EntityManager entityManager, KodeverkRepository kodeverkRepository) {
        Objects.requireNonNull(kodeverkRepository, "kodeverkRepository");
        this.entityManager = entityManager;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public AksjonspunktDefinisjon finnAksjonspunktDefinisjon(String kode) {
        return entityManager.find(AksjonspunktDefinisjon.class, kode);
    }

    @Override
    public List<AksjonspunktDefinisjon> hentAksjonspunktDefinisjonAvType(AksjonspunktType aksjonspunktType) {
        TypedQuery<AksjonspunktDefinisjon> query = entityManager.createQuery("from AksjonspunktDef where aksjonspunktType.kode = :aksjonspunktType", AksjonspunktDefinisjon.class);
        query.setParameter("aksjonspunktType", aksjonspunktType.getKode()); // NOSONAR $NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public AksjonspunktStatus finnAksjonspunktStatus(String kode) {
        return kodeverkRepository.finn(AksjonspunktStatus.class, kode);
    }

    @Override
    public Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                            BehandlingStegType behandlingStegType) {
        Objects.requireNonNull(behandlingStegType, "behandlingStegType");
        return leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, Optional.of(behandlingStegType), Optional.empty(), Optional.empty(),
            Optional.empty());
    }

    @Override
    public Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty());
    }

    private Aksjonspunkt leggTilAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                             Optional<BehandlingStegType> behandlingStegType, Optional<LocalDateTime> frist, Optional<Venteårsak> venteÅrsak,
                                             Optional<Boolean> toTrinnskontroll) {
        // sjekk at alle parametere er spesifisert
        Objects.requireNonNull(behandling, "behandling");
        Objects.requireNonNull(aksjonspunktDefinisjon, "aksjonspunktDefinisjon");
        Objects.requireNonNull(behandlingStegType, "behandlingStegType");
        Objects.requireNonNull(frist, "frist");
        Objects.requireNonNull(venteÅrsak, "venteÅrsak");
        Objects.requireNonNull(toTrinnskontroll, "toTrinnskontroll");

        // slå opp for å få riktig konfigurasjon.
        AksjonspunktDefinisjon ad = finnAksjonspunktDefinisjon(aksjonspunktDefinisjon);
        Aksjonspunkt.Builder adBuilder = behandlingStegType.isPresent()
            ? new Aksjonspunkt.Builder(ad, behandlingStegType.get())
            : new Aksjonspunkt.Builder(ad);

        if (frist.isPresent()) {
            adBuilder.medFristTid(frist.get());
        } else if (ad.getFristPeriod() != null) {
            adBuilder.medFristTid(LocalDateTime.now(FPDateUtil.getOffset()).plus(ad.getFristPeriod()));
        }

        if (venteÅrsak.isPresent()) {
            adBuilder.medVenteårsak(venteÅrsak.get());
        } else {
            adBuilder.medVenteårsak(Venteårsak.UDEFINERT);
        }

        if (ad.getAksjonspunktType() != null && ad.getAksjonspunktType().erOverstyringpunkt()) {
            adBuilder.manueltOpprettet();
        }

        Aksjonspunkt aksjonspunkt = adBuilder.buildFor(behandling);
        log.info("Legger til aksjonspunkt: {}", ad);
        return aksjonspunkt;

    }

    @Override
    public void fjernAksjonspunkt(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        // sjekk at alle parametere er spesifisert
        Objects.requireNonNull(behandling, "behandling");
        Objects.requireNonNull(aksjonspunktDefinisjon, "aksjonspunktDefinisjon");

        AksjonspunktDefinisjon ad = finnAksjonspunktDefinisjon(aksjonspunktDefinisjon);
        log.info("Fjerner aksjonspunkt: {}", ad);
        new Aksjonspunkt.Builder(ad).medSletting().buildFor(behandling);
    }

    /**
     * @deprecated Skal ikke ha dette per Aksjonspunkt
     */
    @Deprecated
    @Override
    public void setSlettingVedRegisterinnhenting(Aksjonspunkt aksjonspunkt, boolean slettesVedRegisterinnhenting) {
        log.info("Markerer aksjonspunkt som slettesVedRegisterinnhenting: {} = {}", aksjonspunkt, slettesVedRegisterinnhenting);
        validerAktivt(aksjonspunkt);
        aksjonspunkt.setSlettesVedRegisterinnhenting(slettesVedRegisterinnhenting);
    }

    protected AksjonspunktDefinisjon finnAksjonspunktDefinisjon(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return entityManager != null ? finnAksjonspunktDefinisjon(aksjonspunktDefinisjon.getKode()) : aksjonspunktDefinisjon;
    }

    @Override
    public Aksjonspunkt settBehandlingPåVent(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjonIn,
                                             BehandlingStegType stegType,
                                             LocalDateTime fristTid, Venteårsak venteårsak) {
        AksjonspunktDefinisjon aksjonspunktDefinisjon = this
            .finnAksjonspunktDefinisjon(aksjonspunktDefinisjonIn.getKode());

        log.info("Setter behandling på vent for steg={}, aksjonspunkt={}, fristTid={}, venteÅrsak={}", stegType, aksjonspunktDefinisjon, fristTid, venteårsak);

        Aksjonspunkt aksjonspunkt;
        Optional<Aksjonspunkt> eksisterendeAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);
        if (eksisterendeAksjonspunkt.isPresent()) {
            if (!eksisterendeAksjonspunkt.get().erAktivt()) {
                throw new IllegalStateException("Kan ikke sette på vent - har eksisterende inaktivt aksjonspunkt.");
            }
            // håndter har allerede angit aksjonpunkt, oppdaterer
            aksjonspunkt = eksisterendeAksjonspunkt.get();
            if (!aksjonspunkt.erOpprettet()) {
                this.setReåpnet(aksjonspunkt);
            }
            this.setFrist(aksjonspunkt, fristTid, venteårsak);
        } else {
            // nytt aksjonspunkt
            aksjonspunkt = this.leggTilAksjonspunkt(behandling, aksjonspunktDefinisjon, Optional.empty(),
                Optional.ofNullable(fristTid), Optional.ofNullable(venteårsak), Optional.empty());
        }
        aksjonspunkt.setBehandlingSteg(stegType);
        return aksjonspunkt;
    }

    @Override
    public void setToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt) {
        validerAktivt(aksjonspunkt);
        aksjonspunkt.settToTrinnsFlag();
    }

    @Override
    public void fjernToTrinnsBehandlingKreves(Aksjonspunkt aksjonspunkt) {
        validerAktivt(aksjonspunkt);
        aksjonspunkt.fjernToTrinnsFlagg();
    }


    @Override
    public boolean setTilUtført(Aksjonspunkt aksjonspunkt, String begrunnelse) {
        validerAktivt(aksjonspunkt);
        log.info("Setter aksjonspunkt utført: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        return aksjonspunkt.setStatus(finnAksjonspunktStatus(AksjonspunktStatus.UTFØRT), begrunnelse);
    }

    @Override
    public void deaktiver(Aksjonspunkt aksjonspunkt) {
        validerAktivt(aksjonspunkt);
        log.info("Deaktiverer aksjonspunkt: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setAktivStatus(ReaktiveringStatus.INAKTIV);
    }

    @Override
    public void settInaktivSomSlettet(Aksjonspunkt aksjonspunkt) {
        validerInaktivt(aksjonspunkt);
        log.info("Deaktiverer aksjonspunkt som slettet: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setAktivStatus(ReaktiveringStatus.SLETTET);
    }

    @Override
    public void reaktiver(Aksjonspunkt aksjonspunkt) {
        check(!aksjonspunkt.erAktivt(), "kan ikke reaktivere et aktivt aksjonspunkt"); //$NON-NLS-1$
        log.info("Reaktiverer aksjonspunkt: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setAktivStatus(ReaktiveringStatus.AKTIV);
    }

    private AksjonspunktStatus finnAksjonspunktStatus(AksjonspunktStatus ap) {
        return entityManager != null ? finnAksjonspunktStatus(ap.getKode()) : ap;
    }

    @Override
    public void setTilAvbrutt(Aksjonspunkt aksjonspunkt) {
        validerAktivt(aksjonspunkt);
        log.info("Setter aksjonspunkt avbrutt: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setStatus(finnAksjonspunktStatus(AksjonspunktStatus.AVBRUTT), aksjonspunkt.getBegrunnelse());
    }

    @Override
    public void setReåpnet(Aksjonspunkt aksjonspunkt) {
        validerAktivt(aksjonspunkt);
        log.info("Setter aksjonspunkt reåpnet: {}", aksjonspunkt.getAksjonspunktDefinisjon());
        aksjonspunkt.setStatus(finnAksjonspunktStatus(AksjonspunktStatus.OPPRETTET), aksjonspunkt.getBegrunnelse());
    }

    @Override
    public void setTilManueltOpprettet(Aksjonspunkt aksjonspunkt) {
        validerAktivt(aksjonspunkt);
        aksjonspunkt.setManueltOpprettet(true);
    }

    @Override
    public void setFrist(Aksjonspunkt ap, LocalDateTime fristTid, Venteårsak venteårsak) {
        validerAktivt(ap);
        ap.setFristTid(fristTid);
        ap.setVenteårsak(venteårsak);
    }

    @Override
    public void setPeriode(Aksjonspunkt ap, DatoIntervallEntitet periode) {
        validerAktivt(ap);
        ap.setPeriode(periode);
    }

    @Override
    public void setToTrinnsBehandlingKreves(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        Aksjonspunkt aksjonspunkt = behandling.getAksjonspunktFor(aksjonspunktDefinisjon);
        if (!aksjonspunkt.isToTrinnsBehandling()) {
            if(!aksjonspunkt.erÅpentAksjonspunkt()) {
                setReåpnet(aksjonspunkt);
            }
            log.info("Setter totrinnskontroll kreves for aksjonspunkt: {}", aksjonspunkt.getAksjonspunktDefinisjon());
            setToTrinnsBehandlingKreves(aksjonspunkt);
        }
    }

    @Override
    public boolean sjekkErBegrunnelseForAksjonspunktEndret(Behandling behandling, AksjonspunktDefinisjon aksjonspunktDefinisjon,
                                                           String begrunnelse) {
        Optional<Aksjonspunkt> funnetAksjonspunkt = behandling.getAksjonspunktMedDefinisjonOptional(aksjonspunktDefinisjon);
        if (funnetAksjonspunkt.isPresent()) {
            Aksjonspunkt aksjonspunkt = funnetAksjonspunkt.get();
            String opprinneligBegrunnelse = aksjonspunkt.getBegrunnelse();
            if (!Objects.equals(opprinneligBegrunnelse, begrunnelse)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void kopierAlleAksjonspunkterOgSettDemInaktive(Behandling opprinneligBehandling, Behandling nyBehandling) {
        Set<Aksjonspunkt> opprinneligeAksjonspunkter = opprinneligBehandling.getAlleAksjonspunkterInklInaktive();
        for (Aksjonspunkt opprinneligAksjonspunkt : opprinneligeAksjonspunkter) {
            if (aksjonspunktSkalKopieres(opprinneligAksjonspunkt)) {
                new Aksjonspunkt.Builder(opprinneligAksjonspunkt)
                    .medReaktiveringsstatus(ReaktiveringStatus.INAKTIV)
                    .medRevurdering()
                    .medTotrinnskontroll(opprinneligAksjonspunkt.getAksjonspunktDefinisjon().getDefaultTotrinnBehandling())
                    .buildFor(nyBehandling);
            }
        }
    }

    private boolean aksjonspunktSkalKopieres(Aksjonspunkt aksjonspunkt) {
        return !aksjonspunkt.erAvbrutt()
            && !aksjonspunkt.getAksjonspunktDefinisjon().getAksjonspunktType().erAutopunkt()
            && !aksjonspunkt.getReaktiveringStatus().equals(ReaktiveringStatus.SLETTET);
    }

    private static void validerAktivt(Aksjonspunkt aksjonspunkt) {
        check(aksjonspunkt.erAktivt(), "Operasjonen er ikke tillatt på et inaktivt aksjonspunkt"); //$NON-NLS-1$
    }

    private static void validerInaktivt(Aksjonspunkt aksjonspunkt) {
        check(aksjonspunkt.getReaktiveringStatus().equals(ReaktiveringStatus.INAKTIV), "Operasjonen er ikke tillatt på et inaktivt aksjonspunkt"); //$NON-NLS-1$
    }
}
