package SmartRotationProcessing;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ij.io.FileInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.File;

public class xmlMetadata {
    //this is the xml file class for the stack meta file
    public int ImgWidth = 2048;
    public int ImgHeight = 2048;
    public int nImage = 500;
    public double xypixelsize = 0.65d;
    public double zpixelsize = 2d;

    public double xpos = 0.0d;
    public double ypos = 0.0d;
    public double startzpos = 0.0d;
    public double endzpos = 0.0d;
    public double anglepos = 0;
    public int samplestartx = 0;
    public int sampleendx = 512;
    public int samplestartz = 0;
    public int sampleendz = 500;
    public int bitdepth = 16;
    public int blk_size = 16;
    public int gapbetweenimages = 4;
    public int background = 700;
    public float entropybackground = 7.2f;
    public int ang_reso=10;
    private static Document doc;


    public void read(String path) {
        try {
            File xmlfile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlfile);
            doc.getDocumentElement().normalize();
            NodeList stagelist = doc.getElementsByTagName("stage_pos");
            Node nNode = stagelist.item(0);
            Element eElement = (Element) nNode;
            xpos = Double.parseDouble(eElement.getElementsByTagName("x").item(0).getTextContent());
            ypos = Double.parseDouble(eElement.getElementsByTagName("y").item(0).getTextContent());
            startzpos = Double.parseDouble(eElement.getElementsByTagName("start_z").item(0).getTextContent());
            endzpos = Double.parseDouble(eElement.getElementsByTagName("end_z").item(0).getTextContent());
            anglepos = Double.parseDouble(eElement.getElementsByTagName("angle").item(0).getTextContent());
            NodeList imagelist = doc.getElementsByTagName("image_attributes");
            Node nNode2 = imagelist.item(0);
            Element iElement = (Element) nNode2;
            ImgHeight = Integer.parseInt(iElement.getElementsByTagName("height").item(0).getTextContent());
            ImgWidth = Integer.parseInt(iElement.getElementsByTagName("width").item(0).getTextContent());
            nImage = Integer.parseInt(iElement.getElementsByTagName("nImages").item(0).getTextContent());
            bitdepth = Integer.parseInt(iElement.getElementsByTagName("bit_depth").item(0).getTextContent());
            xypixelsize = Double.parseDouble(iElement.getElementsByTagName("xypixelsize").item(0).getTextContent());
            zpixelsize = Double.parseDouble(iElement.getElementsByTagName("zpixelsize").item(0).getTextContent());
            gapbetweenimages = Integer.parseInt(iElement.getElementsByTagName("gapbetweenimages").item(0).getTextContent());
            background = Integer.parseInt(iElement.getElementsByTagName("background").item(0).getTextContent());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void save(String path) {
        //update all parameters
        try {
            Node stage_position = doc.getElementsByTagName("stage_pos").item(0);
            Node image_attributes = doc.getElementsByTagName("image_attributes").item(0);
            Node sample_position = doc.getElementsByTagName("sample_position").item(0);
            Node smart_rot_param = doc.getElementsByTagName("smart_rot_param").item(0);
            Node[] attributes = {stage_position, image_attributes, sample_position, smart_rot_param};
            for (int i = 0; i < attributes.length; i++) {
                NodeList list = attributes[i].getChildNodes();
                for (int j = 0; j < list.getLength(); j++) {
                    Node node = list.item(j);

                    if (node.getNodeName() == "x") node.setTextContent(Double.toString(xpos));
                    if (node.getNodeName() == "y") node.setTextContent(Double.toString(ypos));
                    if (node.getNodeName() == "start_z") node.setTextContent(Double.toString(startzpos));
                    if (node.getNodeName() == "end_z") node.setTextContent(Double.toString(endzpos));
                    if (node.getNodeName() == "angle") node.setTextContent(Double.toString(anglepos));
                    if (node.getNodeName() == "width") node.setTextContent(Integer.toString(ImgWidth));
                    if (node.getNodeName() == "height") node.setTextContent(Integer.toString(ImgHeight));
                    if (node.getNodeName() == "nImages") node.setTextContent(Integer.toString(nImage));
                    if (node.getNodeName() == "bit_depth") node.setTextContent(Integer.toString(bitdepth));
                    if (node.getNodeName() == "startX") node.setTextContent(Integer.toString(samplestartx));
                    if (node.getNodeName() == "endX") node.setTextContent(Integer.toString(sampleendx));
                    if (node.getNodeName() == "startZ") node.setTextContent(Integer.toString(samplestartz));
                    if (node.getNodeName() == "endZ") node.setTextContent(Integer.toString(sampleendz));
                    if (node.getNodeName() == "blk_size") node.setTextContent(Integer.toString(blk_size));
                    if (node.getNodeName() == "angular_resolution") node.setTextContent(Integer.toString(ang_reso));
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }

    }

    public void create() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("root");
            doc.appendChild(rootElement);
            Element stage_pos = doc.createElement("stage_pos");
            Element image_attributes = doc.createElement("image_attributes");
            Element sample_position = doc.createElement("sample_position");
            Element smart_rot_params = doc.createElement("smart_rot_param");
            rootElement.appendChild(stage_pos);
            rootElement.appendChild(image_attributes);
            rootElement.appendChild(sample_position);
            rootElement.appendChild(smart_rot_params);
            ///stage position node
            Element x = doc.createElement("x");
            x.appendChild(doc.createTextNode(Double.toString(xpos)));
            stage_pos.appendChild(x);
            Element y = doc.createElement("y");
            y.appendChild(doc.createTextNode(Double.toString(ypos)));
            stage_pos.appendChild(y);
            Element start_z = doc.createElement("start_z");
            start_z.appendChild(doc.createTextNode(Double.toString(startzpos)));
            stage_pos.appendChild(start_z);
            Element end_z = doc.createElement("end_z");
            end_z.appendChild(doc.createTextNode(Double.toString(endzpos)));
            stage_pos.appendChild(end_z);
            Element angle = doc.createElement("angle");
            angle.appendChild(doc.createTextNode(Double.toString(anglepos)));
            stage_pos.appendChild(angle);
            //image_attributes node
            Element xypixel = doc.createElement("xypixelsize");
            xypixel.appendChild(doc.createTextNode(Double.toString(xypixelsize)));
            image_attributes.appendChild(xypixel);
            Element zpixel = doc.createElement("zpixelsize");
            zpixel.appendChild(doc.createTextNode(Double.toString(zpixelsize)));
            image_attributes.appendChild(zpixel);
            Element width = doc.createElement("width");
            width.appendChild(doc.createTextNode(Integer.toString(ImgWidth)));
            image_attributes.appendChild(width);
            Element height = doc.createElement("height");
            height.appendChild(doc.createTextNode(Integer.toString(ImgHeight)));
            image_attributes.appendChild(height);
            Element numimg = doc.createElement("nImages");
            numimg.appendChild(doc.createTextNode(Integer.toString(nImage)));
            image_attributes.appendChild(numimg);
            Element bit = doc.createElement("bitdepth");
            bit.appendChild(doc.createTextNode(Integer.toString(bitdepth)));
            image_attributes.appendChild(bit);
            Element gapbetween = doc.createElement("gapbetweenimages");
            gapbetween.appendChild(doc.createTextNode(Integer.toString(gapbetweenimages)));
            image_attributes.appendChild(gapbetween);
            Element backgnd = doc.createElement("background");
            backgnd.appendChild(doc.createTextNode(Integer.toString(background)));
            image_attributes.appendChild(backgnd);
            //sample position node
            Element xstart = doc.createElement("startX");
            xstart.appendChild(doc.createTextNode(Integer.toString(samplestartx)));
            sample_position.appendChild(xstart);
            Element xend = doc.createElement("endX");
            xend.appendChild(doc.createTextNode(Integer.toString(sampleendx)));
            sample_position.appendChild(xend);
            Element zstart = doc.createElement("startZ");
            zstart.appendChild(doc.createTextNode(Integer.toString(samplestartz)));
            sample_position.appendChild(zstart);
            Element zend = doc.createElement("endZ");
            zend.appendChild(doc.createTextNode(Integer.toString(sampleendz)));
            sample_position.appendChild(zend);
            //smart rot param node
            Element bksize = doc.createElement("blk_size");
            bksize.appendChild(doc.createTextNode(Integer.toString(blk_size)));
            smart_rot_params.appendChild(bksize);
            Element entropythresh = doc.createElement("entropybackground");
            entropythresh.appendChild(doc.createTextNode(Float.toString(entropybackground)));
            smart_rot_params.appendChild(entropythresh);
            Element ang_res = doc.createElement("angular_resolution");
            ang_res.appendChild(doc.createTextNode(Integer.toString(ang_reso)));
            smart_rot_params.appendChild(ang_res);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }
    public void savetofileinfo(FileInfo fi){
        fi.width = this.ImgWidth;
        fi.height = this.ImgHeight;
        fi.nImages = this.nImage;
        fi.intelByteOrder = true;
        fi.gapBetweenImages = this.gapbetweenimages;
        if (this.bitdepth == 16){
            fi.fileType = FileInfo.GRAY16_UNSIGNED;
        }
        else if (this.bitdepth == 32){
            fi.fileType = FileInfo.GRAY32_FLOAT;
        }
    }

}
