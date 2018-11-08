package no.nav.foreldrepenger.web.app.tjenester.behandling.personopplysning;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.behandlingslager.behandling.personopplysning.Diskresjonskode;
import no.nav.foreldrepenger.behandlingslager.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.domene.person.TpsTjeneste;
import no.nav.foreldrepenger.domene.typer.AktørId;
import no.nav.foreldrepenger.domene.typer.PersonIdent;

@ApplicationScoped
public class PersonopplysningDtoPersonIdentTjeneste {
    private KodeverkRepository kodeverkRepository;
    private TpsTjeneste tpsTjeneste;

    public PersonopplysningDtoPersonIdentTjeneste() {
    }

    @Inject
    public PersonopplysningDtoPersonIdentTjeneste(KodeverkRepository kodeverkRepository, TpsTjeneste tpsTjeneste) {
        this.kodeverkRepository = kodeverkRepository;
        this.tpsTjeneste = tpsTjeneste;
    }

    // oppdater med foedselsnr
    public void oppdaterMedPersonIdent(PersonopplysningDto personopplysningDto) {
        // memoriser oppslagsfunksjoner - unngår repeterende tjeneste kall eksternt
        Function<AktørId, Optional<PersonIdent>> personInfoFinder = memoize((aktørId) -> tpsTjeneste.hentFnr(aktørId));
        Function<String, Optional<String>> diskresjonskodeFinder = memoize((fnr) -> tpsTjeneste.hentDiskresjonskodeForAktør(new PersonIdent(fnr)));

        // Sett fødselsnummer og diskresjonskodepå personopplysning for alle
        // behandlinger. Fødselsnummer og diskresjonskode lagres ikke i basen og må derfor hentes fra
        // TPS/IdentRepository// for å vises i GUI.
        if (personopplysningDto != null) {
            setFnrPaPersonopplysning(personopplysningDto,
                personInfoFinder,
                diskresjonskodeFinder);
        }

    }

    void setFnrPaPersonopplysning(PersonopplysningDto dto, Function<AktørId, Optional<PersonIdent>> tpsFnrFinder,
                                  Function<String, Optional<String>> tpsKodeFinder) {

        // Soker
        dto.setFnr(findFnr(dto.getAktoerId(), tpsFnrFinder)); // forelder / soeker
        dto.setDiskresjonskode(findKode(dto.getFnr(), tpsKodeFinder));

        // Medsoker
        if (dto.getAnnenPart() != null) {
            dto.getAnnenPart().setFnr(findFnr(dto.getAnnenPart().getAktoerId(), tpsFnrFinder));
            dto.getAnnenPart().setDiskresjonskode(findKode(dto.getAnnenPart().getFnr(), tpsKodeFinder));
            // Medsøkers barn
            if (!dto.getAnnenPart().getBarn().isEmpty()) {
                for (PersonopplysningDto dtoBarn : dto.getAnnenPart().getBarn()) {
                    dtoBarn.setFnr(findFnr(dtoBarn.getAktoerId(), tpsFnrFinder));
                    dtoBarn.setDiskresjonskode(findKode(dtoBarn.getFnr(), tpsKodeFinder));
                }
            }
        }

        // ektefelle
        if (dto.getEktefelle() != null) {
            dto.getEktefelle().setFnr(findFnr(dto.getEktefelle().getAktoerId(), tpsFnrFinder));
            dto.getEktefelle().setDiskresjonskode(findKode(dto.getEktefelle().getFnr(), tpsKodeFinder));
        }

        // Barn
        for (PersonopplysningDto dtoBarn : dto.getBarn()) {
            dtoBarn.setFnr(findFnr(dtoBarn.getAktoerId(), tpsFnrFinder));
            dtoBarn.setDiskresjonskode(findKode(dtoBarn.getFnr(), tpsKodeFinder));
        }
    }

    private Diskresjonskode findKode(String fnr, Function<String, Optional<String>> tpsKodeFinder) {
        if (fnr != null) {
            Optional<String> kode = tpsKodeFinder.apply(fnr);
            if (kode.isPresent()) {
                return kodeverkRepository.finn(Diskresjonskode.class, kode.get());
            }
        }
        return Diskresjonskode.UDEFINERT;
    }

    private String findFnr(AktørId aktørId, Function<AktørId, Optional<PersonIdent>> tpsFnrFinder) {
        return aktørId == null ? null : tpsFnrFinder.apply(aktørId).map(id -> id.getIdent()).orElse(null);

    }

    /** Lag en funksjon som husker resultat av tidligere input. Nyttig for repeterende lookups */
    static <I, O> Function<I, O> memoize(Function<I, O> f) {
        ConcurrentMap<I, O> lookup = new ConcurrentHashMap<>();
        return input -> input == null ? null : lookup.computeIfAbsent(input, f);
    }
}
