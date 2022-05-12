/**
 *JC Software.
 * copyright @jcs.com.tw 2003~2006. all right reserved.
 */
package com.tcb.ecol.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML Utility Tool
 *
 * @author JC Software Inc.
 * @version 1.0
 */
public class XmlTool {

    private static final Logger informer = LoggerFactory.getLogger(XmlTool.class);

    private static final XPathFactory xpathFactory = XPathFactory.newInstance();

    private static final String DEFAULT_ENCODING = "utf-8";
    
    private static final String EXCEPTION_DESC = "無法將Node ";
    private static final String EXCEPTION_COLUMN = "轉換為XML：";

    private static Transformer transformer;

    private static Transformer getTransformer() {
        if (transformer == null) {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            try {
                transformer = tFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                //e.printStackTrace();
                informer.error(e.toString());
            }
        }
        return transformer;
    }

    /**
     * 將節點Node依預設的編碼轉換為XML
     * 
     * @see XmlTool.DEFAULT_ENCODING
     * @param node
     * @return
     */
    public static String toXML(Node node) {
        return toXML(node, DEFAULT_ENCODING);
    }

    /**
     * 將節點Node依指定的編碼轉換為XML
     * 
     * @param node
     * @param encoding
     * @return
     */
    public static String toXML(Node node, String encoding) {
        StringWriter out = new StringWriter(1024);
        try {
            Transformer tran = getTransformer();
            tran.setOutputProperty(OutputKeys.ENCODING, encoding);
            tran.transform(new DOMSource(node), new StreamResult(out));
            return out.toString();
        } catch (TransformerException e) {
            String msg = EXCEPTION_DESC + //node.getNodeName() + 
                    EXCEPTION_COLUMN + e.toString();
            informer.error(msg, e.toString());
        }
        return "";
    }

    /**
     * 使用自訂的handler，將JAXB標記物件轉換為XML
     * 
     * @param obj
     *            JAXB Element
     * @param handler
     *            ContentHandler
     */
    public static void toXML(Object obj, ContentHandler handler) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(obj.getClass());
            Marshaller mr = ctx.createMarshaller();
            mr.marshal(obj, handler);
        } catch (JAXBException e) {
            informer.error("無法將{}轉換為XML：{}", obj, e.toString());
        }
    }

    /**
     * 將JAXB標記物件轉換為XML
     * 
     * @param obj
     *            JAXB Element
     * @return XML string
     */
    public static String toXML(Object obj) {
        StringWriter writer = new StringWriter(1024);
        try {
            JAXBContext ctx = JAXBContext.newInstance(obj.getClass());
            Marshaller mr = ctx.createMarshaller();
            mr.marshal(obj, writer);
        } catch (JAXBException e) {
            informer.error("無法將{}轉換為XML：{}", obj, e.toString());
        }
        return writer.toString();
    }

    /**
     * 讀取符合XPath所指定的第一個節點中的值 .
     *
     * @param sXML
     *            XML格式的字串
     * @param xpath
     */
    public static String selectSingleNodeValue(String sXML, String xpath) {
        Document document = null;
        try {
            document = XmlTool.newDocument(sXML);
        } catch (IOException e) {
            informer.debug("XmlTool", e.toString());
        }
        return selectSingleNodeValue(document, xpath);
    }

    /**
     * 讀取Dom物件中符合XPath所指定的第一個節點中的值 .
     *
     *
     * Note:呼叫此Mthod時傳入Document 會比傳入Node時使用較少的計憶體[memory resource].
     * 
     * @param node
     * @param xpath
     */
    public static Node selectSingleNode(Node node, String xpath) {
        if (node == null) {
            informer.debug("Warning->參數document =null。");
            return null;
        }

        XPath xp = xpathFactory.newXPath();
        NodeList list = null;
        try {
            list = (NodeList) xp.evaluate(xpath, node, XPathConstants.NODESET);
            if (list.getLength() > 0) {
                return list.item(0);
            }
        } catch (XPathExpressionException e) {
            informer.error("讀取XML發生錯誤：{} [xpath={}]", e.toString(), xpath);
        }

        informer.debug("Warning->找不到[xpath=" + xpath + "]所指定的節點。");
        return null;
    }

    /**
     * 讀取Dom物件中符合XPath所指定的第一個節點中的值 .
     *
     *
     * Note:呼叫此Mthod時傳入Document 會比傳入Node時使用較少的計憶體[memory resource].
     * 
     * @param document
     * @param xpath
     */
    public static String selectSingleNodeValue(Node node, String xpath) {
        Node targetNode = selectSingleNode(node, xpath);
        return (targetNode == null ? "" : (targetNode.getFirstChild() == null) ? "" : (targetNode.getFirstChild().getNodeValue() == null) ? "" : targetNode.getFirstChild().getNodeValue().trim());
    }

    /**
     * 設定符合XPath所指定的第一個節點中的值 .
     *
     * @param node
     * @param xpath
     */
    public static void setSingleNodeValue(Node node, String xpath, String value) throws Exception {
        Node nodeResult = selectSingleNode(node, xpath);
        if (nodeResult != null) {
            if (nodeResult.getFirstChild() == null) {
                nodeResult.appendChild(nodeResult.getOwnerDocument().createTextNode(value));
            } else {
                nodeResult.getFirstChild().setNodeValue(value);
            }
        }
    }

    /**
     * 設定所有符合XPath所指定的節點中的值 .
     *
     * @param node
     * @param xpath
     */
    public static void setNodesValue(Node node, String xpath, String value) throws Exception {
        NodeList nodeList = selectNodes(node, xpath);

        int i = 0;
        for (; i < nodeList.getLength(); ++i) {
            Node nodeResult = nodeList.item(i);
            if (nodeResult.getFirstChild() == null) {
                if (node instanceof Document) {
                    nodeResult.appendChild(((Document) node).createTextNode(value));
                } else {
                    nodeResult.appendChild(node.getOwnerDocument().createTextNode(value));
                }
            } else {
                nodeResult.getFirstChild().setNodeValue(value);
            }
        }

        if (i == 0)
            throw new Exception("找不到" + xpath + "所指定的節點。");
    }

    /**
     * 將所有符合XPath所指定的節點中的值轉為指定的格式.
     *
     * 94/04/29 added.
     * 
     * @param nodeInput
     *            要轉換的節點
     * @param xpath
     *            XPath 路徑
     * @param assignedFormat
     *            指定的格式
     */
    public static void convertToFormat(Node nodeInput, String xpath, DecimalFormat assignedFormat) throws Exception {
        NodeList nodeList = selectNodes(nodeInput, xpath);

        for (int i = 0; i < nodeList.getLength(); ++i) {
            Node node = nodeList.item(i);
            if (node.getFirstChild() != null && node.getFirstChild().getNodeValue().trim().length() > 0) {
                double d = Double.parseDouble(node.getFirstChild().getNodeValue());
                node.getFirstChild().setNodeValue(assignedFormat.format(d));
            }
        }

    }

    /**
     * 讀取符合XPath所指定的第一個節點中的值 .
     *
     * 94/04/29 added.
     * 
     * @param node
     * @param xpath
     */
    public static NodeList selectNodes(Node node, String xpath) {
        XPath xp = xpathFactory.newXPath();
        try {
            return (NodeList) xp.evaluate(xpath, node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            //e.printStackTrace();
            informer.error(e.toString());
        }
        return null;
    }

    /**
     * 將XML 檔( InputStream ) parse 為 Document.
     *
     * 註:使用預設encoding:Big5
     * 
     * @param fis
     *            (String).
     * @return Document.
     * @throws IOException
     */
    public static Document newDocument(InputStream fis) throws IOException {
        return newDocument(fis, "UTF-8");
    }

    /**
     * 將XML 檔( InputStream ) parse 為 Document.
     *
     * @param fis
     *            (String).
     * @return Document.
     * @throws IOException
     */
    public static Document newDocument(InputStream fis, String encoding) throws IOException {
        InputSource in = new InputSource(fis);
        return newDocument(in, encoding);
    }

    /**
     * 將XML 格式的字串 parse 為 Document.
     *
     * 註:使用預設encoding:Big5
     * 
     * @param sXML
     *            (String).
     * @return Document.
     * @throws IOException
     */
    public static Document newDocument(String sXML) throws IOException {
        return newDocument(sXML, "UTF-8");
    }

    /**
     * 將XML 格式的字串 parse 為 Document.
     *
     * @param sXML
     *            (String).
     * @return Document.
     * @throws IOException
     */
    public static Document newDocument(String sXML, String encoding) throws IOException {
        StringReader reader = new StringReader(sXML);
        InputSource in = new InputSource(reader);
        return newDocument(in, encoding);
    }

    public static Document newDocument(InputSource in) throws IOException {
        return newDocument(in, "UTF-8");
    }

    /**
     * 把InputSource Parse 為Document 物件.
     *
     * @param in
     *            來源
     * @param encoding
     *            編碼
     * @return Document
     * @throws IOException
     */
    public static Document newDocument(InputSource in, String encoding) throws IOException {
        Document document = null;
        try {
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            dfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            dfactory.setNamespaceAware(true);
            // 2008-07-21 added:For Support Big5 encoding......................
            dfactory.setAttribute("http://apache.org/xml/features/allow-java-encodings", true);
            // dfactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dfactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dfactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dfactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder domBuilder = dfactory.newDocumentBuilder();
            in.setEncoding(encoding);
            document = domBuilder.parse(in);
        } catch (SAXException e) {
            informer.debug("XmlTool", "In newDocument().Caught a SAXException .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
        } catch (ParserConfigurationException e) {
            informer.debug("XmlTool", "In newDocument().Caught a ParserConfigurationException .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
        }
        return document;
    }

    /**
     * 產生空的 Document 物件.
     */
    public static Document newDocument() {
        Document document = null;
        DocumentBuilderFactory docBuildFactory = DocumentBuilderFactory.newInstance();
        docBuildFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        docBuildFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try {
            DocumentBuilder docBuilder = docBuildFactory.newDocumentBuilder();
            document = docBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            informer.debug("XmlTool", "In newDocument().Caught a ParserConfigurationException e .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
        }
        return document;
    }

    /**
     * 開啟指定的XML檔
     */
    public static Document openDocument(String filename) {
        Document doc = null;
        File file = new File(filename);
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            doc = newDocument(is);
        } catch (FileNotFoundException e) {
            informer.debug("XmlTool", "In openDocument(File file).Caught a FileNotFound Exception e .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
            return null;
        } catch (IOException e) {
            informer.debug("XmlTool", "In openDocument(File file).Caught a IOException e .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
            return null;
        } finally {
            if(is!=null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return doc;
    }

    /**
     * 關啟FILE物件所代表的XML檔.
     *
     * 註:使用預設encoding:Big5
     * 
     * @param file
     * @return Document
     */
    public static Document openDocument(File file) {
        return openDocument(file, "UTF-8");
    }

    /**
     * 關啟FILE物件所代表的XML檔.
     *
     * @param file
     * @return Document
     */
    public static Document openDocument(File file, String encoding) {
        Document doc = null;
        //Unreleased Resource: Streams
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            doc = newDocument(is, encoding);
        } catch (FileNotFoundException e) {
            informer.debug("XmlTool", "In openDocument(File file).Caught a FileNotFound Exception e .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
            return null;
        } catch (IOException e) {
            informer.debug("XmlTool", "In openDocument(File file).Caught a IOException e .訊息=." + e.toString());
            informer.debug("XmlTool", e.toString());
            return null;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return doc;
    }

    /**
     * 取代XML 的保留字to中文大寫(ex:'&'->'＆' , '<' ->'＜' ...).
     *
     * @param text
     *            要檢查的字串
     * @return String
     */
    public static String replaceXMLReservedWord(String text) {
        return replaceXMLReservedWord(text, true);
    }

    /**
     * 取代XML 的保留字to中文大寫(ex:'&'->'＆' , '<' ->'＜' ...).
     *
     * history:<br/>
     * ======= 94/08/23 modify by Rex ============ 增加isTrim參數,是否要保留空白, 不影響原先的使用方式 ===========================================
     *
     * @param text
     *            要檢查的字串
     * @param isTrim
     *            是否要做trim動作
     * @return String
     */
    public static String replaceXMLReservedWord(String text, boolean isTrim) {
        if (text == null || text.length() == 0)
            return text;

        String ret = null;
        ret = text.replace('&', '＆');
        ret = text.replace('<', '＜');
        if (isTrim)
            ret = ret.replaceAll("", ""); // 0x0B
        return ret;
    }

    /**
     * 去除字串中的0x00,0xe字元.
     *
     * @param text
     * @return 去除後的字串
     */
    public static String replaceSpecialChar(String text) {
        String ret = null;
        byte[] c = new byte[1];
        String xx = text;
        String ret2 = null;
        ret2 = xx.replaceAll("\\u0000", " ");
        ret2 = xx.replaceAll("\\u000e", " ");
        c[0] = 0x0;
        ret = text.replaceAll(new String(c), " ");
        c[0] = 0xe;
        ret = ret.replaceAll(new String(c), " ");

        return ret;
    }

    /**
     * 傳回下一個ELEMENT NODE
     * 
     * @param node
     * @return 找到的ELEMENT NODE，若無則回傳NULL
     */
    public static Element getNextElement(Node node) {
        Node nextNode = node;
        while ((nextNode = nextNode.getNextSibling()) != null) {
            if (nextNode.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) nextNode;
            }
        }
        return null;
    }

    /**
     * 傳回DOM架構中parent node 中第一個NodeType 是Element Type的CHILD NODE
     * 
     * @param parentNode
     * @return 找到的第一個ELEMENT NODE 若無則回傳NULL
     */
    public static Element getFirstElement(Node parentNode) {
        Node childNode = parentNode.getFirstChild();
        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) childNode;
        }
        return getNextElement(childNode);
    }

    /**
     * 取回所有符合XPATH所指定的NODE
     *
     * @param xpath
     * @param node
     * @return 以NODE ARRAY的方式回傳符合的NODE
     */
    public static Node[] getNodesArray(String xpath, Node node) {
        NodeList nodeList = getNodes(xpath, node);
        Node[] nodeArray = new Node[nodeList.getLength()];
        for (int i = 0; i < nodeArray.length; ++i) {
            nodeArray[i] = nodeList.item(i);
        }
        return nodeArray;
    }

    /**
     * 取得XPATH所指定的所有NODE
     * 
     * @param xpath
     * @param node
     * @return 以XPathResult的方式回傳符合的NODE
     */
    public static NodeList getNodes(String xpath, Node node) {
        return selectNodes(node, xpath);
    }

    /**
     * 取得節點值.
     * 
     * @param node
     * @return Author:ingram Date:Jan 22, 2007
     */
    public static String getNodeValue(Node node) {
        Node cNode = node.getFirstChild();
        while (cNode != null) {
            if (cNode.getNodeType() == Element.TEXT_NODE) {
                return ((Text) cNode).getNodeValue();
            }
            cNode = cNode.getNextSibling();
        }
        cNode = null;
        return "";
    }


    /**
     * FILE PATH 過濾字串
     * 
     * @param path
     * @return
     */
    public static String cleanPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.length(); i++) {
                sb.append(cleanChar(path.charAt(i)));
            }
            return sb.toString();
        }
        return null;
    }
    private static char cleanChar(char c) {
        int ascii = c;
        if ((ascii >= 48 && ascii < 58) || (ascii >= 65 && ascii < 91) || (ascii >= 97 && ascii < 123)) {
            // 0-9, A-Z, a-z
            return c;
        }
        switch (c) {
        case '/':
        case '.':
        case '-':
        case '_':
        case ' ':
        case ':':
        case '\\':
            return c;
        default:
            break;
        }
        return '%';
    }
}
