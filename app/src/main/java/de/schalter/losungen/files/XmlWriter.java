package de.schalter.losungen.files;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by martin on 29.05.16.
 */
public class XmlWriter {

    private ArrayList<String> tags;
    private ArrayList<String> values;

    public static final String STARTTAG = "startTag";
    public static final String ENDTAG = "endTag";
    public static final String ATTRIBUTE = "attribute";
    public static final String TEXT = "text";

    public XmlWriter() {

    }

    public boolean writeXml(File file) {
        //If file doesnt exist create it
        //And if file could not be created return false
        if(!file.exists()) {
            try {
                if(!file.createNewFile())
                    return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream fileos = null;
        try {
            fileos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        XmlSerializer serializer = Xml.newSerializer();
        try {
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            for(int i = 0; i < tags.size(); i++) {
                String key = tags.get(i);
                String value = values.get(i);
                switch (key) {
                    case STARTTAG:
                        serializer.startTag(null, value);
                        break;
                    case ENDTAG:
                        serializer.endTag(null, value);
                        break;
                    case ATTRIBUTE:
                        String[] attribute = value.split("::");
                        if(attribute.length == 2)
                            serializer.attribute(null, attribute[0], attribute[1]);
                        else {
                            fileos.close();
                            return false;
                        }
                        break;
                    case TEXT:
                        serializer.text(value);
                        break;
                }
            }

            serializer.endDocument();
            serializer.flush();
            fileos.close();
        } catch (IOException e) {
            e.printStackTrace();
            try { fileos.close(); } catch (IOException ignored) { }
            return false;
        }

        return true;
    }

    /**
     * Set the HashMap with the data
     * @param tags
     * @param values
     * the first string describes the function, possible is:
     * startTag, endTag, attribute, text
     *
     * the second string holds the value
     * for attribute (attribute::value)
     */
    public void setData(ArrayList tags, ArrayList values) {
        this.tags = tags;
        this.values = values;
    }
}
