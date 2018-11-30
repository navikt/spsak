package no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse;

import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.grunnlag.Yrkesaktivitet;

public class Opptjeningsnøkkel {

    private ArbeidsforholdRef arbeidsforholdId;
    private String orgNummer;
    private String aktørId;

    public Opptjeningsnøkkel(String arbeidsforholdId, String orgNummer, String aktørId) {
        this.arbeidsforholdId = arbeidsforholdId != null ? ArbeidsforholdRef.ref(arbeidsforholdId) : null;
        this.orgNummer = orgNummer;
        this.aktørId = aktørId;
    }

    public Opptjeningsnøkkel(Yrkesaktivitet yrkesaktivitet) {
        this.arbeidsforholdId = yrkesaktivitet.getArbeidsforholdRef().orElse(null);

        // kan ikke være begge deler.
        final Arbeidsgiver arbeidsgiver = yrkesaktivitet.getArbeidsgiver();
        if (arbeidsgiver != null) {
            if (arbeidsgiver.getErVirksomhet()) {
                this.orgNummer = arbeidsgiver.getIdentifikator();
            } else {
                this.aktørId = arbeidsgiver.getIdentifikator();
            }
        }
    }

    public Opptjeningsnøkkel(Arbeidsgiver arbeidsgiver) {
        this.arbeidsforholdId = null;

        // kan ikke være begge deler.
        if (arbeidsgiver.getErVirksomhet()) {
            this.orgNummer = arbeidsgiver.getIdentifikator();
        } else {
            this.aktørId = arbeidsgiver.getIdentifikator();
        }
    }

    public Opptjeningsnøkkel(ArbeidsforholdRef arbeidsforholdId, Arbeidsgiver arbeidsgiver) {
        this.arbeidsforholdId = arbeidsforholdId;
        // kan ikke være begge deler.
        if (arbeidsgiver.getErVirksomhet()) {
            this.orgNummer = arbeidsgiver.getIdentifikator();
        } else {
            this.aktørId = arbeidsgiver.getIdentifikator();
        }
    }

    public static Opptjeningsnøkkel forOrgnummer(String orgNummer) {
        return new Opptjeningsnøkkel(null, orgNummer, null);
    }

    public static Opptjeningsnøkkel forArbeidsforholdIdMedArbeidgiver(ArbeidsforholdRef arbeidsforholdId, Arbeidsgiver arbeidsgiver) {
        return new Opptjeningsnøkkel(arbeidsforholdId, arbeidsgiver);
    }

    public static Opptjeningsnøkkel forType(String id, Type nøkkelType) {
        return nøkkelType.nyNøkkel(id);
    }

    /**
     * Gir en opptjeningsnøkkel basert på følgende rank
     * 1) ArbeidsforholdId er en unik id fra AAreg
     * 2) Org nummer er iden til en virksomhet som fungere som arbeidsgiver
     * 3) Aktør id er iden til en person som fungere som arbeidsgiver
     */
    public String getVerdi() {
        if (harArbeidsforholdId())
            return this.arbeidsforholdId.getReferanse();
        else if (this.orgNummer != null) {
            return this.orgNummer;
        } else if (this.aktørId != null) {
            return this.aktørId;
        } else {
            // TODO(OJR) lag feil
            throw new IllegalStateException("Har ikke nøkkel");
        }
    }

    private boolean harArbeidsforholdId() {
        return this.arbeidsforholdId != null && this.arbeidsforholdId.getReferanse() != null;
    }

    public Optional<ArbeidsforholdRef> getArbeidsforholdRef() {
        return Optional.ofNullable(arbeidsforholdId);
    }

    public String getForType(Type type) {
        if (type.equals(Type.ARBEIDSFORHOLD_ID)) {
            return arbeidsforholdId != null ? arbeidsforholdId.getReferanse() : null;
        } else if (type.equals(Type.ORG_NUMMER)) {
            return orgNummer;
        } else if (type.equals(Type.AKTØR_ID)) {
            return aktørId;
        }
        // TODO(OJR) lag feil
        throw new IllegalArgumentException("Utvikler-feil: Støtter ikke typen");
    }

    public Type getType() {
        if (harArbeidsforholdId())
            return Type.ARBEIDSFORHOLD_ID;
        else if (this.orgNummer != null) {
            return Type.ORG_NUMMER;
        } else if (this.aktørId != null) {
            return Type.AKTØR_ID;
        } else {
            // TODO(OJR) fiks dette surre her.... må ta inn opptjeningstype også?????
            return null;
        }
    }

    public Type getArbeidsgiverType() {
        if (this.orgNummer != null) {
            return Type.ORG_NUMMER;
        } else if (this.aktørId != null) {
            return Type.AKTØR_ID;
        } else {
            // TODO(OJR) fiks dette surre her.... må ta inn opptjeningstype også?????
            return null;
        }
    }

    //TODO(OJR) håndter ikke frilans?
    public boolean matcher(Opptjeningsnøkkel other) {
        if(other == null) {
            return false;
        }
        if (other.getType() == this.getType()) {
            return other.getVerdi().equals(this.getVerdi());
        } else {
            if ((other.getType().equals(Type.ORG_NUMMER) || this.getType().equals(Type.ORG_NUMMER))
                && (!other.harArbeidsforholdId() || !this.harArbeidsforholdId())) {
                return other.orgNummer != null && other.orgNummer.equals(this.orgNummer);
            }
            return other.aktørId.equals(this.aktørId);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj.getClass().equals(this.getClass()))) {
            return false;
        }
        Opptjeningsnøkkel other = (Opptjeningsnøkkel) obj;
        return Objects.equals(aktørId, other.aktørId)
            && Objects.equals(orgNummer, other.orgNummer)
            && Objects.equals(arbeidsforholdId, other.arbeidsforholdId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsforholdId, orgNummer, aktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<"
            + "type=" + getType()
            + ", key=" + getVerdi()
            + ">";
    }

    public boolean harType(Type type) {
        return type.equals(getType());
    }

    public enum Type {
        ARBEIDSFORHOLD_ID {
            @Override
            Opptjeningsnøkkel nyNøkkel(String id) {
                return new Opptjeningsnøkkel(id, null, null);
            }
        },
        ORG_NUMMER {
            @Override
            Opptjeningsnøkkel nyNøkkel(String id) {
                return new Opptjeningsnøkkel(null, id, null);
            }
        },
        AKTØR_ID {
            @Override
            Opptjeningsnøkkel nyNøkkel(String id) {
                return new Opptjeningsnøkkel(null, null, id);
            }
        };

        abstract Opptjeningsnøkkel nyNøkkel(String id);
    }
}
