package no.nav.vedtak.felles.db.log;

import java.util.NavigableMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Based on http://code.google.com/p/jdbcdslog-exp
 *
 * @author ShunLi
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */
public class LogUtils {

    private static final String NAMED_PARAMETERS_PREFIX = ":";

    private final LogFormatter formatter;

    public LogUtils(LogFormatter dbSpec) {
        this.formatter = dbSpec;
        this.formatter.setLogUtils(this);
    }

    public StringBuilder createLogEntry(String sql, NavigableMap<?, ?> parameters) {
        StringBuilder s = new StringBuilder();

        if (sql != null) {
            int questionMarkCount = 1;
            Pattern p = Pattern.compile("\\?");
            Matcher m = p.matcher(sql);
            StringBuffer buf = new StringBuffer(); // NOSONAR

            while (m.find()) {
                m.appendReplacement(buf, formatter.formatParameter(parameters.get(questionMarkCount)));
                questionMarkCount++;
            }
            sql = String.valueOf(m.appendTail(buf)); // NOSONAR

            s.append(sql);
        }

        return s;
    }

    @SuppressWarnings("unchecked")
    public StringBuilder createLogEntryForNamedParameters(String sql, NavigableMap<?, ?> namedParameters) {
        StringBuilder s = new StringBuilder();

        if (sql != null) {
            if (namedParameters != null && !namedParameters.isEmpty()) {
                for (String key : (Set<String>) namedParameters.keySet()) {
                    sql = sql.replaceAll(NAMED_PARAMETERS_PREFIX + key, formatter.formatParameter(namedParameters.get(key))); // NOSONAR
                }
            }
            s.append(sql);
        }

        return s;
    }

    // Refer apache common lang StringUtils.
    public String replaceEach(String text, String[] searchList, String[] replacementList) {

        // mchyzer Performance note: This creates very few new objects (one major goal)
        // let me know if there are performance requests, we can create a harness to measure

        if ((searchList == null || text == null || replacementList == null)
                || (searchList.length == 0 || replacementList.length == 0 || text.length() == 0)) {
            return text;
        }

        int searchLength = searchList.length;
        int replacementLength = replacementList.length;

        // make sure lengths are ok, these need to be equal
        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        // keep track of which still have matches
        boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        // index on index that the match was found
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1; // NOSONAR

        // index of replace array that will replace the search string found
        // NOTE: logic duplicated below START
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                    searchList[i].length() == 0 || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else {
                if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
        }
        // NOTE: logic mostly below END

        // no search strings found, we are done
        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
        int increase = 0;

        // count the replacement text elements that are larger than their corresponding text being replaced
        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += 3 * greater; // assume 3 matches
            }
        }
        // have upper-bound at 20% increase, then let Java take over
        increase = Math.min(increase, text.length() / 5);

        StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            tempIndex = -1; // NOSONAR
            // find the next earliest match
            // NOTE: logic mostly duplicated above START
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                        searchList[i].length() == 0 || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else {
                    if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = i;
                    }
                }
            }
            // NOTE: logic duplicated above END

        }
        int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        String result = buf.toString(); // NOSONAR

        return result;
    }
}
