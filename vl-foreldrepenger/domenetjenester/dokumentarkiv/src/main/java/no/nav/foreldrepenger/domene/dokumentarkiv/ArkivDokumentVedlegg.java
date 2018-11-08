package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;

/*
 * Til bruk for journalposter der hoveddokument er ett scannet dokument som inneholder både hoveddokument og vedlegg
 */
public class ArkivDokumentVedlegg {
    private String tittel;
    private DokumentTypeId dokumentTypeId;

    public String getTittel() {
        return tittel;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        this.dokumentTypeId = dokumentTypeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArkivDokumentVedlegg that = (ArkivDokumentVedlegg) o;
        return Objects.equals(tittel, that.tittel) &&
            Objects.equals(dokumentTypeId, that.dokumentTypeId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tittel, dokumentTypeId);
    }

    public static class Builder {
        private final ArkivDokumentVedlegg arkivDokumentVedlegg;

        private Builder() {
            this.arkivDokumentVedlegg = new ArkivDokumentVedlegg();
        }

        public static Builder ny() {
            return new Builder();
        }

        public Builder medTittel(String tittel) {
            this.arkivDokumentVedlegg.setTittel(tittel);
            return this;
        }

        public Builder medDokumentTypeId(DokumentTypeId dokumentTypeId) {
            this.arkivDokumentVedlegg.setDokumentTypeId(dokumentTypeId);
            return this;
        }

        public ArkivDokumentVedlegg build() {
            return this.arkivDokumentVedlegg;
        }

    }
}
