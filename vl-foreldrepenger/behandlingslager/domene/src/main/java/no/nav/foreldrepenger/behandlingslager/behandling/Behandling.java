package no.nav.foreldrepenger.behandlingslager.behandling;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.OrganisasjonsEnhet;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Aksjonspunkt;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktType;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.Venteårsak;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurderingResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.klage.KlageVurdertAv;
import no.nav.foreldrepenger.behandlingslager.behandling.oppgave.OppgaveÅrsak;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Vilkår;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.hendelser.StartpunktType;
import no.nav.foreldrepenger.behandlingslager.pip.PipBehandlingsData;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.util.FPDateUtil;

@SqlResultSetMapping(name = "PipDataResult", classes = {
        @ConstructorResult(targetClass = PipBehandlingsData.class, columns = {
                @ColumnResult(name = "behandligStatus"),
                @ColumnResult(name = "ansvarligSaksbehandler"),
                @ColumnResult(name = "fagsakId"),
                @ColumnResult(name = "fagsakStatus")
        })
})
@Entity(name = "Behandling")
@Table(name = "BEHANDLING")
public class Behandling extends BaseEntitet {

    private static final Comparator<BaseEntitet> COMPARATOR_OPPRETTET_TID = Comparator
        .comparing(BaseEntitet::getOpprettetTidspunkt, (a, b) -> {
            if (a != null && b != null) {
                return a.compareTo(b);
            } else if (a == null && b == null) {
                return 0;
            } else {
                return a == null ? -1 : 1;
            }
        });

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fagsak_id", nullable = false, updatable = false)
    private Fagsak fagsak;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_status", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingStatus.DISCRIMINATOR + "'"))
    private BehandlingStatus status = BehandlingStatus.OPPRETTET;

    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, mappedBy = "behandling")
    private List<BehandlingStegTilstand> behandlingStegTilstander = new ArrayList<>(1);

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "behandling_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + BehandlingType.DISCRIMINATOR + "'"))
    private BehandlingType behandlingType = BehandlingType.UDEFINERT;

    /**
     * Er egentlig OneToOne, men må mappes slik da JPA/Hibernate ikke støtter OneToOne på annet enn shared PK.
     */
    @OneToMany(mappedBy = "behandling")
    private Set<Behandlingsresultat> behandlingsresultat = new HashSet<>(1);


    // CascadeType.ALL + orphanRemoval=true må til for at aksjonspunkter skal bli slettet fra databasen ved fjerning fra HashSet
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "behandling", orphanRemoval = true, cascade = CascadeType.ALL, targetEntity = Aksjonspunkt.class)
    private Set<Aksjonspunkt> aksjonspunkter = new HashSet<>();

    @OneToMany(mappedBy = "behandling")
    private Set<BehandlingÅrsak> behandlingÅrsaker = new HashSet<>(1);

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "startpunkt_type", nullable = false)
    private StartpunktType startpunkt = StartpunktType.UDEFINERT;

    /**
     * Er egentlig OneToOne, men må mappes slik da JPA/Hibernate ikke støtter OneToOne på annet enn shared PK.
     */
    // FIXME: FLytt denne ut av Behandling
    @OneToMany(mappedBy = "behandling")
    private Set<InnsynEntitet> innsynEntitet = new HashSet<>(1);

    // FIXME: FLytt denne ut av Behandling
    @OneToMany(mappedBy = "behandling")
    private Set<KlageVurderingResultat> klageVurderingResultat = new HashSet<>(1);

    /**
     * --------------------------------------------------------------
     * FIXME: Produksjonstyringsinformasjon bør flyttes ut av Behandling klassen.
     * Gjelder feltene under
     * --------------------------------------------------------------
     */
    @Column(name = "opprettet_dato", nullable = false, updatable = false)
    private LocalDateTime opprettetDato;

    @Column(name = "avsluttet_dato")
    private LocalDateTime avsluttetDato;

    @Column(name = "totrinnsbehandling", nullable = false)
    @Convert(converter = BooleanToStringConverter.class)
    private boolean toTrinnsBehandling = false;

    @Column(name = "ansvarlig_saksbehandler")
    private String ansvarligSaksbehandler;

    @Column(name = "ansvarlig_beslutter")
    private String ansvarligBeslutter;

    @Column(name = "behandlende_enhet")
    private String behandlendeEnhet;

    @Column(name = "behandlende_enhet_navn")
    private String behandlendeEnhetNavn;

    @Column(name = "behandlende_enhet_arsak")
    private String behandlendeEnhetÅrsak;

    @Column(name = "behandlingstid_frist", nullable = false)
    private LocalDate behandlingstidFrist;


    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aapnet_for_endring", nullable = false)
    private boolean åpnetForEndring = false;

    Behandling() {
        // Hibernate
    }

    private Behandling(Fagsak fagsak, BehandlingType type) {
        Objects.requireNonNull(fagsak, "Behandling må tilknyttes parent Fagsak"); //$NON-NLS-1$
        this.fagsak = fagsak;
        if (type != null) {
            this.behandlingType = type;
        }
    }

    /**
     * Skal kun brukes av BehandlingskontrollTjeneste for prod kode slik at events fyres.
     * <p>
     * Denne oppretter en Builder for å bygge en {@link Behandling}.
     *
     * <h4>NB! BRUKES VED FØRSTE FØRSTEGANGSBEHANDLING</h4>
     * <h4>NB2! FOR TESTER - FORTREKK (ScenarioMorSøkerEngangsstønad) eller (ScenarioFarSøkerEngangsstønad). De
     * forenkler
     * test oppsett</h4>
     * <p>
     * Ved senere behandlinger på samme Fagsak, bruk {@link #fraTidligereBehandling(Behandling, BehandlingType)}.
     */
    public static Behandling.Builder forFørstegangssøknad(Fagsak fagsak) {
        return nyBehandlingFor(fagsak, BehandlingType.FØRSTEGANGSSØKNAD);
    }

    /**
     * @deprecated Ikke bruk, man klager kun på behandling - ikke direkte på fagsak.
     */
    @Deprecated
    public static Behandling.Builder forKlage(Fagsak fagsak) {
        return nyBehandlingFor(fagsak, BehandlingType.KLAGE);
    }

    /**
     * Skal kun brukes av BehandlingskontrollTjeneste for prod kode slik at events fyres.
     *
     * @see #forFørstegangssøknad(Fagsak)
     */
    public static Builder nyBehandlingFor(Fagsak fagsak, BehandlingType behandlingType) {
        return new Builder(fagsak, behandlingType);
    }

    /**
     * Skal kun brukes av BehandlingskontrollTjeneste for prod kode slik at events fyres.
     * <p>
     * Denne oppretter en Builder for å bygge en {@link Behandling} basert på et eksisterende behandling.
     * <p>
     * Ved Endringssøknad eller REVURD_OPPR er det normalt DENNE som skal brukes.
     * <p>
     * NB! FOR TESTER - FORTREKK (ScenarioMorSøkerEngangsstønad) eller (ScenarioFarSøkerEngangsstønad). De forenkler
     * test oppsett basert på vanlige defaults.
     */
    public static Behandling.Builder fraTidligereBehandling(Behandling forrigeBehandling, BehandlingType behandlingType) {
        return new Builder(forrigeBehandling, behandlingType);
    }

    /** 
     * @deprecated FIXME PFP-1131 Fjern direkte kobling Behandling->Behandlingsresultat fra entiteter/jpa modell
     */
    @Deprecated
    // (FC) støtter bare ett Behandlingsresultat for en Behandling - JPA har ikke støtte for OneToOne på non-PK
    // kolonne, så emuleres her ved å tømme listen.
    public Behandlingsresultat getBehandlingsresultat() {
        if (this.behandlingsresultat.size() > 1) {
            throw FeilFactory.create(BehandlingFeil.class).merEnnEttBehandlingsresultat(behandlingsresultat.size()).toException();
        }
        return this.behandlingsresultat.isEmpty() ? null : this.behandlingsresultat.iterator().next();
    }

    public List<BehandlingÅrsak> getBehandlingÅrsaker() {
        return new ArrayList<>(behandlingÅrsaker);
    }

    void leggTilBehandlingÅrsaker(List<BehandlingÅrsak> behandlingÅrsaker) {
        if (erAvsluttet() && erHenlagt()) {
            throw new IllegalStateException("Utvikler-feil: kan ikke legge til årsaker på en behandling som er avsluttet.");
        }
        behandlingÅrsaker.forEach(bå -> {
            bå.setBehandling(this);
            this.behandlingÅrsaker.add(bå);
        });
    }

    public boolean harBehandlingÅrsak(BehandlingÅrsakType behandlingÅrsak) {
        return getBehandlingÅrsaker().stream()
            .map(BehandlingÅrsak::getBehandlingÅrsakType)
            .collect(Collectors.toList())
            .contains(behandlingÅrsak);
    }

    public Optional<Behandling> getOriginalBehandling() {
        return getBehandlingÅrsaker().stream()
            .filter(Objects::nonNull)
            .map(BehandlingÅrsak::getOriginalBehandling)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    /** 
     * @deprecated FIXME PFP-1131 Fjern direkte kobling Behandling->Berørt Behandling
     */
    @Deprecated
    public Optional<Behandling> getBerørtBehandling() {
        return getBehandlingÅrsaker().stream()
            .filter(Objects::nonNull)
            .map(BehandlingÅrsak::getBerørtBehandling)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    /** 
     * @deprecated FIXME PFP-1131 Fjern direkte kobling Behandling->Berørt Behandling
     */
    @Deprecated
    public boolean erBerørtBehandling() {
        return getBehandlingÅrsaker().stream()
            .map(BehandlingÅrsak::getBehandlingÅrsakType)
            .collect(Collectors.toList())
            .contains(BehandlingÅrsakType.BERØRT_BEHANDLING);
    }

    public boolean erManueltOpprettet() {
        return getBehandlingÅrsaker().stream()
            .map(BehandlingÅrsak::erManueltOpprettet)
            .collect(Collectors.toList())
            .contains(true);
    }

    public boolean erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType behandlingÅrsak) {
        return erManueltOpprettet() && harBehandlingÅrsak(behandlingÅrsak);
    }

    public Optional<KlageVurderingResultat> hentGjeldendeKlageVurderingResultat() {
        /**
         *  FIXME: Bør aksesseres gjennom et repository, ikke ligge på Behandling
         *  @deprecated Fjern denne
         */
        @Deprecated
        Optional<KlageVurderingResultat> klageVurderingResultatNK = klageVurderingResultat.stream()
            .filter(kvr -> KlageVurdertAv.NK.equals(kvr.getKlageVurdertAv()))
            .findFirst();

        Optional<KlageVurderingResultat> klageVurderingResultatNFP = klageVurderingResultat.stream()
            .filter(krv -> KlageVurdertAv.NFP.equals(krv.getKlageVurdertAv()))
            .findFirst();

        if (klageVurderingResultatNK.isPresent()) {
            return klageVurderingResultatNK;
        }
        return klageVurderingResultatNFP;
    }

    /**
     *  FIXME: Bør aksesseres gjennom et repository, ikke ligge på Behandling
     *  @deprecated Fjern denne
     */
    @Deprecated
    public Optional<KlageVurderingResultat> hentKlageVurderingResultat(KlageVurdertAv klageVurdertAv) {
        return klageVurderingResultat.stream()
            .filter(krv -> klageVurdertAv.equals(krv.getKlageVurdertAv()))
            .findFirst();
    }

    public Long getId() {
        return id;
    }

    public Long getFagsakId() {
        return getFagsak().getId();
    }

    public AktørId getAktørId() {
        return getFagsak().getNavBruker().getAktørId();
    }

    public BehandlingStatus getStatus() {
        return status;
    }

    /**
     * Oppdater behandlingssteg og tilhørende status.
     * <p>
     * NB::NB::NB Dette skal normalt kun gjøres fra Behandlingskontroll slik at bokføring og events blir riktig.
     * Er ikke en del av offentlig API.
     *
     * @param oppdatertTilstand - tilstand for steg behandlingen er i
     * @param sluttStatusForEksisterendeSteg - avslutt eksisterende åpne steg og sett til denne statusen.
     */
    void oppdaterBehandlingStegOgStatus(BehandlingStegTilstand oppdatertTilstand,
                                        BehandlingStegStatus sluttStatusForEksisterendeSteg) {
        Objects.requireNonNull(oppdatertTilstand, "behandlingStegTilstand"); //$NON-NLS-1$

        this.behandlingStegTilstander.remove(oppdatertTilstand);

        // lukk andre steg
        lukkBehandlingStegStatuser(this.behandlingStegTilstander, sluttStatusForEksisterendeSteg);

        // legg til ny
        this.behandlingStegTilstander.add(oppdatertTilstand);
        BehandlingStegType behandlingSteg = oppdatertTilstand.getBehandlingSteg();
        this.status = behandlingSteg.getDefinertBehandlingStatus();
    }

    /**
     * Marker behandling som avsluttet.
     */
    public void avsluttBehandling() {
        lukkBehandlingStegStatuser(this.behandlingStegTilstander, BehandlingStegStatus.UTFØRT);
        this.status = BehandlingStatus.AVSLUTTET;
        this.avsluttetDato = LocalDateTime.now(FPDateUtil.getOffset());
    }

    private void lukkBehandlingStegStatuser(Collection<BehandlingStegTilstand> stegTilstander, BehandlingStegStatus sluttStatusForSteg) {
        stegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getBehandlingStegStatus()))
            .forEach(t -> t.setBehandlingStegStatus(sluttStatusForSteg));
    }

    public BehandlingType getType() {
        return behandlingType;
    }

    public LocalDateTime getOpprettetDato() {
        return opprettetDato;
    }

    public LocalDateTime getAvsluttetDato() {
        return avsluttetDato;
    }

    public LocalDate getBehandlingstidFrist() {
        return behandlingstidFrist;
    }

    public void setBehandlingstidFrist(LocalDate behandlingstidFrist) {
        guardTilstandPåBehandling();
        this.behandlingstidFrist = behandlingstidFrist;
    }

    public Optional<BehandlingStegTilstand> getBehandlingStegTilstand() {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getBehandlingStegStatus()))
            .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException("Utvikler-feil: Kan ikke ha flere steg samtidig åpne: " + tilstander); //$NON-NLS-1$
        }

        return tilstander.isEmpty() ? Optional.empty() : Optional.of(tilstander.get(0));
    }

    public Optional<BehandlingStegTilstand> getBehandlingStegTilstand(BehandlingStegType stegType) {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getBehandlingStegStatus())
                && Objects.equals(stegType, t.getBehandlingSteg()))
            .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException(
                "Utvikler-feil: Kan ikke ha flere steg samtidig åpne for stegType[" + stegType + "]: " + tilstander); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return tilstander.isEmpty() ? Optional.empty() : Optional.of(tilstander.get(0));
    }

    public Stream<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk() {
        return behandlingStegTilstander.stream().sorted(COMPARATOR_OPPRETTET_TID);
    }

    public BehandlingStegType getAktivtBehandlingSteg() {
        BehandlingStegTilstand stegTilstand = getBehandlingStegTilstand().orElse(null);
        return stegTilstand == null ? null : stegTilstand.getBehandlingSteg();
    }

    /**
     * @deprecated FIXME skal ikke ha public settere, og heller ikke setter for behandlingsresultat her. Bør gå via repository.
     */
    @Deprecated
    public void setBehandlingresultat(Behandlingsresultat behandlingsresultat) {
        // (FC) støtter bare ett Behandlingsresultat for en Behandling - JPA har ikke støtte for OneToOne på non-PK
        // kolonne, så emuleres her ved å tømme listen.

        this.behandlingsresultat.clear();
        behandlingsresultat.setBehandling(this);
        // kun ett om gangen, mappet på annet enn pk
        this.behandlingsresultat.add(behandlingsresultat);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Behandling)) {
            return false;
        }
        Behandling other = (Behandling) object;
        return Objects.equals(getFagsak(), other.getFagsak())
            && Objects.equals(getType(), other.getType())
            && Objects.equals(getOpprettetTidspunkt(), other.getOpprettetTidspunkt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFagsak(), getType(), getOpprettetTidspunkt());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" //$NON-NLS-1$
            + (id != null ? "id=" + id + ", " : "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + "fagsak=" + fagsak + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "status=" + status + ", " //$NON-NLS-1$ //$NON-NLS-2$
            + "type=" + behandlingType + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "steg=" + (getBehandlingStegTilstand().orElse(null)) + "," //$NON-NLS-1$ //$NON-NLS-2$
            + "opprettetTs=" + getOpprettetTidspunkt() //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }

    public NavBruker getNavBruker() {
        return getFagsak().getNavBruker();
    }

    public RelasjonsRolleType getRelasjonsRolleType() {
        return getFagsak().getRelasjonsRolleType();
    }

    public String getBehandlendeEnhetÅrsak() {
        return behandlendeEnhetÅrsak;
    }

    public void setBehandlendeEnhetÅrsak(String behandlendeEnhetÅrsak) {
        guardTilstandPåBehandling();
        this.behandlendeEnhetÅrsak = behandlendeEnhetÅrsak;
    }

    public String getBehandlendeEnhet() {
        return behandlendeEnhet;
    }

    public OrganisasjonsEnhet getBehandlendeOrganisasjonsEnhet() {
        return new OrganisasjonsEnhet(behandlendeEnhet, behandlendeEnhetNavn);
    }

    public void setBehandlendeEnhet(OrganisasjonsEnhet enhet) {
        guardTilstandPåBehandling();
        this.behandlendeEnhet = enhet.getEnhetId();
        this.behandlendeEnhetNavn = enhet.getEnhetNavn();
    }

    public Fagsak getFagsak() {
        return fagsak;
    }

    /**
     * Internt API, IKKE BRUK.
     */
    void addAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        aksjonspunkter.add(aksjonspunkt);
    }

    /**
     * Internt API, IKKE BRUK.
     */
    void fjernAksjonspunkt(Aksjonspunkt aksjonspunkt) {
        Set<Aksjonspunkt> beholdes = aksjonspunkter.stream()
            .filter(ap -> !ap.getAksjonspunktDefinisjon().getKode()
                .equals(aksjonspunkt.getAksjonspunktDefinisjon().getKode()))
            .collect(toSet());
        aksjonspunkter.clear();
        aksjonspunkter.addAll(beholdes);
    }

    public Set<Aksjonspunkt> getAksjonspunkter() {
        return getAksjonspunkterStream()
            .collect(toSet());
    }

    public Optional<Aksjonspunkt> getAksjonspunktMedDefinisjonOptional(AksjonspunktDefinisjon definisjon) {
        return getAksjonspunkterStream()
            .filter(a -> a.getAksjonspunktDefinisjon().equals(definisjon))
            .findFirst();
    }

    public Aksjonspunkt getAksjonspunktFor(AksjonspunktDefinisjon definisjon) {
        return getAksjonspunkterStream()
            .filter(a -> a.getAksjonspunktDefinisjon().equals(definisjon))
            .findFirst()
            .orElseThrow(() -> FeilFactory.create(BehandlingFeil.class).aksjonspunktIkkeFunnet(definisjon.getKode()).toException());
    }

    public List<Aksjonspunkt> getÅpneAksjonspunkter() {
        return getÅpneAksjonspunkterStream()
            .collect(Collectors.toList());
    }

    public Set<Aksjonspunkt> getAlleAksjonspunkterInklInaktive() {
        return aksjonspunkter;
    }

    public List<Aksjonspunkt> getBehandledeAksjonspunkter() {
        return getAksjonspunkterStream()
            .filter(Aksjonspunkt::erBehandletAksjonspunkt)
            .collect(Collectors.toList());
    }

    public List<Aksjonspunkt> getÅpneAksjonspunkter(AksjonspunktType aksjonspunktType) {
        return getÅpneAksjonspunkterStream()
            .filter(ad -> Objects.equals(aksjonspunktType, ad.getAksjonspunktDefinisjon().getAksjonspunktType()))
            .collect(Collectors.toList());
    }

    public List<Aksjonspunkt> getÅpneAksjonspunkter(Collection<AksjonspunktDefinisjon> matchKriterier) {
        return getÅpneAksjonspunkterStream()
            .filter(a -> matchKriterier.contains(a.getAksjonspunktDefinisjon()))
            .collect(Collectors.toList());
    }

    public List<Aksjonspunkt> getAksjonspunkterMedTotrinnskontroll() {
        return getAksjonspunkterStream()
            .filter(a -> !a.erAvbrutt() && a.isToTrinnsBehandling())
            .collect(Collectors.toList());
    }

    public List<Aksjonspunkt> getAksjonspunkterMedTotrinnskontrollInkludertAvbrutte() {
        return getAksjonspunkterStream()
            .filter(a -> a.isToTrinnsBehandling())
            .collect(Collectors.toList());
    }

    public boolean harAksjonspunktMedType(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return getAksjonspunkterStream()
            .anyMatch(ap -> aksjonspunktDefinisjon.equals(ap.getAksjonspunktDefinisjon()));
    }

    public boolean harÅpentAksjonspunktMedType(AksjonspunktDefinisjon aksjonspunktDefinisjon) {
        return getAksjonspunkterStream()
            .anyMatch(ap -> !ap.erBehandletAksjonspunkt() && !ap.erAvbrutt() && aksjonspunktDefinisjon.equals(ap.getAksjonspunktDefinisjon()));
    }

    public boolean harAksjonspunktMedTotrinnskontroll() {
        return getAksjonspunkterStream()
            .anyMatch(a -> !a.erAvbrutt() && a.isToTrinnsBehandling());
    }

    private Optional<Aksjonspunkt> getFørsteÅpneAutopunkt() {
        return getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).stream()
            .findFirst();
    }

    public boolean isBehandlingPåVent() {
        return !getÅpneAksjonspunkter(AksjonspunktType.AUTOPUNKT).isEmpty();
    }

    public boolean erKøet() {
        return this.getÅpneAksjonspunkterStream()
            .anyMatch(ap -> AksjonspunktDefinisjon.AUTO_KØET_BEHANDLING.equals(ap.getAksjonspunktDefinisjon()));
    }

    private Stream<Aksjonspunkt> getAksjonspunkterStream() {
        return aksjonspunkter.stream()
            .filter(Aksjonspunkt::erAktivt);
    }

    private Stream<Aksjonspunkt> getÅpneAksjonspunkterStream() {
        return getAksjonspunkterStream()
            .filter(Aksjonspunkt::erÅpentAksjonspunkt);
    }

    public Long getVersjon() {
        return versjon;
    }

    public BehandlingStegStatus getBehandlingStegStatus() {
        BehandlingStegTilstand stegTilstand = getBehandlingStegTilstand().orElse(null);
        return stegTilstand == null ? null : stegTilstand.getBehandlingStegStatus();
    }

    public boolean isToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    public void setToTrinnsBehandling() {
        guardTilstandPåBehandling();
        this.toTrinnsBehandling = true;
    }

    public void nullstillToTrinnsBehandling() {
        guardTilstandPåBehandling();
        this.toTrinnsBehandling = false;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        guardTilstandPåBehandling();
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public String getAnsvarligBeslutter() {
        return ansvarligBeslutter;
    }

    public void setAnsvarligBeslutter(String ansvarligBeslutter) {
        guardTilstandPåBehandling();
        this.ansvarligBeslutter = ansvarligBeslutter;
    }

    public boolean isBehandlingHenlagt() {
        if (behandlingsresultat == null || behandlingsresultat.isEmpty()) {
            return false;
        }
        return getBehandlingsresultat().isBehandlingHenlagt();
    }

    public LocalDate getOriginalVedtaksDato() {
        Behandlingsresultat originaltBehandlingsResultat = getBehandlingsresultat();
        if (originaltBehandlingsResultat == null || originaltBehandlingsResultat.getBehandlingVedtak() == null) {
            return null;
        }
        return getBehandlingsresultat().getBehandlingVedtak().getVedtaksdato();
    }

    public FagsakYtelseType getFagsakYtelseType() {
        return getFagsak().getYtelseType();
    }

    public LocalDate getFristDatoBehandlingPåVent() {
        Optional<Aksjonspunkt> aksjonspunkt = getFørsteÅpneAutopunkt();
        LocalDateTime fristTid = null;
        if (aksjonspunkt.isPresent()) {
            fristTid = aksjonspunkt.get().getFristTid();
        }
        return fristTid == null ? null : fristTid.toLocalDate();
    }

    public AksjonspunktDefinisjon getBehandlingPåVentAksjonspunktDefinisjon() {
        Optional<Aksjonspunkt> aksjonspunkt = getFørsteÅpneAutopunkt();
        if (aksjonspunkt.isPresent()) {
            return aksjonspunkt.get().getAksjonspunktDefinisjon();
        }
        return null;
    }

    public Venteårsak getVenteårsak() {
        Optional<Aksjonspunkt> aksjonspunkt = getFørsteÅpneAutopunkt();
        if (aksjonspunkt.isPresent()) {
            return aksjonspunkt.get().getVenteårsak();
        }
        return null;
    }

    /**
     *  FIXME: Bør aksesseres gjennom et repository, ikke ligge på Behandling
     *  @deprecated Fjern denne
     */
    @Deprecated
    public InnsynEntitet getInnsyn() {
        if (this.innsynEntitet.size() > 1) {
            throw new IllegalStateException("Utviklerfeil: Kun ett innsyn per behandling tillatt");
        }
        return this.innsynEntitet.isEmpty() ? null : this.innsynEntitet.iterator().next();
    }

    /**
     *  (essv) støtter bare ett Innsyn for en Behandling - JPA har ikke støtte for OneToOne på non-PK kolonne
     *  FIXME: Bør aksesseres gjennom et repository, ikke ligge på Behandling
     *
     *  @deprecated Fjern denne
     */
    @Deprecated
    void setInnsyn(InnsynEntitet innsynEntitet) {
        // (essv) støtter bare ett Innsyn for en Behandling - JPA har ikke støtte for OneToOne på non-PK
        // kolonne, så emuleres her ved å tømme listen.

        this.innsynEntitet.clear();
        innsynEntitet.setBehandling(this);
        // kun ett om gangen, mappet på annet enn pk
        this.innsynEntitet.add(innsynEntitet);
    }

    public boolean erSaksbehandlingAvsluttet() {
        if (behandlingsresultat == null || behandlingsresultat.isEmpty()) {
            return false;
        }
        return erAvsluttet() || erUnderIverksettelse() || erHenlagt();
    }

    private boolean erHenlagt() {
        return getBehandlingsresultat().isBehandlingHenlagt();
    }

    public boolean erUnderIverksettelse() {
        return Objects.equals(BehandlingStatus.IVERKSETTER_VEDTAK, getStatus());
    }

    public boolean erAvsluttet() {
        return Objects.equals(BehandlingStatus.AVSLUTTET, getStatus());
    }

    public boolean erKlage() {
        return BehandlingType.KLAGE.equals(getType());
    }

    public boolean erRevurdering() {
        return BehandlingType.REVURDERING.equals(getType());
    }

    public boolean erRevurderingOgGjelderForeldrepengerYtelse() {
        return erRevurdering() && getFagsakYtelseType().gjelderForeldrepenger();
    }

    public boolean erInnsyn() {
        return BehandlingType.INNSYN.equals(getType());
    }

    public boolean erYtelseBehandling() {
        return (!BehandlingType.INNSYN.equals(getType()) && !BehandlingType.KLAGE.equals(getType()));
    }

    /**
     *  FIXME: Bør aksesseres gjennom et repository, ikke ligge på Behandling
     *  @deprecated Fjern denne
     */
    @Deprecated
    public void leggTilKlageVurderingResultat(KlageVurderingResultat klageVurderingResultat) {
        guardTilstandPåBehandling();
        this.klageVurderingResultat.add(klageVurderingResultat);
    }

    public OppgaveÅrsak getBehandleOppgaveÅrsak() {
        return erRevurdering() ? OppgaveÅrsak.REVURDER : OppgaveÅrsak.BEHANDLE_SAK;
    }

    public Optional<VilkårType> getVilkårTypeForRelasjonTilBarnet() {
        Behandlingsresultat resultat = getBehandlingsresultat();
        if (resultat == null) {
            return Optional.empty();
        }
        VilkårResultat vilkårResultat = resultat.getVilkårResultat();
        if (vilkårResultat == null) {
            return Optional.empty();
        }
        List<VilkårType> vilkårTyper = asList(VilkårType.FØDSELSVILKÅRET_MOR, VilkårType.FØDSELSVILKÅRET_FAR_MEDMOR,
            VilkårType.ADOPSJONSVILKÅRET_ENGANGSSTØNAD, VilkårType.ADOPSJONSVILKARET_FORELDREPENGER,
            VilkårType.OMSORGSVILKÅRET, VilkårType.FORELDREANSVARSVILKÅRET_2_LEDD, VilkårType.FORELDREANSVARSVILKÅRET_4_LEDD);

        return vilkårResultat.getVilkårene().stream()
            .filter(v -> vilkårTyper.contains(v.getVilkårType()))
            .findFirst()
            .map(Vilkår::getVilkårType);
    }

    public StartpunktType getStartpunkt() {
        return startpunkt;
    }

    public void setStartpunkt(StartpunktType startpunkt) {
        guardTilstandPåBehandling();
        this.startpunkt = startpunkt;
    }

    public boolean erÅpnetForEndring() {
        return åpnetForEndring;
    }

    public void setÅpnetForEndring(boolean åpnetForEndring) {
        guardTilstandPåBehandling();
        this.åpnetForEndring = åpnetForEndring;
    }

    private void guardTilstandPåBehandling() {
        if (erSaksbehandlingAvsluttet()) {
            throw new IllegalStateException("Utvikler-feil: kan ikke endre tilstand på en behandling som er avsluttet.");
        }
    }
    
    @PreRemove
    protected void onDelete() {
     // FIXME: FPFEIL-2799 (FrodeC): Fjern denne når FPFEIL-2799 er godkjent
        throw new IllegalStateException("Skal aldri kunne slette behandling. [id=" + id + ", status=" + getStatus() + ", type=" + getType() + "]");
    }

    public static class Builder {

        private final BehandlingType behandlingType;
        private Fagsak fagsak;
        private Behandling forrigeBehandling;
        /**
         * optional
         */
        private Behandlingsresultat.Builder resultatBuilder;

        private LocalDateTime opprettetDato;
        private LocalDateTime avsluttetDato;

        private String behandlendeEnhet;
        private String behandlendeEnhetNavn;
        private String behandlendeEnhetÅrsak;

        private LocalDate behandlingstidFrist = LocalDate.now(FPDateUtil.getOffset()).plusWeeks(6);

        private BehandlingÅrsak.Builder behandlingÅrsakBuilder;

        private Builder(Fagsak fagsak, BehandlingType behandlingType) {
            this(behandlingType);
            Objects.requireNonNull(fagsak, "fagsak"); //$NON-NLS-1$
            this.fagsak = fagsak;
        }

        private Builder(Behandling forrigeBehandling, BehandlingType behandlingType) {
            this(behandlingType);
            this.forrigeBehandling = forrigeBehandling;
        }

        private Builder(BehandlingType behandlingType) {
            Objects.requireNonNull(behandlingType, "behandlingType"); //$NON-NLS-1$
            this.behandlingType = behandlingType;
        }

        public Builder medKopiAvForrigeBehandlingsresultat() {
            Behandlingsresultat behandlingsresultatForrige = forrigeBehandling.getBehandlingsresultat();
            this.resultatBuilder = Behandlingsresultat.builderFraEksisterende(behandlingsresultatForrige);
            return this;
        }

        public Builder medBehandlingÅrsak(BehandlingÅrsak.Builder årsakBuilder) {
            this.behandlingÅrsakBuilder = årsakBuilder;
            return this;
        }

        /**
         * Fix opprettet dato.
         */
        public Builder medOpprettetDato(LocalDateTime tid) {
            this.opprettetDato = tid;
            return this;
        }

        /**
         * Fix avsluttet dato.
         */
        public Builder medAvsluttetDato(LocalDateTime tid) {
            this.avsluttetDato = tid;
            return this;
        }

        public Builder medBehandlendeEnhet(OrganisasjonsEnhet enhet) {
            this.behandlendeEnhet = enhet.getEnhetId();
            this.behandlendeEnhetNavn = enhet.getEnhetNavn();
            return this;
        }

        public Builder medBehandlendeEnhetÅrsak(String behandlendeEnhetÅrsak) {
            this.behandlendeEnhetÅrsak = behandlendeEnhetÅrsak;
            return this;
        }

        public Builder medBehandlingstidFrist(LocalDate frist) {
            this.behandlingstidFrist = frist;
            return this;
        }

        /**
         * Bygger en Behandling.
         * <p>
         * Husk: Har du brukt riktig Factory metode for å lage en Builder? :
         * <ul>
         * <li>{@link Behandling#fraTidligereBehandling(Behandling, BehandlingType)} (&lt;- BRUK DENNE HVIS DET ER
         * TIDLIGERE BEHANDLINGER PÅ SAMME FAGSAK)</li>
         * <li>{@link Behandling#forFørstegangssøknad(Fagsak)}</li>
         * </ul>
         */
        public Behandling build() {
            Behandling behandling;

            if (forrigeBehandling != null) {
                behandling = new Behandling(forrigeBehandling.getFagsak(), behandlingType);
                behandling.behandlendeEnhet = forrigeBehandling.behandlendeEnhet;
                behandling.behandlendeEnhetNavn = forrigeBehandling.behandlendeEnhetNavn;
                behandling.behandlendeEnhetÅrsak = forrigeBehandling.behandlendeEnhetÅrsak;
                behandling.behandlingstidFrist = forrigeBehandling.behandlingstidFrist;
            } else {
                behandling = new Behandling(fagsak, behandlingType);
                behandling.behandlendeEnhet = behandlendeEnhet;
                behandling.behandlendeEnhetNavn = behandlendeEnhetNavn;
                behandling.behandlendeEnhetÅrsak = behandlendeEnhetÅrsak;
                behandling.behandlingstidFrist = behandlingstidFrist;
            }

            behandling.opprettetDato = LocalDateTime.now(FPDateUtil.getOffset());
            if (opprettetDato != null) {
                behandling.opprettetDato = opprettetDato;
            }
            if (avsluttetDato != null) {
                behandling.avsluttetDato = avsluttetDato;
            }
            if (resultatBuilder != null) {
                Behandlingsresultat behandlingsresultat = resultatBuilder.buildFor(behandling);
                behandling.setBehandlingresultat(behandlingsresultat);
            }

            if (behandlingÅrsakBuilder != null) {
                behandlingÅrsakBuilder.buildFor(behandling);
            }

            return behandling;
        }
    }

}
