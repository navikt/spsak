package no.nav.foreldrepenger.behandlingslager.behandling.resultat.vedtak;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "BehandlingVedtak")
@Table(name = "BEHANDLING_VEDTAK")
public class BehandlingVedtak extends BaseEntitet {

    @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_BEHANDLING_VEDTAK")
    private Long id;

    @Column(name = "VEDTAK_DATO", nullable = false)
    private LocalDate vedtaksdato;

    @Column(name = "ANSVARLIG_SAKSBEHANDLER", nullable = false)
    private String ansvarligSaksbehandler;

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "vedtak_resultat_type", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + VedtakResultatType.DISCRIMINATOR
            + "'"))})
    private VedtakResultatType vedtakResultatType = VedtakResultatType.UDEFINERT;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BEHANDLING_RESULTAT_ID", nullable = false, updatable = false, unique = true)
    private Behandlingsresultat behandlingsresultat;

    /**
     * Hvorvidt vedtaket er et "beslutningsvedtak". Et beslutningsvedtak er et vedtak med samme utfall som forrige vedtak.
     *
     * @see https://jira.adeo.no/browse/BEGREP-2012
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "BESLUTNING", nullable = false)
    private boolean beslutningsvedtak;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "iverksetting_status", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + IverksettingStatus.DISCRIMINATOR
            + "'"))})
    private IverksettingStatus iverksettingStatus = IverksettingStatus.UDEFINERT;

    private BehandlingVedtak() {
    }

    public Long getId() {
        return id;
    }

    public LocalDate getVedtaksdato() {
        return vedtaksdato;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public VedtakResultatType getVedtakResultatType() {
        return Objects.equals(VedtakResultatType.UDEFINERT, vedtakResultatType) ? null : vedtakResultatType;
    }

    public Behandlingsresultat getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public Boolean isBeslutningsvedtak() {
        return beslutningsvedtak;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof BehandlingVedtak)) {
            return false;
        }
        BehandlingVedtak vedtak = (BehandlingVedtak) object;
        return Objects.equals(vedtaksdato, vedtak.getVedtaksdato())
            && Objects.equals(ansvarligSaksbehandler, vedtak.getAnsvarligSaksbehandler())
            && Objects.equals(getVedtakResultatType(), vedtak.getVedtakResultatType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(vedtaksdato, ansvarligSaksbehandler, getVedtakResultatType());
    }

    public static Builder builder() {
        return new Builder();
    }

    public IverksettingStatus getIverksettingStatus() {
        return Objects.equals(IverksettingStatus.UDEFINERT, iverksettingStatus) ? null : iverksettingStatus;
    }

    public void setIverksettingStatus(IverksettingStatus iverksettingStatus) {
        this.iverksettingStatus = iverksettingStatus == null ? IverksettingStatus.UDEFINERT : iverksettingStatus;
    }

    public static class Builder {
        private LocalDate vedtaksdato;
        private String ansvarligSaksbehandler;
        private VedtakResultatType vedtakResultatType;
        private Behandlingsresultat behandlingsresultat;
        private IverksettingStatus iverksettingStatus = IverksettingStatus.IKKE_IVERKSATT;
        private boolean beslutning = false;

        public Builder medVedtaksdato(LocalDate vedtaksdato) {
            this.vedtaksdato = vedtaksdato;
            return this;
        }

        public Builder medAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
            this.ansvarligSaksbehandler = ansvarligSaksbehandler;
            return this;
        }

        public Builder medVedtakResultatType(VedtakResultatType vedtakResultatType) {
            this.vedtakResultatType = vedtakResultatType;
            return this;
        }

        public Builder medBehandlingsresultat(Behandlingsresultat behandlingsresultat) {
            this.behandlingsresultat = behandlingsresultat;
            return this;
        }

        public Builder medIverksettingStatus(IverksettingStatus iverksettingStatus) {
            this.iverksettingStatus = iverksettingStatus;
            return this;
        }

        public Builder medBeslutning(boolean beslutning) {
            this.beslutning = beslutning;
            return this;
        }

        public BehandlingVedtak build() {
            verifyStateForBuild();
            BehandlingVedtak vedtak = new BehandlingVedtak();
            vedtak.vedtaksdato = vedtaksdato;
            vedtak.ansvarligSaksbehandler = ansvarligSaksbehandler;
            vedtak.vedtakResultatType = vedtakResultatType;
            vedtak.behandlingsresultat = behandlingsresultat;
            vedtak.beslutningsvedtak = beslutning;
            vedtak.setIverksettingStatus(iverksettingStatus);
            return vedtak;
        }

        public void verifyStateForBuild() {
            Objects.requireNonNull(vedtaksdato, "vedtaksdato");
            Objects.requireNonNull(ansvarligSaksbehandler, "ansvarligSaksbehandler");
            Objects.requireNonNull(vedtakResultatType, "vedtakResultatType");
        }
    }

}
