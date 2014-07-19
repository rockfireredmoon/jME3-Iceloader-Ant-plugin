package icemoon.iceloader.ant;

import icemoon.iceloader.EncryptionContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.crypto.spec.SecretKeySpec;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class AssetProcessor extends Task {

    private File srcDir;
    private File destDir;
    private boolean encrypt = true;
    private boolean index = true;
    private Class<? extends EncryptionContext> context;
    private String simpleSalt;
    private String simplePassword;
    private String magic;
    private String cipher;

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }

    public void setEncryptionContextClassName(String className) {
        try {
            context = (Class<? extends EncryptionContext>) Class.forName(className, true, getClass().getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Failed to set encryption context class name to " + className + ".", ex);
        }
    }

    public void setSimplePassword(String password) {
        this.simplePassword = password;
    }

    public void setSimpleSalt(String salt) {
        this.simpleSalt = salt;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    public void setSrcDir(File srcDir) {
        this.srcDir = srcDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public void setMagic(String magic) {
        this.magic = magic;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    @Override
    public void execute() throws BuildException {
        if (simplePassword != null || simpleSalt != null) {
            if (simplePassword == null) {
                throw new BuildException("If you provide simpleSalt, you must also provide simplePassword.");
            }
            if (simpleSalt == null) {
                throw new BuildException("If you provide simplePassword, you must also provide simpleSalt.");
            }
            if (context != null) {
                throw new BuildException("Cannot use a custom encryption context if you provide simplePassword or simpleSalt.");
            }
            try {
                final EncryptionContext defContext = EncryptionContext.get();
                EncryptionContext.set(new EncryptionContext() {
                    @Override
                    public SecretKeySpec createKey() throws Exception {
                        return createKey(simplePassword, simpleSalt);
                    }

                    @Override
                    public String getMagic() {
                        return magic == null ? defContext.getMagic() : magic;
                    }

                    @Override
                    public String getCipher() {
                        return cipher == null ? defContext.getCipher() : cipher;
                    }
                });
            } catch (Exception e) {
                throw new BuildException("Failed to set custom encryption context.");
            }

        } else {
            if (context != null) {
                try {
                    EncryptionContext.set(context.newInstance());
                } catch (Exception e) {
                    throw new BuildException("Failed to set custom encryption context.");
                }
            }
        }

        // Encrypt all assets
        if (encrypt) {
            try {
                EncryptDir dir = new EncryptDir(srcDir, destDir);
                dir.start();
            } catch (Exception ex) {
                throw new BuildException(String.format("Failed to encrypt %s to %s", srcDir, destDir), ex);
            }
        }

        // Create an index file of the assets (helps the choosers in game locate assets where using 
        // reflections is not possible - i.e. when loaded from server on-the-fly)
        if (index) {
            try {
                File indexFileObject = new File(destDir, "index.dat");
                PrintWriter indexWriter = new PrintWriter(new FileOutputStream(indexFileObject), true);
                try {
                    if (!encrypt) {
                        index(indexWriter, indexFileObject, srcDir, srcDir);
                    } else {
                        index(indexWriter, indexFileObject, destDir, destDir);
                    }
                } finally {
                    indexWriter.close();
                }
            } catch (Exception ex) {
                throw new BuildException(String.format("Failed to index %s", destDir), ex);
            }
        }

    }

    public static void main(String[] args) {
    }

    private void index(PrintWriter indexWriter, File indexFile, File rootDir, File dirToIndex) throws IOException {
        for (File f : dirToIndex.listFiles()) {
            if (!f.equals(indexFile)) {
                if (f.isFile()) {
                    log(String.format("Indexing %s", f));
                    String relPath = f.getCanonicalPath().substring(rootDir.getCanonicalPath().length() + 1);
                    long lastMod = f.lastModified();
                    indexWriter.println(relPath + "\t" + lastMod + "\t" + f.length());
                } else if (f.isDirectory()) {
                    index(indexWriter, indexFile, rootDir, f);
                }
            }
        }
    }
}
