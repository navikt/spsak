package no.nav.foreldrepenger.behandlingslager.hendelser.feilhåndtering;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.behandlingslager.BaseEntitet;

@Entity(name = "MottattHendelse")
@Table(name = "MOTTATT_HENDELSE")
public class MottattHendelse extends BaseEntitet {

    @Id
    @NaturalId
    @Column(name = "hendelse_uid")
    private String hendelseUid;

    MottattHendelse() {
        //for hibernate
    }

    public MottattHendelse(String hendelseUid) {
        this.hendelseUid = hendelseUid;
    }

    public String getHendelseUid() {
        return hendelseUid;
    }
}
