package no.nav.foreldrepenger.behandlingslager.behandling.ytelsefordeling;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "YF_DOKUMENTASJON_PERIODER")
@DiscriminatorColumn(name = "DOKUMENTASJON_KLASSE")
public abstract class DokumentasjonPerioderEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YF_DOKUMENTASJON_PERIODER")
    private Long id;
}
