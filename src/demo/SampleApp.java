package demo;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.AbstractAction;
import static javax.swing.Action.SELECTED_KEY;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.parsers.JavaSyntaxParser;
import org.terifan.sourcecodeeditor.SourceEditor;
import org.terifan.sourcecodeeditor.StyleSheet;
import org.terifan.sourcecodeeditor.parsers.SqlSyntaxParser;
import org.terifan.sourcecodeeditor.SyntaxParser;
import org.terifan.sourcecodeeditor.parsers.TextSyntaxParser;
import org.terifan.sourcecodeeditor.parsers.XmlSyntaxParser;


public class SampleApp
{
	private static JTabbedPane tabbedPane;


	public static void main(String... args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			SyntaxParser parserJava = new JavaSyntaxParser();
			Document documentJava = new Document(
				"package demo;\n"
				+ "\n"
				+ "/**\n"
				+ " * documentation\thello\t\t\tworld\n"
				+ " */\n"
				+ "@Sample\n"
				+ "class HelloWorld\n"
				+ "{\n"
				+ "	public static void main(String... args)\n"
				+ "	{\n"
				+ "		HashSet<String> test/*ing*/ = new HashSet<>();\n"
				+ "		try\n"
				+ "		{\n"
				+ "			char c1 = '';\n"
				+ "			char c2 = ' ';\n"
				+ "			char c3 = '\\u0000';\n"
				+ "			char c4 = '\\123';\n"
				+ "			char c5 = '\n"
				+ "			char c6 = 'abc';\n"
				+ "			char c6 = 'abc;\n"
				+ "			int x = number();\n"
				+ "//			System.out.println(\"3 * X = \" + 3 * x);\n"
				+ "		}\n"
				+ "		catch (Exception e)\n"
				+ "		{\n"
				+ "			e.printStackTrace(System.out);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	private static int number() throws Exception \n"
				+ "	{\n"
				+ "		return 5; //comment\n"
				+ "	}\n"
				+ "}\n"
			);

			SyntaxParser parserSql = new SqlSyntaxParser();
			Document documentSql = new Document(
				"SELECT ed.url FROM tbl_epod_data ed\n" +
				"	INNER JOIN tbl_subscription_publication sp\n" +
				"	INNER JOIN tbl_publication_reference pr ON sp.publication_id = pr.publication_id\n" +
				"	INNER JOIN tbl_epod_reference er ON pr.reference_value = er.reference_value AND pr.reference_type = er.reference_type ON ed.id = er.epod_id\n" +
				"	WHERE sp.subscription_id = @id AND sp.processed = 'n' AND sp.create_date_time > DATEADD(day, -14, GETDATE())\n" +
				"	GROUP BY ed.url\n"
			);

			SyntaxParser parserXml = new XmlSyntaxParser();
			Document documentXml = new Document(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<books xmlns=\"http://www.contoso.com/books\">\n" +
				"    <book genre=\"novel\" ISBN=\"1-861001-57-8\" publicationdate=\"1823-01-28\">\n" +
				"        <title>Pride And Prejudice</title>\n" +
				"        <price>24.95</price>\n" +
				"    </book>\n" +
				"<!--\n" +
				"    <book genre=\"novel\" ISBN=\"1-861002-30-1\" publicationdate=\"1985-01-01\">\n" +
				"        <title>The Handmaid's Tale</title>\n" +
				"        <price>29.95</price>\n" +
				"    </book>\n" +
				"-->\n" +
				"    <book genre=\"novel\" ISBN=\"1-861001-45-3\" publicationdate=\"1811-01-01\">\n" +
				"        <title>Sense and Sensibility</title>\n" +
				"        <price>19.95</price>\n" +
				"    </book>\n" +
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

			tabbedPane = new JTabbedPane();
			add(tabbedPane, "Java Dark", new SourceEditor(parserJava, documentJava, StyleSheet.installJava("monospaced", 14, "dark"))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);
			add(tabbedPane, "Java Light", new SourceEditor(new JavaSyntaxParser(), new Document(documentJava), StyleSheet.installJava("monospaced", 14, ""))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);
			add(tabbedPane, "SQL Dark", new SourceEditor(parserSql, documentSql, StyleSheet.installSql("monospaced", 14, "dark"))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);
			add(tabbedPane, "SQL Light", new SourceEditor(parserSql, new Document(documentSql), StyleSheet.installSql("monospaced", 14, ""))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);
			add(tabbedPane, "Xml Dark", new SourceEditor(parserXml, documentXml, StyleSheet.installXml("monospaced", 14, "dark"))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);
			add(tabbedPane, "Xml Light", new SourceEditor(parserXml, new Document(documentXml), StyleSheet.installXml("monospaced", 14, ""))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);
			add(tabbedPane, "Text", new SourceEditor(parserText, documentText, StyleSheet.installText("monospaced", 14, ""))
				.setWhitespaceSymbolEnabled(true)
				.setLineBreakSymbolEnabled(true)
			);

			JPanel panel = new JPanel(new BorderLayout());
			panel.add(tabbedPane, BorderLayout.CENTER);

			JFrame frame = new JFrame();
			frame.add(panel);
			frame.setSize(1400, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}

	private static void add(JTabbedPane aTabbedPane, String aName, SourceEditor aEditor)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(createToolbar(aEditor), BorderLayout.NORTH);
		panel.add(new JScrollPane(aEditor), BorderLayout.CENTER);
		aTabbedPane.add(aName, panel);
	}

	private static JToolBar createToolbar(SourceEditor aEditor)
	{
		JToolBar toolbar = new JToolBar();
		toolbar.add(newButton(aEditor, "Multiline", e -> e.isMultiline(), (e, b) -> e.setMultiline(b)));
		toolbar.add(newButton(aEditor, "AutoIndent", e -> e.isAutoIndentEnabled(), (e, b) -> e.setAutoIndentEnabled(b)));
		toolbar.add(newButton(aEditor, "BoldCaret", e -> e.isBoldCaretEnabled(), (e, b) -> e.setBoldCaretEnabled(b)));
		toolbar.add(newButton(aEditor, "HighlightCaretRow", e -> e.isHighlightCaretRowEnabled(), (e, b) -> e.setHighlightCaretRowEnabled(b)));
		toolbar.add(newButton(aEditor, "HighlightTextCaseSensative", e -> e.isHighlightTextCaseSensative(), (e, b) -> e.setHighlightTextCaseSensative(b)));
		toolbar.add(newButton(aEditor, "IndentLines", e -> e.isIndentLinesEnabled(), (e, b) -> e.setIndentLinesEnabled(b)));
		toolbar.add(newButton(aEditor, "LineBreakSymbol", e -> e.isLineBreakSymbolEnabled(), (e, b) -> e.setLineBreakSymbolEnabled(b)));
		toolbar.add(newButton(aEditor, "SelectedLineBreakSymbol", e -> e.isSelectedLineBreakSymbolEnabled(), (e, b) -> e.setSelectedLineBreakSymbolEnabled(b)));
		toolbar.add(newButton(aEditor, "OverwriteText", e -> e.isOverwriteTextEnabled(), (e, b) -> e.setOverwriteTextEnabled(b)));
		toolbar.add(newButton(aEditor, "PaintFullRowSelection", e -> e.isPaintFullRowSelectionEnabled(), (e, b) -> e.setPaintFullRowSelectionEnabled(b)));
		toolbar.add(newButton(aEditor, "TabIndentsText", e -> e.isTabIndentsTextBlockEnabled(), (e, b) -> e.setTabIndentsTextBlockEnabled(b)));
		toolbar.add(newButton(aEditor, "WhitespaceSymbol", e -> e.isWhitespaceSymbolEnabled(), (e, b) -> e.setWhitespaceSymbolEnabled(b)));
		return toolbar;
	}

	public static JToggleButton newButton(SourceEditor aEditor, String aLabel, Function<SourceEditor, Boolean> aIsSelected, BiConsumer<SourceEditor,Boolean> aUpdate)
	{
		JToggleButton button = new JToggleButton(new AbstractAction(aLabel)
		{
			{
				putValue(SELECTED_KEY, aIsSelected.apply(aEditor));
			}
			@Override
			public void actionPerformed(ActionEvent aEvent)
			{
				aUpdate.accept(aEditor, ((JToggleButton)aEvent.getSource()).isSelected());
				aEditor.repaint();
			}
		});
		button.setFocusPainted(false);
		button.setFocusable(false);
		return button;
	}
}
