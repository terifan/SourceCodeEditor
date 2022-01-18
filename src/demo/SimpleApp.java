package demo;

import javax.swing.JFrame;
import org.terifan.sourcecodeeditor.Document;
import org.terifan.sourcecodeeditor.JavaSyntaxParser;
import org.terifan.sourcecodeeditor.SourceEditor;
import org.terifan.sourcecodeeditor.SyntaxParser;


public class SimpleApp
{
	public static void main(String ... args)
	{
		try
		{
			SyntaxParser parser = new JavaSyntaxParser();
			Document document = new Document(
				"public static void main(String[] args)\n" +
				"{\n" +
				"	try\n" +
				"	{\n" +
				"		System.out.println(\"3 x 5 = \" + 3 * 5);\n" +
				"	}\n" +
				"	catch (Exception e)\n" +
				"	{\n" +
				"		e.printStackTrace(System.out);\n" +
				"	}\n" +
				"}"
			);

			JFrame frame = new JFrame();
			frame.add(new SourceEditor(parser, document));
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
