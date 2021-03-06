package no.nav.foreldrepenger.behandlingslager.behandling;

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
     * --------------------------------------------------------------
     * FIXME: Produksjonstyringsinformasjon bør flyttes ut av Behandling klassen.
     * Gjelder feltene under
     * --------------------------------------------------------------
     */
    @Column(name = "opprettet_dato", columnDefinition = "DATE", nullable = false, updatable = false)
    private LocalDateTime opprettetDato;

    @Column(name = "avsluttet_dato", columnDefinition = "DATE")
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


    private void guardTilstandPåBehandling() {
        if (erAvsluttet()) {
            throw new IllegalStateException("Utvikler-feil: kan ikke endre tilstand på en behandling som er avsluttet.");
        }
    }

    public Long getVersjon() {
        return versjon;
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

    public boolean erUnderIverksettelse() {
        return Objects.equals(BehandlingStatus.IVERKSETTER_VEDTAK, getStatus());
    }

    public boolean erAvsluttet() {
        return Objects.equals(BehandlingStatus.AVSLUTTET, getStatus());
    }

    public boolean erRevurdering() {
        return BehandlingType.REVURDERING.equals(getType());
    }

    public boolean erÅpnetForEndring() {
        return åpnetForEndring;
    }

    public void setÅpnetForEndring(boolean åpnetForEndring) {
        this.åpnetForEndring = åpnetForEndring;
    }

    @PreRemove
    protected void onDelete() {
        throw new IllegalStateException("Skal aldri kunne slette behandling. [id=" + id + ", status=" + getStatus() + ", type=" + getType() + "]");
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    void leggTilBehandlingÅrsaker(List<BehandlingÅrsak> behandlingÅrsaker) {
        if (erAvsluttet()) {
            throw new IllegalStateException("Utvikler-feil: kan ikke legge til årsaker på en behandling som er avsluttet.");
        }
        behandlingÅrsaker.forEach(bå -> {
            bå.setBehandling(this);
            this.behandlingÅrsaker.add(bå);
        });
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    boolean harBehandlingÅrsak(BehandlingÅrsakType behandlingÅrsak) {
        return getBehandlingÅrsaker().stream()
            .map(BehandlingÅrsak::getBehandlingÅrsakType)
            .collect(Collectors.toList())
            .contains(behandlingÅrsak);
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval=true)
    public Optional<Behandling> getOriginalBehandling() {
        return getBehandlingÅrsaker().stream()
            .filter(Objects::nonNull)
            .map(BehandlingÅrsak::getOriginalBehandling)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval=true)
    public boolean erManueltOpprettet() {
        return getBehandlingÅrsaker().stream()
            .map(BehandlingÅrsak::erManueltOpprettet)
            .collect(Collectors.toList())
            .contains(true);
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval=true)
    public boolean erManueltOpprettetOgHarÅrsak(BehandlingÅrsakType behandlingÅrsak) {
        return erManueltOpprettet() && harBehandlingÅrsak(behandlingÅrsak);
    }

    /**
>>>>>>> Fjerner berørt behandling (fra BehandlingÅrsak, Behandling og Kompletthetsjekker)
     * Oppdater behandlingssteg og tilhørende status.
     * <p>
     * NB::NB::NB Dette skal normalt kun gjøres fra Behandlingskontroll slik at bokføring og events blir riktig.
     * Er ikke en del av offentlig API.
     *
     * @param oppdatertTilstand - tilstand for steg behandlingen er i
     * @param sluttStatusForEksisterendeSteg - avslutt eksisterende åpne steg og sett til denne statusen.
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    void oppdaterBehandlingStegOgStatus(BehandlingStegTilstand oppdatertTilstand,
                                        BehandlingStegStatus sluttStatusForEksisterendeSteg) {
        Objects.requireNonNull(oppdatertTilstand, "behandlingStegTilstand"); //$NON-NLS-1$

        this.behandlingStegTilstander.remove(oppdatertTilstand);

        // lukk andre steg
        lukkBehandlingStegStatuser(this.behandlingStegTilstander, sluttStatusForEksisterendeSteg);

        // legg til ny
        this.behandlingStegTilstander.add(oppdatertTilstand);
        BehandlingStegType behandlingSteg = oppdatertTilstand.getStegType();
        this.status = behandlingSteg.getDefinertBehandlingStatus();
    }

    /**
     * Marker behandling som avsluttet.
     * 
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    void avsluttBehandling() {
        lukkBehandlingStegStatuser(this.behandlingStegTilstander, BehandlingStegStatus.UTFØRT);
        this.status = BehandlingStatus.AVSLUTTET;
        this.avsluttetDato = LocalDateTime.now(FPDateUtil.getOffset());
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    private void lukkBehandlingStegStatuser(Collection<BehandlingStegTilstand> stegTilstander, BehandlingStegStatus sluttStatusForSteg) {
        stegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getStatus()))
            .forEach(t -> t.setBehandlingStegStatus(sluttStatusForSteg));
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    Optional<BehandlingStegTilstand> getBehandlingStegTilstand() {
        Optional<BehandlingStegTilstand> siste = behandlingStegTilstander.stream()
            .sorted(Comparator.comparing(BehandlingStegTilstand::getOpprettetTidspunkt).reversed())
            .findFirst();

        return siste;
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public
    Optional<BehandlingStegTilstand> getBehandlingStegTilstand(BehandlingStegType stegType) {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getStatus())
                && Objects.equals(stegType, t.getStegType()))
            .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException(
                "Utvikler-feil: Kan ikke ha flere steg samtidig åpne for stegType[" + stegType + "]: " + tilstander); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return tilstander.isEmpty() ? Optional.empty() : Optional.of(tilstander.get(0));
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public
    Stream<BehandlingStegTilstand> getBehandlingStegTilstandHistorikk() {
        return behandlingStegTilstander.stream().sorted(COMPARATOR_OPPRETTET_TID);
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public List<BehandlingÅrsak> getBehandlingÅrsaker() {
        return new ArrayList<>(behandlingÅrsaker);
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public BehandlingStegStatus getBehandlingStegStatus() {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getStatus()))
            .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException("Utvikler-feil: Kan ikke ha flere steg samtidig åpne: " + tilstander); //$NON-NLS-1$
        }

        BehandlingStegTilstand stegTilstand = tilstander.isEmpty() ? null : tilstander.get(0);
        return stegTilstand == null ? null : stegTilstand.getStatus();
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public BehandlingStegType getAktivtBehandlingSteg() {
        List<BehandlingStegTilstand> tilstander = behandlingStegTilstander.stream()
            .filter(t -> !BehandlingStegStatus.erSluttStatus(t.getStatus()))
            .collect(Collectors.toList());
        if (tilstander.size() > 1) {
            throw new IllegalStateException("Utvikler-feil: Kan ikke ha flere steg samtidig åpne: " + tilstander); //$NON-NLS-1$
        }

        BehandlingStegTilstand stegTilstand = tilstander.isEmpty() ? null : tilstander.get(0);
        return stegTilstand == null ? null : stegTilstand.getStegType();
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public StartpunktType getStartpunkt() {
        return startpunkt;
    }

    /**
     * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
     */
    @Deprecated(forRemoval = true)
    public void setStartpunkt(StartpunktType startpunkt) {
        guardTilstandPåBehandling();
        this.startpunkt = startpunkt;
    }

    public static class Builder {

        private final BehandlingType behandlingType;
        private Fagsak fagsak;
        private Behandling forrigeBehandling;

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

        /**
         * @deprecated FIXME SP : Flytt til BehandlingskontrollRepository
         */
        @Deprecated(forRemoval = true)
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

            if (behandlingÅrsakBuilder != null) {
                behandlingÅrsakBuilder.buildFor(behandling);
            }

            return behandling;
        }
    }

}
