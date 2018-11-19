package no.nav.foreldrepenger.behandling.steg.inngangsvilkår.auto.util;

import java.util.Arrays;
import java.util.Objects;

class FileRef {
    private byte[] bytes;
    private String utfall;

    FileRef(byte[] bytes, String utfall) {
        this.bytes = bytes;
        this.utfall = utfall;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getUtfall() {
        return utfall;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileRef fileRef = (FileRef) o;
        return Arrays.equals(bytes, fileRef.bytes) &&
            Objects.equals(utfall, fileRef.utfall);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(utfall);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }
}
