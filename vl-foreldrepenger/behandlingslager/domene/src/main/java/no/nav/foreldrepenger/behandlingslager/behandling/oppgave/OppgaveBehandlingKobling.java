package no.nav.foreldrepenger.behandlingslager.behandling.oppgave;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.domene.typer.Saksnummer;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;
import no.nav.vedtak.util.FPDateUtil;

@Entity(name = "OppgaveBehandlingKobling")
@Table(name = "OPPGAVE_BEHANDLING_KOBLING")
public class OppgaveBehandlingKobling extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_OPPGAVE_BEHANDLING_KOBLING")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "oppgave_aarsak", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + OppgaveÅrsak.DISCRIMINATOR + "'"))
    private OppgaveÅrsak oppgaveÅrsak;

    @Column(name = "oppgave_id")
    private String oppgaveId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false)
    private Behandling behandling;

    /**
     * Offisielt tildelt saksnummer fra GSAK.
     */
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer", nullable = false, updatable = false)))
    private Saksnummer saksnummer;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "ferdigstilt")
    private Boolean ferdigstilt = false;

    @Column(name = "ferdigstilt_av")
    private String ferdigstiltAv;

    @Column(name = "ferdigstilt_tid")
    private LocalDateTime ferdigstiltTid;

    OppgaveBehandlingKobling() {
        // Hibernate
    }

    public OppgaveBehandlingKobling(OppgaveÅrsak oppgaveÅrsak, String oppgaveId, Saksnummer saksnummer, Behandling behandling) {
        this.setOppgaveÅrsak(oppgaveÅrsak);
        this.oppgaveId = oppgaveId;
        this.saksnummer = saksnummer;
        this.behandling = behandling;
    }

    public Long getId() {
        return id;
    }

    //GSAK-oppgave-id
    public String getOppgaveId() {
        return oppgaveId;
    }

    public Behandling getBehandling() {
        return behandling;
    }

    public void ferdigstillOppgave(String ferdigstiltAv) {
        this.ferdigstiltTid = LocalDateTime.now(FPDateUtil.getOffset());
        this.ferdigstiltAv = ferdigstiltAv;
        this.ferdigstilt = true;
    }

    public Boolean isFerdigstilt() {
        return ferdigstilt;
    }

    public void setFerdigstilt(Boolean ferdigstilt) {
        this.ferdigstilt = ferdigstilt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OppgaveBehandlingKobling)) {
            return false;
        }
        OppgaveBehandlingKobling other = (OppgaveBehandlingKobling) obj;
        return Objects.equals(getOppgaveId(), other.getOppgaveId())
            && Objects.equals(getOppgaveÅrsak(), other.getOppgaveÅrsak())
            && Objects.equals(getBehandling(), other.getBehandling());
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgaveId, getOppgaveÅrsak(), behandling);
    }

    public OppgaveÅrsak getOppgaveÅrsak() {
        return Objects.equals(oppgaveÅrsak, OppgaveÅrsak.UDEFINERT) ? null : oppgaveÅrsak;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public String getFerdigstiltAv() {
        return ferdigstiltAv;
    }

    private void setOppgaveÅrsak(OppgaveÅrsak oppgaveÅrsak) {
        this.oppgaveÅrsak = oppgaveÅrsak == null ? OppgaveÅrsak.UDEFINERT : oppgaveÅrsak;
    }

    public static Optional<OppgaveBehandlingKobling> getAktivOppgaveMedÅrsak(OppgaveÅrsak årsak, List<OppgaveBehandlingKobling> oppgaver) {
        return oppgaver
            .stream()
            .filter(oppgave -> !oppgave.isFerdigstilt())
            .filter(oppgave -> årsak.equals(oppgave.getOppgaveÅrsak()))
            .findFirst();
    }
}


