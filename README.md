# SourceCodeEditor

The SourceCodeEditor is a color coded text editor supporting HTML, Java, SQL, XML, XSL.

This is old code and has plenty of bugs but is still functional.

This code is released to public domain. You can use it anywhere / anyhow.

### Example
```
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
frame.add(new JScrollPane(new SourceEditor(parser, document)));
frame.setSize(1024, 768);
frame.setLocationRelativeTo(null);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.setVisible(true);
```
