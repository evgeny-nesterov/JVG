package javax.swing;

public class MultiLineLabel extends JLabel {
	private static final long serialVersionUID = 1L;

	public MultiLineLabel(String text) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><table border=\"0\" cellpadding=\"1\"><tr><td valign=\"top\" wrap>");
		sb.append(text.replaceAll("\n", "<br>"));
		sb.append("</td></tr></table></html>");
		setText(sb.toString());
	}
}
