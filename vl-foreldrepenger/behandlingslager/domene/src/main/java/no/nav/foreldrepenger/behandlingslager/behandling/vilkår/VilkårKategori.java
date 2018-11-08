package no.nav.foreldrepenger.behandlingslager.behandling.vilkår;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.kodeverk.Kodeliste;

@Entity(name = "VilkårKategori")
@DiscriminatorValue(VilkårKategori.DISCRIMINATOR)
public class VilkårKategori extends Kodeliste {

    public static final String DISCRIMINATOR = "VILKAR_KATEGORI"; //$NON-NLS-1$

    public static final VilkårKategori INNGANGSVILKÅR = new VilkårKategori("INNGANGSVILKÅR");
    public static final VilkårKategori BEREGNINGSVILKÅR = new VilkårKategori("BEREGNINGSVILKÅR");

    /**
     * Brukes i stedet for null der det er optional.
     */
    public static final VilkårKategori UDEFINERT = new VilkårKategori("-"); //$NON-NLS-1$

    VilkårKategori() {
        // Hibernate trenger den
    }

    public VilkårKategori(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
