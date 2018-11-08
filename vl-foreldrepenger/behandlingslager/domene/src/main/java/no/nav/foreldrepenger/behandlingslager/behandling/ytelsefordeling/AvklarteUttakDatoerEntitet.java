package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;

@Entity(name = "AvklarteUttakDatoerEntitet")
@Table(name = "YF_AVKLART_DATO")
public class AvklarteUttakDatoerEntitet extends BaseEntitet implements AvklarteUttakDatoer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YF_AVKLART_DATO")
    private Long id;

    @Column(name = "forste_uttaksdato")
    @ChangeTracked
    private LocalDate førsteUttaksdato;

    @Column(name = "endringsdato")
    @ChangeTracked
    private LocalDate endringsdato;

    AvklarteUttakDatoerEntitet() {
    }

    AvklarteUttakDatoerEntitet(AvklarteUttakDatoer avklarteUttakDatoer) {
        this.endringsdato = avklarteUttakDatoer.getEndringsdato();
        this.førsteUttaksdato = avklarteUttakDatoer.getFørsteUttaksDato();
    }

    public AvklarteUttakDatoerEntitet(LocalDate førsteUttaksdato, LocalDate endringsdato) {
        this.førsteUttaksdato = førsteUttaksdato;
        this.endringsdato = endringsdato;
    }

    @Override
    public LocalDate getFørsteUttaksDato() {
        return førsteUttaksdato;
    }

    @Override
    public LocalDate getEndringsdato() {
        return endringsdato;
    }

    public boolean harVerdier() {
        return !(førsteUttaksdato == null && endringsdato == null);
    }
}
