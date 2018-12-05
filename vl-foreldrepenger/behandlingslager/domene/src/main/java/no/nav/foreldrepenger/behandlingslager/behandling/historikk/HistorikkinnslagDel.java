package no.nav.foreldrepenger.behandlingslager.behandling.historikk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktDefinisjon;
import no.nav.foreldrepenger.behandlingslager.behandling.aksjonspunkt.AksjonspunktRepository;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "HistorikkinnslagDel")
@Table(name = "HISTORIKKINNSLAG_DEL")
public class HistorikkinnslagDel extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_HISTORIKKINNSLAG_DEL")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "historikkinnslag_id", nullable = false, updatable = false)
    @JsonBackReference
    private Historikkinnslag historikkinnslag;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "historikkinnslagDel")
    private List<HistorikkinnslagFelt> historikkinnslagFelt = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public Historikkinnslag getHistorikkinnslag() {
        return historikkinnslag;
    }

    public List<HistorikkinnslagFelt> getHistorikkinnslagFelt() {
        return historikkinnslagFelt;
    }

    public Optional<String> getAarsak() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.AARSAK);
    }

    public Optional<HistorikkinnslagFelt> getAarsakFelt() {
        return finnFelt(HistorikkinnslagFeltType.AARSAK);
    }

    public Optional<HistorikkinnslagFelt> getTema() {
        return finnFelt(HistorikkinnslagFeltType.ANGÅR_TEMA);
    }


    public Optional<HistorikkinnslagFelt> getAvklartSoeknadsperiode() {
        return finnFelt(HistorikkinnslagFeltType.AVKLART_SOEKNADSPERIODE);
    }

    public Optional<String> getBegrunnelse() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.BEGRUNNELSE);
    }

    public Optional<HistorikkinnslagFelt> getBegrunnelseFelt() {
        return finnFelt(HistorikkinnslagFeltType.BEGRUNNELSE);
    }

    /**
     * Hent en hendelse
     * @return Et HistorikkinnslagFelt fordi vi trenger navn (f.eks. BEH_VENT) og tilVerdi (f.eks. <fristDato>)
     */
    public Optional<HistorikkinnslagFelt> getHendelse() {
        return finnFelt(HistorikkinnslagFeltType.HENDELSE);
    }

    public Optional<String> getResultat() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.RESULTAT);
    }

    public Optional<String> getGjeldendeFra() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.GJELDENDE_FRA);
    }

    public Optional<HistorikkinnslagFelt> getGjeldendeFraFelt() {
        return finnFelt(HistorikkinnslagFeltType.GJELDENDE_FRA);
    }

    public Optional<String> getSkjermlenke() {
        return finnFeltTilVerdi(HistorikkinnslagFeltType.SKJERMLENKE);
    }

    public Optional<HistorikkinnslagFelt> getEndretFelt(HistorikkEndretFeltType endretFeltVerdiType) {
        List<HistorikkinnslagFelt> endredeFelt = getEndredeFelt();
        return endredeFelt
            .stream()
            .filter(felt -> Objects.equals(endretFeltVerdiType.getKode(), felt.getNavn()))
            .findFirst();
    }

    public List<HistorikkinnslagFelt> getEndredeFelt() {
        return finnFeltListe(HistorikkinnslagFeltType.ENDRET_FELT);
    }

    public List<HistorikkinnslagFelt> getOpplysninger() {
        return finnFeltListe(HistorikkinnslagFeltType.OPPLYSNINGER);
    }

    public List<HistorikkinnslagTotrinnsvurdering> getTotrinnsvurderinger(AksjonspunktRepository aksjonspunktRepository) {
        List<HistorikkinnslagFeltType> aksjonspunktFeltTypeKoder = Arrays.asList(HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE,
            HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT,
            HistorikkinnslagFeltType.AKSJONSPUNKT_KODE);

        List<HistorikkinnslagFelt> alleAksjonspunktFelt = historikkinnslagFelt.stream()
            .filter(felt -> aksjonspunktFeltTypeKoder.contains(felt.getFeltType()))
            .collect(Collectors.toList());

        List<HistorikkinnslagTotrinnsvurdering> totrinnsvurderinger = alleAksjonspunktFelt.stream()
            .collect(Collectors.groupingBy(HistorikkinnslagFelt::getSekvensNr))
            .entrySet()
            .stream()
            .map(entry -> lagHistorikkinnslagAksjonspunkt(entry.getKey(), entry.getValue(), aksjonspunktRepository))
            .sorted(Comparator.comparing(HistorikkinnslagTotrinnsvurdering::getSekvensNr))
            .collect(Collectors.toList());
        return totrinnsvurderinger;
    }

    private HistorikkinnslagTotrinnsvurdering lagHistorikkinnslagAksjonspunkt(Integer sekvensNr, List<HistorikkinnslagFelt> historikkinnslagFelt, AksjonspunktRepository aksjonspunktRepository) {
        HistorikkinnslagTotrinnsvurdering historikkinnslagTotrinnsvurdering = new HistorikkinnslagTotrinnsvurdering(sekvensNr);
        historikkinnslagFelt.forEach(felt -> {
            if (HistorikkinnslagFeltType.AKSJONSPUNKT_BEGRUNNELSE.equals(felt.getFeltType())) {
                historikkinnslagTotrinnsvurdering.setBegrunnelse(felt.getTilVerdi());
            } else if (HistorikkinnslagFeltType.AKSJONSPUNKT_GODKJENT.equals(felt.getFeltType())) {
                historikkinnslagTotrinnsvurdering.setGodkjent(Boolean.parseBoolean(felt.getTilVerdi()));
            } else if (HistorikkinnslagFeltType.AKSJONSPUNKT_KODE.equals(felt.getFeltType())) {
                AksjonspunktDefinisjon aksjonspunktDefinisjon = aksjonspunktRepository.finnAksjonspunktDefinisjon(felt.getTilVerdi());
                historikkinnslagTotrinnsvurdering.setAksjonspunktDefinisjon(aksjonspunktDefinisjon);
            } else {
                throw new IllegalStateException("Uventet feltnavn " + felt.getFeltType().getKode());
            }
        });
        return historikkinnslagTotrinnsvurdering;
    }

    private Optional<HistorikkinnslagFelt> finnFelt(HistorikkinnslagFeltType historikkinnslagFeltType) {
        return historikkinnslagFelt.stream()
            .filter(felt -> historikkinnslagFeltType.equals(felt.getFeltType()))
            .findFirst();
    }

    private Optional<String> finnFeltTilVerdi(HistorikkinnslagFeltType historikkinnslagFeltType) {
        return finnFelt(historikkinnslagFeltType)
            .map(HistorikkinnslagFelt::getTilVerdi);
    }

    private List<HistorikkinnslagFelt> finnFeltListe(HistorikkinnslagFeltType feltType) {
        return historikkinnslagFelt.stream()
            .filter(felt -> felt.getFeltType().equals(feltType))
            .sorted(Comparator.comparing(HistorikkinnslagFelt::getSekvensNr))
            .collect(Collectors.toList());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HistorikkinnslagDel.Builder builder(HistorikkinnslagDel del) {
        return new Builder(del);
    }

    public static class Builder {
        private HistorikkinnslagDel kladd;


        private Builder() {
            this(new HistorikkinnslagDel());
        }

        public Builder(HistorikkinnslagDel del) {
            kladd = del;
        }

        public Builder leggTilFelt(HistorikkinnslagFelt felt) {
            kladd.historikkinnslagFelt.add(felt);
            felt.setHistorikkinnslagDel(kladd);
            return this;
        }

        public Builder medHistorikkinnslag(Historikkinnslag historikkinnslag) {
            kladd.historikkinnslag = historikkinnslag;
            return this;
        }

        public boolean harFelt() {
            return !kladd.getHistorikkinnslagFelt().isEmpty();
        }

        public HistorikkinnslagDel build() {
            return kladd;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistorikkinnslagDel)) {
            return false;
        }
        HistorikkinnslagDel that = (HistorikkinnslagDel) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
