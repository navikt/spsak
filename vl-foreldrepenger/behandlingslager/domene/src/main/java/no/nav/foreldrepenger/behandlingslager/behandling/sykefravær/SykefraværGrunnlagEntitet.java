package no.nav.foreldrepenger.behandlingslager.behandling.sykefravær;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.Sykefravær;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.perioder.SykefraværEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.Sykemeldinger;
import no.nav.foreldrepenger.behandlingslager.behandling.sykefravær.sykemelding.SykemeldingerEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "SykefraværGrunnlagEntitet")
@Table(name = "GR_SYKEFRAVAER")
public class SykefraværGrunnlagEntitet extends BaseEntitet implements SykefraværGrunnlag {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_SYKEFRAVAER")
    private Long id;

    @ManyToOne(cascade = { /* NONE - Aldri cascade til behandling! */}, fetch = FetchType.LAZY)
    @JoinColumn(name = "behandling_id", updatable = false, nullable = false)
    private Behandling behandling;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @ChangeTracked
    @ManyToOne
    @JoinColumn(name = "sykemeldinger_id", updatable = false)
    private SykemeldingerEntitet sykemeldinger;

    @ChangeTracked
    @ManyToOne
    @JoinColumn(name = "sykefravaer_id", updatable = false)
    private SykefraværEntitet sykefravær;


    SykefraværGrunnlagEntitet() {
    }

    SykefraværGrunnlagEntitet(SykefraværGrunnlagEntitet grunnlag) {
        this.sykefravær = new SykefraværEntitet(grunnlag.getSykefravær());
        this.sykemeldinger = new SykemeldingerEntitet(grunnlag.getSykemeldinger());
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    @Override
    public Sykemeldinger getSykemeldinger() {
        return sykemeldinger;
    }

    void setSykemeldinger(Sykemeldinger sykemeldinger) {
        this.sykemeldinger = (SykemeldingerEntitet) sykemeldinger;
    }

    @Override
    public Sykefravær getSykefravær() {
        return sykefravær;
    }

    void setSykefravær(Sykefravær sykefravær) {
        this.sykefravær = (SykefraværEntitet) sykefravær;
    }

    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    @Override
    public String toString() {
        return "SykefraværGrunnlagEntitet{" +
            "id=" + id +
            ", behandling=" + behandling +
            ", aktiv=" + aktiv +
            '}';
    }
}
