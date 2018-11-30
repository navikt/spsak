package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.AdresseType;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.OffentligYtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.PensjonTrygdType;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Diskresjonskode;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.SivilstandType;
import no.nav.foreldrepenger.behandlingslager.geografisk.Fylker;
import no.nav.foreldrepenger.behandlingslager.geografisk.Geopolitisk;
import no.nav.foreldrepenger.behandlingslager.geografisk.Kommuner;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landgrupper;
import no.nav.foreldrepenger.behandlingslager.geografisk.LandkodeISO2;
import no.nav.foreldrepenger.behandlingslager.geografisk.Landkoder;
import no.nav.foreldrepenger.behandlingslager.geografisk.Poststed;
import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
/**
 * Lager script for å slette kodeverk (fra kodeliste, kodeliste_relasjon, osv.).
 * Håndterer ikke dersom andre referenasedata refereer tilbake til kodeliste (eks. AKSJONSPUNKT_DEF#SKJERMLENKE_TYPE)
 */
public class LagSlettKodelisteScript {

    public static void main(String[] args) throws Exception {

        IndexClasses index = IndexClasses.getIndexFor(NavBrukerKjønn.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        List<Class<?>> kodelister = index.getSubClassesOf(Kodeliste.class);

        /** Klasser som synkes fra Kodeverkforvaltning og dermed ikke nødvendigvis har konstanter definert i klassen som representerer kodeverket. */
        Set<Class<?>> exceptions = Set.of(NavBrukerKjønn.class, Landkoder.class, Poststed.class, Språkkode.class, Diskresjonskode.class, SivilstandType.class,
            AdresseType.class, ArkivFilType.class, BehandlingTema.class, Kommuner.class, LandkodeISO2.class, Geopolitisk.class, Landgrupper.class, Fylker.class,
            DokumentKategori.class, MottakKanal.class, VariantFormat.class, DokumentTypeId.class, PensjonTrygdType.class,
            OffentligYtelseType.class);

        Set<Class<?>> classes = new LinkedHashSet<>(kodelister);
        classes.removeAll(exceptions);

        for (Class<?> c : classes) {
            DiscriminatorValue ann = c.getAnnotation(DiscriminatorValue.class);

            if (ann == null) {
                continue;
            }
            String kodeverk = ann.value();

            Set<String> koder = new LinkedHashSet<>();
            for (Field f : Set.of(c.getDeclaredFields())) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == c) {
                    f.setAccessible(true);
                    koder.add(((Kodeliste) f.get(c)).getKode());
                }
            }

            if (!koder.isEmpty()) {
                String kodejoin = "'" + String.join("\', \'", koder) + "'";

                System.out.println(
                    String.format(
                        "DELETE FROM KODELISTE_RELASJON WHERE KODEVERK1='%1$s' and kode1 NOT IN (%2$s);\n" +
                            "DELETE FROM KODELISTE_RELASJON WHERE KODEVERK2='%1$s' and kode2 NOT IN (%2$s);\n" +
                            "DELETE FROM KODELISTE_NAVN_I18N WHERE KL_KODEVERK = '%1$s' AND KL_KODE NOT IN (%2$s);\n" +
                            "DELETE FROM KODELISTE WHERE KODEVERK = '%1$s' and kode NOT IN (%2$s);\n" +
                            "",
                        kodeverk, kodejoin));

            }
        }
    }

}
