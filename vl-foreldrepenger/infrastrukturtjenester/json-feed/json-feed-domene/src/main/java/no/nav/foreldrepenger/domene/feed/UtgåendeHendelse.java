package no.nav.foreldrepenger.domene.feed;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.BaseEntitet;

@MappedSuperclass
@Table(name = "UTGAAENDE_HENDELSE")
@DiscriminatorColumn(name = "OUTPUT_FEED_KODE")
@Entity(name = "UtgåendeHendelse")
public abstract class UtgåendeHendelse extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTGAAENDE_HENDELSE_ID")
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE", nullable = false)
    private String type;

    @Lob
    @Column(name = "PAYLOAD", nullable = false)
    private String payload;

    @Column(name = "AKTOER_ID", nullable = false)
    private Long aktørId;

    @Column(name = "SEKVENSNUMMER", nullable = false)
    private long sekvensnummer;

    @Column(name = "KILDE_ID")
    private String kildeId;

    UtgåendeHendelse() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public String getAktørId() {
        return aktørId.toString();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setAktørId(String aktørId) {
        this.aktørId = Long.parseLong(aktørId);
    }

    public long getSekvensnummer() {
        return sekvensnummer;
    }

    protected void setSekvensnummer(long sekvensnummer) {
        this.sekvensnummer = sekvensnummer;
    }

    public String getKildeId() {
        return kildeId;
    }

    public void setKildeId(String kildeId) {
        this.kildeId = kildeId;
    }

}
