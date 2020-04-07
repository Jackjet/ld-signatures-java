package info.weboftrust.ldsignatures.verifier;

import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;

import com.github.jsonldjava.core.JsonLdError;

import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.crypto.ByteVerifier;
import info.weboftrust.ldsignatures.suites.SignatureSuite;
import info.weboftrust.ldsignatures.suites.SignatureSuites;
import info.weboftrust.ldsignatures.util.CanonicalizationUtil;
import info.weboftrust.ldsignatures.util.SHAUtil;

public abstract class LdVerifier <SIGNATURESUITE extends SignatureSuite> {

	private final SIGNATURESUITE signatureSuite;

	private ByteVerifier verifier;

	protected LdVerifier(SIGNATURESUITE signatureSuite, ByteVerifier verifier) {

		this.signatureSuite = signatureSuite;
		this.verifier = verifier;
	}

	public static LdVerifier<? extends SignatureSuite> ldVerifierForSignatureSuite(String signatureSuite) {

		if (SignatureSuites.SIGNATURE_SUITE_RSASIGNATURE2018.getTerm().equals(signatureSuite)) return new RsaSignature2018LdVerifier();
		if (SignatureSuites.SIGNATURE_SUITE_ED25519SIGNATURE2018.getTerm().equals(signatureSuite)) return new Ed25519Signature2018LdVerifier();
		if (SignatureSuites.SIGNATURE_SUITE_ECDSAKOBLITZSIGNATURE2016.getTerm().equals(signatureSuite)) return new EcdsaKoblitzSignature2016LdVerifier();
		if (SignatureSuites.SIGNATURE_SUITE_ECDSASECP256L1SIGNATURE2019.getTerm().equals(signatureSuite)) return new EcdsaSecp256k1Signature2019LdVerifier();

		throw new IllegalArgumentException();
	}

	public static LdVerifier<? extends SignatureSuite> ldVerifierForSignatureSuite(SignatureSuite signatureSuite) {

		return ldVerifierForSignatureSuite(signatureSuite.getTerm());
	}

	public abstract boolean verify(byte[] signingInput, LdSignature ldSignature) throws GeneralSecurityException;

	public boolean verify(LinkedHashMap<String, Object> jsonLdObject, LdSignature ldSignature) throws JsonLdError, GeneralSecurityException {

		// check the signature object

		if (! this.getSignatureSuite().getTerm().equals(ldSignature.getType())) throw new GeneralSecurityException("Unexpected signature type: " + ldSignature.getType() + " is not " + this.getSignatureSuite().getTerm());

		// obtain the canonicalized proof options

		LinkedHashMap<String, Object> jsonLdObjectProofOptions = new LinkedHashMap<String, Object> (ldSignature.getJsonLdSignatureObject());
		LdSignature.removeLdSignatureValues(jsonLdObjectProofOptions);
		LdSignature.addSecurityContextToJsonLdObject(jsonLdObjectProofOptions);
		String canonicalizedProofOptions = CanonicalizationUtil.buildCanonicalizedDocument(jsonLdObjectProofOptions);

		// obtain the canonicalized document

		LinkedHashMap<String, Object> jsonLdDocument = new LinkedHashMap<String, Object> (jsonLdObject);
		LdSignature.removeFromJsonLdObject(jsonLdDocument);
		String canonicalizedDocument = CanonicalizationUtil.buildCanonicalizedDocument(jsonLdDocument);

		// verify

		byte[] signingInput = new byte[64];
		System.arraycopy(SHAUtil.sha256(canonicalizedProofOptions), 0, signingInput, 0, 32);
		System.arraycopy(SHAUtil.sha256(canonicalizedDocument), 0, signingInput, 32, 32);

		boolean verify = this.verify(signingInput, ldSignature);

		// done

		return verify;
	}

	public boolean verify(LinkedHashMap<String, Object> jsonLdObject) throws JsonLdError, GeneralSecurityException {

		// obtain the signature object

		LdSignature ldSignature = LdSignature.getFromJsonLdObject(jsonLdObject);
		if (ldSignature == null) return false;

		// done

		return this.verify(jsonLdObject, ldSignature);
	}

	public SignatureSuite getSignatureSuite() {

		return this.signatureSuite;
	}

	/*
	 * Getters and setters
	 */

	public ByteVerifier getVerifier() {

		return this.verifier;
	}

	public void setVerifier(ByteVerifier verifier) {

		this.verifier = verifier;
	}
}
