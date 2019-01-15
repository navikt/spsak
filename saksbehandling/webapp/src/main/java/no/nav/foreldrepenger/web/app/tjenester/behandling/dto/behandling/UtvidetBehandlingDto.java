package no.nav.foreldrepenger.web.app.tjenester.behandling.dto.behandling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.behandlingslager.geografisk.Språkkode;
import no.nav.foreldrepenger.web.app.rest.ResourceLink;
import no.nav.foreldrepenger.web.app.tjenester.behandling.dto.AsyncPollingStatus;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public class UtvidetBehandlingDto extends BehandlingDto {

    @JsonProperty("behandlingPaaVent")
    private boolean behandlingPåVent;

    @JsonProperty("behandlingKoet")
    private boolean behandlingKøet;

    @JsonProperty("ansvarligSaksbehandler")
    private String ansvarligSaksbehandler;

    @JsonProperty("fristBehandlingPaaVent")
    private String fristBehandlingPåVent;

    @JsonProperty("venteArsakKode")
    private String venteÅrsakKode;

    @JsonProperty("sprakkode")
    private Språkkode språkkode;

    @JsonProperty("behandlingHenlagt")
    private boolean behandlingHenlagt;

    @JsonProperty("toTrinnsBehandling")
    private boolean toTrinnsBehandling;

    @JsonProperty("behandlingsresultat")
    private BehandlingsresultatDto behandlingsresultat;

    @JsonProperty("originalBehandlingId")
    private Long originalBehandlingId;

    /**
     * REST HATEOAS - pekere på data innhold som hentes fra andre url'er, eller handlinger som er tilgjengelig på behandling.
     *
     * @see https://restfulapi.net/hateoas/
     */
    @JsonProperty("links")
    private List<ResourceLink> links = new ArrayList<>();

    /** Eventuelt async status på tasks. */
    @JsonProperty("taskStatus")
    private AsyncPollingStatus taskStatus;

    @JsonProperty("behandlingArsaker")
    private List<BehandlingÅrsakDto> behandlingÅrsaker;

    public boolean isBehandlingPåVent() {
        return behandlingPåVent;
    }

    public String getAnsvarligSaksbehandler() {
        return ansvarligSaksbehandler;
    }

    public AsyncPollingStatus getTaskStatus() {
        return taskStatus;
    }

    public String getFristBehandlingPåVent() {
        return fristBehandlingPåVent;
    }

    public String getVenteÅrsakKode() {
        return venteÅrsakKode;
    }

    public Språkkode getSpråkkode() {
        return språkkode;
    }

    public boolean isBehandlingHenlagt() {
        return behandlingHenlagt;
    }

    public boolean getToTrinnsBehandling() {
        return toTrinnsBehandling;
    }

    public Long getOriginalBehandlingId() {
        return originalBehandlingId;
    }

    void setBehandlingPåVent(boolean behandlingPåVent) {
        this.behandlingPåVent = behandlingPåVent;
    }

    void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    void setFristBehandlingPåVent(String fristBehandlingPåVent) {
        this.fristBehandlingPåVent = fristBehandlingPåVent;
    }

    void setVenteÅrsakKode(String venteÅrsakKode) {
        this.venteÅrsakKode = venteÅrsakKode;
    }

    void setSpråkkode(Språkkode språkkode) {
        this.språkkode = språkkode;
    }

    void setBehandlingHenlagt(boolean behandlingHenlagt) {
        this.behandlingHenlagt = behandlingHenlagt;
    }

    void setToTrinnsBehandling(boolean toTrinnsBehandling) {
        this.toTrinnsBehandling = toTrinnsBehandling;
    }

    public List<ResourceLink> getLinks() {
        return Collections.unmodifiableList(links);
    }

    void leggTil(ResourceLink link) {
        links.add(link);
    }

    public void setAsyncStatus(AsyncPollingStatus asyncStatus) {
        this.taskStatus = asyncStatus;
    }

    public List<BehandlingÅrsakDto> getBehandlingÅrsaker() {
        return behandlingÅrsaker;
    }

    void setBehandlingArsaker(List<BehandlingÅrsakDto> behandlingÅrsaker) {
        this.behandlingÅrsaker = behandlingÅrsaker;
    }

    void setBehandlingsresultatDto(BehandlingsresultatDto behandlingsresultatDto) {
        this.behandlingsresultat = behandlingsresultatDto;
    }

    public BehandlingsresultatDto getBehandlingsresultat() {
        return behandlingsresultat;
    }

    public boolean isBehandlingKoet() {
        return behandlingKøet;
    }

    public void setBehandlingKøet(boolean behandlingKøet) {
        this.behandlingKøet = behandlingKøet;
    }

    public void setOriginalBehandlingId(Long originalBehandlingId) {
        this.originalBehandlingId = originalBehandlingId;
    }
}
