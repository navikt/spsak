package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.diff.ChangeTracked;
import no.nav.foreldrepenger.behandlingslager.diff.IndexKey;
import no.nav.foreldrepenger.domene.typer.AktørId;

@Embeddable
public class Arbeidsgiver implements IndexKey, Serializable {
    /**
     * Kun en av denne og {@link #arbeidsgiverAktørId} kan være satt. Sett denne hvis Arbeidsgiver er en Organisasjon.
     */
    @ChangeTracked
    @ManyToOne
    @JoinColumn(name = "arbeidsgiver_virksomhet_id", updatable = false)
    private VirksomhetEntitet virksomhet;

    /**
     * Kun en av denne og {@link #virksomhet} kan være satt. Sett denne hvis Arbeidsgiver er en Enkelt person.
     */
    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "arbeidsgiver_aktor_id", updatable = false)))
    private AktørId arbeidsgiverAktørId;

    private Arbeidsgiver(VirksomhetEntitet virksomhet, AktørId arbeidsgiverAktørId) {
        this.virksomhet = virksomhet;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    private Arbeidsgiver() {
    }

    @Override
    public String getIndexKey() {
        return virksomhet == null
            ? IndexKey.createKey("arbeidsgiverAktørId", arbeidsgiverAktørId)
            : IndexKey.createKey("virksomhet", virksomhet);
    }

    public static Arbeidsgiver virksomhet(Virksomhet virksomhet) {
        return new Arbeidsgiver((VirksomhetEntitet) virksomhet, null);
    }

    public static Arbeidsgiver person(AktørId arbeidsgiverAktørId) {
        return new Arbeidsgiver(null, arbeidsgiverAktørId);
    }

    public Virksomhet getVirksomhet() {
        return virksomhet;
    }

    public AktørId getAktørId() {
        return arbeidsgiverAktørId;
    }

    /**
     * Returneer ident for arbeidsgiver. Kan være Org nummer eller Aktør id (dersom arbeidsgiver er en enkelt person -
     * f.eks. for Frilans el.)
     */
    public String getIdentifikator() {
        if (arbeidsgiverAktørId != null) {
            return arbeidsgiverAktørId.getId();
        }
        return virksomhet.getOrgnr();
    }

    /**
     * Return true hvis arbeidsgiver er en {@link Virksomhet}, false hvis en Person.
     */
    public boolean getErVirksomhet() {
        return this.virksomhet != null;
    }

    /**
     * Return true hvis arbeidsgiver er en {@link AktørId}, ellers false.
     */
    public boolean erAktørId() {
        return this.arbeidsgiverAktørId != null;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Arbeidsgiver that = (Arbeidsgiver) o;
        return Objects.equals(virksomhet, that.virksomhet) &&
            Objects.equals(arbeidsgiverAktørId, that.arbeidsgiverAktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(virksomhet, arbeidsgiverAktørId);
    }

    @Override
    public String toString() {
        return "Arbeidsgiver{" +
            "virksomhet=" + virksomhet +
            ", arbeidsgiverAktørId='" + arbeidsgiverAktørId + '\'' +
            '}';
    }
}
