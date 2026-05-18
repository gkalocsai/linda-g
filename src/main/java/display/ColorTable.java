package display;
import java.awt.Color;

public class ColorTable {

	private final static Color[] colors={new Color(255,160,122),
		  	new Color(250,128,114),
		  	new Color(233,150,122),
		  	new Color(240,128,128),
		  	new Color(205,92,92),
		  	new Color(220,20,60),
		  	new Color(178,34,34),
		  	new Color(255,0,0),
		  	new Color(139,0,0),
		    new Color(255,127,80),
		  	new Color(255,99,71),
		  	new Color(255,69,0),
		  	new Color(255,215,0),
		  	new Color(255,165,0),
		  	new Color(255,140,0),
		  	new Color(255,228,181),
		  	new Color(255,218,185),
		  	new Color(238,232,170),
		  	new Color(240,230,140),
		  	new Color(189,183,107),
		  	new Color(255,255,0),
		  	new Color(124,252,0),
		  	new Color(127,255,0),
		  	new Color(50,205,50),
		  	new Color(0,255,0),
		  	new Color(34,139,34),
		  	new Color(0,128,0),
		  	new Color(0,100,0),
		  	new Color(173,255,47),
		  	new Color(154,205,50),
		  	new Color(0,255,127),
		  	new Color(0,250,154),
		  	new Color(144,238,144),
		  	new Color(152,251,152),
		  	new Color(143,188,143),
		  	new Color(60,179,113),
		  	new Color(46,139,87),
		  	new Color(128,128,0),
		  	new Color(85,107,47),
		  	new Color(107,142,35),
		  	new Color(176,196,222),
		  	new Color(30,144,255),
		  	new Color(70,130,180),
		  	new Color(65,105,225),
		  	new Color(0,0,255),
		  	new Color(0,0,205),
		  	new Color(0,0,128),
		  	new Color(123,104,238),
		  	new Color(106,90,205),
		  	new Color(72,61,139),
		 	new Color(186,85,211),
		  	new Color(147,112,219),
		  	new Color(138,43,226),
		  	new Color(148,0,211),
		  	new Color(153,50,204),
		  	new Color(128,0,128),
		  	new Color(75,0,130)};
	
	public static Color getOne(int hashcode) {
		if(hashcode < 0) hashcode=-hashcode;
		return colors[hashcode % colors.length];
		
	}

	
}
