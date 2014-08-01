package cn.fanfan.found;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cn.fanfan.common.Config;
import cn.fanfan.detilques.Detilques;
import cn.fanfan.main.R;
import cn.fanfan.topic.imageload.ImageDownLoader;
import cn.fanfan.topic.imageload.ImageDownLoader.onImageLoaderListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FoundPager extends Fragment {
	private int totalItem;
	private boolean isFirstEnter;
	private int mFirstVisibleItem;
	private int mVisibleItemCount;
    private ListView listView;
    private List<Founditem> newlist;
	private FoundAdapter adapter;
	private int currentPage = 1;
	private ImageDownLoader imageDownLoader;
	private int total_row;
	private LinearLayout footerLinearLayout;
	private TextView footText;
	private String type;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(R.layout.foundlist, container, false);
		listView = (ListView)rootView.findViewById(R.id.fodlist);
		newlist = new ArrayList<Founditem>();
		imageDownLoader = new ImageDownLoader(getActivity());
		footerLinearLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.next_page_footer,null);
		footText = (TextView)footerLinearLayout.findViewById(R.id.footer_text);	
		type = "new";
		isFirstEnter = true;
		listView.addFooterView(footerLinearLayout);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(getActivity(), Detilques.class);
				startActivity(intent);
			}
		});
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE&&(mFirstVisibleItem + mVisibleItemCount == totalItem)) {
					if (total_row != 0) {
						System.out.println(currentPage+" @@@@@@");
						getInformation(String.valueOf(currentPage));
					}else {
						footText.setText("û�и���������");
						//footerLinearLayout.setVisibility(View.GONE);
						//listView.removeFooterView(footerLinearLayout);
					}
				}
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					showImage(mFirstVisibleItem, mVisibleItemCount);
				} else {
					cancleTask();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				mFirstVisibleItem = firstVisibleItem;
				mVisibleItemCount = visibleItemCount;
				totalItem = totalItemCount;
				// ���������Ϊ�״ν����������������
				if (isFirstEnter && visibleItemCount > 0) {
					showImage(mFirstVisibleItem, mVisibleItemCount);
					isFirstEnter = false;
				}
			}
		});
		getInformation("1");
		return rootView;
	}
	private void showImage(int firstVisibleItem, int visibleItemCount) {
		// ע��firstVisibleItem + visibleItemCount-1 = 20 1���а�����footview�����һ��ҪС�ģ�
		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount
				- 1; i++) {
			String mImageUrl = newlist.get(i).getAvatar_file();
			//System.out.println(mImageUrl);
			if (!mImageUrl.equals("") ) {
				mImageUrl = Config.getValue("userImageBaseUrl")+mImageUrl;
				//System.err.println(mImageUrl);
				final ImageView mImageView = (ImageView) listView
						.findViewWithTag(mImageUrl);
				imageDownLoader.getBitmap(mImageUrl, new onImageLoaderListener() {

					public void onImageLoader(Bitmap bitmap, String url) {
						//System.out.println(bitmap+")(");
						if (mImageView != null && bitmap != null) {
							mImageView.setImageBitmap(bitmap);
						}
					}
				});
			}else {
				continue;
			}
		}
	}
	private void getInformation(String page){
		RequestParams params = new RequestParams();
		String url = "http://w.hihwei.com/?/api/explore/";
		AsyncHttpClient client = new AsyncHttpClient();
		params.put("page", page);
		params.put("sort_type", type);
		client.get(url, params,new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				// TODO Auto-generated method stub
				String string = new String(arg2);
				try {
					JSONObject all = new JSONObject(string);
					JSONObject rsm = all.getJSONObject("rsm");
		            total_row = (rsm.getInt("total_rows"));
					JSONArray rows = rsm.getJSONArray("rows");
		            for (int i = 0; i < rows.length(); i++) {
						JSONObject jsonObject = rows.getJSONObject(i);
						Founditem founditem = new Founditem();
						founditem.setQuestion_id(jsonObject.getString("question_id"));
						founditem.setQuestion(jsonObject.getString("question_content"));
						founditem.setAnswer_count(jsonObject.getInt("answer_count"));
						founditem.setFocus_count(jsonObject.getInt("focus_count"));
						founditem.setView_count(jsonObject.getInt("view_count"));
						JSONObject object = jsonObject.getJSONObject("user_info");
						founditem.setName(object.getString("user_name"));
						founditem.setAvatar_file(object.getString("avatar_file"));
						
						newlist.add(founditem);
					} 			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (currentPage == 1) {
					adapter = new FoundAdapter(newlist, getActivity(),imageDownLoader);
					listView.setAdapter(adapter);
					currentPage++;
				} else {
					adapter.notifyDataSetChanged();
					currentPage ++;
				}
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				// TODO Auto-generated method stub
				
			}
		});
	} 
	public void cancleTask(){
		imageDownLoader.cacelTask();
	}
}