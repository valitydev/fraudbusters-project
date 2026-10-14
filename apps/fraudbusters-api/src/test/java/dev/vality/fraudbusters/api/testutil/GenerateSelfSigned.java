package dev.vality.fraudbusters.api.testutil;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

public class GenerateSelfSigned {

    public static X509Certificate generateCertificate(KeyPair keyPair) throws CertificateException,
            OperatorCreationException {
        X500Name x500Name = new X500Name("CN=***.com, OU=Security&Defense, O=*** Crypto., L=Ottawa, ST=Ontario, C=CA");
        SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        final Date start = new Date();
        final Date until = Date.from(LocalDate.now().plusDays(365).atStartOfDay().toInstant(ZoneOffset.UTC));
        final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(x500Name,
                new BigInteger(10, new SecureRandom()), start, until, x500Name, pubKeyInfo
        );
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter()
                .setProvider(new BouncyCastleProvider())
                .getCertificate(certificateBuilder.build(contentSigner));
    }
}