package util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class Marshaller {


    public static <T> T unmarshal(Class<T> cls, String path) throws JAXBException{
        File xmlFile = new File(path);
        JAXBContext jaxbContext;
        jaxbContext = JAXBContext.newInstance(cls);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (T) jaxbUnmarshaller.unmarshal(xmlFile);

    }

    public static <T> void marshell (T data, String path) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(data.getClass());
            javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.marshal(data, new File(path));
        } catch (JAXBException e) {
            System.out.println("Marshell Failed!");
            e.printStackTrace();
        }
    }
}
