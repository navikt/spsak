package no.nav.foreldrepenger.behandlingslager.testutilities.fagsak;

import java.time.LocalDate;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.RelasjonsRolleType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.behandlingslager.testutilities.aktør.NavBrukerBuilder;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

/**
 * Builder for å bygge enkle fagsaker. Primært for test.
 */
public class FagsakBuilder {

    private Saksnummer saksnummer;

    private NavBrukerBuilder brukerBuilder = new NavBrukerBuilder();

    private FagsakRelasjonBuilder fagsakRelasjonBuilder;
    private RelasjonsRolleType rolle;

    private Fagsak fagsak;

    private FagsakBuilder(RelasjonsRolleType rolle, FagsakRelasjonBuilder fagsakRelasjonBuilder) {
        this.rolle = rolle;
        this.fagsakRelasjonBuilder = fagsakRelasjonBuilder;
    }

    private FagsakBuilder(Fagsak fagsak) {
        this.fagsak = fagsak;
    }

    public FagsakBuilder medSaksnummer(Saksnummer saksnummer) {
        validerFagsakIkkeSatt();
        this.saksnummer = saksnummer;
        return this;
    }

    private void validerFagsakIkkeSatt() {
        if (fagsak != null) {
            throw new IllegalStateException("Fagsak er allerede konfigurert, kan ikke overstyre her");
        }
    }

    public RelasjonsRolleType getBrukerRolle() {
        return rolle;
    }

    public FagsakBuilder medBrukerAktørId(AktørId aktørId) {
        validerFagsakIkkeSatt();
        brukerBuilder.medAktørId(aktørId);
        return this;
    }

    public FagsakBuilder medBrukerKjønn(NavBrukerKjønn kjønn) {
        validerFagsakIkkeSatt();
        brukerBuilder.medKjønn(kjønn);
        return this;
    }

    public FagsakBuilder medBrukerPersonInfo(Personinfo personinfo) {
        validerFagsakIkkeSatt();
        brukerBuilder.medPersonInfo(personinfo);
        return this;
    }

    public NavBrukerBuilder getBrukerBuilder() {
        return brukerBuilder;
    }

    public RelasjonsRolleType getRolle() {
        return rolle;
    }

    public FagsakBuilder medBruker(NavBruker bruker) {
        validerFagsakIkkeSatt();
        brukerBuilder.medBruker(bruker);
        return this;
    }

    public static FagsakBuilder enkel(Fagsak fagsak) {
        return new FagsakBuilder(fagsak);
    }

    public static FagsakBuilder nyFagsak(FagsakYtelseType fagsakYtelseType, RelasjonsRolleType rolle) {
        if (fagsakYtelseType.equals(FagsakYtelseType.ENGANGSTØNAD)) {
            return nyEngangstønad(rolle);
        }
        if (fagsakYtelseType.equals(FagsakYtelseType.FORELDREPENGER)) {
            return nyForeldrepengesak(rolle);
        }
        throw new IllegalStateException("Utviklerfeil: Kan ikke opprette fagsak for udefinert FagsakYtelseType");
    }

    public static FagsakBuilder nyEngangstønad(RelasjonsRolleType rolle) {
        return new FagsakBuilder(rolle, FagsakRelasjonBuilder.engangsstønad());
    }

    public static FagsakBuilder nyForeldrepengesak(RelasjonsRolleType rolle) {
        return new FagsakBuilder(rolle, FagsakRelasjonBuilder.foreldrepenger());
    }

    public static FagsakBuilder nyEngangstønadForMor() {
        return new FagsakBuilder(RelasjonsRolleType.MORA, FagsakRelasjonBuilder.engangsstønad());
    }

    public static FagsakBuilder nyForeldrepengerForMor() {
        return new FagsakBuilder(RelasjonsRolleType.MORA, FagsakRelasjonBuilder.foreldrepenger());
    }

    public FagsakBuilder medFødseldato(LocalDate dato) {
        validerFagsakIkkeSatt();
        fagsakRelasjonBuilder.medFødseldato(dato);
        return this;
    }

    public FagsakBuilder medAdopsjonsdato(LocalDate dato) {
        validerFagsakIkkeSatt();
        fagsakRelasjonBuilder.medAdopsjonsdato(dato);
        return this;
    }

    public FagsakBuilder medTermindato(LocalDate dato) {
        validerFagsakIkkeSatt();
        fagsakRelasjonBuilder.medTermindato(dato);
        return this;
    }

    public Fagsak build() {

        if (fagsak != null) {
            return fagsak;
        } else {
            fagsakRelasjonBuilder.setDefaults();
            fagsak = Fagsak.opprettNy(fagsakRelasjonBuilder.getYtelseType(), brukerBuilder.build(), rolle, saksnummer);
            return fagsak;
        }

    }
}
