package icemoon.iceloader.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class AssetProcessor extends Task {

    private File srcDir;
    private File destDir;
    private boolean encrypt = true;
    private boolean index = true;

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
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

    @Override
    public void execute() throws BuildException {

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
                final File indexFile = new File(destDir, "index.dat");
                PrintWriter indexWriter = new PrintWriter(new FileOutputStream(indexFile), true);
                try {
                    index(indexWriter, indexFile, destDir, destDir);
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

    private void index(PrintWriter indexWriter, File indexFile, File rootDir, File destDir) throws IOException {
        for (File f : destDir.listFiles()) {
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
