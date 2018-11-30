package no.nav.foreldrepenger.domene.beregningsgrunnlag.adapter.util;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.inntektarbeidytelse.Arbeidsgiver;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.Virksomhet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetEntitet;
import no.nav.foreldrepenger.behandlingslager.behandling.virksomhet.VirksomhetRepository;
import no.nav.foreldrepenger.domene.typer.AktørId;

@ApplicationScoped
public class BeregningArbeidsgiverTestUtil {

    private VirksomhetRepository virksomhetRepository;
    private static final String BEREGNINGVIRKSOMHET = "Beregningvirksomhet";

    BeregningArbeidsgiverTestUtil() {
        // for CDI
    }

    @Inject
    public BeregningArbeidsgiverTestUtil(VirksomhetRepository virksomhetRepository) {
        this.virksomhetRepository = virksomhetRepository;
    }

    public Arbeidsgiver forArbeidsgiverVirksomhet(String orgnr) {
        return Arbeidsgiver.virksomhet(lagVirksomhet(orgnr));
    }

    public Arbeidsgiver forArbeidsgiverpPrivatperson(String aktørId) {
        return Arbeidsgiver.person(new AktørId(aktørId));
    }

    private VirksomhetEntitet lagVirksomhet(String orgnr) {
        Optional<Virksomhet> virksomhetOpt = virksomhetRepository.hent(orgnr);
        if (!virksomhetOpt.isPresent()) {
            VirksomhetEntitet virksomhet = new VirksomhetEntitet.Builder()
                .medOrgnr(orgnr)
                .medNavn(BEREGNINGVIRKSOMHET)
                .oppdatertOpplysningerNå()
                .build();
            virksomhetRepository.lagre(virksomhet);
            return virksomhet;
        } else {
            return (VirksomhetEntitet) virksomhetOpt.get();
        }
    }
}
