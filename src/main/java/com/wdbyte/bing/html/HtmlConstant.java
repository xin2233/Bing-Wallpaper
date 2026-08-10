package com.wdbyte.bing.html;

public class HtmlConstant {

    /**
     * 头部图片
     */
    public static class Head{
        public static final String HEAD_IMG_URL = "${head_img_url}";
        public static final String HEAD_IMG_DESC = "${head_img_desc}";
        public static final String HEAD_TITLE = "${head_title}";
    }

    /**
     * 图片列表
     */
    public static class ImgCard {
        public static final String VAR_IMG_CARD_LIST = "${img_card_list}";
        private static final String VAR_IMG_CARD_URL = "${img_card_url}";
        private static final String VAR_IMG_CARD_DATE = "${img_card_date}";
        private static final String IMG_CARD = ""
            + "<div class=\"w3-third \" style=\"position: relative;\">\n"
            +"  <img class=\"smallImg\" src=\"${img_card_url}&pid=hp&w=50\"  style=\"width:95%;\" />"
            + " <img class=\"bigImg\" src=\"${img_card_url}&pid=hp&w=384&h=216&rs=1&c=4\" style=\"width:95%\" onload=\"imgloading(this)\">\n"
            + " <p>${img_card_date} <a href=\"${img_card_url}\" target=\"_blank\">Download 4k</a> </p>\n"
            + "</div>";

        public static String getImgCard(String imgUrl,String date) {
            String result = IMG_CARD.replace(VAR_IMG_CARD_URL, imgUrl);
            return result.replace(VAR_IMG_CARD_DATE, date);
        }
    }
}
