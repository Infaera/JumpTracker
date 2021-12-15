/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jumptracker;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Infaera
 */
public class Conversion {

//    public static void main(String[] args) {
//        Jumper jumper = new Jumper("Fella");
//        Jump jump = new Jump("Universe");
//        Option option = new Option("Origin");
//        jump.addOption(option);
//        jumper.addJump(jump);
//        System.out.println(jumperToHTML(HTMLToJumper(jumperToHTML(jumper))));
//    }
    static String jumpToOutputSelections(Jump jump) {
        String output = "";
        output += jump.getName() + " [" + jump.getPoints() + "]{@" + jump.calcCosts() + "}" + "\n\n";
        for (Option o : jump.getOptionList()) {
            output += o.getName() + " ["
                    + (o.getPoints() > 0 ? "+" + o.getPoints() : o.getPoints())
                    + "]\n";
        }
        
        return output;
    }

    static String jumpToOutputDetails(Jump jump) {
        String output = "";
        output += jump.getName() + " [" + jump.getPoints() + "]{@" + jump.calcCosts() + "}" + "\n\n";
        for (Option o : jump.getOptionList()) {
            output += o.getName() + " (" + o.getType() + ") ["
                    + (o.getPoints() == 0 ? "Free" : (o.getPoints() < 0 ? -o.getPoints() : "+" + o.getPoints()))
                    + "]\n";
            if (o.getDescription().length() > 1) {
                output += ("   " + o.getDescription() + "\n").replaceAll("[\n\r]{2,}", "\n");
            }
            if (o.getNotes().length() > 1) {
                output += ("    " + o.getNotes() + "\n").replaceAll("[\n\r]{2,}", "\n");
            }
            output += "\n";
        }
        return output;
    }

    public static String substringBetween(final String str, final String open, final String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        final int start = str.indexOf(open);
        if (start != -1) {
            final int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private static final Pattern PATTERN = Pattern.compile("[^ -~]");

    private static String cleanText(String text) {
        Matcher matcher = PATTERN.matcher(text);
        if (matcher.find()) {
            text = text.replace(matcher.group(0), "");
        }
        return text;
    }

    public static String jumperToHTML(Jumper jumper) {

        String text = "";
        text += "<jumptracker>\n";
        text += "<version>" + TrackerFrame.VERSION + "</version>\n";
        text += "<jumper><jumpername>" + jumper.getName() + "</jumpername>";
        text += "<types>" + Arrays.toString(jumper.getTypesArray()) + "</types>\n";

        text = jumper.getJumpList().stream().map((j) -> jumpToHTML(j) + "\n").reduce(text, String::concat);

        text += "</jumper>\n";
        text += "</jumptracker>";
        text = text.replace(".*", ". *").replace("(+", "( +");

        return text;
    }

    public static Jumper HTMLToJumper(String text) {

        Jumper jumper = new Jumper();

        try {

            if (!text.startsWith("<jumptracker>")) {
                return null;
            }

            text = cleanText(text);
            text = text.replace("\r", "").replace("\n", "");
            //text = text.replaceFirst("<jumptracker>", "");

            String version = substringBetween(text, "<version>", "</version>");

            text = substringBetween(text, "<jumper>", "</jumper>");
            String name = substringBetween(text, "<jumpername>", "</jumpername>");
            jumper.setName(name);
            String[] types = substringBetween(text, "<types>[", "]</types>").split(", ");
            jumper.setTypes(types);

            int checkLength;
            String jumpText;
            while (!isEmpty(jumpText = substringBetween(text, "<jump>", "</jump>"))) {

                checkLength = text.length();
                jumper.addJump(HTMLToJump(jumpText));

                text = text.replaceFirst("<jump>", "");
                try {
                    text = text.replaceFirst(jumpText, "");
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("HTML to Jumper Error");
                }
                text = text.replaceFirst("</jump>", "");

                if (checkLength == text.length()) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return jumper;
    }

    public static String jumpToHTML(Jump jump) {
        String text = "";

        text += "<jump>";
        text += "<jumpname>" + jump.getName() + "</jumpname>";
        text += "<supplement>" + (jump.isSupplement() ? "true" : "false") + "</supplement>";
        text += "<points>" + jump.getPoints() + "</points>\n";
        text += "<stepvalue>" + jump.getStepValue() + "</stepvalue>";

        text = jump.getOptionList().stream().map((o) -> optionToHTML(o) + "\n").reduce(text, String::concat);
        text += "</jump>";

        return text;
    }
    
    

    public static Jump HTMLToJump(String text) {
        Jump jump = new Jump();

        String sub = Conversion.substringBetween(text, "<jumpname>", "</jumpname>");
        jump.setName(sub);
        sub = Conversion.substringBetween(text, "<supplement>", "</supplement>");
        jump.setSupplement(sub.equals("true"));
        sub = Conversion.substringBetween(text, "<points>", "</points>");
        jump.setPoints(Integer.parseInt(sub));
        try {
            String subtext = substringBetween(text, "<stepvalue>", "</stepvalue>");
            int stepValue = Integer.parseInt(subtext);
            jump.setStepValue(stepValue);
        } catch (Exception e) {
            jump.setStepValue(1);
        }

        int checkLength;
        String optionText;

        while ((optionText = substringBetween(text, "<option>", "</option>")) != null) {

            checkLength = text.length();

            Option to = HTMLToOption(optionText);
            to.setJumpName(jump.getName());
            jump.addOption(to);

            text = text.replaceFirst("<option>", "");
            try {
                text = text.replaceFirst(optionText, "");
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("HTML to Jump Error");
            }
            text = text.replaceFirst("</option>", "");

            if (checkLength == text.length()) {
                break;
            }
        }

        return jump;
    }

    private static String optionToHTML(Option option) {
        String text = "";

        text += "<option>";
        text += "<optionname>" + option.getName() + "</optionname>";
        text += "<type>" + option.getType() + "</type>";
        text += "<points>" + option.getPoints() + "</points>";
        text += "<chain>" + (option.isChain() ? "true" : "false") + "</chain>";
        text += "<active>" + (option.isActive() ? "true" : "false") + "</active>";
        text += "<description>" + option.getDescription().replace("\n", "%%%") + "</description>";
        text += "<notes>" + option.getNotes().replace("\n", "%%%") + "</notes>";
        text += "</option>";

        return text;

    }

    private static Option HTMLToOption(String text) {
        Option option = new Option();

        String sub = Conversion.substringBetween(text, "<optionname>", "</optionname>");
        option.setName(sub);
        sub = substringBetween(text, "<type>", "</type>");
        option.setType(sub);
        sub = Conversion.substringBetween(text, "<points>", "</points>");
        option.setPoints(Integer.parseInt(sub));
        sub = Conversion.substringBetween(text, "<chain>", "</chain>");
        option.setChain(sub.equals("true"));
        sub = Conversion.substringBetween(text, "<active>", "</active>");
        option.setActive(sub.equals("true"));
        sub = Conversion.substringBetween(text, "<description>", "</description>").replace("%%%", "\n");
        option.setDescription(sub);
        sub = Conversion.substringBetween(text, "<notes>", "</notes>").replace("%%%", "\n");
        option.setNotes(sub);

        return option;
    }

}

// <editor-fold defaultstate="collapsed" desc="Conversion 0.7.5"> 
class C075 {

    public static String jumperToText(Jumper jumper) {
        String text = "";

        text += "<version>" + TrackerFrame.VERSION + "</version>\n";
        text += "<Jumper>" + jumper.getName() + "</Jumper>\n";
        text += "<Types>" + Arrays.toString(jumper.getTypesArray()) + "</types>";
        text += " \n\n";

        text = jumper.getJumpList().stream().map((j) -> jumpToText(j) + "\n").reduce(text, String::concat);

        return text.replace("â€™", "\'");

    }

    public static String jumpToText(Jump j) {
        String text = "";

        text += "<Jump name=\"" + j.getName() + "\" ";
        text += "supplement=\"" + (j.isSupplement() ? "true" : "false") + "\" ";
        text += "points=\"" + j.getPoints() + "\" ";
        text += "> \n";

        text = j.getOptionList().stream().map((o) -> optionToText(o)).reduce(text, String::concat);

        return text;

    }

    private static String optionToText(Option o) {
        String text = "";

        text += "<Option name=\"" + o.getName() + "\" ";
        text += "type=\"" + o.getType() + "\" ";
        text += "points=\"" + o.getPoints() + "\" ";
        text += "chain=\"" + (o.isChain() ? "true" : "false") + "\" ";
        text += "active=\"" + (o.isActive() ? "true" : "false") + "\" ";
        text += "description=\"" + o.getDescription().replace("\n", "%%%") + "\" ";
        text += "notes=\"" + o.getNotes().replace("\n", "%%%") + "\" ";
        text += "> \n";

        return text;

    }

    public static Jumper textToJumper(String text) {
        Jumper jumper;
        String[] lines = text.replace("â€™", "\'").split("\n\n");

        String[] jlines = lines[0].split("\n");

        String jumperName = jlines[0].split(" name=\"")[1].split("\" >")[0];
        jumper = new Jumper(jumperName);

        String[] types = jlines[1].replace("<Types [", "").replace("]>", "").split(", ");
        jumper.setTypes(types);

        for (int i = 1; i < lines.length; i++) {
            Jump j = textToJump(lines[i]);
            if (j != null) {
                jumper.addJump(j);
            }
        }

        return jumper;
    }

    public static Jump textToJump(String text) {
        Jump j = new Jump();
        String[] lines = text.split("\n");
        try {
            j.setName(lines[0].split(" name=\"")[1].split("\" supplement=\"")[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            j.setSupplement(lines[0].split("\" supplement=\"")[1].split("\" points=\"")[0].equals("true"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            j.setPoints(Integer.parseInt(lines[0].split("\" points=\"")[1].split("\" >")[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 1; i < lines.length; i++) {
            Option o = textToOption(lines[i]);
            if (o != null) {
                o.setJumpName(j.getName());
                j.addOption(o);
            }
        }

        return j;
    }

    private static Option textToOption(String text) {
        if (text.length() < 25 || !text.startsWith("<Option")) {
            return null;
        }
        Option o = new Option();

        try {
            o.setName(text.split(" name=\"")[1].split("\" ")[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            o.setPoints(Integer.parseInt(text.split("\" points=\"")[1].split("\" ")[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            o.setType(text.split("\" type=\"")[1].split("\" ")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            o.setChain(text.split("\" chain=\"")[1].split("\" ")[0].equals("true"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            o.setActive(text.split("\" active=\"")[1].split("\" ")[0].equals("true"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            o.setDescription(text.split("\" description=\"")[1].split("\" notes=\"")[0].replace("%%%", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            o.setNotes(text.split("\" notes=\"")[1].split("\" >")[0].replace("%%%", "\n"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }
}
//</editor-fold>

// <editor-fold defaultstate="collapsed" desc="Conversion 0.7.1"> 
class C071 {

    public static Jumper textToJumper(String text) {
        Jumper jumper;
        String[] lines = text.replace("â€™", "\'").split("\n\n");

        String[] jlines = lines[0].split("\n");

        String jumperName = jlines[0].split(" name=\"")[1].split("\" >")[0];
        jumper = new Jumper(jumperName);
        //jumper.setTypes(jumper.typeList);

        for (int i = 1; i < lines.length; i++) {
            Jump j = textToJump(lines[i]);
            if (j != null) {
                jumper.addJump(j);
            }
        }

        return jumper;
    }

    public static Jump textToJump(String text) {
        Jump j = new Jump();
        String[] lines = text.split("\n");

        j.setName(lines[0].split(" name=\"")[1].split("\" supplement=\"")[0]);
        j.setSupplement(lines[0].split("\" supplement=\"")[1].split("\" points=\"")[0].equals("true"));
        j.setPoints(Integer.parseInt(lines[0].split("\" points=\"")[1].split("\" >")[0]));

        for (int i = 1; i < lines.length; i++) {
            Option o = textToOption(lines[i]);
            if (o != null) {
                o.setJumpName(j.getName());
                j.addOption(o);
            }
        }

        return j;
    }

    private static Option textToOption(String text) {
        if (text.length() < 25 || !text.startsWith("<Option")) {
            return null;
        }
        Option o = new Option();
        o.setName(text.split(" name=\"")[1].split("\" type=")[0]);
        o.setType(text.split("\" type=\"")[1].split("\" points=\"")[0]);
        o.setPoints(Integer.parseInt(text.split("\" points=\"")[1].split("\" chain=\"")[0]));
        o.setChain(text.split("\" chain=\"")[1].split("\" description=\"")[0].equals("true"));
        o.setDescription(text.split("\" description=\"")[1].split("\" notes=\"")[0].replace("%%%", "\n"));
        o.setNotes(text.split("\" notes=\"")[1].split("\" >")[0].replace("%%%", "\n"));

        return o;
    }
}
// </editor-fold>
