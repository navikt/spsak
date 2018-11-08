package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PdpRequest {

    private List<String> fnr;
    private List<String> aktørId;
    private AbacIdToken token;
    private String ansvarligSaksbehandler;

    private String xacmlAction;
    private String xacmlResourceType;
    private String xacmlSakstatus;
    private String xacmlBehandlingStatus;
    private List<String> xacmlAksjonspunktType;

    public List<String> getFnr() {
        return fnr;
    }

    public void setFnr(Set<String> fnr) {
        this.fnr = new ArrayList<>(fnr);
    }

    public void setToken(AbacIdToken token) {
        this.token = token;
    }

    public AbacIdToken getToken() {
        return token;
    }

    public Optional<String> getAnsvarligSaksbehandler() {
        return Optional.ofNullable(ansvarligSaksbehandler);
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public String getXacmlAction() {
        return xacmlAction;
    }

    public String getXacmlResourceType() {
        return xacmlResourceType;
    }

    public Optional<String> getXacmlSakstatus() {
        return Optional.ofNullable(xacmlSakstatus);
    }

    public void setSakstatus(AbacFagsakStatus sakstatus) {
        xacmlSakstatus = sakstatus.getEksternKode();
    }

    public Optional<String> getXacmlBehandlingStatus() {
        return Optional.ofNullable(xacmlBehandlingStatus);
    }

    public void setBehandlingStatus(AbacBehandlingStatus behandlingStatus) {
        xacmlBehandlingStatus = behandlingStatus.getEksternKode();
    }

    public List<String> getXacmlAksjonspunktType() {
        return xacmlAksjonspunktType;
    }

    public void setAksjonspunktType(Collection<String> aksjonspunktType) {
        xacmlAksjonspunktType = new ArrayList<>(aksjonspunktType);
    }

    public void setAction(BeskyttetRessursActionAttributt action) {
        this.xacmlAction = action.getEksternKode();
    }

    public void setResource(BeskyttetRessursResourceAttributt resource) {
        this.xacmlResourceType = resource.getEksternKode();
    }

    public int antallResources() {
        return Math.max(1, antallIdenter()) * Math.max(1, antallAksjonspunktTyper());
    }

    public Optional<String> getFnrForIndex(int index) {
        int antallFnr = antallFnr();
        return antallFnr == 0
                ? Optional.empty()
                : Optional.of(fnr.get(index % antallFnr));
    }

    public Optional<String> getAksjonspunktTypeForIndex(int index) {
        return antallAksjonspunktTyper() == 0
                ? Optional.empty()
                : Optional.of(xacmlAksjonspunktType.get(index / Math.max(antallIdenter(), 1)));
    }

    private int antallIdenter() {
        return antallFnr() + antallAktørId();
    }

    public int getAntallFnr() {
        return antallFnr();
    }

    private int antallAksjonspunktTyper() {
        return xacmlAksjonspunktType != null ? xacmlAksjonspunktType.size() : 0;
    }

    private int antallFnr() {
        return fnr != null ? fnr.size() : 0;
    }

    public List<String> getAktørId() {
        return aktørId;
    }

    public PdpRequest setAktørId(Set<String> aktørId) {
        this.aktørId = new ArrayList<>(aktørId);
        return this;
    }

    public Optional<String> getAktørIdForIndex(int index) {
        int antallAktørId = antallAktørId();
        return antallAktørId == 0
                ? Optional.empty()
                : Optional.of(aktørId.get(index % antallAktørId));
    }

    public int antallAktørId() {
        return aktørId != null ? aktørId.size() : 0;
    }

}
