package cn.ucai.fulicenter;

/**
 * 个人资料上传头像bug：服务端没有收到上传的头像。
 * ChatActivity聊天页面没有显示登陆用户的头像
 * @author chen
 *
 */
public interface I {

//    String SERVER_ROOT="http://10.0.2.2:8080/FuLiCenterServer/Server";
    String SERVER_ROOT="http://139.196.185.33:8080/FuLiCenterServer/Server";
    //String SERVER_ROOT="http://139.196.185.33:8080/SuperQQ3Server/Server";

    /**页号*/
    public static final String PAGE_ID="page_id";
    
    /**每页加载的数据*/
    public static final String PAGE_SIZE="page_size";
    /** 上传图片的类型：user_avatar或group_icon*/
    public static final String AVATAR_TYPE="avatarType";
    public static final String FILE_NAME="file_name";
    
    public static final int PAGE_ID_DEFAULT = 0;
    public static final int PAGE_SIZE_DEFAULT = 10;

    public static final int REQUEST_CODE_LOGIN = 1;
    public static final int ACTIVITY_REGISTER_REQUEST_CODE = 2;

    /** 下拉刷新*/
    public static final int ACTION_DOWNLOAD=0;
    /** 第一次下载*/
    public static final int ACTION_PULL_DOWN=1;
    /** 上拉刷新*/
    public static final int ACTION_PULL_UP=2;

    /** 每行显示的数量columNum*/
    public static final int COLUM_NUM = 2;


    /** 表示列表项布局的两种类型*/
    public static final int TYPE_ITEM=0;
    public static final int TYPE_FOOTER=1;

    /** BeeColud APP ID */
    public static final String BEE_COLUD_APP_ID = "3539b590-4859-4128-87a3-5fb8b86b94f6";
    /** BeeColud APP Secret*/
    public static final String BEE_COLUD_APP_SECRET = "c75c74e1-105e-437c-9be9-84c4ddee4d5f";
    /** BeeColud APP Test Secret*/
    public static final String BEE_COLUD_APP_SECRET_TEST = "06eb1210-0eeb-41df-99e3-1ffb9eb87b99";
    /** weixin APP ID */
    public static final String WEIXIN_APP_ID = "wxf1aa465362b4c8f1";
    // 如果使用PayPal需要在支付之前设置client id和应用secret
    public static final String PAYPAL_CLIENT_ID = "AVT1Ch18aTIlUJIeeCxvC7ZKQYHczGwiWm8jOwhrREc4a5FnbdwlqEB4evlHPXXUA67RAAZqZM0H8TCR";
    public static final String PAYPAL_SECRET = "EL-fkjkEUyxrwZAmrfn46awFXlX-h2nRkyCVhhpeVdlSRuhPJKXx3ZvUTTJqPQuAeomXA8PZ2MkX24vF";

    //商户名称
    public static final String MERCHANT_NAME = "福利社";

    //货币单位
    public static final String CURRENCY_TYPE_CNY = "CNY";
    public static final String CURRENCY_TYPE_USD = "USD";
    
    class Cart{
        public static final String ID="id";
        public static final String GOODS_ID="goods_id";
        public static final String GOODS_THUMB="goodsThumb";
        public static final String USER_NAME="userName";
        /**购物车中该商品的件数*/
        public static final String COUNT="count";
        /**商品是否已被选中*/
        public static final String IS_CHECKED="isChecked";
    }

    
    class Collect{
        /** 商品id*/
        public static final String ID="id";
        
        public static final String GOODS_ID="goods_id";
        
        public static final String USER_NAME="userName";
        
        /** 商品的中文名称*/
        public static final String GOODS_NAME="goodsName";
        /** 商品的英文名称*/
        public static final String GOODS_ENGLISH_NAME="goodsEnglishName";
        public static final String GOODS_THUMB="goodsThumb";
        public static final String GOODS_IMG="goodsImg";
        public static final String ADD_TIME="addTime";
    }
    
    class Boutique{
        public static final String TABLE_NAME="tb_boutique";
        public static final String ID="id";
        public static final String CAT_ID="catId";
        public static final String TITLE="title";
        public static final String DESCRIPTION="description";
        public static final String NAME="name";
        public static final String IMAGE_URL="imageurl";
    }
    
    class NewAndBoutiqueGood{
        public static final String CAT_ID="cat_id";
        /** 颜色id*/
        public static final String COLOR_ID="color_id";
        /** 颜色名*/
        public static final String COLOR_NAME="color_name";
        /** 颜色代码*/
        public static final String COLOR_CODE="color_code";
        /** 导购链接*/
        public static final String COLOR_URL="color_url";
    }
    
    class CategoryGroup{
        public static final String ID="id";
        public static final String NAME="name";
        public static final String IMAGE_URL="imageurl";
    }
    
    class CategoryChild extends CategoryGroup{
        public static final String PARENT_ID="parent_id";
        public static final String CAT_ID="catId";
    }
    
    class CategoryGood{
        public static final String TABLE_NAME="tb_category_good";
        public static final String ID="id";
        /** 商品id*/
        public static final String GOODS_ID="goods_id";
        /** 所属类别的id*/
        public static final String CAT_ID="cat_id";
        /** 商品的中文名称*/
        public static final String GOODS_NAME="goods_name";
        /** 商品的英文名称*/
        public static final String GOODS_ENGLISH_NAME="goods_english_name";
        /** 商品简介*/
        public static final String GOODS_BRIEF="goods_brief";
        /** 商品原始价格*/
        public static final String SHOP_PRICE="shop_price";
        /** 商品的RMB价格 */
        public static final String CURRENT_PRICE="currency_price";
        /** 商品折扣价格 */
        public static final String PROMOTE_PRICE="promote_price";
        /** 人民币折扣价格*/
        public static final String RANK_PRICE="rank_price";
        /**是否折扣*/
        public static final String IS_PROMOTE="is_promote";
        /** 商品缩略图地址*/
        public static final String GOODS_THUMB="goods_thumb";
        /** 商品图片地址*/
        public static final String GOODS_IMG="goods_img";
        /** 分享地址*/
        public static final String ADD_TIME="add_time";
        /** 分享地址*/
        public static final String SHARE_URL="share_url";
    }
    
    class Property{
        public static final String ID="id";
        public static final String goodsId="goods_id";
        public static final String COLOR_ID="colorid";
        public static final String COLOR_NAME="colorname";
        public static final String COLOR_CODE="colorcode";
        public static final String COLOR_IMG="colorimg";
        public static final String COLOR_URL="colorurl";
    }
    
    class Album{
        public static final String TABLE_NAME="tb_album";
        public static final String ID="id";
        public static final String PID="pid";
        public static final String IMG_ID="img_id";
        public static final String IMG_URL="img_url";
        public static final String THUMB_URL="thumb_url";
        public static final String IMG_DESC="img_desc";
    }

    class Color{
        public static final String TABLE_NAME="tb_color";
        public static final String COLOR_ID="colorid";
        public static final String CAT_ID="cat_id";
        public static final String COLOR_NAME="colorname";
        public static final String COLOR_CODE="colorcode";
        public static final String COLOR_IMG="colorimg";
    }
    
    
    /**User*/
    public static class User{
        public static final String ID="id";
        public static final String UID="uid";
        public static final String USER_NAME="userName";
        
        public static final String NICK="nick";
        public static final String AVATAR="avatar";
        public static final String HEADER="header";
        public static final String PASSWORD="password";
        public static final String UN_READ_MSG_COUNT="unreadMsgCount";
        public static final String GROUPS="groups";
        
    }
    
    public static class Contact extends User{
        public static final String TABLE_NAME="tb_contact";
        public static final String NAME="name";
        public static final String MYUID="myuid";
        public static final String CUID="cuid";
        public static final String IS_GET_MY_LOCATION="isGetMyLocation";
        public static final String IS_SHOW_MY_LOCATION="isShowMyLocation";
    }
    
    public enum ActionType {
        ACTION_DOWNLOAD, ACTION_PULL_DOWN, ACTION_SCROLL
    }    

    public final int NEW_GOOD=0;
    public final int CATEGORY_GOOD=1;
    public final int CAT_ID=0;
    
    /**
     * 商品排序方式
     */
    public final int SORT_BY_PRICE_ASC=1;
    public final int SORT_BY_PRICE_DESC=2;
    public final int SORT_BY_ADDTIME_ASC=3;
    public final int SORT_BY_ADDTIME_DESC=4;
	
	
    String KEY_REQUEST="request";
    /**
     * 客户端发送的获得服务器状态的请求
     */
    String REQUEST_SERVERSTATUS="server_status";
    /**
     * 客户端发送的注册请求
     */
    String REQUEST_REGISTER="register";
    /**
     * 发送取消注册的请求
     */
    String REQUEST_UNREGISTER="unregister";
    /**上传头像*/
    String REQUEST_UPLOAD_AVATAR="upload_avatar";
    /**登陆*/
    String REQUEST_LOGIN="login";
    
    /**下载头像*/
    String REQUEST_DOWNLOAD_AVATAR = "download_avatar";

    String REQUEST_DOWNLOAD_CONTACTS = "download_contacts";

    String REQUEST_DOWNLOAD_CONTACT_LIST = "download_contactlist";
    
    String REQUEST_DELETE_CONTACT="delete_contact";
    /** 添加联系人 */
    String REQUEST_ADD_CONTACT="add_contact";
    
    /** 查找联系人 */
    String REQUEST_FIND_USER="find_user";
    String REQUEST_FIND_CHARGE = "find_charge";
    
    /** 从服务端查询精选首页的数据*/
    String REQUEST_FIND_BOUTIQUES="find_boutiques";
    /** 从服务端查询新品或精选的商品*/
    String REQUEST_FIND_NEW_BOUTIQUE_GOODS="find_new_boutique_goods";

    /** 从服务端下载tb_category_parent表的数据*/
    String REQUEST_FIND_CATEGORY_GROUP="find_category_group";
    
    /** 从服务端下载tb_category_child表的数据*/
    String REQUEST_FIND_CATEGORY_CHILDREN="find_category_children";
    
    /** 从服务端下载tb_category_good表的数据*/
    String REQUEST_FIND_GOOD_DETAILS="find_good_details";

    /** 从服务端下载一组商品详情的数据*/
    String REQUEST_FIND_GOODS_DETAILS="find_goods_details";

    /** 下载指定小类别的颜色列表*/
    String REQUEST_FIND_COLOR_LIST="find_color_list";
    
    /** 查询是否已收藏*/
    String REQUEST_IS_COLLECT="is_collect";
    /** 添加收藏*/
    String REQUEST_ADD_COLLECT="add_collect";
    /** 删除收藏*/
    String REQUEST_DELETE_COLLECT="delete_collect";
    /** 下载收藏的商品信息*/
    String REQUEST_FIND_COLLECTS="find_collects";
    /** 下载收藏的商品数量信息*/
    String REQUEST_FIND_COLLECT_COUNT="find_collect_count";
    
    String REQUEST_ADD_CART="add_cart";
    
    String REQUEST_FIND_CARTS="find_carts";

    String REQUEST_DELETE_CART="delete_cart";
    
    String REQUEST_UPDATE_CART="update_cart";
    
    /**下载新品首页商品图片*/
    String REQUEST_DOWNLOAD_NEW_GOOD = "download_new_good";
    
    /**下载商品属性颜色的图片*/
    String REQUEST_DOWNLOAD_COLOR_IMG = "download_color_img";
    
    /** 下载商品相册图像的URL*/
    String DOWNLOAD_AVATAR_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_AVATAR+"&avatar=";
    
    /** 下载商品相册图像的请求*/
    String REQUEST_DOWNLOAD_ALBUM_IMG="download_album_img_url";
    /** 下载商品相册图像的接口*/
    String DOWNLOAD_ALBUM_IMG_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_ALBUM_IMG+"&img_url=";
    
    /** 下载精选首页图像的请求*/
    String REQUEST_DOWNLOAD_BOUTIQUE_IMG="download_boutique_img";
    /** 下载精选首页图像的接口*/
    String DOWNLOAD_BOUTIQUE_IMG_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_BOUTIQUE_IMG+"&"+Boutique.IMAGE_URL+"=";
    
    /** 下载分类商品大类图像的请求*/
    String REQUEST_DOWNLOAD_CATEGORY_GROUP_IMAGE="download_category_group_image";
    /** 下载分类商品大类图像的接口*/
    String DOWNLOAD_DOWNLOAD_CATEGORY_GROUP_IMAGE_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_CATEGORY_GROUP_IMAGE
        +"&"+D.CategoryGroup.IMAGE_URL+"=";

    /** 下载收藏商品图像的请求*/
    String REQUEST_DOWNLOAD_GOODS_THUMB="download_goods_thumb";
    /** 下载收藏商品图像的接口*/
    String DOWNLOAD_GOODS_THUMB_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_GOODS_THUMB
        +"&"+Collect.GOODS_THUMB+"=";
    
    /** 下载分类商品小类图像的请求*/
    String REQUEST_DOWNLOAD_CATEGORY_CHILD_IMAGE="download_category_child_image";
    /** 下载分类商品小类图像的接口*/
    String DOWNLOAD_DOWNLOAD_CATEGORY_CHILD_IMAGE_URL=SERVER_ROOT+
        "?request="+REQUEST_DOWNLOAD_CATEGORY_GROUP_IMAGE
        +"&"+D.CategoryChild.IMAGE_URL+"=";
    
    String REQUEST_UPLOAD_NICK="upload_nick";
    //壹收款支付请求
    String REQUEST_PAY="pay";
    /**壹收款服务端支付URL*/
    String PAY_URL=I.SERVER_ROOT+"?request="+I.REQUEST_PAY;
}
