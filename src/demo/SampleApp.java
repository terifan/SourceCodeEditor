package demo;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.parsers.JavaSyntaxParser;
import org.terifan.sourcecodeeditor.SourceEditor;
import org.terifan.sourcecodeeditor.parsers.SqlSyntaxParser;
import org.terifan.sourcecodeeditor.SyntaxParser;
import org.terifan.sourcecodeeditor.parsers.TextSyntaxParser;
import org.terifan.sourcecodeeditor.parsers.XmlSyntaxParser;


public class SampleApp
{
	public static void main(String... args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			SyntaxParser parserJava = new JavaSyntaxParser();
			Document documentJava = new Document(
				"public static void main(String[] args)\n"
				+ "{\n"
				+ "	HashSet<String> test = new HashSet<>();\n"
				+ "	try\n"
				+ "	{\n"
				+ "		System.out.println(\"3 x 5 = \" + 3 * 5);\n"
				+ "	}\n"
				+ "	catch (Exception e)\n"
				+ "	{\n"
				+ "		e.printStackTrace(System.out);\n"
				+ "	}\n"
				+ "}\n"
			);

			SyntaxParser parserSql = new SqlSyntaxParser();
			Document documentSql = new Document(
				"SELECT ed.url FROM tbl_epod_data ed\n" +
				"	INNER JOIN tbl_subscription_publication sp\n" +
				"	INNER JOIN tbl_publication_reference pr ON sp.publication_id = pr.publication_id\n" +
				"	INNER JOIN tbl_epod_reference er ON pr.reference_value = er.reference_value AND pr.reference_type = er.reference_type ON ed.id = er.epod_id\n" +
				"	WHERE sp.subscription_id = @id AND sp.processed = 'n' AND sp.create_date_time > DATEADD(day, - 14, GETDATE())\n" +
				"	GROUP BY ed.url\n"
			);

			SyntaxParser parserXml = new XmlSyntaxParser();
			Document documentXml = new Document(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<books xmlns=\"http://www.contoso.com/books\">\n" +
				"  <book genre=\"novel\" ISBN=\"1-861001-57-8\" publicationdate=\"1823-01-28\">\n" +
				"    <title>Pride And Prejudice</title>\n" +
				"    <price>24.95</price>\n" +
				"  </book>\n" +
				"  <book genre=\"novel\" ISBN=\"1-861002-30-1\" publicationdate=\"1985-01-01\">\n" +
				"    <title>The Handmaid's Tale</title>\n" +
				"    <price>29.95</price>\n" +
				"  </book>\n" +
				"  <book genre=\"novel\" ISBN=\"1-861001-45-3\" publicationdate=\"1811-01-01\">\n" +
				"    <title>Sense and Sensibility</title>\n" +
				"    <price>19.95</price>\n" +
				"  </book>\n" +
				"</books>\n"
			);

//			SyntaxParser parserJson = new JsonSyntaxParser();
//			Document documentJson = new Document(
//				"{\"carrierId\": \"CH-16519\", \"carrierName\": \"Hug Hug Transporte GmbH\", \"status\": \"Inactive\", \"cinNumber\": \"123\", \"blockedInCountries\": [ \"ES\" ], \"countryQualifications\": [], \"transportServiceQualification\": [], \"businessAreas\": [ \"FTL\", \"LTL\", \"LhMainHaulage\", \"CoDiRegionalTraffic\", \"CourierService\", \"Warehousing\", \"HeavyWeightOversizes\", \"Multimodal\" ], \"turnoverEuroValue\": 15, \"warnings\": [ \"Competitor\", \"ObjectOfIdentityTheft\" ], \"type\": \"CarrierWithOwnFleet\", \"nationalTaxId\": \"PL100200015\", \"euVatId\": \"PL100200012\", \"insuranceCoverage\": \"LIMITED_INSURANCE_SCOPE\", \"license\": \"Cemt\", \"adminName\": \"Tomas Pascius\", \"adminEmail\": \"e.voras@apeegris.lt\", \"adminPhone\": \"+36514116197\", \"address\": \"Hubelweg 8\", \"addressAddition\": \"LLP\", \"countryCode\": \"CH\", \"postCode\": \"4663\", \"city\": \"Aarburg\", \"contacts\": [{ \"id\": \"3216549873216497\", \"email\": \"pinjata69@gmail.com\", \"firstName\": \"Patrik\", \"lastName\": \"Olsson\", \"phoneNumber\": \"+31616141651\", \"countryCode\": \"SE\", \"city\": \"Goteborg\" }], \"insuranceLimitDomestic\": \"300000\", \"insuranceLimitInternational\": \"\", \"insuranceLimitCemt\": \"\"}\n"
//			);

//			SyntaxParser parserHtml = new HtmlSyntaxParser();
//			Document documentHtml = new Document(
//				""
//			);

			SyntaxParser parserText = new TextSyntaxParser();
			Document documentText = new Document(
				"The XmlDocument class is an in-memory representation of an XML document. It implements the W3C XML Document Object Model (DOM) Level 1 Core and the Core DOM Level 2.\n" +
				"\n" +
				"DOM stands for document object model. To read more about it, see XML Document Object Model (DOM).\n" +
				"\n" +
				"You can load XML into the DOM by using the XmlDocument class, and then programmatically read, modify, and remove XML in the document.\n" +
				"\n" +
				"If you want to pry open the XmlDocument class and see how it's implemented, see the Reference Source."
			);

			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.add("Java", new JScrollPane(new SourceEditor(parserJava, documentJava)));
			tabbedPane.add("SQL", new JScrollPane(new SourceEditor(parserSql, documentSql)));
			tabbedPane.add("Xml", new JScrollPane(new SourceEditor(parserXml, documentXml)));
//			tabbedPane.add("Json", new JScrollPane(new SourceEditor(parserJson, documentJson)));
//			tabbedPane.add("Html", new JScrollPane(new SourceEditor(parserHtml, documentHtml)));
			tabbedPane.add("Text", new JScrollPane(new SourceEditor(parserText, documentText)));

			JFrame frame = new JFrame();
			frame.add(tabbedPane);
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}
