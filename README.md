# Iceloader Ant

A JME3 plugin library that adds an Ant task for encrypting and indexing assets for
use with the Iceloader plugin.

## Dependencies

This plugin uses an libraries. The actual number needed at runtime will
depend on the usage.

* Commons IO. http://commons.apache.org/proper/commons-io/. Required, I may 
  remove this dependency at some point. Apache 2.0 license.
* Ant.

## Encrypting Your Assets and Creating Indexes

Indexes are used for two things. 

1. To speed up freshness checks when loading assets from a remote server for example using
   EncryptedServerLocator or ServerLocator. The index also contains the last modified
   time so only has to be downloaded once up-front, saving one request per asset.

2. You may have a need to know what assets you have at runtime. I use this for some 
   in game design tools (for creatures and world building). New assets may be uploaded
   by users at any time, so the index is useful to me.

The same tool that is used for indexing is also used to encrypt the assets for upload
to the server that will be supplying them (or used to encrypt classpath resources if
the assets you supply with your game are to be encrypted). 

So, to create indexes and encrypt the assets, you use the provided Ant plugin. Add 
something like the following to your _build-impl.xml_

```
    <target name="compile-assets" depends="init">
        <taskdef name="astproc"
            classname="icemoon.iceloader.ant.AssetProcessor"
            classpath="${javac.classpath}"/>
        <astproc encrypt="true" index="true" srcdir="assets" destdir="enc_assets" simplePassword="password123?" simpleSalt="12345678"/>
    </target>

```

This will create the directory _enc_assets_, you can then upload this entire direwctory
to any HTTP server and use EncryptedServerLocator in your locator list (see below for 
how to configure the location of the server).

## Task attributes

The task supports the following attributes.

| Name  | Type | Default | Description |
| ----- | ---- | ------- | ----------- |
| encrypt | boolean | true | Determines whether assets will will be encrypted. |
| index | boolean | true | Determines whether assets will will be indexed. |
| srcDir | Path | Required | Source location of assets. |
| destDir | Path | Required | Destination location of assets. |
| encryptionContextClassName | Class name | Optional | Fully qualified class name of a custom EncryptionContext (must be on tasks classpath). |
| simplePassword | String | Optional | When default EncryptionContext is in use, the password to use for encryption key. |
| simpleSalt | String | Optional | When default EncryptionContext is in use, the salt to use for encryption key. |
| magic | String | !@ENC/PF_0 | Header used for encrypted files. |
| cipher | String | AES/CFB8/NoPadding | Cipher to use for encrypting files. |
