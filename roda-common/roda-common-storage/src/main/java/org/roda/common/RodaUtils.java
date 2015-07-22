package org.roda.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.yaml.snakeyaml.Yaml;

import pt.gov.dgarq.roda.core.common.RodaConstants;

public class RodaUtils {

	public static String dateToString(Date date) {
		String ret;
		if (date != null) {
			SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
			ret = iso8601DateFormat.format(date);
		} else {
			ret = null;
		}

		return ret;
	}

	public static Date parseDate(String date) throws ParseException {
		Date ret;
		if (date != null) {
			SimpleDateFormat iso8601DateFormat = new SimpleDateFormat(RodaConstants.ISO8601);
			ret = iso8601DateFormat.parse(date);
		} else {
			ret = null;
		}
		return ret;
	}

	public static Map<String, Object> copyMap(Object object) {
		if (!(object instanceof Map)) {
			return null;
		}
		Map<?, ?> map = (Map<?, ?>) object;
		Map<String, Object> temp = new HashMap<String, Object>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			if (entry.getKey() instanceof String) {
				temp.put((String) entry.getKey(), entry.getValue());
			} else {
				return null;
			}
		}
		return temp;
	}

	public static List<String> copyList(Object object) {
		if (!(object instanceof List)) {
			return null;
		}
		List<?> list = (List<?>) object;
		List<String> temp = new ArrayList<String>();
		for (Object ob : list) {
			if (ob instanceof String) {
				temp.add((String) ob);
			} else if (ob == null) {
				temp.add(null);
			} else {
				return null;
			}
		}
		return temp;
	}

	public static Map<String, List<String>> parseYmlPropertiesFile(File file) throws FileNotFoundException {
		Map<String, List<String>> properties = new HashMap<String, List<String>>();

		Yaml yaml = new Yaml();
		Map<String, Object> map = RodaUtils.copyMap(yaml.load(new FileInputStream(file)));
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			properties.put(entry.getKey(), RodaUtils.copyList(entry.getValue()));
		}

		return properties;
	}

	public static void applyStylesheet(Reader xsltReader, Reader fileReader, Writer result)
			throws IOException, TransformerException {
		applyStylesheet(xsltReader, fileReader, new HashMap<String, String>(), result);
	}

	public static void applyStylesheet(Reader xsltReader, Reader fileReader, Map<String, String> parameters,
			Writer result) throws IOException, TransformerException {

		TransformerFactory factory = TransformerFactory.newInstance();
		Source xsltSource = new StreamSource(xsltReader);
		Transformer transformer = factory.newTransformer(xsltSource);
		for (Entry<String, String> parameter : parameters.entrySet()) {
			transformer.setParameter(parameter.getKey(), parameter.getValue());
		}
		Source text = new StreamSource(fileReader);
		transformer.transform(text, new StreamResult(result));
	}
}
