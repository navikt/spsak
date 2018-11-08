package no.nav.foreldrepenger.behandlingslager.fagsak;

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

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.uttak.Stønadskontoberegning;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "FagsakRelasjon")
@Table(name = "FAGSAK_RELASJON")
public class FagsakRelasjon extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_FAGSAK_RELASJON")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fagsak_en_id", nullable = false)
    private Fagsak fagsakNrEn;

    @ManyToOne
    @JoinColumn(name = "fagsak_to_id")
    private Fagsak fagsakNrTo;

    @ManyToOne
    @JoinColumn(name = "konto_beregning_id")
    private Stønadskontoberegning stønadskontoberegning;

    /**
     * Dekningsgrad skal uten unntak være identisk for begge søkere
     */
    @AttributeOverrides(@AttributeOverride(name = "dekningsgrad", column = @Column(name = "dekningsgrad", nullable = false)))
    @Embedded
    private Dekningsgrad dekningsgrad;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    FagsakRelasjon() {
        // For Hibernate
    }

    FagsakRelasjon(Fagsak fagsakNrEn, Fagsak fagsakNrTo, Stønadskontoberegning stønadskontoberegning, Dekningsgrad dekningsgrad) {
        this.stønadskontoberegning = stønadskontoberegning;
        this.fagsakNrEn = fagsakNrEn;
        this.fagsakNrTo = fagsakNrTo;
        this.dekningsgrad = dekningsgrad;
    }

    public Long getId() {
        return id;
    }

    void setAktiv(boolean aktivt) {
        this.aktiv = aktivt;
    }

    public boolean getErAktivt() {
        return aktiv;
    }

    public Optional<Stønadskontoberegning> getStønadskontoberegning() {
        return Optional.ofNullable(stønadskontoberegning);
    }

    public Fagsak getFagsakNrEn() {
        return fagsakNrEn;
    }

    public Optional<Fagsak> getFagsakNrTo() {
        return Optional.ofNullable(fagsakNrTo);
    }

    public Dekningsgrad getDekningsgrad() {
        return dekningsgrad;
    }
}
