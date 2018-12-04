package no.nav.foreldrepenger.behandlingslager.hendelser;

import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingStegType;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.VilkårType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkTabell;

@Entity(name = "StartpunktType")
@Table(name = "STARTPUNKT_TYPE")
public class StartpunktType extends KodeverkTabell {

    public static final StartpunktType UDEFINERT = new StartpunktType("-"); //$NON-NLS-1$
    public static final StartpunktType KONTROLLER_ARBEIDSFORHOLD = new StartpunktType("KONTROLLER_ARBEIDSFORHOLD"); //$NON-NLS-1$
    public static final StartpunktType INNGANGSVILKÅR_OPPLYSNINGSPLIKT = new StartpunktType("INNGANGSVILKÅR_OPPL"); //$NON-NLS-1$
    public static final StartpunktType INNGANGSVILKÅR_MEDLEMSKAP = new StartpunktType("INNGANGSVILKÅR_MEDL"); //$NON-NLS-1$
    public static final StartpunktType OPPTJENING = new StartpunktType("OPPTJENING"); //$NON-NLS-1$
    public static final StartpunktType BEREGNING = new StartpunktType("BEREGNING"); //$NON-NLS-1$
    public static final StartpunktType UTTAKSVILKÅR = new StartpunktType("UTTAKSVILKÅR"); //$NON-NLS-1$

    private static Map<StartpunktType, Set<VilkårType>> VILKÅR_HÅNDTERT_INNEN_STARTPUNKT = new HashMap<>();
    static {
        // Kontroller arbeidsforhold - ingen vilkår håndter før dette startpunktet
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.put(StartpunktType.KONTROLLER_ARBEIDSFORHOLD,
            new HashSet<>());

        // Opplysningsplikt - ingen vilkår håndter før dette startpunktet
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.put(StartpunktType.INNGANGSVILKÅR_OPPLYSNINGSPLIKT,
            new HashSet<>());

        // Medlemskap
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.put(StartpunktType.INNGANGSVILKÅR_MEDLEMSKAP, VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.values().stream().flatMap(Collection::stream).collect(toSet()));

        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.put(StartpunktType.OPPTJENING, VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.values().stream().flatMap(Collection::stream).collect(toSet()));
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.get(StartpunktType.OPPTJENING).addAll(new HashSet<>(
            Collections.singletonList(VilkårType.MEDLEMSKAPSVILKÅRET)));

        // Beregning
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.put(StartpunktType.BEREGNING, VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.values().stream().flatMap(Collection::stream).collect(toSet()));
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.get(StartpunktType.BEREGNING).addAll(new HashSet<>(
            Arrays.asList(VilkårType.OPPTJENINGSPERIODEVILKÅR
                , VilkårType.OPPTJENINGSVILKÅRET
            )));

        // Uttak
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.put(StartpunktType.UTTAKSVILKÅR, VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.values().stream().flatMap(Collection::stream).collect(toSet()));
        VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.get(StartpunktType.UTTAKSVILKÅR).addAll(new HashSet<>(
            Collections.singletonList(VilkårType.BEREGNINGSGRUNNLAGVILKÅR)
        ));
    }


    @ManyToOne(optional = false)
    @JoinColumn(name = "behandling_steg", nullable = false, updatable = false, insertable = false)
    private BehandlingStegType behandlingSteg;

    @Column(name = "rangering", nullable = false, columnDefinition = "NUMERIC")
    private int rangering;

    @SuppressWarnings("unused")
    private StartpunktType() {
        // Hibernate
    }

    private StartpunktType(final String kode) {
        super(kode);
    }

    public BehandlingStegType getBehandlingSteg() {
        return behandlingSteg;
    }

    public String getTransisjonIdentifikator(){
        return "revurdering-fremover-til-" + behandlingSteg.getKode();
    }


    public static Set<VilkårType> finnVilkårHåndtertInnenStartpunkt(StartpunktType startpunkt) {
        return VILKÅR_HÅNDTERT_INNEN_STARTPUNKT.get(startpunkt);
    }

    public int getRangering() {
        return rangering;
    }
}
