package dimitri.suls.allshare.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sec.android.allshare.Device;
import com.sec.android.allshare.Device.DeviceType;
import com.sec.android.allshare.control.TVController;
import com.sec.android.allshare.control.TVController.RemoteKey;
import com.sec.android.allshare.media.AVPlayer;

import dimitri.suls.allshare.R;
import dimitri.suls.allshare.device.frontend.manager.DeviceFrontendManager;
import dimitri.suls.allshare.device.model.manager.DeviceCommand;
import dimitri.suls.allshare.device.model.manager.DeviceManager;
import dimitri.suls.allshare.helpers.frontend.TabManager;
import dimitri.suls.allshare.media.frontend.managers.MediaFrontendManager;
import dimitri.suls.allshare.media.model.managers.MediaManager;
import dimitri.suls.allshare.media.model.managers.MediaManager.MediaType;
import dimitri.suls.allshare.serviceprovider.model.managers.ServiceProviderManager;
import dimitri.suls.allshare.serviceprovider.model.managers.ServiceProviderObserver;
import dimitri.suls.allshare.tv.listeners.TVTouchListener;

public class Main extends Activity {
	private ServiceProviderManager serviceProviderManager = null;
	private DeviceManager tvControllerDeviceManager = null;
	private DeviceManager avPlayerDeviceManager = null;
	private DeviceManager imageViewerDeviceManager = null;
	private DeviceFrontendManager tvControllerDeviceFrontendManager = null;
	private DeviceFrontendManager avPlayerDeviceFrontendManager = null;
	private DeviceFrontendManager imageViewerDeviceFrontendManager = null;
	// TODO: Reference needed for MediaFrontendManagers?
	private MediaFrontendManager songsFrontendManager = null;
	private MediaFrontendManager videosFrontendManager = null;
	private MediaManager mediaManager = null;
	private EditText editTextBrowseTerm = null;
	private ListView listViewSongs = null;
	private ListView listViewVideos = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mediaManager = new MediaManager(this);

		try {
			serviceProviderManager = new ServiceProviderManager(this);
			serviceProviderManager.addObserver(new ServiceProviderObserver() {
				@Override
				public void createdServiceProvider() {
					initializeEachDeviceManager();

					initializeTVTouchListener();

					initializeEditTextBrowseTerm();

					initializeEachMediaList();
				}
			});
		} catch (Exception exception) {
			final Activity activityMain = this;
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();

			alertDialog.setCancelable(false);
			alertDialog.setMessage("Error occured: \r\n" + exception.getMessage());
			alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					activityMain.finish();
				}
			});

			alertDialog.show();
		}
	}

	@Override
	protected void onDestroy() {
		serviceProviderManager.close();

		super.onDestroy();
	}

	private void initializeEachDeviceManager() {
		TabManager tabManager = new TabManager(this);

		tvControllerDeviceManager = new DeviceManager(DeviceType.DEVICE_TV_CONTROLLER, serviceProviderManager);
		avPlayerDeviceManager = new DeviceManager(DeviceType.DEVICE_AVPLAYER, serviceProviderManager);
		imageViewerDeviceManager = new DeviceManager(DeviceType.DEVICE_IMAGEVIEWER, serviceProviderManager);

		ListView listViewTVControllers = (ListView) findViewById(R.id.listViewTVControllers);
		ListView listViewAVPlayers = (ListView) findViewById(R.id.listViewAVPlayers);
		ListView listViewImageViewers = (ListView) findViewById(R.id.listViewImageViewers);

		TextView textViewSelectedTVController = (TextView) findViewById(R.id.textViewSelectedTVController);
		TextView textViewSelectedAVPlayer = (TextView) findViewById(R.id.textViewSelectedAVPlayer);
		TextView textViewSelectedImageViewer = (TextView) findViewById(R.id.textViewSelectedImageViewer);

		tvControllerDeviceFrontendManager = new DeviceFrontendManager(this, tvControllerDeviceManager, listViewTVControllers,
				textViewSelectedTVController, "TV-controller", tabManager, 1);
		avPlayerDeviceFrontendManager = new DeviceFrontendManager(this, avPlayerDeviceManager, listViewAVPlayers, textViewSelectedAVPlayer,
				"AV-player", tabManager, 2);
		// TODO: Add tab for image-viewer
		imageViewerDeviceFrontendManager = new DeviceFrontendManager(this, imageViewerDeviceManager, listViewImageViewers,
				textViewSelectedImageViewer, "image-viewer", tabManager, 2);
	}

	// TODO: Add slider to adjust speed of motion-control.
	private void initializeTVTouchListener() {
		View tabTouch = findViewById(R.id.tabTVTouch);

		tabTouch.setOnTouchListener(new TVTouchListener(tvControllerDeviceManager));
	}

	private void initializeEditTextBrowseTerm() {
		editTextBrowseTerm = (EditText) findViewById(R.id.editTextBrowseTerm);
	}

	private void initializeEachMediaList() {
		listViewSongs = (ListView) findViewById(R.id.listViewSongs);
		listViewVideos = (ListView) findViewById(R.id.listViewVideos);

		songsFrontendManager = new MediaFrontendManager(this, listViewSongs, avPlayerDeviceManager, mediaManager, MediaType.AUDIO);
		videosFrontendManager = new MediaFrontendManager(this, listViewVideos, avPlayerDeviceManager, mediaManager, MediaType.VIDEO);
	}

	// TODO: Add button to disconnect from the selected device

	public void refreshDeviceListEvent(View view) {
		tvControllerDeviceFrontendManager.refreshDeviceList();
		avPlayerDeviceFrontendManager.refreshDeviceList();
		imageViewerDeviceFrontendManager.refreshDeviceList();
	}

	private void sendRemoteKey(final RemoteKey remoteKey) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.sendRemoteKey(remoteKey);
			}
		});
	}

	public void volumeUpKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_VOLUP);
	}

	public void volumeDownKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_VOLDOWN);
	}

	public void muteKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_MUTE);
	}

	public void channelUpKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_CHUP);
	}

	public void channelDownKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_CHDOWN);
	}

	public void preChKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_PRECH);
	}

	public void chListKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_CH_LIST);
	}

	public void arrowUpKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_UP);
	}

	public void arrowDownKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_DOWN);
	}

	public void arrowLeftKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_LEFT);
	}

	public void arrowRightKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_RIGHT);
	}

	public void enterKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_ENTER);
	}

	public void toolsKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_TOOLS);
	}

	public void infoKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_INFO);
	}

	public void returnKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_RETURN);
	}

	public void exitKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_EXIT);
	}

	public void smartHubKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_CONTENTS);
	}

	public void menuKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_MENU);
	}

	public void sourceKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_SOURCE);
	}

	public void powerOffKeyEvent(View view) {
		sendRemoteKey(RemoteKey.KEY_POWEROFF);
	}

	// TODO: Add more buttons for numeric keys/dash, play-keys, color-keys, ..

	public void openWebPageEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;
				String URL = editTextBrowseTerm.getText().toString();

				tvController.openWebPage(URL);
			}
		});
	}

	public void searchInternetEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;
				String searchTerm = editTextBrowseTerm.getText().toString();

				tvController.openWebPage("http://www.google.com/search?q=" + searchTerm);
			}
		});
	}

	public void sendKeyboardStringEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;
				String text = editTextBrowseTerm.getText().toString();

				tvController.sendKeyboardString(text);
				tvController.sendKeyboardEnd();
			}
		});
	}

	public void goHomePageEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.goHomePage();
			}
		});
	}

	public void closeWebPageEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.closeWebPage();
			}
		});
	}

	public void browserScrollUpEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.browserScrollUp();
			}
		});
	}

	public void browserScrollDownEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.browserScrollDown();
			}
		});
	}

	public void goPreviousWebPageEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.goPreviousWebPage();
			}
		});
	}

	public void goNextWebPageEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.goNextWebPage();
			}
		});
	}

	public void browserZoomInEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.browserZoomIn();
			}
		});
	}

	public void browserZoomOutEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.browserZoomOut();
			}
		});
	}

	public void browserZoomDefaultEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.browserZoomDefault();
			}
		});
	}

	public void refreshWebPageEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.refreshWebPage();
			}
		});
	}

	public void stopWebPageLoadingEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.stopWebPageLoading();
			}
		});
	}

	public void sendTouchClickEvent(View view) {
		tvControllerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				TVController tvController = (TVController) selectedDevice;

				tvController.sendTouchClick();
			}
		});
	}

	public void pauseMediaEvent(View view) {
		avPlayerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				AVPlayer avPlayer = (AVPlayer) selectedDevice;

				avPlayer.pause();
			}
		});
	}

	public void resumeMediaEvent(View view) {
		avPlayerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				AVPlayer avPlayer = (AVPlayer) selectedDevice;

				avPlayer.resume();
			}
		});
	}

	public void stopMediaEvent(View view) {
		avPlayerDeviceManager.execute(new DeviceCommand() {
			@Override
			public void execute(Device selectedDevice) {
				AVPlayer avPlayer = (AVPlayer) selectedDevice;

				avPlayer.stop();
			}
		});
	}
}