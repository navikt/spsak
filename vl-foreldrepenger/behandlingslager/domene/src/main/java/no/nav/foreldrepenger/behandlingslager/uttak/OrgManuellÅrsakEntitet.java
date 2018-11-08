package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity
@Table(name = "ORG_MANUELL_BEHANDLING")

public class OrgManuellÅrsakEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ORG_MANUELL_BEHANDLING")
    @Column(name = "id")
    private Long id;

    @Column( name = "Virksomhetsnummer", nullable = false)
    private String virksomhetsnummer;

    @Column( name = "Manuell_beh_arsak", nullable = false)
    private String manuellBehandlingsÅrsak;

    public Long getId() {
        return id;
    }

    public String getVirksomhetsnummer() { return virksomhetsnummer; }

    public String getManuellBehandlingsÅrsak() { return manuellBehandlingsÅrsak; }

    public static class Builder{

        private OrgManuellÅrsakEntitet kladd = new OrgManuellÅrsakEntitet();

        public Builder medVirksomhetsnummer(String virksomhetsnummer) {
            Objects.requireNonNull(virksomhetsnummer);
            kladd.virksomhetsnummer = virksomhetsnummer;
            return this;
        }
        public Builder medManuellBehandlingsÅrsak(String manuellBehandlingsÅrsak) {
            Objects.requireNonNull(manuellBehandlingsÅrsak);
            kladd.manuellBehandlingsÅrsak = manuellBehandlingsÅrsak;
            return this;
        }

        public OrgManuellÅrsakEntitet build() {
            return kladd;
        }
    }

}
