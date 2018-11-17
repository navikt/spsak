package no.nav.foreldrepenger.web.app.tjenester.fagsak.app;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.behandlingslager.aktør.Personinfo;
import no.nav.foreldrepenger.behandlingslager.fagsak.Fagsak;

// View = respons fra applikasjonslag (velges foreløpig fremfor å la applikasjonslag bruke DTO direkte)
public class FagsakSamlingForBruker {

    private Personinfo brukerInfo;
    private List<FagsakRad> fagsakInfoer = new ArrayList<>();

    private FagsakSamlingForBruker() {
    }

    FagsakSamlingForBruker(Personinfo brukerInfo) {
        this.brukerInfo = brukerInfo;
    }

    static FagsakSamlingForBruker emptyView() {
        return new FagsakSamlingForBruker();
    }

    public Personinfo getBrukerInfo() {
        return brukerInfo;
    }

    public List<FagsakRad> getFagsakInfoer() {
        return fagsakInfoer;
    }

    public boolean isEmpty() {
        return brukerInfo == null;
    }

    void leggTil(Fagsak fagsak, LocalDate dato) {
        fagsakInfoer.add(new FagsakRad(fagsak, dato));
    }

    public static class FagsakRad {
        private final Fagsak fagsak;
        private final LocalDate fødselsdato;

        private FagsakRad(Fagsak fagsak, LocalDate dato) {
            this.fagsak = fagsak;
            this.fødselsdato = dato;
        }

        public Fagsak getFagsak() {
            return fagsak;
        }

        public LocalDate getFødselsdato() {
            return fødselsdato;
        }
    }
}
