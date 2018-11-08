package no.nav.foreldrepenger.domene.dokumentarkiv;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;

public class ArkivDokument {
    private String dokumentId;
    private String tittel;
    private DokumentTypeId dokumentTypeId;
    private DokumentKategori dokumentKategori;
    private List<ArkivDokumentHentbart> tilgjengeligSom; // hvilke formater som er tilgjengelig fra joark
    private List<ArkivDokumentVedlegg> interneVedlegg; // sammensatt dokument der vedlegg er scannet inn i ett dokument

    public String getDokumentId() {
        return dokumentId;
    }

    public void setDokumentId(String dokumentId) {
        this.dokumentId = dokumentId;
    }

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

    public DokumentKategori getDokumentKategori() {
        return dokumentKategori;
    }

    public void setDokumentKategori(DokumentKategori dokumentKategori) {
        this.dokumentKategori = dokumentKategori;
    }

    public List<ArkivDokumentVedlegg> getInterneVedlegg() {
        return interneVedlegg;
    }

    public void setInterneVedlegg(List<ArkivDokumentVedlegg> interneVedlegg) {
        this.interneVedlegg = interneVedlegg;
    }

    public List<ArkivDokumentHentbart> getTilgjengeligSom() {
        return tilgjengeligSom;
    }

    public void setTilgjengeligSom(List<ArkivDokumentHentbart> tilgjengeligSom) {
        this.tilgjengeligSom = tilgjengeligSom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArkivDokument that = (ArkivDokument) o;
        return Objects.equals(dokumentId, that.dokumentId) &&
            Objects.equals(tittel, that.tittel) &&
            Objects.equals(dokumentTypeId, that.dokumentTypeId) &&
            Objects.equals(dokumentKategori, that.dokumentKategori);
    }

    @Override
    public int hashCode() {

        return Objects.hash(dokumentId, tittel, dokumentTypeId, dokumentKategori);
    }

    public static class Builder {
        private final ArkivDokument arkivDokument;

        private Builder() {
            this.arkivDokument = new ArkivDokument();
            this.arkivDokument.setDokumentTypeId(DokumentTypeId.UDEFINERT);
            this.arkivDokument.setDokumentKategori(DokumentKategori.UDEFINERT);
            this.arkivDokument.setInterneVedlegg(new ArrayList<>());
            this.arkivDokument.setTilgjengeligSom(new ArrayList<>());
        }

        public static Builder ny() {
            return new Builder();
        }

        public Builder medDokumentId(String dokumentId) {
            this.arkivDokument.setDokumentId(dokumentId);
            return this;
        }

        public Builder medTittel(String tittel) {
            this.arkivDokument.setTittel(tittel);
            return this;
        }

        public Builder medDokumentTypeId(DokumentTypeId dokumentTypeId) {
            this.arkivDokument.setDokumentTypeId(dokumentTypeId);
            return this;
        }

        public Builder medDokumentKategori(DokumentKategori dokumentKategori) {
            this.arkivDokument.setDokumentKategori(dokumentKategori);
            return this;
        }

        public Builder leggTilInterntVedlegg(ArkivDokumentVedlegg vedlegg){
            this.arkivDokument.getInterneVedlegg().add(vedlegg);
            return this;
        }

        public Builder leggTilTilgjengeligFormat(ArkivDokumentHentbart arkivVariant){
            this.arkivDokument.getTilgjengeligSom().add(arkivVariant);
            return this;
        }

        public ArkivDokument build() {
            return this.arkivDokument;
        }

    }
}
