package com.weikun.server.common;

import com.weikun.server.common.annotation.XmlType;
import com.weikun.server.server.db.entity.XmlData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author weikun
 * @date 2020/1/13
 */
public class XmlUtil {
    private static final String ROOT_NAME = "root";
    /**
     * 通过反射读取XML反序列化为对象列表
     *
     * @param path     xml文件路径
     * @param itemName 对象名
     * @param clz      序列化对象类
     */
    public static <T> List<T> readXML(String path, String itemName, Class<T> clz) throws Throwable {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            Element root = doc.getDocumentElement();
            NodeList list = root.getElementsByTagName(itemName);
            List<Element> elements = new ArrayList<>();
            for (int i = 0; i < list.getLength(); i++) {
                elements.add((Element) list.item(i));
            }
            List<T> result = new ArrayList<>();
            Field[] fields = clz.getDeclaredFields();
            for (Element element : elements) {
                T obj = clz.newInstance();
                for (Field field : fields) {
                    Node item = element.getElementsByTagName(field.getName()).item(0);
                    if (item != null) {
                        field.setAccessible(true);
                        field.set(obj, item.getTextContent());
                    }
                }
                result.add(obj);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static synchronized boolean deleteXML(String name,String value,String path){
        synchronized (XmlUtil.class) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    LogUtil.e("xml file "+path+" not exists!");
                    return false;
                }
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document;
                Node root;
                document = builder.parse(file);
                //寻找根节点
                root = document.getElementsByTagName(ROOT_NAME).item(0);
                if (root == null) {
                    LogUtil.e("find xml root failed!");
                    return false;
                }

                NodeList list = document.getElementsByTagName(name);
                Node target = null;
                for (int i=0;i<list.getLength();i++){
                    Node node = list.item(i);
                    if (node.getTextContent().equals(value)){
                        target = node.getParentNode();
                        break;
                    }
                }
                if (target==null){
                    LogUtil.e("item not found!");
                    return false;
                }
                root.removeChild(target);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer tf = transformerFactory.newTransformer();
                tf.setOutputProperty("encoding", "UTF-8");
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                tf.transform(new DOMSource(document), new StreamResult(file));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 通过反射将对象序列化后写入XML文件
     *
     * @param xmlData XML数据对象
     * @param path    文件路径
     */
    public static synchronized boolean writeXML(XmlData xmlData, String path) {
        synchronized (XmlUtil.class) {
            try {
                if (xmlData == null || xmlData.getData() == null) {
                    return false;
                }
                Object data = xmlData.getData();
                Field[] fields = data.getClass().getDeclaredFields();
                if (fields.length == 0) {
                    return false;
                }
                List<Field> fieldList = new ArrayList<>();
                for (Field field : fields) {
                    field.setAccessible(true);
                    fieldList.add(field);
                }
                boolean newCreate = false;
                File file = new File(path);
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        return false;
                    }
                    //第一次创建
                    newCreate = true;
                }
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document;
                Node root;
                if (newCreate) {
                    document = builder.newDocument();
                    //创建根节点
                    root = document.createElement(xmlData.getRootName());
                } else {
                    document = builder.parse(file);
                    //寻找根节点
                    root = document.getElementsByTagName(xmlData.getRootName()).item(0);
                }
                if (root == null) {
                    return false;
                }
                Element item = document.createElement(xmlData.getItemName());
                for (Field field : fieldList) {
                    if (field.get(data) != null) {
                        XmlType type = field.getAnnotation(XmlType.class);
                        if (type != null) {
                            if (type.value() == XmlType.TYPE_ATTRIBUTE) {
                                item.setAttribute(field.getName(), field.get(data).toString());
                            } else if (type.value() == XmlType.TYPE_ELEMENT) {
                                Element element = document.createElement(field.getName());
                                element.setTextContent(field.get(data).toString());
                                item.appendChild(element);
                            }
                        }
                    }
                }
                root.appendChild(item);
                if (newCreate) {
                    document.appendChild(root);
                }
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer tf = transformerFactory.newTransformer();
                tf.setOutputProperty("encoding", "UTF-8");
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                tf.transform(new DOMSource(document), new StreamResult(file));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }


}
