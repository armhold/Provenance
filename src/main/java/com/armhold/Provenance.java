package com.armhold;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.util.*;

/**
 * Analyzes a directory of jar files and determines their Maven coordinates via the Sonatype checksum search API.
 * Very helpful for migrating Ant projects to Maven.
 *
 * Run like:
 *
 *   java Provenance lib_dir
 *
 * Will print out Maven dependency info to stdout; you can then use this to build your pom.xml.
 *
 */
public class Provenance
{
    public static final String REST_BASE_URL = "https://repository.sonatype.org/service/local/lucene/search?sha1=";
    private MessageDigest md;

    public Provenance()
    {
        try
        {
          md = MessageDigest.getInstance("SHA-1");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * retrieve Maven coordinates via Sonatype checksum search REST API
     *
     * @param sha1Hash the hash of the jar file
     * @return the maven dependency stanza in XML format
     */
    private String getXMLDependencyStanza(String sha1Hash)
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(REST_BASE_URL + sha1Hash).openStream());

            NodeList nl = doc.getElementsByTagName("artifact");
            if (nl.getLength() > 0)
            {
                String groupId = ((Element) nl.item(0)).getElementsByTagName("groupId").item(0).getTextContent();
                String artifactId = ((Element) nl.item(0)).getElementsByTagName("artifactId").item(0).getTextContent();
                String version  = ((Element) nl.item(0)).getElementsByTagName("version").item(0).getTextContent();

                StringBuilder b  = new StringBuilder("<dependency>\n");
                b.append("   <groupId>");
                b.append(groupId);
                b.append("</groupId>\n");

                b.append("   <artifactId>");
                b.append(artifactId);
                b.append("</artifactId>\n");

                b.append("   <version>");
                b.append(version);
                b.append("</version>\n");

                b.append("</dependency>\n");

                return b.toString();
            }

            return null;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the SHA-1 hash for the given data
     */
    public String computeSHA(byte[] data)
    {
        return byteArrayToHexString(md.digest(data));
    }

    /**
     * from: http://stackoverflow.com/questions/4895523/java-string-to-sha1/4895572#4895572
     *
     * @return a hex string representation of the given byte data
     */
    private String byteArrayToHexString(byte[] data)
    {
        String result = "";
        for (byte b : data)
        {
            result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }

    public void importJarFiles(String dir)
    {
        for (File file : getJarFiles(new File(dir)))
        {
            byte[] data = readFile(file);
            String sha = computeSHA(data);
            String xml = getXMLDependencyStanza(sha);

            if (xml == null)
            {
                xml = "<dependency>\n   <groupId>unknown</groupId>\n   <artifactId>" + file.getName() + "</artifactId>\n   <version>unknown</version>\n</dependency>";
            }

            StringBuilder b = new StringBuilder();
            b.append("<!-- ").append(file.getAbsoluteFile()).append(" -->\n");
            b.append("<!-- SHA1: ").append(sha).append(" -->\n");
            b.append(xml);
            b.append("\n");

            System.out.println(b.toString());
        }
    }

    /**
     * @return the file contents in a byte array
     */
    private byte[] readFile(File file)
    {
        byte[] result;

        try
        {
            InputStream is = new FileInputStream(file);

            result = new byte[(int) file.length()];

            int offset = 0;
            int numRead;
            while (offset < result.length && (numRead = is.read(result, offset, result.length - offset)) >= 0)
            {
                offset += numRead;
            }

            is.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return result;
    }


    /**
     * @return a list of .jar files contained in the given directory by traversing it recursively
     */
    private List<File> getJarFiles(File dir)
    {
        List<File> result = new ArrayList<File>();

        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.getName().toLowerCase().endsWith(".jar");
            }
        };

        // stack of files/dirs we need to visit
        Stack<File> stack = new Stack<File>();

        // seed the stack with all the top-level files in the dir
        stack.addAll(Arrays.asList(dir.listFiles()));

        while (! stack.isEmpty())
        {
            File current = stack.pop();

            // if it's a directory, add its immediate children to the stack
            if (current.isDirectory())
            {
                // add all files under current to the stack
                for (File file : current .listFiles())
                {
                    stack.push(file);
                }
            }
            else  // if it's a jar file, add it to result
            {
                if (fileFilter.accept(current))
                {
                    result.add(current);
                }
            }

        }

        return result;
    }

    /**
     * a handy way to run the importer via Maven:
     * *
     * $ mvn compile exec:java -Dexec.mainClass="com.armhold.Provenance" -Dexec.args="lib_dir"
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.err.println("Usage: java com.armhold.Provenance lib_dir (where lib_dir contains your *.jar files)");
            System.exit(1);
        }

        Provenance provenance = new Provenance();
        provenance.importJarFiles(args[0]);
    }

}
