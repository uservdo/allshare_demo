package dimitri.suls.allshare.gui.helpers;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.sec.android.allshare.Device;
import com.sec.android.allshare.Item;
import com.sec.android.allshare.media.AVPlayer;
import com.sec.android.allshare.media.ContentInfo;

import dimitri.suls.allshare.gui.listadapters.MediaItemAdapter;
import dimitri.suls.allshare.managers.device.DeviceCommand;
import dimitri.suls.allshare.managers.device.DeviceManager;
import dimitri.suls.allshare.media.MediaFinder;
import dimitri.suls.allshare.media.MediaFinder.MediaType;

public class MediaFrontendManager {
	private Context context;
	private ListView listViewMedia;
	private DeviceManager avPlayerDeviceManager;
	private MediaFinder mediaFinder;
	private MediaType mediaType;

	private class MediaListItemClickListener implements OnItemClickListener {
		private Item selectedSong;
		private ContentInfo contentInfo;

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			this.selectedSong = (Item) adapterView.getItemAtPosition(position);
			this.contentInfo = new ContentInfo.Builder().build();

			avPlayerDeviceManager.execute(new DeviceCommand() {
				@Override
				public void execute(Device selectedDevice) {
					AVPlayer avPlayer = (AVPlayer) selectedDevice;

					avPlayer.play(selectedSong, contentInfo);
				}
			});
		}
	}

	public MediaFrontendManager(Context context, ListView listViewMedia, DeviceManager avPlayerDeviceManager, MediaFinder mediaFinder,
			MediaType mediaType) {
		this.context = context;
		this.listViewMedia = listViewMedia;
		this.avPlayerDeviceManager = avPlayerDeviceManager;
		this.mediaFinder = mediaFinder;
		this.mediaType = mediaType;

		listViewMedia.setOnItemClickListener(new MediaListItemClickListener());

		refreshMediaList();
	}

	public void refreshMediaList() {
		List<Item> mediaItems = mediaFinder.findAllMediaItems(mediaType);
		MediaItemAdapter mediaItemAdapter = new MediaItemAdapter(context, mediaItems);

		listViewMedia.setAdapter(mediaItemAdapter);
		// TODO: Implement observers for mediaItems, just like with devices.
		// mediaFinder.setSelectedSong(null);
	}
}