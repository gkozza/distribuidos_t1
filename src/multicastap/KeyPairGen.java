package multicastap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;

public class KeyPairGen {
private PrivateKey priv;
private PublicKey pub;

public PrivateKey getPriv() {
return priv;
}

public PublicKey getPub() {
return pub;
}

public void writeToFile(String path, byte[] key) throws IOException {
		File f = new File(path);
		f.getParentFile().mkdirs();

		FileOutputStream fos = new FileOutputStream(f);
		fos.write(key);
		fos.flush();
		fos.close();
	}

public KeyPairGen() {
    try {

        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        final SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(2048, random);
        KeyPair pair = keyGen.generateKeyPair();
        this.priv = pair.getPrivate();
        this.pub = pair.getPublic();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

public  byte[] encriptarComChavePrivada(byte[] mensagem, PrivateKey privateKey) {
        byte[] cipherData = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            cipherData = cipher.doFinal(mensagem);
        } catch (Exception ex) {
            System.out.println("Erro Encrypt com chave privada: " + ex.getLocalizedMessage());
            System.out.println(ex.getMessage());
            System.out.println(ex.getClass());
        }
        System.out.println(cipherData.toString());
        return cipherData;
    }

    /**
     * Encripta array de bytes utilizando chave publica
     * 
     * @param mensagem
     * @param publicKey
     * @return byte[] contendo mensagem encriptada
     */
    public  byte[] encriptarComChavePublica(byte[] mensagem, PublicKey publicKey) {
        byte[] cipherData = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherData = cipher.doFinal(mensagem);
        } catch (Exception ex) {
            System.out.println("Erro Encrypt com chave publica: " + ex.getLocalizedMessage());
        }
        System.out.println(cipherData.toString());
        return cipherData;
    }

    /**
     * Decripta um arrey de bytes utilizando chave privada
     * 
     * @param msgEncriptada
     * @param privateKey
     * @return byte[] contendo mensagem decriptada
     */
    public  byte[] decriptarComChavePrivada(byte[] msgEncriptada, PrivateKey privateKey) {
        byte[] cipherData = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            cipherData = cipher.doFinal(msgEncriptada);
        } catch (Exception ex) {
            System.out.println("Erro Decrypt com chave privada: " + ex.getLocalizedMessage());
        }
        return cipherData;
    }

    /**
     * Decripta um arrey de bytes utilizando chave pública
     * 
     * @param msgEncriptada
     * @param publicKey
     * @return byte[] contendo mensagem decriptada
     */
    public  byte[] decriptarComChavePublica(byte[] msgEncriptada, PublicKey publicKey) {
        byte[] cipherData = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            cipherData = cipher.doFinal(msgEncriptada);
        } catch (Exception ex) {
            System.out.println("Erro Decrypt com chave pública: " + ex.getLocalizedMessage());
        }
        return cipherData;
    }


}