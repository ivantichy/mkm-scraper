import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;

import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Sleeper;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.*;

import static org.openqa.selenium.OutputType.*;

public class Levels {

	public final static String DOWNLOAD_PATH = "c:\\Users\\Ivan\\dwhelper\\";

	public final static String RESULTS_PATH = "c:\\temp\\mkm\\results\\";

	private static void cleanFiles() throws IOException {

		File dir = new File(DOWNLOAD_PATH);

		for (File file : dir.listFiles())
			if (!file.isDirectory())
				file.delete();

	}

	// "http://www.maxkravmaga.com/members/p2.cfm"

	public static void stahni(String url, String folder) throws Exception {

		FileWriter logger = new FileWriter(DOWNLOAD_PATH + "\\logger.txt");
		FileWriter linker = new FileWriter(DOWNLOAD_PATH + "\\links.txt");
		FileWriter page = new FileWriter(DOWNLOAD_PATH + "\\page.html");

		File profileDir = new File(
				"c:\\Users\\Ivan\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\ya2cbv31.default\\");
		FirefoxProfile profile = new FirefoxProfile(profileDir);

		FirefoxDriver wd = new FirefoxDriver(profile);

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
		wd.findElement(By.id("Password")).sendKeys("xxx");

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
		wd.findElement(By.id("UserID")).sendKeys("xxx");
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

		int size = links.size();
		int pos = 0;
		for (Iterator iterator = links.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();

			pos++;
			System.out.println("Going to " + object);
			System.out.println(pos + "/" + size);
			try {
				wd.get(object);
			}

			catch (Exception e) {

				System.out.println("catch");
			}
			List imgs = wd.findElementsByTagName("div");
			for (Iterator iterator2 = imgs.iterator(); iterator2.hasNext();) {
				WebElement img = (WebElement) iterator2.next();

				if (img.getAttribute("id") != null) {

					if (img.getAttribute("id").matches("p[0-9].*display_icon$")) {

						System.out.println(img.getAttribute("id"));

						//

						try {
							img.click();
						}

						catch (Exception e) {

							System.out.println("catch2");
						}

						String line = url + ";"
								+ wd.getTitle().replace(';', ',') + ";"
								+ object;
						logger.write(line + "\r\n");

					}

				}

			}
			wd.get("www.google.com");

			cekejCas(2, 3);

		}
		logger.close();
		wd.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

		wd.get("www.google.com");

		// Files.createDirectory(Paths.get(RESULTS_PATH + folder));

		boolean go = true;
		while (go) {
			try {
				Thread.sleep(10000);
				Files.move(Paths.get(DOWNLOAD_PATH),
						Paths.get(RESULTS_PATH + folder),
						java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				go = false;
			} catch (Exception e) {

				System.out.println(e.getClass());
			}

			;

		}

		Files.createDirectories(Paths.get(DOWNLOAD_PATH));

		wd.quit();

		cekejCas(22 * 60, 26 * 60);

	}

	private static void cekejCas(long odminut, long dominut)
			throws InterruptedException {

		long cas = (long) (odminut + Math.random() * (dominut - odminut));
		System.out.println("Waiting... " + cas + " minutes");

		Thread.sleep(cas * 60 * 1000);

	}

	public static void main(String[] args) throws Exception {

		// stahni("http://www.maxkravmaga.com/members/p1.cfm", "P1x");
		// stahni("http://www.maxkravmaga.com/members/p2.cfm", "P2x")
		// stahni("http://www.maxkravmaga.com/members/p3.cfm", "P3x");
		// stahni("http://www.maxkravmaga.com/members/p4.cfm", "P4x");
		// stahni("http://www.maxkravmaga.com/members/p5.cfm", "P5x");
		// stahni("http://www.maxkravmaga.com/members/g1.cfm", "G1");
		// stahni("http://www.maxkravmaga.com/members/g2.cfm", "G2");
		// stahni("http://www.maxkravmaga.com/members/g3.cfm", "G3");
		// stahni("http://www.maxkravmaga.com/members/g4.cfm", "G4");
	//	stahni("http://www.maxkravmaga.com/members/g5.cfm", "G5");

	}

	public static boolean isAlertPresent(FirefoxDriver wd) {
		try {
			wd.switchTo().alert();
			return true;
		} catch (NoAlertPresentException e) {
			return false;
		}
	}
}