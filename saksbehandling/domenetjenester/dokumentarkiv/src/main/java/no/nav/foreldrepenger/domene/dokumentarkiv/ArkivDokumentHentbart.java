package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;

/*
 * Hvilke varianter og filformater dokumentet er tilgjengelig som. Fx ARKIV/PDF(A) ORIGINAL/XML
 */
public class ArkivDokumentHentbart {
    private ArkivFilType arkivFilType;
    private VariantFormat variantFormat;

    public ArkivFilType getArkivFilType() {
        return arkivFilType;
    }

    public void setArkivFilType(ArkivFilType arkivFilType) {
        this.arkivFilType = arkivFilType;
    }

    public VariantFormat getVariantFormat() {
        return variantFormat;
    }

    public void setVariantFormat(VariantFormat variantFormat) {
        this.variantFormat = variantFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArkivDokumentHentbart that = (ArkivDokumentHentbart) o;
        return Objects.equals(arkivFilType, that.arkivFilType) &&
            Objects.equals(variantFormat, that.variantFormat);
    }

    @Override
    public int hashCode() {

        return Objects.hash(arkivFilType, variantFormat);
    }

    public static class Builder {
        private final ArkivDokumentHentbart arkivDokumentHentbart;

        private Builder() {
            this.arkivDokumentHentbart = new ArkivDokumentHentbart();
        }

        public static Builder ny() {
            return new Builder();
        }

        public Builder medArkivFilType(ArkivFilType arkivFilType) {
            this.arkivDokumentHentbart.setArkivFilType(arkivFilType);
            return this;
        }

        public Builder medVariantFormat(VariantFormat variantFormat) {
            this.arkivDokumentHentbart.setVariantFormat(variantFormat);
            return this;
        }

        public ArkivDokumentHentbart build() {
            return this.arkivDokumentHentbart;
        }

    }
}
