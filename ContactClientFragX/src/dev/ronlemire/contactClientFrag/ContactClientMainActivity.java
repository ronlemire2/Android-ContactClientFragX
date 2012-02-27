package dev.ronlemire.contactClientFrag;

//import android.app.Fragment;
//import android.app.FragmentManager;
//import android.app.FragmentTransaction;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import dev.ronlemire.contactClientFrag.PrefsDialogFragment.PrefsFragmentDialogFinishedListener;

public class ContactClientMainActivity extends ActionBarFragmentActivity
		implements PrefsFragmentDialogFinishedListener {
	public static final String TAG = "ContactClientFrag";
	public static final String SHARED_PREFERENCES_NAME = "prefs";
	public static final String MULTIPLE_CONTACT_URL_KEY = "multiple_contact_url";
	public static final String SINGLE_CONTACT_URL_KEY = "single_contact_url";
	private Fragment contactListFragment;
	private ContactListReceiver contactListReceiver;
	private ContactAddedReceiver contactAddedReceiver;
	private ContactUpdatedReceiver contactUpdatedReceiver;
	private ContactLoadedReceiver contactLoadedReceiver;
	private ContactDeletedReceiver contactDeletedReceiver;
	private IntentFilter contactListFilter;
	private IntentFilter contactAddedFilter;
	private IntentFilter contactUpdatedFilter;
	private IntentFilter contactLoadedFilter;
	private IntentFilter contactDeletedFilter;
	private static FragmentManager myFragmentManager;
	private static SharedPreferences prefs;
	private Contact contactLoaded = null;
	private String contactSelectedId;
	private Context mainContext;

	// *****************************************************************************
	// Activity Life Cycle Event Handlers
	// *****************************************************************************
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& isTablet()) {
			setContentView(R.layout.main_land);
		} else {
			setContentView(R.layout.main);
		}

		mainContext = this;
		myFragmentManager = getSupportFragmentManager();

		contactListReceiver = new ContactListReceiver();
		contactListFilter = new IntentFilter(
				ContactListFragment.CONTACT_SELECTED_INTENT);

		contactAddedReceiver = new ContactAddedReceiver();
		contactAddedFilter = new IntentFilter(
				ContactAddFragment.CONTACT_ADDED_INTENT);

		contactUpdatedReceiver = new ContactUpdatedReceiver();
		contactUpdatedFilter = new IntentFilter(
				ContactUpdateFragment.CONTACT_UPDATED_INTENT);

		contactLoadedReceiver = new ContactLoadedReceiver();
		contactLoadedFilter = new IntentFilter(
				ContactViewFragment.CONTACT_LOADED_INTENT);

		contactDeletedReceiver = new ContactDeletedReceiver();
		contactDeletedFilter = new IntentFilter(
				ContactDeleteFragment.CONTACT_DELETED_INTENT);

		prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

		CheckForURLs();
	}

	public OnClickListener refreshButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// CheckForURLs();
			StartContactListFragment();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(contactListReceiver, contactListFilter);
		registerReceiver(contactAddedReceiver, contactAddedFilter);
		registerReceiver(contactUpdatedReceiver, contactUpdatedFilter);
		registerReceiver(contactLoadedReceiver, contactLoadedFilter);
		registerReceiver(contactDeletedReceiver, contactDeletedFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(contactListReceiver);
		unregisterReceiver(contactAddedReceiver);
		unregisterReceiver(contactUpdatedReceiver);
		unregisterReceiver(contactLoadedReceiver);
		unregisterReceiver(contactDeletedReceiver);
	}

	// *****************************************************************************
	// Make sure URLs are setup before doing any REST calls
	// *****************************************************************************
	private void CheckForURLs() {
		if (prefs.getString(MULTIPLE_CONTACT_URL_KEY, null) == null
				|| prefs.getString(MULTIPLE_CONTACT_URL_KEY, "") == ""
				|| prefs.getString(SINGLE_CONTACT_URL_KEY, null) == null
				|| prefs.getString(SINGLE_CONTACT_URL_KEY, "") == "") {

			showPrefsFragmentDialog();
			Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG)
					.show();
		} else {
			StartContactListFragment();
		}
	}

	// *****************************************************************************
	// ActionBar
	// *****************************************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater(); // global MenuInflator
		inflater.inflate(R.menu.actionmenu, menu);

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	// when one of the items was clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			StartContactListFragment();
			StartContactEmptyFragment();
			contactLoaded = null;
			return true;
		case R.id.prefsItem:
			showPrefsFragmentDialog();
			return true;
		case R.id.addContactItem:
			StartContactAddFragment();
			return true;
		case R.id.editContactItem:
			StartContactUpdateFragment();
			return true;
		case R.id.deleteContactItem:
			StartContactDeleteFragment();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void StartContactAddFragment() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& isTablet()) {
			Fragment contactAddFragment = (Fragment) myFragmentManager
					.findFragmentById(R.id.contact_replacer);
			contactAddFragment = ContactAddFragment.newInstance("0");

			FragmentTransaction contactViewFragmentTransaction = myFragmentManager
					.beginTransaction();
			contactViewFragmentTransaction.replace(R.id.contact_replacer,
					contactAddFragment);
			contactViewFragmentTransaction.commit();
		} else {
			Intent intent = new Intent();
			intent.setClass(this, ContactClientDetailsActivity.class);
			intent.putExtra("fragmentName", "ContactAddFragment");
			startActivity(intent);
		}
	}

	private void StartContactDeleteFragment() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& isTablet()) {
			if (contactLoaded != null) {
				Fragment contactDeleteFragment = (Fragment) myFragmentManager
						.findFragmentById(R.id.contact_replacer);
				contactDeleteFragment = ContactDeleteFragment
						.newInstance(new Contact(contactLoaded.getId(),
								contactLoaded.getFirstName(), contactLoaded
										.getLastName(), contactLoaded
										.getEmail()));

				FragmentTransaction contactViewFragmentTransaction = myFragmentManager
						.beginTransaction();
				contactViewFragmentTransaction.replace(R.id.contact_replacer,
						contactDeleteFragment);
				contactViewFragmentTransaction.commit(); // begin the transition
			} else {
				Toast message = Toast.makeText(this,
						R.string.no_contact_selected, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(),
						message.getYOffset());
				message.show();
			}
		}
	}

	public void StartContactEmptyFragment() {
		Fragment contactEmptyFragment = (Fragment) myFragmentManager
				.findFragmentById(R.id.contact_replacer);
		contactEmptyFragment = ContactEmptyFragment.newInstance();

		FragmentTransaction contactViewFragmentTransaction = myFragmentManager
				.beginTransaction();
		contactViewFragmentTransaction.replace(R.id.contact_replacer,
				contactEmptyFragment);
		contactViewFragmentTransaction.commit(); // begin the transition
	}

	public void StartContactListFragment() {
		contactListFragment = (Fragment) getSupportFragmentManager()
				.findFragmentById(R.id.contactList_replacer);
		if (contactListFragment == null) {
			contactListFragment = ContactListFragment.newInstance();
			FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.contactList_replacer,
					contactListFragment);
			fragmentTransaction.commit();
		}
	}

	private void StartContactUpdateFragment() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& isTablet()) {
			if (contactLoaded != null) {
				Fragment contactUpdateFragment = (Fragment) myFragmentManager
						.findFragmentById(R.id.contact_replacer);
				contactUpdateFragment = ContactUpdateFragment
						.newInstance(new Contact(contactLoaded.getId(),
								contactLoaded.getFirstName(), contactLoaded
										.getLastName(), contactLoaded
										.getEmail()));

				FragmentTransaction contactViewFragmentTransaction = myFragmentManager
						.beginTransaction();
				contactViewFragmentTransaction.replace(R.id.contact_replacer,
						contactUpdateFragment);
				contactViewFragmentTransaction.commit(); // begin the transition
			} else {
				Toast message = Toast.makeText(this,
						R.string.no_contact_selected, Toast.LENGTH_SHORT);
				message.setGravity(Gravity.CENTER, message.getXOffset(),
						message.getYOffset());
				message.show();
			}
		}
	}

	// *****************************************************************************
	// SharedPreferences
	// *****************************************************************************
	public static void showPrefsFragmentDialog() {
		PrefsDialogFragment prefsDialogFragment = PrefsDialogFragment
				.newInstance(prefs.getString(MULTIPLE_CONTACT_URL_KEY, null),
						prefs.getString(SINGLE_CONTACT_URL_KEY, null));
		FragmentTransaction prefsFragmentTransition = myFragmentManager
				.beginTransaction();
		prefsDialogFragment.show(ContactClientMainActivity.myFragmentManager, "");
	}

	@Override
	public void onDialogFinished(String multipleContactURL,
			String singleContactURL) {
		setURLs(multipleContactURL, singleContactURL);
		StartContactListFragment();
	}

	// set URLs in SharedPreference
	public void setURLs(String multipleContactURL, String singleContactURL) {
		SharedPreferences.Editor prefsEditor = prefs.edit();
		prefsEditor.putString(MULTIPLE_CONTACT_URL_KEY, multipleContactURL);
		prefsEditor.putString(SINGLE_CONTACT_URL_KEY, singleContactURL);
		prefsEditor.commit(); // commit the changes
	}

	// *****************************************************************************
	// Helper methods
	// *****************************************************************************
	private boolean isTablet() {
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);

		int width = displayMetrics.widthPixels / displayMetrics.densityDpi;
		int height = displayMetrics.heightPixels / displayMetrics.densityDpi;

		double screenDiagonal = Math.sqrt(width * width + height * height);
		return (screenDiagonal >= 8.5);
	}

	// *****************************************************************************
	// BroadcastReceivers
	// *****************************************************************************
	class ContactLoadedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			contactLoaded = (Contact) intent
					.getSerializableExtra(ContactViewFragment.CONTACT_LOADED);
			Log.d("ContactLoadedReceiver",
					"Selected Id: " + contactLoaded.getId());
		}
	}

	class ContactListReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			contactSelectedId = intent
					.getStringExtra(ContactListFragment.CONTACT_SELECTED_ID);
			Log.d("ContactListReceiver", "Selected Id: " + contactSelectedId);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
					&& isTablet()) {
				Fragment contactViewFragment = (Fragment) myFragmentManager
						.findFragmentById(R.id.contact_replacer);
				contactViewFragment = ContactViewFragment
						.newInstance(contactSelectedId);
				FragmentTransaction contactViewFragmentTransaction = myFragmentManager
						.beginTransaction();
				contactViewFragmentTransaction.replace(R.id.contact_replacer,
						contactViewFragment);
				contactViewFragmentTransaction.commit(); // begin the transition
			} else {
				Intent viewIntent = new Intent();
				viewIntent.setClass(mainContext,
						ContactClientDetailsActivity.class);
				viewIntent.putExtra("fragmentName", "ContactViewFragment");
				viewIntent.putExtra("contactSelectedId", contactSelectedId);
				startActivity(viewIntent);
			}
		}
	}

	class ContactAddedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newContactId = intent
					.getStringExtra(ContactAddFragment.CONTACT_ID);
			Log.d("ContactAddedReceiver", "New Contact Id: " + newContactId);

			// get the current visible ForecastFragment
			Fragment contactListFragment = (Fragment) myFragmentManager
					.findFragmentById(R.id.contactList_replacer);
			contactListFragment = ContactListFragment.newInstance();

			// create a new FragmentTransaction
			FragmentTransaction contactViewFragmentTransaction = myFragmentManager
					.beginTransaction();

			// Refresh ContactList with new contact
			contactViewFragmentTransaction.replace(R.id.contactList_replacer,
					contactListFragment);

			contactViewFragmentTransaction.commit(); // begin the transition

			StartContactEmptyFragment();
			contactLoaded = null;
		}
	}

	class ContactUpdatedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String existingContactId = intent
					.getStringExtra(ContactUpdateFragment.CONTACT_UPDATED_ID);
			Log.d("ContactUpdatedReceiver", "Existing Contact Id: "
					+ existingContactId);

			// get the current visible ForecastFragment
			Fragment contactListFragment = (Fragment) myFragmentManager
					.findFragmentById(R.id.contactList_replacer);
			contactListFragment = ContactListFragment.newInstance();

			// create a new FragmentTransaction
			FragmentTransaction contactViewFragmentTransaction = myFragmentManager
					.beginTransaction();

			// Refresh ContactList with new contact
			contactViewFragmentTransaction.replace(R.id.contactList_replacer,
					contactListFragment);

			contactViewFragmentTransaction.commit(); // begin the transition

			StartContactEmptyFragment();
			contactLoaded = null;
		}
	}

	class ContactDeletedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String deleteContactId = intent
					.getStringExtra(ContactDeleteFragment.CONTACT_DELETED_ID);
			Log.d("ContactDeletedReceiver", "Delete Contact Id: "
					+ deleteContactId);

			// get the current visible ForecastFragment
			Fragment contactListFragment = (Fragment) myFragmentManager
					.findFragmentById(R.id.contactList_replacer);
			contactListFragment = ContactListFragment.newInstance();

			// create a new FragmentTransaction
			FragmentTransaction contactViewFragmentTransaction = myFragmentManager
					.beginTransaction();

			// Refresh ContactList with new contact
			contactViewFragmentTransaction.replace(R.id.contactList_replacer,
					contactListFragment);

			contactViewFragmentTransaction.commit(); // begin the transition

			StartContactEmptyFragment();
			contactLoaded = null;

		}
	}
}
