package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.søknad.grunnlag.UtenlandskVirksomhet;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;

/**
 * Hibernate entitet som modellerer en utenlandsk virksomhet.
 */
@Embeddable
public class UtenlandskVirksomhetEntitet implements UtenlandskVirksomhet, IndexKey, Serializable {

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "land", referencedColumnName = "kode", nullable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'"))})
    private Landkoder landkode = Landkoder.NOR;

    @Column(name = "utenlandsk_virksomhet_navn")
    private String utenlandskVirksomhetNavn;

    public UtenlandskVirksomhetEntitet() {
        //hibernate
    }

    public UtenlandskVirksomhetEntitet(Landkoder landkode, String utenlandskVirksomhetNavn) {
        this.landkode = landkode;
        this.utenlandskVirksomhetNavn = utenlandskVirksomhetNavn;
    }
    @Override
    public String getIndexKey() {
        return IndexKey.createKey(utenlandskVirksomhetNavn, landkode);
    }

    @Override
    public Landkoder getLandkode() {
        return landkode;
    }

    @Override
    public String getUtenlandskVirksomhetNavn() {
        return utenlandskVirksomhetNavn;
    }
}
