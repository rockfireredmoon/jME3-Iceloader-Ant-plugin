package icemoon.iceloader.ant;

import icemoon.iceloader.tools.AbstractCrypt;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;

public class EncryptDir extends AbstractCrypt {

    public static void main(String[] args) throws Exception {
        EncryptDir ed = new EncryptDir(new File(args[0]), new File(args[1]));
        ed.start();
    }

    EncryptDir(File sourceDir, File targetDir) throws Exception {
        super(sourceDir, targetDir);
    }

    @Override
    protected void doStream(SecretKeySpec secret, File targetFile, Cipher c, File file) throws Exception {
        c.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = c.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        final FileOutputStream out = new FileOutputStream(targetFile);
        DataOutputStream dos = new DataOutputStream(out);
        out.write(header);
        dos.writeLong(file.length());
        out.write(iv.length);
        out.write(iv);
        out.flush();
        CipherOutputStream cos = new CipherOutputStream(out, c);
        try {

            InputStream in = new FileInputStream(file);
            try {
                IOUtils.copy(in, cos);
            } finally {
                in.close();
            }
        } finally {
            cos.flush();
            cos.close();
        }
        targetFile.setLastModified(file.lastModified());
    }
}
