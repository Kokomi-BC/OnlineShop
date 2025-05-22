package src;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class Key {

    public static void main(String[] args) throws Exception {
        // 初始化 Key 生成器，指定算法类型为 RSA
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        // 密钥长度为 2048 位
        keyPairGenerator.initialize(512);
        // 生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 获取公钥
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        // 获取私钥
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        System.out.println("公钥：" + Base64.getEncoder().encodeToString(rsaPublicKey.getEncoded()));

        System.out.println("私钥：" + Base64.getEncoder().encodeToString(rsaPrivateKey.getEncoded()));
    }

}