package no.nav.foreldrepenger.behandlingslager.uttak;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

@Table(name = "UTTAK_RESULTAT")
@Entity
public class UttakResultatEntitet extends BaseEntitet {

    @Id
    @Column(name = "id", columnDefinition = "NUMERIC", length = 19)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_UTTAK_RESULTAT")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "opprinnelig_perioder_id", updatable = false, unique = true)
    private UttakResultatPerioderEntitet opprinneligPerioder;

    @ManyToOne
    @JoinColumn(name = "overstyrt_perioder_id", updatable = false, unique = true)
    private UttakResultatPerioderEntitet overstyrtPerioder;

    @ManyToOne
    @JoinColumn(name = "behandling_resultat_id", nullable = false, updatable = false)
    private Behandlingsresultat behandlingsresultat;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    public Long getId() {
        return id;
    }

    public Behandlingsresultat getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public UttakResultatPerioderEntitet getOpprinneligPerioder() {
        return opprinneligPerioder;
    }

    public UttakResultatPerioderEntitet getGjeldendePerioder() {
        if (overstyrtPerioder == null && opprinneligPerioder == null) {
            throw new IllegalStateException("Ingen uttaksperioder er satt");
        }
        return overstyrtPerioder != null ? overstyrtPerioder : opprinneligPerioder;
    }

    public void setOpprinneligPerioder(UttakResultatPerioderEntitet opprinneligPerioder) {
        this.opprinneligPerioder = opprinneligPerioder;
    }

    public UttakResultatPerioderEntitet getOverstyrtPerioder() {
        return overstyrtPerioder;
    }

    public void setOverstyrtPerioder(UttakResultatPerioderEntitet overstyrtPerioder) {
        this.overstyrtPerioder = overstyrtPerioder;
    }

    public void deaktiver() {
        aktiv = false;
    }

    public static Builder builder(Behandling behandling) {
        return new Builder(behandling.getBehandlingsresultat());
    }

    public static class Builder {
        private UttakResultatEntitet kladd;

        public Builder(Behandlingsresultat behandlingsresultat) {
            Objects.requireNonNull(behandlingsresultat, "Må ha behandlingsresultat for å opprette UttakResultatEntitet"); // $NON-NLS-1$
            kladd = new UttakResultatEntitet();
            kladd.behandlingsresultat = behandlingsresultat;
        }

        public Builder medOpprinneligPerioder(UttakResultatPerioderEntitet opprinneligPerioder) {
            Objects.requireNonNull(opprinneligPerioder);
            kladd.setOpprinneligPerioder(opprinneligPerioder);
            return this;
        }

        public Builder medOverstyrtPerioder(UttakResultatPerioderEntitet overstyrtPerioder) {
            kladd.setOverstyrtPerioder(overstyrtPerioder);
            return this;
        }

        public Builder nullstill() {
            kladd.setOpprinneligPerioder(null);
            kladd.setOverstyrtPerioder(null);
            return this;
        }

        public UttakResultatEntitet build() {
            if (kladd.getOverstyrtPerioder() != null && kladd.getOpprinneligPerioder() == null) {
                throw UttakFeil.FACTORY.manueltFastettePerioderManglerEksisterendeResultat(kladd.behandlingsresultat.getBehandling()).toException();
            }
            return kladd;
        }
    }
}
