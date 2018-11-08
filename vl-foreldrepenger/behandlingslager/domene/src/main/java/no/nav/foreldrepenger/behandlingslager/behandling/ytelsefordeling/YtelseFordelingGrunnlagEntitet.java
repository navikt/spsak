package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordeling;
import no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling.periode.OppgittFordelingEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Entity(name = "YtelseFordelingGrunnlag")
@Table(name = "GR_YTELSES_FORDELING")
public class YtelseFordelingGrunnlagEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_GR_YTELSES_FORDELING")
    private Long id;

    @OneToOne
    @JoinColumn(name = "behandling_id", nullable = false, updatable = false, unique = true)
    private Behandling behandling;

    @ManyToOne
    @JoinColumn(name = "so_fordeling_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittFordelingEntitet oppgittFordeling;

    @ManyToOne
    @JoinColumn(name = "overstyrt_fordeling_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittFordelingEntitet overstyrtFordeling;

    @ManyToOne
    @JoinColumn(name = "bekreftet_fordeling_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittFordelingEntitet bekreftetFordeling;

    @ManyToOne
    @JoinColumn(name = "so_rettighet_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittRettighetEntitet oppgittRettighet;

    /**
     * TODO Diamant bør ryddes opp ( bør ligge i en struktur som er felles for begge søkere. Kopieres nå til {@link no.nav.foreldrepenger.behandlingslager.fagsak.FagsakRelasjon} )
     */
    @ManyToOne
    @JoinColumn(name = "so_dekningsgrad_id", updatable = false, unique = true)
    @ChangeTracked
    private OppgittDekningsgradEntitet oppgittDekningsgrad;

    @ManyToOne
    @JoinColumn(name = "utenomsorg_id", updatable = false, unique = true)
    @ChangeTracked
    private PerioderUtenOmsorgEntitet perioderUtenOmsorgEntitet;

    @ManyToOne
    @JoinColumn(name = "aleneomsorg_id", updatable = false, unique = true)
    @ChangeTracked
    private PerioderAleneOmsorgEntitet perioderAleneOmsorgEntitet;

    @ManyToOne
    @JoinColumn(name = "uttak_dokumentasjon_id", updatable = false, unique = true)
    @ChangeTracked
    private PerioderUttakDokumentasjonEntitet perioderUttakDokumentasjon;

    @ManyToOne
    @JoinColumn(name = "yf_AVKLART_DATO_id", updatable = false, unique = true)
    @ChangeTracked
    private AvklarteUttakDatoerEntitet avklarteUttakDatoerEntitet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    YtelseFordelingGrunnlagEntitet() {
    }

    OppgittFordelingEntitet getOppgittFordeling() {
        return oppgittFordeling;
    }

    void setOppgittFordeling(OppgittFordeling oppgittFordeling) {
        this.oppgittFordeling = (OppgittFordelingEntitet) oppgittFordeling;
    }

    OppgittFordelingEntitet getOverstyrtFordeling() {
        return overstyrtFordeling;
    }

    void setOverstyrtFordeling(OppgittFordeling overstyrtFordeling) {
        this.overstyrtFordeling = (OppgittFordelingEntitet) overstyrtFordeling;
    }

    public OppgittFordelingEntitet getBekreftetFordeling() {
        return bekreftetFordeling;
    }

    void setBekreftetFordeling(OppgittFordeling bekreftetFordeling) {
        this.bekreftetFordeling = (OppgittFordelingEntitet) bekreftetFordeling;
    }

    OppgittDekningsgradEntitet getDekningsgrad() {
        return oppgittDekningsgrad;
    }

    void setDekningsgrad(OppgittDekningsgrad dekningsgrad) {
        this.oppgittDekningsgrad = (OppgittDekningsgradEntitet) dekningsgrad;
    }

    OppgittRettighetEntitet getOppgittRettighet() {
        return oppgittRettighet;
    }

    void setOppgittRettighet(OppgittRettighet oppgittRettighet) {
        this.oppgittRettighet = (OppgittRettighetEntitet) oppgittRettighet;
    }

    void setBehandling(Behandling behandling) {
        this.behandling = behandling;
    }

    void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    PerioderUtenOmsorg getPerioderUtenOmsorg() {
        return perioderUtenOmsorgEntitet;
    }

    void setPerioderUtenOmsorg(PerioderUtenOmsorg perioder) {
        this.perioderUtenOmsorgEntitet = (PerioderUtenOmsorgEntitet) perioder;
    }

    PerioderAleneOmsorgEntitet getPerioderAleneOmsorgEntitet() {
        return perioderAleneOmsorgEntitet;
    }

    void setPerioderAleneOmsorg(PerioderAleneOmsorg perioder) {
        this.perioderAleneOmsorgEntitet = (PerioderAleneOmsorgEntitet) perioder;
    }

    PerioderUttakDokumentasjonEntitet getPerioderUttakDokumentasjon() {
        return perioderUttakDokumentasjon;
    }

    void setPerioderUttakDokumentasjon(PerioderUttakDokumentasjon perioderUttakDokumentasjon) {
        this.perioderUttakDokumentasjon = (PerioderUttakDokumentasjonEntitet) perioderUttakDokumentasjon;
    }

    AvklarteUttakDatoer getAvklarteUttakDatoer() {
        return avklarteUttakDatoerEntitet;
    }

    void setAvklarteUttakDatoerEntitet(AvklarteUttakDatoer avklarteUttakDatoerEntitet) {
        this.avklarteUttakDatoerEntitet = (AvklarteUttakDatoerEntitet) avklarteUttakDatoerEntitet;
    }

    /* eksponeres ikke public for andre. */
    Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YtelseFordelingGrunnlagEntitet that = (YtelseFordelingGrunnlagEntitet) o;
        return aktiv == that.aktiv &&
            Objects.equals(oppgittFordeling, that.oppgittFordeling) &&
            Objects.equals(oppgittRettighet, that.oppgittRettighet) &&
            Objects.equals(oppgittDekningsgrad, that.oppgittDekningsgrad) &&
            Objects.equals(perioderUtenOmsorgEntitet, that.perioderUtenOmsorgEntitet) &&
            Objects.equals(perioderAleneOmsorgEntitet, that.perioderAleneOmsorgEntitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittFordeling, oppgittRettighet, oppgittDekningsgrad, perioderUtenOmsorgEntitet, perioderAleneOmsorgEntitet, aktiv);
    }
}
