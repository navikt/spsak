package no.nav.foreldrepenger.fordel.web.server;

public final class PropertyUtil {
    
	private PropertyUtil() {
		// hidden ctor
	}
	
	public static String getProperty(String key) {
		String val = System.getProperty(key);
		if (val == null) {
			String envKey = key.toUpperCase().replace('.', '_');
			val = System.getenv(envKey);
		}
		return val;
	}

	public static Integer getPropertyAsInt(String key) {
		String val = getProperty(key);
		return val == null ? null : Integer.valueOf(val);
	}

	public static boolean getPropertyAsBoolean(String key) {
		String val = getProperty(key);
		return val == null ? null : Boolean.parseBoolean(val);  // NOSONAR
	}
}
