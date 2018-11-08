package no.nav.vedtak.sikkerhet.pdp.xacml;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.abac.Decision;

public class XacmlResponseWrapper {

    private static final Logger logger = LoggerFactory.getLogger(XacmlResponseWrapper.class);

    private static final String RESPONSE = "Response";
    private static final String DECISION = "Decision";
    private static final String OBLIGATIONS = "Obligations";
    private static final String ADVICE = "AssociatedAdvice";
    public static final String ATTRIBUTE_ASSIGNMENT = "AttributeAssignment";

    private static final String POLICY_IDENTIFIER = "no.nav.abac.attributter.adviceorobligation.deny_policy";
    private static final String DENY_ADVICE_IDENTIFIER = "no.nav.abac.advices.reason.deny_reason";

    private JsonObject responseJson;

    public XacmlResponseWrapper(JsonObject response) {
        this.responseJson = response;
    }

    public List<Obligation> getObligations() {
        JsonValue v = responseJson.get(RESPONSE);
        if (v.getValueType() == JsonValue.ValueType.ARRAY) {
            JsonArray jsonArray = responseJson.getJsonArray(RESPONSE);
            return jsonArray.stream()
                    .map(jsonValue -> getObligationsFromObject((JsonObject) jsonValue))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            return getObligationsFromObject(responseJson.getJsonObject(RESPONSE));
        }
    }

    private List<Obligation> getObligationsFromObject(JsonObject jsonObject) {
        if (jsonObject.containsKey(OBLIGATIONS)) {
            if (jsonObject.get(OBLIGATIONS).getValueType() == JsonValue.ValueType.ARRAY) {
                JsonArray jsonArray = jsonObject.getJsonArray(OBLIGATIONS);
                return jsonArray.stream()
                        .map(jsonValue -> new Obligation((JsonObject) jsonValue))
                        .collect(Collectors.toList());
            } else {
                Obligation obligation = new Obligation(jsonObject.getJsonObject(OBLIGATIONS));
                return Collections.singletonList(obligation);
            }
        }
        return Collections.emptyList();
    }

    public List<Advice> getAdvice() {
        JsonValue v = responseJson.get(RESPONSE);
        if (v.getValueType() == JsonValue.ValueType.ARRAY) {
            JsonArray jsonArray = responseJson.getJsonArray(RESPONSE);
            return jsonArray.stream()
                    .map(jsonValue -> getAdvicefromObject((JsonObject) jsonValue))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            return getAdvicefromObject(responseJson.getJsonObject(RESPONSE));
        }
    }

    private List<Advice> getAdvicefromObject(JsonObject responseObject) {
        if (!responseObject.containsKey(ADVICE)) {
            return Collections.emptyList();
        }
        JsonObject adviceObject = responseObject.getJsonObject(ADVICE);
        if (!DENY_ADVICE_IDENTIFIER.equals(adviceObject.getString("Id"))) {
            return Collections.emptyList();
        }
        if (adviceObject.get(ATTRIBUTE_ASSIGNMENT).getValueType() == JsonValue.ValueType.ARRAY) {
            JsonArray adviceArray = adviceObject.getJsonArray(ATTRIBUTE_ASSIGNMENT);
            return adviceArray.stream()
                    .map(jsonValue -> jsonToAdvice((JsonObject) jsonValue))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            Optional<Advice> advice = jsonToAdvice(adviceObject.getJsonObject(ATTRIBUTE_ASSIGNMENT));
            return advice.isPresent()
                    ? Collections.singletonList(advice.get())
                    : Collections.emptyList();
        }
    }

    private Optional<Advice> jsonToAdvice(JsonObject advice) {
        String attributeId = advice.getString("AttributeId");
        String attributeValue = advice.getString("Value");
        logger.info("Deny advice AttributeId={} Value={}", attributeId, attributeValue);

        if (!POLICY_IDENTIFIER.equals(attributeId)) {
            return Optional.empty();
        }
        switch (attributeValue) {
            case "fp3_behandle_egen_ansatt":
                return Optional.of(Advice.DENY_EGEN_ANSATT);
            case "fp2_behandle_kode7":
                return Optional.of(Advice.DENY_KODE_7);
            case "fp1_behandle_kode6":
                return Optional.of(Advice.DENY_KODE_6);
            default:
                return Optional.empty();
        }
    }

    public List<Decision> getDecisions() {
        JsonValue response = responseJson.get(RESPONSE);
        if (response.getValueType() == JsonValue.ValueType.ARRAY) {
            return responseJson.getJsonArray(RESPONSE).stream()
                    .map(jsonValue -> ((JsonObject) jsonValue).getString(DECISION))
                    .map(Decision::valueOf)
                    .collect(Collectors.toList());

        } else {
            return Collections.singletonList(Decision.valueOf(responseJson.getJsonObject(RESPONSE).getString(DECISION)));
        }
    }

    @Override
    public String toString() {
        return "XacmlResponse(" + responseJson + ")";
    }

}
