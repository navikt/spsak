package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.behandling.BehandlingTema;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentGruppe;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentKategori;
import no.nav.foreldrepenger.behandlingslager.behandling.DokumentTypeId;
import no.nav.foreldrepenger.behandlingslager.behandling.MottakKanal;
import no.nav.foreldrepenger.behandlingslager.behandling.VariantFormat;
import no.nav.foreldrepenger.behandlingslager.behandling.beregningsgrunnlag.BeregningsgrunnlagAndeltype;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkAvklartSoeknadsperiodeType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkBegrunnelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkEndretFeltVerdiType;
import no.nav.foreldrepenger.behandlingslager.behandling.historikk.HistorikkResultatType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.YtelseType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidType;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.ArbeidTypeKode;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsFilter;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.InntektsFormål;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseResultat;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseSakstype;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseStatus;
import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.kodeverk.RelatertYtelseTema;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Diskresjonskode;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.OpplysningsKilde;
import no.nav.foreldrepenger.behandlingslager.geografisk.Geopolitisk;
import no.nav.foreldrepenger.behandlingslager.geografisk.LandkodeISO2;
import no.nav.foreldrepenger.behandlingslager.geografisk.Poststed;
import no.nav.foreldrepenger.behandlingslager.hendelser.ForretningshendelseType;
import no.nav.foreldrepenger.behandlingslager.kodeverk.arkiv.ArkivFilType;
import no.nav.foreldrepenger.behandlingslager.uttak.IkkeOppfyltÅrsak;
import no.nav.foreldrepenger.behandlingslager.uttak.UttakPeriodeVurderingType;

/** Sjekker hvilke kodeverk som faktisk brukes i entitetene. Outputter liste av de som ikke er brukt (disse er kandidater til å fjernes). */
public class SjekkHvilkeKodelisterBlirFaktiskLagret {

    public static void main(String[] args) throws Exception {

        // disse brukes ikke direkte i lagring men er oppslagsverk (evt. superklasser for andre oppslagsverk) og brukes i integritet sjekker
        var whitelist = Set.of(
            Poststed.class,
            Geopolitisk.class,
            LandkodeISO2.class,
            YtelseType.class,
            ArbeidType.class,
            ArbeidTypeKode.class,
            IkkeOppfyltÅrsak.class);

        // disse brukes ikke direkte i lagring men er oppslagsverk (evt. superklasser for andre oppslagsverk) og brukes i integritet sjekker. Kan
        // også være de lagres som string verdier (gjeldre spesielt historikk felter)
        var whitelistHistorikk = Set.of(
            HistorikkAvklartSoeknadsperiodeType.class,
            HistorikkEndretFeltType.class,
            HistorikkBegrunnelseType.class,
            HistorikkEndretFeltVerdiType.class,
            HistorikkResultatType.class);

        // kandidater til å endres for spsak
        var greylist = Set.of(
            DokumentTypeId.class // denne lastes fra kodeverkklienten
            );

        // kandidater som skal slettes helt fra spsak
        var killList = Set.of();

        // kandidater som kan skrives om til enum (trenger ikke integritetssjekk da de ikke lagres på entiteter)
        var kandidaterTilEnum_må_gjennomgås_for_om_de_brukes_til_oppslag = Set.of(
            VariantFormat.class,
            ArkivFilType.class,
            DokumentKategori.class,
            
            RelatertYtelseTema.class,
            RelatertYtelseStatus.class,
            OpplysningsKilde.class,
            UttakPeriodeVurderingType.class,
            DokumentGruppe.class,
            
            RelatertYtelseSakstype.class,
            InntektsFilter.class,
            ForretningshendelseType.class,
            BehandlingTema.class,
            RelatertYtelseResultat.class,
            Diskresjonskode.class,
            BeregningsgrunnlagAndeltype.class,
            InntektsFormål.class,
            MottakKanal.class
            );

        IndexClasses index = IndexClasses.getIndexFor(NavBrukerKjønn.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        var kodelister = index.getSubClassesOf(Kodeliste.class);

        var brukteKodelister = new HashSet<>();

        var entityClasses = index.getClassesWithAnnotation(Entity.class);

        for (Class<?> e : entityClasses) {
            Class<?> me = e;

            while (me != null && (me == e || me.getSuperclass() != Object.class)) {
                for (Field f : me.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && erKodeliste(f.getType())) {
                        System.out.println("Brukt: " + f.getDeclaringClass().getName() + "#" + f.getName());
                        brukteKodelister.add(f.getType());
                    }
                }
                me = me.getSuperclass();
            }

        }

        var ubrukteKodelister = new HashSet<>(kodelister);
        ubrukteKodelister.removeAll(whitelist);
        ubrukteKodelister.removeAll(whitelistHistorikk);
        ubrukteKodelister.removeAll(greylist);
        ubrukteKodelister.removeAll(killList);
        ubrukteKodelister.removeAll(kandidaterTilEnum_må_gjennomgås_for_om_de_brukes_til_oppslag);
        ubrukteKodelister.removeAll(brukteKodelister);

        // VilkårKategori
        // SøknadtypeTillegg

        for (Class<?> c : ubrukteKodelister) {
            var ann = c.getAnnotation(DiscriminatorValue.class);

            if (ann == null) {
                continue;
            }

            boolean skip = false;
            for (Class<?> w : whitelist) {
                if (w.isAssignableFrom(c)) {
                    skip = true;
                    break;
                }
            }

            if (!skip) {
                var kodeverk = ann.value();
                System.err.println("Ubrukt? " + c.getName() + ": " + kodeverk);
            }
        }
    }

    private static boolean erKodeliste(Class<?> class1) {
        return (Kodeliste.class.isAssignableFrom(class1)) || (KodeverkTabell.class.isAssignableFrom(class1));
    }
}
