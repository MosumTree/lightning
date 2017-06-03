package ui;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mosum.lightning.R;
/**
 * Created by mosum on 2017/2/8.
 * 侧边栏内容适配器
 */
public class
ContentAdapter extends BaseAdapter {

	private Context context;
	private List<ContentModel> list;

	public ContentAdapter(Context context, List<ContentModel> list) {
		super();
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (list != null) {
			return list.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold hold;
		if (convertView == null) {
			hold = new ViewHold();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.content_item, null);
			convertView.setTag(hold);
		} else {
			hold = (ViewHold) convertView.getTag();
		}

		hold.imageView = (ImageView) convertView
				.findViewById(R.id.item_imageview);
		hold.textView = (TextView) convertView.findViewById(R.id.item_textview);

		hold.imageView.setImageResource(list.get(position).getImageView());
		hold.textView.setText(list.get(position).getText());
		return convertView;
	}

	class ViewHold {
		public ImageView imageView;
		public TextView textView;
	}

}
