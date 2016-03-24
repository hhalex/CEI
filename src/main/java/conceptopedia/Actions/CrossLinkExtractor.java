package conceptopedia.Actions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class CrossLinkExtractor {

	private static final Pattern LINKS_PATTERN = Pattern.compile("INSERT INTO `langlinks` VALUES (.+)");

	private static final Pattern LINK_PATTERN = Pattern.compile("\\((.+?),'(.*?)','(.*?)'\\)");

	private String sourceLang;
	// private String[] targetLangs;
	private final ProgressCounter linkCounter = new ProgressCounter();

	public CrossLinkExtractor(String sourceLang, String[] targetLangs) {
		this.sourceLang = sourceLang;
	}

	public int getLinkCount() {
		return linkCounter.getCount();
	}

	// Id internal = id numérique wikipeida
	// keytitle = lang + title
	// id = id internal + lang

	public void parse(InputStream inputFile) throws IOException {
		BufferedReader bd = new BufferedReader(new InputStreamReader(inputFile, "UTF-8"));
		String line = "";
		while ((line = bd.readLine()) != null) {
			Matcher matcher = LINKS_PATTERN.matcher(line);
			if (matcher.find()) {
				String links = matcher.group(1);
				Matcher matcher1 = LINK_PATTERN.matcher(links);
				while (matcher1.find()) {
					String sourcePageIdInternal = matcher1.group(1);
					String targetLang = matcher1.group(2);
					String targetPageTitle = matcher1.group(3);

					if (sourcePageIdInternal != null && isTargetLang(targetLang) && targetPageTitle.length() > 0 && (!targetPageTitle.contains(":")
					/*
					 * ||
					 * targetPage.startsWith(WikipediaNamespace.getCategoryName(
					 * targetLang) + ":")
					 */)) {
						String sourcePageKeyTitle = getKeyTitleFromIdInternal(sourcePageIdInternal, sourceLang);
						targetPageTitle = targetPageTitle.substring(0, 1).toUpperCase() + targetPageTitle.substring(1);
						String targetPageKeyTitle = getKeyTitleFromLangAndTitle(targetPageTitle, targetLang);

						if (sourcePageKeyTitle != null) {
							System.out.println(sourcePageKeyTitle + " -> " + targetPageKeyTitle);
							linkCounter.increment(1);
							HTable intralang = DataModel.tables.get(DataModel.INTRALANG_TBL);

							Put p = new Put(Bytes.toBytes(sourcePageKeyTitle));
							p.add(Bytes.toBytes("voisins_inter"), Bytes.toBytes(targetPageKeyTitle), Bytes.toBytes(1));

							intralang.put(p);
						}
					}
				}
			}
		}
	}
	
	public int getPageCount() {
		return linkCounter.getCount();
	}

	public void parse(String fileName) throws IOException, XMLStreamException {
		parse(new FileInputStream(fileName));
	}

	private String getKeyTitleFromIdInternal(String sourcePageIdInternal, String sourceLang2) {
		HTable transitions = DataModel.tables.get(DataModel.INTRALANG_MATCHING_IDS_TBL);
		Get g = new Get(Bytes.toBytes(sourceLang2 + "-" + sourcePageIdInternal));
		try {
			Result r = transitions.get(g);
			byte[] tmp = r.getValue(Bytes.toBytes("match"), Bytes.toBytes("match"));
			if (tmp != null)
				return Bytes.toString(tmp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String getKeyTitleFromLangAndTitle(String targetPageTitle, String lang) {
		return lang + "-" + targetPageTitle.toLowerCase();
	}

	private boolean isTargetLang(String targetLang) {
		// if(targetLang )
		return targetLang != null && targetLang != "";
	}
}
