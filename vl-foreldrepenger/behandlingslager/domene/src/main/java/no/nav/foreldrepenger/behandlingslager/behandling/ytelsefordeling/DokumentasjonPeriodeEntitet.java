package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.time.LocalDate;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "YF_DOKUMENTASJON_PERIODE")
@DiscriminatorColumn(name = "DOKUMENTASJON_KLASSE")
public abstract class DokumentasjonPeriodeEntitet<T extends DokumentasjonPeriode> extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YF_DOKUMENTASJON_PERIODE")
    private Long id;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + UttakDokumentasjonType.DISCRIMINATOR + "'", referencedColumnName = "kodeverk")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "dokumentasjon_type", referencedColumnName = "kode")),
    })
    @ChangeTracked
    private UttakDokumentasjonType dokumentasjonType;

    @Embedded
    @ChangeTracked
    private DatoIntervallEntitet intervallEntitet;

    DokumentasjonPeriodeEntitet() {
    }

    DokumentasjonPeriodeEntitet(LocalDate fom, LocalDate tom, UttakDokumentasjonType dokumentasjonType) {
        this(DatoIntervallEntitet.fraOgMedTilOgMed(fom, tom), dokumentasjonType);
    }

    DokumentasjonPeriodeEntitet(T periode) {
        this(periode.getPeriode(), periode.getDokumentasjonType());
    }

    DokumentasjonPeriodeEntitet(DatoIntervallEntitet periode, UttakDokumentasjonType dokumentasjonType) {
        this.intervallEntitet = periode;
        this.dokumentasjonType = dokumentasjonType;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(dokumentasjonType, intervallEntitet);
    }
    
    public DatoIntervallEntitet getPeriode() {
        return intervallEntitet;
    }

    public UttakDokumentasjonType getDokumentasjonType() {
        return dokumentasjonType;
    }
}
