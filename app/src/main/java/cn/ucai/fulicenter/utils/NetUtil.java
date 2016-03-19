package cn.ucai.fulicenter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.pingplusplus.model.Charge;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.FuLiCenterApplication;
import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.bean.BoutiqueBean;
import cn.ucai.fulicenter.bean.CartBean;
import cn.ucai.fulicenter.bean.CategoryChildBean;
import cn.ucai.fulicenter.bean.CategoryGroupBean;
import cn.ucai.fulicenter.bean.CollectBean;
import cn.ucai.fulicenter.bean.ColorBean;
import cn.ucai.fulicenter.bean.ContactBean;
import cn.ucai.fulicenter.bean.GoodDetailsBean;
import cn.ucai.fulicenter.bean.MessageBean;
import cn.ucai.fulicenter.bean.NewGoodBean;
import cn.ucai.fulicenter.bean.PropertyBean;
import cn.ucai.fulicenter.bean.UserBean;


public final class NetUtil {
    
    public static final String TAG="NetUtil";

	private String Server_root = "";

	/**
	 * 向app服务器注册
	 * 
	 * @param user
	 * @return
	 * @throws Exception
	 */
    public static boolean register(UserBean user) throws Exception {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_REGISTER));
		params.add(new BasicNameValuePair(I.User.USER_NAME, user.getUserName()));
		params.add(new BasicNameValuePair(I.User.NICK, user.getNick()));
		params.add(new BasicNameValuePair(I.User.PASSWORD, user.getPassword()));

		try {
			InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT,
					params, HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			MessageBean msg;
		    Log.i(TAG, "in="+in);
	        msg=om.readValue(in, MessageBean.class);
            Log.i(TAG, "msg="+msg.toString());
	        return msg.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
		return false;
	}

	/**
	 * 上传头像
	 * 
	 * @param activity
	 *            ：当前Activity
	 * @param userName
	 *            ：用户账号
	 * @param avatarType
	 *            :上传图片类型(user_avatar或group_icon)，也是图片保存在sd卡的最后 一个文件夹。
	 * @return
	 * @throws IOException
	 */
	public static boolean uploadAvatar(Activity activity, String avatarType,
			String userName) throws Exception {
	    Log.i(TAG, "NetUtil.uploadAvatar");
		HttpClient client = new DefaultHttpClient();
		String url= FuLiCenterApplication.SERVER_ROOT+"?"+I.KEY_REQUEST+"="+I.REQUEST_UPLOAD_AVATAR
				+"&"+I.User.USER_NAME+"="+userName
				+"&"+I.AVATAR_TYPE+"="+avatarType;
        Log.i(TAG, "NetUtil.uploadAvatar.url="+url);
		HttpPost post = new HttpPost(url);
		try {
	        Log.i(TAG, "NetUtil.uploadAvatar.userName="+userName);
    		File file = new File(ImageUtils.getAvatarPath(activity, avatarType),
    				userName + ".jpg");
            Log.i(TAG, "NetUtil.uploadAvatar.file="+file);
    		HttpEntity entity = HttpUtils.createInputStreamEntity(file);
            Log.i(TAG, "NetUtil.uploadAvatar.entity="+entity);
    		post.setEntity(entity);
    		HttpResponse response = client.execute(post);
            Log.i(TAG, "NetUtil.uploadAvatar.response="+response.getStatusLine().getStatusCode());
    		if (response.getStatusLine().getStatusCode() == 200) {
    			InputStream in = response.getEntity().getContent();
    			ObjectMapper om = new ObjectMapper();
    			MessageBean msg=om.readValue(in, MessageBean.class);
                Log.i(TAG, "NetUtil.uploadAvatar.msg="+msg.isSuccess());
    			return msg.isSuccess();
    		}
        }catch(FileNotFoundException e){
            Log.i(TAG, "NetUtil.uploadAvatar.FileNotFoundException="+e.getMessage().toString());
            e.printStackTrace();
	    }catch (Exception e) {
            Log.i(TAG, "NetUtil.uploadAvatar.Exception="+e.getMessage().toString());
            e.printStackTrace();
        }
        Log.i(TAG, "NetUtil.uploadAvatar.return false=");
		return false;
	}

	/**
	 * 从应用服务器下载头像
	 * @param file:头像保存的sd卡路径
	 * @param requestType：头像类型：user_avatar：个人，group_icon：群组头像
	 * @param avatar:服务端头像保存的文件名
	 */
	public static void downloadAvatar(File file, String requestType,String avatar) {
	    if(file==null){
	        return;
	    }
		if (!file.exists()) {
			try {
				ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
				params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_AVATAR));
				params.add(new BasicNameValuePair(I.User.AVATAR, avatar));
				InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT,params,HttpUtils.METHOD_GET);
				Bitmap bmpAvatar = BitmapFactory.decodeStream(in);
				OutputStream out = new FileOutputStream(file);
				if(null!=bmpAvatar){
				    bmpAvatar.compress(CompressFormat.JPEG, 100, out);
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				HttpUtils.closeClient();
			}
		}
	}

	/**
	 * 登陆应用服务器
	 * 
	 * @param userName
	 *            ：账号
	 * @param password
	 *            ：密码
	 * @return true:登陆成功
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws IllegalStateException
	 */
	public static UserBean login(String userName, String password)
			throws IllegalStateException, ClientProtocolException, IOException {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_LOGIN));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.User.PASSWORD, password));
		InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params,
				HttpUtils.METHOD_GET);
		Log.e("main","server_root="+ FuLiCenterApplication.SERVER_ROOT);
		ObjectMapper om = new ObjectMapper();
		UserBean user = om.readValue(in, UserBean.class);
		return user;
	}


	/**
	 * 向服务器添加联系人，并返回联系人完整信息->ContactBean类型
	 * 
	 * @param userName
	 *            ：当前用户账号
	 * @param name
	 *            ：联系人账号
	 * @return ContactBean
	 */
	public static ContactBean addContact(String userName, String name) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_ADD_CONTACT));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.Contact.NAME, name));
		try {
			InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			ContactBean contact = om.readValue(in, ContactBean.class);
			Log.e("main","NetUtil.addContact.contact="+contact);
			return contact;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return null;
	}

	/**
	 * 删除联系人
	 * 
	 * @param myuid
	 *            :当前用户的id
	 * @param cuid
	 *            ：联系人的id
	 */
	public static boolean deleteContact(int myuid, int cuid) {
		Log.e(TAG,"NetUtil.deleteContact.myuid="+myuid+",cuid="+cuid);

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,
				I.REQUEST_DELETE_CONTACT));
		params.add(new BasicNameValuePair(I.Contact.MYUID, myuid + ""));
		params.add(new BasicNameValuePair(I.Contact.CUID, cuid + ""));
		try {
			InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			Boolean isSuccess = om.readValue(in, Boolean.class);
			Log.i("main", "删除联系人成功:" + isSuccess);
			return isSuccess;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}

	public static UserBean findUserByUserName(String userName) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_USER));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		try {
			InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params,
					HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
			UserBean user = om.readValue(in, UserBean.class);
			return user;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return null;
	}
	
	/**
	 * 下载联系人->HashMap<Integer,ContactBean>
	 * @param instance
	 * @param userName
	 * @param pageId
	 * @param pageSize
	 */
	public static boolean downloadContacts(FuLiCenterApplication instance, String userName, int pageId, int pageSize){
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_CONTACTS));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
		try {
			InputStream in=HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params,HttpUtils.METHOD_GET);
			ObjectMapper om = new ObjectMapper();
//			Log.e("main","in="+in.toString());
			ContactBean[] contacts = om.readValue(in, ContactBean[].class);
			HashMap<Integer, ContactBean> map = new HashMap<Integer, ContactBean>();
			for (ContactBean contact : contacts) {
				map.put(contact.getCuid(), contact);
			}
			HashMap<Integer,ContactBean> contactMap=instance.getContacts();
			contactMap.putAll(map);
			return true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}
	
	/**
	 * 下载联系人集合：ArrayList<UserBean>
	 * @param userName
     * @[param pageId
     * @param pageSize
	 */
	public static boolean downloadContactList(String userName,int pageId,int pageSize) {

		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST,I.REQUEST_DOWNLOAD_CONTACT_LIST));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
		params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
		try {
			InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params,HttpUtils.METHOD_GET);
//			if(in!=null){
//				byte b[] = new byte[1000];
//				int c = in.read(b);
//				String ss = new String(b,0,c);
//				Log.e("main","ss="+ss);
//
//			}
			ObjectMapper om = new ObjectMapper();
			UserBean[] userArray = om.readValue(in, UserBean[].class);
			if(userArray==null){
			    Log.i("main","download contact list false");
				return false;
			}
			//将数组转换为集合
			ArrayList<UserBean> userList=Utils.array2List(userArray);
			//获取已添加的所有联系人的集合
			ArrayList<UserBean> contactList = FuLiCenterApplication.getInstance().getContactList();
			//将新下载的数据添加到原联系人集合中
			contactList.addAll(userList);
			return true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			HttpUtils.closeClient();
		}
		return false;
	}
	/**
	 * 获得服务器状态的请求
	 */
	public static MessageBean getServerStatus() {
		MessageBean msg = new MessageBean(false, "连接失败");
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_SERVERSTATUS));
		try {
			InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			msg = om.readValue(in, MessageBean.class);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			HttpUtils.closeClient();
		}
		return msg;
	}
	/**
	 * 将注册账号为userName的用户从应用服务器删除,同时将上传的头像从服务器删除
	 * @param userName
	 */
    public static MessageBean unRegister(String userName) {
//		if(isServerConnectioned()){
//			return null;
//		}
		Log.e(TAG,"NetUtil.unRegister.userName="+userName);
        MessageBean msg = new MessageBean(false, "取消注册失败");
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_UNREGISTER));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        try {
            InputStream in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            msg = om.readValue(in, MessageBean.class);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            HttpUtils.closeClient();
        }
        return msg;
    }

    /**
     * 下载新品首页或精选二级页面的商品信息
     * @param catId:新品或精选的下载请求
     * @param pageId
     * @param pageSize
     * @return
     * @throws Exception
     */
    public static ArrayList<NewGoodBean> findNewandBoutiqueGoods(int catId, int pageId, int pageSize) throws Exception {
		Log.i(TAG,"findNewandBoutiqueGoods...catId="+catId+",pageId="+pageId+",pageSize="+pageSize);
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_NEW_BOUTIQUE_GOODS));
        params.add(new BasicNameValuePair(I.NewAndBoutiqueGood.CAT_ID, catId+""));
        params.add(new BasicNameValuePair(I.PAGE_ID, pageId+""));
        params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize+""));
        InputStream in;
		Log.i(TAG,"findNewandBoutiqueGoods....SERVER_ROOT="+FuLiCenterApplication.SERVER_ROOT);
        in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
        ObjectMapper om=new ObjectMapper();
        NewGoodBean[] goodArray = om.readValue(in,NewGoodBean[].class);
		Log.i(TAG,"findNewandBoutiqueGoods....goodArray="+goodArray);
        ArrayList<NewGoodBean> goods = Utils.array2List(goodArray);
		Log.i(TAG,"findNewandBoutiqueGoods....goods="+goods);
        Log.i(TAG,"新品加载完成");
        HttpUtils.closeClient();
        return goods;
    }

    /**
     * 下载商品详情
     * @param goodsId
     * @return
     */
    public static GoodDetailsBean findGoodDetails(int goodsId) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_GOOD_DETAILS));
        params.add(new BasicNameValuePair(I.CategoryGood.GOODS_ID, ""+goodsId));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            GoodDetailsBean goodDetails = om.readValue(in, GoodDetailsBean.class);
            return goodDetails;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return null;
    }

    /**
     * 查询指定商品是否已被收藏
     * @param goodsId
     */
    public static boolean isCollect(String userName,int goodsId) {
        
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_IS_COLLECT));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        params.add(new BasicNameValuePair(I.Collect.GOODS_ID, goodsId+""));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            MessageBean msg = om.readValue(in, MessageBean.class);
            return msg.isSuccess();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return false;
    }

    /**
     * 添加收藏
     * @param userName:用户账号
     * @param good：商品id
     */
    public static MessageBean addCollect(String userName, GoodDetailsBean good) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_ADD_COLLECT));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        params.add(new BasicNameValuePair(I.Collect.GOODS_ID, ""+good.getGoodsId()));
        params.add(new BasicNameValuePair(I.Collect.GOODS_NAME, good.getGoodsName()));
        params.add(new BasicNameValuePair(I.Collect.GOODS_ENGLISH_NAME, good.getGoodsEnglishName()));
        params.add(new BasicNameValuePair(I.Collect.GOODS_THUMB, good.getGoodsThumb()));
        params.add(new BasicNameValuePair(I.Collect.GOODS_IMG, good.getGoodsImg()));
        params.add(new BasicNameValuePair(I.Collect.ADD_TIME, good.getAddTime()+""));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            MessageBean msg = om.readValue(in, MessageBean.class);
            return msg;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return null;
    }
    /**
     * 取消收藏
     * @param userName:用户账号
     * @param goodsId：商品id
     */
    public static MessageBean deleteCollect(String userName, int goodsId) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_DELETE_COLLECT));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        params.add(new BasicNameValuePair(I.Collect.GOODS_ID, ""+goodsId));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            MessageBean msg = om.readValue(in, MessageBean.class);
            return msg;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return null;
    }

    /**
     * 下载精选首页数据
     */
    public static ArrayList<BoutiqueBean> findBoutiqueList() {
        ArrayList<BoutiqueBean> list=null;
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_BOUTIQUES));
        
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            BoutiqueBean[] ary = om.readValue(in, BoutiqueBean[].class);
            list=Utils.array2List(ary);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return list;
    }

    /**
     * 下载分类中的大类数据
     * @return
     */
    public static ArrayList<CategoryGroupBean> findCategoryGroup() {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_CATEGORY_GROUP));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            CategoryGroupBean[] groups = om.readValue(in, CategoryGroupBean[].class);
            ArrayList<CategoryGroupBean> groupList = Utils.array2List(groups);
            return groupList;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return null;
    }

    public static ArrayList<CategoryChildBean> findCategoryChild(int parentId, int pageId, int pageSize) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_CATEGORY_CHILDREN));
        params.add(new BasicNameValuePair(I.CategoryChild.PARENT_ID, parentId+""));
        params.add(new BasicNameValuePair(I.PAGE_ID, ""+pageId));
        params.add(new BasicNameValuePair(I.PAGE_SIZE, ""+pageSize));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            CategoryChildBean[] children = om.readValue(in, CategoryChildBean[].class);
            ArrayList<CategoryChildBean> childList =Utils.array2List(children);
            return childList;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return null;
    }
    
    
    /**
     * 下载商品详情的集合
     * 分类二级页面中颜色按钮下拉列表项被选择时，
     * 分类二级页面:下载一组商品信息
     * @param pageId
     * @param pageSize
     * @return
     * @throws Exception
     */
    public static ArrayList<NewGoodBean> findGoodsDetails(Context context, int catId, int pageId, int pageSize) throws Exception {
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_GOODS_DETAILS));
        params.add(new BasicNameValuePair(I.NewAndBoutiqueGood.CAT_ID, catId+""));
        params.add(new BasicNameValuePair(I.PAGE_ID, pageId+""));
        params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize+""));
        InputStream in;
        in = HttpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
        ObjectMapper om=new ObjectMapper();
        GoodDetailsBean[] goodArray = om.readValue(in,GoodDetailsBean[].class);
        ArrayList<GoodDetailsBean> goods = Utils.array2List(goodArray);
        
        //将GoodDetailsBean类型的集合转换为NewGoodBean类型的集合
        ArrayList<NewGoodBean> goodList=new ArrayList<NewGoodBean>();
        for(int i=0;i<goods.size();i++){
            GoodDetailsBean goodDetails = goods.get(i);
            NewGoodBean good=new NewGoodBean();
            good.setAddTime(goodDetails.getAddTime());
            good.setCatId(goodDetails.getCatId());
            PropertyBean p = goodDetails.getProperties()[0];
            good.setColorCode(p.getColorCode());
            good.setColorId(p.getColorId());
            good.setColorName(p.getColorName());
            good.setColorUrl(p.getColorUrl());
            good.setCurrencyPrice(goodDetails.getCurrencyPrice());
            good.setGoodsBrief(goodDetails.getGoodsBrief());
            good.setGoodsEnglishName(goodDetails.getGoodsEnglishName());
            good.setGoodsId(goodDetails.getGoodsId());
            good.setGoodsImg(goodDetails.getGoodsImg());
            good.setGoodsName(goodDetails.getGoodsName());
            good.setGoodsThumb(goodDetails.getGoodsThumb());
            good.setId(goodDetails.getId());
            good.setPromote(goodDetails.isPromote());
            good.setPromotePrice(goodDetails.getPromotePrice());
            good.setRankPrice(goodDetails.getRankPrice());
            good.setShopPrice(goodDetails.getShopPrice());
            goodList.add(good);
        }
        HttpUtils.closeClient();
        //向CatgeoryChildActivity发送
        context.sendBroadcast(new Intent("good_details_update")
            .putExtra("goods_details", goods));
        return goodList;
    }

    /**
     * 下载指定catId的颜色列表
     * @param catId
     * @return
     */
    public static ArrayList<ColorBean> findColors(int catId) {
        ArrayList<ColorBean> colorList=null;
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_COLOR_LIST));
        params.add(new BasicNameValuePair(I.Color.CAT_ID, ""+catId));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            ColorBean[] array = om.readValue(in, ColorBean[].class);
            colorList=Utils.array2List(array);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return colorList;
    }

    /**
     * 将修改的昵称长传至服务器
     * @param nick
     */
    public static boolean uploadNick(String nick,String userName) {
        
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_UPLOAD_NICK));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        params.add(new BasicNameValuePair(I.User.NICK, nick));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            MessageBean msg = om.readValue(in, MessageBean.class);
            return msg.isSuccess();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return false;
    }

    /**
     * 下载收藏商品
     * @param userName：当前用户账号
     * @return
     */
    public static ArrayList<CollectBean> findCollects(String userName, int pageId, int pageSize) {
        ArrayList<CollectBean> collectList=null;
        
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_COLLECTS));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        params.add(new BasicNameValuePair(I.PAGE_ID, pageId+""));
        params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize+""));
        try {
            InputStream in= httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            CollectBean[] array = om.readValue(in, CollectBean[].class);
            collectList=Utils.array2List(array);
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return collectList;
    }

	/**
	 * 下载收藏商品数量
	 * @param userName：当前用户账号
	 * @return
	 */
	public static int findCollectCount(String userName) {
		ArrayList<CollectBean> collectList=null;

		HttpUtils httpUtils=new HttpUtils();
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_COLLECT_COUNT));
		params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
		try {
			InputStream in= httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			MessageBean msg = om.readValue(in, MessageBean.class);
			if(msg.isSuccess()){
				return Integer.parseInt(msg.getMsg());
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			httpUtils.closeClient();
		}
		return 0;
	}

    public static ArrayList<CartBean> findcartList(String userName, int pageId, int pageSize) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_CARTS));
        params.add(new BasicNameValuePair(I.User.USER_NAME, userName));
        params.add(new BasicNameValuePair(I.PAGE_ID, pageId + ""));
        params.add(new BasicNameValuePair(I.PAGE_SIZE, pageSize + ""));
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            CartBean[] array = om.readValue(in, CartBean[].class);
            ArrayList<CartBean> cartList = Utils.array2List(array);
            return cartList;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return null;
    }

    public static int addCart(CartBean cart) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_ADD_CART));
        params.add(new BasicNameValuePair(I.Cart.COUNT, cart.getCount()+""));
        params.add(new BasicNameValuePair(I.Cart.GOODS_ID, cart.getGoodsId()+""));
        params.add(new BasicNameValuePair(I.Cart.IS_CHECKED, cart.isChecked()+""));
        params.add(new BasicNameValuePair(I.Cart.USER_NAME, cart.getUserName()));
        try {
			Log.e(TAG,"addCart,cart="+cart);
            Log.e(TAG,"addCart,params="+params.toString());
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            MessageBean msg = om.readValue(in, MessageBean.class);
            Log.e(TAG,"addCart,msg="+msg);
            if(msg.isSuccess()){
                return Integer.parseInt(msg.getMsg());
            }
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return 0;
    }

    /**
     * 修改服务端指定用户的购物车中的商品件数
     * @param cartId
     * @param count
     * @return
     */
    public static boolean updateCart(int cartId, int count, boolean isChecked) {
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_UPDATE_CART));
        params.add(new BasicNameValuePair(I.Cart.COUNT, count+""));
        params.add(new BasicNameValuePair(I.Cart.ID, cartId+""));
        params.add(new BasicNameValuePair(I.Cart.IS_CHECKED, isChecked+""));
        try {
            Log.e(TAG,"addCart,cartId="+cartId+",count="+count+",isChecked="+isChecked);
            Log.e(TAG,"addCart,params="+params.toString());
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
            MessageBean msg= om.readValue(in, MessageBean.class);
            Log.e(TAG,"addCart,msg="+msg.toString());
            return msg.isSuccess();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return false;
    }
	/**
	 * 删除服务端指定用户的购物车中的商品件数
	 * @param cartId
	 * @return
	 */
	public static boolean deleteCart(int cartId) {
		HttpUtils httpUtils=new HttpUtils();
		ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_DELETE_CART));
		params.add(new BasicNameValuePair(I.Cart.ID, cartId+""));
		try {
			Log.e(TAG,"addCart,cartId="+cartId);
			Log.e(TAG,"addCart,params="+params.toString());
			InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
			ObjectMapper om=new ObjectMapper();
			MessageBean msg= om.readValue(in, MessageBean.class);
			Log.e(TAG,"addCart,msg="+msg.toString());
			return msg.isSuccess();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			httpUtils.closeClient();
		}
		return false;
	}

    public static HashMap<String, Object> findCharege(){
        HashMap chargeMap =null;
        HttpUtils httpUtils=new HttpUtils();
        ArrayList<BasicNameValuePair> params=new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(I.KEY_REQUEST, I.REQUEST_FIND_CHARGE));
        Charge charge = null;
        try {
            InputStream in = httpUtils.getInputStream(FuLiCenterApplication.SERVER_ROOT, params, HttpUtils.METHOD_GET);
            ObjectMapper om=new ObjectMapper();
//          以下的解析方式不成立
            chargeMap = om.readValue(in, HashMap.class);
            Log.i("main",chargeMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            httpUtils.closeClient();
        }
        return chargeMap;
    }
}
