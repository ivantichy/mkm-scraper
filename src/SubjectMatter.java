import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class SubjectMatter {

	public final static String DOWNLOAD_PATH = "c:\\Users\\Ivan\\dwhelper\\";

	public final static String RESULTS_PATH = "c:\\temp\\mkm\\results\\";

	static File profileDir = new File(
			"c:\\Users\\Ivan\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\ya2cbv31.default\\");
	static FirefoxProfile profile = new FirefoxProfile(profileDir);

	static FirefoxDriver wd = new FirefoxDriver(profile);

	static void cleanFiles() throws IOException {

		File dir = new File(DOWNLOAD_PATH);

		for (File file : dir.listFiles())
			if (!file.isDirectory())
				file.delete();

	}

	// "http://www.maxkravmaga.com/members/p2.cfm"

	public static void login() throws InterruptedException {

		wd.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		wd.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
		try {
			wd.get("https://www.maxkravmaga.com/members/login.cfm");
		}

		catch (Exception e) {
		}
		wd.executeScript("return window.stop()");
		// wd.findElement(By.cssSelector("body")).click();
		wd.findElement(By.id("UserID")).click();
		wd.findElement(By.id("UserID")).clear();
		wd.findElement(By.id("UserID")).sendKeys("xxx");
		wd.findElement(By.id("Password")).click();
		wd.findElement(By.id("Password")).clear();
		wd.findElement(By.id("Password")).sendKeys("xxxx");

		try {
			wd.findElement(By.id("Password")).submit();
		}

		catch (Exception e) {
		}
		wd.executeScript("return window.stop()");

		// The resource you have requested is exclusively available to our
		// premium members - You must log in below to access premium content.
		// wd.findElement(By.xpath("//form[@id='LoginForm']/table[2]/tbody/tr[4]/td/input[2]")).click();
		// wd.findElement(By.cssSelector("body")).click();
		wd.findElement(By.id("UserID")).click();
		wd.findElement(By.id("UserID")).clear();
		wd.findElement(By.id("UserID")).sendKeys("xxxx");
		wd.findElement(By.id("Password")).click();
		wd.findElement(By.id("Password")).clear();
		wd.findElement(By.id("Password")).sendKeys("xxx");
		try {
			wd.findElement(By.id("Password")).submit();
		} catch (Exception e) {
		}
		wd.executeScript("return window.stop()");
		wd.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

		Thread.sleep(10000);

	}

	public static void stahni(String url, String folder) throws Exception {

		FileWriter logger = new FileWriter(DOWNLOAD_PATH + "\\logger.txt");
		FileWriter linker = new FileWriter(DOWNLOAD_PATH + "\\links.txt");
		FileWriter page = new FileWriter(DOWNLOAD_PATH + "\\page.html");

		cleanFiles();

		wd.get(url);

		page.write(wd.getPageSource());
		page.close();

		List list = wd.findElementsByTagName("a");

		HashSet links = new HashSet();

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			WebElement object = (WebElement) iterator.next();

			String link = object.getAttribute("href");

			if (link != null) {

				if (link.matches(".*members[/][0-9]+[.]cfm$")) {
					System.out.println(object.getAttribute("href"));
					System.out.println(object.getText());
					// links.clear();
					links.add(link);

					WebElement parent = object.findElement(By.xpath(".."));
					linker.write(link
							+ ";"
							+ object.getText().replace("\n", "")
									.replace("\r", "").replace(';', ',').trim()
							+ ";"
							+ parent.getText().replace("\n", "")
									.replace("\r", "").replace(';', ',').trim()
							+ "\r\n");
				}
			}
		}
		linker.close();
		System.out.println("Size: " + links.size());
		wd.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		wd.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

		for (Iterator iterator = links.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();

			logger.write(object + "\r\n");

		}

		logger.close();
		wd.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

		wd.get("www.google.com");

		boolean go = true;
		while (go) {
			try {
				Thread.sleep(10000);
				Files.move(Paths.get(SubjectMatter.DOWNLOAD_PATH),
						Paths.get(SubjectMatter.RESULTS_PATH + folder),
						java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				go = false;
			} catch (Exception e) {

				System.out.println(e.getClass());
			}

			;

		}

		Files.createDirectories(Paths.get(SubjectMatter.DOWNLOAD_PATH));

		// wd.quit();

	}

	private static void cekejCas(long odminut, long dominut)
			throws InterruptedException {

		long cas = (long) (odminut + Math.random() * (dominut - odminut));
		System.out.println("Waiting... " + cas + " minutes");

		Thread.sleep(cas * 60 * 1000);

	}

	public static void getTitles() throws IOException, InterruptedException {

		Map<String, String> global = Files.lines(
				Paths.get(RESULTS_PATH + "\\global.txt")).collect(
				Collectors.toMap(p -> p.split(";")[2], p -> p));
		String out = "";

		List<String> list = Files.readAllLines(Paths.get(RESULTS_PATH
				+ "\\unknown.txt"));

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			String string = ((String) iterator.next()).trim();

			wd.get(string);

			String title = wd.getTitle().trim();

			TreeMap tree = new TreeMap(new FilesWalkerTexasRanger.MyMatcher2(
					title));

			tree.putAll(global);

			String selected = ((String) tree.lastEntry().getValue()).split(";")[2];
			String filename = ((String) tree.lastEntry().getValue()).split(";")[1];

			if (StringUtils.getJaroWinklerDistance(selected, title) < 0.95) {

				System.out.println("far away ");
				System.out.println(selected);
				System.out.println(title);
			}

			out += string + ";" + selected + ";" + title + "\r\n";

		}

		Files.write(Paths.get(RESULTS_PATH + "\\titles.txt"), out.getBytes(),
				StandardOpenOption.CREATE);

	}

	public static void main(String[] args) throws Exception {
		login();
		// stahni("http://www.maxkravmaga.com/members/Outside-Defenses-P1-Training-Syllabus-4.cfm",
		// "DefendingArmedAttacksBluntObjects");

		// odtud dal

		/*
		 * stahni(
		 * "http://www.maxkravmaga.com/members/Preliminaries-Principles-of-Attack-P1-Training-Syllabus-3.cfm"
		 * , "CounterAttacks");
		 */
		/*
		 * stahni(
		 * "http://www.maxkravmaga.com/members/Dealing-with-Falls-P1-Training-Syllabus-2-2.cfm"
		 * , "GroundFighting");
		 * 
		 * stahni(
		 * "http://www.maxkravmaga.com/members/Defending-Against-Front-Chokes-P1-Training-Syllabus-2.cfm"
		 * , "ChokeGrabReleases");
		 * 
		 * stahni(
		 * "http://www.maxkravmaga.com/members/Ready-Stances-and-Movements-P1-Training-Syllabus-2-3.cfm"
		 * , "DefendingUnarmedAttacks");
		 */
		/*
		 * // http://www.maxkravmaga.com/members/Edged-Weapons-members.cfm //
		 * EdgedWeapons
		 */

		/*
		 * // http://www.maxkravmaga.com/members/handgun-threats.cfm
		 * HandgunThreats
		 */

		getTitles();

	}

}
