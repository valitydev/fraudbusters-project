package dev.vality.fraudbusters.mg.connector.service;


import dev.vality.damsel.domain.Reference;
import dev.vality.damsel.domain.WalletConfig;
import dev.vality.damsel.domain.WalletConfigRef;
import dev.vality.damsel.domain_config_v2.*;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoNotFoundException;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DominantClientService {

    private final RepositoryClientSrv.Iface dominantClient;

    public WalletConfig getWalletConfig(String walletId) {
        log.debug("Trying to get wallet config, walletId='{}'", walletId);
        Reference revisionReference = new Reference();
        WalletConfigRef walletConfigRef = new WalletConfigRef();
        walletConfigRef.setId(walletId);
        revisionReference.setWalletConfig(walletConfigRef);
        VersionedObject versionedObject = getVersionedObject(revisionReference);
        return versionedObject.getObject().getWalletConfig().getData();

    }

    private VersionedObject getVersionedObject(Reference reference) {
        VersionReference versionRef = new VersionReference();
        versionRef.setHead(new Head());
        try {
            return dominantClient.checkoutObject(versionRef, reference);
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new PaymentInfoNotFoundException(String.format("Version not found, objectRef='%s', versionRef='%s'",
                    reference, versionRef), ex);
        } catch (TException ex) {
            throw new PaymentInfoRequestException(String.format("Failed to get object, objectRef='%s', " +
                    "versionRef='%s'", reference, versionRef), ex);
        }
    }
}
