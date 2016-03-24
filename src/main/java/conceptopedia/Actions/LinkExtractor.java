//
// Copyright (c) 2012 Mirko Nasato
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
package conceptopedia.Actions;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class LinkExtractor extends SimpleStaxParser {

	private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[(.+?)\\]\\]");

	private final ProgressCounter pageCounter = new ProgressCounter();

	private String title;
	private String text;
	private String lang;
	private String id_internal;

	@SuppressWarnings("serial")
	public LinkExtractor() {
		super(Arrays.asList("page", "title", "text", "id"), Arrays.asList("mediawiki"));
		lang = "init";
	}

	public int getPageCount() {
		return pageCounter.getCount();
	}

	@Override
	void handleElement(String element, String value, String parent) {
		if ("page".equals(element)) {
			if (!title.contains(":")) {
				try {
					createHbaseRow();
				} catch (XMLStreamException streamException) {
					throw new RuntimeException(streamException);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			title = null;
			text = null;
		} else if ("title".equals(element) && "page".equals(parent)) {
			
			title = value.toLowerCase();
		} else if ("id".equals(element)  && "page".equals(parent)) {
			id_internal = value;
		} else if ("text".equals(element) && "revision".equals(parent)) {
			text = value;
		}
	}

	@Override
	void handleElementWithAttributes(String element, String value, Map<String, String> attributes, String parent) {
		System.out.println("Element avec attributs géré: " + element + ", " + attributes);
		System.out.println("Langue de la page:" + attributes.get("lang"));
		if (attributes.containsKey("lang"))
			lang = attributes.get("lang");
	}

	private void createHbaseRow() throws XMLStreamException, IOException {
		Set<String> links = parseLinks(text);

		displayStats(links.size());

		Put p = new Put(Bytes.toBytes(lang + "-" + title));

		p.add(Bytes.toBytes("attributs"), Bytes.toBytes("langue"), Bytes.toBytes(lang));
		p.add(Bytes.toBytes("attributs"), Bytes.toBytes("titre"), Bytes.toBytes(title));

		p.add(Bytes.toBytes("attributs"), Bytes.toBytes("id_internal"), Bytes.toBytes(id_internal));

		for (String link : links)
			p.add(Bytes.toBytes("voisins"), Bytes.toBytes(lang + "-" + link), Bytes.toBytes(1));

		DataModel.tables.get(DataModel.INTRALANG_TBL).put(p);

		if (id_internal != null) {
			Put p2 = new Put(Bytes.toBytes(lang + "-" + id_internal));
			p2.add(Bytes.toBytes("match"), Bytes.toBytes("match"), Bytes.toBytes(lang + "-" + title));
			DataModel.tables.get(DataModel.INTRALANG_MATCHING_IDS_TBL).put(p2);
		}
	}

	private void displayStats(int nb) {
		System.out.println("Page {titre: " + title + ", " + "langue: " + lang + ", voisins: " + nb);
		pageCounter.increment(nb);
	}

	private Set<String> parseLinks(String text) {
		Set<String> links = new HashSet<String>();
		if (text != null) {
			Matcher matcher = LINK_PATTERN.matcher(text);
			while (matcher.find()) {
				String link = matcher.group(1);
				if (!link.contains(":")) {
					if (link.contains("|")) {
						link = link.substring(0, link.lastIndexOf('|'));
					}
					if (!link.equals(title))
						links.add(link.toLowerCase());
				}
			}
		}
		return links;
	}
}
