package info.weboftrust.ldsignatures.util;

import java.util.HashMap;
import java.util.Map;

import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;

public class CanonicalizationUtil {

	private CanonicalizationUtil() {

	}

	public static String buildCanonicalizedDocument(Object jsonLdObject) throws JsonLdError {

		// canonicalize the JSON-LD

		JsonLdOptions options = new JsonLdOptions();
		options.format = JsonLdConsts.APPLICATION_NQUADS;
		String canonicalizedDocument = (String) JsonLdProcessor.normalize(jsonLdObject, options);

		// done

		return canonicalizedDocument;
	}

	public static void fixImplicitGraph(Map<String, Object> jsonLdObject) {

		HashMap<String, Object> implicitGraph = new HashMap<String, Object> ();
		implicitGraph.putAll(jsonLdObject);
		jsonLdObject.clear();
		jsonLdObject.put("@graph", implicitGraph);
	}

	public static void fixImplicitGraph(Map<String, Object> jsonLdObject, String property) {

		fixImplicitGraph((Map<String, Object>) jsonLdObject.get(property));
	}
}
