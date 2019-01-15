package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge.BrevMottaker;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge.Verge;
import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.verge.VergeAggregat;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

@ApplicationScoped
public class VergeDtoTjenesteImpl implements VergeDtoTjeneste {

    private TpsTjeneste tpsTjeneste;

    VergeDtoTjenesteImpl() {
    }

    @Inject
    public VergeDtoTjenesteImpl(TpsTjeneste tpsTjeneste) {
        this.tpsTjeneste = tpsTjeneste;
    }

    @Override
    public Optional<VergeDto> lagVergeDto(Optional<VergeAggregat> vergeAggregat) {
        if (vergeAggregat.isPresent()) {
            VergeAggregat aggregat = vergeAggregat.get();
            Verge verge = aggregat.getVerge();
            VergeDto dto = new VergeDto();

            dto.setGyldigFom(verge.getGyldigFom());
            dto.setGyldigTom(verge.getGyldigTom());
            dto.setVergeType(verge.getVergeType());
            dto.setMandatTekst(verge.getMandatTekst());

            // TODO(OJR) burde løftes opp til GUI, midlertidlig løsning
            if (verge.getBrevMottaker().equals(BrevMottaker.SØKER)) {
                dto.setSokerErKontaktPerson(true);
                dto.setVergeErKontaktPerson(false);
            } else if (verge.getBrevMottaker().equals(BrevMottaker.VERGE)) {
                dto.setVergeErKontaktPerson(true);
                dto.setSokerErKontaktPerson(false);
            } else if (verge.getBrevMottaker().equals(BrevMottaker.BEGGE)) {
                dto.setVergeErKontaktPerson(true);
                dto.setSokerErKontaktPerson(true);
            }

            // TODO(OJR) endre GUI-kode til å matche
            // Hvis vergen er stønadsmottaker er det tvungen forvaltning
            dto.setSokerErUnderTvungenForvaltning(verge.getStønadMottaker());

            setPersonIdent(aggregat.getAktørId(), dto);

            return Optional.of(dto);
        } else {
            return Optional.empty();
        }
    }

    private void setPersonIdent(AktørId aktørId, VergeDto dto) {
        Optional<Personinfo> personinfoDto = tpsTjeneste.hentBrukerForAktør(aktørId);
        if (personinfoDto.isPresent()) {
            Personinfo personinfo = personinfoDto.get();
            String navn = personinfoDto.map(Personinfo::getNavn).orElse("Ukjent navn"); //$NON-NLS-1$
            dto.setNavn(navn);

            PersonIdent personIdent = personinfo.getPersonIdent();
            dto.setFnr(personIdent.getIdent());
        }
    }
}
