import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Other {

	public static FirefoxDriver wd = SubjectMatter.wd;

	public static void main(String[] args) throws Exception {

		SubjectMatter.login();

	/*	stahni("http://www.maxkravmaga.com/members/Eyal-Yanilov-Combat-Mindset-Mental-Training.cfm",
				"CombatMindsetMentalTraining");
				
				*
				*/
	//	stahni("http://www.maxkravmaga.com/members/Eyal-Yanilov-Masterclass.cfm","MasterClassSeminar");
		
		
		
		stahni("http://www.maxkravmaga.com/members/krav-maga-theory-and-lectures.cfm","TheoryAndLectures");
		
		stahni("http://www.maxkravmaga.com/members/using-firearms-as-non-lethal-weapons.cfm","Firearms");
		
		stahni("http://www.maxkravmaga.com/members/intro-to-anticarjacking-and-road-rage-defense.cfm", "AntiCarJacking");

	}

	public static void stahni(String url, String folder) throws Exception {

		FileWriter logger = new FileWriter(SubjectMatter.DOWNLOAD_PATH
				+ "\\logger.txt");
		FileWriter linker = new FileWriter(SubjectMatter.DOWNLOAD_PATH
				+ "\\links.txt");
		FileWriter page = new FileWriter(SubjectMatter.DOWNLOAD_PATH
				+ "\\page.html");

		SubjectMatter.cleanFiles();

		wd.get(url);

		page.write(wd.getPageSource());
		page.close();

		List list = wd.findElementsByTagName("a");

		HashSet links = new HashSet();

		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			WebElement object = (WebElement) iterator.next();

			String link = object.getAttribute("href");

			if (link != null) {

				if (link.matches(".*members[/].+[.]cfm$")) {
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

		links.remove("http://www.maxkravmaga.com/members/logout.cfm");
		links.remove("http://www.maxkravmaga.com/members/department336.cfm");
		links.remove("http://www.maxkravmaga.com/members/main.cfm");

		System.out.println("Size: " + links.size());
		wd.manage().timeouts().implicitlyWait(25, TimeUnit.SECONDS);
		wd.manage().timeouts().pageLoadTimeout(25, TimeUnit.SECONDS);

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
			
			Thread.sleep(5000);
			List imgs = wd.findElementsByTagName("div");
			for (Iterator iterator2 = imgs.iterator(); iterator2.hasNext();) {
				WebElement img = (WebElement) iterator2.next();

				try {

					if (img.getAttribute("id") != null) {

						if (img.getAttribute("id").matches(
								"p[0-9].*display_icon$")) {

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
				} catch (Exception e) {
					System.out.println("catch3");

				}

			}
			wd.get("www.google.com");

			// cekejCas(2, 3);

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

		wd.quit();

	}

}
