package no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.aksjonspunkt;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.behandlingslager.aktør.NavBrukerKjønn;
import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.repository.GrunnlagRepositoryProvider;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;
import no.nav.foreldrepenger.web.app.tjenester.behandling.inntektarbeidytelse.aksjonspunkt.AvklarArbeidsforholdOppdaterer;

public class AvklarArbeidsforholdOppdatererTest {

    private AvklarArbeidsforholdOppdaterer avklarArbeidsforholdOppdaterer;
    private VirksomhetRepository virksomhetRepositoryMock;
    private GrunnlagRepositoryProvider grunnlagRepositoryProvider;
    private TpsTjeneste tpsTjenesteMock;

    @Before
    public void oppsett() {
        this.virksomhetRepositoryMock = Mockito.mock(VirksomhetRepository.class);
        this.grunnlagRepositoryProvider = Mockito.mock(GrunnlagRepositoryProvider.class);
        this.tpsTjenesteMock = Mockito.mock(TpsTjeneste.class);
        Mockito.when(grunnlagRepositoryProvider.getVirksomhetRepository()).thenReturn(virksomhetRepositoryMock);

        this.avklarArbeidsforholdOppdaterer = new AvklarArbeidsforholdOppdaterer(
            null, tpsTjenesteMock, null, null, grunnlagRepositoryProvider);
    }

    @Test
    public void skal_lage_riktig_navn_for_historikkinnslag_når_arbeidsgiver_er_en_virksomhet() {
        String orgNr = "910909088";
        String navn = "Virksomhet O'hoi AS";
        String arbeidsforholdId = "78657859";

        VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
            .medNavn(navn).medOrgnr(orgNr).build();
        Optional<Virksomhet> virksomhetOptional = Optional.of(virksomhet);
        Mockito.when(virksomhetRepositoryMock.hent(orgNr)).thenReturn(virksomhetOptional);

        ArbeidsforholdDto arbeidsforholdDto = new ArbeidsforholdDto();
        arbeidsforholdDto.setArbeidsgiverIdentifikator(orgNr);
        arbeidsforholdDto.setArbeidsforholdId(arbeidsforholdId);
        String historikkInnslagNavn = avklarArbeidsforholdOppdaterer.lagNavn(arbeidsforholdDto);

        String historikkInnslagNavnGenerert = navn + "(" + orgNr + ")" + "..." + arbeidsforholdId.substring(arbeidsforholdId.length() - 4, arbeidsforholdId.length());

        assertEquals(historikkInnslagNavn, historikkInnslagNavnGenerert);
    }

    @Test
    public void skal_lage_riktig_navn_for_historikkinnslag_når_arbeidsgiver_er_en_person() {
        String aktørId = "6991000909092";
        String navn = "Line Lærvik";
        String arbeidsforholdId = "267849897";

        Personinfo personInfo = new Personinfo.Builder()
            .medNavn(navn).medAktørId(new AktørId(aktørId)).medPersonIdent(new PersonIdent("12345678901"))
            .medFødselsdato(LocalDate.of(1989, 10, 7)).medNavBrukerKjønn(NavBrukerKjønn.KVINNE).build();
        Optional<Personinfo> personInfoOptional = Optional.of(personInfo);
        Mockito.when(tpsTjenesteMock.hentBrukerForAktør(any())).thenReturn(personInfoOptional);

        ArbeidsforholdDto arbeidsforholdDto = new ArbeidsforholdDto();
        arbeidsforholdDto.setArbeidsgiverIdentifikator(aktørId);
        arbeidsforholdDto.setArbeidsforholdId(arbeidsforholdId);
        String historikkInnslagNavn = avklarArbeidsforholdOppdaterer.lagNavn(arbeidsforholdDto);

        String historikkInnslagNavnGenerert = navn + "(" + aktørId + ")" + "..." + arbeidsforholdId.substring(arbeidsforholdId.length() - 4, arbeidsforholdId.length());

        assertEquals(historikkInnslagNavn, historikkInnslagNavnGenerert);
    }
}
