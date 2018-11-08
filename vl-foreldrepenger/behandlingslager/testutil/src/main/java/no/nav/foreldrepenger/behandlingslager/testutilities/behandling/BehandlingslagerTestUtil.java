package no.nav.foreldrepenger.behandlingslager.testutilities.behandling;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBruker;
import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.Behandling;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlag;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.FamilieHendelseGrunnlagBuilder;
import no.nav.foreldrepenger.behandlingslager.behandling.familiehendelse.HendelseVersjonType;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;
import no.nav.foreldrepenger.behandlingslager.fagsak.FagsakYtelseType;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.domene.typer.Saksnummer;

public class BehandlingslagerTestUtil {


    private BehandlingslagerTestUtil() {
    }

    public static final Fagsak buildFagsak(final Long fagsakid, final boolean erAvsluttet, FagsakYtelseType ytelseType) {
        NavBruker bruker = lagNavBruker();
        Fagsak fagsak = Fagsak.opprettNy(ytelseType, bruker, null, new Saksnummer(fagsakid * 2 + ""));
        fagsak.setId(fagsakid);
        if (erAvsluttet) {
            fagsak.setAvsluttet();
        }
        return fagsak;
    }

    public static final NavBruker lagNavBruker() {
        Personinfo.Builder personinfoBuilder = new Personinfo.Builder();
        personinfoBuilder.medAktørId(new AktørId("17777"));
        personinfoBuilder.medPersonIdent(new PersonIdent("01017012345"));
        personinfoBuilder.medNavn("Tjoms");
        personinfoBuilder.medFødselsdato(LocalDate.now());
        personinfoBuilder.medNavBrukerKjønn(NavBrukerKjønn.KVINNE);
        Personinfo personinfo = personinfoBuilder.build();

        NavBruker navBruker = NavBruker.opprettNy(personinfo);
        return navBruker;
    }

    public static final Behandling byggBehandlingFødsel(final Fagsak fagsakFødsel) {
        Behandling.Builder behandlingBuilder = Behandling.forFørstegangssøknad(fagsakFødsel);
        return behandlingBuilder.build();
    }

    public static final FamilieHendelseGrunnlag byggFødselGrunnlag(LocalDate termindato, LocalDate fødselsdato) {
        final FamilieHendelseBuilder hendelseBuilder = FamilieHendelseBuilder.oppdatere(Optional.empty(), HendelseVersjonType.SØKNAD);
        if (termindato != null) {
            hendelseBuilder.medTerminbekreftelse(hendelseBuilder.getTerminbekreftelseBuilder()
                .medUtstedtDato(termindato.minusDays(40))
                .medTermindato(termindato)
                .medNavnPå("NAVN"));
        }
        if (fødselsdato != null) {
            hendelseBuilder.medFødselsDato(fødselsdato);
        }
        return FamilieHendelseGrunnlagBuilder.oppdatere(Optional.empty())
            .medSøknadVersjon(hendelseBuilder)
            .build();
    }

}
