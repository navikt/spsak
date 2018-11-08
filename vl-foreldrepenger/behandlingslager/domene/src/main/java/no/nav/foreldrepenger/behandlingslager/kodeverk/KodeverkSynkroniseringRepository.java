package no.nav.foreldrepenger.behandlingslager.kodeverk;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KodeverkSynkroniseringRepository {

    List<Kodeverk> hentKodeverkForSynkronisering();

    List<Kodeliste> hentKodeliste(String kodeverk);

    List<KodelisteRelasjon> hentKodelisteRelasjoner(String kodeverk1, String kode1);

    Map<String, String> hentKodeverkEierNavnMap();

    void opprettNyKode(String kodeverk, String kode, String offisiellKode, String navn, LocalDate fom, LocalDate tom);

    void oppdaterEksisterendeKodeVerk(String kodeverk, String versjon, String uri);

    void oppdaterEksisterendeKode(String kodeverk, String kode, String offisiellKode, String navn, LocalDate fom, LocalDate tom);

    void opprettNyKodeRelasjon(String kodeverk1, String kode1, String kodeverk2, String kode2, LocalDate fom, LocalDate tom);

    boolean eksistererKode(String kodeverk, String kode);

    void oppdaterEksisterendeKodeRelasjon(String kodeverk1, String kode1, String kodeverk2, String kode2, LocalDate fom, LocalDate tom);

    List<KodelisteRelasjon> hentKodelisteRelasjonFor(String kodeverk);

    void lagre();
}
