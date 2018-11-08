package no.nav.foreldrepenger.dokumentbestiller.api.mal;

import static no.nav.foreldrepenger.dokumentbestiller.doktype.InnvilgelseForeldrepengerMapper.ENDRING_BEREGNING_OG_UTTAK;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandlingsresultat;
import no.nav.foreldrepenger.behandlingslager.behandling.InnsynEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.KonsekvensForYtelsen;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.Terminbekreftelse;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.InnsynRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.MottatteDokumentRepository;
import no.nav.foreldrepenger.behandlingslager.behandling.søknad.Søknad;
import no.nav.foreldrepenger.behandlingslager.behandling.vilkår.Avslagsårsak;
import no.nav.foreldrepenger.dokumentbestiller.api.DokumentBestillerFeil;
import no.nav.foreldrepenger.dokumentbestiller.api.konfig.BrevParametere;
import no.nav.foreldrepenger.dokumentbestiller.api.mal.dto.DokumentTypeMedPerioderDto;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.util.FPDateUtil;
import no.nav.vedtak.util.StringUtils;

public final class DokumentMalFelles {

    private DokumentMalFelles() {
        // Skal ikke instansieres
    }

    public static Behandlingsresultat verifiserBehandlingsresultat(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = behandling.getBehandlingsresultat();
        if (behandlingsresultat == null) {
            throw DokumentBestillerFeil.FACTORY.behandlingManglerResultat(behandling.getId()).toException();
        }
        return behandlingsresultat;
    }

    public static Avslagsårsak verifiserAvslagsårsak(Behandling behandling) {
        Behandlingsresultat behandlingsresultat = verifiserBehandlingsresultat(behandling);
        Avslagsårsak avslagsårsak = behandlingsresultat.getAvslagsårsak();
        if (avslagsårsak == null || !behandlingsresultat.isBehandlingsresultatAvslåttOrOpphørt()) {
            throw DokumentBestillerFeil.FACTORY.behandlingIkkeAvslått(behandling.getId()).toException();
        }
        return avslagsårsak;
    }

    protected static int getAntallBarn(FamilieHendelseGrunnlag familieHendelseGrunnlag) {
        return familieHendelseGrunnlag.getGjeldendeAntallBarn();
    }

    public static Optional<LocalDate> finnTermindato(FamilieHendelseGrunnlag hendelseAggregat) {
        if (hendelseAggregat.getGjeldendeBarna().isEmpty()) {
            return hendelseAggregat.getGjeldendeTerminbekreftelse().map(Terminbekreftelse::getTermindato);
        }
        return Optional.empty();
    }

    public static LocalDate finnSøknadMottattDato(Behandling behandling, MottatteDokumentRepository mottatteDokumentRepository, Optional<Søknad> søknad, InnsynRepository innsynRepository) {
        if (behandling.erInnsyn()) {
            Optional<InnsynEntitet> innsynEntitet = innsynRepository.hentForBehandling(behandling.getId()).stream().findFirst();
            if (!innsynEntitet.isPresent()) {
                return behandling.getOpprettetDato().toLocalDate();
            }
            return innsynEntitet.get().getMottattDato();
        }
        return MottaksdatoBeregner.finnSøknadsdato(mottatteDokumentRepository, søknad, behandling.getId());
    }

    protected static Flettefelt opprettFlettefelt(String feltnavn, String feltverdi) {
        Flettefelt f = new Flettefelt();
        f.setFeltnavn(feltnavn);
        f.setFeltverdi(feltverdi);
        return f;
    }

    protected static void opprettIkkeObligatoriskeFlettefelt(List<Flettefelt> flettefelter, String feltnavn, String feltverdi) {
        if (!StringUtils.nullOrEmpty(feltverdi)) {
            Flettefelt flettefelt = new Flettefelt();

            flettefelt.setFeltnavn(feltnavn);
            flettefelt.setFeltverdi(feltverdi);

            flettefelter.add(flettefelt);
        }
    }

    static Flettefelt opprettStrukturertFlettefelt(String feltnavn, Object feltverdi) {
        Flettefelt f = new Flettefelt();
        f.setFeltnavn(feltnavn);
        f.setStukturertVerdi(feltverdi);
        return f;
    }

    static List<Flettefelt> opprettStrukturertFlettefeltListe(String feltnavn, List<?> feltverdier) {
        List<Flettefelt> liste = new ArrayList<>();
        int nummer = 0;
        for (Object feltverdi : feltverdier) {
            Flettefelt f = new Flettefelt();
            f.setFeltnavn(feltnavn + ":" + nummer);
            f.setStukturertVerdi(feltverdi);
            liste.add(f);
            nummer++;
        }
        return liste;
    }

    static List<Flettefelt> opprettStrukturertFlettefeltListe(String feltnavn, Set<?> feltverdier) {
        return opprettStrukturertFlettefeltListe(feltnavn, new ArrayList<>(feltverdier));
    }

    static Flettefelt opprettObligatoriskeFlettefelt(String feltnavn, Object feltverdi) {
        try {
            return opprettFlettefelt(feltnavn, feltverdi.toString());
        } catch (RuntimeException e) { //NOSONAR
            throw FeilFactory.create(DokumentBestillerFeil.class).feltManglerVerdi(feltnavn).toException();
        }
    }

    static Flettefelt opprettObligatoriskeStrukturertFlettefelt(String feltnavn, Object feltverdi) {
        try {
            return opprettStrukturertFlettefelt(feltnavn, feltverdi.toString());
        } catch (RuntimeException e) { //NOSONAR
            throw FeilFactory.create(DokumentBestillerFeil.class).feltManglerVerdi(feltnavn).toException();
        }
    }

    static LocalDate getSvarFrist(BrevParametere brevParametere) {
        return LocalDate.now(FPDateUtil.getOffset()).plusDays(brevParametere.getSvarfristDager());
    }

    static Optional<String> avklarFritekst(String fritekst, Optional<String> lagretFritekst) {
        if (!StringUtils.nullOrEmpty(fritekst)) {
            return Optional.of(fritekst);
        }
        return lagretFritekst;
    }

    public static String formaterLovhjemler(String hjemmelliste) {
        if (!hjemmelliste.isEmpty()) {
            return formaterLovhjemlerUttak(new LinkedHashSet<>(Arrays.asList(hjemmelliste.split("\n"))), "", false);
        } else {
            return null;
        }
    }

    public static String formaterLovhjemlerForAvslag(DokumentTypeMedPerioderDto dto) {
        if (dto.getLovhjemmelVurdering().isEmpty()) {
            return formaterLovhjemler(dto.getDokumentBehandlingsresultatDto().getLovhjemmelForAvslag());
        }
        return DokumentMalFelles.formaterLovhjemlerUttak(dto.getLovhjemmelVurdering(), dto.getDokumentBehandlingsresultatDto().getKonsekvensForYtelse(), false);
    }

    public static String formaterLovhjemlerForBeregning(String lovhjemmelBeregning, String konsekvensForYtelse, boolean innvilgetRevurdering) {
        if (lovhjemmelBeregning == null) {
            return "";
        }
        if (endringIBeregning(konsekvensForYtelse) || innvilgetRevurdering) {
            lovhjemmelBeregning += (" og forvaltningsloven § 35");
        }
        return lovhjemmelBeregning.replace("folketrygdloven ", "");
    }

    private static boolean endringIBeregning(String konsekvensForYtelse) {
        return KonsekvensForYtelsen.ENDRING_I_BEREGNING.getKode().equals(konsekvensForYtelse)
            || ENDRING_BEREGNING_OG_UTTAK.equals(konsekvensForYtelse);
    }

    public static String formaterLovhjemlerUttak(Set<String> hjemler, String konsekvensForYtelse, boolean innvilgetRevurdering) {
        StringBuilder builder = new StringBuilder();
        builder.append("§ ");
        int antall = 0;
        for (String hjemmel : hjemler) {
            if (hjemmel.trim().isEmpty()) {
                continue;
            } else if (antall > 0) {
                builder.append(", ");
            }
            builder.append(hjemmel);
            antall++;
        }
        if (antall == 0) {
            return "";
        }

        leggTilEkstraSeksjonstegnHvisRelevant(builder, antall);

        if (endringIBeregningEllerInnvilgetRevurdering(innvilgetRevurdering, konsekvensForYtelse)) {
            builder.append(" og forvaltningsloven § 35");
        } else if (antall > 1) {
            // bytt ut siste kommaforekomst med " og ".
            int pos = builder.lastIndexOf(",");
            builder.replace(pos, pos + 2, " og ");
        }
        return builder.toString();
    }

    private static boolean endringIBeregningEllerInnvilgetRevurdering(boolean innvilgetRevurdering, String konsekvensForYtelse) {
        return endringIBeregning(konsekvensForYtelse) || innvilgetRevurdering;
    }

    private static void leggTilEkstraSeksjonstegnHvisRelevant(StringBuilder builder, int antall) {
        if (antall > 1) {
            //legg til ekstra § i starten hvis det er mer enn en
            builder.insert(0, "§");
        }
    }
}
