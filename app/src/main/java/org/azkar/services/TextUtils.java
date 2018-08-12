package org.azkar.services;

import android.support.annotation.NonNull;

public class TextUtils {

    enum FontSize {
        NORMAL, LARGE
    }

    private FontSize fontSize = FontSize.NORMAL;

    public void setFontLarge() {
        fontSize = FontSize.LARGE;
    }

    public void setFontNormal() {
        fontSize = FontSize.NORMAL;
    }

    private static String addVowels(String arabic) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arabic.length(); i++) {
            result.append(arabic.charAt(i));
            result.append("[\u064B-\u065F]*"); //vowels
            //unicode vowels letters from url: http://unicode.org/charts/PDF/U0600.pdf
        }
        return result.toString();
    }

    @NonNull
    public static String highlight(String bodyString, String highlightWords) {
        final String spanStart = "<font color=\"red\">";
        final String spanEnd = "</font>";

        for (String word : highlightWords.split(" ")) {
            word = word.trim();
            if (word.length() > 0) {
                String processedWord = addVowels(word);
                bodyString = bodyString.replaceAll("(\\b" + processedWord + "\\b)", spanStart + "$1" + spanEnd);
            }
        }
        return bodyString;
    }

    @NonNull
    public String decorate(@NonNull String searchWords, @NonNull String title, @NonNull String content) {

        content = content.trim();
        content = removeTrailingHashes(content);
        content = content.trim();

        //FIXME: Use StringBuilder
        //FIXME: Use HTML templates
        String fontSizeStyle = (fontSize == FontSize.LARGE)? " font-size: 150%; " : " font-size: 110%; ";
        String font = "@font-face {font-family: 'custom';src: url('fonts/Amiri-Regular.ttf');} ";
        String style = "<style>" + font + " body {font-family: 'custom'; direction: rtl; "
                + "text-align:justify; align-content: right;  text-align=right;"
                + fontSizeStyle + "}</style>";
        String head = "<head>" + style + "</head>";
        final String htmlPagePrefix = "<html>" + head + "<body>";
        final String htmlPagePostfix = "</body></html>";

        content = content.replaceAll("##", "<br><hr>");
        content = content.replaceAll("\n", "<br>");
        if(searchWords.trim().length() > 0) { //highlight search text
            content = TextUtils.highlight(content, searchWords);
        }

        //Add title
        content = "<font color=\"blue\">" + TextUtils.removeTrailingDot(title) + "</font><hr>" + content;
        return htmlPagePrefix + content + htmlPagePostfix;
    }

    @NonNull
    public static String removeTrailingHashes(@NonNull String content) {
        if(content.charAt(content.length()-1) == '#') {
            return content.substring(0, content.length()-2);
        }
        return content;
    }

    @NonNull
    public static String removeTrailingDot(@NonNull String content) {
        if(content.charAt(content.length()-1) == '.') {
            return content.substring(0, content.length()-1);
        }
        return content;
    }


}
