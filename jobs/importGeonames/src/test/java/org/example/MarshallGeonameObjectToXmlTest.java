package org.example;

import com.marklogic.junit.Fragment;
import com.marklogic.junit.NamespaceProvider;
import com.marklogic.junit.XmlHelper;
import org.geonames.Geoname;
import org.jdom2.input.DOMBuilder;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.List;

public class MarshallGeonameObjectToXmlTest extends XmlHelper {

    protected NamespaceProvider getNamespaceProvider() {
        return new GeonamesNamespaceProvider();
    }

    @Test
    public void marshallGeonameTest() throws Exception {
        Geoname geo = new Geoname();
        geo.setId("123");
        List<String> names = new ArrayList<>();
        names.add("Alpha");
        names.add("Beta");
        geo.setNames(names);

        Document w3cDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Marshaller marshaller = JAXBContext.newInstance(org.geonames.Geoname.class).createMarshaller();
        marshaller.marshal(geo, w3cDoc);
        Fragment frag = new Fragment(new DOMBuilder().build(w3cDoc));
        frag.setNamespaces(getNamespaceProvider().getNamespaces());
        //frag.prettyPrint();
        frag.assertElementExists("/geo:geoname/geo:names/geo:name[2][text() = 'Beta']");
    }

}
