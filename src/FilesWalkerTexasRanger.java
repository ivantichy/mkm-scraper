import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class FilesWalkerTexasRanger {

	static HashMap<String, String> links = new HashMap<String, String>();
	static HashSet pics = new HashSet();
	static HashSet unknow = new HashSet();

	class Row {

		public Row(String line) {

		}

	}

	static class MyMatcher implements Comparator<Path> {

		String filename;

		public MyMatcher(String filename) {

			this.filename = filename;

		}

		public int compare(Path o1, Path o2) {

			double oo1 = checkFilename(filename, o1);

			double oo2 = checkFilename(filename, o2);

			if (oo1 > oo2)
				return 1;
			if (oo1 < oo2)
				return -1;

			return 0;

		}

	}

	static class MyMatcher2 implements Comparator<String> {

		String filename;

		public MyMatcher2(String filename) {

			this.filename = filename;

		}

		public int compare(String o1, String o2) {

			double oo1 = StringUtils.getJaroWinklerDistance(filename, o1);

			double oo2 = StringUtils.getJaroWinklerDistance(filename, o2);

			if (oo1 > oo2)
				return 1;
			if (oo1 < oo2)
				return -1;

			return 0;

		}

	}

	private static double checkFilename(String log, Path file) {

		String filename = file.getName(file.getNameCount() - 1).toString();
		filename = filename.substring(0, filename.length() - 4);

		double result = StringUtils.getJaroWinklerDistance(filename, log);
		/*
		 * System.out.println("Filename: " + filename);
		 * 
		 * System.out.println("log: " + log);
		 * 
		 * System.out.println("result: " + result);
		 */

		return result;
	}

	private static void levels(String path) throws Exception {

		System.out.println();
		System.out.println("for path " + path);
		String page = new String(Files.readAllBytes(Paths.get(path
				+ "page.html")));
		String title = page.substring(page.indexOf("<title>"),
				page.indexOf("</title>") + 8);
		page = page
				.substring(
						page.indexOf("<div class=\"goal-holder center\"><h3>THE GOALS:</h3>"),
						page.indexOf("<link href=\"/members/cfmbb/themes/default/css/styles.css\" type=\"text/css\" rel=\"stylesheet\" />"));

		page += "<p>Other links: </p> <br />";
		page = page.replaceAll("[<]img[^>]*[>]", "");
		page = page.replaceAll("[<]br [/][>]", "");

		Map<String, String> logger = Files.lines(
				Paths.get(path + "\\logger.txt")).collect(
				Collectors.toMap(p -> p.split(";")[2], p -> p));

		Map<String, Path> files = Files
				.list(Paths.get(path))
				.filter(p -> p.toString().endsWith(".mp4"))
				.collect(
						Collectors.<Path, String, Path> toMap(
								p -> p.toString(), p -> p));

		HashSet usedfiles = new HashSet();
		HashSet unusedfiles = new HashSet();

		unusedfiles.addAll(files.values());

		for (Iterator<String> iterator = logger.values().iterator(); iterator
				.hasNext();) {
			String[] line = iterator.next().split(";");

			String logline = line[1];
			String link = line[2];

			TreeSet<Path> list = new TreeSet<Path>(new MyMatcher(logline));

			list.addAll(files.values());

			Path selected = list.last();

			if (!unusedfiles.remove(selected)) {
				throw new Exception("File already selected: " + selected
						+ " logline " + logline);

			}

			usedfiles.add(selected);

			String logline2 = logline;

			if (logline.length() > 64) {
				logline2 = logline.substring(0, 63);
			}

			double value = checkFilename(logline2, selected);
			String filename = selected.getName(selected.getNameCount() - 1)
					.toString();

			if (value < 0.95) {
				System.out.println("Value not high enough value: " + value);
				System.out.println(logline);
				System.out.println(filename);
			}

			// System.out.println(selected);
			// System.out.println(logline);

			//
			String relativelink = link.substring(link.indexOf("/members"));
			String number = relativelink
					.substring(9, relativelink.indexOf("."));

			addToGlobalList(link, selected.toString() + ";" + logline);

			if (page.indexOf(relativelink) == -1) {
				System.out.println("Missing link: " + link);
			}

			page = page.replaceAll(
					"<a[^>]*href[=][\"]" + Pattern.quote(relativelink) + "\"",
					"<a href=\"" + filename + "\""
			// "<img src=\"http://www.maxkravmaga.com/members/images/"+number
			// +"a.png\"> <a href=\"" + filename + "\""

					);

			//
			page = page.replaceAll("<a[^>]*href=\"" + Pattern.quote(filename)

			+ "\"[^>]*><[/]a>", "");

			page = page.replaceAll(Pattern
					.quote("<a href=\"/members/department357.cfm\"></a>"), "");

			if (page.indexOf(filename) == -1) {

				// System.out.println("adding link to the end " + link);

				page += "<p><a href=\"" + filename + "\">" + logline
						+ "</a> </p>";

			}

		}

		if (unusedfiles.size() > 0) {
			System.out.println("Listing the rest of files");
		}
		unusedfiles.forEach(v -> System.out.println(v));

		if (logger.size() != files.size()) {

			System.out.println("Count does not match L:" + logger.size()
					+ " F:" + files.size());

		}

		// page = page.replaceAll("www[.]maxkravmaga[.]com", "");

		page = "<html><head>" + title + "</head><body>" + page
				+ "</body></html>";

		Files.write(Paths.get(path + "\\page2.html"), page.getBytes());
		//
		// .filter(p -> !p.endsWith(".mp4"))

		if (page.matches("(?ms).*[/]members[/][0-9]*[.]cfm.*")) {
			System.out.println("Missing link");
		}

		if (page.matches("(?ms).*[/]members.*")) {
			System.out.println("/members found in page");
		}

	}

	public static final String GLOBALLISTFILE = "c:\\Users\\Ivan\\Dropbox\\MKM\\results\\global.txt";
	public static final String GLOBALEXPLICITLISTFILE = "c:\\Users\\Ivan\\Dropbox\\MKM\\results\\global_explicit.txt";
	public static final String UNKNOWNFILE = "c:\\Users\\Ivan\\Dropbox\\MKM\\results\\unknown.txt";

	public static void resetGlobalList() throws IOException {

		Path p = Paths.get(GLOBALLISTFILE);

		if (Files.exists(p)) {

			Files.delete(p);
		}
		Files.createFile(Paths.get(GLOBALLISTFILE));

	}

	private static void addToGlobalList(String key, String value) {

		links.put(key, value);
		// Files.write(Paths.get(GLOBALLISTFILE), bytes, options)

	}

	public static void writeGlobalList() throws IOException {

		String output = "";

		for (Iterator<String> iterator = links.keySet().iterator(); iterator
				.hasNext();) {
			String k = iterator.next();

			output += k + ";" + links.get(k) + "\r\n";
		}
		Files.write(Paths.get(GLOBALLISTFILE), output.getBytes());

	

	}

	public static void writeUnknown() throws IOException {

		String output = "";

		for (Iterator<String> iterator = unknow.iterator(); iterator.hasNext();) {
			String k = iterator.next();

			output += k + "\r\n";
		}
		Files.write(Paths.get(UNKNOWNFILE), output.getBytes());

	}

	public static void main(String[] args) throws Exception {

		resetGlobalList();

		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\G1\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\G2\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\G3\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\G4\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\G5\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\P1\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\P2\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\P3\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\P4\\");
		levels("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\P5\\");

		writeGlobalList();

	//	walkTitles();
		Files.write(Paths.get(GLOBALLISTFILE),
				Files.readAllBytes(Paths.get(GLOBALEXPLICITLISTFILE)),
				StandardOpenOption.APPEND);
	

		subjectMatter("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\DefendingArmedAttacksBluntObjects\\");

		subjectMatter("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\CounterAttacks\\");

		subjectMatter("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\DefendingUnarmedAttacks\\");
		subjectMatter("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\GroundFighting\\");
		subjectMatter("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\ChokeGrabReleases\\");
		writeUnknown();

	}

	public static void walkTitles() throws IOException {

		String output = "";

		Map<String, String> map = Files
				.lines(Paths
						.get("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\titles.txt"),
						Charset.forName("Cp1252")).collect(
						Collectors.toMap(l -> l.split(";")[0].trim(),
								l -> l.split(";")[2].trim()));

		for (Iterator<Entry<String, String>> iterator = map.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, String> entry = iterator.next();
			String link = entry.getKey();
			String title = entry.getValue();

			// text, file
			Map<String, String> list = Files.lines(Paths.get(GLOBALLISTFILE),
					Charset.forName("Cp1252"))
					.collect(
							Collectors.toMap(p -> p.split(";")[2],
									p -> p.split(";")[1]));

			TreeMap<String, String> tree = new TreeMap<String, String>(
					new MyMatcher2(title));

			tree.putAll(list);

			String selectedtext = tree.lastKey();
			String filename = tree.get(selectedtext);

			if (StringUtils.getJaroWinklerDistance(selectedtext, title) < 0.99) {

				System.out.println("titles - Too far away ");
				System.out.println("S:" + selectedtext);
				System.out.println("T:" + title);

			}

			output += link + ";" + filename + ";" + title + "\r\n";
		}

		Files.write(
				Paths.get("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\generatedGlobal.txt"),
				output.getBytes(), StandardOpenOption.CREATE);

	}

	public static void subjectMatter(String path) throws IOException {

		System.out.println();
		System.out.println("for path " + path);

		List<String> list = Files.lines(Paths.get(GLOBALLISTFILE),
				Charset.forName("Cp1252")).collect(Collectors.toList());

		String page = new String(Files.readAllBytes(Paths.get(path
				+ "page.html")));

		String title = "<title>"
				+ Paths.get(path).getName(Paths.get(path).getNameCount() - 1)
						.toString() + "</title>";
		page = page
				.substring(
						page.indexOf("<div class=\"goal-holder center\"><h3>THE GOALS:</h3>"),
						page.indexOf("<link href=\"/members/cfmbb/themes/default/css/styles.css\" type=\"text/css\" rel=\"stylesheet\" />"));

		// page += "<p>Other links: </p> <br />";
		page = page.replaceAll("[<]img[^>]*[>]", "");
		page = page.replaceAll("[<]br [/][>]", "");
		page = page.replaceAll("[<]a[^>]*[>][<][/]a[>]", "");
		page = page.replaceAll(
				Pattern.quote("<a href=\"/members/department357.cfm\"></a>"),
				"");

		HashSet<String> used = new HashSet<String>();

		boolean filter = false;
		if (page.indexOf("<h3 align=\"center\">Level:") > -1) {

			page = preprocessSM(page);
			filter = true;
		}
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {

			String[] line = iterator.next().split(";");
			// link, file

			String relativelink = line[0]
					.substring(line[0].indexOf("/members"));

			// System.out.println(relativelink);

			Path p = Paths.get(line[1]);
			String filename = "../"
					+ p.getName(p.getNameCount() - 2).toString() + "/"
					+ p.getName(p.getNameCount() - 1).toString();

			String oldpage = page;

			page = page.replaceAll(Pattern.quote(relativelink), filename);

			// hyper prasarna
			if (!oldpage.equals(page)) {
				used.add(line[2]);
			}

		}

		Matcher m = Pattern.compile(
				"\\<a[^>]*(\\/members\\/[0-9]+\\.cfm).*\\>([^<]+)\\</a\\>")
				.matcher(page);

		Map<String, String> map = Files.lines(Paths.get(GLOBALLISTFILE),
				Charset.forName("Cp1252")).collect(
				Collectors.toMap(
						l -> l.split(";")[2]
								.replaceAll("Training Syllabus", "").trim(),
						l -> l.split(";")[1].trim(), (a, b) -> a ));

		while (m.find()) {

			if (m.group(2).trim().length() > 0) {

				/*
				 * System.out.println("http://www.maxkravmaga.com" + m.group(1)
				 * + " " + m.group(2).trim());
				 */

				String link = "http://www.maxkravmaga.com" + m.group(1).trim();
				String linktext = m.group(2).trim();

				TreeMap<String, String> tree = new TreeMap<String, String>(
						new MyMatcher2(linktext));

				// tree.putAll(map);
				// System.out.println("lll" +linktext);
				if (filter && linktext.matches(".*[PG][1-5]")) {

					String level = linktext.substring(linktext.length() - 2,
							linktext.length()).toUpperCase();

					// System.out.println(level);

					for (Iterator<String> iterator = map.keySet().iterator(); iterator
							.hasNext();) {
						String key = iterator.next();

						if (map.get(key).contains(level)) {

							tree.put(key, map.get(key));
						}

					}
				} else {

					tree.putAll(map);
				}

				String selected = tree.lastKey();

				if (StringUtils.getJaroWinklerDistance(selected, linktext) < 0.9) {

					System.out.println("Too far away ");
					System.out.println("S:" + selected);
					System.out.println("T:" + linktext);
					System.out.println(link);
					System.out.println();
					unknow.add(link);
				}

				if (!used.add(selected)) {

					System.out.println("We have found this twice:");
					System.out.println("S:" + selected);
					System.out.println("T:" + linktext);
					System.out.println(link);

				}

				Path p = Paths.get(tree.get(selected));
				String filename = "../"
						+ p.getName(p.getNameCount() - 2).toString() + "/"
						+ p.getName(p.getNameCount() - 1).toString();

				page = page.replaceAll(Pattern.quote(m.group(1).trim()),
						filename);

				// System.out.println(selected + " | " + linktext);

			}
		}

		page = "<html><head>" + title + "</head><body>" + page
				+ "</body></html>";

		Files.write(Paths.get(path + "page2.html"), page.getBytes());

		if (page.matches("(?ms).*[/]members[/][0-9]*[.]cfm.*")) {
			System.out.println("Missing link");
		}

		if (page.matches("(?ms).*[/]members.*")) {
			System.out.println("/members found in page");
		}
	}

	private static String preprocessSM(String page) throws IOException {

		int position = 0;
		String output = "";
		String heading;

		String hstring = "<h3 align=\"center\">Level:";
		int endposition = page
				.indexOf("<div class=\"additional-videos-content");

		if (endposition == -1) {
			endposition = page.length();
		}

		while (position > -1
				&& (position = page.indexOf(hstring, position)) > -1) {

			heading = page.substring(position);
			heading = heading.substring(hstring.length(),
					heading.indexOf("<", 2)).trim();
			// System.out.println(heading);
			position += heading.length();

			int nextheading = page.indexOf(hstring, position);

			while ((position = page.indexOf("<a", position)) > -1
					&& (nextheading > position || nextheading == -1)) {

				if (position > endposition) {
					break;
				}
				position = page.indexOf(">", position) + 1;

				String text = page.substring(position,
						page.indexOf("</a>", position));

				if (text.trim().length() > 0) {

					if (text.indexOf("<") > -1) {

						continue;
					}
					// System.out.println(text + " " + heading);
					page = page.substring(0, position)
							+ text
							+ " "
							+ heading
							+ page.substring(position + text.length(),
									page.length() - 1);
				}

				// position += text.length();

			}

			position = nextheading;

		}

		/*
		 * Files.write(Paths.get("c:\\Users\\Ivan\\Dropbox\\MKM\\results\\test.html"
		 * ), page.getBytes(), StandardOpenOption.CREATE);
		 */
		// /System.exit(0);

		return page;

	}

}
