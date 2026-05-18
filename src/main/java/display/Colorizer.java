package display;
import java.awt.Color;

public class Colorizer {

	
	
	public static String colorize(String input, Color textColor, Color bgColor){
		 StringBuilder sb = new StringBuilder();
		 if(textColor !=null) {
			 sb.append("\u001b[38;2;"+textColor.getRed()+";"+textColor.getGreen()+";"+textColor.getBlue()+"m");
		 } 
		 if(bgColor != null) {
			 sb.append("\u001b[48;2;"+bgColor.getRed()+";"+bgColor.getGreen()+";"+bgColor.getBlue()+"m");	 
			 
		 }
		 sb.append(input);
		 if(textColor != null || bgColor!=null) {
			 sb.append("\u001b[0m");
		 }
		 return sb.toString();
	}
	
	public static String colorize(String input, Color textColor){
		return colorize(input, textColor, null);
	}
	

	public static String withBackground(String input, Color bgColor){
		Color textColor;
		if(bgColor.getRed()+ bgColor.getGreen()+bgColor.getBlue()<3*128) textColor=Color.WHITE;
		else textColor = Color.BLACK;
		return colorize(input, textColor, bgColor);
	}
	
	
}
