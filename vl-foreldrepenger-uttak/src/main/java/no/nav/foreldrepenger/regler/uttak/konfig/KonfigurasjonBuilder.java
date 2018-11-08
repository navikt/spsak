package no.nav.foreldrepenger.regler.uttak.konfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class KonfigurasjonBuilder {

    private Map<Parametertype, Collection<Parameter>> parameterMap;

    private KonfigurasjonBuilder() {
        this.parameterMap = new EnumMap<>(Parametertype.class);
    }

    public static KonfigurasjonBuilder create() {
        return new KonfigurasjonBuilder();
    }

    public KonfigurasjonBuilder leggTilParameter(Parametertype parametertype, LocalDate fom, LocalDate tom, Object verdi) {
        if (!parametertype.getKlasseForVerdier().isAssignableFrom(verdi.getClass())) {
            throw new IllegalArgumentException("Utvikler-feil: kan legge til verdi fra klasse " + verdi.getClass() + " for " + parametertype);
        }

        Collection<Parameter> parameterListe = parameterMap.get(parametertype);
        if (parameterListe == null) {
            Collection<Parameter> coll = new ArrayList<>();
            coll.add(new Parameter(fom, tom, verdi));
            parameterMap.put(parametertype, coll);
        } else {
            Parameter nyttParameter = new Parameter(fom, tom, verdi);
            long count = parameterListe.stream().filter(p -> p.overlapper(nyttParameter.getFom()) ||
                    (nyttParameter.getTom()!=null && p.overlapper(nyttParameter.getTom()))).count();
            if (count > 0L) {
                throw new IllegalArgumentException("Overlappende perioder kan ikke eksistere i konfigurasjon.");
            }
            parameterListe.add(nyttParameter);
        }
        return this;
    }

    public Konfigurasjon build() {
        return new Konfigurasjon(parameterMap);
    }

}
